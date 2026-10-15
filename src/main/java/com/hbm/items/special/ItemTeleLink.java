package com.hbm.items.special;

import java.util.List;

import com.hbm.tileentity.machine.TileEntityMachineTeleporter;
import com.hbm.util.i18n.I18nUtil;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.world.World;
import net.minecraft.util.ChatComponentTranslation;

public class ItemTeleLink extends Item {

	@Override
	public boolean onItemUse(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side, float hitX, float hitY, float hitZ) {

		if(!player.isSneaking() && !world.isRemote) {

			TileEntity te = world.getTileEntity(x, y, z);

			if(!(te instanceof TileEntityMachineTeleporter)) {

				if(stack.stackTagCompound == null) {
					stack.stackTagCompound = new NBTTagCompound();
				}

				stack.stackTagCompound.setInteger("x", x);
				stack.stackTagCompound.setInteger("y", y);
				stack.stackTagCompound.setInteger("z", z);
				stack.stackTagCompound.setInteger("dim", player.dimension);
				world.playSoundAtEntity(player, "hbm:item.techBleep", 1.0F, 1.0F);
				player.addChatMessage(new ChatComponentTranslation("chat.tele_link.telelink_teleporter_exit", x, y, z).setChatStyle(new net.minecraft.util.ChatStyle().setColor(EnumChatFormatting.AQUA)));
				player.swingItem();

				return true;

			} else {

				if(!stack.hasTagCompound()) {
					world.playSoundAtEntity(player, "hbm:item.techBoop", 1.0F, 1.0F);
					player.addChatMessage(new ChatComponentTranslation("chat.tele_link.telelink_no_destination").setChatStyle(new net.minecraft.util.ChatStyle().setColor(EnumChatFormatting.RED)));
					return false;
				}

				int x1 = stack.stackTagCompound.getInteger("x");
				int y1 = stack.stackTagCompound.getInteger("y");
				int z1 = stack.stackTagCompound.getInteger("z");
				int dim = stack.stackTagCompound.getInteger("dim");

				TileEntityMachineTeleporter tele = (TileEntityMachineTeleporter) te;

				tele.targetX = x1;
				tele.targetY = y1;
				tele.targetZ = z1;
				tele.targetDim = dim;

				tele.markDirty();
				world.playSoundAtEntity(player, "hbm:item.techBleep", 1.0F, 1.0F);
				player.addChatMessage(new ChatComponentTranslation("chat.tele_link.3").setChatStyle(new net.minecraft.util.ChatStyle().setColor(EnumChatFormatting.AQUA)));
				player.swingItem();
				return true;
			}
		}

		return false;
	}

	@Override
	public void addInformation(ItemStack itemstack, EntityPlayer player, List list, boolean bool) {
		if (itemstack.stackTagCompound != null) {
			list.add(I18nUtil.resolveKey("item.tele_link.desc1", itemstack.stackTagCompound.getInteger("x")));
			list.add(I18nUtil.resolveKey("item.tele_link.desc2", itemstack.stackTagCompound.getInteger("y")));
			list.add(I18nUtil.resolveKey("item.tele_link.desc3", itemstack.stackTagCompound.getInteger("z")));
			list.add(I18nUtil.resolveKey("item.tele_link.desc4", itemstack.stackTagCompound.getInteger("dim")));
		} else {
			list.add(EnumChatFormatting.RED + I18nUtil.resolveKey("desc.item.tele_link.select_exit_location_first"));
		}
	}

}
