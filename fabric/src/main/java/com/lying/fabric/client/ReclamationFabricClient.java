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
    }
}
