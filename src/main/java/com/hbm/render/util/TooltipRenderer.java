package com.hbm.render.util;

import java.util.ArrayList;
import java.util.List;

import javax.vecmath.Vector2f;

import org.lwjgl.opengl.GL11;

import com.hbm.items.ModItems;
import com.hbm.items.machine.ItemPWRFuel;
import com.hbm.items.machine.ItemPWRFuel.EnumPWRFuel;
import com.hbm.items.machine.ItemRBMKRod;
import com.hbm.items.machine.ItemWatzPellet;
import com.hbm.items.machine.ItemWatzPellet.EnumWatzType;
import com.hbm.util.EnumUtil;
import com.hbm.util.function.Function;
import com.hbm.util.i18n.I18nUtil;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MathHelper;

public class TooltipRenderer {

	private static ItemStack stack;
	private static List<String> tooltip;

	public static void reserve(ItemStack item, List<String> tooltip) {

		if(item == null) return;

		if(hasGraphs(item)) {
			tooltip.add(I18nUtil.resolveKey(hasDepletionCurve(item) ? "desc.fuel.graphLegend" : "desc.fuel.graphLegendFlux"));
			// NEI reserves and draws its graph through a native tooltip line handler.
			if(!usesNEITooltip()) for(int i = 0; i < 4; i++) tooltip.add("");
		}
	}

	public static boolean usesNEITooltip() {
		return cpw.mods.fml.common.Loader.isModLoaded("NotEnoughItems")
				&& Minecraft.getMinecraft().currentScreen instanceof net.minecraft.client.gui.inventory.GuiContainer;
	}

	/** Draw at the position assigned by the tooltip renderer, not guessed mouse bounds. */
	public static void renderGraph(ItemStack fuel, int x, int y, int width, double z) {
		GL11.glPushAttrib(GL11.GL_ENABLE_BIT | GL11.GL_LINE_BIT | GL11.GL_CURRENT_BIT);
		try {
			GL11.glDisable(GL11.GL_DEPTH_TEST);
			GL11.glDisable(GL11.GL_LIGHTING);
			GL11.glLineWidth(2F);
			drawGraphAxes(x, y, width, 38, z);
			drawGraphCurve(x, y, width, 38, z, 0xFF55FF55, fluxCurve(fuel));
			if(hasDepletionCurve(fuel)) drawGraphCurve(x, y, width, 38, z, 0xFF55AAFF, depletionCurve(fuel));
		} finally {
			GL11.glPopAttrib();
		}
	}

	public static ItemStack peek() {
		return stack;
	}

	public static void clear() {
		stack = null;
		tooltip = null;
	}

	public static void setPending(ItemStack item, List<String> pending) {
		stack = item;
		tooltip = pending;
	}

	public static void renderPending(FontRenderer font, int guiWidth, int guiHeight, int mouseX, int mouseY, double z, boolean nei) {

		if(stack == null || tooltip == null) return;
		List<String> lines = new ArrayList<String>(tooltip);

		int[] bounds = nei
				? neiBounds(lines, font, mouseX, mouseY, guiWidth, guiHeight)
				: hoveringBounds(lines, mouseX, mouseY, font, guiWidth, guiHeight);

		// :catsmile:
		GL11.glPushAttrib(GL11.GL_ENABLE_BIT | GL11.GL_LINE_BIT | GL11.GL_CURRENT_BIT);
		GL11.glDisable(GL11.GL_DEPTH_TEST);
		GL11.glDisable(GL11.GL_LIGHTING);
		GL11.glLineWidth(2F);

		ItemStack item = stack;
		if(hasGraphs(item)) {
			renderGraphs(item, bounds, lines.size(), z);
		}

		GL11.glPopAttrib();

		stack = null;
		tooltip = null;
	}

	// this is so that nei doesn't fuck up the orientation
	private static int[] neiBounds(List<String> lines, FontRenderer font, int mouseX, int mouseY, int guiWidth, int guiHeight) {

		int[] bounds = hoveringBounds(lines, mouseX, mouseY, font, guiWidth, guiHeight);
		int width = bounds[2];
		int height = bounds[3];

		int x = mouseX + 12;
		if(x < 8) x = 8;
		else if(x > guiWidth - width - 8) x -= 24 + width;
		if(x < 8) x = 8;

		int y = mouseY - 12;
		if(y < 8) y = 8;
		int max = guiHeight - 8 - height;
		if(y > max) y = max;

		bounds[0] = x;
		bounds[1] = y;
		return bounds;
	}

	// dark mango graphs
	private static void renderGraphs(ItemStack fuel, int[] bounds, int rowCount, double z) {

		int top = bounds[1] + 2 + 10 * (rowCount - 4);

		// both curves share one plot, the flux one in green and the depletion one in blue
		renderGraph(fuel, bounds[0], top, bounds[2], z);
	}

	public static boolean hasGraphs(ItemStack stack) {
		if(stack == null) return false;
		Item item = stack.getItem();
		return item instanceof ItemRBMKRod || item == ModItems.pwr_fuel || item == ModItems.watz_pellet;
	}

