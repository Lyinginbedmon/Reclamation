package com.lying.init;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

import org.apache.commons.lang3.tuple.Pair;

import com.google.common.collect.Lists;
import com.lying.Reclamation;
import com.lying.block.BrokenGlassBlock;
import com.lying.block.BrokenGlassPaneBlock;
import com.lying.block.CrackedConcreteBlock;
import com.lying.block.DousedLanternBlock;
import com.lying.block.DousedTorchBlock;
import com.lying.block.FadedTerracottaBlock;
import com.lying.block.IvyBlock;
import com.lying.block.LeafPileBlock;
import com.lying.block.MoldBlock;
import com.lying.block.RaggedBannerBlock;
import com.lying.block.RaggedWallBannerBlock;
import com.lying.block.RottenFruitBlock;
import com.lying.block.RottenOrientedFruitBlock;
import com.lying.block.RubbleBlock;
import com.lying.block.ScrapBlock;
import com.lying.block.ScrapeableBlock;
import com.lying.block.SootBlock;
import com.lying.reference.Reference;
import com.lying.utility.RCUtils;
import com.lying.utility.WoodType;

import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.MapColor;
import net.minecraft.block.SlabBlock;
import net.minecraft.block.StairsBlock;
import net.minecraft.block.enums.NoteBlockInstrument;
import net.minecraft.block.piston.PistonBehavior;
import net.minecraft.entity.EntityType;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;

public class RCBlocks
{
	public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(Reference.ModInfo.MOD_ID, RegistryKeys.BLOCK);
	
	public static final List<RegistrySupplier<Block>> ALL_BLOCKS = Lists.newArrayList(), SOLID_CUBES = Lists.newArrayList();
	public static LeafPileBlock[] TINTED_LEAF_PILES = new LeafPileBlock[0];
	
	public static final Map<DyeColor, Terracotta> DYE_TO_TERRACOTTA = new HashMap<>();
	public static final Map<DyeColor, Concrete> DYE_TO_CONCRETE = new HashMap<>();
	public static final Map<DyeColor, Glass> DYE_TO_GLASS_BLOCK = new HashMap<>(), DYE_TO_GLASS_PANE = new HashMap<>();
	public static final Map<DyeColor, Banner> DYE_TO_BANNER	= new HashMap<>();
	
	private static final Map<DyeColor, Pair<RegistrySupplier<Block>,RegistrySupplier<Block>>> DYE_TO_RAGGED_BANNER = new HashMap<>();
	
	public static record Terracotta(Supplier<Block> glazed, Supplier<Block> faded, Supplier<Block> blank)
	{
		public static Terracotta of(Block glazed, Supplier<Block> faded, Block blank) { return new Terracotta(() -> glazed, faded, () -> blank); }
	}
	public static record Concrete(Supplier<Block> dry, Supplier<Block> cracked, Supplier<Block> powder)
	{
		public static Concrete of(Block dry, Supplier<Block> cracked, Block powder) { return new Concrete(() -> dry, cracked, () -> powder); }
	}
	public static record Glass(Supplier<Block> intact, Supplier<Block> broken)
	{
		public static Glass of(Supplier<Block> cracked, Block intact) { return new Glass(() -> intact, cracked); }
	}
	public static record Banner(Supplier<Block> floor, Supplier<Block> wall, Supplier<Block> floorRagged, Supplier<Block> wallRagged)
	{
		public static Banner of(Block floor, Block wall, Supplier<Block> floorRagged, Supplier<Block> wallRagged) { return new Banner(() -> floor, () -> wall, floorRagged, wallRagged); }
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
	
