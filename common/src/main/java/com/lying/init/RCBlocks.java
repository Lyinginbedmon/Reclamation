package com.lying.init;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

import com.google.common.collect.Lists;
import com.lying.Reclamation;
import com.lying.block.CrackedConcreteBlock;
import com.lying.block.DousedLanternBlock;
import com.lying.block.DousedTorchBlock;
import com.lying.block.FadedTerracottaBlock;
import com.lying.block.IvyBlock;
import com.lying.block.LeafPileBlock;
import com.lying.block.RubbleBlock;
import com.lying.block.ScrapeableBlock;
import com.lying.block.SootBlock;
import com.lying.reference.Reference;
import com.lying.utility.WoodType;

import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.MapColor;
import net.minecraft.block.SlabBlock;
import net.minecraft.block.StairsBlock;
import net.minecraft.block.enums.NoteBlockInstrument;
import net.minecraft.block.piston.PistonBehavior;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Identifier;

public class RCBlocks
{
	public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(Reference.ModInfo.MOD_ID, RegistryKeys.BLOCK);
	
	public static final List<RegistrySupplier<Block>> ALL_BLOCKS = Lists.newArrayList(), SOLID_CUBES = Lists.newArrayList();
	public static LeafPileBlock[] TINTED_LEAF_PILES = new LeafPileBlock[0];
	
	public static final Map<DyeColor, Terracotta> DYE_TO_TERRACOTTA = new HashMap<>();
	public static final Map<DyeColor, Concrete> DYE_TO_CONCRETE = new HashMap<>();
	
	public static record Terracotta(Supplier<Block> glazed, Supplier<Block> faded, Supplier<Block> blank)
	{
		public static Terracotta of(Block glazed, Supplier<Block> faded, Block blank) { return new Terracotta(() -> glazed, faded, () -> blank); }
	}
	public static record Concrete(Supplier<Block> dry, Supplier<Block> cracked, Supplier<Block> powder)
	{
		public static Concrete of(Block dry, Supplier<Block> cracked, Block powder) { return new Concrete(() -> dry, cracked, () -> powder); }
	}
	
	public static final RegistrySupplier<Block> WAXED_IRON_BLOCK		= registerSolidCube("waxed_iron_block", settings -> new ScrapeableBlock(() -> Blocks.IRON_BLOCK, settings.mapColor(MapColor.IRON_GRAY).instrument(NoteBlockInstrument.IRON_XYLOPHONE).requiresTool().strength(5F, 6F).sounds(BlockSoundGroup.METAL)));
	public static final RegistrySupplier<Block> WAXED_EXPOSED_IRON		= registerSolidCube("waxed_exposed_iron", settings -> new ScrapeableBlock(RCBlocks.EXPOSED_IRON, settings.mapColor(MapColor.LIGHT_GRAY).instrument(NoteBlockInstrument.IRON_XYLOPHONE).requiresTool().strength(4.0F, 6.0F).sounds(BlockSoundGroup.METAL)));
	public static final RegistrySupplier<Block> WAXED_WEATHERED_IRON	= registerSolidCube("waxed_weathered_iron", settings -> new ScrapeableBlock(RCBlocks.WEATHERED_IRON, settings.mapColor(MapColor.DULL_PINK).instrument(NoteBlockInstrument.IRON_XYLOPHONE).requiresTool().strength(3.0F, 6.0F).sounds(BlockSoundGroup.METAL)));
	public static final RegistrySupplier<Block> WAXED_RUSTED_IRON		= registerSolidCube("waxed_rusted_iron", settings -> new ScrapeableBlock(RCBlocks.RUSTED_IRON, settings.mapColor(MapColor.ORANGE).instrument(NoteBlockInstrument.IRON_XYLOPHONE).requiresTool().strength(2.0F, 6.0F).sounds(BlockSoundGroup.METAL)));
	public static final RegistrySupplier<Block> WAXED_GOLD_BLOCK		= registerSolidCube("waxed_gold_block", settings -> new ScrapeableBlock(() -> Blocks.GOLD_BLOCK, settings.mapColor(MapColor.YELLOW).sounds(BlockSoundGroup.METAL)));
	public static final RegistrySupplier<Block> WAXED_TARNISHED_GOLD	= registerSolidCube("waxed_tarnished_gold", settings -> new ScrapeableBlock(RCBlocks.TARNISHED_GOLD, settings.mapColor(MapColor.YELLOW).sounds(BlockSoundGroup.METAL)));
	
