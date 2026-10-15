package com.hbm.items.tool;

import baubles.api.BaubleType;
import baubles.api.IBauble;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;

public class ItemKey extends ItemKeyPin implements IBauble {

	@Override
	public BaubleType getBaubleType(ItemStack stack) {
		return BaubleType.BELT;
	}

	@Override
	public void onWornTick(ItemStack stack, EntityLivingBase entity) { }

	@Override
	public void onEquipped(ItemStack stack, EntityLivingBase entity) { }

	@Override
	public void onUnequipped(ItemStack stack, EntityLivingBase entity) { }

	@Override
	public boolean canEquip(ItemStack stack, EntityLivingBase entity) {
		return true;
	}

	@Override
	public boolean canUnequip(ItemStack stack, EntityLivingBase entity) {
		return true;
	}

}
