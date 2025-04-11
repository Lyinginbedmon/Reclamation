package com.lying.decay.context;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import com.google.common.collect.Lists;

import net.minecraft.block.BlockState;
import net.minecraft.fluid.FluidState;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

/** DecayContext that performs all enqeued events when closed */
public class QueuedDecayContext extends DecayContext
{
	protected final ServerWorld world;
	
	protected final List<Consumer<ServerWorld>> enqueuedWork = Lists.newArrayList();
	
	protected QueuedDecayContext(BlockPos pos, ServerWorld serverWorld, BlockState state)
	{
		super(pos, state, serverWorld.random);
		world = serverWorld;
	}
	
	public static QueuedDecayContext supplier(BlockPos pos, ServerWorld world){ return new QueuedDecayContext(pos, world, world.getBlockState(pos)); }
	
	public boolean isAir(BlockPos pos) { return world.getBlockState(pos).isAir(); }
	
	public FluidState fluidState() { return world.getFluidState(currentPos()); }
	
	public BlockState getBlockState(BlockPos pos) { return world.getBlockState(pos); }
	
	public void execute(BiConsumer<BlockPos, ServerWorld> consumer)
	{
		enqueue(currentPos(), (p,w) -> consumer.accept(p, w));
	}
	
	public void breakBlock(BlockPos pos)
	{
		enqueue(pos, (p,w) -> w.breakBlock(p, false));
	}
	
	protected void setStateInWorld(BlockPos pos, BlockState state)
	{
		enqueue(pos, (p,w) -> w.setBlockState(p, state));
	}
	
	private void enqueue(BlockPos pos, BiConsumer<BlockPos, ServerWorld> consumer)
	{
		final BlockPos position = new BlockPos(pos);
		enqueuedWork.add(w -> consumer.accept(position, w));
	}
	
	public void close()
	{
		enqueuedWork.forEach(c -> c.accept(world));
	}
}
