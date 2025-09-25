package com.hbm.dim.orbit;

import java.util.List;

import com.hbm.dim.CelestialBody;
import com.hbm.dim.SolarSystem;
import com.hbm.dim.SolarSystem.AstroMetric;
import com.hbm.dim.WorldProviderCelestial;
import com.hbm.dim.orbit.OrbitalStation.StationState;
import com.hbm.dim.trait.CBT_Atmosphere;
import com.hbm.dim.trait.CBT_Destroyed;
import com.hbm.handler.atmosphere.ChunkAtmosphereManager;
import com.hbm.lib.Library;
import com.hbm.util.AstronomyUtil;
import com.hbm.util.BobMathUtil;
import com.hbm.util.Compat;

import cpw.mods.fml.common.Loader;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.biome.WorldChunkManagerHell;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraftforge.client.IRenderHandler;

public class WorldProviderOrbit extends WorldProvider {

	// Orbit at an altitude that provides an hour-long realtime orbit (game time is fast so we go slow)
	// We want a consistent orbital period to prevent orbiting too slow or fast (both for player comfort and feel)
	private static final float ORBITAL_PERIOD = 7200;

	public List<AstroMetric> metrics;

	private double eclipseAmount;
	private float celestialAngle;

	protected float getOrbitalAltitude(CelestialBody body) {
		return getAltitudeForPeriod(body.massKg, ORBITAL_PERIOD);
	}

	// r = ∛[(G x Me x T2) / (4π2)]
	private float getAltitudeForPeriod(float massKg, float period) {
		return (float)Math.cbrt((AstronomyUtil.GRAVITATIONAL_CONSTANT * massKg * (period * period)) / (4 * Math.PI * Math.PI));
	}

	public float getSunPower() {
		double progress = OrbitalStation.clientStation.getTransferProgress(0);
		float sunPower = OrbitalStation.clientStation.orbiting.getSunPower();
		if(progress > 0) {
			return (float)BobMathUtil.lerp(progress, sunPower, OrbitalStation.clientStation.target.getSunPower());
		}
		return sunPower;
	}

	@Override
	public void registerWorldChunkManager() {
		this.worldChunkMgr = new WorldChunkManagerHell(BiomeGenOrbit.biome, 0.0F);
	}

	@Override
	public String getDimensionName() {
		return "Orbit";
	}

	@Override
	public IChunkProvider createChunkGenerator() {
		return new ChunkProviderOrbit(this.worldObj);
	}

	@Override
	public void updateWeather() {
		isHellWorld = !worldObj.isRemote && !Loader.isModLoaded(Compat.MOD_COFH);

		worldObj.getWorldInfo().setRainTime(0);
		worldObj.getWorldInfo().setRaining(false);
		worldObj.getWorldInfo().setThunderTime(0);
		worldObj.getWorldInfo().setThundering(false);
		worldObj.rainingStrength = 0.0F;
		worldObj.thunderingStrength = 0.0F;
	}

