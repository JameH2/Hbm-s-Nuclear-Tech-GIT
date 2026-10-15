package com.hbm.items.armor;

import java.util.List;

import com.hbm.handler.ArmorModHandler;
import com.hbm.util.i18n.I18nUtil;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;

public class ItemModObsidian extends ItemArmorMod {

	public ItemModObsidian() {
		super(ArmorModHandler.cladding, true, true, true, true);
	}

	@Override
	public void addInformation(ItemStack itemstack, EntityPlayer player, List list, boolean bool) {
		list.add(EnumChatFormatting.DARK_PURPLE + I18nUtil.resolveKey("desc.item.mod_obsidian.makes_dropped_armor"));
		list.add("");
		super.addInformation(itemstack, player, list, bool);
	}

	@Override
	public void addDesc(List list, ItemStack stack, ItemStack armor) {
		list.add(EnumChatFormatting.DARK_PURPLE + I18nUtil.resolveKey("desc.item.mod_obsidian.item_indestructible", stack.getDisplayName()));
	}
}
