package com.hbm.hazard.type;

import java.util.List;

import com.hbm.hazard.modifier.HazardModifier;
import com.hbm.util.ContaminationUtil;
import com.hbm.util.i18n.I18nUtil;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;

public class HazardTypeDigamma extends HazardTypeBase {

	@Override
	public void onUpdate(EntityLivingBase target, float level, ItemStack stack) {
		ContaminationUtil.applyDigammaData(target, level / 20F);
	}

	@Override
	public void updateEntity(EntityItem item, float level) { }

	@Override
	public void addHazardInformation(EntityPlayer player, List list, float level, ItemStack stack, List<HazardModifier> modifiers) {

		level = HazardModifier.evalAllModifiers(stack, player, level, modifiers);

		float d = (float)(Math.floor(level * 10000F)) / 10F;
		list.add(EnumChatFormatting.RED + "[" + I18nUtil.resolveKey("trait.digamma") + "]");
		list.add((EnumChatFormatting.DARK_RED) + I18nUtil.resolveKey("desc.item.hazard_digamma.sm_drx", d));

		if(stack.stackSize > 1) {
			list.add((EnumChatFormatting.DARK_RED) + I18nUtil.resolveKey("desc.item.hazard_digamma.stack", ((Math.floor(level * 10000F * stack.stackSize) / 10F) + I18nUtil.resolveKey("gui.hazard_digamma.mdrx"))));
		}
	}

}