	public static final RegistrySupplier<Block> SOOT						= register("soot", settings -> new SootBlock(settings.mapColor(MapColor.BLACK).replaceable().nonOpaque().noCollision().requiresTool().strength(0.1F).sounds(BlockSoundGroup.SNOW).pistonBehavior(PistonBehavior.DESTROY)));
	public static final RegistrySupplier<Block> IVY							= register("ivy", settings -> new IvyBlock(settings.mapColor(MapColor.DARK_GREEN).replaceable().nonOpaque().noCollision().strength(0.2F).velocityMultiplier(0.7F).sounds(BlockSoundGroup.VINE).burnable().pistonBehavior(PistonBehavior.DESTROY)));
	public static final RegistrySupplier<Block> MOLD						= register("mold", settings -> new MoldBlock(settings.mapColor(MapColor.BLACK).replaceable().nonOpaque().noCollision().requiresTool().strength(0.1F).sounds(RCSoundEvents.ROTTEN_FRUIT_SOUNDS).pistonBehavior(PistonBehavior.DESTROY)));
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
	public static final RegistrySupplier<Block> ROTTEN_MELON				= register("rotten_melon", settings -> new RottenFruitBlock(settings.mapColor(MapColor.GRAY).requiresTool().strength(0.2F).sounds(RCSoundEvents.ROTTEN_FRUIT_SOUNDS).pistonBehavior(PistonBehavior.DESTROY)));
	public static final RegistrySupplier<Block> ROTTEN_PUMPKIN				= register("rotten_pumpkin", settings -> new RottenFruitBlock(settings.mapColor(MapColor.GRAY).requiresTool().strength(0.2F).sounds(RCSoundEvents.ROTTEN_FRUIT_SOUNDS).instrument(NoteBlockInstrument.DIDGERIDOO).pistonBehavior(PistonBehavior.DESTROY)));
	public static final RegistrySupplier<Block> ROTTEN_CARVED_PUMPKIN		= register("rotten_carved_pumpkin", settings -> new RottenOrientedFruitBlock(settings.mapColor(MapColor.GRAY).requiresTool().strength(0.2F).sounds(RCSoundEvents.ROTTEN_FRUIT_SOUNDS).pistonBehavior(PistonBehavior.DESTROY).allowsSpawning(RCBlocks::always)));
	public static final RegistrySupplier<Block> ROTTEN_JACK_O_LANTERN		= register("rotten_jack_o_lantern", settings -> new RottenOrientedFruitBlock(settings.mapColor(MapColor.GRAY).luminance(state -> 7).requiresTool().strength(0.2F).sounds(RCSoundEvents.ROTTEN_FRUIT_SOUNDS).pistonBehavior(PistonBehavior.DESTROY)));
	// TODO Rotten fruit textures/models
	public static final RegistrySupplier<Block> BROKEN_GLASS				= registerBrokenGlass("broken_glass", Function.identity());
	public static final RegistrySupplier<Block> BROKEN_GLASS_PANE			= registerBrokenGlassPane("broken_glass_pane", Function.identity());
	
	public static final RegistrySupplier<Block> DOUSED_TORCH				= register("doused_torch", settings -> new DousedTorchBlock(Blocks.TORCH, Blocks.WALL_TORCH, settings.noCollision().breakInstantly().sounds(BlockSoundGroup.WOOD).pistonBehavior(PistonBehavior.DESTROY)));
	public static final RegistrySupplier<Block> DOUSED_SOUL_TORCH			= register("doused_soul_torch", settings -> new DousedTorchBlock(Blocks.SOUL_TORCH, Blocks.SOUL_WALL_TORCH, settings.noCollision().breakInstantly().sounds(BlockSoundGroup.WOOD).pistonBehavior(PistonBehavior.DESTROY)));
	public static final RegistrySupplier<Block> DOUSED_LANTERN				= register("doused_lantern", settings -> new DousedLanternBlock(() -> Blocks.LANTERN, settings.mapColor(MapColor.IRON_GRAY).solid().strength(3.5F).sounds(BlockSoundGroup.LANTERN).nonOpaque().pistonBehavior(PistonBehavior.DESTROY)));
	public static final RegistrySupplier<Block> DOUSED_SOUL_LANTERN			= register("doused_soul_lantern", settings -> new DousedLanternBlock(() -> Blocks.SOUL_LANTERN, settings.mapColor(MapColor.IRON_GRAY).solid().strength(3.5F).sounds(BlockSoundGroup.LANTERN).nonOpaque().pistonBehavior(PistonBehavior.DESTROY)));
	
