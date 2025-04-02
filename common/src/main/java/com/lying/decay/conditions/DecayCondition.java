package com.lying.decay.conditions;

import java.util.Optional;

import org.jetbrains.annotations.Nullable;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.lying.init.RCDecayConditions;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;

import net.minecraft.block.BlockState;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

public abstract class DecayCondition
{
	public static final Codec<DecayCondition> CODEC = Codec.of(DecayCondition::encode, DecayCondition::decode);
	
	protected Optional<String> name = Optional.empty();
	protected boolean inverted = false;
	
	@SuppressWarnings("unchecked")
	private static <T> DataResult<T> encode(final DecayCondition func, final DynamicOps<T> ops, final T prefix)
	{
		return ops == JsonOps.INSTANCE ? (DataResult<T>)DataResult.success(func.toJson()) : DataResult.error(() -> "Storing decay function as NBT is not supported");
	}
	
	private static <T> DataResult<Pair<DecayCondition, T>> decode(final DynamicOps<T> ops, final T input)
	{
		return ops == JsonOps.INSTANCE ? DataResult.success(Pair.of(fromJson((JsonElement)input), input)) : DataResult.error(() -> "Loading decay function from NBT is not supported");
	}
	
	private final Identifier registryID;
	
	protected DecayCondition(Identifier idIn)
	{
		registryID = idIn;
	}
	
	public final Identifier registryId() { return registryID; }
	
	public final boolean inverted() { return inverted; }
	
	protected abstract boolean check(ServerWorld world, BlockPos pos, BlockState currentState);
	
	public final boolean test(ServerWorld world, BlockPos pos, BlockState currentState)
	{
		return check(world, pos, currentState) != inverted;
	}
	
	public final DecayCondition invert()
	{
		inverted = true;
		return this;
	}
	
	/** Sets a name value displayed in JSON data for ease of reading */
	public DecayCondition named(String nameIn)
	{
		name = Optional.of(nameIn);
		return this;
	}
	
	public final JsonElement toJson()
	{
		JsonObject data = write(new JsonObject());
		name.ifPresent(n -> data.addProperty("name", n));
		if(inverted)
			data.addProperty("inverted", inverted);
		
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
	public static final DecayCondition fromJson(JsonElement nbt)
	{
		if(nbt.isJsonPrimitive())
		{
			Identifier id = Identifier.of(nbt.getAsString());
			Optional<DecayCondition> pred = RCDecayConditions.get(id);
			return pred.isEmpty() ? null : pred.get();
		}
		else if(nbt.isJsonObject())
		{
			JsonObject obj = nbt.getAsJsonObject();
			Identifier id = Identifier.of(obj.get("id").getAsString());
			Optional<DecayCondition> pred = RCDecayConditions.get(id);
			if(pred.isEmpty())
				return null;
			
			DecayCondition condition = pred.get();
			condition.inverted = obj.has("inverted") && obj.get("inverted").getAsBoolean();
			if(obj.has("name")) condition.named(obj.get("name").getAsString());
			condition.read(obj);
			return condition;
		}
		return null;
	}
	
	protected void read(JsonObject obj) { }
}