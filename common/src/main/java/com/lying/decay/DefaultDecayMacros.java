package com.lying.decay;

import static com.lying.reference.Reference.ModInfo.prefix;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.lying.block.IvyBlock;
import com.lying.block.MoldBlock;
import com.lying.data.RCBlockTags;
import com.lying.decay.conditions.ConditionIsBlock;
import com.lying.decay.conditions.ConditionNeighbouring;
import com.lying.decay.functions.FunctionBlockState;
import com.lying.decay.functions.FunctionConvert;
import com.lying.decay.functions.FunctionMacro;
import com.lying.decay.handler.DecayMacro;
import com.lying.init.RCBlocks;
import com.lying.init.RCDecayConditions;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Direction;

public class DefaultDecayMacros 
{
	private static final Map<Identifier, DecayMacro> DATA = new HashMap<>();
	
	private static final Map<Direction, BooleanProperty> IVY_FACES = IvyBlock.FACING_PROPERTIES;
	private static final Map<Direction, BooleanProperty> MOLD_FACES = MoldBlock.FACING_PROPERTIES;
	
	public static final Identifier PLACE_FLOWERS	= prefix("place_flowers");
	public static final Identifier PLACE_IVY		= prefix("place_ivy_main");
	public static final Identifier PLACE_MOLD		= prefix("place_mold_main");
	public static final Identifier FADE_TERRACOTTA	= prefix("fade_terracotta_main");
	public static final Identifier BLANK_TERRACOTTA	= prefix("blank_terracotta_main");
	public static final Identifier CRACK_CONCRETE	= prefix("crack_concrete");
	
	private static Identifier register(DecayMacro dataIn)
	{
		DATA.put(dataIn.packName(), dataIn);
		return dataIn.packName();
	}
	
	public static Collection<DecayMacro> getDefaults() { return DATA.values(); }
	
	static
	{
		List<Identifier> ivySet = Lists.newArrayList();
		IVY_FACES.entrySet().forEach(entry -> ivySet.add(register(DecayMacro.Builder.create()
				.name("place_ivy_"+entry.getKey().asString())
				.condition(ConditionNeighbouring.Supported.onFaces(entry.getKey()))
				.function(FunctionConvert.toBlockState(RCBlocks.IVY.get().getDefaultState().with(entry.getValue(), true)))
				.build())));
		
		register(DecayMacro.Builder.create()
				.name(PLACE_IVY)
				.condition(
					RCDecayConditions.IS_REPLACEABLE.get(),
					ConditionIsBlock.of(RCBlocks.IVY.get()).invert()
				)
				.function(FunctionMacro.of(ivySet.toArray(new Identifier[0])).randomised())
				.build());
		
		List<Identifier> moldSet = Lists.newArrayList();
		MOLD_FACES.entrySet().forEach(entry -> moldSet.add(register(DecayMacro.Builder.create()
				.name("place_mold_"+entry.getKey().asString())
				.condition(ConditionNeighbouring.Supported.onFaces(entry.getKey()))
				.function(FunctionConvert.toBlockState(RCBlocks.MOLD.get().getDefaultState().with(entry.getValue(), true)))
				.build())));
		
		register(DecayMacro.Builder.create()
				.name(PLACE_MOLD)
				.condition(
					RCDecayConditions.IS_REPLACEABLE.get(),
					ConditionIsBlock.of(RCBlocks.MOLD.get()).invert()
				)
				.function(FunctionMacro.of(moldSet.toArray(new Identifier[0])).randomised())
				.build());
		
		register(DecayMacro.Builder.create()
				.name(PLACE_FLOWERS)
				.function(FunctionConvert.toBlock(
					Blocks.POPPY, Blocks.DANDELION, 
					Blocks.SHORT_GRASS, Blocks.TALL_GRASS, 
					Blocks.OAK_SAPLING, Blocks.SPRUCE_SAPLING, Blocks.BIRCH_SAPLING, 
					Blocks.BROWN_MUSHROOM, Blocks.RED_MUSHROOM)).build());
		
		List<Identifier> fadeSet = Lists.newArrayList();
		List<Identifier> blankSet = Lists.newArrayList();
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
			fadeSet.add(register(fade));
			
			DecayMacro blank = DecayMacro.Builder.create()
					.name("blank_"+color.asString()+"_terracotta")
					.condition(ConditionIsBlock.of(faded))
					.function(FunctionConvert.toBlock(entry.getValue().blank().get())).build(); 
			blankSet.add(register(blank));
		});
		register(DecayMacro.Builder.create()
				.name(FADE_TERRACOTTA)
				.function(FunctionMacro.of(fadeSet.toArray(new Identifier[0]))).build());
		register(DecayMacro.Builder.create()
				.name(BLANK_TERRACOTTA)
				.function(FunctionMacro.of(blankSet.toArray(new Identifier[0]))).build());
		
		List<Identifier> crackSet = Lists.newArrayList();
		RCBlocks.DYE_TO_CONCRETE.entrySet().forEach(entry -> 
		{
			DyeColor color = entry.getKey();
			Block concrete = entry.getValue().dry().get();
			Block cracked = entry.getValue().cracked().get();
			DecayMacro crack = DecayMacro.Builder.create()
				.name("crack_"+color.asString()+"_concrete")
				.condition(ConditionIsBlock.of(concrete))
				.function(FunctionConvert.toBlock(cracked)).build();
			crackSet.add(register(crack));
		});
		register(DecayMacro.Builder.create()
				.name(CRACK_CONCRETE)
				.condition(ConditionIsBlock.of(RCBlockTags.CONCRETE))
				.function(FunctionMacro.of(crackSet.toArray(new Identifier[0]))).build());
	}
}
