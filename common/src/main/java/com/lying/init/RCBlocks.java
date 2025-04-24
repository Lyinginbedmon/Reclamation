package com.lying.init;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

import com.lying.Reclamation;
import com.lying.block.IvyBlock;
import com.lying.block.ScrapeableBlock;
import com.lying.reference.Reference;

import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.CarpetBlock;
import net.minecraft.block.GlazedTerracottaBlock;
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
	
	public static final Map<DyeColor, Terracotta> DYE_TO_TERRACOTTA = new HashMap<>();
	private static int tally = 0;
	
	public static record Terracotta(Supplier<Block> glazed, Supplier<Block> faded, Supplier<Block> blank) { }
	
	public static final RegistrySupplier<Block> WAXED_IRON_BLOCK		= register("waxed_iron_block", settings -> new ScrapeableBlock(() -> Blocks.IRON_BLOCK, settings.mapColor(MapColor.IRON_GRAY).instrument(NoteBlockInstrument.IRON_XYLOPHONE).requiresTool().strength(5F, 6F).sounds(BlockSoundGroup.METAL)));
	public static final RegistrySupplier<Block> WAXED_EXPOSED_IRON		= register("waxed_exposed_iron", settings -> new ScrapeableBlock(RCBlocks.EXPOSED_IRON, settings.mapColor(MapColor.LIGHT_GRAY).instrument(NoteBlockInstrument.IRON_XYLOPHONE).sounds(BlockSoundGroup.METAL)));
	public static final RegistrySupplier<Block> WAXED_WEATHERED_IRON	= register("waxed_weathered_iron", settings -> new ScrapeableBlock(RCBlocks.WEATHERED_IRON, settings.mapColor(MapColor.DULL_PINK).instrument(NoteBlockInstrument.IRON_XYLOPHONE).sounds(BlockSoundGroup.METAL)));
	public static final RegistrySupplier<Block> WAXED_RUSTED_IRON		= register("waxed_rusted_iron", settings -> new ScrapeableBlock(RCBlocks.RUSTED_IRON, settings.mapColor(MapColor.ORANGE).instrument(NoteBlockInstrument.IRON_XYLOPHONE).sounds(BlockSoundGroup.METAL)));
	
	public static final RegistrySupplier<Block> EXPOSED_IRON			= register("exposed_iron", settings -> new ScrapeableBlock(() -> Blocks.IRON_BLOCK, WAXED_EXPOSED_IRON, settings.mapColor(MapColor.LIGHT_GRAY).instrument(NoteBlockInstrument.IRON_XYLOPHONE).sounds(BlockSoundGroup.METAL)));
	public static final RegistrySupplier<Block> WEATHERED_IRON			= register("weathered_iron", settings -> new ScrapeableBlock(RCBlocks.EXPOSED_IRON, WAXED_WEATHERED_IRON, settings.mapColor(MapColor.DULL_PINK).instrument(NoteBlockInstrument.IRON_XYLOPHONE).sounds(BlockSoundGroup.METAL)));
	public static final RegistrySupplier<Block> RUSTED_IRON				= register("rusted_iron", settings -> new ScrapeableBlock(RCBlocks.WEATHERED_IRON, WAXED_RUSTED_IRON, settings.mapColor(MapColor.ORANGE).instrument(NoteBlockInstrument.IRON_XYLOPHONE).sounds(BlockSoundGroup.METAL)));
	
	public static final RegistrySupplier<Block> TARNISHED_GOLD			= register("tarnished_gold", settings -> new ScrapeableBlock(() -> Blocks.GOLD_BLOCK, settings.mapColor(MapColor.YELLOW).sounds(BlockSoundGroup.METAL)));
	public static final RegistrySupplier<Block> LEAF_PILE				= register("leaf_pile", settings -> new CarpetBlock(settings.mapColor(MapColor.GREEN).strength(0.1F).sounds(BlockSoundGroup.MOSS_CARPET).pistonBehavior(PistonBehavior.DESTROY)));
	public static final RegistrySupplier<Block> IVY						= register("ivy", settings -> new IvyBlock(settings.mapColor(MapColor.DARK_GREEN).replaceable().noCollision().strength(0.2F).sounds(BlockSoundGroup.VINE).burnable().pistonBehavior(PistonBehavior.DESTROY)));
	public static final RegistrySupplier<Block> CRACKED_STONE_BRICK_SLAB	= register("cracked_stone_brick_slab", settings -> new SlabBlock(settings.mapColor(MapColor.STONE_GRAY).instrument(NoteBlockInstrument.BASEDRUM).requiresTool().strength(2.0F, 6.0F)));
	public static final RegistrySupplier<Block> CRACKED_STONE_BRICK_STAIRS	= register("cracked_stone_brick_stairs", settings -> new StairsBlock(Blocks.CRACKED_STONE_BRICKS.getDefaultState(), settings.mapColor(MapColor.STONE_GRAY).instrument(NoteBlockInstrument.BASEDRUM).requiresTool().strength(2.0F, 6.0F)));

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
	
	private static RegistrySupplier<Block> registerFadedTerracotta(DyeColor color)
	{
		RegistrySupplier<Block> supplier = register(color.asString()+"_faded_terracotta", settings -> 
			new GlazedTerracottaBlock(settings
					.mapColor(color)
					.instrument(NoteBlockInstrument.BASEDRUM)
					.requiresTool()
					.strength(1.4F)
					.pistonBehavior(PistonBehavior.PUSH_ONLY)));
		return supplier;
	}
	
	private static RegistrySupplier<Block> register(String nameIn, Function<AbstractBlock.Settings, Block> supplierIn)
	{
		tally++;
		Identifier id = Reference.ModInfo.prefix(nameIn);
		RegistryKey<Block> key = RegistryKey.of(RegistryKeys.BLOCK, id);
		AbstractBlock.Settings settings = AbstractBlock.Settings.create().registryKey(key);
		return BLOCKS.register(id, () -> supplierIn.apply(settings));
	}
	
	public static void init()
	{
		BLOCKS.register();
		Reclamation.LOGGER.info("# Initialised {} blocks", tally);
		
		DYE_TO_TERRACOTTA.put(DyeColor.BLACK, new Terracotta(() -> Blocks.BLACK_GLAZED_TERRACOTTA, RCBlocks.BLACK_FADED_TERRACOTTA, () -> Blocks.BLACK_TERRACOTTA));
		DYE_TO_TERRACOTTA.put(DyeColor.BLUE, new Terracotta(() -> Blocks.BLUE_GLAZED_TERRACOTTA, RCBlocks.BLUE_FADED_TERRACOTTA, () -> Blocks.BLUE_TERRACOTTA));
		DYE_TO_TERRACOTTA.put(DyeColor.BROWN, new Terracotta(() -> Blocks.BROWN_GLAZED_TERRACOTTA, RCBlocks.BROWN_FADED_TERRACOTTA, () -> Blocks.BROWN_TERRACOTTA));
		DYE_TO_TERRACOTTA.put(DyeColor.CYAN, new Terracotta(() -> Blocks.CYAN_GLAZED_TERRACOTTA, RCBlocks.CYAN_FADED_TERRACOTTA, () -> Blocks.CYAN_TERRACOTTA));
		DYE_TO_TERRACOTTA.put(DyeColor.GRAY, new Terracotta(() -> Blocks.GRAY_GLAZED_TERRACOTTA, RCBlocks.GRAY_FADED_TERRACOTTA, () -> Blocks.GRAY_TERRACOTTA));
		DYE_TO_TERRACOTTA.put(DyeColor.GREEN, new Terracotta(() -> Blocks.GREEN_GLAZED_TERRACOTTA, RCBlocks.GREEN_FADED_TERRACOTTA, () -> Blocks.GREEN_TERRACOTTA));
		DYE_TO_TERRACOTTA.put(DyeColor.LIGHT_BLUE, new Terracotta(() -> Blocks.LIGHT_BLUE_GLAZED_TERRACOTTA, RCBlocks.LIGHT_BLUE_FADED_TERRACOTTA, () -> Blocks.LIGHT_BLUE_TERRACOTTA));
		DYE_TO_TERRACOTTA.put(DyeColor.LIGHT_GRAY, new Terracotta(() -> Blocks.LIGHT_GRAY_GLAZED_TERRACOTTA, RCBlocks.LIGHT_GRAY_FADED_TERRACOTTA, () -> Blocks.LIGHT_GRAY_TERRACOTTA));
		DYE_TO_TERRACOTTA.put(DyeColor.LIME, new Terracotta(() -> Blocks.LIME_GLAZED_TERRACOTTA, RCBlocks.LIME_FADED_TERRACOTTA, () -> Blocks.LIME_TERRACOTTA));
		DYE_TO_TERRACOTTA.put(DyeColor.MAGENTA, new Terracotta(() -> Blocks.MAGENTA_GLAZED_TERRACOTTA, RCBlocks.MAGENTA_FADED_TERRACOTTA, () -> Blocks.MAGENTA_TERRACOTTA));
		DYE_TO_TERRACOTTA.put(DyeColor.ORANGE, new Terracotta(() -> Blocks.ORANGE_GLAZED_TERRACOTTA, RCBlocks.ORANGE_FADED_TERRACOTTA, () -> Blocks.ORANGE_TERRACOTTA));
		DYE_TO_TERRACOTTA.put(DyeColor.PINK, new Terracotta(() -> Blocks.PINK_GLAZED_TERRACOTTA, RCBlocks.PINK_FADED_TERRACOTTA, () -> Blocks.PINK_TERRACOTTA));
		DYE_TO_TERRACOTTA.put(DyeColor.PURPLE, new Terracotta(() -> Blocks.PURPLE_GLAZED_TERRACOTTA, RCBlocks.PURPLE_FADED_TERRACOTTA, () -> Blocks.PURPLE_TERRACOTTA));
		DYE_TO_TERRACOTTA.put(DyeColor.RED, new Terracotta(() -> Blocks.RED_GLAZED_TERRACOTTA, RCBlocks.RED_FADED_TERRACOTTA, () -> Blocks.RED_TERRACOTTA));
		DYE_TO_TERRACOTTA.put(DyeColor.WHITE, new Terracotta(() -> Blocks.WHITE_GLAZED_TERRACOTTA, RCBlocks.WHITE_FADED_TERRACOTTA, () -> Blocks.WHITE_TERRACOTTA));
		DYE_TO_TERRACOTTA.put(DyeColor.YELLOW, new Terracotta(() -> Blocks.YELLOW_GLAZED_TERRACOTTA, RCBlocks.YELLOW_FADED_TERRACOTTA, () -> Blocks.YELLOW_TERRACOTTA));
	}
}
