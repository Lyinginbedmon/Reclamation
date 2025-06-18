package com.lying.client.particle;

import com.lying.reference.Reference;

import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleFactory;
import net.minecraft.client.particle.ParticleTextureSheet;
import net.minecraft.client.particle.SpriteBillboardParticle;
import net.minecraft.client.particle.SpriteProvider;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particle.SimpleParticleType;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;

public class FlyParticle extends SpriteBillboardParticle
{
	private static final int FREQ_OF_CHANGE	= 1;
	private static final double MAX_SPEED = 0.03D;
	private static final double RATE_OF_CHANGE = 0.01D;
	
	public FlyParticle(ClientWorld clientWorld, double x, double y, double z)
	{
		super(clientWorld, x, y, z, 0F, 0F, 0F);
		this.velocityX = this.velocityZ = 0D;
		Random rand = clientWorld.getRandom();
		this.velocityY = rand.nextDouble() * MAX_SPEED * 0.1D;
		randomlyOffsetVelocity();
		
		this.maxAge = rand.nextBetween(1 * Reference.Values.TICKS_PER_SECOND, 3 * Reference.Values.TICKS_PER_SECOND);
		this.scale = 0.02F + rand.nextFloat() * 0.03F;
	}
	
	public ParticleTextureSheet getType()
	{
		return ParticleTextureSheet.PARTICLE_SHEET_OPAQUE;
	}
	
	public void tick()
	{
		super.tick();
		if(this.age%FREQ_OF_CHANGE == 0)
			randomlyOffsetVelocity();
	}
	
	private void randomlyOffsetVelocity()
	{
		Random rand = this.world.getRandom();
		
		this.velocityX += getRandomOffset(rand, RATE_OF_CHANGE * 0.3F);
		this.velocityY += getRandomOffset(rand, RATE_OF_CHANGE);
		this.velocityZ += getRandomOffset(rand, RATE_OF_CHANGE * 0.3F);
		
		Vec3d vel = new Vec3d(this.velocityX, this.velocityY, this.velocityZ).normalize().multiply(MAX_SPEED);
		this.velocityX = vel.getX();
		this.velocityY = vel.getY();
		this.velocityZ = vel.getZ();
	}
	
	private static double getRandomOffset(Random rand, double rate)
	{
		return rate * (rand.nextDouble() - 0.5D) / 0.5D;
	}
	
	public static class Factory implements ParticleFactory<SimpleParticleType>
	{
		private final SpriteProvider sprites;
		
		public Factory(SpriteProvider spritesIn)
		{
			this.sprites = spritesIn;
		}
		
		public Particle createParticle(
				SimpleParticleType parameters, 
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
