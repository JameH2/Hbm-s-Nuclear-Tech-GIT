package com.hbm.blocks.machine;

import com.hbm.main.MainRegistry;
import com.hbm.tileentity.machine.TileEntityChestInventory;

import cpw.mods.fml.common.network.internal.FMLNetworkHandler;
import net.minecraft.block.Block;
import net.minecraft.block.BlockChest;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class BlockChestInventory extends BlockChest {

	public BlockChestInventory() {
		super(0);
		this.setCreativeTab(null);
	}

	@Override
	public TileEntity createNewTileEntity(World world, int meta) {
		return new TileEntityChestInventory();
	}

	@Override
	public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float hitX, float hitY, float hitZ) {
		if(world.isRemote) {
			return true;
		} else {
			FMLNetworkHandler.openGui(player, MainRegistry.instance, 0, world, x, y, z);
			return true;
		}
	}

	@Override
	public void breakBlock(World world, int x, int y, int z, Block block, int meta) {
		world.removeTileEntity(x, y, z);
	}

	@Override
	public void onNeighborBlockChange(World world, int x, int y, int z, Block from) {

	}

	@Override
	public int getComparatorInputOverride(World world, int x, int y, int z, int meta) {
		return 0;
	}

}
