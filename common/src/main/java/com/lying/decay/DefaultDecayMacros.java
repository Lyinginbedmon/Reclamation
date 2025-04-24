package com.lying.decay;

import static com.lying.reference.Reference.ModInfo.prefix;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.lying.decay.conditions.ConditionIsBlock;
import com.lying.decay.conditions.ConditionNeighbouring;
import com.lying.decay.functions.FunctionBlockState;
import com.lying.decay.functions.FunctionConvert;
import com.lying.decay.functions.FunctionMacro;
import com.lying.decay.handler.DecayMacro;
import com.lying.init.RCBlocks;
import com.lying.init.RCDecayConditions;
import com.lying.reference.Reference;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.ConnectingBlock;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.DyeColor;
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
	public static final Identifier FADE_TERRACOTTA	= prefix("fade_terracotta_main");
	public static final Identifier BLANK_TERRACOTTA	= prefix("blank_terracotta_main");
	
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
		
		List<DecayMacro> fadeSet = Lists.newArrayList();
		List<DecayMacro> blankSet = Lists.newArrayList();
		RCBlocks.DYE_TO_TERRACOTTA.entrySet().forEach(entry -> 
		{
			DyeColor color = entry.getKey();
			Block faded = entry.getValue().faded().get();
			DecayMacro fade = DecayMacro.Builder.create()
				.name("fade_"+color.asString()+"_terracotta")
				.condition(ConditionIsBlock.of(entry.getValue().glazed().get()))
				.function(
					FunctionConvert.toBlock(faded),
					FunctionBlockState.CopyValue.of(Properties.HORIZONTAL_FACING)).build(); 
			register(fade);
			fadeSet.add(fade);
			
			DecayMacro blank = DecayMacro.Builder.create()
					.name("blank_"+color.asString()+"_terracotta")
					.condition(ConditionIsBlock.of(faded))
					.function(FunctionConvert.toBlock(entry.getValue().blank().get())).build(); 
			register(blank);
			blankSet.add(blank);
		});
		register(DecayMacro.Builder.create()
				.name(FADE_TERRACOTTA)
				.function(FunctionMacro.of(fadeSet.stream().map(DecayMacro::packName).toList().toArray(new Identifier[0]))).build());
		register(DecayMacro.Builder.create()
				.name(BLANK_TERRACOTTA)
				.function(FunctionMacro.of(blankSet.stream().map(DecayMacro::packName).toList().toArray(new Identifier[0]))).build());
	}
}
