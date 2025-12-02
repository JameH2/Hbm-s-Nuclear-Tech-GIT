package com.hbm.dim.orbit;

import java.nio.DoubleBuffer;
import java.util.List;

import org.lwjgl.opengl.GL11;

import com.hbm.dim.CelestialBody;
import com.hbm.dim.SkyProviderCelestial;
import com.hbm.dim.SolarSystem;
import com.hbm.dim.SolarSystem.AstroMetric;
import com.hbm.dim.orbit.OrbitalStation.StationState;
import com.hbm.lib.Library;
import com.hbm.lib.RefStrings;
import com.hbm.main.ResourceManager;
import com.hbm.util.BobMathUtil;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Vec3;

public class SkyProviderOrbit extends SkyProviderCelestial {

	private static CelestialBody lastBody;

	public static final ResourceLocation starfield = new ResourceLocation(RefStrings.MODID, "textures/misc/space/starfield.png");
	public static final ResourceLocation ittyfield = new ResourceLocation(RefStrings.MODID, "textures/misc/space/ittyfield.png");

	private static DoubleBuffer buffer;

	public SkyProviderOrbit() {
		if(buffer == null) buffer = GLAllocation.createDirectByteBuffer(8 * 4).asDoubleBuffer(); // four doubles
	}

	@Override
	public void render(float partialTicks, WorldClient world, Minecraft mc) {
		GL11.glDepthMask(false);
		GL11.glDisable(GL11.GL_FOG);
		GL11.glDisable(GL11.GL_ALPHA_TEST);
		GL11.glEnable(GL11.GL_TEXTURE_2D);

		GL11.glEnable(GL11.GL_BLEND);
		RenderHelper.disableStandardItemLighting();

		OpenGlHelper.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO);

		OrbitalStation station = OrbitalStation.clientStation;


		if(station.state == StationState.FTL) {
			renderStarfield(partialTicks, world, mc);
		} else if(station.target != null && (station.orbiting.getStar() != station.target.getStar())) {
			double unscaled = station.getUnscaledProgress(partialTicks);
			double slicePosition = unscaled * 400 - 200;
			renderSliced(partialTicks, world, mc, 1, slicePosition, unscaled);
		} else if(lastBody != null && station.state == StationState.ARRIVING && lastBody.getStar() != station.orbiting.getStar()) {
			double unscaled = station.getUnscaledProgress(partialTicks);
			double slicePosition = unscaled * 400 - 200;
			renderSliced(partialTicks, world, mc, -1, -slicePosition, 1 - unscaled);
		} else {
			renderOrbit(partialTicks, world, mc);
		}


		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		GL11.glDisable(GL11.GL_BLEND);
		GL11.glEnable(GL11.GL_ALPHA_TEST);
		GL11.glEnable(GL11.GL_FOG);

