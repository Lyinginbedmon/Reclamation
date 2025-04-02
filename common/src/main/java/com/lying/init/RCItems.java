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
	
	public static final RegistrySupplier<Item> WITHERING_DUST	= register("withering_dust", settings -> new Item(settings));
	
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
