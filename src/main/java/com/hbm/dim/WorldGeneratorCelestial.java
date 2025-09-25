package com.hbm.dim;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import com.hbm.blocks.BlockEnums.EnumStoneType;
import com.hbm.blocks.generic.BlockOre;
import com.hbm.blocks.ModBlocks;
import com.hbm.config.WorldConfig;
import com.hbm.inventory.FluidStack;
import com.hbm.items.ModItems;
import com.hbm.main.StructureManager;
import com.hbm.util.WeightedRandomGeneric;
import com.hbm.world.PlanetGen;
import com.hbm.world.feature.BedrockOre;
import com.hbm.world.feature.BedrockOre.BedrockOreDefinition;
import com.hbm.world.feature.DepthDeposit;
import com.hbm.world.gen.nbt.NBTStructure;
import com.hbm.world.gen.nbt.JigsawPiece;
import com.hbm.world.gen.nbt.JigsawPool;
import com.hbm.world.gen.nbt.SpawnCondition;
import com.hbm.world.gen.component.Component.CrabSpawners;
import com.hbm.world.gen.component.Component.GreenOoze;
import com.hbm.world.gen.component.Component.MeteorBricks;
import com.hbm.world.gen.component.Component.SupplyCrates;
import com.hbm.world.generator.DungeonToolbox;

import cpw.mods.fml.common.IWorldGenerator;
import cpw.mods.fml.common.eventhandler.Event.Result;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.WeightedRandom;
import net.minecraft.world.World;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.feature.WorldGenMinable;
import net.minecraft.world.gen.structure.StructureComponent.BlockSelector;
import net.minecraftforge.event.terraingen.OreGenEvent.GenerateMinable;

public class WorldGeneratorCelestial implements IWorldGenerator {

