package com.lying.decay.functions;

import com.google.gson.JsonObject;
import com.lying.decay.context.DecayContext;
import com.lying.utility.BlockProvider;

import net.minecraft.block.BlockState;
import net.minecraft.entity.FallingBlockEntity;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.util.Identifier;

public abstract class FunctionFallingBlock extends DecayFunction
{
	protected FunctionFallingBlock(Identifier idIn)
	{
		super(idIn);
	}
	
	@SuppressWarnings("deprecation")
	protected static boolean canFallThrough(BlockState state)
	{
		return state.isAir() || state.isIn(BlockTags.FIRE) || state.isLiquid() || state.isReplaceable();
	}
	
	public static class Fall extends FunctionFallingBlock
	{
		public Fall(Identifier idIn)
		{
			super(idIn);
		}
		
		protected void applyTo(DecayContext context)
		{
			if(!canFallThrough(context.getBlockState(context.currentPos().down())))
				return;
			
			context.execute((pos, world) -> FallingBlockEntity.spawnFromBlock(world, pos, context.currentState()));
			context.breakBlock();
			context.preventFurtherChanges();
		}
	}
	
	public static class Drop extends FunctionFallingBlock
	{
		private BlockProvider provider = BlockProvider.create();
		
		public Drop(Identifier idIn)
		{
			super(idIn);
		}
		
		protected void applyTo(DecayContext context)
		{
			if(canFallThrough(context.getBlockState(context.currentPos().down())))
				provider.getRandom(context.random).ifPresent(state -> context.execute((pos, world) -> FallingBlockEntity.spawnFromBlock(world, pos.down(), state)));
		}
		
		protected JsonObject write(JsonObject obj)
		{
			if(!provider.isEmpty())
				obj.add("dropped_block", provider.toJson());
			return obj;
		}
		
		protected void read(JsonObject obj)
		{
			if(obj.has("dropped_block"))
				provider = BlockProvider.fromJson(obj.get("dropped_block"));
		}
	}
}
