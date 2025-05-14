package com.lying.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.IntProperty;

public class CrackedConcreteBlock extends Block
{
	public static final IntProperty CRACKS = IntProperty.of("cracks", 1, 4);
	
	public CrackedConcreteBlock(Settings settings)
	{
		super(settings);
		setDefaultState(getDefaultState().with(CRACKS, 1));
	}
	
	protected void appendProperties(StateManager.Builder<Block, BlockState> builder)
	{
		builder.add(CRACKS);
	}
}
