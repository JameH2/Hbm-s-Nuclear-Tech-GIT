package com.hbm.handler.overlay;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;

@SideOnly(Side.CLIENT)
public class MachineOverlay extends HudOverlay {

	@Override
	public boolean isVisible(Minecraft mc, EntityPlayer player) {
		return player != null && MachineHover.machineTitle != null;
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		if(MachineHover.machineTitle == null) return;
		drawLookOverlay(mouseX, mouseY, MachineHover.machineTitle, 0xffff00, 0x404000, MachineHover.machineLines);
	}
}
