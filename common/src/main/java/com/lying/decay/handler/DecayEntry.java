package com.lying.decay.handler;

import static com.lying.utility.RCUtils.listOrSolo;

import java.util.List;
import java.util.Optional;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.lying.Reclamation;
import com.lying.decay.DecayChance;
import com.lying.decay.conditions.DecayCondition;
import com.lying.decay.functions.DecayFunction;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;

/**
 * Holder object for decay information used by the decay algorithm.
 * @author Lying
 */
public class DecayEntry extends AbstractDecayHandler
{
	public static final Codec<DecayEntry> CODEC	= RecordCodecBuilder.create(instance -> instance.group(
			Identifier.CODEC.optionalFieldOf("name").forGetter(d -> d.packName),
			DecayChance.CODEC.optionalFieldOf("chance").forGetter(d -> d.chance.isEmpty() ? Optional.empty() : Optional.of(d.chance)),
			DecayCondition.CODEC.listOf().optionalFieldOf("conditions").forGetter(d -> listOrSolo(Optional.of(d.conditions)).getLeft()),
			DecayCondition.CODEC.optionalFieldOf("condition").forGetter(d -> listOrSolo(Optional.of(d.conditions)).getRight()),
			DecayFunction.CODEC.listOf().optionalFieldOf("functions").forGetter(d -> listOrSolo(Optional.of(d.functions)).getLeft()),
			DecayFunction.CODEC.optionalFieldOf("function").forGetter(d -> listOrSolo(Optional.of(d.functions)).getRight())
			)
			.apply(instance, (name, chance, conditionList, condition, functionList, function) -> 
			{
				DecayEntry.Builder builder = DecayEntry.Builder.create(chance.orElse(DecayChance.base()));
				name.ifPresent(s -> builder.name(s));
				
				conditionList.ifPresent(l -> l.forEach(builder::condition));
				condition.ifPresent(l -> builder.condition(l));
				
				functionList.ifPresent(l -> l.forEach(builder::function));
				function.ifPresent(l -> builder.function(l));
				return builder.build();
			}));
	
	protected final DecayChance chance;
	
	protected DecayEntry(Optional<Identifier> nameIn, List<DecayCondition> conditionsIn, DecayChance chanceIn, List<DecayFunction> functionsIn)
	{
		super(nameIn, conditionsIn, functionsIn);
		chance = chanceIn;
	}
	
	public JsonElement writeToJson(RegistryWrapper.WrapperLookup lookup)
	{
		return CODEC.encodeStart(JsonOps.INSTANCE, this).getOrThrow();
	}
	
	public static DecayEntry readFromJson(Identifier fileName, JsonObject json)
	{
		if(!json.has("name"))
			json.addProperty("name", fileName.toString());
		return CODEC.parse(JsonOps.INSTANCE, json).resultOrPartial(Reclamation.LOGGER::error).orElseThrow();
	}
	
	/** Returns a value between 0 and 1 representing the chance of a given decay update causing a block to decay */
	public float chance(BlockPos pos, ServerWorld world)
	{
		return MathHelper.clamp(chance.chance(pos, world), 0F, 1F);
	}
	
	public static class Builder extends AbstractDecayHandler.Builder<DecayEntry>
	{
		protected final DecayChance likelihood;
		
		protected Builder(DecayChance chance)
		{
			likelihood = chance;
		}
		
		protected Builder(List<DecayCondition> pred, DecayChance chance, List<DecayFunction> func)
		{
			this(chance);
			conditions.addAll(pred);
			functions.addAll(func);
		}
		
		public static Builder create(DecayChance chance)
		{
			return new Builder(chance);
		}
		
		public static Builder create()
		{
			return create(DecayChance.base());
		}
		
		public DecayEntry build()
		{
			return new DecayEntry(packName, conditions, likelihood, functions);
		}
	}
}
