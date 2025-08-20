package com.lying.decay.context;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.lying.event.DecayEvent;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;

/** Holder object for information relevant to the current decay event */
public abstract class DecayContext
{
	public final Optional<ServerWorld> world;
	public final Random random;
	
	public final DecayType type;
	
	/** The original location of the affected block */
	public final BlockPos initialPos;
	
	/** The block state that was originally affected */
	public final BlockState originalState;
	
	public final NbtCompound originalNBT;
	
	/** The current world position of the block being acted on */
	protected BlockPos currentPos;
	
	/** The current condition of the block being acted upon */
	protected BlockState currentState;
	
	/** Cached list of positions identified as being outdoors per world */
	protected Map<RegistryKey<World>, List<BlockPos>> exteriorPositions = Maps.newHashMap();
	
	/** Optional parent context from which to track flagged exterior positions */
	protected Optional<DecayContext> parent = Optional.empty();
	protected List<DecayContext> children = Lists.newArrayList();
	
	private boolean isBroken = false;
	
	protected DecayContext(ServerWorld serverWorld, BlockPos start, BlockState original, DecayType typeIn)
	{
		if(serverWorld == null)
		{
			world = Optional.empty();
			random = null;
			originalNBT = new NbtCompound();
		}
		else
		{
			world = Optional.of(serverWorld);
			random = serverWorld.random;
			
			BlockEntity entity = serverWorld.getBlockEntity(start);
			if(entity == null)
				originalNBT = new NbtCompound();
			else
				originalNBT = entity.createNbt(serverWorld.getRegistryManager());
		}
		type = typeIn;
		initialPos = currentPos = start;
		originalState = currentState = original;
	}
	
	public abstract DecayContext create(ServerWorld serverWorld, BlockPos start, BlockState original);
	
	public final DecayContext create(ServerWorld serverWorld, BlockPos start)
	{
		return create(serverWorld, start, serverWorld.getBlockState(start));
	}
	
	public final DecayContext setParent(DecayContext parentIn)
	{
		if(!isRoot())
			parent = Optional.of(parentIn);
		return this;
	}
	
	/** Returns true if this context has no world variable, so cannot itself be used for calculations */
	public final boolean isRoot() { return world.isEmpty(); }
	
	public final RegistryKey<World> worldKey() { return isRoot() ? World.OVERWORLD : world.get().getRegistryKey(); }
	
	/** Adds the given context to the children of this context and marks this context as its parent */
	public final void addChild(DecayContext context)
	{
		children.add(context.setParent(this));
		// Merge the child context's known exterior positions into this context's knowledge
		context.exteriorPositions.entrySet().forEach(entry -> entry.getValue().forEach(p -> flagExterior(p, entry.getKey())));
	}
	
	public void close() { children.forEach(DecayContext::close); }
	
	public int descendants() { return children.size(); }
	
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
	
	/** Flags the given position as known to be exterior (ie. having direct vertical access to the skybox) to this context */
	public final void flagExterior(BlockPos position)
	{
		RegistryKey<World> worldKey = worldKey();
		parent.ifPresentOrElse(p -> p.flagExterior(position, worldKey), () -> flagExterior(position, worldKey));
	}
	
	protected final void flagExterior(BlockPos position, RegistryKey<World> world)
	{
		List<BlockPos> positions = exteriorPositions.getOrDefault(world, Lists.newArrayList());
		if(!positions.contains(position))
			positions.add(position);
		exteriorPositions.put(world, positions);
	}
	
	/** Returns a list of all known exterior positions in the given world, deferring to parent if available */
	protected final List<BlockPos> getKnownExteriors(RegistryKey<World> world)
	{
		return parent.isPresent() ? parent.get().getKnownExteriors(world) : exteriorPositions.getOrDefault(world, Lists.newArrayList());
	}
	
	public final List<BlockPos> findNearbyExteriors(BlockPos origin, int radius)
	{
		return getKnownExteriors(worldKey()).stream().filter(p -> p.getSquaredDistance(origin) < radius * radius).toList();
	}
	
	public abstract void breakBlock(BlockPos pos);
	
	public final void breakBlock() { breakBlock(currentPos); }
	
	/** Changes this block to the given block state */
	public final void setBlockState(BlockState state)
	{
		if(!isAir() && !currentState.isOf(Blocks.FIRE))
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
