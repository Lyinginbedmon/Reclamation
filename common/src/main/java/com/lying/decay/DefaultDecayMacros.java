package com.lying.decay;

import static com.lying.reference.Reference.ModInfo.prefix;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.lying.decay.conditions.ConditionNeighbouring;
import com.lying.decay.functions.FunctionConvert;
import com.lying.decay.functions.FunctionMacro;
import com.lying.decay.handler.DecayMacro;
import com.lying.init.RCBlocks;
import com.lying.init.RCDecayConditions;
import com.lying.reference.Reference;

import net.minecraft.block.Blocks;
import net.minecraft.block.ConnectingBlock;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Direction;

public class DefaultDecayMacros 
{
	private static final Map<Identifier, DecayMacro> DATA = new HashMap<>();
	
	private static final Map<Direction, BooleanProperty> FACE_CONNECTIONS = Map.of(
			Direction.NORTH, ConnectingBlock.NORTH,
			Direction.EAST, ConnectingBlock.EAST,
			Direction.SOUTH, ConnectingBlock.SOUTH,
			Direction.WEST, ConnectingBlock.WEST);
	
	public static final Identifier PLACE_FLOWERS	= prefix("place_flowers");
	public static final Identifier PLACE_IVY		= prefix("place_ivy_main");
	
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
					RCDecayConditions.IS_REPLACEABLE.get(),
					ConditionNeighbouring.Supported.onFaces(entry.getKey()),
					ConditionNeighbouring.Blocks.of(Blocks.GRASS_BLOCK).faces(Direction.DOWN))
				.function(
					FunctionConvert.toBlockState(RCBlocks.IVY.get().getDefaultState().with(entry.getValue(), true))).build()));
		
		register(DecayMacro.Builder.create()
				.name(PLACE_IVY)
				.function(FunctionMacro.of(FACE_CONNECTIONS.keySet().stream().map(Direction::asString).map(s -> "place_ivy_"+s).map(s -> Reference.ModInfo.prefix(s)).toList().toArray(new Identifier[0]))).build());
		
		register(DecayMacro.Builder.create()
				.name(PLACE_FLOWERS)
				.function(FunctionConvert.toBlock(
					Blocks.POPPY, Blocks.DANDELION, 
					Blocks.SHORT_GRASS, Blocks.TALL_GRASS, 
					Blocks.OAK_SAPLING, Blocks.SPRUCE_SAPLING, Blocks.BIRCH_SAPLING, 
					Blocks.BROWN_MUSHROOM, Blocks.RED_MUSHROOM)).build());
	}
}
