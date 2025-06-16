package com.lying.fabric.data;

import com.lying.fabric.client.RCModelProvider;

import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;

public class RCDataGenerators implements DataGeneratorEntrypoint
{
	public void onInitializeDataGenerator(FabricDataGenerator fabricDataGenerator)
	{
		FabricDataGenerator.Pack pack = fabricDataGenerator.createPack();
		pack.addProvider(RCModelProvider::new);
		pack.addProvider(RCBlockTagsProvider::new);
		pack.addProvider(RCItemTagsProvider::new);
		pack.addProvider(RCBlockLootTableProvider::new);
		pack.addProvider(RCRecipeProvider::new);
		pack.addProvider(RCDecayProvider::new);
		pack.addProvider(RCMacroProvider::new);
	}
}
