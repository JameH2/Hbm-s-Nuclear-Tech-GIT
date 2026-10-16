package com.hbm.items.bomb;

import com.hbm.util.i18n.I18nUtil;

import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class ItemMissileShuttle extends Item {

	@Override
	public void addInformation(ItemStack itemstack, EntityPlayer player, List list, boolean bool) {
		for(String line : I18nUtil.resolveKeyArray("item.missile_shuttle.desc")) {
			list.add(line);
		}
	}
}