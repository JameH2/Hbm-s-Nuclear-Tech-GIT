package com.hbm.render.entity.mob;

import org.lwjgl.opengl.GL11;

import com.hbm.entity.mob.EntityTankbot;
import com.hbm.entity.mob.EntityWarBehemoth;
import com.hbm.entity.mob.glyphid.EntityGlyphid;
import com.hbm.lib.RefStrings;
import com.hbm.main.ResourceManager;

import cpw.mods.fml.client.FMLClientHandler;
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

public class RenderBehemoth extends RenderLiving {

	public static final ResourceLocation glyphid_infested_tex = new ResourceLocation(RefStrings.MODID, "textures/entity/glyphid_infestation.png");
	
	public RenderBehemoth() {
		super(new ModelBehemoth(), 1.0F);
		this.shadowOpaque = 0.0F;
		this.setRenderPassModel(this.mainModel);
	}

	@Override
	protected ResourceLocation getEntityTexture(Entity entity) {
		return ResourceManager.behemoth_body_tex;
	}

	@Override
	protected int shouldRenderPass(EntityLivingBase entity, int pass, float interp) {
		if(pass != 0) {
			return -1;
		}
			return -1;
	}

	public static class ModelBehemoth extends ModelBase {


		double bite = 0;

		@Override
		public void setLivingAnimations(EntityLivingBase entity, float limbSwing, float limbSwingAmount, float interp) {
		    if (entity instanceof EntityWarBehemoth) {
		        EntityWarBehemoth behemoth = (EntityWarBehemoth) entity; 
		        bite = behemoth.getSwingProgress(interp);



		        int targetId = behemoth.getDataWatcher().getWatchableObjectInt(19);

		        EntityLivingBase targetBase = (EntityLivingBase) behemoth.worldObj.getEntityByID(targetId);
		        if (behemoth.worldObj != null) {
		            if (targetBase != null) {
	    	            double dx = targetBase.posX - behemoth.posX;
	    	            double dy = (targetBase.posY - targetBase.getEyeHeight()) - (behemoth.posY + behemoth.getEyeHeight());
	    	            double dz = targetBase.posZ - behemoth.posZ;
	    	            

	    	            // Calculate Pitch (vertical rotation)
	    	            double horizontalDistance = Math.sqrt(dx * dx + dz * dz);  // Distance in XZ plane
	    	            double targetPitch = Math.atan2(dy, horizontalDistance) * (-180 / Math.PI);
	    	            double deltaPitch = MathHelper.wrapAngleTo180_double(targetPitch - behemoth.rotationPitch);
	    	            behemoth.rotationPitch += deltaPitch * 0.1;  // Smooth pitch rotation
	    	            
		                double targetYaw = Math.atan2(dz, dx) * (180 / Math.PI) - 90;
		                double deltaYaw = MathHelper.wrapAngleTo180_double(targetYaw - behemoth.newRotationYaw);
		                behemoth.newRotationYaw += deltaYaw * 0.1; 
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

			double s = 1.2;
			GL11.glScaled(s, s, s);
			
			EntityLivingBase living = (EntityLivingBase) entity;
			EntityWarBehemoth behemoth = (EntityWarBehemoth) entity; 

			

			
			double walkCycle = limbSwing;

			double cy0 = Math.sin(walkCycle % (Math.PI * 2) * 2);
			double cy1 = Math.sin(walkCycle % (Math.PI * 2) - Math.PI * 0.62);
			double cy2 = Math.sin(walkCycle % (Math.PI * 2) - Math.PI);
			double cy3 = Math.sin(walkCycle % (Math.PI * 2) - Math.PI * 0.75);
			double cy4 = Math.sin(walkCycle % (Math.PI * 2) - Math.PI * 1.25);
			GL11.glTranslatef(0, -0.5F, 0F);

			double bite = behemoth.rotationYawHead;
			double headTilt = Math.sin(this.bite * Math.PI) * 30;
			GL11.glDisable(GL11.GL_CULL_FACE);

			GL11.glRotated(-180, 0, 1, 0); 

			GL11.glPushMatrix();
			GL11.glTranslatef(0, 8.6F, 0.5F);
			
		    //GL11.glRotated(-behemoth.rotationYawHead, 0, 1, 0);  // Apply yaw rotation
		    GL11.glRotated(-behemoth.rotationPitch, 1, 0, 0);  // Apply pitch rotation
			GL11.glTranslatef(0, -8.7F, -0.5F);
			 FMLClientHandler.instance().getClient().renderEngine.bindTexture(ResourceManager.behemoth_body_tex);
			ResourceManager.behemoth.renderPart("body");
			ResourceManager.behemoth.renderPart("hatch1");
			ResourceManager.behemoth.renderPart("hatch2");
			ResourceManager.behemoth.renderPart("gun");

			 FMLClientHandler.instance().getClient().renderEngine.bindTexture(ResourceManager.behemoth_helmet_tex);
			ResourceManager.behemoth.renderPart("helmet");
			
			GL11.glPushMatrix();

			 FMLClientHandler.instance().getClient().renderEngine.bindTexture(ResourceManager.behemoth_eye_tex);
			ResourceManager.behemoth.renderPart("eye");

			// Re-enable lighting
			GL11.glPopMatrix();

			GL11.glTranslatef(0, 8.7F, 0.5F);

			GL11.glPopMatrix();


		  
		    double stepHeight = 5.5; 
		    double stepLength = 2.5; 
		    double stepTime = walkCycle * 0.25;

		    double leftFootY = Math.sin(stepTime) * stepHeight - 2.0; 
		    double leftFootX = Math.sin(stepTime) * stepLength + 5.0; 
		    double leftFootZ = Math.cos(stepTime) * stepLength + 1.5; 
		    
		    double rightFootY = Math.sin(stepTime + Math.PI) * stepHeight - 4.0;
		    double rightFootX = Math.sin(stepTime + Math.PI) * stepLength + 5.0; 
		    double rightFootZ = Math.cos(stepTime + Math.PI) * stepLength + 1.5; 

		    leftFootY = Math.max(leftFootY, 7.0); 
		    rightFootY = Math.max(rightFootY, 7.0); 

		    GL11.glPushMatrix(); // RIGHT LEG MATRIX START

		    // Right foot position calculations
		    renderLegIK("right", rightFootX, rightFootY, rightFootZ);

		    GL11.glPopMatrix(); // RIGHT LEG MATRIX END

		    GL11.glPushMatrix(); // LEFT LEG MATRIX START

		    renderLegIK("left", leftFootX, leftFootY, leftFootZ);

		    GL11.glPopMatrix(); // LEFT LEG MATRIX END

		    GL11.glEnable(GL11.GL_CULL_FACE);

		    GL11.glEnable(GL11.GL_CULL_FACE);

			GL11.glPopMatrix();
		}
		public void renderLegIK(String leg, double targetFootX, double targetFootY, double targetFootZ) {
		    // Calculate leg IK based on the target foot position
		    double thighLength = 6.0;
		    double shinLength = 8.0;

		    double deltaX = targetFootX;
		    double deltaY = targetFootY - 8.5; // Hip height offset
		    double deltaZ = targetFootZ - 1.5; // Foot Z offset

		    double distanceToTarget = Math.sqrt(deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ);
		    double cosKneeAngle = (thighLength * thighLength + shinLength * shinLength - distanceToTarget * distanceToTarget) / (2 * thighLength * shinLength);
		    double kneeAngle = Math.acos(cosKneeAngle);
		    double hipAngle = Math.atan2(deltaY, deltaX) - Math.atan2(shinLength * Math.sin(kneeAngle), thighLength + shinLength * Math.cos(kneeAngle));

		    if (leg.equals("right")) {
		        GL11.glTranslatef(0, 8.5F, 0.5F); 
		        GL11.glRotated(hipAngle * (180.0 / Math.PI), 1, 0, 0); 
		        FMLClientHandler.instance().getClient().renderEngine.bindTexture(ResourceManager.behemoth_hip_tex);
		        GL11.glTranslatef(0, -8.6F, -0.5F);
		        ResourceManager.behemoth.renderPart("hip_right");
		        GL11.glTranslatef(0, 8.6F, 0.5F);
		    } else {
		        GL11.glTranslatef(0, 8.5F, 0.5F); 
		        GL11.glRotated(hipAngle * (180.0 / Math.PI), 1, 0, 0); 
		        FMLClientHandler.instance().getClient().renderEngine.bindTexture(ResourceManager.behemoth_hip_tex);
		        GL11.glTranslatef(0, -8.6F, -0.5F);
		        ResourceManager.behemoth.renderPart("hip_left");
		        GL11.glTranslatef(0, 8.6F, 0.5F);
		    }

		    GL11.glPushMatrix(); // KNEE MATRIX START
		    GL11.glTranslatef(0, -1.8F, -0F);
		    GL11.glRotated(kneeAngle * (180.0 / Math.PI), 1, 0, 0); // Knee rotation

		    GL11.glTranslatef(0, -6.7F, -0.5F);
		    FMLClientHandler.instance().getClient().renderEngine.bindTexture(ResourceManager.behemoth_knee_tex);
		    ResourceManager.behemoth.renderPart(leg.equals("right") ? "knee_right" : "knee_left");
		    GL11.glTranslatef(0, 6.7F, 0.5F);

		    GL11.glTranslatef(0, -5.6F, 0F);
		    GL11.glRotated(cosKneeAngle * (-90.0 / Math.PI), 1, 0, 0); 
		    GL11.glTranslatef(0, -1F, -0.5F);
		    FMLClientHandler.instance().getClient().renderEngine.bindTexture(ResourceManager.behemoth_leg_tex);
		    ResourceManager.behemoth.renderPart(leg.equals("right") ? "leg_right" : "leg_left");

		    GL11.glTranslatef(0, 1F, 0.5F);
		    GL11.glPopMatrix(); // KNEE MATRIX END
		}
	}
}
