package com.lying.item;

import net.minecraft.block.Block;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.BannerPatternsComponent;
import net.minecraft.item.BannerItem;

public class RaggedBannerItem extends BannerItem 
{
	public RaggedBannerItem(Block bannerBlock, Block wallBannerBlock, Settings settings)
	{
		super(bannerBlock, wallBannerBlock, settings.component(DataComponentTypes.BANNER_PATTERNS, BannerPatternsComponent.DEFAULT));
	}

}
