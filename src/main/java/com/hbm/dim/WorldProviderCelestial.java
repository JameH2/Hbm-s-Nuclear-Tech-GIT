package com.hbm.dim;

import java.util.ArrayList;
import java.util.Random;
import java.util.List;
import java.util.ListIterator;

import com.hbm.config.GeneralConfig;
import com.hbm.dim.SolarSystem.AstroMetric;
import com.hbm.dim.orbit.WorldProviderOrbit;
import com.hbm.dim.trait.CBT_Atmosphere;
import com.hbm.dim.trait.CBT_Atmosphere.FluidEntry;
import com.hbm.dim.trait.CBT_Destroyed;
import com.hbm.dim.trait.CBT_Weather;
import com.hbm.dim.trait.CBT_War;
import com.hbm.dim.trait.CBT_Water;
import com.hbm.handler.ImpactWorldHandler;
import com.hbm.handler.atmosphere.ChunkAtmosphereManager;
import com.hbm.inventory.FluidStack;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.main.MainRegistry;
import com.hbm.util.Compat;

import cpw.mods.fml.common.Loader;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;
import net.minecraft.util.WeightedRandomFishable;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.WorldProviderSurface;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.client.IRenderHandler;
import net.minecraftforge.client.event.EntityViewRenderEvent.FogDensity;

public abstract class WorldProviderCelestial extends WorldProviderSurface {

	public CelestialBody body;

	public List<AstroMetric> metrics;

	private double eclipseAmount;
	private long localTime = -1;

	public static ArrayList<Meteor> meteors = new ArrayList<>();

	@Override
	public abstract void registerWorldChunkManager();

	// Ore gen will attempt to replace this block with ores
	public Block getStone() {
		return Blocks.stone;
	}

	// What fluid is required to extract new bedrock ores
	public FluidStack getBedrockAcid() {
		return null;
	}

	// Should we generate bedrock ice
	public boolean hasIce() {
		return body.hasIce;
	}

	public boolean hasLife() {
		return false;
	}

	public int getWaterOpacity() {
		return 3;
	}

	// this is the stupidest possible method to override, but also our only reasonable option
	@Override
	protected void generateLightBrightnessTable() {
		super.generateLightBrightnessTable();
		body = CelestialBody.getBody(worldObj);
	}

	@Override
	public void updateWeather() {
		CBT_Atmosphere atmosphere = body.getTrait(CBT_Atmosphere.class);
		double pressure = atmosphere != null ? atmosphere.getPressure() : 0;

		// Will prevent water from existing, will be unset immediately before using a bucket if inside a pressurized room
		isHellWorld = !worldObj.isRemote && pressure <= 0.2F && !Loader.isModLoaded(Compat.MOD_COFH);

		if(worldObj.isRemote && Minecraft.getMinecraft().thePlayer != null && Minecraft.getMinecraft().thePlayer.dimension == dimensionId) {
			ListIterator<Meteor> iterator = meteors.listIterator();
			while(iterator.hasNext()) {
				Meteor meteor = iterator.next();
				Meteor fragment = meteor.update(worldObj.rand);
				if(meteor.isDead) iterator.remove();
				if(fragment != null) iterator.add(fragment);
			}
		} else {
			// Reset eclipse amount for this tick to uncache
			eclipseAmount = -1;
		}

		if(!hasWeatherCycle()) {
			worldObj.prevRainingStrength = 0.0F;
			worldObj.rainingStrength = 0.0F;
			worldObj.prevThunderingStrength = 0.0F;
			worldObj.thunderingStrength = 0.0F;
			return;
		}

		if(!worldObj.isRemote) {
			CBT_Weather weather = CBT_Weather.ensureTrait(body);
			if(weather != null && weather.updateForTick(MinecraftServer.getServer().getTickCounter(), worldObj.rand, body)) {
				SolarSystemWorldSavedData.get(worldObj).markDirty();
			}
			weather = body.getTrait(CBT_Weather.class);
			if(weather != null) {
				worldObj.prevRainingStrength = weather.prevRainStrength;
				worldObj.rainingStrength = weather.rainStrength;
				worldObj.prevThunderingStrength = weather.prevThunderStrength;
				worldObj.thunderingStrength = weather.thunderStrength;
			}
		}
	}


	// Can be overridden to provide fog changing events based on weather
	public float fogDensity(FogDensity event) {
		CBT_Atmosphere atmosphere = CelestialBody.getTrait(worldObj, CBT_Atmosphere.class);
		if(atmosphere == null) return 0;

		float pressure = (float)atmosphere.getPressure();

		if(pressure <= 2F) return 0;

		return pressure * pressure * 0.002F;
	}

