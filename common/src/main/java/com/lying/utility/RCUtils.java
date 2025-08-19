package com.lying.utility;

import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.lang3.tuple.Pair;

import com.google.common.collect.Lists;
import com.mojang.serialization.Codec;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.util.DyeColor;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

public class RCUtils
{
	public static final Codec<EnumSet<Direction>> DIRECTION_SET_CODEC	= Direction.CODEC.listOf().stable().<EnumSet<Direction>>xmap(list -> 
	{
		EnumSet<Direction> set = EnumSet.noneOf(Direction.class);
		list.forEach(set::add);
		return set;
	}, set -> set.stream().toList());
	public static final DyeColor[] COLOR_SPECTRUM = new DyeColor[] 
			{
				DyeColor.RED,
				DyeColor.ORANGE,
				DyeColor.YELLOW,
				DyeColor.LIME,
				DyeColor.GREEN,
				DyeColor.CYAN,
				DyeColor.LIGHT_BLUE,
				DyeColor.BLUE,
				DyeColor.PURPLE,
				DyeColor.MAGENTA,
				DyeColor.PINK,
				DyeColor.BROWN,
				DyeColor.BLACK,
				DyeColor.GRAY,
				DyeColor.LIGHT_GRAY,
				DyeColor.WHITE
			};
	
	private static final Map<DyeColor, Pair<Block,Block>> DYE_TO_CONCRETE = new HashMap<>();
	private static final Map<DyeColor, Pair<Block,Block>> DYE_TO_TERRACOTTA = new HashMap<>();
	private static final Map<DyeColor, Block> DYE_TO_STAINED_GLASS = new HashMap<>(), DYE_TO_STAINED_GLASS_PANE = new HashMap<>();
	
