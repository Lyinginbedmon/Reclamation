package com.lying.decay;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.lying.decay.conditions.ConditionIsBlock;
import com.lying.decay.conditions.ConditionNeighbouring;
import com.lying.decay.functions.FunctionConvert;
import com.lying.decay.handler.DecayMacro;
import com.lying.init.RCBlocks;

import net.minecraft.block.Blocks;
import net.minecraft.block.ConnectingBlock;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Direction;

public class DefaultDecayMacros 
{
	private static final Map<Identifier, DecayMacro> DATA = new HashMap<>();
	
	private static final Map<Direction, BooleanProperty> FACE_CONNECTIONS = Map.of(
			Direction.NORTH, ConnectingBlock.SOUTH,
			Direction.EAST, ConnectingBlock.WEST,
			Direction.SOUTH, ConnectingBlock.NORTH,
			Direction.WEST, ConnectingBlock.EAST);
	
	private static void register(DecayMacro dataIn)
	{
		DATA.put(dataIn.packName(), dataIn);
	}
	
	public static Collection<DecayMacro> getDefaults() { return DATA.values(); }
	
	static
	{
		FACE_CONNECTIONS.entrySet().forEach(entry -> register(DecayMacro.Builder.create()
				.name("place_ivy_"+entry.getKey().asString())
				.condition(
					ConditionIsBlock.of(Blocks.AIR),
					ConditionNeighbouring.Supported.onFaces(entry.getKey()),
					ConditionNeighbouring.Blocks.of(Blocks.GRASS_BLOCK).faces(Direction.DOWN))
				.function(
					FunctionConvert.to(RCBlocks.IVY.get().getDefaultState().with(entry.getValue(), true))).build()));
	}
}
