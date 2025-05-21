package com.lying.fabric.data;

import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

import com.lying.data.RCBlockTags;
import com.lying.init.RCBlocks;
import com.lying.init.RCBlocks.Concrete;
import com.lying.init.RCBlocks.Terracotta;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider.BlockTagProvider;
import net.minecraft.block.Block;
import net.minecraft.registry.RegistryWrapper.WrapperLookup;
import net.minecraft.registry.tag.BlockTags;

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
		
		Block[] fadedTerracotta = RCBlocks.DYE_TO_TERRACOTTA.values().stream().map(Terracotta::faded).map(Supplier::get).toList().toArray(new Block[0]);
		Block[] crackedConcrete = RCBlocks.DYE_TO_CONCRETE.values().stream().map(Concrete::cracked).map(Supplier::get).toList().toArray(new Block[0]);
		getOrCreateTagBuilder(RCBlockTags.FADED_TERRACOTTA).add(fadedTerracotta);
		getOrCreateTagBuilder(RCBlockTags.CRACKED_CONCRETE).add(crackedConcrete);
		
		getOrCreateTagBuilder(BlockTags.PICKAXE_MINEABLE).add(fadedTerracotta).add(crackedConcrete).add(
				RCBlocks.CRACKED_STONE_BRICK_SLAB.get(),
				RCBlocks.CRACKED_STONE_BRICK_STAIRS.get(),
				RCBlocks.EXPOSED_IRON.get(),
				RCBlocks.WEATHERED_IRON.get(),
				RCBlocks.RUSTED_IRON.get(),
				RCBlocks.TARNISHED_GOLD.get(),
				RCBlocks.WAXED_EXPOSED_IRON.get(),
				RCBlocks.WAXED_GOLD_BLOCK.get(),
				RCBlocks.WAXED_IRON_BLOCK.get(),
				RCBlocks.WAXED_RUSTED_IRON.get(),
				RCBlocks.WAXED_TARNISHED_GOLD.get(),
				RCBlocks.WAXED_WEATHERED_IRON.get(),
				RCBlocks.STONE_RUBBLE.get(),
				RCBlocks.DEEPSLATE_RUBBLE.get());
		getOrCreateTagBuilder(BlockTags.HOE_MINEABLE).add(RCBlocks.SOOT.get());
	}
}