	// This is called once, at the beginning of every frame
	// so we use this to memoise expensive calcs
	@SideOnly(Side.CLIENT)
	protected void updateSky(float partialTicks) {
		CelestialBody body = CelestialBody.getBody(worldObj);
		OrbitalStation station = OrbitalStation.clientStation;

		// First fetch the suns true size
		double sunSize = SolarSystem.calculateSunSize(body);

		double progress = station.getTransferProgress(partialTicks);

		// Get our orrery of bodies, this is cached for reuse in sky rendering
		if(station.state == StationState.ORBIT) {
			double altitude = getOrbitalAltitude(station.orbiting);
			metrics = SolarSystem.calculateMetricsFromSatellite(worldObj, partialTicks, station.orbiting, altitude);
		} else {
			double fromAlt = getOrbitalAltitude(station.orbiting);
			double toAlt = getOrbitalAltitude(station.target);
			metrics = SolarSystem.calculateMetricsBetweenSatelliteOrbits(worldObj, partialTicks, station.orbiting, station.target, fromAlt, toAlt, progress);
		}

		// Get our sun angle
		CelestialBody orbiting = station.orbiting;
		CelestialBody target = station.target;
		float angle = (float)SolarSystem.calculateSingleAngle(worldObj, partialTicks, metrics, orbiting, getOrbitalAltitude(orbiting));
		if(progress > 0) {
			angle = (float)BobMathUtil.lerp(progress, angle, (float)SolarSystem.calculateSingleAngle(worldObj, partialTicks, metrics, target, getOrbitalAltitude(target)));
		}

		celestialAngle = 0.5F - (angle / 360.0F);

		// Get our eclipse amount
		eclipseAmount = WorldProviderCelestial.getEclipseFactor(metrics, sunSize);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public Vec3 getFogColor(float x, float y) {
		return Vec3.createVectorHelper(0, 0, 0);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public Vec3 getSkyColor(Entity camera, float partialTicks) {
		// getSkyColor is called first on every frame, so if you want to memoise anything, do it here
		updateSky(partialTicks);

		return Vec3.createVectorHelper(0, 0, 0);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public float[] calcSunriseSunsetColors(float solarAngle, float partialTicks) {
		return null;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public float getStarBrightness(float par1) {
		// Stars look cool in orbit, but obvs at Moho we don't want the big fuckoff sun to not extinguish
		// Stars become visible during the day part of orbit just before Earth
		// And are fully visible during the day beyond the orbit of Duna
		float distanceStart = 9_000_000;
		float distanceEnd = 30_000_000;

		double progress = OrbitalStation.clientStation.getTransferProgress(par1);
		float semiMajorAxisKm = OrbitalStation.clientStation.orbiting.getPlanet().semiMajorAxisKm;
		if(progress > 0) {
			semiMajorAxisKm = (float)BobMathUtil.lerp(progress, semiMajorAxisKm, OrbitalStation.clientStation.target.getPlanet().semiMajorAxisKm);
		}

		float distanceFactor = MathHelper.clamp_float((semiMajorAxisKm - distanceStart) / (distanceEnd - distanceStart), 0F, 1F);

		float solarAngle = worldObj.getCelestialAngle(par1);
		float celestialPhase = (1 - (solarAngle + 0.5F) % 1) * 2 - 1;
		float starBrightness = (float)Library.smoothstep(Math.abs(celestialPhase), 0.6, 0.75);

		return MathHelper.clamp_float(starBrightness, distanceFactor, 1F);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public float getSunBrightness(float par1) {
		if(SolarSystem.kerbol.hasTrait(CBT_Destroyed.class))
			return 0;

		float solarAngle = worldObj.getCelestialAngle(par1);
		float celestialPhase = (1 - (solarAngle + 0.5F) % 1) * 2 - 1;

		float sunBrightness = 1 - (float)Library.smoothstep(Math.abs(celestialPhase), 0.6, 0.8);

		sunBrightness *= 1 - eclipseAmount * 0.6;

		return sunBrightness;
	}

	@Override
	public boolean canDoLightning(Chunk chunk) {
		return false;
	}

	@Override
	public boolean canDoRainSnowIce(Chunk chunk) {
		return false;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public float getCloudHeight() {
		return -99999;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public IRenderHandler getSkyRenderer() {
		return new SkyProviderOrbit();
	}

	// Just fetches a memoised angle instead, for speed
	@Override
	public float calculateCelestialAngle(long worldTime, float partialTicks) {
		return celestialAngle;
	}

	// Same shit as in Celestial
	@Override
	public int getRespawnDimension(EntityPlayerMP player) {
		ChunkCoordinates coords = player.getBedLocation(dimensionId);

		// If no bed, respawn in overworld
		if(coords == null)
			return 0;

		// If the bed location has no breathable atmosphere, respawn in overworld
		CBT_Atmosphere atmosphere = ChunkAtmosphereManager.proxy.getAtmosphere(worldObj, coords.posX, coords.posY, coords.posZ);
		if(!ChunkAtmosphereManager.proxy.canBreathe(atmosphere))
			return 0;

		return dimensionId;
	}

	@Override
	public boolean canRespawnHere() {
		if(WorldProviderCelestial.attemptingSleep) {
			WorldProviderCelestial.attemptingSleep = false;
			return true;
		}

		return false;
	}

}