	public static final RegistrySupplier<Block> EXPOSED_IRON			= registerSolidCube("exposed_iron", settings -> new ScrapeableBlock(() -> Blocks.IRON_BLOCK, WAXED_EXPOSED_IRON, settings.requiresTool().strength(4.0F, 6.0F).mapColor(MapColor.LIGHT_GRAY).instrument(NoteBlockInstrument.IRON_XYLOPHONE).sounds(BlockSoundGroup.METAL)));
	public static final RegistrySupplier<Block> WEATHERED_IRON			= registerSolidCube("weathered_iron", settings -> new ScrapeableBlock(RCBlocks.EXPOSED_IRON, WAXED_WEATHERED_IRON, settings.requiresTool().strength(3.0F, 6.0F).mapColor(MapColor.DULL_PINK).instrument(NoteBlockInstrument.IRON_XYLOPHONE).sounds(BlockSoundGroup.METAL)));
	public static final RegistrySupplier<Block> RUSTED_IRON				= registerSolidCube("rusted_iron", settings -> new ScrapeableBlock(RCBlocks.WEATHERED_IRON, WAXED_RUSTED_IRON, settings.requiresTool().strength(2.0F, 6.0F).mapColor(MapColor.ORANGE).instrument(NoteBlockInstrument.IRON_XYLOPHONE).sounds(BlockSoundGroup.METAL)));
	public static final RegistrySupplier<Block> TARNISHED_GOLD			= registerSolidCube("tarnished_gold", settings -> new ScrapeableBlock(() -> Blocks.GOLD_BLOCK, settings.requiresTool().strength(3.0F, 6.0F).mapColor(MapColor.YELLOW).sounds(BlockSoundGroup.METAL)));
	
	public static final RegistrySupplier<Block> SOOT						= register("soot", settings -> new SootBlock(settings.mapColor(MapColor.BLACK).replaceable().noCollision().requiresTool().strength(0.1F).sounds(BlockSoundGroup.SNOW).pistonBehavior(PistonBehavior.DESTROY)));
	public static final RegistrySupplier<Block> IVY							= register("ivy", settings -> new IvyBlock(settings.mapColor(MapColor.DARK_GREEN).replaceable().noCollision().strength(0.2F).sounds(BlockSoundGroup.VINE).burnable().pistonBehavior(PistonBehavior.DESTROY)));
	public static final RegistrySupplier<Block> CRACKED_STONE_BRICK_SLAB	= register("cracked_stone_brick_slab", settings -> new SlabBlock(settings.mapColor(MapColor.STONE_GRAY).instrument(NoteBlockInstrument.BASEDRUM).requiresTool().strength(2.0F, 6.0F)));
	public static final RegistrySupplier<Block> CRACKED_STONE_BRICK_STAIRS	= register("cracked_stone_brick_stairs", settings -> new StairsBlock(Blocks.CRACKED_STONE_BRICKS.getDefaultState(), settings.mapColor(MapColor.STONE_GRAY).instrument(NoteBlockInstrument.BASEDRUM).requiresTool().strength(2.0F, 6.0F)));
	public static final RegistrySupplier<Block> OAK_LEAF_PILE				= registerLeafPile(WoodType.OAK);
	public static final RegistrySupplier<Block> ACACIA_LEAF_PILE			= registerLeafPile(WoodType.ACACIA);
	public static final RegistrySupplier<Block> BIRCH_LEAF_PILE				= registerLeafPile(WoodType.BIRCH);
	public static final RegistrySupplier<Block> CHERRY_LEAF_PILE			= registerLeafPile(WoodType.CHERRY);
	public static final RegistrySupplier<Block> DARK_OAK_LEAF_PILE			= registerLeafPile(WoodType.DARK_OAK);
	public static final RegistrySupplier<Block> JUNGLE_LEAF_PILE			= registerLeafPile(WoodType.JUNGLE);
	public static final RegistrySupplier<Block> MANGROVE_LEAF_PILE			= registerLeafPile(WoodType.MANGROVE);
	public static final RegistrySupplier<Block> PALE_LEAF_PILE				= registerLeafPile(WoodType.PALE);
	public static final RegistrySupplier<Block> SPRUCE_LEAF_PILE			= registerLeafPile(WoodType.SPRUCE);
	
