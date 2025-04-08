package com.lying.utility;

import java.util.Optional;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;

public class TallyGetter
{
	public static final Codec<TallyGetter> CODEC	= RecordCodecBuilder.create(instance -> instance.group(
			Codec.INT.optionalFieldOf("capacity").forGetter(c -> c.capacity),
			Codec.INT.optionalFieldOf("range").forGetter(c -> c.scanRange),
			Vec3i.CODEC.optionalFieldOf("area").forGetter(c -> c.scanVec),
			BlockPredicate.CODEC.fieldOf("target").forGetter(c -> c.predicate)
			)
			.apply(instance, (a, b, c, d) -> new TallyGetter(a, b, c, d)));
	
	private final Optional<Vec3i> scanVec;
	private final Optional<Integer> capacity, scanRange;
	private final BlockPredicate predicate;
	
	public TallyGetter(Optional<Integer> cap, Optional<Integer> range, Optional<Vec3i> vec, BlockPredicate predicateIn)
	{
		this.capacity = cap;
		this.scanRange = range;
		this.scanVec = vec;
		this.predicate = predicateIn;
	}
	
	public int capacity() { return capacity.orElse(9); }
	
	public float getTally(World world, BlockPos pos)
	{
		float tally = 0;
		Iterable<BlockPos> iterable;
		if(scanVec.isPresent())
			iterable = BlockPos.iterateOutwards(pos, scanVec.get().getX(), scanVec.get().getY(), scanVec.get().getZ());
		else
		{
			int range = scanRange.orElse(4);
			iterable = BlockPos.iterateOutwards(pos, range, range, range);
		}
		for(BlockPos offset : iterable)
		{
			if(offset.equals(pos))
				continue;
			
			if(predicate.test(world.getBlockState(offset)) && ++tally >= capacity())
				return tally;
		}
		return tally;
	}
}