package com.hbm.items.tool;

import java.util.List;

import com.hbm.util.AstronomyUtil;
import com.hbm.util.i18n.I18nUtil;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.world.World;
import net.minecraft.util.ChatComponentTranslation;

public class ItemWandTime extends Item {

	@Override
	public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean bool) {
		list.add(I18nUtil.resolveKey("desc.item.wand.creative_item"));
		list.add(EnumChatFormatting.ITALIC + I18nUtil.resolveKey("desc.item.wand_time.wibbly_wobbly_timey_wimey_stuff"));
		list.add(I18nUtil.resolveKey("item.wand_time.desc2"));
	}

	@Override
	public boolean onItemUse(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side, float fx, float fy, float fz) {
		if(world.isRemote) return true;

		if(player.isSneaking()) {
			AstronomyUtil.TIME_MULTIPLIER /= 2;
			if(AstronomyUtil.TIME_MULTIPLIER < 1) AstronomyUtil.TIME_MULTIPLIER = 1;
		} else {
			AstronomyUtil.TIME_MULTIPLIER *= 2;
		}

		player.addChatMessage(new ChatComponentTranslation("chat.wand_time.celestial_time_multiplier", AstronomyUtil.TIME_MULTIPLIER));

		return true;
	}

}
