package com.lying.decay.conditions;

import java.util.List;

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
}