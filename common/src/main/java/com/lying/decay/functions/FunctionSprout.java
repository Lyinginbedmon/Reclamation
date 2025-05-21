package com.lying.decay.functions;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.google.common.collect.Lists;
import com.google.gson.JsonObject;
import com.lying.Reclamation;
import com.lying.decay.DecayMacros;
import com.lying.decay.conditions.DecayCondition;
import com.lying.decay.context.DecayContext;
import com.lying.decay.handler.DecayMacro;
import com.lying.init.RCDecayFunctions;
import com.lying.utility.RCUtils;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.block.BlockState;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;

public class FunctionSprout extends DecayFunction
{
	private Optional<Integer> count = Optional.empty();
	private Optional<Boolean> force = Optional.empty();
	private SproutMap resultMap = new SproutMap();
	
	public FunctionSprout(Identifier idIn)
	{
		super(idIn);
	}
	
	protected void applyTo(DecayContext context)
	{
		if(resultMap.isEmpty())
			return;
		
		Random random = context.random;
		ServerWorld serverWorld = context.world.get();
		BlockPos currentPos = context.currentPos();
		List<Direction> faces = Lists.newArrayList(resultMap.keySet());
		int placings = count.orElse(faces.size());
		while(!faces.isEmpty())
		{
			Direction face = faces.remove(random.nextInt(faces.size()));
			BlockPos offset = currentPos.offset(face);
			
			if(resultMap.condition.isPresent())
			{
				DecayCondition condition = resultMap.condition.get();
				BlockState stateAt = context.getBlockState(offset);
				if(!DecayCondition.testAny(List.of(condition), context.create(serverWorld, offset, stateAt)))
					continue;
			}
			
			DecayContext child = context.create(serverWorld, offset, context.getBlockState(offset));
			Optional<DecayMacro> macro = DecayMacros.instance().get(resultMap.get(face));
			if(macro.isPresent() && macro.get().tryToApply(child))
			{
				context.addChild(child);
				--placings;
			}
			
			if(placings == 0)
				return;
		}
	}
	
	public boolean canPlaceAt(BlockState state, ServerWorld world, BlockPos pos)
	{
		return world.getBlockState(pos).isReplaceable() && state.canPlaceAt(world, pos);
	}
	
	protected JsonObject write(JsonObject obj)
	{
		obj.add("sprouted", SproutMap.CODEC.encodeStart(JsonOps.INSTANCE, resultMap).getOrThrow());
		count.ifPresent(i -> obj.addProperty("count", i));
		force.ifPresent(b -> obj.addProperty("ignore_contents", b));
		return obj;
	}
	
	protected void read(JsonObject obj)
	{
		resultMap = SproutMap.CODEC.parse(JsonOps.INSTANCE, obj.get("sprouted")).resultOrPartial(Reclamation.LOGGER::error).orElseThrow();
		count = obj.has("count") ? Optional.of(obj.get("count").getAsInt()) : Optional.empty();
		force = obj.has("ignore_contents") ? Optional.of(obj.get("ignore_contents").getAsBoolean()) : Optional.empty();
	}
	
	public static class Builder
	{
		Optional<Integer> count = Optional.empty();
		Optional<Boolean> force = Optional.empty();
		
		Optional<DecayCondition> condition = Optional.empty();
		Optional<EnumSet<Direction>> directionSet = Optional.empty();
		Optional<Identifier> soloGetter = Optional.empty();
		Optional<Map<Direction, Identifier>> resultByFaceMap = Optional.empty();
		
		protected Builder() { }
		
		public static Builder create() { return new Builder(); }
		
		public Builder soloProvider(Identifier solo)
		{
			soloGetter = Optional.of(solo);
			return this;
		}
		
		public Builder faceSet(EnumSet<Direction> onFaces)
		{
			directionSet = Optional.of(onFaces);
			return this;
		}
		
		public Builder perFaceMap(Map<Direction, Identifier> mapIn)
		{
			resultByFaceMap = Optional.of(mapIn);
			return this;
		}
		
		public Builder onCondition(DecayCondition conditionIn)
		{
			condition = Optional.of(conditionIn);
			return this;
		}
		
		public Builder maxPlace(int total)
		{
			count = Optional.of(total);
			return this;
		}
		