	static
	{
		DYE_TO_CONCRETE.put(DyeColor.BLACK, Pair.of(Blocks.BLACK_CONCRETE_POWDER, Blocks.BLACK_CONCRETE));
		DYE_TO_CONCRETE.put(DyeColor.BLUE, Pair.of(Blocks.BLUE_CONCRETE_POWDER, Blocks.BLUE_CONCRETE));
		DYE_TO_CONCRETE.put(DyeColor.BROWN, Pair.of(Blocks.BROWN_CONCRETE_POWDER, Blocks.BROWN_CONCRETE));
		DYE_TO_CONCRETE.put(DyeColor.CYAN, Pair.of(Blocks.CYAN_CONCRETE_POWDER, Blocks.CYAN_CONCRETE));
		DYE_TO_CONCRETE.put(DyeColor.GRAY, Pair.of(Blocks.GRAY_CONCRETE_POWDER, Blocks.GRAY_CONCRETE));
		DYE_TO_CONCRETE.put(DyeColor.GREEN, Pair.of(Blocks.GREEN_CONCRETE_POWDER, Blocks.GREEN_CONCRETE));
		DYE_TO_CONCRETE.put(DyeColor.LIGHT_BLUE, Pair.of(Blocks.LIGHT_BLUE_CONCRETE_POWDER, Blocks.LIGHT_BLUE_CONCRETE));
		DYE_TO_CONCRETE.put(DyeColor.LIGHT_GRAY, Pair.of(Blocks.LIGHT_GRAY_CONCRETE_POWDER, Blocks.LIGHT_GRAY_CONCRETE));
		DYE_TO_CONCRETE.put(DyeColor.LIME, Pair.of(Blocks.LIME_CONCRETE_POWDER, Blocks.LIME_CONCRETE));
		DYE_TO_CONCRETE.put(DyeColor.MAGENTA, Pair.of(Blocks.MAGENTA_CONCRETE_POWDER, Blocks.MAGENTA_CONCRETE));
		DYE_TO_CONCRETE.put(DyeColor.ORANGE, Pair.of(Blocks.ORANGE_CONCRETE_POWDER, Blocks.ORANGE_CONCRETE));
		DYE_TO_CONCRETE.put(DyeColor.PINK, Pair.of(Blocks.PINK_CONCRETE_POWDER, Blocks.PINK_CONCRETE));
		DYE_TO_CONCRETE.put(DyeColor.PURPLE, Pair.of(Blocks.PURPLE_CONCRETE_POWDER, Blocks.PURPLE_CONCRETE));
		DYE_TO_CONCRETE.put(DyeColor.RED, Pair.of(Blocks.RED_CONCRETE_POWDER, Blocks.RED_CONCRETE));
		DYE_TO_CONCRETE.put(DyeColor.WHITE, Pair.of(Blocks.WHITE_CONCRETE_POWDER, Blocks.WHITE_CONCRETE));
		DYE_TO_CONCRETE.put(DyeColor.YELLOW, Pair.of(Blocks.YELLOW_CONCRETE_POWDER, Blocks.YELLOW_CONCRETE));
		
		DYE_TO_TERRACOTTA.put(DyeColor.BLACK, Pair.of(Blocks.BLACK_TERRACOTTA, Blocks.BLACK_GLAZED_TERRACOTTA));
		DYE_TO_TERRACOTTA.put(DyeColor.BLUE, Pair.of(Blocks.BLUE_TERRACOTTA, Blocks.BLUE_GLAZED_TERRACOTTA));
		DYE_TO_TERRACOTTA.put(DyeColor.BROWN, Pair.of(Blocks.BROWN_TERRACOTTA, Blocks.BROWN_GLAZED_TERRACOTTA));
		DYE_TO_TERRACOTTA.put(DyeColor.CYAN, Pair.of(Blocks.CYAN_TERRACOTTA, Blocks.CYAN_GLAZED_TERRACOTTA));
		DYE_TO_TERRACOTTA.put(DyeColor.GRAY, Pair.of(Blocks.GRAY_TERRACOTTA, Blocks.GRAY_GLAZED_TERRACOTTA));
		DYE_TO_TERRACOTTA.put(DyeColor.GREEN, Pair.of(Blocks.GREEN_TERRACOTTA, Blocks.GREEN_GLAZED_TERRACOTTA));
		DYE_TO_TERRACOTTA.put(DyeColor.LIGHT_BLUE, Pair.of(Blocks.LIGHT_BLUE_TERRACOTTA, Blocks.LIGHT_BLUE_GLAZED_TERRACOTTA));
		DYE_TO_TERRACOTTA.put(DyeColor.LIGHT_GRAY, Pair.of(Blocks.LIGHT_GRAY_TERRACOTTA, Blocks.LIGHT_GRAY_GLAZED_TERRACOTTA));
		DYE_TO_TERRACOTTA.put(DyeColor.LIME, Pair.of(Blocks.LIME_TERRACOTTA, Blocks.LIME_GLAZED_TERRACOTTA));
		DYE_TO_TERRACOTTA.put(DyeColor.MAGENTA, Pair.of(Blocks.MAGENTA_TERRACOTTA, Blocks.MAGENTA_GLAZED_TERRACOTTA));
		DYE_TO_TERRACOTTA.put(DyeColor.ORANGE, Pair.of(Blocks.ORANGE_TERRACOTTA, Blocks.ORANGE_GLAZED_TERRACOTTA));
		DYE_TO_TERRACOTTA.put(DyeColor.PINK, Pair.of(Blocks.PINK_TERRACOTTA, Blocks.PINK_GLAZED_TERRACOTTA));
		DYE_TO_TERRACOTTA.put(DyeColor.PURPLE, Pair.of(Blocks.PURPLE_TERRACOTTA, Blocks.PURPLE_GLAZED_TERRACOTTA));
		DYE_TO_TERRACOTTA.put(DyeColor.RED, Pair.of(Blocks.RED_TERRACOTTA, Blocks.RED_GLAZED_TERRACOTTA));
		DYE_TO_TERRACOTTA.put(DyeColor.WHITE, Pair.of(Blocks.WHITE_TERRACOTTA, Blocks.WHITE_GLAZED_TERRACOTTA));
		DYE_TO_TERRACOTTA.put(DyeColor.YELLOW, Pair.of(Blocks.YELLOW_TERRACOTTA, Blocks.YELLOW_GLAZED_TERRACOTTA));
		
		DYE_TO_STAINED_GLASS.put(DyeColor.BLACK, Blocks.BLACK_STAINED_GLASS);
		DYE_TO_STAINED_GLASS.put(DyeColor.BLUE, Blocks.BLUE_STAINED_GLASS);
		DYE_TO_STAINED_GLASS.put(DyeColor.BROWN, Blocks.BROWN_STAINED_GLASS);
		DYE_TO_STAINED_GLASS.put(DyeColor.CYAN, Blocks.CYAN_STAINED_GLASS);
		DYE_TO_STAINED_GLASS.put(DyeColor.GRAY, Blocks.GRAY_STAINED_GLASS);
		DYE_TO_STAINED_GLASS.put(DyeColor.GREEN, Blocks.GREEN_STAINED_GLASS);
		DYE_TO_STAINED_GLASS.put(DyeColor.LIGHT_BLUE, Blocks.LIGHT_BLUE_STAINED_GLASS);
		DYE_TO_STAINED_GLASS.put(DyeColor.LIGHT_GRAY, Blocks.LIGHT_GRAY_STAINED_GLASS);
		DYE_TO_STAINED_GLASS.put(DyeColor.LIME, Blocks.LIME_STAINED_GLASS);
		DYE_TO_STAINED_GLASS.put(DyeColor.MAGENTA, Blocks.MAGENTA_STAINED_GLASS);
		DYE_TO_STAINED_GLASS.put(DyeColor.ORANGE, Blocks.ORANGE_STAINED_GLASS);
		DYE_TO_STAINED_GLASS.put(DyeColor.PINK, Blocks.PINK_STAINED_GLASS);
		DYE_TO_STAINED_GLASS.put(DyeColor.PURPLE, Blocks.PURPLE_STAINED_GLASS);
		DYE_TO_STAINED_GLASS.put(DyeColor.RED, Blocks.RED_STAINED_GLASS);
		DYE_TO_STAINED_GLASS.put(DyeColor.WHITE, Blocks.WHITE_STAINED_GLASS);
		DYE_TO_STAINED_GLASS.put(DyeColor.YELLOW, Blocks.YELLOW_STAINED_GLASS);
		
		DYE_TO_STAINED_GLASS_PANE.put(DyeColor.BLACK, Blocks.BLACK_STAINED_GLASS_PANE);
		DYE_TO_STAINED_GLASS_PANE.put(DyeColor.BLUE, Blocks.BLUE_STAINED_GLASS_PANE);
		DYE_TO_STAINED_GLASS_PANE.put(DyeColor.BROWN, Blocks.BROWN_STAINED_GLASS_PANE);
		DYE_TO_STAINED_GLASS_PANE.put(DyeColor.CYAN, Blocks.CYAN_STAINED_GLASS_PANE);
		DYE_TO_STAINED_GLASS_PANE.put(DyeColor.GRAY, Blocks.GRAY_STAINED_GLASS_PANE);
		DYE_TO_STAINED_GLASS_PANE.put(DyeColor.GREEN, Blocks.GREEN_STAINED_GLASS_PANE);
		DYE_TO_STAINED_GLASS_PANE.put(DyeColor.LIGHT_BLUE, Blocks.LIGHT_BLUE_STAINED_GLASS_PANE);
		DYE_TO_STAINED_GLASS_PANE.put(DyeColor.LIGHT_GRAY, Blocks.LIGHT_GRAY_STAINED_GLASS_PANE);
		DYE_TO_STAINED_GLASS_PANE.put(DyeColor.LIME, Blocks.LIME_STAINED_GLASS_PANE);
		DYE_TO_STAINED_GLASS_PANE.put(DyeColor.MAGENTA, Blocks.MAGENTA_STAINED_GLASS_PANE);
		DYE_TO_STAINED_GLASS_PANE.put(DyeColor.ORANGE, Blocks.ORANGE_STAINED_GLASS_PANE);
		DYE_TO_STAINED_GLASS_PANE.put(DyeColor.PINK, Blocks.PINK_STAINED_GLASS_PANE);
		DYE_TO_STAINED_GLASS_PANE.put(DyeColor.PURPLE, Blocks.PURPLE_STAINED_GLASS_PANE);
		DYE_TO_STAINED_GLASS_PANE.put(DyeColor.RED, Blocks.RED_STAINED_GLASS_PANE);
		DYE_TO_STAINED_GLASS_PANE.put(DyeColor.WHITE, Blocks.WHITE_STAINED_GLASS_PANE);
		DYE_TO_STAINED_GLASS_PANE.put(DyeColor.YELLOW, Blocks.YELLOW_STAINED_GLASS_PANE);
	}
	
