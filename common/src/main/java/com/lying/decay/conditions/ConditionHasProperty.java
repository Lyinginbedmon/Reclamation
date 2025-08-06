package com.lying.decay.conditions;

import java.util.Map;
import java.util.Map.Entry;

import com.google.gson.JsonObject;
import com.lying.decay.context.DecayContext;
import com.lying.init.RCDecayConditions;
import com.lying.utility.PropertyMap;

import net.minecraft.state.property.Property;
import net.minecraft.util.Identifier;

public class ConditionHasProperty extends DecayCondition
{
	private PropertyMap map = new PropertyMap();
	
	public ConditionHasProperty(Identifier idIn)
	{
		super(idIn);
	}
	
	public static <T extends Comparable<T>> ConditionHasProperty of(Map<Property<T>, T> valuesIn)
	{
		ConditionHasProperty condition = RCDecayConditions.HAS_PROPERTY.get();
		for(Entry<Property<T>, T> entry : valuesIn.entrySet())
			condition.map.put(entry.getKey(), entry.getValue());
		
		return condition;
	}
	
	public boolean check(DecayContext context)
	{
		return !map.isEmpty() && map.matches(context.currentState());
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
