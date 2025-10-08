package com.hbm.tileentity.machine;

import com.hbm.inventory.container.ContainerChestInventory;
import com.hbm.inventory.gui.GUIChestInventory;
import com.hbm.tileentity.IGUIProvider;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.world.World;

public class TileEntityChestInventory extends TileEntityChest implements IGUIProvider {

	@Override
	public Container provideContainer(int ID, EntityPlayer player, World world, int x, int y, int z) {
		return new ContainerChestInventory(player.inventory, this);
	}

	@Override
	public Object provideGUI(int ID, EntityPlayer player, World world, int x, int y, int z) {
		return new GUIChestInventory(player.inventory, this);
	}

	@Override
	public int getSizeInventory() {
		return 54;
	}

	@Override
	public void updateEntity() {
		// prevent unsetting current user count
		int prevNumPlayersUsing = numPlayersUsing;
		super.updateEntity();
		numPlayersUsing = prevNumPlayersUsing;
	}

}
