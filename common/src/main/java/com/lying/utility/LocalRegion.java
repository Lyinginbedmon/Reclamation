package com.lying.utility;

import java.util.List;

import org.jetbrains.annotations.Nullable;

import com.google.common.collect.Lists;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;

public class LocalRegion
{
	public static final Codec<LocalRegion> CODEC = Codec.of(LocalRegion::encode, LocalRegion::decode);
	
	@SuppressWarnings("unchecked")
	private static <T> DataResult<T> encode(final LocalRegion func, final DynamicOps<T> ops, final T prefix)
	{
		return ops == JsonOps.INSTANCE ? (DataResult<T>)DataResult.success(func.toJson()) : DataResult.error(() -> "Storing local regions as NBT is not supported");
	}
	
	private static <T> DataResult<Pair<LocalRegion, T>> decode(final DynamicOps<T> ops, final T input)
	{
		return ops == JsonOps.INSTANCE ? DataResult.success(Pair.of(fromJson(((JsonElement)input).getAsJsonObject()), input)) : DataResult.error(() -> "Loading local regions from NBT is not supported");
	}
	
	private final LocalRegion.Type type;
	private Vec3i 
		min = new Vec3i(-1, -1, -1), 
		max = new Vec3i(1, 1, 1), 
		size = new Vec3i(3, 3, 3);
	private double radius = 3;
	
	private final List<Vec3i> positions;
	
	public LocalRegion(int radiusIn)
	{
		type = Type.SPHERE;
		radius = radiusIn;
		
		positions = calculateValid();
	}
	
	public LocalRegion(Vec3i scaleIn)
	{
		type = Type.SCALE;
		size = scaleIn;
		
		positions = calculateValid();
	}
	
	public LocalRegion(Vec3i minIn, Vec3i maxIn)
	{
		type = Type.BOUNDS;
		min = minIn;
		max = maxIn;
		
		positions = calculateValid();
	}
	
	public List<Vec3i> positions() { return positions; }
	
	@Nullable
	public Box toBox(BlockPos origin)
	{
		Vec3d core = new Vec3d(origin.getX() + 0.5D, origin.getY() + 0.5D, origin.getZ() + 0.5D);
		switch(type)
		{
			case BOUNDS:
				return Box.enclosing(origin.add(min), origin.add(max));
			case SCALE:
				return Box.of(core, size.getX(), size.getY(), size.getZ());
			case SPHERE:
				double diameter = radius * 2;
				return Box.of(core, diameter, diameter, diameter);
			default:
				return null;
		}
	}
	
	public boolean test(Vec3i position)
	{
		switch(type)
		{
			case SPHERE:
				return position.isWithinDistance(Vec3i.ZERO, radius);
			case SCALE:
				return 
						Math.abs(position.getX()) <= (size.getX() / 2) &&
						Math.abs(position.getY()) <= (size.getY() / 2) &&
						Math.abs(position.getZ()) <= (size.getZ() / 2);
			case BOUNDS:
				return
						min.getX() <= position.getX() && max.getX() >= position.getX() &&
						min.getY() <= position.getY() && max.getY() >= position.getY() &&
						min.getZ() <= position.getZ() && max.getZ() >= position.getZ();
			default:
				return false;
		}
	}
	
	public List<Vec3i> calculateValid()
	{
		List<Vec3i> positions = Lists.newArrayList();
		switch(type)
		{
			case BOUNDS:
				for(int x=-min.getX(); x<max.getX(); x++)
					for(int z=min.getZ(); z<max.getZ(); z++)
						for(int y=min.getY(); y<max.getY(); y++)
							positions.add(new Vec3i(x, y, z));
				break;
			case SCALE:
				int xH = size.getX() / 2;
				int yH = size.getY() / 2;
				int zH = size.getZ() / 2;
				for(int x=-xH; x<xH; x++)
					for(int z=-zH; z<zH; z++)
						for(int y=-yH; y<yH; y++)
							positions.add(new Vec3i(x, y, z));
				break;
			case SPHERE:
				for(int x=-(int)radius; x<radius; x++)
					for(int z=-(int)radius; z<radius; z++)
						for(int y=-(int)radius; y<radius; y++)
						{
							Vec3i point = new Vec3i(x, y, z);
							if(test(point))
								positions.add(point);
						}
				break;
			default:
				break;
		}
		return positions;
	}
	
	public JsonObject toJson()
	{
		JsonObject obj = new JsonObject();
		switch(type)
		{
			case BOUNDS:
				obj.add("min", Vec3i.CODEC.encodeStart(JsonOps.INSTANCE, min).getOrThrow());
				obj.add("max", Vec3i.CODEC.encodeStart(JsonOps.INSTANCE, max).getOrThrow());
				break;
			case SCALE:
				obj.add("size", Vec3i.CODEC.encodeStart(JsonOps.INSTANCE, size).getOrThrow());
				break;
			case SPHERE:
				obj.addProperty("radius", radius);
				break;
			default:
				break;
		}
		return obj;
	}
	
	@Nullable
	public static LocalRegion fromJson(JsonObject obj)
	{
		if(obj.has("radius"))
			return new LocalRegion(obj.get("radius").getAsInt());
		else if(obj.has("size"))
			return new LocalRegion(Vec3i.CODEC.parse(JsonOps.INSTANCE, obj.get("size")).getOrThrow());
		else if(obj.has("min") && obj.has("max"))
			return new LocalRegion(Vec3i.CODEC.parse(JsonOps.INSTANCE, obj.get("min")).getOrThrow(), Vec3i.CODEC.parse(JsonOps.INSTANCE, obj.get("max")).getOrThrow());
		return null;
	}
	
	public static enum Type
	{
		SCALE,
		BOUNDS,
		SPHERE;
	}
}