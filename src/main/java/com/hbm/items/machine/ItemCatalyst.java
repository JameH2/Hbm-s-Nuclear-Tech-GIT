package com.hbm.items.machine;

import com.hbm.util.i18n.I18nUtil;

import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class ItemCatalyst extends Item {

	int color;
	long powerAbs;
	float powerMod;
	float heatMod;
	float fuelMod;

	public ItemCatalyst(int color) {
		this.color = color;
		this.powerAbs = 0;
		this.powerMod = 1.0F;
		this.heatMod = 1.0F;
		this.fuelMod = 1.0F;
	}

	public ItemCatalyst(int color, long powerAbs, float powerMod, float heatMod, float fuelMod) {
		this.color = color;
		this.powerAbs = powerAbs;
		this.powerMod = powerMod;
		this.heatMod = heatMod;
		this.fuelMod = fuelMod;
	}

	public int getColor() {
		return this.color;
	}

	@Override
	public void addInformation(ItemStack itemstack, EntityPlayer player, List list, boolean bool)
	{
		list.add(I18nUtil.resolveKey("desc.item.catalyst.adds_spice_core"));
		list.add(I18nUtil.resolveKey("desc.item.catalyst.look_those_colors"));
	}

	public static long getPowerAbs(ItemStack stack) {
		if(stack == null || !(stack.getItem() instanceof ItemCatalyst))
			return 0;
		return ((ItemCatalyst)stack.getItem()).powerAbs;
	}

	public static float getPowerMod(ItemStack stack) {
		if(stack == null || !(stack.getItem() instanceof ItemCatalyst))
			return 0;
		return ((ItemCatalyst)stack.getItem()).powerMod;
	}

	public static float getHeatMod(ItemStack stack) {
		if(stack == null || !(stack.getItem() instanceof ItemCatalyst))
			return 0;
		return ((ItemCatalyst)stack.getItem()).heatMod;
	}

	public static float getFuelMod(ItemStack stack) {
		if(stack == null || !(stack.getItem() instanceof ItemCatalyst))
			return 0;
		return ((ItemCatalyst)stack.getItem()).fuelMod;
	}

}
