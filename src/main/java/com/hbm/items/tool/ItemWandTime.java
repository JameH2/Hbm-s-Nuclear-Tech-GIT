package com.hbm.items.tool;

import java.util.List;

import com.hbm.util.AstronomyUtil;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.world.World;

public class ItemWandTime extends Item {
	
	@Override
	public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean bool) {
		list.add("Creative-only item");
		list.add(EnumChatFormatting.ITALIC + "\"Wibbly wobbly, timey-wimey... stuff\"");
		list.add("Probably doesn't work on servers");
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

		player.addChatMessage(new ChatComponentText("Celestial Time Multiplier set to: " + AstronomyUtil.TIME_MULTIPLIER));

		return true;
	}

}
