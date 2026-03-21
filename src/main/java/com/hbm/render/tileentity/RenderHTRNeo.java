package com.hbm.render.tileentity;

import org.lwjgl.opengl.GL11;

import com.hbm.blocks.BlockDummyable;
import com.hbm.blocks.ModBlocks;
import com.hbm.main.ResourceManager;
import com.hbm.render.item.ItemRenderBase;
import com.hbm.tileentity.machine.TileEntityMachineHTRNeo;
import com.hbm.util.Clock;

import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.client.IItemRenderer;

public class RenderHTRNeo extends TileEntitySpecialRenderer implements IItemRendererProvider {

	@Override
	public void renderTileEntityAt(TileEntity tile, double x, double y, double z, float interp) {
		if(!(tile instanceof TileEntityMachineHTRNeo)) return;
		TileEntityMachineHTRNeo rocket = (TileEntityMachineHTRNeo) tile;

		GL11.glPushMatrix();
		GL11.glTranslated(x + 0.5D, y - 2.0D, z + 0.5D);
		GL11.glEnable(GL11.GL_LIGHTING);

		switch(tile.getBlockMetadata() - BlockDummyable.offset) {
		case 3: GL11.glRotatef(270, 0F, 1F, 0F); break;
		case 5: GL11.glRotatef(0, 0F, 1F, 0F); break;
		case 2: GL11.glRotatef(90, 0F, 1F, 0F); break;
		case 4: GL11.glRotatef(180, 0F, 1F, 0F); break;
		}

		float rot = rocket.prevRotor + (rocket.rotor - rocket.prevRotor) * interp;

		GL11.glShadeModel(GL11.GL_SMOOTH);
		bindTexture(ResourceManager.htrf4_neo_tex);
		ResourceManager.htrf4_neo.renderOnly("Base", "Engine");

		GL11.glPushMatrix();
		{

			GL11.glTranslatef(0, 2.5F, 0);
			GL11.glRotatef(rot, 0, 0, 1);
			GL11.glTranslatef(0, -2.5F, 0);
			ResourceManager.htrf4_neo.renderOnly("Rotor", "Rotor2");

		}
		GL11.glPopMatrix();
		

		float trailStretch = tile.getWorldObj().rand.nextFloat();
		trailStretch = 1.2F - (trailStretch * trailStretch * 0.1F);
		trailStretch *= rocket.thrustAmount;
		
		if(trailStretch > 0) {
			GL11.glColor4d(rocket.plasmaR, rocket.plasmaG, rocket.plasmaB, rocket.thrustAmount);

			GL11.glDisable(GL11.GL_CULL_FACE);
			GL11.glDisable(GL11.GL_LIGHTING);
			GL11.glEnable(GL11.GL_BLEND);
			OpenGlHelper.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO);
			GL11.glPushAttrib(GL11.GL_LIGHTING_BIT);
			OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240F, 240F);
			GL11.glDepthMask(false);
			
			GL11.glTranslatef(0, 0, 12);
			GL11.glScalef(1, 1, trailStretch);
			GL11.glTranslatef(0, 0, -12);

			bindTexture(ResourceManager.htrf4_exhaust_tex);
			ResourceManager.htrf4_neo.renderPart("Exhaust");

			
			GL11.glMatrixMode(GL11.GL_TEXTURE);
			GL11.glLoadIdentity();

			long time = Clock.get_ms();
			double sparkleSpin = time / 250D % 1D;
			
			GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
			GL11.glColor4d(rocket.plasmaR * 2, rocket.plasmaG * 2, rocket.plasmaB * 2, rocket.thrustAmount * 0.75);

			GL11.glRotatef(-90, 0, 1, 0); // it's wrong but it looks cooler as a series of concentric rings so whatever
			GL11.glTranslated(0, sparkleSpin, 0);
			
			bindTexture(ResourceManager.fusion_plasma_sparkle_tex);
			ResourceManager.htrf4_neo.renderPart("Exhaust");

			GL11.glLoadIdentity();
			GL11.glMatrixMode(GL11.GL_MODELVIEW);


			
			GL11.glDepthMask(true);
			GL11.glPopAttrib();
			GL11.glEnable(GL11.GL_LIGHTING);
			GL11.glEnable(GL11.GL_CULL_FACE);
			GL11.glDisable(GL11.GL_BLEND);

			GL11.glColor4d(1, 1, 1, 1);
		}
		
		GL11.glShadeModel(GL11.GL_FLAT);


		GL11.glPopMatrix();
	}

	@Override
	public Item getItemForRenderer() {
		return Item.getItemFromBlock(ModBlocks.machine_htrf4neo);
	}

	@Override
	public IItemRenderer getRenderer() {
		return new ItemRenderBase() {
			public void renderInventory() {
				GL11.glTranslated(0, -1, 0);
				GL11.glScaled(1.5, 1.5, 1.5);
			}
			public void renderCommon() {
				GL11.glScaled(0.5, 0.5, 0.5);
				GL11.glShadeModel(GL11.GL_SMOOTH);
				bindTexture(ResourceManager.htrf4_neo_tex);
				ResourceManager.htrf4_neo.renderAll();
				GL11.glShadeModel(GL11.GL_FLAT);
			}
		};
	}
}
