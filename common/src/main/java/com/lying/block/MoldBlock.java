package com.lying.block;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.jetbrains.annotations.Nullable;

import com.google.common.collect.Lists;
import com.lying.data.RCTags;
import com.mojang.serialization.MapCodec;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.ConnectingBlock;
import net.minecraft.block.MultifaceBlock;
import net.minecraft.block.ShapeContext;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.LightType;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;
import net.minecraft.world.tick.ScheduledTickView;

public class MoldBlock extends Block implements IFaceBlock, IDeActivatable
{
	public static final MapCodec<MoldBlock> CODEC	= createCodec(MoldBlock::new);
	public static final BooleanProperty
		UP = ConnectingBlock.UP,
		DOWN = ConnectingBlock.DOWN,
		NORTH = ConnectingBlock.NORTH,
		SOUTH = ConnectingBlock.SOUTH,
		EAST = ConnectingBlock.EAST,
		WEST = ConnectingBlock.WEST;
	private static final VoxelShape 
		UP_SHAPE = Block.createCuboidShape(0.0, 15.0, 0.0, 16.0, 16.0, 16.0),
		DOWN_SHAPE = Block.createCuboidShape(0.0, 0.0, 0.0, 16.0, 1.0, 16.0),
		EAST_SHAPE = Block.createCuboidShape(0.0, 0.0, 0.0, 1.0, 16.0, 16.0),
		WEST_SHAPE = Block.createCuboidShape(15.0, 0.0, 0.0, 16.0, 16.0, 16.0),
		NORTH_SHAPE = Block.createCuboidShape(0.0, 0.0, 15.0, 16.0, 16.0, 16.0),
		SOUTH_SHAPE = Block.createCuboidShape(0.0, 0.0, 0.0, 16.0, 16.0, 1.0);
	public static final Map<Direction, BooleanProperty> FACING_PROPERTIES = ConnectingBlock.FACING_PROPERTIES
		.entrySet().stream().collect(Util.toMap());
	private final Map<BlockState, VoxelShape> shapesByState;

	private final List<GrowthOption> GROW_OPTIONS = Lists.newArrayList();
	
	public MapCodec<MoldBlock> getCodec() { return CODEC; }
	
	public MoldBlock(Settings settings)
	{
		super(settings.ticksRandomly());
		setDefaultState(getStateManager().getDefaultState()
				.with(INERT, false)
				.with(UP, false)
				.with(DOWN, false)
				.with(NORTH, false)
				.with(SOUTH, false)
				.with(EAST, false)
				.with(WEST, false));
		
		shapesByState = Collections.unmodifiableMap(
				getStateManager().getStates().stream()
					.collect(Collectors.toMap(Function.identity(), MoldBlock::getShapeForState)));
		
		FACING_PROPERTIES.keySet().forEach(d -> 
		{
			GROW_OPTIONS.add(growOnFace(d));
			GROW_OPTIONS.add(growInDirection(d));
			
			if(d.getHorizontalQuarterTurns() >= 0)
			{
				GROW_OPTIONS.add(growClockwise(d));
				GROW_OPTIONS.add(growCounterClockwise(d));
			}
		});
	}
	
	public BlockState getInitialState() { return getDefaultState(); }
	
	public List<GrowthOption> getGrowthOptions() { return GROW_OPTIONS; }
	
	protected void appendProperties(StateManager.Builder<Block, BlockState> builder)
	{
		builder.add(INERT, UP, DOWN, NORTH, EAST, SOUTH, WEST);
	}
	
	@Nullable
	public BooleanProperty getFacingProperty(Direction face) { return FACING_PROPERTIES.get(face); }
	
	/** Returns true if ivy can grown on the given face of the given position */
	public static boolean shouldConnectTo(BlockView world, BlockPos pos, Direction direction)
	{
		return !world.getBlockState(pos).isIn(RCTags.MOLD_IMPERVIOUS) && MultifaceBlock.canGrowOn(world, direction, pos, world.getBlockState(pos));
	}
	
	/** Returns true if ivy can grow on the given interior side of the block */
	public boolean shouldHaveSide(BlockView world, BlockPos pos, Direction side)
	{
		BlockPos offset = pos.offset(side);
		return FACING_PROPERTIES.containsKey(side) && shouldConnectTo(world, offset, side);
	}
	
