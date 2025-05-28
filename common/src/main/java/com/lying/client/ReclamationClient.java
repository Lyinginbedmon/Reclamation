package com.lying.client;

import com.lying.block.LeafPileBlock;
import com.lying.init.RCBlocks;

import dev.architectury.registry.client.rendering.RenderTypeRegistry;
import net.minecraft.block.Block;
import net.minecraft.client.color.block.BlockColorProvider;
import net.minecraft.client.color.world.BiomeColors;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.world.biome.GrassColors;

public class ReclamationClient
{
	public static final int BASE_LEAF = -12012264;
	/** BlockColorProvider for local grass colour */
	public static final BlockColorProvider GRASS_COLOR = (state, world, pos, tintIndex) -> world != null && pos != null ? BiomeColors.getGrassColor(world, pos) : GrassColors.getDefaultColor();
	public static final int SPRUCE_LEAF = -10380959;
	/** BlockColorProvider for spruce leaf colour */
	public static final BlockColorProvider SPRUCE_COLOR = (state, world, pos, tintIndex) -> SPRUCE_LEAF;
	public static final int BIRCH_LEAF = -8345771;
	/** BlockColorProvider for birch leaf colour */
	public static final BlockColorProvider BIRCH_COLOR = (state, world, pos, tintIndex) -> BIRCH_LEAF;
	
	public static void clientInit()
	{
		RenderTypeRegistry.register(RenderLayer.getCutout(), RCBlocks.IVY.get(), RCBlocks.MOLD.get());
		RenderTypeRegistry.register(RenderLayer.getCutoutMipped(), LeafPileBlock.LEAF_PILE_TO_LEAVES.keySet().stream().toList().toArray(new Block[0]));
	}
}
