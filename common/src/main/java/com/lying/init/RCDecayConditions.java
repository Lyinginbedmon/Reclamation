package com.lying.init;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

import com.lying.Reclamation;
import com.lying.decay.conditions.ConditionClimate;
import com.lying.decay.conditions.ConditionHasProperty;
import com.lying.decay.conditions.ConditionIsBlock;
import com.lying.decay.conditions.ConditionNeighbouring;
import com.lying.decay.conditions.ConditionBoolean;
import com.lying.decay.conditions.DecayCondition;
import com.lying.reference.Reference;

import net.minecraft.util.Identifier;

public class RCDecayConditions
{
	private static final Map<Identifier, Supplier<DecayCondition>> FUNCTIONS = new HashMap<>();
	
	/** Succeeds if any of its child conditions succeeds */
	public static final Supplier<DecayCondition> OR				= register("or", ConditionBoolean.Or::new);
	/** Succeeds if all of its child conditions succeeds */
	public static final Supplier<DecayCondition> AND			= register("and", ConditionBoolean.And::new);
	
	/** Succeeds if the block meets a {@link BlockPredicate} */
	public static final Supplier<DecayCondition> IS_BLOCK		= register("is_block", ConditionIsBlock::new);
	/** Succeeds if the block meets a specified {@link PropertyMap} */
	public static final Supplier<DecayCondition> HAS_PROPERTY	= register("has_property", ConditionHasProperty::new);
	/** Succeeds if the sky is visible above the affected block */
	public static final Supplier<DecayCondition> SKY_ABOVE		= register("sky_above", ConditionClimate.SkyAbove::new);
	/** Succeeds if there it is raining directly on the affected block */
	public static final Supplier<DecayCondition> IN_RAIN		= register("in_rain", ConditionClimate.IsRaining::new);
	/** Succeeds if the block is adjacent to enough blocks meeting a {@link BlockPredicate} */
	public static final Supplier<DecayCondition> ADJACENT_TO	= register("adjacent_to", ConditionNeighbouring.Blocks::new);
	/** Succeeds if the block above the affected block is air */
	public static final Supplier<DecayCondition> AIR_ABOVE		= register("air_above", ConditionNeighbouring.AirAbove::new);
	/** Succeeds if a number of faces of the affected block are not occluded */
	public static final Supplier<DecayCondition> EXPOSED		= register("exposed", ConditionNeighbouring.Exposed::new);
	/** Succeeds if the block is solidly supported by the block below itself */
	public static final Supplier<DecayCondition> ON_GROUND		= register("on_ground", ConditionNeighbouring.OnGround::new);
	/** Succeeds if the block is solidly supported by enough adjacent blocks */
	public static final Supplier<DecayCondition> SUPPORTED		= register("supported", ConditionNeighbouring.Supported::new);
	/** Succeeds if the block is not solidly supported by enough adjacent blocks */
	public static final Supplier<DecayCondition> UNSUPPORTED	= register("unsupported", ConditionNeighbouring.Unsupported::new);
	/** Succeeds if the block is air */
	public static final Supplier<DecayCondition> IS_AIR			= register("is_air", ConditionIsBlock.Air::new);
	/** Succeeds if the block is replaceable, such as vines or grass */
	public static final Supplier<DecayCondition> IS_REPLACEABLE	= register("is_replaceable", ConditionIsBlock.Replaceable::new);
	
	/*
	 * TODO Add more conditions
	 * Snow
	 * Storming
	 * Climate temperature
	 * Climate humidity
	 * Specific biome
	 * Entity scan
	 */
	
	private static Supplier<DecayCondition> register(String nameIn, Function<Identifier, DecayCondition> funcIn)
	{
		Identifier id = Reference.ModInfo.prefix(nameIn);
		Supplier<DecayCondition> supplier = () -> funcIn.apply(id);
		FUNCTIONS.put(id, supplier);
		return supplier;
	}
	
	public static Optional<DecayCondition> get(Identifier id)
	{
		return FUNCTIONS.containsKey(id) ? Optional.of(FUNCTIONS.get(id).get()) : Optional.empty();
	}
	
	public static void init()
	{
		Reclamation.LOGGER.info("# Initialised {} decay conditions", FUNCTIONS.size());
	}
}
