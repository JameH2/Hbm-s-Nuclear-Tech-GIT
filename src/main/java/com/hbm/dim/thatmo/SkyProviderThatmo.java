package com.hbm.dim.thatmo;

import java.util.Random;

import org.lwjgl.opengl.GL11;

import com.hbm.dim.CelestialBody;
import com.hbm.dim.SkyProviderCelestial;
import com.hbm.dim.thatmo.WorldProviderThatmo.Meteor;
import com.hbm.dim.thatmo.WorldProviderThatmo.MeteorType;
import com.hbm.dim.trait.CelestialBodyTrait.CBT_BATTLEFIELD;
import com.hbm.lib.RefStrings;
import com.hbm.main.ResourceManager;
import com.hbm.render.util.BeamPronter;
import com.hbm.render.util.BeamPronter.EnumBeamType;
import com.hbm.render.util.BeamPronter.EnumWaveType;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Vec3;

public class SkyProviderThatmo extends SkyProviderCelestial {

	private static final ResourceLocation texture = new ResourceLocation(RefStrings.MODID + ":textures/particle/shockwave.png");
	private static final ResourceLocation thatmoShield = new ResourceLocation("hbm:textures/particle/cens.png");
	private static final ResourceLocation flash = new ResourceLocation("hbm:textures/misc/space/flare.png");
	private static final ResourceLocation particleBase = new ResourceLocation(RefStrings.MODID + ":textures/particle/particle_base.png");

	// james I appreciate you
	// no more self-flagellating comments 2025

