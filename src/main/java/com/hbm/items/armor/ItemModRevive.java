package com.hbm.items.armor;

import java.util.List;

import com.hbm.handler.ArmorModHandler;
import com.hbm.items.ModItems;
import com.hbm.util.i18n.I18nUtil;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;

public class ItemModRevive extends ItemArmorMod {

	public ItemModRevive(int durability) {
		super(ArmorModHandler.extra, false, false, true, false);
		this.setMaxDamage(durability);
	}

	@Override
	public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean bool) {

		if(this == ModItems.scrumpy) {
			list.add(EnumChatFormatting.GOLD + I18nUtil.resolveKey("item.mod_revive.question"));
			list.add(EnumChatFormatting.RED + I18nUtil.resolveKey("item.mod_revive.answer"));
		}
		if(this == ModItems.wild_p) {
			list.add(EnumChatFormatting.DARK_GRAY + I18nUtil.resolveKey("item.mod_revive.acronym", EnumChatFormatting.RED, EnumChatFormatting.DARK_GRAY, EnumChatFormatting.RED));
		}

		/*list.add((EnumChatFormatting.ITALIC) + I18nUtil.resolveKey("item.mod_revive.headline"));
		list.add(("" + EnumChatFormatting.RED + EnumChatFormatting.BOLD) + I18nUtil.resolveKey("item.mod_revive.title"));
		list.add("");
		for(String line : I18nUtil.resolveKeyArray("item.mod_revive.news.desc")) {
			list.add((EnumChatFormatting.ITALIC) + line);
		}*/

		list.add("");
		list.add(EnumChatFormatting.GOLD + "" + I18nUtil.resolveKey("item.mod_revive.remaining", (stack.getMaxDamage() - stack.getItemDamage())));
		list.add("");
		super.addInformation(stack, player, list, bool);
	}

	@Override
	public void addDesc(List list, ItemStack stack, ItemStack armor) {

		list.add(EnumChatFormatting.GOLD + "  " + I18nUtil.resolveKey("item.mod_revive.summary", stack.getDisplayName(), (stack.getMaxDamage() - stack.getItemDamage())));
	}
}
