package com.lying.block;

import java.util.function.Supplier;

import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.Property;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;

public class ScrapeableBlock extends Block
{
	private final Supplier<Block> previous;
	
	public ScrapeableBlock(Supplier<Block> prev, Settings settings)
	{
		super(settings);
		previous = prev;
	}
	
	public static boolean canScrape(ItemStack stack, Hand hand, boolean sneaking)
	{
		return !stack.isEmpty() && stack.isIn(ItemTags.AXES) && !sneaking;
	}
	
	protected ActionResult onUseWithItem(ItemStack stack, BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit)
	{
		if(canScrape(stack, hand, player.shouldCancelInteraction()))
		{
			if(player instanceof ServerPlayerEntity)
				Criteria.ITEM_USED_ON_BLOCK.trigger((ServerPlayerEntity)player, pos, stack);
			
			BlockState revert = previous.get().getDefaultState();
			StateManager<Block, BlockState> stateManager = state.getBlock().getStateManager();
			for(Property<?> property : stateManager.getProperties())
				revert = copyValue(property, state, revert);
			
			world.setBlockState(pos, revert, 11);
			world.emitGameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Emitter.of(player, revert));
			world.playSound(player, pos, SoundEvents.ITEM_AXE_SCRAPE, SoundCategory.BLOCKS, 1f, 1f);
			world.syncWorldEvent(player, 3005, pos, 0);
			if(player != null)
				stack.damage(1, player, LivingEntity.getSlotForHand(hand));
			return ActionResult.SUCCESS;
		}
		return super.onUseWithItem(stack, state, world, pos, player, hand, hit);
	}
	
	private static <T extends Comparable<T>> BlockState copyValue(Property<T> property, BlockState original, BlockState target)
	{
		return target.get(property) != null ? target.with(property, original.get(property)) : target;
	}
}
