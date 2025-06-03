package com.lying.block;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.CarpetBlock;
import net.minecraft.block.ShapeContext;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.IntProperty;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;

public class LeafPileBlock extends CarpetBlock implements IDeActivatable
{
	public static final BooleanProperty STEPPED_ON = BooleanProperty.of("trampled");
	public static final IntProperty LAYERS = IntProperty.of("layers", 1, 3);
	public static final List<Block> LEAF_PILES = Lists.newArrayList();
	public static final Map<Block, Block> LEAF_PILE_TO_LEAVES = new HashMap<>();
	
	protected static final VoxelShape[] LAYERS_TO_SHAPE = new VoxelShape[] 
			{
				Block.createCuboidShape(0.0, 0.0, 0.0, 16.0, 1.0, 16.0),
				Block.createCuboidShape(0.0, 0.0, 0.0, 16.0, 4.0, 16.0),
				Block.createCuboidShape(0.0, 0.0, 0.0, 16.0, 6.0, 16.0)
			};
	
	protected final Block parentLeaf;
	
	public LeafPileBlock(Block parentLeafIn, Settings settings)
	{
		super(settings);
		this.setDefaultState(getDefaultState().with(LAYERS, 1).with(STEPPED_ON, false).with(INERT, false));
		parentLeaf = parentLeafIn;
		LEAF_PILES.add(this);
		LEAF_PILE_TO_LEAVES.put(this, parentLeaf);
	}
	
	protected void appendProperties(StateManager.Builder<Block, BlockState> builder)
	{
		builder.add(LAYERS, STEPPED_ON, INERT);
	}
	
	public final Block leaves() { return parentLeaf; }
	
	protected boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos)
	{
		BlockState stateAt = world.getBlockState(pos);
		if(stateAt.isOf(this) && getPlacementShape(pos, world) != null)
			return true;
		
		BlockPos below = pos.down();
		BlockState surface = world.getBlockState(below);
		return Block.isFaceFullSquare(surface.getSidesShape(world, below), Direction.UP);
	}
	
	public static int layerIndex(BlockState state) { return state.get(LAYERS) - 1; }
	
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
			if(state.get(LAYERS) < 3)
				return state.with(LAYERS, state.get(LAYERS) + 1);
			else
				return null;
		}
		else
			return getDefaultState();
	}
	
	protected boolean canReplace(BlockState state, ItemPlacementContext context)
	{
		return super.canReplace(state, context) || !context.shouldCancelInteraction() && context.getStack().isOf(this.asItem()) && state.isOf(this) && state.get(LAYERS) < 3;
	}
	
	public void onEntityCollision(BlockState state, World world, BlockPos pos, Entity entity)
	{
		super.onEntityCollision(state, world, pos, entity);
		if(!world.isClient && !(state.get(STEPPED_ON) || isInert(state)) && !entity.bypassesSteppingEffects())
		{
			Random rand = world.getRandom();
			for(Direction d : Direction.Type.HORIZONTAL.getShuffled(rand))
			{
				if(rand.nextInt(4) > 0)
					continue;
				
				BlockPos offset = pos.offset(d);
				BlockState neighbour = world.getBlockState(offset);
				if((neighbour.isAir() && getDefaultState().canPlaceAt(world, offset)) || (neighbour.isOf(this) && neighbour.get(LAYERS) < 3))
				{
					removeOrDecrementPile(world, pos);
					placeOrIncrementPile(world, offset);
					break;
				}
			}
			
			setSteppedOn(world, pos, true);
		}
	}
	
	private void removeOrDecrementPile(World world, BlockPos pos)
	{
		BlockState state = world.getBlockState(pos);
		int layers = layerIndex(state);
		world.setBlockState(pos, layers > 0 ? state.with(LAYERS, layers) : Blocks.AIR.getDefaultState(), 2);
	}
	
	private void placeOrIncrementPile(World world, BlockPos pos)
	{
		if(world.isAir(pos))
			world.setBlockState(pos, getDefaultState(), 2);
		else
		{
			BlockState state = world.getBlockState(pos);
			world.setBlockState(pos, state.with(LAYERS, state.get(LAYERS) + 1), 2);
		}
		
		setSteppedOn(world, pos, true);
	}
	
	private void setSteppedOn(World world, BlockPos pos, boolean flag)
	{
		BlockState state = world.getBlockState(pos);
		if(state.isAir() || !state.isOf(this))
			return;
		
		world.setBlockState(pos, state.with(STEPPED_ON, flag), 2);
		if(flag)
			world.scheduleBlockTick(pos, state.getBlock(), 10);
	}
	
	protected void scheduledTick(BlockState state, ServerWorld world, BlockPos pos, Random random)
	{
		if(!state.get(STEPPED_ON))
			return;
		
		List<? extends Entity> list = world.getOtherEntities(null, state.getOutlineShape(world, pos).getBoundingBox().offset(pos));
		if(list.isEmpty() || list.stream().allMatch(Entity::bypassesSteppingEffects))
			world.setBlockState(pos, state.with(STEPPED_ON, false));
		else
			world.scheduleBlockTick(pos, state.getBlock(), 10);
	}
}
