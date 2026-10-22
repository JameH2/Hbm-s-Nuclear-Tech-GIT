package com.hbm.handler.overlay;

import java.util.ArrayList;
import java.util.List;

import com.hbm.inventory.fluid.FluidType;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;

@SideOnly(Side.CLIENT)
public class FluidDuctOverlay extends HudOverlay {

	@Override
	public boolean isVisible(Minecraft mc, EntityPlayer player) {
		return player != null && FluidDuctHover.hoveredType != null;
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {

		FluidType type = FluidDuctHover.hoveredType;
		if(type == null) return;

		List<String> lines = new ArrayList<>();
		lines.add("Connected pipes: " + FluidDuctHover.network.size());
		lines.add("Connected machines: " + FluidDuctHover.machineCount);

		drawLookOverlay(mouseX, mouseY, type.getLocalizedName() + " Network", type.getColor(), (type.getColor() & 0xFEFEFE) >> 1, lines);
	}
}
