package com.lying.fabric.data;

import java.util.concurrent.CompletableFuture;

import com.lying.init.RCGameEvents;

import dev.architectury.registry.registries.RegistrySupplier;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.minecraft.data.tag.TagProvider;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.RegistryWrapper.WrapperLookup;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.GameEventTags;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.world.event.GameEvent;

public class RCGameEventTagsProvider extends TagProvider<GameEvent>
{
	public RCGameEventTagsProvider(FabricDataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> completableFuture)
	{
		super(output, RegistryKeys.GAME_EVENT, completableFuture);
	}
	
	@SuppressWarnings("unchecked")
	protected void configure(WrapperLookup art)
	{
		register(GameEventTags.VIBRATIONS, RCGameEvents.GLASS_CRONCH);
	}
	
	@SuppressWarnings("unchecked")
	private void register(TagKey<GameEvent> tagIn, RegistrySupplier<GameEvent>... events)
	{
		ProvidedTagBuilder<GameEvent> tag = getOrCreateTagBuilder(tagIn);
		for(RegistrySupplier<GameEvent> event : events)
			tag.add(((RegistryEntry<GameEvent>)event).getKey().get());
	}
}
