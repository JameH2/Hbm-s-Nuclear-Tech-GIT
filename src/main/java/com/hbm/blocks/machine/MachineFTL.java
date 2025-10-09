package com.hbm.blocks.machine;

import com.hbm.blocks.BlockDummyable;
import com.hbm.tileentity.TileEntityProxyCombo;

import net.minecraft.block.material.Material;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class MachineFTL extends BlockDummyable {

	public MachineFTL(Material mat) {
		super(mat);
	}

	@Override
	public TileEntity createNewTileEntity(World world, int meta) {
		if(meta >= 12) return null;
		if(meta >= 6) return new TileEntityProxyCombo(true, true, true);
		return null;
	}

	@Override
	public int[] getDimensions() {
		return new int[] { 2, 0, 1, 1, 1, 1 };
	}

	@Override
	public int getOffset() {
		return 1;
	}


	//TODO: add a model and remove this

	@Override
	public int getRenderType() {
		return 0;
	}

	@Override
	public boolean isOpaqueCube() {
		return true;
	}

	@Override
	public boolean renderAsNormalBlock() {
		return true;
	}

	//ODOT

}
