package com.lying.init;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

import com.lying.Reclamation;
import com.lying.decay.conditions.ConditionBoolean;
import com.lying.decay.conditions.ConditionClimate;
import com.lying.decay.conditions.ConditionExposed;
import com.lying.decay.conditions.ConditionHasProperty;
import com.lying.decay.conditions.ConditionIsBlock;
import com.lying.decay.conditions.ConditionMacro;
import com.lying.decay.conditions.ConditionNearTo;
import com.lying.decay.conditions.ConditionNeighbouring;
import com.lying.decay.conditions.ConditionPosition;
import com.lying.decay.conditions.DecayCondition;
import com.lying.decay.handler.DecayMacro;
import com.lying.reference.Reference;
import com.lying.utility.BlockPredicate;
import com.lying.utility.PositionPredicate;
import com.lying.utility.PropertyMap;

import net.minecraft.util.Identifier;

public class RCDecayConditions
{
	private static final Map<Identifier, Supplier<? extends DecayCondition>> FUNCTIONS = new HashMap<>();
	
	/** Succeeds if any of its child conditions succeeds */
	public static final Supplier<ConditionBoolean.Or> OR			= register("or", ConditionBoolean.Or::new);
	/** Succeeds if all of its child conditions succeeds */
	public static final Supplier<ConditionBoolean.And> AND			= register("and", ConditionBoolean.And::new);
	
	/** Succeeds if the block meets a {@link BlockPredicate} */
	public static final Supplier<ConditionIsBlock> IS_BLOCK		= register("is_block", ConditionIsBlock::new);
	/** Succeeds if the block is a solid cube */
	public static final Supplier<ConditionIsBlock.Solid> IS_SOLID				= register("is_solid_block", ConditionIsBlock.Solid::new);
	/** Succeeds if the block is replaceable, such as vines or grass */
	public static final Supplier<ConditionIsBlock.Replaceable> IS_REPLACEABLE	= register("is_replaceable", ConditionIsBlock.Replaceable::new);
	/** Succeeds if the block is air */
	public static final Supplier<ConditionIsBlock.Air> IS_AIR					= register("is_air", ConditionIsBlock.Air::new);
	/** Succeeds if the block meets a specified {@link PropertyMap} */
	public static final Supplier<ConditionHasProperty> HAS_PROPERTY				= register("has_property", ConditionHasProperty::new);
	/** Succeeds if the block is in a given biome */
	public static final Supplier<ConditionClimate.IsBiome> IN_BIOME				= register("in_biome", ConditionClimate.IsBiome::new);
	/** Succeeds if the sky is visible above the affected block */
	public static final Supplier<ConditionClimate.SkyAbove> SKY_ABOVE			= register("sky_above", ConditionClimate.SkyAbove::new);
	/** Succeeds if it is raining directly on the affected block */
	public static final Supplier<ConditionClimate.IsRaining> IN_RAIN			= register("in_rain", ConditionClimate.IsRaining::new);
	/** Succeeds if it is snowing directly on the affected block */
	public static final Supplier<ConditionClimate.IsSnowing> IN_SNOW			= register("in_snow", ConditionClimate.IsSnowing::new);
	/** Succeeds if it is storming directly on the affected block */
	public static final Supplier<ConditionClimate.IsStorming> IN_STORM			= register("in_storm", ConditionClimate.IsStorming::new);
	/** Succeeds if the current weather in the chunk matches */
	public static final Supplier<ConditionClimate.IsWeather> IS_WEATHER			= register("current_weather", ConditionClimate.IsWeather::new);
	/** Succeeds if the biome's temperature passes the comparison with a given value */
	public static final Supplier<ConditionClimate.Temperature> TEMPERATURE		= register("temperature", ConditionClimate.Temperature::new);
	/** Succeeds if the biome's humidity passes the comparison with a given value */
	public static final Supplier<ConditionClimate.Humidity> HUMIDITY			= register("is_humid", ConditionClimate.Humidity::new);
	/** Succeeds if the block is adjacent to enough blocks meeting a {@link BlockPredicate} */
	public static final Supplier<ConditionNeighbouring.Blocks> ADJACENT_TO		= register("adjacent_to", ConditionNeighbouring.Blocks::new);
	/** Succeeds if enough blocks within specified bounds around the block meet a {@link BlockPredicate} */
	public static final Supplier<ConditionNearTo> NEAR_TO						= register("near_to", ConditionNearTo::new);
	/** Succeeds if enough entities within specified bounds around the block meet a {@link EntityPredicate} */
	public static final Supplier<ConditionNearTo.Ent> NEAR_ENTITY				= register("near_entity", ConditionNearTo.Ent::new);
	/** Succeeds if the block above the affected block is air */
	public static final Supplier<ConditionNeighbouring.AirAbove> AIR_ABOVE		= register("air_above", ConditionNeighbouring.AirAbove::new);
	/** Succeeds if a number of faces on the affected block are not fully occluded */
	public static final Supplier<ConditionNeighbouring.Uncovered> UNCOVERED		= register("uncovered", ConditionNeighbouring.Uncovered::new);
	/** Succeeds if the block is solidly supported by the block below itself */
	public static final Supplier<ConditionNeighbouring.OnGround> ON_GROUND		= register("on_ground", ConditionNeighbouring.OnGround::new);
	/** Succeeds if the block is solidly supported by enough adjacent blocks */
	public static final Supplier<ConditionNeighbouring.Supported> SUPPORTED		= register("supported", ConditionNeighbouring.Supported::new);
	/** Succeeds if the block is not solidly supported by enough adjacent blocks */
	public static final Supplier<ConditionNeighbouring.Unsupported> UNSUPPORTED	= register("unsupported", ConditionNeighbouring.Unsupported::new);
	/** Succeeds if the block's position meets the given {@link PositionPredicate} */
	public static final Supplier<ConditionPosition> WORLD_POSITION				= register("position", ConditionPosition::new);
	/** Succeeds if the block is in the given dimension */
	public static final Supplier<ConditionPosition.Dimension> DIMENSION			= register("dimension", ConditionPosition.Dimension::new);
	/** Succeeds if the block is at least N blocks above solid ground */
	public static final Supplier<ConditionPosition.Altitude> ALTITUDE			= register("altitude", ConditionPosition.Altitude::new);
	/** Succeeds if the block is at least N blocks below solid ground */
	public static final Supplier<ConditionPosition.Depth> DEPTH					= register("depth", ConditionPosition.Depth::new);
	/** Succeeds if the position's light level meets a given comparison */
	public static final Supplier<ConditionPosition.Light> LIGHT					= register("light", ConditionPosition.Light::new);
	/** Succeeds if there exists a contiguous path through block faces to the outside */
	public static final Supplier<ConditionExposed> EXPOSED						= register("exposed", ConditionExposed::new);
	/** Succeeds if the conditions satisfy those of all given {@link DecayMacro} */
	public static final Supplier<ConditionMacro> MACRO							= register("macro", ConditionMacro::new);
	
	/** Registers the given condition under the Reclamation namespace and creates a supplier */
	private static <T extends DecayCondition> Supplier<T> register(String nameIn, Function<Identifier, T> funcIn)
	{
		return register(Reference.ModInfo.prefix(nameIn), funcIn);
	}
	
	/** Registers the given condition and creates a supplier */
	public static <T extends DecayCondition> Supplier<T> register(Identifier id, Function<Identifier, T> funcIn)
	{
		Supplier<T> supplier = () -> funcIn.apply(id);
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
