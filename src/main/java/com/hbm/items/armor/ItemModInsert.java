package com.hbm.items.armor;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.Multimap;
import com.hbm.extprop.HbmLivingProps;
import com.hbm.handler.ArmorModHandler;
import com.hbm.items.ModItems;
import com.hbm.util.i18n.I18nUtil;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.event.entity.living.LivingHurtEvent;

public class ItemModInsert extends ItemArmorMod {

	float damageMod;
	float projectileMod;
	float explosionMod;
	float speed;

	public boolean corrosionProtection = false;

	public ItemModInsert(int durability, float damageMod, float projectileMod, float explosionMod, float speed) {
		super(ArmorModHandler.kevlar, false, true, false, false);
		this.damageMod = damageMod;
		this.projectileMod = projectileMod;
		this.explosionMod = explosionMod;
		this.speed = speed;
		this.setMaxDamage(durability);
	}

	public ItemModInsert withCorrosionProtection() {
		corrosionProtection = true;
		return this;
	}

	@Override
	public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean bool) {

		if(corrosionProtection)
			list.add(EnumChatFormatting.GOLD + I18nUtil.resolveKey("item.mod_insert.desc1"));
		if(damageMod != 1F)
			list.add(EnumChatFormatting.RED + I18nUtil.resolveKey("item.mod_insert.desc2", (damageMod < 1 ? "-" : "+"), Math.abs(Math.round((1F - damageMod) * 100))));
		if(projectileMod != 1F)
			list.add(EnumChatFormatting.YELLOW + I18nUtil.resolveKey("item.mod_insert.desc3", Math.round((1F - projectileMod) * 100)));
		if(explosionMod != 1F)
			list.add(EnumChatFormatting.YELLOW + I18nUtil.resolveKey("item.mod_insert.desc4", Math.round((1F - explosionMod) * 100)));
		if(speed != 1F)
			list.add(EnumChatFormatting.BLUE + I18nUtil.resolveKey("item.mod_insert.desc5", Math.round((1F - speed) * 100)));

		if(this == ModItems.insert_polonium)
			list.add(EnumChatFormatting.DARK_RED + I18nUtil.resolveKey("item.mod_insert.desc6"));

		list.add(I18nUtil.resolveKey("item.mod_insert.desc7", (stack.getMaxDamage() - stack.getItemDamage()), stack.getMaxDamage()));

		list.add("");
		super.addInformation(stack, player, list, bool);
	}

	@Override
	public void addDesc(List list, ItemStack stack, ItemStack armor) {

		List<String> desc = new ArrayList();

		if(damageMod != 1F)
			desc.add(I18nUtil.resolveKey("item.mod_insert.desc8", (damageMod < 1 ? "-" : "+"), Math.abs(Math.round((1F - damageMod) * 100))));
		if(projectileMod != 1F)
			desc.add(I18nUtil.resolveKey("item.mod_insert.desc9", Math.round((1F - projectileMod) * 100)));
		if(explosionMod != 1F)
			desc.add(I18nUtil.resolveKey("item.mod_insert.desc10", Math.round((1F - explosionMod) * 100)));
		if(explosionMod != 1F)
			desc.add(I18nUtil.resolveKey("item.mod_insert.desc5", Math.round((1F - speed) * 100)));

		if(this == ModItems.insert_polonium)
			desc.add(I18nUtil.resolveKey("item.mod_insert.desc6"));

		String join = String.join(" / ", desc);

		list.add(EnumChatFormatting.DARK_PURPLE + I18nUtil.resolveKey("item.mod_insert.desc11", stack.getDisplayName(), join, (stack.getMaxDamage() - stack.getItemDamage())));
	}

	@Override
	public void modDamage(LivingHurtEvent event, ItemStack armor) {

		event.ammount *= damageMod;

		if(event.source.isProjectile())
			event.ammount *= projectileMod;

		if(event.source.isExplosion())
			event.ammount *= explosionMod;

		ItemStack insert = ArmorModHandler.pryMods(armor)[ArmorModHandler.kevlar];

		if(insert == null)
			return;

		insert.setItemDamage(insert.getItemDamage() + 1);

		if(!event.entity.worldObj.isRemote && this == ModItems.insert_era) {
			event.entity.worldObj.newExplosion(event.entity, event.entity.posX, event.entity.posY - event.entity.yOffset + event.entity.height * 0.5, event.entity.posZ, 0.05F, false, false);
		}

		if(insert.getItemDamage() >= insert.getMaxDamage()) {
			ArmorModHandler.removeMod(armor, ArmorModHandler.kevlar);
		} else {
			ArmorModHandler.applyMod(armor, insert);
		}
	}

	@Override
	public void modUpdate(EntityLivingBase entity, ItemStack armor) {

		if(!entity.worldObj.isRemote && this == ModItems.insert_polonium) {
			HbmLivingProps.incrementRadiation(entity, 100F);
		}
	}

	@Override
	public Multimap getModifiers(ItemStack armor) {

		if(speed == 1)
			return null;

		Multimap multimap = super.getAttributeModifiers(armor);

		multimap.put(SharedMonsterAttributes.movementSpeed.getAttributeUnlocalizedName(),
				new AttributeModifier(ArmorModHandler.UUIDs[((ItemArmor)armor.getItem()).armorType], "NTM Armor Mod Speed", -1F + speed, 2));

		return multimap;
	}
}