	public static Block dyeToConcretePowder(DyeColor color) { return DYE_TO_CONCRETE.get(color).getLeft(); }
	public static Block dyeToConcrete(DyeColor color) { return DYE_TO_CONCRETE.get(color).getRight(); }
	public static Block dyeToTerracotta(DyeColor color) { return DYE_TO_TERRACOTTA.get(color).getLeft(); }
	public static Block dyeToGlazedTerracotta(DyeColor color) { return DYE_TO_TERRACOTTA.get(color).getRight(); }
	public static Block dyeToStainedGlass(DyeColor color) { return DYE_TO_STAINED_GLASS.get(color); }
	public static Block dyeToStainedGlassPane(DyeColor color) { return DYE_TO_STAINED_GLASS_PANE.get(color); }
	
	/** Returns a pair containing an optional for:<br> * the input list (if it is present and greater than size 2)<br>* the only value in that list (if it is present and of size 1) */
	public static <T extends Object> Pair<Optional<List<T>>, Optional<T>> listOrSolo(Optional<List<T>> primary)
	{
		List<T> list = primary.orElse(Lists.newArrayList());
		Optional<List<T>> a = list.size() < 2 ? Optional.empty() : primary;
		Optional<T> b = list.size() == 1 ? Optional.of(list.get(0)) : Optional.empty();
		return Pair.of(a, b);
	}
	
	/** Returns a {@link BlockPos} comparator for nearest-to-furthest sorting towards the given position */
	public static Comparator<BlockPos> closestFirst(BlockPos origin)
	{
		return (a,b) -> 
		{
			double distA = a.getSquaredDistance(origin);
			double distB = b.getSquaredDistance(origin);
			return distA < distB ? -1 : distA > distB ? 1 : 0;
		};
	}
	
	/** Returns an optional of the given list or an empty optional if it is empty */
	public static <T extends Object> Optional<List<T>> orEmpty(List<T> list)
	{
		return list.isEmpty() ? Optional.empty() : Optional.of(list);
	}
}
