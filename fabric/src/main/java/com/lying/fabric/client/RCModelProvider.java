package com.lying.fabric.client;

import java.util.Optional;

import com.lying.block.DousedTorchBlock;
import com.lying.block.LeafPileBlock;
import com.lying.client.ReclamationClient;
import com.lying.init.RCBlocks;
import com.lying.init.RCBlocks.Terracotta;
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
import net.minecraft.client.data.ItemModels;
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
	public static final Model TEMPLATE_LAYER_0 = new Model(
			Optional.of(Reference.ModInfo.prefix("block/template_layered_0")),
			Optional.of("_0"),
			TextureKey.ALL);
	public static final Model TEMPLATE_LAYER_1 = new Model(
			Optional.of(Reference.ModInfo.prefix("block/template_layered_1")),
			Optional.of("_1"),
			TextureKey.ALL);
	public static final Model TEMPLATE_LAYER_2 = new Model(
			Optional.of(Reference.ModInfo.prefix("block/template_layered_2")),
			Optional.of("_2"),
			TextureKey.ALL);
	
	public RCModelProvider(FabricDataOutput output)
	{
		super(output);
	}
	
	public void generateBlockStateModels(BlockStateModelGenerator blockStateModelGenerator)
	{
		RCBlocks.SOLID_CUBES.forEach(entry -> blockStateModelGenerator.registerSimpleCubeAll(entry.get()));
		RCBlocks.DYE_TO_TERRACOTTA.values().stream().map(Terracotta::faded).forEach(b -> blockStateModelGenerator.registerSouthDefaultHorizontalFacing(TexturedModel.TEMPLATE_GLAZED_TERRACOTTA, b.get()));
		DousedLights.register(blockStateModelGenerator);
		LeafPile.register(blockStateModelGenerator);
		registerIvy(RCBlocks.IVY.get(), RCItems.IVY.get(), blockStateModelGenerator);
		registerSlab(RCBlocks.CRACKED_STONE_BRICK_SLAB.get(), Blocks.CRACKED_STONE_BRICKS, blockStateModelGenerator);
		registerStairs(RCBlocks.CRACKED_STONE_BRICK_STAIRS.get(), Blocks.CRACKED_STONE_BRICKS, blockStateModelGenerator);
		blockStateModelGenerator.registerMultifaceBlock(RCBlocks.SOOT.get());
	}
	
	public void generateItemModels(ItemModelGenerator itemModelGenerator)
	{
		RCItems.BASIC_BLOCK_ITEMS.stream().map(e -> (BlockItem)e.get()).forEach(entry -> registerBlockModel(entry, itemModelGenerator));
		itemModelGenerator.register(RCItems.WITHERING_DUST.get(), Models.GENERATED);
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
	
	private static BlockStateVariantMap createBooleanModelMap(BooleanProperty property, Identifier trueModel, Identifier falseModel) {
		return BlockStateVariantMap.create(property)
			.register(true, BlockStateVariant.create().put(VariantSettings.MODEL, trueModel))
			.register(false, BlockStateVariant.create().put(VariantSettings.MODEL, falseModel));
	}
	
	private static class DousedLights
	{
		private static void register(BlockStateModelGenerator blockStateModelGenerator)
		{
			for(Block torch : new Block[] {
					RCBlocks.DOUSED_TORCH.get(), 
					RCBlocks.DOUSED_SOUL_TORCH.get()})
				registerTorchModel(torch, blockStateModelGenerator);
			
			for(Block lantern : new Block[] {
					RCBlocks.DOUSED_LANTERN.get(), 
					RCBlocks.DOUSED_SOUL_LANTERN.get()})
				registerLanternModel(lantern, blockStateModelGenerator);
		}
		
		private static void registerTorchModel(Block torch, BlockStateModelGenerator blockStateModelGenerator)
		{
			Identifier reg = Registries.BLOCK.getId(torch);
			Identifier wallReg = Identifier.of(reg.getNamespace(), "block/wall_"+reg.getPath());
			TextureMap tex = TextureMap.torch(torch);
			Identifier floorModel = Models.TEMPLATE_TORCH.upload(torch, tex, blockStateModelGenerator.modelCollector);
			Identifier wallModel = Models.TEMPLATE_TORCH_WALL.upload(wallReg, tex, blockStateModelGenerator.modelCollector);
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
	}
	
	private static class LeafPile
	{
		private static TextureMap leafPileTex(Block parentLeaf)
		{
			Identifier leafTexture = Registries.BLOCK.getId(parentLeaf);
			leafTexture = Identifier.of(leafTexture.getNamespace(), "block/"+leafTexture.getPath());
			return new TextureMap().put(TextureKey.ALL, leafTexture);
		}
		
		private static void register(BlockStateModelGenerator blockStateModelGenerator)
		{
			makeBlockState(RCBlocks.CHERRY_LEAF_PILE.get(), Blocks.CHERRY_LEAVES, Optional.empty(), blockStateModelGenerator);
			makeBlockState(RCBlocks.PALE_LEAF_PILE.get(), Blocks.PALE_OAK_LEAVES, Optional.empty(), blockStateModelGenerator);
			makeBlockState(RCBlocks.SPRUCE_LEAF_PILE.get(), Blocks.SPRUCE_LEAVES, Optional.of(ReclamationClient.SPRUCE_LEAF), blockStateModelGenerator);
			makeBlockState(RCBlocks.BIRCH_LEAF_PILE.get(), Blocks.BIRCH_LEAVES, Optional.of(ReclamationClient.BIRCH_LEAF), blockStateModelGenerator);
			for(LeafPileBlock pile : RCBlocks.TINTED_LEAF_PILES)
				makeBlockState(pile, pile.leaves(), Optional.of(ReclamationClient.BASE_LEAF), blockStateModelGenerator);
		}
		
		private static void makeBlockState(Block pile, Block leaves, Optional<Integer> itemTint, BlockStateModelGenerator generator)
		{
			TextureMap tex = leafPileTex(leaves);
			Identifier model0 = TEMPLATE_LAYER_0.upload(pile, tex, generator.modelCollector);
			BlockStateVariantMap map = BlockStateVariantMap.create(LeafPileBlock.LAYERS)
					.register(1, BlockStateVariant.create().put(VariantSettings.MODEL, model0))
					.register(2, BlockStateVariant.create().put(VariantSettings.MODEL, TEMPLATE_LAYER_1.upload(pile, tex, generator.modelCollector)))
					.register(3, BlockStateVariant.create().put(VariantSettings.MODEL, TEMPLATE_LAYER_2.upload(pile, tex, generator.modelCollector)));
			generator.blockStateCollector.accept(VariantsBlockStateSupplier.create(pile).coordinate(map));
			
			itemTint.ifPresentOrElse(
					tint -> generator.itemModelOutput.accept(pile.asItem(), ItemModels.tinted(model0, new ConstantTintSource(tint))), 
					() -> generator.registerParentedItemModel(pile, model0));
		}
	}
}
