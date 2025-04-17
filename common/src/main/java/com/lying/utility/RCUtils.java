package com.lying.utility;

import java.util.EnumSet;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.tuple.Pair;

import com.google.common.collect.Lists;
import com.mojang.serialization.Codec;

import net.minecraft.util.math.Direction;

public class RCUtils
{
	public static final Codec<EnumSet<Direction>> DIRECTION_SET_CODEC	= Direction.CODEC.listOf().stable().<EnumSet<Direction>>xmap(list -> 
	{
		EnumSet<Direction> set = EnumSet.noneOf(Direction.class);
		list.forEach(set::add);
		return set;
	}, set -> set.stream().toList());
	
	/** Returns a pair containing an optional for:<br> * the input list (if it is present and greater than size 2)<br>* the only value in that list (if it is present and of size 1) */
	public static <T extends Object> Pair<Optional<List<T>>, Optional<T>> listOrSolo(Optional<List<T>> primary)
	{
		List<T> list = primary.orElse(Lists.newArrayList());
		Optional<List<T>> a = list.size() < 2 ? Optional.empty() : primary;
		Optional<T> b = list.size() == 1 ? Optional.of(list.get(0)) : Optional.empty();
		return Pair.of(a, b);
	}
}
