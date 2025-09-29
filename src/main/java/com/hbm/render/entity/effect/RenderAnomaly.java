package com.hbm.render.entity.effect;

import org.lwjgl.opengl.GL11;

import com.hbm.entity.effect.EntityAnomaly;
import com.hbm.util.BobMathUtil;
import com.hbm.wiaj.WorldInAJar;

import net.minecraft.client.renderer.entity.Render;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;

public class RenderAnomaly extends Render {

	@Override
	public void doRender(Entity entity, double x, double y, double z, float yaw, float f) {
		if(!(entity instanceof EntityAnomaly)) return;
		EntityAnomaly anomaly = (EntityAnomaly) entity;

		if(anomaly.theMany == null) return;

		GL11.glPushMatrix();
		{

			// undo player relative, to reset to world origin relative
			GL11.glTranslated(x, y, z);
			GL11.glTranslated(-anomaly.posX, -anomaly.posY, -anomaly.posZ);

			// render each jar at their original location, plus displacement
			int i = 0;
			for(WorldInAJar jar : anomaly.theMany) {
				GL11.glPushMatrix();
				{

					double time = ((double)entity.worldObj.getTotalWorldTime() + f + entity.getEntityId() * 123 + (i++) * 234) / 160;

					double offsetX = BobMathUtil.sps(time) * 2;
					double offsetY = BobMathUtil.sps(time + 5) * 6;
					double offsetZ = BobMathUtil.sps(time + 12) * 2;

					double rotation = BobMathUtil.sps(time + 17) * 30;
					double axisX = BobMathUtil.sps(time + 22);
					double axisY = BobMathUtil.sps(time + 27);
					double axisZ = BobMathUtil.sps(time + 42);

					jar.lightlevel = entity.worldObj.getLightBrightnessForSkyBlocks((int) Math.floor(jar.posX) + 1, (int) Math.floor(jar.posY) + 16, (int) Math.floor(jar.posZ) + 1, 0);
					jar.dimLowerBlocks = false;

					double loopTime = time % 3.234;
					if(loopTime > 0 && loopTime < 0.6) {
						GL11.glTranslated(entity.worldObj.rand.nextGaussian() * 0.1, entity.worldObj.rand.nextGaussian() * 0.2, entity.worldObj.rand.nextGaussian() * 0.1);
						jar.dimLowerBlocks = true;
					}

					GL11.glTranslated(jar.posX + offsetX, jar.posY + 16 + offsetY, jar.posZ + offsetZ);
					GL11.glTranslated(jar.sizeX / 2, jar.sizeY / 2, jar.sizeZ / 2);
					GL11.glRotated(rotation, axisX, axisY, axisZ);
					GL11.glTranslated(-jar.sizeX / 2, -jar.sizeY / 2, -jar.sizeZ / 2);
					jar.render();

				}
				GL11.glPopMatrix();
			}

		}
		GL11.glPopMatrix();
	}

	@Override
	protected ResourceLocation getEntityTexture(Entity entity) {
		return null;
	}

}
