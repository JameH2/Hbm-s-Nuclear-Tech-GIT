package com.hbm.packet;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import com.hbm.dim.CelestialBody;
import com.hbm.dim.SolarSystemWorldSavedData;
import com.hbm.dim.WorldProviderCelestial;
import com.hbm.dim.trait.CBT_War;
import com.hbm.dim.trait.CBT_War.Projectile;
import com.hbm.dim.trait.CelestialBodyTrait;
import com.hbm.handler.ImpactWorldHandler;
import com.hbm.handler.pollution.PollutionHandler;
import com.hbm.handler.pollution.PollutionHandler.PollutionData;
import com.hbm.handler.pollution.PollutionHandler.PollutionType;
import com.hbm.main.MainRegistry;
import com.hbm.potion.HbmPotion;
import com.hbm.saveddata.SatelliteSavedData;
import com.hbm.saveddata.TomSaveData;
import com.hbm.saveddata.satellites.Satellite;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.world.World;

/**
 * Utility for permanently synchronizing values every tick with a player in the given context of a world.
 * Uses the Byte Buffer directly instead of NBT to cut back on unnecessary data.
 * @author hbm
 */
public class PermaSyncHandler {

	public static HashSet<Integer> boykissers = new HashSet<Integer>();
	public static float[] pollution = new float[PollutionType.values().length];

	public static void writePacket(ByteBuf buf, World world, EntityPlayerMP player) {

		/// TOM IMPACT DATA ///
		TomSaveData data = TomSaveData.forWorld(world);
		buf.writeFloat(data.fire);
		buf.writeFloat(data.dust);
		buf.writeBoolean(data.impact);
		buf.writeLong(data.time);
		/// TOM IMPACT DATA ///

		/// SHITTY MEMES ///
		List<Integer> ids = new ArrayList<Integer>();
		for(Object o : world.playerEntities) {
			EntityPlayer p = (EntityPlayer) o;
			if(p.isPotionActive(HbmPotion.death.id)) {
				ids.add(p.getEntityId());
			}
		}
		buf.writeShort((short) ids.size());
		for(Integer i : ids) buf.writeInt(i);
		/// SHITTY MEMES ///

		/// POLLUTION ///
		PollutionData pollution = PollutionHandler.getPollutionData(world, (int) Math.floor(player.posX), (int) Math.floor(player.posY), (int) Math.floor(player.posZ));
		if(pollution == null) pollution = new PollutionData();
		for(int i = 0; i < PollutionType.values().length; i++) {
			buf.writeFloat(pollution.pollution[i]);
		}
		/// POLLUTION ///

		/// CBT ///
		if(world.getTotalWorldTime() % 5 == 1) { // update a little less frequently to not blast the players with large packets
			buf.writeBoolean(true);

			SolarSystemWorldSavedData solarSystemData = SolarSystemWorldSavedData.get(world);
			for(CelestialBody body : CelestialBody.getAllBodies()) {
				HashMap<Class<? extends CelestialBodyTrait>, CelestialBodyTrait> traits = solarSystemData.getTraits(body.name);
				if(traits != null) {
					buf.writeBoolean(true); // Has traits marker (since we can have an empty list)
					buf.writeInt(traits.size());

					for(int i = 0; i < CelestialBodyTrait.traitList.size(); i++) {
						Class<? extends CelestialBodyTrait> traitClass = CelestialBodyTrait.traitList.get(i);
						CelestialBodyTrait trait = traits.get(traitClass);

						if(trait != null) {
							buf.writeInt(i); // ID of the trait, in order registered
							trait.writeToBytes(buf);
						}
					}
				} else {
					buf.writeBoolean(false);
				}
			}
		} else {
			buf.writeBoolean(false);
		}
		/// CBT ///

		/// SATELLITES ///
		// Only syncs data required for rendering satellites on the client
		HashMap<Integer, Satellite> sats = SatelliteSavedData.getData(world, (int)player.posX, (int)player.posZ).sats;
		buf.writeInt(sats.size());
		for(Map.Entry<Integer, Satellite> entry : sats.entrySet()) {
			buf.writeInt(entry.getKey());
			buf.writeInt(entry.getValue().getID());
			entry.getValue().serialize(buf);

		}
		/// SATELLITES ///

		/// TIME OF DAY ///
		if(world.provider instanceof WorldProviderCelestial && world.provider.dimensionId != 0) {
			buf.writeBoolean(true);
			((WorldProviderCelestial) world.provider).serialize(buf);
		} else {
			buf.writeBoolean(false);
		}
		/// TIME OF DAY ///

		/// RIDING DESYNC FIX ///
		if(player.ridingEntity != null) {
			buf.writeInt(player.ridingEntity.getEntityId());
		} else {
			buf.writeInt(-1);
		}
		/// RIDING DESYNC FIX ///

		// TODO: take out back and shoot
		CBT_War war = CelestialBody.getTrait(world, CBT_War.class);
		if (war != null) {
			List<Projectile> projectiles = war.getProjectiles();
			for (Projectile projectile : projectiles) {
				buf.writeFloat(projectile.getFlashtime());
				buf.writeFloat(projectile.getTravel());
			}
		}
		// EFFECTS THAT I DONT KNOW HOW TO GET WORKING ELSEWHERE :P //
	}

