package com.lying.utility;

import java.util.HashMap;
import java.util.Optional;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.Property;

/** Serializable hashmap for storing blockstate values and testing them against a blockstate */
@SuppressWarnings("serial")
public class PropertyMap extends HashMap<String, String>
{
	public static final Codec<PropertyMap> CODEC	= Codec.of(PropertyMap::encode, PropertyMap::decode);
	
	@SuppressWarnings("unchecked")
	private static <T> DataResult<T> encode(final PropertyMap func, final DynamicOps<T> ops, final T prefix)
	{
		return ops == JsonOps.INSTANCE ? (DataResult<T>)DataResult.success(func.toJson()) : DataResult.error(() -> "Storing property maps as NBT is not supported");
	}
	
	private static <T> DataResult<Pair<PropertyMap, T>> decode(final DynamicOps<T> ops, final T input)
	{
		return ops == JsonOps.INSTANCE ? DataResult.success(Pair.of(fromJson((JsonElement)input), input)) : DataResult.error(() -> "Loading property maps from NBT is not supported");
	}
	
	public JsonElement toJson()
	{
		JsonObject obj = new JsonObject();
		entrySet().forEach(entry -> obj.addProperty(entry.getKey(), entry.getValue()));
		return obj;
	}
	
	public static PropertyMap fromJson(JsonElement obj)
	{
		PropertyMap map = new PropertyMap();
		obj.getAsJsonObject().entrySet().forEach(entry -> map.put(entry.getKey(), entry.getValue().getAsString()));
		return map;
	}
	
	public boolean matches(BlockState state)
	{
		StateManager<Block, BlockState> manager = state.getBlock().getStateManager();
		return entrySet().stream().allMatch(entry ->
		{
			Property<?> property = manager.getProperty(entry.getKey());
			if(property == null)
				return false;
			
			Optional<?> value = property.parse(entry.getValue());
			return value.isPresent() && state.get(property).equals(value.get());
		});
	}
	
	public boolean equals(Object b)
	{
		return b instanceof PropertyMap && equals(this, (PropertyMap)b);
	}
	
	public static boolean equals(PropertyMap a, PropertyMap b)
	{
		if(a.size() != b.size())
			return false;
		
		for(Entry<String, String> entry : a.entrySet())
			if(!b.containsKey(entry.getKey()) || !b.get(entry.getKey()).equals(entry.getValue()))
				return false;
		
		return true;
	}
}