package com.hbm.blocks.machine.rbmk;

import org.lwjgl.opengl.GL11;

import com.hbm.main.MainRegistry;
import com.hbm.main.ResourceManager;
import com.hbm.render.tileentity.RenderArcFurnace;
import com.hbm.tileentity.machine.rbmk.TileEntityRBMKGauge;

import api.hbm.block.IToolable;
import cpw.mods.fml.common.network.internal.FMLNetworkHandler;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class RBMKGauge extends RBMKMiniPanelBase implements IToolable {

	@Override
	public TileEntity createNewTileEntity(World world, int meta) {
		return new TileEntityRBMKGauge();
	}

	@Override
	public boolean onScrew(World world, EntityPlayer player, int x, int y, int z, int side, float fX, float fY, float fZ, ToolType tool) {
		if(tool != ToolType.SCREWDRIVER) return false;
		if(world.isRemote) FMLNetworkHandler.openGui(player, MainRegistry.instance, 0, world, x, y, z);
		return true;
	}

	@Override
	public void renderInventoryBlock(Block block, int meta, int modelId, Object renderBlocks) {
		super.renderInventoryBlock(block, meta, modelId, renderBlocks);
		
		GL11.glPushMatrix();
		GL11.glTranslated(0, -0.5, 0);
		GL11.glRotated(-90, 0, 1, 0);
		
		for(int i = 0; i < 4; i++) {
			
			GL11.glPushMatrix();
			GL11.glTranslated(0.25, (i / 2) * -0.5 + 0.25, (i % 2) * -0.5 + 0.25);

			GL11.glColor3f(1F, 1F, 1F);
			Minecraft.getMinecraft().getTextureManager().bindTexture(ResourceManager.rbmk_gauge_tex);
			ResourceManager.rbmk_gauge.renderPart("Gauge");
			
			GL11.glColor3f(0.5F, 0F, 0F);
			GL11.glTranslated(0, 0.4375, -0.125);
			GL11.glRotated(85, 1, 0, 0);
			GL11.glTranslated(0, -0.4375, 0.125);
			
			GL11.glDisable(GL11.GL_TEXTURE_2D);
			RenderArcFurnace.fullbright(true);
			GL11.glEnable(GL11.GL_LIGHTING);
			ResourceManager.rbmk_gauge.renderPart("Needle");
			RenderArcFurnace.fullbright(false);
			GL11.glEnable(GL11.GL_TEXTURE_2D);
			
			GL11.glPopMatrix();
		}
		
		GL11.glPopMatrix();
	}
}