	public static final RegistrySupplier<Block> IRON_SCRAP					= register("iron_scrap", settings -> new ScrapBlock(settings.nonOpaque().mapColor(MapColor.BROWN).strength(0.3F).pistonBehavior(PistonBehavior.DESTROY)));
	public static final RegistrySupplier<Block> STONE_RUBBLE				= register("stone_rubble", settings -> new RubbleBlock(() -> Blocks.COBBLESTONE, settings.nonOpaque().mapColor(MapColor.STONE_GRAY).instrument(NoteBlockInstrument.BASEDRUM).requiresTool().strength(1.0F, 2.0F)));
	public static final RegistrySupplier<Block> DEEPSLATE_RUBBLE			= register("deepslate_rubble", settings -> new RubbleBlock(() -> Blocks.COBBLED_DEEPSLATE, settings.nonOpaque().mapColor(MapColor.DEEPSLATE_GRAY).instrument(NoteBlockInstrument.BASEDRUM).requiresTool().strength(1.0F, 2.0F)));
	
	private static void registerRaggedBanners(DyeColor color)
	{
		RegistrySupplier<Block> floor = registerBanner("ragged_"+color.asString()+"_banner", color, RaggedBannerBlock::new, Function.identity());
		RegistrySupplier<Block> wall = registerBanner("ragged_"+color.asString()+"_wall_banner", color, RaggedWallBannerBlock::new, settings -> copyLootTable(settings, floor.get(), true));
		DYE_TO_RAGGED_BANNER.put(color, Pair.of(floor, wall));
	}
	
	public static Block[] getAllBanners()
	{
		List<Block> banners = Lists.newArrayList();
		DYE_TO_RAGGED_BANNER.values().forEach(pair -> 
		{
			banners.add(pair.getLeft().get());
			banners.add(pair.getRight().get());
		});
		return banners.toArray(new Block[0]);
	}
	
	private static RegistrySupplier<Block> registerBanner(String nameIn, DyeColor color, BiFunction<DyeColor,AbstractBlock.Settings, Block> constructor, Function<AbstractBlock.Settings,AbstractBlock.Settings> funcIn)
	{
		return register(nameIn, settings -> constructor.apply(color, funcIn.apply(settings)
				.mapColor(MapColor.OAK_TAN)
				.solid()
				.instrument(NoteBlockInstrument.BASEDRUM)
				.noCollision()
				.strength(1F)
				.sounds(BlockSoundGroup.WOOD)
				.burnable()));
	}
	
	private static RegistrySupplier<Block> registerBrokenStainedGlass(DyeColor color, Block intact)
	{
		RegistrySupplier<Block> brokenGlass = registerBrokenGlass("broken_"+color.asString()+"_glass", settings -> settings.mapColor(color));
		DYE_TO_GLASS_BLOCK.put(color, Glass.of(brokenGlass, intact));
		return brokenGlass;
	}
	
	private static RegistrySupplier<Block> registerBrokenStainedGlassPane(DyeColor color, Block intact)
	{
		RegistrySupplier<Block> brokenGlass = registerBrokenGlassPane("broken_"+color.asString()+"_glass_pane", settings -> settings.mapColor(color));
		DYE_TO_GLASS_PANE.put(color, Glass.of(brokenGlass, intact));
		return brokenGlass;
	}
	
