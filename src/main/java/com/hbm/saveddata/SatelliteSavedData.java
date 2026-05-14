package com.hbm.saveddata;

import com.hbm.dim.CelestialBody;
import com.hbm.dim.orbit.OrbitalStation;
import com.hbm.saveddata.satellites.Satellite;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraft.world.WorldSavedData;
import net.minecraftforge.common.DimensionManager;

import java.security.InvalidParameterException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public class SatelliteSavedData extends WorldSavedData {

	public final HashMap<Integer, Satellite> sats = new HashMap<>();

	/**
	 * Constructor used for deserialization
	 * @param name - Map data name
	 */
	public SatelliteSavedData(String name) {
		super(name);
	}

	/**
	 * Default constructor for satellites map data.
	 */
	public SatelliteSavedData() {
		super("satellites");
		this.markDirty();
	}

	public boolean isFreqTaken(int freq) {
		return getSatFromFreq(freq) != null;
	}

	public Satellite getSatFromFreq(int freq) {
		return sats.get(freq);
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		int satCount = nbt.getInteger("satCount");

		for(int i = 0; i < satCount; i++) {
			Satellite sat = Satellite.create(nbt.getInteger("sat_id_" + i));
			NBTTagCompound satNbt = (NBTTagCompound) nbt.getTag("sat_data_" + i);
			sat.readFromNBT(satNbt);

			int freq = nbt.getInteger("sat_freq_" + i);
			sats.put(freq, sat);
		}
	}

	@Override
	public void writeToNBT(NBTTagCompound nbt) {
		nbt.setInteger("satCount", sats.size());

		int i = 0;

		for(Entry<Integer, Satellite> struct : sats.entrySet()) {
			NBTTagCompound data = new NBTTagCompound();
			struct.getValue().writeToNBT(data);

			nbt.setInteger("sat_id_" + i, struct.getValue().getID());
			nbt.setTag("sat_data_" + i, data);
			nbt.setInteger("sat_freq_" + i, struct.getKey());
			i++;
		}
	}

	// will return invalid results in orbit, make sure to pass in coordinates!
	private static SatelliteSavedData getDataForWorld(World worldObj) {
		SatelliteSavedData data = (SatelliteSavedData)worldObj.perWorldStorage.loadData(SatelliteSavedData.class, "satellites");
		if(data == null) {
			worldObj.perWorldStorage.setData("satellites", new SatelliteSavedData());

			data = (SatelliteSavedData)worldObj.perWorldStorage.loadData(SatelliteSavedData.class, "satellites");
		}

		return data;
	}

	public static SatelliteSavedData getData(World worldObj) {
		if(CelestialBody.inOrbit(worldObj)) throw new InvalidParameterException("Orbit dimensions MUST pass coordinates!");
		return getDataForWorld(worldObj);
	}

	public static SatelliteSavedData getData(World worldObj, int x, int z) {
		if(!worldObj.isRemote && CelestialBody.inOrbit(worldObj)) {
			int targetDimensionId = OrbitalStation.getStationFromPosition(x, z).orbiting.dimensionId;

			World orbitingWorld = DimensionManager.getWorld(targetDimensionId);
			if(orbitingWorld == null) {
				DimensionManager.initDimension(targetDimensionId);
				orbitingWorld = DimensionManager.getWorld(targetDimensionId);
			}

			if(orbitingWorld != null) {
				worldObj = orbitingWorld;
			}
		}

		return getDataForWorld(worldObj);
	}

	public static SatelliteSavedData getDataFromFreq(World worldObj, int x, int z, int freq) {
		SatelliteSavedData data = getData(worldObj, x, z);
		if(data.getSatFromFreq(freq) != null) {
			return data;
		}
		if(worldObj.provider.dimensionId == -1 || worldObj.provider.dimensionId == 1) {
			return data;
		}

		for(CelestialBody body : CelestialBody.getLandableBodies()) {
			World bodyWorld = DimensionManager.getWorld(body.dimensionId);
			if(bodyWorld == null) continue;

			SatelliteSavedData bodyData = getData(bodyWorld);
			if(bodyData.getSatFromFreq(freq) != null) {
				return bodyData;
			}
		}

		return data;
	}

	public static HashMap<Integer, Satellite> clientSats = new HashMap<>();
	public static HashMap<Integer, HashMap<Integer, Satellite>> clientSatsByDimension = new HashMap<Integer, HashMap<Integer, Satellite>>();

	@SideOnly(Side.CLIENT)
	public static void setClientSats(HashMap<Integer, Satellite> sats) {
		clientSats = sats;
	}

	@SideOnly(Side.CLIENT)
	public static HashMap<Integer, Satellite> getClientSats() {
		return clientSats;
	}

	@SideOnly(Side.CLIENT)
	public static void setClientSatsByDimension(HashMap<Integer, HashMap<Integer, Satellite>> satsByDimension) {
		clientSatsByDimension = satsByDimension != null ? satsByDimension : new HashMap<Integer, HashMap<Integer, Satellite>>();
	}

	@SideOnly(Side.CLIENT)
	public static Map<Integer, Satellite> getClientSats(int dimensionId) {
		HashMap<Integer, Satellite> sats = clientSatsByDimension.get(dimensionId);
		return sats != null ? sats : Collections.<Integer, Satellite>emptyMap();
	}

}
