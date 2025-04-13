package com.lying.decay;

import static com.lying.utility.RCUtils.listOrSolo;

import java.util.List;
import java.util.Optional;

import com.google.common.collect.Lists;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.lying.decay.conditions.DecayCondition;
import com.lying.decay.context.DecayContext;
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

public class DecayEntry
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
	
	private final Optional<Identifier> packName;
	private final List<DecayCondition> conditions = Lists.newArrayList();
	private final DecayChance chance;
	private final List<DecayFunction> functions = Lists.newArrayList();
	
	protected DecayEntry(Optional<Identifier> nameIn, List<DecayCondition> conditionsIn, DecayChance chanceIn, List<DecayFunction> functionsIn)
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
	
	public static DecayEntry readFromJson(Identifier fileName, JsonObject json)
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
		return MathHelper.clamp(chance.chance(pos, world), 0F, 1F);
	}
	
	/** Sequentially applies all functions of this data to the given context */
	public void apply(DecayContext context)
	{
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
		
		public DecayEntry build()
		{
			return new DecayEntry(packName, conditions, likelihood, functions);
		}
	}
}
