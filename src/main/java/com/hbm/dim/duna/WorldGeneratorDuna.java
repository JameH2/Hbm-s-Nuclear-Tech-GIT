package com.hbm.dim.duna;

import java.util.Random;

import com.hbm.blocks.ModBlocks;
import com.hbm.blocks.generic.BlockOre;
import com.hbm.blocks.machine.Spotlight;
import com.hbm.config.GeneralConfig;
import com.hbm.config.SpaceConfig;
import com.hbm.config.WorldConfig;
import com.hbm.dim.CelestialBody;
import com.hbm.dim.SolarSystem;
import com.hbm.dim.WorldProviderCelestial;
import com.hbm.dim.WorldTypeTeleport;
import com.hbm.handler.MultiblockHandlerXR;
import com.hbm.main.MainRegistry;
import com.hbm.main.StructureManager;
import com.hbm.tileentity.deco.TileEntityLanternBehemoth;
import com.hbm.util.LootGenerator;
import com.hbm.world.gen.nbt.NBTStructure;
import com.hbm.world.gen.nbt.JigsawPiece;
import com.hbm.world.gen.nbt.SpawnCondition;
import com.hbm.world.generator.DungeonToolbox;

import cpw.mods.fml.common.IWorldGenerator;
import net.minecraft.block.Block;
import net.minecraft.world.World;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraftforge.common.util.ForgeDirection;

public class WorldGeneratorDuna implements IWorldGenerator {

	public WorldGeneratorDuna() {
		NBTStructure.registerStructure(SpaceConfig.dunaDimension, new SpawnCondition("duna_comms") {{
			structure = new JigsawPiece("duna_comms", StructureManager.duna_comms, -1);
			canSpawn = biome -> biome.heightVariation < 0.1F;
			spawnWeight = 6;
		}});
		NBTStructure.registerNullWeight(SpaceConfig.dunaDimension, 18);

		BlockOre.addValidBody(ModBlocks.ore_oil, SolarSystem.Body.DUNA);
	}

	@Override
	public void generate(Random random, int chunkX, int chunkZ, World world, IChunkProvider chunkGenerator, IChunkProvider chunkProvider) {
		if(world.provider.dimensionId == SpaceConfig.dunaDimension) {
			generateDuna(world, random, chunkX * 16, chunkZ * 16);
		}
	}

	private void generateDuna(World world, Random rand, int i, int j) {
		int meta = CelestialBody.getMeta(world);
		Block stone = ((WorldProviderCelestial) world.provider).getStone();

		DungeonToolbox.generateOre(world, rand, i, j, WorldConfig.ironSpawn, 8, 32, 64, ModBlocks.ore_iron, meta, stone);
		DungeonToolbox.generateOre(world, rand, i, j, WorldConfig.zincSpawn, 9, 4, 27, ModBlocks.ore_zinc, meta, stone);

		// Basalt rich in minerals, but only in basaltic caves!
		DungeonToolbox.generateOre(world, rand, i, j, 12, 6, 0, 16, ModBlocks.ore_basalt, 0, ModBlocks.basalt);
		DungeonToolbox.generateOre(world, rand, i, j, 8, 8, 0, 16, ModBlocks.ore_basalt, 1, ModBlocks.basalt);
		DungeonToolbox.generateOre(world, rand, i, j, 8, 9, 0, 16, ModBlocks.ore_basalt, 2, ModBlocks.basalt);
		DungeonToolbox.generateOre(world, rand, i, j, 2, 4, 0, 16, ModBlocks.ore_basalt, 3, ModBlocks.basalt);
		DungeonToolbox.generateOre(world, rand, i, j, 6, 10, 0, 16, ModBlocks.ore_basalt, 4, ModBlocks.basalt);


		if(i == 0 && j == 0 && world.getWorldInfo().getTerrainType() == WorldTypeTeleport.martian) {
			int x = 0;
			int z = 0;
			int y = world.getHeightValue(x, z) - 1;

			Spotlight.disableOnGeneration = false;
			StructureManager.martian.build(world, x, y, z);
			Spotlight.disableOnGeneration = true;
		}

		if(rand.nextInt(1234) == 0) {
			int x = i + rand.nextInt(16);
			int z = j + rand.nextInt(16);
			int y = world.getHeightValue(x, z);

			if(world.getBlock(x, y - 1, z).canPlaceTorchOnTop(world, x, y - 1, z) && world.getBlock(x, y, z).isReplaceable(world, x, y, z)) {

				world.setBlock(x, y, z, ModBlocks.lantern_behemoth, 12, 3);
				MultiblockHandlerXR.fillSpace(world, x, y, z, new int[] {4, 0, 0, 0, 0, 0}, ModBlocks.lantern_behemoth, ForgeDirection.NORTH);

				TileEntityLanternBehemoth lantern = (TileEntityLanternBehemoth) world.getTileEntity(x, y, z);
				lantern.isBroken = true;

				if(rand.nextInt(2) == 0) {
					LootGenerator.setBlock(world, x, y, z - 2);
					LootGenerator.lootBooklet(world, x, y, z - 2);
				}

				if(GeneralConfig.enableDebugMode)
					MainRegistry.logger.info("[Debug] Successfully spawned lantern at " + x + " " + (y) + " " + z);
			}
		}
	}

}