package com.lying.decay.conditions;

import net.minecraft.block.BlockState;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.biome.Biome;

public abstract class ConditionClimate extends DecayCondition
{
	protected ConditionClimate(Identifier idIn)
	{
		super(idIn);
	}
	
	public static class SkyAbove extends ConditionClimate
	{
		public SkyAbove(Identifier idIn)
		{
			super(idIn);
		}
		
		public boolean check(ServerWorld world, BlockPos pos, BlockState currentState)
		{
			return world.isSkyVisible(pos.up());
		}
	}
	
	public static class IsRaining extends ConditionClimate
	{
		public IsRaining(Identifier idIn)
		{
			super(idIn);
		}
		
		public boolean check(ServerWorld world, BlockPos pos, BlockState currentState)
		{
			return world.isRaining() && world.getBiome(pos).value().getPrecipitation(pos, world.getSeaLevel()) != Biome.Precipitation.NONE;
		}
	}
}
