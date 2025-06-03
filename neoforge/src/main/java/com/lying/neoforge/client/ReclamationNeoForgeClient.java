package com.lying.neoforge.client;

import com.lying.client.ReclamationClient;
import com.lying.client.renderer.block.RaggedBannerBlockEntityRenderer;
import com.lying.init.RCBlockEntityTypes;
import com.lying.init.RCBlocks;
import com.lying.reference.Reference;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactories;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;

@EventBusSubscriber(modid = Reference.ModInfo.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public class ReclamationNeoForgeClient
{
	@SubscribeEvent
	public static void setupClient(final FMLClientSetupEvent event)
	{
		ReclamationClient.clientInit();
    	
    	registerBlockColors();
    }
    
    @SuppressWarnings("deprecation")
	private static void registerBlockColors()
    {
    	MinecraftClient client = MinecraftClient.getInstance();
    	BlockColors colors = client.getBlockColors();
    	colors.registerColorProvider(ReclamationClient.GRASS_COLOR, RCBlocks.IVY.get());
    	colors.registerColorProvider(ReclamationClient.GRASS_COLOR, RCBlocks.TINTED_LEAF_PILES);
    	colors.registerColorProvider(ReclamationClient.BIRCH_COLOR, RCBlocks.BIRCH_LEAF_PILE.get());
    	colors.registerColorProvider(ReclamationClient.SPRUCE_COLOR, RCBlocks.SPRUCE_LEAF_PILE.get());
    	
    	BlockEntityRendererFactories.register(RCBlockEntityTypes.RAGGED_BANNER.get(), RaggedBannerBlockEntityRenderer::new);
    }
}
