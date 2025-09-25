package com.hbm.dim.dima;

import com.hbm.blocks.ModBlocks;
import com.hbm.dim.WorldProviderCelestial;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.util.Vec3;
import net.minecraft.world.biome.WorldChunkManagerHell;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraftforge.client.IRenderHandler;

public class WorldProviderDima extends WorldProviderCelestial {

	@Override
	public void registerWorldChunkManager() {
		worldChunkMgr = new WorldChunkManagerHell(BiomeGenDima.biome, 0.0F);
	}

	@Override
	public String getDimensionName() {
		return "Dima";
	}

	@Override
	public IChunkProvider createChunkGenerator() {
		return new ChunkProviderDima(this.worldObj, this.getSeed(), false);
	}

	@Override
	public Block getStone() {
		return ModBlocks.dima_stone;
	}

	@Override
	public boolean hasIce() {
		return true;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public IRenderHandler getSkyRenderer() {
		return new SkyProviderDima();
	}

	private IRenderHandler weatherProvider;

	@Override
	@SideOnly(Side.CLIENT)
	public IRenderHandler getWeatherRenderer() {
		if(weatherProvider == null) weatherProvider = new WeatherProviderDima();
		return weatherProvider;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public Vec3 getFogColor(float solarAngle, float y) {
		return Vec3.createVectorHelper(0.125, 0, 0);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public Vec3 getSkyColor(Entity camera, float partialTicks) {
		// getSkyColor is called first on every frame, so if you want to memoise anything, do it here
		updateSky(partialTicks);

		return Vec3.createVectorHelper(0, 0, 0);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public float getSunBrightness(float par1) {
		return Math.min(super.getSunBrightness(par1), 0.5F);
	}

	@Override
	public boolean updateLightmap(int[] lightmap) {
		float sun = getSunBrightness(1.0F);
		for(int i = 0; i < 256; i++) {
			// float sky = lightBrightnessTable[i / 16];
			if(i / 15 >= 14) continue;

			float diggems = Math.max(sun, 0);

			int[] color = unpackColor(lightmap[i]);

			color[1] -= 60;
			if(color[1] < 0) color[1] = 0;
			color[2] -= 60;
			if(color[2] < 0) color[2] = 0;

			color[0] += diggems * 240 - 60;
			if(color[0] < 0) color[0] = 0;
			if(color[0] > 255) color[0] = 255;

			lightmap[i] = packColor(color);
		}
		return true;
	}

}
