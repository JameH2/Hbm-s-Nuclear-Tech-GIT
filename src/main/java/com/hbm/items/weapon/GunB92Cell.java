package com.hbm.items.weapon;

import java.util.List;

import com.hbm.items.ModItems;
import com.hbm.util.i18n.I18nUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

public class GunB92Cell extends Item {


	@Override
	public void onUpdate(ItemStack stack, World world, Entity entity, int i, boolean b) {
		if(entity instanceof EntityPlayer && getPower(stack) < 25) {
			EntityPlayer player = (EntityPlayer) entity;

			for(int j = 0; j < player.inventory.mainInventory.length; j++) {
				if(player.inventory.mainInventory[j] != null && player.inventory.mainInventory[j].getItem() == ModItems.gun_b92) {
					int p = getPower(player.inventory.mainInventory[j]);
					if(p > 1) {
						setPower(player.inventory.mainInventory[j], p - 1);
						setPower(stack, getPower(stack) + 1);
						if(getPower(stack) == 25)
							stack.setItemDamage(1);
						return;
					}
				}
			}
		}
	}

	@Override
	public void addInformation(ItemStack itemstack, EntityPlayer player, List list, boolean bool) {

		for(String line : I18nUtil.resolveKeyArray("item.gun_b92_cell.desc1")) {
			list.add(line);
		}
		list.add("");
		list.add(I18nUtil.resolveKey("item.gun_b92_cell.desc6", getPower(itemstack)));
	}

	private static int getPower(ItemStack stack) {
		if (stack.stackTagCompound == null) {
			stack.stackTagCompound = new NBTTagCompound();
			return 0;
		}

		return stack.stackTagCompound.getInteger("energy");

	}

	private static void setPower(ItemStack stack, int i) {
		if (stack.stackTagCompound == null) {
			stack.stackTagCompound = new NBTTagCompound();
		}

		stack.stackTagCompound.setInteger("energy", i);

	}

	public static ItemStack getFullCell() {
		ItemStack stack = new ItemStack(ModItems.gun_b92_ammo, 1, 1);
		setPower(stack, 25);
		return stack.copy();
	}

}
