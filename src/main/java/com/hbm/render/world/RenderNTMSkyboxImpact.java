package com.hbm.render.world;

import cpw.mods.fml.client.FMLClientHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Vec3;
import net.minecraftforge.client.IRenderHandler;

import org.lwjgl.opengl.GL11;

import com.hbm.extprop.HbmLivingProps;
import com.hbm.handler.ImpactWorldHandler;
import com.hbm.lib.RefStrings;
import com.hbm.main.ModEventHandlerClient;
import com.hbm.main.ModEventHandlerClient.Meteor;
import com.hbm.main.ModEventHandlerClient.MeteorType;
import com.hbm.render.util.TomPronter;

import java.util.Random;

public class RenderNTMSkyboxImpact extends IRenderHandler {
	
	private static final ResourceLocation sunTexture = new ResourceLocation("textures/environment/sun.png");
	private static final ResourceLocation moonTexture = new ResourceLocation("textures/environment/moon_phases.png");
	private static final ResourceLocation digammaStar = new ResourceLocation("hbm:textures/misc/star_digamma.png");
	private static final ResourceLocation bobmazonSat = new ResourceLocation("hbm:textures/misc/sat_bobmazon.png");

	public static int starGLCallList;
	public static int glSkyList;
	public static int glSkyList2;

	public static boolean displayListsInitialized = false;


	protected double x;
	protected double y;
	protected double z;

	public RenderNTMSkyboxImpact() {
	    if (!displayListsInitialized) {
	        initializeDisplayLists();
	    }
	}
	/// I had to break your compat feature for other mods' skyboxes in order to
	/// make the skybox render correctly after Tom. Sorry about that. -Pu

	private void initializeDisplayLists() {
		
	    starGLCallList = GLAllocation.generateDisplayLists(3);
		GL11.glPushMatrix();
		GL11.glNewList(this.starGLCallList, GL11.GL_COMPILE);
		this.renderStars();
		GL11.glEndList();
		GL11.glPopMatrix();
		final Tessellator tessellator = Tessellator.instance;
		this.glSkyList = this.starGLCallList + 1;
		GL11.glNewList(this.glSkyList, GL11.GL_COMPILE);
		final byte byte2 = 64;
		final int i = 256 / byte2 + 2;
		float f = 16F;

		for(int j = -byte2 * i; j <= byte2 * i; j += byte2) {
			for(int l = -byte2 * i; l <= byte2 * i; l += byte2) {
				tessellator.startDrawingQuads();
				tessellator.addVertex(j + 0, f, l + 0);
				tessellator.addVertex(j + byte2, f, l + 0);
				tessellator.addVertex(j + byte2, f, l + byte2);
				tessellator.addVertex(j + 0, f, l + byte2);
				tessellator.draw();
			}
		}

		GL11.glEndList();
		this.glSkyList2 = this.starGLCallList + 2;
		GL11.glNewList(this.glSkyList2, GL11.GL_COMPILE);
		f = -16F;
		tessellator.startDrawingQuads();

		for(int k = -byte2 * i; k <= byte2 * i; k += byte2) {
			for(int i1 = -byte2 * i; i1 <= byte2 * i; i1 += byte2) {
				tessellator.addVertex(k + byte2, f, i1 + 0);
				tessellator.addVertex(k + 0, f, i1 + 0);
				tessellator.addVertex(k + 0, f, i1 + byte2);
				tessellator.addVertex(k + byte2, f, i1 + byte2);
			}
		}

		tessellator.draw();
		GL11.glEndList();
		displayListsInitialized = true;
	}

