package com.lying.item;

import com.lying.data.RCTags;
import com.lying.reference.Reference;

import dev.architectury.event.Event;
import dev.architectury.event.EventFactory;
import net.minecraft.block.Block;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.world.World;

public class RottenFruitItem extends BlockItem
{
	/** Called every tick that the player wears a rotten carved pumpkin */
	public static final Event<WearingRottenFruitTickEvent> WEARING_ROTTEN_FRUIT_TICK_EVENT_SERVER	= EventFactory.createLoop(WearingRottenFruitTickEvent.class);
	/** Called every tick that the player wears a rotten carved pumpkin */
	public static final Event<WearingRottenFruitTickEvent> WEARING_ROTTEN_FRUIT_TICK_EVENT_CLIENT	= EventFactory.createLoop(WearingRottenFruitTickEvent.class);
	private static final int EFFECT_RATE	= Reference.Values.TICKS_PER_SECOND * 5;
	
	public RottenFruitItem(Block block, Settings settings)
	{
		super(block, settings);
	}
	
	public static void onWearingRottenFruitTickEvent(PlayerEntity player, World world)
	{
		// Apply mild nausea effect randomly
		if(player.getRandom().nextInt(EFFECT_RATE) == 0)
			player.addStatusEffect(new StatusEffectInstance(StatusEffects.NAUSEA, EFFECT_RATE, 0, true, false, false));
	}
	
	/** Handles firing the appropriate tick event for wearing a rotten pumpkin */
	public static void fireWearingRottenFruitTickEvent(PlayerEntity player)
	{
		if(player.getEquippedStack(EquipmentSlot.HEAD).isIn(RCTags.ROTTEN_FRUIT))
			if(player.getWorld().isClient())
				RottenFruitItem.WEARING_ROTTEN_FRUIT_TICK_EVENT_CLIENT.invoker().onWearingTick(player, player.getWorld());
			else
				RottenFruitItem.WEARING_ROTTEN_FRUIT_TICK_EVENT_SERVER.invoker().onWearingTick(player, player.getWorld());
	}
	
	@FunctionalInterface
	public static interface WearingRottenFruitTickEvent
	{
		void onWearingTick(PlayerEntity player, World world);
	}
}
