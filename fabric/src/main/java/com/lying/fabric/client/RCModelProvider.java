package com.lying.fabric.client;

import java.util.Optional;

import com.lying.block.DousedTorchBlock;
import com.lying.block.LeafPileBlock;
import com.lying.client.ReclamationClient;
import com.lying.init.RCBlocks;
import com.lying.init.RCItems;
import com.lying.reference.Reference;

import net.fabricmc.fabric.api.client.datagen.v1.provider.FabricModelProvider;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.client.data.BlockStateModelGenerator;
import net.minecraft.client.data.BlockStateVariant;
import net.minecraft.client.data.BlockStateVariantMap;
import net.minecraft.client.data.ItemModelGenerator;
import net.minecraft.client.data.Model;
import net.minecraft.client.data.Models;
import net.minecraft.client.data.TextureKey;
import net.minecraft.client.data.TextureMap;
import net.minecraft.client.data.TexturedModel;
import net.minecraft.client.data.VariantSettings;
import net.minecraft.client.data.VariantsBlockStateSupplier;
import net.minecraft.client.render.item.tint.ConstantTintSource;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Direction;

public class RCModelProvider extends FabricModelProvider
{
	public static final TextureKey LEAF_KEY = TextureKey.of("leaf");
	public static final Model TEMPLATE_LEAF_PILE = new Model(
			Optional.of(Reference.ModInfo.prefix("block/template_leaf_pile")), 
			Optional.empty(), 
			LEAF_KEY);
	
	public RCModelProvider(FabricDataOutput output)
	{
		super(output);
	}
	
	public void generateBlockStateModels(BlockStateModelGenerator blockStateModelGenerator)
	{
		RCBlocks.SOLID_CUBES.forEach(entry -> blockStateModelGenerator.registerSimpleCubeAll(entry.get()));
		registerTorchModel(RCBlocks.DOUSED_TORCH.get(), blockStateModelGenerator);
		registerTorchModel(RCBlocks.DOUSED_SOUL_TORCH.get(), blockStateModelGenerator);
		registerLanternModel(RCBlocks.DOUSED_LANTERN.get(), blockStateModelGenerator);
		registerLanternModel(RCBlocks.DOUSED_SOUL_LANTERN.get(), blockStateModelGenerator);
		registerIvy(RCBlocks.IVY.get(), RCItems.IVY.get(), blockStateModelGenerator);
		registerSlab(RCBlocks.CRACKED_STONE_BRICK_SLAB.get(), Blocks.CRACKED_STONE_BRICKS, blockStateModelGenerator);
		registerStairs(RCBlocks.CRACKED_STONE_BRICK_STAIRS.get(), Blocks.CRACKED_STONE_BRICKS, blockStateModelGenerator);
		registerLeafPiles(blockStateModelGenerator);
		blockStateModelGenerator.registerMultifaceBlock(RCBlocks.SOOT.get());
	}
	
	public void generateItemModels(ItemModelGenerator itemModelGenerator)
	{
		RCItems.BASIC_BLOCK_ITEMS.stream().map(e -> (BlockItem)e.get()).forEach(entry -> registerBlockModel(entry, itemModelGenerator));
		itemModelGenerator.register(RCItems.WITHERING_DUST.get(), Models.GENERATED);
	}
	
	private static TextureMap leafPile(Block parentLeaf)
	{
		Identifier leafTexture = Registries.BLOCK.getId(parentLeaf);
		leafTexture = Identifier.of(leafTexture.getNamespace(), "block/"+leafTexture.getPath());
		return new TextureMap().put(LEAF_KEY, leafTexture);
	}
	
	private static TexturedModel.Factory leafPile(Block leaf, Block parent)
	{
		Identifier leafTexture = Registries.BLOCK.getId(parent);
		leafTexture = Identifier.of(leafTexture.getNamespace(), "block/"+leafTexture.getPath());
		return TexturedModel.makeFactory(b -> leafPile(parent), TEMPLATE_LEAF_PILE);
	}
	
	private static void registerLeafPiles(BlockStateModelGenerator blockStateModelGenerator)
	{
		blockStateModelGenerator.blockStateCollector.accept(BlockStateModelGenerator.createSingletonBlockState(RCBlocks.CHERRY_LEAF_PILE.get(), TEMPLATE_LEAF_PILE.upload(RCBlocks.CHERRY_LEAF_PILE.get(), leafPile(Blocks.CHERRY_LEAVES), blockStateModelGenerator.modelCollector)));
		blockStateModelGenerator.blockStateCollector.accept(BlockStateModelGenerator.createSingletonBlockState(RCBlocks.PALE_LEAF_PILE.get(), TEMPLATE_LEAF_PILE.upload(RCBlocks.PALE_LEAF_PILE.get(), leafPile(Blocks.PALE_OAK_LEAVES), blockStateModelGenerator.modelCollector)));
		blockStateModelGenerator.registerTintedBlockAndItem(RCBlocks.SPRUCE_LEAF_PILE.get(), leafPile(RCBlocks.SPRUCE_LEAF_PILE.get(), Blocks.SPRUCE_LEAVES), ReclamationClient.SPRUCE_LEAF);
		blockStateModelGenerator.registerTintedBlockAndItem(RCBlocks.BIRCH_LEAF_PILE.get(), leafPile(RCBlocks.BIRCH_LEAF_PILE.get(), Blocks.BIRCH_LEAVES), ReclamationClient.BIRCH_LEAF);
		for(LeafPileBlock pile : RCBlocks.TINTED_LEAF_PILES)
			blockStateModelGenerator.registerTintedBlockAndItem(pile, leafPile(pile, pile.leaves()), ReclamationClient.BASE_LEAF);
	}
	
