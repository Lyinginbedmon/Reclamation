package com.lying.utility;

import static com.lying.utility.RCUtils.listOrSolo;
import static com.lying.utility.RCUtils.orEmpty;

import java.util.List;
import java.util.Optional;

import com.google.common.collect.Lists;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.fluid.Fluid;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtOps;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;

/** Utility class for defining a predicate for BlockStates */
public class BlockPredicate extends AbstractMatcherPredicate<BlockState>
{
	public static final Codec<BlockPredicate> CODEC	= RecordCodecBuilder.create(instance -> instance.group(
			Registries.BLOCK.getCodec().listOf().optionalFieldOf("blocks").forGetter(p -> listOrSolo(p.blocks).getLeft()),
			Registries.BLOCK.getCodec().optionalFieldOf("block").forGetter(p -> listOrSolo(p.blocks).getRight()),
			Registries.FLUID.getCodec().listOf().optionalFieldOf("fluids").forGetter(p -> listOrSolo(p.fluids).getLeft()),
			Registries.FLUID.getCodec().optionalFieldOf("fluid").forGetter(p -> listOrSolo(p.fluids).getRight()),
			BlockState.CODEC.listOf().optionalFieldOf("states").forGetter(p -> listOrSolo(p.blockStates).getLeft()),
			BlockState.CODEC.optionalFieldOf("state").forGetter(p -> listOrSolo(p.blockStates).getRight()),
			TagKey.codec(RegistryKeys.BLOCK).listOf().optionalFieldOf("block_tags").forGetter(p -> listOrSolo(p.blockTags).getLeft()),
			TagKey.codec(RegistryKeys.BLOCK).optionalFieldOf("block_tag").forGetter(p -> listOrSolo(p.blockTags).getRight()),
			TagKey.codec(RegistryKeys.FLUID).listOf().optionalFieldOf("fluid_tags").forGetter(p -> listOrSolo(p.fluidTags).getLeft()),
			TagKey.codec(RegistryKeys.FLUID).optionalFieldOf("fluid_tag").forGetter(p -> listOrSolo(p.fluidTags).getRight()),
			Codec.STRING.listOf().optionalFieldOf("properties").forGetter(p -> listOrSolo(p.blockProperties).getLeft()),
			Codec.STRING.optionalFieldOf("property").forGetter(p -> listOrSolo(p.blockProperties).getRight()),
			PropertyMap.CODEC.listOf().optionalFieldOf("values").forGetter(p -> listOrSolo(p.blockValues).getLeft()),
			PropertyMap.CODEC.optionalFieldOf("value").forGetter(p -> listOrSolo(p.blockValues).getRight()))
				.apply(instance, (blockList, block, fluidList, fluid, stateList, state, blockTagList, blockTag, fluidTagList, fluidTag, propertyList, property, valueList, values) -> 
				{
					Builder builder = Builder.create();
					blockList.ifPresent(l -> builder.addBlock(l.toArray(new Block[0])));
					block.ifPresent(builder::addBlock);
					
					fluidList.ifPresent(l -> builder.addFluid(l.toArray(new Fluid[0])));
					fluid.ifPresent(builder::addFluid);
					
					stateList.ifPresent(l -> builder.addBlockState(l.toArray(new BlockState[0])));
					state.ifPresent(builder::addBlockState);
					
					blockTagList.ifPresent(builder::addBlockTags);
					blockTag.ifPresent(builder::addBlockTag);
					
					fluidTagList.ifPresent(builder::addFluidTags);
					fluidTag.ifPresent(builder::addFluidTag);
					
					propertyList.ifPresent(l -> builder.addBlockProperty(l.toArray(new String[0])));
					property.ifPresent(builder::addBlockProperty);
					
					valueList.ifPresent(l -> builder.addBlockValues(l.toArray(new PropertyMap[0])));
					values.ifPresent(builder::addBlockValues);
					return builder.build();
				}));
	
