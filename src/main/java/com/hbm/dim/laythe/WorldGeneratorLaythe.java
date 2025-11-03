package com.hbm.dim.laythe;

import java.util.Random;

import com.hbm.blocks.ModBlocks;
import com.hbm.blocks.generic.BlockOre;
import com.hbm.config.SpaceConfig;
import com.hbm.config.WorldConfig;
import com.hbm.dim.CelestialBody;
import com.hbm.dim.SolarSystem;
import com.hbm.dim.WorldProviderCelestial;
import com.hbm.dim.laythe.biome.BiomeGenBaseLaythe;
import com.hbm.main.StructureManager;
import com.hbm.world.gen.nbt.NBTStructure;
import com.hbm.world.gen.nbt.JigsawPiece;
import com.hbm.world.gen.nbt.SpawnCondition;
import com.hbm.world.generator.DungeonToolbox;

import cpw.mods.fml.common.IWorldGenerator;
import net.minecraft.block.Block;
import net.minecraft.world.World;
import net.minecraft.world.chunk.IChunkProvider;

public class WorldGeneratorLaythe implements IWorldGenerator {

	public WorldGeneratorLaythe() {
		NBTStructure.registerStructure(SpaceConfig.laytheDimension, new SpawnCondition("laythe_nuke_sub") {{
			structure = new JigsawPiece("laythe_nuke_sub", StructureManager.nuke_sub);
			canSpawn = biome -> biome == BiomeGenBaseLaythe.laytheOcean;
			maxHeight = 54;
			spawnWeight = 6;
		}});
		NBTStructure.registerStructure(SpaceConfig.laytheDimension, new SpawnCondition("laythe_vertibird") {{
			structure = new JigsawPiece("laythe_vertibird", StructureManager.vertibird, -3);
			canSpawn = biome -> biome.rootHeight >= 0;
			spawnWeight = 6;
		}});
		NBTStructure.registerStructure(SpaceConfig.laytheDimension, new SpawnCondition("laythe_crashed_vertibird") {{
			structure = new JigsawPiece("laythe_crashed_vertibird", StructureManager.crashed_vertibird, -10);
			canSpawn = biome -> biome.rootHeight >= 0;
			spawnWeight = 6;
		}});

		BlockOre.addValidBody(ModBlocks.ore_emerald, SolarSystem.Body.LAYTHE);
		BlockOre.addValidBody(ModBlocks.ore_lapis, SolarSystem.Body.LAYTHE);
		BlockOre.addValidBody(ModBlocks.ore_asbestos, SolarSystem.Body.LAYTHE);
		BlockOre.addValidBody(ModBlocks.ore_oil, SolarSystem.Body.LAYTHE);
	}

	@Override
	public void generate(Random random, int chunkX, int chunkZ, World world, IChunkProvider chunkGenerator, IChunkProvider chunkProvider) {
		if(world.provider.dimensionId == SpaceConfig.laytheDimension) {
			generateLaythe(world, random, chunkX * 16, chunkZ * 16);
		}
	}

	private void generateLaythe(World world, Random rand, int i, int j) {
		int meta = CelestialBody.getMeta(world);
		Block stone = ((WorldProviderCelestial) world.provider).getStone();

		DungeonToolbox.generateOre(world, rand, i, j, WorldConfig.asbestosSpawn, 4, 16, 16, ModBlocks.ore_asbestos, meta, stone);
		DungeonToolbox.generateOre(world, rand, i, j, WorldConfig.berylliumSpawn, 4, 5, 30, ModBlocks.ore_beryllium, meta, stone);
		DungeonToolbox.generateOre(world, rand, i, j, WorldConfig.rareSpawn, 5, 5, 20, ModBlocks.ore_rare, meta, stone);
	}

}
