package com.lying.decay;

import java.io.Reader;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.base.Predicates;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.lying.Reclamation;
import com.lying.data.ReloadListener;
import com.lying.reference.Reference;

import dev.architectury.registry.ReloadListenerRegistry;
import net.minecraft.block.BlockState;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceType;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.math.BlockPos;

public class DecayLibrary implements ReloadListener<Map<Identifier, JsonObject>>
{
	public static final Gson GSON = (new GsonBuilder()).setPrettyPrinting().disableHtmlEscaping().create();
	public static final String FILE_PATH = "decay_library";
	public static final Pattern REGEX = Pattern.compile("[ \\w-]+?(?=\\.)");
	
	private static DecayLibrary INSTANCE;
	
	private final Map<Identifier, DecayEntry> DATA = new HashMap<>();
	
	public static void init()
	{
		INSTANCE = new DecayLibrary();
		ReloadListenerRegistry.register(ResourceType.SERVER_DATA, INSTANCE, INSTANCE.getId());
	}
	
	public static DecayLibrary instance() { return INSTANCE; }
	
	public Identifier getId() { return Reference.ModInfo.prefix(FILE_PATH); }
	
	private void clear() { DATA.clear(); }
	
	/** Returns a list of all decay entries applicable to the given block */
	public List<DecayEntry> getDecayOptions(ServerWorld world, BlockPos pos, BlockState state)
	{
		return DATA.values().stream().filter(d -> d.test(world, pos, state)).toList();
	}
	
	public Optional<DecayEntry> get(Identifier id) { return DATA.containsKey(id) ? Optional.of(DATA.get(id)) : Optional.empty(); }
	
	public Collection<Identifier> entries() { return DATA.keySet(); }
	
	public void register(DecayEntry dataIn)
	{
		DATA.put(dataIn.packName(), dataIn);
		Reclamation.LOGGER.info(" #  Loaded decay entry {}", dataIn.packName());
	}
	
	public CompletableFuture<Map<Identifier, JsonObject>> load(ResourceManager manager)
	{
		return CompletableFuture.supplyAsync(() -> 
		{
			Map<Identifier, JsonObject> objects = new HashMap<>();
			manager.findAllResources(FILE_PATH, Predicates.alwaysTrue()).forEach((fileName,fileSet) -> 
			{
				// The datapack source this species came from
				String namespace = fileName.getNamespace();
				
				// The filename of this species, to be used as the registry name
				String name = fileName.getPath();
				Matcher match = REGEX.matcher(name);
				if(!match.find())
					return;
				
				name = match.group().replaceAll(" ", "_");
				Identifier registryID = Identifier.of(namespace, name);
				Resource file = fileSet.getFirst();
				try
				{
					objects.put(registryID, JsonHelper.deserialize(GSON, (Reader)file.getReader(), JsonObject.class));
				}
				catch(Exception e) { Reclamation.LOGGER.error("Error while loading decay library entry "+fileName.toString()); }
			});
			return objects;
		});
	}
	
	public CompletableFuture<Void> apply(Map<Identifier, JsonObject> data, ResourceManager manager, Executor executor)
	{
		return CompletableFuture.runAsync(() -> 
		{
			Reclamation.LOGGER.info(" # Loading RC decay library...");
			clear();
			for(Entry<Identifier, JsonObject> prep : data.entrySet())
				register(DecayEntry.readFromJson(prep.getKey(), prep.getValue()));
		});
	}
}
