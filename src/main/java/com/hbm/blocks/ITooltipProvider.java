package com.hbm.blocks;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import org.lwjgl.input.Keyboard;

import com.hbm.util.i18n.I18nUtil;

import java.util.List;

public interface ITooltipProvider {

	public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean ext);

	public default void addStandardInfo(ItemStack stack, EntityPlayer player, List list, boolean ext) {
		this.addStandardInfo(stack, ((Block) this).getUnlocalizedName() + ".desc", player, list, ext);
	}

	public default void addStandardInfo(ItemStack stack, String name, EntityPlayer player, List list, boolean ext) {

		if(Keyboard.isKeyDown(Keyboard.KEY_LSHIFT)) {
			for(String s : I18nUtil.resolveKeyArray(name)) list.add(EnumChatFormatting.YELLOW + s);
		} else {
			list.add(EnumChatFormatting.DARK_GRAY + "" + EnumChatFormatting.ITALIC +I18nUtil.resolveKey("desc.misc.hold_prefix") +
					EnumChatFormatting.YELLOW + "" + EnumChatFormatting.ITALIC + I18nUtil.resolveKey("desc.misc.lshift_key") +
					EnumChatFormatting.DARK_GRAY + "" + EnumChatFormatting.ITALIC + I18nUtil.resolveKey("desc.misc.more_info_suffix"));
		}
	}

	public default EnumRarity getRarity(ItemStack stack) {
		return EnumRarity.common;
	}
}
