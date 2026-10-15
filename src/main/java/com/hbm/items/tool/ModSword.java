package com.hbm.items.tool;

import java.util.List;

import com.hbm.items.ModItems;
import com.hbm.util.i18n.I18nUtil;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;

public class ModSword extends ItemSword {

	public ModSword(ToolMaterial mat) {
		super(mat);
	}

	@Override
	public void addInformation(ItemStack itemstack, EntityPlayer player, List list, boolean bool) {
		if(this == ModItems.pipe_lead)
			list.add(I18nUtil.resolveKey("item.mod_sword.desc1"));

		if(this == ModItems.reer_graar) {
			list.add(I18nUtil.resolveKey("desc.item.mod_sword.call_now"));
			list.add(I18nUtil.resolveKey("desc.item.mod_sword.zx7_infinite"));
		}
	}
}
