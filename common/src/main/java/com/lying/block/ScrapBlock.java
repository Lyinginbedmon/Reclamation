package com.lying.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

public class ScrapBlock extends Block
{
	protected static final VoxelShape SHAPE = Block.createCuboidShape(1.0, 0.0, 1.0, 15.0, 3.0, 15.0);
	
	public ScrapBlock(Settings settings)
	{
		super(settings);
	}
	
	protected VoxelShape getInsideCollisionShape(BlockState state, World world, BlockPos pos) { return state.getOutlineShape(world, pos); }
	
	protected VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) { return SHAPE; }
	
	protected VoxelShape getSidesShape(BlockState state, BlockView world, BlockPos pos) { return SHAPE; }
	
	protected VoxelShape getCameraCollisionShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) { return SHAPE; }
}
