package com.hbm.dim.eve;

import java.util.Random;

import com.hbm.blocks.ModBlocks;
import com.hbm.blocks.generic.BlockOre;
import com.hbm.config.SpaceConfig;
import com.hbm.dim.CelestialBody;
import com.hbm.dim.SolarSystem;
import com.hbm.dim.WorldProviderCelestial;
import com.hbm.dim.eve.biome.BiomeGenBaseEve;
import com.hbm.dim.eve.genlayer.WorldGenElectricVolcano;
import com.hbm.dim.eve.genlayer.WorldGenEveSpike;
import com.hbm.world.gen.nbt.NBTStructure;
import com.hbm.world.generator.DungeonToolbox;

import cpw.mods.fml.common.IWorldGenerator;
import net.minecraft.block.Block;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.chunk.IChunkProvider;

public class WorldGeneratorEve implements IWorldGenerator {

	WorldGenElectricVolcano volcano = new WorldGenElectricVolcano(30, 22, ModBlocks.eve_silt, ModBlocks.eve_rock);

	public WorldGeneratorEve() {
		NBTStructure.registerNullWeight(SpaceConfig.eveDimension, 24);

		BlockOre.addValidBody(ModBlocks.ore_niobium, SolarSystem.Body.EVE);
		BlockOre.addValidBody(ModBlocks.ore_iodine, SolarSystem.Body.EVE);
		BlockOre.addValidBody(ModBlocks.ore_schrabidium, SolarSystem.Body.EVE);
		BlockOre.addValidBody(ModBlocks.ore_gas, SolarSystem.Body.EVE);
	}

	@Override
	public void generate(Random random, int chunkX, int chunkZ, World world, IChunkProvider chunkGenerator, IChunkProvider chunkProvider) {
		if(world.provider.dimensionId == SpaceConfig.eveDimension) {
			generateEve(world, random, chunkX * 16, chunkZ * 16);
		}
	}

	private void generateEve(World world, Random rand, int i, int j) {
		int meta = CelestialBody.getMeta(world);
		Block stone = ((WorldProviderCelestial) world.provider).getStone();

		DungeonToolbox.generateOre(world, rand, i, j, 12,  8, 1, 33, ModBlocks.ore_niobium, meta, stone);
		DungeonToolbox.generateOre(world, rand, i, j, 8,  4, 5, 48, ModBlocks.ore_iodine, meta, stone);
		DungeonToolbox.generateOre(world, rand, i, j, 1,  4, 1, 16, ModBlocks.ore_schrabidium, meta, stone);

		int x = i + rand.nextInt(16) + 8;
		int z = j + rand.nextInt(16) + 8;
		int y = world.getHeightValue(x, z);

		BiomeGenBase biome = world.getBiomeGenForCoords(x, z);
		if(biome == BiomeGenBaseEve.eveSeismicPlains) {
			new WorldGenEveSpike().generate(world, rand, x, y, z);
		}

		// TODO: these span multiple chunks, fix this cascade!
		if(rand.nextInt(100) == 0) {
			volcano.generate(world, rand, x, y, z);
		}
	}

}
