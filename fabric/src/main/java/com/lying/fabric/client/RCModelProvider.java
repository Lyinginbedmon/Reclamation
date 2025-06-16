package com.lying.fabric.client;

import static com.lying.reference.Reference.ModInfo.prefix;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import com.lying.block.CrackedConcreteBlock;
import com.lying.block.DousedTorchBlock;
import com.lying.block.LeafPileBlock;
import com.lying.block.RaggedWallBannerBlock;
import com.lying.block.RubbleBlock;
import com.lying.client.ReclamationClient;
import com.lying.init.RCBlocks;
import com.lying.init.RCBlocks.Terracotta;
import com.lying.init.RCItems;

import net.fabricmc.fabric.api.client.datagen.v1.provider.FabricModelProvider;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.MultifaceBlock;
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
import net.minecraft.client.data.MultipartBlockStateSupplier;
import net.minecraft.client.data.TextureKey;
import net.minecraft.client.data.TextureMap;
import net.minecraft.client.data.TexturedModel;
import net.minecraft.client.data.VariantSettings;
import net.minecraft.client.data.VariantsBlockStateSupplier;
import net.minecraft.client.data.When;
import net.minecraft.client.render.item.model.special.BannerModelRenderer;
import net.minecraft.client.render.item.tint.ConstantTintSource;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Direction;

public class RCModelProvider extends FabricModelProvider
{
	public static final Model TEMPLATE_LAYER_0 = new Model(
			Optional.of(prefix("block/template_layered_0")),
			Optional.of("_0"),
			TextureKey.ALL);
	public static final Model TEMPLATE_LAYER_1 = new Model(
			Optional.of(prefix("block/template_layered_1")),
			Optional.of("_1"),
			TextureKey.ALL);
	public static final Model TEMPLATE_LAYER_2 = new Model(
			Optional.of(prefix("block/template_layered_2")),
			Optional.of("_2"),
			TextureKey.ALL);
	public static final Model TEMPLATE_TINTED_CUBE = new Model(
			Optional.of(prefix("block/template_tinted_cube")),
			Optional.empty(),
			TextureKey.ALL);
	public static final Model TEMPLATE_IVY	= new Model(
			Optional.of(prefix("block/template_ivy")),
			Optional.empty(),
			TextureKey.TEXTURE
			);
	
	public RCModelProvider(FabricDataOutput output)
	{
		super(output);
	}
	
	public void generateBlockStateModels(BlockStateModelGenerator blockStateModelGenerator)
	{
		// Simple solid cubes
		RCBlocks.SOLID_CUBES.forEach(entry -> blockStateModelGenerator.registerSimpleCubeAll(entry.get()));
		
		RCBlocks.DYE_TO_TERRACOTTA.values().stream().map(Terracotta::faded).forEach(b -> blockStateModelGenerator.registerSouthDefaultHorizontalFacing(TexturedModel.TEMPLATE_GLAZED_TERRACOTTA, b.get()));
		
		// Vertical column blocks
		for(Block block : new Block[] {
				RCBlocks.ROTTEN_MELON.get(),
				RCBlocks.ROTTEN_PUMPKIN.get()
		})
			blockStateModelGenerator.registerSingleton(block, TexturedModel.CUBE_COLUMN);
		
		// Horizontal aligned blocks eg furnaces
		for(Block block : new Block[] {
				RCBlocks.ROTTEN_CARVED_PUMPKIN.get(),
				RCBlocks.ROTTEN_JACK_O_LANTERN.get()
		})
			blockStateModelGenerator.registerNorthDefaultHorizontalRotated(block, TexturedModel.ORIENTABLE);
		
		RCBlocks.DYE_TO_CONCRETE.values().stream().forEach(b -> CrackedConcrete.makeBlockState((CrackedConcreteBlock)b.cracked().get(), b.dry().get(), blockStateModelGenerator));
		RCBlocks.DYE_TO_RAGGED_BANNER.entrySet().forEach(e -> RaggedBanner.makeBlockState(e.getValue().get(), RaggedWallBannerBlock.getForColor(e.getKey()), e.getKey(), blockStateModelGenerator));
		DousedLights.register(blockStateModelGenerator);
		LeafPile.register(blockStateModelGenerator);
		registerSlab(RCBlocks.CRACKED_STONE_BRICK_SLAB.get(), Blocks.CRACKED_STONE_BRICKS, blockStateModelGenerator);
		registerStairs(RCBlocks.CRACKED_STONE_BRICK_STAIRS.get(), Blocks.CRACKED_STONE_BRICKS, blockStateModelGenerator);
		
		Scrap.makeBlockState(RCBlocks.IRON_SCRAP.get(), blockStateModelGenerator);
		Rubble.makeBlockState((RubbleBlock)RCBlocks.STONE_RUBBLE.get(), blockStateModelGenerator);
		Rubble.makeBlockState((RubbleBlock)RCBlocks.DEEPSLATE_RUBBLE.get(), blockStateModelGenerator);
		
		// Multiface blocks
		registerIvy(RCBlocks.IVY.get(), RCItems.IVY.get(), blockStateModelGenerator);
		Mold.makeBlockState(RCBlocks.MOLD.get(), RCItems.MOLD.get(), blockStateModelGenerator);
		blockStateModelGenerator.registerMultifaceBlock(RCBlocks.SOOT.get());
	}
	
