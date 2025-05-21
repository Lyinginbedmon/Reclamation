package com.lying.command;

import static com.lying.reference.Reference.ModInfo.translate;
import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

import java.util.List;
import java.util.Optional;

import org.jetbrains.annotations.Nullable;

import com.google.common.collect.Lists;
import com.lying.Reclamation;
import com.lying.decay.DecayLibrary;
import com.lying.decay.DecayMacros;
import com.lying.decay.context.DecayContext;
import com.lying.decay.context.DecayContext.DecayType;
import com.lying.decay.context.QueuedDecayContext;
import com.lying.decay.handler.DecayEntry;
import com.lying.decay.handler.DecayMacro;
import com.lying.init.RCGameRules;
import com.lying.reference.Reference;
import com.lying.utility.ExteriorUtility;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.SuggestionProvider;

import dev.architectury.event.events.common.CommandRegistrationEvent;
import net.minecraft.block.BlockState;
import net.minecraft.command.CommandSource;
import net.minecraft.command.argument.BlockPosArgumentType;
import net.minecraft.command.argument.IdentifierArgumentType;
import net.minecraft.command.suggestion.SuggestionProviders;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameRules;
import net.minecraft.world.GameRules.BooleanRule;
import net.minecraft.world.GameRules.IntRule;

public class RCCommands
{
	private static final SimpleCommandExceptionType FAILED_UNKNOWN_ENTRY = make("unrecognised_decay_entry");
	private static final Dynamic2CommandExceptionType TOO_BIG_EXCEPTION = new Dynamic2CommandExceptionType(
			(maxCount, count) -> Text.stringifiedTranslatable("commands.fill.toobig", maxCount, count)
		);
	public static final SuggestionProvider<ServerCommandSource> DECAY_ENTRY_IDS = SuggestionProviders.register(Identifier.of("decay_entries"), (context, builder) -> CommandSource.suggestIdentifiers(DecayLibrary.instance().entries(), builder));
	public static final SuggestionProvider<ServerCommandSource> DECAY_MACRO_IDS = SuggestionProviders.register(Identifier.of("decay_macros"), (context, builder) -> CommandSource.suggestIdentifiers(DecayMacros.instance().entries(), builder));
	
	private static SimpleCommandExceptionType make(String name)
	{
		return new SimpleCommandExceptionType(translate("command", "failed_"+name.toLowerCase()));
	}
	
