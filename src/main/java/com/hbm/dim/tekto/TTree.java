package com.hbm.dim.tekto;

import java.util.Random;

import com.hbm.blocks.ModBlocks;

import net.minecraft.block.Block;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.WorldGenAbstractTree;

public class TTree extends WorldGenAbstractTree {

	int offset;
	int smallest;
	int tallest;
	int xz;
	int y;
	boolean vines;
	Block logBlock;
	Block leavBlock;

	public TTree(boolean notify, int offset, int smallest, int tallest, int xz, int y, boolean vines, Block log, Block leaf) {
		super(notify);
		this.offset = offset;
		this.smallest = smallest;
		this.tallest = tallest;
		this.xz = xz;
		this.y = y;
		this.vines = vines;
		this.logBlock = log;
		this.leavBlock = leaf;
	}

	@Override
	public boolean generate(World world, Random rand, int x, int y, int z) {
		int height = rand.nextInt(smallest) + tallest;

		if (y < 1 || y + height + 1 > 256) {
			return false;
		}

		Block blockBelow = world.getBlock(x, y - 1, z);
		if (blockBelow != ModBlocks.rubber_grass && blockBelow != ModBlocks.rubber_silt) {
			return false;
		}

		for (int i = 0; i < height; i++) {
			world.setBlock(x, y + i, z, logBlock, 0, 2);
		}


		int bulbStartY = y + height - offset;
		int bulbRadiusXz = xz;
		int bulbRadiusY = this.y;


		for (int dy = -bulbRadiusY; dy <= bulbRadiusY; dy++) {
			for (int dx = -bulbRadiusXz; dx <= bulbRadiusXz; dx++) {
				for (int dz = -bulbRadiusXz; dz <= bulbRadiusXz; dz++) {

					if (Math.pow(dx / (double)bulbRadiusXz, 2) + Math.pow(dy / (double)bulbRadiusY, 2) + Math.pow(dz / (double)bulbRadiusXz, 2) <= 1) {
						Block block = world.getBlock(x + dx, bulbStartY + dy, z + dz);
						if (block.isAir(world, x + dx, bulbStartY + dy, z + dz)) {
							world.setBlock(x + dx, bulbStartY + dy, z + dz, leavBlock, 0, 2);
							if (vines) {
								for (int i = 0; i < 4; i++) {
									int vineX = x + dx + (i % 2 == 0 ? (i - 1) : 0);
									int vineZ = z + dz + (i % 2 == 1 ? (i - 2) : 0);

									if (rand.nextInt(12) == 0) {
										for (int j = 0; j < rand.nextInt(15); j++) {
											int vineY = bulbStartY + dy - j;
											if (world.isAirBlock(vineX, vineY, vineZ)) {
												world.setBlock(vineX, vineY, vineZ, ModBlocks.vinyl_vines, 0, 2);
												if (vineX > x + dx) {
													world.setBlockMetadataWithNotify(vineX, vineY, vineZ, 0x2, 2);
												} else if (vineX < x + dx) {
													world.setBlockMetadataWithNotify(vineX, vineY, vineZ, 0x1, 2);
												} else if (vineZ > z + dz) {
													world.setBlockMetadataWithNotify(vineX, vineY, vineZ, 0x4, 2);
												} else if (vineZ < z + dz) {
													world.setBlockMetadataWithNotify(vineX, vineY, vineZ, 0x3, 2);
												}
											} else {
												break;
											}
										}
									}
								}
							}
						}
					}
				}
			}
		}

		return true;
	}

}