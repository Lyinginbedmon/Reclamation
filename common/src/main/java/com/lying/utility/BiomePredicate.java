package com.lying.utility;

import java.util.List;
import java.util.Optional;

import com.google.common.collect.Lists;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.world.biome.Biome;

public record BiomePredicate(List<RegistryKey<Biome>> biomeList, List<TagKey<Biome>> biomeTags)
{
	public static final Codec<BiomePredicate> CODEC	= RecordCodecBuilder.create(instance -> instance.group(
			RegistryKey.createCodec(RegistryKeys.BIOME).listOf().optionalFieldOf("biomes").forGetter(b -> RCUtils.listOrSolo(Optional.of(b.biomeList)).getLeft()),
			RegistryKey.createCodec(RegistryKeys.BIOME).optionalFieldOf("biome").forGetter(b -> RCUtils.listOrSolo(Optional.of(b.biomeList)).getRight()),
			TagKey.codec(RegistryKeys.BIOME).listOf().optionalFieldOf("tags").forGetter(t -> RCUtils.listOrSolo(Optional.of(t.biomeTags)).getLeft()),
			TagKey.codec(RegistryKeys.BIOME).optionalFieldOf("tag").forGetter(t -> RCUtils.listOrSolo(Optional.of(t.biomeTags)).getRight())
			).apply(instance, (biomes, biome, tags, tag) -> 
			{
				List<RegistryKey<Biome>> biomeList = Lists.newArrayList();
				biomes.ifPresent(biomeList::addAll);
				biome.ifPresent(biomeList::add);
				
				List<TagKey<Biome>> tagList = Lists.newArrayList();
				tags.ifPresent(tagList::addAll);
				tag.ifPresent(tagList::add);
				
				return new BiomePredicate(biomeList, tagList);
			}));
	
	public boolean test(RegistryEntry<Biome> biome)
	{
		return biomeList.stream().anyMatch(b -> b.equals(biome.getKey().get())) || biomeTags.stream().anyMatch(t -> biome.isIn(t));
	}
	
	public JsonElement toJson()
	{
		return CODEC.encodeStart(JsonOps.INSTANCE, this).getOrThrow();
	}
	
	public static BiomePredicate fromJson(JsonObject obj)
	{
		return CODEC.parse(JsonOps.INSTANCE, obj).getOrThrow();
	}
}