	public static final RegistrySupplier<Block> BLACK_FADED_TERRACOTTA		= registerFadedTerracotta(DyeColor.BLACK);
	public static final RegistrySupplier<Block> BLUE_FADED_TERRACOTTA		= registerFadedTerracotta(DyeColor.BLUE);
	public static final RegistrySupplier<Block> BROWN_FADED_TERRACOTTA		= registerFadedTerracotta(DyeColor.BROWN);
	public static final RegistrySupplier<Block> CYAN_FADED_TERRACOTTA		= registerFadedTerracotta(DyeColor.CYAN);
	public static final RegistrySupplier<Block> GRAY_FADED_TERRACOTTA		= registerFadedTerracotta(DyeColor.GRAY);
	public static final RegistrySupplier<Block> GREEN_FADED_TERRACOTTA		= registerFadedTerracotta(DyeColor.GREEN);
	public static final RegistrySupplier<Block> LIGHT_BLUE_FADED_TERRACOTTA	= registerFadedTerracotta(DyeColor.LIGHT_BLUE);
	public static final RegistrySupplier<Block> LIGHT_GRAY_FADED_TERRACOTTA	= registerFadedTerracotta(DyeColor.LIGHT_GRAY);
	public static final RegistrySupplier<Block> LIME_FADED_TERRACOTTA		= registerFadedTerracotta(DyeColor.LIME);
	public static final RegistrySupplier<Block> MAGENTA_FADED_TERRACOTTA	= registerFadedTerracotta(DyeColor.MAGENTA);
	public static final RegistrySupplier<Block> ORANGE_FADED_TERRACOTTA		= registerFadedTerracotta(DyeColor.ORANGE);
	public static final RegistrySupplier<Block> PINK_FADED_TERRACOTTA		= registerFadedTerracotta(DyeColor.PINK);
	public static final RegistrySupplier<Block> PURPLE_FADED_TERRACOTTA		= registerFadedTerracotta(DyeColor.PURPLE);
	public static final RegistrySupplier<Block> RED_FADED_TERRACOTTA		= registerFadedTerracotta(DyeColor.RED);
	public static final RegistrySupplier<Block> WHITE_FADED_TERRACOTTA		= registerFadedTerracotta(DyeColor.WHITE);
	public static final RegistrySupplier<Block> YELLOW_FADED_TERRACOTTA		= registerFadedTerracotta(DyeColor.YELLOW);
	
	public static final RegistrySupplier<Block> CRACKED_BLACK_CONCRETE		= registerCrackedConcrete(DyeColor.BLACK);
	public static final RegistrySupplier<Block> CRACKED_BLUE_CONCRETE		= registerCrackedConcrete(DyeColor.BLUE);
	public static final RegistrySupplier<Block> CRACKED_BROWN_CONCRETE		= registerCrackedConcrete(DyeColor.BROWN);
	public static final RegistrySupplier<Block> CRACKED_CYAN_CONCRETE		= registerCrackedConcrete(DyeColor.CYAN);
	public static final RegistrySupplier<Block> CRACKED_GRAY_CONCRETE		= registerCrackedConcrete(DyeColor.GRAY);
	public static final RegistrySupplier<Block> CRACKED_GREEN_CONCRETE		= registerCrackedConcrete(DyeColor.GREEN);
	public static final RegistrySupplier<Block> CRACKED_LIGHT_BLUE_CONCRETE	= registerCrackedConcrete(DyeColor.LIGHT_BLUE);
	public static final RegistrySupplier<Block> CRACKED_LIGHT_GRAY_CONCRETE	= registerCrackedConcrete(DyeColor.LIGHT_GRAY);
	public static final RegistrySupplier<Block> CRACKED_LIME_CONCRETE		= registerCrackedConcrete(DyeColor.LIME);
	public static final RegistrySupplier<Block> CRACKED_MAGENTA_CONCRETE	= registerCrackedConcrete(DyeColor.MAGENTA);
	public static final RegistrySupplier<Block> CRACKED_ORANGE_CONCRETE		= registerCrackedConcrete(DyeColor.ORANGE);
	public static final RegistrySupplier<Block> CRACKED_PINK_CONCRETE		= registerCrackedConcrete(DyeColor.PINK);
	public static final RegistrySupplier<Block> CRACKED_PURPLE_CONCRETE		= registerCrackedConcrete(DyeColor.PURPLE);
	public static final RegistrySupplier<Block> CRACKED_RED_CONCRETE		= registerCrackedConcrete(DyeColor.RED);
	public static final RegistrySupplier<Block> CRACKED_WHITE_CONCRETE		= registerCrackedConcrete(DyeColor.WHITE);
	public static final RegistrySupplier<Block> CRACKED_YELLOW_CONCRETE		= registerCrackedConcrete(DyeColor.YELLOW);
	
