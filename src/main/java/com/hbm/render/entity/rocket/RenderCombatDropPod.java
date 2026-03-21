package com.hbm.render.entity.rocket;

import org.lwjgl.opengl.GL11;

import com.hbm.entity.missile.EntityCombatDropPod;
import com.hbm.main.ResourceManager;

import net.minecraft.client.renderer.entity.Render;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;

public class RenderCombatDropPod extends Render {

	@Override
	public void doRender(Entity entity, double x, double y, double z, float i, float j) {
		
		GL11.glPushMatrix();
		
        GL11.glTranslated(x, y, z);
        EntityCombatDropPod pod = (EntityCombatDropPod) entity;
        int color = pod.getColor();
        double time = (entity.worldObj.getTotalWorldTime());

        int height = 7;
        
        GL11.glTranslated(0.0F, height, 0.0F);
        GL11.glTranslated(0.0F, -height, 0.0F);
        
        GL11.glEnable(GL11.GL_CULL_FACE);
        GL11.glShadeModel(GL11.GL_SMOOTH);

        switch(color) {
        case 0:
              bindTexture(ResourceManager.combat_pod_skin_white);
              break;
        case 1:
            bindTexture(ResourceManager.combat_pod_skin_red);
            break;
        case 2:
            bindTexture(ResourceManager.combat_pod_skin_yellow);
            break;
        default:
            bindTexture(ResourceManager.combat_pod_skin_white);
            break;
        }
        ResourceManager.combat_pod.renderAll();

        
        GL11.glShadeModel(GL11.GL_FLAT);
        
		GL11.glPopMatrix();
	}

	@Override
	protected ResourceLocation getEntityTexture(Entity p_110775_1_) {
		return ResourceManager.combat_pod_skin_yellow;
	}
}
