package com.hbm.util;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;

/** Presents two inventories as one contiguous inventory. */
public class CombinedInventory implements IInventory {

	private final IInventory primary;
	private final IInventory secondary;

	public CombinedInventory(IInventory primary, IInventory secondary) {
		this.primary = primary;
		this.secondary = secondary;
	}

	private IInventory inventoryFor(int slot) {
		return slot < primary.getSizeInventory() ? primary : secondary;
	}

	private int localSlot(int slot) {
		return slot < primary.getSizeInventory() ? slot : slot - primary.getSizeInventory();
	}

	@Override public int getSizeInventory() { return primary.getSizeInventory() + secondary.getSizeInventory(); }
	@Override public ItemStack getStackInSlot(int slot) { return inventoryFor(slot).getStackInSlot(localSlot(slot)); }
	@Override public ItemStack decrStackSize(int slot, int amount) { return inventoryFor(slot).decrStackSize(localSlot(slot), amount); }
	@Override public ItemStack getStackInSlotOnClosing(int slot) { return inventoryFor(slot).getStackInSlotOnClosing(localSlot(slot)); }
	@Override public void setInventorySlotContents(int slot, ItemStack stack) { inventoryFor(slot).setInventorySlotContents(localSlot(slot), stack); }
	@Override public String getInventoryName() { return primary.getInventoryName(); }
	@Override public boolean hasCustomInventoryName() { return primary.hasCustomInventoryName(); }
	@Override public int getInventoryStackLimit() { return primary.getInventoryStackLimit(); }
	@Override public void markDirty() { primary.markDirty(); secondary.markDirty(); }
	@Override public boolean isUseableByPlayer(EntityPlayer player) { return primary.isUseableByPlayer(player) && secondary.isUseableByPlayer(player); }
	@Override public void openInventory() { primary.openInventory(); secondary.openInventory(); }
	@Override public void closeInventory() { primary.closeInventory(); secondary.closeInventory(); }
	@Override public boolean isItemValidForSlot(int slot, ItemStack stack) { return inventoryFor(slot).isItemValidForSlot(localSlot(slot), stack); }
}
