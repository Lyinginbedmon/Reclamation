package com.lying.decay.functions;

import java.util.List;
import java.util.Optional;

import com.google.common.collect.Lists;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.lying.Reclamation;
import com.lying.decay.DecayContext;
import com.lying.init.RCDecayFunctions;
import com.mojang.serialization.JsonOps;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.random.Random;

public class FunctionConvert extends DecayFunction
{
	private List<Block> blocks = Lists.newArrayList();
	private List<BlockState> blockstates = Lists.newArrayList();
	
	public FunctionConvert(Identifier idIn)
	{
		super(idIn);
	}
	
	public static DecayFunction to(Block... target)
	{
		FunctionConvert inst = ((FunctionConvert)RCDecayFunctions.CONVERT.get()).clear();
		for(Block state : target)
			inst.blocks.add(state);
		return inst;
	}
	
	public static DecayFunction to(BlockState... target)
	{
		FunctionConvert inst = ((FunctionConvert)RCDecayFunctions.CONVERT.get()).clear();
		for(BlockState state : target)
			inst.blockstates.add(state);
		return inst;
	}
	
	protected FunctionConvert clear()
	{
		blocks.clear();
		blockstates.clear();
		return this;
	}
	
	protected void applyTo(DecayContext context)
	{
		destination(context.random).ifPresent(state -> context.setBlockState(state));
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