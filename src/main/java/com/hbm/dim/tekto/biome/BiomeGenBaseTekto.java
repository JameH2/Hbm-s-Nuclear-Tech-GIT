

package com.hbm.dim.tekto.biome;

import com.hbm.blocks.ModBlocks;
import com.hbm.config.SpaceConfig;
import com.hbm.dim.BiomeDecoratorCelestial;
import com.hbm.dim.BiomeGenBaseCelestial;

import net.minecraft.world.biome.BiomeGenBase;
import net.minecraftforge.common.BiomeDictionary;

public abstract class BiomeGenBaseTekto extends BiomeGenBaseCelestial {

	public static final BiomeGenBase polyvinylPlains = new BiomeGenPolyvinylPlains(SpaceConfig.tektoPolyvinylBiome);
	public static final BiomeGenBase halogenHills = new BiomeGenHalogenHills(SpaceConfig.tektoHalogenHillBiome);
	public static final BiomeGenBase tetrachloricRiver = new BiomeGenTetrachloricRiver(SpaceConfig.tektoRiverBiome);
	public static final BiomeGenBase forest = new BiomeGenForest(SpaceConfig.tektoForestBiome);
	public static final BiomeGenBase vinylsands = new BiomeGenVinylSands(SpaceConfig.tektoVinylIslandBiome);

	public BiomeGenBaseTekto(int id) {
		super(id);
		this.waterColorMultiplier = 0x5b009a;

		setTemperatureRainfall(1.0F, 0.5F);

		BiomeDecoratorCelestial decorator = new BiomeDecoratorCelestial(ModBlocks.basalt);
		decorator.rubberPlantsPerChunk = 64;
		this.theBiomeDecorator = decorator;
		this.theBiomeDecorator.generateLakes = false;

		this.topBlock = ModBlocks.vinyl_sand;
		this.fillerBlock = ModBlocks.vinyl_sand;
		BiomeDictionary.registerBiomeType(this, BiomeDictionary.Type.HOT, BiomeDictionary.Type.DRY, BiomeDictionary.Type.DEAD, BiomeDictionary.Type.SPOOKY);
	}
}