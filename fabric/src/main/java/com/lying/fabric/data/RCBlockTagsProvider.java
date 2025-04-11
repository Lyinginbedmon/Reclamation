package com.lying.fabric.data;

import java.util.concurrent.CompletableFuture;

import com.lying.data.RCBlockTags;
import com.lying.init.RCBlocks;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider.BlockTagProvider;
import net.minecraft.registry.RegistryWrapper.WrapperLookup;

public class RCBlockTagsProvider extends BlockTagProvider
{
	public RCBlockTagsProvider(FabricDataOutput output, CompletableFuture<WrapperLookup> registriesFuture)
	{
		super(output, registriesFuture);
	}
	
	protected void configure(WrapperLookup wrapperLookup)
	{
		getOrCreateTagBuilder(RCBlockTags.RUST).add(
				RCBlocks.EXPOSED_IRON.get(),
				RCBlocks.WEATHERED_IRON.get(),
				RCBlocks.RUSTED_IRON.get());
	}
}
