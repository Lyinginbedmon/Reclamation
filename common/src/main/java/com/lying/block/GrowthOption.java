package com.lying.block;

import net.minecraft.block.BlockState;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

public abstract class GrowthOption
{
	private final String name;
	
	protected GrowthOption(String nameIn)
	{
		name = nameIn;
	}
	
	/** Utility, used for debugging */
	public final String name() { return name; }
	
	/** Returns true if this option is available for use in the given context */
	public abstract boolean viable(BlockState state, BlockPos pos, ServerWorld world);
	
	/** Applies this option to the given context */
	public abstract void enact(BlockState state, BlockPos pos, ServerWorld world);
}