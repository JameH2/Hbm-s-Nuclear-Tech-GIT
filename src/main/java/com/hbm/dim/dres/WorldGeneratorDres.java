package com.hbm.dim.dres;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import com.hbm.blocks.ModBlocks;
import com.hbm.config.GeneralConfig;
import com.hbm.config.SpaceConfig;
import com.hbm.config.WorldConfig;
import com.hbm.dim.CelestialBody;
import com.hbm.dim.dres.biome.BiomeGenBaseDres;
import com.hbm.main.StructureManager;
import com.hbm.world.gen.NBTStructure;
import com.hbm.world.gen.NBTStructure.JigsawPiece;
import com.hbm.world.gen.NBTStructure.JigsawPool;
import com.hbm.world.gen.NBTStructure.SpawnCondition;
import com.hbm.world.gen.component.Component.LabTiles;
import com.hbm.world.generator.DungeonToolbox;

import cpw.mods.fml.common.IWorldGenerator;
import net.minecraft.block.Block;
import net.minecraft.world.World;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.structure.StructureComponent.BlockSelector;

public class WorldGeneratorDres implements IWorldGenerator {

	public WorldGeneratorDres() {
        Map<Block, BlockSelector> tiles = new HashMap<Block, BlockSelector>() {{
            put(ModBlocks.tile_lab, new LabTiles(0.2F));
        }};

		NBTStructure.registerStructure(SpaceConfig.dresDimension, new SpawnCondition() {{
			spawnWeight = 4;
			minHeight = 40;
			maxHeight = 40;
			sizeLimit = 128;
			rangeLimit = 64;
			canSpawn = biome -> biome == BiomeGenBaseDres.dresPlains;
			startPool = "start";
			pools = new HashMap<String, JigsawPool>() {{
				put("start", new JigsawPool() {{
					add(new JigsawPiece("dres_core", StructureManager.dres_core) {{ blockTable = tiles; }}, 1);
				}});
				put("default", new JigsawPool() {{
					add(new JigsawPiece("dres_t", StructureManager.dres_t) {{ blockTable = tiles; }}, 1);
					add(new JigsawPiece("dres_airlock", StructureManager.dres_airlock) {{ blockTable = tiles; }}, 1);
					add(new JigsawPiece("dres_dome", StructureManager.dres_dome) {{ blockTable = tiles; }}, 1);
					add(new JigsawPiece("dres_pool", StructureManager.dres_pool) {{ blockTable = tiles; }}, 1);
					fallback = "inback";
				}});
				put("outside", new JigsawPool() {{
					add(new JigsawPiece("dres_balcony", StructureManager.dres_balcony) {{ blockTable = tiles; }}, 1);
					add(new JigsawPiece("dres_pad", StructureManager.dres_pad) {{ blockTable = tiles; }}, 1);
					fallback = "outback";
				}});
				put("reactor", new JigsawPool() {{
					add(new JigsawPiece("dres_hall_starbmk", StructureManager.dres_hall_starbmk) {{ blockTable = tiles; }}, 5);
					add(new JigsawPiece("dres_hall_breeder", StructureManager.dres_hall_breeder) {{ blockTable = tiles; }}, 1);
				}});
				put("inback", new JigsawPool() {{
					add(new JigsawPiece("dres_incap", StructureManager.dres_incap) {{ blockTable = tiles; }}, 1);
				}});
				put("outback", new JigsawPool() {{
					add(new JigsawPiece("dres_outcap", StructureManager.dres_outcap) {{ blockTable = tiles; }}, 1);
				}});
			}};
		}});

		NBTStructure.registerNullWeight(SpaceConfig.dresDimension, 20);
	}

	@Override
	public void generate(Random random, int chunkX, int chunkZ, World world, IChunkProvider chunkGenerator, IChunkProvider chunkProvider) {
		if(world.provider.dimensionId == SpaceConfig.dresDimension) {
			generateDres(world, random, chunkX * 16, chunkZ * 16);
		}
	}

	private void generateDres(World world, Random rand, int i, int j) {
		int meta = CelestialBody.getMeta(world);

		DungeonToolbox.generateOre(world, rand, i, j, WorldConfig.cobaltSpawn, 4, 3, 22, ModBlocks.ore_cobalt, meta, ModBlocks.dres_rock);
		DungeonToolbox.generateOre(world, rand, i, j, WorldConfig.copperSpawn, 9, 4, 27, ModBlocks.ore_iron, meta, ModBlocks.dres_rock);
		DungeonToolbox.generateOre(world, rand, i, j, 12,  8, 1, 33, ModBlocks.ore_niobium, meta, ModBlocks.dres_rock);
		DungeonToolbox.generateOre(world, rand, i, j, GeneralConfig.coltanRate, 4, 15, 40, ModBlocks.ore_coltan, meta, ModBlocks.dres_rock);
		DungeonToolbox.generateOre(world, rand, i, j, 1, 6, 4, 64, ModBlocks.ore_lanthanium, meta, ModBlocks.dres_rock);

        DungeonToolbox.generateOre(world, rand, i, j, 1, 12, 8, 32, ModBlocks.ore_shale, meta, ModBlocks.dres_rock);
	}
}