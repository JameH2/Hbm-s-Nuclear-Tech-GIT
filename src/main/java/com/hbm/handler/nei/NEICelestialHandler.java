package com.hbm.handler.nei;

import static codechicken.lib.gui.GuiDraw.changeTexture;
import static codechicken.lib.gui.GuiDraw.drawStringC;

import java.util.HashMap;
import java.util.Map.Entry;

import com.hbm.dim.CelestialBody;
import com.hbm.items.ModItems;
import com.hbm.util.Clock;
import com.hbm.util.InventoryUtil;
import com.hbm.util.i18n.I18nUtil;

import codechicken.nei.NEIServerUtils;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public abstract class NEICelestialHandler extends NEIUniversalHandler {

	public NEICelestialHandler(String display, ItemStack machine[], HashMap recipes) { super(display, machine, recipes); }
	public NEICelestialHandler(String display, HashMap recipes, HashMap machines) { super(display, recipes, machines); }
	public NEICelestialHandler(String display, ItemStack machine, HashMap recipes) { super(display, machine, recipes); }
	public NEICelestialHandler(String display, Item machine, HashMap recipes) { super(display, machine, recipes); }
	public NEICelestialHandler(String display, Block machine, HashMap recipes) { super(display, machine, recipes); }



	protected HashMap<Integer, CelestialBody> recipeInputCache = new HashMap<>();

	@Override
	public void drawBackground(int recipe) {
		super.drawBackground(recipe);

		CelestialBody body = recipeInputCache.get(recipe);
		changeTexture(body.texture);

		double uvOffset = (double)(Clock.get_ms() % 4000) / 4000;

		double minX = 32;
		double minY = 12;
		double maxX = minX + 32;
		double maxY = minY + 32;

		Tessellator tessellator = Tessellator.instance;
		tessellator.startDrawingQuads();
		tessellator.addVertexWithUV(minX, maxY, 0, 0 + uvOffset, 1);
		tessellator.addVertexWithUV(maxX, maxY, 0, 1 + uvOffset, 1);
		tessellator.addVertexWithUV(maxX, minY, 0, 1 + uvOffset, 0);
		tessellator.addVertexWithUV(minX, minY, 0, 0 + uvOffset, 0);
		tessellator.draw();

		drawStringC(I18nUtil.resolveKey("body." + body.name), 16, 48, 64, 12, 0x000000, false);
	}


	// Gotta skip trying to read the recipe inputs because they are not ItemStacks

	@Override
	public void loadCraftingRecipes(String outputId, Object... results) {

		if(outputId.equals(getKey())) {

			outer: for(Entry<Object, Object> recipe : recipes.entrySet()) {
				ItemStack[][] ins = new ItemStack[0][0];
				ItemStack[][] outs = InventoryUtil.extractObject(recipe.getValue());

				for(ItemStack[] array : outs) for(ItemStack stack : array) if(stack.getItem() == ModItems.item_secret) continue outer;

				recipeInputCache.put(arecipes.size(), (CelestialBody) recipe.getKey());
				arecipes.add(new RecipeSet(ins, outs, recipe.getKey()));
			}

		} else {
			super.loadCraftingRecipes(outputId, results);
		}
	}

	@Override
	public void loadCraftingRecipes(ItemStack result) {

		outer: for(Entry<Object, Object> recipe : recipes.entrySet()) {
			ItemStack[][] ins = new ItemStack[0][0];
			ItemStack[][] outs = InventoryUtil.extractObject(recipe.getValue());

			for(ItemStack[] array : outs) for(ItemStack stack : array) if(stack.getItem() == ModItems.item_secret) continue outer;

			match:
			for(ItemStack[] array : outs) {
				for(ItemStack stack : array) {
					if(NEIServerUtils.areStacksSameTypeCrafting(stack, result)) {
						recipeInputCache.put(arecipes.size(), (CelestialBody) recipe.getKey());
						arecipes.add(new RecipeSet(ins, outs, recipe.getKey()));
						break match;
					}
				}
			}
		}
	}

	@Override
	public void loadUsageRecipes(ItemStack ingredient) {
		// nop
	}

}
