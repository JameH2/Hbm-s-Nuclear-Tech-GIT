package com.hbm.world.gen;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

import com.hbm.config.StructureConfig;
import com.hbm.main.StructureManager;
import com.hbm.world.gen.component.BunkerComponents.BunkerStart;
import com.hbm.world.gen.nbt.JigsawPiece;
import com.hbm.world.gen.nbt.NBTStructure;
import com.hbm.world.gen.nbt.SpawnCondition;

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
		final List<BiomeGenBase> lighthouseBiomes = Arrays.asList(new BiomeGenBase[] { BiomeGenBase.ocean, BiomeGenBase.deepOcean, BiomeGenBase.beach, BiomeGenBase.stoneBeach, BiomeGenBase.coldBeach });
		final List<BiomeGenBase> flatbiomes = Arrays.asList(BiomeGenBase.plains,BiomeGenBase.icePlains,BiomeGenBase.desert, BiomeGenBase.forest, BiomeGenBase.taiga, BiomeGenBase.coldTaiga, BiomeGenBase.savanna, BiomeGenBase.savannaPlateau, BiomeGenBase.birchForest);


		/// SPIRE ///
		NBTStructure.registerStructure(0, new SpawnCondition("spire") {{
			canSpawn = biome -> biome.heightVariation <= 0.05F && !invalidBiomes.contains(biome);
			structure = new JigsawPiece("spire", StructureManager.spire, -1);
			spawnWeight = StructureConfig.spireSpawnWeight;
		}});

		NBTStructure.registerStructure(0, new SpawnCondition("features") {{
			canSpawn = biome -> !invalidBiomes.contains(biome);
			start = d -> new MapGenNTMFeatures.Start(d.getW(), d.getX(), d.getY(), d.getZ());
			spawnWeight = StructureConfig.featuresSpawnWeight;
		}});

		NBTStructure.registerStructure(0, new SpawnCondition("bunker") {{
			canSpawn = biome -> !invalidBiomes.contains(biome);
			start = d -> new BunkerStart(d.getW(), d.getX(), d.getY(), d.getZ());
			spawnWeight = StructureConfig.bunkerSpawnWeight;
		}});

		NBTStructure.registerStructure(0, new SpawnCondition("vertibird") {{
			canSpawn = biome -> !biome.canSpawnLightningBolt() && biome.temperature >= 2F;
			structure = new JigsawPiece("vertibird", StructureManager.vertibird, -3);
			spawnWeight = StructureConfig.vertibirdSpawnWeight;
		}});

		NBTStructure.registerStructure(0, new SpawnCondition("crashed_vertibird") {{
			canSpawn = biome -> !biome.canSpawnLightningBolt() && biome.temperature >= 2F;
			structure = new JigsawPiece("crashed_vertibird", StructureManager.crashed_vertibird, -10);
			spawnWeight = StructureConfig.vertibirdCrashedSpawnWeight;
		}});

		NBTStructure.registerStructure(0, new SpawnCondition("beached_patrol") {{
			canSpawn = beachBiomes::contains;
			structure = new JigsawPiece("beached_patrol", StructureManager.beached_patrol, -5);
			minHeight = 58;
			maxHeight = 67;
			spawnWeight = StructureConfig.beachedPatrolSpawnWeight;
		}});

		NBTStructure.registerStructure(0, new SpawnCondition("aircraft_carrier") {{
			canSpawn = oceanBiomes::contains;
			structure = new JigsawPiece("aircraft_carrier", StructureManager.aircraft_carrier, -6);
			maxHeight = 42;
			spawnWeight = StructureConfig.enableOceanStructures ? StructureConfig.aircraftCarrierSpawnWeight : 0;
		}});

		NBTStructure.registerStructure(0, new SpawnCondition("oil_rig") {{
			canSpawn = biome -> biome == BiomeGenBase.deepOcean;
			structure = new JigsawPiece("oil_rig", StructureManager.oil_rig, -20);
			maxHeight = 12;
			minHeight = 11;
			spawnWeight = StructureConfig.enableOceanStructures ? StructureConfig.oilRigSpawnWeight : 0;
		}});

		NBTStructure.registerStructure(0, new SpawnCondition("lighthouse") {{
			canSpawn = lighthouseBiomes::contains;
			structure = new JigsawPiece("lighthouse", StructureManager.lighthouse, -40);
			maxHeight = 29;
			minHeight = 28;
			spawnWeight = StructureConfig.enableOceanStructures ? StructureConfig.lighthouseSpawnWeight : 0;
		}});

		NBTStructure.registerStructure(0, new SpawnCondition("dish") {{
			canSpawn = biome -> biome == BiomeGenBase.plains;
			structure = new JigsawPiece("dish", StructureManager.dish, -10);
			minHeight = 53;
			maxHeight = 65;
			spawnWeight = StructureConfig.dishSpawnWeight;
		}});

		NBTStructure.registerStructure(0, new SpawnCondition("forestchem") {{
			canSpawn = biome -> biome.heightVariation <= 0.3F;
			structure = new JigsawPiece("forest_chem", StructureManager.forest_chem, -9);
			spawnWeight = StructureConfig.forestChemSpawnWeight;
		}});

		NBTStructure.registerStructure(0, new SpawnCondition("labolatory") {{
			canSpawn = flatbiomes::contains;
			structure = new JigsawPiece("laboratory", StructureManager.laboratory, -10);
			minHeight = 53;
			maxHeight = 65;
			spawnWeight = StructureConfig.laboratorySpawnWeight;
		}});

		NBTStructure.registerStructure(0, new SpawnCondition("forest_post") {{
			canSpawn = biome -> biome.heightVariation <= 0.3F;
			structure = new JigsawPiece("forest_post", StructureManager.forest_post, -10);
			spawnWeight = StructureConfig.forestPostSpawnWeight;
		}});

		NBTStructure.registerStructure(0, new SpawnCondition("radio") {{
			canSpawn = flatbiomes::contains;
			structure = new JigsawPiece("radio_house", StructureManager.radio_house, -6);
			spawnWeight = StructureConfig.radioSpawnWeight;
		}});

		NBTStructure.registerStructure(0, new SpawnCondition("factory") {{
			canSpawn = flatbiomes::contains;
			structure = new JigsawPiece("factory", StructureManager.factory, -10);
			spawnWeight = StructureConfig.factorySpawnWeight;
		}});

		NBTStructure.registerStructure(0, new SpawnCondition("crane") {{
			canSpawn = flatbiomes::contains;
			structure = new JigsawPiece("crane", StructureManager.crane, -9);
			spawnWeight = StructureConfig.craneSpawnWeight;
		}});
    
		NBTStructure.registerStructure(0, new SpawnCondition("broadcaster_tower") {{
			canSpawn = flatbiomes::contains;
			structure = new JigsawPiece("broadcaster_tower", StructureManager.broadcasting_tower, -9);
			spawnWeight = StructureConfig.broadcastingTowerSpawnWeight;
		}});

		NBTStructure.registerStructure(0, new SpawnCondition("plane1") {{
			canSpawn = biome -> biome.heightVariation <= 0.3F;
			structure = new JigsawPiece("crashed_plane_1", StructureManager.plane1, -5);
			spawnWeight = StructureConfig.plane1SpawnWeight;
		}});

		NBTStructure.registerStructure(0, new SpawnCondition("plane2") {{
			canSpawn = biome -> biome.heightVariation <= 0.3F;
			structure = new JigsawPiece("crashed_plane_2", StructureManager.plane2, -8);
			spawnWeight = StructureConfig.plane2SpawnWeight;
		}});

		NBTStructure.registerStructure(0, new SpawnCondition("desert_shack_1") {{
			canSpawn = biome -> biome == BiomeGenBase.desert;
			structure = new JigsawPiece("desert_shack_1", StructureManager.desert_shack_1, -7);
			spawnWeight = StructureConfig.desertShack1SpawnWeight;
		}});

		NBTStructure.registerStructure(0, new SpawnCondition("desert_shack_2") {{
			canSpawn = biome -> biome == BiomeGenBase.desert;
			structure = new JigsawPiece("desert_shack_2", StructureManager.desert_shack_2, -7);
			spawnWeight = StructureConfig.desertShack2SpawnWeight;
		}});

		NBTStructure.registerStructure(0, new SpawnCondition("desert_shack_3") {{
			canSpawn = biome -> biome == BiomeGenBase.desert;
			structure = new JigsawPiece("desert_shack_3", StructureManager.desert_shack_3, -5);
			spawnWeight = StructureConfig.desertShack3SpawnWeight;
		}});

		NBTStructure.registerStructure(0, new SpawnCondition("ruinA") {{
			canSpawn = biome -> !invalidBiomes.contains(biome) && biome.canSpawnLightningBolt();
			structure = new JigsawPiece("NTMRuinsA", StructureManager.ntmruinsA, -1) {{conformToTerrain = true;}};
			spawnWeight = StructureConfig.enableRuins ? StructureConfig.ruinsASpawnWeight : 0;
		}});
		NBTStructure.registerStructure(0, new SpawnCondition("ruinB") {{
			canSpawn = biome -> !invalidBiomes.contains(biome) && biome.canSpawnLightningBolt();
			structure = new JigsawPiece("NTMRuinsB", StructureManager.ntmruinsB, -1) {{conformToTerrain = true;}};
			spawnWeight = StructureConfig.enableRuins ? StructureConfig.ruinsBSpawnWeight : 0;
		}});
		NBTStructure.registerStructure(0, new SpawnCondition("ruinC") {{
			canSpawn = biome -> !invalidBiomes.contains(biome) && biome.canSpawnLightningBolt();
			structure = new JigsawPiece("NTMRuinsC", StructureManager.ntmruinsC, -1) {{conformToTerrain = true;}};
			spawnWeight = StructureConfig.enableRuins ? StructureConfig.ruinsCSpawnWeight : 0;
		}});
		NBTStructure.registerStructure(0, new SpawnCondition("ruinD") {{
			canSpawn = biome -> !invalidBiomes.contains(biome) && biome.canSpawnLightningBolt();
			structure = new JigsawPiece("NTMRuinsD", StructureManager.ntmruinsD, -1) {{conformToTerrain = true;}};
			spawnWeight = StructureConfig.enableRuins ? StructureConfig.ruinsDSpawnWeight : 0;
		}});
		NBTStructure.registerStructure(0, new SpawnCondition("ruinE") {{
			canSpawn = biome -> !invalidBiomes.contains(biome) && biome.canSpawnLightningBolt();
			structure = new JigsawPiece("NTMRuinsE", StructureManager.ntmruinsE, -1) {{conformToTerrain = true;}};
			spawnWeight = StructureConfig.enableRuins ? StructureConfig.ruinsESpawnWeight : 0;
		}});
		NBTStructure.registerStructure(0, new SpawnCondition("ruinF") {{
			canSpawn = biome -> !invalidBiomes.contains(biome) && biome.canSpawnLightningBolt();
			structure = new JigsawPiece("NTMRuinsF", StructureManager.ntmruinsF, -1) {{conformToTerrain = true;}};
			spawnWeight = StructureConfig.enableRuins ? StructureConfig.ruinsFSpawnWeight : 0;
		}});
		NBTStructure.registerStructure(0, new SpawnCondition("ruinG") {{
			canSpawn = biome -> !invalidBiomes.contains(biome) && biome.canSpawnLightningBolt();
			structure = new JigsawPiece("NTMRuinsG", StructureManager.ntmruinsG, -1) {{conformToTerrain = true;}};
			spawnWeight = StructureConfig.enableRuins ? StructureConfig.ruinsGSpawnWeight : 0;
		}});
		NBTStructure.registerStructure(0, new SpawnCondition("ruinH") {{
			canSpawn = biome -> !invalidBiomes.contains(biome) && biome.canSpawnLightningBolt();
			structure = new JigsawPiece("NTMRuinsH", StructureManager.ntmruinsH, -1) {{conformToTerrain = true;}};
			spawnWeight = StructureConfig.enableRuins ? StructureConfig.ruinsHSpawnWeight : 0;
		}});
		NBTStructure.registerStructure(0, new SpawnCondition("ruinI") {{
			canSpawn = biome -> !invalidBiomes.contains(biome) && biome.canSpawnLightningBolt();
			structure = new JigsawPiece("NTMRuinsI", StructureManager.ntmruinsI, -1) {{conformToTerrain = true;}};
			spawnWeight = StructureConfig.enableRuins ? StructureConfig.ruinsISpawnWeight : 0;
		}});
		NBTStructure.registerStructure(0, new SpawnCondition("ruinJ") {{
			canSpawn = biome -> !invalidBiomes.contains(biome) && biome.canSpawnLightningBolt();
			structure = new JigsawPiece("NTMRuinsJ", StructureManager.ntmruinsJ, -1) {{conformToTerrain = true;}};
			spawnWeight = StructureConfig.enableRuins ? StructureConfig.ruinsJSpawnWeight : 0;
		}});

		NBTStructure.registerNullWeight(0, StructureConfig.plainsNullWeight, biome -> biome == BiomeGenBase.plains);
		NBTStructure.registerNullWeight(0, StructureConfig.oceanNullWeight, oceanBiomes::contains);

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

	public SpawnCondition getStructureAt(World world, int chunkX, int chunkZ) {
		if(StructureConfig.enableStructures == 0) return null;
		if(StructureConfig.enableStructures == 2 && !world.getWorldInfo().isMapFeaturesEnabled()) return null;

		setRandomSeed(world, chunkX, chunkZ);

		return nbtGen.getStructureAt(world, chunkX, chunkZ);
	}

}
