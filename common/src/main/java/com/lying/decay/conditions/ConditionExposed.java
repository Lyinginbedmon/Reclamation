package com.lying.decay.conditions;

import java.util.Optional;

import com.lying.utility.ExteriorUtility;

import net.minecraft.block.BlockState;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

public class ConditionExposed extends DecayCondition
{
	private Optional<Integer> searchRange = Optional.empty();
	
	public ConditionExposed(Identifier idIn)
	{
		super(idIn, 10);
	}
	
	protected boolean check(ServerWorld world, BlockPos pos, BlockState currentState)
	{
		return ExteriorUtility.isBlockInExterior(pos, world, searchRange.orElse(ExteriorUtility.DEFAULT_SEARCH_RANGE)).isPresent();
	}
}
