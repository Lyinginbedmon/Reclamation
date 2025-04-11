package com.lying.decay.functions;

import com.lying.decay.context.DecayContext;

import net.minecraft.item.BoneMealItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Identifier;

public class FunctionBonemeal extends DecayFunction
{
	public FunctionBonemeal(Identifier idIn)
	{
		super(idIn);
	}
	
	protected void applyTo(DecayContext context)
	{
		context.execute((pos,world) -> BoneMealItem.useOnFertilizable(new ItemStack(Items.BONE_MEAL), world, pos));
	}
}