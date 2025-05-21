package com.lying.decay.conditions;

import java.util.Optional;

import com.google.gson.JsonObject;
import com.lying.init.RCDecayConditions;

import net.minecraft.block.BlockState;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Heightmap;
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
			super(idIn, (w,p) -> w.getTopPosition(Heightmap.Type.MOTION_BLOCKING, p).getY() <= p.getY());
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
	
	public static class IsWeather extends DecayCondition
	{
		private Optional<Weather> weather = Optional.empty();
		
		public IsWeather(Identifier idIn)
		{
			super(idIn);
		}
		
		public static IsWeather of(Weather weatherIn)
		{
			IsWeather condition = (IsWeather)RCDecayConditions.IS_WEATHER.get();
			condition.weather = Optional.of(weatherIn);
			return condition;
		}
		
		protected boolean check(ServerWorld world, BlockPos pos, BlockState currentState)
		{
			Biome biome = world.getBiome(pos).value();
			switch(weather.orElse(Weather.RAIN))
			{
				case CLEAR:
					return !world.isRaining() || biome.getPrecipitation(pos, world.getSeaLevel()) == Precipitation.NONE;
				case RAIN:
					return world.isRaining() && biome.getPrecipitation(pos, world.getSeaLevel()) == Precipitation.RAIN;
				case SNOW:
					return world.isRaining() && biome.getPrecipitation(pos, world.getSeaLevel()) == Precipitation.SNOW;
				case THUNDER:
					return world.isRaining() && world.isThundering();
			}
			return false;
		}
		
		protected JsonObject write(JsonObject obj)
		{
			if(weather.orElse(Weather.RAIN) != Weather.RAIN)
				weather.ifPresent(w -> obj.addProperty("weather", w.asString()));
			return obj;
		}
		
		protected void read(JsonObject obj)
		{
			weather = Optional.empty();
			if(obj.has("weather"))
				weather = Optional.of(Weather.fromString(obj.get("weather").getAsString()));
		}
		
		public static enum Weather implements StringIdentifiable
		{
			CLEAR,
			RAIN,
			SNOW,
			THUNDER;
			
			public String asString() { return name().toLowerCase(); }
			
			public static Weather fromString(String name)
			{
				for(Weather weather : values())
					if(weather.asString().equalsIgnoreCase(name))
						return weather;
				return RAIN;
			}
		}
	}
}
