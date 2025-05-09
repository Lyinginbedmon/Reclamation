package com.lying.block;

import java.util.Collections;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.mojang.serialization.MapCodec;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.ConnectingBlock;
import net.minecraft.block.MultifaceBlock;
import net.minecraft.block.ShapeContext;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.WorldView;
import net.minecraft.world.tick.ScheduledTickView;

public class SootBlock extends Block
{
	public static final MapCodec<SootBlock> CODEC	= createCodec(SootBlock::new);
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
	
	public MapCodec<SootBlock> getCodec() { return CODEC; }
	
	public SootBlock(Settings settings)
	{
		super(settings);
		setDefaultState(getStateManager().getDefaultState()
				.with(UP, false)
				.with(DOWN, false)
				.with(NORTH, false)
				.with(SOUTH, false)
				.with(EAST, false)
				.with(WEST, false));
		
		shapesByState = Collections.unmodifiableMap(
				getStateManager().getStates().stream()
					.collect(Collectors.toMap(Function.identity(), SootBlock::getShapeForState)));
	}
	
	protected void appendProperties(StateManager.Builder<Block, BlockState> builder)
	{
		builder.add(UP, DOWN, NORTH, EAST, SOUTH, WEST);
	}
	
	protected static BooleanProperty getFacingProperty(Direction face) { return FACING_PROPERTIES.get(face); }
	
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
}