	@Override
	public void renderSpecialEffects(float partialTicks, WorldClient world, Minecraft mc) {
		float alpha = (WorldProviderThatmo.flashd <= 0) ? 0.0F : 1.0F - Math.min(1.0F, WorldProviderThatmo.flashd / 100);

		GL11.glPushMatrix();
		float var14 = WorldProviderThatmo.flashd * 2 + partialTicks;

		GL11.glEnable(GL11.GL_BLEND);
		GL11.glRotated(180.0, 0.0, 5.0, 0.0);
		GL11.glRotated(90.0, -12.0, 7.3F, -4.0);

		mc.renderEngine.bindTexture(texture);

		GL11.glColor4f(1, 1, 1, alpha);
		Tessellator tessellator = Tessellator.instance;
		tessellator.startDrawingQuads();
		tessellator.addVertexWithUV(-var14, 100.0D, -var14, 0.0D, 0.0D);
		tessellator.addVertexWithUV(var14, 100.0D, -var14, 1.0D, 0.0D);
		tessellator.addVertexWithUV(var14, 100.0D, var14, 1.0D, 1.0D);
		tessellator.addVertexWithUV(-var14, 100.0D, var14, 0.0D, 1.0D);
		tessellator.draw();

		GL11.glPopMatrix();

		GL11.glPushMatrix();
		GL11.glRotated(180.0, 0.0, 5.0, 0.0);
		GL11.glRotated(90.0, -12.0, 7.3F, -4.0);
		var14 = var14 * 0.5F;
		mc.renderEngine.bindTexture(flash);

		GL11.glColor4f(1, 1, 1, alpha);
		tessellator.startDrawingQuads();
		tessellator.addVertexWithUV(-var14, 100.0D, -var14, 0.0D, 0.0D);
		tessellator.addVertexWithUV(var14, 100.0D, -var14, 1.0D, 0.0D);
		tessellator.addVertexWithUV(var14, 100.0D, var14, 1.0D, 1.0D);
		tessellator.addVertexWithUV(-var14, 100.0D, var14, 0.0D, 1.0D);
		tessellator.draw();
		GL11.glPopMatrix();
		Random random = new Random(42);
		float alt = WorldProviderThatmo.altitude + partialTicks;
		float rnd = WorldProviderThatmo.randPos;
		float beamscale = WorldProviderThatmo.scale;
		float shieldscale = WorldProviderThatmo.shield;
		float waveinterp = WorldProviderThatmo.nmass;
		float shieldinterp = WorldProviderThatmo.shielde;
		float shielalpha = WorldProviderThatmo.csyw + partialTicks;

		GL11.glShadeModel(GL11.GL_FLAT);

		GL11.glEnable(GL11.GL_TEXTURE_2D);

		float alphad = 1.0F - Math.min(1.0F, waveinterp / 100);
		float alpd = 1.0F - Math.min(1.0F, shielalpha / 100);

		GL11.glPushMatrix();

		GL11.glTranslated(21.5, 33, -28);
		GL11.glScaled(0 + shieldscale, 0 + shieldscale, 0 + shieldscale);
		GL11.glRotated(90.0, -10.0, -1.0, 50.0);
		GL11.glRotated(20.0, -0.0, -1.0, 1.0);

		GL11.glColor4d(1, 1, 1,  alphad);


		//GL11.glDepthMask(false);

		mc.renderEngine.bindTexture(texture);
		ResourceManager.plane.renderAll();

		GL11.glPopMatrix();
		GL11.glPushMatrix();


		GL11.glTranslated(21.5, 33, -28);

		GL11.glScaled(0 + shieldinterp, 0 + shieldinterp, 0 + shieldinterp);
		GL11.glRotated(90.0, -10.0, -1.0, 50.0);
		GL11.glRotated(20.0, -0.0, -1.0, 1.0);
		GL11.glColor4d(1, 1, 1, alpd);


		//GL11.glDepthMask(false);

		mc.renderEngine.bindTexture(thatmoShield);
		ResourceManager.plane.renderAll();

		GL11.glPopMatrix();


		double playerX = mc.thePlayer.prevPosX + (mc.thePlayer.posX - mc.thePlayer.prevPosX) * partialTicks;
		double playerY = mc.thePlayer.prevPosY + (mc.thePlayer.posY - mc.thePlayer.prevPosY) * partialTicks;
		double playerZ = mc.thePlayer.prevPosZ + (mc.thePlayer.posZ - mc.thePlayer.prevPosZ) * partialTicks;

		GL11.glDisable(GL11.GL_FOG);
		GL11.glEnable(GL11.GL_TEXTURE_2D);

		for(Meteor meteor : WorldProviderThatmo.meteors) {
			GL11.glPushMatrix();

			// optimised 3 sqrt per meteor to just 1
			Vec3 offset = Vec3.createVectorHelper(meteor.posX - playerX, meteor.posY - playerY, meteor.posZ - playerZ);
			double offsetLength = offset.lengthVector();
			double distance = Math.min(Minecraft.getMinecraft().gameSettings.renderDistanceChunks * 16, offsetLength);
			Vec3 offsetNormal = offsetLength >= 1.0E-4D ? Vec3.createVectorHelper(offset.xCoord / offsetLength, offset.yCoord / offsetLength, offset.zCoord / offsetLength) : offset;
			Vec3 renderOffset = Vec3.createVectorHelper(offsetNormal.xCoord * distance, offsetNormal.yCoord * distance, offsetNormal.zCoord * distance);

			GL11.glTranslated(renderOffset.xCoord, renderOffset.yCoord, renderOffset.zCoord);

			double descent = 2017d - meteor.posY;
			double quadratic = (-(descent * descent) + (1517 * descent)) / 41;

			float scalar = (float) (quadratic / offsetLength);
			GL11.glScaled(scalar, scalar, scalar);

			if(meteor.type == MeteorType.SMOKE) {
				GL11.glColor4d(1, 0, 0, 1);
				renderSmoke(particleBase, meteor.age);
			} else {
				GL11.glColor4d(1, 1, 1, 1);
				renderGlow(shockFlareTexture, 1, 1, 1, partialTicks);
			}

			GL11.glPopMatrix();
		}

		GL11.glDisable(GL11.GL_TEXTURE_2D);
		GL11.glEnable(GL11.GL_FOG);

		GL11.glPushMatrix();

		//god awful and shouldnt even be here. ill figure out something later im so sorry
		//another thing, this is a thatmo exclusive, when the war update rolls out there will be a better way to render a beam from a sattelite
		//help me i beg
		//:(
		GL11.glTranslated(16.5, 28.5, 100);
		GL11.glScaled(10, 10, 10);
		GL11.glRotated(-63.5, 0.0, 0.0, 1.0);
		BeamPronter.prontBeam(Vec3.createVectorHelper(0, WorldProviderThatmo.flashd * 0.5, 0), EnumWaveType.SPIRAL, EnumBeamType.SOLID, 0x202060, 0x202060, 0, 1, 0F, 6, (float)0.2 * 0.2F, alpha );
		BeamPronter.prontBeam(Vec3.createVectorHelper(0, WorldProviderThatmo.flashd * 0.5, 0), EnumWaveType.SPIRAL, EnumBeamType.SOLID, 0x202060, 0x202060, 0, 1, 0F, 6, (float)0.2 * 0.6F, alpha );
		BeamPronter.prontBeam(Vec3.createVectorHelper(0, WorldProviderThatmo.flashd * 0.5, 0), EnumWaveType.RANDOM, EnumBeamType.SOLID, 0x202060, 0x202060, (int)(world.getTotalWorldTime() / 5) % 1000, 25, 0.2F, 6, (float)0.2 * 0.1F, alpha );

		GL11.glRotated(27, 00, 80, 0);

		GL11.glColor4f(1, 1, 1, alpha);

		GL11.glPopMatrix();

		//genuinley horrific peice of shit i actually vomit looking at this


		GL11.glEnable(GL11.GL_TEXTURE_2D);
		GL11.glDepthMask(true);
		GL11.glPushMatrix();
		GL11.glTranslated(145, 75.5, -15);
		GL11.glRotated(90.0, -10.0, -1.0, 50.0);
		GL11.glRotated(20.0, -0.0, -1.0, 1.0);

		//GL11.glRotated(90.0, -0.0, 5.0, -2.0);
		GL11.glScaled(6, 6, 6);

		GL11.glColor4d(1, 1, 1, 1);
		BeamPronter.prontBeam(Vec3.createVectorHelper(0, beamscale, 0), EnumWaveType.SPIRAL, EnumBeamType.SOLID, 0xFF9000, 0xFF9000, 0, 2, 0F, 2, (float) alphad * 0.1F, 0.5F);



		GL11.glPopMatrix();
		GL11.glPushMatrix();
		GL11.glRotated(25.0 * rnd, -12.0, 5.0 + rnd, 2.0 + rnd);
		GL11.glRotated(15.0 * rnd, -11.0, 20.0 + rnd, 1.0 + rnd);

		GL11.glTranslated(50 - rnd, alt - rnd * rnd, -10 * rnd);
		for (int i = 0; i < 17; i++) {
			GL11.glTranslated( - rnd, 0 - random.nextInt(20) , rnd);
			BeamPronter.prontBeam(Vec3.createVectorHelper(0, 5, 0), EnumWaveType.SPIRAL, EnumBeamType.SOLID, 0xFF9000, 0xFF9000, 0, 1, 0F, 6, (float)0.2 * 0.2F, 0.5F);
		}

		GL11.glPopMatrix();

		GL11.glPushMatrix();
		GL11.glRotated(45.0 * rnd, -12.0, 5.0, 0.0);

		GL11.glTranslated(-40 + rnd,alt - 50 * rnd, -90 * rnd);

		for (int i = 0; i < 17; i++) {
			GL11.glTranslated( -rnd, 0 - random.nextInt(20) ,- 1 - rnd);
			BeamPronter.prontBeam(Vec3.createVectorHelper(0, 5, 0), EnumWaveType.SPIRAL, EnumBeamType.SOLID, 0xFF9000, 0xFF9000, 0, 1, 0F, 6, (float)0.2 * 0.2F, 0.5F);
		}
		GL11.glPopMatrix();

		GL11.glPushMatrix();
		GL11.glRotated(-90.0 * rnd, -12.0, 5.0, -20 + -45);

		GL11.glTranslated(-10 * rnd, rnd + alt - 110, -80 + rnd);

		for (int i = 0; i < 17; i++) {
			GL11.glTranslated( -rnd, 0 - random.nextInt(20) ,- 1 - rnd);
			BeamPronter.prontBeam(Vec3.createVectorHelper(0, 5, 0), EnumWaveType.SPIRAL, EnumBeamType.SOLID, 0xFF9000, 0xFF9000, 0, 1, 0F, 6, (float)0.2 * 0.2F, 0.5F);
		}
		GL11.glPopMatrix();

		GL11.glPushMatrix();
		GL11.glRotated(-90.0 * 0.5961033, -11.0, 5.0, -20 + -45);
		GL11.glTranslated(80 * rnd, rnd + alt - 110, 55 + rnd);

		for (int i = 0; i < 17; i++) {
			GL11.glTranslated( -rnd, 0 - random.nextInt(20) ,- 1 - rnd);
			BeamPronter.prontBeam(Vec3.createVectorHelper(0, 5, 0), EnumWaveType.SPIRAL, EnumBeamType.SOLID, 0xFF9000, 0xFF9000, 0, 1, 0F, 6, (float)0.2 * 0.2F, 0.5F);
		}
		GL11.glPopMatrix();


		GL11.glPushMatrix();
		GL11.glRotated(-25.0 - rnd, -16.0, 5.0, 0.0);
		GL11.glTranslated(-60 - rnd, alt - 150 * rnd, 20 * rnd);

		for (int i = 0; i < 17; i++) {
			GL11.glTranslated( - rnd, 0 - random.nextInt(15) , rnd);
			BeamPronter.prontBeam(Vec3.createVectorHelper(0, 5, 0), EnumWaveType.SPIRAL, EnumBeamType.SOLID,0xFF9000, 0xFF9000, 0, 1, 0F, 6, (float)0.2 * 0.2F, 0.5F);
		}
		GL11.glPopMatrix();

		GL11.glPushMatrix();
		GL11.glRotated(-65.0 - rnd, -12.0, 5.0, 0.0);

		GL11.glTranslated(50 + rnd, alt - 150 * rnd, -80 * rnd);

		for (int i = 0; i < 17; i++) {
			GL11.glTranslated( rnd, 0 - random.nextInt(15) ,- rnd);
			BeamPronter.prontBeam(Vec3.createVectorHelper(0, 5, 0), EnumWaveType.SPIRAL, EnumBeamType.SOLID, 0xFF9000, 0xFF9000, 0, 1, 0F, 6, (float)0.2 * 0.2F, 0.5F);
		}
		GL11.glPopMatrix();
		GL11.glEnable(GL11.GL_CULL_FACE);


		// the reason we don't write big messy functions is because it makes it
		// impossible to understand the current state of the GL state machine
		GL11.glDepthMask(false);
	}

