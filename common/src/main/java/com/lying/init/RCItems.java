package com.lying.init;

import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

import com.google.common.collect.Lists;
import com.lying.Reclamation;
import com.lying.reference.Reference;

import dev.architectury.registry.CreativeTabRegistry;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.Item.Settings;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.text.Text;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Identifier;

public class RCItems
{
	public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(Reference.ModInfo.MOD_ID, RegistryKeys.ITEM);
	public static final DeferredRegister<ItemGroup> TABS = DeferredRegister.create(Reference.ModInfo.MOD_ID, RegistryKeys.ITEM_GROUP);
	private static int itemTally = 0;
	
	public static List<RegistrySupplier<Item>> BASIC_BLOCK_ITEMS = Lists.newArrayList();
	
	public static final RegistrySupplier<ItemGroup> RECLAMATION_TAB = TABS.register(Reference.ModInfo.MOD_ID, () -> CreativeTabRegistry.create(
			Text.translatable("itemGroup."+Reference.ModInfo.MOD_ID+".item_group"), 
			() -> new ItemStack(RCItems.WEATHERED_IRON.get())));
	
	public static final RegistrySupplier<Item> WITHERING_DUST	= register("withering_dust", settings -> new Item(settings));
	
	public static final RegistrySupplier<Item> RUSTED_IRON					= registerBlock("rusted_iron", RCBlocks.RUSTED_IRON);
	public static final RegistrySupplier<Item> WEATHERED_IRON				= registerBlock("weathered_iron", RCBlocks.WEATHERED_IRON);
	public static final RegistrySupplier<Item> EXPOSED_IRON					= registerBlock("exposed_iron", RCBlocks.EXPOSED_IRON);
	public static final RegistrySupplier<Item> WAXED_IRON_BLOCK				= registerBlock("waxed_iron_block", RCBlocks.WAXED_IRON_BLOCK);
	public static final RegistrySupplier<Item> WAXED_RUSTED_IRON			= registerBlock("waxed_rusted_iron", RCBlocks.WAXED_RUSTED_IRON);
	public static final RegistrySupplier<Item> WAXED_WEATHERED_IRON			= registerBlock("waxed_weathered_iron", RCBlocks.WAXED_WEATHERED_IRON);
	public static final RegistrySupplier<Item> WAXED_EXPOSED_IRON			= registerBlock("waxed_exposed_iron", RCBlocks.WAXED_EXPOSED_IRON);
	public static final RegistrySupplier<Item> WAXED_GOLD_BLOCK				= registerBlock("waxed_gold_block", RCBlocks.WAXED_GOLD_BLOCK);
	public static final RegistrySupplier<Item> TARNISHED_GOLD				= registerBlock("tarnished_gold", RCBlocks.TARNISHED_GOLD);
	public static final RegistrySupplier<Item> WAXED_TARNISHED_GOLD			= registerBlock("waxed_tarnished_gold", RCBlocks.WAXED_TARNISHED_GOLD);
	public static final RegistrySupplier<Item> OAK_LEAF_PILE				= registerBlockNoItem("oak_leaf_pile", RCBlocks.OAK_LEAF_PILE);
	public static final RegistrySupplier<Item> ACACIA_LEAF_PILE				= registerBlockNoItem("acacia_leaf_pile", RCBlocks.ACACIA_LEAF_PILE);
	public static final RegistrySupplier<Item> BIRCH_LEAF_PILE				= registerBlockNoItem("birch_leaf_pile", RCBlocks.BIRCH_LEAF_PILE);
	public static final RegistrySupplier<Item> CHERRY_LEAF_PILE				= registerBlock("cherry_leaf_pile", RCBlocks.CHERRY_LEAF_PILE);
	public static final RegistrySupplier<Item> DARK_OAK_LEAF_PILE			= registerBlockNoItem("dark_oak_leaf_pile", RCBlocks.DARK_OAK_LEAF_PILE);
	public static final RegistrySupplier<Item> JUNGLE_LEAF_PILE				= registerBlockNoItem("jungle_leaf_pile", RCBlocks.JUNGLE_LEAF_PILE);
	public static final RegistrySupplier<Item> MANGROVE_LEAF_PILE			= registerBlockNoItem("mangrove_leaf_pile", RCBlocks.MANGROVE_LEAF_PILE);
	public static final RegistrySupplier<Item> PALE_LEAF_PILE				= registerBlock("pale_leaf_pile", RCBlocks.PALE_LEAF_PILE);
	public static final RegistrySupplier<Item> SPRUCE_LEAF_PILE				= registerBlockNoItem("spruce_leaf_pile", RCBlocks.SPRUCE_LEAF_PILE);
	public static final RegistrySupplier<Item> SOOT							= registerBlockNoItem("soot", RCBlocks.SOOT);
	public static final RegistrySupplier<Item> IVY							= registerBlockNoItem("ivy", RCBlocks.IVY);
	public static final RegistrySupplier<Item> CRACKED_STONE_BRICK_SLAB		= registerBlock("cracked_stone_brick_slab", RCBlocks.CRACKED_STONE_BRICK_SLAB);
	public static final RegistrySupplier<Item> CRACKED_STONE_BRICK_STAIRS	= registerBlock("cracked_stone_brick_stairs", RCBlocks.CRACKED_STONE_BRICK_STAIRS);
	public static final RegistrySupplier<Item> DOUSED_TORCH					= registerBlock("doused_torch", RCBlocks.DOUSED_TORCH);
	public static final RegistrySupplier<Item> DOUSED_SOUL_TORCH			= registerBlock("doused_soul_torch", RCBlocks.DOUSED_SOUL_TORCH);
	public static final RegistrySupplier<Item> DOUSED_LANTERN				= registerBlock("doused_lantern", RCBlocks.DOUSED_LANTERN);
	public static final RegistrySupplier<Item> DOUSED_SOUL_LANTERN			= registerBlock("doused_soul_lantern", RCBlocks.DOUSED_SOUL_LANTERN);
	public static final RegistrySupplier<Item> BLACK_FADED_TERRACOTTA		= registerTerracottaBlock(DyeColor.BLACK);
	public static final RegistrySupplier<Item> BLUE_FADED_TERRACOTTA		= registerTerracottaBlock(DyeColor.BLUE);
	public static final RegistrySupplier<Item> BROWN_FADED_TERRACOTTA		= registerTerracottaBlock(DyeColor.BROWN);
	public static final RegistrySupplier<Item> CYAN_FADED_TERRACOTTA		= registerTerracottaBlock(DyeColor.CYAN);
	public static final RegistrySupplier<Item> GRAY_FADED_TERRACOTTA		= registerTerracottaBlock(DyeColor.GRAY);
	public static final RegistrySupplier<Item> GREEN_FADED_TERRACOTTA		= registerTerracottaBlock(DyeColor.GREEN);
	public static final RegistrySupplier<Item> LIGHT_BLUE_FADED_TERRACOTTA	= registerTerracottaBlock(DyeColor.LIGHT_BLUE);
	public static final RegistrySupplier<Item> LIGHT_GRAY_FADED_TERRACOTTA	= registerTerracottaBlock(DyeColor.LIGHT_GRAY);
	public static final RegistrySupplier<Item> LIME_FADED_TERRACOTTA		= registerTerracottaBlock(DyeColor.LIME);
	public static final RegistrySupplier<Item> MAGENTA_FADED_TERRACOTTA		= registerTerracottaBlock(DyeColor.MAGENTA);
	public static final RegistrySupplier<Item> ORANGE_FADED_TERRACOTTA		= registerTerracottaBlock(DyeColor.ORANGE);
	public static final RegistrySupplier<Item> PINK_FADED_TERRACOTTA		= registerTerracottaBlock(DyeColor.PINK);
	public static final RegistrySupplier<Item> PURPLE_FADED_TERRACOTTA		= registerTerracottaBlock(DyeColor.PURPLE);
	public static final RegistrySupplier<Item> RED_FADED_TERRACOTTA			= registerTerracottaBlock(DyeColor.RED);
	public static final RegistrySupplier<Item> WHITE_FADED_TERRACOTTA		= registerTerracottaBlock(DyeColor.WHITE);
	public static final RegistrySupplier<Item> YELLOW_FADED_TERRACOTTA		= registerTerracottaBlock(DyeColor.YELLOW);
	
