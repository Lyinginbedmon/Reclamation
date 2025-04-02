package com.lying.utility;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

import com.google.common.collect.Lists;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;

/** Utility class for defining a predicate for BlockStates */
public class BlockPredicate implements Predicate<BlockState>
{
	public static final Codec<BlockPredicate> CODEC	= RecordCodecBuilder.create(instance -> instance.group(
			Registries.BLOCK.getCodec().listOf().optionalFieldOf("blocks").forGetter(p -> p.blocks),
			BlockState.CODEC.listOf().optionalFieldOf("states").forGetter(p -> p.states),
			TagKey.codec(RegistryKeys.BLOCK).listOf().optionalFieldOf("tags").forGetter(p -> p.tags))
				.apply(instance, (a, b, c) -> 
				{
					Builder builder = Builder.create();
					a.ifPresent(l -> builder.add(l.toArray(new Block[0])));
					b.ifPresent(l -> builder.add(l.toArray(new BlockState[0])));
					c.ifPresent(l -> builder.addTags(l));
					return builder.build();
				}));
	
	protected final Optional<List<Block>> blocks;
	protected final Optional<List<BlockState>> states;
	protected final Optional<List<TagKey<Block>>> tags;
	
	protected BlockPredicate(Optional<List<Block>> blocksIn, Optional<List<BlockState>> statesIn, Optional<List<TagKey<Block>>> tagsIn)
	{
		blocks = blocksIn;
		states = statesIn;
		tags = tagsIn;
	}
	
	public JsonElement toJson()
	{
		return CODEC.encodeStart(JsonOps.INSTANCE, this).getOrThrow().getAsJsonObject();
	}
	
	public static BlockPredicate fromJson(JsonObject obj)
	{
		return CODEC.parse(JsonOps.INSTANCE, obj).getOrThrow();
	}
	
	public boolean isEmpty()
	{
		return
				blocks.isEmpty() &&
				states.isEmpty() &&
				tags.isEmpty();
	}
	
	public boolean test(BlockState currentState)
	{
		return
				!isEmpty() &&
				(
					(blocks.isPresent() && blocks.get().stream().anyMatch(b -> currentState.isOf(b))) || 
					(states.isPresent() && states.get().stream().anyMatch(s -> currentState.equals(s))) ||
					(tags.isPresent() && tags.get().stream().anyMatch(t -> currentState.isIn(t)))
				);
	}
	
	public static class Builder
	{
		List<Block> blocks = Lists.newArrayList();
		List<BlockState> states = Lists.newArrayList();
		List<TagKey<Block>> tags = Lists.newArrayList();
		
		protected Builder() { }
		
		public static Builder create() { return new Builder(); }
		
		public Builder add(Block... blocks)
		{
			for(Block block : blocks)
			{
				this.blocks.removeIf(b -> b.equals(block));
				this.blocks.add(block);
			}
			
			return this;
		}
		
		public Builder add(BlockState... states)
		{
			for(BlockState state : states)
			{
				this.states.removeIf(b -> b.equals(state));
				this.states.add(state);
			}
			
			return this;
		}
		
		@SuppressWarnings("unchecked")
		public Builder add(TagKey<Block>... tags)
		{
			for(TagKey<Block> tag : tags)
			{
				this.tags.removeIf(b -> b.equals(tag));
				this.tags.add(tag);
			}
			
			return this;
		}
		
		public Builder addTags(List<TagKey<Block>> tagsIn)
		{
			tagsIn.forEach(tag -> 
			{
				this.tags.removeIf(t -> t.equals(tag));
				this.tags.add(tag);
			});
			return this;
		}
		
		public BlockPredicate build()
		{
			return new BlockPredicate(
					blocks.isEmpty() ? Optional.empty() : Optional.of(blocks), 
					states.isEmpty() ? Optional.empty() : Optional.of(states),
					tags.isEmpty() ? Optional.empty() : Optional.of(tags));
		}
	}
}