	private static int getAdjacentBlockCount(BlockState state)
	{
		return (int)FACING_PROPERTIES.values().stream().filter(state::get).count();
	}
	
	public BlockState getPlacementState(ItemPlacementContext context)
	{
		BlockState state = context.getWorld().getBlockState(context.getBlockPos());
		boolean isSoot = state.isOf(this);
		BlockState state2 = isSoot ? state : getDefaultState();
		for(Direction face : context.getPlacementDirections())
		{
			BooleanProperty property = getFacingProperty(face);
			if(!(isSoot && state.get(property)) && shouldHaveSide(context.getWorld(), context.getBlockPos(), face))
				return state2.with(property, true);
		}
		return isSoot ? state2 : null;
	}
	
	protected boolean canReplace(BlockState state, ItemPlacementContext context)
	{
		BlockState blockState = context.getWorld().getBlockState(context.getBlockPos());
		return blockState.isOf(this) ? getAdjacentBlockCount(blockState) < FACING_PROPERTIES.size() : super.canReplace(state, context);
	}
	
	private static VoxelShape getShapeForState(BlockState state)
	{
		if(getAdjacentBlockCount(state) == FACING_PROPERTIES.size())
			return VoxelShapes.fullCube();
		
		VoxelShape shape = VoxelShapes.empty();
		if(state.get(UP))
			shape = UP_SHAPE;
		if(state.get(DOWN))
			shape = VoxelShapes.union(shape, DOWN_SHAPE);
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
	
	protected VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context)
	{
		return this.shapesByState.get(state);
	}
	
	protected BlockState getStateForNeighborUpdate(BlockState state, WorldView world, ScheduledTickView tickView, BlockPos pos, Direction direction, BlockPos neighbourPos, BlockState neighbourState, Random random)
	{
		// Ignore if we aren't attached to that direction
		if(!state.get(getFacingProperty(direction)))
			return state;
		// If we shouldn't be attached to that direction, update accordingly
		else if(!shouldHaveSide(world, pos, direction))
		{
			state = state.with(getFacingProperty(direction), false);
			if(getAdjacentBlockCount(state) == 0)
				return Blocks.AIR.getDefaultState();
		}
		return state;
	}
	
	protected void randomTick(BlockState state, ServerWorld world, BlockPos pos, Random random)
	{
		if(isInert(state))
			return;
		
		if(world.getLightLevel(LightType.SKY, pos) > 4 || world.getLightLevel(LightType.BLOCK, pos) > 8)
		{
			if(random.nextInt(6) == 0)
				world.setBlockState(pos, Blocks.AIR.getDefaultState(), 2);
		}
		else
			applyGrowth(state, world, pos, random);
	}
	
	public void randomDisplayTick(BlockState state, World world, BlockPos pos, Random random)
	{
		super.randomDisplayTick(state, world, pos, random);
		if(random.nextInt(3) == 0)
		{
			List<Direction> faces = FACING_PROPERTIES.entrySet().stream().filter(e -> state.get(e.getValue())).map(Entry::getKey).toList();
			if(faces.isEmpty())
				return;
			
			Direction face = faces.size() == 1 ? faces.get(0) : faces.get(random.nextInt(faces.size()));
			
			double x = 0D, y = 0D, z = 0D;
			switch(face.getAxis())
			{
				case X:
					y = random.nextDouble();
					z = random.nextDouble();
					x = 0.5D + face.getOffsetX() * 0.5D;
					break;
				case Y:
					x = random.nextDouble();
					z = random.nextDouble();
					y = 0.5D + face.getOffsetY() * 0.5D;
					break;
				case Z:
					x = random.nextDouble();
					y = random.nextDouble();
					z = 0.5D + face.getOffsetZ() * 0.5D;
					break;
			}
			
			Direction move = face.getOpposite();
			double speed = 0.1D;
			world.addParticle(ParticleTypes.MYCELIUM, (double)pos.getX() + x, (double)pos.getY() + y, (double)pos.getZ() + z, move.getOffsetX() * speed, move.getOffsetY() * speed, move.getOffsetZ() * speed);
		}
	}
}
