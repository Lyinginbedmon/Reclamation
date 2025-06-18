package com.lying.init;

import static com.lying.reference.Reference.ModInfo.prefix;

import java.util.function.Function;
import java.util.function.Supplier;

import com.lying.Reclamation;
import com.lying.reference.Reference;
import com.mojang.serialization.MapCodec;

import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleType;
import net.minecraft.particle.SimpleParticleType;
import net.minecraft.registry.RegistryKeys;

public class RCParticleTypes
{
	private static final DeferredRegister<ParticleType<?>> PARTICLES	= DeferredRegister.create(Reference.ModInfo.MOD_ID, RegistryKeys.PARTICLE_TYPE);
	private static int tally = 0;
	
	public static final RegistrySupplier<SimpleParticleType> FLY	= register("fly", false);
	
	public static final RegistrySupplier<SimpleParticleType> SHOCKWAVE	= register("shockwave", true);
	
	private static RegistrySupplier<SimpleParticleType> register(String nameIn, boolean alwaysShow)
	{
		tally++;
		return PARTICLES.register(prefix(nameIn), () -> new SimpleParticleType(alwaysShow));
	}
	
	@SuppressWarnings("unused")
	private static <T extends ParticleEffect> RegistrySupplier<ParticleType<T>> register(
			String nameIn, 
			boolean alwaysShow, 
			Function<ParticleType<T>, MapCodec<T>> codecGetter,
			Function<ParticleType<T>, PacketCodec<? super RegistryByteBuf, T>> packetCodecGetter,
			Supplier<T> type)
	{
		tally++;
		return PARTICLES.register(prefix(nameIn), () -> new ParticleType<T>(alwaysShow) 
		{
			public MapCodec<T> getCodec() { return codecGetter.apply(this); }
			
			public PacketCodec<? super RegistryByteBuf, T> getPacketCodec() { return packetCodecGetter.apply(this); }
		});
	}
	
	public static void init()
	{
		PARTICLES.register();
		Reclamation.LOGGER.info("# Initialised {} custom particles", tally);
	}
}
