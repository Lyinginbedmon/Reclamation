package com.lying.decay.context;

import java.util.function.BiConsumer;

import com.lying.event.DecayEvent;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;

/** Holder object for information relevant to the current decay event */
public abstract class DecayContext
{
	public final ServerWorld world;
	public final Random random;
	
	public final DecayType type;
	
	/** The original location of the affected block */
	public final BlockPos initialPos;
	
	/** The block state that was originally affected */
	public final BlockState originalState;
	
	/** The current world position of the block being acted on */
	protected BlockPos currentPos;
	
	/** The current condition of the block being acted upon */
	protected BlockState currentState;
	
	private boolean isBroken = false;
	
	protected DecayContext(ServerWorld serverWorld, BlockPos start, BlockState original, Random rand, DecayType typeIn)
	{
		world = serverWorld;
		type = typeIn;
		random = rand;
		initialPos = currentPos = start;
		originalState = currentState = original;
	}
	
	public void close() { }
	
	public final BlockPos currentPos() { return currentPos; }
	
	public final BlockState currentState() { return currentState; }
	
	/** Returns true if this context has broken continuity, such as by changing the block into an entity */
	public final boolean continuityBroken() { return isBroken; }
	
	/** Prevents further DecayFunctions from acting on this context */
	public final void preventFurtherChanges() { isBroken = true; }
	
	public abstract BlockState getBlockState(BlockPos pos);
	
	public final boolean isAir() { return getBlockState(currentPos).isAir() || currentState.isAir(); }
	
	public abstract boolean isAir(BlockPos pos);
	
	protected abstract void setStateInWorld(BlockPos pos, BlockState state);
	
	/** Performs a function server-side otherwise not directly supported by the context */
	public abstract void execute(BiConsumer<BlockPos, ServerWorld> consumer);
	
	public abstract void breakBlock(BlockPos pos);
	
	public final void breakBlock() { breakBlock(currentPos); }
	
	/** Changes this block to the given block state */
	public final void setBlockState(BlockState state)
	{
		if(!isAir())
			breakBlock();
		setStateInWorld(currentPos, state);
		currentState = state;
	}
	
	/** Changes another block in the world */
	public final void setBlockState(BlockPos pos, BlockState state)
	{
		if(pos.equals(currentPos))
		{
			setBlockState(state);
			return;
		}
		
		if(!isAir(pos))
			breakBlock(pos);
		setStateInWorld(pos, state);
	}
	
	public final void move(Direction direction) { moveTo(currentPos.offset(direction)); }
	
	/** Moves this block to the given position, destroying whatever was there */
	public final void moveTo(BlockPos pos)
	{
		breakBlock();
		setStateInWorld(currentPos, Blocks.AIR.getDefaultState());
		
		currentPos = pos;
		setStateInWorld(currentPos, currentState);
	}
	
	/** Breaks both the given block and this one and replaces them in opposite positions */
	public final void swapWith(BlockPos pos)
	{
		BlockState stateAt = getBlockState(pos);
		
		if(!isAir(currentPos))
			breakBlock(currentPos);
		
		if(!stateAt.isAir())
			breakBlock(pos);
		
		setStateInWorld(currentPos, stateAt);
		setStateInWorld(pos, currentState);
		
		currentPos = pos;
	}
	
	public static enum DecayType
	{
		/** Decay applied by normal server function */
		NATURAL,
		/** Decay applied by commands */
		ARTIFICIAL;
		
		public boolean canDecayBlock(BlockPos pos, ServerWorld world)
		{
			return !DecayEvent.CAN_DECAY_BLOCK_EVENT.invoker().canBlockDecay(pos, world, this).isFalse();
		}
	}
}
