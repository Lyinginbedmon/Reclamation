package com.lying.fabric.client;

import com.lying.client.ReclamationClient;
import com.lying.init.RCBlocks;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;

public final class ReclamationFabricClient implements ClientModInitializer
{
    public void onInitializeClient()
    {
    	ReclamationClient.clientInit();
    	
    	registerBlockColors();
    }
    
    private static void registerBlockColors()
    {
    	ColorProviderRegistry.BLOCK.register(ReclamationClient.GRASS_COLOR, RCBlocks.IVY.get());
    	ColorProviderRegistry.BLOCK.register(ReclamationClient.GRASS_COLOR, RCBlocks.TINTED_LEAF_PILES);
    	ColorProviderRegistry.BLOCK.register(ReclamationClient.BIRCH_COLOR, RCBlocks.BIRCH_LEAF_PILE.get());
    	ColorProviderRegistry.BLOCK.register(ReclamationClient.SPRUCE_COLOR, RCBlocks.SPRUCE_LEAF_PILE.get());
    }
}
