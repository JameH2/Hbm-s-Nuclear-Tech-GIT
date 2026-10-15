package com.hbm.creativetabs;

import java.util.List;

import com.hbm.items.ModItems;
import com.hbm.items.weapon.ItemCustomMissile;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.util.EnumChatFormatting;
import com.hbm.util.i18n.I18nUtil;

public class MissileTab extends CreativeTabs {

	public MissileTab(int p_i1853_1_, String p_i1853_2_) {
		super(p_i1853_1_, p_i1853_2_);
	}

	@Override
	public Item getTabIconItem() {

		if(ModItems.missile_nuclear != null)
		{
			return ModItems.missile_nuclear;
		}

		return Items.iron_pickaxe;
	}

	@Override
    @SideOnly(Side.CLIENT)
	public void displayAllReleventItems(List list) {

		super.displayAllReleventItems(list);

		list.add(I18nUtil.setItemName(ItemCustomMissile.buildMissile(ModItems.mp_chip_3,
				ModItems.mp_warhead_10_he,
				ModItems.mp_fuselage_10_kerosene,
				ModItems.mp_stability_10_flat,
				ModItems.mp_thruster_10_kerosene), "missile.preset.lil_bub"));

		list.add(I18nUtil.setItemName(ItemCustomMissile.buildMissile(ModItems.mp_chip_3,
				ModItems.mp_warhead_10_incendiary,
				ModItems.mp_fuselage_10_long_solid,
				ModItems.mp_stability_10_space,
				ModItems.mp_thruster_10_solid), "missile.preset.long_boy"));

		list.add(I18nUtil.setItemName(ItemCustomMissile.buildMissile(ModItems.mp_chip_3,
				ModItems.mp_warhead_10_nuclear,
				ModItems.mp_fuselage_10_15_kerosene,
				ModItems.mp_stability_15_flat,
				ModItems.mp_thruster_15_kerosene), "missile.preset.uncle_kim"));

		list.add(I18nUtil.setItemName(ItemCustomMissile.buildMissile(ModItems.mp_chip_3,
				ModItems.mp_warhead_10_nuclear_large,
				ModItems.mp_fuselage_10_15_balefire,
				ModItems.mp_stability_15_flat,
				ModItems.mp_thruster_15_balefire_large), "missile.preset.trotty_toy_rocket"));

		list.add(I18nUtil.setItemName(ItemCustomMissile.buildMissile(ModItems.mp_chip_3,
				ModItems.mp_warhead_15_nuclear_shark,
				ModItems.mp_fuselage_15_kerosene_camo,
				ModItems.mp_stability_15_thin,
				ModItems.mp_thruster_15_kerosene_triple), "missile.preset.stealthy_shark"));

		list.add(I18nUtil.setItemName(ItemCustomMissile.buildMissile(ModItems.mp_chip_3,
				ModItems.mp_warhead_15_he,
				ModItems.mp_fuselage_15_kerosene_polite,
				ModItems.mp_stability_15_thin,
				ModItems.mp_thruster_15_kerosene_dual), "missile.preset.polite_lad"));

		list.add(I18nUtil.setItemName(ItemCustomMissile.buildMissile(ModItems.mp_chip_3,
				ModItems.mp_warhead_15_n2,
				ModItems.mp_fuselage_15_solid_desh,
				ModItems.mp_stability_15_thin,
				ModItems.mp_thruster_15_solid_hexdecuple), "missile.preset.nerv_leftover_missile"));

		list.add(I18nUtil.setItemName(ItemCustomMissile.buildMissile(ModItems.mp_chip_5,
				ModItems.mp_warhead_15_boxcar,
				ModItems.mp_fuselage_15_kerosene_blackjack,
				ModItems.mp_stability_15_thin,
				ModItems.mp_thruster_15_kerosene), "missile.preset.auntie_blackjack"));

		list.add(I18nUtil.setItemName(ItemCustomMissile.buildMissile(ModItems.mp_chip_4,
				ModItems.mp_warhead_15_balefire,
				ModItems.mp_fuselage_15_20_kerosene_magnusson,
				null,
				ModItems.mp_thruster_20_kerosene), "missile.preset.hightower_missile"));
	}
}
