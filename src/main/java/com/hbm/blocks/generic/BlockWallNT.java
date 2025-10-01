package com.hbm.blocks.generic;

import java.util.List;

import com.hbm.blocks.IBlockMulti;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.block.BlockWall;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;

public class BlockWallNT extends BlockWall implements IBlockMulti {

	private final Block[] blocks;

	public BlockWallNT(Block... blocks) {
		super(blocks[0]);
		this.blocks = blocks;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public IIcon getIcon(int side, int meta) {
		return blocks[meta].getBlockTextureFromSide(side);
	}

	@Override
	public String getUnlocalizedName(ItemStack stack) {
		return blocks[rectify(stack.getItemDamage())].getUnlocalizedName() + ".wall";
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	@SideOnly(Side.CLIENT)
	public void getSubBlocks(Item item, CreativeTabs tabs, List list) {
		for(int i = 0; i < getSubCount(); i++) {
			list.add(new ItemStack(item, 1, i));
		}
	}

	@Override
	public int getSubCount() {
		return blocks.length;
	}

}
