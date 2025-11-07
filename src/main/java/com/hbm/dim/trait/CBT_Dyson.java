package com.hbm.dim.trait;

import java.util.HashMap;
import java.util.Map.Entry;

import com.hbm.dim.CelestialBody;

import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

public class CBT_Dyson extends CelestialBodyTrait {

	// Correlates an ID with a swarm
	private HashMap<Integer, Swarm> swarms = new HashMap<>();

	private static class Swarm {

		int members;
		int consumers;

		// Incremented whenever another consumer is added
		// is copied to consumers at the start of a tick
		private int addedConsumers;

		public Swarm(int members) {
			this.members = members;
		}

	}

	public static void launch(World world, int id) {
		launch(world, id, 1);
	}

	public static void launch(World world, int id, int amount) {
		CelestialBody star = CelestialBody.getStar(world);
		CBT_Dyson dyson = star.getTrait(CBT_Dyson.class);
		if(dyson == null) dyson = new CBT_Dyson();

		Swarm swarm = dyson.swarms.get(id);
		if(swarm == null) {
			swarm = new Swarm(0);
			dyson.swarms.put(id, swarm);
		}

		swarm.members += amount;

		star.modifyTraits(dyson);
	}

	public static int count(World world, int id) {
		CelestialBody star = CelestialBody.getStar(world);
		CBT_Dyson dyson = star.getTrait(CBT_Dyson.class);
		if(dyson == null) return 0;

		Swarm swarm = dyson.swarms.get(id);
		if(swarm == null) return 0;

		return swarm.members;
	}

	public static int consumers(World world, int id) {
		CelestialBody star = CelestialBody.getStar(world);
		CBT_Dyson dyson = star.getTrait(CBT_Dyson.class);
		if(dyson == null) return 0;

		Swarm swarm = dyson.swarms.get(id);
		if(swarm == null) return 0;

		swarm.addedConsumers++;

		return swarm.consumers;
	}

	public int size() {
		int size = 0;
		for(Swarm swarm : swarms.values()) {
			size += swarm.members;
		}
		return size;
	}

	@Override
	public void update(boolean isRemote) {
		if(!isRemote) {
			attenuate();
		}
	}

	// Called once per tick to lower swarm counts from satellite failures, encouraging continuous automation
	// based on total across all swarms, meaning players on servers are encouraged to either annihilate other launchers or work together
	public void attenuate() {
		for(Swarm swarm : swarms.values()) {
			swarm.consumers = swarm.addedConsumers;
			swarm.addedConsumers = 0;

			if(swarm.members <= 0) continue;

			double decayChance = (double)size() / (1024 * 5 * 20);
			if(Math.random() < decayChance) swarm.members--;
		}
	}

	@Override
	public void writeToNBT(NBTTagCompound nbt) {
		int[] swarmData = new int[swarms.size() * 2];
		int i = 0;
		for(Entry<Integer, Swarm> entry : swarms.entrySet()) {
			swarmData[i] = entry.getKey();
			swarmData[i+1] = entry.getValue().members;
			i += 2;
		}

		nbt.setIntArray("swarm", swarmData);
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		int[] swarmData = nbt.getIntArray("swarm");
		swarms = new HashMap<>();
		for(int i = 0; i < swarmData.length; i += 2) {
			swarms.put(swarmData[i], new Swarm(swarmData[i+1]));
		}
	}

	@Override
	public void writeToBytes(ByteBuf buf) {
		buf.writeInt(swarms.size() * 2);
		for(Entry<Integer, Swarm> entry : swarms.entrySet()) {
			buf.writeShort(entry.getKey());
			buf.writeInt(entry.getValue().members);
		}
	}

	@Override
	public void readFromBytes(ByteBuf buf) {
		int count = buf.readInt();
		swarms = new HashMap<>();
		for(int i = 0; i < count; i += 2) {
			int id = buf.readShort();
			int members = buf.readInt();
			swarms.put(id, new Swarm(members));
		}
	}

}
