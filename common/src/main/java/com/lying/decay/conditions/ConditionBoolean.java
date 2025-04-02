package com.lying.decay.conditions;

import java.util.List;
import java.util.Optional;

import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.lying.init.RCDecayConditions;

import net.minecraft.block.BlockState;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

public abstract class ConditionBoolean extends DecayCondition
{
	protected List<DecayCondition> subConditions = Lists.newArrayList();
	
	public ConditionBoolean(Identifier idIn)
	{
		super(idIn);
	}
	
	protected ConditionBoolean addAll(DecayCondition... conditions)
	{
		for(DecayCondition c : conditions)
			subConditions.add(c);
		return this;
	}
	
	protected JsonObject write(JsonObject obj)
	{
		name.ifPresent(n -> obj.addProperty("name", n));
		if(!subConditions.isEmpty())
		{
			JsonArray list = new JsonArray();
			subConditions.forEach(c -> list.add(c.toJson()));
			obj.add("set", list);
		}
		return obj;
	}
	
	protected void read(JsonObject obj)
	{
		name = obj.has("name") ? Optional.of(obj.get("name").getAsString()) : Optional.empty();
		subConditions.clear();
		if(obj.has("set"))
			obj.getAsJsonArray("set").forEach(element -> 
			{
				DecayCondition condition = DecayCondition.fromJson(element);
				if(condition != null)
					subConditions.add(condition);
			});
	}
	
	public static class Or extends ConditionBoolean
	{
		public Or(Identifier idIn)
		{
			super(idIn);
		}
		
		public static ConditionBoolean of(DecayCondition... conditionsIn)
		{
			return ((ConditionBoolean)RCDecayConditions.OR.get()).addAll(conditionsIn);
		}
		
		public boolean check(ServerWorld world, BlockPos pos, BlockState currentState)
		{
			return subConditions.stream().anyMatch(c -> c.test(world, pos, currentState));
		}
	}
	
	public static class And extends ConditionBoolean
	{
		public And(Identifier idIn)
		{
			super(idIn);
		}
		
		public static ConditionBoolean of(DecayCondition... conditionsIn)
		{
			return ((ConditionBoolean)RCDecayConditions.AND.get()).addAll(conditionsIn);
		}
		
		public boolean check(ServerWorld world, BlockPos pos, BlockState currentState)
		{
			return subConditions.stream().allMatch(c -> c.test(world, pos, currentState));
		}
	}
}
