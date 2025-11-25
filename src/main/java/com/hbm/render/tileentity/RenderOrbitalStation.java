package com.hbm.render.tileentity;

import org.lwjgl.opengl.GL11;

import com.hbm.blocks.BlockDummyable;
import com.hbm.blocks.ModBlocks;
import com.hbm.main.ResourceManager;
import com.hbm.render.item.ItemRenderBase;
import com.hbm.render.util.MissilePronter;
import com.hbm.tileentity.machine.TileEntityOrbitalStation;
import com.hbm.tileentity.machine.TileEntityOrbitalStationLauncher;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.client.IItemRenderer;

public class RenderOrbitalStation extends TileEntitySpecialRenderer implements IItemRendererProvider {

	@Override
	public void renderTileEntityAt(TileEntity te, double x, double y, double z, float interp) {
		float armRotation;
		if(te instanceof TileEntityOrbitalStation) {
			TileEntityOrbitalStation station = (TileEntityOrbitalStation) te;
			armRotation = station.prevRot + (station.rot - station.prevRot) * interp;
			bindTexture(ResourceManager.docking_port_tex);
		} else if(te instanceof TileEntityOrbitalStationLauncher) {
			TileEntityOrbitalStationLauncher station = (TileEntityOrbitalStationLauncher) te;
			armRotation = station.prevRot + (station.rot - station.prevRot) * interp;
			bindTexture(ResourceManager.docking_port_launcher_tex);
		} else {
			return;
		}

		GL11.glPushMatrix();
		{

			GL11.glTranslated(x + 0.5D, y + 1.0D, z + 0.5D);
			GL11.glEnable(GL11.GL_LIGHTING);

			switch(te.getBlockMetadata() - BlockDummyable.offset) {
			case 2: GL11.glRotatef(0, 0F, 1F, 0F); break;
			case 4: GL11.glRotatef(90, 0F, 1F, 0F); break;
			case 3: GL11.glRotatef(180, 0F, 1F, 0F); break;
			case 5: GL11.glRotatef(270, 0F, 1F, 0F); break;
			}

			GL11.glShadeModel(GL11.GL_SMOOTH);

			ResourceManager.docking_port.renderPart("Port");

			for(int i = 0; i < 4; i++) {
				GL11.glPushMatrix();
				{

					// one hop this time
					GL11.glTranslatef(0, -1.75F, -2);

					// criss cross
					GL11.glRotatef(-armRotation, 1, 0, 0);

					// one hop this time
					GL11.glTranslatef(0, 1.75F, 2);

					// let's go to work
					ResourceManager.docking_port.renderPart("ArmZP");

				}
				GL11.glPopMatrix();

				// cha cha real smooth
				GL11.glRotatef(90, 0, 1, 0);
			}

			if(te instanceof TileEntityOrbitalStationLauncher) {
				TileEntityOrbitalStationLauncher launcher = (TileEntityOrbitalStationLauncher) te;

				if(launcher.rocket != null && launcher.rocket.extraIssues.size() == 0) {
					GL11.glPushMatrix();
					{

						GL11.glTranslated(0, -launcher.rocket.getHeight() - 0.5, 0);

						MissilePronter.prontRocket(launcher.rocket, Minecraft.getMinecraft().getTextureManager(), false);

					}
					GL11.glPopMatrix();
				}
			}

			GL11.glShadeModel(GL11.GL_FLAT);

		}
		GL11.glPopMatrix();
	}

	@Override
	public IItemRenderer getRenderer() {
		return new ItemRenderBase() {
			public void renderInventory() {
				GL11.glTranslated(0, 2, 0);
				GL11.glScaled(2, 2, 2);
			}
			public void renderCommonWithStack(ItemStack stack) {
				GL11.glDisable(GL11.GL_CULL_FACE);
				GL11.glShadeModel(GL11.GL_SMOOTH);
				ItemBlock itemBlock = (ItemBlock) stack.getItem();
				if(itemBlock.field_150939_a == ModBlocks.orbital_station_launcher) {
					bindTexture(ResourceManager.docking_port_launcher_tex);
				} else {
					bindTexture(ResourceManager.docking_port_tex);
				}
				ResourceManager.docking_port.renderAll();
				GL11.glShadeModel(GL11.GL_FLAT);
				GL11.glEnable(GL11.GL_CULL_FACE);
			}
		};
	}

	@Override
	public Item getItemForRenderer() {
		return Item.getItemFromBlock(ModBlocks.orbital_station_port);
	}

	@Override
	public Item[] getItemsForRenderer() {
		return new Item[] {
			Item.getItemFromBlock(ModBlocks.orbital_station),
			Item.getItemFromBlock(ModBlocks.orbital_station_port),
			Item.getItemFromBlock(ModBlocks.orbital_station_launcher),
		};
	}

}
