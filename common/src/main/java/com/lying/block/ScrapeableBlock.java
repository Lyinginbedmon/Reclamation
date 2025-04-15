package com.lying.block;

import java.util.Optional;
import java.util.function.Supplier;

import org.jetbrains.annotations.Nullable;

import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
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
	private final Supplier<Block> waxed;
	
	public ScrapeableBlock(Supplier<Block> prev, @Nullable Supplier<Block> wax, Settings settings)
	{
		super(settings);
		previous = prev;
		waxed = wax;
	}
	
	public ScrapeableBlock(Supplier<Block> prev, Settings settings)
	{
		this(prev, null, settings);
	}
	
	public static boolean canScrape(ItemStack stack, Hand hand, boolean sneaking)
	{
		return !stack.isEmpty() && stack.isIn(ItemTags.AXES) && !sneaking;
	}
	
	public static boolean canWax(ItemStack stack, Hand hand, boolean sneaking)
	{
		return !stack.isEmpty() && stack.isOf(Items.HONEYCOMB) && !sneaking;
	}
	
	public Optional<BlockState> revertState() { return previous != null && previous.get() != null ? Optional.of(previous.get().getDefaultState()) : Optional.empty(); }
	
	public Optional<BlockState> waxedState() { return waxed != null && waxed.get() != null ? Optional.of(waxed.get().getDefaultState()) : Optional.empty(); }
	
	protected ActionResult onUseWithItem(ItemStack stack, BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit)
	{
		if(canScrape(stack, hand, player.shouldCancelInteraction()))
		{
			Optional<BlockState> revertState = revertState();
			revertState.ifPresent(revert -> 
			{
				if(player instanceof ServerPlayerEntity)
					Criteria.ITEM_USED_ON_BLOCK.trigger((ServerPlayerEntity)player, pos, stack);
				
				replaceBlock(pos, world, state, previous.get().getDefaultState(), player, 3005);
				if(player != null)
					stack.damage(1, player, LivingEntity.getSlotForHand(hand));
			});
			if(revertState.isPresent())
				return ActionResult.SUCCESS;
		}
		else if(canWax(stack, hand, player.shouldCancelInteraction()))
		{
			Optional<BlockState> waxedState = waxedState();
			waxedState.ifPresent(waxed -> 
			{
				if(player instanceof ServerPlayerEntity)
					Criteria.ITEM_USED_ON_BLOCK.trigger((ServerPlayerEntity)player, pos, stack);
				
				replaceBlock(pos, world, state, waxed, player, 3003);
				stack.decrementUnlessCreative(1, player);
			});
			if(waxedState.isPresent())
				return ActionResult.SUCCESS;
		}
		return super.onUseWithItem(stack, state, world, pos, player, hand, hit);
	}
	
	private static void replaceBlock(BlockPos pos, World world, BlockState state, BlockState next, PlayerEntity player, int eventId)
	{
		StateManager<Block, BlockState> stateManager = state.getBlock().getStateManager();
		for(Property<?> property : stateManager.getProperties())
			next = copyValue(property, state, next);
		
		world.setBlockState(pos, next, 11);
		world.emitGameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Emitter.of(player, next));
		if(eventId == 3005)
			world.playSound(player, pos, SoundEvents.ITEM_AXE_SCRAPE, SoundCategory.BLOCKS, 1f, 1f);
		world.syncWorldEvent(player, eventId, pos, 0);
	}
	
	private static <T extends Comparable<T>> BlockState copyValue(Property<T> property, BlockState original, BlockState target)
	{
		return target.get(property) != null ? target.with(property, original.get(property)) : target;
	}
}
