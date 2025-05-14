package com.lying.decay;

import java.util.Collection;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.lying.data.RCBlockTags;
import com.lying.decay.conditions.ConditionBoolean;
import com.lying.decay.conditions.ConditionHasProperty;
import com.lying.decay.conditions.ConditionIsBlock;
import com.lying.decay.conditions.ConditionNeighbouring;
import com.lying.decay.conditions.ConditionPosition;
import com.lying.decay.functions.FunctionBlockState;
import com.lying.decay.functions.FunctionConvert;
import com.lying.decay.functions.FunctionMacro;
import com.lying.decay.functions.FunctionSprout;
import com.lying.decay.handler.DecayEntry;
import com.lying.init.RCBlocks;
import com.lying.init.RCDecayConditions;
import com.lying.init.RCDecayFunctions;
import com.lying.utility.BlockSaturationCalculator;
import com.lying.utility.BlockSaturationCalculator.Mode;
import com.lying.utility.PropertyMap;

import net.minecraft.block.Blocks;
import net.minecraft.block.CampfireBlock;
import net.minecraft.block.LanternBlock;
import net.minecraft.block.enums.SlabType;
import net.minecraft.entity.attribute.EntityAttributeModifier.Operation;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.state.property.Properties;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Direction;

public class DefaultDecayLibrary 
{
	private static final Map<Identifier, DecayEntry> DATA = new HashMap<>();
	
	private static void register(DecayEntry dataIn)
	{
		DATA.put(dataIn.packName(), dataIn);
	}
	
	public static Collection<DecayEntry> getDefaults() { return DATA.values(); }
	
	private static void registerStoneBrick()
	{
		register(DecayEntry.Builder.create(
				DecayChance.base(0.2F)
					.addModifier(Operation.ADD_MULTIPLIED_TOTAL, BlockSaturationCalculator.Builder.create().min(0.1F).power(0.2F).searchRange(4).blockCap(9).blocks(Blocks.CRACKED_STONE_BRICKS).build()))
				.name("stone_brick_to_cracked_stone_brick")
				.condition(ConditionIsBlock.of(Blocks.STONE_BRICKS))
				.function(FunctionConvert.toBlock(Blocks.CRACKED_STONE_BRICKS)).build());
		register(DecayEntry.Builder.create(
				DecayChance.base(0.1F)
					.addModifier(Operation.ADD_MULTIPLIED_TOTAL, BlockSaturationCalculator.Builder.create().min(0.1F).power(0.2F).searchRange(4).blockCap(9).blocks(Blocks.MOSSY_STONE_BRICKS).build()))
				.name("stone_brick_to_mossy_stone_brick")
				.condition(ConditionIsBlock.of(Blocks.STONE_BRICKS))
				.function(FunctionConvert.toBlock(Blocks.MOSSY_STONE_BRICKS)).build());
		
		register(DecayEntry.Builder.create(
				DecayChance.base(0.15F)
					.addModifier(Operation.ADD_MULTIPLIED_TOTAL, BlockSaturationCalculator.Builder.create().min(0.1F).power(0.2F).searchRange(4).blockCap(9).blocks(Blocks.MOSSY_STONE_BRICK_STAIRS).build()))
				.name("mossy_stone_brick_to_mossy_stone_brick_stairs")
				.condition(ConditionIsBlock.of(Blocks.MOSSY_STONE_BRICKS))
				.function(
					FunctionConvert.toBlock(Blocks.MOSSY_STONE_BRICK_STAIRS),
					FunctionBlockState.RandomValue.of(Properties.HORIZONTAL_FACING)).build());
		register(DecayEntry.Builder.create(
				DecayChance.base(0.15F)
					.addModifier(Operation.ADD_MULTIPLIED_TOTAL, BlockSaturationCalculator.Builder.create().min(0.1F).power(0.2F).searchRange(4).blockCap(9).blocks(Blocks.MOSSY_STONE_BRICK_STAIRS).build()))
				.name("mossy_stone_brick_stairs_to_mossy_stone_brick_slab")
				.condition(ConditionIsBlock.of(Blocks.MOSSY_STONE_BRICK_STAIRS))
				.function(
					FunctionConvert.toBlock(Blocks.MOSSY_STONE_BRICK_SLAB),
					FunctionBlockState.RandomValue.of(Properties.SLAB_TYPE)).build());
		register(DecayEntry.Builder.create(
				DecayChance.base(0.15F)
					.addModifier(Operation.ADD_MULTIPLIED_TOTAL, BlockSaturationCalculator.Builder.create().min(0.1F).power(0.2F).searchRange(4).blockCap(9).blocks(Blocks.MOSSY_STONE_BRICK_SLAB).build()))
				.name("mossy_stone_brick_slab_vanish")
				.condition(ConditionIsBlock.of(Blocks.MOSSY_STONE_BRICK_SLAB))
				.function(
					FunctionConvert.toBlockState(Blocks.MOSSY_STONE_BRICK_SLAB.getDefaultState().with(Properties.SLAB_TYPE, SlabType.BOTTOM)),
					RCDecayFunctions.FALL.get(),
					RCDecayFunctions.TO_AIR.get()).build());
		
		register(DecayEntry.Builder.create(
				DecayChance.base(0.4F)
					.addModifier(Operation.ADD_MULTIPLIED_TOTAL, BlockSaturationCalculator.Builder.create().min(0.1F).power(0.2F).searchRange(4).blockCap(9).blocks(RCBlocks.CRACKED_STONE_BRICK_STAIRS.get()).build()))
				.name("cracked_stone_brick_to_cracked_stone_brick_stairs")
				.condition(ConditionIsBlock.of(Blocks.CRACKED_STONE_BRICKS))
				.function(
					FunctionConvert.toBlock(RCBlocks.CRACKED_STONE_BRICK_STAIRS.get()),
					FunctionBlockState.RandomValue.of(Properties.HORIZONTAL_FACING)).build());
		register(DecayEntry.Builder.create(
				DecayChance.base(0.4F)
					.addModifier(Operation.ADD_MULTIPLIED_TOTAL, BlockSaturationCalculator.Builder.create().min(0.1F).power(0.2F).searchRange(4).blockCap(9).blocks(RCBlocks.CRACKED_STONE_BRICK_STAIRS.get()).build()))
				.name("cracked_stone_brick_stairs_to_cracked_stone_brick_slab")
				.condition(ConditionIsBlock.of(RCBlocks.CRACKED_STONE_BRICK_STAIRS.get()))
				.function(
					FunctionConvert.toBlock(RCBlocks.CRACKED_STONE_BRICK_SLAB.get()),
					FunctionBlockState.RandomValue.of(Properties.SLAB_TYPE)).build());
		register(DecayEntry.Builder.create(
				DecayChance.base(0.4F)
					.addModifier(Operation.ADD_MULTIPLIED_TOTAL, BlockSaturationCalculator.Builder.create().min(0.1F).power(0.2F).blockCap(9).blocks(RCBlocks.CRACKED_STONE_BRICK_SLAB.get()).build()))
				.name("cracked_stone_brick_slab_vanish")
				.condition(
					ConditionIsBlock.of(RCBlocks.CRACKED_STONE_BRICK_SLAB.get()))
				.function(
					FunctionConvert.toBlockState(Blocks.MOSSY_STONE_BRICK_SLAB.getDefaultState().with(Properties.SLAB_TYPE, SlabType.BOTTOM)),
					RCDecayFunctions.FALL.get(),
					RCDecayFunctions.TO_AIR.get()).build());
	}
	
