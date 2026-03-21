package com.hbm.items.armor;

import com.hbm.items.weapon.sedna.ItemGunBaseNT.LambdaContext;

import net.minecraft.item.ItemStack;

public interface IPARanged {

	public void clickPrimary(ItemStack stack, LambdaContext ctx);
	public void clickSecondary(ItemStack stack, LambdaContext ctx);
}
