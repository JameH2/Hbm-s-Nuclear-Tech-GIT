package com.hbm.items.armor;

import java.util.List;

import com.hbm.handler.ArmorModHandler;
import com.hbm.util.i18n.I18nUtil;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.event.entity.living.LivingHurtEvent;

public class ItemModPolish extends ItemArmorMod {

	public ItemModPolish() {
		super(ArmorModHandler.extra, true, true, true, true);
	}

	@Override
	public void addInformation(ItemStack itemstack, EntityPlayer player, List list, boolean bool) {

		list.add(EnumChatFormatting.BLUE + I18nUtil.resolveKey("desc.item.mod_polish.chance_nullify_damage"));
		list.add("");
		super.addInformation(itemstack, player, list, bool);
	}

	@Override
	public void addDesc(List list, ItemStack stack, ItemStack armor) {
		list.add(EnumChatFormatting.BLUE + I18nUtil.resolveKey("item.mod_polish.desc2", stack.getDisplayName()));
	}

	@Override
	public void modDamage(LivingHurtEvent event, ItemStack armor) {

		if(event.entity.worldObj.rand.nextInt(20) == 0)
			event.ammount = 0;
	}
}
