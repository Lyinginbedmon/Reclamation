package com.lying.fabric.data;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

import com.lying.block.IvyBlock;
import com.lying.block.LeafPileBlock;
import com.lying.block.SootBlock;
import com.lying.init.RCBlocks;
import com.lying.init.RCBlocks.Terracotta;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricBlockLootTableProvider;
import net.minecraft.block.Block;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.loot.LootPool;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.condition.BlockStatePropertyLootCondition;
import net.minecraft.loot.entry.ItemEntry;
import net.minecraft.loot.entry.LootPoolEntry;
import net.minecraft.loot.function.ApplyBonusLootFunction;
import net.minecraft.loot.function.SetCountLootFunction;
import net.minecraft.loot.provider.number.ConstantLootNumberProvider;
import net.minecraft.loot.provider.number.UniformLootNumberProvider;
import net.minecraft.predicate.StatePredicate;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.RegistryWrapper.WrapperLookup;
import net.minecraft.state.property.BooleanProperty;

public class RCBlockLootTableProvider extends FabricBlockLootTableProvider
{
	private final RegistryWrapper.Impl<Enchantment> enchantments = this.registries.getOrThrow(RegistryKeys.ENCHANTMENT);
	private static final List<Supplier<Block>> DROP_SELF = List.of(
			RCBlocks.WAXED_EXPOSED_IRON,
			RCBlocks.WAXED_GOLD_BLOCK,
			RCBlocks.WAXED_IRON_BLOCK,
			RCBlocks.WAXED_RUSTED_IRON,
			RCBlocks.WAXED_TARNISHED_GOLD,
			RCBlocks.TARNISHED_GOLD,
			RCBlocks.CRACKED_STONE_BRICK_SLAB,
			RCBlocks.CRACKED_STONE_BRICK_STAIRS,
			RCBlocks.DOUSED_LANTERN,
			RCBlocks.DOUSED_SOUL_LANTERN,
			RCBlocks.DOUSED_SOUL_TORCH,
			RCBlocks.DOUSED_TORCH
			);
	
	public RCBlockLootTableProvider(FabricDataOutput dataOutput, CompletableFuture<WrapperLookup> registryLookup)
	{
		super(dataOutput, registryLookup);
	}
	
	public void generate()
	{
		DROP_SELF.stream().map(Supplier::get).forEach(this::addDrop);
		RCBlocks.DYE_TO_TERRACOTTA.values().stream().map(Terracotta::faded).map(Supplier::get).forEach(this::addDrop);
		LeafPileBlock.LEAF_PILES.forEach(l -> addLeafPileDrops(l));
		
		addRustDrops(RCBlocks.EXPOSED_IRON.get(), Items.IRON_INGOT, 4, 7);
		addRustDrops(RCBlocks.WEATHERED_IRON.get(), Items.IRON_INGOT, 2, 5);
		addRustDrops(RCBlocks.RUSTED_IRON.get(), Items.IRON_INGOT, 0, 3);
		addIvyDrops(RCBlocks.IVY.get());
		addSootDrops(RCBlocks.SOOT.get());
	}
	
	private void addRustDrops(Block silk, Item alt, int min, int max)
	{
		this.addDrop(
				silk,
				block -> this.dropsWithSilkTouch(
						block,
						(LootPoolEntry.Builder<?>)this.applyExplosionDecay(
							block,
							ItemEntry.builder(alt)
								.apply(SetCountLootFunction.builder(UniformLootNumberProvider.create(min, max)))
								.apply(ApplyBonusLootFunction.oreDrops(enchantments.getOrThrow(Enchantments.FORTUNE)))
						)
					)
			);
	}
	
	private void addLeafPileDrops(Block block)
	{
		addDrop(block, LootTable.builder().pool(LootPool.builder().conditionally(this.createWithShearsCondition()).rolls(ConstantLootNumberProvider.create(1.0F)).with(ItemEntry.builder(block))));
	}
	
	private void addIvyDrops(Block block)
	{
		addDrop(
				block, LootTable.builder()
					.pool(boolConditionalPool(block, IvyBlock.EAST, true).conditionally(createWithShearsCondition()))
					.pool(boolConditionalPool(block, IvyBlock.NORTH, true).conditionally(createWithShearsCondition()))
					.pool(boolConditionalPool(block, IvyBlock.SOUTH, true).conditionally(createWithShearsCondition()))
					.pool(boolConditionalPool(block, IvyBlock.WEST, true).conditionally(createWithShearsCondition()))
					.pool(boolConditionalPool(block, IvyBlock.UP, true).conditionally(createWithShearsCondition()))
				);
	}
	
	private void addSootDrops(Block block)
	{
		addDrop(
				block, LootTable.builder()
					.pool(boolConditionalPool(block, SootBlock.EAST, true).conditionally(createSilkTouchCondition()))
					.pool(boolConditionalPool(block, SootBlock.NORTH, true).conditionally(createSilkTouchCondition()))
					.pool(boolConditionalPool(block, SootBlock.SOUTH, true).conditionally(createSilkTouchCondition()))
					.pool(boolConditionalPool(block, SootBlock.WEST, true).conditionally(createSilkTouchCondition()))
					.pool(boolConditionalPool(block, SootBlock.UP, true).conditionally(createSilkTouchCondition()))
					.pool(boolConditionalPool(block, SootBlock.DOWN, true).conditionally(createSilkTouchCondition()))
				);
	}
	
	private LootPool.Builder boolConditionalPool(Block drop, BooleanProperty north, boolean b)
	{
		return addSurvivesExplosionCondition(
				drop,
				LootPool.builder()
					.rolls(ConstantLootNumberProvider.create(1.0F))
					.with(
						ItemEntry.builder(drop)
							.conditionally(BlockStatePropertyLootCondition.builder(drop).properties(StatePredicate.Builder.create().exactMatch(north, b)))
					)
			);
	}
}
