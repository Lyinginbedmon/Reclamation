package com.lying.decay.conditions;

import java.util.EnumSet;
import java.util.List;
import java.util.Optional;

import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.lying.init.RCDecayConditions;
import com.lying.utility.BlockPredicate;
import com.lying.utility.BlockPredicate.Builder;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

public abstract class ConditionNeighbouring extends DecayCondition
{
	protected Optional<EnumSet<Direction>> faces = Optional.empty();
	protected int threshold = 1;
	
	protected ConditionNeighbouring(Identifier idIn)
	{
		super(idIn);
	}
	
	public ConditionNeighbouring faces(Direction... facesIn)
	{
		if(facesIn == null || facesIn.length == 0)
			faces = Optional.empty();
		else
		{
			EnumSet<Direction> faces = EnumSet.noneOf(Direction.class);
			for(Direction face : facesIn)
				faces.add(face);
			this.faces = Optional.of(faces);
		}
		return this;
	}
	
	public ConditionNeighbouring threshold(int value)
	{
		this.threshold = Math.max(0, value);
		return this;
	}
	
	public boolean check(ServerWorld world, BlockPos pos, BlockState currentState)
	{
		int tally = 0;
		for(Direction face : this.faces.orElse(EnumSet.allOf(Direction.class)))
			if(isMatch(world.getBlockState(pos.offset(face)), pos.offset(face), face, pos, world) && ++tally >= threshold)
				return true;
		
		return false;
	}
	
	protected abstract boolean isMatch(BlockState state, BlockPos neighbour, Direction face, BlockPos pos, ServerWorld world);
	
	protected JsonObject write(JsonObject obj)
	{
		if(threshold != 1)
			obj.addProperty("threshold", threshold);
		faces.ifPresent(set -> 
		{
			JsonArray array = new JsonArray();
			set.forEach(face -> array.add(face.toString()));
			obj.add("faces", array);
		});
		return obj;
	}
	
	protected void read(JsonObject obj)
	{
		this.threshold = obj.has("threshold") ? obj.get("threshold").getAsInt() : 1;
		this.faces = Optional.empty();
		if(obj.has("faces"))
		{
			EnumSet<Direction> faces = EnumSet.noneOf(Direction.class);
			obj.get("faces").getAsJsonArray().forEach(e -> 
			{
				Direction face = Direction.byName(e.getAsString());
				if(face != null)
					faces.add(face);
			});
			this.faces = Optional.of(faces);
		}
	}
	
	public static class Blocks extends ConditionNeighbouring
	{
		protected List<Block> blocks = Lists.newArrayList();
		protected List<BlockState> states = Lists.newArrayList();
		protected List<TagKey<Block>> tags = Lists.newArrayList();
		protected BlockPredicate predicate = BlockPredicate.Builder.create().build();
		
		public Blocks(Identifier idIn)
		{
			super(idIn);
		}
		
		protected boolean isMatch(BlockState state, BlockPos neighbour, Direction face, BlockPos pos, ServerWorld world)
		{
			return predicate.test(state);
		}
		
		public static Blocks of(Block... target)
		{
			Blocks inst = (Blocks)RCDecayConditions.IS_BLOCK.get();
			Builder builder = BlockPredicate.Builder.create();
			builder.add(target);
			inst.predicate = builder.build();
			return inst;
		}
		
		public static Blocks of(BlockState... target)
		{
			Blocks inst = (Blocks)RCDecayConditions.IS_BLOCK.get();
			Builder builder = BlockPredicate.Builder.create();
			builder.add(target);
			inst.predicate = builder.build();
			return inst;
		}
		
		@SuppressWarnings("unchecked")
		public static Blocks of(TagKey<Block>... target)
		{
			Blocks inst = (Blocks)RCDecayConditions.IS_BLOCK.get();
			Builder builder = BlockPredicate.Builder.create();
			builder.add(target);
			inst.predicate = builder.build();
			return inst;
		}
		
		protected JsonObject write(JsonObject obj)
		{
			super.write(obj);
			obj.add("target", predicate.toJson());
			return obj;
		}
		
		protected void read(JsonObject obj)
		{
			super.read(obj);
			predicate = BlockPredicate.fromJson(obj.getAsJsonObject("target"));
		}
	}
	
	public static class AirAbove extends DecayCondition
	{
		public AirAbove(Identifier idIn)
		{
			super(idIn);
		}
		
		public boolean check(ServerWorld world, BlockPos pos, BlockState currentState)
		{
			return world.isAir(pos.up());
		}
	}
	
	public static class Supported extends ConditionNeighbouring
	{
		public Supported(Identifier idIn)
		{
			super(idIn);
		}
		
		protected boolean isMatch(BlockState state, BlockPos neighbour, Direction face, BlockPos pos, ServerWorld world)
		{
			return !state.isReplaceable() && state.isSideSolidFullSquare(world, neighbour, face.getOpposite());
		}
	}
	
	public static class OnGround extends Supported
	{
		public OnGround(Identifier idIn)
		{
			super(idIn);
			faces(Direction.DOWN);
		}
		
		protected JsonObject write(JsonObject obj)
		{
			return obj;
		}
	}
	
	public static class Unsupported extends Supported
	{
		public Unsupported(Identifier idIn)
		{
			super(idIn);
			invert();
		}
	}
	
	public static class Exposed extends ConditionNeighbouring
	{
		public Exposed(Identifier idIn)
		{
			super(idIn);
		}
		
		public Exposed face(Direction... facesIn)
		{
			Exposed condition = (Exposed)RCDecayConditions.EXPOSED.get();
			EnumSet<Direction> faces = EnumSet.noneOf(Direction.class);
			for(Direction face : facesIn)
				faces.add(face);
			condition.faces = Optional.of(faces);
			return condition;
		}
		
		protected boolean isMatch(BlockState state, BlockPos neighbour, Direction face, BlockPos pos, ServerWorld world)
		{
			return state.isAir();
		}
	}
}
