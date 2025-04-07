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
	
	public static final Supplier<DecayCondition> OR				= register("or", id -> new ConditionBoolean.Or(id));
	public static final Supplier<DecayCondition> AND			= register("and", id -> new ConditionBoolean.And(id));
	
	public static final Supplier<DecayCondition> IS_BLOCK		= register("is_block", id -> new ConditionIsBlock(id));
	public static final Supplier<DecayCondition> HAS_PROPERTY	= register("has_property", id -> new ConditionHasProperty(id));
	/** Succeeds if the sky is visible above the affected block */
	public static final Supplier<DecayCondition> SKY_ABOVE		= register("sky_above", id -> new ConditionClimate.SkyAbove(id));
	public static final Supplier<DecayCondition> IN_RAIN		= register("in_rain", id -> new ConditionClimate.IsRaining(id));
	public static final Supplier<DecayCondition> ADJACENT_TO	= register("adjacent_to", id -> new ConditionNeighbouring.Blocks(id));
	/** Succeeds if the block above the affected block is air */
	public static final Supplier<DecayCondition> AIR_ABOVE		= register("air_above", id -> new ConditionNeighbouring.AirAbove(id));
	/** Succeeds if a number of faces of the affected block are not occluded */
	public static final Supplier<DecayCondition> EXPOSED		= register("exposed", id -> new ConditionNeighbouring.Exposed(id));
	public static final Supplier<DecayCondition> ON_GROUND		= register("on_ground", id -> new ConditionNeighbouring.OnGround(id));
	public static final Supplier<DecayCondition> SUPPORTED		= register("supported", id -> new ConditionNeighbouring.Supported(id));
	public static final Supplier<DecayCondition> UNSUPPORTED	= register("unsupported", id -> new ConditionNeighbouring.Unsupported(id));
	
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
