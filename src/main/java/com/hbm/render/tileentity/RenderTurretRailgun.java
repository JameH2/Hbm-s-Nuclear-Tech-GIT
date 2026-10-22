package com.hbm.render.tileentity;

import com.hbm.blocks.ModBlocks;
import com.hbm.main.ResourceManager;
import com.hbm.render.item.ItemRenderBase;
import com.hbm.tileentity.turret.TileEntityTurretRailgun;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.client.IItemRenderer;
import org.lwjgl.opengl.GL11;

public class RenderTurretRailgun extends TileEntitySpecialRenderer implements IItemRendererProvider {
	@Override
	public void renderTileEntityAt(TileEntity tile, double x, double y, double z, float interp) {
		TileEntityTurretRailgun railgun = (TileEntityTurretRailgun) tile;

		GL11.glPushMatrix();
		GL11.glTranslated(x + 0.5, y, z + 0.5);
		GL11.glEnable(GL11.GL_CULL_FACE);
		GL11.glShadeModel(GL11.GL_SMOOTH);

		bindTexture(ResourceManager.turret_railgun_tex);
		ResourceManager.turret_railgun.renderPart("Base");

		double yaw = Math.toDegrees(railgun.lastRotationYaw + (railgun.rotationYaw - railgun.lastRotationYaw) * interp);
		double pitch = Math.toDegrees(railgun.lastRotationPitch + (railgun.rotationPitch - railgun.lastRotationPitch) * interp);

		GL11.glRotated(yaw, 0, 1, 0);
		ResourceManager.turret_railgun.renderPart("Mount");

		GL11.glTranslated(0, 2.25, 0);
		GL11.glRotated(pitch, 0, 0, 1);
		GL11.glTranslated(0, -2.25, 0);
		ResourceManager.turret_railgun.renderPart("Barrel");

		if (railgun.charge > 0L) {
			GL11.glTranslated(0.09375, 0, 0);
			GL11.glDisable(GL11.GL_TEXTURE_2D);
			GL11.glEnable(GL11.GL_BLEND);
			GL11.glDisable(GL11.GL_LIGHTING);
			GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
			GL11.glDisable(GL11.GL_ALPHA_TEST);

			Tessellator tess = Tessellator.instance;

			double dl = 15D / 29D;
			double ds = 0.25;
			double dh = 0.6875;

			tess.startDrawingQuads();
			tess.setColorRGBA_F(0.4F, 0.9F, 1.0F, (float) Math.log10((double) railgun.charge / (double) railgun.maxCharge  * 9D + 1D) * ((float) (Math.random() * 0.2D + 0.8D)));

			double middle = 2.25;

			tess.addVertex(-dl, middle - dh, -ds);
			tess.addVertex(-dl, middle + dh, -ds);
			tess.addVertex(dl, middle + dh, -ds);
			tess.addVertex(dl, middle - dh, -ds);

			tess.addVertex(-dl, middle + dh, -ds);
			tess.addVertex(-dl, middle + dh, ds);
			tess.addVertex(dl, middle + dh, ds);
			tess.addVertex(dl, middle + dh, -ds);

			tess.addVertex(dl, middle - dh, -ds);
			tess.addVertex(dl, middle + dh, -ds);
			tess.addVertex(dl, middle + dh, ds);
			tess.addVertex(dl, middle - dh, ds);


			tess.addVertex(-dl, middle - dh, -ds);
			tess.addVertex(-dl, middle - dh, ds);
			tess.addVertex(-dl, middle + dh, ds);
			tess.addVertex(-dl, middle + dh, -ds);

			tess.addVertex(-dl, middle - dh, ds);
			tess.addVertex(dl, middle - dh, ds);
			tess.addVertex(dl, middle + dh, ds);
			tess.addVertex(-dl, middle + dh, ds);

			tess.addVertex(-dl, middle - dh, -ds);
			tess.addVertex(dl, middle - dh, -ds);
			tess.addVertex(dl, middle - dh, ds);
			tess.addVertex(-dl, middle - dh, ds);

			tess.draw();

			GL11.glEnable(GL11.GL_LIGHTING);
			GL11.glDisable(GL11.GL_BLEND);
			GL11.glEnable(GL11.GL_TEXTURE_2D);
			GL11.glEnable(GL11.GL_ALPHA_TEST);
		}

		GL11.glShadeModel(GL11.GL_FLAT);
		GL11.glPopMatrix();
	}

	@Override
	public Item getItemForRenderer() {
		return Item.getItemFromBlock(ModBlocks.turret_railgun);
	}

	@Override
	public IItemRenderer getRenderer() {
		return new ItemRenderBase() {

			public void renderInventory() {
				GL11.glTranslated(0, -1.5, 0);
				GL11.glScaled(4, 4, 4);
			}
			public void renderCommonWithStack(ItemStack item) {
				GL11.glScaled(0.6, 0.6, 0.6);
				GL11.glShadeModel(GL11.GL_SMOOTH);
				bindTexture(ResourceManager.turret_railgun_tex);

				ResourceManager.turret_railgun.renderPart("Base");
				ResourceManager.turret_railgun.renderPart("Mount");
				GL11.glTranslated(0, 2.25, 0);
				GL11.glRotated(45, 0, 0, 1);
				GL11.glTranslated(0, -2.25, 0);
				ResourceManager.turret_railgun.renderPart("Barrel");

				GL11.glShadeModel(GL11.GL_FLAT);
			}
		};
	}
}
