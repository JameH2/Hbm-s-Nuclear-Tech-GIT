package com.hbm.items.machine;

import java.util.List;

import com.hbm.items.ISatChip;
import com.hbm.items.ModItems;
import com.hbm.util.i18n.I18nUtil;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;

public class ItemSatChip extends Item implements ISatChip {

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public void addInformation(ItemStack itemstack, EntityPlayer player, List list, boolean bool) {
		list.add(EnumChatFormatting.AQUA + I18nUtil.resolveKey("satchip.frequency") + ": " + getFreq(itemstack));
	}

}
