package com.lying.block;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.IntProperty;
import net.minecraft.util.DyeColor;

public class CrackedConcreteBlock extends Block
{
	public static final IntProperty CRACKS = IntProperty.of("cracks", 1, 4);
	
	public static final Map<DyeColor, Block> DYE_TO_BLOCK = new HashMap<>();
	
	private final DyeColor color;
	
	public CrackedConcreteBlock(DyeColor colorIn, Settings settings)
	{
		super(settings);
		color = colorIn;
		setDefaultState(getDefaultState().with(CRACKS, 1));
		
		DYE_TO_BLOCK.put(colorIn, this);
	}
	
	public static Block byColor(DyeColor color) { return DYE_TO_BLOCK.get(color); }
	
	public DyeColor color() { return color; }
	
	protected void appendProperties(StateManager.Builder<Block, BlockState> builder)
	{
		builder.add(CRACKS);
	}
}
