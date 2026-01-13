package com.hbm.dim.dima;

import java.util.Random;

import org.lwjgl.opengl.GL11;

import com.hbm.dim.SkyProviderCelestial;
import com.hbm.lib.RefStrings;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;

public class SkyProviderDima extends SkyProviderCelestial {

	private static final ResourceLocation deadChannel = new ResourceLocation(RefStrings.MODID, "textures/misc/space/dead_channel.png");

	private static final boolean PSYCHO_MANTIS = false; // turning this on violates the geneva convention

	public SkyProviderDima() {
		super();
	}

	@Override
	protected ResourceLocation getNightTexture() {
		return nightTextureDemeter;
	}

	@Override
	public void render(float partialTicks, WorldClient world, Minecraft mc) {
		GL11.glMatrixMode(GL11.GL_TEXTURE);
		GL11.glLoadIdentity();
		GL11.glMatrixMode(GL11.GL_MODELVIEW);

		if(!EventHandlerDima.tunedToADeadChannel) {
			super.render(partialTicks, world, mc);
			return;
		}

		GL11.glDisable(GL11.GL_FOG);
		GL11.glDisable(GL11.GL_ALPHA_TEST);
		GL11.glEnable(GL11.GL_BLEND);
		OpenGlHelper.glBlendFunc(770, 771, 1, 0);
		RenderHelper.disableStandardItemLighting();
		GL11.glDepthMask(false);
		mc.renderEngine.bindTexture(deadChannel);
		Tessellator tessellator = Tessellator.instance;



		for(int i = 0; i < 6; ++i) {
			GL11.glPushMatrix();

			if(i == 1) GL11.glRotatef(90.0F, 1.0F, 0.0F, 0.0F);
			if(i == 2) GL11.glRotatef(-90.0F, 1.0F, 0.0F, 0.0F);
			if(i == 3) GL11.glRotatef(180.0F, 1.0F, 0.0F, 0.0F);
			if(i == 4) GL11.glRotatef(90.0F, 0.0F, 0.0F, 1.0F);
			if(i == 5) GL11.glRotatef(-90.0F, 0.0F, 0.0F, 1.0F);

			tessellator.startDrawingQuads();
			// tessellator.setColorOpaque_I(2631720);
			tessellator.addVertexWithUV(-100.0D, -100.0D, -100.0D, 0.0D, 0.0D);
			tessellator.addVertexWithUV(-100.0D, -100.0D, 100.0D, 0.0D, 16.0D);
			tessellator.addVertexWithUV(100.0D, -100.0D, 100.0D, 16.0D, 16.0D);
			tessellator.addVertexWithUV(100.0D, -100.0D, -100.0D, 16.0D, 0.0D);

			GL11.glMatrixMode(GL11.GL_TEXTURE);
			if(!PSYCHO_MANTIS) GL11.glPushMatrix();
			{

				GL11.glTranslated(world.rand.nextDouble(), world.rand.nextDouble(), 0);

				tessellator.draw();

			}
			if(!PSYCHO_MANTIS) GL11.glPopMatrix();
			GL11.glMatrixMode(GL11.GL_MODELVIEW);

			GL11.glPopMatrix();
		}

		GL11.glDepthMask(true);
		GL11.glEnable(GL11.GL_TEXTURE_2D);
		GL11.glEnable(GL11.GL_ALPHA_TEST);
	}

