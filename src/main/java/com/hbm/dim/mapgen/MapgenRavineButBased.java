package com.hbm.dim.mapgen;

import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.gen.MapGenBase;

public class MapgenRavineButBased extends MapGenBase {

	public Block stoneBlock;
	public int yMin = 40;
	public int frequency = 50; // lower is more frequent
	public int strataFreq = 3;
	public float strataScale = 1.0F;
	public double taper = 6.0D;
	public double width = 1.0D;
	public boolean allowUnderwater = false;
	public double height = 3.0D;
	
	private float[] randoms = new float[1024];

	public MapgenRavineButBased() {
		super();
		this.stoneBlock = Blocks.stone;
	}

	protected void buildRavine(long seed, int chunkX, int chunkZ, Block[] blocks, double x, double y, double z, float br, float rot, float sr, int from, int to) {
		Random random = new Random(seed);
		double d4 = (double) (chunkX * 16 + 8);
		double d5 = (double) (chunkZ * 16 + 8);
		float f3 = 0.0F;
		float f4 = 0.0F;

		if(to <= 0) {
			int j1 = this.range * 16 - 16;
			to = j1 - random.nextInt(j1 / 4);
		}

		boolean flag1 = false;

		if(from == -1) {
			from = to / 2;
			flag1 = true;
		}

		float f5 = 1.0F;

		for(int k1 = 0; k1 < 256; ++k1) {
			if(k1 == 0 || random.nextInt(strataFreq) == 0) {
				f5 = 1.0F + random.nextFloat() * random.nextFloat() * strataScale;
			}

			this.randoms[k1] = f5 * f5;
		}

		for(; from < to; ++from) {
			double d12 = 1.5D + (double) (MathHelper.sin((float) from * (float) Math.PI / (float) to) * br * 1.0F);
			double d6 = d12 * height;
			d12 *= (double) random.nextFloat() * 0.25D + 0.75D;
			d6 *= (double) random.nextFloat() * 0.25D + 0.75D;
			float f6 = MathHelper.cos(sr);
			float f7 = MathHelper.sin(sr);
			x += (double) (MathHelper.cos(rot) * f6);
			y += (double) f7;
			z += (double) (MathHelper.sin(rot) * f6);
			sr *= 0.7F;
			sr += f4 * 0.05F;
			rot += f3 * 0.05F;
			f4 *= 0.8F;
			f3 *= 0.5F;
			f4 += (random.nextFloat() - random.nextFloat()) * random.nextFloat() * 2.0F;
			f3 += (random.nextFloat() - random.nextFloat()) * random.nextFloat() * 4.0F;

			if(flag1 || random.nextInt(4) != 0) {
				double d7 = x - d4;
				double d8 = z - d5;
				double d9 = (double) (to - from);
				double d10 = (double) (br + 2.0F + 16.0F);

				if(d7 * d7 + d8 * d8 - d9 * d9 > d10 * d10) {
					return;
				}

				if(x >= d4 - 16.0D - d12 * 2.0D && z >= d5 - 16.0D - d12 * 2.0D && x <= d4 + 16.0D + d12 * 2.0D && z <= d5 + 16.0D + d12 * 2.0D) {
					int i4 = MathHelper.floor_double(x - d12) - chunkX * 16 - 1;
					int l1 = MathHelper.floor_double(x + d12) - chunkX * 16 + 1;
					int j4 = MathHelper.floor_double(y - d6) - 1;
					int i2 = MathHelper.floor_double(y + d6) + 1;
					int k4 = MathHelper.floor_double(z - d12) - chunkZ * 16 - 1;
					int j2 = MathHelper.floor_double(z + d12) - chunkZ * 16 + 1;

					if(i4 < 0) {
						i4 = 0;
					}

					if(l1 > 16) {
						l1 = 16;
					}

					if(j4 < 1) {
						j4 = 1;
					}

					if(i2 > 248) {
						i2 = 248;
					}

					if(k4 < 0) {
						k4 = 0;
					}

					if(j2 > 16) {
						j2 = 16;
					}

					boolean isUnderwater = false;
					int k2;
					int j3;

					for(k2 = i4; !isUnderwater && k2 < l1; ++k2) {
						for(int l2 = k4; !isUnderwater && l2 < j2; ++l2) {
							for(int i3 = i2 + 1; !isUnderwater && i3 >= j4 - 1; --i3) {
								j3 = (k2 * 16 + l2) * 256 + i3;

								if(i3 >= 0 && i3 < 256) {
									if(isOceanBlock(blocks, j3, k2, i3, l2, chunkX, chunkZ)) {
										isUnderwater = true;
									}

									if(i3 != j4 - 1 && k2 != i4 && k2 != l1 - 1 && l2 != k4 && l2 != j2 - 1) {
										i3 = j4;
									}
								}
							}
						}
					}

					if(!isUnderwater || allowUnderwater) {
						for(k2 = i4; k2 < l1; ++k2) {
							double d13 = ((double) (k2 + chunkX * 16) + 0.5D - x) / d12;

							for(j3 = k4; j3 < j2; ++j3) {
								double d14 = ((double) (j3 + chunkZ * 16) + 0.5D - z) / d12;
								int k3 = (k2 * 16 + j3) * 256 + i2;
								boolean foundTop = false;

								if(d13 * d13 + d14 * d14 < width) {
									for(int l3 = i2 - 1; l3 >= j4; --l3) {
										double d11 = ((double) l3 + 0.5D - y) / d6;

										if((d13 * d13 + d14 * d14) * (double) this.randoms[l3] + d11 * d11 / taper < width) {
											if(isTopBlock(blocks, k3, k2, l3, j3, chunkX, chunkZ)) {
												foundTop = true;
											}

											digBlock(blocks, k3, k2, l3, j3, chunkX, chunkZ, foundTop, isUnderwater);
										}

										--k3;
									}
								}
							}
						}

						if(flag1) {
							break;
						}
					}
				}
			}
		}
	}

