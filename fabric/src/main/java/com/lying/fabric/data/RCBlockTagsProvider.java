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
		getOrCreateTagBuilder(RCBlockTags.FADED_TERRACOTTA).add(
				RCBlocks.BLACK_FADED_TERRACOTTA.get(),
				RCBlocks.BLUE_FADED_TERRACOTTA.get(),
				RCBlocks.BROWN_FADED_TERRACOTTA.get(),
				RCBlocks.CYAN_FADED_TERRACOTTA.get(),
				RCBlocks.GRAY_FADED_TERRACOTTA.get(),
				RCBlocks.GREEN_FADED_TERRACOTTA.get(),
				RCBlocks.LIGHT_BLUE_FADED_TERRACOTTA.get(),
				RCBlocks.LIGHT_GRAY_FADED_TERRACOTTA.get(),
				RCBlocks.LIME_FADED_TERRACOTTA.get(),
				RCBlocks.MAGENTA_FADED_TERRACOTTA.get(),
				RCBlocks.ORANGE_FADED_TERRACOTTA.get(),
				RCBlocks.PINK_FADED_TERRACOTTA.get(),
				RCBlocks.PURPLE_FADED_TERRACOTTA.get(),
				RCBlocks.RED_FADED_TERRACOTTA.get(),
				RCBlocks.WHITE_FADED_TERRACOTTA.get(),
				RCBlocks.YELLOW_FADED_TERRACOTTA.get());
	}
}