	public void renderSmoke(ResourceLocation loc1, long age) {
		GL11.glPushMatrix();
		GL11.glEnable(GL11.GL_BLEND);
		float f4 = 1.0F;
		float f5 = 0.5F;
		float f6 = 0.25F;
		float dark = 1f - Math.min(((float)(age) / (float)(100f * 0.35F)), 1f);
		GL11.glRotatef(180.0F - RenderManager.instance.playerViewY, 0.0F, 1.0F, 0.0F);
		GL11.glRotatef(-RenderManager.instance.playerViewX, 1.0F, 0.0F, 0.0F);
		GL11.glColor4d(0.6*dark+0.0, 0.6*dark+0.0, 1*dark+0.0, 1);
		Tessellator tess = Tessellator.instance;
		TextureManager tex = Minecraft.getMinecraft().getTextureManager();
		tess.startDrawingQuads();
		tess.setNormal(0.0F, 1.0F, 0.0F);
		tess.addVertexWithUV(0.0F - f5, 0.0F - f6, 0.0D, 1, 0);
		tess.addVertexWithUV(f4 - f5, 0.0F - f6, 0.0D, 0, 0);
		tess.addVertexWithUV(f4 - f5, f4 - f6, 0.0D, 0, 1);
		tess.addVertexWithUV(0.0F - f5, f4 - f6, 0.0D, 1, 1);
		tex.bindTexture(loc1);
		tess.draw();
		GL11.glDisable(GL11.GL_BLEND);
		GL11.glPopMatrix();
	}