	@Override
	public void render(float partialTicks, WorldClient world, Minecraft mc) {
		float atmosphericDust = ImpactWorldHandler.getDustForClient(world);
		long time = ImpactWorldHandler.getTimeForClient(world);
		GL11.glDisable(GL11.GL_TEXTURE_2D);
		Vec3 vec3 = world.getSkyColor(mc.renderViewEntity, partialTicks);
		float f1 = (float) vec3.xCoord;
		float f2 = (float) vec3.yCoord;
		float f3 = (float) vec3.zCoord;
		float f6;
		float dust = Math.max((1.0F - (atmosphericDust * 4)), 0);
		float rain = dust * (1.0F - world.getRainStrength(partialTicks));

		if(mc.gameSettings.anaglyph) {
			float f4 = (f1 * 30.0F + f2 * 59.0F + f3 * 11.0F) / 100.0F;
			float f5 = (f1 * 30.0F + f2 * 70.0F) / 100.0F;
			f6 = (f1 * 30.0F + f3 * 70.0F) / 100.0F;
			f1 = f4;
			f2 = f5;
			f3 = f6;
		}

		GL11.glColor3f(f1, f2, f3);
		Tessellator tessellator = Tessellator.instance;
		GL11.glDepthMask(false);
		GL11.glEnable(GL11.GL_FOG);
		GL11.glColor3f(f1, f2, f3);
		GL11.glCallList(this.glSkyList);
		GL11.glDisable(GL11.GL_FOG);
		GL11.glDisable(GL11.GL_ALPHA_TEST);
		GL11.glEnable(GL11.GL_BLEND);
		OpenGlHelper.glBlendFunc(770, 771, 1, 0);
		RenderHelper.disableStandardItemLighting();
		float f7;
		float f8;
		float f9;
		float f10;
		float f18 = world.getStarBrightness(partialTicks);

		if(f18 > 0.0F) {
			GL11.glPushMatrix();
			GL11.glRotatef(-90.0F, 0.0F, 1.0F, 0.0F);
			GL11.glRotatef(world.getCelestialAngle(partialTicks) * 360.0F, 1.0F, 0.0F, 0.0F);
			GL11.glRotatef(-19.0F, 0, 1.0F, 0);
			GL11.glColor4f(f18, f18, f18, f18 * rain);
			GL11.glCallList(this.starGLCallList);
			GL11.glPopMatrix();
		}

		GL11.glShadeModel(GL11.GL_FLAT);

		GL11.glEnable(GL11.GL_TEXTURE_2D);
		OpenGlHelper.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ONE, GL11.GL_ZERO);
		GL11.glPushMatrix();
		f7 = 0.0F;
		f8 = 0.0F;
		f9 = 0.0F;
		GL11.glTranslatef(f7, f8, f9);
		GL11.glRotatef(-90.0F, 0.0F, 1.0F, 0.0F);
		GL11.glRotatef(world.getCelestialAngle(partialTicks) * 360.0F, 1.0F, 0.0F, 0.0F);