	private static boolean hasDepletionCurve(ItemStack fuel) {
		return fuel.getItem() instanceof ItemRBMKRod;
	}

	private static double[] fluxCurve(ItemStack fuel) {
		Item item = fuel.getItem();
		double[] values = new double[64];

		if(item instanceof ItemRBMKRod) {
			ItemRBMKRod rod = (ItemRBMKRod) item;
			double enrichment = ItemRBMKRod.getEnrichment(fuel);
			double max = fluxMax(rod.function);
			for(int i = 0; i < 64; i++) values[i] = rod.reactivityFunc(sample(i) * max, enrichment);
		} else {
			Function function = null;
			if(item == ModItems.pwr_fuel) {
				EnumPWRFuel pwr = EnumUtil.grabEnumSafely(EnumPWRFuel.class, fuel.getItemDamage());
				function = pwr.function;
			} else if(item == ModItems.watz_pellet) {
				EnumWatzType type = EnumUtil.grabEnumSafely(EnumWatzType.class, fuel.getItemDamage());
				function = type.burnFunc != null ? type.burnFunc : type.absorbFunc;
			}
			if(function != null) {
				for(int i = 0; i < 64; i++) values[i] = function.effonix(sample(i) * 2500D);
			}
		}

		return normalize(values);
	}

	private static double[] depletionCurve(ItemStack fuel) {
		ItemRBMKRod rod = (ItemRBMKRod) fuel.getItem();
		double[] values = new double[64];
		for(int i = 0; i < 64; i++) values[i] = rod.reactivityModByEnrichment(1D - sample(i)) / 2D;
		return values;
	}

	private static double sample(int index) {
		return index / 63D;
	}

	private static double fluxMax(ItemRBMKRod.EnumBurnFunc function) {

		switch(function) {
		case PLATEU: return 150D;
		case SLOW_LINEAR: return 320D;
		case ARCH: return 5000D;
		default: return 100D;
		}
	}

	private static double[] normalize(double[] values) {
		double max = 0D;
		for(double value : values) if(value > max) max = value;
		if(max <= 0D) return values;
		for(int i = 0; i < values.length; i++) values[i] /= max;
		return values;
	}

	// copies from the GUIElements calss, i really dont want to fiddle with it
	private static int[] hoveringBounds(List<?> lines, int x, int y, FontRenderer font, int guiWidth, int guiHeight) {
		int width = 0;
		for(Object line : lines) width = Math.max(width, font.getStringWidth((String) line));

		int boundX = x + 12;
		int boundY = y - 12;
		int height = 8;
		if(lines.size() > 1) height += 2 + (lines.size() - 1) * 10;

		if(boundX + width + 4 > guiWidth) boundX -= 28 + width;
		if(boundY + height + 6 > guiHeight) boundY = guiHeight - height - 6;
		if(boundX < 4) boundX = 4;
		if(boundY < 4) boundY = 4;

		return new int[] { boundX, boundY, width, height };
	}

	private static void drawGraphAxes(int x, int y, int w, int h, double z) {
		int ox = x + 3;
		int oy = y + h - 3;
		drawArrowVector(ox, oy, (float) z, new Vector2f(ox, y + 4));
		drawArrowVector(ox, oy, (float) z, new Vector2f(x + w - 4, oy));
	}

	private static void drawGraphCurve(int x, int y, int w, int h, double z, int color, double[] values) {

		GL11.glDisable(GL11.GL_TEXTURE_2D);
		Tessellator tess = Tessellator.instance;
		tess.startDrawing(GL11.GL_LINE_STRIP);
		tess.setColorOpaque_I(color);

		double ox = x + 3;
		double oy = y + h - 3;
		for(int i = 0; i < values.length; i++) {
			double v = MathHelper.clamp_double(values[i], 0, 1);
			tess.addVertex(ox + (double) i / (values.length - 1) * (w - 6), oy - v * (h - 7), z);
		}

		tess.draw();
		GL11.glColor4f(1F, 1F, 1F, 1F);
		GL11.glEnable(GL11.GL_TEXTURE_2D);
	}

	private static void drawArrowVector(int x, int y, float z, Vector2f vector) {
		GL11.glDisable(GL11.GL_TEXTURE_2D);
		Tessellator tess = Tessellator.instance;
		tess.startDrawing(GL11.GL_LINE_LOOP);
		tess.setColorOpaque_I(0x00A000);

		Vector2f delta = new Vector2f(vector.x - x, vector.y - y);
		float m = delta.length();
		float sx = 2F * delta.x / m;
		float sy = 2F * delta.y / m;

		tess.addVertex(x, y, z);
		tess.addVertex(x + delta.x - sx, y + delta.y - sy, z);
		tess.addVertex(x + delta.x - sx - sy, y + delta.y - sy + sx, z);
		tess.addVertex(x + delta.x, y + delta.y, z);
		tess.addVertex(x + delta.x - sx + sy, y + delta.y - sy - sx, z);
		tess.addVertex(x + delta.x - sx, y + delta.y - sy, z);

		tess.draw();
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		GL11.glEnable(GL11.GL_TEXTURE_2D);
	}
}
