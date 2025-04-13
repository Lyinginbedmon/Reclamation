package com.lying.command;

import static com.lying.reference.Reference.ModInfo.translate;
import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

import java.util.List;
import java.util.Optional;

import org.jetbrains.annotations.Nullable;

import com.google.common.collect.Lists;
import com.lying.Reclamation;
import com.lying.decay.DecayEntry;
import com.lying.decay.DecayLibrary;
import com.lying.decay.context.DecayContext;
import com.lying.decay.context.DecayContext.DecayType;
import com.lying.decay.context.QueuedDecayContext;
import com.lying.init.RCGameRules;
import com.lying.reference.Reference;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.SuggestionProvider;

import dev.architectury.event.events.common.CommandRegistrationEvent;
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
						.executes(context -> setNaturalDecayRate(-1, context.getSource())))
					.then(literal("set")
						.then(argument("value", IntegerArgumentType.integer(0))
							.executes(context -> setNaturalDecayRate(IntegerArgumentType.getInteger(context, "value"), context.getSource()))))
					.then(literal("get")
						.executes(context -> getNaturalDecayRate(context.getSource())))
					)
				.then(literal("radius")
					.then(literal("reset")
						.executes(context -> setNaturalDecayRadius(-1, context.getSource())))
					.then(literal("set")
						.then(argument("value", IntegerArgumentType.integer(1))
							.executes(context -> setNaturalDecayRadius(IntegerArgumentType.getInteger(context, "value"), context.getSource()))))
					.then(literal("get")
						.executes(context -> getNaturalDecayRadius(context.getSource())))
					)
				.then(literal("spawn")
					.then(literal("reset")
						.executes(context -> setSpawnNaturalDecay(false, context.getSource())))
					.then(literal("set")
						.then(argument("value", BoolArgumentType.bool())
							.executes(context -> setSpawnNaturalDecay(BoolArgumentType.getBool(context, "value"), context.getSource()))))
					.then(literal("get")
						.executes(context -> getSpawnNaturalDecay(context.getSource()))))
				.then(literal("decay")
					.then(argument("from", BlockPosArgumentType.blockPos())
						.executes(context -> tryDecayRegion(new BlockBox(BlockPosArgumentType.getBlockPos(context, "from")), context.getSource()))
						.then(argument("to", BlockPosArgumentType.blockPos())
							.executes(context -> tryDecayRegion(BlockBox.create(BlockPosArgumentType.getBlockPos(context, "from"), BlockPosArgumentType.getBlockPos(context, "to")), context.getSource()))
							.then(argument("entry", IdentifierArgumentType.identifier()).suggests(DECAY_ENTRY_IDS)
								.executes(context -> tryDecayRegionSpecifically(BlockBox.create(BlockPosArgumentType.getBlockPos(context, "from"), BlockPosArgumentType.getBlockPos(context, "to")), IdentifierArgumentType.getIdentifier(context, "entry"), context.getSource()))
								)
							)
						)
					)
				);
		});
	}
	
	private static int getNaturalDecayRate(ServerCommandSource source) throws CommandSyntaxException
	{
		source.sendFeedback(() -> Reference.ModInfo.translate("command", "natural_decay_rate.get", source.getWorld().getGameRules().getInt(RCGameRules.DECAY_SPEED)), true);
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
		source.sendFeedback(() -> Reference.ModInfo.translate("command", "natural_decay_rate.set", source.getWorld().getGameRules().getInt(RCGameRules.DECAY_SPEED)), true);
		return 15;
	}
	
	private static int getNaturalDecayRadius(ServerCommandSource source) throws CommandSyntaxException
	{
		source.sendFeedback(() -> Reference.ModInfo.translate("command", "natural_decay_radius.get", source.getWorld().getGameRules().getInt(RCGameRules.DECAY_RADIUS)), true);
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
		source.sendFeedback(() -> Reference.ModInfo.translate("command", "natural_decay_radius.set", world.getGameRules().getInt(RCGameRules.DECAY_RADIUS)), true);
		return 15;
	}
	
	private static int getSpawnNaturalDecay(ServerCommandSource source) throws CommandSyntaxException
	{
		source.sendFeedback(() -> Reference.ModInfo.translate("command", "spawn_natural_decay.get", source.getWorld().getGameRules().getBoolean(RCGameRules.DECAY_SPAWN)), true);
		return 15;
	}
	
	private static int setSpawnNaturalDecay(@Nullable boolean radius, ServerCommandSource source) throws CommandSyntaxException
	{
		MinecraftServer server = source.getServer();
		ServerWorld world = server.getOverworld();
		BooleanRule rule = world.getGameRules().get(RCGameRules.DECAY_SPAWN);
		rule.set(radius, server);
		source.sendFeedback(() -> Reference.ModInfo.translate("command", "spawn_natural_decay.set", world.getGameRules().getBoolean(RCGameRules.DECAY_SPAWN)), true);
		return 15;
	}
	
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
		source.sendFeedback(() -> Reference.ModInfo.translate("command", "decay_region", totalBlocks, decayed), true);
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
		source.sendFeedback(() -> Reference.ModInfo.translate("command", "decay_region", totalBlocks, decayed), true);
		return 15;
	}
}