	/**
	 * Read/write for weather data and anything else you wanna store that is per planet and not for every body
	 * the serialization function synchronizes weather data to the player
	 *
	 * also we don't need to mark the WorldSavedData as dirty because the world time is updated every tick and marks it as such
	 */
	public void writeToNBT(NBTTagCompound nbt) {

	}

	public void readFromNBT(NBTTagCompound nbt) {

	}

	public void serialize(ByteBuf buf) {
		buf.writeLong(getWorldTime());
	}

	public void deserialize(ByteBuf buf) {
		long time = buf.readLong();

		// Allow a half second desync for smoothness
		if(Math.abs(time - getWorldTime()) > 10) {
			setWorldTime(time);
		}
	}


	/**
	 * Override to modify the lightmap, return true if the lightmap is actually modified
	 * @param lightmap a 16x16 lightmap stored in a 256 value buffer
	 * @return whether or not the dynamic lightmap texture needs to be updated
	 */
	public boolean updateLightmap(int[] lightmap) {
		return false;
	}

	protected final int packColor(final int[] colors) {
		return packColor(colors[0], colors[1], colors[2]);
	}

	protected final int packColor(final int r, final int g, final int b) {
		return 255 << 24 | r << 16 | g << 8 | b;
	}

	protected final int[] unpackColor(final int color) {
		final int[] colors = new int[3];
		colors[0] = color >> 16 & 255;
		colors[1] = color >> 8 & 255;
		colors[2] = color & 255;
		return colors;
	}

	// This is called once, at the beginning of every frame
	// so we use this to memoise expensive calcs
	@SideOnly(Side.CLIENT)
	protected void updateSky(float partialTicks) {
		// First fetch the suns true size
		double sunSize = SolarSystem.calculateSunSize(body);
		float solarAngle = worldObj.getCelestialAngle(partialTicks);

		// Get our orrery of bodies, this is cached for reuse in sky rendering
		metrics = SolarSystem.calculateMetricsFromBody(worldObj, partialTicks, body, solarAngle);

		// Get our eclipse amount
		eclipseAmount = getEclipseFactor(metrics, sunSize, SolarSystem.MAX_APPARENT_SIZE_SURFACE);
	}

	public static double getEclipseFactor(List<AstroMetric> metrics, double sunSize, double maxSize) {
		double factor = 0;
		double sunArc = getArc(sunSize);

		// Calculate eclipse
		for(AstroMetric metric : metrics) {
			if(metric.apparentSize < 1) continue;

			double planetArc = getArc(MathHelper.clamp_double(metric.apparentSize, 0, maxSize));
			double minPhase = 1 - (planetArc + sunArc);
			double maxPhase = 1 - (planetArc - sunArc);
			if(metric.phaseObscure < minPhase) continue;

			double thisFactor = 1 - (metric.phaseObscure - maxPhase) / (minPhase - maxPhase);

			factor = Math.min(Math.max(factor, thisFactor), 1.0);
		}

		return factor;
	}

	// due to rendering, the arc is not exactly 1deg = 1deg, this converts from apparentSize to 0-1
	// note that we are rendering flat quads, so the arc size is warped more the larger you get!
	private static double getArc(double apparentSize) {
		return apparentSize * 0.0017D + Math.sqrt(apparentSize * 0.00003D);
	}

	// Might refactor all the separate fluid color calcs into using just this one (but they all vary slightly so not yet)
	// For now, it'll go here, next to the other fluid color stuff, so we don't forget about it
	// also, lightning sky tinting doesn't actually work outside of earth air/oxygen/nitrogen so uh yeah we should fix that lmao
	public static Vec3 getAtmosphereFluidColor(FluidType fluid) {
		if(fluid == null) {
			return Vec3.createVectorHelper(1.0D, 1.0D, 1.0D);
		}

		if(fluid == Fluids.EVEAIR) {
			return Vec3.createVectorHelper(53F / 255F, 32F / 255F, 74F / 255F);
		}

		// Slightly redder "red sand" tint for Duna-like atmospheres.
		if(fluid == Fluids.DUNAAIR) {
			return Vec3.createVectorHelper(198F / 255F, 96F / 255F, 64F / 255F);
		}

		// Neutral/desaturated CO2 tint.
		if(fluid == Fluids.CARBONDIOXIDE) {
			return Vec3.createVectorHelper(188F / 255F, 192F / 255F, 198F / 255F);
		}

		if(fluid == Fluids.EARTHAIR || fluid == Fluids.OXYGEN || fluid == Fluids.NITROGEN) {
			return Vec3.createVectorHelper(0.7529412F, 0.84705883F, 1.0F);
		}

		return getColorFromHex(fluid.getColor());
	}

