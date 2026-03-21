package com.hbm.items.armor;

import com.hbm.items.weapon.sedna.ItemGunBaseNT.LambdaContext;
import com.hbm.render.anim.AnimationEnums.GunAnimation;
import com.hbm.render.anim.BusAnimation;

import net.minecraft.item.ItemStack;

public interface IPAMelee {

	public void setupFirstPerson(ItemStack stack);
	public void renderFirstPerson(ItemStack stack);

	public BusAnimation playAnim(ItemStack stack, GunAnimation type);
	public void orchestra(ItemStack stack, LambdaContext ctx);

	public void clickPrimary(ItemStack stack, LambdaContext ctx);
	public void clickSecondary(ItemStack stack, LambdaContext ctx);
}
