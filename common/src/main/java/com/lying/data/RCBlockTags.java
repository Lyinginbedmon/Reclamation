package com.lying.data;

import static com.lying.reference.Reference.ModInfo.prefix;

import net.minecraft.block.Block;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;

public class RCBlockTags
{
	public static final TagKey<Block> RUST				= TagKey.of(RegistryKeys.BLOCK, prefix("rust_block"));
	public static final TagKey<Block> FADED_TERRACOTTA	= TagKey.of(RegistryKeys.BLOCK, prefix("faded_terracotta"));
}
