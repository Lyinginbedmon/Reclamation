package com.lying.init;

import static com.lying.reference.Reference.ModInfo.prefix;

import com.lying.Reclamation;
import com.lying.reference.Reference;

import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.world.event.GameEvent;

public class RCGameEvents
{
	private static final DeferredRegister<GameEvent> EVENTS	= DeferredRegister.create(Reference.ModInfo.MOD_ID, RegistryKeys.GAME_EVENT);
	private static int tally = 0;
	
	public static final RegistrySupplier<GameEvent> GLASS_CRONCH	= register("glass_crunch");
	
	private static RegistrySupplier<GameEvent> register(String nameIn)
	{
		return register(nameIn, 16);
	}
	
	private static RegistrySupplier<GameEvent> register(String nameIn, int rangeIn)
	{
		tally++;
		return EVENTS.register(prefix(nameIn), () -> new GameEvent(rangeIn));
	}
	
	public static void init()
	{
		EVENTS.register();
		Reclamation.LOGGER.info("# Registered {} game events", tally);
	}
}
