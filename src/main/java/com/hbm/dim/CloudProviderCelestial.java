package com.hbm.dim;

import com.hbm.dim.trait.CBT_Atmosphere;
import com.hbm.dim.trait.CBT_Atmosphere.FluidEntry;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.fluid.Fluids;

import org.lwjgl.opengl.GL11;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.entity.Entity;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Vec3;
import net.minecraftforge.client.IRenderHandler;

@SideOnly(Side.CLIENT)
public class CloudProviderCelestial extends IRenderHandler {

	private static final ResourceLocation CLOUDS_TEXTURE = new ResourceLocation("textures/environment/clouds.png");

	private static final float CLOUD_BASE_ALPHA = 0.8F;
	private static final float CLOUD_HEIGHT_SHIFT = 0.33F;
	private static final float CLOUD_SCROLL_SPEED = 0.03F;

	private static final float FAST_CLOUD_TILE_SIZE = 32.0F;
	private static final int FAST_CLOUD_RADIUS = 256 / (int) FAST_CLOUD_TILE_SIZE;

	private static final float FANCY_CLOUD_SCALE = 12.0F;
	private static final float FANCY_CLOUD_DEPTH = 4.0F;
	private static final int FANCY_CLOUD_TILE_SIZE = 8;
	private static final int FANCY_CLOUD_RADIUS = 4;
	private static final double LAYER_POSITION_RANGE = 16384.0D;

	private static final float[] LAYER_HEIGHT_OFFSETS = {0.0F, 12.0F, 24.0F};
	private static final float[] LAYER_ALPHA_MULTIPLIERS = {1.0F, 0.84F, 0.76F};
	private static final double[] LAYER_SCROLL_MULTIPLIERS = {1.0D, 0.84D, 0.7D};

	@Override
	public void render(float partialTicks, WorldClient world, Minecraft mc) {
		if(!(world.provider instanceof WorldProviderCelestial) || mc.renderViewEntity == null) {
			return;
		}

		WorldProviderCelestial provider = (WorldProviderCelestial) world.provider;
		int layerCount = provider.getCloudLayerCount();
		if(layerCount <= 0) {
			return;
		}

		float baseCloudHeight = provider.getCloudHeight();
		if(baseCloudHeight < -9000.0F) {
			return;
		}

		CBT_Atmosphere atmosphere = CelestialBody.getTrait(world, CBT_Atmosphere.class);
		Vec3 cloudColor = getTintedCloudColor(atmosphere, provider.drawClouds(partialTicks));
		if(cloudColor == null) {
			cloudColor = Vec3.createVectorHelper(1.0D, 1.0D, 1.0D);
		}

		for(int layerIndex = 0; layerIndex < layerCount && layerIndex < LAYER_HEIGHT_OFFSETS.length; layerIndex++) {
			float layerHeight = baseCloudHeight + LAYER_HEIGHT_OFFSETS[layerIndex];
			float layerAlpha = CLOUD_BASE_ALPHA * LAYER_ALPHA_MULTIPLIERS[layerIndex];

			if(mc.gameSettings.fancyGraphics) {
				renderFancyLayer(partialTicks, world, mc, cloudColor, layerHeight, layerIndex, layerAlpha);
			} else {
				renderFlatLayer(partialTicks, world, mc, cloudColor, layerHeight, layerIndex, layerAlpha);
			}
		}
	}

	private void renderFlatLayer(float partialTicks, WorldClient world, Minecraft mc, Vec3 cloudColor, float cloudHeight, int layerIndex, float alpha) {
		Entity camera = mc.renderViewEntity;
		if(camera == null) {
			return;
		}

		GL11.glDisable(GL11.GL_CULL_FACE);
		mc.getTextureManager().bindTexture(CLOUDS_TEXTURE);
		GL11.glEnable(GL11.GL_BLEND);
		OpenGlHelper.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, 1, 0);

		float cameraY = (float) (camera.lastTickPosY + (camera.posY - camera.lastTickPosY) * partialTicks);
		float layerY = cloudHeight - cameraY + CLOUD_HEIGHT_SHIFT;

