package com.hbm.render.tileentity;

import java.util.Random;

import org.lwjgl.opengl.GL11;

import com.hbm.main.ResourceManager;
import com.hbm.tileentity.machine.storage.TileEntityCombatDropPod;
import com.hbm.util.fauxpointtwelve.BlockPos;

import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;

public class RenderCombatPod extends TileEntitySpecialRenderer {
	public static Random rand = new Random();

	@Override
	public void renderTileEntityAt(TileEntity te, double x, double y, double z, float i) {
	TileEntityCombatDropPod pod = (TileEntityCombatDropPod) te;	
        GL11.glPushMatrix();
        GL11.glTranslated(x + 0.5, y+1.5, z + 0.5);
        GL11.glEnable(GL11.GL_LIGHTING);

        GL11.glTranslatef(0.0F, -0.25F, 0.0F);
        rand.setSeed(BlockPos.getIdentity(te.xCoord, te.yCoord, te.zCoord));
		double yaw = rand.nextDouble() * 360;
		double pitch = rand.nextDouble() * 8;
		double roll = rand.nextDouble() * 6;

	 GL11.glRotated(yaw, 0, 1, 0);
	 GL11.glRotated(pitch, 1, 0, 0);
	 GL11.glRotated(roll, 0, 0, 1);

        int color = pod.color;
        double open = pod.prevHatchopen + (pod.hatchopen - pod.prevHatchopen) * i;
        double open2 = pod.prevHatchopen2 + (pod.hatchopen2 - pod.prevHatchopen2) * i;
        
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
        
        GL11.glEnable(GL11.GL_CULL_FACE);
        GL11.glShadeModel(GL11.GL_SMOOTH);
        GL11.glPushMatrix();
        GL11.glTranslated(0, open*0.013, open*0.01);
        GL11.glRotated(open, 1, 0, 0);
        ResourceManager.combat_pod.renderPart("hatch1");
        
        GL11.glPopMatrix();

        GL11.glPushMatrix();
        GL11.glTranslated(0, -open2*0.013, -open*0.01);
        GL11.glRotated(open2, 1, 0, 0);

        ResourceManager.combat_pod.renderPart("hatch2");
        GL11.glPopMatrix();
        ResourceManager.combat_pod.renderPart("bomb");

        GL11.glShadeModel(GL11.GL_FLAT);
        
        GL11.glPopMatrix();
	}

}
