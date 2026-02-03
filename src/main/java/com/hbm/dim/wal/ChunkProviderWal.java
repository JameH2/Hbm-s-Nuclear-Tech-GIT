package com.hbm.dim.wal;

import com.hbm.blocks.ModBlocks;
import com.hbm.config.WorldConfig;
import com.hbm.dim.CelestialBody;
import com.hbm.dim.ChunkProviderCelestial;
import com.hbm.dim.SolarSystem;
import com.hbm.dim.mapgen.MapGenCrater;
import com.hbm.dim.mapgen.MapGenVolcano;
import com.hbm.dim.mapgen.ExperimentalCaveGenerator;
import com.hbm.world.gen.terrain.MapGenBubble;

import net.minecraft.world.World;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.util.Vec3;
import net.minecraft.init.Blocks;
import net.minecraft.block.Block;
import net.minecraft.block.BlockFalling;

public class ChunkProviderWal extends ChunkProviderCelestial {

	private ExperimentalCaveGenerator caveGenV2 = new ExperimentalCaveGenerator(2, 40, 3.0F);

	private MapGenCrater smallCrater = new MapGenCrater(6);
	private MapGenCrater largeCrater = new MapGenCrater(64);
	private MapGenVolcano volcano = new MapGenVolcano(12);

	public ChunkProviderWal(World world, long seed, boolean hasMapFeatures) {
		super(world, seed, hasMapFeatures);
		
		// Configure noise for more varied terrain
		this.firstOrderFreq = Vec3.createVectorHelper(342.206, 342.206, 342.206); // Increased frequency for more variation
		this.secondOrderFreq = Vec3.createVectorHelper(342.206, 342.206, 342.206);
		this.thirdOrderFreq = Vec3.createVectorHelper(17.1103, 8.55515, 17.1103);
		this.heightOrderFreq = Vec3.createVectorHelper(100.0, 100.0, 0.25); // Increased height variation
		this.amplified = true; // Enable amplified terrain
		this.reclamp = false; // Smoother transitions

		// Configure cave generators
		caveGenV2.lavaBlock = ModBlocks.brine_block;
		caveGenV2.stoneBlock = ModBlocks.wal_rock;

		// Configure craters with more variation
		smallCrater.setSize(8, 32);
		largeCrater.setSize(96, 128);

		smallCrater.regolith = largeCrater.regolith = ModBlocks.wal_basalt;
		smallCrater.rock = largeCrater.rock = ModBlocks.wal_rock;

		// Configure volcano
		volcano.setSize(8, 24); // Slightly larger volcanoes
		volcano.setMaterial(ModBlocks.geysir_water, ModBlocks.wal_basalt);

		// Set world properties
		stoneBlock = ModBlocks.wal_rock;
		seaBlock = ModBlocks.wal_basalt;
		seaLevel = 64;
	}

	private void generateIceLayersBelowTurf(BlockMetaBuffer buffer) {
		// Loop through all blocks in the chunk
		for (int localX = 0; localX < 16; localX++) {
			for (int localZ = 0; localZ < 16; localZ++) {
				// Start from the top of the world and go down to find wal_turf blocks
				for (int y = 1; y < 256; y++) {
					int index = (localX * 16 + localZ) * 256 + y;
					if (buffer.blocks[index] == ModBlocks.wal_turf && y > 0) {
						// Place ice_slush below the wal_turf
						buffer.blocks[(localX * 16 + localZ) * 256 + (y - 1)] = ModBlocks.ice_slush;
					}
				}
			}
		}
	}
	
	private void mixIceSlushUnderground(BlockMetaBuffer buffer) {
		// Loop through all blocks in the chunk at all levels
		for (int localX = 0; localX < 16; localX++) {
			for (int localZ = 0; localZ < 16; localZ++) {
				for (int y = 0; y < 256; y++) { // All levels from bedrock to build height
					int index = (localX * 16 + localZ) * 256 + y;
					// Replace some wal_rock with ice_slush (about 30% chance)
					if (buffer.blocks[index] == ModBlocks.wal_rock && worldObj.rand.nextFloat() < 0.3F) {
						buffer.blocks[index] = ModBlocks.ice_slush;
					}
				}
			}
		}
	}
	
	private void generateRandomBrineBlocks(BlockMetaBuffer buffer) {
		// Generate random brine blocks throughout the chunk (much less common)
		int brineBlocksPerChunk = 2 + worldObj.rand.nextInt(4); // 2-5 brine blocks per chunk
		
		for (int i = 0; i < brineBlocksPerChunk; i++) {
			int x = worldObj.rand.nextInt(16);
			int z = worldObj.rand.nextInt(16);
			// Generate brine underground only (where it can potentially flow)
			int y = worldObj.rand.nextInt(seaLevel - 10); // Between Y=0 and seaLevel-10 (underground only)
			
			int index = (x * 16 + z) * 256 + y;
			
			// Only replace wal_rock or wal_basalt with brine
			if (buffer.blocks[index] == ModBlocks.wal_rock || buffer.blocks[index] == ModBlocks.wal_basalt) {
				buffer.blocks[index] = ModBlocks.brine_block;
			}
		}
	}

	@Override
	public BlockMetaBuffer getChunkPrimer(int x, int z) {
		BlockMetaBuffer buffer = super.getChunkPrimer(x, z);

		// Generate terrain features
		caveGenV2.func_151539_a(this, worldObj, x, z, buffer.blocks);
		smallCrater.func_151539_a(this, worldObj, x, z, buffer.blocks);
		largeCrater.func_151539_a(this, worldObj, x, z, buffer.blocks);
		volcano.func_151539_a(this, worldObj, x, z, buffer.blocks);

		// Add ice layers below wal_turf
		generateIceLayersBelowTurf(buffer);
		
		// Mix ice_slush with wal_rock underground
		mixIceSlushUnderground(buffer);
		
		// Generate random brine blocks throughout the chunk
		generateRandomBrineBlocks(buffer);

		return buffer;
	}

}
