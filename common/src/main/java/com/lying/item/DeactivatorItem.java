package com.lying.item;

import com.lying.block.IDeActivatable;

import net.minecraft.block.BlockState;
import net.minecraft.item.Item;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class DeactivatorItem extends Item
{
	public DeactivatorItem(Settings settings)
	{
		super(settings);
	}
	
	public ActionResult useOnBlock(ItemUsageContext context)
	{
		if(!context.shouldCancelInteraction())
		{
			World world = context.getWorld();
			if(!world.isClient())
			{
				ServerWorld serverWorld = (ServerWorld)world;
				BlockPos position = context.getBlockPos();
				BlockState state = serverWorld.getBlockState(position);
				
				if(state.getBlock() instanceof IDeActivatable)
				{
					IDeActivatable activator = (IDeActivatable)state.getBlock();
					serverWorld.setBlockState(position, activator.toggleInert(state));
					serverWorld.playSound(null, position, activator.isInert(state) ? SoundEvents.BLOCK_STONE_BUTTON_CLICK_OFF : SoundEvents.BLOCK_STONE_BUTTON_CLICK_ON, SoundCategory.PLAYERS);
					return ActionResult.SUCCESS;
				}
			}
			return ActionResult.SUCCESS;
		}
		
		return super.useOnBlock(context);
	}
}
