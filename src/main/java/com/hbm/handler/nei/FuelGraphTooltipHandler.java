package com.hbm.handler.nei;

import java.awt.Dimension;
import java.util.List;

import codechicken.lib.gui.GuiDraw;
import codechicken.nei.guihook.IContainerTooltipHandler;
import com.hbm.render.util.TooltipRenderer;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.item.ItemStack;

/** Let NEI position the graph with the actual, fully assembled tooltip. */
public class FuelGraphTooltipHandler implements IContainerTooltipHandler {

	@Override
	public List<String> handleItemTooltip(GuiContainer gui, ItemStack stack, int mouseX, int mouseY, List<String> tooltip) {
		if(!TooltipRenderer.hasGraphs(stack) || tooltip.isEmpty()) return tooltip;
		final ItemStack fuel = stack.copy();
		tooltip.add(GuiDraw.TOOLTIP_HANDLER + GuiDraw.getTipLineId(new GuiDraw.ITooltipLineHandler() {
			@Override
			public Dimension getSize() {
				return new Dimension(160, 40);
			}

			@Override
			public void draw(int x, int y) {
				TooltipRenderer.renderGraph(fuel, x, y, getSize().width, 300D);
			}
		}));
		return tooltip;
	}
}
