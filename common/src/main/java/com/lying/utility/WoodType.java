package com.lying.utility;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;

public enum WoodType
{
	OAK(Blocks.OAK_LOG, Blocks.OAK_PLANKS, Blocks.OAK_SLAB, Blocks.OAK_STAIRS, Blocks.STRIPPED_OAK_LOG, Blocks.OAK_BUTTON, Blocks.OAK_DOOR, Blocks.OAK_FENCE),
	SPRUCE(Blocks.SPRUCE_LOG, Blocks.SPRUCE_PLANKS, Blocks.SPRUCE_SLAB, Blocks.SPRUCE_STAIRS, Blocks.STRIPPED_SPRUCE_LOG, Blocks.SPRUCE_BUTTON, Blocks.SPRUCE_DOOR, Blocks.SPRUCE_FENCE),
	BIRCH(Blocks.BIRCH_LOG, Blocks.BIRCH_PLANKS, Blocks.BIRCH_SLAB, Blocks.BIRCH_STAIRS, Blocks.STRIPPED_BIRCH_LOG, Blocks.BIRCH_BUTTON, Blocks.BIRCH_DOOR, Blocks.BIRCH_FENCE),
	DARK_OAK(Blocks.DARK_OAK_LOG, Blocks.DARK_OAK_PLANKS, Blocks.DARK_OAK_SLAB, Blocks.DARK_OAK_STAIRS, Blocks.STRIPPED_DARK_OAK_LOG, Blocks.DARK_OAK_BUTTON, Blocks.DARK_OAK_DOOR, Blocks.DARK_OAK_FENCE),
	JUNGLE(Blocks.JUNGLE_LOG, Blocks.JUNGLE_PLANKS, Blocks.JUNGLE_SLAB, Blocks.JUNGLE_STAIRS, Blocks.STRIPPED_JUNGLE_LOG, Blocks.JUNGLE_BUTTON, Blocks.JUNGLE_DOOR, Blocks.JUNGLE_FENCE),
	ACACIA(Blocks.ACACIA_LOG, Blocks.ACACIA_PLANKS, Blocks.ACACIA_SLAB, Blocks.ACACIA_STAIRS, Blocks.STRIPPED_ACACIA_LOG, Blocks.ACACIA_BUTTON, Blocks.ACACIA_DOOR, Blocks.ACACIA_FENCE),
	MANGROVE(Blocks.MANGROVE_LOG, Blocks.MANGROVE_PLANKS, Blocks.MANGROVE_SLAB, Blocks.MANGROVE_STAIRS, Blocks.STRIPPED_MANGROVE_LOG, Blocks.MANGROVE_BUTTON, Blocks.MANGROVE_DOOR, Blocks.MANGROVE_FENCE),
	CHERRY(Blocks.CHERRY_LOG, Blocks.CHERRY_PLANKS, Blocks.CHERRY_SLAB, Blocks.CHERRY_STAIRS, Blocks.STRIPPED_CHERRY_LOG, Blocks.CHERRY_BUTTON, Blocks.CHERRY_DOOR, Blocks.CHERRY_FENCE),
	PALE(Blocks.PALE_OAK_LOG, Blocks.PALE_OAK_PLANKS, Blocks.PALE_OAK_SLAB, Blocks.PALE_OAK_STAIRS, Blocks.STRIPPED_PALE_OAK_LOG, Blocks.PALE_OAK_BUTTON, Blocks.PALE_OAK_DOOR, Blocks.PALE_OAK_FENCE),
	BAMBOO(Blocks.BAMBOO_BLOCK, Blocks.BAMBOO_PLANKS, Blocks.BAMBOO_SLAB, Blocks.BAMBOO_STAIRS, Blocks.STRIPPED_BAMBOO_BLOCK, Blocks.BAMBOO_BUTTON, Blocks.BAMBOO_DOOR, Blocks.BAMBOO_FENCE),
	CRIMSON(Blocks.CRIMSON_STEM, Blocks.CRIMSON_PLANKS, Blocks.CRIMSON_SLAB, Blocks.CRIMSON_STAIRS, Blocks.STRIPPED_CRIMSON_STEM, Blocks.CRIMSON_BUTTON, Blocks.CRIMSON_DOOR, Blocks.CRIMSON_FENCE),
	WARPED(Blocks.WARPED_STEM, Blocks.WARPED_PLANKS, Blocks.WARPED_SLAB, Blocks.WARPED_STAIRS, Blocks.STRIPPED_WARPED_STEM, Blocks.WARPED_BUTTON, Blocks.WARPED_DOOR, Blocks.WARPED_FENCE);
	
	public final Block log, planks, slab, stairs, strippedLog, button, door, fence;
	
	private WoodType(Block logIn, Block planksIn, Block slabIn, Block stairsIn, Block strippedLogIn, Block buttonIn, Block doorIn, Block fenceIn)
	{
		this.log = logIn;
		this.planks = planksIn;
		this.slab = slabIn;
		this.stairs = stairsIn;
		this.strippedLog = strippedLogIn;
		this.button = buttonIn;
		this.door = doorIn;
		this.fence = fenceIn;
	}
}
