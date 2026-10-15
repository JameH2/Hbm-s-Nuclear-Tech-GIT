package com.hbm.items.block;

import java.util.List;

import com.hbm.blocks.ModBlocks;
import com.hbm.blocks.generic.BlockOreFluid;
import com.hbm.blocks.generic.RedBarrel;
import com.hbm.util.i18n.I18nUtil;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.ItemStack;

public class ItemBlockLore extends ItemBlockBase {

	public ItemBlockLore(Block p_i45328_1_) {
		super(p_i45328_1_);
	}

	@Override
	public void addInformation(ItemStack itemstack, EntityPlayer player, List list, boolean bool) {
		super.addInformation(itemstack, player, list, bool);

		// Check for a generic localisation so we can add descriptions to them regular ass blocks
		String unloc = this.getUnlocalizedName() + ".desc";
		String loc = I18nUtil.resolveKey(unloc);

		if(!unloc.equals(loc)) {
			String[] locs = loc.split("\\$");

			for(String s : locs) {
				list.add(s);
			}
		}

		if(this.field_150939_a instanceof RedBarrel) {
			list.add(I18nUtil.resolveKey("item.block_lore.desc1"));
		}

		if(this.field_150939_a == ModBlocks.meteor_battery) {
			list.add(I18nUtil.resolveKey("item.block_lore.desc2"));
		}

		if(this.field_150939_a == ModBlocks.gravel_diamond) {
			list.add(I18nUtil.resolveKey("item.block_lore.desc3"));
			list.add(I18nUtil.resolveKey("item.block_lore.desc4"));
			list.add("");
			list.add(I18nUtil.resolveKey("item.block_lore.desc5"));
			list.add(I18nUtil.resolveKey("item.block_lore.desc6"));
			list.add(I18nUtil.resolveKey("item.block_lore.desc7"));
			list.add(I18nUtil.resolveKey("item.block_lore.desc8"));
			list.add("");
			list.add(I18nUtil.resolveKey("item.block_lore.desc9"));
			list.add(I18nUtil.resolveKey("item.block_lore.desc10"));
			list.add(I18nUtil.resolveKey("item.block_lore.desc11"));
			list.add(I18nUtil.resolveKey("item.block_lore.desc12"));
			list.add(I18nUtil.resolveKey("item.block_lore.desc13"));
			list.add(I18nUtil.resolveKey("item.block_lore.desc14"));
			list.add("");
			list.add(I18nUtil.resolveKey("item.line.desc01"));
		}
	}

	@Override
	public EnumRarity getRarity(ItemStack stack) {

		if(this.field_150939_a == ModBlocks.gravel_diamond)
			return EnumRarity.rare;

		if(this.field_150939_a == ModBlocks.block_euphemium || this.field_150939_a == ModBlocks.block_euphemium_cluster)
			return EnumRarity.epic;

		return EnumRarity.common;
	}

}
