package com.hbm.main;

import com.hbm.sound.SSmartSong;

import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.SoundHandler;

public class MusicManager {

	private static long lastStartMs;
	private static long lastTickMs;
	public static SSmartSong currentSong;

	public static void start(SSmartSong song) {
		SoundHandler handler = Minecraft.getMinecraft().getSoundHandler();

		if(handler.isSoundPlaying(ModEventHandlerClient.currentSong)) {
			handler.stopSound(ModEventHandlerClient.currentSong);
		}

		currentSong = song;
		lastStartMs = System.currentTimeMillis();

		handler.playSound(currentSong.getNextSound());
	}

	public static void stop() {
		currentSong.setState(-1);
	}
	
	public static boolean isPlaying() {
		return currentSong != null || lastStartMs + 120_000 > System.currentTimeMillis(); // prevent new music for two minutes after conclusion
	}

	// call as often as possible probably iunno
	public static void update() {
		if(!isPlaying()) return;

		int expectedLengthMs = currentSong.getCurrentSegmentLengthMs();

		if(expectedLengthMs == 0) {
			currentSong = null;
			return;
		}

		SoundHandler handler = Minecraft.getMinecraft().getSoundHandler();
		
		if(lastStartMs + expectedLengthMs <= System.currentTimeMillis()) {
			lastStartMs = System.currentTimeMillis();
			handler.playSound(currentSong.getNextSound());
		}

		if(Minecraft.getMinecraft().isGamePaused()) {
			lastStartMs += System.currentTimeMillis() - lastTickMs;
		}
		lastTickMs = System.currentTimeMillis();
	}

}
