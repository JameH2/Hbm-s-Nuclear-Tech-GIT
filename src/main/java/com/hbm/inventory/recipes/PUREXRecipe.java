package com.hbm.inventory.recipes;

import com.hbm.inventory.recipes.loader.GenericRecipe;
import com.hbm.util.BobMathUtil;
import com.hbm.util.i18n.I18nUtil;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;

public class PUREXRecipe extends GenericRecipe {

	public PUREXRecipe(String name) {
		super(name);
	}

	@Override
	public void printNEIExtras() {

		FontRenderer fontRenderer = Minecraft.getMinecraft().fontRenderer;
		String line = I18nUtil.resolveKey("nei.purex.extras", BobMathUtil.getShortNumber(this.duration), BobMathUtil.getShortNumber(this.power));

		int side = 164;
		fontRenderer.drawString(line, side - fontRenderer.getStringWidth(line), 57, 0x306030); // why so geen
	}
}
