package com.hbm.inventory.container;

import com.hbm.inventory.SlotDeprecated;
import com.hbm.inventory.SlotTakeOnly;
import com.hbm.tileentity.machine.TileEntityMachineWarController;
import com.hbm.tileentity.machine.oil.TileEntityMachineVacuumDistill;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ICrafting;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class ContainerMachineWarController extends ContainerBase {

	private TileEntityMachineWarController shill;
	
	public ContainerMachineWarController(InventoryPlayer invPlayer, TileEntityMachineWarController tedf) {
		super(invPlayer, tedf);
		shill = tedf;
		
		// 4 Slots (shifted upwards by modifying y values)
		this.addSlotToContainer(new Slot(tedf, 0, 6, 52)); 
		this.addSlotToContainer(new Slot(tedf, 1, 29, 11)); 
		this.addSlotToContainer(new Slot(tedf, 2, 49, 11));  
		this.addSlotToContainer(new Slot(tedf, 3, 29, 31));  
		
		playerInv(invPlayer,8,103,162);
	}
	
	@Override
	public ItemStack transferStackInSlot(EntityPlayer player, int index) {
		ItemStack itemstack = null;
		Slot slot = (Slot) this.inventorySlots.get(index);

		if(slot != null && slot.getHasStack()) {
			ItemStack itemstack1 = slot.getStack();
			itemstack = itemstack1.copy();

			// TileEntity Slots to Player Inventory
			if(index < 4) {
				if(!this.mergeItemStack(itemstack1, 4, this.inventorySlots.size(), true)) {
					return null;
				}
			} else if(!this.mergeItemStack(itemstack1, 0, 4, false)) {
				return null;
			}

			if(itemstack1.stackSize == 0) {
				slot.putStack(null);
			} else {
				slot.onSlotChanged();
			}
		}

		return itemstack;
	}
	
	@Override
	public boolean canInteractWith(EntityPlayer player) {
		return shill.isUseableByPlayer(player);
	}
}