package com.lying.decay.conditions;

import net.minecraft.block.BlockState;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Heightmap.Type;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.Biome.Precipitation;

public abstract class ConditionClimate extends DecayCondition
{
	private final ClimateFunc func;
	
	protected ConditionClimate(Identifier idIn, ClimateFunc funcIn)
	{
		super(idIn);
		func = funcIn;
	}
	
	@FunctionalInterface
	private interface ClimateFunc
	{
		public boolean check(ServerWorld world, BlockPos pos);
	}
	
	public boolean check(ServerWorld world, BlockPos pos, BlockState currentState)
	{
		return func.check(world, pos);
	}
	
	public static class SkyAbove extends ConditionClimate
	{
		public SkyAbove(Identifier idIn)
		{
			super(idIn, (w,p) -> w.isSkyVisible(p.up()));
		}
	}
	
	public static class IsRaining extends ConditionClimate
	{
		public IsRaining(Identifier idIn)
		{
			super(idIn, (w,p) -> w.hasRain(p));
		}
	}
	
	public static class IsStorming extends ConditionClimate
	{
		public IsStorming(Identifier idIn)
		{
			super(idIn, (w,p) -> w.isThundering() && w.hasRain(p));
		}
	}
	
	public static class IsSnowing extends ConditionClimate
	{
		public IsSnowing(Identifier idIn)
		{
			super(idIn, IsSnowing::checkSnow);
		}
		
		private static boolean checkSnow(ServerWorld world, BlockPos pos)
		{
			if(!world.isRaining())
				return false;
			else if(!world.isSkyVisible(pos))
				return false;
			else if(world.getTopPosition(Type.MOTION_BLOCKING, pos).getY() > pos.getY())
				return false;
			Biome biome = world.getBiome(pos).value();
			return biome.getPrecipitation(pos, world.getSeaLevel()) == Precipitation.SNOW;
		}
	}
}
