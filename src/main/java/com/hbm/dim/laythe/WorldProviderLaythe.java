package com.hbm.dim.laythe;

import com.hbm.dim.WorldChunkManagerCelestial.BiomeGenLayers;

import com.hbm.dim.WorldChunkManagerCelestial;
import com.hbm.dim.WorldProviderCelestial;
import com.hbm.dim.laythe.biome.BiomeGenBaseLaythe;
import com.hbm.dim.laythe.genlayer.GenLayerDiversifyLaythe;
import com.hbm.dim.laythe.genlayer.GenLayerLaytheBiomes;
import com.hbm.dim.laythe.genlayer.GenLayerLaytheIslands;
import com.hbm.dim.laythe.genlayer.GenLayerLaytheOceans;
import com.hbm.dim.laythe.genlayer.GenLayerLaythePolar;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.material.Material;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.layer.GenLayer;
import net.minecraft.world.gen.layer.GenLayerSmooth;
import net.minecraft.world.gen.layer.GenLayerVoronoiZoom;
import net.minecraft.world.gen.layer.GenLayerZoom;
import net.minecraftforge.client.IRenderHandler;
import net.minecraftforge.client.event.EntityViewRenderEvent.FogDensity;

public class WorldProviderLaythe extends WorldProviderCelestial {

	@Override
	public void registerWorldChunkManager() {
		this.worldChunkMgr = new WorldChunkManagerCelestial(createBiomeGenerators(worldObj.getSeed()));
	}

	@Override
	public String getDimensionName() {
		return "Laythe";
	}

	@Override
	public IChunkProvider createChunkGenerator() {
		return new ChunkProviderLaythe(this.worldObj, this.getSeed(), false);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public IRenderHandler getSkyRenderer() {
		if(skyRenderer == null) skyRenderer = new SkyProviderLaytheSunset();
		return skyRenderer;
	}

	@Override
	public boolean hasLife() {
		return true;
	}

	@Override
	public int getWaterOpacity() {
		return 1;
	}

	@Override
	public float fogDensity(FogDensity event) {
		if(event.block.getMaterial() == Material.water) {
			return 0.03F;
		}

		return super.fogDensity(event);
	}

	@Override
	public boolean updateLightmap(int[] lightmap) {
		float sun = getSunBrightness(1.0F);
		for(int i = 0; i < 256; i++) {
			float sky = lightBrightnessTable[i / 16];
			float jool = Math.max(sky - sun, 0);

			int[] color = unpackColor(lightmap[i]);

			color[1] += jool * 60;
			if(color[1] > 255) color[1] = 255;

			lightmap[i] = packColor(color);
		}
		return true;
	}

	private static BiomeGenLayers createBiomeGenerators(long seed) {
		GenLayer biomes = new GenLayerLaytheBiomes(seed);
		biomes = new GenLayerLaythePolar(1000L, biomes);

		biomes = new GenLayerZoom(1040L, biomes);
		biomes = GenLayerZoom.magnify(1000L, biomes, 1);
		biomes = new GenLayerLaythePolar(1300L, biomes);

		biomes = new GenLayerDiversifyLaythe(1000L, biomes);
		biomes = new GenLayerLaytheOceans(56000L, biomes);
		biomes = new GenLayerLaytheIslands(200L, biomes);

		biomes = new GenLayerZoom(1200L, biomes);
		biomes = new GenLayerZoom(1001L, biomes);

		biomes = new GenLayerLaytheOceans(4000L, biomes);
		biomes = new GenLayerLaytheOceans(4001L, biomes, BiomeGenBaseLaythe.laytheCoast.biomeID);

		biomes = new GenLayerZoom(1003L, biomes);
		biomes = new GenLayerLaytheOceans(4342L, biomes, BiomeGenBaseLaythe.laytheCoast.biomeID);
		biomes = new GenLayerSmooth(700L, biomes);

		biomes = new GenLayerZoom(1006L, biomes);

		GenLayer genLayerVoronoiZoom = new GenLayerVoronoiZoom(10L, biomes);

		return new BiomeGenLayers(biomes, genLayerVoronoiZoom, seed);
	}

}