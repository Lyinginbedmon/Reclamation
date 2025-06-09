package com.lying.fabric.data;

import java.util.concurrent.CompletableFuture;

import com.lying.data.RCTags;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.minecraft.data.tag.TagProvider;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.RegistryWrapper.WrapperLookup;
import net.minecraft.registry.tag.TagKey;

public class RCItemTagsProvider extends TagProvider<Item>
{
	public RCItemTagsProvider(FabricDataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> completableFuture)
	{
		super(output, RegistryKeys.ITEM, completableFuture);
	}
	
	protected void configure(WrapperLookup wrapperLookup)
	{
		registerToTag(RCTags.IGNITER_ITEMS, 
				Items.FLINT_AND_STEEL, 
				Items.FIRE_CHARGE);
	}
	
	@SuppressWarnings("deprecation")
	private void registerToTag(TagKey<Item> tag, Item... types)
	{
		ProvidedTagBuilder<Item> builder = getOrCreateTagBuilder(tag);
		for(Item type : types)
			builder.add(type.getRegistryEntry().registryKey());
	}
}
