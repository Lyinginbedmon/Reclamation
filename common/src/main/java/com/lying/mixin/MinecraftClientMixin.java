package com.lying.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.lying.client.renderer.LumaSpriteManager;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.RunArgs;
import net.minecraft.client.texture.TextureManager;

@Mixin(MinecraftClient.class)
public class MinecraftClientMixin
{
	@Shadow
	public TextureManager getTextureManager() { return null; }
	
	@Inject(
			method = "<init>(Lnet/minecraft/client/RunArgs;)V", 
			at = @At(value = "INVOKE", target = "Lnet/minecraft/client/texture/PaintingManager;<init>(Lnet/minecraft/client/texture/TextureManager;)V"))
	private void rcl$Constructor(RunArgs args, final CallbackInfo ci)
	{
		LumaSpriteManager.init(getTextureManager());
	}
}
