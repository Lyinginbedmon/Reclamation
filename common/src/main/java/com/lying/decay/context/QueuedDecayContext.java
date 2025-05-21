package com.lying.decay.context;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import com.google.common.collect.Lists;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.fluid.FluidState;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldEvents;

/** DecayContext that performs all enqeued events when closed */
public class QueuedDecayContext extends DecayContext
{
	protected final List<Consumer<ServerWorld>> enqueuedWork = Lists.newArrayList();
	
	protected QueuedDecayContext(BlockPos pos, ServerWorld serverWorld, BlockState state, DecayType typeIn)
	{
		super(serverWorld, pos, state, typeIn);
	}
	
	public static QueuedDecayContext root(DecayType type) { return new QueuedDecayContext(BlockPos.ORIGIN, (ServerWorld)null, Blocks.AIR.getDefaultState(), type); }
	
	public static QueuedDecayContext supplier(BlockPos pos, ServerWorld world, DecayType type){ return new QueuedDecayContext(pos, world, world.getBlockState(pos), type); }
	
	public DecayContext create(ServerWorld serverWorld, BlockPos start, BlockState original) { return new QueuedDecayContext(start, serverWorld, original, this.type); }
	
	public boolean isAir(BlockPos pos) { return world.get().getBlockState(pos).isAir(); }
	
	public FluidState fluidState() { return world.get().getFluidState(currentPos()); }
	
	public BlockState getBlockState(BlockPos pos) { return world.get().getBlockState(pos); }
	
	public void execute(BiConsumer<BlockPos, ServerWorld> consumer)
	{
		enqueue(currentPos(), (p,w) -> consumer.accept(p, w));
	}
	
	public void breakBlock(BlockPos pos)
	{
		enqueue(pos, (p,w) -> w.syncWorldEvent(WorldEvents.BLOCK_BROKEN, p, Block.getRawIdFromState(w.getBlockState(p))));
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
		super.close();
		enqueuedWork.forEach(c -> c.accept(world.get()));
	}
}
