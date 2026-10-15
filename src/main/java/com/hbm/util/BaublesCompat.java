package com.hbm.util;

import baubles.api.BaublesApi;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public final class BaublesCompat {

	private BaublesCompat() { }

	public static boolean hasItem(EntityPlayer player, Item item) {
		if(player == null || item == null) return false;
		IInventory baubles = BaublesApi.getBaubles(player);
		if(baubles == null) return false;

		for(int i = 0; i < baubles.getSizeInventory(); i++) {
			ItemStack stack = baubles.getStackInSlot(i);
			if(stack != null && stack.getItem() == item) return true;
		}
		return false;
	}

	public static IInventory includeBaubles(IInventory inventory) {
		if(inventory instanceof CombinedInventory || !(inventory instanceof InventoryPlayer)) return inventory;

		IInventory baubles = BaublesApi.getBaubles(((InventoryPlayer) inventory).player);
		return baubles == null ? inventory : new CombinedInventory(inventory, baubles);
	}

	public static int getCombinedSlot(EntityPlayer player, ItemStack stack) {
		IInventory baubles = BaublesApi.getBaubles(player);
		if(baubles != null) {
			for(int i = 0; i < baubles.getSizeInventory(); i++) {
				if(baubles.getStackInSlot(i) == stack) return player.inventory.getSizeInventory() + i;
			}
		}
		return player.inventory.currentItem;
	}
}
