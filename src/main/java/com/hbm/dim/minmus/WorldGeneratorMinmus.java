package com.hbm.dim.minmus;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import com.hbm.blocks.ModBlocks;
import com.hbm.blocks.generic.BlockOre;
import com.hbm.blocks.BlockEnums.EnumStoneType;
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
import net.minecraft.world.World;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.structure.StructureComponent.BlockSelector;

public class WorldGeneratorMinmus implements IWorldGenerator {

	public WorldGeneratorMinmus() {
		Map<Block, BlockSelector> concrete = new HashMap<Block, BlockSelector>() {{
			put(ModBlocks.concrete_colored, new DesertConcrete());
		}};

		JigsawPiece minmusBase = new JigsawPiece("minmus_base", StructureManager.mun_base) {{ alignToTerrain = true; heightOffset = -1; blockTable = concrete; }};

		NBTStructure.registerStructure(SpaceConfig.minmusDimension, new SpawnCondition("minmus_base") {{
			spawnWeight = 6;
			sizeLimit = 32;
			startPool = "start";
			pools = new HashMap<String, JigsawPool>() {{
				put("start", new JigsawPool() {{
					add(minmusBase, 1);
				}});
				put("default", new JigsawPool() {{
					add(minmusBase, 1);
					add(new JigsawPiece("minmus_flag", StructureManager.mun_flag_uk) {{ alignToTerrain = true; heightOffset = -1; }}, 2);
					add(new JigsawPiece("minmus_panels", StructureManager.mun_panels) {{ alignToTerrain = true; heightOffset = -1; }}, 6);
					add(new JigsawPiece("minmus_stardar", StructureManager.mun_stardar) {{ alignToTerrain = true; heightOffset = -1; }}, 1);
					add(new JigsawPiece("minmus_tanks", StructureManager.mun_tanks) {{ alignToTerrain = true; heightOffset = -1; }}, 6);
				}});
				put("connect", new JigsawPool() {{
					add(new JigsawPiece("minmus_connector_1", StructureManager.mun_connector_1), 1);
					add(new JigsawPiece("minmus_connector_2", StructureManager.mun_connector_2), 1);
					add(new JigsawPiece("minmus_connector_3", StructureManager.mun_connector_3), 1);
					add(new JigsawPiece("minmus_connector_s", StructureManager.mun_connector_s), 1);
					add(new JigsawPiece("minmus_connector_l", StructureManager.mun_connector_l), 1);
					add(new JigsawPiece("minmus_connector_t", StructureManager.mun_connector_t), 1);
				}});
			}};
		}});

		NBTStructure.registerNullWeight(SpaceConfig.minmusDimension, 18);

		BlockOre.addValidBody(ModBlocks.ore_brine, SolarSystem.Body.MINMUS);
	}

	@Override
	public void generate(Random random, int chunkX, int chunkZ, World world, IChunkProvider chunkGenerator, IChunkProvider chunkProvider) {
		if(world.provider.dimensionId == SpaceConfig.minmusDimension) {
			generateMinmus(world, random, chunkX * 16, chunkZ * 16);
		}
	}

	private void generateMinmus(World world, Random rand, int i, int j) {
		int meta = CelestialBody.getMeta(world);
		Block stone = ((WorldProviderCelestial) world.provider).getStone();

		DungeonToolbox.generateOre(world, rand, i, j, WorldConfig.nickelSpawn, 8, 1, 43, ModBlocks.ore_nickel, meta, stone);
		DungeonToolbox.generateOre(world, rand, i, j, WorldConfig.titaniumSpawn, 12, 4, 27, ModBlocks.ore_titanium, meta, stone);
		DungeonToolbox.generateOre(world, rand, i, j, 1, 16, 6, 40, ModBlocks.stone_resource, EnumStoneType.MALACHITE.ordinal(), stone);
		DungeonToolbox.generateOre(world, rand, i, j, WorldConfig.copperSpawn * 3, 12, 8, 56, ModBlocks.ore_copper, meta, stone);
	}



	private static class DesertConcrete extends BlockSelector {

		@Override
		public void selectBlocks(Random rand, int posX, int posY, int posZ, boolean notInterior) {
			this.field_151562_a = ModBlocks.concrete_colored_ext;
			this.selectedBlockMetaData = 6;
		}

	}

}