	public static void init()
	{
		CommandRegistrationEvent.EVENT.register((dispatcher, access, environment) -> 
		{
			dispatcher.register(literal(Reference.ModInfo.MOD_ID).requires(source -> source.hasPermissionLevel(2))
				.then(literal("rate")
					.then(literal("reset")
						.executes(context -> Config.setNaturalDecayRate(-1, context.getSource())))
					.then(literal("set")
						.then(argument("value", IntegerArgumentType.integer(0))
							.executes(context -> Config.setNaturalDecayRate(IntegerArgumentType.getInteger(context, "value"), context.getSource()))))
					.then(literal("get")
						.executes(context -> Config.getNaturalDecayRate(context.getSource())))
					)
				.then(literal("radius")
					.then(literal("reset")
						.executes(context -> Config.setNaturalDecayRadius(-1, context.getSource())))
					.then(literal("set")
						.then(argument("value", IntegerArgumentType.integer(1))
							.executes(context -> Config.setNaturalDecayRadius(IntegerArgumentType.getInteger(context, "value"), context.getSource()))))
					.then(literal("get")
						.executes(context -> Config.getNaturalDecayRadius(context.getSource())))
					)
				.then(literal("spawn")
					.then(literal("reset")
						.executes(context -> Config.setSpawnNaturalDecay(false, context.getSource())))
					.then(literal("set")
						.then(argument("value", BoolArgumentType.bool())
							.executes(context -> Config.setSpawnNaturalDecay(BoolArgumentType.getBool(context, "value"), context.getSource()))))
					.then(literal("get")
						.executes(context -> Config.getSpawnNaturalDecay(context.getSource()))))
				.then(literal("decay")
					.then(argument("from", BlockPosArgumentType.blockPos())
						.executes(context -> Decay.tryDecayRegion(new BlockBox(BlockPosArgumentType.getBlockPos(context, "from")), context.getSource()))
						.then(argument("to", BlockPosArgumentType.blockPos())
							.executes(context -> Decay.tryDecayRegion(BlockBox.create(BlockPosArgumentType.getBlockPos(context, "from"), BlockPosArgumentType.getBlockPos(context, "to")), context.getSource()))
							.then(argument("entry", IdentifierArgumentType.identifier()).suggests(DECAY_ENTRY_IDS)
								.executes(context -> Decay.tryDecayRegionSpecifically(BlockBox.create(BlockPosArgumentType.getBlockPos(context, "from"), BlockPosArgumentType.getBlockPos(context, "to")), IdentifierArgumentType.getIdentifier(context, "entry"), context.getSource()))
								)
							.then(argument("macro", IdentifierArgumentType.identifier()).suggests(DECAY_MACRO_IDS)
								.executes(context -> Decay.tryMacroRegion(BlockBox.create(BlockPosArgumentType.getBlockPos(context, "from"), BlockPosArgumentType.getBlockPos(context, "to")), IdentifierArgumentType.getIdentifier(context, "macro"), context.getSource()))
								)
							)
						)
					)
				.then(literal("chance")
					.then(argument("position", BlockPosArgumentType.blockPos())
						.executes(context -> Info.getDecayChances(BlockPosArgumentType.getBlockPos(context, "position"), context.getSource()))
						.then(argument("entry", IdentifierArgumentType.identifier()).suggests(DECAY_ENTRY_IDS)
							.executes(context -> Info.getSpecificDecayChance(BlockPosArgumentType.getBlockPos(context, "position"), IdentifierArgumentType.getIdentifier(context, "entry"), context.getSource())))))
				.then(literal("exterior")
						.then(argument("pos", BlockPosArgumentType.blockPos())
							.executes(context -> Info.checkBlockExposure(BlockPosArgumentType.getBlockPos(context, "pos"), ExteriorUtility.DEFAULT_SEARCH_RANGE, context.getSource()))
							.then(argument("range", IntegerArgumentType.integer(1))
								.executes(context -> Info.checkBlockExposure(BlockPosArgumentType.getBlockPos(context, "pos"), IntegerArgumentType.getInteger(context, "range"), context.getSource())))))
				);
		});
	}
	
	private static class Config
	{
		private static int getNaturalDecayRate(ServerCommandSource source) throws CommandSyntaxException
		{
			source.sendFeedback(() -> translate("command", "natural_decay_rate.get", source.getWorld().getGameRules().getInt(RCGameRules.DECAY_SPEED)), true);
			return 15;
		}
		
		private static int setNaturalDecayRate(int rate, ServerCommandSource source) throws CommandSyntaxException
		{
			MinecraftServer server = source.getServer();
			ServerWorld world = server.getOverworld();
			IntRule rule = world.getGameRules().get(RCGameRules.DECAY_SPEED);
			if(rate >= 0)
				rule.set(rate, server);
			else
				rule.set(Reclamation.config.naturalDecaySpeed(), server);
			source.sendFeedback(() -> translate("command", "natural_decay_rate.set", source.getWorld().getGameRules().getInt(RCGameRules.DECAY_SPEED)), true);
			return 15;
		}
		
		private static int getNaturalDecayRadius(ServerCommandSource source) throws CommandSyntaxException
		{
			source.sendFeedback(() -> translate("command", "natural_decay_radius.get", source.getWorld().getGameRules().getInt(RCGameRules.DECAY_RADIUS)), true);
			return 15;
		}
		
