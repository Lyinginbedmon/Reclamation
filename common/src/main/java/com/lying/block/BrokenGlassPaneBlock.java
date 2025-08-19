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
import net.minecraft.block.PaneBlock;
import net.minecraft.block.ShapeContext;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;
import net.minecraft.world.tick.ScheduledTickView;

public class BrokenGlassPaneBlock extends AbstractBrokenGlassBlock
{
	public static final MapCodec<BrokenGlassPaneBlock> CODEC	= createCodec(BrokenGlassPaneBlock::new);
	public static final BooleanProperty
		NORTH = ConnectingBlock.NORTH,
		SOUTH = ConnectingBlock.SOUTH,
		EAST = ConnectingBlock.EAST,
		WEST = ConnectingBlock.WEST;
	private static final VoxelShape 
		EAST_SHAPE = Block.createCuboidShape(0, 0, 7, 3, 16, 9),
		WEST_SHAPE = Block.createCuboidShape(13, 0, 7, 16, 16, 9),
		NORTH_SHAPE = Block.createCuboidShape(7, 0, 13, 9, 16, 16),
		SOUTH_SHAPE = Block.createCuboidShape(7, 0, 0, 9, 16, 3);
	public static final Map<Direction, BooleanProperty> FACING_PROPERTIES = ConnectingBlock.FACING_PROPERTIES
		.entrySet().stream().filter(d -> d.getKey().getAxis() != Axis.Y).collect(Util.toMap());
	private final Map<BlockState, VoxelShape> shapesByState;
	
	public BrokenGlassPaneBlock(Settings settings)
	{
		super(settings);
		setDefaultState(getStateManager().getDefaultState()
				.with(NORTH, false)
				.with(SOUTH, false)
				.with(EAST, false)
				.with(WEST, false));
		
		shapesByState = Collections.unmodifiableMap(
				getStateManager().getStates().stream()
					.collect(Collectors.toMap(Function.identity(), BrokenGlassPaneBlock::getShapeForState)));
	}
	
	protected MapCodec<? extends BrokenGlassPaneBlock> getCodec() { return CODEC; }
	
	protected void appendProperties(StateManager.Builder<Block, BlockState> builder)
	{
		builder.add(NORTH, EAST, SOUTH, WEST);
	}
	
	private static VoxelShape getShapeForState(BlockState state)
	{
		VoxelShape shape = VoxelShapes.empty();
		if(state.get(NORTH))
			shape = SOUTH_SHAPE;
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
	
	protected boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos)
	{
		return FACING_PROPERTIES.keySet().stream().anyMatch(face -> shouldConnectTo(world.getBlockState(pos.offset(face)), world, pos.offset(face), face.getOpposite()));
	}
	
	public BlockState getPlacementState(ItemPlacementContext context)
	{
		World world = context.getWorld();
		BlockState state = getDefaultState();
		for(Direction face : FACING_PROPERTIES.keySet())
		{
			BlockPos neighbour = context.getBlockPos().offset(face);
			if(shouldConnectTo(world.getBlockState(neighbour), world, neighbour, face.getOpposite()))
				state = state.with(FACING_PROPERTIES.get(face), true);
		}
		return hasAnyFaces(state) ? state : Blocks.AIR.getDefaultState();
	}
	
	protected BlockState getStateForNeighborUpdate(BlockState state, WorldView world, ScheduledTickView tickView, BlockPos pos, Direction direction, BlockPos neighbourPos, BlockState neighbourState, Random random)
	{
		if(!FACING_PROPERTIES.containsKey(direction))
			return state;
		
		// Update the state with all valid connection faces
		state = state.with(FACING_PROPERTIES.get(direction), shouldConnectTo(neighbourState, world, neighbourPos, direction.getOpposite()));
		
		// If we have no connected faces, break
		if(!hasAnyFaces(state))
			return Blocks.AIR.getDefaultState();
		
		return state;
	}
	
	public static boolean shouldConnectTo(BlockState state, WorldView world, BlockPos pos, Direction onFace)
	{
		return !(
				state.isAir() || 
				state.getBlock() instanceof AbstractBrokenGlassBlock || 
				state.getCollisionShape(world, pos).isEmpty()) && 
				sideCoversSmallSquare(world, pos, onFace) && 
				((PaneBlock)Blocks.GLASS_PANE).connectsTo(state, state.isSideSolidFullSquare(world, pos, onFace));
	}
	
	public static boolean hasAnyFaces(BlockState state)
	{
		return FACING_PROPERTIES.values().stream().anyMatch(state::get);
	}
}
