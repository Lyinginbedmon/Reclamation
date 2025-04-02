package com.lying.decay.functions;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

import org.jetbrains.annotations.Nullable;

import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.lying.Reclamation;
import com.lying.decay.DecayContext;
import com.lying.init.RCDecayFunctions;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.Properties;
import net.minecraft.state.property.Property;
import net.minecraft.util.Identifier;

public abstract class FunctionBlockState extends DecayFunction
{
	protected FunctionBlockState(Identifier idIn)
	{
		super(idIn);
	}
	
	protected static Optional<Property<?>> getProperty(String name, BlockState state)
	{
		return state.getProperties().stream().filter(p -> p.getName().equalsIgnoreCase(name)).findFirst();
	}
	
	@Nullable
	protected static <T extends Comparable<T>> T parsePropertyValue(String value, Property<T> property)
	{
		return property.parse(value).orElse(null);
	}
	
	@Nullable
	protected static <T extends Comparable<T>> Property<T> parseProperty(String name)
	{
		return null;
	}
	
	public static class Waterlog extends FunctionBlockState
	{
		public Waterlog(Identifier idIn)
		{
			super(idIn);
		}
		
		protected void applyTo(DecayContext context)
		{
			BlockState state = context.currentState;
			if(state.contains(Properties.WATERLOGGED) && !state.get(Properties.WATERLOGGED))
				context.setBlockState(state.with(Properties.WATERLOGGED, true));
		}
	}
	
	public static class Dehydrate extends FunctionBlockState
	{
		public Dehydrate(Identifier idIn)
		{
			super(idIn);
		}
		
		protected void applyTo(DecayContext context)
		{
			Reclamation.LOGGER.info(" # Dehydrating block at {}", context.currentPos.toString());
			BlockState state = context.currentState;
			if(state.contains(Properties.WATERLOGGED) && state.get(Properties.WATERLOGGED))
				context.setBlockState(state.with(Properties.WATERLOGGED, false));
		}
	}
	
	public static class CycleValue extends FunctionBlockState
	{
		protected List<String> properties = Lists.newArrayList();
		
		public CycleValue(Identifier idIn)
		{
			super(idIn);
		}
		
		public static CycleValue of(Property<?>... set)
		{
			CycleValue function = (CycleValue)RCDecayFunctions.CYCLE_VALUE.get();
			for(Property<?> property : set)
				function.properties.add(property.getName());
			return function;
		}
		
		protected void applyTo(DecayContext context)
		{
			BlockState state = context.currentState;
			Block block = state.getBlock();
			StateManager<Block, BlockState> manager = block.getStateManager();
			
			Property<?> property = null;
			for(String propertyName : properties)
				if((property = manager.getProperty(propertyName)) != null)
					state = state.cycle(property);
			
			if(!state.equals(context.currentState))
				context.setBlockState(state);
		}
		
		protected JsonObject write(JsonObject obj)
		{
			if(properties.isEmpty())
				return obj;
			
			JsonArray array = new JsonArray();
			properties.forEach(name -> array.add(name));
			obj.add("properties", array);
			return obj;
		}
		
		protected void read(JsonObject obj)
		{
			properties.clear();
			if(obj.has("properties"))
			{
				JsonArray array = obj.getAsJsonArray("properties");
				array.forEach(element -> properties.add(element.getAsString()));
			}
		}
	}
	
	public static class SetValue extends FunctionBlockState
	{
		protected Map<String, String> properties = new HashMap<>();
		
		public SetValue(Identifier idIn)
		{
			super(idIn);
		}
		
		protected void applyTo(DecayContext context)
		{
			BlockState state = context.currentState;
			Block block = state.getBlock();
			StateManager<Block, BlockState> manager = block.getStateManager();
			
			Property<?> property = null;
			for(Entry<String, String> entry : properties.entrySet())
				if((property = manager.getProperty(entry.getKey())) != null)
					state = setValue(property, entry.getValue(), state);
			
			if(!state.equals(context.currentState))
				context.setBlockState(state);
		}
		
		protected static <T extends Comparable<T>> BlockState setValue(Property<T> property, String string, BlockState state)
		{
			Optional<T> value = property.parse(string);
			return value.isPresent() ? state.with(property, value.get()) : state;
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
}
