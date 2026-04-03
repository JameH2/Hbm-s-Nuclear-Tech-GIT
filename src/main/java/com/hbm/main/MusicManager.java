package com.hbm.main;

import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.hbm.sound.SSmartSong;

import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.ISound;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.client.gui.GuiIngameMenu;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.WorldEvent;

public class MusicManager {

	private static MusicHandler handler;

	private static long lastStartMs;
	private static long lastPauseMs;
	private static long extraDelayMs;
	public static SSmartSong currentSong;

	private static final String threadPrefix = "NTM-Music-Thread-";
	private static final ThreadFactory packetThreadFactory = new ThreadFactoryBuilder().setNameFormat(threadPrefix + "%d").build();
	private static final ThreadPoolExecutor threadPool = (ThreadPoolExecutor) Executors.newFixedThreadPool(1, packetThreadFactory);

	public static void init() {
		handler = new MusicHandler();
		MinecraftForge.EVENT_BUS.register(handler);
	}

	public static void start(SSmartSong song) {
		if(handler != null) handler.start(song);
	}

	public static void stop() {
		if(handler != null) handler.stop(false);
	}

	public static void stop(boolean blockEndSegment) {
		if(handler != null) handler.stop(blockEndSegment);
	}
	
	public static boolean isPlaying() {
		if(handler == null) return false;
		return handler.isPlaying();
	}

	@SideOnly(Side.CLIENT)
	public static class MusicHandler {

		public void start(SSmartSong song) {
			SoundHandler handler = Minecraft.getMinecraft().getSoundHandler();

			if(handler.isSoundPlaying(ModEventHandlerClient.currentSong)) {
				handler.stopSound(ModEventHandlerClient.currentSong);
			}

			currentSong = song;
			threadPool.submit(new MusicThread());
		}

		public void stop(boolean blockEndSegment) {
			if(blockEndSegment) {
				currentSong = null;
			} else {
				currentSong.setState(-1);
			}
		}
		
		public boolean isPlaying() {
			return currentSong != null || lastStartMs + 120_000 > System.currentTimeMillis(); // prevent new music for two minutes after conclusion
		}

		@SubscribeEvent(priority = EventPriority.LOWEST)
		public void onGui(GuiOpenEvent event) {
			Minecraft mc = Minecraft.getMinecraft();

			if(event.gui instanceof GuiIngameMenu) {
				// ya it open
				if(mc.isSingleplayer() && !mc.getIntegratedServer().getPublic()) {
					lastPauseMs = System.currentTimeMillis();
				}
			} else if(event.gui == null && lastPauseMs > 0) {
				// ya it close
				extraDelayMs += System.currentTimeMillis() - lastPauseMs;
				lastPauseMs = 0;
			}
		}

		@SubscribeEvent
		public void onWorldUnload(WorldEvent.Unload event) {
			if(!event.world.isRemote) return;
			stop(true);
		}
		
	}

	@SideOnly(Side.CLIENT)
	public static class MusicThread implements Runnable {

		@Override
		public void run() {
			try {
				lastStartMs = System.currentTimeMillis();

				Minecraft mc = Minecraft.getMinecraft();
				SoundHandler handler = mc.getSoundHandler();

				if(currentSong == null) return;

				ISound currentSound = currentSong.getNextSound();
				int expectedLengthMs = currentSong.getCurrentSegmentLengthMs();

				if(currentSound == null || mc.theWorld == null) {
					currentSong = null;
					return;
				}

				handler.playSound(currentSound);

				if(expectedLengthMs <= 0) {
					currentSong = null;
					return;
				}

				Thread.sleep(expectedLengthMs);

				while(extraDelayMs > 0 || lastPauseMs > 0) {
					long toDelay = extraDelayMs;

					// still paused, wait 10ms and reduce extra delay on unpausing by same amount
					if(lastPauseMs > 0) {
						toDelay = 10;
						extraDelayMs -= 10;
					} else {
						extraDelayMs = 0;
					}

					Thread.sleep(toDelay);
				}

				threadPool.submit(new MusicThread());
			} catch(InterruptedException ex) {
				// no mo music
			}
		}

	}

}