	// Register system-wide structures
	public WorldGeneratorCelestial() {
		Map<Block, BlockSelector> bricks = new HashMap<Block, BlockSelector>() {{
			put(ModBlocks.meteor_brick, new MeteorBricks());
		}};
		Map<Block, BlockSelector> crates = new HashMap<Block, BlockSelector>() {{
			put(ModBlocks.meteor_brick, new MeteorBricks());
			put(ModBlocks.crate, new SupplyCrates());
			put(ModBlocks.meteor_spawner, new CrabSpawners());
		}};
		Map<Block, BlockSelector> ooze = new HashMap<Block, BlockSelector>() {{
			put(ModBlocks.meteor_brick, new MeteorBricks());
			put(ModBlocks.concrete_colored, new GreenOoze());
		}};

		NBTStructure.registerStructure(new SpawnCondition("meteor_dungeon") {{
			minHeight = 32;
			maxHeight = 32;
			sizeLimit = 128;
			canSpawn = biome -> biome.rootHeight >= 0;
			startPool = "start";
			pools = new HashMap<String, JigsawPool>() {{
				put("start", new JigsawPool() {{
					add(new JigsawPiece("meteor_core", StructureManager.meteor_core) {{ blockTable = bricks; }}, 1);
				}});
				put("spike", new JigsawPool() {{
					add(new JigsawPiece("meteor_spike", StructureManager.meteor_spike) {{ heightOffset = -3; conformToTerrain = true; }}, 1);
				}});
				put("default", new JigsawPool() {{
					add(new JigsawPiece("meteor_corner", StructureManager.meteor_corner) {{ blockTable = bricks; }}, 2);
					add(new JigsawPiece("meteor_t", StructureManager.meteor_t) {{ blockTable = bricks; }}, 3);
					add(new JigsawPiece("meteor_stairs", StructureManager.meteor_stairs) {{ blockTable = bricks; }}, 1);
					add(new JigsawPiece("meteor_room_base_thru", StructureManager.meteor_room_base_thru) {{ blockTable = bricks; }}, 3);
					add(new JigsawPiece("meteor_room_base_end", StructureManager.meteor_room_base_end) {{ blockTable = bricks; }}, 4);
					fallback = "fallback";
				}});
				put("10room", new JigsawPool() {{
					add(new JigsawPiece("meteor_room_basic", StructureManager.meteor_room_basic) {{ blockTable = bricks; }}, 1);
					add(new JigsawPiece("meteor_room_balcony", StructureManager.meteor_room_balcony) {{ blockTable = bricks; }}, 1);
					add(new JigsawPiece("meteor_room_dragon", StructureManager.meteor_room_dragon) {{ blockTable = bricks; }}, 1);
					add(new JigsawPiece("meteor_room_ladder", StructureManager.meteor_room_ladder) {{ blockTable = bricks; }}, 1);
					add(new JigsawPiece("meteor_room_ooze", StructureManager.meteor_room_ooze) {{ blockTable = ooze; }}, 1);
					add(new JigsawPiece("meteor_room_split", StructureManager.meteor_room_split) {{ blockTable = bricks; }}, 1);
					add(new JigsawPiece("meteor_room_stairs", StructureManager.meteor_room_stairs) {{ blockTable = bricks; }}, 1);
					add(new JigsawPiece("meteor_room_triple", StructureManager.meteor_room_triple) {{ blockTable = bricks; }}, 1);
					fallback = "roomback";
				}});
				put("3x3loot", new JigsawPool() {{
					add(new JigsawPiece("meteor_3_bale", StructureManager.meteor_3_bale), 1);
					add(new JigsawPiece("meteor_3_blank", StructureManager.meteor_3_blank), 1);
					add(new JigsawPiece("meteor_3_block", StructureManager.meteor_3_block), 1);
					add(new JigsawPiece("meteor_3_crab", StructureManager.meteor_3_crab), 1);
					add(new JigsawPiece("meteor_3_crab_tesla", StructureManager.meteor_3_crab_tesla), 1);
					add(new JigsawPiece("meteor_3_crate", StructureManager.meteor_3_crate), 1);
					add(new JigsawPiece("meteor_3_dirt", StructureManager.meteor_3_dirt), 1);
					add(new JigsawPiece("meteor_3_lead", StructureManager.meteor_3_lead), 1);
					add(new JigsawPiece("meteor_3_ooze", StructureManager.meteor_3_ooze), 1);
					add(new JigsawPiece("meteor_3_pillar", StructureManager.meteor_3_pillar), 1);
					add(new JigsawPiece("meteor_3_star", StructureManager.meteor_3_star), 1);
					add(new JigsawPiece("meteor_3_tesla", StructureManager.meteor_3_tesla), 1);
					add(new JigsawPiece("meteor_3_book", StructureManager.meteor_3_book), 1);
					add(new JigsawPiece("meteor_3_mku", StructureManager.meteor_3_mku), 1);
					add(new JigsawPiece("meteor_3_statue", StructureManager.meteor_3_statue), 1);
					add(new JigsawPiece("meteor_3_glow", StructureManager.meteor_3_glow), 1);
					fallback = "3x3loot"; // generate loot even if we're at the size limit
				}});
				put("headloot", new JigsawPool() {{
					add(new JigsawPiece("meteor_dragon_chest", StructureManager.meteor_dragon_chest) {{ blockTable = crates; }}, 1);
					add(new JigsawPiece("meteor_dragon_tesla", StructureManager.meteor_dragon_tesla) {{ blockTable = crates; }}, 1);
					add(new JigsawPiece("meteor_dragon_trap", StructureManager.meteor_dragon_trap) {{ blockTable = crates; }}, 1);
					add(new JigsawPiece("meteor_dragon_crate_crab", StructureManager.meteor_dragon_crate_crab) {{ blockTable = crates; }}, 1);
					fallback = "headback";
				}});
				put("fallback", new JigsawPool() {{
					add(new JigsawPiece("meteor_fallback", StructureManager.meteor_fallback) {{ blockTable = bricks; }}, 1);
				}});
				put("roomback", new JigsawPool() {{
					add(new JigsawPiece("meteor_room_fallback", StructureManager.meteor_room_fallback) {{ blockTable = bricks; }}, 1);
				}});
				put("headback", new JigsawPool() {{
					add(new JigsawPiece("meteor_loot_fallback", StructureManager.meteor_dragon_fallback) {{ blockTable = crates; }}, 1);
				}});
			}};
		}}, PlanetGen.getSpaceDimensions());

		// Specify which ores spawn where
		BlockOre.addAllBodies(ModBlocks.ore_iron);
		BlockOre.addAllBodies(ModBlocks.ore_gold);
		BlockOre.addAllBodies(ModBlocks.ore_diamond);
		BlockOre.addAllBodies(ModBlocks.ore_redstone);

		BlockOre.addValidBody(ModBlocks.ore_emerald, SolarSystem.Body.KERBIN);
		BlockOre.addValidBody(ModBlocks.ore_lapis, SolarSystem.Body.KERBIN);

		BlockOre.addAllBodies(ModBlocks.ore_uranium);
		BlockOre.addAllBodies(ModBlocks.ore_thorium);
		BlockOre.addAllBodies(ModBlocks.ore_titanium);
		BlockOre.addAllBodies(ModBlocks.ore_sulfur);
		BlockOre.addAllBodies(ModBlocks.ore_aluminium);
		BlockOre.addAllBodies(ModBlocks.ore_copper);
		BlockOre.addAllBodies(ModBlocks.ore_zinc);
		BlockOre.addAllBodies(ModBlocks.ore_fluorite);
		BlockOre.addAllBodies(ModBlocks.ore_niter);
		BlockOre.addAllBodies(ModBlocks.ore_tungsten);
		BlockOre.addAllBodies(ModBlocks.ore_lead);
		BlockOre.addAllBodies(ModBlocks.ore_beryllium);
		BlockOre.addAllBodies(ModBlocks.ore_rare);
		BlockOre.addAllBodies(ModBlocks.ore_cinnebar);
		BlockOre.addAllBodies(ModBlocks.ore_cobalt);
		BlockOre.addAllBodies(ModBlocks.cluster_iron);
		BlockOre.addAllBodies(ModBlocks.cluster_titanium);
		BlockOre.addAllBodies(ModBlocks.cluster_aluminium);
		BlockOre.addAllBodies(ModBlocks.cluster_copper);

		BlockOre.addValidBody(ModBlocks.ore_asbestos, SolarSystem.Body.KERBIN);
		BlockOre.addValidBody(ModBlocks.ore_lignite, SolarSystem.Body.KERBIN);
		BlockOre.addValidBody(ModBlocks.ore_oil, SolarSystem.Body.KERBIN);
		BlockOre.addValidBody(ModBlocks.ore_bedrock_oil, SolarSystem.Body.KERBIN);
		BlockOre.addValidBody(ModBlocks.ore_coltan, SolarSystem.Body.KERBIN);

		BlockOre.addAllExcept(ModBlocks.ore_nickel, SolarSystem.Body.KERBIN);
	}

