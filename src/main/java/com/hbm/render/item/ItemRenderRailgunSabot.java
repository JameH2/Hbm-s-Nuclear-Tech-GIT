package com.hbm.render.item;

import com.hbm.items.weapon.ItemAmmoRailgun;
import com.hbm.main.ResourceManager;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import org.lwjgl.opengl.GL11;

public class ItemRenderRailgunSabot extends ItemRenderBase {

	@Override
	public void renderInventory() {
		GL11.glTranslated(0, 3, 0);
		GL11.glScaled(7.5, 7.5, 7.5);
	}

	@Override
	public void renderCommonWithStack(ItemStack item) {
		ItemAmmoRailgun.RailgunSabot sabot = ItemAmmoRailgun.itemTypes[item.getItemDamage()];
		GL11.glShadeModel(GL11.GL_SMOOTH);
		Minecraft.getMinecraft().getTextureManager().bindTexture(sabot.texture);
		GL11.glRotated(-45, 1, 1, 0);
		GL11.glRotatef(System.currentTimeMillis() / 25 % 360, 0, -1, 0);
		ResourceManager.railgun_sabot.renderAll();
		GL11.glShadeModel(GL11.GL_FLAT);
	}
}
