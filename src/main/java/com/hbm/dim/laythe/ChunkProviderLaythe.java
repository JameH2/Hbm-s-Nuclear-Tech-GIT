package com.hbm.dim.laythe;

import java.util.ArrayList;
import java.util.List;

import com.hbm.blocks.ModBlocks;
import com.hbm.config.WorldConfig;
import com.hbm.dim.CelestialBody;
import com.hbm.dim.ChunkProviderCelestial;
import com.hbm.dim.laythe.biome.BiomeGenBaseLaythe;
import com.hbm.dim.mapgen.MapGenGreg;
import com.hbm.dim.mapgen.MapGenRoots;
import com.hbm.dim.mapgen.MapGenTiltedSpires;
import com.hbm.dim.mapgen.MapgenRavineButBased;
import com.hbm.entity.mob.EntityCreeperFlesh;
import com.hbm.world.gen.terrain.MapGenBubble;

import net.minecraft.block.Block;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase.SpawnListEntry;

public class ChunkProviderLaythe extends ChunkProviderCelestial {

	private MapGenGreg caveGenV3 = new MapGenGreg();
	private MapgenRavineButBased rgen = new MapgenRavineButBased();
	private MapGenRoots roots = new MapGenRoots();

	private MapGenTiltedSpires spires = new MapGenTiltedSpires(2, 14, 0.75F);
	private MapGenTiltedSpires snowires = new MapGenTiltedSpires(2, 14, 0.75F);

	private MapGenBubble oil = new MapGenBubble(WorldConfig.laytheOilSpawn);

	private List<SpawnListEntry> spawnedOfFlesh = new ArrayList<SpawnListEntry>();

	public ChunkProviderLaythe(World world, long seed) {
		super(world, seed);

		rgen.yMin = 32;
		rgen.frequency = 20;
		rgen.strataFreq = 3;
		rgen.strataScale = 8.0F;
		rgen.width = 16.0D;
		rgen.taper = 8.0D;
		rgen.allowUnderwater = true;
		rgen.height = 5.0D;

		snowires.rock = Blocks.packed_ice;
		snowires.regolith = Blocks.snow;
		spires.rock = Blocks.stone;
		spires.regolith = ModBlocks.laythe_silt;

		spires.curve = snowires.curve = true;
		spires.maxPoint = snowires.maxPoint = 6.0F;
		spires.maxTilt = snowires.maxTilt = 3.5F;

		oil.block = ModBlocks.ore_oil;
		oil.meta = (byte)CelestialBody.getMeta(world);
		oil.replace = Blocks.stone;
		oil.setSize(8, 16);

		seaBlock = Blocks.water;
		// seaLevel = 96;

		spawnedOfFlesh.add(new SpawnListEntry(EntityCreeperFlesh.class, 10, 4, 4));
	}

	@Override
	protected Block getFlatWorldBlock(Block block) {
		if(block == Blocks.grass || block == Blocks.sand || block == Blocks.water || block == Blocks.flowing_water) return Blocks.water;
		if(block == Blocks.dirt || block == Blocks.stone || block == Blocks.sandstone) return ModBlocks.laythe_silt;
		if(block == Blocks.snow_layer) return Blocks.snow_layer;
		return block;
	}

	@Override
	public BlockMetaBuffer getChunkPrimer(int x, int z) {
		BlockMetaBuffer buffer = super.getChunkPrimer(x, z);
		oil.setMetas(buffer.metas);

		if(biomesForGeneration[0] == BiomeGenBaseLaythe.laythePolar) {
			snowires.func_151539_a(this, worldObj, x, z, buffer.blocks);
		} else {
			spires.func_151539_a(this, worldObj, x, z, buffer.blocks);
		}
		roots.func_151539_a(this, worldObj, x, z, buffer.blocks);
		caveGenV3.func_151539_a(this, worldObj, x, z, buffer.blocks);
		rgen.func_151539_a(this, worldObj, x, z, buffer.blocks);
		oil.func_151539_a(this, worldObj, x, z, buffer.blocks);

		return buffer;
	}

	@SuppressWarnings("rawtypes")
	@Override
	public List getPossibleCreatures(EnumCreatureType creatureType, int x, int y, int z) {
		if(creatureType == EnumCreatureType.monster && worldObj.getBlock(x, y - 1, z) == ModBlocks.tumor)
			return spawnedOfFlesh;

		return super.getPossibleCreatures(creatureType, x, y, z);
	}

}