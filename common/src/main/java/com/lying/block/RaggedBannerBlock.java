package com.lying.block;

import java.util.Map;

import com.google.common.collect.Maps;
import com.lying.block.entity.RaggedBannerBlockEntity;
import com.lying.init.RCBlocks;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.block.AbstractBannerBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.IntProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.DyeColor;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.RotationPropertyHelper;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.WorldView;
import net.minecraft.world.tick.ScheduledTickView;

public class RaggedBannerBlock extends AbstractBannerBlock
{
	public static final MapCodec<RaggedBannerBlock> CODEC = RecordCodecBuilder.mapCodec(
			instance -> instance.group(DyeColor.CODEC.fieldOf("color").forGetter(AbstractBannerBlock::getColor), createSettingsCodec()).apply(instance, RaggedBannerBlock::new)
		);
	public static final IntProperty ROTATION = Properties.ROTATION;
	private static final Map<DyeColor, Block> COLORED_BANNERS = Maps.<DyeColor, Block>newHashMap();
	private static final VoxelShape SHAPE = Block.createCuboidShape(4.0, 0.0, 4.0, 12.0, 16.0, 12.0);
	
	private final DyeColor color;
	
	@Override
	public MapCodec<RaggedBannerBlock> getCodec() { return CODEC; }
	
	public RaggedBannerBlock(DyeColor dyeColor, Settings settings)
	{
		super(dyeColor, settings);
		color = dyeColor;
		
		COLORED_BANNERS.put(dyeColor, this);
	}
	
	@Override
	public BlockEntity createBlockEntity(BlockPos pos, BlockState state)
	{
		return new RaggedBannerBlockEntity(pos, state, this.color);
	}
	
	@SuppressWarnings("deprecation")
	@Override
	protected boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos)
	{
		return world.getBlockState(pos.down()).isSolid();
	}
	
	@Override
	protected VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context)
	{
		return SHAPE;
	}

	@Override
	public BlockState getPlacementState(ItemPlacementContext ctx)
	{
		return this.getDefaultState().with(ROTATION, Integer.valueOf(RotationPropertyHelper.fromYaw(ctx.getPlayerYaw() + 180.0F)));
	}
	
	@Override
	protected BlockState getStateForNeighborUpdate(
		BlockState state,
		WorldView world,
		ScheduledTickView tickView,
		BlockPos pos,
		Direction direction,
		BlockPos neighborPos,
		BlockState neighborState,
		Random random
	)
	{
		return direction == Direction.DOWN && !state.canPlaceAt(world, pos)
			? Blocks.AIR.getDefaultState()
			: super.getStateForNeighborUpdate(state, world, tickView, pos, direction, neighborPos, neighborState, random);
	}
	
	@Override
	protected BlockState rotate(BlockState state, BlockRotation rotation)
	{
		return state.with(ROTATION, Integer.valueOf(rotation.rotate((Integer)state.get(ROTATION), 16)));
	}
	
	@Override
	protected BlockState mirror(BlockState state, BlockMirror mirror)
	{
		return state.with(ROTATION, Integer.valueOf(mirror.mirror((Integer)state.get(ROTATION), 16)));
	}
	
	@Override
	protected void appendProperties(StateManager.Builder<Block, BlockState> builder)
	{
		builder.add(ROTATION);
	}
	
	public static Block getForColor(DyeColor color)
	{
		return (Block)COLORED_BANNERS.getOrDefault(color, RCBlocks.WHITE_RAGGED_BANNER.get());
	}
	
	public static Block[] getRegistered()
	{
		return COLORED_BANNERS.values().toArray(new Block[0]);
	}
}
