package com.lying.item;

import java.util.List;
import java.util.Optional;

import com.lying.Reclamation;
import com.lying.decay.DecayLibrary;
import com.lying.decay.context.DecayContext;
import com.lying.decay.context.DecayContext.DecayType;
import com.lying.decay.context.LiveDecayContext;
import com.lying.decay.handler.DecayEntry;

import net.minecraft.block.BlockState;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class DecayDustItem extends Item
{
	public DecayDustItem(Settings settings)
	{
		super(settings);
	}
	
	public ActionResult useOnBlock(ItemUsageContext context)
	{
		if(!context.shouldCancelInteraction())
		{
			World world = context.getWorld();
			if(!world.isClient())
			{
				ServerWorld serverWorld = (ServerWorld)world;
				BlockPos position = context.getBlockPos();
				Optional<DecayContext> cxt = tryGuaranteedDecay(serverWorld, LiveDecayContext.supplier(position, serverWorld, DecayType.ARTIFICIAL));
				if(cxt.isPresent())
				{
					cxt.get().close();
					ItemStack stack = context.getStack();
					if(!context.getPlayer().isCreative())
						stack.decrement(1);
					
					return ActionResult.SUCCESS.withNewHandStack(stack);
				}
			}
			return ActionResult.SUCCESS;
		}
		
		return super.useOnBlock(context);
	}
	
	public static Optional<DecayContext> tryGuaranteedDecay(ServerWorld world, DecayContext context)
	{
    	BlockPos pos = context.initialPos;
    	if(!Reclamation.canBlockDecay(pos, world, Optional.empty()))
    		return Optional.empty();
    	
		// Natural decay checks validity before calling this function, so everything else is checked here
		if(context.type != DecayType.NATURAL && !context.type.canDecayBlock(pos, world))
			return Optional.empty();
		
		BlockState state = context.originalState;
		List<DecayEntry> decayOptions = DecayLibrary.instance().getDecayOptions(world, pos, state);
		DecayEntry decay = decayOptions.size() > 1 ? decayOptions.get(context.random.nextInt(decayOptions.size())) : decayOptions.get(0);
		return Optional.of(Reclamation.applyDecay(world, decay, context));
	}
}
