package com.hbm.dim.Ike;

import java.util.Random;

import com.hbm.blocks.ModBlocks;
import com.hbm.blocks.generic.BlockOre;
import com.hbm.config.SpaceConfig;
import com.hbm.config.WorldConfig;
import com.hbm.dim.CelestialBody;
import com.hbm.dim.SolarSystem;
import com.hbm.dim.WorldProviderCelestial;
import com.hbm.main.StructureManager;
import com.hbm.world.dungeon.AncientTomb;
import com.hbm.world.gen.nbt.NBTStructure;
import com.hbm.world.gen.nbt.JigsawPiece;
import com.hbm.world.gen.nbt.SpawnCondition;
import com.hbm.world.generator.DungeonToolbox;

import cpw.mods.fml.common.IWorldGenerator;
import net.minecraft.block.Block;
import net.minecraft.world.World;
import net.minecraft.world.chunk.IChunkProvider;

public class WorldGeneratorIke implements IWorldGenerator {

	public WorldGeneratorIke() {
		NBTStructure.registerStructure(SpaceConfig.ikeDimension, new SpawnCondition("ike_artifact") {{
			structure = new JigsawPiece("ike_artifact", StructureManager.ike_artifact, -5);
			spawnWeight = 4;
		}});

		NBTStructure.registerNullWeight(SpaceConfig.ikeDimension, 12);

		BlockOre.addValidBody(ModBlocks.ore_mineral, SolarSystem.Body.IKE);
		BlockOre.addValidBody(ModBlocks.ore_lithium, SolarSystem.Body.IKE);
		BlockOre.addValidBody(ModBlocks.ore_coltan, SolarSystem.Body.IKE);
		BlockOre.addValidBody(ModBlocks.ore_asbestos, SolarSystem.Body.IKE);

		BlockOre.addValidBody(ModBlocks.ore_brine, SolarSystem.Body.IKE);
	}

	@Override
	public void generate(Random random, int chunkX, int chunkZ, World world, IChunkProvider chunkGenerator, IChunkProvider chunkProvider) {
		if(world.provider.dimensionId == SpaceConfig.ikeDimension) {
			generateIke(world, random, chunkX * 16, chunkZ * 16);
		}
	}

	private void generateIke(World world, Random rand, int i, int j) {
		int meta = CelestialBody.getMeta(world);
		Block stone = ((WorldProviderCelestial) world.provider).getStone();

		DungeonToolbox.generateOre(world, rand, i, j, WorldConfig.asbestosSpawn, 8, 3, 22, ModBlocks.ore_asbestos, meta, stone);
		DungeonToolbox.generateOre(world, rand, i, j, WorldConfig.copperSpawn, 9, 4, 27, ModBlocks.ore_copper, meta, stone);
		DungeonToolbox.generateOre(world, rand, i, j, WorldConfig.ironSpawn,  8, 1, 33, ModBlocks.ore_iron, meta, stone);
		DungeonToolbox.generateOre(world, rand, i, j, WorldConfig.lithiumSpawn,  6, 4, 8, ModBlocks.ore_lithium, meta, stone);
		DungeonToolbox.generateOre(world, rand, i, j, 2, 4, 15, 40, ModBlocks.ore_coltan, meta, stone);

		//okay okay okay, lets say on duna you DO make solvent, this is now awesome because you can now make gallium arsenide to then head to
		//dres and the likes :)


		DungeonToolbox.generateOre(world, rand, i, j, WorldConfig.mineralSpawn, 10, 12, 32, ModBlocks.ore_mineral, meta, stone);


		if(WorldConfig.pyramidStructure > 0 && rand.nextInt(WorldConfig.pyramidStructure) == 0) {
			int x = i + rand.nextInt(16);
			int z = j + rand.nextInt(16);
			int y = world.getHeightValue(x, z);

			new AncientTomb().build(world, rand, x, y, z);
		}
	}

}