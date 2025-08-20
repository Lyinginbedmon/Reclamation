package com.lying.block;

import com.lying.init.RCGameEvents;
import com.lying.init.RCSoundEvents;

import net.minecraft.block.BlockState;
import net.minecraft.block.TransparentBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.World;

public abstract class AbstractBrokenGlassBlock extends TransparentBlock
{
	public AbstractBrokenGlassBlock(Settings settings)
	{
		super(settings);
	}
	
	public static boolean shouldAffectEntity(Entity entity, BlockState state, BlockPos pos, World world)
	{
		/**
		 * Skip non-living entities and entities whose horizontal position differs from ours
		 * This ensures only 1 broken glass block affects an entity at a time
		 */
		if(!(entity instanceof LivingEntity))
			return false;
		
		// Ensure entity is actually colliding with the block outline
		Box entityBox = entity.getBoundingBox();
		VoxelShape blockBounds = state.getOutlineShape(world, pos);
		return blockBounds.getBoundingBoxes().stream().anyMatch(box -> box.offset(pos).intersects(entityBox));
	}
	
	protected void onEntityCollision(BlockState state, World world, BlockPos pos, Entity entity)
	{
		if(!shouldAffectEntity(entity, state, pos, world) || !entity.getBlockPos().withY(pos.getY()).equals(pos))
			return;
		
		// If we're on top of another broken glass block and the entity's position is in that block, skip
		if(world.getBlockState(pos.down()).getBlock() instanceof AbstractBrokenGlassBlock && entity.getBlockPos().getY() != pos.getY())
			return;
		
		entity.slowMovement(state, new Vec3d(0.8F, 0.75F, 0.8F));
		if(!entity.bypassesSteppingEffects())
		{
			if(world.isClient())
				return;
			
			Vec3d motion = entity.isControlledByPlayer() ? entity.getMovement() : entity.getLastRenderPos().subtract(entity.getPos());
			boolean isMoving = false;
			if(motion.horizontalLengthSquared() > 0)
			{
				double lenX = Math.abs(motion.getX());
				double lenZ = Math.abs(motion.getZ());
				if(lenX >= 0.003F || lenZ >= 0.003F)
					isMoving = true;
			}
			
			if(isMoving)
			{
				if(world.getRandom().nextInt(20) == 0)
				{
					world.playSound(null, pos, RCSoundEvents.GLASS_CRUNCH.get(), SoundCategory.BLOCKS);
					world.emitGameEvent(entity, RCGameEvents.GLASS_CRONCH, pos);
				}
				
				if(!entity.isInvulnerable() && world.getRandom().nextInt(8) == 0)
					entity.damage((ServerWorld)world, world.getDamageSources().sweetBerryBush(), 3F);
			}
			
		}
	}
}
