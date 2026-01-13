package com.hbm.dim.minmus;

import com.hbm.dim.WorldChunkManagerCelestial.BiomeGenLayers;
import com.hbm.blocks.ModBlocks;
import com.hbm.dim.GenLayerDiversify;
import com.hbm.dim.WorldChunkManagerCelestial;
import com.hbm.dim.WorldProviderCelestial;
import com.hbm.dim.minmus.biome.BiomeGenBaseMinmus;
import com.hbm.dim.minmus.genlayer.GenLayerMinmusBasins;
import com.hbm.dim.minmus.genlayer.GenLayerMinmusBiomes;
import com.hbm.dim.minmus.genlayer.GenLayerMinmusPlains;

import net.minecraft.block.Block;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.layer.GenLayer;
import net.minecraft.world.gen.layer.GenLayerFuzzyZoom;
import net.minecraft.world.gen.layer.GenLayerSmooth;
import net.minecraft.world.gen.layer.GenLayerVoronoiZoom;
import net.minecraft.world.gen.layer.GenLayerZoom;

public class WorldProviderMinmus extends WorldProviderCelestial {

	@Override
	public void registerWorldChunkManager() {
		this.worldChunkMgr = new WorldChunkManagerCelestial(createBiomeGenerators(worldObj.getSeed()));
	}

	@Override
	public String getDimensionName() {
		return "Minmus";
	}

	@Override
	public IChunkProvider createChunkGenerator() {
		return new ChunkProviderMinmus(this.worldObj, this.getSeed(), false);
	}

	@Override
	public Block getStone() {
		return ModBlocks.minmus_stone;
	}

	private static BiomeGenLayers createBiomeGenerators(long seed) {
		GenLayer biomes = new GenLayerMinmusBiomes(seed);

		biomes = new GenLayerFuzzyZoom(2000L, biomes);
		biomes = new GenLayerZoom(2001L, biomes);
		biomes = new GenLayerDiversify(1000L, biomes, 2, BiomeGenBaseMinmus.minmusCanyon, BiomeGenBaseMinmus.minmusPlains);
		biomes = new GenLayerZoom(1000L, biomes);
		biomes = new GenLayerDiversify(1001L, biomes, 2, BiomeGenBaseMinmus.minmusCanyon, BiomeGenBaseMinmus.minmusPlains);
		biomes = new GenLayerZoom(1001L, biomes);
		biomes = new GenLayerMinmusBasins(3000L, biomes);
		biomes = new GenLayerZoom(1003L, biomes);
		biomes = new GenLayerSmooth(700L, biomes);
		biomes = new GenLayerMinmusPlains(200L, biomes);
		biomes = new GenLayerZoom(1006L, biomes);

		GenLayer genLayerVoronoiZoom = new GenLayerVoronoiZoom(10L, biomes);

		return new BiomeGenLayers(biomes, genLayerVoronoiZoom, seed);
	}

}