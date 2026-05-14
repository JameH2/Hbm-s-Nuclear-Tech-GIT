package com.hbm.handler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.hbm.dim.CelestialBody;
import com.hbm.entity.logic.EntityNukeExplosionMK5;
import com.hbm.items.ItemVOTVdrive.Target;

import cpw.mods.fml.common.gameevent.TickEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import io.netty.buffer.ByteBuf;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;

public class CelestialNukeShockHandler {

	private static final long MAX_AGE = 220L;
	public static final int MAX_ACTIVE_SHOCKS = 4;
	private static final float FLASH_GRID = 16.0F;

	public static final class ShockStatus {
		public long time;
		public float centerX;
		public float centerY;
		public float strength;

		public ShockStatus(long time, float centerX, float centerY, float strength) {
			this.time = time;
			this.centerX = centerX;
			this.centerY = centerY;
			this.strength = strength;
		}
	}

	private static final HashMap<Integer, List<ShockStatus>> serverStatuses = new HashMap<Integer, List<ShockStatus>>();
	private static final HashMap<Integer, List<ShockStatus>> clientStatuses = new HashMap<Integer, List<ShockStatus>>();

	@SubscribeEvent
	public void onEntityJoinWorld(EntityJoinWorldEvent event) {
		if(event.world == null || event.world.isRemote || !(event.entity instanceof EntityNukeExplosionMK5)) {
			return;
		}

		EntityNukeExplosionMK5 explosion = (EntityNukeExplosionMK5) event.entity;
		if(explosion.ticksExisted > 1) {
			return;
		}

		trigger(event.world, explosion.posX, explosion.posZ, explosion.length);
	}

	@SubscribeEvent
	public void onServerTick(TickEvent.ServerTickEvent event) {
		if(event.phase != TickEvent.Phase.START || serverStatuses.isEmpty()) {
			return;
		}

		World overworld = cpw.mods.fml.common.FMLCommonHandler.instance().getMinecraftServerInstance() != null
			? cpw.mods.fml.common.FMLCommonHandler.instance().getMinecraftServerInstance().worldServerForDimension(0)
			: null;
		if(overworld == null) {
			return;
		}

		cleanup(serverStatuses, overworld.getTotalWorldTime());
	}

	@SubscribeEvent
	public void onWorldUnload(WorldEvent.Unload event) {
		if(event.world == null) {
			return;
		}

		if(event.world.isRemote) {
			clientStatuses.clear();
			return;
		}

		serverStatuses.remove(event.world.provider.dimensionId);
	}

	public static void trigger(World world, double x, double z, float blastRadius) {
		if(world == null || world.isRemote) {
			return;
		}

		Target target = CelestialBody.getTarget(world, (int) Math.floor(x), (int) Math.floor(z));
		CelestialBody body = target != null ? target.body : null;
		if(body == null) {
			return;
		}

		float rawX = 0.18F + hashToUnit(x * 0.03125D + z * 0.0125D + world.getTotalWorldTime() * 0.017D) * 0.64F;
		float rawY = 0.22F + hashToUnit(x * -0.015D + z * 0.0275D + world.getTotalWorldTime() * 0.011D + 19.0D) * 0.52F;
		float centerX = quantizeCenter(rawX);
		float centerY = quantizeCenter(rawY);
		float strength = MathHelper.clamp_float(blastRadius / 140.0F, 0.35F, 1.0F);

		List<ShockStatus> statuses = serverStatuses.get(body.dimensionId);
		if(statuses == null) {
			statuses = new ArrayList<ShockStatus>();
			serverStatuses.put(body.dimensionId, statuses);
		}

		pruneStatuses(statuses, world.getTotalWorldTime());
		statuses.add(new ShockStatus(world.getTotalWorldTime(), centerX, centerY, strength));
		while(statuses.size() > MAX_ACTIVE_SHOCKS) {
			statuses.remove(0);
		}
	}

	public static List<ShockStatus> getClientShocks(CelestialBody body) {
		if(body == null) {
			return Collections.emptyList();
		}

		List<ShockStatus> statuses = clientStatuses.get(body.dimensionId);
		return statuses != null ? statuses : Collections.<ShockStatus>emptyList();
	}

	public static void writeSync(ByteBuf buf, World world) {
		cleanup(serverStatuses, world.getTotalWorldTime());

		buf.writeShort(serverStatuses.size());
		for(Map.Entry<Integer, List<ShockStatus>> entry : serverStatuses.entrySet()) {
			List<ShockStatus> statuses = entry.getValue();
			buf.writeInt(entry.getKey());
			buf.writeByte(statuses.size());
			for(ShockStatus status : statuses) {
				buf.writeLong(status.time);
				buf.writeFloat(status.centerX);
				buf.writeFloat(status.centerY);
				buf.writeFloat(status.strength);
			}
		}
	}

	public static void readSync(ByteBuf buf) {
		clientStatuses.clear();

		int count = buf.readShort();
		for(int i = 0; i < count; i++) {
			int dimensionId = buf.readInt();
			int shockCount = buf.readUnsignedByte();
			List<ShockStatus> statuses = new ArrayList<ShockStatus>(shockCount);
			for(int j = 0; j < shockCount; j++) {
				long time = buf.readLong();
				float centerX = buf.readFloat();
				float centerY = buf.readFloat();
				float strength = buf.readFloat();
				statuses.add(new ShockStatus(time, centerX, centerY, strength));
			}
			clientStatuses.put(dimensionId, statuses);
		}
	}

	private static void cleanup(HashMap<Integer, List<ShockStatus>> statuses, long currentTime) {
		Iterator<Map.Entry<Integer, List<ShockStatus>>> iterator = statuses.entrySet().iterator();
		while(iterator.hasNext()) {
			Map.Entry<Integer, List<ShockStatus>> entry = iterator.next();
			pruneStatuses(entry.getValue(), currentTime);
			if(entry.getValue().isEmpty()) {
				iterator.remove();
			}
		}
	}

	private static void pruneStatuses(List<ShockStatus> statuses, long currentTime) {
		Iterator<ShockStatus> iterator = statuses.iterator();
		while(iterator.hasNext()) {
			ShockStatus status = iterator.next();
			if(currentTime - status.time > MAX_AGE) {
				iterator.remove();
			}
		}
	}

	private static float hashToUnit(double value) {
		return (float) (value - Math.floor(value));
	}

	private static float quantizeCenter(float value) {
		float clamped = MathHelper.clamp_float(value, 0.05F, 0.95F);
		return (MathHelper.floor_float(clamped * FLASH_GRID) + 0.5F) / FLASH_GRID;
	}
}
