package com.lying.decay.context;

import java.util.function.BiConsumer;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.fluid.FluidState;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldEvents;

/** DecayContext that performs all events live on the server */
public class LiveDecayContext extends DecayContext
{
	protected LiveDecayContext(BlockPos pos, ServerWorld serverWorld, BlockState state, DecayType typeIn)
	{
		super(serverWorld, pos, state, typeIn);
	}
	
	public static LiveDecayContext root(DecayType type) { return new LiveDecayContext(BlockPos.ORIGIN, (ServerWorld)null, Blocks.AIR.getDefaultState(), type); }
	
	public static LiveDecayContext supplier(BlockPos pos, ServerWorld world, DecayType type) { return new LiveDecayContext(pos, world, world.getBlockState(pos), type); }
	
	public DecayContext create(ServerWorld serverWorld, BlockPos start, BlockState original) { return new LiveDecayContext(start, serverWorld, original, this.type); }
	
	public boolean isAir(BlockPos pos) { return world.get().getBlockState(pos).isAir(); }
	
	public FluidState fluidState() { return world.get().getFluidState(currentPos()); }
	
	public BlockState getBlockState(BlockPos pos) { return world.get().getBlockState(pos); }
	
	public void breakBlock(BlockPos pos) { world.get().syncWorldEvent(WorldEvents.BLOCK_BROKEN, pos, Block.getRawIdFromState(currentState)); }
	
	public void execute(BiConsumer<BlockPos, ServerWorld> consumer) { consumer.accept(currentPos, world.get()); }
	
	protected void setStateInWorld(BlockPos pos, BlockState state) { world.get().setBlockState(pos, state); }
}
