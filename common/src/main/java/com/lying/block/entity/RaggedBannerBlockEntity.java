package com.lying.block.entity;

import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import com.lying.init.RCBlockEntityTypes;
import com.mojang.logging.LogUtils;

import net.minecraft.block.AbstractBannerBlock;
import net.minecraft.block.BannerBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.component.ComponentMap;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.BannerPatternsComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.text.Text;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Nameable;
import net.minecraft.util.math.BlockPos;

public class RaggedBannerBlockEntity extends BlockEntity implements Nameable
{
	private static final Logger LOGGER = LogUtils.getLogger();
	public static final int MAX_PATTERN_COUNT = 6;
	private static final String PATTERNS_KEY = "patterns";
	@Nullable
	private Text customName;
	private final DyeColor baseColor;
	private BannerPatternsComponent patterns = BannerPatternsComponent.DEFAULT;
	
	public RaggedBannerBlockEntity(BlockPos pos, BlockState state)
	{
		this(pos, state, ((AbstractBannerBlock)state.getBlock()).getColor());
	}
	
	public RaggedBannerBlockEntity(BlockPos pos, BlockState state, DyeColor baseColor)
	{
		super(RCBlockEntityTypes.RAGGED_BANNER.get(), pos, state);
		this.baseColor = baseColor;
	}
	
	@Override
	public Text getName()
	{
		return (Text)(this.customName != null ? this.customName : Text.translatable("block.minecraft.banner"));
	}
	
	@Nullable
	@Override
	public Text getCustomName()
	{
		return this.customName;
	}
	
	@Override
	protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries)
	{
		super.writeNbt(nbt, registries);
		if (!this.patterns.equals(BannerPatternsComponent.DEFAULT)) {
			nbt.put(PATTERNS_KEY, BannerPatternsComponent.CODEC.encodeStart(registries.getOps(NbtOps.INSTANCE), this.patterns).getOrThrow());
		}

		if (this.customName != null) {
			nbt.putString("CustomName", Text.Serialization.toJsonString(this.customName, registries));
		}
	}
	
	@Override
	protected void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries)
	{
		super.readNbt(nbt, registries);
		if (nbt.contains("CustomName", 8)) {
			this.customName = tryParseCustomName(nbt.getString("CustomName"), registries);
		}

		if (nbt.contains(PATTERNS_KEY)) {
			BannerPatternsComponent.CODEC
				.parse(registries.getOps(NbtOps.INSTANCE), nbt.get(PATTERNS_KEY))
				.resultOrPartial(patterns -> LOGGER.error("Failed to parse banner patterns: '{}'", patterns))
				.ifPresent(patterns -> this.patterns = patterns);
		}
	}
	
	public BlockEntityUpdateS2CPacket toUpdatePacket()
	{
		return BlockEntityUpdateS2CPacket.create(this);
	}
	
	@Override
	public NbtCompound toInitialChunkDataNbt(RegistryWrapper.WrapperLookup registries)
	{
		return this.createNbt(registries);
	}
	
	public BannerPatternsComponent getPatterns()
	{
		return this.patterns;
	}
	
	public ItemStack getPickStack()
	{
		ItemStack itemStack = new ItemStack(BannerBlock.getForColor(this.baseColor));
		itemStack.applyComponentsFrom(this.createComponentMap());
		return itemStack;
	}
	
	public DyeColor getColorForState()
	{
		return this.baseColor;
	}
	
	@Override
	protected void readComponents(BlockEntity.ComponentsAccess components)
	{
		super.readComponents(components);
		this.patterns = components.getOrDefault(DataComponentTypes.BANNER_PATTERNS, BannerPatternsComponent.DEFAULT);
		this.customName = components.get(DataComponentTypes.CUSTOM_NAME);
	}
	
	@Override
	protected void addComponents(ComponentMap.Builder builder)
	{
		super.addComponents(builder);
		builder.add(DataComponentTypes.BANNER_PATTERNS, this.patterns);
		builder.add(DataComponentTypes.CUSTOM_NAME, this.customName);
	}
	
	@Override
	public void removeFromCopiedStackNbt(NbtCompound nbt)
	{
		nbt.remove(PATTERNS_KEY);
		nbt.remove("CustomName");
	}
}