		// Render sun
		GL11.glDisable(GL11.GL_TEXTURE_2D);
		GL11.glColor4f(0.0F, 0.0F, 0.0F, 1.0F);
		// Some blanking to conceal the stars
		f10 = 30.0F;
		tessellator.startDrawingQuads();
		tessellator.addVertex(-f10, 99.9D, -f10);
		tessellator.addVertex(f10, 99.9D, -f10);
		tessellator.addVertex(f10, 99.9D, f10);
		tessellator.addVertex(-f10, 99.9D, f10);
		tessellator.draw();
		{
			GL11.glEnable(GL11.GL_TEXTURE_2D);
			GL11.glColor4f(1.0F, 1.0F, 1.0F, rain);
			mc.renderEngine.bindTexture(this.sunTexture);
			tessellator.startDrawingQuads();
			tessellator.addVertexWithUV(-f10, 100.0D, -f10, 0.0D, 0.0D);
			tessellator.addVertexWithUV(f10, 100.0D, -f10, 1.0D, 0.0D);
			tessellator.addVertexWithUV(f10, 100.0D, f10, 1.0D, 1.0D);
			tessellator.addVertexWithUV(-f10, 100.0D, f10, 0.0D, 1.0D);
			tessellator.draw();
		}
		{
			GL11.glColor4d(1, 1, 1, rain);
			f10 = 20.0F;
			FMLClientHandler.instance().getClient().renderEngine.bindTexture(RenderNTMSkyboxImpact.moonTexture);
			float sinphi = FMLClientHandler.instance().getClient().theWorld.getMoonPhase();
			final int cosphi = (int) (sinphi % 4);
			final int var29 = (int) (sinphi / 4 % 2);
			final float yy = (cosphi + 0) / 4.0F;
			final float rand7 = (var29 + 0) / 2.0F;
			final float zz = (cosphi + 1) / 4.0F;
			final float rand9 = (var29 + 1) / 2.0F;
			tessellator.startDrawingQuads();
			tessellator.addVertexWithUV(-f10, -100.0D, f10, zz, rand9);
			tessellator.addVertexWithUV(f10, -100.0D, f10, yy, rand9);
			tessellator.addVertexWithUV(f10, -100.0D, -f10, yy, rand7);
			tessellator.addVertexWithUV(-f10, -100.0D, -f10, zz, rand7);
			tessellator.draw();
		}
		{
			OpenGlHelper.glBlendFunc(770, 1, 1, 0);

			float brightness = (float) Math.sin(world.getCelestialAngle(partialTicks) * Math.PI);
			brightness *= brightness;

			GL11.glPushMatrix();
			GL11.glColor4f(brightness, brightness, brightness, dust);
			GL11.glRotatef(-90.0F, 0.0F, 1.0F, 0.0F);
			GL11.glRotatef(world.getCelestialAngle(partialTicks) * 360.0F, 1.0F, 0.0F, 0.0F);
			GL11.glRotatef(140.0F, 1.0F, 0.0F, 0.0F);
			GL11.glRotatef(-40.0F, 0.0F, 0.0F, 1.0F);

			FMLClientHandler.instance().getClient().renderEngine.bindTexture(digammaStar);

			float digamma = HbmLivingProps.getDigamma(Minecraft.getMinecraft().thePlayer);
			float var12 = 1F * (1 + digamma * 0.25F);
			double dist = 100D - digamma * 2.5;

			tessellator.startDrawingQuads();
			tessellator.addVertexWithUV(-var12, dist, -var12, 0.0D, 0.0D);
			tessellator.addVertexWithUV(var12, dist, -var12, 0.0D, 1.0D);
			tessellator.addVertexWithUV(var12, dist, var12, 1.0D, 1.0D);
			tessellator.addVertexWithUV(-var12, dist, var12, 1.0D, 0.0D);
			tessellator.draw();
			GL11.glPopMatrix();

			if(!ImpactWorldHandler.getImpactForClient(world))
			{
				GL11.glPushMatrix();
				GL11.glColor4f(brightness, brightness, brightness, rain);
				GL11.glRotatef(-40.0F, 1.0F, 0.0F, 0.0F);
				GL11.glRotatef((System.currentTimeMillis() % (360 * 1000) / 1000F), 0.0F, 1.0F, 0.0F);
				GL11.glRotatef((System.currentTimeMillis() % (360 * 100) / 100F), 1.0F, 0.0F, 0.0F);

				FMLClientHandler.instance().getClient().renderEngine.bindTexture(bobmazonSat);

				var12 = 0.5F;
				dist = 100D;

				tessellator.startDrawingQuads();
				tessellator.addVertexWithUV(-var12, dist, -var12, 0.0D, 0.0D);
				tessellator.addVertexWithUV(var12, dist, -var12, 0.0D, 1.0D);
				tessellator.addVertexWithUV(var12, dist, var12, 1.0D, 1.0D);
				tessellator.addVertexWithUV(-var12, dist, var12, 1.0D, 0.0D);
				tessellator.draw();
				GL11.glPopMatrix();
			}
		}
		GL11.glDisable(GL11.GL_TEXTURE_2D);

		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		GL11.glDisable(GL11.GL_BLEND);
		GL11.glEnable(GL11.GL_ALPHA_TEST);
		GL11.glEnable(GL11.GL_FOG);
		GL11.glPopMatrix();
		if(time>0)
		{
			GL11.glPushMatrix();
			GL11.glDisable(GL11.GL_FOG);
			GL11.glColor3f(1.0F, 1.0F, 1.0F);
			GL11.glEnable(GL11.GL_TEXTURE_2D);
			renderAsteroid(partialTicks);
			GL11.glDisable(GL11.GL_TEXTURE_2D);
			GL11.glEnable(GL11.GL_FOG);
			GL11.glPopMatrix();
		}
		for(Meteor meteor : ModEventHandlerClient.meteors) {
				GL11.glPushMatrix();
				GL11.glDisable(GL11.GL_FOG);
				GL11.glEnable(GL11.GL_TEXTURE_2D);
				double dx = mc.thePlayer.prevPosX + (mc.thePlayer.posX - mc.thePlayer.prevPosX) * partialTicks;
				double dy = mc.thePlayer.prevPosY + (mc.thePlayer.posY - mc.thePlayer.prevPosY) * partialTicks;
				double dz = mc.thePlayer.prevPosZ + (mc.thePlayer.posZ - mc.thePlayer.prevPosZ) * partialTicks;
				Vec3 vec = Vec3.createVectorHelper(meteor.posX - dx, meteor.posY - dy, meteor.posZ - dz);
				Vec3 vec2 = Vec3.createVectorHelper(meteor.posX - dx, meteor.posY - dy, meteor.posZ - dz);
				double l = Math.min(Minecraft.getMinecraft().gameSettings.renderDistanceChunks*16, vec.lengthVector());
				double sf = Math.max(0.2,(312.5/(vec2.lengthVector()/l)));
				vec = vec.normalize();
				Vec3 vecd = Vec3.createVectorHelper(vec.xCoord*l, vec.yCoord*l, vec.zCoord*l);
				GL11.glTranslated( vecd.xCoord, vecd.yCoord , vecd.zCoord);
				double descent = 2017d-meteor.posY;
				double quadratic = (-1*Math.pow(descent, 2)+(1517*descent))/41;
				//float scalar = (float) (7000f/vec2.lengthVector()); 
				float scalar = (float) (quadratic/vec2.lengthVector()); 
				GL11.glScaled(scalar, scalar, scalar);
					//System.out.println("scalar "+scalar);
				renderGlow(new ResourceLocation(RefStrings.MODID + ":textures/particle/flare.png"), 1, 1, 1, partialTicks);
				GL11.glDisable(GL11.GL_TEXTURE_2D);
				GL11.glEnable(GL11.GL_FOG);
				GL11.glPopMatrix();
		}
		for(Meteor fragment : ModEventHandlerClient.fragments) {
			GL11.glPushMatrix();
			GL11.glDisable(GL11.GL_FOG);
			GL11.glEnable(GL11.GL_TEXTURE_2D);
			double dx = mc.thePlayer.prevPosX + (mc.thePlayer.posX - mc.thePlayer.prevPosX) * partialTicks;
			double dy = mc.thePlayer.prevPosY + (mc.thePlayer.posY - mc.thePlayer.prevPosY) * partialTicks;
			double dz = mc.thePlayer.prevPosZ + (mc.thePlayer.posZ - mc.thePlayer.prevPosZ) * partialTicks;
			Vec3 vec = Vec3.createVectorHelper(fragment.posX - dx, fragment.posY - dy, fragment.posZ - dz);
			Vec3 vec2 = Vec3.createVectorHelper(fragment.posX - dx, fragment.posY - dy, fragment.posZ - dz);
			double l = Math.min(Minecraft.getMinecraft().gameSettings.renderDistanceChunks*16, vec.lengthVector());
			double sf = Math.max(0.2,(312.5/(vec2.lengthVector()/l)));
			vec = vec.normalize();
			Vec3 vecd = Vec3.createVectorHelper(vec.xCoord*l, vec.yCoord*l, vec.zCoord*l);
			GL11.glTranslated( vecd.xCoord, vecd.yCoord , vecd.zCoord);
			double descent = 2017d-fragment.posY;
			double quadratic = (-1*Math.pow(descent, 2)+(1517*descent))/82;
			//float scalar = (float) (7000f/vec2.lengthVector()); 
			float scalar = (float) (quadratic/vec2.lengthVector()); 
				GL11.glScaled(scalar, scalar, scalar);
				//System.out.println("scalar "+scalar);
				renderGlow(new ResourceLocation(RefStrings.MODID + ":textures/particle/flare.png"), 1, 1, 1, partialTicks);
			GL11.glDisable(GL11.GL_TEXTURE_2D);
			GL11.glEnable(GL11.GL_FOG);
			GL11.glPopMatrix();
	}
		for(Meteor smoke : ModEventHandlerClient.smoke) {
			GL11.glPushMatrix();
			GL11.glDisable(GL11.GL_FOG);
			GL11.glEnable(GL11.GL_TEXTURE_2D);
			double dx = mc.thePlayer.prevPosX + (mc.thePlayer.posX - mc.thePlayer.prevPosX) * partialTicks;
			double dy = mc.thePlayer.prevPosY + (mc.thePlayer.posY - mc.thePlayer.prevPosY) * partialTicks;
			double dz = mc.thePlayer.prevPosZ + (mc.thePlayer.posZ - mc.thePlayer.prevPosZ) * partialTicks;
			Vec3 vec = Vec3.createVectorHelper(smoke.posX - dx, smoke.posY - dy, smoke.posZ - dz);
			Vec3 vec2 = Vec3.createVectorHelper(smoke.posX - dx, smoke.posY - dy, smoke.posZ - dz);
			double l = Math.min(Minecraft.getMinecraft().gameSettings.renderDistanceChunks*16, vec.lengthVector());
			double sf = Math.max(0.2,(312.5/(vec2.lengthVector()/l)));
			vec = vec.normalize();
			Vec3 vecd = Vec3.createVectorHelper(vec.xCoord*l, vec.yCoord*l, vec.zCoord*l);
			GL11.glTranslated( vecd.xCoord, vecd.yCoord , vecd.zCoord);
			double descent = 2017d-smoke.posY;
			double quadratic = (-1*Math.pow(descent, 2)+(1517*descent))/82;
			//float scalar = (float) (14000f/vec2.lengthVector()); 
			float scalar = (float) (quadratic/vec2.lengthVector()); 
				//scalar = 3500;
			GL11.glColor4d(1, 0, 0, 1);
				GL11.glScaled(scalar, scalar, scalar);
				renderSmoke(new ResourceLocation(RefStrings.MODID + ":textures/particle/particle_base.png"), smoke.age);
			GL11.glDisable(GL11.GL_TEXTURE_2D);
			GL11.glEnable(GL11.GL_FOG);
			GL11.glPopMatrix();
	}
		GL11.glDisable(GL11.GL_TEXTURE_2D);
		GL11.glColor3f(0.0F, 0.0F, 0.0F);
		double d0 = mc.thePlayer.getPosition(partialTicks).yCoord - world.getHorizon();

