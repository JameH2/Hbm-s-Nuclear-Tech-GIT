package com.hbm.dim.wal;

import java.util.Random;

import com.hbm.blocks.ModBlocks;
import com.hbm.config.MobConfig;
import com.hbm.config.SpaceConfig;
import com.hbm.dim.BiomeDecoratorCelestial;
import com.hbm.dim.BiomeGenBaseCelestial;
import com.hbm.entity.mob.EntityBrineSlime;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;

public class BiomeGenWal extends BiomeGenBaseCelestial {

	public static final BiomeGenBase biome = new BiomeGenWal(SpaceConfig.walBiome);

	public BiomeGenWal(int id) {
		super(id);
		this.setBiomeName("Wal");
		this.setDisableRain();

		if(MobConfig.enableBrineSlime) {
			this.monsters.add(new BiomeGenBase.SpawnListEntry(EntityBrineSlime.class, 30, 10, 10));
		}

		BiomeDecoratorCelestial decorator = new BiomeDecoratorCelestial(ModBlocks.wal_rock);
		decorator.lakeChancePerChunk = 4; // Slightly more common than Ike's craters
		decorator.lakeBlock = ModBlocks.brine_block;
		decorator.lavaCount = 0; // Prevent hardcoded lava springs
		this.theBiomeDecorator = decorator;
		this.theBiomeDecorator.generateLakes = true;

		this.setHeight(new BiomeGenBase.Height(0.125F, 0.05F));

		this.topBlock = ModBlocks.wal_turf;
		this.fillerBlock = ModBlocks.wal_rock;
	}

	@Override
	public float getSpawningChance() {
		return 0.008F;
	}

	@Override
	public void genTerrainBlocks(World world, Random rand, Block[] blocks, byte[] meta, int x, int z, double noise) {
		// boolean flag = true;
		Block block = this.topBlock;
		byte b0 = (byte) (this.field_150604_aj & 255);
		Block block1 = this.fillerBlock;
		int k = -1;
		int l = (int) (noise / 3.0D + 3.0D + rand.nextDouble() * 0.25D);
		int i1 = x & 15;
		int j1 = z & 15;
		int k1 = blocks.length / 256;

		for (int l1 = 255; l1 >= 0; --l1) {
			int i2 = (j1 * 16 + i1) * k1 + l1;

			if (l1 <= 0 + rand.nextInt(5)) {
				blocks[i2] = Blocks.bedrock;
			} else {
				Block block2 = blocks[i2];

				if (block2 != null && block2.getMaterial() != Material.air) {
					if (block2 == ModBlocks.wal_rock) {
						if (k == -1) {
							if (l <= 0) {
								block = null;
								b0 = 0;
								block1 = ModBlocks.wal_rock;
							} else if (l1 >= 59 && l1 <= 64) {
								block = this.topBlock;
								b0 = (byte) (this.field_150604_aj & 255);
								block1 = this.fillerBlock;
							}

							if (l1 < 63 && (block == null || block.getMaterial() == Material.air)) {
								if (this.getFloatTemperature(x, l1, z) < 0.15F) {
									block = this.topBlock;
									b0 = 0;
								} else {
									block = this.topBlock;
									b0 = 0;
								}
							}

							k = l;

							if (l1 >= 62) {
								blocks[i2] = block;
								meta[i2] = b0;
							} else if (l1 < 56 - l) {
								block = null;
								block1 = ModBlocks.wal_rock;
								blocks[i2] = Blocks.gravel;
							} else {
								blocks[i2] = block1;
							}
						} else if (k > 0) {
							--k;
							blocks[i2] = block1;

							if (k == 0 && block1 == Blocks.sand) {
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