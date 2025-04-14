package com.hbm.world.gen;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

import com.hbm.config.StructureConfig;
import com.hbm.main.StructureManager;
import com.hbm.world.gen.NBTStructure.JigsawPiece;
import com.hbm.world.gen.NBTStructure.SpawnCondition;
import com.hbm.world.gen.component.BunkerComponents.BunkerStart;

import cpw.mods.fml.common.IWorldGenerator;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraftforge.event.terraingen.InitMapGenEvent.EventType;
import net.minecraftforge.event.terraingen.PopulateChunkEvent;
import net.minecraftforge.event.terraingen.TerrainGen;
import net.minecraftforge.event.world.WorldEvent;

public class NTMWorldGenerator implements IWorldGenerator {

	public NTMWorldGenerator() {
		final List<BiomeGenBase> invalidBiomes = Arrays.asList(new BiomeGenBase[] {BiomeGenBase.ocean, BiomeGenBase.river, BiomeGenBase.frozenOcean, BiomeGenBase.frozenRiver, BiomeGenBase.deepOcean});
		final List<BiomeGenBase> oceanBiomes = Arrays.asList(new BiomeGenBase[] { BiomeGenBase.ocean, BiomeGenBase.deepOcean });
		final List<BiomeGenBase> beachBiomes = Arrays.asList(new BiomeGenBase[] { BiomeGenBase.beach, BiomeGenBase.stoneBeach, BiomeGenBase.coldBeach });


		NBTStructure.registerStructure(0, new SpawnCondition() {{
			canSpawn = biome -> !invalidBiomes.contains(biome);
			start = d -> new MapGenNTMFeatures.Start(d.getW(), d.getX(), d.getY(), d.getZ());
			spawnWeight = 14 * 4;
		}});

		NBTStructure.registerStructure(0, new SpawnCondition() {{
			canSpawn = biome -> !invalidBiomes.contains(biome);
			start = d -> new BunkerStart(d.getW(), d.getX(), d.getY(), d.getZ());
			spawnWeight = 1 * 4;
		}});

		NBTStructure.registerStructure(0, new SpawnCondition() {{
			canSpawn = biome -> !biome.canSpawnLightningBolt() && biome.temperature >= 2F;
			structure = new JigsawPiece("vertibird", StructureManager.vertibird, -3);
			spawnWeight = 3 * 4;
		}});

		NBTStructure.registerStructure(0, new SpawnCondition() {{
			canSpawn = biome -> !biome.canSpawnLightningBolt() && biome.temperature >= 2F;
			structure = new JigsawPiece("crashed_vertibird", StructureManager.crashed_vertibird, -10);
			spawnWeight = 3 * 4;
		}});

		NBTStructure.registerStructure(0, new SpawnCondition() {{
			canSpawn = oceanBiomes::contains;
			structure = new JigsawPiece("aircraft_carrier", StructureManager.aircraft_carrier, -6);
			maxHeight = 42;
			spawnWeight = 1;
		}});

		NBTStructure.registerStructure(0, new SpawnCondition() {{
			canSpawn = biome -> biome == BiomeGenBase.deepOcean;
			structure = new JigsawPiece("oil_rig", StructureManager.oil_rig, -20);
			maxHeight = 12;
			minHeight = 11;
			spawnWeight = 2;
		}});

		NBTStructure.registerStructure(0, new SpawnCondition() {{
			canSpawn = beachBiomes::contains;
			structure = new JigsawPiece("beached_patrol", StructureManager.beached_patrol, -5);
			minHeight = 58;
			maxHeight = 67;
			spawnWeight = 8;
		}});

		NBTStructure.registerNullWeight(0, 2, oceanBiomes::contains);
		NBTStructure.registerNullWeight(0, 2, beachBiomes::contains);
	}

	private NBTStructure.GenStructure nbtGen = new NBTStructure.GenStructure();

	private final Random rand = new Random(); //A central random, used to cleanly generate our stuff without affecting vanilla or modded seeds.


	/** Inits all MapGen upon the loading of a new world. Hopefully clears out structureMaps and structureData when a different world is loaded. */
	@SubscribeEvent
	public void onLoad(WorldEvent.Load event) {
		nbtGen = (NBTStructure.GenStructure) TerrainGen.getModdedMapGen(new NBTStructure.GenStructure(), EventType.CUSTOM);

		hasPopulationEvent = false;
	}


	/** Called upon the initial population of a chunk. Called in the pre-population event first; called again if pre-population didn't occur (flatland) */
	private void setRandomSeed(World world, int chunkX, int chunkZ) {
		rand.setSeed(world.getSeed() + world.provider.dimensionId);
		final long i = rand.nextLong() / 2L * 2L + 1L;
		final long j = rand.nextLong() / 2L * 2L + 1L;
		rand.setSeed((long)chunkX * i + (long)chunkZ * j ^ world.getSeed());
	}


	/*
	 * Pre-population Events / Structure Generation
	 * Used to generate structures without unnecessary intrusion by biome decoration, like trees.
	 */
	private boolean hasPopulationEvent = false; // Does the given chunkGenerator have a population event? If not (flatlands), default to using generate.

	@SubscribeEvent
	public void generateStructures(PopulateChunkEvent.Pre event) {
		hasPopulationEvent = true;

		if(StructureConfig.enableStructures == 0) return;
		if(StructureConfig.enableStructures == 2 && !event.world.getWorldInfo().isMapFeaturesEnabled()) return;

		setRandomSeed(event.world, event.chunkX, event.chunkZ); //Set random for population down the line.

		nbtGen.generateStructures(event.world, rand, event.chunkProvider, event.chunkX, event.chunkZ);
	}


	/*
	 * Post-Vanilla / Modded Generation
	 * Used to generate features that don't care about intrusions (ores, craters, caves, etc.)
	 */
	@Override
	public void generate(Random unusedRandom, int chunkX, int chunkZ, World world, IChunkProvider chunkGenerator, IChunkProvider chunkProvider) {
		if(hasPopulationEvent) return; //If we've failed to generate any structures (flatlands)

		if(StructureConfig.enableStructures == 0) return;
		if(StructureConfig.enableStructures == 2 && !world.getWorldInfo().isMapFeaturesEnabled()) return;

		setRandomSeed(world, chunkX, chunkZ); //Reset the random seed to compensate

		nbtGen.generateStructures(world, rand, chunkProvider, chunkX, chunkZ);
	}

}
