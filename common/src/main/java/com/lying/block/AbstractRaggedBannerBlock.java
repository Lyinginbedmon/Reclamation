package com.lying.block;

import com.lying.block.entity.RaggedBannerBlockEntity;

import net.minecraft.block.AbstractBannerBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DyeColor;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldView;

public abstract class AbstractRaggedBannerBlock extends AbstractBannerBlock
{
	protected final DyeColor color;
	
	protected AbstractRaggedBannerBlock(DyeColor colorIn, Settings settings)
	{
		super(colorIn, settings);
		color = colorIn;
	}
	
	public DyeColor color() { return color; }
	
	public BlockEntity createBlockEntity(BlockPos pos, BlockState state)
	{
		return new RaggedBannerBlockEntity(pos, state, this.color);
	}
	
	protected ItemStack getPickStack(WorldView world, BlockPos pos, BlockState state, boolean includeData)
	{
		return world.getBlockEntity(pos) instanceof RaggedBannerBlockEntity bannerBlockEntity
			? bannerBlockEntity.getPickStack()
			: super.getPickStack(world, pos, state, includeData);
	}
}
