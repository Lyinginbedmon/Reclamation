package com.lying.decay.conditions;

import java.util.List;

import com.google.common.collect.Lists;
import com.google.gson.JsonObject;
import com.lying.decay.context.DecayContext;
import com.lying.init.RCDecayConditions;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;

import net.minecraft.util.Identifier;

public abstract class ConditionBoolean extends DecayCondition
{
	protected static final Codec<List<DecayCondition>> CODEC	= DecayCondition.CODEC.listOf();
	
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
		if(!subConditions.isEmpty())
			obj.add("set", CODEC.encodeStart(JsonOps.INSTANCE, subConditions).getOrThrow());
		return obj;
	}
	
	protected void read(JsonObject obj)
	{
		subConditions.clear();
		if(obj.has("set"))
			subConditions = CODEC.parse(JsonOps.INSTANCE, obj.get("set")).getOrThrow();
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
		
		protected boolean check(DecayContext context)
		{
			return DecayCondition.testAny(subConditions, context);
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
		
		protected boolean check(DecayContext context)
		{
			return DecayCondition.testAll(subConditions, context);
		}
	}
}
