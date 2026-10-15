package com.hbm.items.tool;

import java.util.List;

import com.hbm.items.ModItems;
import com.hbm.tileentity.machine.TileEntityLockableBase;
import com.hbm.util.i18n.I18nUtil;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.world.World;
import net.minecraft.util.ChatComponentTranslation;

public class ItemCounterfeitKeys extends Item {

	@Override
	public boolean onItemUse(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int b, float f0, float f1, float f2) {
		TileEntity te = world.getTileEntity(x, y, z);

		if(te instanceof TileEntityLockableBase) {

			TileEntityLockableBase locked = (TileEntityLockableBase) te;

			if(locked.isLocked() && locked.cheesable) {
				ItemStack st = new ItemStack(ModItems.key_fake);
				ItemKeyPin.setPins(st, locked.getPins());

				player.inventory.setInventorySlotContents(player.inventory.currentItem, st.copy());

				if(!player.inventory.addItemStackToInventory(st.copy())) {
					player.dropPlayerItemWithRandomChoice(st.copy(), false);
				}

				player.inventoryContainer.detectAndSendChanges();

				player.swingItem();

				return true;
			} else if(!locked.cheesable){
				player.addChatMessage(new ChatComponentTranslation("chat.counterfeit_keys.1").setChatStyle(new net.minecraft.util.ChatStyle().setColor(EnumChatFormatting.LIGHT_PURPLE)));
				player.addChatMessage(new ChatComponentTranslation("chat.counterfeit_keys.2").setChatStyle(new net.minecraft.util.ChatStyle().setColor(EnumChatFormatting.LIGHT_PURPLE)));
			}
		}

		return false;
	}

	@Override
	public void addInformation(ItemStack itemstack, EntityPlayer player, List list, boolean bool) {
		list.add(I18nUtil.resolveKey("item.counterfeit_keys.desc1"));
	}

}
