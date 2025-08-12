package com.lying.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.world.biome.Biome;

@Mixin(Biome.class)
public interface AccessorBiome
{
	@Accessor("weather")
	Biome.Weather weather();
}
