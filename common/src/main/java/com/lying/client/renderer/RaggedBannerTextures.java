package com.lying.client.renderer;

import static com.lying.reference.Reference.ModInfo.prefix;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import com.google.common.collect.Lists;
import com.lying.Reclamation;
import com.lying.data.ReloadListener;
import com.lying.reference.Reference;

import dev.architectury.event.events.common.PlayerEvent;
import dev.architectury.registry.ReloadListenerRegistry;
import net.minecraft.block.entity.BannerPattern;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.render.TexturedRenderLayers;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.texture.SpriteContents;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.ColorHelper;

public class RaggedBannerTextures implements ReloadListener<Integer>
{
	private static final MinecraftClient mc = MinecraftClient.getInstance();
	private static final SpriteIdentifier LUMA_MATTE	= new SpriteIdentifier(RCBannerSpriteManager.ATLAS_ID, prefix("banner_luma"));
	private static final int FLAT_MATTE	= ColorHelper.fromFloats(1F, 1F, 1F, 1F);
	public static final String FILE_PATH = "ragged_banner";
	
	public static RaggedBannerTextures INSTANCE;
	
	private Identifier bannerBase = null;
	private final List<RegistryEntry<BannerPattern>> generatedPatterns = Lists.newArrayList();
	
	public Identifier getId() { return Reference.ModInfo.prefix(FILE_PATH); }
	
	public static void init()
	{
		INSTANCE = new RaggedBannerTextures();
		ReloadListenerRegistry.register(ResourceType.SERVER_DATA, INSTANCE, INSTANCE.getId());
		
		PlayerEvent.PLAYER_QUIT.register((player) -> INSTANCE.reset());
	}
	
	private void reset()
	{
		if(bannerBase != null)
			mc.getTextureManager().destroyTexture(bannerBase);
		bannerBase = null;
		
		generatedPatterns.forEach(pattern -> mc.getTextureManager().destroyTexture(getRaggedPattern(pattern)));
		generatedPatterns.clear();
	}
	
	public Identifier getRaggedBannerBase()
	{
		if(bannerBase == null)
		{
			Reclamation.LOGGER.info(" # Generating masked banner base");
			Sprite sprite = TexturedRenderLayers.BANNER_BASE.getSprite();
			TextureManager texManager = mc.getTextureManager();
			texManager.registerTexture((bannerBase = prefix("entity/banner_base")), tatterPattern(sprite, texManager));
		}
		return bannerBase;
	}
	
	public Identifier getRaggedPattern(RegistryEntry<BannerPattern> entry)
	{
		if(!generatedPatterns.contains(entry))
			generatePattern(entry);
		return patternToID(entry);
	}
	
	public static Identifier patternToID(RegistryEntry<BannerPattern> entry)
	{
		Identifier id = entry.value().assetId();
		id = prefix(id.getPath());
		return id.withPrefixedPath("ragged_banner/");
	}
	
	private void generatePattern(RegistryEntry<BannerPattern> entry)
	{
		Reclamation.LOGGER.info(" # Generating masked banner pattern for {}", entry.value().assetId().toString());
		Sprite sprite = TexturedRenderLayers.getBannerPatternTextureId(entry).getSprite();
		TextureManager texManager = mc.getTextureManager();
		texManager.registerTexture(patternToID(entry), tatterPattern(sprite, texManager));
		generatedPatterns.add(entry);
	}
	
	// FIXME Ensure luma & source image are allocated
	private static NativeImageBackedTexture tatterPattern(Sprite sprite, TextureManager manager)
	{
		SpriteContents contents = sprite.getContents();
		NativeImage image = contents.image;
		
		SpriteContents matte = LUMA_MATTE.getSprite().getContents();
		NativeImage luma = matte.image;
		
		boolean imageNotLoaded = false, lumaNotLoaded = false;
		NativeImage masked = new NativeImage(image.getFormat(), image.getWidth(), image.getHeight(), true);
		if(masked.getWidth() > 0 && masked.getHeight() > 0)
			for(int x=0; x<masked.getWidth(); x++)
				for(int y=0; y<masked.getHeight(); y++)
				{
					// Pixel ARGB value from source image
					int base = FLAT_MATTE;
					if(!imageNotLoaded)
						try
						{
							base = image.getColorArgb(x, y);
						}
						catch(IllegalStateException e) { imageNotLoaded = true; }
					
					// Pixel ARGB value from luma matte
					int lumaValue = FLAT_MATTE;
					if(!lumaNotLoaded && x<luma.getWidth() && y<luma.getHeight())
						try
						{
							lumaValue = luma.getColorArgb(x, y);
						}
						catch(IllegalStateException e) { lumaNotLoaded = true; }
					
					masked.setColorArgb(x, y, maskValue(base, lumaValue));
				}
		matte.close();
		contents.close();
		
		if(imageNotLoaded)
			Reclamation.LOGGER.warn(" !! Error generating ragged texture, image is not allocated !!");
		else if(lumaNotLoaded)
			Reclamation.LOGGER.warn(" !! Error generating ragged texture, luma matte is not allocated !!");
		
		return new NativeImageBackedTexture(masked);
	}
	
	private static int maskValue(int value, int mask)
	{
		int vR = ColorHelper.getRed(value);
		int vG = ColorHelper.getGreen(value);
		int vB = ColorHelper.getBlue(value);
		
		float vA = ColorHelper.getAlpha(value);
		float luminance = ColorHelper.getRed(ColorHelper.grayscale(mask)) / 255F;
		
		return ColorHelper.getArgb((int)(vA * luminance), vR, vG, vB);
	}
	
	public CompletableFuture<Integer> load(ResourceManager manager)
	{
		return CompletableFuture.supplyAsync(() -> 
		{
			ClientPlayNetworkHandler networkHandler = mc.getNetworkHandler();
			if(networkHandler == null)
				return 0;
			
			reset();
			Registry<BannerPattern> registry = networkHandler.getRegistryManager().getOrThrow(RegistryKeys.BANNER_PATTERN);
			for(int i=registry.size(); i>0; i--)
				generatePattern(registry.getEntry(registry.get(i)));
			return registry.size();
		});
	}
	
	public CompletableFuture<Void> apply(Integer data, ResourceManager manager, Executor executor)
	{
		return CompletableFuture.runAsync(() -> Reclamation.LOGGER.info(" # Generated {} masked banner patterns", data));
	}
}
