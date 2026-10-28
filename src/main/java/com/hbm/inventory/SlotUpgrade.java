package com.hbm.inventory;

import com.hbm.items.machine.ItemMachineUpgrade;
import com.hbm.tileentity.IUpgradeInfoProvider;

import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class SlotUpgrade extends Slot {
	private final Item[] acceptedItems;

	public SlotUpgrade(IInventory inventory, int i, int j, int k, Item... acceptedItems) {
		super(inventory, i, j, k);
		this.acceptedItems = acceptedItems;
	}

	@Override
	public boolean isItemValid(ItemStack stack) {
		if(stack == null || !(stack.getItem() instanceof ItemMachineUpgrade)) return false;
		if(acceptedItems.length > 0) {
			for(Item item : acceptedItems) if(stack.getItem() == item) return true;
			return false;
		}
		if(inventory instanceof IUpgradeInfoProvider) {
			return ((IUpgradeInfoProvider) inventory).isUpgradeValid(stack);
		}
		return true;
	}

	@Override
	public void onSlotChange(ItemStack sta1, ItemStack sta2) {
		super.onSlotChange(sta1, sta2);
	}
}