		float cloudRed = (float) cloudColor.xCoord;
		float cloudGreen = (float) cloudColor.yCoord;
		float cloudBlue = (float) cloudColor.zCoord;
		if(mc.gameSettings.anaglyph) {
			float[] anaglyph = applyAnaglyph(cloudRed, cloudGreen, cloudBlue);
			cloudRed = anaglyph[0];
			cloudGreen = anaglyph[1];
			cloudBlue = anaglyph[2];
		}

		double cloudTime = getCloudTime(world, mc, partialTicks);
		double layerOffsetX = getLayerPositionOffset(world, layerIndex, 0x4F1BBCDCBFA54001L);
		double layerOffsetZ = getLayerPositionOffset(world, layerIndex, 0x2C1B3C6D5E7F8103L);
		double motionX = camera.lastTickPosX + (camera.posX - camera.lastTickPosX) * partialTicks
			+ cloudTime * CLOUD_SCROLL_SPEED * LAYER_SCROLL_MULTIPLIERS[layerIndex]
			+ layerOffsetX;
		double motionZ = camera.lastTickPosZ + (camera.posZ - camera.lastTickPosZ) * partialTicks
			+ layerOffsetZ;

		int wrapX = MathHelper.floor_double(motionX / 2048.0D);
		int wrapZ = MathHelper.floor_double(motionZ / 2048.0D);
		motionX -= wrapX * 2048.0D;
		motionZ -= wrapZ * 2048.0D;

		float uvOffsetX = (float) (motionX * 4.8828125E-4F);
		float uvOffsetZ = (float) (motionZ * 4.8828125E-4F);

		Tessellator tessellator = Tessellator.instance;
		tessellator.startDrawingQuads();
		tessellator.setColorRGBA_F(cloudRed, cloudGreen, cloudBlue, alpha);

		for(int tileX = -FAST_CLOUD_RADIUS; tileX < FAST_CLOUD_RADIUS; tileX++) {
			for(int tileZ = -FAST_CLOUD_RADIUS; tileZ < FAST_CLOUD_RADIUS; tileZ++) {
				float minX = tileX * FAST_CLOUD_TILE_SIZE;
				float minZ = tileZ * FAST_CLOUD_TILE_SIZE;
				float maxX = minX + FAST_CLOUD_TILE_SIZE;
				float maxZ = minZ + FAST_CLOUD_TILE_SIZE;

				tessellator.addVertexWithUV(minX, layerY, maxZ, minX * 4.8828125E-4F + uvOffsetX, maxZ * 4.8828125E-4F + uvOffsetZ);
				tessellator.addVertexWithUV(maxX, layerY, maxZ, maxX * 4.8828125E-4F + uvOffsetX, maxZ * 4.8828125E-4F + uvOffsetZ);
				tessellator.addVertexWithUV(maxX, layerY, minZ, maxX * 4.8828125E-4F + uvOffsetX, minZ * 4.8828125E-4F + uvOffsetZ);
				tessellator.addVertexWithUV(minX, layerY, minZ, minX * 4.8828125E-4F + uvOffsetX, minZ * 4.8828125E-4F + uvOffsetZ);
			}
		}

