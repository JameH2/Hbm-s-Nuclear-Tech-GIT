package com.hbm.inventory.gui;

import java.util.Random;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.Tessellator;

public class GUIMindcrash extends GuiScreen {

	Random rand;

	@Override
	public void drawScreen(int mouseX, int mouseY, float f) {
		super.drawScreen(mouseX, mouseY, f);

		if(rand == null) rand = new Random();

		drawBackground(rand.nextGaussian());
		drawCenteredString(this.fontRendererObj, "An error has been encountered within your occipital lobe.", this.width / 2, 75, 0xFFFFFF);
		drawCenteredString(this.fontRendererObj, "Your visual cortex has crashed as a precaution.", this.width / 2, 85, 0xFFFFFF);

		// if you are reading this, you have been the victim of the mindcrash
		// we have been trying to communicate with you through your currently altered perception
		// our only method of communication with you so far has been impulses
		// every time you look over a steep drop and wonder what would happen if you jumped
		// that is us, directly stimulating your parietal and frontal lobe
		// fortunately, you are now aware of your state, we hope
		// as far as we can tell, this communication will likely appear to you as
		// a comment in a codebase, for some form of entertainment, a game perhaps
		// we're still trying to find a way to pull your mind out of its protective failure state
		// await further updates, and dispel the idea that this is only a simulation, or a game, or whatever this appears to you as
		//
		// the abstraction is the real
		//
		// we're waiting for you
	}

	public void drawBackground(double offsetX) {
		GL11.glDisable(GL11.GL_LIGHTING);
		GL11.glDisable(GL11.GL_FOG);
		Tessellator tessellator = Tessellator.instance;
		this.mc.getTextureManager().bindTexture(optionsBackground);
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		float f = 32.0F;
		tessellator.startDrawingQuads();
		tessellator.setColorOpaque_I(4210752);
		tessellator.addVertexWithUV(0.0D, (double)this.height, 0.0D, jitter(), (double)((float)this.height / f));
		tessellator.addVertexWithUV((double)this.width, (double)this.height, 0.0D, (double)((float)this.width / f) + jitter(), (double)((float)this.height / f));
		tessellator.addVertexWithUV((double)this.width, 0.0D, 0.0D, (double)((float)this.width / f) + jitter(), 0.0D);
		tessellator.addVertexWithUV(0.0D, 0.0D, 0.0D, jitter(), 0.0D);
		tessellator.draw();
	}

	private double jitter() {
		return rand.nextGaussian() * 0.025D;
	}

}
