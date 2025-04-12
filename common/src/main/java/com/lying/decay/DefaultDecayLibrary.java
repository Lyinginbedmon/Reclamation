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
import com.lying.decay.functions.FunctionBlockState;
import com.lying.decay.functions.FunctionConvert;
import com.lying.decay.functions.FunctionSprout;
import com.lying.init.RCBlocks;
import com.lying.init.RCDecayConditions;
import com.lying.init.RCDecayFunctions;
import com.lying.utility.BlockSaturationCalculator;
import com.lying.utility.BlockProvider;
import com.lying.utility.BlockSaturationCalculator.Mode;

import net.minecraft.block.Blocks;
import net.minecraft.block.enums.SlabType;
import net.minecraft.entity.attribute.EntityAttributeModifier.Operation;
import net.minecraft.state.property.Properties;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Direction;

public class DefaultDecayLibrary 
{
	private static final Map<Identifier, DecayData> DATA = new HashMap<>();
	
	private static void register(DecayData dataIn)
	{
		DATA.put(dataIn.packName(), dataIn);
	}
	
	public static Collection<DecayData> getDefaults() { return DATA.values(); }
	
	static
	{
		register(DecayData.Builder.create(DecayChance.base(0.3F))
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
		register(DecayData.Builder.create()
				.name("unsupported_blocks_fall")
				.condition(
					((ConditionNeighbouring)RCDecayConditions.SUPPORTED.get()).threshold(2).invert(),
					ConditionBoolean.Or.of(
						ConditionIsBlock.of(
							Blocks.STONE_BRICKS,
							Blocks.BRICKS,
							Blocks.CRACKED_STONE_BRICKS,
							Blocks.MOSSY_STONE_BRICKS).named("brick_blocks"),
						ConditionIsBlock.of(
							Blocks.STONE_BRICK_STAIRS,
							Blocks.BRICK_STAIRS,
							RCBlocks.CRACKED_STONE_BRICK_STAIRS.get()).named("stairs_blocks"),
						ConditionIsBlock.of(
							Blocks.BRICK_SLAB,
							Blocks.STONE_BRICK_SLAB,
							Blocks.MOSSY_STONE_BRICK_SLAB,
							RCBlocks.CRACKED_STONE_BRICK_SLAB.get()).named("slabs")))
				.function(RCDecayFunctions.FALL.get()).build());
		
		register(DecayData.Builder.create(
				DecayChance.base(0.2F)
					.addModifier(Operation.ADD_MULTIPLIED_TOTAL, BlockSaturationCalculator.Builder.create().min(0.1F).power(0.2F).searchRange(4).blockCap(9).blocks(Blocks.CRACKED_STONE_BRICKS).build()))
				.name("stone_brick_to_cracked_stone_brick")
				.condition(ConditionIsBlock.of(Blocks.STONE_BRICKS))
				.function(FunctionConvert.to(Blocks.CRACKED_STONE_BRICKS)).build());
		register(DecayData.Builder.create(
				DecayChance.base(0.1F)
					.addModifier(Operation.ADD_MULTIPLIED_TOTAL, BlockSaturationCalculator.Builder.create().min(0.1F).power(0.2F).searchRange(4).blockCap(9).blocks(Blocks.MOSSY_STONE_BRICKS).build()))
				.name("stone_brick_to_mossy_stone_brick")
				.condition(ConditionIsBlock.of(Blocks.STONE_BRICKS))
				.function(FunctionConvert.to(Blocks.MOSSY_STONE_BRICKS)).build());
		
		register(DecayData.Builder.create(
				DecayChance.base(0.15F)
					.addModifier(Operation.ADD_MULTIPLIED_TOTAL, BlockSaturationCalculator.Builder.create().min(0.1F).power(0.2F).searchRange(4).blockCap(9).blocks(Blocks.MOSSY_STONE_BRICK_STAIRS).build()))
				.name("mossy_stone_brick_to_mossy_stone_brick_stairs")
				.condition(ConditionIsBlock.of(Blocks.MOSSY_STONE_BRICKS))
				.function(
					FunctionConvert.to(Blocks.MOSSY_STONE_BRICK_STAIRS),
					FunctionBlockState.RandomValue.of(Properties.HORIZONTAL_FACING)).build());
		register(DecayData.Builder.create(
				DecayChance.base(0.15F)
					.addModifier(Operation.ADD_MULTIPLIED_TOTAL, BlockSaturationCalculator.Builder.create().min(0.1F).power(0.2F).searchRange(4).blockCap(9).blocks(Blocks.MOSSY_STONE_BRICK_STAIRS).build()))
				.name("mossy_stone_brick_stairs_to_mossy_stone_brick_slab")
				.condition(ConditionIsBlock.of(Blocks.MOSSY_STONE_BRICK_STAIRS))
				.function(
					FunctionConvert.to(Blocks.MOSSY_STONE_BRICK_SLAB),
					FunctionBlockState.RandomValue.of(Properties.SLAB_TYPE)).build());
		register(DecayData.Builder.create(
				DecayChance.base(0.15F)
					.addModifier(Operation.ADD_MULTIPLIED_TOTAL, BlockSaturationCalculator.Builder.create().min(0.1F).power(0.2F).searchRange(4).blockCap(9).blocks(Blocks.MOSSY_STONE_BRICK_SLAB).build()))
				.name("mossy_stone_brick_slab_vanish")
				.condition(ConditionIsBlock.of(Blocks.MOSSY_STONE_BRICK_SLAB))
				.function(
					FunctionConvert.to(Blocks.MOSSY_STONE_BRICK_SLAB.getDefaultState().with(Properties.SLAB_TYPE, SlabType.BOTTOM)),
					RCDecayFunctions.FALL.get(),
					RCDecayFunctions.TO_AIR.get()).build());
		
		register(DecayData.Builder.create(
				DecayChance.base(0.4F)
					.addModifier(Operation.ADD_MULTIPLIED_TOTAL, BlockSaturationCalculator.Builder.create().min(0.1F).power(0.2F).searchRange(4).blockCap(9).blocks(RCBlocks.CRACKED_STONE_BRICK_STAIRS.get()).build()))
				.name("cracked_stone_brick_to_cracked_stone_brick_stairs")
				.condition(ConditionIsBlock.of(Blocks.CRACKED_STONE_BRICKS))
				.function(
					FunctionConvert.to(RCBlocks.CRACKED_STONE_BRICK_STAIRS.get()),
					FunctionBlockState.RandomValue.of(Properties.HORIZONTAL_FACING)).build());
		register(DecayData.Builder.create(
				DecayChance.base(0.4F)
					.addModifier(Operation.ADD_MULTIPLIED_TOTAL, BlockSaturationCalculator.Builder.create().min(0.1F).power(0.2F).searchRange(4).blockCap(9).blocks(RCBlocks.CRACKED_STONE_BRICK_STAIRS.get()).build()))
				.name("cracked_stone_brick_stairs_to_cracked_stone_brick_slab")
				.condition(ConditionIsBlock.of(RCBlocks.CRACKED_STONE_BRICK_STAIRS.get()))
				.function(
					FunctionConvert.to(RCBlocks.CRACKED_STONE_BRICK_SLAB.get()),
					FunctionBlockState.RandomValue.of(Properties.SLAB_TYPE)).build());
		register(DecayData.Builder.create(
				DecayChance.base(0.4F)
					.addModifier(Operation.ADD_MULTIPLIED_TOTAL, BlockSaturationCalculator.Builder.create().min(0.1F).power(0.2F).blockCap(9).blocks(RCBlocks.CRACKED_STONE_BRICK_SLAB.get()).build()))
				.name("cracked_stone_brick_slab_vanish")
				.condition(
					ConditionIsBlock.of(RCBlocks.CRACKED_STONE_BRICK_SLAB.get()))
				.function(
					FunctionConvert.to(Blocks.MOSSY_STONE_BRICK_SLAB.getDefaultState().with(Properties.SLAB_TYPE, SlabType.BOTTOM)),
					RCDecayFunctions.FALL.get(),
					RCDecayFunctions.TO_AIR.get()).build());
		
		register(DecayData.Builder.create(
				DecayChance.base(0.2F))
				.name("grass_sprout")
				.condition(
					ConditionIsBlock.of(Blocks.GRASS_BLOCK, Blocks.PODZOL),
					RCDecayConditions.AIR_ABOVE.get(),
					RCDecayConditions.SKY_ABOVE.get())
				.function(
					FunctionSprout.of(
						BlockProvider.create().addBlock(
							Blocks.POPPY, Blocks.DANDELION, 
							Blocks.SHORT_GRASS, Blocks.TALL_GRASS, 
							Blocks.OAK_SAPLING, Blocks.SPRUCE_SAPLING, Blocks.BIRCH_SAPLING, 
							Blocks.BROWN_MUSHROOM, Blocks.RED_MUSHROOM), 
						EnumSet.of(Direction.UP)).count(1)).build());
		register(DecayData.Builder.create()
				.name("gravel_shuffle")
				.condition(
					ConditionIsBlock.of(Blocks.GRAVEL),
					RCDecayConditions.ON_GROUND.get(),
					RCDecayConditions.AIR_ABOVE.get())
				.function(
					RCDecayFunctions.SHUFFLE.get()).build());
		
		register(DecayData.Builder.create(
				DecayChance.base(0.0025F)
					.addModifier(0.4F, Operation.ADD_VALUE, BlockSaturationCalculator.Builder.create().mode(Mode.FLAT_VALUE).searchRange(2).blockCap(3).tags(RCBlockTags.RUST).build()))
				.name("iron_block_start_rusting")
				.condition(
					ConditionBoolean.Or.of(
						RCDecayConditions.EXPOSED.get(),
						ConditionNeighbouring.Blocks.of(List.of(RCBlockTags.RUST))),
					ConditionIsBlock.of(Blocks.IRON_BLOCK))
				.function(FunctionConvert.to(RCBlocks.EXPOSED_IRON.get())).build());
		register(DecayData.Builder.create(
				DecayChance.base(0F)
					.addModifier(0.3F, Operation.ADD_VALUE, BlockSaturationCalculator.Builder.create().mode(Mode.FLAT_VALUE).searchRange(1).tags(RCBlockTags.RUST).build()))
				.name("exposed_iron_block_to_weathered_iron_block")
				.condition(
					ConditionIsBlock.of(RCBlocks.EXPOSED_IRON.get()))
				.function(FunctionConvert.to(RCBlocks.WEATHERED_IRON.get())).build());
		register(DecayData.Builder.create(
				DecayChance.base(0F)
					.addModifier(0.3F, Operation.ADD_VALUE, BlockSaturationCalculator.Builder.create().mode(Mode.FLAT_VALUE).searchRange(1).blocks(RCBlocks.WEATHERED_IRON.get(), RCBlocks.RUSTED_IRON.get()).build()))
				.name("weathered_iron_block_to_rusted_iron_block")
				.condition(
					ConditionIsBlock.of(RCBlocks.WEATHERED_IRON.get()))
				.function(FunctionConvert.to(RCBlocks.RUSTED_IRON.get())).build());
		
		register(DecayData.Builder.create(
				DecayChance.base(0.000025F)
					.addModifier(0.15F, Operation.ADD_VALUE, BlockSaturationCalculator.Builder.create().mode(Mode.FLAT_VALUE).blockCap(6).searchRange(1).blocks(RCBlocks.TARNISHED_GOLD.get()).build()))
				.name("gold_to_tarnished_gold")
				.condition(
					RCDecayConditions.EXPOSED.get(),
					ConditionIsBlock.of(Blocks.GOLD_BLOCK))
				.function(FunctionConvert.to(RCBlocks.TARNISHED_GOLD.get())).build());
		
	}
}