		private static int setNaturalDecayRadius(int radius, ServerCommandSource source) throws CommandSyntaxException
		{
			MinecraftServer server = source.getServer();
			ServerWorld world = server.getOverworld();
			IntRule rule = world.getGameRules().get(RCGameRules.DECAY_RADIUS);
			if(radius >= 0)
				rule.set(radius, server);
			else
				rule.set(Reclamation.config.naturalDecayRadius(), server);
			source.sendFeedback(() -> translate("command", "natural_decay_radius.set", world.getGameRules().getInt(RCGameRules.DECAY_RADIUS)), true);
			return 15;
		}
		
		private static int getSpawnNaturalDecay(ServerCommandSource source) throws CommandSyntaxException
		{
			source.sendFeedback(() -> translate("command", "spawn_natural_decay.get", source.getWorld().getGameRules().getBoolean(RCGameRules.DECAY_SPAWN)), true);
			return 15;
		}
		
		private static int setSpawnNaturalDecay(@Nullable boolean radius, ServerCommandSource source) throws CommandSyntaxException
		{
			MinecraftServer server = source.getServer();
			ServerWorld world = server.getOverworld();
			BooleanRule rule = world.getGameRules().get(RCGameRules.DECAY_SPAWN);
			rule.set(radius, server);
			source.sendFeedback(() -> translate("command", "spawn_natural_decay.set", world.getGameRules().getBoolean(RCGameRules.DECAY_SPAWN)), true);
			return 15;
		}
	}
	
	private static class Decay
	{
		private static int tryDecayRegion(BlockBox region, ServerCommandSource source) throws CommandSyntaxException
		{
			ServerWorld serverWorld = source.getWorld();
			int totalBlocks = region.getBlockCountX() * region.getBlockCountY() * region.getBlockCountZ();
			int limit = serverWorld.getGameRules().getInt(GameRules.COMMAND_MODIFICATION_BLOCK_LIMIT);
			if(totalBlocks > limit)
				throw TOO_BIG_EXCEPTION.create(limit, totalBlocks);
			
			List<DecayContext> queue = Lists.newArrayList();
			for(BlockPos position : BlockPos.iterate(region.getMinX(), region.getMinY(), region.getMinZ(), region.getMaxX(), region.getMaxY(), region.getMaxZ()))
				Reclamation.tryToDecay(serverWorld, QueuedDecayContext.supplier(position, serverWorld, DecayType.ARTIFICIAL)).ifPresent(context -> queue.add(context));
			queue.forEach(DecayContext::close);
			
			final int decayed = queue.size();
			source.sendFeedback(() -> translate("command", "decay_region", totalBlocks, decayed), true);
			return 15;
		}
		
		private static int tryDecayRegionSpecifically(BlockBox region, Identifier entryType, ServerCommandSource source) throws CommandSyntaxException
		{
			ServerWorld world = source.getWorld();
			int totalBlocks = region.getBlockCountX() * region.getBlockCountY() * region.getBlockCountZ();
			int limit = world.getGameRules().getInt(GameRules.COMMAND_MODIFICATION_BLOCK_LIMIT);
			if(totalBlocks > limit)
				throw TOO_BIG_EXCEPTION.create(limit, totalBlocks);
			
			Optional<DecayEntry> entry = DecayLibrary.instance().get(entryType);
			if(entry.isEmpty())
				throw FAILED_UNKNOWN_ENTRY.create();
			
			List<DecayContext> queue = Lists.newArrayList();
			for(BlockPos pos : BlockPos.iterate(region.getMinX(), region.getMinY(), region.getMinZ(), region.getMaxX(), region.getMaxY(), region.getMaxZ()))
				Reclamation.tryToDecay(world, entry.get(), false, QueuedDecayContext.supplier(pos, world, DecayType.ARTIFICIAL)).ifPresent(context -> queue.add(context));
			queue.forEach(DecayContext::close);
			
			final int decayed = queue.size();
			source.sendFeedback(() -> translate("command", "decay_region", totalBlocks, decayed), true);
			return 15;
		}
		
