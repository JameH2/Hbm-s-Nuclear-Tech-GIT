package com.hbm.inventory.container;

import api.hbm.energymk2.IBatteryItem;
import com.hbm.inventory.SlotNonRetarded;
import com.hbm.items.ModItems;
import com.hbm.util.InventoryUtil;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class ContainerTurretRailgun extends ContainerBase {
	public ContainerTurretRailgun(InventoryPlayer invPlayer, IInventory tedf) {
		super(invPlayer, tedf);

		// Battery
		this.addSlotToContainer(new SlotNonRetarded(tedf, 0, 153, 78));
		// Ammo slots
		this.addSlots(tedf, 1, 62, 104, 1, 3);

		this.playerInv(invPlayer, 8, 140);
	}

	@Override
	public ItemStack transferStackInSlot(EntityPlayer player, int index) {
		ItemStack slotOriginal = null;
		Slot slot = (Slot) this.inventorySlots.get(index);

		if(slot != null && slot.getHasStack()) {
			ItemStack slotStack = slot.getStack();
			slotOriginal = slotStack.copy();

			if(index <= tile.getSizeInventory() - 1) {
				if(!this.mergeItemStack(slotStack, tile.getSizeInventory(), this.inventorySlots.size(), true)) {
					return null;
				}
			} else {
				if(slotOriginal.getItem() instanceof IBatteryItem || slotOriginal.getItem() == ModItems.battery_creative) {
					if (!this.mergeItemStack(slotStack, 0, 1, false)) return null;
				} else {
					if(!InventoryUtil.mergeItemStack(this.inventorySlots, slotStack, 1, 6, false)) return null;
				}
			}

			if(slotStack.stackSize == 0) {
				slot.putStack(null);
			} else {
				slot.onSlotChanged();
			}

			slot.onPickupFromSlot(player, slotStack);
		}

		return slotOriginal;
	}
}