		if(d0 < 0.0D) {
			GL11.glPushMatrix();
			GL11.glTranslatef(0.0F, 12.0F, 0.0F);
			GL11.glCallList(this.glSkyList2);
			GL11.glPopMatrix();
			f8 = 1.0F;
			f9 = -((float) (d0 + 65.0D));
			f10 = -f8;
			tessellator.startDrawingQuads();
			tessellator.setColorRGBA_I(0, 255);
			tessellator.addVertex(-f8, f9, f8);
			tessellator.addVertex(f8, f9, f8);
			tessellator.addVertex(f8, f10, f8);
			tessellator.addVertex(-f8, f10, f8);
			tessellator.addVertex(-f8, f10, -f8);
			tessellator.addVertex(f8, f10, -f8);
			tessellator.addVertex(f8, f9, -f8);
			tessellator.addVertex(-f8, f9, -f8);
			tessellator.addVertex(f8, f10, -f8);
			tessellator.addVertex(f8, f10, f8);
			tessellator.addVertex(f8, f9, f8);
			tessellator.addVertex(f8, f9, -f8);
			tessellator.addVertex(-f8, f9, -f8);
			tessellator.addVertex(-f8, f9, f8);
			tessellator.addVertex(-f8, f10, f8);
			tessellator.addVertex(-f8, f10, -f8);
			tessellator.addVertex(-f8, f10, -f8);
			tessellator.addVertex(-f8, f10, f8);
			tessellator.addVertex(f8, f10, f8);
			tessellator.addVertex(f8, f10, -f8);
			tessellator.draw();
		}

