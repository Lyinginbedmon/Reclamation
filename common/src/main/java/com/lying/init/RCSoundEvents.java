package com.lying.init;

import static com.lying.reference.Reference.ModInfo.prefix;

import com.lying.Reclamation;
import com.lying.reference.Reference;

import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;

public class RCSoundEvents
{
	public static final DeferredRegister<SoundEvent> SOUND_EVENTS 	= DeferredRegister.create(Reference.ModInfo.MOD_ID, RegistryKeys.SOUND_EVENT);
	private static int tally;
	
	private static final Identifier ID_WITHERING_DUST	= prefix("withering_dust");
	public static final RegistrySupplier<SoundEvent> WITHERING_DUST	= register(ID_WITHERING_DUST);
	
	private static RegistrySupplier<SoundEvent> register(Identifier name)
	{
		tally++;
		return SOUND_EVENTS.register(name, () -> SoundEvent.of(name));
	}
	
	public static void init()
	{
		SOUND_EVENTS.register();
		Reclamation.LOGGER.info("# Registered {} sound events", tally);
	}
}
