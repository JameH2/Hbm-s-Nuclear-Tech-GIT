package com.hbm.blocks;

import java.util.Random;

import com.hbm.items.ModItems;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.BlockLeaves;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class BlockRubberLeaves extends BlockLeaves {

	public BlockRubberLeaves() {
		super();
	}

	@Override
	public Item getItemDropped(int p_149650_1_, Random p_149650_2_, int p_149650_3_) {
		if(this == ModBlocks.pet_leaves) {
			return ModItems.leaf_pet;
		}
		return ModItems.leaf_rubber;
	}

	@Override
	public boolean isOpaqueCube() {
		return false;
	}

	@Override
	protected boolean canSilkHarvest() {
		return false;
	}

	@Override
	public void dropBlockAsItemWithChance(World world, int x, int y, int z, int metadata, float chance, int fortune) {
		super.dropBlockAsItemWithChance(world, x, y, z, metadata, chance, fortune);

		if(!world.isRemote) {
			Random rand = world.rand;
			if(this == ModBlocks.rubber_leaves && rand.nextFloat() < 0.3F) {
				this.dropBlockAsItem(world, x, y, z, new ItemStack(ModItems.leaf_rubber));

				if(rand.nextFloat() < 0.5F) {
					this.dropBlockAsItem(world, x, y, z, new ItemStack(ModBlocks.sapling_pvc, 1, 1));
				}
			}
			if(this == ModBlocks.pet_leaves && rand.nextFloat() < 0.3F) {
				this.dropBlockAsItem(world, x, y, z, new ItemStack(ModItems.leaf_pet));

				if(rand.nextFloat() < 0.5F) {
					this.dropBlockAsItem(world, x, y, z, new ItemStack(ModBlocks.sapling_pvc, 1, 0));
				}
			}
		}
	}

	// Resetting some leaf stuff back to `Block` implementation, as we don't use biome colours, and we like our leaves dense
	@Override
	@SideOnly(Side.CLIENT)
	public int getBlockColor() {
		return 0xFFFFFF;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public int getRenderColor(int meta) {
		return 0xFFFFFF;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public int colorMultiplier(IBlockAccess world, int x, int y, int z) {
		return 0xFFFFFF;
	}

	@Override
	public IIcon getIcon(int side, int meta) {
		return this.blockIcon;
	}

	@Override
	public String[] func_150125_e() {
		// we don't use `ItemLeaves` so just give nothing
		return null;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public boolean shouldSideBeRendered(IBlockAccess world, int x, int y, int z, int side) {
		return side == 0 && this.minY > 0.0D ? true : (side == 1 && this.maxY < 1.0D ? true : (side == 2 && this.minZ > 0.0D ? true : (side == 3 && this.maxZ < 1.0D ? true : (side == 4 && this.minX > 0.0D ? true : (side == 5 && this.maxX < 1.0D ? true : !world.getBlock(x, y, z).isOpaqueCube())))));
	}

}