		private static int tryMacroRegion(BlockBox region, Identifier entryType, ServerCommandSource source) throws CommandSyntaxException
		{
			ServerWorld world = source.getWorld();
			int totalBlocks = region.getBlockCountX() * region.getBlockCountY() * region.getBlockCountZ();
			int limit = world.getGameRules().getInt(GameRules.COMMAND_MODIFICATION_BLOCK_LIMIT);
			if(totalBlocks > limit)
				throw TOO_BIG_EXCEPTION.create(limit, totalBlocks);
			
			Optional<DecayMacro> entry = DecayMacros.instance().get(entryType);
			if(entry.isEmpty())
				throw FAILED_UNKNOWN_ENTRY.create();
			
			List<DecayContext> queue = Lists.newArrayList();
			for(BlockPos pos : BlockPos.iterate(region.getMinX(), region.getMinY(), region.getMinZ(), region.getMaxX(), region.getMaxY(), region.getMaxZ()))
			{
				DecayContext context = QueuedDecayContext.supplier(pos, world, DecayType.ARTIFICIAL);
				if(entry.get().tryToApply(context))
					queue.add(context);
			}
			queue.forEach(DecayContext::close);
			
			final int decayed = queue.size();
			source.sendFeedback(() -> translate("command", "decay_region", totalBlocks, decayed), true);
			return 15;
		}
	}
	
	private static class Info
	{
		private static int getDecayChances(BlockPos pos, ServerCommandSource source) throws CommandSyntaxException
		{
			ServerWorld world = source.getWorld();
			if(!Reclamation.canBlockDecay(pos, world, Optional.of(source)))
				return 15;
			
			BlockState state = world.getBlockState(pos);
			List<DecayEntry> decayOptions = DecayLibrary.instance().getDecayOptions(world, pos, state);
			source.sendFeedback(() -> translate("command", "decay_chance_list", decayOptions.size()), true);
			decayOptions.forEach(entry -> source.sendFeedback(() -> Text.literal(" - ").append(translate("command", "decay_chance_entry", entry.packName().toString(), entry.chance(pos, world))), false));
			
			return 15;
		}
		
		private static int getSpecificDecayChance(BlockPos pos, Identifier entryType, ServerCommandSource source) throws CommandSyntaxException
		{
			ServerWorld world = source.getWorld();
			if(!Reclamation.canBlockDecay(pos, world, Optional.of(source)))
				return 15;
			
			Optional<DecayEntry> entry = DecayLibrary.instance().get(entryType);
			if(entry.isEmpty())
				throw FAILED_UNKNOWN_ENTRY.create();
			entry.ifPresent(e -> 
			{
				if(e.test(world, pos, world.getBlockState(pos)))
					source.sendFeedback(() -> translate("command", "decay_chance_entry", e.packName().toString(), e.chance(pos, world)), true);
				else
					source.sendFeedback(() -> translate("command", "block_entry_invalid"), true);
			});
			return 15;
		}
		
		private static int checkBlockExposure(BlockPos pos, int threshold, ServerCommandSource source) throws CommandSyntaxException
		{
			ServerWorld world = source.getWorld();
			if(pos.getY() < world.getBottomY() || pos.getY() > world.getTopYInclusive())
				throw BlockPosArgumentType.OUT_OF_WORLD_EXCEPTION.create();
			
			Optional<BlockPos> terminus = ExteriorUtility.isBlockInExterior(pos, world, threshold);
			if(terminus.isPresent())
			{
				source.sendFeedback(() -> translate("command", "exterior.success", pos.toShortString(), terminus.get().toShortString()), false);
				return 15;
			}
			else
			{
				source.sendFeedback(() -> translate("command", "exterior.failure", pos.toShortString()), false);
				return 0;
			}
		}
	}
}
