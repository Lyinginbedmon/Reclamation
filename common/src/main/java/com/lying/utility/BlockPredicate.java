package com.lying.utility;

import static com.lying.utility.RCUtils.listOrSolo;

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
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtOps;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;

// TODO Add support for detecting fluid contents

/** Utility class for defining a predicate for BlockStates */
public class BlockPredicate implements Predicate<BlockState>
{
	public static final Codec<BlockPredicate> CODEC	= RecordCodecBuilder.create(instance -> instance.group(
			Registries.BLOCK.getCodec().listOf().optionalFieldOf("blocks").forGetter(p -> listOrSolo(p.blocks).getLeft()),
			Registries.BLOCK.getCodec().optionalFieldOf("block").forGetter(p -> listOrSolo(p.blocks).getRight()),
			BlockState.CODEC.listOf().optionalFieldOf("states").forGetter(p -> listOrSolo(p.blockStates).getLeft()),
			BlockState.CODEC.optionalFieldOf("state").forGetter(p -> listOrSolo(p.blockStates).getRight()),
			TagKey.codec(RegistryKeys.BLOCK).listOf().optionalFieldOf("tags").forGetter(p -> listOrSolo(p.blockTags).getLeft()),
			TagKey.codec(RegistryKeys.BLOCK).optionalFieldOf("tag").forGetter(p -> listOrSolo(p.blockTags).getRight()),
			Codec.STRING.listOf().optionalFieldOf("properties").forGetter(p -> listOrSolo(p.blockProperties).getLeft()),
			Codec.STRING.optionalFieldOf("property").forGetter(p -> listOrSolo(p.blockProperties).getRight()),
			PropertyMap.CODEC.listOf().optionalFieldOf("values").forGetter(p -> listOrSolo(p.blockValues).getLeft()),
			PropertyMap.CODEC.optionalFieldOf("value").forGetter(p -> listOrSolo(p.blockValues).getRight()))
				.apply(instance, (blockList, block, stateList, state, tagList, tag, propertyList, property, valueList, values) -> 
				{
					Builder builder = Builder.create();
					blockList.ifPresent(l -> builder.addBlock(l.toArray(new Block[0])));
					block.ifPresent(l -> builder.addBlock(l));
					
					stateList.ifPresent(l -> builder.addBlockState(l.toArray(new BlockState[0])));
					state.ifPresent(l -> builder.addBlockState(l));
					
					tagList.ifPresent(l -> builder.addBlockTags(l));
					tag.ifPresent(l -> builder.addBlockTag(l));
					
					propertyList.ifPresent(l -> builder.addBlockProperty(l.toArray(new String[0])));
					property.ifPresent(l -> builder.addBlockProperty(l));
					
					valueList.ifPresent(l -> builder.addBlockValues(l.toArray(new PropertyMap[0])));
					values.ifPresent(l -> builder.addBlockValues(l));
					return builder.build();
				}));
	
	protected final Optional<List<Block>> blocks;
	protected final Optional<List<BlockState>> blockStates;
	protected final Optional<List<TagKey<Block>>> blockTags;
	protected final Optional<List<String>> blockProperties;
	protected final Optional<List<PropertyMap>> blockValues;
	
	/** List of type-specific ListMatcher objects for each internal Optional value */
	protected final List<ListMatcher<BlockState, ?>> matchers;
	
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
				new ListMatcher<BlockState, Block>(blocks, (state, stream) -> stream.anyMatch(b -> state.isOf(b))), 
				new ListMatcher<BlockState, BlockState>(blockStates, (state, stream) -> stream.anyMatch(s -> state.equals(s))), 
				new ListMatcher<BlockState, TagKey<Block>>(blockTags, (state, stream) -> stream.anyMatch(t -> state.isIn(t))), 
				new ListMatcher<BlockState, String>(blockProperties, (state, stream) -> stream.allMatch(s -> state.getProperties().stream().anyMatch(p -> p.getName().equalsIgnoreCase(s)))),
				new ListMatcher<BlockState, PropertyMap>(blockValues, (state, stream) -> stream.anyMatch(map -> map.matches(state)))
				);
	}
	
	public JsonElement toJson()
	{
		return CODEC.encodeStart(JsonOps.INSTANCE, this).getOrThrow();
	}
	
	public static BlockPredicate fromJson(JsonObject obj)
	{
		return CODEC.parse(JsonOps.INSTANCE, obj).getOrThrow();
	}
	
	public NbtElement toNbt()
	{
		return CODEC.encodeStart(NbtOps.INSTANCE, this).getOrThrow();
	}
	
	public static BlockPredicate fromNbt(NbtElement nbt)
	{
		return CODEC.parse(NbtOps.INSTANCE, nbt).getOrThrow();
	}
	
	public boolean isEmpty()
	{
		return matchers.stream().allMatch(ListMatcher::isEmpty);
	}
	
	public boolean test(BlockState currentState)
	{
		return !isEmpty() && matchers.stream().allMatch(v -> v.match(currentState));
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
		
		public Builder addBlockTag(TagKey<Block> tags)
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
