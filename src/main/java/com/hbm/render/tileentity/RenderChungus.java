package com.hbm.render.tileentity;

import org.lwjgl.opengl.GL11;

import com.hbm.blocks.BlockDummyable;
import com.hbm.blocks.ModBlocks;
import com.hbm.main.ResourceManager;
import com.hbm.render.item.ItemRenderBase;
import com.hbm.tileentity.machine.TileEntityChungus;

import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.client.IItemRenderer;

public class RenderChungus extends TileEntitySpecialRenderer implements IItemRendererProvider {

	@Override
	public void renderTileEntityAt(TileEntity tile, double x, double y, double z, float f) {

		GL11.glPushMatrix();
		GL11.glTranslated(x + 0.5D, y, z + 0.5D);
		GL11.glEnable(GL11.GL_LIGHTING);
		GL11.glEnable(GL11.GL_CULL_FACE);

		GL11.glRotatef(90, 0F, 1F, 0F);

		switch(tile.getBlockMetadata() - BlockDummyable.offset) {
		case 2:
			GL11.glRotatef(90, 0F, 1F, 0F);
			break;
		case 4:
			GL11.glRotatef(180, 0F, 1F, 0F);
			break;
		case 3:
			GL11.glRotatef(270, 0F, 1F, 0F);
			break;
		case 5:
			GL11.glRotatef(0, 0F, 1F, 0F);
			break;
		}

		TileEntityChungus turbine = (TileEntityChungus) tile;

		GL11.glTranslated(0, 0, -3);

		GL11.glShadeModel(GL11.GL_SMOOTH);

		if(turbine.damaged) {
			bindTexture(ResourceManager.chungus_destroyed_tex);

			ResourceManager.chungus_destroyed.renderAll();
		} else {
			bindTexture(ResourceManager.chungus_tex);

			ResourceManager.chungus.renderPart("Body");

			GL11.glPushMatrix();
			GL11.glTranslated(0, 0, 4.5);
			GL11.glRotatef(15 - (turbine.tanks[0].getTankType().ordinal() - 2) * 10, 1, 0, 0);
			GL11.glTranslated(0, 0, -4.5);
			ResourceManager.chungus.renderPart("Lever");
			GL11.glPopMatrix();

			GL11.glTranslated(0, 2.5, 0);
			GL11.glRotatef(turbine.lastRotor + (turbine.rotor - turbine.lastRotor) * f, 0, 0, -1);
			GL11.glTranslated(0, -2.5, 0);

			ResourceManager.chungus.renderPart("Blades");
		}

		GL11.glShadeModel(GL11.GL_FLAT);

		GL11.glPopMatrix();

	}

	@Override
	public Item getItemForRenderer() {
		return Item.getItemFromBlock(ModBlocks.machine_chungus);
	}

	@Override
	public IItemRenderer getRenderer() {
		return new ItemRenderBase() {
			public void renderInventory() {
				GL11.glTranslated(0.5, 0, 0);
				GL11.glScaled(2.5, 2.5, 2.5);
			}
			public void renderCommonWithStack(ItemStack stack) {
				GL11.glScaled(0.5, 0.5, 0.5);
				GL11.glRotated(90, 0, 1, 0);

				boolean damaged = false;
				if(stack.hasTagCompound()) {
					damaged = stack.getTagCompound().getBoolean("damaged");
				}

				GL11.glShadeModel(GL11.GL_SMOOTH);

				if(damaged) {
					bindTexture(ResourceManager.chungus_destroyed_tex);
					ResourceManager.chungus_destroyed.renderAll();
				} else {
					bindTexture(ResourceManager.chungus_tex);
					ResourceManager.chungus.renderPart("Body");
					ResourceManager.chungus.renderPart("Lever");
					ResourceManager.chungus.renderPart("Blades");
				}

				GL11.glShadeModel(GL11.GL_FLAT);
			}
		};
	}

}
