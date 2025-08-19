package com.lying.init;

import static com.lying.reference.Reference.ModInfo.prefix;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

import com.google.common.collect.Lists;
import com.lying.Reclamation;
import com.lying.block.RaggedBannerBlock;
import com.lying.block.RaggedWallBannerBlock;
import com.lying.item.DeactivatorItem;
import com.lying.item.DecayDustItem;
import com.lying.item.RaggedBannerItem;
import com.lying.item.RottenFruitItem;
import com.lying.reference.Reference;
import com.lying.utility.RCUtils;

import dev.architectury.registry.CreativeTabRegistry;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.block.Block;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.EquippableComponent;
import net.minecraft.entity.EquipmentSlot;
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
import net.minecraft.util.Rarity;

public class RCItems
{
	public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(Reference.ModInfo.MOD_ID, RegistryKeys.ITEM);
	public static final DeferredRegister<ItemGroup> TABS = DeferredRegister.create(Reference.ModInfo.MOD_ID, RegistryKeys.ITEM_GROUP);
	private static int itemTally = 0, blockTally = 0;
	
	public static Map<DyeColor, RegistrySupplier<Item>> DYE_TO_SHARD = new HashMap<>();
	protected static final List<RegistrySupplier<Item>> ALL_BLOCKS	= Lists.newArrayList();
	public static List<RegistrySupplier<Item>> BASIC_BLOCK_ITEMS = Lists.newArrayList();
	
	public static final RegistrySupplier<ItemGroup> RECLAMATION_TAB = TABS.register(Reference.ModInfo.MOD_ID, () -> CreativeTabRegistry.create(
			Text.translatable("itemGroup."+Reference.ModInfo.MOD_ID+".item_group"), 
			() -> new ItemStack(RCItems.WEATHERED_IRON.get())));
	
