package com.hbm.commands;

import java.util.HashMap;

import com.hbm.config.RunningConfig.ConfigWrapper;
import com.hbm.config.ServerConfig;

import net.minecraft.command.ICommandSender;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.ChatComponentTranslation;

public class CommandReloadServer extends CommandReloadConfig {

	@Override
	public String getCommandName() {
		return "ntmserver";
	}

	@Override
	public String getCommandUsage(ICommandSender sender) {
		return "commands.reload_server.help_hint";
	}

	@Override public void help(ICommandSender sender, String[] args) {
		if(args.length >= 2) {
			String command = args[1];
			if("help".equals(command)) sender.addChatMessage(new ChatComponentTranslation("commands.reload_server.help").setChatStyle(new net.minecraft.util.ChatStyle().setColor(EnumChatFormatting.YELLOW)));
			if("list".equals(command)) sender.addChatMessage(new ChatComponentTranslation("commands.reload_server.help_list").setChatStyle(new net.minecraft.util.ChatStyle().setColor(EnumChatFormatting.YELLOW)));
			if("reload".equals(command)) sender.addChatMessage(new ChatComponentTranslation("commands.reload_server.help_load").setChatStyle(new net.minecraft.util.ChatStyle().setColor(EnumChatFormatting.YELLOW)));
			if("get".equals(command)) sender.addChatMessage(new ChatComponentTranslation("commands.reload_server.help_get").setChatStyle(new net.minecraft.util.ChatStyle().setColor(EnumChatFormatting.YELLOW)));
			if("set".equals(command)) sender.addChatMessage(new ChatComponentTranslation("commands.reload_server.help_set").setChatStyle(new net.minecraft.util.ChatStyle().setColor(EnumChatFormatting.YELLOW)));
		} else {
			sender.addChatMessage(new ChatComponentTranslation("commands.reload_server.usage_help", EnumChatFormatting.GOLD, EnumChatFormatting.RED).setChatStyle(new net.minecraft.util.ChatStyle().setColor(EnumChatFormatting.YELLOW)));
			sender.addChatMessage(new ChatComponentTranslation("commands.reload_server.usage_list", EnumChatFormatting.GOLD).setChatStyle(new net.minecraft.util.ChatStyle().setColor(EnumChatFormatting.YELLOW)));
			sender.addChatMessage(new ChatComponentTranslation("commands.reload_server.usage_reload", EnumChatFormatting.GOLD).setChatStyle(new net.minecraft.util.ChatStyle().setColor(EnumChatFormatting.YELLOW)));
			sender.addChatMessage(new ChatComponentTranslation("commands.reload_server.usage_get", EnumChatFormatting.GOLD, EnumChatFormatting.RED).setChatStyle(new net.minecraft.util.ChatStyle().setColor(EnumChatFormatting.YELLOW)));
			sender.addChatMessage(new ChatComponentTranslation("commands.reload_server.usage_set", EnumChatFormatting.GOLD, EnumChatFormatting.RED).setChatStyle(new net.minecraft.util.ChatStyle().setColor(EnumChatFormatting.YELLOW)));
		}
	}

	@Override public HashMap<String, ConfigWrapper> getConfigMap() { return ServerConfig.configMap; }
	@Override public void refresh() { ServerConfig.refresh(); }
	@Override public void reload() { ServerConfig.reload(); }
	@Override public String getTitle() { return "commands.ntmserver.title"; }
}
