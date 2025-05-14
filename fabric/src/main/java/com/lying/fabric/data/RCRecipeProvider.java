package com.lying.fabric.data;
import java.util.concurrent.CompletableFuture;

import com.lying.block.LeafPileBlock;
import com.lying.init.RCBlocks;
import com.lying.init.RCItems;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricRecipeProvider;
import net.minecraft.block.Blocks;
import net.minecraft.data.recipe.RecipeExporter;
import net.minecraft.data.recipe.RecipeGenerator;
import net.minecraft.data.recipe.ShapedRecipeJsonBuilder;
import net.minecraft.data.recipe.ShapelessRecipeJsonBuilder;
import net.minecraft.item.Item;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.recipe.book.RecipeCategory;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.RegistryWrapper.WrapperLookup;

public class RCRecipeProvider extends FabricRecipeProvider
{
	public static final int PILE_TO_LEAF_CONVERSION_RATE = 8;
	
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
						ShapelessRecipeJsonBuilder.create(Registries.ITEM, RecipeCategory.MISC, new ItemStack(RCItems.WITHERING_DUST.get(), 6))
							.input(Items.WITHER_ROSE)
							.input(Items.NETHER_STAR)
//							.input(ItemTags.SAND)
							.input(Items.WITHER_ROSE)
							.criterion(hasItem(Items.WITHER_ROSE), conditionsFromItem(Items.WITHER_ROSE))
							.criterion(hasItem(Items.NETHER_STAR), conditionsFromItem(Items.NETHER_STAR)).offerTo(exporter);
						
						ShapedRecipeJsonBuilder.create(Registries.ITEM, RecipeCategory.BUILDING_BLOCKS, RCItems.CRACKED_STONE_BRICK_SLAB.get(), 6)
							.pattern("bbb").input('b', Blocks.CRACKED_STONE_BRICKS)
							.criterion(hasItem(Blocks.CRACKED_STONE_BRICKS), conditionsFromItem(Blocks.CRACKED_STONE_BRICKS)).offerTo(exporter);
						offerStonecuttingRecipe(RecipeCategory.BUILDING_BLOCKS, RCBlocks.CRACKED_STONE_BRICK_SLAB.get(), Blocks.CRACKED_STONE_BRICKS, 2);
						
						ShapedRecipeJsonBuilder.create(Registries.ITEM, RecipeCategory.BUILDING_BLOCKS, RCItems.CRACKED_STONE_BRICK_STAIRS.get(), 4)
							.pattern("b  ").pattern("bb ").pattern("bbb")
							.input('b', Blocks.CRACKED_STONE_BRICKS)
							.criterion(hasItem(Blocks.CRACKED_STONE_BRICKS), conditionsFromItem(Blocks.CRACKED_STONE_BRICKS)).offerTo(exporter);
						offerStonecuttingRecipe(RecipeCategory.BUILDING_BLOCKS, RCBlocks.CRACKED_STONE_BRICK_STAIRS.get(), Blocks.CRACKED_STONE_BRICKS);
						
						ShapelessRecipeJsonBuilder.create(Registries.ITEM, RecipeCategory.BUILDING_BLOCKS, Blocks.MOSSY_COBBLESTONE)
							.group("mossy_cobblestone")
							.input(Blocks.COBBLESTONE).input(RCBlocks.IVY.get())
							.criterion(hasItem(Blocks.COBBLESTONE), conditionsFromItem(Blocks.COBBLESTONE))
							.criterion(hasItem(RCBlocks.IVY.get()), conditionsFromItem(RCBlocks.IVY.get())).offerTo(exporter);
						
						ShapelessRecipeJsonBuilder.create(Registries.ITEM, RecipeCategory.BUILDING_BLOCKS, Blocks.MOSSY_STONE_BRICKS)
							.group("mossy_stone_bricks")
							.input(Blocks.STONE_BRICKS).input(RCBlocks.IVY.get())
							.criterion(hasItem(Blocks.STONE_BRICKS), conditionsFromItem(Blocks.STONE_BRICKS))
							.criterion(hasItem(RCBlocks.IVY.get()), conditionsFromItem(RCBlocks.IVY.get())).offerTo(exporter);
						
