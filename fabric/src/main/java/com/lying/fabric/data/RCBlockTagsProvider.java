package com.lying.fabric.data;

import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

import com.lying.data.RCTags;
import com.lying.init.RCBlocks;
import com.lying.init.RCBlocks.Concrete;
import com.lying.init.RCBlocks.Terracotta;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider.BlockTagProvider;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
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
		getOrCreateTagBuilder(BlockTags.DIRT);
		getOrCreateTagBuilder(BlockTags.LEAVES);
		getOrCreateTagBuilder(BlockTags.DOORS);
		getOrCreateTagBuilder(BlockTags.TRAPDOORS);
		getOrCreateTagBuilder(BlockTags.SHULKER_BOXES);
		
		getOrCreateTagBuilder(RCTags.CONCRETE).add(
				Blocks.BLACK_CONCRETE,
				Blocks.BLUE_CONCRETE,
				Blocks.BROWN_CONCRETE,
				Blocks.CYAN_CONCRETE,
				Blocks.GRAY_CONCRETE,
				Blocks.GREEN_CONCRETE,
				Blocks.LIGHT_BLUE_CONCRETE,
				Blocks.LIGHT_GRAY_CONCRETE,
				Blocks.LIME_CONCRETE,
				Blocks.MAGENTA_CONCRETE,
				Blocks.ORANGE_CONCRETE,
				Blocks.PINK_CONCRETE,
				Blocks.PURPLE_CONCRETE,
				Blocks.RED_CONCRETE,
				Blocks.WHITE_CONCRETE,
				Blocks.YELLOW_CONCRETE);
		
		getOrCreateTagBuilder(RCTags.RUST).add(
				RCBlocks.EXPOSED_IRON.get(),
				RCBlocks.WEATHERED_IRON.get(),
				RCBlocks.RUSTED_IRON.get());
		
		Block[] fadedTerracotta = RCBlocks.DYE_TO_TERRACOTTA.values().stream().map(Terracotta::faded).map(Supplier::get).toList().toArray(new Block[0]);
		Block[] crackedConcrete = RCBlocks.DYE_TO_CONCRETE.values().stream().map(Concrete::cracked).map(Supplier::get).toList().toArray(new Block[0]);
		getOrCreateTagBuilder(RCTags.FADED_TERRACOTTA).add(fadedTerracotta);
		getOrCreateTagBuilder(RCTags.CRACKED_CONCRETE).add(crackedConcrete);
		
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
				RCBlocks.DEEPSLATE_RUBBLE.get(),
				RCBlocks.IRON_SCRAP.get());
		getOrCreateTagBuilder(BlockTags.HOE_MINEABLE).add(RCBlocks.SOOT.get(), RCBlocks.MOLD.get());
		
		getOrCreateTagBuilder(RCTags.MOLD_IMPERVIOUS)
				.addTag(BlockTags.LEAVES)
				.addTag(BlockTags.DOORS)
				.addTag(BlockTags.TRAPDOORS)
				.addTag(BlockTags.SHULKER_BOXES)
				.addTag(BlockTags.DIRT);
	}
}
