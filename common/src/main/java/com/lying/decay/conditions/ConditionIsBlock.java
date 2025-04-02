package com.lying.decay.conditions;

import java.util.List;

import com.google.common.collect.Lists;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.lying.Reclamation;
import com.lying.init.RCDecayConditions;
import com.mojang.serialization.JsonOps;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

public class ConditionIsBlock extends DecayCondition
{
	protected List<Block> blocks = Lists.newArrayList();
	protected List<BlockState> states = Lists.newArrayList();
	protected List<TagKey<Block>> tags = Lists.newArrayList();
	
	public ConditionIsBlock(Identifier idIn)
	{
		super(idIn);
	}
	
	public boolean check(ServerWorld world, BlockPos pos, BlockState currentState)
	{
		return
				blocks.stream().anyMatch(b -> currentState.isOf(b)) || 
				states.stream().anyMatch(s -> currentState.equals(s)) ||
				tags.stream().anyMatch(t -> currentState.isIn(t));
	}
	
	protected void clear()
	{
		blocks.clear();
		states.clear();
		tags.clear();
	}
	
	public static DecayCondition of(Block... target)
	{
		ConditionIsBlock inst = (ConditionIsBlock)RCDecayConditions.IS_BLOCK.get();
		inst.clear();
		for(Block block : target)
			inst.blocks.add(block);
		return inst;
	}
	
	public static DecayCondition of(BlockState... target)
	{
		ConditionIsBlock inst = (ConditionIsBlock)RCDecayConditions.IS_BLOCK.get();
		inst.clear();
		for(BlockState block : target)
			inst.states.add(block);
		return inst;
	}
	
	@SuppressWarnings("unchecked")
	public static DecayCondition of(TagKey<Block>... target)
	{
		ConditionIsBlock inst = (ConditionIsBlock)RCDecayConditions.IS_BLOCK.get();
		inst.clear();
		for(TagKey<Block> block : target)
			inst.tags.add(block);
		return inst;
	}
	
	protected JsonObject write(JsonObject obj)
	{
		if(!blocks.isEmpty())
			obj.add("blocks", (JsonElement)BLOCK_CODEC.encodeStart(JsonOps.INSTANCE, blocks).getOrThrow());
		if(!states.isEmpty())
			obj.add("states", (JsonElement)BLOCKSTATE_CODEC.encodeStart(JsonOps.INSTANCE, states).getOrThrow());
		if(!tags.isEmpty())
			obj.add("tags", (JsonElement)TAG_CODEC.encodeStart(JsonOps.INSTANCE, tags).getOrThrow());
		return obj;
	}
	
	protected void read(JsonObject obj)
	{
		clear();
		if(obj.has("blocks"))
		{
			List<Block> blockList = BLOCK_CODEC.parse(JsonOps.INSTANCE, obj.get("blocks")).resultOrPartial(Reclamation.LOGGER::error).orElse(List.of());
			blocks.clear();
			blocks.addAll(blockList);
		}
		
		if(obj.has("states"))
		{
			List<BlockState> stateList = BLOCKSTATE_CODEC.parse(JsonOps.INSTANCE, obj.get("states")).resultOrPartial(Reclamation.LOGGER::error).orElse(List.of());
			states.clear();
			states.addAll(stateList);
		}
		
		if(obj.has("tags"))
		{
			List<TagKey<Block>> stateList = TAG_CODEC.parse(JsonOps.INSTANCE, obj.get("tags")).resultOrPartial(Reclamation.LOGGER::error).orElse(List.of());
			tags.clear();
			tags.addAll(stateList);
		}
	}
}