						ShapelessRecipeJsonBuilder.create(Registries.ITEM, RecipeCategory.DECORATIONS, Items.BORDURE_INDENTED_BANNER_PATTERN)
							.input(Items.PAPER).input(RCBlocks.IVY.get())
							.criterion(hasItem(Items.PAPER), conditionsFromItem(Items.PAPER))
							.criterion(hasItem(RCBlocks.IVY.get()), conditionsFromItem(RCBlocks.IVY.get())).offerTo(exporter);
						
						LeafPileBlock.LEAF_PILES.stream().map(b -> (LeafPileBlock)b).forEach(p -> 
						{
							pileToLeavesRecipe(p, PILE_TO_LEAF_CONVERSION_RATE).offerTo(exporter);
							leavesToPileRecipe(p, PILE_TO_LEAF_CONVERSION_RATE).offerTo(exporter);
						});
						
//						Block sponge = Blocks.WET_SPONGE;
//						RCBlocks.DYE_TO_TERRACOTTA.entrySet().forEach(entry -> 
//						{
//							DyeItem dye = DyeItem.byColor(entry.getKey());
//							Block faded = entry.getValue().faded().get();
//							Block glazed = entry.getValue().glazed().get();
//							ShapelessRecipeJsonBuilder.create(Registries.ITEM, RecipeCategory.MISC, glazed)
//								.group("faded_to_glazed_terracotta")
//								.input(faded).input(dye).input(sponge)
//								.criterion(hasItem(dye), conditionsFromItem(dye))
//								.criterion(hasItem(faded), conditionsFromItem(faded))
//								.criterion(hasItem(sponge), conditionsFromItem(sponge))
//								.offerTo(exporter);
//						});
						
						waxingRecipe(Blocks.IRON_BLOCK, RCBlocks.WAXED_IRON_BLOCK.get()).offerTo(exporter);
						waxingRecipe(RCBlocks.EXPOSED_IRON.get(), RCBlocks.WAXED_EXPOSED_IRON.get()).offerTo(exporter);
						waxingRecipe(RCBlocks.WEATHERED_IRON.get(), RCBlocks.WAXED_WEATHERED_IRON.get()).offerTo(exporter);
						waxingRecipe(RCBlocks.RUSTED_IRON.get(), RCBlocks.WAXED_RUSTED_IRON.get()).offerTo(exporter);
						waxingRecipe(Blocks.GOLD_BLOCK, RCBlocks.WAXED_GOLD_BLOCK.get()).offerTo(exporter);
						waxingRecipe(RCBlocks.TARNISHED_GOLD.get(), RCBlocks.WAXED_TARNISHED_GOLD.get()).offerTo(exporter);
					}
					
					private ShapelessRecipeJsonBuilder waxingRecipe(ItemConvertible item, ItemConvertible output)
					{
						return ShapelessRecipeJsonBuilder.create(Registries.ITEM, RecipeCategory.BUILDING_BLOCKS, output)
							.input(item).input(Items.HONEYCOMB)
							.criterion(hasItem(item), conditionsFromItem(item))
							.criterion(hasItem(Items.HONEYCOMB), conditionsFromItem(Items.HONEYCOMB));
					}
					
					private ShapelessRecipeJsonBuilder pileToLeavesRecipe(LeafPileBlock leafPile, int count)
					{
						Item pile = leafPile.asItem();
						ShapelessRecipeJsonBuilder builder = ShapelessRecipeJsonBuilder.create(Registries.ITEM, RecipeCategory.DECORATIONS, leafPile.leaves().asItem())
							.group("leaf_pile_to_leaves")
							.criterion(hasItem(pile), conditionsFromItem(pile));
						
						for(int i=count; i>0; --i)
							builder.input(pile);
						
						return builder;
					}
					
					private ShapelessRecipeJsonBuilder leavesToPileRecipe(LeafPileBlock leafPile, int count)
					{
						Item leaves = leafPile.leaves().asItem();
						return ShapelessRecipeJsonBuilder.create(Registries.ITEM, RecipeCategory.DECORATIONS, new ItemStack(leafPile.asItem(), count))
								.group("leaves_to_leaf_piles")
								.input(leaves)
								.criterion(hasItem(leaves), conditionsFromItem(leaves));
					}
				};
	}
}