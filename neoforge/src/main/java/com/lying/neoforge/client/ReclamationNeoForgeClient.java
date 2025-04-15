package com.lying.neoforge.client;

import com.lying.client.ReclamationClient;
import com.lying.init.RCBlocks;
import com.lying.reference.Reference;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.color.block.BlockColors;
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
    }
}
