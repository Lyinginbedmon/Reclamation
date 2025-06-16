package com.lying;

import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.lying.command.RCCommands;
import com.lying.config.ServerConfig;
import com.lying.decay.DecayLibrary;
import com.lying.decay.DecayMacros;
import com.lying.decay.context.DecayContext;
import com.lying.decay.context.DecayContext.DecayType;
import com.lying.decay.context.LiveDecayContext;
import com.lying.decay.handler.DecayEntry;
import com.lying.event.DecayEvent;
import com.lying.init.RCBlockEntityTypes;
import com.lying.init.RCBlocks;
import com.lying.init.RCDataComponentTypes;
import com.lying.init.RCDecayConditions;
import com.lying.init.RCDecayFunctions;
import com.lying.init.RCGameRules;
import com.lying.init.RCItems;
import com.lying.init.RCParticleTypes;
import com.lying.init.RCSoundEvents;
import com.lying.item.RottenFruitItem;
import com.lying.reference.Reference;

import dev.architectury.event.EventResult;
import dev.architectury.event.events.common.TickEvent;
import net.minecraft.block.BlockState;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;

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
		RCBlockEntityTypes.init();
		RCDataComponentTypes.init();
		RCItems.init();
		RCSoundEvents.init();
		RCParticleTypes.init();
		RCDecayConditions.init();
		RCDecayFunctions.init();
		DecayMacros.init();
		DecayLibrary.init();
		registerServerEvents();
	}
	
	@SuppressWarnings("deprecation")
	private static void registerServerEvents()
	{
		TickEvent.SERVER_POST.register(server -> 
		{
			ServerWorld overworld = server.getOverworld();
			Random rand = overworld.getRandom();
			
			List<ServerPlayerEntity> players = server.getPlayerManager().getPlayerList();
			if(players.isEmpty())
				return;
			
			GameRules gameRules = overworld.getGameRules();
			int radius = gameRules.getInt(RCGameRules.DECAY_RADIUS);
			int rate = gameRules.getInt(RCGameRules.DECAY_SPEED);
			if(rate == 0 || radius == 0)
				return;
			
			// Log of updated blocks to prevent blocks being decayed multiple times in one update
			List<BlockPos> updated = Lists.newArrayList();
			DecayContext root = LiveDecayContext.root(DecayType.NATURAL);
			for(int i=rate; i>0; --i)
			{
				ServerPlayerEntity player = players.size() > 1 ? players.get(rand.nextInt(players.size())) : players.get(0);
				
				int offX = rand.nextBetween(-radius, radius);
				
				// Constrain vertical offset by proximity to world height limits
				// This reduces occasions where natural decay fails because it targets a block outside of the world
				ServerWorld world = player.getServerWorld();
				double yDown = Math.max(world.getBottomY(), player.getY() - radius);
				double yUp = Math.min(world.getTopYInclusive(), player.getY() + radius);
				int offY = rand.nextBetween((int)yDown, (int)yUp);
				
				int offZ = rand.nextBetween(-radius, radius);
				BlockPos randomPos = player.getBlockPos().add(offX, 0, offZ).withY(offY);
				if(updated.contains(randomPos) || !world.isChunkLoaded(randomPos) || !DecayType.NATURAL.canDecayBlock(randomPos, world))
					continue;
				
				Optional<DecayContext> context = tryToDecay(world, root.create(world, randomPos).setParent(root));
				context.ifPresent(root::addChild);
				updated.add(randomPos);
			}
			root.close();
		});
		
		DecayEvent.CAN_DECAY_BLOCK_EVENT.register((pos, world, type) -> 
		{
			// Prevent blocks from decaying naturally within the spawn radius if spawn decay is disabled
			GameRules rules;
			if(type != DecayType.NATURAL || world.getRegistryKey() != World.OVERWORLD || (rules = world.getGameRules()).getBoolean(RCGameRules.DECAY_SPAWN))
				return EventResult.pass();
			
			BlockPos worldSpawn = world.getSpawnPos();
			int spawnRadius = rules.getInt(GameRules.SPAWN_RADIUS);
			return pos.withY(worldSpawn.getY()).isWithinDistance(worldSpawn, spawnRadius) ? EventResult.interruptFalse() : EventResult.pass();
		});
		
		TickEvent.PLAYER_POST.register(RottenFruitItem::fireWearingRottenFruitTickEvent);
		RottenFruitItem.WEARING_ROTTEN_FRUIT_TICK_EVENT_SERVER.register(RottenFruitItem::onWearingRottenFruitTickEvent);
	}
	
	/**
	 * Attempts to decay the context with the entire decay library, checking prohibitions first.
	 * @param world	The world the affected block exists in
	 * @param context	The context of this decay event
	 * @return An Optional containing the affected DecayContext, or an empty Optional if it fails
	 */
	public static Optional<DecayContext> tryToDecay(ServerWorld world, DecayContext context)
	{
		if(context.isRoot()) return Optional.empty();
		BlockPos pos = context.initialPos;
		if(!canBlockDecay(pos, world, Optional.empty()))
			return Optional.empty();
		
		// Natural decay checks validity before calling this function, so everything else is checked here
		if(context.type != DecayType.NATURAL && !context.type.canDecayBlock(pos, world))
			return Optional.empty();
		
		List<DecayEntry> decayOptions = DecayLibrary.instance().getDecayOptions(context);
		DecayEntry decay = decayOptions.size() > 1 ? decayOptions.get(context.random.nextInt(decayOptions.size())) : decayOptions.get(0);
		return decay(world, decay, context);
	}
	
	/**
	 * Attempts to apply the given DecayEntry to the given DecayContext
	 * @param world	The world the affected block exists in
	 * @param entry	The DecayData to apply
	 * @param ignoreConditions	If true, {@link DecayEntry.test} is not checked before application
	 * @param context	The context of this decay event
	 * @return An Optional containing the affected DecayContext, or an empty Optional if it fails
	 */
	public static Optional<DecayContext> tryToDecay(ServerWorld world, DecayEntry entry, boolean ignoreConditions, DecayContext context)
	{
		return ignoreConditions || entry.test(context) ? decay(world, entry, context) : Optional.empty();
	}
	
	/** Applies the given DecayEntry to the given DecayContext, respecting probability */
	public static Optional<DecayContext> decay(ServerWorld world, DecayEntry entry, DecayContext context)
	{
		if(!context.isRoot() && context.random.nextFloat() <= entry.chance(context.currentPos(), world))
			return Optional.of(applyDecay(world, entry, context));
		else
			return Optional.empty();
	}
	
	/** Applies the given DecayEntry to the given DecayContext, without checking odds */
	public static DecayContext applyDecay(ServerWorld world, DecayEntry entry, DecayContext context)
	{
		DecayEvent.BEFORE_BLOCK_DECAY_EVENT.invoker().onBlockDecay(context, entry);
		entry.apply(context);
		DecayEvent.AFTER_BLOCK_DECAY_EVENT.invoker().onBlockDecay(context, entry);
		return context;
	}
	
	public static boolean canBlockDecay(BlockPos pos, ServerWorld world, Optional<ServerCommandSource> source)
	{
		if(world.isOutOfHeightLimit(pos))
		{
			source.ifPresent(s -> s.sendFeedback(() -> Reference.ModInfo.translate("command", "block_outside_world"), false));
			return false;
		}
		
		BlockState state = world.getBlockState(pos);
		if(state.isIn(BlockTags.WITHER_IMMUNE))
		{
			source.ifPresent(s -> s.sendFeedback(() -> Reference.ModInfo.translate("command", "block_wither_immune"), false));
			return false;
		}
		
		List<DecayEntry> decayOptions = DecayLibrary.instance().getDecayOptions(LiveDecayContext.supplier(pos, world, DecayType.NATURAL));
		if(decayOptions.isEmpty())
		{
			source.ifPresent(s -> s.sendFeedback(() -> Reference.ModInfo.translate("command", "block_no_entries"), false));
			return false;
		}
		
		return true;
	}
}
