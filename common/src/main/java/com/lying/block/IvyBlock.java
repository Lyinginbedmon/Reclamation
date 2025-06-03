package com.lying.block;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.google.common.collect.Lists;
import com.mojang.serialization.MapCodec;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.ConnectingBlock;
import net.minecraft.block.MultifaceBlock;
import net.minecraft.block.ShapeContext;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.WorldView;
import net.minecraft.world.tick.ScheduledTickView;

public class IvyBlock extends Block implements IFaceBlock, IDeActivatable
{
	public static final MapCodec<IvyBlock> CODEC	= createCodec(IvyBlock::new);
	public static final BooleanProperty
		UP = ConnectingBlock.UP,
		NORTH = ConnectingBlock.NORTH,
		SOUTH = ConnectingBlock.SOUTH,
		EAST = ConnectingBlock.EAST,
		WEST = ConnectingBlock.WEST;
	private static final VoxelShape 
		UP_SHAPE = Block.createCuboidShape(0.0, 15.0, 0.0, 16.0, 16.0, 16.0),
		EAST_SHAPE = Block.createCuboidShape(0.0, 0.0, 0.0, 1.0, 16.0, 16.0),
		WEST_SHAPE = Block.createCuboidShape(15.0, 0.0, 0.0, 16.0, 16.0, 16.0),
		NORTH_SHAPE = Block.createCuboidShape(0.0, 0.0, 15.0, 16.0, 16.0, 16.0),
		SOUTH_SHAPE = Block.createCuboidShape(0.0, 0.0, 0.0, 16.0, 16.0, 1.0);
	public static final Map<Direction, BooleanProperty> FACING_PROPERTIES = ConnectingBlock.FACING_PROPERTIES
		.entrySet().stream().filter(e -> e.getKey() != Direction.DOWN).collect(Util.toMap());
	private final Map<BlockState, VoxelShape> shapesByState;
	
	private final List<GrowthOption> growOptions = Lists.newArrayList();
	
	public MapCodec<IvyBlock> getCodec() { return CODEC; }
	
	public IvyBlock(Settings settings)
	{
		super(settings.ticksRandomly());
		setDefaultState(getStateManager().getDefaultState()
			.with(INERT, false)
			.with(UP, false)
			.with(NORTH, false)
			.with(SOUTH, false)
			.with(EAST, false)
			.with(WEST, false));
		
		shapesByState = Collections.unmodifiableMap(
				getStateManager().getStates().stream()
					.collect(Collectors.toMap(Function.identity(), IvyBlock::getShapeForState)));
		
		FACING_PROPERTIES.keySet().forEach(d -> 
		{
			growOptions.add(growOnFace(d));
			growOptions.add(growInDirection(d));
			
			if(d.getHorizontalQuarterTurns() >= 0)
			{
				growOptions.add(growClockwise(d));
				growOptions.add(growCounterClockwise(d));
			}
		});
	}
	
	public BlockState getInitialState() { return this.getDefaultState(); }
	
	public List<GrowthOption> getGrowthOptions() { return this.growOptions; }
	
	public BooleanProperty getFacingProperty(Direction face) { return FACING_PROPERTIES.get(face); }
	
	/** Returns true if ivy can grow on the given interior side of the block */
	public boolean shouldHaveSide(BlockView world, BlockPos pos, Direction side)
	{
		BlockPos offset = pos.offset(side);
		return getFacingProperty(side) != null && shouldConnectTo(world, offset, side);
	}
	
	protected void appendProperties(StateManager.Builder<Block, BlockState> builder)
	{
		builder.add(INERT, UP, NORTH, EAST, SOUTH, WEST);
	}
	
	private static VoxelShape getShapeForState(BlockState state)
	{
		VoxelShape shape = VoxelShapes.empty();
		if(state.get(UP))
			shape = UP_SHAPE;
		if(state.get(NORTH))
			shape = VoxelShapes.union(shape, SOUTH_SHAPE);
		if(state.get(SOUTH))
			shape = VoxelShapes.union(shape, NORTH_SHAPE);
		if(state.get(EAST))
			shape = VoxelShapes.union(shape, WEST_SHAPE);
		if(state.get(WEST))
			shape = VoxelShapes.union(shape, EAST_SHAPE);
		return shape.isEmpty() ? VoxelShapes.fullCube() : shape;
	}
	