	@Override
	public void generate(Random rand, int chunkX, int chunkZ, World world, IChunkProvider chunkGenerator, IChunkProvider chunkProvider) {
		if(!(world.provider instanceof WorldProviderCelestial))
			return;

		if(world.provider.dimensionId == 0)
			return;

		WorldProviderCelestial celestialProvider = (WorldProviderCelestial)world.provider;
		Block blockToReplace = celestialProvider.getStone();
		FluidStack drillAcid = celestialProvider.getBedrockAcid();
		boolean hasIce = celestialProvider.hasIce();
		int meta = CelestialBody.getMeta(world);

		generateStructures(world, rand, chunkX * 16, chunkZ * 16);

		// Generate vanilla ores too
		if(blockToReplace != Blocks.stone) {
			generateVanillaOres(world, rand, chunkX * 16, chunkZ * 16, blockToReplace, meta);
		}

		generateNTMOres(world, rand, chunkX * 16, chunkZ * 16, blockToReplace, meta);
		generateBedrockOres(world, rand, chunkX * 16, chunkZ * 16, blockToReplace, hasIce, drillAcid);
	}

	public void generateStructures(World world, Random rand, int x, int z) {

	}

	public void generateNTMOres(World world, Random rand, int x, int z, Block planetStone, int meta) {

		if(WorldConfig.alexandriteSpawn > 0 && rand.nextInt(WorldConfig.alexandriteSpawn) == 0) {
			DungeonToolbox.generateOre(world, rand, x, z, 1, 3, 10, 5, ModBlocks.ore_alexandrite, 0, planetStone);
		}

		DepthDeposit.generateCondition(world, x, 0, 3, z, 5, 0.6D, ModBlocks.cluster_depth_iron, rand, 24, planetStone, ModBlocks.stone_depth);
		DepthDeposit.generateCondition(world, x, 0, 3, z, 5, 0.6D, ModBlocks.cluster_depth_titanium, rand, 32, planetStone, ModBlocks.stone_depth);
		DepthDeposit.generateCondition(world, x, 0, 3, z, 5, 0.6D, ModBlocks.cluster_depth_tungsten, rand, 32, planetStone, ModBlocks.stone_depth);
		DepthDeposit.generateCondition(world, x, 0, 3, z, 5, 0.8D, ModBlocks.ore_depth_cinnebar, rand, 16, planetStone, ModBlocks.stone_depth);
		DepthDeposit.generateCondition(world, x, 0, 3, z, 5, 0.8D, ModBlocks.ore_depth_zirconium, rand, 16, planetStone, ModBlocks.stone_depth);
		DepthDeposit.generateCondition(world, x, 0, 3, z, 5, 0.8D, ModBlocks.ore_depth_borax, rand, 16, planetStone, ModBlocks.stone_depth);

		DungeonToolbox.generateOre(world, rand, x, z, WorldConfig.uraniumSpawn, 5, 5, 20, ModBlocks.ore_uranium, meta, planetStone);
		DungeonToolbox.generateOre(world, rand, x, z, WorldConfig.thoriumSpawn, 5, 5, 25, ModBlocks.ore_thorium, meta, planetStone);
		DungeonToolbox.generateOre(world, rand, x, z, WorldConfig.titaniumSpawn, 6, 5, 30, ModBlocks.ore_titanium, meta, planetStone);
		DungeonToolbox.generateOre(world, rand, x, z, WorldConfig.sulfurSpawn, 8, 5, 30, ModBlocks.ore_sulfur, meta, planetStone);
		DungeonToolbox.generateOre(world, rand, x, z, WorldConfig.aluminiumSpawn, 6, 5, 40, ModBlocks.ore_aluminium, meta, planetStone);
		DungeonToolbox.generateOre(world, rand, x, z, WorldConfig.copperSpawn, 6, 5, 45, ModBlocks.ore_copper, meta, planetStone);
		DungeonToolbox.generateOre(world, rand, x, z, WorldConfig.nickelSpawn, 6, 5, 10, ModBlocks.ore_nickel, meta, planetStone);
		DungeonToolbox.generateOre(world, rand, x, z, WorldConfig.zincSpawn, 6, 5, 32, ModBlocks.ore_zinc, meta, planetStone);
		//DungeonToolbox.generateOre(world, rand, x, z, WorldConfig.mineralSpawn, 10, 12, 32, ModBlocks.ore_mineral, meta, planetStone);
		DungeonToolbox.generateOre(world, rand, x, z, WorldConfig.fluoriteSpawn, 4, 5, 45, ModBlocks.ore_fluorite, meta, planetStone);
		DungeonToolbox.generateOre(world, rand, x, z, WorldConfig.niterSpawn, 6, 5, 30, ModBlocks.ore_niter, meta, planetStone);
		DungeonToolbox.generateOre(world, rand, x, z, WorldConfig.tungstenSpawn, 8, 5, 30, ModBlocks.ore_tungsten, meta, planetStone);
		DungeonToolbox.generateOre(world, rand, x, z, WorldConfig.leadSpawn, 9, 5, 30, ModBlocks.ore_lead, meta, planetStone);
		DungeonToolbox.generateOre(world, rand, x, z, WorldConfig.berylliumSpawn, 4, 5, 30, ModBlocks.ore_beryllium, meta, planetStone);
		DungeonToolbox.generateOre(world, rand, x, z, WorldConfig.rareSpawn, 5, 5, 20, ModBlocks.ore_rare, meta, planetStone);
		// DungeonToolbox.generateOre(world, rand, x, z, WorldConfig.asbestosSpawn, 4, 16, 16, ModBlocks.ore_asbestos, meta, planetStone);
		DungeonToolbox.generateOre(world, rand, x, z, WorldConfig.cinnebarSpawn, 4, 8, 16, ModBlocks.ore_cinnebar, meta, planetStone);
		DungeonToolbox.generateOre(world, rand, x, z, WorldConfig.cobaltSpawn, 4, 4, 8, ModBlocks.ore_cobalt, meta, planetStone);

		DungeonToolbox.generateOre(world, rand, x, z, WorldConfig.ironClusterSpawn, 6, 15, 45, ModBlocks.cluster_iron, meta, planetStone);
		DungeonToolbox.generateOre(world, rand, x, z, WorldConfig.titaniumClusterSpawn, 6, 15, 30, ModBlocks.cluster_titanium, meta, planetStone);
		DungeonToolbox.generateOre(world, rand, x, z, WorldConfig.aluminiumClusterSpawn, 6, 15, 35, ModBlocks.cluster_aluminium, meta, planetStone);
		DungeonToolbox.generateOre(world, rand, x, z, WorldConfig.copperClusterSpawn, 6, 15, 20, ModBlocks.cluster_copper, meta, planetStone);

		//DungeonToolbox.generateOre(world, rand, x, z, WorldConfig.malachiteSpawn, 16, 6, 40, ModBlocks.stone_resource, EnumStoneType.MALACHITE.ordinal(), planetStone);
		DungeonToolbox.generateOre(world, rand, x, z, WorldConfig.limestoneSpawn, 12, 25, 30, ModBlocks.stone_resource, EnumStoneType.CALCIUM.ordinal(), planetStone);

		if(rand.nextInt(4) == 0) {
			int rx = x + rand.nextInt(16) + 8;
			int ry = 6 + rand.nextInt(13);
			int rz = z + rand.nextInt(16) + 8;

			if(world.getBlock(rx, ry, rz).isReplaceableOreGen(world, rx, ry, rz, planetStone)) {
				world.setBlock(rx, ry, rz, ModBlocks.stone_keyhole);
			}
		}
	}

