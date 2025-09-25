package com.hbm.render.tileentity;

import java.nio.FloatBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import com.hbm.dim.SolarSystem;
import com.hbm.items.ModItems;
import com.hbm.lib.RefStrings;
import com.hbm.main.ResourceManager;
import com.hbm.render.shader.Shader;
import com.hbm.render.util.HorsePronter;
import com.hbm.wiaj.WorldInAJar;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Vec3;

public class RendererObjTester extends TileEntitySpecialRenderer {

	private static RenderBlocks renderer;
	private static WorldInAJar world;
	private static ResourceLocation extra = new ResourceLocation(RefStrings.MODID, "textures/models/horse/dyx.png");
	private static final ResourceLocation noise = new ResourceLocation(RefStrings.MODID, "shaders/iChannel1.png");
	private static final Shader shaeder =  new Shader(new ResourceLocation(RefStrings.MODID, "shaders/fle.frag"));
	@Override
	public void renderTileEntityAt(TileEntity tileEntity, double x, double y, double z, float f) {		
		GL11.glPushMatrix();
		GL11.glTranslated(x + 0.5, y + 0.2, z + 0.5);
		GL11.glDisable(GL11.GL_CULL_FACE);
		long time = tileEntity.getWorldObj().getTotalWorldTime();
		double sine = Math.sin(time * 0.05) * 15;
		double sin3 = Math.sin(time * 0.05 + Math.PI * 0.5) * 15;
		double sin2 = Math.sin(time * 0.05 + Math.PI);
		double insine = Math.sin(time * 0.05) * -15;
		double cy0 = Math.sin(time % (Math.PI * 2));
		double cy1 = Math.sin(time % (Math.PI * 2) - Math.PI * 0.2);
		double cy2 = Math.sin(time % (Math.PI * 2) - Math.PI * 0.4);
		double cy3 = Math.sin(time % (Math.PI * 2) - Math.PI * 0.6);

		GL11.glTranslatef(0, 0.5F, 0);
		bindTexture(ResourceManager.eel_tex);


		ResourceManager.sifter_eel.renderPart("jaw");





		// Head
		GL11.glPushMatrix();
		{
			GL11.glRotatef(1 * 0.5F, -1, 0, 0);
			GL11.glRotatef(0, 0, 0, 0);
			ResourceManager.sifter_eel.renderPart("head");
		}
		GL11.glPopMatrix();

		// Side fins
		GL11.glPushMatrix();
		{
			GL11.glRotated(cy0 * 20, 0, 1, 0);
			ResourceManager.sifter_eel.renderPart("finL");
		}
		GL11.glPopMatrix();
		GL11.glPushMatrix();
		{
			GL11.glRotated(cy0 * -20, 0, 1, 0);
			ResourceManager.sifter_eel.renderPart("finR");
		}
		GL11.glPopMatrix();

		// Tail fin
		GL11.glPushMatrix();
		{
			bindTexture(ResourceManager.eel_tex);
			ResourceManager.sifter_eel.renderPart("frontbodyseg");

			GL11.glRotated(cy1 * 10, 0, 1, 0);
			GL11.glRotated(cy2 * -2, 0, 1, 0);
			ResourceManager.sifter_eel.renderPart("topfinfront");


			ResourceManager.sifter_eel.renderPart("midbodyseg");
			GL11.glRotated(cy3 * -2 * 0.5, 0, 1, 0);
			ResourceManager.sifter_eel.renderPart("topfinmid");
			ResourceManager.sifter_eel.renderPart("topfinmidlast");
			GL11.glRotated(cy1 * -6 * 0.5, 0, 1, 0);

			ResourceManager.sifter_eel.renderPart("midbodysegtwo");

			ResourceManager.sifter_eel.renderPart("bottomfinfront");
			ResourceManager.sifter_eel.renderPart("bottomfinmid");
			ResourceManager.sifter_eel.renderPart("bottomfinmidlast");
			GL11.glRotated(cy2 * -4 * 0.5, 0, 1, 0);
			ResourceManager.sifter_eel.renderPart("topfinlast");
			ResourceManager.sifter_eel.renderPart("bottomfinlast");
			ResourceManager.sifter_eel.renderPart("endbodyseg");
			GL11.glRotated(cy3 * -6 * 0.5, 0, 1, 0);
			ResourceManager.sifter_eel.renderPart("tail");

			ResourceManager.sifter_eel.renderPart("tailbodyseg");

			GL11.glRotated(cy1 * -3 * 0.5, 0, 1, 0);
			ResourceManager.sifter_eel.renderPart("tailtipseg");




			bindTexture(ResourceManager.eel_tex);

		}
		GL11.glPopMatrix();
		GL11.glPopMatrix();

	
		
		/*
		bindTexture(ResourceManager.b2x_tex_mex_sex);

		ResourceManager.b2x.renderAll();
		float trailStretch = tileEntity.getWorldObj().rand.nextFloat();
		trailStretch = 1.2F - (trailStretch * trailStretch * 0.2F);
		trailStretch *= 2;

		GL11.glShadeModel(GL11.GL_SMOOTH);

		
		if(trailStretch > 0) {
			GL11.glColor4d(0.25, 0.88, 0.82, 1);

			GL11.glDisable(GL11.GL_CULL_FACE);
			GL11.glDisable(GL11.GL_LIGHTING);
			GL11.glEnable(GL11.GL_BLEND);
			OpenGlHelper.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO);
			GL11.glPushAttrib(GL11.GL_LIGHTING_BIT);
			OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240F, 240F);
			GL11.glDepthMask(false);
			
			GL11.glPushMatrix();
			GL11.glTranslatef(-0.56F, -0.56F, 1F);
			GL11.glRotatef(180, 0, 1, 0);
			GL11.glScalef(0.5F, 0.5F, trailStretch);
			GL11.glTranslatef(0, 0, 1F);

			bindTexture(ResourceManager.xenon_exhaust_tex);
			ResourceManager.xenon_thruster.renderPart("Exhaust");
			
			
			
			GL11.glPopMatrix();
			GL11.glPushMatrix();
			GL11.glTranslatef(0.56F, -0.56F, 1F);
			GL11.glRotatef(180, 0, 1, 0);
			GL11.glScalef(0.5F, 0.5F, trailStretch);
			GL11.glTranslatef(0, 0, 1F);

			bindTexture(ResourceManager.xenon_exhaust_tex);
			ResourceManager.xenon_thruster.renderPart("Exhaust");
			
			
			GL11.glPopMatrix();
			
			GL11.glDepthMask(true);
			GL11.glPopAttrib();
			GL11.glEnable(GL11.GL_LIGHTING);
			GL11.glEnable(GL11.GL_CULL_FACE);
			GL11.glDisable(GL11.GL_BLEND);

			GL11.glColor4d(1, 1, 1, 1);
		}

		GL11.glShadeModel(GL11.GL_FLAT);
		GL11.glPopMatrix();

		
		
		/*
		GL11.glRotated(sin2, 0, 1, 0); 

		GL11.glPushMatrix();
		GL11.glTranslatef(0, 8.6F, 0.5F);
		
		GL11.glRotated(sin2, 0, 1, 0); 

		GL11.glTranslatef(0, -8.7F, -0.5F);
		bindTexture(ResourceManager.behemoth_body_tex);
		ResourceManager.behemoth.renderPart("body");
		ResourceManager.behemoth.renderPart("hatch1");
		ResourceManager.behemoth.renderPart("hatch2");
		ResourceManager.behemoth.renderPart("gun");

		bindTexture(ResourceManager.behemoth_helmet_tex);
		ResourceManager.behemoth.renderPart("helmet");
		
		GL11.glPushMatrix();

		bindTexture(ResourceManager.behemoth_eye_tex);
		ResourceManager.behemoth.renderPart("eye");

		// Re-enable lighting
		GL11.glPopMatrix();

		GL11.glTranslatef(0, 8.7F, 0.5F);

		GL11.glPopMatrix();

	    GL11.glPushMatrix(); // RIGHT LEG MATRIX START

	    // Define the target position for the right foot
	    double targetFootX = 5.0; // Target X position for right foot
	    double targetFootY = 2.0 * Math.sin(insine * 0.15) + 7; // Target Y position for right foot
	    double targetFootZ = 2.0; // Target Z position for right foot
	    // Define the length of the thigh and shin parts (you can adjust these values)
	    double thighLength = 8.0;
	    double shinLength = 6.0;

	    // Calculate the distance between the hip and the target foot
	    double deltaX = targetFootX;
	    double deltaY = targetFootY - 8.5; // Hip height offset
	    double deltaZ = targetFootZ - 0.5; // Foot Z offset

	    // Calculate the hip and knee angles using basic inverse kinematics (Law of Cosines)
	    double distanceToTarget = Math.sqrt(deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ);
	    double cosKneeAngle = (thighLength * thighLength + shinLength * shinLength - distanceToTarget * distanceToTarget) / (2 * thighLength * shinLength);
	    double kneeAngle = Math.acos(cosKneeAngle); // Knee angle in radians
	    double hipAngle = Math.atan2(deltaY, deltaX) - Math.atan2(shinLength * Math.sin(kneeAngle), thighLength + shinLength * Math.cos(kneeAngle));

	    // Apply the calculated angles to the right leg parts
	    GL11.glTranslatef(0, 8.5F, 0.5F); // Move to the hip position
	    GL11.glRotated(hipAngle * (180.0 / Math.PI), 1, 0, 0); // Rotate the hip to the calculated angle

	    // Render the hip for the right leg
	    bindTexture(ResourceManager.behemoth_hip_tex);
	    GL11.glTranslatef(0, -8.6F, -0.5F);
	    ResourceManager.behemoth.renderPart("hip_right");
	    GL11.glTranslatef(0, 8.6F, 0.5F);

	    // Calculate knee transformation and render knee part for the right leg
	    GL11.glPushMatrix(); // KNEE MATRIX START
	    GL11.glTranslatef(0, -1.8F, -0F);

	    GL11.glRotated(kneeAngle * (180.0 / Math.PI), 1, 0, 0); // Rotate the knee to the calculated angle

	    GL11.glTranslatef(0, -6.7F, -0.5F);
	    bindTexture(ResourceManager.behemoth_knee_tex);
	    ResourceManager.behemoth.renderPart("knee_right");
	    GL11.glTranslatef(0, 6.7F, 0.5F);

	    // Apply foot transformation for the right leg
	    GL11.glTranslatef(0, -5.6F, 0F);
	    GL11.glRotated(sine, 1, 0, 0); // Foot animation

	    GL11.glTranslatef(0, -1F, -0.5F);
	    bindTexture(ResourceManager.behemoth_leg_tex);
	    ResourceManager.behemoth.renderPart("leg_right");

	    GL11.glTranslatef(0, 1F, 0.5F);

	    GL11.glPopMatrix(); // KNEE MATRIX END
	    GL11.glPopMatrix(); // RIGHT LEG MATRIX END

	    // Inverse Kinematics for the Left Leg
	    GL11.glPushMatrix(); // LEFT LEG MATRIX START

	    // Define the target position for the left foot
	    double targetFootX_left = 1.0 * Math.sin(time * 0.15) + 2; // Target X position for left foot
	    double targetFootY_left = 1.0 *  Math.sin(sin2 * 0.015) + 9 ; // Target Y position for left foot
	    double targetFootZ_left =   Math.sin(time * 0.015) - 2 ; // Target Z position for left foot (opposite direction to right leg)

	    // Calculate the distance between the hip and the target foot
	    double deltaX_left = targetFootX_left;
	    double deltaY_left = targetFootY_left - 8.5; // Hip height offset
	    double deltaZ_left = targetFootZ_left - 0.5; // Foot Z offset

	    // Calculate the hip and knee angles using basic inverse kinematics (Law of Cosines)
	    double distanceToTarget_left = Math.sqrt(deltaX_left * deltaX_left + deltaY_left * deltaY_left + deltaZ_left * deltaZ_left);
	    double cosKneeAngle_left = (thighLength * thighLength + shinLength * shinLength - distanceToTarget_left * distanceToTarget_left) / (2 * thighLength * shinLength);
	    double kneeAngle_left = Math.acos(cosKneeAngle_left); // Knee angle in radians
	    double hipAngle_left = Math.atan2(deltaY_left, deltaX_left) - Math.atan2(shinLength * Math.sin(kneeAngle_left), thighLength + shinLength * Math.cos(kneeAngle_left));

	    // Apply the calculated angles to the left leg parts
	    GL11.glTranslatef(0, 8.5F, 0.5F); // Move to the hip position
	    GL11.glRotated(-hipAngle_left * (180.0 / Math.PI), 1, 0, 0); // Rotate the hip to the calculated angle (negative for left leg)

	    // Render the hip for the left leg
	    bindTexture(ResourceManager.behemoth_hip_tex);
	    GL11.glTranslatef(0, -8.6F, -0.5F);
	    ResourceManager.behemoth.renderPart("hip_left");
	    GL11.glTranslatef(0, 8.6F, 0.5F);

	    // Calculate knee transformation and render knee part for the left leg
	    GL11.glPushMatrix(); // KNEE MATRIX START
	    GL11.glTranslatef(0, -1.8F, -0F);

	    GL11.glRotated(kneeAngle_left * (180.0 / Math.PI), 1, 0, 0); // Rotate the knee to the calculated angle

	    GL11.glTranslatef(0, -6.7F, -0.5F);
	    bindTexture(ResourceManager.behemoth_knee_tex);
	    ResourceManager.behemoth.renderPart("knee_left");
	    GL11.glTranslatef(0, 6.7F, 0.5F);

	    // Apply foot transformation for the left leg
	    GL11.glTranslatef(0, -5.6F, 0F);
	    GL11.glRotated(sin2, 1, 0, 0); // Foot animation

	    GL11.glTranslatef(0, -1F, -0.5F);
	    bindTexture(ResourceManager.behemoth_leg_tex);
	    ResourceManager.behemoth.renderPart("leg_left");

	    GL11.glTranslatef(0, 1F, 0.5F);

	    GL11.glPopMatrix(); // KNEE MATRIX END
	    GL11.glPopMatrix(); // LEFT LEG MATRIX END

	    GL11.glEnable(GL11.GL_CULL_FACE);

		GL11.glPopMatrix();
		*/
	}
	

