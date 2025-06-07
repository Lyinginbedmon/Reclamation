package com.lying.mixin;

import static com.lying.reference.Reference.ModInfo.prefix;

import java.util.Map;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.lying.client.renderer.LumaSpriteManager;

import net.minecraft.client.render.model.SpriteAtlasManager;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.util.Identifier;

@Mixin(SpriteAtlasManager.class)
public class SpriteAtlasManagerMixin
{
	private static final Map<Identifier, Identifier> EXTRA_ATLASES = Map.of(
			LumaSpriteManager.ATLAS_ID, prefix("banner_patterns")
			);
	
	@Shadow
	private Map<Identifier, SpriteAtlasManager.Atlas> atlases;
	
	@Inject(
			method = "<init>(Ljava/util/Map;Lnet/minecraft/client/texture/TextureManager;)V",
			at = @At("TAIL"))
	private void rcl$AddCustomAtlases(Map<Identifier, Identifier> loaders, TextureManager textureManager, final CallbackInfo ci)
	{
		EXTRA_ATLASES.entrySet().forEach(entry -> 
		{
			Identifier id = entry.getKey();
			SpriteAtlasTexture tex = new SpriteAtlasTexture(id);
			textureManager.registerTexture(id, tex);
			atlases.putIfAbsent(id, new SpriteAtlasManager.Atlas(tex, entry.getValue()));
		});
	}
}
