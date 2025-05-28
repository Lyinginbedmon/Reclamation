package com.lying.block;

import net.minecraft.block.BlockState;
import net.minecraft.state.property.BooleanProperty;

public interface IDeActivatable
{
	public static final BooleanProperty INERT = BooleanProperty.of("inert");
	
	public default boolean isInert(BlockState currentState) { return currentState.get(INERT); }
	
	public default BlockState toggleInert(BlockState currentState) { return currentState.with(INERT, !isInert(currentState)); }
}
