package com.lying.decay;

import static com.lying.utility.RCUtils.listOrSolo;

import java.util.List;
import java.util.Optional;

import com.google.common.collect.Lists;
import com.lying.utility.BlockSaturationCalculator;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.entity.attribute.EntityAttributeModifier.Operation;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

/** Utility class for calculating decay chance */
public class DecayChance
{
	public static final Codec<DecayChance> CODEC	= RecordCodecBuilder.create(instance -> instance.group(
			Codec.FLOAT.optionalFieldOf("base_chance").forGetter(d -> d.baseChance),
			ChanceModifier.CODEC.listOf().optionalFieldOf("modifiers").forGetter(d -> listOrSolo(Optional.of(d.modifiers)).getLeft()),
			ChanceModifier.CODEC.optionalFieldOf("modifier").forGetter(d -> listOrSolo(Optional.of(d.modifiers)).getRight())
			)
				.apply(instance, (base, modifierList, modifier) -> 
				{
					DecayChance chance = new DecayChance(base, List.of());
					modifierList.ifPresent(l -> chance.modifiers.addAll(l));
					modifier.ifPresent(l -> chance.modifiers.add(l));
					return chance;
				}));
	
	private Optional<Float> baseChance = Optional.empty();
	private List<ChanceModifier> modifiers = Lists.newArrayList();
	
	protected DecayChance() { }
	
	protected DecayChance(Optional<Float> chanceIn, List<ChanceModifier> modifiersIn)
	{
		baseChance = chanceIn;
		modifiers.addAll(modifiersIn);
	}
	
	public boolean isEmpty() { return baseChance.isEmpty()  && modifiers.isEmpty(); }
	
	/** Returns a DecayChance with a base chance of 1F */
	public static DecayChance base() { return new DecayChance(); }
	
	/** Returns a DecayChance with a base chance of the given value */
	public static DecayChance base(float value)
	{
		return new DecayChance(value >= 0F ? Optional.of(value) : Optional.empty(), List.of());
	}
	
	public DecayChance addModifier(Operation mode)
	{
		modifiers.add(new ChanceModifier(1F, mode, Optional.empty()));
		return this;
	}
	
	public DecayChance addModifier(Operation mode, BlockSaturationCalculator catalyser)
	{
		modifiers.add(new ChanceModifier(1F, mode, Optional.of(catalyser)));
		return this;
	}
	
	public DecayChance addModifier(float amount, Operation mode)
	{
		modifiers.add(new ChanceModifier(amount, mode, Optional.empty()));
		return this;
	}
	
	public DecayChance addModifier(float amount, Operation mode, BlockSaturationCalculator catalyser)
	{
		modifiers.add(new ChanceModifier(amount, mode, Optional.of(catalyser)));
		return this;
	}
	
	public float chance(BlockPos pos, ServerWorld world)
	{
		float totalChance = baseChance.orElse(1F);
		
		for(ChanceModifier modifier : modifiers.stream().filter(m -> m.mode() == Operation.ADD_VALUE).toList())
			totalChance += modifier.value(world, pos);
		
		for(ChanceModifier modifier : modifiers.stream().filter(m -> m.mode() == Operation.ADD_MULTIPLIED_BASE).toList())
			totalChance *= modifier.value(world, pos);
		
		for(ChanceModifier modifier : modifiers.stream().filter(m -> m.mode() == Operation.ADD_MULTIPLIED_TOTAL).toList())
			totalChance *= modifier.value(world, pos);
		
		return totalChance;
	}
	
	private static record ChanceModifier(float amount, Operation mode, Optional<BlockSaturationCalculator> catalyser)
	{
		public static final Codec<ChanceModifier> CODEC	= RecordCodecBuilder.create(instance -> instance.group(
				Codec.FLOAT.fieldOf("amount").forGetter(ChanceModifier::amount),
				Operation.CODEC.fieldOf("operation").forGetter(ChanceModifier::mode),
				BlockSaturationCalculator.CODEC.optionalFieldOf("multiplier").forGetter(ChanceModifier::catalyser))
					.apply(instance, (a,b,c) -> new ChanceModifier(a, b, c)));
		
		public float value(ServerWorld world, BlockPos pos)
		{
			return amount() * catalyser().orElse(BlockSaturationCalculator.ofValue(1F)).calculate(world, pos);
		}
	}
}
