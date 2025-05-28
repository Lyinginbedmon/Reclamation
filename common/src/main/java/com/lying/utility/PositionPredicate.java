package com.lying.utility;

import static com.lying.utility.RCUtils.listOrSolo;

import java.util.List;
import java.util.Optional;
import java.util.function.BiPredicate;

import com.google.common.collect.Lists;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.lying.Reclamation;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction.Axis;

public class PositionPredicate
{
	public static final Codec<PositionPredicate> CODEC	= RecordCodecBuilder.create(instance -> instance.group(
				Property.CODEC.listOf().optionalFieldOf("properties").forGetter(p -> listOrSolo(Optional.of(p.values)).getLeft()),
				Property.CODEC.optionalFieldOf("property").forGetter(p -> listOrSolo(Optional.of(p.values)).getRight())
			).apply(instance, (l,s) -> 
			{
				PositionPredicate predicate = create();
				l.ifPresent(list -> list.forEach(predicate::add));
				s.ifPresent(predicate::add);
				return predicate;
			}));
	
	private final List<Property> values = Lists.newArrayList();
	
	protected PositionPredicate() { }
	
	public static PositionPredicate create() { return new PositionPredicate(); }
	
	public PositionPredicate add(Property valueIn)
	{
		values.add(valueIn);
		return this;
	}
	
	public boolean test(BlockPos pos)
	{
		return values.isEmpty() || values.stream().allMatch(v -> v.matches(pos));
	}
	
	public JsonElement toJson()
	{
		return CODEC.encodeStart(JsonOps.INSTANCE, this).getOrThrow();
	}
	
	public static PositionPredicate fromJson(JsonObject obj)
	{
		return CODEC.parse(JsonOps.INSTANCE, obj).getOrThrow();
	}
	
	public static record Property(int value, Axis axis, Comparison operation)
	{
		public static final Codec<Property> CODEC	= RecordCodecBuilder.create(instance -> instance.group(
				Codec.INT.fieldOf("value").forGetter(Property::value),
				Axis.CODEC.fieldOf("axis").forGetter(Property::axis),
				Comparison.CODEC.fieldOf("operation").forGetter(Property::operation)
				)
				.apply(instance, (v,s,o) -> new Property(v, s, o)));
		
		public static Property of(int value, Axis axis, Comparison operation) { return new Property(value, axis, operation); }
		
		public boolean matches(BlockPos pos)
		{
			int coordinate;
			switch(axis)
			{
				case X: coordinate = pos.getX(); break;
				case Y: coordinate = pos.getY(); break;
				case Z: coordinate = pos.getZ(); break;
				default:
					return false;
			}
			
			return operation.apply(coordinate, value);
		}
	}
	
	/** Serializable comparator functions for any two integers */
	public static enum Comparison implements StringIdentifiable
	{
		EQUAL("=", (a,b) -> a == b),
		UNEQUAL("!=", (a,b) -> a != b),
		GREATER_THAN(">", (a,b) -> a > b),
		GREATER_THAN_OR_EQUAL(">=", (a,b) -> a >= b),
		LESS_THAN("<", (a,b) -> a < b),
		LESS_THAN_OR_EQUAL("<=", (a,b) -> a <= b);
		
		@SuppressWarnings("deprecation")
		public static final StringIdentifiable.EnumCodec<Comparison> CODEC = StringIdentifiable.createCodec(Comparison::values);
		
		private final String name;
		private final BiPredicate<Float, Float> func;
		
		private Comparison(String nameIn, BiPredicate<Float, Float> funcIn)
		{
			name = nameIn;
			func = funcIn;
		}
		
		public boolean apply(float a, float b) { return func.test(a, b); }
		
		public String asString() { return name; }
		
		public JsonElement toJson() { return CODEC.encodeStart(JsonOps.INSTANCE, this).getOrThrow(); }
		
		public static Comparison fromJson(JsonElement obj) { return CODEC.parse(JsonOps.INSTANCE, obj).getOrThrow(); }
		
		public static Comparison fromString(String name)
		{
			for(Comparison op : values())
				if(op.asString().equalsIgnoreCase(name))
					return op;
			
			Reclamation.LOGGER.warn("Failed to parse position predicate operator '{}', treating as = instead", name);
			return EQUAL;
		}
	}
}
