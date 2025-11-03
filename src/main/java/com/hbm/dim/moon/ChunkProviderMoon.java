package com.hbm.dim.moon;

import com.hbm.blocks.ModBlocks;
import com.hbm.config.WorldConfig;
import com.hbm.dim.CelestialBody;
import com.hbm.dim.ChunkProviderCelestial;
import com.hbm.dim.mapgen.MapGenCrater;
import com.hbm.dim.mapgen.MapGenGreg;
import com.hbm.dim.mapgen.MapgenRavineButBased;
import com.hbm.world.gen.terrain.MapGenBubble;

import net.minecraft.world.World;

public class ChunkProviderMoon extends ChunkProviderCelestial {

	private MapGenGreg caveGenV3 = new MapGenGreg();
	private MapgenRavineButBased rgen = new MapgenRavineButBased();

	private MapGenCrater smallCrater = new MapGenCrater(6);
	private MapGenCrater largeCrater = new MapGenCrater(64);

	private MapGenBubble brine = new MapGenBubble(WorldConfig.munBrineSpawn);

	public ChunkProviderMoon(World world, long seed, boolean hasMapFeatures) {
		super(world, seed, hasMapFeatures);
		caveGenV3.stoneBlock = ModBlocks.moon_rock;
		rgen.stoneBlock = ModBlocks.moon_rock;

		smallCrater.setSize(8, 32);
		largeCrater.setSize(96, 128);

		smallCrater.regolith = largeCrater.regolith = ModBlocks.basalt;
		smallCrater.rock = largeCrater.rock = ModBlocks.moon_rock;

		brine.block = ModBlocks.ore_brine;
		brine.meta = (byte)CelestialBody.getMeta(world);
		brine.replace = ModBlocks.moon_rock;
		brine.setSize(8, 16);

		stoneBlock = ModBlocks.moon_rock;
		seaBlock = ModBlocks.basalt;
		seaLevel = 64;
	}

	@Override
	public BlockMetaBuffer getChunkPrimer(int x, int z) {
		BlockMetaBuffer buffer = super.getChunkPrimer(x, z);
		brine.setMetas(buffer.metas);

		// NEW CAVES
		caveGenV3.func_151539_a(this, worldObj, x, z, buffer.blocks);
		rgen.func_151539_a(this, worldObj, x, z, buffer.blocks);
		smallCrater.func_151539_a(this, worldObj, x, z, buffer.blocks);
		largeCrater.func_151539_a(this, worldObj, x, z, buffer.blocks);
		brine.func_151539_a(this, worldObj, x, z, buffer.blocks);

		return buffer;
	}

}
