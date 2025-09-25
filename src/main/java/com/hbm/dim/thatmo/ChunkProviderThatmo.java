package com.hbm.dim.thatmo;

import com.hbm.blocks.ModBlocks;
import com.hbm.dim.ChunkProviderCelestial;
import com.hbm.dim.mapgen.MapGenCrater;
import com.hbm.dim.mapgen.MapGenGreg;
import com.hbm.dim.mapgen.MapgenRavineButBased;

import net.minecraft.world.World;

public class ChunkProviderThatmo extends ChunkProviderCelestial {

	private MapGenGreg caveGenV3 = new MapGenGreg();
	private MapgenRavineButBased rgen = new MapgenRavineButBased();

	private MapGenCrater smallCrater = new MapGenCrater(6);


	public ChunkProviderThatmo(World world, long seed, boolean hasMapFeatures) {
		super(world, seed, hasMapFeatures);
		caveGenV3.stoneBlock = ModBlocks.sellafield_slaked;
		rgen.stoneBlock = ModBlocks.sellafield_slaked;

		smallCrater.setSize(8, 32);

		smallCrater.regolith = ModBlocks.sellafield_slaked;
		smallCrater.rock = ModBlocks.sellafield_slaked;

		stoneBlock = ModBlocks.basalt;
		seaBlock = ModBlocks.basalt;
		seaLevel = 64;
	}

	@Override
	public BlockMetaBuffer getChunkPrimer(int x, int z) {
		BlockMetaBuffer buffer = super.getChunkPrimer(x, z);
		
		// NEW CAVES
		caveGenV3.func_151539_a(this, worldObj, x, z, buffer.blocks);
		rgen.func_151539_a(this, worldObj, x, z, buffer.blocks);
		smallCrater.func_151539_a(this, worldObj, x, z, buffer.blocks);

		return buffer;
	}

}
