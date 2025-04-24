package com.lying.block;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.google.common.collect.Lists;
import com.lying.init.RCBlocks;
import com.mojang.serialization.MapCodec;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.ConnectingBlock;
import net.minecraft.block.MultifaceBlock;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.SideShapeType;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.GameRules;
import net.minecraft.world.WorldView;
import net.minecraft.world.tick.ScheduledTickView;

public class IvyBlock extends Block
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
	
	private static List<GrowthOption> GROW_OPTIONS = Lists.newArrayList();
	static
	{
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
	
	public MapCodec<IvyBlock> getCodec() { return CODEC; }
	
	public IvyBlock(Settings settings)
	{
		super(settings.ticksRandomly());
		setDefaultState(getStateManager().getDefaultState()
			.with(UP, false)
			.with(NORTH, false)
			.with(SOUTH, false)
			.with(EAST, false)
			.with(WEST, false));
		
		shapesByState = Collections.unmodifiableMap(
				getStateManager().getStates().stream()
					.collect(Collectors.toMap(Function.identity(), IvyBlock::getShapeForState)));
	}
	
	protected void appendProperties(StateManager.Builder<Block, BlockState> builder)
	{
		builder.add(UP, NORTH, EAST, SOUTH, WEST);
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
			if(!FACING_PROPERTIES.values().stream().anyMatch(state::get))
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
		int tally = 0;
		for(BooleanProperty property : FACING_PROPERTIES.values())
			if(state.get(property))
				tally++;
		return tally;
	}
	
	/** Returns true if ivy can grown on the given face of the given position */
	public static boolean shouldConnectTo(BlockView world, BlockPos pos, Direction direction)
	{
		return MultifaceBlock.canGrowOn(world, direction, pos, world.getBlockState(pos));
	}
	
	/** Returns true if ivy can grow on the given interior side of the block */
	private static boolean shouldHaveSide(BlockView world, BlockPos pos, Direction side)
	{
		BlockPos offset = pos.offset(side);
		return FACING_PROPERTIES.containsKey(side) && shouldConnectTo(world, offset, side);
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
	
	protected static BooleanProperty getFacingProperty(Direction face) { return FACING_PROPERTIES.get(face); }
	
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
		if(!world.getGameRules().getBoolean(GameRules.DO_VINES_SPREAD) || random.nextInt(4) > 0)
			return;
		
		GROW_OPTIONS.stream().filter(g -> g.viable(state, pos, world)).findAny().ifPresent(g -> 
		{
//			Reclamation.LOGGER.info(" # Ivy growing according to {} at {}", g.name(), pos.toShortString());
			g.enact(state, pos, world); 
		});
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
	
	public static abstract class GrowthOption
	{
		private final String name;
		
		protected GrowthOption(String nameIn)
		{
			name = nameIn;
		}
		
		/** Utility, used for debugging */
		public final String name() { return name; }
		
		/** Returns true if this option is available for use in the given context */
		public abstract boolean viable(BlockState state, BlockPos pos, ServerWorld world);
		
		/** Applies this option to the given context */
		public abstract void enact(BlockState state, BlockPos pos, ServerWorld world);
	}
	
	public static GrowthOption growClockwise(Direction face)
	{
		return turn("grow_"+face.asString()+"_clockwise", face, Direction::rotateYClockwise);
	}
	
	public static GrowthOption growCounterClockwise(Direction face)
	{
		return turn("grow_"+face.asString()+"_counter_clockwise", face, Direction::rotateYCounterclockwise);
	}
	
	public static GrowthOption turn(String name, Direction face, Function<Direction,Direction> rotator)
	{
		return new GrowthOption(name)
		{
			public boolean viable(BlockState state, BlockPos pos, ServerWorld world)
			{
				if(!state.get(getFacingProperty(face)))
					return false;
				
				Direction side = rotator.apply(face.getOpposite());
				
				// Corner occlusion
				BlockPos cornerBlock = pos.offset(side);
				if(!world.isAir(cornerBlock))
				{
					BlockState corner = world.getBlockState(cornerBlock);
					if(corner.isSideSolid(world, cornerBlock, face, SideShapeType.FULL) || corner.isSideSolid(world, cornerBlock, side.getOpposite(), SideShapeType.FULL))
						return false;
				}
				
				// Target validity
				BlockPos targetBlock = pos.offset(face).offset(side);
				BlockState targetState = world.getBlockState(targetBlock);
				if(!(targetState.isAir() || targetState.isOf(state.getBlock()) && !targetState.get(getFacingProperty(side.getOpposite()))))
					return false;
				
				return shouldHaveSide(world, targetBlock, side.getOpposite());
			}
			
			public void enact(BlockState state, BlockPos pos, ServerWorld world)
			{
				Direction side = rotator.apply(face.getOpposite());
				BlockPos targetBlock = pos.offset(face).offset(side);
				BlockState stateAt = world.getBlockState(targetBlock);
				
				if(stateAt.isOf(state.getBlock()))
					stateAt = stateAt.with(getFacingProperty(side.getOpposite()), true);
				else
					stateAt = RCBlocks.IVY.get().getDefaultState().with(getFacingProperty(side.getOpposite()), true);
				
				world.setBlockState(targetBlock, stateAt, 2);
			}
		};
	}
	
	public static GrowthOption growInDirection(Direction direction)
	{
		return new GrowthOption("grow_"+direction.asString()) 
		{
			public boolean viable(BlockState state, BlockPos pos, ServerWorld world)
			{
				BlockPos offset = pos.offset(direction);
				/**
				 * Offset must be air
				 * State must not be attached in offset direction
				 * State must not be able to attach in offset direction
				 * Offset must have at least one matching attachment with current state
				 */
				return 
						world.isAir(offset) && 
						!state.get(getFacingProperty(direction)) && 
						!shouldHaveSide(world, pos, direction) && 
						Direction.Type.HORIZONTAL.stream().anyMatch(d -> state.get(getFacingProperty(d)) && shouldHaveSide(world, offset, d));
			}
			
			public void enact(BlockState state, BlockPos pos, ServerWorld world)
			{
				BlockPos offset = pos.offset(direction);
				Direction.Type.HORIZONTAL
					.stream().filter(d -> shouldHaveSide(world, offset, d))
					.map(IvyBlock::getFacingProperty).filter(state::get)
						.findAny().ifPresent(p -> world.setBlockState(offset, RCBlocks.IVY.get().getDefaultState().with(p, true), 2));
			}
		};
	}
	
	public static GrowthOption growOnFace(Direction direction)
	{
		return new GrowthOption("expand_to_"+direction.asString())
		{
			public boolean viable(BlockState state, BlockPos pos, ServerWorld world)
			{
				// Attachment must be available and not currently present in state
				if(state.get(getFacingProperty(direction)) || !shouldHaveSide(world, pos, direction))
					return false;
				
				// State has any horizontal and direction is vertical
				if(direction.getAxis() == Axis.Y && Direction.Type.HORIZONTAL.stream().map(IvyBlock::getFacingProperty).anyMatch(state::get))
					return true;
				else if(direction.getHorizontalQuarterTurns() >= 0)
				{
					// State has vertical and direction is horizontal
					if(state.get(getFacingProperty(Direction.UP)))
						return true;
					
					// Direction is adjacent to an existing horizontal
					if(Direction.Type.HORIZONTAL.stream().filter(d -> d == direction.rotateYClockwise() || d == direction.rotateYCounterclockwise()).map(IvyBlock::getFacingProperty).anyMatch(state::get))
						return true;
					
					// Direction is adjacent to another vine block with the same direction
					BlockState neighbour;
					if(
						(neighbour = world.getBlockState(pos.offset(direction.rotateYClockwise()))).isOf(state.getBlock()) && neighbour.get(getFacingProperty(direction)) ||
						(neighbour = world.getBlockState(pos.offset(direction.rotateYCounterclockwise()))).isOf(state.getBlock()) && neighbour.get(getFacingProperty(direction))
						)
						return true;
				}
				return false;
			}
			
			public void enact(BlockState state, BlockPos pos, ServerWorld world) { world.setBlockState(pos, state.with(getFacingProperty(direction), true), 2); }
		};
	}
}