	private static void registerBlockModel(BlockItem item, ItemModelGenerator itemModelGenerator)
	{
		itemModelGenerator.register(item, makeBlockModel(item));
	}
	
	private static Model makeBlockModel(BlockItem item)
	{
		Block block = item.getBlock();
		Identifier reg = Registries.BLOCK.getId(block);
		Model model = new Model(Optional.of(Identifier.of(reg.getNamespace(), "block/"+reg.getPath())), Optional.empty());
		return model;
	}
	
	private static void registerStairs(Block stairs, Block full, BlockStateModelGenerator blockStateModelGenerator)
	{
		TexturedModel textured = TexturedModel.CUBE_ALL.get(full);
		Identifier innerModel = Models.INNER_STAIRS.upload(stairs, textured.getTextures(), blockStateModelGenerator.modelCollector);
		Identifier regularModel = Models.STAIRS.upload(stairs, textured.getTextures(), blockStateModelGenerator.modelCollector);
		Identifier outerModel = Models.OUTER_STAIRS.upload(stairs, textured.getTextures(), blockStateModelGenerator.modelCollector);
		blockStateModelGenerator.blockStateCollector.accept(BlockStateModelGenerator.createStairsBlockState(stairs, innerModel, regularModel, outerModel));
	}
	
	private static void registerSlab(Block slab, Block full, BlockStateModelGenerator blockStateModelGenerator)
	{
		Identifier id = Registries.BLOCK.getId(full);
		TexturedModel textured = TexturedModel.CUBE_ALL.get(full);
		Identifier bottomModel = Models.SLAB.upload(slab, textured.getTextures(), blockStateModelGenerator.modelCollector);
		Identifier topModel = Models.SLAB_TOP.upload(slab, textured.getTextures(), blockStateModelGenerator.modelCollector);
		blockStateModelGenerator.blockStateCollector.accept(BlockStateModelGenerator.createSlabBlockState(slab, bottomModel, topModel, id));
	}
	
	private static void registerIvy(Block block, Item item, BlockStateModelGenerator blockStateModelGenerator)
	{
		Identifier id = Models.GENERATED.upload(item, TextureMap.layer0(block), blockStateModelGenerator.modelCollector);
		blockStateModelGenerator.registerTintedItemModel(block, id, new ConstantTintSource(ReclamationClient.BASE_LEAF));
		blockStateModelGenerator.registerMultifaceBlockModel(block);
	}
	
	private static void registerTorchModel(Block torch, BlockStateModelGenerator blockStateModelGenerator)
	{
		Identifier reg = Registries.BLOCK.getId(torch);
		Identifier wallReg = Identifier.of(reg.getNamespace(), "block/wall_"+reg.getPath());
		TextureMap map = TextureMap.torch(torch);
		Identifier floorModel = Models.TEMPLATE_TORCH.upload(torch, map, blockStateModelGenerator.modelCollector);
		Identifier wallModel = Models.TEMPLATE_TORCH_WALL.upload(wallReg, map, blockStateModelGenerator.modelCollector);
		blockStateModelGenerator.blockStateCollector.accept(
				VariantsBlockStateSupplier.create(torch)
					.coordinate(BlockStateVariantMap.create(DousedTorchBlock.FACING)
						.register(Direction.UP, BlockStateVariant.create().put(VariantSettings.MODEL, floorModel))
						.register(Direction.NORTH, BlockStateVariant.create().put(VariantSettings.MODEL, wallModel).put(VariantSettings.Y, VariantSettings.Rotation.R270))
						.register(Direction.EAST, BlockStateVariant.create().put(VariantSettings.MODEL, wallModel))
						.register(Direction.SOUTH, BlockStateVariant.create().put(VariantSettings.MODEL, wallModel).put(VariantSettings.Y, VariantSettings.Rotation.R90))
						.register(Direction.WEST, BlockStateVariant.create().put(VariantSettings.MODEL, wallModel).put(VariantSettings.Y, VariantSettings.Rotation.R180))));
	}
	
	private static void registerLanternModel(Block lantern, BlockStateModelGenerator blockStateModelGenerator)
	{
		Identifier identifier = TexturedModel.TEMPLATE_LANTERN.upload(lantern, blockStateModelGenerator.modelCollector);
		Identifier identifier2 = TexturedModel.TEMPLATE_HANGING_LANTERN.upload(lantern, blockStateModelGenerator.modelCollector);
		blockStateModelGenerator.blockStateCollector.accept(VariantsBlockStateSupplier.create(lantern).coordinate(createBooleanModelMap(Properties.HANGING, identifier2, identifier)));
	}
	
	private static BlockStateVariantMap createBooleanModelMap(BooleanProperty property, Identifier trueModel, Identifier falseModel) {
		return BlockStateVariantMap.create(property)
			.register(true, BlockStateVariant.create().put(VariantSettings.MODEL, trueModel))
			.register(false, BlockStateVariant.create().put(VariantSettings.MODEL, falseModel));
	}
}
