package com.lying.utility;

import java.util.List;
import java.util.Optional;

import org.jetbrains.annotations.Nullable;

import com.google.common.collect.Lists;
import com.lying.decay.context.DecayContext;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.Heightmap;
import net.minecraft.world.World;

/**
 * Utility class for determining if a given position is in an "interior" or "exterior" location<br>
 * Exterior is defined as a position that can access the skybox through a contiguous chain of block faces
 * Interior is any position that cannot find an exterior position within range
 */
public class ExteriorUtility
{
	/** Hard cap to search iterations */
	private static final int MAX_ITERATIONS	= 1000;
	
	public static final int DEFAULT_SEARCH_RANGE	= 32;
	
	/**
	 * Identifies the nearest position within range that is exposed to the skybox, using a flood fill approach
	 * @param context The context to search from
	 * @param searchRange The farthest distance to search
	 * @return An {@link Optional} containing the position found, or an empty one if nothing was found
	 */
	public static Optional<BlockPos> isBlockInExterior(DecayContext context, int searchRange)
	{
		// TODO Cache result in context where possible to reduce excessive calls
		return isBlockInExterior(context.currentPos(), context.world, searchRange);
	}
	
	// TODO Directed search between two positions
	
	/**
	 * Identifies the nearest position within range that is exposed to the skybox, using a flood fill approach
	 * @param pos The starting position
	 * @param world The world in which to search
	 * @param searchRange The farthest distance to search
	 * @return An {@link Optional} containing the position found, or an empty one if nothing was found
	 */
	public static Optional<BlockPos> isBlockInExterior(BlockPos pos, ServerWorld world, int searchRange)
	{
		if(searchRange < 0 || world.getRegistryKey() == World.NETHER)
			return Optional.empty();
		
		if(isBlockExterior(pos, world))
			return Optional.of(pos);
		
		// Positions that have already been checked
		List<BlockPos> searchSpace = Lists.newArrayList(pos);
		
		// Candidate positions to check
		List<BlockPos> pathsToCheck = Lists.newArrayList(Moves.getAvailable(pos, world, false).stream().map(m -> m.apply(pos)).toList());
		
		// Iterate until search validates or exceeds search range
		int searchLimit = MAX_ITERATIONS;
		while(!pathsToCheck.isEmpty() && searchLimit-- > 0)
		{
			// Next position to check, usually meaning the nearest unchecked position from the origin
			BlockPos current = pathsToCheck.remove(0);
			
			// Update searchspace
			searchSpace.add(current);
			
			Iteration iteration = iterateScan(current, world, searchSpace);
			if(iteration.isSuccess())
				return iteration.terminus();
			
			List<BlockPos> candidates = Lists.newArrayList(iteration.options());
			// Ignore any candidate positions that are already scheduled to be checked
			candidates.removeAll(pathsToCheck);
			// Ignore any candidate positions that are beyond the maximum search range
			candidates.removeIf(p -> p.getSquaredDistance(pos) > searchRange * searchRange);
			pathsToCheck.addAll(candidates);
		}
		return Optional.empty();
	}
	
	/** Returns true if the given block constitutes exterior access */
	private static boolean isBlockExterior(BlockPos pos, ServerWorld world)
	{
		return world.getTopPosition(Heightmap.Type.MOTION_BLOCKING, pos).getY() <= pos.getY();
	}
	
	private static Iteration iterateScan(BlockPos current, ServerWorld world, List<BlockPos> searchSpace)
	{
		List<BlockPos> options = Lists.newArrayList();
		for(Moves move : Moves.values())
		{
			// Moves.getAvailable isn't used here to reduce loop load per iteration
			if(!move.isAvailable(current, world, true))
				continue;
			
			BlockPos terminus = move.apply(current);
			if(isBlockExterior(terminus, world))
				return new Iteration(terminus, List.of(), searchSpace);
			else if(!searchSpace.contains(terminus))
				options.add(terminus);
		}
		return new Iteration(null, options, searchSpace);
	}
	
	private static record Iteration(@Nullable BlockPos success, List<BlockPos> options, List<BlockPos> searchSpace)
	{
		public boolean isSuccess() { return success != null; }
		
		public Optional<BlockPos> terminus() { return isSuccess() ? Optional.of(success) : Optional.empty(); }
	}
	
	public static enum Moves
	{
		UP(Direction.UP),
		DOWN(Direction.DOWN),
		NORTH(Direction.NORTH),
		EAST(Direction.EAST),
		SOUTH(Direction.SOUTH),
		WEST(Direction.WEST);
		
		private final Direction direction;
		
		private Moves(Direction faceIn)
		{
			direction = faceIn;
		}
		
		public BlockPos apply(BlockPos pos) { return pos.offset(direction); }
		
		/**
		 * Checks if this move is available to the given position
		 * @param pos The position to check from
		 * @param world The world to check in
		 * @param checkBidirectional If the reverse move is valid from the destination
		 * @return True if the adjoined face is passable
		 */
		public boolean isAvailable(BlockPos pos, World world, boolean checkBidirectional)
		{
			if(checkBidirectional)
			{
				BlockState state = world.getBlockState(pos);
				if(!isPassable(state, pos, world, direction))
					return false;
			}
			
			BlockPos offset = pos.offset(direction);
			return isPassable(world.getBlockState(offset), offset, world, direction.getOpposite());
		}
		
		private static boolean isPassable(BlockState state, BlockPos pos, World world, Direction face)
		{
			return state.isAir() || state.getCollisionShape(world, pos).isEmpty() || !Block.isFaceFullSquare(state.getSidesShape(world, pos), face);
		}
		
		/** Returns the list of {@link Moves} available to the given position */
		private static List<Moves> getAvailable(BlockPos pos, World world, boolean checkReverse)
		{
			List<Moves> moves = Lists.newArrayList();
			for(Moves move : Moves.values())
				if(move.isAvailable(pos, world, checkReverse))
					moves.add(move);
			return moves;
		}
	}
}
