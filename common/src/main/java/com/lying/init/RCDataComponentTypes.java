package com.lying.init;

import java.util.function.UnaryOperator;

import com.lying.Reclamation;
import com.lying.reference.Reference;

import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.component.ComponentType;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;

public class RCDataComponentTypes
{
	public static final DeferredRegister<ComponentType<?>> TYPES	= DeferredRegister.create(Reference.ModInfo.MOD_ID, RegistryKeys.DATA_COMPONENT_TYPE);
	private static int tally;
	
	public static final RegistrySupplier<ComponentType<Identifier>> DECAY_ENTRY	= register("decay_entry", builder -> builder.codec(Identifier.CODEC).packetCodec(Identifier.PACKET_CODEC));
	
	private static <T extends Object> RegistrySupplier<ComponentType<T>> register(String nameIn, UnaryOperator<ComponentType.Builder<T>> builderOperator)
	{
		tally++;
		return TYPES.register(Reference.ModInfo.prefix(nameIn), () -> builderOperator.apply(ComponentType.builder()).build());
	}
	
	public static void init()
	{
		TYPES.register();
		Reclamation.LOGGER.info("# Registered {} data component types", tally);
	}
}