	private static void registerIronRust()
	{
		register(DecayEntry.Builder.create(
				DecayChance.base(0.0025F)
					.addModifier(0.4F, Operation.ADD_VALUE, BlockSaturationCalculator.Builder.create().mode(Mode.FLAT_VALUE).searchRange(2).blockCap(3).tag(RCBlockTags.RUST).build()))
				.name("iron_block_start_rusting")
				.condition(
					ConditionBoolean.Or.of(
						ConditionBoolean.And.of(
							RCDecayConditions.EXPOSED.get(),
							RCDecayConditions.IN_RAIN.get()),
						ConditionNeighbouring.Blocks.of(List.of(RCBlockTags.RUST))),
					ConditionIsBlock.of(Blocks.IRON_BLOCK))
				.function(FunctionConvert.toBlock(RCBlocks.EXPOSED_IRON.get())).build());
		register(DecayEntry.Builder.create(
				DecayChance.base(0F)
					.addModifier(0.3F, Operation.ADD_VALUE, BlockSaturationCalculator.Builder.create().mode(Mode.FLAT_VALUE).searchRange(1).tag(RCBlockTags.RUST).build()))
				.name("exposed_iron_block_to_weathered_iron_block")
				.condition(
					ConditionIsBlock.of(RCBlocks.EXPOSED_IRON.get()))
				.function(FunctionConvert.toBlock(RCBlocks.WEATHERED_IRON.get())).build());
		register(DecayEntry.Builder.create(
				DecayChance.base(0F)
					.addModifier(0.3F, Operation.ADD_VALUE, BlockSaturationCalculator.Builder.create().mode(Mode.FLAT_VALUE).searchRange(1).blocks(RCBlocks.WEATHERED_IRON.get(), RCBlocks.RUSTED_IRON.get()).build()))
				.name("weathered_iron_block_to_rusted_iron_block")
				.condition(
					ConditionIsBlock.of(RCBlocks.WEATHERED_IRON.get()))
				.function(FunctionConvert.toBlock(RCBlocks.RUSTED_IRON.get())).build());
	}
	
