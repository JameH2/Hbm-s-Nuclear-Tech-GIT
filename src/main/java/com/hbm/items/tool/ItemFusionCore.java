package com.hbm.items.tool;

import java.util.List;

import com.hbm.items.armor.ArmorFSB;
import com.hbm.items.armor.ArmorFSBPowered;
import com.hbm.main.NTMSounds;
import com.hbm.util.BobMathUtil;
import com.hbm.util.i18n.I18nUtil;

import api.hbm.energymk2.IBatteryItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.world.World;

public class ItemFusionCore extends Item {

	private int charge;

	public ItemFusionCore(int charge) {
		this.charge = charge;
	}

	@Override
	public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player) {

		if(ArmorFSB.hasFSBArmorIgnoreCharge(player) && player.inventory.armorInventory[2].getItem() instanceof ArmorFSBPowered) {

			for(ItemStack st : player.inventory.armorInventory) {

				if(st == null)
					continue;

				if(st.getItem() instanceof IBatteryItem) {

					long maxcharge = ((IBatteryItem) st.getItem()).getMaxCharge(st);
					long charge = ((IBatteryItem) st.getItem()).getCharge(st);
					long newcharge = Math.min(charge + this.charge, maxcharge);

					((IBatteryItem) st.getItem()).setCharge(st, newcharge);
				}
			}

			stack.stackSize--;

			world.playSoundAtEntity(player, NTMSounds.SUIT_BATTERY, 1.0F, 1.0F);
		}

		return stack;
	}

	@Override
	public void addInformation(ItemStack itemstack, EntityPlayer player, List list, boolean bool) {

		list.add(EnumChatFormatting.YELLOW + I18nUtil.resolveKey("desc.item.fusion_core.charges_worn_armor_pieces_he", BobMathUtil.getShortNumber(charge)));
		list.add(I18nUtil.resolveKey("desc.item.fusion_core.requires_full_electric_worn"));
	}
}