		GL11.glEnable(GL11.GL_TEXTURE_2D);
		GL11.glDepthMask(true);
	}

	private void renderSliced(float partialTicks, WorldClient world, Minecraft mc, double normal, double slicePosition, double stretch) {
		GL11.glPushMatrix();
		{

			GL11.glScaled(stretch * 4 + 1, 1, 1);
			renderOrbit(partialTicks, world, mc);

		}
		GL11.glPopMatrix();

		buffer.put(new double[] { normal, 0, 0, slicePosition });
		buffer.rewind();

		GL11.glEnable(GL11.GL_CLIP_PLANE0);
		GL11.glClipPlane(GL11.GL_CLIP_PLANE0, buffer);

		renderStarfield(partialTicks, world, mc);

		GL11.glDisable(GL11.GL_CLIP_PLANE0);
	}

	private void renderOrbit(float partialTicks, WorldClient world, Minecraft mc) {
		WorldProviderOrbit provider = (WorldProviderOrbit) world.provider;
		OrbitalStation station = OrbitalStation.clientStation;
		double progress = station.getTransferProgress(partialTicks);
		float orbitalTilt = 80;


		float solarAngle = getCelestialAngle(world, provider.metrics, partialTicks, station);
		float siderealAngle = (float)SolarSystem.calculateSiderealAngle(world, partialTicks, station.orbiting);
		float celestialPhase = (1 - (solarAngle + 0.5F) % 1) * 2 - 1;

		CelestialBody star = station.orbiting.getStar();
		float starBrightness = world.getStarBrightness(partialTicks);

		renderStars(partialTicks, world, mc, starBrightness, solarAngle + siderealAngle, orbitalTilt);

		GL11.glPushMatrix();
		{

			GL11.glRotatef(orbitalTilt, 1.0F, 0.0F, 0.0F);
			GL11.glRotatef(-90.0F, 0.0F, 1.0F, 0.0F);
			GL11.glRotatef(solarAngle * 360.0F, 1.0F, 0.0F, 0.0F);

			// digma balls
			if(star != SolarSystem.demeter) {
				renderDigamma(partialTicks, world, mc, solarAngle);
			}

			OpenGlHelper.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ONE, GL11.GL_ZERO);

			double sunSize = SolarSystem.calculateSunSize(station.orbiting) * SolarSystem.SUN_RENDER_SCALE;
			if(station.state != StationState.ORBIT) {
				double sunTargetSize = SolarSystem.calculateSunSize(station.target) * SolarSystem.SUN_RENDER_SCALE;
				sunSize = BobMathUtil.lerp(progress, sunSize, sunTargetSize);
			}
			double coronaSize = sunSize * (3 - Library.smoothstep(Math.abs(celestialPhase), 0.7, 0.8));

			renderSun(partialTicks, world, mc, star, sunSize, coronaSize, 1, 0);

			CelestialBody orbiting = station.orbiting;
			if(station.state != StationState.ORBIT && progress > 0.5) orbiting = station.target;

			renderCelestials(partialTicks, world, mc, provider.metrics, solarAngle, null, Vec3.createVectorHelper(0, 0, 0), 1, 1, orbiting, SolarSystem.MAX_APPARENT_SIZE_ORBIT);

		}
		GL11.glPopMatrix();
	}

	@Override
	protected ResourceLocation getNightTexture() {
		OrbitalStation station = OrbitalStation.clientStation;
		CelestialBody orbiting = station.orbiting;

		if(station.state != StationState.ORBIT && station.getTransferProgress(0) > 0.5) orbiting = station.target;

		if(orbiting.getStar() == SolarSystem.demeter) return nightTextureDemeter;
		return nightTextureKerbol;
	}

	private void renderStarfield(float partialTicks, WorldClient world, Minecraft mc) {
		GL11.glPushMatrix();
		{


			GL11.glEnable(GL11.GL_FOG);
			GL11.glPushAttrib(GL11.GL_FOG_BIT);
			{

				GL11.glFogf(GL11.GL_FOG_START, 50.0F);
				GL11.glFogf(GL11.GL_FOG_END, 200.0F);

				GL11.glColor3f(0, 0, 0);
				GL11.glDisable(GL11.GL_TEXTURE_2D);
				GL11.glDisable(GL11.GL_BLEND);

				ResourceManager.bubble.renderAll();

				GL11.glEnable(GL11.GL_TEXTURE_2D);
				GL11.glEnable(GL11.GL_BLEND);

				OpenGlHelper.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_CONSTANT_ALPHA, GL11.GL_ONE, GL11.GL_ZERO);

				GL11.glMatrixMode(GL11.GL_TEXTURE);

				GL11.glPushMatrix();
				{

					GL11.glTranslated(((double)System.currentTimeMillis() * 0.0001) % 1, ((double)System.currentTimeMillis() * 0.0014) % 1, 0);
					GL11.glScalef(8.0F, 4.0F, 1.0F);

					mc.renderEngine.bindTexture(starfield);

					GL11.glColor3f(1, 0, 0);
					ResourceManager.bubble.renderAll();

					GL11.glTranslated(Math.sin(System.currentTimeMillis() * 0.001) * 0.004, 0.005, 0);

					GL11.glColor3f(0, 1, 1);
					ResourceManager.bubble.renderAll();
				}
				GL11.glPopMatrix();

				GL11.glMatrixMode(GL11.GL_MODELVIEW);

				GL11.glScaled(1.25, 0.75, 1.5);

				GL11.glMatrixMode(GL11.GL_TEXTURE);

				GL11.glPushMatrix();
				{

					mc.renderEngine.bindTexture(ittyfield);

					// GL11.glTranslated(-((double)System.currentTimeMillis() * 0.0004) % 1, ((double)System.currentTimeMillis() * 0.0004) % 1, 0);
					GL11.glTranslated(Math.sin(System.currentTimeMillis() * 0.0001) * 3, ((double)System.currentTimeMillis() * 0.0004) % 1, 0);
					GL11.glScalef(8.0F, 4.0F, 1.0F);

					GL11.glColor3f(0, 1, 0);
					ResourceManager.bubble.renderAll();

					GL11.glTranslated(Math.sin(System.currentTimeMillis() * 0.001) * 0.002, 0.001, 0);

					GL11.glColor3f(1, 0, 1);
					ResourceManager.bubble.renderAll();

				}
				GL11.glPopMatrix();

				GL11.glMatrixMode(GL11.GL_MODELVIEW);

			}
			GL11.glPopAttrib();
			GL11.glDisable(GL11.GL_FOG);

		}
		GL11.glPopMatrix();
	}

	// All angles within are normalized to -180/180
	private float getCelestialAngle(WorldClient world, List<AstroMetric> metrics, float partialTicks, OrbitalStation station) {
		float solarAngle = world.getCelestialAngle(partialTicks);
		if(station.state == StationState.ORBIT) return solarAngle;

		if(station.state != StationState.ARRIVING) lastBody = station.orbiting;

		if(station.target != null && (station.orbiting.getStar() != station.target.getStar())) return solarAngle;
		if(lastBody != null && station.state == StationState.ARRIVING && lastBody.getStar() != station.orbiting.getStar()) return solarAngle;

		solarAngle = solarAngle * 360.0F - 180.0F;

		double progress = station.getUnscaledProgress(partialTicks);
		float travelAngle = -(float)SolarSystem.calculateSingleAngle(metrics, lastBody, station.target);
		travelAngle = MathHelper.wrapAngleTo180_float(travelAngle + 90.0F);

		if(station.state == StationState.TRANSFER) {
			return (travelAngle + 180.0F) / 360.0F;
		} else if(station.state == StationState.LEAVING) {
			return ((float)BobMathUtil.clerp(progress, solarAngle, travelAngle) + 180.0F) / 360.0F;
		} else {
			return ((float)BobMathUtil.clerp(progress, travelAngle, solarAngle) + 180.0F) / 360.0F;
		}
	}

}
