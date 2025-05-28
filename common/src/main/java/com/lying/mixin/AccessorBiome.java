package com.lying.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import com.lying.utility.BiomeWeatherHolder;

import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.Biome.Weather;

@Mixin(Biome.class)
public class AccessorBiome implements BiomeWeatherHolder
{
	@Shadow
	private Biome.Weather weather;
	
	public Weather getWeather() { return weather; }
}
