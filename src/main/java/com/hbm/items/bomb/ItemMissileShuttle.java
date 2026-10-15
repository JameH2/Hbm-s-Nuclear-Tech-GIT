package com.hbm.items.bomb;

import com.hbm.util.i18n.I18nUtil;

import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class ItemMissileShuttle extends Item {

	@Override
	public void addInformation(ItemStack itemstack, EntityPlayer player, List list, boolean bool) {
		list.add(I18nUtil.resolveKey("desc.item.missile_shuttle.tonite_bo_om_gear"));
		list.add(I18nUtil.resolveKey("desc.item.missile_shuttle.james_huffs_leaded_gasoline"));
		list.add(I18nUtil.resolveKey("desc.item.missile_shuttle.goes_insane_richard_spends"));
		list.add(I18nUtil.resolveKey("desc.item.missile_shuttle.entire_budget_broken_png"));
		list.add(I18nUtil.resolveKey("item.missile_shuttle.desc5"));
		list.add(I18nUtil.resolveKey("desc.item.missile_shuttle.water_tanks_rbmk_flow"));
		list.add(I18nUtil.resolveKey("desc.item.missile_shuttle.blowing_entire_base"));
	}
}