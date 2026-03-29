package com.hbm.render.item;

import org.lwjgl.opengl.GL11;

import com.hbm.items.machine.ItemBatteryPack.EnumBatteryPack;
import com.hbm.main.ResourceManager;
import com.hbm.util.EnumUtil;

import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;

public class ItemRenderAngelCore extends ItemRenderBase {

	@Override
	public void renderInventory() {
		GL11.glTranslated(0, -3, 0);
		GL11.glScaled(4, 4, 4);

	}

	@Override
	public void renderCommonWithStack(ItemStack item) {
		GL11.glShadeModel(GL11.GL_SMOOTH);
		GL11.glScaled(4, 4, 4);

		Minecraft.getMinecraft().getTextureManager().bindTexture(ResourceManager.bfangel_tex);
		ResourceManager.bfangel.renderOnly("body");

		GL11.glShadeModel(GL11.GL_FLAT);
	}
}
