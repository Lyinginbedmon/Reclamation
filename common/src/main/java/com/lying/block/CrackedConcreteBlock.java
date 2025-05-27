package com.lying.block;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.FallingBlock;
import net.minecraft.entity.Entity;
import net.minecraft.particle.BlockStateParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.particle.ParticleUtil;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.IntProperty;
import net.minecraft.util.DyeColor;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;

public class CrackedConcreteBlock extends Block
{
	public static final IntProperty CRACKS = IntProperty.of("cracks", 1, 4);
	
	public static final Map<DyeColor, Block> DYE_TO_BLOCK = new HashMap<>();
	
	private final DyeColor color;
	
	public CrackedConcreteBlock(DyeColor colorIn, Settings settings)
	{
		super(settings);
		color = colorIn;
		setDefaultState(getDefaultState().with(CRACKS, 1));
		
		DYE_TO_BLOCK.put(colorIn, this);
	}
	
	public static Block byColor(DyeColor color) { return DYE_TO_BLOCK.get(color); }
	
	public DyeColor color() { return color; }
	
	protected void appendProperties(StateManager.Builder<Block, BlockState> builder)
	{
		builder.add(CRACKS);
	}
	
	public void randomDisplayTick(BlockState state, World world, BlockPos pos, Random random)
	{
		if(random.nextInt(16) > 0) return;
		
		BlockPos blockPos = pos.down();
		if(FallingBlock.canFallThrough(world.getBlockState(blockPos)))
			ParticleUtil.spawnParticle(world, pos, random, new BlockStateParticleEffect(ParticleTypes.BLOCK_CRUMBLE, state));
	}
	
	public void onLandedUpon(World world, BlockState state, BlockPos pos, Entity entity, float fallDistance)
	{
		super.onLandedUpon(world, state, pos, entity, fallDistance);
		int cracks = state.get(CRACKS);
		if(cracks < 4 && fallDistance > 10F && world.getRandom().nextInt(4) == 0)
			world.setBlockState(pos, state.with(CRACKS, ++cracks), 2);
	}
}