	private static Vec3 getColorFromHex(int hexColor) {
		float red = ((hexColor >> 16) & 0xFF) / 255.0F;
		float green = ((hexColor >> 8) & 0xFF) / 255.0F;
		float blue = (hexColor & 0xFF) / 255.0F;
		return Vec3.createVectorHelper(red, green, blue);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public Vec3 getFogColor(float solarAngle, float y) {
		CBT_Atmosphere atmosphere = CelestialBody.getTrait(worldObj, CBT_Atmosphere.class);

		// The cold hard vacuum of space
		if(atmosphere == null) return Vec3.createVectorHelper(0, 0, 0);

		float sun = MathHelper.clamp_float(MathHelper.cos(solarAngle * (float)Math.PI * 2.0F) * 2.0F + 0.5F, 0.0F, 1.0F);

		float sunR = sun;
		float sunG = sun;
		float sunB = sun;

		if(!GeneralConfig.enableHardcoreDarkness) {
			sunR *= 0.94F;
			sunG *= 0.94F;
			sunB *= 0.91F;
		}

		float totalPressure = (float)atmosphere.getPressure();
		Vec3 color = Vec3.createVectorHelper(0, 0, 0);

		for(int i = 0; i < atmosphere.fluids.size(); i++) {
			FluidEntry entry = atmosphere.fluids.get(i);
			Vec3 fluidColor;

			if(entry.fluid == Fluids.EVEAIR) {
				fluidColor = Vec3.createVectorHelper(53F / 255F * sunR, 32F / 255F * sunG, 74F / 255F * sunB);
			} else if(entry.fluid == Fluids.DUNAAIR || entry.fluid == Fluids.CARBONDIOXIDE) {
				fluidColor = Vec3.createVectorHelper(212F / 255F * sunR, 112F / 255F * sunG, 78F / 255F * sunB);
			} else if(entry.fluid == Fluids.EARTHAIR || entry.fluid == Fluids.OXYGEN || entry.fluid == Fluids.NITROGEN) {
				// Default to regular ol' overworld
				fluidColor = Vec3.createVectorHelper(0.7529412F * sunR, 0.84705883F * sunG, 1.0F * sunB);
			} else {
				fluidColor = getColorFromHex(entry.fluid.getColor());
				fluidColor.xCoord *= sunR * 1.4F;
				fluidColor.yCoord *= sunG * 1.4F;
				fluidColor.zCoord *= sunB * 1.4F;
			}

			float percentage = (float)entry.pressure / totalPressure;
			color = Vec3.createVectorHelper(
				color.xCoord + fluidColor.xCoord * percentage,
				color.yCoord + fluidColor.yCoord * percentage,
				color.zCoord + fluidColor.zCoord * percentage
			);
		}

		// Add minimum fog colour, for night-time glow
		if(!GeneralConfig.enableHardcoreDarkness) {
			float nightDensity = MathHelper.clamp_float(totalPressure, 0.0F, 1.0F);
			color.xCoord += 0.06F * nightDensity;
			color.yCoord += 0.06F * nightDensity;
			color.zCoord += 0.09F * nightDensity;
		}

		// Fog intensity remains high to simulate a thin looking atmosphere on low pressure planets
		float pressureFactor = MathHelper.clamp_float(totalPressure * 10.0F, 0.0F, 1.0F);
		color.xCoord *= pressureFactor;
		color.yCoord *= pressureFactor;
		color.zCoord *= pressureFactor;

		if(Minecraft.getMinecraft().renderViewEntity.posY > 600) {
			double curvature = MathHelper.clamp_float((1000.0F - (float)Minecraft.getMinecraft().renderViewEntity.posY) / 400.0F, 0.0F, 1.0F);
			color.xCoord *= curvature;
			color.yCoord *= curvature;
			color.zCoord *= curvature;
		}

		if(eclipseAmount > 0) {
			color.xCoord *= 1 - eclipseAmount * 0.3;
			color.yCoord *= 1 - eclipseAmount * 0.3;
			color.zCoord *= 1 - eclipseAmount * 0.3;

			float[] sunsetFog = calcSunriseSunsetColors(0.25F, 0);
			if(sunsetFog != null) {
				double sunsetAmount = MathHelper.clamp_double(eclipseAmount * 0.5 - (1 - sun), 0.0, 1.0);
				color.xCoord = color.xCoord * (1.0F - sunsetAmount) + sunsetFog[0] * sunsetAmount;
				color.yCoord = color.yCoord * (1.0F - sunsetAmount) + sunsetFog[1] * sunsetAmount;
				color.zCoord = color.zCoord * (1.0F - sunsetAmount) + sunsetFog[2] * sunsetAmount;
			}
		}

		float dust = ImpactWorldHandler.getDustForClient(worldObj);
		float fire = ImpactWorldHandler.getFireForClient(worldObj);

		color.yCoord *= 1 - (dust * 0.5F);
		color.zCoord *= 1 - dust;

		if(fire > 0) {
			color.xCoord *= Math.max((1 - (dust * 2)), 0);
			color.yCoord *= Math.max((1 - (dust * 2)), 0);
			color.zCoord *= Math.max((1 - (dust * 2)), 0);
		} else {
			color.xCoord *= 1 - dust;
			color.yCoord *= 1 - dust;
			color.zCoord *= 1 - dust;
		}

		return color;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public Vec3 getSkyColor(Entity camera, float partialTicks) {
		// getSkyColor is called first on every frame, so if you want to memoise anything, do it here
		updateSky(partialTicks);

		CBT_Atmosphere atmosphere = CelestialBody.getTrait(worldObj, CBT_Atmosphere.class);
		Vec3 color = Vec3.createVectorHelper(0, 0, 0);

		// The cold hard vacuum of space
		if(atmosphere == null) {
			return color;
		}

		float sun = this.getSunBrightnessFactor(1.0F);
		float totalPressure = (float)atmosphere.getPressure();

		for(int i = 0; i < atmosphere.fluids.size(); i++) {
			FluidEntry entry = atmosphere.fluids.get(i);
			Vec3 fluidColor;

			if(entry.fluid == Fluids.EVEAIR) {
				fluidColor = Vec3.createVectorHelper(53F / 255F * sun, 32F / 255F * sun, 74F / 255F * sun);
			} else if(entry.fluid == Fluids.DUNAAIR || entry.fluid == Fluids.CARBONDIOXIDE) {
				fluidColor = Vec3.createVectorHelper(212F / 255F * sun, 112F / 255F * sun, 78F / 255F * sun);
			} else if(entry.fluid == Fluids.EARTHAIR || entry.fluid == Fluids.OXYGEN || entry.fluid == Fluids.NITROGEN) {
				// Default to regular ol' overworld
				fluidColor = super.getSkyColor(camera, partialTicks);
			} else {
				fluidColor = getColorFromHex(entry.fluid.getColor());
				fluidColor.xCoord *= sun;
				fluidColor.yCoord *= sun;
				fluidColor.zCoord *= sun;
			}

			float percentage = (float)entry.pressure / totalPressure;
			color = Vec3.createVectorHelper(
				color.xCoord + fluidColor.xCoord * percentage,
				color.yCoord + fluidColor.yCoord * percentage,
				color.zCoord + fluidColor.zCoord * percentage
			);
		}

		CBT_War wardat = body.getTrait(CBT_War.class);

		if(wardat != null) {
			for(int i = 0; i < wardat.getProjectiles().size(); i++) {
				CBT_War.Projectile projectile = wardat.getProjectiles().get(i);
				float flash = projectile.getFlashtime();
				if(projectile.getAnimtime() > 0) {
					float invertedFlash = 100 - flash;

					color.xCoord += invertedFlash * 0.5;
					color.yCoord += invertedFlash * 0.5;
					color.zCoord += invertedFlash * 0.5;
				}
			}
		}

		// Lower pressure sky renders thinner
		float pressureFactor = MathHelper.clamp_float(totalPressure, 0.0F, 1.0F);
		color.xCoord *= pressureFactor;
		color.yCoord *= pressureFactor;
		color.zCoord *= pressureFactor;

		if(eclipseAmount > 0) {
			color.xCoord *= 1 - eclipseAmount * 0.6;
			color.yCoord *= 1 - eclipseAmount * 0.6;
			color.zCoord *= 1 - eclipseAmount * 0.5;
		}

		float dust = ImpactWorldHandler.getDustForClient(worldObj);
		float fire = ImpactWorldHandler.getFireForClient(worldObj);

		if(dust > 0) {
			if(fire > 0) {
				color.xCoord *= 1.3;
				color.yCoord *= Math.max((1 - (dust * 1.4f)), 0);
				color.zCoord *= Math.max((1 - (dust * 4)), 0);
			} else {
				color.yCoord *= 1 - (dust * 0.5F);
				color.zCoord *= Math.max((1 - (dust * 4)), 0);
			}

			color.xCoord *= fire + (1 - dust);
			color.yCoord *= fire + (1 - dust);
			color.zCoord *= fire + (1 - dust);
		}

		return color;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public float[] calcSunriseSunsetColors(float solarAngle, float partialTicks) {
		CBT_Atmosphere atmosphere = CelestialBody.getTrait(worldObj, CBT_Atmosphere.class);
		if(atmosphere == null || atmosphere.getPressure() < 0.05F) return null;

		float[] colors = super.calcSunriseSunsetColors(solarAngle, partialTicks);
		if(colors == null) return null;

		// Mars IRL has inverted blue sunsets, which look cool as
		// So carbon dioxide rich atmospheres will do the same
		// for now, it's just a swizzle between red and blue
		if(atmosphere.hasFluid(Fluids.DUNAAIR) || atmosphere.hasFluid(Fluids.CARBONDIOXIDE)) {
			float tmp = colors[0];
			colors[0] = colors[2];
			colors[2] = tmp;
		} else if(atmosphere.hasFluid(Fluids.EVEAIR)) {
			float f2 = 0.4F;
			float f3 = MathHelper.cos((solarAngle) * (float)Math.PI * 2.0F) - 0.0F;
			float f4 = -0.0F;

			if(f3 >= f4 - f2 && f3 <= f4 + f2) {
				float f5 = (f3 - f4) / f2 * 0.5F + 0.5F;
				float f6 = 1.0F - (1.0F - MathHelper.sin(f5 * (float)Math.PI)) * 0.99F;
				f6 *= f6;
				colors[0] = f5 * 0.01F;
				colors[1] = f5 * f5 * 0.9F + 0.3F;
				colors[2] = f5 * f5;
				colors[3] = f6;
			}
		} else if( atmosphere.hasFluid(Fluids.TEKTOAIR) ||  atmosphere.hasFluid(Fluids.JOOLGAS) || atmosphere.hasFluid(Fluids.CHLORINE)) {
			float tmp = colors[1];
			colors[1] = colors[2];
			colors[2] = tmp;
		}

		float dustFactor = 1 - ImpactWorldHandler.getDustForClient(worldObj);
		colors[0] *= dustFactor;
		colors[1] *= dustFactor;
		colors[2] *= dustFactor;
		colors[3] *= dustFactor;

		return colors;
	}

	public static int getCloudLayerCount(CBT_Atmosphere atmosphere) {
		if(atmosphere == null || atmosphere.getPressure() < 0.5F) {
			return 0;
		}

		if(atmosphere.getPressure() >= 5.0F) {
			return 3;
		}

		if(atmosphere.getPressure() >= 2.5F) {
			return 2;
		}

		return 1;
	}
	public boolean hasWeatherCycle() {
		return CBT_Weather.supportsWeather(body);
	}

	@SideOnly(Side.CLIENT)
	public Vec3 getWeatherColor() {
		CBT_Water water = CelestialBody.getTrait(worldObj, CBT_Water.class);
		if(water == null || water.fluid == null) {
			return Vec3.createVectorHelper(1.0D, 1.0D, 1.0D);
		}

		Vec3 base = getColorFromHex(water.fluid.getColor());
		double luminance = base.xCoord * 0.299D + base.yCoord * 0.587D + base.zCoord * 0.114D;
		double saturation = 0.35D;

		double desaturatedR = luminance + (base.xCoord - luminance) * saturation;
		double desaturatedG = luminance + (base.yCoord - luminance) * saturation;
		double desaturatedB = luminance + (base.zCoord - luminance) * saturation;

		return Vec3.createVectorHelper(
			MathHelper.clamp_double(desaturatedR, 0.0D, 1.0D),
			MathHelper.clamp_double(desaturatedG, 0.0D, 1.0D),
			MathHelper.clamp_double(desaturatedB, 0.0D, 1.0D)
		);
	}

	@SideOnly(Side.CLIENT)
	public Vec3 getSnowColor() {
		CBT_Water water = CelestialBody.getTrait(worldObj, CBT_Water.class);
		if(water == null || water.fluid == null || water.fluid == Fluids.WATER) {
			return Vec3.createVectorHelper(1.0D, 1.0D, 1.0D);
		}

		return getWeatherColor();
	}

	@Override
	public boolean canDoLightning(Chunk chunk) {
		return hasWeatherCycle();
	}

	@Override
	public boolean canDoRainSnowIce(Chunk chunk) {
		return hasWeatherCycle();
	}

	private IRenderHandler weatherProvider;

	@Override
	@SideOnly(Side.CLIENT)
	public IRenderHandler getWeatherRenderer() {
		if(weatherProvider == null) weatherProvider = new WeatherProviderCelestial();
		return weatherProvider;
	}

	// Stars do not show up during the day in a vacuum, common misconception:
	// The reason stars aren't visible during the day on Earth isn't because of the sky,
	// the sky is ALWAYS there. The reason they aren't visible is because the Sun is too bright!
	@Override
	@SideOnly(Side.CLIENT)
	public float getStarBrightness(float par1) {
		// Stars become visible during the day beyond the orbit of Duna
		// And are fully visible during the day beyond the orbit of Jool
		float distanceStart = 20_000_000;
		float distanceEnd = 80_000_000;

		float semiMajorAxisKm = CelestialBody.getPlanet(worldObj).semiMajorAxisKm;
		float distanceFactor = MathHelper.clamp_float((semiMajorAxisKm - distanceStart) / (distanceEnd - distanceStart), 0F, 1F);

		float starBrightness = super.getStarBrightness(par1);

		float dust = ImpactWorldHandler.getDustForClient(worldObj);

		return MathHelper.clamp_float(starBrightness, distanceFactor, 1F) * (1 - dust);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public float getSunBrightness(float par1) {
		if(CelestialBody.getStar(worldObj).hasTrait(CBT_Destroyed.class))
			return 0;

		CBT_Atmosphere atmosphere = CelestialBody.getTrait(worldObj, CBT_Atmosphere.class);

		float sunBrightness = super.getSunBrightness(par1);

		sunBrightness *= 1 - eclipseAmount * 0.6;

		float dust = ImpactWorldHandler.getDustForClient(worldObj);
		sunBrightness *= (1 - dust);


		// brightness _inside_ of the atmosphere, from effects like lightning or war weapons
		float insideBrightness = 0;

		CBT_War wardat = body.getTrait(CBT_War.class);

		if(wardat != null) {
			for(int i = 0; i < wardat.getProjectiles().size(); i++) {
				CBT_War.Projectile projectile = wardat.getProjectiles().get(i);
				float flash = projectile.getFlashtime();
				if(projectile.getAnimtime() > 0) {
					insideBrightness += 100 - flash;
				}
			}
		}


		if(atmosphere == null) {
			return sunBrightness + insideBrightness;
		}

		return sunBrightness * MathHelper.clamp_float(1.0F - ((float) atmosphere.getPressure() - 1.5F) * 0.2F, 0.25F, 1.0F) + insideBrightness;
	}

	public float[] getSunColor() {
		CBT_Atmosphere atmosphere = CelestialBody.getTrait(worldObj, CBT_Atmosphere.class);

		if(atmosphere == null) return new float[] { 1.0F, 1.0F, 1.0F };

		float[] sunColor = { 1.0F, 1.0F, 1.0F };

		// Adjust the sun colour based on atmospheric composition
		for(FluidEntry entry : atmosphere.fluids) {
			// Chlorines all redden the sun by absorbing blue and green
			if(entry.fluid == Fluids.TEKTOAIR
			|| entry.fluid == Fluids.CHLORINE
			|| entry.fluid == Fluids.CHLOROMETHANE
			|| entry.fluid == Fluids.RADIOSOLVENT
			|| entry.fluid == Fluids.CCL) {
				float absorption = MathHelper.clamp_float(1.0F - (float)entry.pressure * 0.5F, 0.0F, 1.0F);
				sunColor[1] *= absorption;
				sunColor[2] *= absorption;
			}
		}

		return sunColor;
	}

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

	// We want spawning to check for breathable, and getRespawnDimension() only runs if this is FALSE
	// BUT this also makes beds blow up (Mojang I swear), so we hook into the sleep event and set a flag
	public static boolean attemptingSleep = false;

	@Override
	public boolean canRespawnHere() {
		if(attemptingSleep) {
			attemptingSleep = false;
			return true;
		}

		return false;
	}

	// Another AWFULLY named deobfuscation function, this one is called when players have all slept,
	// which means we can set the time of day to local morning safely here!
	@Override
	public void resetRainAndThunder() {
		CBT_Weather weather = CBT_Weather.ensureTrait(body);
		if(weather != null) {
			weather.forceClear(worldObj.rand, worldObj.rand.nextInt(168000) + 12000);
			SolarSystemWorldSavedData.get(worldObj).markDirty();
		}

		worldObj.prevRainingStrength = 0.0F;
		worldObj.rainingStrength = 0.0F;
		worldObj.prevThunderingStrength = 0.0F;
		worldObj.thunderingStrength = 0.0F;

		if(dimensionId == 0) return;
		if(!worldObj.getGameRules().getGameRuleBooleanValue("doDaylightCycle")) return;

		long dayLength = (long)getDayLength();
		long i = getWorldTime() % dayLength;
		setWorldTime(i - i % dayLength);
	}

	@Override
	public long getWorldTime() {
		if(dimensionId == 0) {
			return super.getWorldTime();
		}

		if(!worldObj.isRemote) {
			localTime = CelestialBodyWorldSavedData.get(this).getLocalTime();
		}

		return localTime;
	}

	@Override
	public void setWorldTime(long time) {
		if(dimensionId == 0) {
			super.setWorldTime(time);
			return;
		}

		if(!worldObj.isRemote) {
			CelestialBodyWorldSavedData.get(this).setLocalTime(time);
		}

		localTime = time;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public float getCloudHeight() {
		CBT_Atmosphere atmosphere = CelestialBody.getTrait(worldObj, CBT_Atmosphere.class);

		if(getCloudLayerCount(atmosphere) <= 0) return -99999;

		return super.getCloudHeight();
	}

	@SideOnly(Side.CLIENT)
	public int getCloudLayerCount() {
		return getCloudLayerCount(CelestialBody.getTrait(worldObj, CBT_Atmosphere.class));
	}

	private IRenderHandler cloudProvider;

	@Override
	@SideOnly(Side.CLIENT)
	public IRenderHandler getCloudRenderer() {
		if(cloudProvider == null) cloudProvider = new CloudProviderCelestial();
		return cloudProvider;
	}

	private IRenderHandler skyProvider;

	@Override
	@SideOnly(Side.CLIENT)
	public IRenderHandler getSkyRenderer() {
		// I do not condone this because it WILL confuse your players, but if you absolutely must,
		// you can uncomment this line below in your fork to get default skybox rendering on Earth.

		// if(dimensionId == 0) return super.getSkyRenderer();

		// Make sure you also uncomment the relevant line in getMoonPhase below too.

		// This is not in a config because it is not a decision you should make lightly, as it will break:
		//  * certain atmosphere/terraforming modifications
		//  * Dyson swarm rendering
		//  * seeing weapons platforms in orbit (the big cannon from the trailer will NOT be visible)
		//  * weapon effects on the atmosphere (burning holes in the atmosphere, hitting planetary defense shields)
		//  * accurate celestial body rendering (you won't be able to see ANY other planets)
		//     * this also breaks future plans to modify orbits via huge mass drivers, if someone decides to yeet the moon at you, you won't know
		//  * sun extinction/modification events (the sun will appear normal even if it has been turned into a black hole)
		//  * player launched satellites won't be visible
		//  * artificial moons/rings (once implemented) won't be visible

		if(skyProvider == null) skyProvider = new SkyProviderCelestial();
		return skyProvider;
	}

	protected double getDayLength() {
		return body.getRotationalPeriod() / (1 - (1 / body.getPlanet().getOrbitalPeriod()));
	}

	// This calculates SOLAR angle, not sidereal/celestial!
	@Override
	public float calculateCelestialAngle(long worldTime, float partialTicks) {
		worldTime = getWorldTime(); // the worldtime passed in is from the fucking overworld
		double dayLength = getDayLength();
		double j = worldTime % dayLength;
		double f1 = (j + partialTicks) / dayLength - 0.25F;

		if(f1 < 0.0F) {
			++f1;
		}

		if(f1 > 1.0F) {
			--f1;
		}

		double f2 = f1;
		f1 = 0.5F - Math.cos(f1 * Math.PI) / 2.0F;
		return (float)(f2 + (f1 - f2) / 3.0D);
	}

	@Override
	public int getMoonPhase(long worldTime) {
		// Uncomment this line as well to return moon phase difficulty calcs to vanilla
		// if(dimensionId == 0) return super.getMoonPhase(worldTime);

		// if no moons, default to half-moon difficulty
		if(body.satellites.size() == 0) return 2;

		// Determine difficulty phase from closest moon
		int phase = Math.round(8 - ((float)SolarSystem.calculateSingleAngle(worldObj, body, body.satellites.get(0)) / 45 + 4));
		if(phase >= 8) return 0;
		return phase;
	}

	// Because I'm a big dumb idiot, the providers don't inherit a single base (and, well, can't, because `WorldProviderSurface` vs `WorldProvider` :c)
	// I have to make this thing to help you get sun power without having to do a shit ton of silly casting. yayyy!
	public static float getSunPower(WorldProvider provider, int x, int z) {
		if(provider instanceof WorldProviderCelestial) return ((WorldProviderCelestial) provider).getSunPower();
		if(provider instanceof WorldProviderOrbit) return ((WorldProviderOrbit) provider).getSunPower(x, z);

		return CelestialBody.getBody(provider.worldObj).getSunPower();
	}

	// Specifically for the server, and caches per tick
	public double getEclipseAmount() {
		if(eclipseAmount > -1) return eclipseAmount;

		// First fetch the suns true size
		double sunSize = SolarSystem.calculateSunSize(body);
		float solarAngle = worldObj.getCelestialAngle(0);

		// Get our orrery of bodies
		metrics = SolarSystem.calculateMetricsFromBody(worldObj, 0, body, solarAngle);

		eclipseAmount = getEclipseFactor(metrics, sunSize, SolarSystem.MAX_APPARENT_SIZE_SURFACE);

		// Get our eclipse amount
		return eclipseAmount;
	}

	public boolean isEclipse() {
		return getEclipseAmount() > 0.0;
	}

	public float getSunPower() {
		return body.getSunPower() * (1 - (float)getEclipseAmount());
	}

	@Override
	public double getHorizon() {
		if(dimensionId == 0) return super.getHorizon();
		return 63;
	}

	// This is the vanilla junk table, for replacing fish on dead worlds
	private static ArrayList<WeightedRandomFishable> junk;

	// you know what that means
	/// FISH ///

	// returning null from any of these methods will revert to overworld loot tables
	public ArrayList<WeightedRandomFishable> getFish() {
		if(junk == null) {
			junk = new ArrayList<>();
			// junk.add((new WeightedRandomFishable(new ItemStack(Items.leather_boots), 10)).func_150709_a(0.9F));
			// junk.add(new WeightedRandomFishable(new ItemStack(Items.leather), 10));
			// junk.add(new WeightedRandomFishable(new ItemStack(Items.bone), 10));
			junk.add(new WeightedRandomFishable(new ItemStack(Items.potionitem), 10));
			junk.add(new WeightedRandomFishable(new ItemStack(Items.string), 5));
			junk.add((new WeightedRandomFishable(new ItemStack(Items.fishing_rod), 2)).func_150709_a(0.9F));
			junk.add(new WeightedRandomFishable(new ItemStack(Items.bowl), 10));
			junk.add(new WeightedRandomFishable(new ItemStack(Items.stick), 5));
			junk.add(new WeightedRandomFishable(new ItemStack(Items.dye, 10, 0), 1));
			junk.add(new WeightedRandomFishable(new ItemStack(Blocks.tripwire_hook), 10));
			// junk.add(new WeightedRandomFishable(new ItemStack(Items.rotten_flesh), 10));
		}

		return junk;
	}

	public ArrayList<WeightedRandomFishable> getJunk() {
		return null;
	}

	public ArrayList<WeightedRandomFishable> getTreasure() {
		return null;
	}
	/// FISH ///

	public static class Meteor {

		public double posX;
		public double posY;
		public double posZ;
		public double prevPosX;
		public double prevPosY;
		public double prevPosZ;
		public double motionX;
		public double motionY;
		public double motionZ;
		public boolean isDead = false;
		public long age;
		public MeteorType type;

		public Meteor(double posX, double posY, double posZ) {
			this(posX, posY, posZ, MeteorType.STANDARD, -31.2, -20.8, 20);
		}

		public Meteor(double posX, double posY, double posZ, MeteorType type, double motionX, double motionY, double motionZ) {
			this.posX = posX;
			this.posY = posY;
			this.posZ = posZ;
			this.type = type;
			this.motionX = motionX;
			this.motionY = motionY;
			this.motionZ = motionZ;
		}

		public static void addMeteor() {
			EntityPlayer player = MainRegistry.proxy.me();
			if(player == null) return;

			meteors.add(new Meteor(player.posX + player.worldObj.rand.nextInt(16000) - 8000, 2017, player.posZ + player.worldObj.rand.nextInt(16000) - 8000));
		}

		private Meteor update(Random rand) {
			if(this.posY <= 500 && this.type != MeteorType.SMOKE) {
				this.isDead = true;
			}

			if(this.type == MeteorType.SMOKE) {
				this.age++;
				if(this.age >= 60)
					this.isDead = true;
			}

			this.prevPosX = this.posX;
			this.prevPosY = this.posY;
			this.prevPosZ = this.posZ;
			this.posX += this.motionX;
			this.posY += this.motionY;
			this.posZ += this.motionZ;

			if(this.type != MeteorType.SMOKE && this.type != MeteorType.FRAGMENT) {
				return new Meteor((this.posX + rand.nextInt(16)) - 8, (this.posY + rand.nextInt(16)), (this.posZ + rand.nextInt(16)) - 8, MeteorType.SMOKE, 0, 0, 0);
			}

			return null;
		}
	}

	public static enum MeteorType {
		STANDARD,
		FRAGMENT,
		SMOKE
	}

}
