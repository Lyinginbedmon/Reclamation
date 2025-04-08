package com.lying.decay;

import java.util.Optional;
import java.util.function.BiFunction;

import com.lying.utility.BlockPredicate;
import com.lying.utility.TallyGetter;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;

public class Catalysers
{
	public static final Codec<Catalysers> CODEC	= RecordCodecBuilder.create(instance -> instance.group(
			Codec.FLOAT.optionalFieldOf("minimum").forGetter(c -> c.minMult),
			Codec.FLOAT.optionalFieldOf("maximum").forGetter(c -> c.maxMult),
			Mode.CODEC.optionalFieldOf("mode").forGetter(c -> c.mode),
			Codec.FLOAT.optionalFieldOf("power").forGetter(c -> c.power),
			TallyGetter.CODEC.fieldOf("tally").forGetter(c -> c.tally)
			)
			.apply(instance, (min, max, mod, pow, tally) -> new Catalysers(min, max, mod, pow, tally)));
	
	private final Optional<Float> minMult, maxMult, power;
	private final Optional<Mode> mode;
	
	private final TallyGetter tally;
	
	protected Catalysers(Optional<Float> min, Optional<Float> max, Optional<Mode> modeIn, Optional<Float> power, TallyGetter getter)
	{
		this.minMult = min;
		this.maxMult = max;
		this.mode = modeIn;
		this.power = power;
		
		this.tally = getter;
	}
	
	public float calculateMultiplier(World world, BlockPos pos)
	{
		float minimum = minMult.orElse(0.1F);
		float maximum = maxMult.orElse(1F);
		if(minimum == maximum)
			return minimum;
		
		float value = mode.orElse(Mode.PERCENTILE).func.apply(tally.getTally(world, pos), (float)tally.capacity());
		return MathHelper.clamp((float)Math.pow(value, power.orElse(0.2F)), minimum, maximum);
	}
	
	public static enum Mode implements StringIdentifiable
	{
		FLAT((tally, capacity) -> tally),
		PERCENTILE((tally, capacity) -> tally / capacity);
		
		public static final Codec<Mode> CODEC = StringIdentifiable.createBasicCodec(Mode::values);
		private final BiFunction<Float, Float, Float> func;
		
		private Mode(BiFunction<Float, Float, Float> funcIn)
		{
			func = funcIn;
		}
		
		public String asString() { return name().toLowerCase(); }
	}
	
	public static class Builder
	{
		private Optional<Integer> capacity = Optional.empty(), scanRange = Optional.empty();
		private Optional<Vec3i> scanVec = Optional.empty();
		private Optional<Float> min = Optional.empty(), max = Optional.empty(), power = Optional.empty();
		private Optional<Mode> mode = Optional.empty();
		private BlockPredicate.Builder predicate = BlockPredicate.Builder.create();
		
		protected Builder() { }
		
		public static Builder create() { return new Builder(); }
		
		public Builder mode(Mode modeIn)
		{
			mode = Optional.of(modeIn);
			return this;
		}
		
		public Builder minMax(float val1, float val2)
		{
			min = Optional.of(Math.min(val1, val2));
			max = Optional.of(Math.max(val1, val2));
			return this;
		}
		
		public Builder blockCap(int value)
		{
			capacity = Optional.of(Math.max(1, value));
			return this;
		}
		
		public Builder searchRange(int range)
		{
			scanRange = Optional.of(Math.max(1, range));
			return this;
		}
		
		public Builder searchRange(int x, int y, int z)
		{
			return searchRange(new Vec3i(Math.max(0, x), Math.max(0, y), Math.max(0, z)));
		}
		
		public Builder searchRange(Vec3i range)
		{
			scanVec = Optional.of(range);
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
			return new Catalysers(min, max, mode, power, new TallyGetter(capacity, scanRange, scanVec, predicate.build()));
		}
	}
}