	public void renderTileEntityAt2(TileEntity tileEntity, double x, double y, double z, float f)
    {
        GL11.glPushMatrix();
        GL11.glTranslated(x + 0.5, y, z + 0.5);
        GL11.glEnable(GL11.GL_LIGHTING);
		/*switch(tileEntity.getBlockMetadata())
		{
		case 5:
			GL11.glRotatef(90, 0F, 1F, 0F); break;
		case 3:
			GL11.glRotatef(180, 0F, 1F, 0F); break;
		case 4:
			GL11.glRotatef(270, 0F, 1F, 0F); break;
		case 2:
			GL11.glRotatef(0, 0F, 1F, 0F); break;
		}*/

        /*bindTexture(objTesterTexture);
        objTesterModel.renderAll();*/
        
		//GL11.glScaled(5, 5, 5);
		
        /*GL11.glEnable(GL11.GL_LIGHTING);
        GL11.glEnable(GL11.GL_CULL_FACE);
		bindTexture(ResourceManager.sat_foeq_burning_tex);
		ResourceManager.sat_foeq_burning.renderAll();
		
		GL11.glDisable(GL11.GL_TEXTURE_2D);
		GL11.glDisable(GL11.GL_CULL_FACE);
		GL11.glEnable(GL11.GL_BLEND);
		
		Random rand = new Random(System.currentTimeMillis() / 50);

        GL11.glScaled(1.15, 0.75, 1.15);
        GL11.glTranslated(0, -0.5, 0.3);
        GL11.glDisable(GL11.GL_CULL_FACE);
		for(int i = 0; i < 10; i++) {
			GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
			GL11.glColor3d(1, 0.75, 0.25);
	        GL11.glRotatef(rand.nextInt(360), 0F, 1F, 0F);
			ResourceManager.sat_foeq_fire.renderAll();
	        GL11.glTranslated(0, 2, 0);
			GL11.glColor3d(1, 0.5, 0);
	        GL11.glRotatef(rand.nextInt(360), 0F, 1F, 0F);
			ResourceManager.sat_foeq_fire.renderAll();
	        GL11.glTranslated(0, 2, 0);
			GL11.glColor3d(1, 0.25, 0);
	        GL11.glRotatef(rand.nextInt(360), 0F, 1F, 0F);
			ResourceManager.sat_foeq_fire.renderAll();
	        GL11.glTranslated(0, 2, 0);
			GL11.glColor3d(1, 0.0, 0);
	        GL11.glRotatef(rand.nextInt(360), 0F, 1F, 0F);
			ResourceManager.sat_foeq_fire.renderAll();
			
	        GL11.glTranslated(0, -3.8, 0);
	        
	        GL11.glScaled(0.95, 1.2, 0.95);
		}
		
		GL11.glDisable(GL11.GL_BLEND);
		GL11.glEnable(GL11.GL_TEXTURE_2D);*/

        /*ModelCalBarrel barrel = new ModelCalBarrel();
        ModelCalStock stock = new ModelCalStock();
        ModelCalDualStock saddle = new ModelCalDualStock();

        bindTexture(new ResourceLocation(RefStrings.MODID, "textures/models/ModelCalDualStock.png"));
        saddle.renderAll(1F/16F);
        
        bindTexture(new ResourceLocation(RefStrings.MODID, "textures/models/ModelCalBarrel.png"));
        GL11.glTranslated(0, 0, -0.25);
        barrel.renderAll(1F/16F);
        GL11.glTranslated(0, 0, 0.5);
        barrel.renderAll(1F/16F);
        
        bindTexture(new ResourceLocation(RefStrings.MODID, "textures/models/ModelCalStock.png"));*/
        //stock.renderAll(1F/16F);
        
        //SoyuzPronter.prontSoyuz(2);
        //TomPronter.prontTom();
        //BeamPronter.prontBeam(Vec3.createVectorHelper(5, 5, 5), EnumWaveType.SPIRAL, EnumBeamType.SOLID, 0xff8000, 0xff8000, (int)tileEntity.getWorldObj().getTotalWorldTime() % 360 * 25, 25, 0.1F, 4, 0.05F);
        //BeamPronter.prontBeam(Vec3.createVectorHelper(5, 5, 5), EnumWaveType.SPIRAL, EnumBeamType.SOLID, 0xffff00, 0xffff00, (int)tileEntity.getWorldObj().getTotalWorldTime() % 360 * 25, 1, 0F, 4, 0.05F);
        //BeamPronter.prontHelix(Vec3.createVectorHelper(0, 5, 0), 0.5, 0.5, 0.5, EnumWaveType.SPIRAL, EnumBeamType.LINE, 0x0000ff, 0xffff00, (int)tileEntity.getWorldObj().getTotalWorldTime() % 360 * 25 + 180, 25, 0.25F);

        //DiamondPronter.pront(1, 2, 3, EnumSymbol.OXIDIZER);

        //GL11.glTranslatef(0.0F, -0.25F, 0.0F);
        //GL11.glRotatef(-25, 0, 1, 0);
        //GL11.glRotatef(15, 0, 0, 1);
        
        /*long time = tileEntity.getWorldObj().getTotalWorldTime();
        double sine = Math.sin(time * 0.05) * 5;
        double sin3 = Math.sin(time * 0.05 + Math.PI * 0.5) * 5;
        double sin2 = Math.sin(time * 0.05 + Math.PI);
        int height = 7;
        GL11.glTranslated(0.0F, height + sin2, 0.0F);
        GL11.glRotated(sine, 0, 0, 1);
        GL11.glRotated(sin3, 1, 0, 0);
        GL11.glTranslated(0.0F, -height, 0.0F);
        
        GL11.glEnable(GL11.GL_CULL_FACE);
        GL11.glShadeModel(GL11.GL_SMOOTH);
        bindTexture(ResourceManager.soyuz_lander_tex);
        ResourceManager.soyuz_lander.renderPart("Capsule");
        bindTexture(ResourceManager.soyuz_chute_tex);
        ResourceManager.soyuz_lander.renderPart("Chute");
        GL11.glShadeModel(GL11.GL_FLAT);*/

        /*GL11.glRotatef(-90, 0, 1, 0);
        GL11.glTranslated(0, 3, 0);
        bindTexture(ResourceManager.nikonium_tex);
        ResourceManager.nikonium.renderAll();
        GL11.glTranslated(0, -3, 0);
        GL11.glRotatef(90, 0, 1, 0);

        GL11.glShadeModel(GL11.GL_SMOOTH);
        bindTexture(ResourceManager.fstbmb_tex);
        ResourceManager.fstbmb.renderPart("Body");
        ResourceManager.fstbmb.renderPart("Balefire");
        
        bindTexture(new ResourceLocation(RefStrings.MODID + ":textures/misc/glintBF.png"));
        RenderMiscEffects.renderClassicGlint(tileEntity.getWorldObj(), f, ResourceManager.fstbmb, "Balefire", 0.0F, 0.8F, 0.15F, 5, 2F);
        
        FontRenderer font = Minecraft.getMinecraft().fontRenderer;
        float f3 = 0.04F;
        GL11.glTranslatef(0.815F, 0.9275F, 0.5F);
        GL11.glScalef(f3, -f3, f3);
        GL11.glNormal3f(0.0F, 0.0F, -1.0F * f3);
        GL11.glRotatef(90, 0, 1, 0);
        GL11.glDepthMask(false);
        GL11.glTranslatef(0, 1, 0);
        font.drawString("00:15", 0, 0, 0xff0000);
        GL11.glDepthMask(true);
        
        GL11.glShadeModel(GL11.GL_FLAT);*/
        
        GL11.glTranslated(0D, 4D, 0D);
        GL11.glRotated(System.currentTimeMillis() % 3600 / 10D, 0, 0, 1);
        GL11.glTranslated(0D, -4D, 0D);
        GL11.glRotated(System.currentTimeMillis() % 3600 / 10D, 0, 1, 0);

		GL11.glShadeModel(GL11.GL_SMOOTH);
		GL11.glEnable(GL11.GL_CULL_FACE);
		GL11.glEnable(GL11.GL_LIGHTING);
		
		//GL11.glDisable(GL11.GL_TEXTURE_2D);
		RenderHelper.disableStandardItemLighting();
		RenderHelper.enableStandardItemLighting();
		GL11.glColor4d(1, 1, 1, 1);
		GL11.glClearColor(0, 0, 0, 0);

		float amb = 2F;
		float dif = 2F;
		float spe = 2F;
		float shi = 1F;
		FloatBuffer iamb = (FloatBuffer) BufferUtils.createFloatBuffer(8).put(new float[] { amb, amb, amb, 1F }).flip();
		FloatBuffer idif = (FloatBuffer) BufferUtils.createFloatBuffer(8).put(new float[] { dif, dif, dif, 1F }).flip();
		FloatBuffer ispe = (FloatBuffer) BufferUtils.createFloatBuffer(8).put(new float[] { spe, spe, spe, 1F }).flip();
		FloatBuffer mamb = (FloatBuffer) BufferUtils.createFloatBuffer(8).put(new float[] { amb, amb, amb, 1F }).flip();
		FloatBuffer mdif = (FloatBuffer) BufferUtils.createFloatBuffer(8).put(new float[] { dif, dif, dif, 1F }).flip();
		FloatBuffer mspe = (FloatBuffer) BufferUtils.createFloatBuffer(8).put(new float[] { spe, spe, spe, 1F }).flip();
		float msh = 128F * shi;
		FloatBuffer mem = (FloatBuffer) BufferUtils.createFloatBuffer(8).put(new float[] { 1F, 1F, 1F, 1F }).flip();
		
		GL11.glEnable(GL11.GL_LIGHTING);
		GL11.glShadeModel(GL11.GL_SMOOTH);
		GL11.glLight(GL11.GL_LIGHT0, GL11.GL_AMBIENT, iamb);
		GL11.glLight(GL11.GL_LIGHT0, GL11.GL_DIFFUSE, idif);
		GL11.glLight(GL11.GL_LIGHT0, GL11.GL_SPECULAR, ispe);
		GL11.glLight(GL11.GL_LIGHT1, GL11.GL_AMBIENT, iamb);
		GL11.glLight(GL11.GL_LIGHT1, GL11.GL_DIFFUSE, idif);
		GL11.glLight(GL11.GL_LIGHT1, GL11.GL_SPECULAR, ispe);
		GL11.glMaterial(GL11.GL_FRONT, GL11.GL_AMBIENT, mamb);
		GL11.glMaterial(GL11.GL_FRONT, GL11.GL_DIFFUSE, mdif);
		GL11.glMaterial(GL11.GL_FRONT, GL11.GL_SPECULAR, mspe);
		GL11.glMaterialf(GL11.GL_FRONT, GL11.GL_SHININESS, msh);
		GL11.glMaterial(GL11.GL_FRONT, GL11.GL_EMISSION, mem); 
		GL11.glLightModeli(GL12.GL_LIGHT_MODEL_COLOR_CONTROL, GL12.GL_SEPARATE_SPECULAR_COLOR);
		
		
		bindTexture(ResourceManager.soyuz_module_dome_tex);
		ResourceManager.soyuz_module.renderPart("Dome");
		bindTexture(ResourceManager.soyuz_module_lander_tex);
		ResourceManager.soyuz_module.renderPart("Capsule");
		bindTexture(ResourceManager.soyuz_module_propulsion_tex);
		ResourceManager.soyuz_module.renderPart("Propulsion");
		bindTexture(ResourceManager.soyuz_module_solar_tex);
		ResourceManager.soyuz_module.renderPart("Solar");

		GL11.glShadeModel(GL11.GL_FLAT);
		GL11.glEnable(GL11.GL_TEXTURE_2D);
		GL11.glColor3f(1F, 1F, 1F);

        /*GL11.glDisable(GL11.GL_CULL_FACE);
        GL11.glShadeModel(GL11.GL_SMOOTH);
        
        bindTexture(ResourceManager.igen_tex);
        ResourceManager.igen.renderPart("Base");
        
        float angle = System.currentTimeMillis() * 1 % 360;
        float px = 0.0625F;
        float sine = (float) Math.sin(Math.toRadians(angle));
        float cosine = (float) Math.cos(Math.toRadians(angle));
        float armAng = 22.5F;
        
        GL11.glPushMatrix();
        GL11.glTranslated(0, 3.5, 0);
        GL11.glRotatef(angle, 0, 0, 1);
        GL11.glTranslated(0, -3.5, 0);
        
        bindTexture(ResourceManager.igen_rotor);
        ResourceManager.igen.renderPart("Rotor");
        GL11.glPopMatrix();
        
        
        
        GL11.glPushMatrix();
        GL11.glTranslated(0, 3.5, px * 5);
        GL11.glRotatef(angle, -1, 0, 0);
        GL11.glTranslated(0, -3.5, px * -5);
        
        bindTexture(ResourceManager.igen_cog);
        ResourceManager.igen.renderPart("CogLeft");
        GL11.glPopMatrix();
        
        
        
        GL11.glPushMatrix();
        GL11.glTranslated(0, 3.5, px * 5);
        GL11.glRotatef(angle, 1, 0, 0);
        GL11.glTranslated(0, -3.5, px * -5);
        
        bindTexture(ResourceManager.igen_cog);
        ResourceManager.igen.renderPart("CogRight");
        GL11.glPopMatrix();
        
        
        
        GL11.glPushMatrix();
        GL11.glTranslated(0, 0, cosine * 0.8725 - 1);
        
        bindTexture(ResourceManager.igen_pistons);
        ResourceManager.igen.renderPart("Pistons");
        GL11.glPopMatrix();
        
        GL11.glPushMatrix();
        
        GL11.glTranslated(0, sine * 0.55, cosine * 0.8725 - 1.125);
        
        GL11.glTranslated(0, 3.5, px * 6.5);
        GL11.glRotatef(sine * -armAng, 1, 0, 0);
        GL11.glTranslated(0, -3.5, px * -5);
        
        bindTexture(ResourceManager.igen_arm);
        ResourceManager.igen.renderPart("ArmLeft");
        GL11.glPopMatrix();
        
        GL11.glPushMatrix();
        
        GL11.glTranslated(0, -sine * 0.55, cosine * 0.8725 - 1.125);
        
        GL11.glTranslated(0, 3.5, px * 6.5);
        GL11.glRotatef(sine * armAng, 1, 0, 0);
        GL11.glTranslated(0, -3.5, px * -5);
        
        bindTexture(ResourceManager.igen_arm);
        ResourceManager.igen.renderPart("ArmRight");
        GL11.glPopMatrix();

        GL11.glTranslated(-0.75, 5.5625, -7);
        for(int i = 0; i < 2; i++) {
	        BeamPronter.prontBeam(Vec3.createVectorHelper(1.5, 0, 0), EnumWaveType.RANDOM, EnumBeamType.LINE, 0x8080ff, 0x0000ff, (int)tileEntity.getWorldObj().getTotalWorldTime() % 1000 + i, 5, px * 4, 0, 0);
	        BeamPronter.prontBeam(Vec3.createVectorHelper(1.5, 0, 0), EnumWaveType.RANDOM, EnumBeamType.LINE, 0xffffff, 0x0000ff, (int)tileEntity.getWorldObj().getTotalWorldTime() % 1000 + 2 + i, 5, px * 4, 0, 0);
        }
        
        GL11.glShadeModel(GL11.GL_FLAT);
        GL11.glEnable(GL11.GL_CULL_FACE);

        tileEntity.getWorldObj().spawnParticle("splash", tileEntity.xCoord + 2.1, tileEntity.yCoord + 5.875, tileEntity.zCoord + 0.5, 0, 0, -0.25);
        tileEntity.getWorldObj().spawnParticle("smoke", tileEntity.xCoord + 2.8, tileEntity.yCoord + 5.05, tileEntity.zCoord + 2, 0, 0, -0.1);
        
        if(tileEntity.getWorldObj().rand.nextInt(100) == 0) {

    		tileEntity.getWorldObj().spawnParticle("flame", tileEntity.xCoord + 2.8, tileEntity.yCoord + 5.05, tileEntity.zCoord + 2, 0, 0, -0.3);
        	for(int i = 0; i < 5; i++) {
                tileEntity.getWorldObj().spawnParticle("smoke", tileEntity.xCoord + 2.8, tileEntity.yCoord + 5.05, tileEntity.zCoord + 2, 0, 0, -0.3);
        	}
        }*/
        
        GL11.glPopMatrix();
        RenderHelper.enableStandardItemLighting();
    }

}