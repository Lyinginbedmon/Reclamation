package com.lying.init;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

import com.lying.Reclamation;
import com.lying.decay.functions.DecayFunction;
import com.lying.decay.functions.FunctionBlockState;
import com.lying.decay.functions.FunctionBonemeal;
import com.lying.decay.functions.FunctionConvert;
import com.lying.decay.functions.FunctionFallingBlock;
import com.lying.decay.functions.FunctionShuffle;
import com.lying.reference.Reference;

import net.minecraft.util.Identifier;

public class RCDecayFunctions
{
	private static final Map<Identifier, Supplier<DecayFunction>> FUNCTIONS = new HashMap<>();
	
	/** Replaces affected block with another */
	public static final Supplier<DecayFunction> CONVERT		= register("convert", id -> new FunctionConvert(id));
	/** Replaces affected block with air */
	public static final Supplier<DecayFunction> TO_AIR		= register("to_air", id -> new FunctionConvert.ToAir(id));
	/** Moves affected block randomly to an adjacent open space */
	public static final Supplier<DecayFunction> SHUFFLE		= register("shuffle", id -> new FunctionShuffle(id));
	/** Applies bonemeal to the affected block */
	public static final Supplier<DecayFunction> BONEMEAL	= register("bonemeal", id -> new FunctionBonemeal(id));
	/** Causes the affected block to drop as a falling block if able */
	public static final Supplier<DecayFunction> FALL		= register("fall", id -> new FunctionFallingBlock.Fall(id));
	/** Causes the affected block to drop a different blockstate as a falling block if able */
	public static final Supplier<DecayFunction> DROP		= register("drop", id -> new FunctionFallingBlock.Drop(id));
	/** Waterlogs the affected block if possible */
	public static final Supplier<DecayFunction> WATERLOG	= register("waterlog", id -> new FunctionBlockState.Waterlog(id));
	/** Removes water from the affected block if possible */
	public static final Supplier<DecayFunction> DEHYDRATE	= register("dehydrate", id -> new FunctionBlockState.Dehydrate(id));
	/** Copies one or more blockstate values from the original blockstate */
	public static final Supplier<DecayFunction> COPY_VALUE	= register("copy_blockstate_value", id -> new FunctionBlockState.CopyValue(id));
	/** Cycles one or more blockstate properties */
	public static final Supplier<DecayFunction> CYCLE_VALUE	= register("cycle_blockstate_value", id -> new FunctionBlockState.CycleValue(id));
	/** Sets one or more blockstate properties to random values */
	public static final Supplier<DecayFunction> RANDOMISE_VALUE	= register("randomise_blockstate_value", id -> new FunctionBlockState.RandomValue(id));
	/** Sets the value of one or more blockstate properties */
	public static final Supplier<DecayFunction> SET_STATE_VALUE		= register("set_blockstate_value", id -> new FunctionBlockState.SetValue(id));
	
	public static final Supplier<DecayFunction> SPROUT				= register("sprout", id -> null);
	
	private static Supplier<DecayFunction> register(String nameIn, Function<Identifier, DecayFunction> funcIn)
	{
		Identifier id = Reference.ModInfo.prefix(nameIn);
		Supplier<DecayFunction> supplier = () -> funcIn.apply(id);
		FUNCTIONS.put(id, () -> funcIn.apply(id));
		return supplier;
	}
	
	public static Optional<DecayFunction> get(Identifier id)
	{
		return FUNCTIONS.containsKey(id) ? Optional.of(FUNCTIONS.get(id).get()) : Optional.empty();
	}
	
	public static void init()
	{
		Reclamation.LOGGER.info("# Initialised {} decay functions", FUNCTIONS.size());
	}
}
