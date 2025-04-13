package com.lying.init;

import com.lying.Reclamation;

import net.minecraft.world.GameRules;
import net.minecraft.world.GameRules.Category;

public class RCGameRules
{
	private static int tally = 0;
	
	public static final GameRules.Key<GameRules.IntRule> DECAY_SPEED	= register("naturalDecaySpeed", Category.UPDATES, GameRules.IntRule.create(Reclamation.config.naturalDecaySpeed()));
	public static final GameRules.Key<GameRules.IntRule> DECAY_RADIUS	= register("naturalDecayRadius", Category.UPDATES, GameRules.IntRule.create(Reclamation.config.naturalDecayRadius()));
	public static final GameRules.Key<GameRules.BooleanRule> DECAY_SPAWN	= register("spawnNaturalDecay", Category.UPDATES, GameRules.BooleanRule.create(true));
	
	private static <T extends GameRules.Rule<T>> GameRules.Key<T> register(String name, GameRules.Category category, GameRules.Type<T> type)
	{
		tally++;
		return GameRules.register(name, category, type);
	}
	
	public static void init()
	{
		Reclamation.LOGGER.info("# Initialised {} custom gamerules", tally);
	}
}