	private static RegistrySupplier<Block> registerBrokenGlass(String nameIn, Function<AbstractBlock.Settings,AbstractBlock.Settings> funcIn)
	{
		return register(nameIn, settings -> new BrokenGlassBlock(funcIn.apply(settings)
				.noCollision()
				.pistonBehavior(PistonBehavior.DESTROY)
				.nonOpaque()
				.strength(0.1F)
				.instrument(NoteBlockInstrument.HAT)
				.sounds(BlockSoundGroup.GLASS)
				.allowsSpawning(RCBlocks::never)
				.suffocates(RCBlocks::never)
				.blockVision(RCBlocks::never)));
	}
	
	private static RegistrySupplier<Block> registerBrokenGlassPane(String nameIn, Function<AbstractBlock.Settings,AbstractBlock.Settings> funcIn)
	{
		return register(nameIn, settings -> new BrokenGlassPaneBlock(funcIn.apply(settings)
				.noCollision()
				.pistonBehavior(PistonBehavior.DESTROY)
				.nonOpaque()
				.strength(0.3F)
				.instrument(NoteBlockInstrument.HAT)
				.sounds(BlockSoundGroup.GLASS)
				.allowsSpawning(RCBlocks::never)
				.suffocates(RCBlocks::never)
				.blockVision(RCBlocks::never)));
	}
	
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
	
	private static Boolean always(BlockState state, BlockView world, BlockPos pos, EntityType<?> type) { return true; }
	
	@SuppressWarnings("unused")
	private static Boolean always(BlockState state, BlockView world, BlockPos pos) { return true; }
	
	private static Boolean never(BlockState state, BlockView world, BlockPos pos, EntityType<?> type) { return false; }
	
	private static Boolean never(BlockState state, BlockView world, BlockPos pos) { return false; }
	
	private static AbstractBlock.Settings copyLootTable(AbstractBlock.Settings settings, Block block, boolean copyTranslationKey)
	{
		settings.lootTable(block.getLootTableKey());
		if(copyTranslationKey)
			settings.overrideTranslationKey(block.getTranslationKey());
		return settings;
	}
	
	public static void init()
	{
		// Bulk registration of colour spectrum blocks
		Map<DyeColor,RegistrySupplier<Block>> crackedConcrete = new HashMap<>(), fadedTerracotta = new HashMap<>();
		for(DyeColor color : RCUtils.COLOR_SPECTRUM)
		{
			crackedConcrete.put(color, registerCrackedConcrete(color));
			fadedTerracotta.put(color, registerFadedTerracotta(color));
			
			registerRaggedBanners(color);
			registerBrokenStainedGlass(color, RCUtils.dyeToStainedGlass(color));
			registerBrokenStainedGlassPane(color, RCUtils.dyeToStainedGlassPane(color));
		}
		
		BLOCKS.register();
		Reclamation.LOGGER.info("# Initialised {} blocks", ALL_BLOCKS.size());
		
		for(DyeColor color : DyeColor.values())
		{
			DYE_TO_CONCRETE.put(color, Concrete.of(RCUtils.dyeToConcrete(color), crackedConcrete.get(color), RCUtils.dyeToConcretePowder(color)));
			DYE_TO_TERRACOTTA.put(color, Terracotta.of(RCUtils.dyeToGlazedTerracotta(color), fadedTerracotta.get(color), RCUtils.dyeToTerracotta(color)));
			
			Pair<RegistrySupplier<Block>, RegistrySupplier<Block>> raggedBanners = DYE_TO_RAGGED_BANNER.get(color);
			DYE_TO_BANNER.put(color, Banner.of(RCUtils.dyeToBanner(color).getLeft(), RCUtils.dyeToBanner(color).getRight(), raggedBanners.getLeft(), raggedBanners.getRight()));
		}
		
		TINTED_LEAF_PILES = List.of(
				ACACIA_LEAF_PILE,
				DARK_OAK_LEAF_PILE,
				JUNGLE_LEAF_PILE,
				MANGROVE_LEAF_PILE,
				OAK_LEAF_PILE
				).stream().map(Supplier::get).map(b -> (LeafPileBlock)b).toList().toArray(new LeafPileBlock[0]);
	}
}
