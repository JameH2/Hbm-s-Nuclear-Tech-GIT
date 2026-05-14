package com.hbm.dim;

import java.util.Random;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
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

@SideOnly(Side.CLIENT)
public class WeatherProviderCelestial extends IRenderHandler {

	private static final ResourceLocation RAIN_TEXTURE = new ResourceLocation("textures/environment/rain.png");
	private static final ResourceLocation SNOW_TEXTURE = new ResourceLocation("textures/environment/snow.png");

	private final Random random = new Random();
	private float[] rainXCoords;
	private float[] rainYCoords;

	@Override
	public void render(float partialTicks, WorldClient world, Minecraft mc) {
		float intensity = world.getRainStrength(partialTicks);

		if(intensity <= 0.0F) {
			return;
		}

		EntityLivingBase camera = mc.renderViewEntity;
		if(camera == null) {
			return;
		}

		if(world.provider instanceof WorldProviderCelestial && !((WorldProviderCelestial)world.provider).hasWeatherCycle()) {
			return;
		}

		mc.entityRenderer.enableLightmap(partialTicks);
		initRainCoords();

		int timer = mc.thePlayer != null ? mc.thePlayer.ticksExisted : (int)(world.getTotalWorldTime() & Integer.MAX_VALUE);
		int playerX = MathHelper.floor_double(camera.posX);
		int playerY = MathHelper.floor_double(camera.posY);
		int playerZ = MathHelper.floor_double(camera.posZ);
		double interpX = camera.lastTickPosX + (camera.posX - camera.lastTickPosX) * partialTicks;
		double interpY = camera.lastTickPosY + (camera.posY - camera.lastTickPosY) * partialTicks;
		double interpZ = camera.lastTickPosZ + (camera.posZ - camera.lastTickPosZ) * partialTicks;
		int playerHeight = MathHelper.floor_double(interpY);
		int renderLayerCount = mc.gameSettings.fancyGraphics ? 10 : 5;
		Vec3 rainColor = getRainColor(world);
		Vec3 snowColor = getSnowColor(world);

		Tessellator tessellator = Tessellator.instance;
		GL11.glDisable(GL11.GL_CULL_FACE);
		GL11.glNormal3f(0.0F, 1.0F, 0.0F);
		GL11.glEnable(GL11.GL_BLEND);
		OpenGlHelper.glBlendFunc(770, 771, 1, 0);
		GL11.glAlphaFunc(GL11.GL_GREATER, 0.1F);
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		enableTextureAlphaTint();

		int layer = -1;

		for(int layerZ = playerZ - renderLayerCount; layerZ <= playerZ + renderLayerCount; ++layerZ) {
			for(int layerX = playerX - renderLayerCount; layerX <= playerX + renderLayerCount; ++layerX) {
				int rainCoord = (layerZ - playerZ + 16) * 32 + layerX - playerX + 16;
				float rainCoordX = this.rainXCoords[rainCoord] * 0.5F;
				float rainCoordY = this.rainYCoords[rainCoord] * 0.5F;
				BiomeGenBase biome = world.getBiomeGenForCoords(layerX, layerZ);

				if(!biome.canSpawnLightningBolt() && !biome.getEnableSnow()) {
					continue;
				}

				int precipitationHeight = world.getPrecipitationHeight(layerX, layerZ);
				int minHeight = playerY - renderLayerCount;
				int maxHeight = playerY + renderLayerCount;

				if(minHeight < precipitationHeight) minHeight = precipitationHeight;
				if(maxHeight < precipitationHeight) maxHeight = precipitationHeight;

				int layerY = precipitationHeight;
				if(precipitationHeight < playerHeight) layerY = playerHeight;

				if(minHeight == maxHeight) {
					continue;
				}

				this.random.setSeed(layerX * layerX * 3121 + layerX * 45238971 ^ layerZ * layerZ * 418711 + layerZ * 13761);
				float temperature = biome.getFloatTemperature(layerX, minHeight, layerZ);
				boolean renderRain = world.getWorldChunkManager().getTemperatureAtHeight(temperature, precipitationHeight) >= 0.15F;

				if(renderRain) {
					if(layer != 0) {
						if(layer >= 0) {
							tessellator.draw();
						}

						layer = 0;
						mc.getTextureManager().bindTexture(RAIN_TEXTURE);
						tessellator.startDrawingQuads();
					}

					int rainSeed = layerX * layerX * 3121 + layerX * 45238971 + layerZ * layerZ * 418711 + layerZ * 13761;
					float rainOffset = ((timer + (rainSeed & 31)) + partialTicks) / 32.0F * (3.0F + this.random.nextFloat());
					double distX = layerX + 0.5D - camera.posX;
					double distZ = layerZ + 0.5D - camera.posZ;
					float intensityMod = MathHelper.sqrt_double(distX * distX + distZ * distZ) / renderLayerCount;

					tessellator.setBrightness(world.getLightBrightnessForSkyBlocks(layerX, layerY, layerZ, 0));
					tessellator.setColorRGBA_F(
						(float)rainColor.xCoord,
						(float)rainColor.yCoord,
						(float)rainColor.zCoord,
						((1.0F - intensityMod * intensityMod) * 0.5F + 0.5F) * intensity
					);
					tessellator.setTranslation(-interpX, -interpY, -interpZ);
					tessellator.addVertexWithUV(layerX - rainCoordX + 0.5D, minHeight, layerZ - rainCoordY + 0.5D, 0.0F, minHeight / 4.0F + rainOffset);
					tessellator.addVertexWithUV(layerX + rainCoordX + 0.5D, minHeight, layerZ + rainCoordY + 0.5D, 1.0F, minHeight / 4.0F + rainOffset);
					tessellator.addVertexWithUV(layerX + rainCoordX + 0.5D, maxHeight, layerZ + rainCoordY + 0.5D, 1.0F, maxHeight / 4.0F + rainOffset);
					tessellator.addVertexWithUV(layerX - rainCoordX + 0.5D, maxHeight, layerZ - rainCoordY + 0.5D, 0.0F, maxHeight / 4.0F + rainOffset);
					tessellator.setTranslation(0.0D, 0.0D, 0.0D);
				} else {
					if(layer != 1) {
						if(layer >= 0) {
							tessellator.draw();
						}

						layer = 1;
						mc.getTextureManager().bindTexture(SNOW_TEXTURE);
						tessellator.startDrawingQuads();
					}

					float swayLoop = ((timer & 511) + partialTicks) / 512.0F;
					float fallVariation = this.random.nextFloat() + timer * 0.01F * (float)this.random.nextGaussian();
					float swayVariation = this.random.nextFloat() + timer * (float)this.random.nextGaussian() * 0.001F;
					double distX = layerX + 0.5D - camera.posX;
					double distZ = layerZ + 0.5D - camera.posZ;
					float intensityMod = MathHelper.sqrt_double(distX * distX + distZ * distZ) / renderLayerCount;

					tessellator.setBrightness((world.getLightBrightnessForSkyBlocks(layerX, layerY, layerZ, 0) * 3 + 15728880) / 4);
					tessellator.setColorRGBA_F(
						(float)snowColor.xCoord,
						(float)snowColor.yCoord,
						(float)snowColor.zCoord,
						((1.0F - intensityMod * intensityMod) * 0.3F + 0.5F) * intensity
					);
					tessellator.setTranslation(-interpX, -interpY, -interpZ);
					tessellator.addVertexWithUV(layerX - rainCoordX + 0.5D, minHeight, layerZ - rainCoordY + 0.5D, 0.0F + fallVariation, minHeight / 4.0F + swayLoop + swayVariation);
					tessellator.addVertexWithUV(layerX + rainCoordX + 0.5D, minHeight, layerZ + rainCoordY + 0.5D, 1.0F + fallVariation, minHeight / 4.0F + swayLoop + swayVariation);
					tessellator.addVertexWithUV(layerX + rainCoordX + 0.5D, maxHeight, layerZ + rainCoordY + 0.5D, 1.0F + fallVariation, maxHeight / 4.0F + swayLoop + swayVariation);
					tessellator.addVertexWithUV(layerX - rainCoordX + 0.5D, maxHeight, layerZ - rainCoordY + 0.5D, 0.0F + fallVariation, maxHeight / 4.0F + swayLoop + swayVariation);
					tessellator.setTranslation(0.0D, 0.0D, 0.0D);
				}
			}
		}

		if(layer >= 0) {
			tessellator.draw();
		}

		GL11.glEnable(GL11.GL_CULL_FACE);
		GL11.glDisable(GL11.GL_BLEND);
		GL11.glAlphaFunc(GL11.GL_GREATER, 0.1F);
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		disableTextureAlphaTint();
		mc.entityRenderer.disableLightmap(partialTicks);
	}

