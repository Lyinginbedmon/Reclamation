package com.lying.init;

import static com.lying.reference.Reference.ModInfo.prefix;

import com.lying.Reclamation;
import com.lying.reference.Reference;

import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.particle.ParticleType;
import net.minecraft.particle.SimpleParticleType;
import net.minecraft.registry.RegistryKeys;

public class RCParticleTypes
{
	private static final DeferredRegister<ParticleType<?>> PARTICLES	= DeferredRegister.create(Reference.ModInfo.MOD_ID, RegistryKeys.PARTICLE_TYPE);
	private static int tally = 0;
	
	public static final RegistrySupplier<SimpleParticleType> FLY	= register("fly", false);
	
	private static RegistrySupplier<SimpleParticleType> register(String nameIn, boolean alwaysShow)
	{
		tally++;
		return PARTICLES.register(prefix(nameIn), () -> new EasyParticleType(alwaysShow));
	}
	
	public static void init()
	{
		PARTICLES.register();
		Reclamation.LOGGER.info("# Initialised {} custom particles", tally);
	}
	
	private static class EasyParticleType extends SimpleParticleType
	{
		public EasyParticleType(boolean alwaysShow)
		{
			super(alwaysShow);
		}
	}
}
