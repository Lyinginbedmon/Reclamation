package com.lying.decay.conditions;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

import com.google.gson.JsonObject;
import com.lying.init.RCDecayConditions;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.Property;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

public class ConditionHasValue extends DecayCondition
{
	private Map<String, String> properties = new HashMap<>();
	
	public ConditionHasValue(Identifier idIn)
	{
		super(idIn);
	}
	
	public static <T extends Comparable<T>> ConditionHasValue of(Map<Property<T>, T> valuesIn)
	{
		ConditionHasValue condition = (ConditionHasValue)RCDecayConditions.HAS_VALUE.get();
		for(Entry<Property<T>, T> entry : valuesIn.entrySet())
			condition.properties.put(entry.getKey().getName(), entry.getKey().name(entry.getValue()));
		return condition;
	}
	
	public boolean check(ServerWorld world, BlockPos pos, BlockState currentState)
	{
		if(properties.isEmpty())
			return false;
		
		Block block = currentState.getBlock();
		StateManager<Block, BlockState> manager = block.getStateManager();
		return properties.entrySet().stream().allMatch(entry -> 
		{
			Property<?> property = manager.getProperty(entry.getKey());
			if(property == null)
				return false;
			
			Optional<?> value = property.parse(entry.getValue());
			return value.isPresent() && currentState.get(property).equals(value.get());
		});
	}
	
	protected JsonObject write(JsonObject obj)
	{
		if(!properties.isEmpty())
		{
			JsonObject map = new JsonObject();
			properties.entrySet().forEach(entry -> map.addProperty(entry.getKey(), entry.getValue()));
			obj.add("properties", map);
		}
		return obj;
	}
	
	protected void read(JsonObject obj)
	{
		properties.clear();
		if(obj.has("properties"))
		{
			JsonObject map = obj.getAsJsonObject("properties");
			map.entrySet().forEach(entry -> properties.put(entry.getKey(), entry.getValue().getAsString()));
		}
	}
}
