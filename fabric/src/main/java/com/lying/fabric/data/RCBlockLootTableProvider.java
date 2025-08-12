package com.lying.fabric.data;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

import com.lying.block.CrackedConcreteBlock;
import com.lying.block.IvyBlock;
import com.lying.block.LeafPileBlock;
import com.lying.block.RubbleBlock;
import com.lying.block.SootBlock;
import com.lying.init.RCBlocks;
import com.lying.init.RCItems;
import com.lying.init.RCBlocks.Terracotta;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricBlockLootTableProvider;
import net.minecraft.block.Block;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.item.Item;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.Items;
import net.minecraft.loot.LootPool;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.condition.BlockStatePropertyLootCondition;
import net.minecraft.loot.condition.EntityPropertiesLootCondition;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.entry.AlternativeEntry;
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
import net.minecraft.state.property.Property;

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
			RCBlocks.DOUSED_TORCH,
			RCBlocks.MOLD
			);
	
	public RCBlockLootTableProvider(FabricDataOutput dataOutput, CompletableFuture<WrapperLookup> registryLookup)
	{
		super(dataOutput, registryLookup);
	}
	
	public void generate()
	{
		DROP_SELF.stream().map(Supplier::get).forEach(this::addDrop);
		RCBlocks.DYE_TO_TERRACOTTA.values().stream().map(Terracotta::faded).map(Supplier::get).forEach(this::addDrop);
		RCBlocks.DYE_TO_CONCRETE.values().stream().forEach(c -> addCrackedConcreteDrops(c.cracked().get(), c.powder().get()));
		LeafPileBlock.LEAF_PILES.forEach(l -> addLeafPileDrops(l));
		
		addRustDrops(RCBlocks.EXPOSED_IRON.get(), Items.IRON_INGOT, 4, 7);
		addRustDrops(RCBlocks.WEATHERED_IRON.get(), Items.IRON_INGOT, 2, 5);
		addRustDrops(RCBlocks.RUSTED_IRON.get(), Items.IRON_INGOT, 0, 3);
		addRustDrops(RCBlocks.IRON_SCRAP.get(), Items.IRON_NUGGET, 0, 3);
		addIvyDrops(RCBlocks.IVY.get());
		addSootDrops(RCBlocks.SOOT.get());
		addRubbleDrops(RCBlocks.STONE_RUBBLE.get());
		addRubbleDrops(RCBlocks.DEEPSLATE_RUBBLE.get());
		addRottenFruitDrops(RCBlocks.ROTTEN_MELON.get(), Items.MELON_SEEDS);
		addBrokenGlassDrops(RCBlocks.BROKEN_GLASS.get(), RCItems.GLASS_SHARD.get());
		RCBlocks.DYE_TO_GLASS.entrySet().forEach(entry -> addBrokenGlassDrops(entry.getValue().broken().get(), RCItems.DYE_TO_SHARD.get(entry.getKey()).get()));
		for(Block pumpkin : new Block[] {RCBlocks.ROTTEN_PUMPKIN.get(), RCBlocks.ROTTEN_CARVED_PUMPKIN.get(), RCBlocks.ROTTEN_JACK_O_LANTERN.get()})
			addRottenFruitDrops(pumpkin, Items.PUMPKIN_SEEDS);
	}
	
	private void addBrokenGlassDrops(Block block, Item shards)
	{
		addDrop(block, LootTable.builder()
				.pool(LootPool.builder().with(ItemEntry.builder(block).conditionally(createSilkTouchCondition())))
				.pool(LootPool.builder().with(ItemEntry.builder(shards).apply(SetCountLootFunction.builder(UniformLootNumberProvider.create(0, 3))).conditionally(createWithoutSilkTouchCondition()))));
	}
	
	private void addRottenFruitDrops(Block block, Item seeds)
	{
		addDrop(block, LootTable.builder()
				.pool(LootPool.builder().with(ItemEntry.builder(block).conditionally(createSilkTouchCondition())))
				.pool(LootPool.builder().with(ItemEntry.builder(seeds).apply(SetCountLootFunction.builder(UniformLootNumberProvider.create(0, 3))).conditionally(createWithoutSilkTouchCondition()))));
	}
	
	private void addCrackedConcreteDrops(Block block, Block powder)
	{
		addDrop(
				block, LootTable.builder()
					.pool(LootPool.builder().with(ItemEntry.builder(block)).conditionally(createSilkTouchCondition()))
					.pool(forCrackLevel(block, powder, 1))
					.pool(forCrackLevel(block, powder, 2))
					.pool(forCrackLevel(block, powder, 3))
					.pool(forCrackLevel(block, powder, 4))
				);
	}
	
	private LootPool.Builder forCrackLevel(Block silk, Block powder, int crack)
	{
		return LootPool.builder()
				.rolls(ConstantLootNumberProvider.create(1.0F))
				.conditionally(createWithoutSilkTouchCondition())
				.conditionally(BlockStatePropertyLootCondition.builder(silk).properties(StatePredicate.Builder.create().exactMatch(CrackedConcreteBlock.CRACKS, CrackedConcreteBlock.CRACKS.name(crack))))
				.with(ofItem(silk).weight(5 - crack))
				.with(ofItem(powder).weight(crack));
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
	
	private void addRubbleDrops(Block block)
	{
		addDrop(
				block, LootTable.builder()
					// Drop 1 block per depth when harvested
					.pool(
							LootPool.builder()
							.conditionally(EntityPropertiesLootCondition.create(LootContext.EntityTarget.THIS))
							.with(
									AlternativeEntry.builder(
										RubbleBlock.DEPTH.getValues(),
										integer -> ItemEntry.builder(block)
												.apply(SetCountLootFunction.builder(ConstantLootNumberProvider.create(integer.floatValue())))
												.conditionally(BlockStatePropertyLootCondition.builder(block).properties(StatePredicate.Builder.create().exactMatch(RubbleBlock.DEPTH, integer)))
												)
									)
							)
					
					);
	}
	
	private void addLeafPileDrops(Block block)
	{
		addDrop(
				block, LootTable.builder()
					// Drop 1 pile per layer when harvested with shears
					.pool(
							LootPool.builder()
							.conditionally(EntityPropertiesLootCondition.create(LootContext.EntityTarget.THIS))
							.with(
									AlternativeEntry.builder(
										LeafPileBlock.LAYERS.getValues(),
										integer -> ItemEntry.builder(block)
												.apply(SetCountLootFunction.builder(ConstantLootNumberProvider.create(integer.floatValue())))
												.conditionally(BlockStatePropertyLootCondition.builder(block).properties(StatePredicate.Builder.create().exactMatch(LeafPileBlock.LAYERS, integer)))
												)
										.conditionally(createWithShearsCondition())
									)
							)
					
					);
	}
	
	private void addIvyDrops(Block block)
	{
		addDrop(
				block, LootTable.builder()
					// Drop 1 ivy per covered face when harvested with shears
					.pool(conditionalPool(block, IvyBlock.EAST, true).conditionally(createWithShearsCondition()))
					.pool(conditionalPool(block, IvyBlock.NORTH, true).conditionally(createWithShearsCondition()))
					.pool(conditionalPool(block, IvyBlock.SOUTH, true).conditionally(createWithShearsCondition()))
					.pool(conditionalPool(block, IvyBlock.WEST, true).conditionally(createWithShearsCondition()))
					.pool(conditionalPool(block, IvyBlock.UP, true).conditionally(createWithShearsCondition()))
				);
	}
	
	private void addSootDrops(Block block)
	{
		addDrop(
				block, LootTable.builder()
					// Drop 0-1 charcoal when harvested without silk touch
					.pool(LootPool.builder().rolls(ConstantLootNumberProvider.create(1F)).with(
							ofItem(Items.CHARCOAL)
							.conditionally(createWithoutSilkTouchCondition())
							.apply(SetCountLootFunction.builder(UniformLootNumberProvider.create(0, 1)))))
					// Drop 1 soot per covered face when harvested with silk touch
					.pool(conditionalPool(block, SootBlock.EAST, true).conditionally(createSilkTouchCondition()))
					.pool(conditionalPool(block, SootBlock.NORTH, true).conditionally(createSilkTouchCondition()))
					.pool(conditionalPool(block, SootBlock.SOUTH, true).conditionally(createSilkTouchCondition()))
					.pool(conditionalPool(block, SootBlock.WEST, true).conditionally(createSilkTouchCondition()))
					.pool(conditionalPool(block, SootBlock.UP, true).conditionally(createSilkTouchCondition()))
					.pool(conditionalPool(block, SootBlock.DOWN, true).conditionally(createSilkTouchCondition()))
				);
	}
	
	private <T extends Property<U>, U extends Comparable<U>> LootPool.Builder conditionalPool(Block drop, T property, U val)
	{
		return addSurvivesExplosionCondition(
				drop,
				LootPool.builder()
					.rolls(ConstantLootNumberProvider.create(1.0F))
					.with(
						ofItem(drop)
							.conditionally(BlockStatePropertyLootCondition.builder(drop).properties(StatePredicate.Builder.create().exactMatch(property, property.name(val))))
					)
			);
	}
	
	private static ItemEntry.Builder<?> ofItem(ItemConvertible item)
	{
		return ItemEntry.builder(item);
	}
}
