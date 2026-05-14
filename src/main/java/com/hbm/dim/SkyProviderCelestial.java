package com.hbm.dim;

import java.util.List;
import java.util.Map;
import java.util.Random;

import org.lwjgl.opengl.GL11;

import com.hbm.dim.SolarSystem.AstroMetric;
import com.hbm.dim.orbit.OrbitalStation;
import com.hbm.dim.trait.CBT_Atmosphere;
import com.hbm.dim.trait.CBT_DEMETER;
import com.hbm.dim.trait.CBT_Dyson;
import com.hbm.dim.trait.CelestialBodyTrait.CBT_COMPROMISED;
import com.hbm.dim.trait.CBT_War;
import com.hbm.dim.trait.CBT_Destroyed;
import com.hbm.extprop.HbmLivingProps;
import com.hbm.lib.RefStrings;
import com.hbm.main.ResourceManager;
import com.hbm.render.shader.Shader;
import com.hbm.render.util.AtmosphereRenderUtil;
import com.hbm.saveddata.SatelliteSavedData;
import com.hbm.saveddata.satellites.Satellite;
import com.hbm.util.BobMathUtil;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Vec3;
import net.minecraftforge.client.IRenderHandler;

import org.lwjgl.opengl.ContextCapabilities;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GLContext;

import com.hbm.dim.trait.CBT_Impact;
import com.hbm.dim.trait.CBT_Lights;
import com.hbm.handler.CelestialNukeShockHandler;
import com.hbm.items.ISatChip;
import com.hbm.main.ModEventHandlerClient;
import com.hbm.main.ModEventHandlerRenderer;

import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.relauncher.ReflectionHelper;
import net.minecraft.item.ItemStack;

public class SkyProviderCelestial extends IRenderHandler {

	private static final ResourceLocation planetTexture = new ResourceLocation(RefStrings.MODID, "textures/misc/space/planet.png");
	private static final ResourceLocation flareTexture = new ResourceLocation(RefStrings.MODID, "textures/misc/space/sunspike.png");
	private static final ResourceLocation digammaStar = new ResourceLocation(RefStrings.MODID, "textures/misc/space/star_digamma.png");
	private static final ResourceLocation lodeStar = new ResourceLocation(RefStrings.MODID, "textures/misc/star_lode.png");
	private static final ResourceLocation stationTexture = new ResourceLocation(RefStrings.MODID, "textures/misc/space/station.png");

	public static final ResourceLocation nightTextureKerbol = new ResourceLocation(RefStrings.MODID, "textures/misc/space/night.png");
	public static final ResourceLocation nightTextureDemeter = new ResourceLocation(RefStrings.MODID, "textures/misc/space/night_2.png");

	private static final ResourceLocation impactTexture = new ResourceLocation(RefStrings.MODID, "textures/misc/space/impact.png");
	private static final ResourceLocation shockwaveTexture = new ResourceLocation(RefStrings.MODID, "textures/particle/shockwave.png");
	protected static final ResourceLocation shockFlareTexture = new ResourceLocation(RefStrings.MODID, "textures/particle/flare.png");

	private static final ResourceLocation ringTexture = new ResourceLocation(RefStrings.MODID, "textures/misc/space/rings.png");
	private static final ResourceLocation destroyedBody = new ResourceLocation(RefStrings.MODID, "textures/misc/space/destroyed.png");

	private static final ResourceLocation thatmoShield = new ResourceLocation(RefStrings.MODID, "textures/particle/cens.png");

	private static final Shader fleshShader = new Shader(new ResourceLocation(RefStrings.MODID, "shaders/fle.frag"));
	private static final Shader angel = new Shader(new ResourceLocation(RefStrings.MODID, "shaders/angel.frag"));
	private static final ResourceLocation eye = new ResourceLocation(RefStrings.MODID, "textures/misc/space/eyeball.png");

	private static final ResourceLocation noise = new ResourceLocation(RefStrings.MODID, "shaders/iChannel1.png");

	protected static final Shader crescentShader = new Shader(new ResourceLocation(RefStrings.MODID, "shaders/crescent.frag"));
	protected static final Shader atmosphereShader = new Shader(new ResourceLocation(RefStrings.MODID, "shaders/atmosphere.frag"));
	protected static final Shader atmosphereEmissiveShader = new Shader(new ResourceLocation(RefStrings.MODID, "shaders/atmosphere_emissive.frag"));
	protected static final Shader lightningShader = new Shader(new ResourceLocation(RefStrings.MODID, "shaders/lightning.frag"));
	protected static final Shader nukeShader = new Shader(new ResourceLocation(RefStrings.MODID, "shaders/nuke.frag"));
	protected static final Shader nightLightsShader = new Shader(new ResourceLocation(RefStrings.MODID, "shaders/nightlights.frag"));
	protected static final Shader swarmShader = new Shader(new ResourceLocation(RefStrings.MODID, "shaders/swarm.vert"), new ResourceLocation(RefStrings.MODID, "shaders/swarm.frag"));

	private static final ResourceLocation particleBase = new ResourceLocation(RefStrings.MODID + ":textures/particle/particle_base.png");


	private static final ResourceLocation[] citylights = new ResourceLocation[]{
		new ResourceLocation(RefStrings.MODID, "textures/misc/space/citylights_0.png"),
		new ResourceLocation(RefStrings.MODID, "textures/misc/space/citylights_1.png"),
		new ResourceLocation(RefStrings.MODID, "textures/misc/space/citylights_2.png"),
		new ResourceLocation(RefStrings.MODID, "textures/misc/space/citylights_3.png"),
	};

	private static final ResourceLocation defaultMask = new ResourceLocation(RefStrings.MODID, "textures/misc/space/default_mask.png");

	private static final String[] GL_SKY_LIST = new String[]{"glSkyList", "field_72771_w", "G"};
	private static final String[] GL_SKY_LIST2 = new String[]{"glSkyList2", "field_72781_x", "H"};

	public static boolean displayListsInitialized = false;
	public static int glSkyList;
	public static int glSkyList2;

	private static boolean gl13;

	private static float currentFov = 70;

	public SkyProviderCelestial() {
		if(!displayListsInitialized) {
			initializeDisplayLists();
		}
	}

	private void initializeDisplayLists() {
		ContextCapabilities contextcapabilities = GLContext.getCapabilities();

		Minecraft mc = Minecraft.getMinecraft();
		glSkyList = ReflectionHelper.getPrivateValue(RenderGlobal.class, mc.renderGlobal, GL_SKY_LIST);
		glSkyList2 = ReflectionHelper.getPrivateValue(RenderGlobal.class, mc.renderGlobal, GL_SKY_LIST2);

		gl13 = contextcapabilities.OpenGL13;

		displayListsInitialized = true;
	}

	private static int lastBrightestPixel = 0;

	@Override
	public void render(float partialTicks, WorldClient world, Minecraft mc) {
		// We can now guarantee that this only runs with celestial, but it doesn't hurt to be safe
		if(!(world.provider instanceof WorldProviderCelestial)) return;

		WorldProviderCelestial celestialProvider = (WorldProviderCelestial) world.provider;

		// Without mixins, we have to resort to some very wacky ways of checking that the lightmap needs to be updated
		// fortunately, thanks to torch flickering, we can just check to see if the brightest pixel has been modified
		int[] lmc = mc.entityRenderer.lightmapColors;
		if(lastBrightestPixel != lmc[255] + lmc[250] + lmc[128]) {
			if(celestialProvider.updateLightmap(lmc)) {
				mc.entityRenderer.lightmapTexture.updateDynamicTexture();
			}

			lastBrightestPixel = lmc[255] + lmc[250] + lmc[128];
		}

		float fogIntensity = ModEventHandlerRenderer.lastFogDensity * 30;
		currentFov = mc.entityRenderer.getFOVModifier(partialTicks, true);

		CelestialBody body = CelestialBody.getBody(world);
		CelestialBody sun = body.getStar();
		CBT_Atmosphere atmosphere = body.getTrait(CBT_Atmosphere.class);

		boolean hasAtmosphere = atmosphere != null;

		float pressure = hasAtmosphere ? (float) atmosphere.getPressure() : 0.0F;
		float visibility = hasAtmosphere ? MathHelper.clamp_float(2.0F - pressure, 0.1F, 1.0F) : 1.0F;

		GL11.glDisable(GL11.GL_TEXTURE_2D);
		Vec3 skyColor = world.getSkyColor(mc.renderViewEntity, partialTicks);

		float skyR = (float) skyColor.xCoord;
		float skyG = (float) skyColor.yCoord;
		float skyB = (float) skyColor.zCoord;

		// Diminish sky colour when leaving the atmosphere
		if(mc.renderViewEntity.posY > 300) {
			double curvature = MathHelper.clamp_float((800.0F - (float) mc.renderViewEntity.posY) / 500.0F, 0.0F, 1.0F);
			skyR *= curvature;
			skyG *= curvature;
			skyB *= curvature;
		}

		if(mc.gameSettings.anaglyph) {
			float[] anaglyphColor = applyAnaglyph(skyR, skyG, skyB);
			skyR = anaglyphColor[0];
			skyG = anaglyphColor[1];
			skyB = anaglyphColor[2];
		}

		float planetR = skyR;
		float planetG = skyG;
		float planetB = skyB;

		if(fogIntensity > 0.01F) {
			Vec3 fogColor = world.getFogColor(partialTicks);
			planetR = (float) BobMathUtil.clampedLerp(skyR, fogColor.xCoord, fogIntensity);
			planetG = (float) BobMathUtil.clampedLerp(skyG, fogColor.yCoord, fogIntensity);
			planetB = (float) BobMathUtil.clampedLerp(skyB, fogColor.zCoord, fogIntensity);
		}

		Vec3 planetTint = Vec3.createVectorHelper(planetR, planetG, planetB);

		Tessellator tessellator = Tessellator.instance;

		GL11.glDepthMask(false);
		GL11.glEnable(GL11.GL_FOG);
		GL11.glColor3f(skyR, skyG, skyB);

		// Set maximum sky fog distance to 12 chunks, works nicely with Celeritas/Distant Horizons
		// and makes for a more consistent sky in vanilla too
		GL11.glPushAttrib(GL11.GL_FOG_BIT);
		{

			GL11.glFogf(GL11.GL_FOG_START, 0.0F);
			GL11.glFogf(GL11.GL_FOG_END, Math.min(12.0F, mc.gameSettings.renderDistanceChunks) * 16.0F);

			GL11.glCallList(glSkyList);

		}
		GL11.glPopAttrib();

		GL11.glDisable(GL11.GL_FOG);
		GL11.glDisable(GL11.GL_ALPHA_TEST);
		GL11.glEnable(GL11.GL_TEXTURE_2D);

		GL11.glEnable(GL11.GL_BLEND);
		RenderHelper.disableStandardItemLighting();

		OpenGlHelper.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO);

		float starBrightness = world.getStarBrightness(partialTicks) * visibility;
		float solarAngle = world.getCelestialAngle(partialTicks);
		float siderealAngle = (float) SolarSystem.calculateSiderealAngle(world, partialTicks, body);

