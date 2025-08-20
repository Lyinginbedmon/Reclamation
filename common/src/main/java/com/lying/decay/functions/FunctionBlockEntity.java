package com.lying.decay.functions;

import com.google.gson.JsonObject;
import com.lying.decay.context.DecayContext;
import com.mojang.serialization.JsonOps;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;

public abstract class FunctionBlockEntity extends DecayFunction
{
	protected FunctionBlockEntity(Identifier idIn)
	{
		super(idIn);
	}
	
	protected void applyTo(DecayContext context)
	{
		if(context.world.isEmpty()) return;
		ServerWorld world = context.world.get();
		BlockEntity entity = world.getBlockEntity(context.currentPos());
		if(entity != null)
			applyTo(entity, world, context);
	}
	
	protected abstract void applyTo(BlockEntity entity, ServerWorld world, DecayContext context);
	
	public static class Copy extends FunctionBlockEntity
	{
		public Copy(Identifier idIn)
		{
			super(idIn);
		}
		
		protected void applyTo(BlockEntity entity, ServerWorld world, DecayContext context)
		{
			entity.read(context.originalNBT, world.getRegistryManager());
		}
	}
	
	public static class Merge extends FunctionBlockEntity
	{
		private NbtCompound data = new NbtCompound();
		
		public Merge(Identifier idIn)
		{
			super(idIn);
		}
		
		protected void applyTo(BlockEntity entity, ServerWorld world, DecayContext context)
		{
			NbtCompound entityData = entity.createNbt(world.getRegistryManager()).copyFrom(data);
			entity.read(entityData, world.getRegistryManager());
		}
		
		protected JsonObject write(JsonObject obj)
		{
			if(data.isEmpty())
				return obj;
			
			obj.add("nbt", NbtCompound.CODEC.encodeStart(JsonOps.INSTANCE, data).getOrThrow());
			return obj;
		}
		
		protected void read(JsonObject obj)
		{
			data = obj.has("nbt") ? NbtCompound.CODEC.parse(JsonOps.INSTANCE, obj.get("nbt")).getOrThrow() : new NbtCompound();
		}
	}
}