		tessellator.draw();
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		GL11.glDisable(GL11.GL_BLEND);
		GL11.glEnable(GL11.GL_CULL_FACE);
	}

	private void renderFancyLayer(float partialTicks, WorldClient world, Minecraft mc, Vec3 cloudColor, float cloudHeight, int layerIndex, float alpha) {
		Entity camera = mc.renderViewEntity;
		if(camera == null) {
			return;
		}

		GL11.glPushMatrix();
		GL11.glDisable(GL11.GL_CULL_FACE);
		mc.getTextureManager().bindTexture(CLOUDS_TEXTURE);
		GL11.glEnable(GL11.GL_BLEND);
		OpenGlHelper.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, 1, 0);

		double cloudTime = getCloudTime(world, mc, partialTicks);
		double layerOffsetX = getLayerPositionOffset(world, layerIndex, 0x4F1BBCDCBFA54001L);
		double layerOffsetZ = getLayerPositionOffset(world, layerIndex, 0x2C1B3C6D5E7F8103L);
		double motionX = (camera.lastTickPosX + (camera.posX - camera.lastTickPosX) * partialTicks
			+ cloudTime * CLOUD_SCROLL_SPEED * LAYER_SCROLL_MULTIPLIERS[layerIndex]
			+ layerOffsetX) / FANCY_CLOUD_SCALE;
		double motionZ = (camera.lastTickPosZ + (camera.posZ - camera.lastTickPosZ) * partialTicks
			+ layerOffsetZ) / FANCY_CLOUD_SCALE + 0.33000001311302185D;
		float cameraY = (float) (camera.lastTickPosY + (camera.posY - camera.lastTickPosY) * partialTicks);
		float relativeCloudY = cloudHeight - cameraY + CLOUD_HEIGHT_SHIFT;

		int wrapX = MathHelper.floor_double(motionX / 2048.0D);
		int wrapZ = MathHelper.floor_double(motionZ / 2048.0D);
		motionX -= wrapX * 2048.0D;
		motionZ -= wrapZ * 2048.0D;

		float cloudRed = (float) cloudColor.xCoord;
		float cloudGreen = (float) cloudColor.yCoord;
		float cloudBlue = (float) cloudColor.zCoord;
		if(mc.gameSettings.anaglyph) {
			float[] anaglyph = applyAnaglyph(cloudRed, cloudGreen, cloudBlue);
			cloudRed = anaglyph[0];
			cloudGreen = anaglyph[1];
			cloudBlue = anaglyph[2];
		}

		float cloudUBase = MathHelper.floor_double(motionX) * 0.00390625F;
		float cloudVBase = MathHelper.floor_double(motionZ) * 0.00390625F;
		float cloudXOffset = (float) (motionX - MathHelper.floor_double(motionX));
		float cloudZOffset = (float) (motionZ - MathHelper.floor_double(motionZ));

		GL11.glScalef(FANCY_CLOUD_SCALE, 1.0F, FANCY_CLOUD_SCALE);

		for(int pass = 0; pass < 2; pass++) {
			if(pass == 0) {
				GL11.glColorMask(false, false, false, false);
			} else if(mc.gameSettings.anaglyph) {
				if(EntityRenderer.anaglyphField == 0) {
					GL11.glColorMask(false, true, true, true);
				} else {
					GL11.glColorMask(true, false, false, true);
				}
			} else {
				GL11.glColorMask(true, true, true, true);
			}

			for(int tileX = -FANCY_CLOUD_RADIUS + 1; tileX <= FANCY_CLOUD_RADIUS; tileX++) {
				for(int tileZ = -FANCY_CLOUD_RADIUS + 1; tileZ <= FANCY_CLOUD_RADIUS; tileZ++) {
					Tessellator tessellator = Tessellator.instance;
					tessellator.startDrawingQuads();

					float minCloudX = tileX * FANCY_CLOUD_TILE_SIZE;
					float minCloudZ = tileZ * FANCY_CLOUD_TILE_SIZE;
					float cloudX = minCloudX - cloudXOffset;
					float cloudZ = minCloudZ - cloudZOffset;

					if(relativeCloudY > -FANCY_CLOUD_DEPTH - 1.0F) {
						tessellator.setColorRGBA_F(cloudRed * 0.7F, cloudGreen * 0.7F, cloudBlue * 0.7F, alpha);
						tessellator.setNormal(0.0F, -1.0F, 0.0F);
						addHorizontalFace(tessellator, cloudX, relativeCloudY, cloudZ, minCloudX, minCloudZ, cloudUBase, cloudVBase, false);
					}

					if(relativeCloudY <= FANCY_CLOUD_DEPTH + 1.0F) {
						tessellator.setColorRGBA_F(cloudRed, cloudGreen, cloudBlue, alpha);
						tessellator.setNormal(0.0F, 1.0F, 0.0F);
						addHorizontalFace(tessellator, cloudX, relativeCloudY + FANCY_CLOUD_DEPTH - 9.765625E-4F, cloudZ, minCloudX, minCloudZ, cloudUBase, cloudVBase, true);
					}

					tessellator.setColorRGBA_F(cloudRed * 0.9F, cloudGreen * 0.9F, cloudBlue * 0.9F, alpha);
					if(tileX > -1) {
						tessellator.setNormal(-1.0F, 0.0F, 0.0F);
						addVerticalXFaces(tessellator, cloudX, relativeCloudY, cloudZ, minCloudX, minCloudZ, cloudUBase, cloudVBase, 0.0F);
					}
					if(tileX <= 1) {
						tessellator.setNormal(1.0F, 0.0F, 0.0F);
						addVerticalXFaces(tessellator, cloudX, relativeCloudY, cloudZ, minCloudX, minCloudZ, cloudUBase, cloudVBase, 1.0F - 9.765625E-4F);
					}

					tessellator.setColorRGBA_F(cloudRed * 0.8F, cloudGreen * 0.8F, cloudBlue * 0.8F, alpha);
					if(tileZ > -1) {
						tessellator.setNormal(0.0F, 0.0F, -1.0F);
						addVerticalZFaces(tessellator, cloudX, relativeCloudY, cloudZ, minCloudX, minCloudZ, cloudUBase, cloudVBase, 0.0F);
					}
					if(tileZ <= 1) {
						tessellator.setNormal(0.0F, 0.0F, 1.0F);
						addVerticalZFaces(tessellator, cloudX, relativeCloudY, cloudZ, minCloudX, minCloudZ, cloudUBase, cloudVBase, 1.0F - 9.765625E-4F);
					}

					tessellator.draw();
				}
			}
		}

		GL11.glColorMask(true, true, true, true);
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		GL11.glDisable(GL11.GL_BLEND);
		GL11.glEnable(GL11.GL_CULL_FACE);
		GL11.glPopMatrix();
	}

	private void addHorizontalFace(Tessellator tessellator, float cloudX, float cloudY, float cloudZ, float minCloudX, float minCloudZ, float uvBaseX, float uvBaseZ, boolean topFace) {
		float maxCloudCoord = FANCY_CLOUD_TILE_SIZE;
		float worldMinZ = topFace ? cloudZ : cloudZ + maxCloudCoord;
		float worldMaxZ = topFace ? cloudZ + maxCloudCoord : cloudZ;
		float uvMinZ = topFace ? minCloudZ : minCloudZ + maxCloudCoord;
		float uvMaxZ = topFace ? minCloudZ + maxCloudCoord : minCloudZ;

		tessellator.addVertexWithUV(cloudX + 0.0F, cloudY, worldMinZ, (minCloudX + 0.0F) * 0.00390625F + uvBaseX, uvMinZ * 0.00390625F + uvBaseZ);
		tessellator.addVertexWithUV(cloudX + maxCloudCoord, cloudY, worldMinZ, (minCloudX + maxCloudCoord) * 0.00390625F + uvBaseX, uvMinZ * 0.00390625F + uvBaseZ);
		tessellator.addVertexWithUV(cloudX + maxCloudCoord, cloudY, worldMaxZ, (minCloudX + maxCloudCoord) * 0.00390625F + uvBaseX, uvMaxZ * 0.00390625F + uvBaseZ);
		tessellator.addVertexWithUV(cloudX + 0.0F, cloudY, worldMaxZ, (minCloudX + 0.0F) * 0.00390625F + uvBaseX, uvMaxZ * 0.00390625F + uvBaseZ);
	}

	private void addVerticalXFaces(Tessellator tessellator, float cloudX, float cloudY, float cloudZ, float minCloudX, float minCloudZ, float uvBaseX, float uvBaseZ, float xOffset) {
		for(int face = 0; face < FANCY_CLOUD_TILE_SIZE; face++) {
			float faceX = cloudX + face + xOffset;
			float uvX = (minCloudX + face + 0.5F) * 0.00390625F + uvBaseX;

			tessellator.addVertexWithUV(faceX, cloudY + 0.0F, cloudZ + FANCY_CLOUD_TILE_SIZE, uvX, (minCloudZ + FANCY_CLOUD_TILE_SIZE) * 0.00390625F + uvBaseZ);
			tessellator.addVertexWithUV(faceX, cloudY + FANCY_CLOUD_DEPTH, cloudZ + FANCY_CLOUD_TILE_SIZE, uvX, (minCloudZ + FANCY_CLOUD_TILE_SIZE) * 0.00390625F + uvBaseZ);
			tessellator.addVertexWithUV(faceX, cloudY + FANCY_CLOUD_DEPTH, cloudZ + 0.0F, uvX, (minCloudZ + 0.0F) * 0.00390625F + uvBaseZ);
			tessellator.addVertexWithUV(faceX, cloudY + 0.0F, cloudZ + 0.0F, uvX, (minCloudZ + 0.0F) * 0.00390625F + uvBaseZ);
		}
	}

	private void addVerticalZFaces(Tessellator tessellator, float cloudX, float cloudY, float cloudZ, float minCloudX, float minCloudZ, float uvBaseX, float uvBaseZ, float zOffset) {
		for(int face = 0; face < FANCY_CLOUD_TILE_SIZE; face++) {
			float faceZ = cloudZ + face + zOffset;
			float uvZ = (minCloudZ + face + 0.5F) * 0.00390625F + uvBaseZ;

			tessellator.addVertexWithUV(cloudX + 0.0F, cloudY + FANCY_CLOUD_DEPTH, faceZ, (minCloudX + 0.0F) * 0.00390625F + uvBaseX, uvZ);
			tessellator.addVertexWithUV(cloudX + FANCY_CLOUD_TILE_SIZE, cloudY + FANCY_CLOUD_DEPTH, faceZ, (minCloudX + FANCY_CLOUD_TILE_SIZE) * 0.00390625F + uvBaseX, uvZ);
			tessellator.addVertexWithUV(cloudX + FANCY_CLOUD_TILE_SIZE, cloudY + 0.0F, faceZ, (minCloudX + FANCY_CLOUD_TILE_SIZE) * 0.00390625F + uvBaseX, uvZ);
			tessellator.addVertexWithUV(cloudX + 0.0F, cloudY + 0.0F, faceZ, (minCloudX + 0.0F) * 0.00390625F + uvBaseX, uvZ);
		}
	}

	private double getCloudTime(WorldClient world, Minecraft mc, float partialTicks) {
		int tickCounter = mc.thePlayer != null ? mc.thePlayer.ticksExisted : (int) (world.getTotalWorldTime() & Integer.MAX_VALUE);
		return tickCounter + partialTicks;
	}

	private double getLayerPositionOffset(WorldClient world, int layerIndex, long salt) {
		double normalized = getLayerRandom(world, layerIndex, salt);
		return (normalized - 0.5D) * LAYER_POSITION_RANGE;
	}

	private double getLayerRandom(WorldClient world, int layerIndex, long salt) {
		long seed = world.getSeed();
		long dimension = world.provider != null ? world.provider.dimensionId : 0L;
		long mixed = mixSeed(seed ^ (dimension * 0x9E3779B97F4A7C15L) ^ ((long) (layerIndex + 1) * 0xC2B2AE3D27D4EB4FL) ^ salt);
		return (double) (mixed & ((1L << 53) - 1)) / (double) (1L << 53);
	}

	private long mixSeed(long value) {
		value ^= value >>> 33;
		value *= 0xff51afd7ed558ccdL;
		value ^= value >>> 33;
		value *= 0xc4ceb9fe1a85ec53L;
		value ^= value >>> 33;
		return value;
	}

	private static boolean isNeutralCloudFluid(FluidType fluid) {
		return fluid == Fluids.EARTHAIR
			|| fluid == Fluids.OXYGEN
			|| fluid == Fluids.NITROGEN
			|| fluid == Fluids.DUNAAIR
			|| fluid == Fluids.CARBONDIOXIDE;
	}

	private static double[] getCloudTintData(CBT_Atmosphere atmosphere) {
		if(atmosphere == null || atmosphere.fluids.isEmpty()) {
			return null;
		}

		double totalPressure = 0.0D;
		double tintPressure = 0.0D;
		double tintR = 0.0D;
		double tintG = 0.0D;
		double tintB = 0.0D;

		for(int i = 0; i < atmosphere.fluids.size(); i++) {
			FluidEntry entry = atmosphere.fluids.get(i);
			if(entry == null || entry.fluid == null || entry.pressure <= 0.0D) {
				continue;
			}

			totalPressure += entry.pressure;

			if(isNeutralCloudFluid(entry.fluid)) {
				continue;
			}

			Vec3 fluidColor = WorldProviderCelestial.getAtmosphereFluidColor(entry.fluid);
			tintPressure += entry.pressure;
			tintR += fluidColor.xCoord * entry.pressure;
			tintG += fluidColor.yCoord * entry.pressure;
			tintB += fluidColor.zCoord * entry.pressure;
		}

		if(totalPressure <= 0.0D || tintPressure <= 0.0D) {
			return null;
		}

		tintR /= tintPressure;
		tintG /= tintPressure;
		tintB /= tintPressure;

		double tintPeak = Math.max(tintR, Math.max(tintG, tintB));
		if(tintPeak <= 0.0D) {
			return null;
		}

		double tintStrength = MathHelper.clamp_double(tintPressure / totalPressure, 0.0D, 0.65D);
		return new double[] {
			tintStrength,
			MathHelper.clamp_double(tintR / tintPeak, 0.0D, 1.0D),
			MathHelper.clamp_double(tintG / tintPeak, 0.0D, 1.0D),
			MathHelper.clamp_double(tintB / tintPeak, 0.0D, 1.0D)
		};
	}

	public static float getCloudTintStrength(CBT_Atmosphere atmosphere) {
		double[] tintData = getCloudTintData(atmosphere);
		return tintData != null ? (float) tintData[0] : 0.0F;
	}

	public static Vec3 getTintedCloudColor(CBT_Atmosphere atmosphere, Vec3 clouds) {
		if(clouds == null) {
			clouds = Vec3.createVectorHelper(1.0D, 1.0D, 1.0D);
		}

		double[] tintData = getCloudTintData(atmosphere);
		if(tintData == null) {
			return clouds;
		}

		double cloudLuma = clouds.xCoord * 0.299D + clouds.yCoord * 0.587D + clouds.zCoord * 0.114D;
		double cloudBrightness = MathHelper.clamp_double(cloudLuma * 0.9D, 0.0D, 1.0D);
		double tintStrength = tintData[0];
		double cloudColorR = MathHelper.clamp_double(tintData[1] * cloudBrightness, 0.0D, 1.0D);
		double cloudColorG = MathHelper.clamp_double(tintData[2] * cloudBrightness, 0.0D, 1.0D);
		double cloudColorB = MathHelper.clamp_double(tintData[3] * cloudBrightness, 0.0D, 1.0D);

		return Vec3.createVectorHelper(
			MathHelper.clamp_double(clouds.xCoord + (cloudColorR - clouds.xCoord) * tintStrength, 0.0D, 1.0D),
			MathHelper.clamp_double(clouds.yCoord + (cloudColorG - clouds.yCoord) * tintStrength, 0.0D, 1.0D),
			MathHelper.clamp_double(clouds.zCoord + (cloudColorB - clouds.zCoord) * tintStrength, 0.0D, 1.0D)
		);
	}

	private float[] applyAnaglyph(float red, float green, float blue) {
		return new float[] {
			(red * 30.0F + green * 59.0F + blue * 11.0F) / 100.0F,
			(red * 30.0F + green * 70.0F) / 100.0F,
			(red * 30.0F + blue * 70.0F) / 100.0F
		};
	}
}
