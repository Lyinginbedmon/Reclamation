package com.lying.decay.functions;

import com.google.gson.JsonObject;
import com.lying.decay.context.DecayContext;
import com.lying.init.RCDecayFunctions;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandOutput;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;

public class FunctionRunCmd extends DecayFunction
{
	private static final String DEFAULT_CMD	= "/say Block Decayed";
	private String command = DEFAULT_CMD;
	
	public FunctionRunCmd(Identifier idIn)
	{
		super(idIn);
	}
	
	public static FunctionRunCmd of(String commandIn)
	{
		FunctionRunCmd function = RCDecayFunctions.RUN_CMD.get();
		function.command = commandIn;
		return function;
	}
	
	protected void applyTo(DecayContext context)
	{
		if(context.world.isEmpty())
			return;
		
		ServerWorld world = context.world.get();
		MinecraftServer server = world.getServer();
		BlockPos pos = context.currentPos();
		ServerCommandSource source = new ServerCommandSource(
				CommandOutput.DUMMY,
				Vec3d.ofCenter(pos),
				new Vec2f(0F, 180F),
				world,
				2,
				"decay_update",
				Text.literal("decay_update"),
				server,
				null
				);
		server.getCommandManager().executeWithPrefix(source, command);
	}
	
	protected JsonObject write(JsonObject obj)
	{
		if(command.length() > 0)
			obj.add("command", Codec.STRING.encodeStart(JsonOps.INSTANCE, command).getOrThrow());
		return obj;
	}
	
	protected void read(JsonObject obj)
	{
		command = obj.has("command") ? Codec.STRING.parse(JsonOps.INSTANCE, obj.get("command")).getOrThrow() : DEFAULT_CMD;
	}
}
