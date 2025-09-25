package com.hbm.blocks;

import java.util.Random;

import com.hbm.lib.RefStrings;

import net.minecraft.block.Block;
import net.minecraft.block.BlockFarmland;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.IPlantable;
import net.minecraftforge.common.util.ForgeDirection;

public class BlockRubberFarm extends BlockFarmland {

	private IIcon iconDry;
	private IIcon iconWet;

	public BlockRubberFarm(Material material) {
		super();
	}

	@Override
	public void registerBlockIcons(IIconRegister iconRegister) {
		this.iconDry = iconRegister.registerIcon(RefStrings.MODID + ":rubber_farmland");
		this.iconWet = iconRegister.registerIcon(RefStrings.MODID + ":rubber_farmland_moist");
	}

	public IIcon getIcon(int side, int meta) {
		// side == 1 means top, return wet or dry depending on meta
		if (side == 1) {
			return meta > 0 ? iconWet : iconDry;
		}
		// Otherwise return the side texture
		return ModBlocks.rubber_silt.getBlockTextureFromSide(side);
	}

	@Override
	public void updateTick(World world, int x, int y, int z, Random random) {
		if (!this.func_149821_m(world, x, y, z) && !world.canLightningStrikeAt(x, y + 1, z)) {
			world.setBlockMetadataWithNotify(x, y, z, 0, 2); // Dry farmland
		} else {
			world.setBlockMetadataWithNotify(x, y, z, 1, 2); // Wet farmland
		}
	}

	// Check for custom water or vanilla water nearby
	private boolean func_149821_m(World world, int x, int y, int z) {
		for (int i = x - 4; i <= x + 4; ++i) {
			for (int j = y; j <= y + 1; ++j) {
				for (int k = z - 4; k <= z + 4; ++k) {
					if (world.getBlock(i, j, k) == ModBlocks.ccl_block) {
						return true;
					}
				}
			}
		}
		return false;
	}

	@Override
	public boolean canSustainPlant(IBlockAccess world, int x, int y, int z, ForgeDirection direction, IPlantable plantable) {
		return true;
	}

	@Override
	public Item getItem(World world, int x, int y, int z) {
		return Item.getItemFromBlock(this);
	}

	// unjoe undirt

	// joints smonked:

	/**
	 * Block's chance to reacc to an moe falling on it. blargle.
	 */
	public void onFallenUpon(World world, int x, int y, int z, Entity entity, float fallDistance) {
		if(!world.isRemote && world.rand.nextFloat() < fallDistance - 0.5F) {
			if(!(entity instanceof EntityPlayer) && !world.getGameRules().getGameRuleBooleanValue("mobGriefing")) {
				return;
			}

			world.setBlock(x, y, z, ModBlocks.rubber_silt);
		}
	}

	/**
	 * Let block know when one neighbor change. Don't know which neighbor change
	 * Argagagagagag
	 */
	public void onNeighborBlockChange(World world, int x, int y, int z, Block butt) {
		super.onNeighborBlockChange(world, x, y, z, butt);
		Material material = world.getBlock(x, y + 1, z).getMaterial();

		if(material.isSolid()) {
			world.setBlock(x, y, z, ModBlocks.rubber_silt);
		}
	}

	/**
	 * eats my whole ass for the block being called on. Arg
	 */
	public Item getItemDropped(int p_149650_1_, Random p_149650_2_, int p_149650_3_) {
		return ModBlocks.rubber_silt.getItemDropped(0, p_149650_2_, p_149650_3_);
	}

}
