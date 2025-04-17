package com.lying.decay.functions;

import java.util.Optional;

import com.google.gson.JsonObject;
import com.lying.decay.context.DecayContext;
import com.lying.init.RCDecayFunctions;
import com.lying.utility.BlockProvider;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;

public class FunctionConvert extends DecayFunction
{
	private static final String GETTER = "convert_to";
	private BlockProvider states = BlockProvider.create();
	private Optional<Boolean> shouldRunAfterPlace = Optional.empty();
	
	public FunctionConvert(Identifier idIn)
	{
		super(idIn);
	}
	
	public static DecayFunction to(BlockProvider stateIn)
	{
		FunctionConvert inst = ((FunctionConvert)RCDecayFunctions.CONVERT.get());
		inst.states = stateIn;
		return inst;
	}
	
	public static DecayFunction toBlock(Block... blocks)
	{
		return to(BlockProvider.create().addBlock(blocks));
	}
	
	public static DecayFunction toBlockState(BlockState... statesIn)
	{
		return to(BlockProvider.create().addBlockState(statesIn));
	}
	
	protected void applyTo(DecayContext context)
	{
		states.getRandom(context.random).ifPresent(state -> 
		{
			context.setBlockState(state);
			if(shouldRunAfterPlace.orElse(true))
				context.execute((pos, world) -> state.getBlock().onPlaced(world, pos, state, null, new ItemStack(state.getBlock().asItem())));
		});
	}
	
	protected JsonObject write(JsonObject obj)
	{
		if(!states.isEmpty())
			obj.add(GETTER, states.toJson());
		shouldRunAfterPlace.ifPresent(b -> obj.addProperty("update_after_placing", b));
		return obj;
	}
	
	protected void read(JsonObject obj)
	{
		if(obj.has(GETTER))
			states = BlockProvider.fromJson(obj.get(GETTER));
		if(obj.has("update_after_placing"))
			shouldRunAfterPlace = Optional.of(obj.get("update_after_placing").getAsBoolean());
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