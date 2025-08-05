package com.lying.decay.conditions;

import java.util.Optional;

import com.google.gson.JsonObject;
import com.lying.decay.context.DecayContext;
import com.lying.utility.ExteriorUtility;
import com.lying.utility.RCUtils;

import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

public class ConditionExposed extends DecayCondition
{
	private Optional<Integer> searchRange = Optional.empty();
	
	public ConditionExposed(Identifier idIn)
	{
		super(idIn, 10);
	}
	
	protected boolean check(DecayContext context)
	{
		int range = searchRange.orElse(ExteriorUtility.DEFAULT_SEARCH_RANGE);
		
		// First, test if we can reach any known exterior positions nearby
		if(context.findNearbyExteriors(context.currentPos(), range).stream()
				.sorted(RCUtils.closestFirst(context.currentPos()))
				.anyMatch(b -> ExteriorUtility.contiguousWith(context.currentPos(), b, context.world.get(), range)))
			return true;
		
		// Second, try to find an as-yet-undetected exterior position nearby
		Optional<BlockPos> result = ExteriorUtility.isBlockInExterior(context.currentPos(), context.world.get(), range);
		result.ifPresent(p -> context.flagExterior(p));
		return result.isPresent();
	}
	
	protected JsonObject write(JsonObject obj)
	{
		searchRange.ifPresent(i -> obj.addProperty("range", i));
		return obj;
	}
	
	protected void read(JsonObject obj)
	{
		if(obj.has("range"))
			searchRange = Optional.of(obj.get("range").getAsInt());
	}
}