	private static void registerTorchLanternCampfire()
	{
		register(DecayEntry.Builder.create(
				DecayChance.base(0.3F))
				.name("torch_burnout")
				.condition(ConditionIsBlock.of(Blocks.TORCH, Blocks.WALL_TORCH))
				.function(
					FunctionConvert.toBlock(RCBlocks.DOUSED_TORCH.get()),
					FunctionBlockState.CopyValue.of(Properties.FACING)).build());
		
		register(DecayEntry.Builder.create(
				DecayChance.base(0.3F))
				.name("soul_torch_burnout")
				.condition(ConditionIsBlock.of(Blocks.SOUL_TORCH, Blocks.SOUL_WALL_TORCH))
				.function(
					FunctionConvert.toBlock(RCBlocks.DOUSED_SOUL_TORCH.get()),
					FunctionBlockState.CopyValue.of(Properties.FACING)).build());
		
		register(DecayEntry.Builder.create(
				DecayChance.base(0.15F))
				.name("lantern_burnout")
				.condition(ConditionIsBlock.of(Blocks.LANTERN))
				.function(
					FunctionConvert.toBlock(RCBlocks.DOUSED_LANTERN.get()),
					FunctionBlockState.CopyValue.of(LanternBlock.HANGING)).build());
		
		register(DecayEntry.Builder.create(
				DecayChance.base(0.15F))
				.name("soul_lantern_burnout")
				.condition(ConditionIsBlock.of(Blocks.SOUL_LANTERN))
				.function(
					FunctionConvert.toBlock(RCBlocks.DOUSED_SOUL_LANTERN.get()),
					FunctionBlockState.CopyValue.of(LanternBlock.HANGING)).build());
		
		register(DecayEntry.Builder.create(
				DecayChance.base(0.2F))
				.name("campfire_burnout")
				.condition(ConditionIsBlock.of(Blocks.CAMPFIRE, Blocks.SOUL_CAMPFIRE))
				.function(
					FunctionBlockState.SetValue.of(PropertyMap.create().add(CampfireBlock.LIT, false))).build());
	}
	
	private static void registerSandWeathering()
	{
		register(DecayEntry.Builder.create(
				DecayChance.base(0.002F)
					.addModifier(0.001F, Operation.ADD_VALUE, BlockSaturationCalculator.Builder.create().mode(Mode.FLAT_VALUE).searchRange(2).blocks(Blocks.SANDSTONE, Blocks.SAND, Blocks.RED_SAND).build()))
				.name("sandstone_weathering")
				.condition(
					ConditionIsBlock.of(Blocks.CHISELED_SANDSTONE, Blocks.CUT_SANDSTONE),
					RCDecayConditions.EXPOSED.get())
				.function(FunctionConvert.toBlock(Blocks.SANDSTONE)).build());
		
		register(DecayEntry.Builder.create(
				DecayChance.base(0.002F))
				.name("sandstone_crumbling")
				.condition(
					ConditionIsBlock.of(Blocks.SANDSTONE),
					ConditionNeighbouring.Exposed.face(Direction.values()).threshold(3))
				.function(FunctionConvert.toBlock(Blocks.SAND)).build());
		
		register(DecayEntry.Builder.create(
				DecayChance.base(0.002F)
					.addModifier(0.001F, Operation.ADD_VALUE, BlockSaturationCalculator.Builder.create().mode(Mode.FLAT_VALUE).searchRange(2).blocks(Blocks.RED_SANDSTONE, Blocks.SAND, Blocks.RED_SAND).build()))
				.name("red_sandstone_weathering")
				.condition(
					ConditionIsBlock.of(Blocks.CHISELED_RED_SANDSTONE, Blocks.CUT_RED_SANDSTONE),
					RCDecayConditions.EXPOSED.get())
				.function(FunctionConvert.toBlock(Blocks.RED_SANDSTONE)).build());
		
		register(DecayEntry.Builder.create(
				DecayChance.base(0.002F))
				.name("red_sandstone_crumbling")
				.condition(
					ConditionIsBlock.of(Blocks.RED_SANDSTONE),
					ConditionNeighbouring.Exposed.face(Direction.values()).threshold(3))
				.function(FunctionConvert.toBlock(Blocks.RED_SAND)).build());
	}
	
