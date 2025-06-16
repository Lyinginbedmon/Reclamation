package com.lying.particle;

import com.mojang.serialization.MapCodec;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleType;

public class BasicParticleType extends ParticleType<BasicParticleType> implements ParticleEffect
{
	private final MapCodec<BasicParticleType> codec = MapCodec.unit(this::getType);
	private final PacketCodec<RegistryByteBuf, BasicParticleType> packetCodec = PacketCodec.unit(this);
	
	public BasicParticleType(boolean alwaysShow)
	{
		super(alwaysShow);
	}

	public BasicParticleType getType()
	{
		return this;
	}
	
	public MapCodec<BasicParticleType> getCodec()
	{
		return this.codec;
	}
	
	public PacketCodec<RegistryByteBuf, BasicParticleType> getPacketCodec()
	{
		return this.packetCodec;
	}
}
