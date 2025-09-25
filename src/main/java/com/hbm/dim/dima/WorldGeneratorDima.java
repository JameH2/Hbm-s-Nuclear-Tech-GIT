package com.hbm.dim.dima;

import java.util.Random;

import com.hbm.blocks.ModBlocks;
import com.hbm.config.SpaceConfig;
import com.hbm.config.WorldConfig;
import com.hbm.dim.CelestialBody;
import com.hbm.dim.WorldProviderCelestial;
import com.hbm.world.generator.DungeonToolbox;

import cpw.mods.fml.common.IWorldGenerator;
import net.minecraft.block.Block;
import net.minecraft.world.World;
import net.minecraft.world.chunk.IChunkProvider;

public class WorldGeneratorDima implements IWorldGenerator {

	@Override
	public void generate(Random random, int chunkX, int chunkZ, World world, IChunkProvider chunkGenerator, IChunkProvider chunkProvider) {
		if(world.provider.dimensionId == SpaceConfig.dimaDimension) {
			generateDima(world, random, chunkX * 16, chunkZ * 16);
		}
	}

	private void generateDima(World world, Random rand, int i, int j) {
		int meta = CelestialBody.getMeta(world);
		Block stone = ((WorldProviderCelestial) world.provider).getStone();

		DungeonToolbox.generateOre(world, rand, i, j, WorldConfig.mineralSpawn, 10, 12, 32, ModBlocks.ore_mineral, meta, stone);
	}

}
