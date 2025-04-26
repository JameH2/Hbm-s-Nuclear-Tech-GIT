package com.hbm.dim.moon;

import java.util.HashMap;
import java.util.Random;

import com.hbm.blocks.ModBlocks;
import com.hbm.blocks.generic.BlockOre;
import com.hbm.config.SpaceConfig;
import com.hbm.config.WorldConfig;
import com.hbm.dim.CelestialBody;
import com.hbm.dim.SolarSystem;
import com.hbm.main.StructureManager;
import com.hbm.world.gen.NBTStructure;
import com.hbm.world.gen.NBTStructure.JigsawPiece;
import com.hbm.world.gen.NBTStructure.JigsawPool;
import com.hbm.world.gen.NBTStructure.SpawnCondition;
import com.hbm.world.generator.DungeonToolbox;

import cpw.mods.fml.common.IWorldGenerator;
import net.minecraft.world.World;
import net.minecraft.world.chunk.IChunkProvider;

public class WorldGeneratorMoon implements IWorldGenerator {

	public WorldGeneratorMoon() {
		JigsawPiece munBase = new JigsawPiece("mun_base", StructureManager.mun_base) {{ alignToTerrain = true; heightOffset = -1; }};

		NBTStructure.registerStructure(SpaceConfig.moonDimension, new SpawnCondition() {{
			spawnWeight = 6;
			sizeLimit = 32;
			startPool = "start";
			pools = new HashMap<String, JigsawPool>() {{
				put("start", new JigsawPool() {{
					add(munBase, 1);
				}});
				put("default", new JigsawPool() {{
					add(munBase, 1);
					add(new JigsawPiece("mun_flag", StructureManager.mun_flag) {{ alignToTerrain = true; heightOffset = -1; }}, 2);
					add(new JigsawPiece("mun_panels", StructureManager.mun_panels) {{ alignToTerrain = true; heightOffset = -1; }}, 6);
					add(new JigsawPiece("mun_stardar", StructureManager.mun_stardar) {{ alignToTerrain = true; heightOffset = -1; }}, 1);
					add(new JigsawPiece("mun_tanks", StructureManager.mun_tanks) {{ alignToTerrain = true; heightOffset = -1; }}, 6);
				}});
				put("connect", new JigsawPool() {{
					add(new JigsawPiece("mun_connector_1", StructureManager.mun_connector_1), 1);
					add(new JigsawPiece("mun_connector_2", StructureManager.mun_connector_2), 1);
					add(new JigsawPiece("mun_connector_3", StructureManager.mun_connector_3), 1);
					add(new JigsawPiece("mun_connector_s", StructureManager.mun_connector_s), 1);
					add(new JigsawPiece("mun_connector_l", StructureManager.mun_connector_l), 1);
					add(new JigsawPiece("mun_connector_t", StructureManager.mun_connector_t), 1);
				}});
			}};
		}});

		NBTStructure.registerNullWeight(SpaceConfig.moonDimension, 18);

		BlockOre.addValidBody(ModBlocks.ore_lithium, SolarSystem.Body.MUN);
		BlockOre.addValidBody(ModBlocks.ore_quartz, SolarSystem.Body.MUN);
		BlockOre.addValidBody(ModBlocks.ore_shale, SolarSystem.Body.MUN);
	}

	@Override
	public void generate(Random random, int chunkX, int chunkZ, World world, IChunkProvider chunkGenerator, IChunkProvider chunkProvider) {
		if(world.provider.dimensionId == SpaceConfig.moonDimension) {
			generateMoon(world, random, chunkX * 16, chunkZ * 16);
		}
	}

	private void generateMoon(World world, Random rand, int i, int j) {
		int meta = CelestialBody.getMeta(world);

		DungeonToolbox.generateOre(world, rand, i, j, WorldConfig.nickelSpawn, 8, 1, 43, ModBlocks.ore_nickel, meta, ModBlocks.moon_rock);
		DungeonToolbox.generateOre(world, rand, i, j, WorldConfig.titaniumSpawn, 12, 4, 27, ModBlocks.ore_titanium, meta, ModBlocks.moon_rock);
		DungeonToolbox.generateOre(world, rand, i, j, WorldConfig.lithiumSpawn,  6, 4, 8, ModBlocks.ore_lithium, meta, ModBlocks.moon_rock);
		DungeonToolbox.generateOre(world, rand, i, j, WorldConfig.aluminiumSpawn,  6, 5, 40, ModBlocks.ore_aluminium, meta, ModBlocks.moon_rock);
        DungeonToolbox.generateOre(world, rand, i, j, WorldConfig.fluoriteSpawn, 4, 5, 45, ModBlocks.ore_fluorite, meta, ModBlocks.moon_rock);
        DungeonToolbox.generateOre(world, rand, i, j, 10, 13, 5, 64, ModBlocks.ore_quartz, meta, ModBlocks.moon_rock);

        DungeonToolbox.generateOre(world, rand, i, j, 1, 12, 8, 32, ModBlocks.ore_shale, meta, ModBlocks.moon_rock);
	}
}