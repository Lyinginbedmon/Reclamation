package com.lying.fabric.client;

import com.lying.client.ReclamationClient;
import com.lying.client.particle.FlyParticle;
import com.lying.client.renderer.block.RaggedBannerBlockEntityRenderer;
import com.lying.init.RCBlockEntityTypes;
import com.lying.init.RCBlocks;
import com.lying.init.RCParticleTypes;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactories;

public final class ReclamationFabricClient implements ClientModInitializer
{
    public void onInitializeClient()
    {
    	ReclamationClient.clientInit();
    	
    	registerBlockColors();
    	registerParticleProviders();
    }
    
    private static void registerBlockColors()
    {
    	ColorProviderRegistry.BLOCK.register(ReclamationClient.GRASS_COLOR, RCBlocks.IVY.get());
    	ColorProviderRegistry.BLOCK.register(ReclamationClient.GRASS_COLOR, RCBlocks.TINTED_LEAF_PILES);
    	ColorProviderRegistry.BLOCK.register(ReclamationClient.BIRCH_COLOR, RCBlocks.BIRCH_LEAF_PILE.get());
    	ColorProviderRegistry.BLOCK.register(ReclamationClient.SPRUCE_COLOR, RCBlocks.SPRUCE_LEAF_PILE.get());
    	
    	BlockEntityRendererFactories.register(RCBlockEntityTypes.RAGGED_BANNER.get(), RaggedBannerBlockEntityRenderer::new);
    }
    
    private static void registerParticleProviders()
    {
    	ParticleFactoryRegistry.getInstance().register(RCParticleTypes.FLY.get(), FlyParticle.Factory::new);
    }
}
