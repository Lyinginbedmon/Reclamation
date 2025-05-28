package com.lying.block;

import java.util.List;
import java.util.function.Function;

import org.jetbrains.annotations.Nullable;

import net.minecraft.block.BlockState;
import net.minecraft.block.SideShapeType;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.BlockView;
import net.minecraft.world.GameRules;

/** Handler interface for a block that grows across exterior block faces */
public interface IFaceBlock
{
	public List<GrowthOption> getGrowthOptions();
	
	public BlockState getInitialState();
	
	public boolean shouldHaveSide(BlockView world, BlockPos pos, Direction side);
	
	@Nullable
	public BooleanProperty getFacingProperty(Direction face);
	
	public default void applyGrowth(BlockState state, ServerWorld world, BlockPos pos, Random random)
	{
		if(!world.getGameRules().getBoolean(GameRules.DO_VINES_SPREAD) || random.nextInt(4) > 0)
			return;
		
		getGrowthOptions().stream().filter(g -> g.viable(state, pos, world)).findAny().ifPresent(g -> g.enact(state, pos, world));
	}
	
	public default boolean hasSide(BlockState state, Direction side)
	{
		return getFacingProperty(side) != null && state.get(getFacingProperty(side));
	}
	
	public default GrowthOption growClockwise(Direction face)
	{
		return turn("grow_"+face.asString()+"_clockwise", face, Direction::rotateYClockwise);
	}
	
	public default GrowthOption growCounterClockwise(Direction face)
	{
		return turn("grow_"+face.asString()+"_counter_clockwise", face, Direction::rotateYCounterclockwise);
	}
	
	public default GrowthOption turn(String name, Direction face, Function<Direction,Direction> rotator)
	{
		return new GrowthOption(name)
		{
			public boolean viable(BlockState state, BlockPos pos, ServerWorld world)
			{
				if(!state.get(getFacingProperty(face)))
					return false;
				
				Direction side = rotator.apply(face.getOpposite());
				
				// Corner occlusion
				BlockPos cornerBlock = pos.offset(side);
				if(!world.isAir(cornerBlock))
				{
					BlockState corner = world.getBlockState(cornerBlock);
					if(corner.isSideSolid(world, cornerBlock, face, SideShapeType.FULL) || corner.isSideSolid(world, cornerBlock, side.getOpposite(), SideShapeType.FULL))
						return false;
				}
				
				// Target validity
				BlockPos targetBlock = pos.offset(face).offset(side);
				BlockState targetState = world.getBlockState(targetBlock);
				if(!(targetState.isAir() || targetState.isOf(state.getBlock()) && !targetState.get(getFacingProperty(side.getOpposite()))))
					return false;
				
				return shouldHaveSide(world, targetBlock, side.getOpposite());
			}
			
			public void enact(BlockState state, BlockPos pos, ServerWorld world)
			{
				Direction side = rotator.apply(face.getOpposite());
				BlockPos targetBlock = pos.offset(face).offset(side);
				BlockState stateAt = world.getBlockState(targetBlock);
				
				if(stateAt.isOf(state.getBlock()))
					stateAt = stateAt.with(getFacingProperty(side.getOpposite()), true);
				else
					stateAt = getInitialState().with(getFacingProperty(side.getOpposite()), true);
				
				world.setBlockState(targetBlock, stateAt, 2);
			}
		};
	}
	
	public default GrowthOption growInDirection(Direction direction)
	{
		return new GrowthOption("grow_"+direction.asString()) 
		{
			public boolean viable(BlockState state, BlockPos pos, ServerWorld world)
			{
				BlockPos offset = pos.offset(direction);
				/**
				 * Offset must be air
				 * State must not be attached in offset direction
				 * State must not be able to attach in offset direction
				 * Offset must have at least one matching attachment with current state
				 */
				return 
						world.isAir(offset) && 
						!hasSide(state, direction) && 
						!shouldHaveSide(world, pos, direction) && 
						Direction.Type.HORIZONTAL.stream().anyMatch(d -> hasSide(state, d) && shouldHaveSide(world, offset, d));
			}
			
			public void enact(BlockState state, BlockPos pos, ServerWorld world)
			{
				BlockPos offset = pos.offset(direction);
				Direction.Type.HORIZONTAL
					.stream().filter(d -> shouldHaveSide(world, offset, d))
					.map(d -> getFacingProperty(d)).filter(state::get)
						.findAny().ifPresent(p -> world.setBlockState(offset, getInitialState().with(p, true), 2));
			}
		};
	}
	
	public default GrowthOption growOnFace(Direction direction)
	{
		return new GrowthOption("expand_to_"+direction.asString())
		{
			public boolean viable(BlockState state, BlockPos pos, ServerWorld world)
			{
				// Attachment must be available and not currently present in state
				if(hasSide(state, direction) || !shouldHaveSide(world, pos, direction))
					return false;
				
				// State has any horizontal and direction is vertical
				if(direction.getAxis() == Axis.Y && Direction.Type.HORIZONTAL.stream().map(d -> getFacingProperty(d)).anyMatch(state::get))
					return true;
				else if(direction.getHorizontalQuarterTurns() >= 0)
				{
					// State has vertical and direction is horizontal
					if(hasSide(state, Direction.UP) || hasSide(state, Direction.DOWN))
						return true;
					
					// Direction is adjacent to an existing horizontal
					if(Direction.Type.HORIZONTAL.stream().filter(d -> d == direction.rotateYClockwise() || d == direction.rotateYCounterclockwise()).map(d -> getFacingProperty(d)).anyMatch(state::get))
						return true;
					
					// Direction is adjacent to another vine block with the same direction
					BlockState neighbour;
					if(
						(neighbour = world.getBlockState(pos.offset(direction.rotateYClockwise()))).isOf(state.getBlock()) && neighbour.get(getFacingProperty(direction)) ||
						(neighbour = world.getBlockState(pos.offset(direction.rotateYCounterclockwise()))).isOf(state.getBlock()) && neighbour.get(getFacingProperty(direction))
						)
						return true;
				}
				return false;
			}
			
			public void enact(BlockState state, BlockPos pos, ServerWorld world) { world.setBlockState(pos, state.with(getFacingProperty(direction), true), 2); }
		};
	}
}
