package com.lying.block;

import java.util.EnumSet;
import java.util.List;

import com.google.common.collect.Lists;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.ShapeContext;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.IntProperty;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;
import net.minecraft.world.tick.ScheduledTickView;

public class RubbleBlock extends Block
{
	public static final int DELAY = 20;
	public static final IntProperty DEPTH = IntProperty.of("depth", 1, 4);
	protected static final VoxelShape SHAPE = Block.createCuboidShape(1.0, 0.0, 1.0, 15.0, 5.0, 15.0);
	protected static final VoxelShape[] LAYERS_TO_SHAPE = new VoxelShape[] 
			{
				Block.createCuboidShape(1.0, 0.0, 1.0, 15.0, 4.0, 15.0),
				Block.createCuboidShape(0.0, 0.0, 0.0, 16.0, 8.0, 16.0),
				Block.createCuboidShape(0.0, 0.0, 0.0, 16.0, 12.0, 16.0),
				Block.createCuboidShape(0.0, 0.0, 0.0, 16.0, 16.0, 16.0)
			};
	protected static final List<Direction> MOVES = Lists.newArrayList(Direction.Type.HORIZONTAL);
	
	public RubbleBlock(Settings settings)
	{
		super(settings);
		setDefaultState(getDefaultState().with(DEPTH, 1));
	}
	
	protected void appendProperties(StateManager.Builder<Block, BlockState> builder)
	{
		builder.add(DEPTH);
	}
	
	public static int layerIndex(BlockState state) { return state.get(DEPTH) - 1; }
	
	protected VoxelShape getInsideCollisionShape(BlockState state, World world, BlockPos pos) { return state.getOutlineShape(world, pos); }
	
	protected VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) { return LAYERS_TO_SHAPE[layerIndex(state)]; }
	
	protected VoxelShape getSidesShape(BlockState state, BlockView world, BlockPos pos) { return LAYERS_TO_SHAPE[layerIndex(state)]; }
	
	protected VoxelShape getCameraCollisionShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) { return LAYERS_TO_SHAPE[layerIndex(state)]; }
	
	public BlockState getPlacementState(ItemPlacementContext context)
	{
		return getPlacementShape(context.getBlockPos(), context.getWorld());
	}
	
	private BlockState getPlacementShape(BlockPos position, WorldView world)
	{
		BlockState state = world.getBlockState(position);
		if(state.isOf(this))
		{
			if(state.get(DEPTH) < 4)
				return state.with(DEPTH, state.get(DEPTH) + 1);
			else
				return null;
		}
		else
			return getDefaultState();
	}
	
	protected boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos)
	{
		BlockState stateAt = world.getBlockState(pos);
		if(stateAt.isOf(this) && getPlacementShape(pos, world) != null)
			return true;
		
		BlockPos below = pos.down();
		BlockState surface = world.getBlockState(below);
		return Block.isFaceFullSquare(surface.getSidesShape(world, below), Direction.UP);
	}
	
	protected boolean canReplace(BlockState state, ItemPlacementContext context)
	{
		return state.isReplaceable() && context.canReplaceExisting() || state.isOf(this) && state.get(DEPTH) < 4;
	}
	
	protected void onBlockAdded(BlockState state, World world, BlockPos pos, BlockState oldState, boolean notify)
	{
		world.scheduleBlockTick(pos, this, DELAY);
	}
	
	protected BlockState getStateForNeighborUpdate(
			BlockState state,
			WorldView world,
			ScheduledTickView tickView,
			BlockPos pos,
			Direction direction,
			BlockPos neighborPos,
			BlockState neighborState,
			Random random
			)
	{
		tickView.scheduleBlockTick(pos, this, DELAY);
		return super.getStateForNeighborUpdate(state, world, tickView, pos, direction, neighborPos, neighborState, random);
	}
	
	private static EnumSet<Direction> randomSequence(Random random)
	{
		EnumSet<Direction> sequence = EnumSet.noneOf(Direction.class);
		List<Direction> options = Lists.newArrayList(MOVES.toArray(new Direction[0]));
		while(!options.isEmpty())
			sequence.add(options.remove(random.nextInt(options.size())));
		return sequence;
	}
	
	protected void scheduledTick(BlockState state, ServerWorld world, BlockPos pos, Random random)
	{
		if(state.get(DEPTH) == 1 && !canAddTo(world.getBlockState(pos.down())))
			return;
		
		BlockState myState = tryEqualise(pos, pos.down(), state, world, true);
		for(Direction side : randomSequence(random))
			myState = tryEqualise(pos, pos.offset(side), myState, world, false);
		
		if(state != myState)
			world.setBlockState(pos, myState, 2);
	}
	
	private boolean canAddTo(BlockState state)
	{
		return state.isReplaceable() || state.isOf(this) && state.get(DEPTH) < 4;
	}
	
	private static boolean canStack(BlockState from, BlockState to, boolean forceTransfer)
	{
		int fromD = from.get(DEPTH);
		int toD = to.get(DEPTH);
		return toD < 4 && (forceTransfer || fromD >= toD && (fromD - toD) > 1);
	}
	
	private BlockState tryEqualise(BlockPos myPos, BlockPos otherPos, BlockState myState, World world, boolean forceTransfer)
	{
		if(!myState.isOf(this))
			return myState;
		
		int depth = myState.get(DEPTH);
		if(!forceTransfer && depth == 1)
			return myState;
		
		BlockState otherState = world.getBlockState(otherPos);
		if(!canAddTo(otherState))
			return myState;
		
		if(otherState.isOf(this) && canStack(myState, otherState, forceTransfer))
		{
			int depth2 = otherState.get(DEPTH);
			int transfer = forceTransfer ? Math.min(depth, 4 - depth2) : 1;
			world.setBlockState(otherPos, otherState.with(DEPTH, depth2 + transfer), 2);
			depth -= transfer;
		}
		else if(otherState.isReplaceable())
		{
			world.setBlockState(otherPos, getDefaultState(), 2);
			--depth;
		}
		
		return depth < 1 ? Blocks.AIR.getDefaultState() : myState.with(DEPTH, depth);
	}
}
