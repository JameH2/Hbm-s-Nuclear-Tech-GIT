package com.hbm.render.entity.mob;

import org.lwjgl.opengl.GL11;

import com.hbm.entity.mob.EntityTankbot;
import com.hbm.entity.mob.glyphid.EntityGlyphid;
import com.hbm.lib.RefStrings;
import com.hbm.main.ResourceManager;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;

public class RenderTankbot extends RenderLiving {

	public static final ResourceLocation glyphid_infested_tex = new ResourceLocation(RefStrings.MODID, "textures/entity/glyphid_infestation.png");
	
	public RenderTankbot() {
		super(new ModelTankbot(), 1.0F);
		this.shadowOpaque = 0.0F;
		this.setRenderPassModel(this.mainModel);
	}

	@Override
	protected ResourceLocation getEntityTexture(Entity entity) {
		return ResourceManager.tankbot_tex;
	}

	@Override
	protected int shouldRenderPass(EntityLivingBase entity, int pass, float interp) {
		if(pass != 0) {
			return -1;
		}
			return -1;
	}

	public static class ModelTankbot extends ModelBase {


		double bite = 0;

		@Override
		public void setLivingAnimations(EntityLivingBase entity, float limbSwing, float limbSwingAmount, float interp) {
		    if (entity instanceof EntityTankbot) {
		        EntityTankbot tankbot = (EntityTankbot) entity; 
		        bite = tankbot.getSwingProgress(interp);
		        
		        int targetId = tankbot.getDataWatcher().getWatchableObjectInt(20);

		        EntityLivingBase targetBase = (EntityLivingBase) tankbot.worldObj.getEntityByID(targetId);
		        
		        if (tankbot.worldObj != null) {
		            if (targetBase != null) {
		                double dx = targetBase.posX - tankbot.posX;
		                double dz = targetBase.posZ - tankbot.posZ;

		                double targetYaw = Math.atan2(dz, dx) * (180 / Math.PI) - 90;
		                double deltaYaw = MathHelper.wrapAngleTo180_double(targetYaw - tankbot.headTargetYaw);
		                tankbot.headTargetYaw += deltaYaw * 0.1; // Adjust the multiplier for smoothing effect
		            }
		        }
		    }
		}

		@Override
		public void render(Entity entity, float limbSwing, float limbSwingAmount, float rotationYaw, float rotationHeadYaw, float rotationPitch, float scale) {
			GL11.glPushMatrix();

			GL11.glRotatef(180, 1, 0, 0);
			GL11.glTranslatef(0, -1.5F, 0);
			GL11.glEnable(GL11.GL_LIGHTING);
			GL11.glDisable(GL11.GL_CULL_FACE);
			
			this.renderModel(entity, limbSwing);
			//GL11.glRotatef((float) headTargetYaw, 1, 0, 0);

			GL11.glPopMatrix();
		}
		
		public void renderModel(Entity entity, float limbSwing) {
			
			GL11.glPushMatrix();
			
			double s = 2;
			GL11.glScaled(s, s, s);
			
			EntityLivingBase living = (EntityLivingBase) entity;
	        EntityTankbot tankbot = (EntityTankbot) entity; 

			double walkCycle = limbSwing;

			double cy0 = Math.sin(walkCycle % (Math.PI * 2) * 2);
			double cy1 = Math.sin(walkCycle % (Math.PI * 2) - Math.PI * 0.62);
			double cy2 = Math.sin(walkCycle % (Math.PI * 2) - Math.PI);
			double cy3 = Math.sin(walkCycle % (Math.PI * 2) - Math.PI * 0.75);
			double cy4 = Math.sin(walkCycle % (Math.PI * 2) - Math.PI * 1.25);

			double bite = tankbot.headTargetYaw;
			double headTilt = Math.sin(this.bite * Math.PI) * 30;
			
			ResourceManager.tankbot.renderPart("body");
			
			/// LEFT ARM ///
			GL11.glPushMatrix();
			
			GL11.glPushMatrix();

			GL11.glTranslated(0.25, 0.625, 0.0625);
			GL11.glRotated(10, 0, 1, 0);
			GL11.glRotated(-cy3 * 10, 0, 1, 0);
			GL11.glRotated(cy2 * -2, 0, 0, 1);
			GL11.glTranslated(-0.25, -0.625, -0.0625);
			ResourceManager.tankbot.renderPart("front2foot");
			ResourceManager.tankbot.renderPart("front2leg");
			ResourceManager.tankbot.renderPart("front2knee");
			GL11.glPopMatrix();
			GL11.glPushMatrix();

			GL11.glTranslated(0.25, 0.625, 0.4375);
			GL11.glRotated(cy3 - cy2 * 10, 0, 1, 0);
			GL11.glRotated(cy2 - cy3 * -2, 0, 0, 1);
			GL11.glTranslated(-0.25, -0.625, -0.4375);
			ResourceManager.tankbot.renderPart("front1foot");
			ResourceManager.tankbot.renderPart("front1leg");
			ResourceManager.tankbot.renderPart("front1knee");
			GL11.glPopMatrix();

			GL11.glPushMatrix();

			GL11.glTranslated(0.25, 0.625, 0.9375);
			GL11.glRotated(cy3 - cy2 * 10, 0, 1, 0);
			GL11.glRotated(cy2 - cy3 * -2, 0, 0, 1);

			GL11.glTranslated(-0.25, -0.625, -0.9375);
			ResourceManager.tankbot.renderPart("back1foot");
			ResourceManager.tankbot.renderPart("back1leg");
			ResourceManager.tankbot.renderPart("back1knee");
			GL11.glPopMatrix();

			GL11.glPushMatrix();

			GL11.glTranslated(0.25, 0.625, 0.9375);
			GL11.glRotated(cy2 - cy3 * 5, 0, 1, 0);
			GL11.glRotated(cy2 * 2, 1, 0, 0);

			GL11.glTranslated(-0.25, -0.625, -0.9375);
			ResourceManager.tankbot.renderPart("back2foot");
			ResourceManager.tankbot.renderPart("back2leg");
			ResourceManager.tankbot.renderPart("back2knee");
			GL11.glPopMatrix();
			
			GL11.glPopMatrix();
			
			GL11.glPushMatrix();
			//this makes NO SENSE but its working okay for some reason...
			//someone help me :(
			double bier = entity.getRotationYawHead();
			//GL11.glRotated(bier, 0, 1, 0);
			GL11.glRotated(-bite + bier, 0, 1, 0);
			GL11.glRotated(-90, 0, 1, 0);
			ResourceManager.tankbot.renderPart("head");
			ResourceManager.tankbot.renderPart("barrelbottom");
			ResourceManager.tankbot.renderPart("barreltop");

			GL11.glPopMatrix();



			
			GL11.glPopMatrix();
		}
	}
}
