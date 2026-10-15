package com.hbm.commands;

import com.hbm.items.ICustomizable;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.ChatComponentTranslation;

public class CommandCustomize extends CommandBase {

	@Override
	public String getCommandName() {
		return "ntmcustomize";
	}

	@Override
	public String getCommandUsage(ICommandSender sender) {
		return "commands.customize.ntmcustomize";
	}

	@Override
	public int getRequiredPermissionLevel() {
		return 0;
	}

	@Override
	public boolean canCommandSenderUseCommand(ICommandSender sender) {
		return true;
	}

	@Override
	public void processCommand(ICommandSender sender, String[] args) {
		if(!(sender instanceof EntityPlayer)) {
			sender.addChatMessage(new ChatComponentTranslation("commands.customize.customization_available_players").setChatStyle(new ChatStyle().setColor(EnumChatFormatting.RED)));
			return;
		}

		EntityPlayer player = (EntityPlayer) sender;

		if(player.getHeldItem() == null || !(player.getHeldItem().getItem() instanceof ICustomizable)) {
			sender.addChatMessage(new ChatComponentTranslation("commands.customize.text.02").setChatStyle(new ChatStyle().setColor(EnumChatFormatting.RED)));
			return;
		}

		ICustomizable item = (ICustomizable) player.getHeldItem().getItem();
		item.customize(player, player.getHeldItem(), args);
	}
}
