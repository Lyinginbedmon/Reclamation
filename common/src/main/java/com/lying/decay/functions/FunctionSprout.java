package com.lying.decay.functions;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import com.google.common.collect.Lists;
import com.google.gson.JsonObject;
import com.lying.Reclamation;
import com.lying.decay.context.DecayContext;
import com.lying.init.RCDecayFunctions;
import com.lying.utility.BlockProvider;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.block.BlockState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.property.Properties;
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
	
	public static FunctionSprout of(BlockProvider solo)
	{
		FunctionSprout func = (FunctionSprout)RCDecayFunctions.SPROUT.get();
		func.resultMap = new SproutMap(Optional.of(solo), Optional.empty(), Optional.empty());
		return func;
	}
	
	public static FunctionSprout of(BlockProvider solo, EnumSet<Direction> onFaces)
	{
		FunctionSprout func = (FunctionSprout)RCDecayFunctions.SPROUT.get();
		func.resultMap = new SproutMap(Optional.of(solo), Optional.of(onFaces), Optional.empty());
		return func;
	}
	
	public static FunctionSprout of(Map<Direction, BlockProvider> mapIn)
	{
		FunctionSprout func = (FunctionSprout)RCDecayFunctions.SPROUT.get();
		func.resultMap = new SproutMap(Optional.empty(), Optional.empty(), Optional.of(mapIn));
		return func;
	}
	
	public FunctionSprout count(int countIn)
	{
		count = countIn < 1 ? Optional.empty() : Optional.of(Math.min(countIn, 6));
		return this;
	}
	
	public FunctionSprout ignorePlacement(boolean ignoreIn)
	{
		force = ignoreIn ? Optional.of(ignoreIn) : Optional.empty();
		return this;
	}
	
	protected void applyTo(DecayContext context)
	{
		if(resultMap.isEmpty())
			return;
		
		Random random = context.random;
		context.execute((pos, world) -> 
		{
			List<Direction> faces = Lists.newArrayList(resultMap.keySet());
			int placings = count.orElse(faces.size());
			while(!faces.isEmpty())
			{
				Direction face = faces.remove(random.nextInt(faces.size()));
				BlockPos offset = pos.offset(face);
				boolean waterLogged = world.getFluidState(offset).isOf(Fluids.WATER);
				Optional<BlockState> growth = resultMap.get(face, random);
				if(growth.isPresent())
				{
					BlockState state = growth.get();
					if(force.orElse(false) || canPlaceAt(state, world, offset))
					{
						world.setBlockState(offset, 
							state.getBlock().getStateManager().getProperty(Properties.WATERLOGGED.getName()) != null && waterLogged ? state.with(Properties.WATERLOGGED, waterLogged) : state);
						
						if(!force.orElse(false))
							state.getBlock().onPlaced(world, offset, state, null, new ItemStack(state.getBlock().asItem()));
					}
					
					if(--placings == 0)
						return;
				}
			}
		});
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
	
	@SuppressWarnings("serial")
	private static class SproutMap extends HashMap<Direction, BlockProvider>
	{
		public static final Codec<SproutMap> CODEC	= RecordCodecBuilder.create(instance -> instance.group(
				BlockProvider.CODEC.optionalFieldOf("growth").forGetter(s -> s.soloGetter),
				SerializedFaceSet.CODEC.optionalFieldOf("face").forGetter(s -> s.directionSet),
				SerializedFaceGetterMap.CODEC.optionalFieldOf("growth_by_face").forGetter(s -> s.resultByFaceMap))
				.apply(instance, (a, b, c) -> new SproutMap(a, b, c)));
		
		private final Optional<EnumSet<Direction>> directionSet;
		private final Optional<BlockProvider> soloGetter;
		private final Optional<Map<Direction, BlockProvider>> resultByFaceMap;
		
		public SproutMap()
		{
			this(Optional.empty(), Optional.empty(), Optional.empty());
		}
		
		protected SproutMap(Optional<BlockProvider> soloGetterIn, Optional<EnumSet<Direction>> facesIn, Optional<Map<Direction, BlockProvider>> cloneIn)
		{
			directionSet = facesIn;
			soloGetter = soloGetterIn;
			resultByFaceMap = cloneIn;
			
			soloGetter.ifPresentOrElse(
				getter -> directionSet.orElse(EnumSet.allOf(Direction.class)).forEach(face -> put(face, getter)), 
				() -> resultByFaceMap.ifPresent(clone -> clone.entrySet().forEach(entry -> put(entry.getKey(), entry.getValue()))));
		}
		
		public Optional<BlockState> get(Direction face, Random random) { return get(face).getRandom(random); }
	}
	
	private static class SerializedFaceSet
	{
		private static final Codec<EnumSet<Direction>> CODEC	= Codec.of(SerializedFaceSet::encode, SerializedFaceSet::decode);
		
		private static <T> DataResult<T> encode(final EnumSet<Direction> set, final DynamicOps<T> ops, final T prefix)
		{
			T obj;
			switch(set.size())
			{
				case 1:
					obj = ops.createString(set.toArray(new Direction[0])[0].asString());
					break;
				default:
					obj = ops.createList(set.stream().map(d -> ops.createString(d.asString())));
					break;
			}
			return (DataResult<T>)DataResult.success(obj);
		}
		
		private static <T> DataResult<Pair<EnumSet<Direction>, T>> decode(final DynamicOps<T> ops, final T input)
		{
			DataResult<String> string;
			if((string = ops.getStringValue(input)) != null)
				return DataResult.success(Pair.of(EnumSet.of(Direction.byName(string.getOrThrow())), input));
			
			EnumSet<Direction> set = EnumSet.noneOf(Direction.class);
			set.addAll(ops.getStream(input).result().orElse(Stream.empty()).map(e -> Direction.byName(ops.getStringValue(e).getOrThrow())).toList());
			return DataResult.success(Pair.of(set, input));
		}
	}
	
	private static class SerializedFaceGetterMap
	{
		private static final Codec<Map<Direction, BlockProvider>> CODEC	= Codec.of(SerializedFaceGetterMap::encode, SerializedFaceGetterMap::decode);
		
		private static <T> DataResult<T> encode(final Map<Direction, BlockProvider> map, final DynamicOps<T> ops, final T prefix)
		{
			Map<T,T> valueMap = new HashMap<>();
			map.entrySet().forEach(entry -> valueMap.put(ops.createString(entry.getKey().asString()), BlockProvider.CODEC.encode(entry.getValue(), ops, null).getOrThrow()));
			return DataResult.success(ops.createMap(valueMap));
		}
		
		private static <T> DataResult<Pair<Map<Direction, BlockProvider>, T>> decode(final DynamicOps<T> ops, final T input)
		{
			Map<Direction, BlockProvider> map = new HashMap<>();
			ops.getMap(input).result().ifPresent(m -> 
				m.entries().forEach(entry -> 
					map.put(Direction.byName(ops.getStringValue(entry.getFirst()).getOrThrow()), BlockProvider.CODEC.parse(ops, entry.getSecond()).resultOrPartial(Reclamation.LOGGER::error).orElseThrow())));
			return DataResult.success(Pair.of(map, input));
		}
	}
}
