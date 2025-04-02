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

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

public class DecayData
{
	public static final Codec<DecayData> CODEC	= RecordCodecBuilder.create(instance -> instance.group(
			Identifier.CODEC.optionalFieldOf("name").forGetter(d -> d.packName),
			DecayCondition.CODEC.listOf().fieldOf("conditions").forGetter(d -> d.conditions),
			Codec.FLOAT.fieldOf("base_chance").forGetter(d -> d.baseChance),
			DecayFunction.CODEC.listOf().fieldOf("functions").forGetter(d -> d.functions),
			Catalysers.CODEC.optionalFieldOf("catalysts").forGetter(d -> d.catalysis))
			.apply(instance, (n,p,l,f,c) -> 
			{
				DecayData.Builder builder = DecayData.Builder.create(l);
				n.ifPresent(s -> builder.name(s));
				c.ifPresent(cat -> builder.catalysts(cat));
				p.forEach(condition -> builder.condition(condition));
				f.forEach(function -> builder.function(function));
				return builder.build();
			}));
	
	private final Optional<Identifier> packName;
	private final List<DecayCondition> conditions = Lists.newArrayList();
	private final float baseChance;
	private final List<DecayFunction> functions = Lists.newArrayList();
	
	private final Optional<Catalysers> catalysis;
	
	protected DecayData(Optional<Identifier> nameIn, List<DecayCondition> conditionsIn, float likelihood, List<DecayFunction> functionsIn, Optional<Catalysers> catalysisIn)
	{
		packName = nameIn;
		conditions.addAll(conditionsIn);
		baseChance = MathHelper.clamp(likelihood, 0F, 1F);
		functions.addAll(functionsIn);
		catalysis = catalysisIn;
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
	public float chance(BlockPos pos, World world)
	{
		// If there are no catalysers, just return the base chance
		// Otherwise, calculate chance as proportional to the number of catalysers within scanning range
		return MathHelper.clamp(baseChance * catalysis.map(c -> c.calculateMultiplier(world, pos)).orElse(1F), 0F, 1F);
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
		
		private final float likelihood;
		
		private Catalysers catalysis = null;
		
		protected Builder(float chance)
		{
			likelihood = chance;
		}
		
		protected Builder(List<DecayCondition> pred, float chance, List<DecayFunction> func)
		{
			this(chance);
			conditions.addAll(pred);
			functions.addAll(func);
		}
		
		public static Builder create(float chance)
		{
			return new Builder(chance);
		}
		
		public static Builder create()
		{
			return create(1F);
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
		
		public Builder catalysts(Catalysers cat)
		{
			catalysis = cat;
			return this;
		}
		
		public DecayData build()
		{
			return new DecayData(packName, conditions, likelihood, functions, catalysis == null ? Optional.empty() : Optional.of(catalysis));
		}
	}
}
