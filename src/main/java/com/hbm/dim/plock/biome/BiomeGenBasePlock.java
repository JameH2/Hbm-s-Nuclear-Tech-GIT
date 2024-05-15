
package com.hbm.dim.plock.biome;

import java.util.Random;

import com.hbm.config.SpaceConfig;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;

public class BiomeGenBasePlock extends BiomeGenBase
{
    public static final BiomeGenBase plockPlains = new BiomeGenPlock(SpaceConfig.PlockBiome).setTemperatureRainfall(-1.0F, 0.0F);
    public static final BiomeGenBase plockLowlands = new BiomeGenPlockOceans(SpaceConfig.PlockBasins).setTemperatureRainfall(-1.0F, 0.0F);
    //public static final BiomeGenBase plockPolar = new BiomeGenDunaPolar(SpaceConfig.dunaPolarBiome).setTemperatureRainfall(-1.0F, 0.0F);
    //public static final BiomeGenBase plockHills = new BiomeGenDunaHills(SpaceConfig.dunaHillsBiome).setTemperatureRainfall(-1.0F, 0.0F);
    
    public BiomeGenBasePlock(int id)
    {
        super(id);
    }
}