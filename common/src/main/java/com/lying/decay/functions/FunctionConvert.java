package com.lying.decay.functions;

import com.google.gson.JsonObject;
import com.lying.decay.DecayContext;
import com.lying.init.RCDecayFunctions;
import com.lying.utility.StateGetter;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.Identifier;

public class FunctionConvert extends DecayFunction
{
	private static final String GETTER = "convert_to";
	private StateGetter states = new StateGetter();
	
	public FunctionConvert(Identifier idIn)
	{
		super(idIn);
	}
	
	public static DecayFunction to(Block... target)
	{
		FunctionConvert inst = ((FunctionConvert)RCDecayFunctions.CONVERT.get());
		for(Block state : target)
			inst.states.addBlock(state);
		return inst;
	}
	
	public static DecayFunction to(BlockState... target)
	{
		FunctionConvert inst = ((FunctionConvert)RCDecayFunctions.CONVERT.get());
		for(BlockState state : target)
			inst.states.addBlockState(state);
		return inst;
	}
	
	protected void applyTo(DecayContext context)
	{
		states.getRandom(context.random).ifPresent(state -> context.setBlockState(state));
	}
	
	protected JsonObject write(JsonObject obj)
	{
		if(!states.isEmpty())
			obj.add(GETTER, states.toJson());
		return obj;
	}
	
	protected void read(JsonObject obj)
	{
		if(obj.has(GETTER))
			states = StateGetter.fromJson(obj.get(GETTER));
	}
	
	public static class ToAir extends DecayFunction
	{
		public ToAir(Identifier idIn)
		{
			super(idIn);
		}
		
		protected void applyTo(DecayContext context)
		{
			context.setBlockState(Blocks.AIR.getDefaultState());
		}
	}
}