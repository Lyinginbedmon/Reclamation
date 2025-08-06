package com.lying.decay.conditions;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import com.google.common.collect.Lists;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.lying.decay.context.DecayContext;
import com.lying.init.RCDecayConditions;
import com.lying.utility.BiomePredicate;
import com.lying.utility.PositionPredicate.Comparison;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.TagKey;
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
	
	protected boolean check(DecayContext context)
	{
		return func.check(context.world.get(), context.currentPos());
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
			IsWeather condition = RCDecayConditions.IS_WEATHER.get();
			condition.weather = Optional.of(weatherIn);
			return condition;
		}
		
		protected boolean check(DecayContext context)
		{
			ServerWorld world = context.world.get();
			BlockPos pos = context.currentPos();
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
	
	public static class IsBiome extends DecayCondition
	{
		private BiomePredicate options = new BiomePredicate(List.of(), List.of());
		
		public IsBiome(Identifier idIn)
		{
			super(idIn);
		}
		
		public static IsBiome create()
		{
			return RCDecayConditions.IN_BIOME.get();
		}
		
		public IsBiome addBiome(RegistryKey<Biome> biome)
		{
			List<RegistryKey<Biome>> tags = Lists.newArrayList(options.biomeList());
			if(!tags.contains(biome))
				tags.add(biome);
			options = new BiomePredicate(tags, options.biomeTags());
			return this;
		}
		
		public IsBiome addTag(TagKey<Biome> tag)
		{
			List<TagKey<Biome>> tags = Lists.newArrayList(options.biomeTags());
			if(!tags.contains(tag))
				tags.add(tag);
			options = new BiomePredicate(options.biomeList(), tags);
			return this;
		}
		
		protected boolean check(DecayContext context)
		{
			RegistryEntry<Biome> biome = context.world.get().getBiome(context.currentPos());
			return biome != null && options.test(biome);
		}
		
		protected JsonObject write(JsonObject obj)
		{
			return options.toJson().getAsJsonObject();
		}
		
		protected void read(JsonObject obj)
		{
			options = BiomePredicate.fromJson(obj);
		}
	}
	
	protected static abstract class CheckClimate extends DecayCondition
	{
		protected Optional<Float> getter = Optional.empty();
		protected Optional<Comparison> comp = Optional.empty();
		
		private final Function<Biome, Float> getterFunc;
		
		protected CheckClimate(Identifier idIn, Function<Biome, Float> funcIn)
		{
			super(idIn);
			getterFunc = funcIn;
		}
		
		protected boolean check(DecayContext context)
		{
			float temperature = getter.orElse(0.5F);
			Biome biome = context.world.get().getBiome(context.currentPos()).value();
			return comp.orElse(Comparison.GREATER_THAN_OR_EQUAL).apply(getterFunc.apply(biome), temperature);
		}
		
		protected JsonObject write(JsonObject obj)
		{
			return new BiomeData(getter, comp).toJson().getAsJsonObject();
		}
		
		protected void read(JsonObject obj)
		{
			BiomeData data = BiomeData.fromJson(obj);
			getter = data.val;
			comp = data.op;
		}
		
		private static record BiomeData(Optional<Float> val, Optional<Comparison> op)
		{
			public static final Codec<BiomeData> CODEC	= RecordCodecBuilder.create(instance -> instance.group(
					Codec.FLOAT.optionalFieldOf("value").forGetter(BiomeData::val),
					Comparison.CODEC.optionalFieldOf("operation").forGetter(BiomeData::op)
					).apply(instance, BiomeData::new));
			
			public JsonElement toJson()
			{
				return CODEC.encodeStart(JsonOps.INSTANCE, this).getOrThrow();
			}
			
			public static BiomeData fromJson(JsonObject obj)
			{
				return CODEC.parse(JsonOps.INSTANCE, obj).getOrThrow();
			}
		}
	}
	
	public static class Temperature extends CheckClimate
	{
		public Temperature(Identifier idIn)
		{
			super(idIn, b -> b.getTemperature());
		}
		
		public static Temperature of(float value)
		{
			Temperature condition = RCDecayConditions.TEMPERATURE.get();
			condition.getter = Optional.of(value);
			return condition;
		}
		
		public static Temperature of(float value, Comparison operation)
		{
			Temperature condition = RCDecayConditions.TEMPERATURE.get();
			condition.getter = Optional.of(value);
			condition.comp = Optional.of(operation);
			return condition;
		}
	}
	
	public static class Humidity extends CheckClimate
	{
		public Humidity(Identifier idIn)
		{
			super(idIn, b -> b.weather.downfall());
		}
		
		public static Humidity of(float value)
		{
			Humidity condition = RCDecayConditions.HUMIDITY.get();
			condition.getter = Optional.of(value);
			return condition;
		}
		
		public static Humidity of(float value, Comparison operation)
		{
			Humidity condition = RCDecayConditions.HUMIDITY.get();
			condition.getter = Optional.of(value);
			condition.comp = Optional.of(operation);
			return condition;
		}
	}
}
