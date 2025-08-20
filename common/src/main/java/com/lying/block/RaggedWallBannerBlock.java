package com.lying.block;

import java.util.Map;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.lying.init.RCBlocks;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.block.AbstractBannerBlock;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.HorizontalFacingBlock;
import net.minecraft.block.ShapeContext;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.DyeColor;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.WorldView;
import net.minecraft.world.tick.ScheduledTickView;

public class RaggedWallBannerBlock extends AbstractRaggedBannerBlock
{
	public static final MapCodec<RaggedWallBannerBlock> CODEC = RecordCodecBuilder.mapCodec(
		instance -> instance.group(DyeColor.CODEC.fieldOf("color").forGetter(AbstractBannerBlock::getColor), createSettingsCodec())
				.apply(instance, RaggedWallBannerBlock::new)
	);
	public static final EnumProperty<Direction> FACING = HorizontalFacingBlock.FACING;
	private static final Map<Direction, VoxelShape> FACING_TO_SHAPE = Maps.newEnumMap(
		ImmutableMap.of(
			Direction.NORTH,
			Block.createCuboidShape(0.0, 0.0, 14.0, 16.0, 12.5, 16.0),
			Direction.SOUTH,
			Block.createCuboidShape(0.0, 0.0, 0.0, 16.0, 12.5, 2.0),
			Direction.WEST,
			Block.createCuboidShape(14.0, 0.0, 0.0, 16.0, 12.5, 16.0),
			Direction.EAST,
			Block.createCuboidShape(0.0, 0.0, 0.0, 2.0, 12.5, 16.0)
		)
	);
	private static final Map<DyeColor, Block> COLORED_BANNERS = Maps.<DyeColor, Block>newHashMap();
	
	public MapCodec<RaggedWallBannerBlock> getCodec() { return CODEC; }
	
	public RaggedWallBannerBlock(DyeColor dyeColor, AbstractBlock.Settings settings)
	{
		super(dyeColor, settings);
		this.setDefaultState(this.stateManager.getDefaultState().with(FACING, Direction.NORTH));
		COLORED_BANNERS.put(dyeColor, this);
	}

	@SuppressWarnings("deprecation")
	protected boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos)
	{
		return world.getBlockState(pos.offset(((Direction)state.get(FACING)).getOpposite())).isSolid();
	}
	
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
		return direction == ((Direction)state.get(FACING)).getOpposite() && !state.canPlaceAt(world, pos)
			? Blocks.AIR.getDefaultState()
			: super.getStateForNeighborUpdate(state, world, tickView, pos, direction, neighborPos, neighborState, random);
	}
	
	protected VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context)
	{
		return (VoxelShape)FACING_TO_SHAPE.get(state.get(FACING));
	}
	
	public BlockState getPlacementState(ItemPlacementContext ctx)
	{
		BlockState blockState = this.getDefaultState();
		WorldView worldView = ctx.getWorld();
		BlockPos blockPos = ctx.getBlockPos();
		Direction[] directions = ctx.getPlacementDirections();
		
		for (Direction direction : directions)
			if (direction.getAxis().isHorizontal()) {
				Direction direction2 = direction.getOpposite();
				blockState = blockState.with(FACING, direction2);
				if (blockState.canPlaceAt(worldView, blockPos)) {
					return blockState;
				}
			}

		return null;
	}
	
	protected BlockState rotate(BlockState state, BlockRotation rotation)
	{
		return state.with(FACING, rotation.rotate(state.get(FACING)));
	}
	
	protected BlockState mirror(BlockState state, BlockMirror mirror)
	{
		return state.rotate(mirror.getRotation(state.get(FACING)));
	}
	
	protected void appendProperties(StateManager.Builder<Block, BlockState> builder)
	{
		builder.add(FACING);
	}
	
	public static Block getForColor(DyeColor color)
	{
		return (Block)COLORED_BANNERS.getOrDefault(color, RCBlocks.DYE_TO_BANNER.get(DyeColor.WHITE).wallRagged().get());
	}
	
	public static Block[] getRegistered()
	{
		return COLORED_BANNERS.values().toArray(new Block[0]);
	}
}
