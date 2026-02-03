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
	    
		this.firstOrderFreq = Vec3.createVectorHelper(342.206, 342.206, 342.206);
		this.secondOrderFreq = Vec3.createVectorHelper(342.206, 342.206, 342.206);
		this.thirdOrderFreq = Vec3.createVectorHelper(17.1103, 8.55515, 17.1103);
		this.heightOrderFreq = Vec3.createVectorHelper(100.0, 100.0, 0.25);
		this.amplified = true;
		this.reclamp = false;
		
		caveGenV2.lavaBlock = ModBlocks.brine_block;
		caveGenV2.stoneBlock = ModBlocks.wal_rock;

		smallCrater.setSize(8, 32);
		largeCrater.setSize(96, 128);

		smallCrater.regolith = largeCrater.regolith = ModBlocks.wal_basalt;
		smallCrater.rock = largeCrater.rock = ModBlocks.wal_rock;
		
		volcano.setSize(8, 24);
		volcano.setMaterial(ModBlocks.geysir_water, ModBlocks.wal_basalt);

		stoneBlock = ModBlocks.wal_rock;
		seaBlock = ModBlocks.wal_basalt;
		seaLevel = 64;
	}

	private void generateIceLayersBelowTurf(BlockMetaBuffer buffer) {
		for (int localX = 0; localX < 16; localX++) {
			for (int localZ = 0; localZ < 16; localZ++) {
				for (int y = 1; y < 256; y++) {
					int index = (localX * 16 + localZ) * 256 + y;
					if (buffer.blocks[index] == ModBlocks.wal_turf && y > 0) {
						buffer.blocks[(localX * 16 + localZ) * 256 + (y - 1)] = ModBlocks.ice_slush;
					}
				}
			}
		}
	}
	
	private void mixIceSlushUnderground(BlockMetaBuffer buffer) {
		for (int localX = 0; localX < 16; localX++) {
			for (int localZ = 0; localZ < 16; localZ++) {
				for (int y = 0; y < 256; y++) {
					int index = (localX * 16 + localZ) * 256 + y;
					if (buffer.blocks[index] == ModBlocks.wal_rock && worldObj.rand.nextFloat() < 0.3F) {
						buffer.blocks[index] = ModBlocks.ice_slush;
					}
				}
			}
		}
	}
	
	private void generateRandomBrineBlocks(BlockMetaBuffer buffer) {
		int brineBlocksPerChunk = 2 + worldObj.rand.nextInt(4);
		
		for (int i = 0; i < brineBlocksPerChunk; i++) {
			int x = worldObj.rand.nextInt(16);
			int z = worldObj.rand.nextInt(16);
			int y = worldObj.rand.nextInt(seaLevel - 10);
			
			int index = (x * 16 + z) * 256 + y;
			
			if (buffer.blocks[index] == ModBlocks.wal_rock || buffer.blocks[index] == ModBlocks.wal_basalt) {
				buffer.blocks[index] = ModBlocks.brine_block;
			}
		}
	}

	@Override
	public BlockMetaBuffer getChunkPrimer(int x, int z) {
		BlockMetaBuffer buffer = super.getChunkPrimer(x, z);
		
		caveGenV2.func_151539_a(this, worldObj, x, z, buffer.blocks);
		smallCrater.func_151539_a(this, worldObj, x, z, buffer.blocks);
		largeCrater.func_151539_a(this, worldObj, x, z, buffer.blocks);
		volcano.func_151539_a(this, worldObj, x, z, buffer.blocks);

		generateIceLayersBelowTurf(buffer);
		
		mixIceSlushUnderground(buffer);
		
		generateRandomBrineBlocks(buffer);

		return buffer;
	}

}
