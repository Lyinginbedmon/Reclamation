package com.lying.decay.context;

import java.util.function.BiConsumer;

import net.minecraft.block.BlockState;
import net.minecraft.fluid.FluidState;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

/** DecayContext that performs all events live on the server */
public class LiveDecayContext extends DecayContext
{
	protected final ServerWorld world;
	
	protected LiveDecayContext(BlockPos pos, ServerWorld serverWorld, BlockState state)
	{
		super(pos, state, serverWorld.random);
		world = serverWorld;
	}
	
	public static LiveDecayContext supplier(BlockPos pos, ServerWorld world) { return new LiveDecayContext(pos, world, world.getBlockState(pos)); }
	
	public boolean isAir(BlockPos pos) { return world.getBlockState(pos).isAir(); }
	
	public FluidState fluidState() { return world.getFluidState(currentPos()); }
	
	public BlockState getBlockState(BlockPos pos) { return world.getBlockState(pos); }
	
	public void breakBlock(BlockPos pos) { world.breakBlock(pos, false); }
	
	public void execute(BiConsumer<BlockPos, ServerWorld> consumer) { consumer.accept(currentPos(), world); }
	
	protected void setStateInWorld(BlockPos pos, BlockState state) { world.setBlockState(pos, state); }
}
