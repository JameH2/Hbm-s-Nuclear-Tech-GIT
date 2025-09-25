package com.hbm.render.entity.effect;

import java.util.Random;

import org.lwjgl.opengl.GL11;

import com.hbm.entity.effect.EntityAnomaly;
import com.hbm.wiaj.WorldInAJar;

import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;

public class RenderAnomaly extends Render {

	private RenderBlocks renderer;

	@Override
	public void doRender(Entity entity, double x, double y, double z, float yaw, float f) {
		if(!(entity instanceof EntityAnomaly)) return;
		EntityAnomaly anomaly = (EntityAnomaly) entity;

		if(anomaly.theMany == null) return;

		if(renderer == null) {
			renderer = new RenderBlocks(entity.worldObj);
		}

		GL11.glPushMatrix();
		{

			Random rand = entity.worldObj.rand;

			GL11.glTranslated(x, y, z);
			GL11.glTranslated(rand.nextGaussian(), rand.nextGaussian(), rand.nextGaussian());
			for(WorldInAJar jar : anomaly.theMany) {
				jar.render(renderer);
			}

		}
		GL11.glPopMatrix();
	}

	@Override
	protected ResourceLocation getEntityTexture(Entity entity) {
		return null;
	}

}
