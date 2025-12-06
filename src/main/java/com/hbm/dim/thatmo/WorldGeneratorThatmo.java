package com.hbm.dim.thatmo;

import java.util.HashMap;
import java.util.Random;

import com.hbm.blocks.ModBlocks;
import com.hbm.config.SpaceConfig;
import com.hbm.main.StructureManager;
import com.hbm.world.gen.component.Component.ConcreteBricks;
import com.hbm.world.gen.nbt.JigsawPiece;
import com.hbm.world.gen.nbt.JigsawPool;
import com.hbm.world.gen.nbt.NBTStructure;
import com.hbm.world.gen.nbt.SpawnCondition;

import cpw.mods.fml.common.IWorldGenerator;
import net.minecraft.block.Block;
import net.minecraft.world.World;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.structure.StructureComponent.BlockSelector;

public class WorldGeneratorThatmo implements IWorldGenerator {

	public WorldGeneratorThatmo() {
		NBTStructure.registerStructure(SpaceConfig.thatmoDimension, new SpawnCondition("thatmotest") {{
			structure = new JigsawPiece("thatmotest", StructureManager.THATMOTESTMO, -1);
			canSpawn = biome -> biome.heightVariation < 0.1F;
		}});
		NBTStructure.registerStructure(SpaceConfig.thatmoDimension, new SpawnCondition("thatmo2") {{
			structure = new JigsawPiece("thatmotest2", StructureManager.thatmo2, -1);
			canSpawn = biome -> biome.heightVariation < 0.1F;
		}});
		NBTStructure.registerStructure(SpaceConfig.thatmoDimension, new SpawnCondition("trenches") {{
			structure = new JigsawPiece("trenches", StructureManager.trenches, -2) {{
				conformToTerrain = true;
				blockTable = new HashMap<Block, BlockSelector>() {{
					put(ModBlocks.brick_concrete_cracked, new ConcreteBricks());
				}};
			}};
			spawnWeight = 2;
			canSpawn = biome -> biome.heightVariation < 0.1F;
		}});

		NBTStructure.registerStructure(SpaceConfig.thatmoDimension, new SpawnCondition("thatmocity") {{
			sizeLimit = 256;
			canSpawn = biome -> true;
			startPool = "road";
			pools = new HashMap<String, JigsawPool>() {{
				put("default", new JigsawPool() {{
					add(new JigsawPiece("thatmocity-building-short", StructureManager.thatmocity_building_short), 1);
					add(new JigsawPiece("thatmocity-building-medium", StructureManager.thatmocity_building_medium), 1);
				}});
				put("road", new JigsawPool() {{
					add(new JigsawPiece("thatmocity-road", StructureManager.thatmocity_road, -1) {{ conformToTerrain = true; }}, 1);
					add(new JigsawPiece("thatmocity-intersection", StructureManager.thatmocity_intersection, -1) {{ conformToTerrain = true; }}, 1);
					add(new JigsawPiece("thatmocity-intersection-t", StructureManager.thatmocity_intersection_t, -1) {{ conformToTerrain = true; }}, 1);
					add(new JigsawPiece("thatmocity-block1", StructureManager.thatmocity_block1, -1) {{ conformToTerrain = true; }}, 1);
					add(new JigsawPiece("thatmocity-block2", StructureManager.thatmocity_block2, -1) {{ conformToTerrain = true; }}, 1);
					add(new JigsawPiece("thatmocity-curve", StructureManager.thatmocity_curve, -1) {{ conformToTerrain = true; }}, 1);
				}});
			}};
		}});


	}

	@Override
	public void generate(Random random, int chunkX, int chunkZ, World world, IChunkProvider chunkGenerator, IChunkProvider chunkProvider) {
		if(world.provider.dimensionId == SpaceConfig.thatmoDimension) {
			generateThatmo(world, random, chunkX * 16, chunkZ * 16);
		}
	}

	private void generateThatmo(World world, Random rand, int i, int j) {

	}

}