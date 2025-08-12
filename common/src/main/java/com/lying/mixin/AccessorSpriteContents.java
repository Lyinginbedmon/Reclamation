package com.lying.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.SpriteContents;

@Mixin(SpriteContents.class)
public interface AccessorSpriteContents
{
	@Accessor("image")
	NativeImage image();
}
