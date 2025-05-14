package com.lying.fabric.client;

import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

import com.lying.block.CrackedConcreteBlock;
import com.lying.block.DousedTorchBlock;
import com.lying.block.LeafPileBlock;
import com.lying.block.RubbleBlock;
import com.lying.client.ReclamationClient;
import com.lying.init.RCBlocks;
import com.lying.init.RCBlocks.Terracotta;
import com.lying.init.RCItems;
import com.lying.reference.Reference;

import net.fabricmc.fabric.api.client.datagen.v1.provider.FabricModelProvider;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.SlabBlock;
import net.minecraft.block.enums.SlabType;
import net.minecraft.client.data.BlockStateModelGenerator;
import net.minecraft.client.data.BlockStateVariant;
import net.minecraft.client.data.BlockStateVariantMap;
import net.minecraft.client.data.ItemModelGenerator;
import net.minecraft.client.data.ItemModels;
import net.minecraft.client.data.Model;
import net.minecraft.client.data.ModelIds;
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
		RCBlocks.DYE_TO_CONCRETE.values().stream().map(Supplier::get).forEach(b -> CrackedConcrete.makeBlockState(b, blockStateModelGenerator));
		DousedLights.register(blockStateModelGenerator);
		LeafPile.register(blockStateModelGenerator);
		registerIvy(RCBlocks.IVY.get(), RCItems.IVY.get(), blockStateModelGenerator);
		registerSlab(RCBlocks.CRACKED_STONE_BRICK_SLAB.get(), Blocks.CRACKED_STONE_BRICKS, blockStateModelGenerator);
		registerStairs(RCBlocks.CRACKED_STONE_BRICK_STAIRS.get(), Blocks.CRACKED_STONE_BRICKS, blockStateModelGenerator);
		blockStateModelGenerator.registerMultifaceBlock(RCBlocks.SOOT.get());
		
		Rubble.makeBlockState(RCBlocks.RUBBLE.get(), blockStateModelGenerator);
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
		TexturedModel textured = TexturedModel.CUBE_ALL.get(full);
		Identifier bottomModel = Models.SLAB.upload(slab, textured.getTextures(), blockStateModelGenerator.modelCollector);
		Identifier topModel = Models.SLAB_TOP.upload(slab, textured.getTextures(), blockStateModelGenerator.modelCollector);
		Identifier fullModel = ModelIds.getBlockModelId(full);
		BlockStateVariantMap map = BlockStateVariantMap.create(SlabBlock.TYPE)
				.register(SlabType.BOTTOM, BlockStateVariant.create().put(VariantSettings.MODEL, bottomModel))
				.register(SlabType.TOP, BlockStateVariant.create().put(VariantSettings.MODEL, topModel))
				.register(SlabType.DOUBLE, BlockStateVariant.create().put(VariantSettings.MODEL, fullModel));
		blockStateModelGenerator.blockStateCollector.accept(VariantsBlockStateSupplier.create(slab).coordinate(map));
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
	
	private static class CrackedConcrete
	{
		public static final Model TEMPLATE_CRACKED_0 = new Model(
				Optional.of(Identifier.ofVanilla("block/cube_all")),
				Optional.of("_0"),
				TextureKey.ALL);
		public static final Model TEMPLATE_CRACKED_1 = new Model(
				Optional.of(Identifier.ofVanilla("block/cube_all")),
				Optional.of("_1"),
				TextureKey.ALL);
		public static final Model TEMPLATE_CRACKED_2 = new Model(
				Optional.of(Identifier.ofVanilla("block/cube_all")),
				Optional.of("_2"),
				TextureKey.ALL);
		public static final Model TEMPLATE_CRACKED_3 = new Model(
				Optional.of(Identifier.ofVanilla("block/cube_all")),
				Optional.of("_3"),
				TextureKey.ALL);
		
		private static TextureMap crackedTex(Block block, int index)
		{
			Identifier tex = Registries.BLOCK.getId(block);
			tex = Identifier.of(tex.getNamespace(), "block/"+tex.getPath()+"_"+index);
			return new TextureMap().put(TextureKey.ALL, tex);
		}
		
		private static void makeBlockState(Block block, BlockStateModelGenerator generator)
		{
			Identifier model0 = TEMPLATE_CRACKED_0.upload(block, crackedTex(block, 0), generator.modelCollector);
			BlockStateVariantMap map = BlockStateVariantMap.create(CrackedConcreteBlock.CRACKS)
					.register(1, BlockStateVariant.create().put(VariantSettings.MODEL, model0))
					.register(2, BlockStateVariant.create().put(VariantSettings.MODEL, TEMPLATE_CRACKED_1.upload(block, crackedTex(block, 1), generator.modelCollector)))
					.register(3, BlockStateVariant.create().put(VariantSettings.MODEL, TEMPLATE_CRACKED_2.upload(block, crackedTex(block, 2), generator.modelCollector)))
					.register(4, BlockStateVariant.create().put(VariantSettings.MODEL, TEMPLATE_CRACKED_3.upload(block, crackedTex(block, 3), generator.modelCollector)));
			
			generator.blockStateCollector.accept(VariantsBlockStateSupplier.create(block).coordinate(map));
			generator.registerParentedItemModel(block, model0);
		}
	}
	
	private static class Rubble
	{
		public static final Model TEMPLATE_RUBBLE_0 = new Model(
				Optional.of(Reference.ModInfo.prefix("block/template_rubble_0")),
				Optional.of("_0"),
				TextureKey.ALL);
		public static final Model TEMPLATE_RUBBLE_1 = new Model(
				Optional.of(Reference.ModInfo.prefix("block/template_rubble_1")),
				Optional.of("_1"),
				TextureKey.ALL);
		public static final Model TEMPLATE_RUBBLE_2 = new Model(
				Optional.of(Reference.ModInfo.prefix("block/template_rubble_2")),
				Optional.of("_2"),
				TextureKey.ALL);
		public static final Model TEMPLATE_RUBBLE_3 = new Model(
				Optional.of(Reference.ModInfo.prefix("block/template_rubble_3")),
				Optional.of("_3"),
				TextureKey.ALL);
		
		private static TextureMap rubbleTex(Block cobble)
		{
			Identifier tex = Registries.BLOCK.getId(Blocks.COBBLESTONE);
			tex = Identifier.of(tex.getNamespace(), "block/"+tex.getPath());
			return new TextureMap().put(TextureKey.ALL, tex);
		}
		
		private static List<BlockStateVariant> entry(Identifier model)
		{
			return List.of(
					BlockStateVariant.create().put(VariantSettings.MODEL, model).put(VariantSettings.Y, VariantSettings.Rotation.R0),
					BlockStateVariant.create().put(VariantSettings.MODEL, model).put(VariantSettings.Y, VariantSettings.Rotation.R90),
					BlockStateVariant.create().put(VariantSettings.MODEL, model).put(VariantSettings.Y, VariantSettings.Rotation.R180),
					BlockStateVariant.create().put(VariantSettings.MODEL, model).put(VariantSettings.Y, VariantSettings.Rotation.R270)
					);
		}
		
		private static void makeBlockState(Block block, BlockStateModelGenerator generator)
		{
			Identifier model0 = TEMPLATE_RUBBLE_0.upload(block, rubbleTex(block), generator.modelCollector);
			BlockStateVariantMap map = BlockStateVariantMap.create(RubbleBlock.DEPTH)
					.register(1, entry(model0))
					.register(2, entry(TEMPLATE_RUBBLE_1.upload(block, rubbleTex(block), generator.modelCollector)))
					.register(3, entry(TEMPLATE_RUBBLE_2.upload(block, rubbleTex(block), generator.modelCollector)))
					.register(4, entry(TEMPLATE_RUBBLE_3.upload(block, rubbleTex(block), generator.modelCollector)));
			
			generator.blockStateCollector.accept(VariantsBlockStateSupplier.create(block).coordinate(map));
			generator.registerParentedItemModel(block, model0);
		}
	}
}
