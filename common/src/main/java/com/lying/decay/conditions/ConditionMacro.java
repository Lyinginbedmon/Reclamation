package com.lying.decay.conditions;

import java.util.List;
import java.util.Optional;

import com.google.common.collect.Lists;
import com.google.gson.JsonObject;
import com.lying.Reclamation;
import com.lying.decay.context.DecayContext;
import com.lying.decay.functions.FunctionMacro.MacroList;
import com.lying.init.RCDecayConditions;
import com.mojang.serialization.JsonOps;

import net.minecraft.util.Identifier;

public class ConditionMacro extends DecayCondition
{
	private MacroList macros = new MacroList(List.of(), Optional.empty());
	
	public ConditionMacro(Identifier idIn)
	{
		super(idIn);
	}
	
	public static ConditionMacro of(Identifier... macroIDs)
	{
		ConditionMacro condition = RCDecayConditions.MACRO.get();
		List<Identifier> ids = Lists.newArrayList();
		for(Identifier id : macroIDs)
			ids.add(id);
		condition.macros = new MacroList(ids, Optional.empty()); 
		return condition;
	}
	
	protected boolean check(DecayContext context)
	{
		return macros.contents().stream().allMatch(m -> m.test(context));
	}
	
	protected JsonObject write(JsonObject obj)
	{
		return MacroList.CODEC.encodeStart(JsonOps.INSTANCE, macros).resultOrPartial(Reclamation.LOGGER::error).orElseThrow().getAsJsonObject();
	}
	
	protected void read(JsonObject obj)
	{
		macros = MacroList.CODEC.parse(JsonOps.INSTANCE, obj).resultOrPartial(Reclamation.LOGGER::error).orElse(new MacroList(List.of(), Optional.empty()));
	}
}