	public void generateBedrockOres(World world, Random rand, int x, int z, Block planetStone, boolean hasIce, FluidStack drillAcid) {
		if(WorldConfig.newBedrockOres) {

			if(rand.nextInt(10) == 0) {
				int randPosX = x + rand.nextInt(2) + 8;
				int randPosZ = z + rand.nextInt(2) + 8;

				BedrockOre.generate(world, randPosX, randPosZ, new ItemStack(ModItems.bedrock_ore_base), drillAcid, 0xD78A16, 1, ModBlocks.stone_depth, planetStone);
			} else if(hasIce && rand.nextInt(3) == 0) {
				int randPosX = x + rand.nextInt(2) + 8;
				int randPosZ = z + rand.nextInt(2) + 8;

				BedrockOre.generate(world, randPosX, randPosZ, new ItemStack(Blocks.packed_ice, 8 * 4), null, 0xAAFFFF, 1);
			}

		} else {

			if(rand.nextInt(3) == 0) {
				List<WeightedRandomGeneric<BedrockOreDefinition>> list = BedrockOre.weightedOres;
				SolarSystem.Body bodyEnum = CelestialBody.getEnum(world);

				// If we haven't got any defined body bedrock ores, default to earth bedrock ores
				if(BedrockOre.weightedPlanetOres.containsKey(bodyEnum))
					list = BedrockOre.weightedPlanetOres.get(bodyEnum);

				@SuppressWarnings("unchecked")
				WeightedRandomGeneric<BedrockOreDefinition> item = (WeightedRandomGeneric<BedrockOreDefinition>) WeightedRandom.getRandomItem(rand, list);
				BedrockOreDefinition def = item.get();

				int randPosX = x + rand.nextInt(2) + 8;
				int randPosZ = z + rand.nextInt(2) + 8;
				BedrockOre.generate(world, randPosX, randPosZ, def.stack, def.acid, def.color, def.tier, ModBlocks.stone_depth, planetStone);
			}
		}
	}