		public Builder ignoreContents()
		{
			force = Optional.of(true);
			return this;
		}
		
		public FunctionSprout build()
		{
			FunctionSprout func = (FunctionSprout)RCDecayFunctions.SPROUT.get();
			func.resultMap = new SproutMap(soloGetter, directionSet, condition, resultByFaceMap);
			func.count = count;
			func.force = force;
			return func;
		}
	}
	
	@SuppressWarnings("serial")
	private static class SproutMap extends HashMap<Direction, Identifier>
	{
		public static final Codec<SproutMap> CODEC	= RecordCodecBuilder.create(instance -> instance.group(
				Identifier.CODEC.optionalFieldOf("growth").forGetter(s -> s.soloGetter),
				Direction.CODEC.optionalFieldOf("face").forGetter(s -> s.directionSet.isEmpty() || s.directionSet.get().size() > 1 ? Optional.empty() : s.directionSet.get().stream().findFirst()),
				RCUtils.DIRECTION_SET_CODEC.optionalFieldOf("faces").forGetter(s -> s.directionSet.isEmpty() || s.directionSet.get().size() == 1 ? Optional.empty() : s.directionSet),
				DecayCondition.CODEC.optionalFieldOf("must_be").forGetter(s -> s.condition),
				SerializedFaceGetterMap.CODEC.optionalFieldOf("growth_by_face").forGetter(s -> s.resultByFaceMap))
				.apply(instance, (growth, face, faceSet, condition, map) -> 
				{
					EnumSet<Direction> faces = EnumSet.noneOf(Direction.class);
					face.ifPresent(faces::add);
					faceSet.ifPresent(s -> s.forEach(faces::add));
					return new SproutMap(growth, faces.isEmpty() ? Optional.empty() : Optional.of(faces), condition, map);
				}));
		
		private final Optional<DecayCondition> condition;
		private final Optional<EnumSet<Direction>> directionSet;
		private final Optional<Identifier> soloGetter;
		private final Optional<Map<Direction, Identifier>> resultByFaceMap;
		
		public SproutMap()
		{
			this(Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty());
		}
		
		protected SproutMap(Optional<Identifier> soloGetterIn, Optional<EnumSet<Direction>> facesIn, Optional<DecayCondition> conditionIn, Optional<Map<Direction, Identifier>> cloneIn)
		{
			condition = conditionIn;
			directionSet = facesIn;
			soloGetter = soloGetterIn;
			resultByFaceMap = cloneIn;
			
			soloGetter.ifPresentOrElse(
				getter -> directionSet.orElse(EnumSet.allOf(Direction.class)).forEach(face -> put(face, getter)), 
				() -> resultByFaceMap.ifPresent(clone -> clone.entrySet().forEach(entry -> put(entry.getKey(), entry.getValue()))));
		}
	}
	
	private static class SerializedFaceGetterMap
	{
		private static final Codec<Map<Direction, Identifier>> CODEC	= Codec.of(SerializedFaceGetterMap::encode, SerializedFaceGetterMap::decode);
		
		private static <T> DataResult<T> encode(final Map<Direction, Identifier> map, final DynamicOps<T> ops, final T prefix)
		{
			Map<T,T> valueMap = new HashMap<>();
			map.entrySet().forEach(entry -> 
			{
				T face = Direction.CODEC.encodeStart(ops, entry.getKey()).getOrThrow();
				T macro = Identifier.CODEC.encodeStart(ops, entry.getValue()).getOrThrow();
				valueMap.put(face, macro);
			});
			return DataResult.success(ops.createMap(valueMap));
		}
		
		private static <T> DataResult<Pair<Map<Direction, Identifier>, T>> decode(final DynamicOps<T> ops, final T input)
		{
			Map<Direction, Identifier> map = new HashMap<>();
			ops.getMap(input).result().ifPresent(m -> 
				m.entries().forEach(entry -> 
				{
					Direction face = Direction.CODEC.parse(ops, entry.getFirst()).resultOrPartial(Reclamation.LOGGER::error).orElseThrow();
					Identifier macro = Identifier.CODEC.parse(ops, entry.getSecond()).resultOrPartial(Reclamation.LOGGER::error).orElseThrow();
					map.put(face, macro);
				}));
			return DataResult.success(Pair.of(map, input));
		}
	}
}