	public static void readPacket(ByteBuf buf, World world, EntityPlayer player) {

		/// TOM IMPACT DATA ///
		ImpactWorldHandler.lastSyncWorld = player.worldObj;
		ImpactWorldHandler.fire = buf.readFloat();
		ImpactWorldHandler.dust = buf.readFloat();
		ImpactWorldHandler.impact = buf.readBoolean();
		ImpactWorldHandler.time = buf.readLong();
		/// TOM IMPACT DATA ///

		/// SHITTY MEMES ///
		boykissers.clear();
		int ids = buf.readShort();
		for(int i = 0; i < ids; i++) boykissers.add(buf.readInt());
		/// SHITTY MEMES ///

		/// POLLUTION ///
		for(int i = 0; i < PollutionType.values().length; i++) {
			pollution[i] = buf.readFloat();
		}
		/// POLLUTION ///

		/// CBT ///
		if(buf.readBoolean()) {
			try {
				HashMap<String, HashMap<Class<? extends CelestialBodyTrait>, CelestialBodyTrait>> traitMap = SolarSystemWorldSavedData.clientTraits;

				if(traitMap == null) {
					traitMap = new HashMap<String, HashMap<Class<? extends CelestialBodyTrait>, CelestialBodyTrait>>();
					SolarSystemWorldSavedData.updateClientTraits(traitMap);
				}

				for(CelestialBody body : CelestialBody.getAllBodies()) {
					if(buf.readBoolean()) {
						HashMap<Class<? extends CelestialBodyTrait>, CelestialBodyTrait> traits = traitMap.get(body.name);

						if(traits == null) {
							traits = new HashMap<Class<? extends CelestialBodyTrait>, CelestialBodyTrait>();
							traitMap.put(body.name, traits);
						}

						List<Class<? extends CelestialBodyTrait>> sentTraits = new ArrayList<>();

						int cbtSize = buf.readInt();
						for(int i = 0; i < cbtSize; i++) {
							Class<? extends CelestialBodyTrait> clazz = CelestialBodyTrait.traitList.get(buf.readInt());
							sentTraits.add(clazz);

							CelestialBodyTrait trait = traits.getOrDefault(clazz, clazz.newInstance());
							trait.readFromBytes(buf);

							traits.put(trait.getClass(), trait);
						}

						traits.keySet().removeIf(traitClass -> !sentTraits.contains(traitClass));
					} else {
						traitMap.remove(body.name);
					}
				}
			} catch (Exception ex) {
				// If any exception occurs, stop parsing any more bytes, they'll be unaligned
				// We'll unset the client trait set to prevent any issues

				MainRegistry.logger.catching(ex);
				SolarSystemWorldSavedData.updateClientTraits(null);

				return;
			}
		}
		/// CBT ///

		/// SATELLITES ///
		int satSize = buf.readInt();
		HashMap<Integer, Satellite> sats = new HashMap<Integer, Satellite>();
		for(int i = 0; i < satSize; i++) {
			int satelliteID = buf.readInt();

			Satellite satellite = Satellite.create(buf.readInt());

			sats.put(satelliteID, satellite);

			satellite.deserialize(buf);

		}

		SatelliteSavedData.setClientSats(sats);

		/// SATELLITES ///

		/// TIME OF DAY ///
		if(buf.readBoolean() && world.provider instanceof WorldProviderCelestial) {
			((WorldProviderCelestial) world.provider).deserialize(buf);
		}
		/// TIME OF DAY ///

		/// RIDING DESYNC FIX ///
		int ridingId = buf.readInt();
		if(ridingId >= 0 && player.ridingEntity == null) {
			Entity entity = world.getEntityByID(ridingId);
			player.mountEntity(entity);
		}
		/// RIDING DESYNC FIX ///

		// TODO: remove this or lose your leg bone privileges
		CBT_War war = CelestialBody.getTrait(world, CBT_War.class);
		if (war != null) {
			List<Projectile> projectiles = war.getProjectiles();
			for (Projectile projectile : projectiles){
					float flashtime = buf.readFloat();
					float traveltime = buf.readFloat();

					projectile.setFlashtime(flashtime);
					projectile.setTravel(traveltime);
				}
			}
		// EFFECTS THAT I DONT KNOW HOW TO GET WORKING ELSEWHERE :P //
	}

}
