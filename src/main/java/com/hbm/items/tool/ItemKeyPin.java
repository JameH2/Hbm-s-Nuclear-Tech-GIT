package com.hbm.items.tool;

import java.util.List;

import com.hbm.items.ModItems;
import com.hbm.util.i18n.I18nUtil;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

public class ItemKeyPin extends Item {

	@Override
	public void addInformation(ItemStack itemstack, EntityPlayer player, List list, boolean bool)
	{
		if(getPins(itemstack) != 0)
			list.add(I18nUtil.resolveKey("desc.item.key_pin.pin_configuration", getPins(itemstack)));
		else
			list.add(I18nUtil.resolveKey("desc.item.key_pin.pins_not"));

		if(this == ModItems.key_fake) {

			list.add("");
			list.add(I18nUtil.resolveKey("desc.item.key_pin.pins_neither_changed_nor_copied"));
		}
	}

	public static int getPins(ItemStack stack) {
		if(stack.stackTagCompound == null) {
			stack.stackTagCompound = new NBTTagCompound();
			return 0;
		}
		return stack.stackTagCompound.getInteger("pins");
	}

	public static void setPins(ItemStack stack, int i) {
		if(stack.stackTagCompound == null) {
			stack.stackTagCompound = new NBTTagCompound();
		}
		stack.stackTagCompound.setInteger("pins", i);
	}

	public boolean canTransfer() {
		return this != ModItems.key_fake;
	}
}
