package com.hbm.dim.moho;

import com.hbm.blocks.ModBlocks;
import com.hbm.dim.ChunkProviderCelestial;
import com.hbm.dim.mapgen.ExperimentalCaveGenerator;
import com.hbm.dim.mapgen.MapGenCrater;
import com.hbm.dim.mapgen.MapGenPlateau;
import com.hbm.dim.mapgen.MapGenVolcano;
import com.hbm.dim.mapgen.MapgenRavineButBased;
import com.hbm.dim.moho.biome.BiomeGenBaseMoho;
import com.hbm.dim.noise.MapGenVNoise;

import net.minecraft.init.Blocks;
import net.minecraft.world.World;

public class ChunkProviderMoho extends ChunkProviderCelestial {

	private ExperimentalCaveGenerator caveGenV2 = new ExperimentalCaveGenerator(1, 52, 5.0F);
	private MapgenRavineButBased rgen = new MapgenRavineButBased();

	private MapGenVNoise noise = new MapGenVNoise();
	private MapGenCrater smallCrater = new MapGenCrater(6);
	private MapGenCrater largeCrater = new MapGenCrater(64);
	private MapGenVolcano volcano = new MapGenVolcano(72);
	private MapGenPlateau plateau = new MapGenPlateau(worldObj);

	public ChunkProviderMoho(World world, long seed, boolean hasMapFeatures) {
		super(world, seed, hasMapFeatures);

		smallCrater.setSize(8, 32);
		largeCrater.setSize(96, 128);
		volcano.setSize(64, 128);
		volcano.setMaterial(ModBlocks.volcano_core, ModBlocks.basalt);

		smallCrater.regolith = largeCrater.regolith = ModBlocks.moho_regolith;
		smallCrater.rock = largeCrater.rock = ModBlocks.moho_stone;

		caveGenV2.stoneBlock = ModBlocks.moho_stone;
		rgen.stoneBlock = ModBlocks.moho_stone;
		stoneBlock = ModBlocks.moho_stone;
		seaBlock = Blocks.lava;

		noise.surfBlock = ModBlocks.moho_stone;
		noise.rockBlock = ModBlocks.moho_stone;
		noise.fluidBlock = Blocks.lava;
		noise.crackSize = 0.5;
		noise.cellSize = 27;
		noise.plateStartY = 62;
		noise.plateThickness = 25;
		noise.applyToBiome = BiomeGenBaseMoho.mohoLavaSea;

		plateau.maxPlateauAddition = 6;
		plateau.surfrock = ModBlocks.moho_regolith;
		plateau.stoneBlock = ModBlocks.moho_stone;
		plateau.fillblock = Blocks.lava;
		plateau.maxPlateauAddition = 6;
		plateau.stepHeight = 2;
		plateau.noiseScale = 0.03;
		plateau.applyToBiome = BiomeGenBaseMoho.mohoPlateau;
	}

	@Override
	public BlockMetaBuffer getChunkPrimer(int x, int z) {
		BlockMetaBuffer buffer = super.getChunkPrimer(x, z);

		boolean hasLavaSea = false;
		boolean hasPlateau = false;

		for(int i = 0; i < biomesForGeneration.length; i++) {
			if(biomesForGeneration[i] == BiomeGenBaseMoho.mohoLavaSea) hasLavaSea = true;
			if(biomesForGeneration[i] == BiomeGenBaseMoho.mohoPlateau) hasPlateau = true;
			if(hasLavaSea && hasPlateau) break;
		}

		if(hasLavaSea) noise.func_151539_a(this, worldObj, x, z, buffer.blocks);

		if(hasPlateau) plateau.func_151539_a(this, worldObj, x, z, buffer.blocks);

		caveGenV2.func_151539_a(this, worldObj, x, z, buffer.blocks);
		rgen.func_151539_a(this, worldObj, x, z, buffer.blocks);
		smallCrater.func_151539_a(this, worldObj, x, z, buffer.blocks);
		largeCrater.func_151539_a(this, worldObj, x, z, buffer.blocks);
		volcano.func_151539_a(this, worldObj, x, z, buffer.blocks);
		return buffer;
	}

}