	private static RegistrySupplier<Item> registerTerracottaBlock(DyeColor color)
	{
		return registerBlock(color.asString()+"_faded_terracotta", (RegistrySupplier<Block>)RCBlocks.DYE_TO_TERRACOTTA.get(color).faded());
	}
	
	private static RegistrySupplier<Item> registerBlock(String nameIn, RegistrySupplier<Block> blockIn)
	{
		RegistrySupplier<Item> registry = registerBlockNoItem(nameIn, blockIn);
		BASIC_BLOCK_ITEMS.add(registry);
		return registry;
	}
	
	private static RegistrySupplier<Item> registerBlockNoItem(String nameIn, RegistrySupplier<Block> blockIn)
	{
		Identifier id = Reference.ModInfo.prefix(nameIn);
		RegistryKey<Item> key = RegistryKey.of(RegistryKeys.ITEM, id);
		Item.Settings settings = new Item.Settings().useBlockPrefixedTranslationKey().registryKey(key).arch$tab(RECLAMATION_TAB);
		return register(id, () -> new BlockItem(blockIn.get(), settings));
	}
	
	private static RegistrySupplier<Item> register(String nameIn, Function<Settings,Item> supplierIn)
	{
		Identifier id = Reference.ModInfo.prefix(nameIn);
		RegistryKey<Item> key = RegistryKey.of(RegistryKeys.ITEM, id);
		Item.Settings settings = new Item.Settings().registryKey(key).arch$tab(RECLAMATION_TAB);
		return register(id, () -> supplierIn.apply(settings));
	}
	
	private static RegistrySupplier<Item> register(Identifier id, Supplier<Item> supplierIn)
	{
		itemTally++;
		return ITEMS.register(id, supplierIn);
	}
	
	public static void init()
	{
		TABS.register();
		ITEMS.register();
		Reclamation.LOGGER.info("# Initialised {} items ({} block items)", itemTally, BASIC_BLOCK_ITEMS.size());
	}
}
