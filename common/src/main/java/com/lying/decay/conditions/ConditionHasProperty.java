package com.lying.decay.conditions;

import java.util.Map;
import java.util.Map.Entry;

import com.google.gson.JsonObject;
import com.lying.init.RCDecayConditions;
import com.lying.utility.PropertyMap;

import net.minecraft.block.BlockState;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.property.Property;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

public class ConditionHasProperty extends DecayCondition
{
	private PropertyMap map = new PropertyMap();
	
	public ConditionHasProperty(Identifier idIn)
	{
		super(idIn);
	}
	
	public static <T extends Comparable<T>> ConditionHasProperty of(Map<Property<T>, T> valuesIn)
	{
		ConditionHasProperty condition = (ConditionHasProperty)RCDecayConditions.HAS_PROPERTY.get();
		for(Entry<Property<T>, T> entry : valuesIn.entrySet())
			condition.map.put(entry.getKey(), entry.getValue());
		
		return condition;
	}
	
	public boolean check(ServerWorld world, BlockPos pos, BlockState currentState)
	{
		return !map.isEmpty() && map.matches(currentState);
	}
	
	protected JsonObject write(JsonObject obj)
	{
		if(!map.isEmpty())
			obj.add("properties", map.toJson());
		return obj;
	}
	
	protected void read(JsonObject obj)
	{
		map.clear();
		if(obj.has("properties"))
			map = PropertyMap.fromJson(obj.get("properties"));
	}
}
