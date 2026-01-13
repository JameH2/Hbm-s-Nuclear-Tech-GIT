package com.hbm.inventory.gui;

import java.util.ArrayList;
import java.util.List;

import org.lwjgl.opengl.GL11;

import com.hbm.inventory.container.ContainerHydroponic;
import com.hbm.lib.RefStrings;
import com.hbm.tileentity.machine.TileEntityHydroponic;
import com.hbm.util.i18n.I18nUtil;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

public class GUIHydroponic extends GuiInfoContainer {

	private static final ResourceLocation texture = new ResourceLocation( RefStrings.MODID + ":textures/gui/machine/gui_hydrobay.png");

	TileEntityHydroponic hydro;

	public GUIHydroponic(InventoryPlayer invPlayer, TileEntityHydroponic hydro) {
		super(new ContainerHydroponic(invPlayer, hydro));
		this.hydro = hydro;

		xSize = 176;
		ySize = 186;
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float f) {
		super.drawScreen(mouseX, mouseY, f);

		hydro.tanks[0].renderTankInfo(this, mouseX, mouseY, guiLeft + 13, guiTop + 70 - 52, 16, 52);
		hydro.tanks[1].renderTankInfo(this, mouseX, mouseY, guiLeft + 31, guiTop + 70 - 52, 16, 52);
		drawElectricityInfo(this, mouseX, mouseY, guiLeft + 147, guiTop + 52 - 34, 16, 34, hydro.power, TileEntityHydroponic.maxPower);

		if(mc.thePlayer.inventory.getItemStack() == null && isMouseOverSlot(this.inventorySlots.getSlot(1), mouseX, mouseY) && !inventorySlots.getSlot(1).getHasStack()) {
			ItemStack[] validItems = hydro.getValidFertilizers();
			int cycle = (int) ((System.currentTimeMillis() % (1000 * validItems.length)) / 1000);
			ItemStack selected = validItems[cycle];
			selected.stackSize = 0;

			List<Object[]> lines = new ArrayList<>();
			lines.add(validItems);
			lines.add(new Object[] {I18nUtil.resolveKey(selected.getDisplayName())});
			drawStackText(lines, mouseX, mouseY, this.fontRendererObj);
		}
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int i, int j) {
		String name = hydro.hasCustomInventoryName() ? hydro.getInventoryName() : I18n.format(hydro.getInventoryName());

		this.fontRendererObj.drawString(name, 97 - this.fontRendererObj.getStringWidth(name) / 2, 5, 4210752);
		this.fontRendererObj.drawString(I18n.format("container.inventory"), 8, this.ySize - 96 + 2, 4210752);
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float interp, int x, int y) {
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		Minecraft.getMinecraft().getTextureManager().bindTexture(texture);
		drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);

		int power = (int) (hydro.power * 34 / TileEntityHydroponic.maxPower);
		drawTexturedModalRect(guiLeft + 147, guiTop + 52 - power, xSize, 34 - power, 16, power);

		int fertilizer = hydro.fertilizer * 32 / TileEntityHydroponic.maxFertilizer;
		drawTexturedModalRect(guiLeft + 102, guiTop + 69 - fertilizer, xSize + 16, 32 - fertilizer, 6, fertilizer);

		if(hydro.power >= 200) {
			drawTexturedModalRect(guiLeft + 151, guiTop + 4, xSize, 34, 9, 12);
		}

		hydro.tanks[0].renderTank(guiLeft + 13, guiTop + 70, this.zLevel, 16, 52);
		hydro.tanks[1].renderTank(guiLeft + 31, guiTop + 70, this.zLevel, 16, 52);
	}

}
