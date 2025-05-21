package com.lying.decay.conditions;

import java.util.EnumSet;
import java.util.List;
import java.util.Optional;

import com.google.gson.JsonObject;
import com.lying.Reclamation;
import com.lying.init.RCDecayConditions;
import com.lying.utility.BlockPredicate;
import com.lying.utility.BlockPredicate.Builder;
import com.lying.utility.RCUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

public abstract class ConditionNeighbouring extends DecayCondition
{
	protected NeighbourData data = new NeighbourData(Optional.empty(), Optional.empty());
	
	protected ConditionNeighbouring(Identifier idIn)
	{
		super(idIn);
	}
	
	public ConditionNeighbouring faces(Direction... facesIn)
	{
		if(facesIn == null || facesIn.length == 0)
			data = new NeighbourData(Optional.empty(), data.threshold());
		else
		{
			EnumSet<Direction> faces = EnumSet.noneOf(Direction.class);
			faces.addAll(List.of(facesIn));
			data = new NeighbourData(Optional.of(faces), data.threshold());
		}
		return this;
	}
	
	public ConditionNeighbouring threshold(int value)
	{
		data = new NeighbourData(data.faces(), Optional.of(Math.max(0, value)));
		return this;
	}
	
	public boolean check(ServerWorld world, BlockPos pos, BlockState currentState)
	{
		int tally = 0;
		for(Direction face : data.facesToCheck())
			if(isMatch(world.getBlockState(pos.offset(face)), pos.offset(face), face, pos, world) && ++tally >= data.minimumTally())
				return true;
		
		return false;
	}
	
	protected abstract boolean isMatch(BlockState state, BlockPos neighbour, Direction face, BlockPos pos, ServerWorld world);
	
	protected JsonObject write(JsonObject obj)
	{
		return NeighbourData.CODEC.encodeStart(JsonOps.INSTANCE, data).resultOrPartial(Reclamation.LOGGER::error).orElseThrow().getAsJsonObject();
	}
	
	protected void read(JsonObject obj)
	{
		data = NeighbourData.CODEC.parse(JsonOps.INSTANCE, obj).resultOrPartial(Reclamation.LOGGER::error).orElseThrow();
	}
	
	private record NeighbourData(Optional<EnumSet<Direction>> faces, Optional<Integer> threshold)
	{
		private static final Codec<NeighbourData> CODEC	= RecordCodecBuilder.create(instance -> instance.group(
				Direction.CODEC.optionalFieldOf("face").forGetter(d -> d.faces().isEmpty() || d.faces().get().size() > 1 ? Optional.empty() : d.faces().get().stream().findFirst()),
				RCUtils.DIRECTION_SET_CODEC.optionalFieldOf("faces").forGetter(d -> d.faces().isEmpty() || d.faces().get().size() == 1 ? Optional.empty() : d.faces()),
				Codec.INT.optionalFieldOf("at_least").forGetter(NeighbourData::threshold))
				.apply(instance, (face, faceSet, limit) -> 
				{
					EnumSet<Direction> set = EnumSet.noneOf(Direction.class);
					face.ifPresent(f -> set.add(f));
					faceSet.ifPresent(s -> s.forEach(set::add));
					return new NeighbourData(set.isEmpty() ? Optional.empty() : Optional.of(set), limit);
				}));
		
		public EnumSet<Direction> facesToCheck() { return faces().orElse(EnumSet.allOf(Direction.class)); }
		
		public int minimumTally() { return threshold.orElse(1); }
	}
	
	public static class Blocks extends ConditionNeighbouring
	{
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
			Blocks inst = (Blocks)RCDecayConditions.ADJACENT_TO.get();
			Builder builder = BlockPredicate.Builder.create();
			builder.addBlock(target);
			inst.predicate = builder.build();
			return inst;
		}
		
		public static Blocks of(BlockState... target)
		{
			Blocks inst = (Blocks)RCDecayConditions.ADJACENT_TO.get();
			Builder builder = BlockPredicate.Builder.create();
			builder.addBlockState(target);
			inst.predicate = builder.build();
			return inst;
		}
		
		public static Blocks of(TagKey<Block> target)
		{
			Blocks inst = (Blocks)RCDecayConditions.ADJACENT_TO.get();
			Builder builder = BlockPredicate.Builder.create();
			builder.addBlockTag(target);
			inst.predicate = builder.build();
			return inst;
		}
		
		public static Blocks of(List<TagKey<Block>> target)
		{
			Blocks inst = (Blocks)RCDecayConditions.ADJACENT_TO.get();
			Builder builder = BlockPredicate.Builder.create();
			builder.addBlockTags(target);
			inst.predicate = builder.build();
			return inst;
		}
		
		public static Blocks of(BlockPredicate predicate)
		{
			Blocks inst = (Blocks)RCDecayConditions.ADJACENT_TO.get();
			inst.predicate = predicate;
			return inst;
		}
		
		protected JsonObject write(JsonObject obj)
		{
			obj = super.write(obj);
			obj.add("look_for", predicate.toJson());
			return obj;
		}
		
		protected void read(JsonObject obj)
		{
			super.read(obj);
			predicate = BlockPredicate.fromJson(obj.getAsJsonObject("look_for"));
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
		
		public static Supported create() { return (Supported)RCDecayConditions.SUPPORTED.get(); }
		
		public static Supported onFaces(Direction... facesIn)
		{
			Supported condition = (Supported)RCDecayConditions.SUPPORTED.get();
			condition.faces(facesIn);
			return condition;
		}
		
		protected boolean isMatch(BlockState state, BlockPos neighbour, Direction face, BlockPos pos, ServerWorld world)
		{
			return !state.isReplaceable() && state.isSideSolidFullSquare(world, neighbour, face);
		}
	}
	
	public static class OnGround extends DecayCondition
	{
		public OnGround(Identifier idIn)
		{
			super(idIn);
		}
		
		public boolean check(ServerWorld world, BlockPos pos, BlockState currentState)
		{
			BlockPos neighbour = pos.down();
			BlockState state = world.getBlockState(neighbour);
			return !state.isReplaceable() && state.isSideSolidFullSquare(world, neighbour, Direction.UP);
		}
	}
	
	public static class Unsupported extends Supported
	{
		public Unsupported(Identifier idIn)
		{
			super(idIn);
		}
		
		public static Unsupported create() { return (Unsupported)RCDecayConditions.UNSUPPORTED.get(); }
		
		protected boolean isMatch(BlockState state, BlockPos neighbour, Direction face, BlockPos pos, ServerWorld world)
		{
			return !super.isMatch(state, neighbour, face, pos, world);
		}
	}
	
	public static class Uncovered extends ConditionNeighbouring
	{
		public Uncovered(Identifier idIn)
		{
			super(idIn);
		}
		
		public static Uncovered face(Direction... facesIn)
		{
			Uncovered condition = (Uncovered)RCDecayConditions.UNCOVERED.get();
			EnumSet<Direction> faces = EnumSet.noneOf(Direction.class);
			faces.addAll(List.of(facesIn));
			condition.data = new NeighbourData(Optional.of(faces), condition.data.threshold());
			return condition;
		}
		
		protected boolean isMatch(BlockState state, BlockPos neighbour, Direction face, BlockPos pos, ServerWorld world)
		{
			return !state.isSideSolidFullSquare(world, pos, face.getOpposite());
		}
	}
}
