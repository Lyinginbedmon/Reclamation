package com.lying.utility;

import java.util.List;
import java.util.Optional;

import com.google.common.collect.Lists;
import com.google.gson.JsonElement;
import com.lying.Reclamation;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.registry.Registries;
import net.minecraft.util.math.random.Random;

public class StateGetter
{
	public static final Codec<StateGetter> CODEC	= RecordCodecBuilder.create(instance -> instance.group(
			BlockState.CODEC.optionalFieldOf("blockstate").forGetter(g -> g.targetState),
			Registries.BLOCK.getCodec().optionalFieldOf("block").forGetter(g -> g.targetBlock),
			BlockState.CODEC.listOf().optionalFieldOf("blockstates").forGetter(StateGetter::blockStatesOpt),
			Registries.BLOCK.getCodec().listOf().optionalFieldOf("blocks").forGetter(StateGetter::blocksOpt))
			.apply(instance, (a,b, c, d) -> 
				create()
					.addBlockState(a.orElse(null))
					.addBlock(b.orElse(null))
					.addBlockState(c.orElse(List.of()).toArray(new BlockState[0]))
					.addBlock(d.orElse(List.of()).toArray(new Block[0]))));
	
	private List<Block> blocks = Lists.newArrayList();
	private List<BlockState> blockStates = Lists.newArrayList();
	
	private Optional<BlockState> targetState = Optional.empty();
	private Optional<Block> targetBlock = Optional.empty();
	
	private List<BlockState> states = Lists.newArrayList();
	
	protected StateGetter() { }
	
	public static StateGetter create() { return new StateGetter(); }
	
	private Optional<List<BlockState>> blockStatesOpt()
	{
		return !blockStates.isEmpty() && targetState.isEmpty() ? Optional.of(blockStates) : Optional.empty();
	}
	
	private Optional<List<Block>> blocksOpt()
	{
		return !blocks.isEmpty() && targetBlock.isEmpty() ? Optional.of(blocks) : Optional.empty();
	}
	
	public StateGetter addBlockState(BlockState... statesIn)
	{
		for(BlockState state : statesIn)
		{
			if(state == null)
				continue;
			
			blockStates.removeIf(s -> s.equals(state));
			blockStates.add(state);
			addState(state);
		}
		return this;
	}
	
	public StateGetter addBlock(Block... blocksIn)
	{
		for(Block block : blocksIn)
		{
			if(block == null)
				continue;
			
			blocks.removeIf(b -> b.equals(block));
			blocks.add(block);
			addState(block.getDefaultState());
		}
		return this;
	}
	
	private boolean addState(BlockState state)
	{
		boolean result = !states.removeIf(s -> s.equals(state));
		states.add(state);
		
		switch(states.size())
		{
			case 1:
				if(!blocks.isEmpty())
					targetBlock = Optional.of(blocks.get(0));
				else if(!blockStates.isEmpty())
					targetState = Optional.of(blockStates.get(0));
				break;
			default:
				targetState = Optional.empty();
				targetBlock = Optional.empty();
				break;
		}
		return result;
	}
	
	public boolean isEmpty()
	{
		return states.isEmpty();
	}
	
	public Optional<BlockState> getRandom(Random random)
	{
		return states.isEmpty() ? Optional.empty() : Optional.of(states.get(random.nextInt(states.size())));
	}
	
	public JsonElement toJson()
	{
		return (JsonElement)CODEC.encodeStart(JsonOps.INSTANCE, this).getOrThrow();
	}
	
	public static StateGetter fromJson(JsonElement json)
	{
		return CODEC.parse(JsonOps.INSTANCE, json).resultOrPartial(Reclamation.LOGGER::error).orElse(new StateGetter());
	}
}
