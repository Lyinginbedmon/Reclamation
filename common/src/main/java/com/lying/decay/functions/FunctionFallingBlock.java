package com.lying.decay.functions;

import java.util.List;
import java.util.Optional;

import com.google.common.collect.Lists;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.lying.Reclamation;
import com.lying.decay.DecayContext;
import com.mojang.serialization.JsonOps;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.FallingBlockEntity;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.random.Random;

public abstract class FunctionFallingBlock extends DecayFunction
{
	protected FunctionFallingBlock(Identifier idIn)
	{
		super(idIn);
	}
	
	@SuppressWarnings("deprecation")
	protected static boolean canFallThrough(BlockState state)
	{
		return state.isAir() || state.isIn(BlockTags.FIRE) || state.isLiquid() || state.isReplaceable();
	}
	
	public static class Fall extends FunctionFallingBlock
	{
		public Fall(Identifier idIn)
		{
			super(idIn);
		}
		
		protected void applyTo(DecayContext context)
		{
			if(!canFallThrough(context.world.getBlockState(context.currentPos.down())))
				return;
			
			FallingBlockEntity.spawnFromBlock(context.world, context.currentPos, context.currentState);
			context.breakBlock();
			context.preventFurtherChanges();
		}
	}
	
	public static class Drop extends FunctionFallingBlock
	{
		private List<Block> blocks = Lists.newArrayList();
		private List<BlockState> blockstates = Lists.newArrayList();
		
		public Drop(Identifier idIn)
		{
			super(idIn);
		}
		
		protected void applyTo(DecayContext context)
		{
			if(canFallThrough(context.world.getBlockState(context.currentPos.down())))
				destination(context.random).ifPresent(state -> FallingBlockEntity.spawnFromBlock(context.world, context.currentPos.down(), state));
		}
		
		protected Optional<BlockState> destination(Random random)
		{
			if(blocks.isEmpty() && blockstates.isEmpty())
				return Optional.empty();
			
			List<BlockState> states = Lists.newArrayList();
			states.addAll(blockstates);
			states.addAll(blocks.stream().map(b -> b.getDefaultState()).toList());
			return states.isEmpty() ? Optional.empty() : Optional.of(states.size() > 1 ? states.get(random.nextInt(states.size())) : states.get(0));
		}
		
		protected Drop clear()
		{
			blocks.clear();
			blockstates.clear();
			return this;
		}
		
		protected JsonObject write(JsonObject obj)
		{
			if(!blocks.isEmpty())
				obj.add("blocks", (JsonElement)BLOCK_CODEC.encodeStart(JsonOps.INSTANCE, blocks).getOrThrow());
			if(!blockstates.isEmpty())
				obj.add("states", (JsonElement)BLOCKSTATE_CODEC.encodeStart(JsonOps.INSTANCE, blockstates).getOrThrow());
			return obj;
		}
		
		protected void read(JsonObject obj)
		{
			clear();
			if(obj.has("blocks"))
			{
				List<Block> blockList = BLOCK_CODEC.parse(JsonOps.INSTANCE, obj.get("blocks")).resultOrPartial(Reclamation.LOGGER::error).orElse(null);
				if(blockList != null)
					blocks.addAll(blockList);
			}
			
			if(obj.has("states"))
			{
				List<BlockState> stateList = BLOCKSTATE_CODEC.parse(JsonOps.INSTANCE, obj.get("states")).resultOrPartial(Reclamation.LOGGER::error).orElse(List.of());
				blockstates.addAll(stateList);
			}
		}
	}
}
