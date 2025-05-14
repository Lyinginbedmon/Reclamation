package com.lying.component.type;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import io.netty.buffer.ByteBuf;
import net.minecraft.item.Item.TooltipContext;
import net.minecraft.item.tooltip.TooltipAppender;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public record OptionalIdentifierComponent(Optional<Identifier> id) implements TooltipAppender
{
	public static final Codec<OptionalIdentifierComponent> CODEC	= RecordCodecBuilder.create(instance -> instance.group(
			Identifier.CODEC.optionalFieldOf("entry").forGetter(OptionalIdentifierComponent::id))
				.apply(instance, (id) -> new OptionalIdentifierComponent(id)));
	public static final PacketCodec<ByteBuf, OptionalIdentifierComponent> PACKET_CODEC = Identifier.PACKET_CODEC.xmap(OptionalIdentifierComponent::of, c -> c.id().get());
	
	public void appendTooltip(TooltipContext context, Consumer<Text> tooltip, TooltipType type)
	{
		id().ifPresent(item -> tooltip.accept(Text.translatable("gui.reclamation.withering_dust.entry_id", item.toString())));
	}
	
	public static OptionalIdentifierComponent empty() { return new OptionalIdentifierComponent(Optional.empty()); }
	
	public static OptionalIdentifierComponent of(Identifier id) { return new OptionalIdentifierComponent(Optional.of(id)); }
	
	public int hashCode() { return Objects.hash(id); }
	
	public boolean equals(Object obj)
	{
		if(obj == this)
			return true;
		else
			return obj instanceof OptionalIdentifierComponent comp && comp.id.isPresent() == id.isPresent() && (id.isEmpty() || comp.id.get().equals(id.get()));
	}
}
