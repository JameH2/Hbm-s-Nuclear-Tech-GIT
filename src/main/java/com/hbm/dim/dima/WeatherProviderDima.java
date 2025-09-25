package com.hbm.dim.dima;

import java.util.Random;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Vec3;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraftforge.client.IRenderHandler;

public class WeatherProviderDima extends IRenderHandler {

    private static final ResourceLocation locationRainPng = new ResourceLocation("textures/environment/rain.png");

    private Random random = new Random();

	/** Rain X coords */
	float[] rainXCoords;
	/** Rain Y coords */
	float[] rainYCoords;

	protected Vec3 getRainColor() {
		return Vec3.createVectorHelper(1, 0, 0);
	}

	@Override
	public void render(float partialTicks, WorldClient world, Minecraft mc) {
		int rendererUpdateCount = (int)world.getTotalWorldTime();

		float f1 = world.getRainStrength(partialTicks);

		if(f1 > 0.0F) {
			mc.entityRenderer.enableLightmap((double) partialTicks);

			if(this.rainXCoords == null) {
				this.rainXCoords = new float[1024];
				this.rainYCoords = new float[1024];

				for(int i = 0; i < 32; ++i) {
					for(int j = 0; j < 32; ++j) {
						float f2 = (float) (j - 16);
						float f3 = (float) (i - 16);
						float f4 = MathHelper.sqrt_float(f2 * f2 + f3 * f3);
						this.rainXCoords[i << 5 | j] = -f3 / f4;
						this.rainYCoords[i << 5 | j] = f2 / f4;
					}
				}
			}

			EntityLivingBase entitylivingbase = mc.renderViewEntity;
			WorldClient worldclient = world;
			int ix = MathHelper.floor_double(entitylivingbase.posX);
			int iy = MathHelper.floor_double(entitylivingbase.posY);
			int iz = MathHelper.floor_double(entitylivingbase.posZ);
			Tessellator tessellator = Tessellator.instance;
			GL11.glDisable(GL11.GL_CULL_FACE);
			GL11.glNormal3f(0.0F, 1.0F, 0.0F);
			GL11.glEnable(GL11.GL_BLEND);
			OpenGlHelper.glBlendFunc(770, 771, 1, 0);
			GL11.glAlphaFunc(GL11.GL_GREATER, 0.1F);
			double posX = entitylivingbase.lastTickPosX + (entitylivingbase.posX - entitylivingbase.lastTickPosX) * (double) partialTicks;
			double posY = entitylivingbase.lastTickPosY + (entitylivingbase.posY - entitylivingbase.lastTickPosY) * (double) partialTicks;
			double posZ = entitylivingbase.lastTickPosZ + (entitylivingbase.posZ - entitylivingbase.lastTickPosZ) * (double) partialTicks;
			int level = MathHelper.floor_double(posY);
			byte dist = 5;

			if(mc.gameSettings.fancyGraphics) {
				dist = 10;
			}

			byte drawFlag = -1;

			GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);

			for(int z = iz - dist; z <= iz + dist; ++z) {
				for(int x = ix - dist; x <= ix + dist; ++x) {
					int index = (z - iz + 16) * 32 + x - ix + 16;
					float f6 = this.rainXCoords[index] * 0.5F;
					float f7 = this.rainYCoords[index] * 0.5F;
					BiomeGenBase biomegenbase = worldclient.getBiomeGenForCoords(x, z);

					if(biomegenbase.canSpawnLightningBolt() || biomegenbase.getEnableSnow()) {
						int height = worldclient.getPrecipitationHeight(x, z);
						int min = iy - dist;
						int max = iy + dist;

						if(min < height) {
							min = height;
						}

						if(max < height) {
							max = height;
						}

						float f8 = 1.0F;
						int j2 = height;

						if(height < level) {
							j2 = level;
						}

						if(min != max) {
							this.random.setSeed((long) (x * x * 3121 + x * 45238971 ^ z * z * 418711 + z * 13761));
							float salt;

							if(drawFlag != 0) {
								if(drawFlag >= 0) {
									tessellator.draw();
								}

								drawFlag = 0;
								mc.getTextureManager().bindTexture(locationRainPng);
								tessellator.startDrawingQuads();
							}

							salt = ((float) (rendererUpdateCount + x * x * 3121 + x * 45238971
									+ z * z * 418711 + z * 13761 & 31) + partialTicks) / 32.0F
									* (3.0F + this.random.nextFloat());
							double rainX = (double) ((float) x + 0.5F) - entitylivingbase.posX;
							double rainZ = (double) ((float) z + 0.5F) - entitylivingbase.posZ;
							float unitDistance = MathHelper.sqrt_double(rainX * rainX + rainZ * rainZ) / (float) dist;

							Vec3 rainColor = getRainColor();

							tessellator.setBrightness(worldclient.getLightBrightnessForSkyBlocks(x, j2, z, 0));
							tessellator.setColorRGBA_F((float)rainColor.xCoord, (float)rainColor.yCoord, (float)rainColor.zCoord, ((1.0F - unitDistance * unitDistance) * 0.5F + 0.5F) * f1);
							tessellator.setTranslation(-posX * 1.0D, -posY * 1.0D, -posZ * 1.0D);
							tessellator.addVertexWithUV((double) ((float) x - f6) + 0.5D, (double) min,
									(double) ((float) z - f7) + 0.5D, (double) (0.0F * f8),
									(double) ((float) min * f8 / 4.0F + salt * f8));
							tessellator.addVertexWithUV((double) ((float) x + f6) + 0.5D, (double) min,
									(double) ((float) z + f7) + 0.5D, (double) (1.0F * f8),
									(double) ((float) min * f8 / 4.0F + salt * f8));
							tessellator.addVertexWithUV((double) ((float) x + f6) + 0.5D, (double) max,
									(double) ((float) z + f7) + 0.5D, (double) (1.0F * f8),
									(double) ((float) max * f8 / 4.0F + salt * f8));
							tessellator.addVertexWithUV((double) ((float) x - f6) + 0.5D, (double) max,
									(double) ((float) z - f7) + 0.5D, (double) (0.0F * f8),
									(double) ((float) max * f8 / 4.0F + salt * f8));
							tessellator.setTranslation(0.0D, 0.0D, 0.0D);
						}
					}
				}
			}

			if(drawFlag >= 0) {
				tessellator.draw();
			}

			GL11.glEnable(GL11.GL_CULL_FACE);
			GL11.glDisable(GL11.GL_BLEND);
			GL11.glAlphaFunc(GL11.GL_GREATER, 0.1F);
			mc.entityRenderer.disableLightmap((double) partialTicks);
		}
	}

}