	public void renderGlow(ResourceLocation loc1, double x, double y, double z, float partialTicks) {
		GL11.glPushMatrix();
		GL11.glEnable(GL11.GL_BLEND);
		float f4 = 1.0F;
		float f5 = 0.5F;
		float f6 = 0.25F;
		GL11.glRotatef(180.0F - RenderManager.instance.playerViewY, 0.0F, 1.0F, 0.0F);
		GL11.glRotatef(-RenderManager.instance.playerViewX, 1.0F, 0.0F, 0.0F);
		// double distant = 1d-(Math.min(6300000f, Math.max(0d, y-40000f))/6300000f);
		//  double sf = 1d-(Math.min(400000f, Math.max(0d, y-350000f))/400000f);
		//
		//double near = distant*(Math.min(40000f, Math.max(0d, y-35000f))/40000f)*Math.min(1d,Minecraft.getMinecraft().thePlayer.worldObj.getStarBrightness(partialTicks)+sf);
		double near = 0.51d*(Math.min(40000f, Math.max(0d, y-35000d))/40000d);
		//  System.out.println((1d-(Math.min(200d, Math.max(0d, y-2017d))/200f)));
		double entry = near*(1d-Minecraft.getMinecraft().thePlayer.worldObj.getRainStrength(partialTicks))+(1d-(Math.min(200d, Math.max(0d, x-2017d))/200f));
		GL11.glColor4d(entry, entry, entry, entry);
		Tessellator tess = Tessellator.instance;
		TextureManager tex = Minecraft.getMinecraft().getTextureManager();
		tess.startDrawingQuads();
		tess.setNormal(0.0F, 1.0F, 0.0F);
		tess.addVertexWithUV(0.0F - f5, 0.0F - f6, 0.0D, 1, 0);
		tess.addVertexWithUV(f4 - f5, 0.0F - f6, 0.0D, 0, 0);
		tess.addVertexWithUV(f4 - f5, f4 - f6, 0.0D, 0, 1);
		tess.addVertexWithUV(0.0F - f5, f4 - f6, 0.0D, 1, 1);
		tex.bindTexture(loc1);
		tess.draw();
		GL11.glDisable(GL11.GL_BLEND);
		GL11.glPopMatrix();
	}

	@Override
	public void render3DModel(float partialTicks, WorldClient world, Minecraft mc) {
		CelestialBody body = CelestialBody.getBody(world);
		CBT_BATTLEFIELD wared = body.getTrait(CBT_BATTLEFIELD.class);
		if(wared != null) {
			GL11.glPushMatrix();
			GL11.glEnable(GL11.GL_DEPTH_TEST);
			GL11.glDisable(GL11.GL_BLEND);
			GL11.glTranslated(-35, 4.5, 100);
			GL11.glScaled(10, 10, 10);
			GL11.glRotated(180.0, 0.0, 5.0, 0.0);
			GL11.glRotated(90.0, -12.0, 5.0, 0.0);

			GL11.glDisable(GL11.GL_FOG);

			GL11.glColor4f(0, 0, 0, 1);
			GL11.glDepthRange(0.0, 1.0);

			//GL11.glDepthMask(false);

			mc.renderEngine.bindTexture(ResourceManager.sat_rail_tex);
			ResourceManager.sat_rail.renderAll();
			GL11.glClear(GL11.GL_DEPTH_BUFFER_BIT);
			GL11.glEnable(GL11.GL_BLEND);
			GL11.glPopMatrix();

		}
	}

}
