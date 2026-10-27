package com.hbm.render.tileentity;

import org.lwjgl.opengl.GL11;

import com.hbm.blocks.BlockDummyable;
import com.hbm.blocks.ModBlocks;
import com.hbm.main.ResourceManager;
import com.hbm.render.item.ItemRenderBase;
import com.hbm.tileentity.machine.TileEntityMachineHTR3;
import com.hbm.util.BobMathUtil;

import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.client.IItemRenderer;

public class RenderHTR3 extends TileEntitySpecialRenderer implements IItemRendererProvider {

	@Override
	public void renderTileEntityAt(TileEntity tile, double x, double y, double z, float interp) {
		if(!(tile instanceof TileEntityMachineHTR3)) return;
		TileEntityMachineHTR3 rocket = (TileEntityMachineHTR3) tile;

		GL11.glPushMatrix();
		{

			GL11.glTranslated(x + 0.5D, y - 3.0D, z + 0.5D);

			GL11.glEnable(GL11.GL_CULL_FACE);
			GL11.glEnable(GL11.GL_LIGHTING);
			GL11.glShadeModel(GL11.GL_SMOOTH);

			switch(tile.getBlockMetadata() - BlockDummyable.offset) {
			case 3: GL11.glRotatef(270, 0F, 1F, 0F); break;
			case 5: GL11.glRotatef(0, 0F, 1F, 0F); break;
			case 2: GL11.glRotatef(90, 0F, 1F, 0F); break;
			case 4: GL11.glRotatef(180, 0F, 1F, 0F); break;
			}

			double t = rocket.lastTime + (rocket.time - rocket.lastTime) * interp;

			double swayTimer = (t / 3D) % (Math.PI * 4);
			double sway = (Math.sin(swayTimer) + Math.sin(swayTimer * 2) + Math.sin(swayTimer * 4) + 2.23255D) * 0.5;

			double bellTimer = (t / 5D) % (Math.PI * 4);
			double h = (Math.sin(bellTimer + Math.PI) + Math.sin(bellTimer * 1.5D)) / 1.90596D;
			double v = (Math.sin(bellTimer) + Math.sin(bellTimer * 1.5D)) / 1.90596D;

			double pistonTimer = (t / 5D) % (Math.PI * 2);
			double piston = BobMathUtil.sps(pistonTimer);
			double rotorTimer = (t / 5D) % (Math.PI * 16);
			double rotor = (BobMathUtil.sps(rotorTimer) + rotorTimer / 2D - 1) / 25.1327412287D;
			double turbine = (t % 100) / 100D;

			bindTexture(ResourceManager.lpw2_tex);
			ResourceManager.htr3.renderPart("Center");

			ResourceManager.htr3.renderOnly("PipeL1", "PipeL2", "PipeR1", "PipeR2");

			renderMainAssembly(sway, h, v, piston, rotor, turbine);

			double coverTimer = (t / 5D) % (Math.PI * 4);
			double cover = (Math.sin(coverTimer) + Math.sin(coverTimer * 2) + Math.sin(coverTimer * 4)) * 0.5;

			GL11.glPushMatrix();
			GL11.glTranslated(0, 0, -cover * 0.125);
			ResourceManager.htr3.renderOnly("CoverTop", "CoverBottom");
			GL11.glPopMatrix();

			GL11.glPushMatrix();
			GL11.glTranslated(0, 0, 3.5);
			GL11.glScaled(1, 1, (3 + cover * 0.125) / 3);
			GL11.glTranslated(0, 0, -3.5);
			ResourceManager.htr3.renderOnly("SuspensionBottom", "SuspensionTop");
			GL11.glPopMatrix();

			GL11.glShadeModel(GL11.GL_FLAT);

		}
		GL11.glPopMatrix();
	}

	public static void renderMainAssembly(double sway, double h, double v, double piston, double rotor, double turbine) {
		GL11.glPushMatrix();
		GL11.glTranslated(0, 0, -sway * 0.125);

		GL11.glPushMatrix();
		GL11.glTranslated(0, 0, piston * 0.375D + 0.375D);
		ResourceManager.htr3.renderPart("Piston");
		GL11.glPopMatrix();

		renderBell(h, v);
		GL11.glPopMatrix();

		renderShroud(h, v);
	}

	public static void renderBell(double h, double v) {
		GL11.glPushMatrix();
		GL11.glTranslated(0, 3.5, -1);
		double magnitude = 2D;
		GL11.glRotated(v * magnitude, 0, 1, 0);
		GL11.glRotated(h * magnitude, 1, 0, 0);
		GL11.glTranslated(0, -3.5, 1);
		ResourceManager.htr3.renderPart("Engine");
		GL11.glPopMatrix();
	}

	public static void renderShroud(double h, double v) {

		double magnitude = 0.125D;

		GL11.glPushMatrix();
		GL11.glTranslated(0, -h * magnitude, 0);
		ResourceManager.htr3.renderPart("ShroudH");

		GL11.glPopMatrix();

		GL11.glPushMatrix();
		GL11.glTranslated(v * magnitude, 0, 0);
		ResourceManager.htr3.renderPart("ShroudV");

		GL11.glPopMatrix();
	}

	@Override
	public Item getItemForRenderer() {
		return Item.getItemFromBlock(ModBlocks.machine_htr3);
	}

	@Override
	public IItemRenderer getRenderer() {
		return new ItemRenderBase() {
			public void renderInventory() {
				GL11.glTranslated(0, -3, 0);
				GL11.glScaled(2.5, 2.5, 2.5);
			}
			public void renderCommon() {
				GL11.glScaled(0.5, 0.5, 0.5);
				GL11.glShadeModel(GL11.GL_SMOOTH);
				bindTexture(ResourceManager.lpw2_tex);
				ResourceManager.htr3.renderAllExcept("ExhaustVacuum");
				GL11.glShadeModel(GL11.GL_FLAT);
			}
		};
	}
}
