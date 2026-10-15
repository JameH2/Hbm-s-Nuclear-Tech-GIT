package com.hbm.items.special;

import java.util.List;

import com.hbm.items.ModItems;
import com.hbm.main.MainRegistry;
import com.hbm.util.i18n.I18nUtil;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class ItemAMSCore extends Item {

	long powerBase;
	int heatBase;
	int fuelBase;

	public ItemAMSCore(long powerBase, int heatBase, int fuelBase) {
		this.powerBase = powerBase;
		this.heatBase = heatBase;
		this.fuelBase = fuelBase;
	}

	@Override
	public void addInformation(ItemStack itemstack, EntityPlayer player, List list, boolean bool) {

		if (this == ModItems.ams_core_sing) {
			list.add(I18nUtil.resolveKey("item.ams_core.desc1"));
			list.add(I18nUtil.resolveKey("item.ams_core.desc2"));
			list.add(I18nUtil.resolveKey("item.ams_core.desc3"));
			list.add(I18nUtil.resolveKey("item.ams_core.desc4"));
			list.add(I18nUtil.resolveKey("item.ams_core.desc5"));
			list.add(I18nUtil.resolveKey("item.ams_core.desc6"));
		}

		if (this == ModItems.ams_core_wormhole) {
			list.add(I18nUtil.resolveKey("item.ams_core.desc7"));
			list.add(I18nUtil.resolveKey("item.ams_core.desc8"));
			list.add(I18nUtil.resolveKey("item.ams_core.desc9"));
			list.add(I18nUtil.resolveKey("item.ams_core.desc10"));
			list.add(I18nUtil.resolveKey("item.ams_core.desc11"));
			list.add(I18nUtil.resolveKey("item.ams_core.desc12"));
			list.add(I18nUtil.resolveKey("item.ams_core.desc13"));
			list.add(I18nUtil.resolveKey("item.ams_core.desc14"));
			list.add(I18nUtil.resolveKey("item.ams_core.desc15"));
			list.add(I18nUtil.resolveKey("item.ams_core.desc16"));
		}

		if (this == ModItems.ams_core_eyeofharmony) {
			list.add(I18nUtil.resolveKey("item.ams_core.desc17"));
			list.add(I18nUtil.resolveKey("item.ams_core.desc18"));
			list.add(I18nUtil.resolveKey("item.ams_core.desc19"));
			list.add(I18nUtil.resolveKey("item.ams_core.desc20"));
			list.add(I18nUtil.resolveKey("item.ams_core.desc21"));
			list.add(I18nUtil.resolveKey("item.ams_core.desc22"));
			list.add(I18nUtil.resolveKey("item.ams_core.desc23"));
		}

		if (this == ModItems.ams_core_thingy) {
			if(MainRegistry.polaroidID == 11) {
				list.add(I18nUtil.resolveKey("item.ams_core.desc24"));
			} else {
				list.add("...");
				list.add("...");
				list.add(I18nUtil.resolveKey("item.ams_core.desc25"));
				list.add(I18nUtil.resolveKey("item.ams_core.desc26"));
				list.add(I18nUtil.resolveKey("item.ams_core.desc27"));
				list.add(I18nUtil.resolveKey("item.ams_core.desc28"));
				list.add(I18nUtil.resolveKey("item.ams_core.desc29"));
				list.add(I18nUtil.resolveKey("item.ams_core.desc30"));
				list.add(I18nUtil.resolveKey("item.ams_core.desc31"));
				list.add(I18nUtil.resolveKey("item.ams_core.desc32"));
			}
		}
	}

    @Override
	public EnumRarity getRarity(ItemStack p_77613_1_) {

    	if(this == ModItems.ams_core_thingy)
    	{
    		return EnumRarity.epic;
    	}

    	return EnumRarity.uncommon;
    }

    @Override
	@SideOnly(Side.CLIENT)
    public boolean hasEffect(ItemStack p_77636_1_)
    {
    	if(this == ModItems.ams_core_thingy && MainRegistry.polaroidID == 11)
    	{
    		return true;
    	}

    	return false;
    }

    public static long getPowerBase(ItemStack stack) {
		if(stack == null || !(stack.getItem() instanceof ItemAMSCore))
			return 0;
		return ((ItemAMSCore)stack.getItem()).powerBase;
    }

    public static int getHeatBase(ItemStack stack) {
		if(stack == null || !(stack.getItem() instanceof ItemAMSCore))
			return 0;
		return ((ItemAMSCore)stack.getItem()).heatBase;
    }

    public static int getFuelBase(ItemStack stack) {
		if(stack == null || !(stack.getItem() instanceof ItemAMSCore))
			return 0;
		return ((ItemAMSCore)stack.getItem()).fuelBase;
    }
}
