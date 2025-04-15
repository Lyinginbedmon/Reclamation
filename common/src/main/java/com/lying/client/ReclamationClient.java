package com.lying.client;

import com.lying.init.RCBlocks;

import dev.architectury.registry.client.rendering.RenderTypeRegistry;
import net.minecraft.client.color.block.BlockColorProvider;
import net.minecraft.client.color.world.BiomeColors;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.world.biome.GrassColors;

public class ReclamationClient
{
	/** BlockColorProvider for local grass colour */
	public static final BlockColorProvider GRASS_COLOR = (state, world, pos, tintIndex) -> world != null && pos != null ? BiomeColors.getGrassColor(world, pos) : GrassColors.getDefaultColor();
	
	public static void clientInit()
	{
		RenderTypeRegistry.register(RenderLayer.getCutout(), RCBlocks.IVY.get());
	}
}
