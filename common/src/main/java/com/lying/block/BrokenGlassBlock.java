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
import net.minecraft.block.ShapeContext;
import net.minecraft.block.TransparentBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;
import net.minecraft.world.tick.ScheduledTickView;

public class BrokenGlassBlock extends TransparentBlock
{
	public static final MapCodec<BrokenGlassBlock> CODEC	= createCodec(BrokenGlassBlock::new);
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
	
	public BrokenGlassBlock(Settings settings)
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
					.collect(Collectors.toMap(Function.identity(), BrokenGlassBlock::getShapeForState)));
	}
	
	protected void appendProperties(StateManager.Builder<Block, BlockState> builder)
	{
		builder.add(UP, DOWN, NORTH, EAST, SOUTH, WEST);
	}
	
	private static VoxelShape getShapeForState(BlockState state)
	{
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
	
	protected void onEntityCollision(BlockState state, World world, BlockPos pos, Entity entity)
	{
		if(!(entity instanceof LivingEntity) || entity.getBlockPos().getY() != pos.getY())
			return;
		
		entity.slowMovement(state, new Vec3d(0.8F, 0.75F, 0.8F));
		if(world.isClient() || entity.isInvulnerable() || entity.isSneaky() || world.getRandom().nextInt(8) > 0)
			return;
		
		Vec3d motion = entity.isControlledByPlayer() ? entity.getMovement() : entity.getLastRenderPos().subtract(entity.getPos());
		if(motion.horizontalLengthSquared() > 0)
		{
			double lenX = Math.abs(motion.getX());
			double lenZ = Math.abs(motion.getZ());
			if(lenX >= 0.003F || lenZ >= 0.003F)
				entity.damage((ServerWorld)world, world.getDamageSources().sweetBerryBush(), 3F);
		}
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
		return state;
	}
	
	protected BlockState getStateForNeighborUpdate(BlockState state, WorldView world, ScheduledTickView tickView, BlockPos pos, Direction direction, BlockPos neighbourPos, BlockState neighbourState, Random random)
	{
		// Update the state with all valid connection faces
		state = state.with(FACING_PROPERTIES.get(direction), shouldConnectTo(neighbourState, world, neighbourPos, direction.getOpposite()));
		
		// If we have no connected faces, break
		if(!hasAnyFaces(state))
			return Blocks.AIR.getDefaultState();
		
		return state;
	}
	
	public static boolean shouldConnectTo(BlockState state, WorldView world, BlockPos pos, Direction onFace)
	{
		return !(state.isAir() || state.getBlock() instanceof BrokenGlassBlock || state.getCollisionShape(world, pos).isEmpty()) && sideCoversSmallSquare(world, pos, onFace);
	}
	
	public static boolean hasAnyFaces(BlockState state)
	{
		return FACING_PROPERTIES.values().stream().anyMatch(state::get);
	}
}
