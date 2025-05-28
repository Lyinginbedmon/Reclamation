package com.lying.data;

import static com.lying.reference.Reference.ModInfo.prefix;

import net.minecraft.block.Block;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;

public class RCBlockTags
{
	public static final TagKey<Block> RUST				= TagKey.of(RegistryKeys.BLOCK, prefix("rust_block"));
	public static final TagKey<Block> FADED_TERRACOTTA	= TagKey.of(RegistryKeys.BLOCK, prefix("faded_terracotta"));
	public static final TagKey<Block> CRACKED_CONCRETE	= TagKey.of(RegistryKeys.BLOCK, prefix("cracked_concrete"));
	public static final TagKey<Block> CONCRETE			= TagKey.of(RegistryKeys.BLOCK, Identifier.ofVanilla("concrete"));
	public static final TagKey<Block> MOLD_IMPERVIOUS	= TagKey.of(RegistryKeys.BLOCK, prefix("mold_impervious"));
}