		// Handle any special per-body sunset rendering
		renderSunset(partialTicks, world, mc, solarAngle, pressure, body.surfaceTexture);

		renderStars(partialTicks, world, mc, starBrightness, solarAngle + siderealAngle, body.axialTilt);


		GL11.glPushMatrix();
		{

			GL11.glRotatef(body.axialTilt, 1.0F, 0.0F, 0.0F);
			GL11.glRotatef(-90.0F, 0.0F, 1.0F, 0.0F);
			GL11.glRotatef(solarAngle * 360.0F, 1.0F, 0.0F, 0.0F);

			OpenGlHelper.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ONE, GL11.GL_ZERO);

			// Scale sun size for rendering (texture is 4 times larger than actual, for glow)
			double sunSize = SolarSystem.calculateSunSize(body) * SolarSystem.SUN_RENDER_SCALE;
			double coronaSize = sunSize * (3 - MathHelper.clamp_float(pressure, 0.0F, 1.0F));

			renderSun(partialTicks, world, mc, sun, sunSize, coronaSize, visibility, pressure);

			float blendAmount = hasAtmosphere ? MathHelper.clamp_float(1 - world.getSunBrightnessFactor(partialTicks), 0.25F, 1F) : 1F;

			renderCelestials(partialTicks, world, mc, celestialProvider.metrics, solarAngle, null, planetTint, visibility, blendAmount, null, SolarSystem.MAX_APPARENT_SIZE_SURFACE);

			GL11.glEnable(GL11.GL_BLEND);
			GL11.glEnable(GL11.GL_TEXTURE_2D);

