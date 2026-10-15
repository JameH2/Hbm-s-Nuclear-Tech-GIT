package com.hbm.commands;

import java.util.List;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.storage.WorldInfo;
import net.minecraft.util.ChatComponentTranslation;

public class CommandTotalTime extends CommandBase {

	public String getCommandName() {
		return "totaltime";
	}

	/**
	 * Return the required permission level for this command.
	 */
	public int getRequiredPermissionLevel() {
		return 2;
	}

	public String getCommandUsage(ICommandSender sender) {
		return "commands.total_time.totaltime";
	}

	public void processCommand(ICommandSender sender, String[] args) {
		if(args.length > 1) {
			int i;

			if(args[0].equals("set")) {
				i = parseIntWithMin(sender, args[1], 0);
				this.setTime(sender, i);
				func_152373_a(sender, this, "commands.time.set", new Object[] { Integer.valueOf(i) });
				return;
			}

			if(args[0].equals("add")) {
				i = parseIntWithMin(sender, args[1], 0);
				this.addTime(sender, i);
				func_152373_a(sender, this, "commands.time.added", new Object[] { Integer.valueOf(i) });
				return;
			}
		}

		sender.addChatMessage(new ChatComponentTranslation("commands.time.usage"));
	}

	/**
	 * Adds the strings available in this command to the given list of tab
	 * completion options.
	 */
	@SuppressWarnings("rawtypes")
	public List addTabCompletionOptions(ICommandSender sender, String[] args) {
		return args.length == 1 ? getListOfStringsMatchingLastWord(args, new String[] { "set", "add" }) : null;
	}

	/**
	 * Set the time in the server object.
	 */
	protected void setTime(ICommandSender sender, int time) {
		for (int j = 0; j < MinecraftServer.getServer().worldServers.length; ++j) {
			WorldInfo worldInfo = MinecraftServer.getServer().worldServers[j].getWorldInfo();
			worldInfo.incrementTotalWorldTime((long) time);
		}
	}

	/**
	 * Adds (or removes) time in the server object.
	 */
	protected void addTime(ICommandSender sender, int time) {
		for (int j = 0; j < MinecraftServer.getServer().worldServers.length; ++j) {
			WorldInfo worldInfo = MinecraftServer.getServer().worldServers[j].getWorldInfo();
			worldInfo.incrementTotalWorldTime(worldInfo.getWorldTotalTime() + (long) time);
		}
	}
}