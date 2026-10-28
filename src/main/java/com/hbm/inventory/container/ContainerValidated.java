package com.hbm.inventory.container;

import com.hbm.util.InventoryUtil;

import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;

public abstract class ContainerValidated extends Container {

	@Override
	protected boolean mergeItemStack(ItemStack stack, int start, int end, boolean reverse) {
		return InventoryUtil.mergeItemStack(this.inventorySlots, stack, start, end, reverse);
	}
}
