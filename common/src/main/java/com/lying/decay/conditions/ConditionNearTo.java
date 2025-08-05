package com.lying.decay.conditions;

import java.util.Optional;

import com.google.gson.JsonObject;
import com.lying.decay.context.DecayContext;
import com.lying.init.RCDecayConditions;
import com.lying.utility.BlockPredicate;
import com.lying.utility.EntityPredicate;
import com.lying.utility.LocalRegion;

import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;

public class ConditionNearTo extends DecayCondition
{
	protected BlockPredicate predicate = BlockPredicate.Builder.create().build();
	protected LocalRegion region = new LocalRegion(new Vec3i(3, 3, 3));
	protected Optional<Integer> threshold = Optional.empty();
	
	public ConditionNearTo(Identifier idIn)
	{
		super(idIn, 5);
	}
	
	public static ConditionNearTo create(){ return RCDecayConditions.NEAR_TO.get(); }
	
	public ConditionNearTo predicate(BlockPredicate predicateIn)
	{
		predicate = predicateIn;
		return this;
	}
	
	public ConditionNearTo threshold(int thresholdIn)
	{
		threshold = Optional.of(thresholdIn);
		return this;
	}
	
	/** Creates a spherical region */
	public ConditionNearTo bounds(int radius)
	{
		region = new LocalRegion(radius);
		return this;
	}
	
	/** Creates a centred region */
	public ConditionNearTo bounds(int sizeX, int sizeY, int sizeZ)
	{
		region = new LocalRegion(new Vec3i(sizeX, sizeY, sizeZ));
		return this;
	}
	
	/** Creates a bounded region */
	public ConditionNearTo bounds(int minX, int maxX, int minY, int maxY, int minZ, int maxZ)
	{
		region = new LocalRegion(new Vec3i(minX, minY, minZ), new Vec3i(maxX, maxY, maxZ));
		return this;
	}
	
	protected boolean check(DecayContext context)
	{
		int tally = 0;
		int limit = threshold.orElse(1);
		ServerWorld world = context.world.get();
		for(Vec3i point : region.positions())
		{
			BlockPos offset = context.currentPos().add(point);
			if(offset.getY() < world.getBottomY() || offset.getY() > world.getTopYInclusive())
				continue;
			
			BlockState stateAt = context.getBlockState(offset);
			if(predicate.test(stateAt))
			{
				tally++;
				if(tally >= limit)
					return true;
			}
		}
		return false;
	}
	
	protected JsonObject write(JsonObject obj)
	{
		threshold.ifPresent(i -> obj.addProperty("threshold", i));
		obj.add("bounds", region.toJson());
		obj.add("predicate", predicate.toJson());
		return obj;
	}
	
	protected void read(JsonObject obj)
	{
		if(obj.has("threshold"))
			threshold = Optional.of(obj.get("threshold").getAsInt());
		region = LocalRegion.fromJson(obj.getAsJsonObject("bounds"));
		predicate = BlockPredicate.fromJson(obj.getAsJsonObject("predicate"));
	}
	
	public static class Ent extends ConditionNearTo
	{
		protected EntityPredicate predicate = EntityPredicate.Builder.create().build();
		
		public Ent(Identifier idIn)
		{
			super(idIn);
		}
		
		public static Ent create(){ return RCDecayConditions.NEAR_ENTITY.get(); }
		
		public Ent predicate(EntityPredicate predicateIn)
		{
			predicate = predicateIn;
			return this;
		}
		
		protected boolean check(DecayContext context)
		{
			if(context.world.isEmpty())
				return false;
			
			ServerWorld world = context.world.get();
			BlockPos currentPos = context.currentPos();
			return world.getEntitiesByClass(Entity.class, region.toBox(context.currentPos()), e -> region.test(e.getBlockPos().subtract(currentPos)) && predicate.apply(e)).stream().count() >= threshold.orElse(1);
		}
		
		protected JsonObject write(JsonObject obj)
		{
			threshold.ifPresent(i -> obj.addProperty("threshold", i));
			obj.add("bounds", region.toJson());
			obj.add("predicate", predicate.toJson());
			return obj;
		}
		
		protected void read(JsonObject obj)
		{
			if(obj.has("threshold"))
				threshold = Optional.of(obj.get("threshold").getAsInt());
			region = LocalRegion.fromJson(obj.getAsJsonObject("bounds"));
			predicate = EntityPredicate.fromJson(obj.getAsJsonObject("predicate"));
		}
	}
}
