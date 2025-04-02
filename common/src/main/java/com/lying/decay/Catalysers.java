package com.lying.decay;

import java.util.List;
import java.util.Optional;

import com.google.common.collect.Lists;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class Catalysers
{
	public static final Codec<Catalysers> CODEC	= RecordCodecBuilder.create(instance -> instance.group(
			Codec.INT.fieldOf("cap").forGetter(c -> c.saturation),
			Codec.INT.fieldOf("range").forGetter(c -> c.scanRange),
			Registries.BLOCK.getCodec().listOf().optionalFieldOf("blocks").forGetter(c -> c.blocks),
			BlockState.CODEC.listOf().optionalFieldOf("block_states").forGetter(c -> c.states),
			TagKey.codec(RegistryKeys.BLOCK).listOf().optionalFieldOf("block_tags").forGetter(c -> c.tags)
			)
			.apply(instance, (c,r,b,s,t) -> new Catalysers(c, r, b, s, t)));
	
	private final int saturation, scanRange;
	private final Optional<List<Block>> blocks;
	private final Optional<List<BlockState>> states;
	private final Optional<List<TagKey<Block>>> tags;
	
	protected Catalysers(int saturation, int scanRange, Optional<List<Block>> blocks, Optional<List<BlockState>> states, Optional<List<TagKey<Block>>> tags)
	{
		this.saturation = saturation;
		this.scanRange = scanRange;
		this.blocks = blocks;
		this.states = states;
		this.tags = tags;
	}
	
	public float calculateMultiplier(World world, BlockPos pos)
	{
		float tally = 0;
		for (BlockPos offset : BlockPos.iterateOutwards(pos, scanRange, scanRange, scanRange))
		{
			int l = offset.getManhattanDistance(pos);
			if (l > scanRange)
				break;
			
			if(!offset.equals(pos) && isCatalyst(world.getBlockState(offset)))
				tally++;
		}
		return Math.max(0.1F, (float)Math.pow(tally / saturation, 0.2F));
	}
	
	private boolean isCatalyst(BlockState state)
	{
		return 
				blocks.orElse(List.of()).stream().anyMatch(b -> state.isOf(b)) || 
				states.orElse(List.of()).stream().anyMatch(s -> s.equals(state)) ||
				tags.orElse(List.of()).stream().anyMatch(t -> state.isIn(t));
	}
	
	public static class Builder
	{
		private int saturation = 9, scanRange = 4;
		private Optional<List<Block>> blocks = Optional.empty();
		private Optional<List<BlockState>> states = Optional.empty();
		private Optional<List<TagKey<Block>>> tags = Optional.empty();
		
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
		
		public Builder blocks(Block... blocksIn)
		{
			if(blocksIn == null || blocksIn.length == 0)
				blocks = Optional.empty();
			else
			{
				List<Block> blockSet = Lists.newArrayList();
				for(Block block : blocksIn)
					blockSet.add(block);
				blocks = Optional.of(blockSet);
			}
			return this;
		}
		
		public Builder states(BlockState... statesIn)
		{
			if(statesIn == null || statesIn.length == 0)
				states = Optional.empty();
			else
			{
				List<BlockState> blockStateSet = Lists.newArrayList();
				for(BlockState block : statesIn)
					blockStateSet.add(block);
				states = Optional.of(blockStateSet);
			}
			return this;
		}
		
		@SuppressWarnings("unchecked")
		public Builder states(TagKey<Block>... tagsIn)
		{
			if(tagsIn == null || tagsIn.length == 0)
				tags = Optional.empty();
			else
			{
				List<TagKey<Block>> tagSet = Lists.newArrayList();
				for(TagKey<Block> block : tagsIn)
					tagSet.add(block);
				tags = Optional.of(tagSet);
			}
			return this;
		}
		
		
		
		public Catalysers build()
		{
			return new Catalysers(saturation, scanRange, blocks, states, tags);
		}
	}
}