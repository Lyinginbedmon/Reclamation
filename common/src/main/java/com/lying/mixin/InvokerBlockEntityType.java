package com.lying.mixin;

import java.util.Set;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import net.minecraft.block.Block;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.entity.BlockEntityType.BlockEntityFactory;

@Mixin(BlockEntityType.class)
public interface InvokerBlockEntityType
{
	@Invoker("<init>(Lnet/minecraft.block/entity/BlockEntityType/BlockEntityFactory;Ljava/util/Set;)V")
	public static <T extends BlockEntity> BlockEntityType<T> create(BlockEntityFactory<? extends T> factory, Set<Block> blocks) { return null; }
}
