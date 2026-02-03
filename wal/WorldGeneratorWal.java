package com.hbm.dim.wal;

import java.util.Random;

import com.hbm.blocks.ModBlocks;
import com.hbm.blocks.generic.BlockOre;
import com.hbm.config.SpaceConfig;
import com.hbm.config.WorldConfig;
import com.hbm.dim.CelestialBody;
import com.hbm.dim.SolarSystem;
import com.hbm.dim.WorldProviderCelestial;
import com.hbm.main.StructureManager;
import com.hbm.world.gen.nbt.NBTStructure;
import com.hbm.world.gen.nbt.JigsawPiece;
import com.hbm.world.gen.nbt.JigsawPool;
import com.hbm.world.gen.nbt.SpawnCondition;
import com.hbm.world.generator.DungeonToolbox;

import cpw.mods.fml.common.IWorldGenerator;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.chunk.IChunkProvider;

public class WorldGeneratorWal implements IWorldGenerator {

	public WorldGeneratorWal() {

		// Use maximum safe weight for guaranteed structure removal
		NBTStructure.registerNullWeight(SpaceConfig.walDimension, 1000);

		BlockOre.addValidBody(ModBlocks.ore_cadmium, SolarSystem.Body.WAL);
		BlockOre.addValidBody(ModBlocks.ore_arsenic, SolarSystem.Body.WAL);
		BlockOre.addValidBody(ModBlocks.ore_shale, SolarSystem.Body.WAL);

	}

	@Override
	public void generate(Random random, int chunkX, int chunkZ, World world, IChunkProvider chunkGenerator, IChunkProvider chunkProvider) {
		if(world.provider.dimensionId == SpaceConfig.walDimension) {
			generateWal(world, random, chunkX * 16, chunkZ * 16);
		}
	}

	private void generateWal(World world, Random rand, int i, int j) {
		int meta = CelestialBody.getMeta(world);
		Block stone = ((WorldProviderCelestial) world.provider).getStone();

		// Generate ores
		DungeonToolbox.generateOre(world, rand, i, j, WorldConfig.aluminiumSpawn,  6, 5, 40, ModBlocks.ore_aluminium, meta, stone);
		DungeonToolbox.generateOre(world, rand, i, j, WorldConfig.fluoriteSpawn, 4, 5, 45, ModBlocks.ore_fluorite, meta, stone);
		DungeonToolbox.generateOre(world, rand, i, j, 10, 13, 5, 64, ModBlocks.ore_arsenic, meta, stone);
        DungeonToolbox.generateOre(world, rand, i, j, 10, 6, 4, 8, ModBlocks.ore_cadmium, meta, stone);
		DungeonToolbox.generateOre(world, rand, i, j, 1, 12, 8, 32, ModBlocks.ore_shale, meta, stone);

		// Scan for and update any existing water geysers in this chunk
		for(int x = 0; x < 16; x++) {
			for(int z = 0; z < 16; z++) {
				for(int y = 32; y < 128; y++) {
					int ox = i + x + 8;
					int oz = j + z + 8;
					Block b = world.getBlock(ox, y, oz);
					if(b == ModBlocks.geysir_water) {
						world.setBlock(ox, y, oz, ModBlocks.geysir_water);
						world.markBlockForUpdate(ox, y, oz);
					}
				}
			}
		}
		
	}
}