package com.hbm.sound;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import net.minecraft.client.audio.ISound;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.util.ResourceLocation;

public class SSmartSong { // yes it's a pun

	private int state;
	public float tempo;

	private Multimap<Integer, SSmartSegment> segments = HashMultimap.create();

	private SSmartSegment currentSegment;
	private int lastState = -1;

	public SSmartSong(float tempo) {
		this.tempo = tempo;
	}

	public void addSegmentWithBeats(int state, ResourceLocation audio, float beats) { segments.put(state, new SSmartSegment(audio, beats * (60 / tempo), SegmentType.LOOP)); }
	public void addSegmentWithLength(int state, ResourceLocation audio, float length) { segments.put(state, new SSmartSegment(audio, length, SegmentType.LOOP)); }

	public void addTransitionWithBeats(int state, ResourceLocation audio, float beats) { segments.put(state, new SSmartSegment(audio, beats * (60 / tempo), SegmentType.TRANSITION)); }
	public void addTransitionWithLength(int state, ResourceLocation audio, float length) { segments.put(state, new SSmartSegment(audio, length, SegmentType.TRANSITION)); }

	public void addEnding(ResourceLocation audio) { segments.put(-1, new SSmartSegment(audio, 0, SegmentType.TRANSITION)); }

	public void setState(int state) {
		if(this.state == -1) return;
		this.state = state;
	}

	public ISound getNextSound() {
		currentSegment = getNextSegment();
		if(currentSegment == null) return null;
		return currentSegment.getSound();
	}

	public int getCurrentSegmentLengthMs() {
		return (int) (currentSegment.length * 1000);
	}

	public boolean isStarting() {
		return state == 0 && lastState == -1;
	}

	public boolean isEnding() {
		return state == -1;
	}

	private SSmartSegment getNextSegment() {
		List<SSmartSegment> stateSegments = new ArrayList<>(segments.get(state));
		Collections.shuffle(stateSegments);

		// attempt to find a transitional segment
		if(lastState != state) {
			for(SSmartSegment segment : stateSegments) {
				if(segment.type == SegmentType.TRANSITION) {
					lastState = state;
					return segment;
				}
			}
		}

		// then loop
		for(SSmartSegment segment : stateSegments) {
			if(segment.type == SegmentType.LOOP) {
				lastState = state;
				return segment;
			}
		}

		lastState = state;
		return null;
	}

	public static class SSmartSegment {

		public final ResourceLocation audio;
		public final float length; // in seconds
		public final SegmentType type;

		private SSmartSegment(ResourceLocation audio, float length, SegmentType type) {
			this.audio = audio;
			this.length = length;
			this.type = type;
		}

		public ISound getSound() {
			return PositionedSoundRecord.func_147673_a(audio);
		}

	}

	public static enum SegmentType {
		LOOP,
		TRANSITION,
	}

}
