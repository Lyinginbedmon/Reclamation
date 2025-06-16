package com.lying.fabric.data;

import java.util.concurrent.CompletableFuture;

import com.lying.data.RCTags;
import com.lying.init.RCItems;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider.ItemTagProvider;
import net.minecraft.item.Items;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.RegistryWrapper.WrapperLookup;
import net.minecraft.registry.tag.ItemTags;

public class RCItemTagsProvider extends ItemTagProvider
{
	public RCItemTagsProvider(FabricDataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> completableFuture)
	{
		super(output, completableFuture);
	}
	
	@SuppressWarnings("deprecation")
	protected void configure(WrapperLookup wrapperLookup)
	{
		getOrCreateTagBuilder(RCTags.IGNITER_ITEMS).add(
				Items.FLINT_AND_STEEL.getRegistryEntry().registryKey(),
				Items.FIRE_CHARGE.getRegistryEntry().registryKey());
		
		getOrCreateTagBuilder(RCTags.ROTTEN_PUMPKIN).add(
				RCItems.ROTTEN_PUMPKIN.get().getRegistryEntry().registryKey(),
				RCItems.ROTTEN_CARVED_PUMPKIN.get().getRegistryEntry().registryKey(),
				RCItems.ROTTEN_JACK_O_LANTERN.get().getRegistryEntry().registryKey());
		
		getOrCreateTagBuilder(RCTags.ROTTEN_FRUIT).addTag(RCTags.ROTTEN_PUMPKIN).add(RCItems.ROTTEN_MELON.get().getRegistryEntry().registryKey());
		
		getOrCreateTagBuilder(ItemTags.EQUIPPABLE_ENCHANTABLE).add(RCItems.ROTTEN_CARVED_PUMPKIN.get().getRegistryEntry().registryKey());
		getOrCreateTagBuilder(ItemTags.EQUIPPABLE_ENCHANTABLE).add(RCItems.ROTTEN_CARVED_PUMPKIN.get().getRegistryEntry().registryKey());
		getOrCreateTagBuilder(ItemTags.VANISHING_ENCHANTABLE).add(RCItems.ROTTEN_CARVED_PUMPKIN.get().getRegistryEntry().registryKey());
		getOrCreateTagBuilder(ItemTags.MAP_INVISIBILITY_EQUIPMENT).add(RCItems.ROTTEN_CARVED_PUMPKIN.get().getRegistryEntry().registryKey());
		getOrCreateTagBuilder(ItemTags.GAZE_DISGUISE_EQUIPMENT).add(RCItems.ROTTEN_CARVED_PUMPKIN.get().getRegistryEntry().registryKey());
	}
	
	
}