	public void generateItemModels(ItemModelGenerator itemModelGenerator)
	{
		RCItems.BASIC_BLOCK_ITEMS.stream().map(e -> (BlockItem)e.get()).forEach(entry -> registerBlockModel(entry, itemModelGenerator));
		itemModelGenerator.register(RCItems.WITHERING_DUST.get(), Models.GENERATED);
		itemModelGenerator.register(RCItems.DEACTIVATOR.get(), Models.HANDHELD_ROD);
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
		
		private static TextureMap concreteTex(Block block, int index)
		{
			Identifier tex = Registries.BLOCK.getId(block);
			return new TextureMap()
					.put(TextureKey.ALL, Identifier.of(tex.getNamespace(), "block/cracked_concrete/"+tex.getPath()+"_"+index));
		}
		
		private static void makeBlockState(Block block, Block base, BlockStateModelGenerator generator)
		{
			Identifier heldModel;
			BlockStateVariantMap map = BlockStateVariantMap.create(CrackedConcreteBlock.CRACKS)
					.register(1, entry(TEMPLATE_CRACKED_0.upload(block, concreteTex(block, 0), generator.modelCollector)))
					.register(2, entry(heldModel = TEMPLATE_CRACKED_1.upload(block, concreteTex(block, 1), generator.modelCollector)))
					.register(3, entry(TEMPLATE_CRACKED_2.upload(block, concreteTex(block, 2), generator.modelCollector)))
					.register(4, entry(TEMPLATE_CRACKED_3.upload(block, concreteTex(block, 3), generator.modelCollector)));
			
			generator.blockStateCollector.accept(VariantsBlockStateSupplier.create(block).coordinate(map));
			generator.registerParentedItemModel(block, heldModel);
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
	}
	
	private static class Rubble
	{
		public static final Model TEMPLATE_RUBBLE_0 = new Model(
				Optional.of(prefix("block/template_rubble_0")),
				Optional.of("_0"),
				TextureKey.ALL);
		public static final Model TEMPLATE_RUBBLE_1 = new Model(
				Optional.of(prefix("block/template_rubble_1")),
				Optional.of("_1"),
				TextureKey.ALL);
		public static final Model TEMPLATE_RUBBLE_2 = new Model(
				Optional.of(prefix("block/template_rubble_2")),
				Optional.of("_2"),
				TextureKey.ALL);
		public static final Model TEMPLATE_RUBBLE_3 = new Model(
				Optional.of(prefix("block/template_rubble_3")),
				Optional.of("_3"),
				TextureKey.ALL);
		public static final Model TEMPLATE_RUBBLE_FULL_BLOCK = new Model(
				Optional.of(prefix("block/template_tinted_cube")),
				Optional.of("_full"),
				TextureKey.ALL);
		
		private static TextureMap rubbleTex(Block cobble)
		{
			Identifier tex = Registries.BLOCK.getId(cobble);
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
		
		private static void makeBlockState(RubbleBlock block, BlockStateModelGenerator generator)
		{
			TextureMap tex = rubbleTex(block.parentBlock());
			Identifier model0 = TEMPLATE_RUBBLE_0.upload(block, tex, generator.modelCollector);
			
			List<BlockStateVariant> entry0 = entry(model0);
			List<BlockStateVariant> entry1 = entry(TEMPLATE_RUBBLE_1.upload(block, tex, generator.modelCollector));
			List<BlockStateVariant> entry2 = entry(TEMPLATE_RUBBLE_2.upload(block, tex, generator.modelCollector));
			List<BlockStateVariant> entry3 = entry(TEMPLATE_RUBBLE_3.upload(block, tex, generator.modelCollector));
			List<BlockStateVariant> entry4 = entry(TEMPLATE_RUBBLE_FULL_BLOCK.upload(block, tex, generator.modelCollector));
			
			BlockStateVariantMap map = BlockStateVariantMap.create(RubbleBlock.DEPTH, RubbleBlock.FULL)
					.register(1, false, entry0)
					.register(1, true, entry0)
					.register(2, false, entry1)
					.register(2, true, entry1)
					.register(3, false, entry2)
					.register(3, true, entry2)
					.register(4, false, entry3)
					.register(4, true, entry4);
			
			generator.blockStateCollector.accept(VariantsBlockStateSupplier.create(block).coordinate(map));
			generator.registerParentedItemModel(block, model0);
		}
	}
	
	private static class Mold
	{
		private static final Model MOLD_0 = new Model(
				Optional.of(prefix("block/template_ivy")),
				Optional.of("_0"),
				TextureKey.TEXTURE
				);
		private static final Model MOLD_1 = new Model(
				Optional.of(prefix("block/template_ivy")),
				Optional.of("_1"),
				TextureKey.TEXTURE
				);
		private static final Model MOLD_2 = new Model(
				Optional.of(prefix("block/template_ivy")),
				Optional.of("_2"),
				TextureKey.TEXTURE
				);
		
		private static final Map<Direction, Function<Identifier, BlockStateVariant>> CONNECTION_VARIANT_FUNCTIONS = Map.of(
				Direction.NORTH,
					model -> BlockStateVariant.create().put(VariantSettings.MODEL, model),
				Direction.EAST,
					model -> BlockStateVariant.create().put(VariantSettings.MODEL, model).put(VariantSettings.Y, VariantSettings.Rotation.R90).put(VariantSettings.UVLOCK, true),
				Direction.SOUTH,
					model -> BlockStateVariant.create()
							.put(VariantSettings.MODEL, model)
							.put(VariantSettings.Y, VariantSettings.Rotation.R180)
							.put(VariantSettings.UVLOCK, true),
				Direction.WEST,
					model -> BlockStateVariant.create()
							.put(VariantSettings.MODEL, model)
							.put(VariantSettings.Y, VariantSettings.Rotation.R270)
							.put(VariantSettings.UVLOCK, true),
				Direction.UP,
					model -> BlockStateVariant.create()
							.put(VariantSettings.MODEL, model)
							.put(VariantSettings.X, VariantSettings.Rotation.R270)
							.put(VariantSettings.UVLOCK, true),
				Direction.DOWN,
					model -> BlockStateVariant.create().put(VariantSettings.MODEL, model).put(VariantSettings.X, VariantSettings.Rotation.R90).put(VariantSettings.UVLOCK, true)
			);
		
		private static void makeBlockState(Block block, Item item, BlockStateModelGenerator generator)
		{
			Identifier blockID = ModelIds.getBlockModelId(block);
			Identifier texture0 = Identifier.of(blockID.getNamespace(), blockID.getPath()+"_0");
			generator.registerItemModel(item, Models.GENERATED.upload(item, TextureMap.layer0(texture0), generator.modelCollector));
			
			List<Identifier> models = List.of(
					MOLD_0.upload(block, new TextureMap().put(TextureKey.TEXTURE, texture0), generator.modelCollector),
					MOLD_1.upload(block, new TextureMap().put(TextureKey.TEXTURE, Identifier.of(blockID.getNamespace(), blockID.getPath()+"_1")), generator.modelCollector),
					MOLD_2.upload(block, new TextureMap().put(TextureKey.TEXTURE, Identifier.of(blockID.getNamespace(), blockID.getPath()+"_2")), generator.modelCollector)
					);
			MultipartBlockStateSupplier map = MultipartBlockStateSupplier.create(block);
			CONNECTION_VARIANT_FUNCTIONS.entrySet().forEach(entry -> 
				map.with(When.create().set(MultifaceBlock.getProperty(entry.getKey()), true), models.stream().map(model -> entry.getValue().apply(model)).toList()));
			generator.blockStateCollector.accept(map);
		}
	}
	
	private static class RaggedBanner
	{
		@SuppressWarnings("deprecation")
		private static void makeBlockState(Block block, Block wallBlock, DyeColor color, BlockStateModelGenerator generator)
		{
			Identifier id = ModelIds.getMinecraftNamespacedBlock("banner");
			Identifier id2 = ModelIds.getMinecraftNamespacedItem("template_banner");
			generator.blockStateCollector.accept(BlockStateModelGenerator.createSingletonBlockState(block, id));
			generator.blockStateCollector.accept(BlockStateModelGenerator.createSingletonBlockState(wallBlock, id));
			
			Item item = block.asItem();
			generator.itemModelOutput.accept(item, ItemModels.special(id2, new BannerModelRenderer.Unbaked(color)));
		}
	}
	
	private static class Scrap
	{
		private static void makeBlockState(Block block, BlockStateModelGenerator generator)
		{
			Identifier id = Registries.BLOCK.getId(block);
			generator.blockStateCollector.accept(VariantsBlockStateSupplier.create(block, entry(Identifier.of(id.getNamespace(), "block/"+id.getPath())).toArray(new BlockStateVariant[0])));
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
	}
}
