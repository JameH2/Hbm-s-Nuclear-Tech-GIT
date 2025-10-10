package com.hbm.dim.dima;

import java.util.Random;

import com.hbm.blocks.ModBlocks;
import com.hbm.config.SpaceConfig;
import com.hbm.config.WorldConfig;
import com.hbm.dim.CelestialBody;
import com.hbm.dim.WorldProviderCelestial;
import com.hbm.entity.effect.EntityAnomaly;
import com.hbm.lib.RefStrings;
import com.hbm.world.gen.nbt.JigsawPiece;
import com.hbm.world.gen.nbt.NBTStructure;
import com.hbm.world.gen.nbt.SpawnCondition;
import com.hbm.world.generator.DungeonToolbox;

import cpw.mods.fml.common.IWorldGenerator;
import net.minecraft.block.Block;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraft.world.chunk.IChunkProvider;

public class WorldGeneratorDima implements IWorldGenerator {

	public WorldGeneratorDima() {
		NBTStructure.registerStructure(-99, new SpawnCondition(EnumChatFormatting.OBFUSCATED + "tower") {{
			structure = new JigsawPiece("idma", new NBTStructure(new ResourceLocation(RefStrings.MODID, "structures/invalid/idma.nbt")), -20);
		}});
	}

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

		// anomalous projections
		if(rand.nextInt(77) == 0) {
			EntityAnomaly anomaly = new EntityAnomaly(world);
			int x = i + rand.nextInt(16) + 8;
			int z = j + rand.nextInt(16) + 8;
			anomaly.posX = x;
			anomaly.posZ = z;
			anomaly.posY = world.getHeightValue(x, z) + 2;

			world.spawnEntityInWorld(anomaly);
		}

		// fire that floats on occasion
		{
			int x = i + rand.nextInt(16) + 8;
			int z = j + rand.nextInt(16) + 8;
			int y = world.getHeightValue(x, z);

			if(rand.nextInt(10) == 0) y += rand.nextInt(6);

			world.setBlock(x, y, z, ModBlocks.fire_digamma);
		}
	}

}
