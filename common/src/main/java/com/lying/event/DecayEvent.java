package com.lying.event;

import com.lying.decay.context.DecayContext;
import com.lying.decay.context.DecayContext.DecayType;
import com.lying.decay.handler.DecayEntry;

import dev.architectury.event.Event;
import dev.architectury.event.EventFactory;
import dev.architectury.event.EventResult;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

public class DecayEvent
{
	/** Called during decay to verify if a block can be decayed, such as natural decay within spawn protection */
	public static final Event<CanDecayBlockEvent> CAN_DECAY_BLOCK_EVENT	= EventFactory.createEventResult(CanDecayBlockEvent.class);
	
	@FunctionalInterface
	public interface CanDecayBlockEvent
	{
		/** If {@link EventResult.isFalse}, prevents the block from being decayed */
		EventResult canBlockDecay(BlockPos pos, ServerWorld world, DecayType type);
	}
	
	/** Called before a decay entry is applied to a decay context of any kind */
	public static final Event<OnBlockDecayEvent> BEFORE_BLOCK_DECAY_EVENT	= EventFactory.createLoop(OnBlockDecayEvent.class);
	
	/** Called after a decay entry is applied to a decay context of any kind */
	public static final Event<OnBlockDecayEvent> AFTER_BLOCK_DECAY_EVENT	= EventFactory.createLoop(OnBlockDecayEvent.class);
	
	@FunctionalInterface
	public interface OnBlockDecayEvent
	{
		void onBlockDecay(DecayContext context, DecayEntry entry);
	}
}