	public static final RegistrySupplier<Item> WITHERING_DUST				= register("withering_dust", settings -> new DecayDustItem(settings));
	public static final RegistrySupplier<Item> DEACTIVATOR					= register("deactivator", settings -> new DeactivatorItem(settings.maxCount(1).rarity(Rarity.EPIC)));
	public static final RegistrySupplier<Item> GLASS_SHARD					= register("glass_shard", settings -> new Item(settings));
	
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
	public static final RegistrySupplier<Item> ACACIA_LEAF_PILE				= registerBlockNoItem("acacia_leaf_pile", RCBlocks.ACACIA_LEAF_PILE);
	public static final RegistrySupplier<Item> BIRCH_LEAF_PILE				= registerBlockNoItem("birch_leaf_pile", RCBlocks.BIRCH_LEAF_PILE);
	public static final RegistrySupplier<Item> CHERRY_LEAF_PILE				= registerBlockNoItem("cherry_leaf_pile", RCBlocks.CHERRY_LEAF_PILE);
	public static final RegistrySupplier<Item> DARK_OAK_LEAF_PILE			= registerBlockNoItem("dark_oak_leaf_pile", RCBlocks.DARK_OAK_LEAF_PILE);
	public static final RegistrySupplier<Item> JUNGLE_LEAF_PILE				= registerBlockNoItem("jungle_leaf_pile", RCBlocks.JUNGLE_LEAF_PILE);
	public static final RegistrySupplier<Item> MANGROVE_LEAF_PILE			= registerBlockNoItem("mangrove_leaf_pile", RCBlocks.MANGROVE_LEAF_PILE);
	public static final RegistrySupplier<Item> OAK_LEAF_PILE				= registerBlockNoItem("oak_leaf_pile", RCBlocks.OAK_LEAF_PILE);
	public static final RegistrySupplier<Item> PALE_LEAF_PILE				= registerBlockNoItem("pale_leaf_pile", RCBlocks.PALE_LEAF_PILE);
	public static final RegistrySupplier<Item> SPRUCE_LEAF_PILE				= registerBlockNoItem("spruce_leaf_pile", RCBlocks.SPRUCE_LEAF_PILE);
	public static final RegistrySupplier<Item> SOOT							= registerBlockNoItem("soot", RCBlocks.SOOT);
	public static final RegistrySupplier<Item> IVY							= registerBlockNoItem("ivy", RCBlocks.IVY);
	public static final RegistrySupplier<Item> MOLD							= registerBlockNoItem("mold", RCBlocks.MOLD);
	public static final RegistrySupplier<Item> ROTTEN_MELON					= registerBlock("rotten_melon", RCBlocks.ROTTEN_MELON);
	public static final RegistrySupplier<Item> ROTTEN_PUMPKIN				= registerBlock("rotten_pumpkin", RCBlocks.ROTTEN_PUMPKIN);
	public static final RegistrySupplier<Item> ROTTEN_CARVED_PUMPKIN		= register("rotten_carved_pumpkin", settings -> new RottenFruitItem(RCBlocks.ROTTEN_CARVED_PUMPKIN.get(), settings
			.useBlockPrefixedTranslationKey()
			.component(
				DataComponentTypes.EQUIPPABLE, 
				EquippableComponent.builder(EquipmentSlot.HEAD).swappable(false).cameraOverlay(Identifier.ofVanilla("misc/pumpkinblur")).build())));
	public static final RegistrySupplier<Item> ROTTEN_JACK_O_LANTERN		= registerBlock("rotten_jack_o_lantern", RCBlocks.ROTTEN_JACK_O_LANTERN);
	public static final RegistrySupplier<Item> CRACKED_STONE_BRICK_SLAB		= registerBlock("cracked_stone_brick_slab", RCBlocks.CRACKED_STONE_BRICK_SLAB);
	public static final RegistrySupplier<Item> CRACKED_STONE_BRICK_STAIRS	= registerBlock("cracked_stone_brick_stairs", RCBlocks.CRACKED_STONE_BRICK_STAIRS);
	public static final RegistrySupplier<Item> DOUSED_TORCH					= registerBlock("doused_torch", RCBlocks.DOUSED_TORCH);
	public static final RegistrySupplier<Item> DOUSED_SOUL_TORCH			= registerBlock("doused_soul_torch", RCBlocks.DOUSED_SOUL_TORCH);
	public static final RegistrySupplier<Item> DOUSED_LANTERN				= registerBlock("doused_lantern", RCBlocks.DOUSED_LANTERN);
	public static final RegistrySupplier<Item> DOUSED_SOUL_LANTERN			= registerBlock("doused_soul_lantern", RCBlocks.DOUSED_SOUL_LANTERN);
	public static final RegistrySupplier<Item> IRON_SCRAP					= registerBlock("iron_scrap", RCBlocks.IRON_SCRAP);
	public static final RegistrySupplier<Item> STONE_RUBBLE					= registerBlockNoItem("stone_rubble", RCBlocks.STONE_RUBBLE);
	public static final RegistrySupplier<Item> DEEPSLATE_RUBBLE				= registerBlockNoItem("deepslate_rubble", RCBlocks.DEEPSLATE_RUBBLE);
	public static final RegistrySupplier<Item> BROKEN_GLASS					= registerBlockNoItem("broken_glass", RCBlocks.BROKEN_GLASS);
	public static final RegistrySupplier<Item> BROKEN_GLASS_PANE			= registerBlockNoItem("broken_glass_pane", RCBlocks.BROKEN_GLASS_PANE);
	
	private static RegistrySupplier<Item> registerGlassShard(DyeColor color)
	{
		RegistrySupplier<Item> shard = register(color.asString()+"_glass_shard", settings -> new Item(settings));
		DYE_TO_SHARD.put(color, shard);
		return shard;
	}
	
	private static RegistrySupplier<Item> registerBrokenGlass(DyeColor color)
	{
		return registerBlockNoItem("broken_"+color.asString()+"_glass", (RegistrySupplier<Block>)RCBlocks.DYE_TO_GLASS_BLOCK.get(color).broken());
	}
	
