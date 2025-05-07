package com.lying.block;

import java.util.Map;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.Waterloggable;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.Item;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;
import net.minecraft.world.tick.ScheduledTickView;

public class DousedTorchBlock extends Block implements Waterloggable
{
	public static final EnumProperty<Direction> FACING	= EnumProperty.of("facing", Direction.class, Direction.UP, Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST);
	public static final BooleanProperty WATERLOGGED = Properties.WATERLOGGED;
	protected static final VoxelShape SHAPE_DOWN = Block.createCuboidShape(6.0, 0.0, 6.0, 10.0, 10.0, 10.0);
	private static final Map<Direction, VoxelShape> BOUNDING_SHAPES = Maps.newEnumMap(
			ImmutableMap.of(
					Direction.UP, SHAPE_DOWN,
					Direction.NORTH, Block.createCuboidShape(5.5, 3.0, 11.0, 10.5, 13.0, 16.0),
					Direction.SOUTH, Block.createCuboidShape(5.5, 3.0, 0.0, 10.5, 13.0, 5.0),
					Direction.WEST, Block.createCuboidShape(11.0, 3.0, 5.5, 16.0, 13.0, 10.5),
					Direction.EAST, Block.createCuboidShape(0.0, 3.0, 5.5, 5.0, 13.0, 10.5)));
	
	/** Function for converting the {@link FACING} property into a corresponding lit torch */
	protected final Function<Direction, BlockState> igniter;
	
	public DousedTorchBlock(Block litFloor, Block litWall, Settings settings)
	{
		super(settings);
		igniter = d -> d.getAxis().isHorizontal() ? litWall.getDefaultState().with(Properties.HORIZONTAL_FACING, d) : litFloor.getDefaultState();
		this.setDefaultState(getDefaultState().with(FACING, Direction.UP).with(WATERLOGGED, false));
	}
	
	protected void appendProperties(StateManager.Builder<Block, BlockState> builder)
	{
		builder.add(FACING, WATERLOGGED);
	}
	
	public static VoxelShape getBoundingShape(BlockState state)
	{
		return BOUNDING_SHAPES.getOrDefault(state.get(FACING), SHAPE_DOWN);
	}
	
	protected VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context)
	{
		return getBoundingShape(state);
	}
	
	public BlockState getPlacementState(ItemPlacementContext ctx)
	{
		WorldView worldView = ctx.getWorld();
		BlockPos blockPos = ctx.getBlockPos();
		BlockState placementState = this.getDefaultState().with(WATERLOGGED, worldView.getFluidState(blockPos).getFluid() == Fluids.WATER);
		
		Direction face = ctx.getSide();
		if(!FACING.getValues().contains(face.getAxis().isHorizontal() ? face.getOpposite() : face))
			for(Direction direction : FACING.getValues())
			{
				placementState = placementState.with(FACING, direction);
				if(placementState.canPlaceAt(worldView, blockPos))
					return placementState;
			}
		else
			return placementState.with(FACING, face);
		
		return placementState;
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
		if(state.get(WATERLOGGED))
			tickView.scheduleFluidTick(pos, Fluids.WATER, Fluids.WATER.getTickRate(world));
		
		return state.get(FACING).getOpposite() == direction && !state.canPlaceAt(world, pos)
			? Blocks.AIR.getDefaultState()
			: super.getStateForNeighborUpdate(state, world, tickView, pos, direction, neighborPos, neighborState, random);
	}
	
	protected ActionResult onUseWithItem(ItemStack stack, BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit)
	{
		if(state.get(WATERLOGGED) || !stack.isOf(Items.FLINT_AND_STEEL) && !stack.isOf(Items.FIRE_CHARGE))
			return super.onUseWithItem(stack, state, world, pos, player, hand, hit);
		else
		{
			world.setBlockState(pos, igniter.apply(state.get(FACING)), 11);
			Item item = stack.getItem();
			if(stack.isOf(Items.FLINT_AND_STEEL))
			{
				stack.damage(1, player, LivingEntity.getSlotForHand(hand));
				world.playSound(player, pos, SoundEvents.ITEM_FLINTANDSTEEL_USE, SoundCategory.BLOCKS, 1.0F, world.getRandom().nextFloat() * 0.4F + 0.8F);
			}
			else
			{
				stack.decrementUnlessCreative(1, player);
				world.playSound(player, pos, SoundEvents.ITEM_FIRECHARGE_USE, SoundCategory.BLOCKS, 1.0F, world.getRandom().nextFloat() * 0.4F + 0.8F);
			}
			
			player.incrementStat(Stats.USED.getOrCreateStat(item));
			return ActionResult.SUCCESS;
		}
	}
	
	protected boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos)
	{
		return canPlaceAt(world, pos, state.get(FACING));
	}
	
	public static boolean canPlaceAt(WorldView world, BlockPos pos, Direction facing)
	{
		switch(facing)
		{
			case UP:	return sideCoversSmallSquare(world, pos.down(), Direction.UP);
			case NORTH:
			case EAST:
			case SOUTH:
			case WEST:
				BlockPos host = pos.offset(facing.getOpposite());
				BlockState hostState = world.getBlockState(host);
				return hostState.isSideSolidFullSquare(world, host, facing);
			default:	return false;
		}
	}
	
	protected FluidState getFluidState(BlockState state) { return state.get(WATERLOGGED) ? Fluids.WATER.getStill(false) : super.getFluidState(state); }
	
	protected BlockState rotate(BlockState state, BlockRotation rotation)
	{
		if(state.get(FACING).getAxis().isHorizontal())
			return state.with(FACING, rotation.rotate(state.get(FACING)));
		else
			return state;
	}
	
	protected BlockState mirror(BlockState state, BlockMirror mirror)
	{
		return state.rotate(mirror.getRotation(state.get(FACING)));
	}
}
