package com.hbm.dim.moho;

import com.hbm.blocks.ModBlocks;
import com.hbm.dim.GenLayerDiversify;
import com.hbm.dim.WorldChunkManagerCelestial;
import com.hbm.dim.WorldChunkManagerCelestial.BiomeGenLayers;
import com.hbm.dim.WorldProviderCelestial;
import com.hbm.dim.moho.biome.BiomeGenBaseMoho;
import com.hbm.dim.moho.genlayer.GenLayerMohoBiomes;
import com.hbm.dim.moho.genlayer.GenLayerMohoPlateauExtend;

import net.minecraft.block.Block;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.layer.GenLayer;
import net.minecraft.world.gen.layer.GenLayerFuzzyZoom;
import net.minecraft.world.gen.layer.GenLayerRiver;
import net.minecraft.world.gen.layer.GenLayerRiverMix;
import net.minecraft.world.gen.layer.GenLayerSmooth;
import net.minecraft.world.gen.layer.GenLayerVoronoiZoom;
import net.minecraft.world.gen.layer.GenLayerZoom;

public class WorldProviderMoho extends WorldProviderCelestial {

	@Override
	public void registerWorldChunkManager() {
		this.worldChunkMgr = new WorldChunkManagerCelestial(createBiomeGenerators(worldObj.getSeed()));
	}

	@Override
	public String getDimensionName() {
		return "Moho";
	}

	@Override
	public IChunkProvider createChunkGenerator() {
		return new ChunkProviderMoho(this.worldObj, this.getSeed(), false);
	}

	@Override
	public Block getStone() {
		return ModBlocks.moho_stone;
	}

	private static BiomeGenLayers createBiomeGenerators(long seed) {
		GenLayer biomes = new GenLayerMohoBiomes(seed);

		biomes = new GenLayerFuzzyZoom(2000L, biomes);
		biomes = new GenLayerDiversify(1234L, biomes, 6, BiomeGenBaseMoho.mohoBasalt);
		biomes = new GenLayerZoom(2001L, biomes);
		biomes = new GenLayerMohoPlateauExtend(9944L, biomes);
		biomes = new GenLayerZoom(1006L, biomes);
		biomes = new GenLayerSmooth(700L, biomes);
		biomes = new GenLayerZoom(1000L, biomes);
		biomes = new GenLayerZoom(107L, biomes);

		// biome detail layer should ignore rivers (for monster spawns and biome block replacement)
		GenLayer genlayerVoronoiZoom = new GenLayerVoronoiZoom(10L, biomes);

		GenLayer genlayerRiver = new GenLayerRiver(1004L, biomes);
		genlayerRiver = new GenLayerZoom(105L, genlayerRiver);
		genlayerRiver = new GenLayerZoom(106L, genlayerRiver); // Added extra zoom for more frequent rivers

		// apply the rivers to the biome map
		GenLayer genlayerRiverMix = new GenLayerRiverMix(100L, biomes, genlayerRiver);

		return new BiomeGenLayers(genlayerRiverMix, genlayerVoronoiZoom, seed);
	}

}