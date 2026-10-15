package com.hbm.items.armor;

import java.util.List;

import com.hbm.extprop.HbmPlayerProps;
import com.hbm.handler.ArmorModHandler;
import com.hbm.items.ModItems;
import com.hbm.util.i18n.I18nUtil;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.event.entity.living.LivingHurtEvent;

public class ItemModCard extends ItemArmorMod {

	public ItemModCard() {
		super(ArmorModHandler.helmet_only, true, true, false, false);
		this.setCreativeTab(null);
	}

	@Override
	public void addInformation(ItemStack itemstack, EntityPlayer player, List list, boolean bool) {
		if(this == ModItems.card_aos) {
			list.add(EnumChatFormatting.RED + I18nUtil.resolveKey("desc.item.mod_card.top_line"));
			list.add(EnumChatFormatting.RED + I18nUtil.resolveKey("desc.item.mod_card.guns_now_chance_not_consume"));
		}
		if(this == ModItems.card_qos) {
			list.add(EnumChatFormatting.RED + I18nUtil.resolveKey("item.mod_card.desc3"));
			list.add(EnumChatFormatting.RED + I18nUtil.resolveKey("desc.item.mod_card.adds_chance_tank_damage_no"));
		}
		list.add("");
		super.addInformation(itemstack, player, list, bool);
	}

	@Override
	public void addDesc(List list, ItemStack stack, ItemStack armor) {
		list.add(EnumChatFormatting.RED + stack.getDisplayName());
	}

	@Override
	public void modDamage(LivingHurtEvent event, ItemStack armor) {
		if(this == ModItems.card_qos && event.entityLiving.getRNG().nextInt(3) == 0 && event.entityLiving instanceof EntityPlayer) {
			HbmPlayerProps.plink((EntityPlayer) event.entityLiving, "random.break", 0.5F, 1.0F + event.entityLiving.getRNG().nextFloat() * 0.5F);
			event.ammount = 0;
			event.setCanceled(true);
		}
	}
}
