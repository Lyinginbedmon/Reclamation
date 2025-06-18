package com.lying.block;

import com.lying.init.RCParticleTypes;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.state.StateManager;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;

public class RottenFruitBlock extends Block implements IDeActivatable
{
	public RottenFruitBlock(Settings settings)
	{
		super(settings);
		setDefaultState(getDefaultState().with(INERT, false));
	}
	
	protected void appendProperties(StateManager.Builder<Block, BlockState> builder)
	{
		builder.add(INERT);
	}
	
	public void randomDisplayTick(BlockState state, World world, BlockPos pos, Random random)
	{
		if(random.nextInt(3) == 0)
			for(int i=3; i>0; i--)
				world.addParticle(RCParticleTypes.FLY.get(), pos.getX() + random.nextDouble(), pos.getY() + random.nextDouble(), pos.getZ() + random.nextDouble(), 0, 0, 0);
	}
	
	public void onLandedUpon(World world, BlockState state, BlockPos pos, Entity entity, float fallDistance)
	{
		if(fallDistance > 2F && !world.isClient())
		{
			entity.handleFallDamage(fallDistance, 0.2F, world.getDamageSources().fall());
			if(!isInert(state))
				world.breakBlock(pos, true);
			return;
		}
		super.onLandedUpon(world, state, pos, entity, fallDistance);
	}
	
	public void onSteppedOn(World world, BlockPos pos, BlockState state, Entity entity)
	{
		if(!world.isClient() && !isInert(state))
			if(!entity.bypassesSteppingEffects() && world.random.nextInt(entity.isSprinting() ? 6 : 24) == 0)
			{
				world.breakBlock(pos, true);
				return;
			}
		
		super.onSteppedOn(world, pos, state, entity);
	}
}