	@Override
	protected void renderDigamma(float partialTicks, WorldClient world, Minecraft mc, float solarAngle) {
		Tessellator tess = Tessellator.instance;

		float alpha = 1.0f - (WorldProviderDima.flash / 100.0f);
		int segments = 12;
		int strands = 10;
		double height = 252.0;
		double horizontalScale = 29.0;
		float thickness = 0.25f;
		int layers = 3;

		GL11.glPushMatrix();
		{

			GL11.glRotatef(-solarAngle * 360.0F, 1.0F, 0.0F, 0.0F);

			// ===================== FLARE =====================
			GL11.glPushMatrix();
			{

				GL11.glEnable(GL11.GL_TEXTURE_2D);

				OpenGlHelper.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ONE, GL11.GL_ZERO);

				float flareScale = 100f;
				GL11.glColor4f(1f, 0.2f, 0.2f, alpha * 0.3f);
				GL11.glRotatef(192, 1, 0, 0);
				GL11.glRotatef(62, 0, 0, 1);
				mc.renderEngine.bindTexture(shockFlareTexture);

				tess.startDrawingQuads();
				tess.addVertexWithUV(-flareScale, 100.0D, -flareScale, 0.0D, 0.0D);
				tess.addVertexWithUV(flareScale, 100.0D, -flareScale, 1.0D, 0.0D);
				tess.addVertexWithUV(flareScale, 100.0D, flareScale, 1.0D, 1.0D);
				tess.addVertexWithUV(-flareScale, 100.0D, flareScale, 0.0D, 1.0D);
				tess.draw();

				GL11.glDisable(GL11.GL_TEXTURE_2D);

			}
			GL11.glPopMatrix();
			// ===================== END FLARE =====================

			// ===================== BOLTS =====================
			GL11.glPushMatrix();
			{

				double clusterX = -160;
				double clusterY = -194;
				double clusterZ = -32;
				GL11.glTranslated(clusterX, clusterY, clusterZ + 12);

				int innerColor = 0xFF0000;
				int outerColor = 0x880000;

				for(int strand = 0; strand < strands; strand++) {
					Random rand = new Random(800 + strand * 1000L);
					double prevX = 0, prevZ = 0;
					int strandSegments = 4 + rand.nextInt(segments - 3);

					for(int i = 0; i < strandSegments; i++) {
						double nextX = prevX + (rand.nextDouble() - 0.5) * horizontalScale;
						double nextZ = prevZ + (rand.nextDouble() - 0.5) * horizontalScale;

						double spread = 0.2 * (i / (double)segments) * (strand - 1.5);
						nextX += spread;
						nextZ += spread;

						double y0 = (i / (double)segments) * height;
						double y1 = ((i + 1) / (double)segments) * height;

						float radius = thickness / layers;

						for(int j = 1; j <= layers; j++) {
							float inter = (float)(j - 1) / (float)(layers - 1);

							int r1 = ((outerColor & 0xFF0000) >> 16);
							int g1 = ((outerColor & 0x00FF00) >> 8);
							int b1 = (outerColor & 0x0000FF);

							int r2 = ((innerColor & 0xFF0000) >> 16);
							int g2 = ((innerColor & 0x00FF00) >> 8);
							int b2 = (innerColor & 0x0000FF);

							int r = ((int)(r1 + (r2 - r1) * inter)) << 16;
							int g = ((int)(g1 + (g2 - g1) * inter)) << 8;
							int b = ((int)(b1 + (b2 - b1) * inter));
							int color = r | g | b;

							for(int face = 0; face < 4; face++) {
								tess.startDrawingQuads();
								setColorWithAlpha(tess, color, alpha * 255);

								switch (face) {
								case 0:
									tess.addVertex(prevX + radius * j, y0, prevZ + radius * j);
									tess.addVertex(prevX + radius * j, y0, prevZ - radius * j);
									tess.addVertex(nextX + radius * j, y1, nextZ - radius * j);
									tess.addVertex(nextX + radius * j, y1, nextZ + radius * j);
									break;
								case 1:
									tess.addVertex(prevX - radius * j, y0, prevZ + radius * j);
									tess.addVertex(prevX - radius * j, y0, prevZ - radius * j);
									tess.addVertex(nextX - radius * j, y1, nextZ - radius * j);
									tess.addVertex(nextX - radius * j, y1, nextZ + radius * j);
									break;
								case 2:
									tess.addVertex(prevX + radius * j, y0, prevZ + radius * j);
									tess.addVertex(prevX - radius * j, y0, prevZ + radius * j);
									tess.addVertex(nextX - radius * j, y1, nextZ + radius * j);
									tess.addVertex(nextX + radius * j, y1, nextZ + radius * j);
									break;
								case 3:
									tess.addVertex(prevX + radius * j, y0, prevZ - radius * j);
									tess.addVertex(prevX - radius * j, y0, prevZ - radius * j);
									tess.addVertex(nextX - radius * j, y1, nextZ - radius * j);
									tess.addVertex(nextX + radius * j, y1, nextZ - radius * j);
									break;
								}

								tess.draw();
							}
						}

						prevX = nextX;
						prevZ = nextZ;
					}
				}

			}
			GL11.glPopMatrix();
			// ===================== END BOLTS =====================

		}
		GL11.glPopMatrix(); // pop global
	}

	private static void setColorWithAlpha(Tessellator tessellator, int color, float alpha) {
		float red = ((color >> 16) & 0xFF) / 255.0f;
		float green = ((color >> 8) & 0xFF) / 255.0f;
		float blue = (color & 0xFF) / 255.0f;
		float a = MathHelper.clamp_float(alpha / 255.0f, 0.0f, 1.0f); // convert to 0-1
		GL11.glColor4f(red, green, blue, a);
	}

	@Override
	protected float[] calcSunriseSunsetColors(float partialTicks, WorldClient world, Minecraft mc, float solarAngle, float pressure) {
		return world.provider.calcSunriseSunsetColors(world.getCelestialAngle(partialTicks), partialTicks);
	}

}
