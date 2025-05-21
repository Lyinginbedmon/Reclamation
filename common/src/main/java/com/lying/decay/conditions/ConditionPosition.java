package com.lying.decay.conditions;

import java.util.Optional;

import com.google.gson.JsonObject;
import com.lying.decay.context.DecayContext;
import com.lying.init.RCDecayConditions;
import com.lying.utility.PositionPredicate;
import com.lying.utility.PositionPredicate.Comparison;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;

import net.minecraft.block.Block;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

public class ConditionPosition extends DecayCondition
{
	private PositionPredicate predicate;
	
	public ConditionPosition(Identifier idIn)
	{
		super(idIn);
	}
	
	public static ConditionPosition of(PositionPredicate predicateIn)
	{
		ConditionPosition condition = (ConditionPosition)RCDecayConditions.WORLD_POSITION.get();
		condition.predicate = predicateIn;
		return condition;
	}
	
	protected boolean check(DecayContext context)
	{
		return predicate.test(context.currentPos());
	}
	
	protected JsonObject write(JsonObject obj)
	{
		return predicate.toJson().getAsJsonObject();
	}
	
	protected void read(JsonObject obj)
	{
		predicate = PositionPredicate.fromJson(obj);
	}
	
	public static class Dimension extends DecayCondition
	{
		private static final Codec<RegistryKey<World>> CODEC	= RegistryKey.createCodec(RegistryKeys.WORLD);
		
		private RegistryKey<World> dimension;
		
		public Dimension(Identifier idIn)
		{
			super(idIn);
		}
		
		protected boolean check(DecayContext context)
		{
			return context.worldKey() == dimension;
		}
		
		protected JsonObject write(JsonObject obj)
		{
			obj.add("target", CODEC.encodeStart(JsonOps.INSTANCE, dimension).getOrThrow());
			return obj;
		}
		
		protected void read(JsonObject obj)
		{
			dimension = CODEC.parse(JsonOps.INSTANCE, obj.get("target")).getOrThrow();
		}
	}
	
	public static class Altitude extends DecayCondition
	{
		private Optional<Integer> threshold = Optional.empty();
		private Optional<Comparison> operation = Optional.empty();
		
		public Altitude(Identifier idIn)
		{
			super(idIn);
		}
		
		public static Altitude of(int thresholdIn)
		{
			Altitude condition = (Altitude)RCDecayConditions.ALTITUDE.get();
			condition.threshold = Optional.of(Math.abs(thresholdIn));
			return condition;
		}
		
		public static Altitude of(int thresholdIn, Comparison operationIn)
		{
			Altitude condition = (Altitude)RCDecayConditions.ALTITUDE.get();
			condition.threshold = Optional.of(Math.abs(thresholdIn));
			condition.operation = Optional.of(operationIn);
			return condition;
		}
		
		protected boolean check(DecayContext context)
		{
			ServerWorld world = context.world.get();
			BlockPos pos = context.currentPos();
			if(pos.getY() == world.getBottomY())
				return test(0);
			
			int altitude = 0;
			for(int y=pos.getY() - 1; y>world.getBottomY(); y--)
				if(isGround(world, pos.withY(y)))
					return test(altitude);
				else
					altitude++;
			
			return test(altitude);
		}
		
		private boolean test(int altitude)
		{
			return operation.orElse(Comparison.GREATER_THAN_OR_EQUAL).apply(altitude, threshold.orElse(4));
		}
		
		private static boolean isGround(World world, BlockPos pos)
		{
			return Block.sideCoversSmallSquare(world, pos, Direction.UP);
		}
		
		protected JsonObject write(JsonObject obj)
		{
			threshold.ifPresent(v -> obj.addProperty("threshold", v));
			operation.ifPresent(c -> obj.add("operation", c.toJson()));
			return obj;
		}
		
		protected void read(JsonObject obj)
		{
			if(obj.has("threshold"))
				threshold = Optional.of(obj.get("threshold").getAsInt());
			if(obj.has("operation"))
				operation = Optional.of(Comparison.fromJson(obj.get("operation")));
		}
	}
}
