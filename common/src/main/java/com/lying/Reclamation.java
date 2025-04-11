package com.lying;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.lying.command.RCCommands;
import com.lying.config.ServerConfig;
import com.lying.decay.DecayData;
import com.lying.decay.DecayLibrary;
import com.lying.decay.context.DecayContext;
import com.lying.decay.context.LiveDecayContext;
import com.lying.init.RCBlocks;
import com.lying.init.RCDecayConditions;
import com.lying.init.RCDecayFunctions;
import com.lying.init.RCGameRules;
import com.lying.init.RCItems;
import com.lying.reference.Reference;

import dev.architectury.event.events.common.TickEvent;
import net.minecraft.block.BlockState;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;

public final class Reclamation
{
	public static Logger LOGGER = LoggerFactory.getLogger(Reference.ModInfo.MOD_ID);
	
	public static ServerConfig config;
	
    public static void init()
    {
    	config = new ServerConfig("config/ReclamationServer.cfg");
    	config.read();
    	
    	RCGameRules.init();
    	RCCommands.init();
    	RCBlocks.init();
        RCItems.init();
        RCDecayConditions.init();
        RCDecayFunctions.init();
        DecayLibrary.init();
        registerServerEvents();
    }
    
    private static void registerServerEvents()
    {
    	TickEvent.SERVER_POST.register(server -> 
    	{
    		ServerWorld overworld = server.getOverworld();
    		Random rand = overworld.getRandom();
    		
    		List<ServerPlayerEntity> players = server.getPlayerManager().getPlayerList();
    		if(players.isEmpty())
    			return;
    		
    		// Log of updated blocks to prevent blocks being decayed multiple times in one update
    		List<BlockPos> updated = Lists.newArrayList();
    		int radius = overworld.getGameRules().getInt(RCGameRules.DECAY_RADIUS);
    		for(int i=overworld.getGameRules().getInt(RCGameRules.DECAY_SPEED); i>0; --i)
    		{
    			ServerPlayerEntity player = players.size() > 1 ? players.get(rand.nextInt(players.size())) : players.get(0);
    			
    			int offX = rand.nextBetween(-radius, radius);
    			int offY = rand.nextBetween(-radius, radius);
    			int offZ = rand.nextBetween(-radius, radius);
    			BlockPos randomPos = player.getBlockPos().add(offX, offY, offZ);
    			if(updated.contains(randomPos))
    				continue;
    			
    			tryToDecay(player.getServerWorld(), LiveDecayContext.supplier(randomPos, player.getServerWorld()));
    			updated.add(randomPos);
    		}
    	});
    }
    
    public static DecayContext tryToDecay(ServerWorld world, DecayContext context)
    {
    	BlockPos pos = context.initialPos;
    	if(world.isOutOfHeightLimit(pos))
			return null;
    	
		BlockState state = world.getBlockState(pos);
		if(state.isIn(BlockTags.WITHER_IMMUNE))
			return null;
		
		List<DecayData> decayOptions = DecayLibrary.instance().getDecayOptions(world, pos, state);
		if(decayOptions.isEmpty())
			return null;
		
		DecayData decay = decayOptions.size() > 1 ? decayOptions.get(context.random.nextInt(decayOptions.size())) : decayOptions.get(0);
		return tryToDecay(world, decay, true, context);
    }
    
    public static DecayContext tryToDecay(ServerWorld world, DecayData entry, boolean ignoreConditions, DecayContext context)
    {
		if((ignoreConditions || entry.test(world, context.currentPos(), context.currentState())) && context.random.nextFloat() <= entry.chance(context.currentPos(), world))
		{
			entry.apply(context);
			return context;
		}
		return null;
    }
}