	@Override
	protected void func_151538_a(World world, int offsetX, int offsetZ, int chunkX, int chunkZ, Block[] blocks) {
		if(this.rand.nextInt(frequency) == 0) {
			double x = (double) (offsetX * 16 + this.rand.nextInt(16));
			double y = (double) (this.rand.nextInt(this.rand.nextInt(yMin) + 8) + 20);
			double z = (double) (offsetZ * 16 + this.rand.nextInt(16));
			byte b0 = 1;

			for(int i1 = 0; i1 < b0; ++i1) {
				float rotation = this.rand.nextFloat() * (float) Math.PI * 2.0F;
				float smallRandom = (this.rand.nextFloat() - 0.5F) * 2.0F / 8.0F;
				float bigRandom = (this.rand.nextFloat() * 2.0F + this.rand.nextFloat()) * 2.0F;
				this.buildRavine(this.rand.nextLong(), chunkX, chunkZ, blocks, x, y, z, bigRandom, rotation, smallRandom, 0, 0);
			}
		}
	}

	protected boolean isOceanBlock(Block[] data, int index, int x, int y, int z, int chunkX, int chunkZ) {
		return data[index] == Blocks.water || data[index] == Blocks.flowing_water;
	}

	// Determine if the block at the specified location is the top block for the
	// biome, we take into account
	// Vanilla bugs to make sure that we generate the map the same way vanilla does.
	private boolean isTopBlock(Block[] data, int index, int x, int y, int z, int chunkX, int chunkZ) {
		BiomeGenBase biome = worldObj.getBiomeGenForCoords(x + chunkX * 16, z + chunkZ * 16);
		return data[index] == biome.topBlock;
	}

	protected void digBlock(Block[] data, int index, int x, int y, int z, int chunkX, int chunkZ, boolean foundTop, boolean underwater) {
		BiomeGenBase biome = worldObj.getBiomeGenForCoords(x + chunkX * 16, z + chunkZ * 16);
		Block top = biome.topBlock;
		Block filler = biome.fillerBlock;
		Block block = data[index];

		if(block == stoneBlock || block == filler || block == top) {
			if(y < 10) {
				data[index] = underwater ? Blocks.obsidian : Blocks.flowing_lava;
			} else {
				data[index] = underwater && y < 62 ? Blocks.water : null;

				if(foundTop && data[index - 1] == filler) {
					data[index - 1] = top;
				}
			}
		}
	}

}
