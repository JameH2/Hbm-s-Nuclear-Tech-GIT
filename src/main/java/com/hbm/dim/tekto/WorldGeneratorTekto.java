package com.hbm.dim.tekto;

import java.util.Random;

import com.hbm.blocks.ModBlocks;
import com.hbm.blocks.generic.BlockOre;
import com.hbm.config.SpaceConfig;
import com.hbm.config.WorldConfig;
import com.hbm.dim.CelestialBody;
import com.hbm.dim.SolarSystem;
import com.hbm.dim.WorldProviderCelestial;
import com.hbm.dim.tekto.biome.BiomeGenBaseTekto;
import com.hbm.world.gen.nbt.NBTStructure;
import com.hbm.world.generator.DungeonToolbox;

import cpw.mods.fml.common.IWorldGenerator;
import net.minecraft.block.Block;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.feature.WorldGenAbstractTree;

public class WorldGeneratorTekto implements IWorldGenerator {

	public WorldGeneratorTekto() {
		NBTStructure.registerNullWeight(SpaceConfig.tektoDimension, 24);

		BlockOre.addValidBody(ModBlocks.ore_tekto, SolarSystem.Body.TEKTO);
	}

	@Override
	public void generate(Random random, int chunkX, int chunkZ, World world, IChunkProvider chunkGenerator, IChunkProvider chunkProvider) {
		if(world.provider.dimensionId == SpaceConfig.tektoDimension) {
			generateTekto(world, random, chunkX * 16, chunkZ * 16);
		}
	}

	private void generateTekto(World world, Random rand, int i, int j) {
		int meta = CelestialBody.getMeta(world);
		Block stone = ((WorldProviderCelestial) world.provider).getStone();

		DungeonToolbox.generateOre(world, rand, i, j, WorldConfig.cobaltSpawn * 2,  6, 4, 8, ModBlocks.ore_cobalt, meta, stone);

		BiomeGenBase biome = world.getBiomeGenForCoords(i + 16, j + 16);

		for(int x = 0; x < 16; x++) {
			for(int z = 0; z < 16; z++) {
				for(int y = 32; y < 128; y++) {
					int ox = i + x + 8;
					int oz = j + z + 8;
					Block b = world.getBlock(ox, y, oz);
					if(b == ModBlocks.geysir_chloric) {
						world.setBlock(ox, y, oz, ModBlocks.geysir_chloric);
						world.markBlockForUpdate(ox, y, oz);
					}
				}
			}
		}

		if(biome == BiomeGenBaseTekto.polyvinylPlains) {
			for(int o = 0; o < 2; o++) {
				if(rand.nextInt(10) == 0) {
					int x = i + rand.nextInt(16) + 8;
					int z = j + rand.nextInt(16) + 8;
					int y = world.getHeightValue(x, z);

					WorldGenAbstractTree customTreeGen = new TTree(false, 4, 2, 10, 2, 4, false, ModBlocks.pvc_log, ModBlocks.rubber_leaves);
					customTreeGen.generate(world, rand, x, y, z);
				}

				if(rand.nextInt(8) == 0) {
					int x = i + rand.nextInt(16) + 8;
					int z = j + rand.nextInt(16) + 8;
					int y = world.getHeightValue(x, z);

					WorldGenAbstractTree chopped = new TTree(false, 2, 4, 5, 3, 2, false, ModBlocks.vinyl_log, ModBlocks.pet_leaves);
					chopped.generate(world, rand, x, y, z);
				}
			}

		}

		if(biome == BiomeGenBaseTekto.halogenHills) {
			if(rand.nextInt(12) == 0) {
				for(int o = 0; o < 4; o++) {
					int x = i + rand.nextInt(16) + 8;
					int z = j + rand.nextInt(16) + 8;
					int y = world.getHeightValue(x, z);

					WorldGenAbstractTree customTreeGen = new TTree(false, 3, 2, 14, 3, 3, false, ModBlocks.pvc_log, ModBlocks.rubber_leaves);
					customTreeGen.generate(world, rand, x, y, z);
				}
			}
		}

		if(biome == BiomeGenBaseTekto.forest) {
			for(int o = 0; o < 8; o++) {
				int x = i + rand.nextInt(16) + 8;
				int z = j + rand.nextInt(16) + 8;
				int y = world.getHeightValue(x, z);

				if(rand.nextInt(2) == 0) {
					WorldGenAbstractTree customTreeGen = new TTree(false, 3, 2, 20, 3, 5, true, ModBlocks.pvc_log, ModBlocks.rubber_leaves);
					customTreeGen.generate(world, rand, x, y, z);
				} else {
					WorldGenAbstractTree tustomTreeGen = new TTree(false, 3, 1, 1, 3, 5, false, ModBlocks.pvc_log, ModBlocks.rubber_leaves);
					tustomTreeGen.generate(world, rand, x, y, z);
				}
			}
		}
	}

}
