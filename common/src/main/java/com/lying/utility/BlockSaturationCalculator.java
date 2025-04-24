package com.lying.utility;

import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;

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

public class BlockSaturationCalculator
{
	public static final Codec<BlockSaturationCalculator> CODEC	= RecordCodecBuilder.create(instance -> instance.group(
			Codec.FLOAT.optionalFieldOf("minimum").forGetter(c -> c.minMult),
			Codec.FLOAT.optionalFieldOf("maximum").forGetter(c -> c.maxMult),
			Codec.FLOAT.optionalFieldOf("result").forGetter(c -> c.staticResult),
			Mode.CODEC.optionalFieldOf("use_tally_as").forGetter(c -> c.mode),
			Codec.FLOAT.optionalFieldOf("power").forGetter(c -> c.power),
			TallyGetter.CODEC.optionalFieldOf("search").forGetter(c -> (c.tallyGetter.isEmpty() || c.tallyGetter.isPresent() && c.tallyGetter.get().isBlank() || c.isStaticResult()) ? Optional.empty() : c.tallyGetter)
			)
			.apply(instance, (min, max, staticVal, mod, pow, tally) -> new BlockSaturationCalculator(min, max, staticVal, mod, pow, tally)));
	
	private final Optional<Float> staticResult;
	private final Optional<Float> minMult, maxMult;
	
	private final Optional<Mode> mode;
	private final Optional<Float> power;
	
	private final Optional<TallyGetter> tallyGetter;
	
	protected BlockSaturationCalculator(Optional<Float> min, Optional<Float> max, Optional<Float> staticIn, Optional<Mode> modeIn, Optional<Float> power, Optional<TallyGetter> getter)
	{
		this.minMult = min;
		this.maxMult = max;
		this.staticResult = staticIn;
		this.mode = modeIn;
		this.power = power;
		this.tallyGetter = getter;
	}
	
	public static BlockSaturationCalculator ofValue(float value)
	{
		Builder builder = Builder.create();
		builder.staticVal = Optional.of(value);
		return builder.build();
	}
	
	/** Returns true if the result of this calculator has equal minimum and maximum values */
	public boolean isStaticResult()
	{
		return staticResult.isPresent() || minMult.orElse(0F) == maxMult.orElse(1F);
	}
	
	/** Returns a calculation of the saturation of applicable blocks within range around the given position */
	public float calculate(World world, BlockPos pos)
	{
		if(isStaticResult())
			return staticResult.orElse(minMult.orElse(0F));
		
		TallyGetter tally = tallyGetter.orElse(TallyGetter.blank());
		float value = mode.orElse(Mode.PERCENT_OF_CAPACITY).func.apply(tally.getTally(world, pos), (float)tally.capacity());
		return MathHelper.clamp((float)Math.pow(value, power.orElse(1F)), minMult.orElse(0F), maxMult.orElse(1F));
	}
	
	public static enum Mode implements StringIdentifiable
	{
		FLAT_VALUE((tally, capacity) -> tally),
		PERCENT_OF_CAPACITY((tally, capacity) -> tally / capacity);
		
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
		private Optional<Float> min = Optional.empty(), max = Optional.empty(), staticVal = Optional.empty(), power = Optional.empty();
		private Optional<Mode> mode = Optional.empty();
		private BlockPredicate.Builder predicate = BlockPredicate.Builder.create();
		
		protected Builder() { }
		
		public static Builder create() { return new Builder(); }
		
		public Builder mode(Mode modeIn)
		{
			mode = Optional.of(modeIn);
			return this;
		}
		
		public Builder power(float powIn)
		{
			power = powIn != 1F ? Optional.of(powIn) : Optional.empty();
			return this;
		}
		
		public Builder minMax(float val1, float val2)
		{
			setCaps(Math.min(val1, val2), Math.max(val1, val2));
			return this;
		}
		
		public Builder min(float val)
		{
			float maxVal = max.orElse(1F);
			float minVal = val;
			setCaps(Math.min(maxVal, minVal), Math.max(maxVal, minVal));
			return this;
		}
		
		public Builder max(float val)
		{
			float maxVal = val;
			float minVal = min.orElse(0F);
			setCaps(Math.min(maxVal, minVal), Math.max(maxVal, minVal));
			return this;
		}
		
		private void setCaps(float b, float t)
		{
			if(t < 1F)
				max = Optional.of(t);
			else
				max = Optional.empty();
			
			if(b > 0F)
				min = Optional.of(b);
			else
				min = Optional.empty();
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
		
		public Builder tag(TagKey<Block> tagsIn)
		{
			predicate.addBlockTag(tagsIn);
			return this;
		}
		
		public Builder tags(List<TagKey<Block>> tagsIn)
		{
			tagsIn.forEach(predicate::addBlockTag);
			return this;
		}
		
		public BlockSaturationCalculator build()
		{
			return new BlockSaturationCalculator(min, max, staticVal, mode, power, Optional.of(new TallyGetter(capacity, scanRange, scanVec, predicate.build())));
		}
	}
}