	private BlockState getPlacementShape(BlockState state, BlockView world, BlockPos pos)
	{
		BlockPos above = pos.up();
		if(state.get(UP))
			state = state.with(UP, shouldConnectTo(world, above, Direction.DOWN));
		
		BlockState blockState = null;
		for(Direction d : Direction.Type.HORIZONTAL)
		{
			BooleanProperty property = getFacingProperty(d);
			if(state.get(property))
			{
				boolean flag = shouldHaveSide(world, pos, d);
				if(!flag)
				{
					if(blockState == null)
						blockState = world.getBlockState(above);
					flag = blockState.isOf(this) && blockState.get(property);
				}
				state = state.with(property, flag);
			}
		}
		
		return state;
	}
	
	protected BlockState getStateForNeighborUpdate(BlockState state, WorldView world, ScheduledTickView tickView, BlockPos pos, Direction direction, BlockPos neighbourPos, BlockState neighbourState, Random random)
	{
		// Ignore if we can't be connected in that direction
		if(!FACING_PROPERTIES.containsKey(direction))
			return state;
		// Ignore if we aren't attached to that direction
		else if(!state.get(getFacingProperty(direction)))
			return state;
		// If we shouldn't be attached to that direction, update accordingly
		else if(!shouldHaveSide(world, pos, direction))
		{
			state = state.with(getFacingProperty(direction), false);
			if(FACING_PROPERTIES.values().stream().noneMatch(state::get))
				return Blocks.AIR.getDefaultState();
		}
		return state;
	}
	
	protected boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos)
	{
		return hasAdjacentBlocks(getPlacementShape(state, world, pos));
	}
	
	private boolean hasAdjacentBlocks(BlockState state)
	{
		return state != null && FACING_PROPERTIES.keySet().stream().anyMatch(d -> state.get(getFacingProperty(d)));
	}
	
	private int getAdjacentBlockCount(BlockState state)
	{
		return (int)FACING_PROPERTIES.values().stream().filter(state::get).count();
	}
	
	/** Returns true if ivy can grown on the given face of the given position */
	public static boolean shouldConnectTo(BlockView world, BlockPos pos, Direction direction)
	{
		return MultifaceBlock.canGrowOn(world, direction, pos, world.getBlockState(pos));
	}
	
	protected VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context)
	{
		return this.shapesByState.get(state);
	}
	
	protected boolean isTransparent(BlockState state) { return true; }
	
	protected boolean canReplace(BlockState state, ItemPlacementContext context)
	{
		BlockState blockState = context.getWorld().getBlockState(context.getBlockPos());
		return blockState.isOf(this) ? getAdjacentBlockCount(blockState) < FACING_PROPERTIES.size() : super.canReplace(state, context);
	}
	
	public BlockState getPlacementState(ItemPlacementContext context)
	{
		BlockState state = context.getWorld().getBlockState(context.getBlockPos());
		boolean isIvy = state.isOf(this);
		BlockState state2 = isIvy ? state : getDefaultState();
		for(Direction face : context.getPlacementDirections())
			if(face != Direction.DOWN)
			{
				BooleanProperty property = getFacingProperty(face);
				if(!(isIvy && state.get(property)) && shouldHaveSide(context.getWorld(), context.getBlockPos(), face))
					return state2.with(property, true);
			}
		return isIvy ? state2 : null;
	}
	
	protected void randomTick(BlockState state, ServerWorld world, BlockPos pos, Random random)
	{
		if(!isInert(state))
			applyGrowth(state, world, pos, random);
	}
	
	protected BlockState rotate(BlockState state, BlockRotation rotation)
	{
		switch(rotation)
		{
			case CLOCKWISE_180:
				return state
					.with(NORTH, state.get(SOUTH))
					.with(EAST, state.get(WEST))
					.with(SOUTH, state.get(NORTH))
					.with(WEST, state.get(EAST));
			case CLOCKWISE_90:
				return state
					.with(NORTH, state.get(WEST))
					.with(EAST, state.get(NORTH))
					.with(SOUTH, state.get(EAST))
					.with(WEST, state.get(SOUTH));
			case COUNTERCLOCKWISE_90:
				return state
					.with(NORTH, state.get(EAST))
					.with(EAST, state.get(SOUTH))
					.with(SOUTH, state.get(WEST))
					.with(WEST, state.get(NORTH));
			default:
				return state;
		}
	}
	
	protected BlockState mirror(BlockState state, BlockMirror mirror)
	{
		switch(mirror)
		{
			case FRONT_BACK:
				return state
					.with(NORTH, state.get(SOUTH))
					.with(SOUTH, state.get(NORTH));
			case LEFT_RIGHT:
				return state
					.with(EAST, state.get(WEST))
					.with(WEST, state.get(EAST));
			default:
				return super.mirror(state, mirror);	
		}
	}
	
}
