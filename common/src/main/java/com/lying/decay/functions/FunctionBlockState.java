package com.lying.decay.functions;

import java.util.List;

import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.lying.decay.context.DecayContext;
import com.lying.init.RCDecayFunctions;
import com.lying.utility.PropertyMap;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.Properties;
import net.minecraft.state.property.Property;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.random.Random;

public abstract class FunctionBlockState extends DecayFunction
{
	protected FunctionBlockState(Identifier idIn)
	{
		super(idIn);
	}
	
	public static class Waterlog extends FunctionBlockState
	{
		public Waterlog(Identifier idIn)
		{
			super(idIn);
		}
		
		protected void applyTo(DecayContext context)
		{
			BlockState state = context.currentState();
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
			BlockState state = context.currentState();
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
			BlockState state = context.currentState();
			Block block = state.getBlock();
			StateManager<Block, BlockState> manager = block.getStateManager();
			
			Property<?> property = null;
			for(String propertyName : properties)
				if((property = manager.getProperty(propertyName)) != null)
					state = state.cycle(property);
			
			if(!state.equals(context.currentState()))
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
				obj.getAsJsonArray("properties").forEach(element -> properties.add(element.getAsString()));
		}
	}
	
	public static class RandomValue extends CycleValue
	{
		public RandomValue(Identifier idIn)
		{
			super(idIn);
		}
		
		public static RandomValue of(Property<?>... set)
		{
			RandomValue function = (RandomValue)RCDecayFunctions.RANDOMISE_VALUE.get();
			for(Property<?> property : set)
				function.properties.add(property.getName());
			return function;
		}
		
		protected void applyTo(DecayContext context)
		{
			BlockState state = context.currentState();
			Block block = state.getBlock();
			StateManager<Block, BlockState> manager = block.getStateManager();
			
			Property<?> property = null;
			for(String propertyName : properties)
				if((property = manager.getProperty(propertyName)) != null)
					state = randomise(property, context.random, state);
			
			if(!state.equals(context.currentState()))
				context.setBlockState(state);
		}
		
		private static <T extends Comparable<T>> BlockState randomise(Property<T> property, Random random, BlockState state)
		{
			List<T> values = property.getValues();
			return state.with(property, values.get(random.nextInt(values.size())));
		}
	}
	
	public static class CopyValue extends CycleValue
	{
		public CopyValue(Identifier idIn)
		{
			super(idIn);
		}
		
		public static CopyValue of(Property<?>... set)
		{
			CopyValue function = (CopyValue)RCDecayFunctions.COPY_VALUE.get();
			for(Property<?> property : set)
				function.properties.add(property.getName());
			return function;
		}
		
		protected void applyTo(DecayContext context)
		{
			BlockState original = context.originalState;
			BlockState current = context.currentState();
			
			StateManager<Block, BlockState> stateManager = original.getBlock().getStateManager();
			Property<?> property = null;
			for(String propertyName : properties)
				if((property = stateManager.getProperty(propertyName)) != null)
					current = copyValue(property, original, current);
			
			if(!current.equals(context.currentState()))
				context.setBlockState(current);
		}
		
		private static <T extends Comparable<T>> BlockState copyValue(Property<T> property, BlockState original, BlockState current)
		{
			return current.get(property) != null ? current.with(property, original.get(property)) : current;
		}
	}
	
	public static class SetValue extends FunctionBlockState
	{
		protected PropertyMap properties = new PropertyMap();
		
		public SetValue(Identifier idIn)
		{
			super(idIn);
		}
		
		protected void applyTo(DecayContext context)
		{
			BlockState state = properties.applyTo(context.currentState());
			if(!state.equals(context.currentState()))
				context.setBlockState(state);
		}
		
		protected JsonObject write(JsonObject obj)
		{
			if(!properties.isEmpty())
				obj.add("properties", properties.toJson());
			return obj;
		}
		
		protected void read(JsonObject obj)
		{
			properties.clear();
			if(obj.has("properties"))
				properties = PropertyMap.fromJson(obj.get("properties"));
		}
	}
}