	public static final RegistrySupplier<Block> DOUSED_TORCH				= register("doused_torch", settings -> new DousedTorchBlock(Blocks.TORCH, Blocks.WALL_TORCH, settings.noCollision().breakInstantly().sounds(BlockSoundGroup.WOOD).pistonBehavior(PistonBehavior.DESTROY)));
	public static final RegistrySupplier<Block> DOUSED_SOUL_TORCH			= register("doused_soul_torch", settings -> new DousedTorchBlock(Blocks.SOUL_TORCH, Blocks.SOUL_WALL_TORCH, settings.noCollision().breakInstantly().sounds(BlockSoundGroup.WOOD).pistonBehavior(PistonBehavior.DESTROY)));
	public static final RegistrySupplier<Block> DOUSED_LANTERN				= register("doused_lantern", settings -> new DousedLanternBlock(() -> Blocks.LANTERN, settings.mapColor(MapColor.IRON_GRAY).solid().strength(3.5F).sounds(BlockSoundGroup.LANTERN).nonOpaque().pistonBehavior(PistonBehavior.DESTROY)));
	public static final RegistrySupplier<Block> DOUSED_SOUL_LANTERN			= register("doused_soul_lantern", settings -> new DousedLanternBlock(() -> Blocks.SOUL_LANTERN, settings.mapColor(MapColor.IRON_GRAY).solid().strength(3.5F).sounds(BlockSoundGroup.LANTERN).nonOpaque().pistonBehavior(PistonBehavior.DESTROY)));
	
	public static final RegistrySupplier<Block> STONE_RUBBLE				= register("stone_rubble", settings -> new RubbleBlock(settings.nonOpaque().mapColor(MapColor.STONE_GRAY).instrument(NoteBlockInstrument.BASEDRUM).requiresTool().strength(1.0F, 2.0F)));
	
	private static RegistrySupplier<Block> registerLeafPile(WoodType typeIn)
	{
		RegistrySupplier<Block> registry = register(typeIn.asString()+"_leaf_pile", settings -> 
			new LeafPileBlock(typeIn.leaves, settings
					.mapColor(MapColor.GREEN)
					.nonOpaque()
					.noCollision()
					.strength(0.1F)
					.sounds(BlockSoundGroup.GRASS)
					.burnable()
					.pistonBehavior(PistonBehavior.DESTROY)));
		return registry;
	}
	
	private static RegistrySupplier<Block> registerFadedTerracotta(DyeColor color)
	{
		return register(color.asString()+"_faded_terracotta", settings -> 
			new FadedTerracottaBlock(color, settings
					.mapColor(color)
					.instrument(NoteBlockInstrument.BASEDRUM)
					.requiresTool()
					.strength(1.4F)
					.pistonBehavior(PistonBehavior.PUSH_ONLY)));
	}
	
	private static RegistrySupplier<Block> registerCrackedConcrete(DyeColor color)
	{
		RegistrySupplier<Block> registry = register("cracked_"+color.asString()+"_concrete", settings -> 
			new CrackedConcreteBlock(color, settings
					.mapColor(color)
					.instrument(NoteBlockInstrument.BASEDRUM)
					.requiresTool()
					.strength(1.8F)));
		return registry;
	}
	
	private static RegistrySupplier<Block> registerSolidCube(String nameIn, Function<AbstractBlock.Settings, Block> supplierIn)
	{
		RegistrySupplier<Block> registry = register(nameIn, supplierIn);
		SOLID_CUBES.add(registry);
		return registry;
	}
	
	private static RegistrySupplier<Block> register(String nameIn, Function<AbstractBlock.Settings, Block> supplierIn)
	{
		Identifier id = Reference.ModInfo.prefix(nameIn);
		RegistryKey<Block> key = RegistryKey.of(RegistryKeys.BLOCK, id);
		AbstractBlock.Settings settings = AbstractBlock.Settings.create().registryKey(key);
		RegistrySupplier<Block> registry = BLOCKS.register(id, () -> supplierIn.apply(settings));
		ALL_BLOCKS.add(registry);
		return registry;
	}
	
