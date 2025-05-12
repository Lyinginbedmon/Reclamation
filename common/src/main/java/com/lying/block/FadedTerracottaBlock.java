package com.lying.block;

import com.lying.init.RCBlocks;

import net.minecraft.block.BlockState;
import net.minecraft.block.GlazedTerracottaBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.DyeItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldEvents;
import net.minecraft.world.event.GameEvent;

public class FadedTerracottaBlock extends GlazedTerracottaBlock
{
	private final DyeColor color;
	
	public FadedTerracottaBlock(DyeColor colorIn, Settings settings)
	{
		super(settings);
		color = colorIn;
	}
	
	protected ActionResult onUseWithItem(ItemStack stack, BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit)
	{
		if(!player.shouldCancelInteraction() && stack.isOf(DyeItem.byColor(color)))
		{
			if(!world.isClient())
			{
				if(!player.isCreative() && world.getRandom().nextInt(8) == 0)
					stack.decrement(1);
				
				BlockState glazed = RCBlocks.DYE_TO_TERRACOTTA.get(color).glazed().get().getStateWithProperties(state);
				world.setBlockState(pos, glazed, 11);
				world.emitGameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Emitter.of(player, glazed));
				world.syncWorldEvent(player, WorldEvents.WAX_REMOVED, pos, 0);	// TODO Replace with properietary world event
				return ActionResult.SUCCESS.withNewHandStack(stack);
			}
			return ActionResult.SUCCESS;
		}
		return super.onUseWithItem(stack, state, world, pos, player, hand, hit);
	}
}
