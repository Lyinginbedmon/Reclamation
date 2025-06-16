package com.lying.init;

import static com.lying.reference.Reference.ModInfo.prefix;

import java.util.function.Supplier;

import com.lying.Reclamation;
import com.lying.reference.Reference;

import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Identifier;

public class RCSoundEvents
{
	public static final DeferredRegister<SoundEvent> SOUND_EVENTS 	= DeferredRegister.create(Reference.ModInfo.MOD_ID, RegistryKeys.SOUND_EVENT);
	private static int tally;
	
	public static final RegistrySupplier<SoundEvent> WITHERING_DUST		= register(prefix("withering_dust"));
	public static final RegistrySupplier<SoundEvent> RUBBLE_SHIFTING	= register(prefix("rubble_shifting"));
	public static final RegistrySupplier<SoundEvent> ROTTEN_FRUIT_BREAK	= register(prefix("rotten_fruit_break"));
	
	public static BlockSoundGroup	ROTTEN_FRUIT_SOUNDS	= new SupplierSoundGroup(
			1F,
			1F,
			RCSoundEvents.ROTTEN_FRUIT_BREAK,
			() -> SoundEvents.BLOCK_HONEY_BLOCK_STEP,
			() -> SoundEvents.BLOCK_HONEY_BLOCK_PLACE,
			() -> SoundEvents.BLOCK_HONEY_BLOCK_HIT,
			() -> SoundEvents.BLOCK_HONEY_BLOCK_FALL
		);
	
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
	
	private static class SupplierSoundGroup extends BlockSoundGroup
	{
		private final Supplier<SoundEvent> breakSound;
		private final Supplier<SoundEvent> stepSound;
		private final Supplier<SoundEvent> placeSound;
		private final Supplier<SoundEvent> hitSound;
		private final Supplier<SoundEvent> fallSound;
		
		public SupplierSoundGroup(
				float volume, 
				float pitch, 
				Supplier<SoundEvent> breakSound, 
				Supplier<SoundEvent> stepSound,
				Supplier<SoundEvent> placeSound, 
				Supplier<SoundEvent> hitSound, 
				Supplier<SoundEvent> fallSound)
		{
			super(volume, pitch, null, null, null, null, null);
			this.breakSound = breakSound;
			this.stepSound = stepSound;
			this.placeSound = placeSound;
			this.hitSound = hitSound;
			this.fallSound = fallSound;
		}
		
		public SoundEvent getBreakSound() { return this.breakSound.get(); }
		
		public SoundEvent getStepSound() { return this.stepSound.get(); }
		
		public SoundEvent getPlaceSound() { return this.placeSound.get(); }
		
		public SoundEvent getHitSound() { return this.hitSound.get(); }
		
		public SoundEvent getFallSound() { return this.fallSound.get(); }
	}
}
