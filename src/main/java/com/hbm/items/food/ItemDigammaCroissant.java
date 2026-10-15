package com.hbm.items.food;

import java.util.List;

import com.hbm.items.ModItems;
import com.hbm.main.MainRegistry;
import com.hbm.util.ContaminationUtil;
import com.hbm.util.i18n.I18nUtil;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.world.World;

public class ItemDigammaCroissant extends ItemFood {

	public ItemDigammaCroissant(int hunger, float saturation, boolean isWolfFood) {
		super(hunger, saturation, isWolfFood);
		this.setAlwaysEdible();
	}

	@Override
	public void onUpdate(ItemStack stack, World world, Entity entity, int i, boolean b) {
		if(!world.isRemote && entity instanceof EntityPlayer) {
			ContaminationUtil.applyDigammaData(entity, 3.33F / 20F);
		}
	}

	@Override
	protected void onFoodEaten(ItemStack stack, World world, EntityPlayer player) {
		if(!world.isRemote) {
			player.triggerAchievement(MainRegistry.digammaCroissant);
			player.setHealth(0.0F);
		}
	}

	@Override
	public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean bool) {
		list.add(EnumChatFormatting.DARK_RED + I18nUtil.resolveKey("item.digamma_croissant.desc1"));
		super.addInformation(stack, player, list, bool);
	}
}
