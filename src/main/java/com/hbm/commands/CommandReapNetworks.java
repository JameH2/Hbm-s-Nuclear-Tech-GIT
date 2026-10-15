package com.hbm.commands;

import com.hbm.uninos.GenNode;
import com.hbm.uninos.UniNodespace;
import com.hbm.util.ChatBuilder;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.ChatComponentTranslation;

public class CommandReapNetworks extends CommandBase {

	@Override
	public String getCommandName() {
		return "ntmreapnetworks";
	}

	@Override
	public String getCommandUsage(ICommandSender sender) {
		return "commands.reap_networks.ntmreapnetworks";
	}

	@Override
	public void processCommand(ICommandSender sender, String[] args) {

		try {

			UniNodespace.activeNodeNets.forEach((net) -> {
				net.links.forEach((link) -> { ((GenNode)link).expired = true; });
				net.links.clear();
				net.providerEntries.clear();
				net.receiverEntries.clear();
			});
			UniNodespace.activeNodeNets.clear();
			UniNodespace.worlds.clear();

			sender.addChatMessage(new ChatComponentTranslation("commands.reap_networks.nodespace_cleared").setChatStyle(new net.minecraft.util.ChatStyle().setColor(EnumChatFormatting.YELLOW)));

		} catch(Exception ex) {
			sender.addChatMessage(ChatBuilder.start("----------------------------------").color(EnumChatFormatting.GRAY).flush());
			sender.addChatMessage(ChatBuilder.startTranslation("commands.reap_networks.text.01").color(EnumChatFormatting.RED).flush());
			sender.addChatMessage(ChatBuilder.start(ex.getLocalizedMessage()).color(EnumChatFormatting.RED).flush());
			sender.addChatMessage(ChatBuilder.start(ex.getStackTrace()[0].toString()).color(EnumChatFormatting.RED).flush());
			sender.addChatMessage(ChatBuilder.start("----------------------------------").color(EnumChatFormatting.GRAY).flush());
			throw ex;
		}
	}
}
