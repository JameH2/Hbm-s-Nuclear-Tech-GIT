package com.hbm.dim.tekto;

import com.hbm.blocks.ModBlocks;
import com.hbm.config.WorldConfig;
import com.hbm.dim.CelestialBody;
import com.hbm.dim.ChunkProviderCelestial;
import com.hbm.dim.mapgen.MapGenGreg;
import com.hbm.dim.mapgen.MapGenVolcano;
import com.hbm.dim.tekto.biome.BiomeGenBaseTekto;
import com.hbm.world.gen.terrain.MapGenBubble;

import net.minecraft.world.World;

public class ChunkProviderTekto extends ChunkProviderCelestial {

	private MapGenGreg caveGenV3 = new MapGenGreg();
	private MapGenVolcano volcano = new MapGenVolcano(12);

	private MapGenBubble tektonic = new MapGenBubble(WorldConfig.tektoOilSpawn);

	public ChunkProviderTekto(World world, long seed, boolean hasMapFeatures) {
		super(world, seed, hasMapFeatures);
		reclamp = false; // please kill this, we spend more time calculating perlin noise than anything then skip two whole octaves of it!
		caveGenV3.stoneBlock = ModBlocks.basalt;

		volcano.setSize(8, 16);
		volcano.setMaterial(ModBlocks.geysir_chloric, ModBlocks.vinyl_sand);

		tektonic.block = ModBlocks.ore_tekto;
		tektonic.meta = (byte)CelestialBody.getMeta(world);
		tektonic.replace = ModBlocks.basalt;
		tektonic.setSize(8, 16);

		stoneBlock = ModBlocks.basalt;
		seaBlock = ModBlocks.ccl_block;
	}

	@Override
	public BlockMetaBuffer getChunkPrimer(int x, int z) {
		BlockMetaBuffer buffer = super.getChunkPrimer(x, z);
		tektonic.setMetas(buffer.metas);

		if(biomesForGeneration[0] == BiomeGenBaseTekto.vinylsands) {
			volcano.func_151539_a(this, worldObj, x, z, buffer.blocks);
		}

		caveGenV3.func_151539_a(this, worldObj, x, z, buffer.blocks);

		tektonic.func_151539_a(this, worldObj, x, z, buffer.blocks);

		// how many times do I gotta say BEEEEG
		return buffer;
	}

}