package com.lying.decay.handler;

import static com.lying.utility.RCUtils.listOrSolo;

import java.util.List;
import java.util.Optional;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.lying.Reclamation;
import com.lying.decay.conditions.DecayCondition;
import com.lying.decay.context.DecayContext;
import com.lying.decay.functions.DecayFunction;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.Identifier;

/**
 * Similar to {@link DecayEntry}, but without any RNG.<br>
 * Used by some {@link DecayFunction} objects for ease of comprehension.
 * @author Lying
 */
public class DecayMacro extends AbstractDecayHandler
{
	public static final Codec<DecayMacro> CODEC	= RecordCodecBuilder.create(instance -> instance.group(
			Identifier.CODEC.optionalFieldOf("name").forGetter(d -> d.packName),
			DecayCondition.CODEC.listOf().optionalFieldOf("conditions").forGetter(d -> listOrSolo(Optional.of(d.conditions)).getLeft()),
			DecayCondition.CODEC.optionalFieldOf("condition").forGetter(d -> listOrSolo(Optional.of(d.conditions)).getRight()),
			DecayFunction.CODEC.listOf().optionalFieldOf("functions").forGetter(d -> listOrSolo(Optional.of(d.functions)).getLeft()),
			DecayFunction.CODEC.optionalFieldOf("function").forGetter(d -> listOrSolo(Optional.of(d.functions)).getRight())
			)
			.apply(instance, (name, conditionList, condition, functionList, function) -> 
			{
				DecayMacro.Builder builder = DecayMacro.Builder.create();
				name.ifPresent(s -> builder.name(s));
				
				conditionList.ifPresent(l -> l.forEach(builder::condition));
				condition.ifPresent(l -> builder.condition(l));
				
				functionList.ifPresent(l -> l.forEach(builder::function));
				function.ifPresent(l -> builder.function(l));
				return builder.build();
			}));
	
	protected DecayMacro(Optional<Identifier> nameIn, List<DecayCondition> conditionsIn, List<DecayFunction> functionsIn)
	{
		super(nameIn, conditionsIn, functionsIn);
	}
	
	public JsonElement writeToJson(RegistryWrapper.WrapperLookup lookup)
	{
		return CODEC.encodeStart(JsonOps.INSTANCE, this).getOrThrow();
	}
	
	public static DecayMacro readFromJson(Identifier fileName, JsonObject json)
	{
		if(!json.has("name"))
			json.addProperty("name", fileName.toString());
		return CODEC.parse(JsonOps.INSTANCE, json).getOrThrow();
	}
	
	public boolean tryToApply(DecayContext context)
	{
		if(test(context))
		{
			apply(context);
			return true;
		}
		return false;
	}
	
	public static class Builder extends AbstractDecayHandler.Builder<DecayMacro>
	{
		public static Builder create()
		{
			return new Builder();
		}
		
		public DecayMacro build()
		{
			return new DecayMacro(packName, conditions, functions);
		}
	}
}
