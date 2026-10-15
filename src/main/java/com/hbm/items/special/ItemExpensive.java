package com.hbm.items.special;

import java.util.List;

import com.hbm.items.ItemEnumMulti;
import com.hbm.items.ItemEnums.EnumExpensiveType;
import com.hbm.util.i18n.I18nUtil;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;

public class ItemExpensive extends ItemEnumMulti {

	public ItemExpensive() {
		super(EnumExpensiveType.class, true, true);
	}

	@Override
	public void addInformation(ItemStack itemstack, EntityPlayer player, List list, boolean bool) {
		list.add(EnumChatFormatting.RED + I18nUtil.resolveKey("desc.item.expensive.expensive_mode_item"));
	}
}
