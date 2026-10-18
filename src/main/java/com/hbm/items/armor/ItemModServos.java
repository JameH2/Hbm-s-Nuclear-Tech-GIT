package com.hbm.items.armor;

import java.util.List;

import com.google.common.collect.Multimap;
import com.hbm.handler.ArmorModHandler;
import com.hbm.items.ModItems;
import com.hbm.util.i18n.I18nUtil;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.EnumChatFormatting;

public class ItemModServos extends ItemArmorMod {

	public ItemModServos() {
		super(ArmorModHandler.servos, false, true, true, false);
	}

	@Override
	public void addInformation(ItemStack itemstack, EntityPlayer player, List list, boolean bool) {

		if(this == ModItems.servo_set) {
			list.add(EnumChatFormatting.DARK_PURPLE + I18nUtil.resolveKey("item.mod_servos.chestplate", "I", 50));
			list.add(EnumChatFormatting.DARK_PURPLE + I18nUtil.resolveKey("item.mod_servos.leggings", 25, "II"));
		}
		if(this == ModItems.servo_set_desh) {
			list.add(EnumChatFormatting.DARK_PURPLE + I18nUtil.resolveKey("item.mod_servos.chestplate", "III", 150));
			list.add(EnumChatFormatting.DARK_PURPLE + I18nUtil.resolveKey("item.mod_servos.leggings", 50, "III"));
		}

		list.add("");
		super.addInformation(itemstack, player, list, bool);
	}

	@Override
	public void addDesc(List list, ItemStack stack, ItemStack armor) {

		ItemArmor item = (ItemArmor)armor.getItem();

		if(item.armorType == 1) {

			if(this == ModItems.servo_set) {
				list.add(EnumChatFormatting.DARK_PURPLE + I18nUtil.resolveKey("item.mod_servos.chestplate_summary", stack.getDisplayName(), "I", 50));
			}
			if(this == ModItems.servo_set_desh) {
				list.add(EnumChatFormatting.DARK_PURPLE + I18nUtil.resolveKey("item.mod_servos.chestplate_summary", stack.getDisplayName(), "III", 150));
			}
		}

		if(item.armorType == 2) {

			if(this == ModItems.servo_set) {
				list.add(EnumChatFormatting.DARK_PURPLE + I18nUtil.resolveKey("item.mod_servos.leggings_summary", stack.getDisplayName(), 25, "II"));
			}
			if(this == ModItems.servo_set_desh) {
				list.add(EnumChatFormatting.DARK_PURPLE + I18nUtil.resolveKey("item.mod_servos.leggings_summary", stack.getDisplayName(), 50, "III"));
			}
		}
	}

	public void modUpdate(EntityLivingBase entity, ItemStack armor) {

		ItemArmor item = (ItemArmor)armor.getItem();

		if(item.armorType == 1) {

			if(this == ModItems.servo_set) {
				entity.addPotionEffect(new PotionEffect(Potion.digSpeed.id, 60, 0));
			}
			if(this == ModItems.servo_set_desh) {
				entity.addPotionEffect(new PotionEffect(Potion.digSpeed.id, 60, 2));
			}
		}

		if(item.armorType == 2) {

			if(this == ModItems.servo_set) {
				entity.addPotionEffect(new PotionEffect(Potion.jump.id, 60, 1));
			}
			if(this == ModItems.servo_set_desh) {
				entity.addPotionEffect(new PotionEffect(Potion.jump.id, 60, 2));
			}
		}
	}

	@Override
	public Multimap getModifiers(ItemStack armor) {
		Multimap multimap = super.getAttributeModifiers(armor);

		ItemArmor item = (ItemArmor)armor.getItem();

		if(item.armorType == 1) {
			if(this == ModItems.servo_set)
				multimap.put(SharedMonsterAttributes.attackDamage.getAttributeUnlocalizedName(),
						new AttributeModifier(ArmorModHandler.UUIDs[((ItemArmor)armor.getItem()).armorType], "NTM Armor Mod Servos", 0.5, 2));

			if(this == ModItems.servo_set_desh)
				multimap.put(SharedMonsterAttributes.attackDamage.getAttributeUnlocalizedName(),
						new AttributeModifier(ArmorModHandler.UUIDs[((ItemArmor)armor.getItem()).armorType], "NTM Armor Mod Servos", 1.5, 2));
		}

		if(item.armorType == 2) {
			if(this == ModItems.servo_set)
				multimap.put(SharedMonsterAttributes.movementSpeed.getAttributeUnlocalizedName(),
						new AttributeModifier(ArmorModHandler.UUIDs[((ItemArmor)armor.getItem()).armorType], "NTM Armor Mod Servos", 0.25, 2));

			if(this == ModItems.servo_set_desh)
				multimap.put(SharedMonsterAttributes.movementSpeed.getAttributeUnlocalizedName(),
						new AttributeModifier(ArmorModHandler.UUIDs[((ItemArmor)armor.getItem()).armorType], "NTM Armor Mod Servos", 0.5, 2));
		}

		return multimap;
	}
}
