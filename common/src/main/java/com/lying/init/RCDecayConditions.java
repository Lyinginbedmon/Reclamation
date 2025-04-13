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
	
	public static final Supplier<DecayCondition> OR				= register("or", ConditionBoolean.Or::new);
	public static final Supplier<DecayCondition> AND			= register("and", ConditionBoolean.And::new);
	
	public static final Supplier<DecayCondition> IS_BLOCK		= register("is_block", ConditionIsBlock::new);
	public static final Supplier<DecayCondition> HAS_PROPERTY	= register("has_property", ConditionHasProperty::new);
	/** Succeeds if the sky is visible above the affected block */
	public static final Supplier<DecayCondition> SKY_ABOVE		= register("sky_above", ConditionClimate.SkyAbove::new);
	public static final Supplier<DecayCondition> IN_RAIN		= register("in_rain", ConditionClimate.IsRaining::new);
	public static final Supplier<DecayCondition> ADJACENT_TO	= register("adjacent_to", ConditionNeighbouring.Blocks::new);
	/** Succeeds if the block above the affected block is air */
	public static final Supplier<DecayCondition> AIR_ABOVE		= register("air_above", ConditionNeighbouring.AirAbove::new);
	/** Succeeds if a number of faces of the affected block are not occluded */
	public static final Supplier<DecayCondition> EXPOSED		= register("exposed", ConditionNeighbouring.Exposed::new);
	public static final Supplier<DecayCondition> ON_GROUND		= register("on_ground", ConditionNeighbouring.OnGround::new);
	public static final Supplier<DecayCondition> SUPPORTED		= register("supported", ConditionNeighbouring.Supported::new);
	public static final Supplier<DecayCondition> UNSUPPORTED	= register("unsupported", ConditionNeighbouring.Unsupported::new);
	
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
