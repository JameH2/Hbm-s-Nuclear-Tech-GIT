package com.hbm.inventory.gui;

import org.lwjgl.opengl.GL11;

import com.hbm.inventory.container.ContainerChestInventory;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.resources.I18n;
import net.minecraft.inventory.IInventory;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.util.ResourceLocation;

@SideOnly(Side.CLIENT)
public class GUIChestInventory extends GuiContainer {

	private static final ResourceLocation texture = new ResourceLocation("textures/gui/container/generic_54.png");
	private IInventory playerInv;
	private int inventoryRows;

	public GUIChestInventory(IInventory playerInv, TileEntityChest chest) {
		super(new ContainerChestInventory(playerInv, chest));
		this.playerInv = playerInv;
		this.allowUserInput = false;
		this.inventoryRows = chest.getSizeInventory() / 9;
		this.ySize = 222 - 108 + this.inventoryRows * 18;
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
		fontRendererObj.drawString(I18n.format("container.chest", new Object[0]), 8, 6, 4210752);
		fontRendererObj.drawString(playerInv.hasCustomInventoryName() ? playerInv.getInventoryName() : I18n.format(playerInv.getInventoryName(), new Object[0]), 8, ySize - 96 + 2, 4210752);
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float mouseX, int mouseY, int f) {
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		mc.getTextureManager().bindTexture(texture);
		int k = (width - xSize) / 2;
		int l = (height - ySize) / 2;
		drawTexturedModalRect(k, l, 0, 0, xSize, inventoryRows * 18 + 17);
		drawTexturedModalRect(k, l + inventoryRows * 18 + 17, 0, 126, xSize, 96);
	}

}