	public static void init()
	{
		BLOCKS.register();
		Reclamation.LOGGER.info("# Initialised {} blocks", ALL_BLOCKS.size());
		
		DYE_TO_TERRACOTTA.put(DyeColor.BLACK, Terracotta.of(Blocks.BLACK_GLAZED_TERRACOTTA, RCBlocks.BLACK_FADED_TERRACOTTA, Blocks.BLACK_TERRACOTTA));
		DYE_TO_TERRACOTTA.put(DyeColor.BLUE, Terracotta.of(Blocks.BLUE_GLAZED_TERRACOTTA, RCBlocks.BLUE_FADED_TERRACOTTA, Blocks.BLUE_TERRACOTTA));
		DYE_TO_TERRACOTTA.put(DyeColor.BROWN, Terracotta.of(Blocks.BROWN_GLAZED_TERRACOTTA, RCBlocks.BROWN_FADED_TERRACOTTA, Blocks.BROWN_TERRACOTTA));
		DYE_TO_TERRACOTTA.put(DyeColor.CYAN, Terracotta.of(Blocks.CYAN_GLAZED_TERRACOTTA, RCBlocks.CYAN_FADED_TERRACOTTA, Blocks.CYAN_TERRACOTTA));
		DYE_TO_TERRACOTTA.put(DyeColor.GRAY, Terracotta.of(Blocks.GRAY_GLAZED_TERRACOTTA, RCBlocks.GRAY_FADED_TERRACOTTA, Blocks.GRAY_TERRACOTTA));
		DYE_TO_TERRACOTTA.put(DyeColor.GREEN, Terracotta.of(Blocks.GREEN_GLAZED_TERRACOTTA, RCBlocks.GREEN_FADED_TERRACOTTA, Blocks.GREEN_TERRACOTTA));
		DYE_TO_TERRACOTTA.put(DyeColor.LIGHT_BLUE, Terracotta.of(Blocks.LIGHT_BLUE_GLAZED_TERRACOTTA, RCBlocks.LIGHT_BLUE_FADED_TERRACOTTA, Blocks.LIGHT_BLUE_TERRACOTTA));
		DYE_TO_TERRACOTTA.put(DyeColor.LIGHT_GRAY, Terracotta.of(Blocks.LIGHT_GRAY_GLAZED_TERRACOTTA, RCBlocks.LIGHT_GRAY_FADED_TERRACOTTA, Blocks.LIGHT_GRAY_TERRACOTTA));
		DYE_TO_TERRACOTTA.put(DyeColor.LIME, Terracotta.of(Blocks.LIME_GLAZED_TERRACOTTA, RCBlocks.LIME_FADED_TERRACOTTA, Blocks.LIME_TERRACOTTA));
		DYE_TO_TERRACOTTA.put(DyeColor.MAGENTA, Terracotta.of(Blocks.MAGENTA_GLAZED_TERRACOTTA, RCBlocks.MAGENTA_FADED_TERRACOTTA, Blocks.MAGENTA_TERRACOTTA));
		DYE_TO_TERRACOTTA.put(DyeColor.ORANGE, Terracotta.of(Blocks.ORANGE_GLAZED_TERRACOTTA, RCBlocks.ORANGE_FADED_TERRACOTTA, Blocks.ORANGE_TERRACOTTA));
		DYE_TO_TERRACOTTA.put(DyeColor.PINK, Terracotta.of(Blocks.PINK_GLAZED_TERRACOTTA, RCBlocks.PINK_FADED_TERRACOTTA, Blocks.PINK_TERRACOTTA));
		DYE_TO_TERRACOTTA.put(DyeColor.PURPLE, Terracotta.of(Blocks.PURPLE_GLAZED_TERRACOTTA, RCBlocks.PURPLE_FADED_TERRACOTTA, Blocks.PURPLE_TERRACOTTA));
		DYE_TO_TERRACOTTA.put(DyeColor.RED, Terracotta.of(Blocks.RED_GLAZED_TERRACOTTA, RCBlocks.RED_FADED_TERRACOTTA, Blocks.RED_TERRACOTTA));
		DYE_TO_TERRACOTTA.put(DyeColor.WHITE, Terracotta.of(Blocks.WHITE_GLAZED_TERRACOTTA, RCBlocks.WHITE_FADED_TERRACOTTA, Blocks.WHITE_TERRACOTTA));
		DYE_TO_TERRACOTTA.put(DyeColor.YELLOW, Terracotta.of(Blocks.YELLOW_GLAZED_TERRACOTTA, RCBlocks.YELLOW_FADED_TERRACOTTA, Blocks.YELLOW_TERRACOTTA));
		
		DYE_TO_CONCRETE.put(DyeColor.BLACK, Concrete.of(Blocks.BLACK_CONCRETE, RCBlocks.CRACKED_BLACK_CONCRETE, Blocks.BLACK_CONCRETE_POWDER));
		DYE_TO_CONCRETE.put(DyeColor.BLUE, Concrete.of(Blocks.BLUE_CONCRETE, RCBlocks.CRACKED_BLUE_CONCRETE, Blocks.BLUE_CONCRETE_POWDER));
		DYE_TO_CONCRETE.put(DyeColor.BROWN, Concrete.of(Blocks.BROWN_CONCRETE, RCBlocks.CRACKED_BROWN_CONCRETE, Blocks.BROWN_CONCRETE_POWDER));
		DYE_TO_CONCRETE.put(DyeColor.CYAN, Concrete.of(Blocks.CYAN_CONCRETE, RCBlocks.CRACKED_CYAN_CONCRETE, Blocks.CYAN_CONCRETE_POWDER));
		DYE_TO_CONCRETE.put(DyeColor.GRAY, Concrete.of(Blocks.GRAY_CONCRETE, RCBlocks.CRACKED_GRAY_CONCRETE, Blocks.GRAY_CONCRETE_POWDER));
		DYE_TO_CONCRETE.put(DyeColor.GREEN, Concrete.of(Blocks.GREEN_CONCRETE, RCBlocks.CRACKED_GREEN_CONCRETE, Blocks.GREEN_CONCRETE_POWDER));
		DYE_TO_CONCRETE.put(DyeColor.LIGHT_BLUE, Concrete.of(Blocks.LIGHT_BLUE_CONCRETE, RCBlocks.CRACKED_LIGHT_BLUE_CONCRETE, Blocks.LIGHT_BLUE_CONCRETE_POWDER));
		DYE_TO_CONCRETE.put(DyeColor.LIGHT_GRAY, Concrete.of(Blocks.LIGHT_GRAY_CONCRETE, RCBlocks.CRACKED_LIGHT_GRAY_CONCRETE, Blocks.LIGHT_GRAY_CONCRETE_POWDER));
		DYE_TO_CONCRETE.put(DyeColor.LIME, Concrete.of(Blocks.LIME_CONCRETE, RCBlocks.CRACKED_LIME_CONCRETE, Blocks.LIME_CONCRETE_POWDER));
		DYE_TO_CONCRETE.put(DyeColor.MAGENTA, Concrete.of(Blocks.MAGENTA_CONCRETE, RCBlocks.CRACKED_MAGENTA_CONCRETE, Blocks.MAGENTA_CONCRETE_POWDER));
		DYE_TO_CONCRETE.put(DyeColor.ORANGE, Concrete.of(Blocks.ORANGE_CONCRETE, RCBlocks.CRACKED_ORANGE_CONCRETE, Blocks.ORANGE_CONCRETE_POWDER));
		DYE_TO_CONCRETE.put(DyeColor.PINK, Concrete.of(Blocks.PINK_CONCRETE, RCBlocks.CRACKED_PINK_CONCRETE, Blocks.PINK_CONCRETE_POWDER));
		DYE_TO_CONCRETE.put(DyeColor.PURPLE, Concrete.of(Blocks.PURPLE_CONCRETE, RCBlocks.CRACKED_PURPLE_CONCRETE, Blocks.PURPLE_CONCRETE_POWDER));
		DYE_TO_CONCRETE.put(DyeColor.RED, Concrete.of(Blocks.RED_CONCRETE, RCBlocks.CRACKED_RED_CONCRETE, Blocks.RED_CONCRETE_POWDER));
		DYE_TO_CONCRETE.put(DyeColor.WHITE, Concrete.of(Blocks.WHITE_CONCRETE, RCBlocks.CRACKED_WHITE_CONCRETE, Blocks.WHITE_CONCRETE_POWDER));
		DYE_TO_CONCRETE.put(DyeColor.YELLOW, Concrete.of(Blocks.YELLOW_CONCRETE, RCBlocks.CRACKED_YELLOW_CONCRETE, Blocks.YELLOW_CONCRETE_POWDER));
		
		TINTED_LEAF_PILES = List.of(
				ACACIA_LEAF_PILE,
				DARK_OAK_LEAF_PILE,
				JUNGLE_LEAF_PILE,
				MANGROVE_LEAF_PILE,
				OAK_LEAF_PILE
				).stream().map(Supplier::get).map(b -> (LeafPileBlock)b).toList().toArray(new LeafPileBlock[0]);
	}
}