	private void enableTextureAlphaTint() {
		GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE, GL13.GL_COMBINE);
		GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, GL13.GL_COMBINE_RGB, GL11.GL_REPLACE);
		GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, GL13.GL_SOURCE0_RGB, GL13.GL_PRIMARY_COLOR);
		GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, GL13.GL_OPERAND0_RGB, GL11.GL_SRC_COLOR);
		GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, GL13.GL_COMBINE_ALPHA, GL11.GL_MODULATE);
		GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, GL13.GL_SOURCE0_ALPHA, GL11.GL_TEXTURE);
		GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, GL13.GL_OPERAND0_ALPHA, GL11.GL_SRC_ALPHA);
		GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, GL13.GL_SOURCE1_ALPHA, GL13.GL_PRIMARY_COLOR);
		GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, GL13.GL_OPERAND1_ALPHA, GL11.GL_SRC_ALPHA);
	}

	private void disableTextureAlphaTint() {
		GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE, GL11.GL_MODULATE);
	}

	private void initRainCoords() {
		if(this.rainXCoords != null) {
			return;
		}

		this.rainXCoords = new float[1024];
		this.rainYCoords = new float[1024];

		for(int i = 0; i < 32; ++i) {
			for(int j = 0; j < 32; ++j) {
				float coordX = j - 16;
				float coordY = i - 16;
				float coordLength = MathHelper.sqrt_float(coordX * coordX + coordY * coordY);
				this.rainXCoords[i << 5 | j] = -coordY / coordLength;
				this.rainYCoords[i << 5 | j] = coordX / coordLength;
			}
		}
	}

	private Vec3 getRainColor(WorldClient world) {
		if(world.provider instanceof WorldProviderCelestial) {
			return ((WorldProviderCelestial)world.provider).getWeatherColor();
		}

		return Vec3.createVectorHelper(1.0D, 1.0D, 1.0D);
	}

	private Vec3 getSnowColor(WorldClient world) {
		if(world.provider instanceof WorldProviderCelestial) {
			return ((WorldProviderCelestial)world.provider).getSnowColor();
		}

		return Vec3.createVectorHelper(1.0D, 1.0D, 1.0D);
	}
}