	private static RegistrySupplier<Item> registerBrokenGlassPane(DyeColor color)
	{
		return registerBlockNoItem("broken_"+color.asString()+"_glass_pane", (RegistrySupplier<Block>)RCBlocks.DYE_TO_GLASS_PANE.get(color).broken());
	}
	
	private static RegistrySupplier<Item> registerRaggedBanner(DyeColor color)
	{
		RegistryKey<Item> key = RegistryKey.of(RegistryKeys.ITEM, prefix("ragged_"+color.asString()+"_banner"));
		return registerBlockItem("ragged_"+color.asString()+"_banner", () -> new RaggedBannerItem(RaggedBannerBlock.getForColor(color), RaggedWallBannerBlock.getForColor(color), new Item.Settings()
				.useBlockPrefixedTranslationKey()
				.registryKey(key)
				.arch$tab(RECLAMATION_TAB)
				.maxCount(16)));
	}
	
	private static RegistrySupplier<Item> registerTerracottaBlock(DyeColor color)
	{
		return registerBlock(color.asString()+"_faded_terracotta", (RegistrySupplier<Block>)RCBlocks.DYE_TO_TERRACOTTA.get(color).faded());
	}
	
	private static RegistrySupplier<Item> registerCrackedConcrete(DyeColor color)
	{
		return registerBlockNoItem("cracked_"+color.asString()+"_concrete", (RegistrySupplier<Block>)RCBlocks.DYE_TO_CONCRETE.get(color).cracked());
	}
	
	private static RegistrySupplier<Item> registerBlock(String nameIn, RegistrySupplier<Block> blockIn)
	{
		return registerBlock(nameIn, blockIn, UnaryOperator.identity());
	}
	
	private static RegistrySupplier<Item> registerBlock(String nameIn, RegistrySupplier<Block> blockIn, UnaryOperator<Item.Settings> settingsOp)
	{
		RegistrySupplier<Item> registry = registerBlockNoItem(nameIn, blockIn, settingsOp);
		BASIC_BLOCK_ITEMS.add(registry);
		return registry;
	}
	
	private static RegistrySupplier<Item> registerBlockNoItem(String nameIn, RegistrySupplier<Block> blockIn)
	{
		return registerBlockNoItem(nameIn, blockIn, UnaryOperator.identity());
	}
	
	private static RegistrySupplier<Item> registerBlockNoItem(String nameIn, RegistrySupplier<Block> blockIn, UnaryOperator<Item.Settings> settingsOp)
	{
		Identifier id = prefix(nameIn);
		RegistryKey<Item> key = RegistryKey.of(RegistryKeys.ITEM, id);
		return registerBlockItem(nameIn, () -> new BlockItem(blockIn.get(), settingsOp.apply(new Item.Settings().useBlockPrefixedTranslationKey().registryKey(key).arch$tab(RECLAMATION_TAB))));
	}
	
	private static RegistrySupplier<Item> registerBlockItem(String nameIn, Supplier<Item> supplier)
	{
		RegistrySupplier<Item> registry = register(prefix(nameIn), supplier);
		ALL_BLOCKS.add(registry);
		blockTally++;
		return registry;
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
		// Bulk registration of colour spectrum items
		for(Consumer<DyeColor> group : List.<Consumer<DyeColor>>of(
				RCItems::registerCrackedConcrete, 
				RCItems::registerTerracottaBlock, 
				RCItems::registerRaggedBanner,
				RCItems::registerBrokenGlass,
				RCItems::registerBrokenGlassPane,
				RCItems::registerGlassShard
				))
			for(DyeColor color : RCUtils.COLOR_SPECTRUM)
				group.accept(color);
		
		TABS.register();
		ITEMS.register();
		Reclamation.LOGGER.info("# Initialised {} items ({} block items)", itemTally, blockTally);
	}
}
