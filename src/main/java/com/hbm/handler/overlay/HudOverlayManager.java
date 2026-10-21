package com.hbm.handler.overlay;

import java.util.ArrayList;
import java.util.List;

import org.lwjgl.opengl.GL11;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType;

/** Read-only overlays anchored to the player's crosshair. */
@SideOnly(Side.CLIENT)
public class HudOverlayManager {

	private final List<HudOverlay> overlays = new ArrayList<>();

	public HudOverlayManager register(HudOverlay overlay) {
		this.overlays.add(overlay);
		return this;
	}

	@SubscribeEvent
	public void onRenderHUD(RenderGameOverlayEvent.Post event) {
		if(event.type != ElementType.HOTBAR || !HudOverlay.interactionsEnabled()) return;
		Minecraft mc = Minecraft.getMinecraft();
		int width = event.resolution.getScaledWidth();
		int height = event.resolution.getScaledHeight();
		GL11.glPushAttrib(GL11.GL_ENABLE_BIT | GL11.GL_COLOR_BUFFER_BIT | GL11.GL_CURRENT_BIT);
		try {
			GL11.glEnable(GL11.GL_BLEND);
			GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
			GL11.glColor4f(1F, 1F, 1F, 1F);
			for(HudOverlay overlay : overlays) {
				if(!overlay.isVisible(mc, mc.thePlayer)) continue;
				overlay.screenWidth = width;
				overlay.screenHeight = height;
				overlay.drawScreen(width / 2, height / 2, event.partialTicks);
			}
		} finally {
			GL11.glPopAttrib();
		}
	}
}
