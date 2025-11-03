package com.hbm.dim.Ike;

import java.util.List;

import com.hbm.blocks.ModBlocks;
import com.hbm.config.WorldConfig;
import com.hbm.dim.CelestialBody;
import com.hbm.dim.ChunkProviderCelestial;
import com.hbm.dim.mapgen.MapGenTiltedSpires;
import com.hbm.world.gen.terrain.MapGenBubble;
import com.hbm.world.gen.terrain.MapGenCrater;

import net.minecraft.entity.EnumCreatureType;
import net.minecraft.world.World;
import net.minecraft.world.gen.MapGenBase;
import net.minecraft.world.gen.MapGenCaves;

public class ChunkProviderIke extends ChunkProviderCelestial {

	private MapGenBase caveGenerator = new MapGenCaves();
	private MapGenTiltedSpires spires = new MapGenTiltedSpires(6, 6, 0F);

	private MapGenBubble brine = new MapGenBubble(WorldConfig.ikeBrineSpawn);
	private MapGenCrater sellafield = new MapGenCrater(WorldConfig.radfreq / 10);

	public ChunkProviderIke(World world, long seed, boolean hasMapFeatures) {
		super(world, seed, hasMapFeatures);

		spires.rock = ModBlocks.ike_stone;
		spires.regolith = ModBlocks.ike_regolith;
		spires.mid = 86;

		brine.block = ModBlocks.ore_brine;
		brine.meta = (byte)CelestialBody.getMeta(world);
		brine.replace = ModBlocks.ike_stone;
		brine.setSize(8, 16);

		sellafield.regolith = ModBlocks.sellafield;
		sellafield.rock = ModBlocks.sellafield_slaked;

		stoneBlock = ModBlocks.ike_stone;
	}

	@Override
	public BlockMetaBuffer getChunkPrimer(int x, int z) {
		BlockMetaBuffer buffer = super.getChunkPrimer(x, z);
		brine.setMetas(buffer.metas);

		spires.func_151539_a(this, worldObj, x, z, buffer.blocks);
		caveGenerator.func_151539_a(this, worldObj, x, z, buffer.blocks);
		brine.func_151539_a(this, worldObj, x, z, buffer.blocks);
		sellafield.func_151539_a(this, worldObj, x, z, buffer.blocks);

		return buffer;
	}

	// man fuck Ike, why you gotta be spawning shit again
	@SuppressWarnings("rawtypes")
	@Override
	public List getPossibleCreatures(EnumCreatureType creatureType, int x, int y, int z) {
        return null;
	}

}
