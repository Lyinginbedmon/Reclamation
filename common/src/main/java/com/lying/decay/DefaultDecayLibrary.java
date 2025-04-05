package com.lying.decay;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.lying.decay.conditions.ConditionHasProperty;
import com.lying.decay.conditions.ConditionIsBlock;
import com.lying.decay.conditions.ConditionNeighbouring;
import com.lying.decay.conditions.ConditionBoolean;
import com.lying.decay.functions.FunctionBlockState;
import com.lying.decay.functions.FunctionConvert;
import com.lying.init.RCBlocks;
import com.lying.init.RCDecayConditions;
import com.lying.init.RCDecayFunctions;

import net.minecraft.block.Blocks;
import net.minecraft.block.enums.SlabType;
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
		register(DecayData.Builder.create(0.3F)
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
		
		register(DecayData.Builder.create(0.2F)
				.name("stone_brick_to_cracked_stone_brick")
				.catalysts(Catalysers.Builder.create().blockCap(9).blocks(Blocks.CRACKED_STONE_BRICKS).build())
				.condition(ConditionIsBlock.of(Blocks.STONE_BRICKS))
				.function(FunctionConvert.to(Blocks.CRACKED_STONE_BRICKS)).build());
		register(DecayData.Builder.create(0.1F)
				.name("stone_brick_to_mossy_stone_brick")
				.catalysts(Catalysers.Builder.create().blockCap(9).blocks(Blocks.MOSSY_STONE_BRICKS).build())
				.condition(ConditionIsBlock.of(Blocks.STONE_BRICKS))
				.function(FunctionConvert.to(Blocks.MOSSY_STONE_BRICKS)).build());
		
		register(DecayData.Builder.create(0.15F)
				.name("mossy_stone_brick_to_mossy_stone_brick_stairs")
				.catalysts(Catalysers.Builder.create().blockCap(9).blocks(Blocks.MOSSY_STONE_BRICK_STAIRS).build())
				.condition(ConditionIsBlock.of(Blocks.MOSSY_STONE_BRICKS))
				.function(
					FunctionConvert.to(
					Blocks.MOSSY_STONE_BRICK_STAIRS.getDefaultState().with(Properties.HORIZONTAL_FACING, Direction.NORTH),
					Blocks.MOSSY_STONE_BRICK_STAIRS.getDefaultState().with(Properties.HORIZONTAL_FACING, Direction.EAST),
					Blocks.MOSSY_STONE_BRICK_STAIRS.getDefaultState().with(Properties.HORIZONTAL_FACING, Direction.SOUTH),
					Blocks.MOSSY_STONE_BRICK_STAIRS.getDefaultState().with(Properties.HORIZONTAL_FACING, Direction.WEST))).build());
		register(DecayData.Builder.create(0.15F)
				.name("mossy_stone_brick_stairs_to_mossy_stone_brick_slab")
				.catalysts(Catalysers.Builder.create().blockCap(9).blocks(Blocks.MOSSY_STONE_BRICK_STAIRS).build())
				.condition(ConditionIsBlock.of(Blocks.MOSSY_STONE_BRICK_STAIRS))
				.function(FunctionConvert.to(
					Blocks.MOSSY_STONE_BRICK_SLAB.getDefaultState().with(Properties.SLAB_TYPE, SlabType.BOTTOM),
					Blocks.MOSSY_STONE_BRICK_SLAB.getDefaultState().with(Properties.SLAB_TYPE, SlabType.TOP))).build());
		register(DecayData.Builder.create(0.15F)
				.name("mossy_stone_brick_slab_vanish")
				.catalysts(Catalysers.Builder.create().blockCap(9).blocks(Blocks.MOSSY_STONE_BRICK_SLAB).build())
				.condition(ConditionIsBlock.of(Blocks.MOSSY_STONE_BRICK_SLAB))
				.function(
					FunctionConvert.to(Blocks.MOSSY_STONE_BRICK_SLAB.getDefaultState().with(Properties.SLAB_TYPE, SlabType.BOTTOM)),
					RCDecayFunctions.FALL.get(),
					FunctionConvert.to(Blocks.AIR)).build());
		
		register(DecayData.Builder.create(0.4F)
				.name("cracked_stone_brick_to_cracked_stone_brick_stairs")
				.catalysts(Catalysers.Builder.create().blockCap(9).blocks(RCBlocks.CRACKED_STONE_BRICK_STAIRS.get()).build())
				.condition(ConditionIsBlock.of(Blocks.CRACKED_STONE_BRICKS))
				.function(
					FunctionConvert.to(
						RCBlocks.CRACKED_STONE_BRICK_STAIRS.get().getDefaultState().with(Properties.HORIZONTAL_FACING, Direction.NORTH),
						RCBlocks.CRACKED_STONE_BRICK_STAIRS.get().getDefaultState().with(Properties.HORIZONTAL_FACING, Direction.EAST),
						RCBlocks.CRACKED_STONE_BRICK_STAIRS.get().getDefaultState().with(Properties.HORIZONTAL_FACING, Direction.SOUTH),
						RCBlocks.CRACKED_STONE_BRICK_STAIRS.get().getDefaultState().with(Properties.HORIZONTAL_FACING, Direction.WEST))).build());
		register(DecayData.Builder.create(0.4F)
				.name("cracked_stone_brick_stairs_to_cracked_stone_brick_slab")
				.catalysts(Catalysers.Builder.create().blockCap(9).blocks(RCBlocks.CRACKED_STONE_BRICK_STAIRS.get()).build())
				.condition(ConditionIsBlock.of(RCBlocks.CRACKED_STONE_BRICK_STAIRS.get()))
				.function(FunctionConvert.to(
					RCBlocks.CRACKED_STONE_BRICK_SLAB.get().getDefaultState().with(Properties.SLAB_TYPE, SlabType.BOTTOM),
					RCBlocks.CRACKED_STONE_BRICK_SLAB.get().getDefaultState().with(Properties.SLAB_TYPE, SlabType.TOP))).build());
		register(DecayData.Builder.create(0.4F)
				.name("cracked_stone_brick_slab_vanish")
				.catalysts(Catalysers.Builder.create().blockCap(9).blocks(RCBlocks.CRACKED_STONE_BRICK_SLAB.get()).build())
				.condition(
					ConditionIsBlock.of(RCBlocks.CRACKED_STONE_BRICK_SLAB.get()))
				.function(
					FunctionConvert.to(Blocks.MOSSY_STONE_BRICK_SLAB.getDefaultState().with(Properties.SLAB_TYPE, SlabType.BOTTOM)),
					RCDecayFunctions.FALL.get(),
					FunctionConvert.to(Blocks.AIR)).build());
		
		register(DecayData.Builder.create(0.5F)
				.name("grass_get_fluffy")
				.condition(
					ConditionIsBlock.of(Blocks.GRASS_BLOCK), 
					RCDecayConditions.SKY_ABOVE.get())
				.function(RCDecayFunctions.BONEMEAL.get()).build());
		register(DecayData.Builder.create()
				.name("gravel_shuffle")
				.condition(
					ConditionIsBlock.of(Blocks.GRAVEL),
					RCDecayConditions.ON_GROUND.get(),
					RCDecayConditions.AIR_ABOVE.get())
				.function(
					RCDecayFunctions.SHUFFLE.get()).build());
		
		register(DecayData.Builder.create(0.1F)
				.name("iron_block_to_exposed_iron_block")
				.catalysts(Catalysers.Builder.create().blockCap(9).searchRange(1).blocks(RCBlocks.EXPOSED_IRON.get(), Blocks.WATER).build())
				.condition(
					RCDecayConditions.EXPOSED.get(),
					ConditionIsBlock.of(Blocks.IRON_BLOCK))
				.function(FunctionConvert.to(RCBlocks.EXPOSED_IRON.get())).build());
		register(DecayData.Builder.create(0.1F)
				.name("exposed_iron_block_to_weathered_iron_block")
				.catalysts(Catalysers.Builder.create().blockCap(9).searchRange(1).blocks(RCBlocks.WEATHERED_IRON.get()).build())
				.condition(ConditionIsBlock.of(RCBlocks.EXPOSED_IRON.get()))
				.function(FunctionConvert.to(RCBlocks.WEATHERED_IRON.get())).build());
		register(DecayData.Builder.create(0.1F)
				.name("weathered_iron_block_to_rusted_iron_block")
				.catalysts(Catalysers.Builder.create().blockCap(9).searchRange(1).blocks(RCBlocks.RUSTED_IRON.get()).build())
				.condition(ConditionIsBlock.of(RCBlocks.WEATHERED_IRON.get()))
				.function(FunctionConvert.to(RCBlocks.RUSTED_IRON.get())).build());
		
		register(DecayData.Builder.create(0.05F)
				.name("gold_to_tarnished_gold")
				.catalysts(Catalysers.Builder.create().blockCap(4).searchRange(1).blocks(RCBlocks.TARNISHED_GOLD.get()).build())
				.condition(
					RCDecayConditions.EXPOSED.get(),
					ConditionIsBlock.of(Blocks.GOLD_BLOCK))
				.function(FunctionConvert.to(RCBlocks.TARNISHED_GOLD.get())).build());
	}
}
