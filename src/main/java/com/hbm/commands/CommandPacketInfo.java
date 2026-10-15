package com.hbm.commands;

import com.hbm.config.GeneralConfig;
import com.hbm.handler.threading.PacketThreading;
import com.hbm.main.MainRegistry;
import com.hbm.util.BobMathUtil;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.util.concurrent.TimeUnit;

import static com.hbm.handler.threading.PacketThreading.totalCnt;
import net.minecraft.util.ChatComponentTranslation;

public class CommandPacketInfo extends CommandBase {
	@Override
	public String getCommandName() {
		return "ntmpackets";
	}

	@Override
	public String getCommandUsage(ICommandSender sender) {
		return EnumChatFormatting.RED + "/ntmpackets [info/resetState/toggleThreadingStatus/forceLock/forceUnlock]";
	}

	@Override
	public void processCommand(ICommandSender sender, String[] args) {

		if (args.length > 0) {
			switch (args[0]) {
				case "resetState":
					PacketThreading.hasTriggered = false;
					PacketThreading.clearCnt = 0;
					return;
				case "toggleThreadingStatus":
					GeneralConfig.enablePacketThreading = !GeneralConfig.enablePacketThreading; // Force toggle.
					PacketThreading.init(); // Reinit threads.
					sender.addChatMessage(new ChatComponentTranslation("commands.packet_info.packet_sending_status_toggled_to", GeneralConfig.enablePacketThreading).setChatStyle(new net.minecraft.util.ChatStyle().setColor(EnumChatFormatting.GREEN)));
					return;
				case "forceLock":
					PacketThreading.lock.lock(); // oh my fucking god never do this please unless you really have to
					sender.addChatMessage(new ChatComponentTranslation("commands.packet_info.text.01").setChatStyle(new net.minecraft.util.ChatStyle().setColor(EnumChatFormatting.RED)));
					MainRegistry.logger.error("Packet thread lock acquired by {}, this may freeze the main thread!", sender.getCommandSenderName());
					return;
				case "forceUnlock":
					PacketThreading.lock.unlock();
					MainRegistry.logger.warn("Packet thread lock released by {}.", sender.getCommandSenderName());
					return;
				case "info":
					sender.addChatMessage(new ChatComponentTranslation("commands.packet_info.ntm_packet_debugger_v1_2").setChatStyle(new net.minecraft.util.ChatStyle().setColor(EnumChatFormatting.GOLD)));

					if (PacketThreading.isTriggered() && GeneralConfig.enablePacketThreading)
						sender.addChatMessage(new ChatComponentTranslation("commands.packet_info.packet_threading_errored_check_l").setChatStyle(new net.minecraft.util.ChatStyle().setColor(EnumChatFormatting.RED)));
					else if (GeneralConfig.enablePacketThreading)
						sender.addChatMessage(new ChatComponentTranslation("commands.packet_info.packet_threading_active").setChatStyle(new net.minecraft.util.ChatStyle().setColor(EnumChatFormatting.GREEN)));
					else
						sender.addChatMessage(new ChatComponentTranslation("commands.packet_info.packet_threading_inactive").setChatStyle(new net.minecraft.util.ChatStyle().setColor(EnumChatFormatting.RED)));

					sender.addChatMessage(new ChatComponentTranslation("commands.packet_info.thread_pool_info").setChatStyle(new net.minecraft.util.ChatStyle().setColor(EnumChatFormatting.YELLOW)));
					sender.addChatMessage(new ChatComponentTranslation("commands.packet_info.threads_total", PacketThreading.threadPool.getPoolSize()).setChatStyle(new net.minecraft.util.ChatStyle().setColor(EnumChatFormatting.YELLOW)));
					sender.addChatMessage(new ChatComponentTranslation("commands.packet_info.threads_core", PacketThreading.threadPool.getCorePoolSize()).setChatStyle(new net.minecraft.util.ChatStyle().setColor(EnumChatFormatting.YELLOW)));
					sender.addChatMessage(new ChatComponentTranslation("commands.packet_info.threads_idle", (PacketThreading.threadPool.getPoolSize() - PacketThreading.threadPool.getActiveCount())).setChatStyle(new net.minecraft.util.ChatStyle().setColor(EnumChatFormatting.YELLOW)));
					sender.addChatMessage(new ChatComponentTranslation("commands.packet_info.threads_maximum", PacketThreading.threadPool.getMaximumPoolSize()).setChatStyle(new net.minecraft.util.ChatStyle().setColor(EnumChatFormatting.YELLOW)));

					for (ThreadInfo thread : ManagementFactory.getThreadMXBean().dumpAllThreads(false, false))
						if (thread.getThreadName().startsWith(PacketThreading.threadPrefix)) {
							sender.addChatMessage(new ChatComponentTranslation("commands.packet_info.thread_name", thread.getThreadName()).setChatStyle(new net.minecraft.util.ChatStyle().setColor(EnumChatFormatting.GOLD)));
							sender.addChatMessage(new ChatComponentTranslation("commands.packet_info.thread_id", thread.getThreadId()).setChatStyle(new net.minecraft.util.ChatStyle().setColor(EnumChatFormatting.YELLOW)));
							sender.addChatMessage(new ChatComponentTranslation("commands.packet_info.thread_state", thread.getThreadState()).setChatStyle(new net.minecraft.util.ChatStyle().setColor(EnumChatFormatting.YELLOW)));
							sender.addChatMessage(new ChatComponentTranslation("commands.packet_info.locked_by", (thread.getLockOwnerName() == null ? "None" : thread.getLockName())).setChatStyle(new net.minecraft.util.ChatStyle().setColor(EnumChatFormatting.YELLOW)));
						}

					sender.addChatMessage(new ChatComponentTranslation("commands.packet_info.packet_info").setChatStyle(new net.minecraft.util.ChatStyle().setColor(EnumChatFormatting.GOLD)));
					sender.addChatMessage(new ChatComponentTranslation("commands.packet_info.amount_total", totalCnt).setChatStyle(new net.minecraft.util.ChatStyle().setColor(EnumChatFormatting.YELLOW)));
					sender.addChatMessage(new ChatComponentTranslation("commands.packet_info.amount_remaining", PacketThreading.threadPool.getQueue().size()).setChatStyle(new net.minecraft.util.ChatStyle().setColor(EnumChatFormatting.YELLOW)));

					if (totalCnt != 0)
						sender.addChatMessage(new ChatComponentTranslation("commands.packet_info.remaining_to_process", BobMathUtil.roundDecimal(((double) PacketThreading.threadPool.getQueue().size() / totalCnt) * 100, 2)).setChatStyle(new net.minecraft.util.ChatStyle().setColor(EnumChatFormatting.YELLOW)));

					sender.addChatMessage(new ChatComponentTranslation("commands.packet_info.text.02", BobMathUtil.roundDecimal(TimeUnit.MILLISECONDS.convert(PacketThreading.nanoTimeWaited, TimeUnit.NANOSECONDS), 4)).setChatStyle(new net.minecraft.util.ChatStyle().setColor(EnumChatFormatting.YELLOW)));
					return;
			}
		}
		sender.addChatMessage(new ChatComponentText(getCommandUsage(sender)));
	}
}
