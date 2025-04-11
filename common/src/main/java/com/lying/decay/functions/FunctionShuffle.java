package com.lying.decay.functions;

import com.lying.decay.context.DecayContext;

import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.Axis;

public class FunctionShuffle extends DecayFunction
{
	public FunctionShuffle(Identifier idIn)
	{
		super(idIn);
	}
	
	protected void applyTo(DecayContext context)
	{
		if(context.isAir())
			return;
		
		for(Direction face : Direction.shuffle(context.random).stream().filter(d -> d.getAxis() != Axis.Y).toList())
		{
			BlockPos offset = context.currentPos().offset(face);
			if(context.isAir(offset))
			{
				context.moveTo(offset);
				return;
			}
		}
	}
}