	// This will generate vanilla ores for the chunk when the biome decorator fails to find any regular stone
	public void generateVanillaOres(World world, Random rand, int x, int z, Block planetStone, int meta) {
		genVanillaOre(world, rand, x, z, 0, 64, 20, 8, ModBlocks.ore_iron, planetStone, meta);
		genVanillaOre(world, rand, x, z, 0, 32, 2, 8, ModBlocks.ore_gold, planetStone, meta);
		genVanillaOre(world, rand, x, z, 0, 16, 8, 7, ModBlocks.ore_redstone, planetStone, meta);
		genVanillaOre(world, rand, x, z, 0, 16, 1, 7, ModBlocks.ore_diamond, planetStone, meta);
		// what the fuck is a lapis lazuli
		// emeralds also spawn in a special way but... like... fuck emeralds
	}

	// Called by ModEventHandler to handle vanilla ore generation events
	public static void onGenerateOre(GenerateMinable event) {
		// No coal on celestial bodies, no dead dinodoys :(
		if(event.type == GenerateMinable.EventType.COAL) {
			event.setResult(Result.DENY);
		}
	}

	// A simple reimplementation of `genStandardOre1` without needing to instance a BiomeDecorator
	private void genVanillaOre(World world, Random rand, int x, int z, int yMin, int yMax, int count, int numberOfBlocks, Block ore, Block target, int meta) {
		WorldGenMinable worldGenMinable = new WorldGenMinable(ore, meta, numberOfBlocks, target);

		for (int l = 0; l < count; ++l) {
			int genX = x + rand.nextInt(16);
			int genY = rand.nextInt(yMax - yMin) + yMin; // millenial supremacy
			int genZ = z + rand.nextInt(16);
			worldGenMinable.generate(world, rand, genX, genY, genZ);
		}
	}

}

