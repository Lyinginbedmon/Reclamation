package com.lying.block;

import net.minecraft.block.Block;
import net.minecraft.block.CarpetBlock;

public class LeafPileBlock extends CarpetBlock
{
	protected final Block parentLeaf;
	
	public LeafPileBlock(Block parentLeafIn, Settings settings)
	{
		super(settings);
		parentLeaf = parentLeafIn;
	}
	
	public final Block leaves() { return parentLeaf; }
}
