package com.lying.init;

import static com.lying.reference.Reference.ModInfo.prefix;

import java.util.Set;

import com.lying.Reclamation;
import com.lying.block.entity.RaggedBannerBlockEntity;
import com.lying.mixin.InvokerBlockEntityType;
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
	
	public static final RegistrySupplier<BlockEntityType<RaggedBannerBlockEntity>> RAGGED_BANNER	= register("ragged_banner", RaggedBannerBlockEntity::new,
			RCBlocks.BLACK_RAGGED_BANNER.get(),
			RCBlocks.BLACK_RAGGED_WALL_BANNER.get(),
			RCBlocks.BLUE_RAGGED_BANNER.get(),
			RCBlocks.BLUE_RAGGED_WALL_BANNER.get(),
			RCBlocks.BROWN_RAGGED_BANNER.get(),
			RCBlocks.BROWN_RAGGED_WALL_BANNER.get(),
			RCBlocks.CYAN_RAGGED_BANNER.get(),
			RCBlocks.CYAN_RAGGED_WALL_BANNER.get(),
			RCBlocks.GRAY_RAGGED_BANNER.get(),
			RCBlocks.GRAY_RAGGED_WALL_BANNER.get(),
			RCBlocks.GREEN_RAGGED_BANNER.get(),
			RCBlocks.GREEN_RAGGED_WALL_BANNER.get(),
			RCBlocks.LIGHT_BLUE_RAGGED_BANNER.get(),
			RCBlocks.LIGHT_BLUE_RAGGED_WALL_BANNER.get(),
			RCBlocks.LIGHT_GRAY_RAGGED_BANNER.get(),
			RCBlocks.LIGHT_GRAY_RAGGED_WALL_BANNER.get(),
			RCBlocks.MAGENTA_RAGGED_BANNER.get(),
			RCBlocks.MAGENTA_RAGGED_WALL_BANNER.get(),
			RCBlocks.ORANGE_RAGGED_BANNER.get(),
			RCBlocks.ORANGE_RAGGED_WALL_BANNER.get(),
			RCBlocks.PINK_RAGGED_BANNER.get(),
			RCBlocks.PINK_RAGGED_WALL_BANNER.get(),
			RCBlocks.PURPLE_RAGGED_BANNER.get(),
			RCBlocks.PURPLE_RAGGED_WALL_BANNER.get(),
			RCBlocks.RED_RAGGED_BANNER.get(),
			RCBlocks.RED_RAGGED_WALL_BANNER.get(),
			RCBlocks.WHITE_RAGGED_BANNER.get(),
			RCBlocks.WHITE_RAGGED_WALL_BANNER.get(),
			RCBlocks.YELLOW_RAGGED_BANNER.get(),
			RCBlocks.YELLOW_RAGGED_WALL_BANNER.get());
	
	private static <T extends BlockEntity> RegistrySupplier<BlockEntityType<T>> register(String nameIn, BlockEntityType.BlockEntityFactory<? extends T> supplierIn, Block... blocksIn)
	{
		tally++;
		return TYPES.register(prefix(nameIn), () -> InvokerBlockEntityType.create(supplierIn, Set.of(blocksIn)));
	}
	
	public static void init()
	{
		TYPES.register();
		Reclamation.LOGGER.info("# Initialised {} block entity types", tally);
	}
}
