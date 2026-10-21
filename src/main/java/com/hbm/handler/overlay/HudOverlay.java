package com.hbm.handler.overlay;

import java.util.List;


import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.entity.player.EntityPlayer;

@SideOnly(Side.CLIENT)
public abstract class HudOverlay extends Gui {

	public int guiLeft, guiTop;
	public int screenWidth, screenHeight;

	public abstract boolean isVisible(Minecraft mc, EntityPlayer player);

	public int[] getAnchor(int screenWidth, int screenHeight) {
		return new int[] { 0, 0 };
	}

	public boolean sortOnSide() {
		return false;
	}

	public int[] getSize() {
		return new int[] { 0, 0 };
	}

	public abstract void drawScreen(int mouseX, int mouseY, float partialTicks);

	public boolean mouseClicked(int mouseX, int mouseY, int button) {
		return false;
	}

	public boolean checkClick(int mouseX, int mouseY, int left, int top, int sizeX, int sizeY) {
		return mouseX >= this.guiLeft + left && mouseX < this.guiLeft + left + sizeX
				&& mouseY >= this.guiTop + top && mouseY < this.guiTop + top + sizeY;
	}

	public static boolean interactionsEnabled() {
		Minecraft mc = Minecraft.getMinecraft();
		return mc.theWorld != null && mc.thePlayer != null && mc.currentScreen == null && !mc.gameSettings.hideGUI;
	}

	public void drawLookOverlay(int mouseX, int mouseY, String title, int titleCol, int bgCol, List<String> lines) {

		FontRenderer font = Minecraft.getMinecraft().fontRenderer;
		int width = font.getStringWidth(title);

		for(String line : lines) {
			String text = line.startsWith("&[") && line.contains("&]") ? line.substring(line.lastIndexOf("&]") + 2) : line;
			width = Math.max(width, font.getStringWidth(text));
		}

		// The existing ILookOverlay owns the right side of the crosshair.
		int x = mouseX - width - 12;
		int y = mouseY - 10;
		if(y + 10 + lines.size() * 10 > this.screenHeight - 4) y = this.screenHeight - 14 - lines.size() * 10;
		if(x < 4) x = 4;
		if(y < 4) y = 4;

		font.drawString(title, x + 1, y - 1, bgCol);
		font.drawString(title, x, y - 2, titleCol);

		int lineY = y + 8;
		for(String line : lines) {

			int color = 0xFFFFFF;
			String text = line;

			try {
				if(text.startsWith("&[")) {
					int end = text.lastIndexOf("&]");
					color = Integer.parseInt(text.substring(2, end));
					text = text.substring(end + 2);
				}
			} catch(Exception ex) { }

			font.drawStringWithShadow(text, x, lineY, color);
			lineY += 10;
		}
	}
}
