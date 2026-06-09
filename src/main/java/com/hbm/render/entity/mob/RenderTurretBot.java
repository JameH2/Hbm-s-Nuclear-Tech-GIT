package com.hbm.render.entity.mob;

import org.lwjgl.opengl.GL11;

import com.hbm.entity.mob.EntityTurretBot;
import com.hbm.main.ResourceManager;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;

public class RenderTurretBot extends Render {

	public RenderTurretBot() {
		this.shadowSize = 1.0F;
		this.shadowOpaque = 0.8F;
	}

	@Override
	protected ResourceLocation getEntityTexture(Entity entity) {
		return ResourceManager.turretbot_body;
	}

	@Override
	public void doRender(Entity entity, double x, double y, double z, float yaw, float partialTicks) {
		EntityTurretBot bot = (EntityTurretBot) entity;

		float age = bot.ticksExisted + partialTicks;

		int targetId = bot.getDataWatcher().getWatchableObjectInt(20);
		EntityLivingBase targetBase = (EntityLivingBase) bot.worldObj.getEntityByID(targetId);

		if(targetBase != null) {
			double dx = targetBase.posX - bot.posX;
			double dz = targetBase.posZ - bot.posZ;
			double targetYaw = Math.atan2(dz, dx) * (180 / Math.PI) - 90;
			double dYaw = MathHelper.wrapAngleTo180_double(targetYaw - bot.headTargetYaw);
			bot.headTargetYaw += dYaw * 0.1;
		}

		float targetPitch = computeTargetPitch(bot);
		bot.headTargetPitch += (targetPitch - bot.headTargetPitch) * 0.1F;

		float bodyYaw = interp(bot.prevRenderYawOffset, bot.renderYawOffset, partialTicks);

		float limbSwing = bot.limbSwing - bot.limbSwingAmount * (1.0F - partialTicks);
		float swingAmount = bot.prevLimbSwingAmount + (bot.limbSwingAmount - bot.prevLimbSwingAmount) * partialTicks;
		double cy1 = Math.sin(limbSwing % (Math.PI * 2) - Math.PI * 0.62) *3;

		float bite = bot.getSwingProgress(partialTicks);

		float recoilCurve = 1.0F - (1.0F - bite) * (1.0F - bite);
		float recoilKick = (float) Math.sin(recoilCurve * Math.PI) * 25F;

		double idleBreath = Math.sin(age * 0.08) * 1.5;
		double idleSway   = Math.sin(age * 0.05) * 2.0;
		double idleNod    = Math.cos(age * 0.07) * 1.0;
		double idleHum    = Math.sin(age * 0.15) * 0.3;

		boolean engaged = bot.getAttackTarget() != null;
		float idleScale = engaged ? 0.3F : 1.0F;

		float bodyLean = swingAmount * (float) Math.sin(limbSwing * 0.5) * 3F;

		double bob = -Math.abs(Math.cos(limbSwing)) * swingAmount * 0.08;

		GL11.glPushMatrix();
		{
			GL11.glTranslated(x, y + bob, z);


			double s = 0.7;
			GL11.glScaled(s, s, s);

			GL11.glRotatef(-bodyYaw + 180, 0, 1, 0);

			GL11.glTranslated(0, idleBreath * 0.02 * idleScale, 0);

			GL11.glRotatef(bodyLean, 1, 0, 0);

			GL11.glEnable(GL11.GL_LIGHTING);
			GL11.glDisable(GL11.GL_CULL_FACE);

			Minecraft.getMinecraft().renderEngine.bindTexture(ResourceManager.turretbot_leg);

			GL11.glPushMatrix();
			GL11.glTranslated(0, 2, 0);
			GL11.glRotated(-cy1 * 10 * swingAmount, 1, 0, 0);
			GL11.glTranslated(0, -2, 0);
			ResourceManager.turretBot.renderPart("legLeft");

			GL11.glTranslated(0, 1, 0);
			GL11.glRotated(-cy1 * 10 * swingAmount, 1, 0, 0);
			GL11.glTranslated(0, -1, 0);
			ResourceManager.turretBot.renderPart("legLeft1");
			ResourceManager.turretBot.renderPart("legLeft2");
			ResourceManager.turretBot.renderPart("legLeft3");
			GL11.glPopMatrix();

			GL11.glPushMatrix();
			GL11.glTranslated(0, 2, 0);
			GL11.glRotated(cy1 * 10 * swingAmount, 1, 0, 0);
			GL11.glTranslated(0, -2, 0);
			ResourceManager.turretBot.renderPart("legRight");

			GL11.glTranslated(0, 1, 0);
			GL11.glRotated(cy1 * 10 * swingAmount, 1, 0, 0);
			GL11.glTranslated(0, -1, 0);
			ResourceManager.turretBot.renderPart("legRight1");
			ResourceManager.turretBot.renderPart("legRight2");
			ResourceManager.turretBot.renderPart("legRight3");
			GL11.glPopMatrix();

			Minecraft.getMinecraft().renderEngine.bindTexture(ResourceManager.turretbot_body);
			ResourceManager.turretBot.renderPart("body");

			GL11.glPushMatrix();
			{
				GL11.glRotated(bodyYaw - bot.headTargetYaw + idleSway * idleScale, 0, 1, 0);

				GL11.glRotated(idleNod * idleScale, 1, 0, 0);

				Minecraft.getMinecraft().renderEngine.bindTexture(ResourceManager.turretbot_head);
				ResourceManager.turretBot.renderPart("head");

				GL11.glPushMatrix();
				{
					GL11.glTranslated(0, 4, 0);
					GL11.glRotated(bot.headTargetPitch, 1, 0, 0);
					GL11.glTranslated(-0, -4, -0);

					GL11.glRotated(-recoilKick, 1, 0, 0);

					GL11.glRotated(idleHum * idleScale, 1, 0, 0);

					double recoilPush = recoilCurve * 0.08;
					GL11.glTranslated(0, 0, recoilPush);

					Minecraft.getMinecraft().renderEngine.bindTexture(ResourceManager.turretbot_gun);
					ResourceManager.turretBot.renderPart("gun");
				}
				GL11.glPopMatrix();
			}
			GL11.glPopMatrix();
		}
		GL11.glPopMatrix();
	}

	private float computeTargetPitch(EntityTurretBot bot) {
		int targetId = bot.getDataWatcher().getWatchableObjectInt(20);
		if(targetId == 0) return 0F;

		Entity target = bot.worldObj.getEntityByID(targetId);
		if(!(target instanceof EntityLivingBase)) return 0F;

		EntityLivingBase living = (EntityLivingBase) target;

		double dx = living.posX - bot.posX;
		double dz = living.posZ - bot.posZ;
		double dy = (living.posY + living.getEyeHeight()) - (bot.posY + 2.5);
		double horiz = Math.sqrt(dx * dx + dz * dz);

		return (float)(Math.atan2(dy, horiz) * 180.0 / Math.PI);
	}
	
	private float interp(float prev, float curr, float partial) {
		float diff = MathHelper.wrapAngleTo180_float(curr - prev);
		return prev + diff * partial;
	}
}