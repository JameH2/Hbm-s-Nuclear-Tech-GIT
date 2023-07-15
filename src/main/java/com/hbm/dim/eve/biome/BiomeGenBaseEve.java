/*******************************************************************************
 * Copyright 2015 SteveKunG - More Planets Mod
 *
 * This work is licensed under a Creative Commons Attribution-NonCommercial-NoDerivatives 4.0 International Public License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-nc-nd/4.0/.
 ******************************************************************************/

package com.hbm.dim.eve.biome;

import java.util.Random;

import com.hbm.config.WorldConfig;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;

public class BiomeGenBaseEve extends BiomeGenBase
{
    public static final BiomeGenBase evePlains = new BiomeGenEve(WorldConfig.eveBiome).setTemperatureRainfall(-1.0F, 0.0F);
    public static final BiomeGenBase eveOcean = new BiomeGenEveOcean(WorldConfig.eveOceanBiome).setTemperatureRainfall(-1.0F, 0.0F);
    //public static final BiomeGenBase eveHighlands = new BiomeGenDunaPolar(WorldConfig.dunaPolarBiome).setTemperatureRainfall(-1.0F, 0.0F);
    //public static final BiomeGenBase eveHills = new BiomeGenDunaHills(WorldConfig.dunaHillsBiome).setTemperatureRainfall(-1.0F, 0.0F);
    public static final BiomeGenBase eveMountains = new BiomeGenEveMountains(WorldConfig.eveMountainsBiome).setTemperatureRainfall(-1.0F, 0.0F);
    
    public BiomeGenBaseEve(int id)
    {
        super(id);
    }
}