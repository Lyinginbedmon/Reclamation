package com.lying.init;

import java.util.function.Function;

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
import net.minecraft.block.MapColor;
import net.minecraft.block.SlabBlock;
import net.minecraft.block.StairsBlock;
import net.minecraft.block.enums.NoteBlockInstrument;
import net.minecraft.block.piston.PistonBehavior;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.Identifier;

public class RCBlocks
{
	public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(Reference.ModInfo.MOD_ID, RegistryKeys.BLOCK);
	private static int tally = 0;
	
	public static final RegistrySupplier<Block> WAXED_IRON_BLOCK		= register("waxed_iron_block", settings -> new ScrapeableBlock(() -> Blocks.IRON_BLOCK, settings.mapColor(MapColor.IRON_GRAY).instrument(NoteBlockInstrument.IRON_XYLOPHONE).requiresTool().strength(5F, 6F).sounds(BlockSoundGroup.METAL)));
	public static final RegistrySupplier<Block> WAXED_EXPOSED_IRON		= register("waxed_exposed_iron", settings -> new ScrapeableBlock(RCBlocks.EXPOSED_IRON, settings.mapColor(MapColor.LIGHT_GRAY).instrument(NoteBlockInstrument.IRON_XYLOPHONE).sounds(BlockSoundGroup.METAL)));
	public static final RegistrySupplier<Block> WAXED_WEATHERED_IRON	= register("waxed_weathered_iron", settings -> new ScrapeableBlock(RCBlocks.WEATHERED_IRON, settings.mapColor(MapColor.DULL_PINK).instrument(NoteBlockInstrument.IRON_XYLOPHONE).sounds(BlockSoundGroup.METAL)));
	public static final RegistrySupplier<Block> WAXED_RUSTED_IRON		= register("waxed_rusted_iron", settings -> new ScrapeableBlock(RCBlocks.RUSTED_IRON, settings.mapColor(MapColor.ORANGE).instrument(NoteBlockInstrument.IRON_XYLOPHONE).sounds(BlockSoundGroup.METAL)));
	
	public static final RegistrySupplier<Block> EXPOSED_IRON			= register("exposed_iron", settings -> new ScrapeableBlock(() -> Blocks.IRON_BLOCK, settings.mapColor(MapColor.LIGHT_GRAY).instrument(NoteBlockInstrument.IRON_XYLOPHONE).sounds(BlockSoundGroup.METAL)));
	public static final RegistrySupplier<Block> WEATHERED_IRON			= register("weathered_iron", settings -> new ScrapeableBlock(RCBlocks.EXPOSED_IRON, settings.mapColor(MapColor.DULL_PINK).instrument(NoteBlockInstrument.IRON_XYLOPHONE).sounds(BlockSoundGroup.METAL)));
	public static final RegistrySupplier<Block> RUSTED_IRON				= register("rusted_iron", settings -> new ScrapeableBlock(RCBlocks.WEATHERED_IRON, settings.mapColor(MapColor.ORANGE).instrument(NoteBlockInstrument.IRON_XYLOPHONE).sounds(BlockSoundGroup.METAL)));
	
	public static final RegistrySupplier<Block> TARNISHED_GOLD			= register("tarnished_gold", settings -> new ScrapeableBlock(() -> Blocks.GOLD_BLOCK, settings.mapColor(MapColor.YELLOW).sounds(BlockSoundGroup.METAL)));
	public static final RegistrySupplier<Block> LEAF_PILE				= register("leaf_pile", settings -> new CarpetBlock(settings.mapColor(MapColor.GREEN).strength(0.1F).sounds(BlockSoundGroup.MOSS_CARPET).pistonBehavior(PistonBehavior.DESTROY)));
	public static final RegistrySupplier<Block> IVY						= register("ivy", settings -> new IvyBlock(settings.mapColor(MapColor.DARK_GREEN).replaceable().noCollision().strength(0.2F).sounds(BlockSoundGroup.VINE).burnable().pistonBehavior(PistonBehavior.DESTROY)));
	public static final RegistrySupplier<Block> CRACKED_STONE_BRICK_SLAB	= register("cracked_stone_brick_slab", settings -> new SlabBlock(settings.mapColor(MapColor.STONE_GRAY).instrument(NoteBlockInstrument.BASEDRUM).requiresTool().strength(2.0F, 6.0F)));
	public static final RegistrySupplier<Block> CRACKED_STONE_BRICK_STAIRS	= register("cracked_stone_brick_stairs", settings -> new StairsBlock(Blocks.CRACKED_STONE_BRICKS.getDefaultState(), settings.mapColor(MapColor.STONE_GRAY).instrument(NoteBlockInstrument.BASEDRUM).requiresTool().strength(2.0F, 6.0F)));
	
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
	}
}
