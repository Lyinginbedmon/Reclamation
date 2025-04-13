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
import com.lying.decay.functions.FunctionSprout;
import com.lying.reference.Reference;

import net.minecraft.util.Identifier;

public class RCDecayFunctions
{
	private static final Map<Identifier, Supplier<DecayFunction>> FUNCTIONS = new HashMap<>();
	
	/** Replaces affected block with another */
	public static final Supplier<DecayFunction> CONVERT		= register("convert", FunctionConvert::new);
	/** Replaces affected block with air */
	public static final Supplier<DecayFunction> TO_AIR		= register("to_air", FunctionConvert.ToAir::new);
	/** Moves affected block randomly to an adjacent open space */
	public static final Supplier<DecayFunction> SHUFFLE		= register("shuffle", FunctionShuffle::new);
	/** Applies bonemeal to the affected block */
	public static final Supplier<DecayFunction> BONEMEAL	= register("bonemeal", FunctionBonemeal::new);
	/** Causes the affected block to drop as a falling block if able */
	public static final Supplier<DecayFunction> FALL		= register("fall", FunctionFallingBlock.Fall::new);
	/** Causes the affected block to drop a different blockstate as a falling block if able */
	public static final Supplier<DecayFunction> DROP		= register("drop", FunctionFallingBlock.Drop::new);
	/** Waterlogs the affected block if possible */
	public static final Supplier<DecayFunction> WATERLOG	= register("waterlog", FunctionBlockState.Waterlog::new);
	/** Removes water from the affected block if possible */
	public static final Supplier<DecayFunction> DEHYDRATE	= register("dehydrate", FunctionBlockState.Dehydrate::new);
	/** Copies one or more blockstate values from the original blockstate */
	public static final Supplier<DecayFunction> COPY_VALUE	= register("copy_blockstate_value", FunctionBlockState.CopyValue::new);
	/** Cycles one or more blockstate properties */
	public static final Supplier<DecayFunction> CYCLE_VALUE	= register("cycle_blockstate_value", FunctionBlockState.CycleValue::new);
	/** Sets one or more blockstate properties to random values */
	public static final Supplier<DecayFunction> RANDOMISE_VALUE	= register("randomise_blockstate_value", FunctionBlockState.RandomValue::new);
	/** Sets the value of one or more blockstate properties */
	public static final Supplier<DecayFunction> SET_STATE_VALUE		= register("set_blockstate_value", FunctionBlockState.SetValue::new);
	/* Places a block in an adjacent space to the affected block */
	public static final Supplier<DecayFunction> SPROUT				= register("sprout", FunctionSprout::new);
	
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
