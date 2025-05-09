package com.lying.block;

import java.util.function.Supplier;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.LanternBlock;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class DousedLanternBlock extends LanternBlock
{
	private final Supplier<Block> igniter;
	
	public DousedLanternBlock(Supplier<Block> lit, Settings settings)
	{
		super(settings);
		igniter = lit;
	}
	
	protected ActionResult onUseWithItem(ItemStack stack, BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit)
	{
		if(state.get(WATERLOGGED) || !stack.isOf(Items.FLINT_AND_STEEL) && !stack.isOf(Items.FIRE_CHARGE))
			return super.onUseWithItem(stack, state, world, pos, player, hand, hit);
		else
		{
			BlockState litState = igniter.get().getDefaultState()
					.with(LanternBlock.HANGING, state.get(LanternBlock.HANGING))
					.with(Properties.WATERLOGGED, state.get(Properties.WATERLOGGED));
			world.setBlockState(pos, litState, 11);
			Item item = stack.getItem();
			if(stack.isOf(Items.FLINT_AND_STEEL))
			{
				stack.damage(1, player, LivingEntity.getSlotForHand(hand));
				world.playSound(player, pos, SoundEvents.ITEM_FLINTANDSTEEL_USE, SoundCategory.BLOCKS, 1.0F, world.getRandom().nextFloat() * 0.4F + 0.8F);
			}
			else
			{
				stack.decrementUnlessCreative(1, player);
				world.playSound(player, pos, SoundEvents.ITEM_FIRECHARGE_USE, SoundCategory.BLOCKS, 1.0F, world.getRandom().nextFloat() * 0.4F + 0.8F);
			}
			
			player.incrementStat(Stats.USED.getOrCreateStat(item));
			return ActionResult.SUCCESS;
		}
	}
}
