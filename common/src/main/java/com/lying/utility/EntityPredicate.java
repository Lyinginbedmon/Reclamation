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

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtOps;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;

/** Utility class for defining a predicate for entities */
public class EntityPredicate extends AbstractMatcherPredicate<Entity>
{
	public static final Codec<EntityPredicate> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Registries.ENTITY_TYPE.getCodec().listOf().optionalFieldOf("types").forGetter(p -> listOrSolo(p.types).getLeft()),
			Registries.ENTITY_TYPE.getCodec().optionalFieldOf("type").forGetter(p -> listOrSolo(p.types).getRight()),
			TagKey.codec(RegistryKeys.ENTITY_TYPE).listOf().optionalFieldOf("tags").forGetter(p -> listOrSolo(p.tags).getLeft()),
			TagKey.codec(RegistryKeys.ENTITY_TYPE).optionalFieldOf("tag").forGetter(p -> listOrSolo(p.tags).getRight()))
				.apply(instance, (typeList, type, tagList, tag) -> 
				{
					Builder builder = Builder.create();
					
					typeList.ifPresent(l -> builder.type(l.toArray(new EntityType[0])));
					type.ifPresent(builder::type);
					
					tagList.ifPresent(l -> builder.tag(l));
					tag.ifPresent(builder::tag);
					
					return builder.build();
				}));
	
	protected final Optional<List<EntityType<?>>> types;
	protected final Optional<List<TagKey<EntityType<?>>>> tags;
	
	protected EntityPredicate(
			Optional<List<EntityType<?>>> typesIn,
			Optional<List<TagKey<EntityType<?>>>> tagsIn
			)
	{
		super(List.of(
				new ListMatcher<Entity, EntityType<?>>(typesIn, (entity, stream) -> stream.anyMatch(t -> t.equals(entity.getType()))),
				new ListMatcher<Entity, TagKey<EntityType<?>>>(tagsIn, (entity, stream) -> stream.anyMatch(t -> entity.getType().isIn(t)))
				));
		types = typesIn;
		tags = tagsIn;
	}
	
	public JsonElement toJson()
	{
		return CODEC.encodeStart(JsonOps.INSTANCE, this).getOrThrow();
	}
	
	public static EntityPredicate fromJson(JsonObject obj)
	{
		return CODEC.parse(JsonOps.INSTANCE, obj).getOrThrow();
	}
	
	public NbtElement toNbt()
	{
		return CODEC.encodeStart(NbtOps.INSTANCE, this).getOrThrow();
	}
	
	public static EntityPredicate fromNbt(NbtElement nbt)
	{
		return CODEC.parse(NbtOps.INSTANCE, nbt).getOrThrow();
	}
	
	public static class Builder
	{
		List<EntityType<?>> types = Lists.newArrayList();
		List<TagKey<EntityType<?>>> tags = Lists.newArrayList();
		
		private Builder() { }
		
		public static Builder create() { return new Builder(); }
		
		public Builder type(EntityType<?>... typesIn)
		{
			for(EntityType<?> type : typesIn)
			{
				types.removeIf(type::equals);
				types.add(type);
			}
			return this;
		}
		
		public Builder tag(List<TagKey<EntityType<?>>> tagsIn)
		{
			tagsIn.forEach(this::tag);
			return this;
		}
		
		public Builder tag(TagKey<EntityType<?>> tagIn)
		{
			tags.removeIf(tagIn::equals);
			tags.add(tagIn);
			return this;
		}
		
		public EntityPredicate build()
		{
			return new EntityPredicate(
					orEmpty(types),
					orEmpty(tags));
		}
	}
}
