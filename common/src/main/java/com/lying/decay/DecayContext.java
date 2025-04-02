package com.lying.decay;

import java.util.function.BiConsumer;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.fluid.FluidState;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;

/** Holder object for information relevant to the current decay event */
public class DecayContext
{
	public final ServerWorld world;
	public final Random random;
	
	/** The original location of the affected block */
	public final BlockPos initialPos;
	
	/** The block state that was originally affected */
	public final BlockState originalState;
	
	/** The current world position of the block being acted on */
	public BlockPos currentPos;
	
	/** The current condition of the block being acted upon */
	public BlockState currentState;
	
	private boolean isBroken = false;
	
	public DecayContext(BlockPos pos, ServerWorld serverWorld, BlockState state)
	{
		world = serverWorld;
		random = serverWorld.random;
		initialPos = currentPos = pos;
		originalState = currentState = state;
	}
	
	/** Returns true if this context has broken continuity, such as by changing the block into an entity */
	public boolean continuityBroken() { return isBroken; }
	
	/** Prevents further DecayFunctions from acting on this context */
	public void preventFurtherChanges() { isBroken = true; }
	
	public boolean isAir() { return world.isAir(currentPos) || currentState.isAir(); }
	
	public FluidState fluidState() { return world.getFluidState(currentPos); }
	
	public void breakBlock() { world.breakBlock(currentPos, false); }
	
	public void execute(BiConsumer<BlockPos, ServerWorld> consumer) { consumer.accept(currentPos, world); }
	
	/** Changes this block to the given block state */
	public void setBlockState(BlockState state)
	{
		if(!isAir())
			breakBlock();
		world.setBlockState(currentPos, state);
		currentState = state;
	}
	
	/** Moves this block to the given position, destroying whatever was there */
	public void moveTo(BlockPos pos)
	{
		breakBlock();
		world.setBlockState(currentPos, Blocks.AIR.getDefaultState());
		
		currentPos = pos;
		world.setBlockState(currentPos, currentState);
	}
	
	/** Breaks both the given block and this one and replaces them in opposite positions */
	public void swapWith(BlockPos pos)
	{
		BlockState stateAt = world.getBlockState(pos);
		
		if(!world.isAir(currentPos))
			world.breakBlock(currentPos, false);
		
		if(!world.isAir(pos))
			world.breakBlock(pos, false);
		
		world.setBlockState(currentPos, stateAt);
		world.setBlockState(pos, currentState);
		
		currentPos = pos;
	}
}
