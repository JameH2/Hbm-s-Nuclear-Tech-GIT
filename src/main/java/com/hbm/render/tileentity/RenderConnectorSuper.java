package com.hbm.render.tileentity;

import org.lwjgl.opengl.GL11;

import com.hbm.blocks.ModBlocks;
import com.hbm.main.ResourceManager;
import com.hbm.render.item.ItemRenderBase;
import com.hbm.tileentity.network.TileEntityConnectorSuper;

import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.client.IItemRenderer;

public class RenderConnectorSuper extends RenderPylonBase implements IItemRendererProvider {

	@Override
	public void renderTileEntityAt(TileEntity te, double x, double y, double z, float interp) {
		TileEntityConnectorSuper con = (TileEntityConnectorSuper) te;
		
		GL11.glPushMatrix();
		GL11.glTranslated(x + 0.5D, y + 0.5D, z + 0.5D);
		GL11.glEnable(GL11.GL_LIGHTING);
		GL11.glEnable(GL11.GL_CULL_FACE);
		
		switch(te.getBlockMetadata()) {
		case 0: GL11.glRotated(180, 1, 0, 0); break;
		case 1: break;
		case 2: GL11.glRotated(90, 1, 0, 0); GL11.glRotated(180, 0, 0, 1); break;
		case 3: GL11.glRotated(90, 1, 0, 0); break;
		case 4: GL11.glRotated(90, 1, 0, 0); GL11.glRotated(90, 0, 0, 1); break;
		case 5: GL11.glRotated(90, 1, 0, 0); GL11.glRotated(270, 0, 0, 1); break;
		}

		GL11.glTranslated(0, -0.5F, 0);
		
		bindTexture(ResourceManager.connector_super_tex);
		ResourceManager.connector_super.renderAll();
		GL11.glPopMatrix();
		
		GL11.glPushMatrix();
		this.renderLinesGeneric(con, x, y, z);
		GL11.glPopMatrix();
	}

	@Override
	public Item getItemForRenderer() {
		return Item.getItemFromBlock(ModBlocks.red_connector_super);
	}

	@Override
	public IItemRenderer getRenderer() {
		return new ItemRenderBase() {
			public void renderInventory() {
				GL11.glTranslated(0, -5, 0);
				double scale = 7;
				GL11.glScaled(scale, scale, scale);
			}
			public void renderCommon() {
				GL11.glScaled(2, 2, 2);
				GL11.glShadeModel(GL11.GL_SMOOTH);
				bindTexture(ResourceManager.connector_super_tex);
				ResourceManager.connector_super.renderAll();
				GL11.glShadeModel(GL11.GL_FLAT);
			}};
	}
}
