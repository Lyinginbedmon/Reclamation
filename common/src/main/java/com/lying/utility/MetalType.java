package com.lying.utility;

import java.util.Optional;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.Items;

public enum MetalType
{
	COPPER(Items.COPPER_INGOT, Blocks.COPPER_BLOCK),
	IRON(Items.IRON_INGOT, Blocks.IRON_BLOCK, Items.IRON_NUGGET),
	GOLD(Items.GOLD_INGOT, Blocks.GOLD_BLOCK, Items.GOLD_NUGGET),
	NETHERITE(Items.NETHERITE_INGOT, Blocks.NETHERITE_BLOCK);
	
	public final Item ingot;
	public final Optional<Item> nugget;
	
	public final Block block;
	
	private MetalType(Item ingotIn, Block blockIn)
	{
		this(ingotIn, blockIn, Optional.empty());
	}
	
	private MetalType(Item ingotIn, Block blockIn, Item nuggetIn)
	{
		this(ingotIn, blockIn, Optional.of(nuggetIn));
	}
	
	private MetalType(Item ingotIn, Block blockIn, Optional<Item> nuggetIn)
	{
		this.ingot = ingotIn;
		this.block = blockIn;
		this.nugget = nuggetIn;
	}
}
