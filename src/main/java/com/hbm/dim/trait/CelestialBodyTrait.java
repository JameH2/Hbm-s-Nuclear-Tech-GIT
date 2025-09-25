package com.hbm.dim.trait;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.HashBiMap;

import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.NBTTagCompound;

public abstract class CelestialBodyTrait {

	// Similarly to fluid traits, we have classes, and instance members.
	// For the simple traits, we'll just init both here rather than two places.


	public static class CBT_BATTLEFIELD extends CelestialBodyTrait { }
	public static CBT_BATTLEFIELD BATTLE = new CBT_BATTLEFIELD();



	public static class CBT_COMPROMISED extends CelestialBodyTrait { }
	public static CBT_COMPROMISED COMP = new CBT_COMPROMISED();


	// Constructor and loading
	public static List<Class<? extends CelestialBodyTrait>> traitList = new ArrayList<Class<? extends CelestialBodyTrait>>();
	public static HashBiMap<String, Class<? extends CelestialBodyTrait>> traitMap = HashBiMap.create();

	static {
		registerTrait("atmosphere", CBT_Atmosphere.class);
		registerTrait("temperature", CBT_Temperature.class);
		registerTrait("bees", CBT_Bees.class);
		registerTrait("war", CBT_War.class);
		registerTrait("destroyed", CBT_Destroyed.class);
		registerTrait("water", CBT_Water.class);
		registerTrait("battle", CBT_BATTLEFIELD.class);
		registerTrait("infected", CBT_COMPROMISED.class);
		registerTrait("dyson", CBT_Dyson.class);
		registerTrait("impact", CBT_Impact.class);
		registerTrait("lights", CBT_Lights.class);
	};

	private static void registerTrait(String name, Class<? extends CelestialBodyTrait> clazz) {
		traitList.add(clazz);
		traitMap.put(name, clazz);
	}

	// Serialization
	public void readFromNBT(NBTTagCompound nbt) { }
	public void writeToNBT(NBTTagCompound nbt) { }

	public void readFromBytes(ByteBuf buf) { }
	public void writeToBytes(ByteBuf buf) { }

	public void update(boolean isremote) { }

}
