package com.hbm.items.armor;

import java.util.List;

import com.hbm.handler.ArmorModHandler;
import com.hbm.util.i18n.I18nUtil;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.Vec3;

public class ItemModFlippers extends ItemArmorMod {

	// :o_  flipows

	public ItemModFlippers() {
		super(ArmorModHandler.boots_only, false, false, false, true);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public void addInformation(ItemStack itemstack, EntityPlayer player, List list, boolean bool) {
		list.add(EnumChatFormatting.BLUE + I18nUtil.resolveKey("desc.item.mod_flippers.increases_swim_speed"));
		list.add("");
		super.addInformation(itemstack, player, list, bool);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public void addDesc(List list, ItemStack stack, ItemStack armor) {
		list.add(EnumChatFormatting.DARK_PURPLE + I18nUtil.resolveKey("desc.item.mod_flippers.increased_swim_speed", stack.getDisplayName()));
	}

	@Override
	public void modUpdate(EntityLivingBase entity, ItemStack armor) {
		if(entity instanceof EntityPlayer) {
			EntityPlayer player = (EntityPlayer) entity;

			// Skip if we already have armor that applies swim speed buff
			if(armor.getItem() instanceof ArmorEnvsuit) {
				if(ArmorFSB.hasFSBArmor(player)) return;
			}

			if(entity.isInWater()) {
				double mo = 0.08 * player.moveForward;
				Vec3 vec = entity.getLookVec();
				vec.xCoord *= mo;
				vec.yCoord *= mo;
				vec.zCoord *= mo;

				entity.motionX += vec.xCoord;
				entity.motionY += vec.yCoord;
				entity.motionZ += vec.zCoord;
			}
		}
	}

}
