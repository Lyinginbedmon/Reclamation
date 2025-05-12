package com.lying.decay.conditions;

import java.util.EnumSet;
import java.util.List;
import java.util.Optional;

import com.google.common.collect.Lists;
import com.google.gson.JsonObject;
import com.lying.init.RCDecayConditions;
import com.lying.utility.BlockPredicate;
import com.lying.utility.BlockPredicate.Builder;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SideShapeType;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

public class ConditionIsBlock extends DecayCondition
{
	protected BlockPredicate predicate = BlockPredicate.Builder.create().build();
	
	public ConditionIsBlock(Identifier idIn)
	{
		super(idIn);
	}
	
	public boolean check(ServerWorld world, BlockPos pos, BlockState currentState)
	{
		return predicate.test(currentState);
	}
	
	public static DecayCondition of(Block... target)
	{
		ConditionIsBlock inst = (ConditionIsBlock)RCDecayConditions.IS_BLOCK.get();
		Builder builder = BlockPredicate.Builder.create();
		builder.addBlock(target);
		inst.predicate = builder.build();
		return inst;
	}
	
	public static DecayCondition of(BlockState... target)
	{
		ConditionIsBlock inst = (ConditionIsBlock)RCDecayConditions.IS_BLOCK.get();
		Builder builder = BlockPredicate.Builder.create();
		builder.addBlockState(target);
		inst.predicate = builder.build();
		return inst;
	}
	
	public static DecayCondition of(TagKey<Block> target)
	{
		ConditionIsBlock inst = (ConditionIsBlock)RCDecayConditions.IS_BLOCK.get();
		Builder builder = BlockPredicate.Builder.create();
		builder.addBlockTag(target);
		inst.predicate = builder.build();
		return inst;
	}
	
	public static DecayCondition of(List<TagKey<Block>> target)
	{
		ConditionIsBlock inst = (ConditionIsBlock)RCDecayConditions.IS_BLOCK.get();
		Builder builder = BlockPredicate.Builder.create();
		builder.addBlockTags(target);
		inst.predicate = builder.build();
		return inst;
	}
	
	public static DecayCondition of(BlockPredicate predicate)
	{
		ConditionIsBlock inst = (ConditionIsBlock)RCDecayConditions.IS_BLOCK.get();
		inst.predicate = predicate;
		return inst;
	}
	
	protected JsonObject write(JsonObject obj)
	{
		return predicate.toJson().getAsJsonObject();
	}
	
	protected void read(JsonObject obj)
	{
		predicate = BlockPredicate.fromJson(obj);
	}
	
	public static class Air extends DecayCondition
	{
		public Air(Identifier idIn)
		{
			super(idIn);
		}
		
		protected boolean check(ServerWorld world, BlockPos pos, BlockState currentState)
		{
			return currentState.isAir();
		}
	}
	
	public static class Replaceable extends DecayCondition
	{
		public Replaceable(Identifier idIn)
		{
			super(idIn);
		}
		
		protected boolean check(ServerWorld world, BlockPos pos, BlockState currentState)
		{
			return currentState.isReplaceable();
		}
	}
	
	public static class Solid extends DecayCondition
	{
		private static final Codec<List<Direction>> FACE_CODEC = Direction.CODEC.listOf();
		
		private Optional<EnumSet<Direction>> faces = Optional.empty();
		
		public Solid(Identifier idIn)
		{
			super(idIn);
		}
		
		public static Solid onFace(Direction... facesIn)
		{
			Solid condition = (Solid)RCDecayConditions.IS_SOLID.get();

			EnumSet<Direction> set = EnumSet.noneOf(Direction.class);
			for(Direction face : facesIn)
				set.add(face);
			
			condition.faces = Optional.of(set);
			return condition;
		}
		
		protected boolean check(ServerWorld world, BlockPos pos, BlockState currentState)
		{
			if(faces.isEmpty())
				return currentState.isFullCube(world, pos);
			
			for(Direction face : faces.get())
				if(!currentState.isSideSolid(world, pos, face, SideShapeType.FULL))
					return false;
			return true;
		}
		
		protected JsonObject write(JsonObject obj)
		{
			if(faces.isPresent())
			{
				List<Direction> list = Lists.newArrayList();
				faces.get().forEach(list::add);
				obj.add("Faces", FACE_CODEC.encodeStart(JsonOps.INSTANCE, list).getOrThrow());
			}
			return obj;
		}
		
		protected void read(JsonObject obj)
		{
			if(obj.has("Faces"))
			{
				EnumSet<Direction> set = EnumSet.noneOf(Direction.class);
				FACE_CODEC.parse(JsonOps.INSTANCE, obj.get("Faces")).getOrThrow().forEach(set::add);
				faces = Optional.of(set);
			}
		}
	}
}