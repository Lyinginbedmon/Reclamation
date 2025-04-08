package com.lying.decay;

import java.util.List;
import java.util.Optional;

import com.google.common.collect.Lists;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.lying.decay.conditions.DecayCondition;
import com.lying.decay.functions.DecayFunction;
import com.lying.reference.Reference;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.block.BlockState;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;

public class DecayData
{
	public static final Codec<DecayData> CODEC	= RecordCodecBuilder.create(instance -> instance.group(
			Identifier.CODEC.optionalFieldOf("name").forGetter(d -> d.packName),
			DecayCondition.CODEC.listOf().fieldOf("conditions").forGetter(d -> d.conditions),
			DecayChance.CODEC.optionalFieldOf("likelihood").forGetter(d -> d.chance.isEmpty() ? Optional.empty() : Optional.of(d.chance)),
			DecayFunction.CODEC.listOf().fieldOf("functions").forGetter(d -> d.functions))
			.apply(instance, (name, conditions, likelihood, functions) -> 
			{
				DecayData.Builder builder = DecayData.Builder.create(likelihood.orElse(DecayChance.base()));
				name.ifPresent(s -> builder.name(s));
				conditions.forEach(condition -> builder.condition(condition));
				functions.forEach(function -> builder.function(function));
				return builder.build();
			}));
	
	private final Optional<Identifier> packName;
	private final List<DecayCondition> conditions = Lists.newArrayList();
	private final DecayChance chance;
	private final List<DecayFunction> functions = Lists.newArrayList();
	
	protected DecayData(Optional<Identifier> nameIn, List<DecayCondition> conditionsIn, DecayChance chanceIn, List<DecayFunction> functionsIn)
	{
		packName = nameIn;
		conditions.addAll(conditionsIn);
		chance = chanceIn;
		functions.addAll(functionsIn);
	}
	
	public Identifier packName() { return packName.orElse(Reference.ModInfo.prefix("unknown_entry")); }
	
	public boolean hasName() { return packName.isPresent(); }
	
	public JsonElement writeToJson(RegistryWrapper.WrapperLookup lookup)
	{
		return CODEC.encodeStart(JsonOps.INSTANCE, this).getOrThrow();
	}
	
	public static DecayData readFromJson(Identifier fileName, JsonObject json)
	{
		if(!json.has("name"))
			json.addProperty("name", fileName.toString());
		return CODEC.parse(JsonOps.INSTANCE, json).getOrThrow();
	}
	
	/** Returns true if the given world position meets all conditions of this data */
	public boolean test(ServerWorld world, BlockPos pos, BlockState state)
	{
		return conditions.stream().allMatch(p -> p.test(world, pos, state));
	}
	
	/** Returns a value between 0 and 1 representing the chance of a given decay update causing a block to decay */
	public float chance(BlockPos pos, ServerWorld world)
	{
		// If there are no catalysers, just return the base chance
		// Otherwise, calculate chance as proportional to the number of catalysers within scanning range
		return MathHelper.clamp(chance.chance(pos, world), 0F, 1F);
	}
	
	/** Sequentially applies all functions of this data to the given position */
	public void apply(ServerWorld world, BlockPos pos, BlockState currentState)
	{
		DecayContext context = new DecayContext(pos, world, currentState);
		for(DecayFunction func : functions)
			if(context.continuityBroken())
				break;
			else
				func.apply(context);
	}
	
	public static class Builder
	{
		private Optional<Identifier> packName = Optional.empty();
		
		private final List<DecayCondition> conditions = Lists.newArrayList();
		private final List<DecayFunction> functions = Lists.newArrayList();
		
		private final DecayChance likelihood;
		
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
		
		public Builder name(String string)
		{
			return name(Reference.ModInfo.prefix(string.toLowerCase().replace(" ", "_")));
		}
		
		public Builder name(Identifier nameIn)
		{
			packName = Optional.of(nameIn);
			return this;
		}
		
		public Builder condition(DecayCondition... predicatesIn)
		{
			for(DecayCondition pred : predicatesIn)
				conditions.add(pred);
			return this;
		}
		
		public Builder function(DecayFunction... functionsIn)
		{
			for(DecayFunction func : functionsIn)
				functions.add(func);
			return this;
		}
		
		public DecayData build()
		{
			return new DecayData(packName, conditions, likelihood, functions);
		}
	}
}
