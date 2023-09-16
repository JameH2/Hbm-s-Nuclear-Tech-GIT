package com.hbm.world.feature;

import java.util.Random;

import com.hbm.blocks.ModBlocks;

import net.minecraft.block.Block;
import com.hbm.util.LootGenerator;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;

public class GlyphidHive {

	public static final int[][][] schematicBigGround = new int[][][] {
		{
			{0,0,0,0,0,0,0,0,0,0,0},
			{0,0,0,0,0,0,0,0,0,0,0},
			{0,0,0,0,0,0,0,0,0,0,0},
			{0,0,0,0,1,1,1,0,0,0,0},
			{0,0,0,1,1,1,1,1,0,0,0},
			{0,0,0,1,1,1,1,1,0,0,0},
			{0,0,0,1,1,1,1,1,0,0,0},
			{0,0,0,0,1,1,1,0,0,0,0},
			{0,0,0,0,0,0,0,0,0,0,0},
			{0,0,0,0,0,0,0,0,0,0,0},
			{0,0,0,0,0,0,0,0,0,0,0},
		},
		{
			{0,0,0,0,0,0,0,0,0,0,0},
			{0,0,0,0,0,0,0,0,0,0,0},
			{0,0,0,0,1,1,1,0,0,0,0},
			{0,0,0,1,1,9,1,1,0,0,0},
			{0,0,1,1,9,9,9,1,1,0,0},
			{0,0,1,9,9,9,9,9,1,0,0},
			{0,0,1,1,9,9,9,1,1,0,0},
			{0,0,0,1,1,9,1,1,0,0,0},
			{0,0,0,0,1,1,1,0,0,0,0},
			{0,0,0,0,0,0,0,0,0,0,0},
			{0,0,0,0,0,0,0,0,0,0,0},
		},
		{
			{0,0,0,0,0,0,0,0,0,0,0},
			{0,0,0,0,3,3,3,0,0,0,0},
			{0,0,0,1,1,9,1,1,0,0,0},
			{0,0,1,1,9,9,9,1,1,0,0},
			{0,3,1,9,9,9,9,9,1,2,0},
			{0,3,9,9,9,9,9,9,9,2,0},
			{0,3,1,9,9,9,9,9,1,2,0},
			{0,0,1,1,9,9,9,1,1,0,0},
			{0,0,0,1,1,9,1,1,0,0,0},
			{0,0,0,0,2,2,2,0,0,0,0},
			{0,0,0,0,0,0,0,0,0,0,0},
		},
		{
			{0,0,0,0,0,3,0,0,0,0,0},
			{0,0,0,0,3,3,3,0,0,0,0},
			{0,0,0,1,3,9,3,1,0,0,0},
			{0,0,1,1,9,9,9,1,1,0,0},
			{0,3,3,9,9,9,9,9,2,2,0},
			{3,3,9,9,9,9,9,9,9,2,2},
			{0,3,3,9,9,9,9,9,2,2,0},
			{0,0,1,1,9,9,9,1,1,0,0},
			{0,0,0,1,2,9,2,1,0,0,0},
			{0,0,0,0,2,2,2,0,0,0,0},
			{0,0,0,0,0,2,0,0,0,0,0},
		},
		{
			{0,0,0,0,3,3,3,0,0,0,0},
			{0,0,0,1,3,3,3,1,0,0,0},
			{0,0,1,1,3,9,3,1,1,0,0},
			{0,1,1,1,9,9,0,1,1,1,0},
			{3,3,3,9,9,9,9,9,2,2,2},
			{3,3,9,9,9,9,9,9,9,2,2},
			{3,3,3,9,9,9,9,9,2,2,2},
			{0,1,1,1,9,9,9,1,1,1,0},
			{0,0,1,1,2,9,2,1,1,0,0},
			{0,0,0,1,2,2,2,1,0,0,0},
			{0,0,0,0,2,2,2,0,0,0,0},
		},
		{
			{0,0,0,0,1,1,1,0,0,0,0},
			{0,0,0,1,1,1,1,1,0,0,0},
			{0,0,1,1,1,1,1,1,1,0,0},
			{0,1,1,1,1,1,1,1,1,1,0},
			{1,1,1,1,1,1,1,1,1,1,1},
			{1,1,1,1,1,1,1,1,1,1,1},
			{1,1,1,1,1,1,1,1,1,1,1},
			{0,1,1,1,1,1,1,1,1,1,0},
			{0,0,1,1,1,1,1,1,1,0,0},
			{0,0,0,1,1,1,1,1,0,0,0},
			{0,0,0,0,1,1,1,0,0,0,0},
		},
		{
			{0,0,0,0,0,0,0,0,0,0,0},
			{0,0,0,0,1,1,1,0,0,0,0},
			{0,0,0,1,1,1,1,1,0,0,0},
			{0,0,1,1,1,1,1,1,1,0,0},
			{0,1,1,1,1,1,1,1,1,1,0},
			{0,1,1,1,1,1,1,1,1,1,0},
			{0,1,1,1,1,1,1,1,1,1,0},
			{0,0,1,1,1,1,1,1,1,0,0},
			{0,0,0,1,1,1,1,1,0,0,0},
			{0,0,0,0,1,1,1,0,0,0,0},
			{0,0,0,0,0,0,0,0,0,0,0},
		}
	};
	public static final int[][][] schematicBigStructure = new int[][][] {
			{
					{0,0,0,0,0,0,0,0,0,0,0},
					{0,0,0,0,0,0,0,0,0,0,0},
					{0,0,0,0,0,0,0,0,0,0,0},
					{0,0,0,0,1,1,1,0,0,0,0},
					{0,0,0,1,1,1,1,1,0,0,0},
					{0,0,0,1,1,1,1,1,0,0,0},
					{0,0,0,1,1,1,1,1,0,0,0},
					{0,0,0,0,1,1,1,0,0,0,0},
					{0,0,0,0,0,0,0,0,0,0,0},
					{0,0,0,0,0,0,0,0,0,0,0},
					{0,0,0,0,0,0,0,0,0,0,0},
			},
			{
					{0,0,0,0,0,0,0,0,0,0,0},
					{0,0,0,0,0,0,0,0,0,0,0},
					{0,0,0,0,1,1,1,0,0,0,0},
					{0,0,0,1,1,9,1,1,0,0,0},
					{0,0,1,1,9,9,9,1,1,0,0},
					{0,0,1,9,9,9,9,9,1,0,0},
					{0,0,1,1,9,9,9,1,1,0,0},
					{0,0,0,1,1,9,1,1,0,0,0},
					{0,0,0,0,1,1,1,0,0,0,0},
					{0,0,0,0,0,0,0,0,0,0,0},
					{0,0,0,0,0,0,0,0,0,0,0},
			},
			{
					{0,0,0,0,9,9,9,0,0,0,0},
					{0,0,0,0,9,9,9,0,0,0,0},
					{0,0,0,1,9,9,9,1,0,0,0},
					{0,0,1,1,9,9,9,1,1,0,0},
					{9,9,9,9,9,9,9,9,9,9,9},
					{9,9,9,9,9,9,9,9,9,9,9},
					{9,9,9,9,9,9,9,9,9,9,9},
					{0,0,1,1,9,9,9,1,1,0,0},
					{0,0,0,1,9,9,9,1,0,0,0},
					{0,0,0,0,9,9,9,0,0,0,0},
					{0,0,0,0,9,9,9,0,0,0,0},
			},
			{
					{0,0,0,0,9,9,9,0,0,0,0},
					{0,0,0,0,9,9,9,0,0,0,0},
					{0,0,0,1,9,9,9,1,0,0,0},
					{0,0,1,1,9,9,9,1,1,0,0},
					{9,9,9,9,9,9,9,9,9,9,9},
					{9,9,9,9,9,9,9,9,9,9,9},
					{9,9,9,9,9,9,9,9,9,9,9},
					{0,0,1,1,9,9,9,1,1,0,0},
					{0,0,0,1,9,9,9,1,0,0,0},
					{0,0,0,0,9,9,9,0,0,0,0},
					{0,0,0,0,9,9,9,0,0,0,0},
			},
			{
					{0,0,0,0,9,9,9,0,0,0,0},
					{0,0,0,0,9,9,9,0,0,0,0},
					{0,0,0,1,9,8,9,1,0,0,0},
					{0,0,1,1,9,9,9,1,1,0,0},
					{9,9,9,9,9,9,9,9,7,9,9},
					{9,9,9,9,9,9,9,9,9,9,9},
					{9,9,9,9,9,9,9,9,9,9,9},
					{0,0,1,1,7,9,9,1,1,0,0},
					{0,0,0,1,9,9,9,1,0,0,0},
					{0,0,0,0,9,9,9,0,0,0,0},
					{0,0,0,0,9,9,9,0,0,0,0},
			},
			{
					{0,0,0,0,0,0,0,0,0,0,0},
					{0,0,0,0,0,0,0,0,0,0,0},
					{0,0,0,0,1,1,1,0,0,0,0},
					{0,0,0,1,1,9,1,1,0,0,0},
					{0,0,1,1,9,7,9,1,1,0,0},
					{0,0,1,9,9,9,9,9,1,0,0},
					{0,0,1,1,7,9,9,1,1,0,0},
					{0,0,0,1,1,9,1,1,0,0,0},
					{0,0,0,0,1,1,1,0,0,0,0},
					{0,0,0,0,0,0,0,0,0,0,0},
					{0,0,0,0,0,0,0,0,0,0,0},
			},
			{
					{0,0,0,0,0,0,0,0,0,0,0},
					{0,0,0,0,0,0,0,0,0,0,0},
					{0,0,0,0,0,0,0,0,0,0,0},
					{0,0,0,0,1,1,1,0,0,0,0},
					{0,0,0,1,1,1,1,1,0,0,0},
					{0,0,0,1,1,1,1,1,0,0,0},
					{0,0,0,1,1,1,1,1,0,0,0},
					{0,0,0,0,1,1,1,0,0,0,0},
					{0,0,0,0,0,0,0,0,0,0,0},
					{0,0,0,0,0,0,0,0,0,0,0},
					{0,0,0,0,0,0,0,0,0,0,0},
			}
	};
	public static void generateBigGround(World world, int x, int y, int z, Random rand, boolean openDesign) {
		
		int orientation = rand.nextInt(2) + 2;
		
		for(int i = 0; i < 11; i++) {
			for(int j = 0; j < 7; j++) {
				for(int k = 0; k < 11; k++) {
					
					int block = schematicBigGround[6 - j][i][k];

					boolean hasWall = !openDesign && (block != orientation && block > 1 && block < 6);

					if(block == 1 || hasWall) {
						world.setBlock(x + i - 5, y + j - 2, z + k - 5, ModBlocks.glyphid_base);
					} else if (block != 0) {
						world.setBlock(x + i - 5, y + j - 2, z + k - 5, Blocks.air);
					}
				}
			}
		}
		
		world.setBlock(x, y - 1, z, ModBlocks.glyphid_spawner);

	}
	public static void generateBigOrb(World world, int x, int y, int z, Random rand) {

		int orientation = rand.nextInt(2) + 2;

		for(int i = 0; i < 11; i++) {
			for(int j = 0; j < 7; j++) {
				for(int k = 0; k < 11; k++) {

					int block = schematicBigStructure[6 - j][i][k];

					switch (block) {
						case 1:
							world.setBlock(x + i - 5, y + j - 2, z + k - 5, ModBlocks.glyphid_base);
							break;

						case 7:
						case 8:
							world.setBlock(x + i - 5, y + j - 2, z + k - 5, ModBlocks.deco_loot);
							if (block == 8) LootGenerator.lootMakeshiftGun(world, x + i - 5, y + j - 2, z + k - 5);
							else LootGenerator.lootScrapMetal(world,x + i - 5, y + j - 2, z + k - 5);
							break;
						case 9:
							world.setBlock(x + i - 5, y + j - 2, z + k - 5, Blocks.air);
							break;

						default:
							if(block != orientation && block > 1 && block < 6){
								world.setBlock(x + i - 5, y + j - 2, z + k - 5, ModBlocks.glyphid_base);
							}
					}
				}
			}
		}

		world.setBlock(x, y - 1, z, ModBlocks.glyphid_spawner);
	}
	public static void generateBigFwatz(World world, int x, int y, int z) {

		for(int i = 0; i < 19; i++) {
			for(int j = 0; j < 19; j++) {
				for(int k = 0; k < 19; k++) {
					String c = schmaticBigFwatz[j][i].substring(k, k + 1);

					Block b = null;

					switch (c) {
						case "P":
							b = Blocks.air;
							break;
						case "X":
							b = ModBlocks.glyphid_support;
							break;
						case "M":
							b = ModBlocks.glyphid_base;
							break;
						case "Z":
							b = ModBlocks.glyphid_spawner;
							break;
					}

					if (b != null) {
						world.setBlock(x + i, y + j, z + k, b);
					}
				}
			}
		}
	}
	public static final String[][] schmaticBigFwatz = new String[][] {

			{
					"        XXX        ",
					"       XXXXX       ",
					"        XXX        ",
					"         X         ",
					"                   ",
					"                   ",
					"                   ",
					" X               X ",
					"XXX             XXX",
					"XXXX           XXXX",
					"XXX             XXX",
					" X               X ",
					"                   ",
					"                   ",
					"                   ",
					"         X         ",
					"        XXX        ",
					"       XXXXX       ",
					"        XXX        "
			},
			{
					"        XXX        ",
					"       XXXXX       ",
					"        XXX        ",
					"         X         ",
					"                   ",
					"                   ",
					"                   ",
					" X               X ",
					"XXX             XXX",
					"XXXX           XXXX",
					"XXX             XXX",
					" X               X ",
					"                   ",
					"                   ",
					"                   ",
					"         X         ",
					"        XXX        ",
					"       XXXXX       ",
					"        XXX        "
			},
			{
					"        XXX        ",
					"       XXXXX       ",
					"         X         ",
					"                   ",
					"                   ",
					"                   ",
					"                   ",
					" X               X ",
					"XX               XX",
					"XXX             XXX",
					"XX               XX",
					" X               X ",
					"                   ",
					"                   ",
					"                   ",
					"                   ",
					"         X         ",
					"       XXXXX       ",
					"        XXX        "
			},
			{
					"                   ",
					"        XXX        ",
					"       XXXXX       ",
					"         X         ",
					"                   ",
					"                   ",
					"                   ",
					"  X             X  ",
					" XX             XX ",
					" XXX           XXX ",
					" XX             XX ",
					"  X             X  ",
					"                   ",
					"                   ",
					"                   ",
					"         X         ",
					"        XXX        ",
					"       XXXXX       ",
					"                   "
			},
			{
					"                   ",
					"        XXX        ",
					"       XXXXX       ",
					"                   ",
					"                   ",
					"                   ",
					"                   ",
					"  X             X  ",
					" XX             XX ",
					" XX             XX ",
					" XX             XX ",
					"  X             X  ",
					"                   ",
					"                   ",
					"                   ",
					"                   ",
					"       XXXXX       ",
					"        XXX        ",
					"                   "
			},
			{
					"                   ",
					"        XXX        ",
					"       XXXXX       ",
					"                   ",
					"                   ",
					"                   ",
					"                   ",
					"  X             X  ",
					" XX             XX ",
					" XX             XX ",
					" XX             XX ",
					"  X             X  ",
					"                   ",
					"                   ",
					"                   ",
					"                   ",
					"       XXXXX       ",
					"        XXX        ",
					"                   "
			},
			{
					"                   ",
					"                   ",
					"        XXX        ",
					"        XXX        ",
					"                   ",
					"                   ",
					"                   ",
					"       XXXXX       ",
					"  XX   XXXXX   XX  ",
					"  XX   XXXXX   XX  ",
					"  XX   XXXXX   XX  ",
					"       XXXXX       ",
					"                   ",
					"                   ",
					"                   ",
					"        XXX        ",
					"        XXX        ",
					"                   ",
					"                   "
			},
			{
					"                   ",
					"                   ",
					"        XXX        ",
					"        XXX        ",
					"        XXX        ",
					"       XMXMM       ",
					"      MMXXXMX      ",
					"     XMXXXMMMX     ",
					"  XXXXXXXMMMMMXXX  ",
					"  XXXMMXMZMXMMXXX  ",
					"  XXXMXMMMMXXMXXX  ",
					"     XXMMMXXMX     ",
					"      MXXMMXM      ",
					"       MMXMX       ",
					"        XXX        ",
					"        XXX        ",
					"        XXX        ",
					"                   ",
					"                   "
			},
			{
					"                   ",
					"                   ",
					"        XXX        ",
					"        XXX        ",
					"       XMXMM       ",
					"      XMXXXMX      ",
					"     MMXMMMMMX     ",
					"    XMMMPPPMMMX    ",
					"  XXXMMPPPPPMMMXX  ",
					"  XXMMMPPPPPMMMXX  ",
					"  XXMMMPPMZMMMMXX  ",
					"    XMMMPPMMMMM    ",
					"     XXMMMXMMM     ",
					"      MMMMXMX      ",
					"       MMXMM       ",
					"        XXX        ",
					"        XXX        ",
					"                   ",
					"                   "
			},
			{
					"                   ",
					"                   ",
					"                   ",
					"        XPX        ",
					"      MMPPPMM      ",
					"     MPPPPPPPM     ",
					"    MMPPPPPPPMM    ",
					"    MMPPPPPPPMM    ",
					"   MPPPPPPPPPPPM   ",
					"   PPPPPPPPPPPPP   ",
					"   MPPPPPPPPPPPM   ",
					"    MMPPPPPPPMM    ",
					"    MMPPPPPPPMM    ",
					"     MPPPPPPPM     ",
					"      MMPPPMM      ",
					"        XPX        ",
					"                   ",
					"                   ",
					"                   "
			},
			{
					"                   ",
					"                   ",
					"                   ",
					"       MPPPM       ",
					"     MMPPPPPMM     ",
					"    MMPPPPPPPMM    ",
					"    MMPPPPPPPMM    ",
					"   MMPPPPPPPPPMM   ",
					"   PPPPPPPPPPPPP   ",
					"   PPPPPPPPPPPPP   ",
					"   PPPPPPPPPPPPP   ",
					"   MMPPPPPPPPPMM   ",
					"    MMPPPPPPPMM    ",
					"    MMPPPPPPPMM    ",
					"     MMPPPPPMM     ",
					"       MPPPM       ",
					"                   ",
					"                   ",
					"                   "
			},
			{
					"                   ",
					"                   ",
					"                   ",
					"       PPPPP       ",
					"     MMPPPPPMM     ",
					"    MMPPPPPPPMM    ",
					"    MMPPPPPPPMM    ",
					"   PMMMMPPPPPPMP   ",
					"   PPPPPPPPPPPPP   ",
					"   PPPPPPPPPPPPP   ",
					"   PPPPPPPPPPPPP   ",
					"   PPPPPPPPMMMMP   ",
					"    MMPPPPPPPMM    ",
					"    MMPPPPPPPMM    ",
					"     MMPPPPPMM     ",
					"       PPPPP       ",
					"                   ",
					"                   ",
					"                   "
			},
			{
					"                   ",
					"                   ",
					"                   ",
					"       PPPPP       ",
					"     MPPPPPPPM     ",
					"    MMPPPPPPPMM    ",
					"    MMPPPPPPPMM    ",
					"   PMMMMPPPPPPMP   ",
					"   PPPPMMMPPPPPP   ",
					"   PPPPPMMMPPPPP   ",
					"   PPPPPMMMMPPPP   ",
					"   PPPPPPPMMMMMP   ",
					"    MMPPPPPPMMM    ",
					"    MMPPPPPPPMM    ",
					"     MPPPPPPPM     ",
					"       PPPPP       ",
					"                   ",
					"                   ",
					"                   "
			},
			{
					"                   ",
					"                   ",
					"                   ",
					"       PPPPP       ",
					"     MPPPPPPPM     ",
					"    MMPPPPPPPMM    ",
					"    MMPPPPPPPMM    ",
					"   PMMMMPPPPPPMP   ",
					"   PPPMMMMPPPPPP   ",
					"   PPPPMMZMMPPPP   ",
					"   PPPPMMMMMPPPP   ",
					"   PPPPPPMMMMMPP   ",
					"    MMPPPPPPPMM    ",
					"    MMPPPPPPPMM    ",
					"     MPPPPPPMM     ",
					"       PPPPP       ",
					"                   ",
					"                   ",
					"                   "
			},
			{
					"                   ",
					"                   ",
					"                   ",
					"       MPPPM       ",
					"     MMPPPPPMM     ",
					"    MMPPPPPPPMM    ",
					"    MMPPPPPPPMM    ",
					"   MMPPPPPPPPPMM   ",
					"   PPPPPPPPPPPPP   ",
					"   PPPPPPPPPPPPP   ",
					"   PPPPPPPPPPPPP   ",
					"   MMPPPPPPPPPMM   ",
					"    MMPPPPPPPMM    ",
					"    MMPPPPPPPMM    ",
					"     MMPPPPPMM     ",
					"       MPPPM       ",
					"                   ",
					"                   ",
					"                   "
			},
			{
					"                   ",
					"                   ",
					"                   ",
					"                   ",
					"      MMPPPMM      ",
					"     MPPPPPPPM     ",
					"    MMPPPPPPPMM    ",
					"    MMPPPPPPPMM    ",
					"    PPPPPPPPPPP    ",
					"    PPPPPPPPPPP    ",
					"    PPPPPPPPPPP    ",
					"    MMPPPPPPPMM    ",
					"    MMPPPPPPPMM    ",
					"     MPPPPPPPM     ",
					"      MMPPPMM      ",
					"                   ",
					"                   ",
					"                   ",
					"                   "
			},
			{
					"                   ",
					"                   ",
					"                   ",
					"                   ",
					"       MMPMM       ",
					"      MMPPPMM      ",
					"     MMPPPPPMM     ",
					"    MMMPPPPPMMM    ",
					"    MMPPPPPPPMM    ",
					"    PPPPPPPPPPP    ",
					"    MMPPPPPPPMM    ",
					"    MMMPPPPPMMM    ",
					"     MMPPPPPMM     ",
					"      MMPPPMM      ",
					"       MMPMM       ",
					"                   ",
					"                   ",
					"                   ",
					"                   "
			},
			{
					"                   ",
					"                   ",
					"                   ",
					"                   ",
					"                   ",
					"       MMMMM       ",
					"      MMMPMMM      ",
					"     MMMPPPMMM     ",
					"     MMPPPPPMM     ",
					"     MMPPPPPMM     ",
					"     MMPPPPPMM     ",
					"     MMMPPPMMM     ",
					"      MMMPMMM      ",
					"       MMMMM       ",
					"                   ",
					"                   ",
					"                   ",
					"                   ",
					"                   "
			},
			{
					"                   ",
					"                   ",
					"                   ",
					"                   ",
					"                   ",
					"                   ",
					"                   ",
					"       MMMMM       ",
					"       MMMMM       ",
					"       MMMMM       ",
					"       MMMMM       ",
					"       MMMMM       ",
					"                   ",
					"                   ",
					"                   ",
					"                   ",
					"                   ",
					"                   ",
					"                   "
			}
	};
}

