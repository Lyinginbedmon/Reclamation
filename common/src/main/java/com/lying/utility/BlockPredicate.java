package com.lying.utility;

import java.util.List;
import java.util.Optional;
import java.util.function.BiPredicate;
import java.util.function.Predicate;
import java.util.stream.Stream;

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
			BlockState.CODEC.listOf().optionalFieldOf("states").forGetter(p -> p.blockStates),
			TagKey.codec(RegistryKeys.BLOCK).listOf().optionalFieldOf("tags").forGetter(p -> p.blockTags),
			Codec.STRING.listOf().optionalFieldOf("properties").forGetter(p -> p.blockProperties),
			PropertyMap.CODEC.listOf().optionalFieldOf("values").forGetter(p -> p.blockValues))
				.apply(instance, (a, b, c, d, e) -> 
				{
					Builder builder = Builder.create();
					a.ifPresent(l -> builder.addBlock(l.toArray(new Block[0])));
					b.ifPresent(l -> builder.addBlockState(l.toArray(new BlockState[0])));
					c.ifPresent(l -> builder.addBlockTags(l));
					d.ifPresent(l -> builder.addBlockProperty(l.toArray(new String[0])));
					return builder.build();
				}));
	
	protected final Optional<List<Block>> blocks;
	protected final Optional<List<BlockState>> blockStates;
	protected final Optional<List<TagKey<Block>>> blockTags;
	protected final Optional<List<String>> blockProperties;
	protected final Optional<List<PropertyMap>> blockValues;
	
	/** List of type-specific Matcher objects for each internal Optional value */
	protected final List<Matcher<?>> matchers;
	
	protected BlockPredicate(
			Optional<List<Block>> blocksIn, 
			Optional<List<BlockState>> statesIn, 
			Optional<List<TagKey<Block>>> tagsIn, 
			Optional<List<String>> blockPropertiesIn, 
			Optional<List<PropertyMap>> blockValuesIn)
	{
		blocks = blocksIn;
		blockStates = statesIn;
		blockTags = tagsIn;
		blockProperties = blockPropertiesIn;
		blockValues = blockValuesIn;
		
		matchers = List.of(
				new Matcher<Block>(blocks, (state, stream) -> stream.anyMatch(b -> state.isOf(b))), 
				new Matcher<BlockState>(blockStates, (state, stream) -> stream.anyMatch(s -> state.equals(s))), 
				new Matcher<TagKey<Block>>(blockTags, (state, stream) -> stream.anyMatch(t -> state.isIn(t))), 
				new Matcher<String>(blockProperties, (state, stream) -> stream.allMatch(s -> state.getProperties().stream().anyMatch(p -> p.getName().equalsIgnoreCase(s)))),
				new Matcher<PropertyMap>(blockValues, (state, stream) -> stream.anyMatch(map -> map.matches(state)))
				);
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
		return matchers.stream().allMatch(Matcher::isEmpty);
	}
	
	public boolean test(BlockState currentState)
	{
		return matchers.stream().anyMatch(Matcher::isPresent) && matchers.stream().anyMatch(v -> v.match(currentState));
	}
	
	private static class Matcher<T extends Object>
	{
		private final Optional<List<T>> values;
		private final BiPredicate<BlockState, Stream<T>> handlerFunc;
		
		public Matcher(Optional<List<T>> valuesIn, BiPredicate<BlockState, Stream<T>> handlerFuncIn)
		{
			values = valuesIn;
			handlerFunc = handlerFuncIn;
		}
		
		public final boolean isPresent() { return values.isPresent(); }
		
		public final boolean isEmpty() { return values.isEmpty(); }
		
		public final boolean match(BlockState state)
		{
			return !values.isEmpty() && handlerFunc.test(state, values.get().stream());
		}
	}
	
	public static class Builder
	{
		List<Block> blocks = Lists.newArrayList();
		List<BlockState> states = Lists.newArrayList();
		List<TagKey<Block>> tags = Lists.newArrayList();
		List<String> blockProperties = Lists.newArrayList();
		List<PropertyMap> blockValues = Lists.newArrayList();
		
		protected Builder() { }
		
		public static Builder create() { return new Builder(); }
		
		public Builder addBlock(Block... blocks)
		{
			for(Block block : blocks)
			{
				this.blocks.removeIf(b -> b.equals(block));
				this.blocks.add(block);
			}
			
			return this;
		}
		
		public Builder addBlockState(BlockState... states)
		{
			for(BlockState state : states)
			{
				this.states.removeIf(b -> b.equals(state));
				this.states.add(state);
			}
			
			return this;
		}
		
		@SuppressWarnings("unchecked")
		public Builder addBlockTag(TagKey<Block>... tags)
		{
			return addBlockTags(List.of(tags));
		}
		
		public Builder addBlockTags(List<TagKey<Block>> tagsIn)
		{
			tagsIn.forEach(tag -> 
			{
				this.tags.removeIf(t -> t.equals(tag));
				this.tags.add(tag);
			});
			return this;
		}
		
		public Builder addBlockProperty(String... values)
		{
			for(String value : values)
				if(!blockProperties.contains(value))
					blockProperties.add(value);
			return this;
		}
		
		public Builder addBlockValues(PropertyMap... values)
		{
			for(PropertyMap map : values)
				if(blockValues.stream().noneMatch(b -> PropertyMap.equals(b, map)))
					blockValues.add(map);
			return this;
		}
		
		/** Returns an optional of the given list or an empty optional if it is empty */
		private static <T extends Object> Optional<List<T>> orEmpty(List<T> list)
		{
			return list.isEmpty() ? Optional.empty() : Optional.of(list);
		}
		
		public BlockPredicate build()
		{
			return new BlockPredicate(
					orEmpty(blocks), 
					orEmpty(states),
					orEmpty(tags),
					orEmpty(blockProperties),
					orEmpty(blockValues));
		}
	}
}
