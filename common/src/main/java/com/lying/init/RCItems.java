package com.lying.init;

import java.util.function.Function;
import java.util.function.Supplier;

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
	private static int itemTally = 0, blockTally = 0;
	
	public static final RegistrySupplier<ItemGroup> RECLAMATION_TAB = TABS.register(Reference.ModInfo.MOD_ID, () -> CreativeTabRegistry.create(
			Text.translatable("itemGroup."+Reference.ModInfo.MOD_ID+".item_group"), 
			() -> new ItemStack(RCItems.WEATHERED_IRON.get())));
	
	public static final RegistrySupplier<Item> RUSTED_IRON		= registerBlock("rusted_iron", RCBlocks.RUSTED_IRON);
	public static final RegistrySupplier<Item> WEATHERED_IRON	= registerBlock("weathered_iron", RCBlocks.WEATHERED_IRON);
	public static final RegistrySupplier<Item> EXPOSED_IRON		= registerBlock("exposed_iron", RCBlocks.EXPOSED_IRON);
	public static final RegistrySupplier<Item> WAXED_IRON_BLOCK			= registerBlock("waxed_iron_block", RCBlocks.WAXED_IRON_BLOCK);
	public static final RegistrySupplier<Item> WAXED_RUSTED_IRON		= registerBlock("waxed_rusted_iron", RCBlocks.WAXED_RUSTED_IRON);
	public static final RegistrySupplier<Item> WAXED_WEATHERED_IRON		= registerBlock("waxed_weathered_iron", RCBlocks.WAXED_WEATHERED_IRON);
	public static final RegistrySupplier<Item> WAXED_EXPOSED_IRON		= registerBlock("waxed_exposed_iron", RCBlocks.WAXED_EXPOSED_IRON);
	public static final RegistrySupplier<Item> TARNISHED_GOLD	= registerBlock("tarnished_gold", RCBlocks.TARNISHED_GOLD);
	public static final RegistrySupplier<Item> LEAF_PILE		= registerBlock("leaf_pile", RCBlocks.LEAF_PILE);
	public static final RegistrySupplier<Item> IVY				= registerBlock("ivy", RCBlocks.IVY);
	// Mold
	// Mulch
	// Rubble pile
	public static final RegistrySupplier<Item> CRACKED_STONE_BRICK_SLAB		= registerBlock("cracked_stone_brick_slab", RCBlocks.CRACKED_STONE_BRICK_SLAB);
	public static final RegistrySupplier<Item> CRACKED_STONE_BRICK_STAIRS	= registerBlock("cracked_stone_brick_stairs", RCBlocks.CRACKED_STONE_BRICK_STAIRS);
	// Broken glass variants
	// Rotten plank variants
	// Rotten log variants
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
	
	public static final RegistrySupplier<Item> DOUSED_TORCH					= registerBlock("doused_torch", RCBlocks.DOUSED_TORCH);
	public static final RegistrySupplier<Item> DOUSED_LANTERN				= registerBlock("doused_lantern", RCBlocks.DOUSED_LANTERN);
	
	public static final RegistrySupplier<Item> WITHERING_DUST	= register("withering_dust", settings -> new Item(settings));
	
	private static RegistrySupplier<Item> registerTerracottaBlock(DyeColor color)
	{
		return registerBlock(color.asString()+"_faded_terracotta", (RegistrySupplier<Block>)RCBlocks.DYE_TO_TERRACOTTA.get(color).faded());
	}
	
	private static RegistrySupplier<Item> registerBlock(String nameIn, RegistrySupplier<Block> blockIn)
	{
		blockTally++;
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
		Reclamation.LOGGER.info("# Initialised {} items ({} block items)", itemTally, blockTally);
	}
}
