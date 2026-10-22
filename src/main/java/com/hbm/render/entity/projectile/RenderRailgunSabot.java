package com.hbm.render.entity.projectile;

import com.hbm.entity.projectile.EntityRailgunProjectile;
import com.hbm.items.weapon.ItemAmmoRailgun;
import com.hbm.main.ResourceManager;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

public class RenderRailgunSabot extends Render {
	@Override
	public void doRender(Entity entity, double x, double y, double z, float f0, float f1) {
		GL11.glPushMatrix();
		GL11.glTranslated(x, y, z);
		GL11.glRotatef(entity.prevRotationYaw + (entity.rotationYaw - entity.prevRotationYaw) * f1 - 90.0F, 0.0F, 1.0F, 0.0F);
		GL11.glRotatef(entity.prevRotationPitch + (entity.rotationPitch - entity.prevRotationPitch) * f1 - 90, 0.0F, 0.0F, 1.0F);

		float scale = 1.5F;
		GL11.glScalef(scale, scale, scale);

		this.bindEntityTexture(entity);

		boolean fog = GL11.glIsEnabled(GL11.GL_FOG);

		if(fog) GL11.glDisable(GL11.GL_FOG);
		GL11.glShadeModel(GL11.GL_SMOOTH);
		EntityRailgunProjectile sabot = (EntityRailgunProjectile) entity;
		ResourceManager.railgun_sabot.renderPart("Sabot");
		if (!sabot.sabotSeparation) {
			ResourceManager.railgun_sabot.renderPart("Jacket");
		}
		GL11.glShadeModel(GL11.GL_FLAT);
		if(fog) GL11.glEnable(GL11.GL_FOG);

		GL11.glPopMatrix();
	}

	@Override
	protected ResourceLocation getEntityTexture(Entity entity) {
		EntityRailgunProjectile sabot = (EntityRailgunProjectile) entity;
		return ItemAmmoRailgun.itemTypes[sabot.getDataWatcher().getWatchableObjectInt(10)].texture;
	}
}
