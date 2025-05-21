package com.lying.decay.handler;

import java.util.List;
import java.util.Optional;

import com.google.common.collect.Lists;
import com.google.gson.JsonElement;
import com.lying.decay.conditions.DecayCondition;
import com.lying.decay.context.DecayContext;
import com.lying.decay.functions.DecayFunction;
import com.lying.reference.Reference;

import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.Identifier;

public abstract class AbstractDecayHandler
{
	protected final Optional<Identifier> packName;
	protected final List<DecayCondition> conditions = Lists.newArrayList();
	protected final List<DecayFunction> functions = Lists.newArrayList();
	
	protected AbstractDecayHandler(Optional<Identifier> nameIn, List<DecayCondition> conditionsIn, List<DecayFunction> functionsIn)
	{
		packName = nameIn;
		conditions.addAll(conditionsIn);
		functions.addAll(functionsIn);
	}
	
	public final Identifier packName() { return packName.orElse(Reference.ModInfo.prefix("unknown_decay_handler")); }
	
	public final boolean hasName() { return packName.isPresent(); }
	
	public abstract JsonElement writeToJson(RegistryWrapper.WrapperLookup lookup);
	
	/** Returns true if the given world position meets all conditions of this data */
	public final boolean test(DecayContext context) { return DecayCondition.testAll(conditions, context); }
	
	/** Sequentially applies all functions of this data to the given context */
	public final void apply(DecayContext context)
	{
		for(DecayFunction func : functions)
			if(context.continuityBroken())
				break;
			else
				func.apply(context);
	}
	
	public static abstract class Builder<T extends AbstractDecayHandler>
	{
		protected Optional<Identifier> packName = Optional.empty();
		
		protected final List<DecayCondition> conditions = Lists.newArrayList();
		protected final List<DecayFunction> functions = Lists.newArrayList();
		
		protected Builder() { }
		
		protected Builder(List<DecayCondition> pred, List<DecayFunction> func)
		{
			this();
			conditions.addAll(pred);
			functions.addAll(func);
		}
		
		public Builder<T> name(String string)
		{
			return name(Reference.ModInfo.prefix(string.toLowerCase().replace(" ", "_")));
		}
		
		public Builder<T> name(Identifier nameIn)
		{
			packName = Optional.of(nameIn);
			return this;
		}
		
		public Builder<T> condition(DecayCondition... predicatesIn)
		{
			for(DecayCondition pred : predicatesIn)
				conditions.add(pred);
			return this;
		}
		
		public Builder<T> function(DecayFunction... functionsIn)
		{
			for(DecayFunction func : functionsIn)
				functions.add(func);
			return this;
		}
		
		public abstract T build();
	}
}
