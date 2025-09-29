package com.hbm.dim.dima;

import com.hbm.blocks.ModBlocks;
import com.hbm.dim.ChunkProviderCelestial;
import com.hbm.dim.mapgen.MapGenTiltedSpires;

import net.minecraft.world.World;
import net.minecraft.world.gen.MapGenBase;
import net.minecraft.world.gen.MapGenCaves;

public class ChunkProviderDima extends ChunkProviderCelestial {

	private MapGenBase caveGenerator = new MapGenCaves();
	private MapGenTiltedSpires spires = new MapGenTiltedSpires(6, 6, 0F);

	public ChunkProviderDima(World world, long seed, boolean hasMapFeatures) {
		super(world, seed, hasMapFeatures);

		spires.rock = ModBlocks.dima_regolith;
		spires.regolith = ModBlocks.dima_stone; // inverted, because it falls up
		spires.mid = 82;
		spires.curve = true;
		spires.maxTilt = 3.5F;

		stoneBlock = ModBlocks.dima_stone;
	}

	@Override
	public BlockMetaBuffer getChunkPrimer(int x, int z) {
		BlockMetaBuffer buffer = super.getChunkPrimer(x, z);

		spires.func_151539_a(this, worldObj, x, z, buffer.blocks);
		caveGenerator.func_151539_a(this, worldObj, x, z, buffer.blocks);

		return buffer;
	}

}
