package com.lying.init;

import static com.lying.reference.Reference.ModInfo.prefix;

import java.util.Set;

import com.lying.Reclamation;
import com.lying.block.RaggedBannerBlock;
import com.lying.block.entity.RaggedBannerBlockEntity;
import com.lying.reference.Reference;

import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.block.Block;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.registry.RegistryKeys;

public class RCBlockEntityTypes
{
	public static final DeferredRegister<BlockEntityType<?>> TYPES = DeferredRegister.create(Reference.ModInfo.MOD_ID, RegistryKeys.BLOCK_ENTITY_TYPE);
	private static int tally = 0;
	
	public static final RegistrySupplier<BlockEntityType<RaggedBannerBlockEntity>> RAGGED_BANNER	= register("ragged_banner", RaggedBannerBlockEntity::new, RaggedBannerBlock.getRegistered());
	
	private static <T extends BlockEntity> RegistrySupplier<BlockEntityType<T>> register(String nameIn, BlockEntityType.BlockEntityFactory<? extends T> supplierIn, Block... blocksIn)
	{
		tally++;
		return TYPES.register(prefix(nameIn), () -> new BlockEntityType<T>(supplierIn, Set.of(blocksIn)));
	}
	
	public static void init()
	{
		TYPES.register();
		Reclamation.LOGGER.info("# Initialised {} block entity types", tally);
	}
}
