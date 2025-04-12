package com.lying.utility;

import static com.lying.utility.RCUtils.listOrSolo;

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

public class BlockProvider
{
	public static final Codec<BlockProvider> CODEC	= RecordCodecBuilder.create(instance -> instance.group(
			BlockState.CODEC.optionalFieldOf("state").forGetter(g -> listOrSolo(Optional.of(g.blockStates)).getRight()),
			BlockState.CODEC.listOf().optionalFieldOf("states").forGetter(g -> listOrSolo(Optional.of(g.blockStates)).getLeft()),
			Registries.BLOCK.getCodec().optionalFieldOf("block").forGetter(g -> listOrSolo(Optional.of(g.blocks)).getRight()),
			Registries.BLOCK.getCodec().listOf().optionalFieldOf("blocks").forGetter(g -> listOrSolo(Optional.of(g.blocks)).getLeft()))
			.apply(instance, (state, stateList, block, blockList) -> 
			{
				BlockProvider getter = create();
				state.ifPresent(s -> getter.addBlockState(s));
				stateList.ifPresent(s -> getter.addBlockState(s.toArray(new BlockState[0])));
				
				block.ifPresent(s -> getter.addBlock(s));
				blockList.ifPresent(s -> getter.addBlock(s.toArray(new Block[0])));
				return getter;
			}));
	
	private List<Block> blocks = Lists.newArrayList();
	private List<BlockState> blockStates = Lists.newArrayList();
	
	private List<BlockState> states = Lists.newArrayList();
	
	protected BlockProvider() { }
	
	public static BlockProvider create() { return new BlockProvider(); }
	
	public BlockProvider addBlockState(BlockState... statesIn)
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
	
	public BlockProvider addBlock(Block... blocksIn)
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
	
	public static BlockProvider fromJson(JsonElement json)
	{
		return CODEC.parse(JsonOps.INSTANCE, json).resultOrPartial(Reclamation.LOGGER::error).orElse(new BlockProvider());
	}
}
