package com.lying.decay.functions;

import static com.lying.utility.RCUtils.listOrSolo;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import com.google.common.collect.Lists;
import com.google.gson.JsonObject;
import com.lying.Reclamation;
import com.lying.decay.DecayMacros;
import com.lying.decay.context.DecayContext;
import com.lying.decay.handler.DecayMacro;
import com.lying.init.RCDecayFunctions;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.util.Identifier;

public class FunctionMacro extends DecayFunction
{
	private MacroList macros = new MacroList(List.of(), Optional.empty());
	
	public FunctionMacro(Identifier idIn)
	{
		super(idIn);
	}
	
	public static FunctionMacro of(Identifier... idsIn)
	{
		FunctionMacro func = (FunctionMacro)RCDecayFunctions.MACRO.get();
		func.macros = new MacroList(List.of(idsIn), Optional.empty());
		return func;
	}
	
	public FunctionMacro randomised()
	{
		macros = macros.setRandom(true);
		return this;
	}
	
	protected void applyTo(DecayContext context)
	{
		for(DecayMacro macro : macros.contents())
			if(macro.tryToApply(context))
				return;
	}
	
	protected JsonObject write(JsonObject obj)
	{
		return MacroList.CODEC.encodeStart(JsonOps.INSTANCE, macros).resultOrPartial(Reclamation.LOGGER::error).orElseThrow().getAsJsonObject();
	}
	
	protected void read(JsonObject obj)
	{
		macros = MacroList.CODEC.parse(JsonOps.INSTANCE, obj).resultOrPartial(Reclamation.LOGGER::error).orElse(new MacroList(List.of(), Optional.empty()));
	}
	
	public static record MacroList(List<Identifier> macroIDs, Optional<Boolean> isRandom)
	{
		public static final Codec<MacroList> CODEC	= RecordCodecBuilder.create(instance -> instance.group(
				Identifier.CODEC.optionalFieldOf("macro").forGetter(g -> listOrSolo(Optional.of(g.macroIDs())).getRight()),
				Identifier.CODEC.listOf().optionalFieldOf("macros").forGetter(g -> listOrSolo(Optional.of(g.macroIDs())).getLeft()),
				Codec.BOOL.optionalFieldOf("randomise").forGetter(MacroList::isRandom))
				.apply(instance, (solo, set, rand) -> 
				{
					List<Identifier> macroList = Lists.newArrayList();
					solo.ifPresent(s -> macroList.add(s));
					set.ifPresent(s -> macroList.addAll(s));
					return new MacroList(macroList, rand);
				}));
		
		public List<DecayMacro> contents()
		{
			List<DecayMacro> list = Lists.newArrayList(macroIDs.stream().map(DecayMacros.instance()::get).filter(Optional::isPresent).map(Optional::get).toList());
			if(isRandom.orElse(false) && list.size() > 1)
				Collections.shuffle(list);
			return list;
		}
		
		public MacroList setRandom(boolean rand)
		{
			return new MacroList(macroIDs, Optional.of(rand));
		}
	}
}