	private static void registerTerracotta()
	{
		register(DecayEntry.Builder.create(
				DecayChance.base(0.03F)
					.addModifier(0.03F, Operation.ADD_VALUE, BlockSaturationCalculator.Builder.create().searchRange(1).blockCap(8).tags(List.of(RCBlockTags.FADED_TERRACOTTA, BlockTags.TERRACOTTA)).build()))
				.name("glazed_terracotta_to_faded_terracotta")
				.condition(
					RCDecayConditions.EXPOSED.get(),
					ConditionIsBlock.of(TagKey.of(RegistryKeys.BLOCK, Identifier.of("c","glazed_terracotta"))))
				.function(FunctionMacro.of(DefaultDecayMacros.FADE_TERRACOTTA)).build());
			
		register(DecayEntry.Builder.create(
				DecayChance.base(0F)
					.addModifier(0.01F, Operation.ADD_VALUE, BlockSaturationCalculator.Builder.create().mode(Mode.FLAT_VALUE).searchRange(2).blockCap(10).tag(RCBlockTags.FADED_TERRACOTTA).build()))
				.name("faded_terracotta_to_blank_terracotta")
				.condition(
					RCDecayConditions.EXPOSED.get(),
					ConditionIsBlock.of(RCBlockTags.FADED_TERRACOTTA))
				.function(FunctionMacro.of(DefaultDecayMacros.BLANK_TERRACOTTA)).build());
	}
	
	static
	{
		registerStoneBrick();
		registerIronRust();
		registerTorchLanternCampfire();
		registerSandWeathering();
		registerTerracotta();
		
		register(DecayEntry.Builder.create(DecayChance.base(0.3F))
				.name("rain_interaction_with_waterlogging")
				.condition(
					RCDecayConditions.SKY_ABOVE.get(),
					ConditionBoolean.Or.of(
						ConditionBoolean.And.of(
							ConditionHasProperty.of(Map.of(Properties.WATERLOGGED, false)),
							RCDecayConditions.IN_RAIN.get()).named("dry_block_in_rain"),
						ConditionBoolean.And.of(
							ConditionHasProperty.of(Map.of(Properties.WATERLOGGED, true)),
							RCDecayConditions.IN_RAIN.get().invert()).named("wet_block_in_sun")))
				.function(
					FunctionBlockState.CycleValue.of(Properties.WATERLOGGED)).build());
		
		register(DecayEntry.Builder.create()
				.name("unsupported_blocks_fall")
				.condition(
					RCDecayConditions.IS_SOLID.get(),
					ConditionNeighbouring.Supported.create().threshold(2).invert(),
					ConditionPosition.Altitude.of(5))
				.function(RCDecayFunctions.FALL.get()).build());
		
		register(DecayEntry.Builder.create(
				DecayChance.base(0.2F))
				.name("grass_sprout")
				.condition(
					ConditionIsBlock.of(Blocks.GRASS_BLOCK, Blocks.PODZOL, Blocks.MYCELIUM),
					RCDecayConditions.AIR_ABOVE.get(),
					RCDecayConditions.SKY_ABOVE.get())
				.function(
					FunctionSprout.Builder.create()
						.soloProvider(DefaultDecayMacros.PLACE_FLOWERS)
						.faceSet(EnumSet.of(Direction.UP))
						.maxPlace(1).build()).build());
		
		register(DecayEntry.Builder.create(
				DecayChance.base(0F)
					.addModifier(0.3F, Operation.ADD_VALUE, BlockSaturationCalculator.Builder.create().blockCap(1).searchRange(1).blocks(Blocks.GRASS_BLOCK).build()))
				.name("ivy_sprout")
				.condition(
					RCDecayConditions.IS_SOLID.get(),
					RCDecayConditions.ON_GROUND.get(),
					ConditionNeighbouring.Exposed.face(Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST))
				.function(
					FunctionSprout.Builder.create()
						.faceSet(EnumSet.of(Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST))
						.soloProvider(DefaultDecayMacros.PLACE_IVY)
						.onCondition(
							ConditionBoolean.And.of(
								RCDecayConditions.IS_AIR.get(),
								ConditionNeighbouring.Blocks.of(Blocks.GRASS_BLOCK).faces(Direction.DOWN))).build()).build());
		
		register(DecayEntry.Builder.create()
				.name("particulate_shuffle")
				.condition(
					ConditionIsBlock.of(Blocks.GRAVEL, Blocks.SAND, Blocks.RED_SAND),
					RCDecayConditions.ON_GROUND.get(),
					RCDecayConditions.AIR_ABOVE.get())
				.function(
					RCDecayFunctions.SHUFFLE.get()).build());
		
		register(DecayEntry.Builder.create(
				DecayChance.base(0.000025F)
					.addModifier(0.15F, Operation.ADD_VALUE, BlockSaturationCalculator.Builder.create().mode(Mode.FLAT_VALUE).blockCap(6).searchRange(1).blocks(RCBlocks.TARNISHED_GOLD.get()).build()))
				.name("gold_to_tarnished_gold")
				.condition(
					RCDecayConditions.EXPOSED.get(),
					ConditionIsBlock.of(Blocks.GOLD_BLOCK))
				.function(FunctionConvert.toBlock(RCBlocks.TARNISHED_GOLD.get())).build());
	}
}
