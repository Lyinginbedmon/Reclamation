package com.lying.decay;

import java.util.List;
import java.util.Optional;

import com.google.common.collect.Lists;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.entity.attribute.EntityAttributeModifier.Operation;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

/** Utility class for calculating decay chance */
public class DecayChance
{
	public static final Codec<DecayChance> CODEC	= RecordCodecBuilder.create(instance -> instance.group(
			Codec.FLOAT.optionalFieldOf("base").forGetter(d -> d.baseChance),
			ChanceModifier.CODEC.listOf().optionalFieldOf("modifiers").forGetter(d -> d.modifiers.isEmpty() ? Optional.empty() : Optional.of(d.modifiers)))
				.apply(instance, (a,b) -> new DecayChance(a, b.orElse(List.of()))));
	
	private Optional<Float> baseChance = Optional.empty();
	private List<ChanceModifier> modifiers = Lists.newArrayList();
	
	protected DecayChance() { }
	
	protected DecayChance(Optional<Float> chanceIn, List<ChanceModifier> modifiersIn)
	{
		baseChance = chanceIn;
		modifiers.addAll(modifiersIn);
	}
	
	public boolean isEmpty() { return baseChance.isEmpty()  && modifiers.isEmpty(); }
	
	public static DecayChance base() { return new DecayChance(); }
	
	public static DecayChance base(float value)
	{
		DecayChance chance = base();
		if(value >= 0F)
			chance.baseChance = Optional.of(value);
		return chance;
	}
	
	public float chance(BlockPos pos, ServerWorld world)
	{
		float base = baseChance.orElse(1F);
		float chance = 0F;
		
		for(ChanceModifier modifier : modifiers.stream().filter(m -> m.mode() == Operation.ADD_VALUE).toList())
			chance += modifier.amount() * modifier.catalyser().calculateMultiplier(world, pos);
		
		for(ChanceModifier modifier : modifiers.stream().filter(m -> m.mode() == Operation.ADD_MULTIPLIED_BASE).toList())
			base *= modifier.amount() * modifier.catalyser().calculateMultiplier(world, pos);
		
		chance += base;
		for(ChanceModifier modifier : modifiers.stream().filter(m -> m.mode() == Operation.ADD_MULTIPLIED_TOTAL).toList())
			base *= modifier.amount() * modifier.catalyser().calculateMultiplier(world, pos);
		
		return chance;
	}
	
	public DecayChance addModifier(Operation mode, Catalysers catalyser)
	{
		modifiers.add(new ChanceModifier(1F, mode, catalyser));
		return this;
	}
	
	public DecayChance addModifier(float amount, Operation mode, Catalysers catalyser)
	{
		modifiers.add(new ChanceModifier(amount, mode, catalyser));
		return this;
	}
	
	private static record ChanceModifier(float amount, Operation mode, Catalysers catalyser)
	{
		public static final Codec<ChanceModifier> CODEC	= RecordCodecBuilder.create(instance -> instance.group(
				Codec.FLOAT.fieldOf("amount").forGetter(ChanceModifier::amount),
				Operation.CODEC.fieldOf("operation").forGetter(ChanceModifier::mode),
				Catalysers.CODEC.fieldOf("factor").forGetter(ChanceModifier::catalyser))
					.apply(instance, (a,b,c) -> new ChanceModifier(a, b, c)));
	}
}
