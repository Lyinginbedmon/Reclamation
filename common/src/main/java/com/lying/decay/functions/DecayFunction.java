package com.lying.decay.functions;

import java.util.List;
import java.util.Optional;

import org.jetbrains.annotations.Nullable;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.lying.decay.context.DecayContext;
import com.lying.init.RCDecayFunctions;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

public abstract class DecayFunction
{
	public static final Codec<DecayFunction> CODEC = Codec.of(DecayFunction::encode, DecayFunction::decode);
	protected static final Codec<List<Block>> BLOCK_CODEC = Registries.BLOCK.getCodec().listOf();
	protected static final Codec<List<BlockState>> BLOCKSTATE_CODEC = BlockState.CODEC.listOf();
	
	@SuppressWarnings("unchecked")
	private static <T> DataResult<T> encode(final DecayFunction func, final DynamicOps<T> ops, final T prefix)
	{
		return ops == JsonOps.INSTANCE ? (DataResult<T>)DataResult.success(func.toJson()) : DataResult.error(() -> "Storing decay function as NBT is not supported");
	}
	
	private static <T> DataResult<Pair<DecayFunction, T>> decode(final DynamicOps<T> ops, final T input)
	{
		return ops == JsonOps.INSTANCE ? DataResult.success(Pair.of(fromJson((JsonElement)input), input)) : DataResult.error(() -> "Loading decay function from NBT is not supported");
	}
	
	private final Identifier registryID;
	
	protected DecayFunction(Identifier idIn)
	{
		registryID = idIn;
	}
	
	public final Identifier registryId() { return registryID; }
	
	public final void apply(DecayContext context)
	{
		if(context.isRoot() || context.continuityBroken())
			return;
		applyTo(context);
	}
	
	/** Applies this function to the given non-root decay context */
	protected abstract void applyTo(DecayContext context);
	
	public final JsonElement toJson()
	{
		JsonObject data = write(new JsonObject());
		if(data.isEmpty())
			return new JsonPrimitive(registryID.toString());
		else
		{
			data.add("id", new JsonPrimitive(registryID.toString()));
			return data;
		}
	}
	
	protected JsonObject write(JsonObject obj) { return obj; }
	
	@Nullable
	public static final DecayFunction fromJson(JsonElement nbt)
	{
		if(nbt.isJsonPrimitive())
		{
			Identifier id = Identifier.of(nbt.getAsString());
			Optional<DecayFunction> func = RCDecayFunctions.get(id);
			return func.isEmpty() ? null : func.get();
		}
		else if(nbt.isJsonObject())
		{
			JsonObject obj = nbt.getAsJsonObject();
			Identifier id = Identifier.of(obj.get("id").getAsString());
			Optional<DecayFunction> func = RCDecayFunctions.get(id);
			if(func.isEmpty())
				return null;
			
			DecayFunction function = func.get();
			function.read(obj);
			return function;
		}
		return null;
	}
	
	protected void read(JsonObject obj) { }
}
