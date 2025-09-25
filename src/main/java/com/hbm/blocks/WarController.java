package com.hbm.blocks;

import com.hbm.blocks.machine.BlockMachineBase;
import com.hbm.tileentity.machine.TileEntityMachineDiesel;
import com.hbm.tileentity.machine.TileEntityMachineWarController;

import net.minecraft.block.material.Material;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class WarController extends BlockMachineBase {

	public WarController() {
		super(Material.iron, 0);
	}

	@Override
	public TileEntity createNewTileEntity(World world, int meta) {
		return new TileEntityMachineWarController();
	}
	

}
