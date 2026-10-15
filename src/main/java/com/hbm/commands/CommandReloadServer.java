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
		return "commands.reload_server.ntmserver_help";
	}

	@Override public void help(ICommandSender sender, String[] args) {
		if(args.length >= 2) {
			String command = args[1];
			if("help".equals(command)) sender.addChatMessage(new ChatComponentTranslation("commands.reload_server.help").setChatStyle(new net.minecraft.util.ChatStyle().setColor(EnumChatFormatting.YELLOW)));
			if("list".equals(command)) sender.addChatMessage(new ChatComponentTranslation("commands.reload_server.shows_variable_names_values").setChatStyle(new net.minecraft.util.ChatStyle().setColor(EnumChatFormatting.YELLOW)));
			if("reload".equals(command)) sender.addChatMessage(new ChatComponentTranslation("commands.reload_server.reads_variables_config_file").setChatStyle(new net.minecraft.util.ChatStyle().setColor(EnumChatFormatting.YELLOW)));
			if("get".equals(command)) sender.addChatMessage(new ChatComponentTranslation("commands.reload_server.shows_value_specified_variable").setChatStyle(new net.minecraft.util.ChatStyle().setColor(EnumChatFormatting.YELLOW)));
			if("set".equals(command)) sender.addChatMessage(new ChatComponentTranslation("commands.reload_server.text.02").setChatStyle(new net.minecraft.util.ChatStyle().setColor(EnumChatFormatting.YELLOW)));
		} else {
			sender.addChatMessage(new ChatComponentTranslation("commands.reload_server.ntmserver_shelp_command", EnumChatFormatting.GOLD, EnumChatFormatting.RED).setChatStyle(new net.minecraft.util.ChatStyle().setColor(EnumChatFormatting.YELLOW)));
			sender.addChatMessage(new ChatComponentTranslation("commands.reload_server.ntmserver_slist", EnumChatFormatting.GOLD).setChatStyle(new net.minecraft.util.ChatStyle().setColor(EnumChatFormatting.YELLOW)));
			sender.addChatMessage(new ChatComponentTranslation("commands.reload_server.ntmserver_sreload", EnumChatFormatting.GOLD).setChatStyle(new net.minecraft.util.ChatStyle().setColor(EnumChatFormatting.YELLOW)));
			sender.addChatMessage(new ChatComponentTranslation("commands.reload_server.ntmserver_sget_name", EnumChatFormatting.GOLD, EnumChatFormatting.RED).setChatStyle(new net.minecraft.util.ChatStyle().setColor(EnumChatFormatting.YELLOW)));
			sender.addChatMessage(new ChatComponentTranslation("commands.reload_server.ntmserver_sset_name_value", EnumChatFormatting.GOLD, EnumChatFormatting.RED).setChatStyle(new net.minecraft.util.ChatStyle().setColor(EnumChatFormatting.YELLOW)));
		}
	}

	@Override public HashMap<String, ConfigWrapper> getConfigMap() { return ServerConfig.configMap; }
	@Override public void refresh() { ServerConfig.refresh(); }
	@Override public void reload() { ServerConfig.reload(); }
	@Override public String getTitle() { return "commands.ntmserver.title"; }
}
