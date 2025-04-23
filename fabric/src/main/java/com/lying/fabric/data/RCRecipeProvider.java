package com.lying.fabric.data;
import java.util.concurrent.CompletableFuture;

import com.lying.init.RCBlocks;
import com.lying.init.RCItems;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricRecipeProvider;
import net.minecraft.block.Blocks;
import net.minecraft.data.recipe.RecipeExporter;
import net.minecraft.data.recipe.RecipeGenerator;
import net.minecraft.data.recipe.ShapedRecipeJsonBuilder;
import net.minecraft.data.recipe.ShapelessRecipeJsonBuilder;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.Items;
import net.minecraft.recipe.book.RecipeCategory;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.RegistryWrapper.WrapperLookup;

public class RCRecipeProvider extends FabricRecipeProvider
{
	public RCRecipeProvider(FabricDataOutput output, CompletableFuture<WrapperLookup> lookup)
	{
		super(output, lookup);
	}
	
	public String getName() { return "Reclamation recipes"; }
	
	protected RecipeGenerator getRecipeGenerator(RegistryWrapper.WrapperLookup wrapperLookup, RecipeExporter exporter)
	{
		return new RecipeGenerator(wrapperLookup, exporter)
				{
					public void generate()
					{
						ShapedRecipeJsonBuilder.create(Registries.ITEM, RecipeCategory.BUILDING_BLOCKS, RCItems.CRACKED_STONE_BRICK_SLAB.get(), 6)
							.pattern("bbb").input('b', Blocks.CRACKED_STONE_BRICKS)
							.criterion(hasItem(Blocks.CRACKED_STONE_BRICKS), conditionsFromItem(Blocks.CRACKED_STONE_BRICKS)).offerTo(exporter);
						
						ShapedRecipeJsonBuilder.create(Registries.ITEM, RecipeCategory.BUILDING_BLOCKS, RCItems.CRACKED_STONE_BRICK_STAIRS.get(), 4)
							.pattern("b  ").pattern("bb ").pattern("bbb")
							.input('b', Blocks.CRACKED_STONE_BRICKS)
							.criterion(hasItem(Blocks.CRACKED_STONE_BRICKS), conditionsFromItem(Blocks.CRACKED_STONE_BRICKS)).offerTo(exporter);
						
						waxingRecipe(Blocks.IRON_BLOCK, RCBlocks.WAXED_IRON_BLOCK.get()).offerTo(exporter);
						waxingRecipe(RCBlocks.EXPOSED_IRON.get(), RCBlocks.WAXED_EXPOSED_IRON.get()).offerTo(exporter);
						waxingRecipe(RCBlocks.WEATHERED_IRON.get(), RCBlocks.WAXED_WEATHERED_IRON.get()).offerTo(exporter);
						waxingRecipe(RCBlocks.RUSTED_IRON.get(), RCBlocks.WAXED_RUSTED_IRON.get()).offerTo(exporter);
					}
					
					private ShapelessRecipeJsonBuilder waxingRecipe(ItemConvertible item, ItemConvertible output)
					{
						return ShapelessRecipeJsonBuilder.create(Registries.ITEM, RecipeCategory.BUILDING_BLOCKS, output)
							.input(item).input(Items.HONEYCOMB)
							.criterion(hasItem(item), conditionsFromItem(item))
							.criterion(hasItem(Items.HONEYCOMB), conditionsFromItem(Items.HONEYCOMB));
					}
				};
	}
}