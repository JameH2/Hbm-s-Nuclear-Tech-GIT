/*******************************************************************************
 * Copyright 2015 SteveKunG - More Planets Mod
 *
 * This work is licensed under a Creative Commons Attribution-NonCommercial-NoDerivatives 4.0 International Public License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-nc-nd/4.0/.
 ******************************************************************************/

package com.hbm.dim.laythe.biome;

import java.util.Random;

import com.hbm.blocks.ModBlocks;
import com.hbm.config.SpaceConfig;
import com.hbm.dim.BiomeDecoratorCelestial;
import com.hbm.dim.BiomeGenBaseCelestial;
import com.hbm.entity.mob.EntityScutterfish;
import com.hbm.entity.mob.EntitySifterEel;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraftforge.common.BiomeDictionary;

public abstract class BiomeGenBaseLaythe extends BiomeGenBaseCelestial {

	public static final BiomeGenBase laytheIsland = new BiomeGenLaytheIslands(SpaceConfig.laytheBiome);
	public static final BiomeGenBase laytheOcean = new BiomeGenLaytheOcean(SpaceConfig.laytheOceanBiome);
	public static final BiomeGenBase laythePolar = new BiomeGenLaythePolar(SpaceConfig.laythePolarBiome);
	public static final BiomeGenBase laytheCoast = new BiomeGenLaytheCoast(SpaceConfig.laytheCoastBiome);

	public BiomeGenBaseLaythe(int id) {
		super(id);
		this.waterColorMultiplier = 0x5b009a;

		this.waterCreatures.add(new BiomeGenBase.SpawnListEntry(EntityScutterfish.class, 10, 4, 4));
		this.waterCreatures.add(new BiomeGenBase.SpawnListEntry(EntitySifterEel.class, 10, 1, 3));
		
		BiomeDecoratorCelestial decorator = new BiomeDecoratorCelestial(Blocks.stone);
		decorator.waterPlantsPerChunk = 32;
		// decorator.seaLevel = 96;
		this.theBiomeDecorator = decorator;
		this.theBiomeDecorator.generateLakes = false;

		this.topBlock = ModBlocks.laythe_silt;
		this.fillerBlock = ModBlocks.laythe_silt;
		BiomeDictionary.registerBiomeType(this, BiomeDictionary.Type.COLD, BiomeDictionary.Type.WET, BiomeDictionary.Type.DENSE, BiomeDictionary.Type.SPOOKY);
	}

	// Same as vanilla but without the gravel
	@Override
	public void genTerrainBlocks(World world, Random rand, Block[] blocks, byte[] meta, int x, int z, double noise) {
		Block block = this.topBlock;
		byte b0 = (byte) (this.field_150604_aj & 255);
		Block block1 = this.fillerBlock;
		int k = -1;
		int l = (int) (noise / 3.0D + 3.0D + rand.nextDouble() * 0.25D);
		int i1 = x & 15;
		int j1 = z & 15;
		int k1 = blocks.length / 256;

		for(int l1 = 255; l1 >= 0; --l1) {
			int i2 = (j1 * 16 + i1) * k1 + l1;

			if(l1 <= 0 + rand.nextInt(5)) {
				blocks[i2] = Blocks.bedrock;
			} else {
				Block block2 = blocks[i2];

				if(block2 != null && block2.getMaterial() != Material.air) {
					if(block2 == Blocks.stone) {
						if(k == -1) {
							if(l <= 0) {
								block = null;
								b0 = 0;
								block1 = Blocks.stone;
							} else if(l1 >= 59 && l1 <= 64) {
								block = this.topBlock;
								b0 = (byte) (this.field_150604_aj & 255);
								block1 = this.fillerBlock;
							}

							if(l1 < 63 && (block == null || block.getMaterial() == Material.air)) {
								if(this.getFloatTemperature(x, l1, z) < 0.15F) {
									block = Blocks.ice;
									b0 = 0;
								} else {
									block = Blocks.water;
									b0 = 0;
								}
							}

							k = l;

							if(l1 >= 62) {
								blocks[i2] = block;
								meta[i2] = b0;
							} else if(l1 < 56 - l) {
								block = null;
								block1 = Blocks.stone;
								blocks[i2] = ModBlocks.laythe_silt;
							} else {
								blocks[i2] = block1;
							}
						} else if(k > 0) {
							--k;
							blocks[i2] = block1;

							if(k == 0 && block1 == Blocks.sand) {
								k = rand.nextInt(4) + Math.max(0, l1 - 63);
								block1 = Blocks.sandstone;
							}
						}
					}
				} else {
					k = -1;
				}
			}
		}
	}
}