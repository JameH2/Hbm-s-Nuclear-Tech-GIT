package com.hbm.blocks.machine;

import java.util.Random;

import com.hbm.items.ModItems;
import com.hbm.tileentity.machine.storage.TileEntityCombatDropPod;

import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class CombatDropPod extends BlockContainer {

	public CombatDropPod(Material p_i45386_1_) {
		super(p_i45386_1_);
	}

	@Override
	public TileEntity createNewTileEntity(World p_149915_1_, int p_149915_2_) {
		return new TileEntityCombatDropPod();
	}
	
	@Override
	public int getRenderType() {
		return -1;
	}
	
	@Override
	public boolean isOpaqueCube() {
		return false;
	}
	
	@Override
	public boolean renderAsNormalBlock() {
		return false;
	}

	@Override
	public Item getItemDropped(int meta, Random rand, int fortune) {
		return ModItems.ingot_steel;
	}

	@Override public int quantityDropped(Random rand) {
		return 16;
	}

}