			if(visibility > 0.2F) {
				// JEFF BOZOS WOULD LIKE TO KNOW YOUR LOCATION
				// ... to send you a pakedge :)))
				if(world.provider.dimensionId == 0) {
					Satellite.renderDefault(partialTicks, world, mc, solarAngle, 1916169, 1.0F, 0.534F, 0.385F, Satellite.DEFAULT_INCLINATION, Satellite.DEFAULT_ALTITUDE_KM, Satellite.DEFAULT_IS_BLINKING, Satellite.DEFAULT_BLINK_PERIOD);
				}

				// Light up the sky
				for(Map.Entry<Integer, Satellite> satelliteEntry : SatelliteSavedData.getClientSats().entrySet()) {
					satelliteEntry.getValue().render(partialTicks, world, mc, solarAngle, satelliteEntry.getKey());
				}

				renderHeldSatellitePreview(partialTicks, world, mc, solarAngle);

				// Stations, too
				for(OrbitalStation station : OrbitalStation.orbitingStations) {
					renderStation(partialTicks, world, mc, station, solarAngle);
				}
			}

		}
		GL11.glPopMatrix();

		render3DModel(partialTicks, world, mc);

		// TODO: fix EVERYTHING
		// k lmao

		CBT_War war = body.getTrait(CBT_War.class);
		if(war != null) {
			for(CBT_War.Projectile projectile : war.projectiles) {
				float thing = projectile.getFlashtime() + partialTicks;

				if(projectile.getTravel() <= 0) {
					float alpd = 1.0F - Math.min(1.0F, thing / 100);

					GL11.glPushMatrix();
					{

						render3DModel(partialTicks, world, mc);

						GL11.glTranslated(projectile.getTranslateX() + 70, projectile.getTranslateY(), projectile.getTranslateZ() + 50);
						GL11.glScaled(thing, thing, thing);
						GL11.glRotated(90.0, -10.0, -1.0, 50.0);
						GL11.glRotated(20.0, -0.0, -1.0, 1.0);

						GL11.glColor4d(1, 1, 1, alpd);

						mc.renderEngine.bindTexture(shockwaveTexture);
						ResourceManager.plane.renderAll();

					}
					GL11.glPopMatrix();

					GL11.glPushMatrix();
					{

						GL11.glTranslated(projectile.getTranslateX() + 70, projectile.getTranslateY(), projectile.getTranslateZ() + 50);
						GL11.glScaled(thing * 0.4f, thing * 0.4f, thing * 0.4f);
						GL11.glRotated(90.0, -10.0, -1.0, 50.0);
						GL11.glRotated(20.0, -0.0, -1.0, 1.0);
						GL11.glColor4d(1, 1, 1, alpd);

						mc.renderEngine.bindTexture(thatmoShield);
						ResourceManager.plane.renderAll();

					}
					GL11.glPopMatrix();
				}
			}
		}

		Vec3 pos = mc.thePlayer.getPosition(partialTicks);

		float rainStrength = world.getRainStrength(partialTicks);

		for(WorldProviderCelestial.Meteor meteor : WorldProviderCelestial.meteors) {
			GL11.glPushMatrix();

			// optimised 3 sqrt per meteor to just 1
			Vec3 offset = Vec3.createVectorHelper(meteor.posX - pos.xCoord, meteor.posY - pos.yCoord, meteor.posZ - pos.zCoord);
			double offsetLength = offset.lengthVector();
			double distance = Math.min(mc.gameSettings.renderDistanceChunks * 16, offsetLength);
			Vec3 offsetNormal = offsetLength >= 1.0E-4D ? Vec3.createVectorHelper(offset.xCoord / offsetLength, offset.yCoord / offsetLength, offset.zCoord / offsetLength) : offset;
			Vec3 renderOffset = Vec3.createVectorHelper(offsetNormal.xCoord * distance, offsetNormal.yCoord * distance, offsetNormal.zCoord * distance);

			GL11.glTranslated(renderOffset.xCoord, renderOffset.yCoord, renderOffset.zCoord);

			double descent = 2017d - meteor.posY;
			double quadratic = (-(descent * descent) + (1517 * descent)) / 41;

			float scalar = (float) (quadratic / offsetLength);
			GL11.glScaled(scalar, scalar, scalar);

			if(meteor.type == WorldProviderCelestial.MeteorType.SMOKE) {
				GL11.glColor4d(1, 0, 0, 1);
				mc.renderEngine.bindTexture(particleBase);
				renderSmoke(meteor.age);
			} else {
				GL11.glColor4d(1, 1, 1, 1);
				mc.renderEngine.bindTexture(shockFlareTexture);
				renderGlow(1, 1, 1, rainStrength);
			}

			GL11.glPopMatrix();
		}

		if(body.hasRings) {
			GL11.glPushMatrix();
			{

				GL11.glRotatef(body.axialTilt - body.ringTilt, 1.0F, 0.0F, 0.0F);
				GL11.glTranslatef(0, -100, 0);
				GL11.glRotatef(-90.0F, 0.0F, 1.0F, 0.0F);

				renderRings(partialTicks, world, mc, body.ringTilt, body.ringColor, 200, visibility);

			}
			GL11.glPopMatrix();
		}

		renderSpecialEffects(partialTicks, world, mc);

		CBT_COMPROMISED compromised = body.getTrait(CBT_COMPROMISED.class);
		if(compromised != null) {
			GL11.glPushMatrix();
			{

				float time = ((float) world.getWorldTime() + partialTicks) * 0.2F;

				GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
				GL11.glDisable(GL11.GL_CULL_FACE);

				fleshShader.use();
				GL11.glScaled(194.5, 66.5, 94.5);
				GL11.glRotated(90, 1, 0, 0);

				mc.renderEngine.bindTexture(noise);
				ResourceManager.sphere_v2.renderAll();

				// Fix orbital plane
				GL11.glRotatef(-90.0F, 0, 1, 0);

				fleshShader.setUniform1f("iTime", time * 0.05F);
				fleshShader.setUniform1i("iChannel1", 0);

				fleshShader.stop();

				OpenGlHelper.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ONE, GL11.GL_ZERO);

			}
			GL11.glPopMatrix();
		}

		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		GL11.glDisable(GL11.GL_BLEND);
		GL11.glEnable(GL11.GL_ALPHA_TEST);
		GL11.glEnable(GL11.GL_FOG);

		GL11.glDisable(GL11.GL_TEXTURE_2D);
		GL11.glColor3f(0.0F, 0.0F, 0.0F);

		double heightAboveHorizon = pos.yCoord - world.getHorizon();

		if(heightAboveHorizon < 0.0D) {
			GL11.glPushMatrix();
			{

				GL11.glTranslatef(0.0F, 12.0F, 0.0F);
				GL11.glCallList(glSkyList2);

			}
			GL11.glPopMatrix();

			float f8 = 1.0F;
			float f9 = -((float) (heightAboveHorizon + 65.0D));
			float opposite = -f8;

			tessellator.startDrawingQuads();
			tessellator.setColorRGBA_I(0, 255);
			tessellator.addVertex(-f8, f9, f8);
			tessellator.addVertex(f8, f9, f8);
			tessellator.addVertex(f8, opposite, f8);
			tessellator.addVertex(-f8, opposite, f8);
			tessellator.addVertex(-f8, opposite, -f8);
			tessellator.addVertex(f8, opposite, -f8);
			tessellator.addVertex(f8, f9, -f8);
			tessellator.addVertex(-f8, f9, -f8);
			tessellator.addVertex(f8, opposite, -f8);
			tessellator.addVertex(f8, opposite, f8);
			tessellator.addVertex(f8, f9, f8);
			tessellator.addVertex(f8, f9, -f8);
			tessellator.addVertex(-f8, f9, -f8);
			tessellator.addVertex(-f8, f9, f8);
			tessellator.addVertex(-f8, opposite, f8);
			tessellator.addVertex(-f8, opposite, -f8);
			tessellator.addVertex(-f8, opposite, -f8);
			tessellator.addVertex(-f8, opposite, f8);
			tessellator.addVertex(f8, opposite, f8);
			tessellator.addVertex(f8, opposite, -f8);
			tessellator.draw();
		}

		if(world.provider.isSkyColored()) {
			GL11.glColor3f(skyR * 0.2F + 0.04F, skyG * 0.2F + 0.04F, skyB * 0.6F + 0.1F);
		} else {
			GL11.glColor3f(skyR, skyG, skyB);
		}

		GL11.glPushMatrix();
		{

			GL11.glTranslatef(0.0F, -((float) (heightAboveHorizon - 16.0D)), 0.0F);
			GL11.glCallList(glSkyList2);

		}
		GL11.glPopMatrix();

		double sc = 1 / (pos.yCoord / 1000);
		double uvOffset = (pos.xCoord / 1024) % 1;
		GL11.glPushMatrix();
		{

			GL11.glEnable(GL11.GL_TEXTURE_2D);
			GL11.glDisable(GL11.GL_ALPHA_TEST);
			GL11.glDisable(GL11.GL_FOG);
			GL11.glEnable(GL11.GL_BLEND);

			GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

			float sunBrightness = world.getSunBrightness(partialTicks);

			GL11.glColor4f(sunBrightness, sunBrightness, sunBrightness, ((float) pos.yCoord - 200.0F) / 300.0F);
			mc.renderEngine.bindTexture(body.texture);
			GL11.glRotated(180, 1, 0, 0);

			tessellator.startDrawingQuads();
			tessellator.addVertexWithUV(-115 * sc, 100.0D, -115 * sc, 0.0D + uvOffset, 0.0D);
			tessellator.addVertexWithUV(115 * sc, 100.0D, -115 * sc, 1.0D + uvOffset, 0.0D);
			tessellator.addVertexWithUV(115 * sc, 100.0D, 115 * sc, 1.0D + uvOffset, 1.0D);
			tessellator.addVertexWithUV(-115 * sc, 100.0D, 115 * sc, 0.0D + uvOffset, 1.0D);
			tessellator.draw();

			GL11.glDisable(GL11.GL_TEXTURE_2D);
			GL11.glEnable(GL11.GL_ALPHA_TEST);
			GL11.glEnable(GL11.GL_FOG);
			GL11.glDisable(GL11.GL_BLEND);

			OpenGlHelper.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ONE, GL11.GL_ZERO);

		}

		GL11.glPopMatrix();
		GL11.glEnable(GL11.GL_TEXTURE_2D);
		GL11.glDepthMask(true);

	}

	protected void renderSunset(float partialTicks, WorldClient world, Minecraft mc, float solarAngle, float pressure, ResourceLocation surfaceTexture) {
		Tessellator tessellator = Tessellator.instance;

		float[] sunsetColor = calcSunriseSunsetColors(partialTicks, world, mc, solarAngle, pressure);

		if(sunsetColor != null) {
			float[] anaglyphColor = mc.gameSettings.anaglyph ? applyAnaglyph(sunsetColor) : sunsetColor;
			float sunsetDirection = MathHelper.sin(world.getCelestialAngleRadians(partialTicks)) < 0.0F ? 180.0F : 0.0F;

			GL11.glDisable(GL11.GL_TEXTURE_2D);
			GL11.glShadeModel(GL11.GL_SMOOTH);

			GL11.glPushMatrix();
			{

				GL11.glRotatef(90.0F, 1.0F, 0.0F, 0.0F);
				GL11.glRotatef(sunsetDirection, 0.0F, 0.0F, 1.0F);
				GL11.glRotatef(90.0F, 0.0F, 0.0F, 1.0F);

				tessellator.startDrawing(6);
				tessellator.setColorRGBA_F(anaglyphColor[0], anaglyphColor[1], anaglyphColor[2], sunsetColor[3]);
				tessellator.addVertex(0.0, 100.0, 0.0);
				tessellator.setColorRGBA_F(sunsetColor[0], sunsetColor[1], sunsetColor[2], 0.0F);
				byte segments = 16;

				for(int j = 0; j <= segments; ++j) {
					float angle = (float) j * 3.1415927F * 2.0F / (float) segments;
					float sinAngle = MathHelper.sin(angle);
					float cosAngle = MathHelper.cos(angle);
					tessellator.addVertex(sinAngle * 120.0F, cosAngle * 120.0F, -cosAngle * 40.0F * sunsetColor[3]);
				}

				tessellator.draw();

			}
			GL11.glPopMatrix();
			GL11.glShadeModel(GL11.GL_FLAT);
			GL11.glEnable(GL11.GL_TEXTURE_2D);

			// charged dust
			if(pressure < 0.05F) {
				Random rand = new Random(0);

				GL11.glPushMatrix();
				{

					double time = ((double) world.provider.getWorldTime() + partialTicks) * 0.002;

					GL11.glRotatef(90.0F, 1.0F, 0.0F, 0.0F);
					GL11.glRotatef(sunsetDirection, 0.0F, 0.0F, 1.0F);
					GL11.glRotatef(90.0F, 0.0F, 0.0F, 1.0F);

					mc.renderEngine.bindTexture(surfaceTexture);
					GL11.glColor4f(0.5F + rand.nextFloat() * 0.5F, 0.5F + rand.nextFloat() * 0.5F, 0.5F + rand.nextFloat() * 0.5F, rand.nextFloat() * sunsetColor[3] * 4.0F);

					OpenGlHelper.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_CONSTANT_ALPHA, GL11.GL_ONE, GL11.GL_ZERO);

					tessellator.startDrawing(GL11.GL_POINTS);
					for(int i = 0; i < 1024; i++) {
						tessellator.addVertex(rand.nextGaussian() * 50, 100, -((Math.abs(rand.nextGaussian() * 20) + time) % Math.abs(rand.nextGaussian()) * 20));
					}
					tessellator.draw();

				}
				GL11.glPopMatrix();
			}
		}
	}

	// We don't want certain sunrise/sunset effects to change the fog colour, so we do them here
	protected float[] calcSunriseSunsetColors(float partialTicks, WorldClient world, Minecraft mc, float solarAngle, float pressure) {
		if(pressure < 0.05F) {
			float cutoff = 0.4F;
			float angle = MathHelper.cos(solarAngle * (float) Math.PI * 2.0F) - 0.0F;

			if(angle < -cutoff || angle > cutoff) return null;

			float colorIntensity = angle / cutoff * 0.5F + 0.5F;
			float alpha = 1.0F - (1.0F - MathHelper.sin(colorIntensity * (float) Math.PI)) * 0.99F;
			alpha *= alpha;
			return new float[]{0.9F, 1.0F, 1.0F, alpha * 0.2F};
		}

		return world.provider.calcSunriseSunsetColors(world.getCelestialAngle(partialTicks), partialTicks);
	}

	protected ResourceLocation getNightTexture() {
		return nightTextureKerbol;
	}

	protected void renderStars(float partialTicks, WorldClient world, Minecraft mc, float starBrightness, float siderealAngle, float axialTilt) {
		Tessellator tessellator = Tessellator.instance;

		OpenGlHelper.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ONE, GL11.GL_ZERO);

		if(ModEventHandlerClient.renderLodeStar) {
			float lodeSize = 1F + world.rand.nextFloat() * 0.5F;
			double lodeDist = 100D;

			GL11.glPushMatrix();
			{

				GL11.glRotatef(-75.0F, 1.0F, 0.0F, 0.0F);
				GL11.glRotatef(10.0F, 0.0F, 1.0F, 0.0F);
				GL11.glColor4f(1F, 1F, 1F, 1.0F);
				mc.renderEngine.bindTexture(lodeStar); // genu-ine bona-fide ass whooping
	
				tessellator.startDrawingQuads();
				tessellator.addVertexWithUV(-lodeSize, lodeDist, -lodeSize, 0.0D, 0.0D);
				tessellator.addVertexWithUV(lodeSize, lodeDist, -lodeSize, 0.0D, 1.0D);
				tessellator.addVertexWithUV(lodeSize, lodeDist, lodeSize, 1.0D, 1.0D);
				tessellator.addVertexWithUV(-lodeSize, lodeDist, lodeSize, 1.0D, 0.0D);
				tessellator.draw();

			}
			GL11.glPopMatrix();
		}

		OpenGlHelper.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO);

		GL11.glPushMatrix();
		{

			GL11.glRotatef(axialTilt, 1.0F, 0.0F, 0.0F);

			GL11.glRotatef(-90.0F, 0.0F, 1.0F, 0.0F);

			GL11.glRotatef(siderealAngle * 360.0F, 1.0F, 0.0F, 0.0F);
			GL11.glRotatef(-90.0F, 1.0F, 0.0F, 0.0F);

			if(starBrightness > 0.0F) {
				GL11.glPushMatrix();
				{

					GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
	
					mc.renderEngine.bindTexture(nightTextureKerbol);
					float starBrightnessAlpha = starBrightness * 0.6f;
					GL11.glColor4f(1.0F, 1.0F, 1.0F, starBrightnessAlpha);
	
					GL11.glRotatef(90.0F, 1.0F, 0.0F, 0.0F);
					GL11.glRotatef(-90.0F, 0.0F, 0.0F, 1.0F);
					renderSkyboxSide(tessellator, 4);
	
					GL11.glPushMatrix();
					GL11.glRotatef(90.0F, 1.0F, 0.0F, 0.0F);
					renderSkyboxSide(tessellator, 1);
					GL11.glPopMatrix();
	
					GL11.glPushMatrix();
					GL11.glRotatef(-90.0F, 1.0F, 0.0F, 0.0F);
					renderSkyboxSide(tessellator, 0);
					GL11.glPopMatrix();
	
					GL11.glRotatef(90.0F, 0.0F, 0.0F, 1.0F);
					renderSkyboxSide(tessellator, 5);
	
					GL11.glRotatef(90.0F, 0.0F, 0.0F, 1.0F);
					renderSkyboxSide(tessellator, 2);
	
					GL11.glRotatef(90.0F, 0.0F, 0.0F, 1.0F);
					renderSkyboxSide(tessellator, 3);

				}
				GL11.glPopMatrix();
			}

			// demeter
			GL11.glPushMatrix();
			{

				OpenGlHelper.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ONE, GL11.GL_ZERO);
	
				float brightness = Math.max(0.5F, starBrightness * 1.6F);
				GL11.glColor4f(brightness, brightness, brightness, brightness);
	
				mc.renderEngine.bindTexture(digammaStar);
	
				float digamma = HbmLivingProps.getDigamma(mc.thePlayer);
				float digmaSize = (1 + digamma * 0.25F);
				float digmaDist = 100F - digamma * 2.5F;
	
				GL11.glRotatef(140.0F, 1.0F, 0.0F, 0.0F);
				GL11.glRotatef(-40.0F, 0.0F, 0.0F, 1.0F);
	
				tessellator.startDrawingQuads();
				tessellator.addVertexWithUV(-digmaSize, digmaDist, -digmaSize, 0.0D, 0.0D);
				tessellator.addVertexWithUV(digmaSize, digmaDist, -digmaSize, 0.0D, 1.0D);
				tessellator.addVertexWithUV(digmaSize, digmaDist, digmaSize, 1.0D, 1.0D);
				tessellator.addVertexWithUV(-digmaSize, digmaDist, digmaSize, 1.0D, 0.0D);
				tessellator.draw();
		
				OpenGlHelper.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO);

			}
			GL11.glPopMatrix();

		}
		GL11.glPopMatrix();
	}

	protected void renderSun(float partialTicks, WorldClient world, Minecraft mc, CelestialBody sun, double sunSize, double coronaSize, float visibility, float pressure) {
		Tessellator tessellator = Tessellator.instance;

		CBT_Dyson dyson = sun.getTrait(CBT_Dyson.class);
		int swarmCount = dyson != null ? dyson.size() : 0;

		if(sun.shader != null) {
			// BLACK HOLE SUN
			// WON'T YOU COME
			// AND WASH AWAY THE RAIN
			CBT_DEMETER d = sun.getTrait(CBT_DEMETER.class);
			Shader shader = sun.shader;
			double shaderSize = sunSize * sun.shaderScale;
			if(d != null) {
				RenderIt(world, partialTicks, shaderSize, mc, sun);
			}else {
				GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

				shader.use();

				float time = ((float)world.getWorldTime() + partialTicks) / 20.0F;

				mc.renderEngine.bindTexture(noise);
				GL11.glPushMatrix();

				// Fix orbital plane
				GL11.glRotatef(-90.0F, 0, 1, 0);
				shader.setUniform1f("iTime", time);
				shader.setUniform1i("iChannel1", 0);

				tessellator.startDrawingQuads();
				tessellator.addVertexWithUV(-shaderSize, 100.0D, -shaderSize, 0.0D, 0.0D);
				tessellator.addVertexWithUV(shaderSize, 100.0D, -shaderSize, 1.0D, 0.0D);
				tessellator.addVertexWithUV(shaderSize, 100.0D, shaderSize, 1.0D, 1.0D);
				tessellator.addVertexWithUV(-shaderSize, 100.0D, shaderSize, 0.0D, 1.0D);
				tessellator.draw();

				shader.stop();

				GL11.glPopMatrix();

				OpenGlHelper.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ONE, GL11.GL_ZERO);
			}



			
		} else {
			// Some blanking to conceal the stars
			GL11.glDisable(GL11.GL_TEXTURE_2D);
			GL11.glColor4f(0.0F, 0.0F, 0.0F, 1.0F);

			tessellator.startDrawingQuads();
			tessellator.addVertex(-sunSize, 99.9D, -sunSize);
			tessellator.addVertex(sunSize, 99.9D, -sunSize);
			tessellator.addVertex(sunSize, 99.9D, sunSize);
			tessellator.addVertex(-sunSize, 99.9D, sunSize);
			tessellator.draw();

			// Draw the sun to the depth buffer to block swarm members that are behind
			GL11.glDepthMask(true);
			GL11.glColor4f(0.0F, 0.0F, 0.0F, 0.0F);

			tessellator.startDrawingQuads();
			tessellator.addVertexWithUV(-sunSize * 0.25D, 100.1D, -sunSize * 0.25D, 0.0D, 0.0D);
			tessellator.addVertexWithUV(sunSize * 0.25D, 100.1D, -sunSize * 0.25D, 1.0D, 0.0D);
			tessellator.addVertexWithUV(sunSize * 0.25D, 100.1D, sunSize * 0.25D, 1.0D, 1.0D);
			tessellator.addVertexWithUV(-sunSize * 0.25D, 100.1D, sunSize * 0.25D, 0.0D, 1.0D);
			tessellator.draw();

			GL11.glDepthMask(false);

			GL11.glEnable(GL11.GL_TEXTURE_2D);
			GL11.glColor4f(1.0F, 1.0F, 1.0F, visibility);

			mc.renderEngine.bindTexture(sun.texture);

			float[] sunColor = world.provider instanceof WorldProviderCelestial
				? ((WorldProviderCelestial) world.provider).getSunColor()
				: new float[]{1.0F, 1.0F, 1.0F};

			GL11.glColor4f(sunColor[0], sunColor[1], sunColor[2], visibility);
			tessellator.startDrawingQuads();
			tessellator.addVertexWithUV(-sunSize, 100.0D, -sunSize, 0.0D, 0.0D);
			tessellator.addVertexWithUV(sunSize, 100.0D, -sunSize, 1.0D, 0.0D);
			tessellator.addVertexWithUV(sunSize, 100.0D, sunSize, 1.0D, 1.0D);
			tessellator.addVertexWithUV(-sunSize, 100.0D, sunSize, 0.0D, 1.0D);
			tessellator.draw();

			// Draw a big ol' spiky flare! Less so when there is an atmosphere
			GL11.glColor4f(sunColor[0], sunColor[1], sunColor[2], 1 - MathHelper.clamp_float(pressure, 0.0F, 1.0F) * 0.75F);
			mc.renderEngine.bindTexture(flareTexture);

			tessellator.startDrawingQuads();
			tessellator.addVertexWithUV(-coronaSize, 99.9D, -coronaSize, 0.0D, 0.0D);
			tessellator.addVertexWithUV(coronaSize, 99.9D, -coronaSize, 1.0D, 0.0D);
			tessellator.addVertexWithUV(coronaSize, 99.9D, coronaSize, 1.0D, 1.0D);
			tessellator.addVertexWithUV(-coronaSize, 99.9D, coronaSize, 0.0D, 1.0D);
			tessellator.draw();

			// Draw the swarm members with depth occlusion
			// We do this last so we can render transparency against the sun
			renderSwarm(partialTicks, world, mc, sunSize * 0.5, swarmCount);

			// Clear and disable the depth buffer once again, buffer has to be writable to clear it
			GL11.glDepthMask(true);
			GL11.glClear(GL11.GL_DEPTH_BUFFER_BIT);
			GL11.glDepthMask(false);
		}
	}

	private void renderSwarm(float partialTicks, WorldClient world, Minecraft mc, double swarmRadius, int swarmCount) {
		Tessellator tessellator = Tessellator.instance;

		// bloodseeking, parasitic, ecstatically tracing decay
		// thriving in the glow that death emits, the warm perfume it radiates

		//perfume makes my eyes water -j

		swarmShader.use();

		// swarm members render as pixels, which can vary based on screen resolution
		// because of this, we make the pixels more transparent based on their apparent size, which varies by a fair few factors
		// this isn't a foolproof solution, analyzing the projection matrices would be best, but it works for now.
		float swarmScreenSize = (float) ((mc.displayHeight / currentFov) * swarmRadius * 0.002);
		float time = ((float) world.getWorldTime() + partialTicks) / 800.0F;

		swarmShader.setUniform1f("iTime", time);

		int offsetLocation = swarmShader.getUniformLocation("iOffset");

		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		GL11.glColor4f(0.0F, 0.0F, 0.0F, MathHelper.clamp_float(swarmScreenSize, 0, 1));

		GL11.glPushMatrix();
		{

			GL11.glTranslatef(0.0F, 100.0F, 0.0F);
			GL11.glScaled(swarmRadius, swarmRadius, swarmRadius);

			GL11.glPushMatrix();
			{

				GL11.glRotatef(80.0F, 1, 0, 0);

				tessellator.startDrawing(GL11.GL_POINTS);
				for(int i = 0; i < swarmCount; i += 3) {
					swarmShader.setUniform1f(offsetLocation, i);

					float t = i + time;
					double x = Math.cos(t);
					double z = Math.sin(t);

					tessellator.addVertex(x, 0, z);
				}
				tessellator.draw();

			}
			GL11.glPopMatrix();

			GL11.glPushMatrix();
			{

				GL11.glRotatef(60.0F, 0, 1, 0);
				GL11.glRotatef(80.0F, 1, 0, 0);

				tessellator.startDrawing(GL11.GL_POINTS);
				for(int i = 1; i < swarmCount; i += 3) {
					swarmShader.setUniform1f(offsetLocation, i);

					float t = i + time;
					double x = Math.cos(t);
					double z = Math.sin(t);

					tessellator.addVertex(x, 0, z);
				}
				tessellator.draw();

			}
			GL11.glPopMatrix();

			GL11.glPushMatrix();
			{

				GL11.glRotatef(-60.0F, 0, 1, 0);
				GL11.glRotatef(80.0F, 1, 0, 0);

				tessellator.startDrawing(GL11.GL_POINTS);
				for(int i = 2; i < swarmCount; i += 3) {
					swarmShader.setUniform1f(offsetLocation, i);

					float t = i + time;
					double x = Math.cos(t);
					double z = Math.sin(t);

					tessellator.addVertex(x, 0, z);
				}
				tessellator.draw();

			}
			GL11.glPopMatrix();

		}
		GL11.glPopMatrix();

		swarmShader.stop();

		OpenGlHelper.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ONE, GL11.GL_ZERO);
	}

	protected void renderCelestials(float partialTicks, WorldClient world, Minecraft mc, List<AstroMetric> metrics, float solarAngle, CelestialBody tidalLockedBody, Vec3 planetTint, float visibility, float blendAmount, CelestialBody orbiting, float maxSize) {
		Tessellator tessellator = Tessellator.instance;
		float blendDarken = 0.1F;

		double transitionMinSize = 0.01D;
		double transitionMaxSize = 0.5D;

		for(AstroMetric metric : metrics) {

			// Ignore self
			if(metric.distance == 0)
				continue;

			boolean orbitingThis = metric.body == orbiting;

			double uvOffset = orbitingThis ? 1 - ((((double) world.getWorldTime() + partialTicks) / 1024) % 1) : 0;
			double atmospherePatternOffset = orbitingThis ? -(((double) world.getWorldTime() + partialTicks) / 1024.0D) : 0.0D;
			float axialTilt = orbitingThis ? 0 : metric.body.axialTilt;

			GL11.glPushMatrix();
			{

				double size = MathHelper.clamp_double(metric.apparentSize, 0, maxSize);
				boolean renderPoint = size < transitionMaxSize;
				boolean renderBody = size > transitionMinSize;

				GL11.glRotated(metric.angle, 1.0, 0.0, 0.0);
				GL11.glRotated(metric.inclination, 0.0, 0.0, 1.0);
				GL11.glRotatef(axialTilt + 90.0F, 0.0F, 1.0F, 0.0F);

				if(renderBody) {
					// Draw the back half of the ring (obscured by body)
					if(metric.body.hasRings) {
						GL11.glPushMatrix();
						{

							GL11.glColor4f(metric.body.ringColor[0], metric.body.ringColor[1], metric.body.ringColor[2], visibility);
							mc.renderEngine.bindTexture(ringTexture);

							GL11.glDisable(GL11.GL_CULL_FACE);

							double ringSize = size * metric.body.ringSize;

							GL11.glTranslatef(0.0F, 100.0F, 0.0F);
							GL11.glRotated(-metric.angle, 0, 0, 1);
							GL11.glRotatef(90.0F - metric.body.ringTilt, 1, 0, 0);
							GL11.glRotated(metric.angle, 0, 1, 0);

							tessellator.startDrawingQuads();
							tessellator.addVertexWithUV(-ringSize, 0, -ringSize, 0.0D, 0.0D);
							tessellator.addVertexWithUV(ringSize, 0, -ringSize, 1.0D, 0.0D);
							tessellator.addVertexWithUV(ringSize, 0, 0, 1.0D, 0.5D);
							tessellator.addVertexWithUV(-ringSize, 0, 0, 0.0D, 0.5D);
							tessellator.draw();

							GL11.glEnable(GL11.GL_CULL_FACE);

						}
						GL11.glPopMatrix();
					}

					CBT_Destroyed d = metric.body.getTrait(CBT_Destroyed.class);

					if(d != null) {
						// Stop calling things "interp", that's a verb not a noun
						double destroyedProgressClientInterpolation = d.effectTimer + size * 0.5;

						float alpha = (float) (1.0F - Math.min(1.0F, destroyedProgressClientInterpolation / 100));
						Random random = new Random(12);

						int numQuads = 30;
						for (int i = 0; i < numQuads; i++) {
							double radius = (random.nextDouble() * size) * d.effectTimer;

							double randomTheta = random.nextDouble() * Math.PI * 2;
							double randomPhi = random.nextDouble() * Math.PI;

							double randomX = radius * Math.sin(randomPhi) * Math.cos(randomTheta) * 0.7;
							double randomY = radius * Math.sin(randomPhi) * Math.sin(randomTheta);
							double randomZ = radius * Math.cos(randomPhi) * 0.7;

							float randomRotation = random.nextFloat() * 360.0F;


							double uMin = random.nextDouble();
							double vMin = random.nextDouble();
							double uMax = Math.min(uMin + (random.nextDouble() * 0.2), 1.0);
							double vMax = Math.min(vMin + (random.nextDouble() * 0.2), 1.0);

							GL11.glPushMatrix();
							{

								GL11.glTranslated(randomX * -0.05, randomY * 0.00, randomZ * -0.05);

								GL11.glRotatef(randomRotation * d.effectTimer * 0.05F, 0.0F, 1.0F, 0.0F);

								mc.renderEngine.bindTexture(metric.body.texture);
								GL11.glColor4d(1, 1, 1, 1);

								tessellator.startDrawingQuads();
								double qsize = size * random.nextDouble() * 0.1;
								tessellator.addVertexWithUV(-qsize, 100.0D, -qsize, uMin, vMin);
								tessellator.addVertexWithUV(qsize, 100.0D, -qsize, uMax, vMin);
								tessellator.addVertexWithUV(qsize, 100.0D, qsize, uMax, vMax);
								tessellator.addVertexWithUV(-qsize, 100.0D, qsize, uMin, vMax);

								tessellator.draw();
							}

							GL11.glPopMatrix();
							GL11.glPushMatrix();
							{

								GL11.glTranslated(randomX * 0.04, randomY * 0.00, randomZ * 0.04);

								GL11.glRotatef(randomRotation * d.effectTimer * 0.05F, 0.0F, 1.0F, 0.0F);
								mc.renderEngine.bindTexture(destroyedBody);
								GL11.glColor4d(1, 1, 1, 1);
								tessellator.startDrawingQuads();
								double qsize = size * random.nextDouble() * 0.07;
								tessellator.addVertexWithUV(-qsize, 100.0D, -qsize, uMin, vMin);
								tessellator.addVertexWithUV(qsize, 100.0D, -qsize, uMax, vMin);
								tessellator.addVertexWithUV(qsize, 100.0D, qsize, uMax, vMax);
								tessellator.addVertexWithUV(-qsize, 100.0D, qsize, uMin, vMax);

								tessellator.draw();

							}
							GL11.glPopMatrix();

						}


						GL11.glColor4f(1.0F, 1.0F, 1.0F, alpha);
						mc.renderEngine.bindTexture(shockwaveTexture);
						double interpe = (d.effectTimer * 0.5) * size * 0.1;
						tessellator.startDrawingQuads();
						tessellator.addVertexWithUV(-interpe, 100.0D, -interpe, 0.0D + uvOffset, 0.0D);
						tessellator.addVertexWithUV(interpe, 100.0D, -interpe, 1.0D + uvOffset, 0.0D);
						tessellator.addVertexWithUV(interpe, 100.0D, interpe, 1.0D + uvOffset, 1.0D);
						tessellator.addVertexWithUV(-interpe, 100.0D, interpe, 0.0D + uvOffset, 1.0D);
						tessellator.draw();


						GL11.glColor4f(1.0F, 1.0F, 1.0F, alpha * 2);
						mc.renderEngine.bindTexture(shockFlareTexture);

						destroyedProgressClientInterpolation = size * 3;
						tessellator.startDrawingQuads();
						tessellator.addVertexWithUV(-destroyedProgressClientInterpolation, 100.0D, -destroyedProgressClientInterpolation, 0.0D + uvOffset, 0.0D);
						tessellator.addVertexWithUV(destroyedProgressClientInterpolation, 100.0D, -destroyedProgressClientInterpolation, 1.0D + uvOffset, 0.0D);
						tessellator.addVertexWithUV(destroyedProgressClientInterpolation, 100.0D, destroyedProgressClientInterpolation, 1.0D + uvOffset, 1.0D);
						tessellator.addVertexWithUV(-destroyedProgressClientInterpolation, 100.0D, destroyedProgressClientInterpolation, 0.0D + uvOffset, 1.0D);
						tessellator.draw();

					} else {

						renderAtmosphereGlow(tessellator, mc, metric.body, size, 1.0F, metric.phase);

						GL11.glDisable(GL11.GL_BLEND);
						GL11.glColor4f(1.0F, 1.0F, 1.0F, visibility);
						mc.renderEngine.bindTexture(metric.body.texture);

						tessellator.startDrawingQuads();
						tessellator.addVertexWithUV(-size, 100.0D, -size, 0.0D + uvOffset, 0.0D);
						tessellator.addVertexWithUV(size, 100.0D, -size, 1.0D + uvOffset, 0.0D);
						tessellator.addVertexWithUV(size, 100.0D, size, 1.0D + uvOffset, 1.0D);
						tessellator.addVertexWithUV(-size, 100.0D, size, 0.0D + uvOffset, 1.0D);
						tessellator.draw();


						CBT_Impact impact = metric.body.getTrait(CBT_Impact.class);
						CBT_Lights light = metric.body.getTrait(CBT_Lights.class);
						List<CelestialNukeShockHandler.ShockStatus> nukeShocks = CelestialNukeShockHandler.getClientShocks(metric.body);

						double impactTime = impact != null ? (world.getTotalWorldTime() - impact.time) + partialTicks : 0;
						float impactAnimationTime = impact != null ? (float) impactTime : -1.0F;
						int lightIntensity = light != null && impactTime < 40 ? MathHelper.clamp_int(light.getIntensity(), 0, citylights.length - 1) : 0;

						int blackoutInterval = 8;
						int maxBlackouts = 5;

						int activeBlackouts = Math.min((int) (impactTime / blackoutInterval), maxBlackouts);

						Vec3 atmosphereColor = AtmosphereRenderUtil.getBodyAtmosphereColor(metric.body);
						Vec3 cloudColor = AtmosphereRenderUtil.getBodyCloudColor(metric.body);
						float cloudTintStrength = AtmosphereRenderUtil.getBodyCloudTintStrength(metric.body);
						float cloudStormDarkness = AtmosphereRenderUtil.getBodyCloudStormDarkness(metric.body, partialTicks);
						float cloudLightningStrength = AtmosphereRenderUtil.getBodyCloudLightningStrength(metric.body, partialTicks);
						float atmosphereOverlayAlpha = AtmosphereRenderUtil.getAtmosphereSurfaceAlpha(metric.body);
						float atmosphereDensity = AtmosphereRenderUtil.getAtmosphereDensity(metric.body);
						int atmosphereStyle = AtmosphereRenderUtil.getAtmosphereStyle(metric.body);

						float atmosphereTime = ((float) world.getTotalWorldTime() + partialTicks) / 20.0F;
						double currentShockTime = world.getTotalWorldTime() + partialTicks;
						renderAtmosphereSurface(tessellator, atmosphereColor, cloudColor, cloudTintStrength, cloudStormDarkness, atmosphereOverlayAlpha, uvOffset, atmospherePatternOffset, size, atmosphereTime, atmosphereStyle, impactAnimationTime, nukeShocks, currentShockTime);
						renderCrescentShadow(tessellator, (float) -metric.phase, uvOffset, size);
						renderAtmosphereEmissive(tessellator, mc, metric.body, (float) -metric.phase, uvOffset, size, lightIntensity, activeBlackouts, atmosphereDensity, atmospherePatternOffset, atmosphereTime, atmosphereStyle, impactAnimationTime, nukeShocks, currentShockTime);
						renderNightLights(tessellator, mc, metric.body, (float) -metric.phase, uvOffset, size, lightIntensity, activeBlackouts, atmosphereDensity, atmospherePatternOffset, atmosphereTime, atmosphereStyle, impactAnimationTime, nukeShocks, currentShockTime);
						renderLightningOverlay(tessellator, mc, metric.body, (float) -metric.phase, cloudTintStrength, cloudLightningStrength, atmosphereOverlayAlpha, uvOffset, atmospherePatternOffset, size, atmosphereTime, atmosphereStyle, impactAnimationTime, nukeShocks, currentShockTime);

						OpenGlHelper.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ONE, GL11.GL_ZERO);

						if(impact != null) {
							double lavaAlpha = Math.min(impactTime * 0.1, 1.0);

							double impactSize = (impactTime * 0.1) * size * 0.035;
							double impactAlpha = 1.0 - Math.min(1.0, impactTime * 0.0015);
							double flareSize = size * 1.5;
							double flareAlpha = 1.0 - Math.min(1.0, impactTime * 0.002);

							if(lavaAlpha > 0) {
								GL11.glColor4d(1.0, 1.0, 1.0, lavaAlpha);
								mc.renderEngine.bindTexture(impactTexture);

								tessellator.startDrawingQuads();
								tessellator.addVertexWithUV(-size, 100.0D, -size, 0.0D + uvOffset, 0.0D);
								tessellator.addVertexWithUV(size, 100.0D, -size, 1.0D + uvOffset, 0.0D);
								tessellator.addVertexWithUV(size, 100.0D, size, 1.0D + uvOffset, 1.0D);
								tessellator.addVertexWithUV(-size, 100.0D, size, 0.0D + uvOffset, 1.0D);
								tessellator.draw();
							}

							GL11.glPushMatrix();
							{

								GL11.glTranslated(-size * 0.5, 0, size * 0.4);

								// impact shockwave, increases in size and fades out
								if(impactAlpha > 0) {
									GL11.glColor4d(1.0, 1.0, 1.0F, impactAlpha);
									mc.renderEngine.bindTexture(shockwaveTexture);

									tessellator.startDrawingQuads();
									tessellator.addVertexWithUV(-impactSize, 100.0D, -impactSize, 0.0D, 0.0D);
									tessellator.addVertexWithUV(impactSize, 100.0D, -impactSize, 1.0D, 0.0D);
									tessellator.addVertexWithUV(impactSize, 100.0D, impactSize, 1.0D, 1.0D);
									tessellator.addVertexWithUV(-impactSize, 100.0D, impactSize, 0.0D, 1.0D);
									tessellator.draw();
								}

								// impact flare, remains static in size and fades out
								if(flareAlpha > 0) {
									GL11.glColor4d(1.0F, 1.0F, 1.0F, flareAlpha);
									mc.renderEngine.bindTexture(shockFlareTexture);

									tessellator.startDrawingQuads();
									tessellator.addVertexWithUV(-flareSize, 100.0D, -flareSize, 0.0D, 0.0D);
									tessellator.addVertexWithUV(flareSize, 100.0D, -flareSize, 1.0D, 0.0D);
									tessellator.addVertexWithUV(flareSize, 100.0D, flareSize, 1.0D, 1.0D);
									tessellator.addVertexWithUV(-flareSize, 100.0D, flareSize, 0.0D, 1.0D);
									tessellator.draw();
								}

							}
							GL11.glPopMatrix();
						}


						GL11.glDisable(GL11.GL_TEXTURE_2D);

						// Draw another layer on top to blend with the atmosphere
						GL11.glColor4d(planetTint.xCoord - blendDarken, planetTint.yCoord - blendDarken, planetTint.zCoord - blendDarken, (1 - blendAmount * visibility));

						tessellator.startDrawingQuads();
						tessellator.addVertexWithUV(-size, 100.0D, -size, 0.0D, 0.0D);
						tessellator.addVertexWithUV(size, 100.0D, -size, 1.0D, 0.0D);
						tessellator.addVertexWithUV(size, 100.0D, size, 1.0D, 1.0D);
						tessellator.addVertexWithUV(-size, 100.0D, size, 0.0D, 1.0D);
						tessellator.draw();

						GL11.glEnable(GL11.GL_TEXTURE_2D);
					}


					// Draw the front half of the ring (unobscured)
					if(metric.body.hasRings) {
						GL11.glPushMatrix();
						GL11.glColor4f(metric.body.ringColor[0], metric.body.ringColor[1], metric.body.ringColor[2], visibility);
						mc.renderEngine.bindTexture(ringTexture);

						double ringSize = size * metric.body.ringSize;

						GL11.glDisable(GL11.GL_CULL_FACE);

						GL11.glTranslatef(0.0F, 100.0F, 0.0F);
						GL11.glRotated(-metric.angle, 0, 0, 1);
						GL11.glRotatef(90.0F - metric.body.ringTilt, 1, 0, 0);
						GL11.glRotated(metric.angle, 0, 1, 0);

						tessellator.startDrawingQuads();
						tessellator.addVertexWithUV(-ringSize, 0, 0, 0.0D, 0.5D);
						tessellator.addVertexWithUV(ringSize, 0, 0, 1.0D, 0.5D);
						tessellator.addVertexWithUV(ringSize, 0, ringSize, 1.0D, 1.0D);
						tessellator.addVertexWithUV(-ringSize, 0, ringSize, 0.0D, 1.0D);
						tessellator.draw();

						GL11.glEnable(GL11.GL_CULL_FACE);
						GL11.glPopMatrix();
					}

					List<CelestialNukeShockHandler.ShockStatus> flashShocks = CelestialNukeShockHandler.getClientShocks(metric.body);
					double flashShockTime = world.getTotalWorldTime() + partialTicks;
					renderNukeImpactOverlays(tessellator, mc, size, (float) -metric.phase, flashShocks, flashShockTime);
				}

				if(renderPoint) {
					float alpha = MathHelper.clamp_float((float) size * 100.0F, 0.0F, 1.0F);
					alpha *= 1 - BobMathUtil.remap01_clamp((float) size, (float) transitionMinSize, (float) transitionMaxSize);
					GL11.glColor4f(metric.body.color[0], metric.body.color[1], metric.body.color[2], alpha * visibility);
					mc.renderEngine.bindTexture(planetTexture);

					tessellator.startDrawingQuads();
					tessellator.addVertexWithUV(-1.0D, 100.0D, -1.0D, 0.0D, 0.0D);
					tessellator.addVertexWithUV(1.0D, 100.0D, -1.0D, 1.0D, 0.0D);
					tessellator.addVertexWithUV(1.0D, 100.0D, 1.0D, 1.0D, 1.0D);
					tessellator.addVertexWithUV(-1.0D, 100.0D, 1.0D, 0.0D, 1.0D);
					tessellator.draw();
				}

			}
			GL11.glPopMatrix();
		}
	}

	private void drawPlanetShaderQuad(Tessellator tessellator, double size) {
		tessellator.startDrawingQuads();
		tessellator.addVertexWithUV(-size, 100.0D, -size, 0.0D, 0.0D);
		tessellator.addVertexWithUV(size, 100.0D, -size, 1.0D, 0.0D);
		tessellator.addVertexWithUV(size, 100.0D, size, 1.0D, 1.0D);
		tessellator.addVertexWithUV(-size, 100.0D, size, 0.0D, 1.0D);
		tessellator.draw();
	}

	private void renderAtmosphereSurface(Tessellator tessellator, Vec3 atmosphereColor, Vec3 cloudColor, float cloudTintStrength, float cloudStormDarkness, float atmosphereAlpha, double uvOffset, double patternOffset, double size, float atmosphereTime, int atmosphereStyle, float impactTime, List<CelestialNukeShockHandler.ShockStatus> nukeShocks, double currentShockTime) {
		if(atmosphereAlpha <= 0.001F) {
			return;
		}

		GL11.glEnable(GL11.GL_BLEND);
		OpenGlHelper.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO);
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);

		atmosphereShader.use();
		atmosphereShader.setUniform1f("offset", (float) uvOffset);
		atmosphereShader.setUniform1f("patternOffset", (float) patternOffset);
		atmosphereShader.setUniform1i("bodyTex", 0);
		atmosphereShader.setUniform1i("useBodyAlphaMask", 0);
		atmosphereShader.setUniform1f("atmosphereColorR", (float) atmosphereColor.xCoord);
		atmosphereShader.setUniform1f("atmosphereColorG", (float) atmosphereColor.yCoord);
		atmosphereShader.setUniform1f("atmosphereColorB", (float) atmosphereColor.zCoord);
		atmosphereShader.setUniform1f("cloudColorR", (float) cloudColor.xCoord);
		atmosphereShader.setUniform1f("cloudColorG", (float) cloudColor.yCoord);
		atmosphereShader.setUniform1f("cloudColorB", (float) cloudColor.zCoord);
		atmosphereShader.setUniform1f("cloudTintStrength", cloudTintStrength);
		atmosphereShader.setUniform1f("cloudStormDarkness", cloudStormDarkness);
		atmosphereShader.setUniform1f("atmosphereAlpha", atmosphereAlpha);
		atmosphereShader.setUniform1f("atmosphereTime", atmosphereTime);
		atmosphereShader.setUniform1i("atmosphereStyle", atmosphereStyle);
		atmosphereShader.setUniform1f("impactTime", impactTime);
		AtmosphereRenderUtil.applyNukeShockUniforms(atmosphereShader, nukeShocks, currentShockTime);
		drawPlanetShaderQuad(tessellator, size);
		atmosphereShader.stop();
	}

	private void renderLightningOverlay(Tessellator tessellator, Minecraft mc, CelestialBody body, float phase, float cloudTintStrength, float cloudLightningStrength, float atmosphereAlpha, double uvOffset, double patternOffset, double size, float atmosphereTime, int atmosphereStyle, float impactTime, List<CelestialNukeShockHandler.ShockStatus> nukeShocks, double currentShockTime) {
		if(atmosphereAlpha <= 0.001F || cloudLightningStrength <= 0.001F || (atmosphereStyle != AtmosphereRenderUtil.ATMOSPHERE_STYLE_CLOUDS && atmosphereStyle != AtmosphereRenderUtil.ATMOSPHERE_STYLE_HAZE)) {
			return;
		}

		GL11.glEnable(GL11.GL_BLEND);
		OpenGlHelper.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ONE, GL11.GL_ZERO);
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);

		lightningShader.use();
		lightningShader.setUniform1f("phase", phase);
		lightningShader.setUniform1f("offset", (float) uvOffset);
		lightningShader.setUniform1f("patternOffset", (float) patternOffset);
		lightningShader.setUniform1i("bodyTex", 0);
		lightningShader.setUniform1i("cityMask", 1);
		lightningShader.setUniform1i("useBodyAlphaMask", 0);
		lightningShader.setUniform1f("cloudTintStrength", cloudTintStrength);
		lightningShader.setUniform1f("cloudLightningStrength", cloudLightningStrength);
		lightningShader.setUniform1f("atmosphereAlpha", atmosphereAlpha);
		lightningShader.setUniform1f("atmosphereTime", atmosphereTime);
		lightningShader.setUniform1f("eveFlashStrength", AtmosphereRenderUtil.getBodyEveFlashStrength(body, atmosphereTime));
		lightningShader.setUniform1i("atmosphereStyle", atmosphereStyle);
		lightningShader.setUniform1i("lightningMode", AtmosphereRenderUtil.getBodyLightningMode(body));
		lightningShader.setUniform1f("impactTime", impactTime);
		AtmosphereRenderUtil.applyNukeShockUniforms(lightningShader, nukeShocks, currentShockTime);

		mc.renderEngine.bindTexture(body.texture);
		if(gl13) {
			GL13.glActiveTexture(GL13.GL_TEXTURE1);
			mc.renderEngine.bindTexture(body.cityMask != null ? body.cityMask : defaultMask);
			GL13.glActiveTexture(GL13.GL_TEXTURE0);
		}
		drawPlanetShaderQuad(tessellator, size);
		lightningShader.stop();
	}

	private void renderNukeImpactOverlays(Tessellator tessellator, Minecraft mc, double size, float phase, List<CelestialNukeShockHandler.ShockStatus> nukeShocks, double currentShockTime) {
		if(nukeShocks.isEmpty()) {
			return;
		}

		GL11.glEnable(GL11.GL_BLEND);
		OpenGlHelper.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ONE, GL11.GL_ZERO);
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);

		nukeShader.use();
		nukeShader.setUniform1f("phase", phase);
		AtmosphereRenderUtil.applyNukeShockUniforms(nukeShader, nukeShocks, currentShockTime);
		drawPlanetShaderQuad(tessellator, size);
		nukeShader.stop();
	}

	private void renderAtmosphereEmissive(Tessellator tessellator, Minecraft mc, CelestialBody body, float phase, double uvOffset, double size, int lightIntensity, int activeBlackouts, float atmosphereDensity, double patternOffset, float atmosphereTime, int atmosphereStyle, float impactTime, List<CelestialNukeShockHandler.ShockStatus> nukeShocks, double currentShockTime) {
		if(lightIntensity <= 0 || atmosphereDensity <= 0.001F || (atmosphereStyle != AtmosphereRenderUtil.ATMOSPHERE_STYLE_CLOUDS && atmosphereStyle != AtmosphereRenderUtil.ATMOSPHERE_STYLE_HAZE)) {
			return;
		}

		GL11.glEnable(GL11.GL_BLEND);
		OpenGlHelper.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ONE, GL11.GL_ZERO);
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);

		atmosphereEmissiveShader.use();
		atmosphereEmissiveShader.setUniform1f("phase", phase);
		atmosphereEmissiveShader.setUniform1f("offset", (float) uvOffset);
		atmosphereEmissiveShader.setUniform1f("atmosphereDensity", atmosphereDensity);
		atmosphereEmissiveShader.setUniform1f("patternOffset", (float) patternOffset);
		atmosphereEmissiveShader.setUniform1f("atmosphereTime", atmosphereTime);
		atmosphereEmissiveShader.setUniform1i("atmosphereStyle", atmosphereStyle);
		atmosphereEmissiveShader.setUniform1f("impactTime", impactTime);
		AtmosphereRenderUtil.applyNukeShockUniforms(atmosphereEmissiveShader, nukeShocks, currentShockTime);
		atmosphereEmissiveShader.setUniform1i("bodyTex", 0);
		atmosphereEmissiveShader.setUniform1i("lights", 0);
		atmosphereEmissiveShader.setUniform1i("cityMask", 1);
		atmosphereEmissiveShader.setUniform1i("blackouts", activeBlackouts);
		atmosphereEmissiveShader.setUniform1i("useBodyAlphaMask", 0);

		mc.renderEngine.bindTexture(citylights[lightIntensity]);
		if(gl13) {
			GL13.glActiveTexture(GL13.GL_TEXTURE1);
			mc.renderEngine.bindTexture(body.cityMask != null ? body.cityMask : defaultMask);
			GL13.glActiveTexture(GL13.GL_TEXTURE0);
		}

		drawPlanetShaderQuad(tessellator, size);
		atmosphereEmissiveShader.stop();
	}

	private void renderCrescentShadow(Tessellator tessellator, float phase, double uvOffset, double size) {
		GL11.glEnable(GL11.GL_BLEND);
		OpenGlHelper.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO);
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);

		crescentShader.use();
		crescentShader.setUniform1f("phase", phase);
		crescentShader.setUniform1f("offset", (float) uvOffset);
		crescentShader.setUniform1i("bodyTex", 0);
		crescentShader.setUniform1i("useBodyAlphaMask", 0);
		drawPlanetShaderQuad(tessellator, size);
		crescentShader.stop();
	}

	private void renderNightLights(Tessellator tessellator, Minecraft mc, CelestialBody body, float phase, double uvOffset, double size, int lightIntensity, int activeBlackouts, float atmosphereDensity, double patternOffset, float atmosphereTime, int atmosphereStyle, float impactTime, List<CelestialNukeShockHandler.ShockStatus> nukeShocks, double currentShockTime) {
		if(lightIntensity <= 0) {
			return;
		}

		GL11.glEnable(GL11.GL_BLEND);
		OpenGlHelper.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ONE, GL11.GL_ZERO);
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);

		nightLightsShader.use();
		nightLightsShader.setUniform1f("phase", phase);
		nightLightsShader.setUniform1f("offset", (float) uvOffset);
		nightLightsShader.setUniform1f("atmosphereDensity", atmosphereDensity);
		nightLightsShader.setUniform1f("patternOffset", (float) patternOffset);
		nightLightsShader.setUniform1f("atmosphereTime", atmosphereTime);
		nightLightsShader.setUniform1i("atmosphereStyle", atmosphereStyle);
		nightLightsShader.setUniform1f("impactTime", impactTime);
		AtmosphereRenderUtil.applyNukeShockUniforms(nightLightsShader, nukeShocks, currentShockTime);
		nightLightsShader.setUniform1i("bodyTex", 0);
		nightLightsShader.setUniform1i("lights", 0);
		nightLightsShader.setUniform1i("cityMask", 1);
		nightLightsShader.setUniform1i("blackouts", activeBlackouts);
		nightLightsShader.setUniform1i("useBodyAlphaMask", 0);

		mc.renderEngine.bindTexture(citylights[lightIntensity]);
		if(gl13) {
			GL13.glActiveTexture(GL13.GL_TEXTURE1);
			mc.renderEngine.bindTexture(body.cityMask != null ? body.cityMask : defaultMask);
			GL13.glActiveTexture(GL13.GL_TEXTURE0);
		}

		drawPlanetShaderQuad(tessellator, size);
		nightLightsShader.stop();
	}


	protected void renderDigamma(float partialTicks, WorldClient world, Minecraft mc, float solarAngle) {
		Tessellator tessellator = Tessellator.instance;

		GL11.glPushMatrix();
		{

			float var12 = 1F + world.rand.nextFloat() * 0.5F;
			double dist = 100D;

			if(ModEventHandlerClient.renderLodeStar) {
				GL11.glPushMatrix();
				GL11.glRotatef(-75.0F, 1.0F, 0.0F, 0.0F);
				GL11.glRotatef(10.0F, 0.0F, 1.0F, 0.0F);
				FMLClientHandler.instance().getClient().renderEngine.bindTexture(lodeStar); // genu-ine bona-fide ass whooping

				tessellator.startDrawingQuads();
				tessellator.addVertexWithUV(-var12, dist, -var12, 0.0D, 0.0D);
				tessellator.addVertexWithUV(var12, dist, -var12, 0.0D, 1.0D);
				tessellator.addVertexWithUV(var12, dist, var12, 1.0D, 1.0D);
				tessellator.addVertexWithUV(-var12, dist, var12, 1.0D, 0.0D);
				tessellator.draw();

				GL11.glPopMatrix();
			}

			OpenGlHelper.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ONE, GL11.GL_ZERO);

			float brightness = (float) Math.sin(solarAngle * Math.PI);
			brightness *= brightness;
			GL11.glColor4f(brightness, brightness, brightness, brightness);
			GL11.glRotatef(-90.0F, 0.0F, 1.0F, 0.0F);
			GL11.glRotatef(solarAngle * 360.0F, 1.0F, 0.0F, 0.0F);
			GL11.glRotatef(140.0F, 1.0F, 0.0F, 0.0F);
			GL11.glRotatef(-40.0F, 0.0F, 0.0F, 1.0F);

			mc.renderEngine.bindTexture(digammaStar);

			float digamma = HbmLivingProps.getDigamma(Minecraft.getMinecraft().thePlayer);
			var12 = 1F * (1 + digamma * 0.25F);
			dist = 100D - digamma * 2.5;

			tessellator.startDrawingQuads();
			tessellator.addVertexWithUV(-var12, dist, -var12, 0.0D, 0.0D);
			tessellator.addVertexWithUV(var12, dist, -var12, 0.0D, 1.0D);
			tessellator.addVertexWithUV(var12, dist, var12, 1.0D, 1.0D);
			tessellator.addVertexWithUV(-var12, dist, var12, 1.0D, 0.0D);
			tessellator.draw();

		}
		GL11.glPopMatrix();
	}


	
	private void renderAtmosphereGlow(Tessellator tessellator, Minecraft mc, CelestialBody body, double size, float visibility, double phase) {
		float glowAlpha = AtmosphereRenderUtil.getAtmosphereGlowAlpha(body) * visibility;
		if(glowAlpha <= 0.001F) {
			return;
		}

		// until I can figure out how to make a four way quad lerp not look like shit I'm disabling this
		float leadingGlow = glowAlpha;// * MathHelper.clamp_float((float)Math.abs(0.5 - phase) * 2, 0, 1);
		float trailingGlow = glowAlpha;// * MathHelper.clamp_float((float)Math.abs(-0.5 - phase) * 2, 0, 1);

		Vec3 atmo = AtmosphereRenderUtil.getBodyAtmosphereColor(body);
		float r = MathHelper.clamp_float((float) atmo.xCoord * 1.15F, 0.0F, 1.0F);
		float g = MathHelper.clamp_float((float) atmo.yCoord * 1.15F, 0.0F, 1.0F);
		float b = MathHelper.clamp_float((float) atmo.zCoord * 1.15F, 0.0F, 1.0F);

		// non-linear gradient stepping
		double innerSize = size * 0.98D;
		double middleSize = size * 1.075D;
		double outerSize = size * 1.15D * (1.0D + glowAlpha * 0.25D);

		GL11.glEnable(GL11.GL_BLEND);
		OpenGlHelper.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO);
		GL11.glDisable(GL11.GL_TEXTURE_2D);
		GL11.glDisable(GL11.GL_CULL_FACE);
		GL11.glShadeModel(GL11.GL_SMOOTH);

		tessellator.startDrawingQuads();

		// Top band
		tessellator.setColorRGBA_F(r, g, b, 0.0F);
		tessellator.addVertex(-outerSize, 100.0D, -outerSize);
		tessellator.addVertex(outerSize, 100.0D, -outerSize);
		tessellator.setColorRGBA_F(r, g, b, leadingGlow / 2);
		tessellator.addVertex(middleSize, 100.0D, -middleSize);
		tessellator.setColorRGBA_F(r, g, b, trailingGlow / 2);
		tessellator.addVertex(-middleSize, 100.0D, -middleSize);

		tessellator.addVertex(-middleSize, 100.0D, -middleSize);
		tessellator.setColorRGBA_F(r, g, b, leadingGlow / 2);
		tessellator.addVertex(middleSize, 100.0D, -middleSize);
		tessellator.setColorRGBA_F(r, g, b, leadingGlow);
		tessellator.addVertex(innerSize, 100.0D, -innerSize);
		tessellator.setColorRGBA_F(r, g, b, trailingGlow);
		tessellator.addVertex(-innerSize, 100.0D, -innerSize);

		// Left band
		tessellator.setColorRGBA_F(r, g, b, 0.0F);
		tessellator.addVertex(outerSize, 100.0D, -outerSize);
		tessellator.addVertex(outerSize, 100.0D, outerSize);
		tessellator.setColorRGBA_F(r, g, b, leadingGlow / 2);
		tessellator.addVertex(middleSize, 100.0D, middleSize);
		tessellator.addVertex(middleSize, 100.0D, -middleSize);

		tessellator.addVertex(middleSize, 100.0D, -middleSize);
		tessellator.addVertex(middleSize, 100.0D, middleSize);
		tessellator.setColorRGBA_F(r, g, b, leadingGlow);
		tessellator.addVertex(innerSize, 100.0D, innerSize);
		tessellator.addVertex(innerSize, 100.0D, -innerSize);

		// Bottom band
		tessellator.setColorRGBA_F(r, g, b, 0.0F);
		tessellator.addVertex(outerSize, 100.0D, outerSize);
		tessellator.addVertex(-outerSize, 100.0D, outerSize);
		tessellator.setColorRGBA_F(r, g, b, trailingGlow / 2);
		tessellator.addVertex(-middleSize, 100.0D, middleSize);
		tessellator.setColorRGBA_F(r, g, b, leadingGlow / 2);
		tessellator.addVertex(middleSize, 100.0D, middleSize);

		tessellator.addVertex(middleSize, 100.0D, middleSize);
		tessellator.setColorRGBA_F(r, g, b, trailingGlow / 2);
		tessellator.addVertex(-middleSize, 100.0D, middleSize);
		tessellator.setColorRGBA_F(r, g, b, trailingGlow);
		tessellator.addVertex(-innerSize, 100.0D, innerSize);
		tessellator.setColorRGBA_F(r, g, b, leadingGlow);
		tessellator.addVertex(innerSize, 100.0D, innerSize);

		// Right band
		tessellator.setColorRGBA_F(r, g, b, 0.0F);
		tessellator.addVertex(-outerSize, 100.0D, outerSize);
		tessellator.addVertex(-outerSize, 100.0D, -outerSize);
		tessellator.setColorRGBA_F(r, g, b, trailingGlow / 2);
		tessellator.addVertex(-middleSize, 100.0D, -middleSize);
		tessellator.addVertex(-middleSize, 100.0D, middleSize);

		tessellator.addVertex(-middleSize, 100.0D, middleSize);
		tessellator.addVertex(-middleSize, 100.0D, -middleSize);
		tessellator.setColorRGBA_F(r, g, b, trailingGlow);
		tessellator.addVertex(-innerSize, 100.0D, -innerSize);
		tessellator.addVertex(-innerSize, 100.0D, innerSize);

		tessellator.draw();

		GL11.glShadeModel(GL11.GL_FLAT);
		GL11.glEnable(GL11.GL_CULL_FACE);
		GL11.glEnable(GL11.GL_TEXTURE_2D);
		OpenGlHelper.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO);
	}

	protected void renderRings(float partialTicks, WorldClient world, Minecraft mc, float ringTilt, float[] ringColor, float ringSize, float visibility) {
		Tessellator tessellator = Tessellator.instance;

		GL11.glColor4f(ringColor[0], ringColor[1], ringColor[2], visibility);
		mc.renderEngine.bindTexture(ringTexture);

		double offset = -20.0D;

		tessellator.startDrawingQuads();
		tessellator.addVertexWithUV(offset, -ringSize, -ringSize, 0.0D, 0.0D);
		tessellator.addVertexWithUV(offset, ringSize, -ringSize, 1.0D, 0.0D);
		tessellator.addVertexWithUV(offset, ringSize, ringSize, 1.0D, 1.0D);
		tessellator.addVertexWithUV(offset, -ringSize, ringSize, 0.0D, 1.0D);
		tessellator.draw();
	}

	// Does anyone even play with 3D glasses anymore?
	protected float[] applyAnaglyph(float... colors) {
		float r = (colors[0] * 30.0F + colors[1] * 59.0F + colors[2] * 11.0F) / 100.0F;
		float g = (colors[0] * 30.0F + colors[1] * 70.0F) / 100.0F;
		float b = (colors[0] * 30.0F + colors[2] * 70.0F) / 100.0F;

		return new float[]{r, g, b};
	}

	// is just drawing a big cube with UVs prepared to draw a gradient
	private void renderSkyboxSide(Tessellator tessellator, int side) {
		double u = side % 3 / 3.0D;
		double v = side / 3 / 2.0D;
		tessellator.startDrawingQuads();
		tessellator.addVertexWithUV(-100.0D, -100.0D, -100.0D, u, v);
		tessellator.addVertexWithUV(-100.0D, -100.0D, 100.0D, u, v + 0.5D);
		tessellator.addVertexWithUV(100.0D, -100.0D, 100.0D, u + 0.3333333333333333D, v + 0.5D);
		tessellator.addVertexWithUV(100.0D, -100.0D, -100.0D, u + 0.3333333333333333D, v);
		tessellator.draw();
	}

	protected void renderSpecialEffects(float partialTicks, WorldClient world, Minecraft mc) {

	}

	protected void render3DModel(float partialTicks, WorldClient world, Minecraft mc) {

	}

	protected void renderHeldSatellitePreview(float partialTicks, WorldClient world, Minecraft mc, float solarAngle) {
		ItemStack held = mc.thePlayer.getHeldItem();
		if(held == null || !Satellite.isSatelliteItem(held.getItem())) return;
		int currentBodyDimensionId = CelestialBody.getTarget(world, (int) mc.thePlayer.posX, (int) mc.thePlayer.posZ).body.dimensionId;
		if(Satellite.getTargetDimensionId(held, currentBodyDimensionId) != currentBodyDimensionId) return;

		float r = Satellite.getColorR(held);
		float g = Satellite.getColorG(held);
		float b = Satellite.getColorB(held);
		float inclination = Satellite.getInclination(held);
		float altitude = Satellite.getAltitude(held);
		float phaseOffset = Satellite.getPhaseOffset(held);
		boolean isBlinking = Satellite.isBlinking(held);
		float blinkPeriod = Satellite.getBlinkPeriod(held);

		Satellite.renderOrbitLine(solarAngle, r, g, b, inclination, altitude, isBlinking, blinkPeriod);
		Satellite.renderDefault(partialTicks, world, mc, solarAngle, ISatChip.getFreqS(held), r, g, b, inclination, altitude, phaseOffset, isBlinking, blinkPeriod);
	}

	protected void renderStation(float partialTicks, WorldClient world, Minecraft mc, OrbitalStation station, float solarAngle) {
		Tessellator tessellator = Tessellator.instance;

		long seed = station.dX * 1024L + station.dZ;

		double ticks = (double) (System.currentTimeMillis() % (1600 * 50)) / 50;

		GL11.glPushMatrix();
		{

			GL11.glRotatef(solarAngle * -360.0F, 1.0F, 0.0F, 0.0F);
			GL11.glRotatef(-40.0F + (float) (seed % 800) * 0.1F - 5.0F, 1.0F, 0.0F, 0.0F);
			GL11.glRotatef((float) (seed % 50) * 0.1F - 20.0F, 0.0F, 1.0F, 0.0F);
			GL11.glRotatef((float) (seed % 80) * 0.1F - 2.5F, 0.0F, 0.0F, 1.0F);
			GL11.glRotated((ticks / 1600.0D) * -360.0D, 1.0F, 0.0F, 0.0F);

			GL11.glColor4f(0.8F, 1, 1, 1);

			mc.renderEngine.bindTexture(stationTexture);

			float size = 0.8F;

			tessellator.startDrawingQuads();
			tessellator.addVertexWithUV(-size, 100.0, -size, 0.0D, 0.0D);
			tessellator.addVertexWithUV(size, 100.0, -size, 0.0D, 1.0D);
			tessellator.addVertexWithUV(size, 100.0, size, 1.0D, 1.0D);
			tessellator.addVertexWithUV(-size, 100.0, size, 1.0D, 0.0D);
			tessellator.draw();

		}
		GL11.glPopMatrix();
	}

	public void renderSmoke(long age) {
		GL11.glPushMatrix();
		GL11.glEnable(GL11.GL_BLEND);

		float f4 = 1.0F;
		float f5 = 0.5F;
		float f6 = 0.25F;
		float dark = 1f - Math.min(((float) (age) / (100f * 0.35F)), 1f);

		GL11.glRotatef(180.0F - RenderManager.instance.playerViewY, 0.0F, 1.0F, 0.0F);
		GL11.glRotatef(-RenderManager.instance.playerViewX, 1.0F, 0.0F, 0.0F);
		GL11.glColor4f(0.6F * dark + 0.0F, 0.6F * dark + 0.0F, dark + 0.0F, 1F);

		Tessellator tess = Tessellator.instance;
		tess.startDrawingQuads();
		tess.setNormal(0.0F, 1.0F, 0.0F);
		tess.addVertexWithUV(0.0F - f5, 0.0F - f6, 0.0D, 1, 0);
		tess.addVertexWithUV(f4 - f5, 0.0F - f6, 0.0D, 0, 0);
		tess.addVertexWithUV(f4 - f5, f4 - f6, 0.0D, 0, 1);
		tess.addVertexWithUV(0.0F - f5, f4 - f6, 0.0D, 1, 1);
		tess.draw();

		GL11.glDisable(GL11.GL_BLEND);
		GL11.glPopMatrix();
	}

	public void renderGlow(double x, double y, double z, float rainStrength) {
		GL11.glPushMatrix();
		GL11.glEnable(GL11.GL_BLEND);

		float f4 = 1.0F;
		float f5 = 0.5F;
		float f6 = 0.25F;
		double near = 0.51d * (Math.min(40000f, Math.max(0d, y - 35000d)) / 40000d);
		double entry = near * (1d - rainStrength) + (1d - (Math.min(200d, Math.max(0d, x - 2017d)) / 200f));

		GL11.glRotatef(180.0F - RenderManager.instance.playerViewY, 0.0F, 1.0F, 0.0F);
		GL11.glRotatef(-RenderManager.instance.playerViewX, 1.0F, 0.0F, 0.0F);
		GL11.glColor4d(entry, entry, entry, entry);

		Tessellator tess = Tessellator.instance;
		tess.startDrawingQuads();
		tess.setNormal(0.0F, 1.0F, 0.0F);
		tess.addVertexWithUV(0.0F - f5, 0.0F - f6, 0.0D, 1, 0);
		tess.addVertexWithUV(f4 - f5, 0.0F - f6, 0.0D, 0, 0);
		tess.addVertexWithUV(f4 - f5, f4 - f6, 0.0D, 0, 1);
		tess.addVertexWithUV(0.0F - f5, f4 - f6, 0.0D, 1, 1);
		tess.draw();

		GL11.glDisable(GL11.GL_BLEND);
		GL11.glPopMatrix();
	}


	
	public void RenderIt(WorldClient world, float partialTicks, double shaderSize, Minecraft mc, CelestialBody sun) {
		// i dont know where to properly put this yet plus its 4 am so take that as you
		// will (SEVEN)
		CBT_DEMETER d = sun.getTrait(CBT_DEMETER.class);

		Tessellator tessellator = Tessellator.instance;
		Shader shader = angel;

		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

		shader.use();

		float time = ((float) world.getWorldTime() + partialTicks) / 20.0F;
		mc.renderEngine.bindTexture(noise);
		GL11.glPushMatrix();
		// Fix orbital plane
		GL11.glRotatef(-90.0F, 0, 1, 0);
		shader.setUniform1f("time", time + 0.2F* d.spinSpeed * 0.1F);
		shader.setUniform1f("divergence", 1);
		shader.setUniform1f("colorR", 1f);
		shader.setUniform1f("colorG", 0.1f);
		shader.setUniform1f("colorB", 0.3f);
		shader.setUniform1f("interp", d.wingPhase);

		tessellator.startDrawingQuads();
		tessellator.addVertexWithUV(-shaderSize *2, 100.0D, -shaderSize*2, 0.0D, 0.0D);
		tessellator.addVertexWithUV(shaderSize*2, 100.0D, -shaderSize*2, 1.0D, 0.0D);
		tessellator.addVertexWithUV(shaderSize*2, 100.0D, shaderSize*2, 1.0D, 1.0D);
		tessellator.addVertexWithUV(-shaderSize*2, 100.0D, shaderSize*2, 0.0D, 1.0D);
		tessellator.draw();

		shader.stop();

		GL11.glPushMatrix();
		GL11.glColor4f(1, 0.2f, 0.2f, 1);
		GL11.glEnable(GL11.GL_TEXTURE_2D);
		GL11.glRotatef(180, 0, 1, 0);
		GL11.glTranslatef(0, 0, -1);
		mc.renderEngine.bindTexture(eye);
		float size = 7;
		tessellator.startDrawingQuads();
		tessellator.addVertexWithUV(-size*2, 100.0D, -size*2, 0.0D, 0.0D);
		tessellator.addVertexWithUV(size*2, 100.0D, -size*2, 1.0D, 0.0D);
		tessellator.addVertexWithUV(size*2, 100.0D, size*2, 1.0D, 1.0D);
		tessellator.addVertexWithUV(-size*2, 100.0D, size*2, 0.0D, 1.0D);
		tessellator.draw();
		GL11.glPopMatrix();
		GL11.glDisable(GL11.GL_TEXTURE_2D);

		GL11.glPopMatrix();
		OpenGlHelper.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ONE, GL11.GL_ZERO);

	}

}
