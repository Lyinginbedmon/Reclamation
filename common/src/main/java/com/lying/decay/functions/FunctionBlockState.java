package com.lying.decay.functions;

import java.util.List;
import java.util.Optional;

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
		// TODO Implement specification of value options
		
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
		
		@SuppressWarnings("unchecked")
		private static <T extends Comparable<T>> BlockState copyValue(Property<T> p1, BlockState s1, BlockState s2)
		{
			StateManager<Block, BlockState> stateManager = s2.getBlock().getStateManager();
			Property<T> p2 = (Property<T>)stateManager.getProperty(p1.getName());
			return p2 == null ? s2 : copyValue(p1, p2, s1, s2);
		}
		
		private static <T extends Comparable<T>, U extends Comparable<U>> BlockState copyValue(Property<T> p1, Property<U> p2, BlockState s1, BlockState s2)
		{
			String name = p1.name(s1.get(p1));
			Optional<U> value = p2.parse(name);
			return value.isPresent() ? s2.with(p2, value.get()) : s2;
		}
	}
	
	public static class SetValue extends FunctionBlockState
	{
		protected PropertyMap properties = new PropertyMap();
		
		public SetValue(Identifier idIn)
		{
			super(idIn);
		}
		
		public static SetValue of(PropertyMap map)
		{
			SetValue func = (SetValue)RCDecayFunctions.SET_STATE_VALUE.get();
			func.properties = map;
			return func;
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