		if(world.provider.isSkyColored()) {
			GL11.glColor3f(f1 * 0.2F + 0.04F, f2 * 0.2F + 0.04F, f3 * 0.6F + 0.1F);
		} else {
			GL11.glColor3f(f1, f2, f3);
		}
		GL11.glPushMatrix();
		GL11.glTranslatef(0.0F, -((float) (d0 - 16.0D)), 0.0F);
		GL11.glCallList(this.glSkyList2);
		GL11.glPopMatrix();
		GL11.glEnable(GL11.GL_TEXTURE_2D);
		GL11.glDepthMask(true);

	}

	private void renderStars() {
		Random random = new Random(10842L);
		Tessellator tessellator = Tessellator.instance;
		tessellator.startDrawingQuads();

		for(int i = 0; i < 1500; ++i) {
			double d0 = (double) (random.nextFloat() * 2.0F - 1.0F);
			double d1 = (double) (random.nextFloat() * 2.0F - 1.0F);
			double d2 = (double) (random.nextFloat() * 2.0F - 1.0F);
			double d3 = (double) (0.15F + random.nextFloat() * 0.1F);
			double d4 = d0 * d0 + d1 * d1 + d2 * d2;

			if(d4 < 1.0D && d4 > 0.01D) {
				d4 = 1.0D / Math.sqrt(d4);
				d0 *= d4;
				d1 *= d4;
				d2 *= d4;
				double d5 = d0 * 100.0D;
				double d6 = d1 * 100.0D;
				double d7 = d2 * 100.0D;
				double d8 = Math.atan2(d0, d2);
				double d9 = Math.sin(d8);
				double d10 = Math.cos(d8);
				double d11 = Math.atan2(Math.sqrt(d0 * d0 + d2 * d2), d1);
				double d12 = Math.sin(d11);
				double d13 = Math.cos(d11);
				double d14 = random.nextDouble() * Math.PI * 2.0D;
				double d15 = Math.sin(d14);
				double d16 = Math.cos(d14);

				for(int j = 0; j < 4; ++j) {
					double d17 = 0.0D;
					double d18 = (double) ((j & 2) - 1) * d3;
					double d19 = (double) ((j + 1 & 2) - 1) * d3;
					double d20 = d18 * d16 - d19 * d15;
					double d21 = d19 * d16 + d18 * d15;
					double d22 = d20 * d12 + d17 * d13;
					double d23 = d17 * d12 - d20 * d13;
					double d24 = d23 * d9 - d21 * d10;
					double d25 = d21 * d9 + d23 * d10;
					tessellator.addVertex(d5 + d24, d6 + d22, d7 + d25);
				}
			}
		}
		tessellator.draw();
	}
	
	//ASTEROID
	  private void renderAsteroid(float partialTicks)
	  {				
			GL11.glPushMatrix();
			EntityPlayer player = Minecraft.getMinecraft().thePlayer;
			double T = player.worldObj.getWorldTime()+3460;
			long t = ImpactWorldHandler.getTimeForClient(player.worldObj);
			double dx = player.prevPosX + (player.posX - player.prevPosX) * partialTicks;
			double dy = player.prevPosY + (player.posY - player.prevPosY) * partialTicks;
			double dz = player.prevPosZ + (player.posZ - player.prevPosZ) * partialTicks;

			//int dist = 6;
			double P = 24000;
			double R = t*37.5;
			float x = (float) (ImpactWorldHandler.x+0.5+R);//(R*Math.cos((2*Math.PI*T)/P)));
			float y = (float) (R/1.5f);//*Math.sin((2*Math.PI*T)/P));
			float z = (float)(ImpactWorldHandler.z+0.5);
			if(t<=6 && t>0 && System.currentTimeMillis() - ModEventHandlerClient.flashTimestamp > 1_000)
			{
				ModEventHandlerClient.flashTimestamp = System.currentTimeMillis();
				ModEventHandlerClient.asteroidflashDuration = 15_000;
			}
			Vec3 vec = Vec3.createVectorHelper(x - dx, y - dy, z - dz);
			Vec3 vec2 = Vec3.createVectorHelper(x - dx, y - dy, z - dz);
			double l = Math.min(Minecraft.getMinecraft().gameSettings.renderDistanceChunks*16, vec.lengthVector());
			vec = vec.normalize();
			Vec3 vec3 = Vec3.createVectorHelper(vec.xCoord*l, vec.yCoord*l, vec.zCoord*l);

			double sf = Math.max(0.2,(312.5/(vec2.lengthVector()/l)));//(2*Math.atan(1/(2*vec2.lengthVector())));//*17.2958);
			//System.out.println("sf: "+sf);
			if(vec2.lengthVector()>Minecraft.getMinecraft().gameSettings.renderDistanceChunks*6) {

				GL11.glTranslated(vec3.xCoord, vec3.yCoord, vec3.zCoord);
				GL11.glPushMatrix();
				RenderHelper.enableStandardItemLighting();

				GL11.glRotated(80, 0, 0, 1);
				GL11.glRotated(30, 0, 1, 0);

				double sine = Math.sin(System.currentTimeMillis() * 0.0005) * 5;
				double sin3 = Math.sin(System.currentTimeMillis() * 0.0005 + Math.PI * 0.5) * 5;
				GL11.glRotated(sine, 0, 0, 1);
				GL11.glRotated(sin3, 1, 0, 0);

				GL11.glTranslated(0, -3, 0);
				OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 6500F, 30F);
				GL11.glPopMatrix();

				GL11.glDisable(GL11.GL_CULL_FACE);
				GL11.glDisable(GL11.GL_LIGHTING);
				GL11.glScaled(sf, sf, sf);
				GL11.glPushMatrix();
				GL11.glRotatef(-55, 0.0F, 0.0F, 1.0F);
				//renderBlock(new ResourceLocation(RefStrings.MODID + ":textures/blocks/block_meteor_broken.png"), 0, y, 0);
				GL11.glPushMatrix();
				GL11.glPushMatrix();
				float scalar = (float) (16000f/Math.min(60000, vec2.lengthVector())); 
				//System.out.println(vec2.lengthVector());
				GL11.glScaled(scalar, scalar, scalar);
				GL11.glRotatef(55, 0.0F, 0.0F, 1.0F);
				GL11.glTranslated(0, -1/4f/*(sf*0.768)*/, 0);
				renderGlow(new ResourceLocation(RefStrings.MODID + ":textures/particle/flare.png"), y, vec2.lengthVector(), 1, partialTicks);
				GL11.glPopMatrix();
				GL11.glTranslated(0, -1/*(sf*0.768)*/, 0);
				GL11.glScaled(1, 1, 1);
				if(R<=10000)
				{
					TomPronter.prontTom2(2, y);
				}
				GL11.glPopMatrix();
				GL11.glPopMatrix();
				GL11.glEnable(GL11.GL_LIGHTING);
				GL11.glEnable(GL11.GL_CULL_FACE);
			}
			GL11.glPopMatrix();
	  }
	  
	//ASTEROID ENTRY GLOW
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
}