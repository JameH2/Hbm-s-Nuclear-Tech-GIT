package com.hbm.dim.eve;

import com.hbm.blocks.ModBlocks;
import com.hbm.config.WorldConfig;
import com.hbm.dim.CelestialBody;
import com.hbm.dim.ChunkProviderCelestial;
import com.hbm.dim.eve.biome.BiomeGenBaseEve;
import com.hbm.dim.noise.MapGenVNoise;
import com.hbm.world.gen.terrain.MapGenBubble;

import net.minecraft.init.Blocks;
import net.minecraft.world.World;
import net.minecraft.world.gen.NoiseGeneratorPerlin;

public class ChunkProviderEve extends ChunkProviderCelestial {

	private final NoiseGeneratorPerlin crackNoise;
	private final MapGenVNoise noise = new MapGenVNoise();

	private MapGenBubble oil = new MapGenBubble(WorldConfig.eveGasSpawn);

	public ChunkProviderEve(World world, long seed, boolean hasMapFeatures) {
		super(world, seed, hasMapFeatures);
		reclamp = false;
		stoneBlock = ModBlocks.eve_rock;
		seaBlock = ModBlocks.mercury_block;
		this.crackNoise = new NoiseGeneratorPerlin(world.rand, 4);

		noise.fluidBlock = ModBlocks.mercury_block;
		noise.rockBlock = ModBlocks.eve_rock;
		noise.surfBlock = ModBlocks.eve_silt;
		noise.cellSize = 72;
		noise.crackSize = 2.0;
		noise.plateThickness = 35;
		noise.shapeExponent = 2.0;
		noise.plateStartY = 57;

		noise.applyToBiome = BiomeGenBaseEve.eveOcean;

		oil.block = ModBlocks.ore_gas;
		oil.meta = (byte)CelestialBody.getMeta(world);
		oil.replace = ModBlocks.eve_rock;
		oil.setSize(8, 16);
	}

	@Override
	public BlockMetaBuffer getChunkPrimer(int x, int z) {
		BlockMetaBuffer buffer = super.getChunkPrimer(x, z);
		oil.setMetas(buffer.metas);

		boolean hasOcean = false;
		boolean hasSeismic = false;

		for(int i = 0; i < biomesForGeneration.length; i++) {
			if(biomesForGeneration[i] == BiomeGenBaseEve.eveOcean) hasOcean = true;
			if(biomesForGeneration[i] == BiomeGenBaseEve.eveSeismicPlains) hasSeismic = true;
			if(hasOcean && hasSeismic) break;
		}

		if(hasSeismic) generateCracks(x, z, buffer);
		if(hasOcean) noise.func_151539_a(this, worldObj, x, z, buffer.blocks);

		oil.func_151539_a(this, worldObj, x, z, buffer.blocks);

		return buffer;
	}


	private void generateCracks(int chunkX, int chunkZ, BlockMetaBuffer buffer) {
		for(int x = 0; x < 16; x++) {
			for(int z = 0; z < 16; z++) {
				if(biomesForGeneration[x + z * 16] != BiomeGenBaseEve.eveSeismicPlains) continue;

				double crackValue = crackNoise.func_151601_a((chunkX * 16 + x) * 0.3, (chunkZ * 16 + z) * 0.3);  // Lower scale value for more spread-out cracks

				if(crackValue > 0.9) {
					int bedrockY = -1;
					for(int y = 0; y < 256; y++) {
						int index = (x * 16 + z) * 256 + y;
						if(buffer.blocks[index] == Blocks.bedrock) {
							if(bedrockY == -1) bedrockY = y;
						} else {
							buffer.blocks[index] = Blocks.air;
						}
					}
					if(bedrockY != -1) {
						for(int y = bedrockY + 1; y < Math.min(bedrockY + 10, 256); y++) {
							int index = (x * 16 + z) * 256 + y;
							buffer.blocks[index] = Blocks.lava;
						}
					}
				}
			}
		}
	}

}