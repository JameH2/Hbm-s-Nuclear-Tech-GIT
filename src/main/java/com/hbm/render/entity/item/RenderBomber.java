package com.hbm.render.entity.item;

import org.lwjgl.opengl.GL11;

import com.hbm.main.ResourceManager;

import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;

public class RenderBomber extends Render {

	public RenderBomber() {
	}

	@Override
	public void doRender(Entity entity, double x, double y, double z, float f0, float interp) {

		GL11.glPushMatrix();
		GL11.glTranslatef((float) x, (float) y, (float) z);
		GL11.glRotatef(entity.prevRotationYaw + (entity.rotationYaw - entity.prevRotationYaw) * interp - 90.0F, 0.0F, 1.0F, 0.0F);
		GL11.glRotatef(90, 0F, 0F, 1F);
		GL11.glRotatef(entity.prevRotationPitch + (entity.rotationPitch - entity.prevRotationPitch) * interp, 0.0F, 0.0F, 1.0F);

		GL11.glEnable(GL11.GL_LIGHTING);
		GL11.glDisable(GL11.GL_CULL_FACE);

		int i = entity.getDataWatcher().getWatchableObjectByte(16);

		switch(i) {
		case 0: bindTexture(ResourceManager.dornier_1_tex); break;
		case 1: bindTexture(ResourceManager.dornier_1_tex); break;
		case 2: bindTexture(ResourceManager.dornier_2_tex); break;
		case 3: bindTexture(ResourceManager.dornier_1_tex); break;
		case 4: bindTexture(ResourceManager.dornier_4_tex); break;
		case 5: bindTexture(ResourceManager.b29_0_tex); break;
		case 6: bindTexture(ResourceManager.b29_1_tex); break;
		case 7: bindTexture(ResourceManager.b29_2_tex); break;
		case 8: bindTexture(ResourceManager.b29_3_tex); break;
		case 9: bindTexture(ResourceManager.airliner_tex); break;
		case 10:bindTexture(ResourceManager.b2x_tex_mex_sex); break;
		default: bindTexture(ResourceManager.dornier_1_tex); break;
		}

		GL11.glRotatef((float) Math.sin((entity.ticksExisted + interp) * 0.05) * 10, 1F, 0F, 0F);

		switch(i) {
		case 0:
		case 1:
		case 2:
		case 3:
		case 4:
			GL11.glScalef(5F, 5F, 5F);
			GL11.glRotatef(-90, 0F, 1F, 0F);
			ResourceManager.dornier.renderAll();
			break;
		case 5:
		case 6:
		case 7:
		case 8:
			GL11.glScalef(30F / 3.1F, 30F / 3.1F, 30F / 3.1F);
			GL11.glRotatef(180, 0F, 1F, 0F);
			ResourceManager.b29.renderAll();
			break; //TODO: passenger plane model
		case 9:
			GL11.glScalef(30F / 3.1F, 30F / 3.1F, 30F / 3.1F);
			GL11.glRotatef(180, 0F, 1F, 0F);
			ResourceManager.Airliner.renderAll();
			break;
		case 10:
			
			GL11.glScalef(3,3,3);
			GL11.glTranslated(2, 0, 0);
			GL11.glRotatef(-90, 0F, 1F, 0F);
			ResourceManager.b2x.renderAll();
			bindTexture(ResourceManager.b2x_tex_mex_sex);

			float trailStretch = entity.worldObj.rand.nextFloat();
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

			
			break;
		default:
			ResourceManager.dornier.renderAll();
			break;
		}

		GL11.glEnable(GL11.GL_CULL_FACE);

		GL11.glPopMatrix();
	}

	@Override
	protected ResourceLocation getEntityTexture(Entity p_110775_1_) {
		return ResourceManager.dornier_1_tex;
	}
}
