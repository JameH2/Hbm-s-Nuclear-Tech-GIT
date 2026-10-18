package com.hbm.inventory.container;

import com.hbm.util.InventoryUtil;

import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;

/** Keeps shift-click insertion subject to the same slot checks as manual insertion. */
public abstract class ContainerWithUpgradeSlots extends Container {

	@Override
	protected boolean mergeItemStack(ItemStack stack, int start, int end, boolean reverse) {
		return InventoryUtil.mergeItemStack(this.inventorySlots, stack, start, end, reverse);
	}
}
