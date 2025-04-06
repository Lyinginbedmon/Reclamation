package com.lying.decay;

import com.lying.utility.BlockPredicate;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class Catalysers
{
	public static final Codec<Catalysers> CODEC	= RecordCodecBuilder.create(instance -> instance.group(
			Codec.INT.fieldOf("cap").forGetter(c -> c.saturation),
			Codec.INT.fieldOf("range").forGetter(c -> c.scanRange),
			BlockPredicate.CODEC.fieldOf("target").forGetter(c -> c.predicate)
			)
			.apply(instance, (c,r,b) -> new Catalysers(c, r, b)));
	
	private final int saturation, scanRange;
	private final BlockPredicate predicate;
	
	protected Catalysers(int saturation, int scanRange, BlockPredicate predicate)
	{
		this.saturation = saturation;
		this.scanRange = scanRange;
		this.predicate = predicate;
	}
	
	public float calculateMultiplier(World world, BlockPos pos)
	{
		return Math.max(0.1F, (float)Math.pow(getTally(world, pos) / saturation, 0.2F));
	}
	
	private float getTally(World world, BlockPos pos)
	{
		float tally = 0;
		for(BlockPos offset : BlockPos.iterateOutwards(pos, scanRange, scanRange, scanRange))
		{
			if(offset.equals(pos))
				continue;
			
			if(predicate.test(world.getBlockState(offset)) && ++tally >= saturation)
				return tally;
		}
		return tally;
	}
	
	public static class Builder
	{
		private int saturation = 9, scanRange = 4;
		private BlockPredicate.Builder predicate = BlockPredicate.Builder.create();
		
		protected Builder() { }
		
		public static Builder create() { return new Builder(); }
		
		public Builder blockCap(int value)
		{
			saturation = Math.max(1, value);
			return this;
		}
		
		public Builder searchRange(int range)
		{
			scanRange = Math.max(1, range);
			return this;
		}
		
		public Builder predicate(BlockPredicate.Builder predicateIn)
		{
			predicate = predicateIn;
			return this;
		}
		
		public Builder blocks(Block... blocksIn)
		{
			predicate.addBlock(blocksIn);
			return this;
		}
		
		public Builder states(BlockState... statesIn)
		{
			predicate.addBlockState(statesIn);
			return this;
		}
		
		@SuppressWarnings("unchecked")
		public Builder tags(TagKey<Block>... tagsIn)
		{
			predicate.addBlockTag(tagsIn);
			return this;
		}
		
		public Catalysers build()
		{
			return new Catalysers(saturation, scanRange, predicate.build());
		}
	}
}