	protected final Optional<List<Block>> blocks;
	protected final Optional<List<Fluid>> fluids;
	protected final Optional<List<BlockState>> blockStates;
	protected final Optional<List<TagKey<Block>>> blockTags;
	protected final Optional<List<TagKey<Fluid>>> fluidTags;
	protected final Optional<List<String>> blockProperties;
	protected final Optional<List<PropertyMap>> blockValues;
	
	@SuppressWarnings("deprecation")
	protected BlockPredicate(
			Optional<List<Block>> blocksIn, 
			Optional<List<Fluid>> fluidsIn,
			Optional<List<BlockState>> statesIn, 
			Optional<List<TagKey<Block>>> blockTagsIn, 
			Optional<List<TagKey<Fluid>>> fluidTagsIn,
			Optional<List<String>> blockPropertiesIn, 
			Optional<List<PropertyMap>> blockValuesIn)
	{
		super(List.of(
				new ListMatcher<BlockState, Block>(blocksIn, (state, stream) -> stream.anyMatch(b -> state.isOf(b))), 
				new ListMatcher<BlockState, Fluid>(fluidsIn, (state, stream) -> stream.anyMatch(f -> state.getFluidState().getFluid().equals(f))),
				new ListMatcher<BlockState, BlockState>(statesIn, (state, stream) -> stream.anyMatch(s -> state.equals(s))), 
				new ListMatcher<BlockState, TagKey<Block>>(blockTagsIn, (state, stream) -> stream.anyMatch(t -> state.isIn(t))), 
				new ListMatcher<BlockState, TagKey<Fluid>>(fluidTagsIn, (state, stream) -> stream.anyMatch(t -> state.getFluidState().getFluid().isIn(t))),
				new ListMatcher<BlockState, String>(blockPropertiesIn, (state, stream) -> stream.allMatch(s -> state.getProperties().stream().anyMatch(p -> p.getName().equalsIgnoreCase(s)))),
				new ListMatcher<BlockState, PropertyMap>(blockValuesIn, (state, stream) -> stream.anyMatch(map -> map.matches(state)))
				));
		blocks = blocksIn;
		fluids = fluidsIn;
		blockStates = statesIn;
		blockTags = blockTagsIn;
		fluidTags = fluidTagsIn;
		blockProperties = blockPropertiesIn;
		blockValues = blockValuesIn;
		
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
	
	public static class Builder
	{
		List<Block> blocks = Lists.newArrayList();
		List<Fluid> fluids = Lists.newArrayList();
		List<BlockState> states = Lists.newArrayList();
		List<TagKey<Block>> blockTags = Lists.newArrayList();
		List<TagKey<Fluid>> fluidTags = Lists.newArrayList();
		List<String> blockProperties = Lists.newArrayList();
		List<PropertyMap> blockValues = Lists.newArrayList();
		
		protected Builder() { }
		
		public static Builder create() { return new Builder(); }
		
		public Builder addBlock(Block... blocks)
		{
			for(Block block : blocks)
			{
				this.blocks.removeIf(block::equals);
				this.blocks.add(block);
			}
			
			return this;
		}
		
		public Builder addFluid(Fluid... fluids)
		{
			for(Fluid fluid : fluids)
			{
				this.fluids.removeIf(fluid::equals);
				this.fluids.add(fluid);
			}
			return this;
		}
		
		public Builder addBlockState(BlockState... states)
		{
			for(BlockState state : states)
			{
				this.states.removeIf(state::equals);
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
				this.blockTags.removeIf(tag::equals);
				this.blockTags.add(tag);
			});
			return this;
		}
		
		public Builder addFluidTag(TagKey<Fluid> tags)
		{
			return addFluidTags(List.of(tags));
		}
		
		public Builder addFluidTags(List<TagKey<Fluid>> tagsIn)
		{
			tagsIn.forEach(tag -> 
			{
				this.fluidTags.removeIf(tag::equals);
				this.fluidTags.add(tag);
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
		
		public BlockPredicate build()
		{
			return new BlockPredicate(
					orEmpty(blocks), 
					orEmpty(fluids),
					orEmpty(states),
					orEmpty(blockTags),
					orEmpty(fluidTags),
					orEmpty(blockProperties),
					orEmpty(blockValues));
		}
	}
}
