package com.lying.fabric.client;

import com.lying.init.RCBlocks;
import com.lying.init.RCItems;

import net.fabricmc.fabric.api.client.datagen.v1.provider.FabricModelProvider;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.minecraft.client.data.BlockStateModelGenerator;
import net.minecraft.client.data.ItemModelGenerator;
import net.minecraft.client.data.Models;

public class RCModelProvider extends FabricModelProvider
{
	public RCModelProvider(FabricDataOutput output)
	{
		super(output);
	}

	public void generateBlockStateModels(BlockStateModelGenerator blockStateModelGenerator)
	{
		RCBlocks.SOLID_CUBES.forEach(entry -> blockStateModelGenerator.registerSimpleCubeAll(entry.get()));
	}
	
	public void generateItemModels(ItemModelGenerator itemModelGenerator)
	{
		itemModelGenerator.register(RCItems.WITHERING_DUST.get(), Models.GENERATED);
		
		itemModelGenerator.register(RCItems.TARNISHED_GOLD.get());
	}
}
