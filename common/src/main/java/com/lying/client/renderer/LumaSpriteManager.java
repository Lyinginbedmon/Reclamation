package com.lying.client.renderer;

import static com.lying.reference.Reference.ModInfo.prefix;

import dev.architectury.registry.ReloadListenerRegistry;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.texture.SpriteAtlasHolder;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;

public class LumaSpriteManager extends SpriteAtlasHolder
{
	public static Identifier ATLAS_ID = prefix("textures/atlas/luma_mattes.png");
	public static Identifier SOURCE_PATH = prefix("luma_mattes");
	public static LumaSpriteManager INSTANCE = null;
	
	protected LumaSpriteManager(TextureManager textureManager)
	{
		super(textureManager, ATLAS_ID, SOURCE_PATH);
	}
	
	public Sprite getSprite(Identifier id)
	{
		return super.getSprite(id);
	}
	
	public static void init(TextureManager manager)
	{
		if(INSTANCE == null)
			ReloadListenerRegistry.register(ResourceType.CLIENT_RESOURCES, INSTANCE = new LumaSpriteManager(manager));
	}
}
