package com.hbm.inventory.container;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntityChest;

public class ContainerChestInventory extends Container {

	private TileEntityChest chest;

	public ContainerChestInventory(IInventory playerInv, TileEntityChest chest) {
		this.chest = chest;
		chest.openInventory();
		int numRows = chest.getSizeInventory() / 9;
        int offset = (numRows - 4) * 18;

		for(int row = 0; row < numRows; ++row) {
			for(int col = 0; col < 9; ++col) {
				int index = col + row * 9;
				if(index >= playerInv.getSizeInventory()) break;
				this.addSlotToContainer(new Slot(playerInv, index, 8 + col * 18, 18 + row * 18));
			}
		}

		for(int row = 0; row < 3; ++row) {
			for(int col = 0; col < 9; ++col) {
				this.addSlotToContainer(new Slot(playerInv, col + row * 9 + 9, 8 + col * 18, 103 + row * 18 + offset));
			}
		}

		for(int col = 0; col < 9; ++col) {
			this.addSlotToContainer(new Slot(playerInv, col, 8 + col * 18, 161 + offset));
		}
	}

	@Override
	public boolean canInteractWith(EntityPlayer player) {
		return true;
	}

	@Override
	public ItemStack transferStackInSlot(EntityPlayer player, int index) {
		return null;
	}

	@Override
	public void onContainerClosed(EntityPlayer player) {
		super.onContainerClosed(player);
		chest.closeInventory();
	}

}
