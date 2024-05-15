package com.hbm.dim.plock;

import java.util.Random;

import com.hbm.blocks.ModBlocks;
import com.hbm.blocks.BlockEnums.EnumStoneType;
import com.hbm.config.GeneralConfig;
import com.hbm.config.SpaceConfig;
import com.hbm.main.MainRegistry;
import com.hbm.tileentity.bomb.TileEntityLandmine;
import com.hbm.tileentity.deco.TileEntityGeysir;
import com.hbm.world.feature.OreLayer3D;
import com.hbm.world.feature.Sellafield;
import com.hbm.world.generator.DungeonToolbox;

import cpw.mods.fml.common.IWorldGenerator;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.feature.WorldGenMinable;

public class WorldGeneratorPlock implements IWorldGenerator {

	@Override
	public void generate(Random random, int chunkX, int chunkZ, World world, IChunkProvider chunkGenerator, IChunkProvider chunkProvider) {
		if(world.provider.dimensionId == SpaceConfig.plockDimension) {
			generatePlock(world, random, chunkX * 16, chunkZ * 16);
		}
	}
	private void generatePlock(World world, Random rand, int i, int j) {
		//DungeonToolbox.generateOre(world, rand, i, j, SpaceConfig.asbestosSpawn, 4, 3, 22, ModBlocks.ike_asbestos, ModBlocks.ike_stone);
		//DungeonToolbox.generateOre(world, rand, i, j, SpaceConfig.copperSpawn, 9, 4, 27, ModBlocks.ike_copper, ModBlocks.ike_stone);
		//DungeonToolbox.generateOre(world, rand, i, j, SpaceConfig.ironClusterSpawn,  8, 1, 33, ModBlocks.ike_iron, ModBlocks.ike_stone);
		
		if (rand.nextInt(10) == 0) {
			
			int x = i + rand.nextInt(16);
			int z = j + rand.nextInt(16);
			int y = world.getHeightValue(x, z);

			if(world.getBlock(x, y - 1, z)==ModBlocks.plock_tholinated_ice) {
				world.setBlock(x, y-1, z, ModBlocks.geysir_plock);	
			}
		}
		
	}
}