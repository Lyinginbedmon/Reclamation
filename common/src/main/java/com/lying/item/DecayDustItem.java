package com.lying.item;

import java.util.List;
import java.util.Optional;

import org.jetbrains.annotations.Nullable;

import com.lying.Reclamation;
import com.lying.decay.DecayLibrary;
import com.lying.decay.context.DecayContext;
import com.lying.decay.context.DecayContext.DecayType;
import com.lying.decay.context.LiveDecayContext;
import com.lying.decay.handler.DecayEntry;
import com.lying.init.RCDataComponentTypes;
import com.lying.init.RCSoundEvents;

import net.minecraft.block.BlockState;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class DecayDustItem extends Item
{
	public DecayDustItem(Settings settings)
	{
		super(settings
				.component(RCDataComponentTypes.DECAY_ENTRY.get(), null));
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
				Optional<DecayContext> cxt = tryGuaranteedDecay(serverWorld, context.getStack().get(RCDataComponentTypes.DECAY_ENTRY.get()), LiveDecayContext.supplier(position, serverWorld, DecayType.ARTIFICIAL));
				if(cxt.isPresent())
				{
					serverWorld.playSound(null, position, RCSoundEvents.WITHERING_DUST.get(), SoundCategory.PLAYERS);
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
	
	public static Optional<DecayContext> tryGuaranteedDecay(ServerWorld world, @Nullable Identifier entry, DecayContext context)
	{
    	BlockPos pos = context.initialPos;
    	if(!Reclamation.canBlockDecay(pos, world, Optional.empty()))
    		return Optional.empty();
    	
		// Natural decay checks validity before calling this function, so everything else is checked here
		if(context.type != DecayType.NATURAL && !context.type.canDecayBlock(pos, world))
			return Optional.empty();
		
		BlockState state = context.originalState;
		DecayEntry decay = null;
		if(entry != null)
		{
			Optional<DecayEntry> citation = DecayLibrary.instance().get(entry);
			if(citation.isPresent() && citation.get().test(context))
				decay = citation.get();
		}
		else
		{
			List<DecayEntry> decayOptions = DecayLibrary.instance().getDecayOptions(world, pos, state);
			decay = decayOptions.size() > 1 ? decayOptions.get(context.random.nextInt(decayOptions.size())) : decayOptions.get(0);
		}
		
		if(decay != null)
		{
			Reclamation.LOGGER.info("Applied decay entry {} with withering dust", decay.packName().toString());
			return Optional.of(Reclamation.applyDecay(world, decay, context));
		}
		return Optional.empty();
	}
}
