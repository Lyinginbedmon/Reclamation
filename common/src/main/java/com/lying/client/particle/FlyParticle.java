package com.lying.client.particle;

import com.lying.Reclamation;
import com.lying.particle.BasicParticleType;

import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleFactory;
import net.minecraft.client.particle.ParticleTextureSheet;
import net.minecraft.client.particle.SpriteBillboardParticle;
import net.minecraft.client.particle.SpriteProvider;
import net.minecraft.client.world.ClientWorld;

public class FlyParticle extends SpriteBillboardParticle
{
	public FlyParticle(ClientWorld clientWorld, double x, double y, double z)
	{
		super(clientWorld, x, y, z, 0F, 0F, 0F);
		this.scale = 0.02F + clientWorld.getRandom().nextFloat() * 0.03F;
	}
	
	public ParticleTextureSheet getType()
	{
		return ParticleTextureSheet.PARTICLE_SHEET_OPAQUE;
	}
	
	public void tick()
	{
		super.tick();
		Reclamation.LOGGER.info("# Ticking fly particle");
	}

	public static class Factory implements ParticleFactory<BasicParticleType>
	{
		private final SpriteProvider sprites;
		
		public Factory(SpriteProvider spritesIn)
		{
			this.sprites = spritesIn;
		}
		
		public Particle createParticle(
				BasicParticleType parameters, 
				ClientWorld world, 
				double x, double y, double z,
				double velocityX, double velocityY, double velocityZ)
		{
			FlyParticle particle = new FlyParticle(world, x, y, z);
			particle.setSprite(sprites);
			return particle;
		}
		
	}
}
