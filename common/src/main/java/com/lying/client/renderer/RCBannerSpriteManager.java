package com.lying.client.renderer;

import static com.lying.reference.Reference.ModInfo.prefix;

import dev.architectury.registry.ReloadListenerRegistry;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.texture.SpriteAtlasHolder;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;

public class RCBannerSpriteManager extends SpriteAtlasHolder
{
	public static Identifier ATLAS_ID = prefix("textures/atlas/banner_patterns.png");
	public static Identifier SOURCE_PATH = prefix("banner_patterns");
	public static RCBannerSpriteManager INSTANCE = null;
	
	protected RCBannerSpriteManager(TextureManager textureManager)
	{
		super(textureManager, ATLAS_ID, SOURCE_PATH);
	}
	
	public static void init(TextureManager manager)
	{
		if(INSTANCE == null)
			ReloadListenerRegistry.register(ResourceType.CLIENT_RESOURCES, INSTANCE = new RCBannerSpriteManager(manager));
	}
	
	public Sprite getSprite(Identifier id)
	{
		return super.getSprite(id);
	}
}
