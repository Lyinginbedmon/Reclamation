package com.lying.mixin;

import static com.lying.reference.Reference.ModInfo.prefix;

import java.util.HashMap;
import java.util.Map;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import com.lying.Reclamation;
import com.lying.client.renderer.LumaSpriteManager;

import net.minecraft.client.render.model.SpriteAtlasManager;
import net.minecraft.util.Identifier;

@Mixin(SpriteAtlasManager.class)
public class SpriteAtlasManagerMixin
{
	private static final Map<Identifier, Identifier> EXTRA_ATLASES = Map.of(
			LumaSpriteManager.ATLAS_ID, prefix("banner_patterns")
			);
	
	@ModifyVariable(
			method = "<init>(Ljava/util/Map;Lnet/minecraft/client/texture/TextureManager;)V",
			at = @At("HEAD"), 
			ordinal = 0)
	private static Map<Identifier, Identifier> rcl$appendLoaders(Map<Identifier, Identifier> loaders)
	{
		Map<Identifier, Identifier> modified = new HashMap<>(loaders);
		EXTRA_ATLASES.entrySet().forEach(entry -> modified.putIfAbsent(entry.getKey(), entry.getValue()));
		Reclamation.LOGGER.info(" # Appended {} custom sprite atlases", EXTRA_ATLASES.size());
		return modified;
	}
}
