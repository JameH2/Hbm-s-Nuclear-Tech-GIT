package com.hbm.items.tool;

import com.hbm.main.MainRegistry;
import com.hbm.tileentity.network.IDroneLinkable;
import com.hbm.util.ChatBuilder;
import com.hbm.util.fauxpointtwelve.BlockPos;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.world.World;
import com.hbm.util.i18n.I18nUtil;

public class ItemDroneLinker extends Item {

	@Override
	public boolean onItemUse(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side, float fX, float fY, float fZ) {

		TileEntity tile = world.getTileEntity(x, y, z);

		if(tile instanceof IDroneLinkable) {

			if(!world.isRemote) {
				if(!stack.hasTagCompound()) {
					stack.stackTagCompound = new NBTTagCompound();
					stack.stackTagCompound.setInteger("x", x);
					stack.stackTagCompound.setInteger("y", y);
					stack.stackTagCompound.setInteger("z", z);

					player.addChatMessage(ChatBuilder.start("[").color(EnumChatFormatting.DARK_AQUA)
							.nextTranslation(this.getUnlocalizedName() + ".name").color(EnumChatFormatting.DARK_AQUA)
							.next("] ").color(EnumChatFormatting.DARK_AQUA)
							.nextTranslation("chat.drone_linker.initial_position").color(EnumChatFormatting.AQUA).flush());

				} else {

					int tx = stack.stackTagCompound.getInteger("x");
					int ty = stack.stackTagCompound.getInteger("y");
					int tz = stack.stackTagCompound.getInteger("z");

					TileEntity prev = world.getTileEntity(tx, ty, tz);

					if(prev instanceof IDroneLinkable) {

						BlockPos dest = ((IDroneLinkable) tile).getPoint();
						((IDroneLinkable) prev).setNextTarget(dest.getX(), dest.getY(), dest.getZ());

						player.addChatMessage(ChatBuilder.start("[").color(EnumChatFormatting.DARK_AQUA)
								.nextTranslation(this.getUnlocalizedName() + ".name").color(EnumChatFormatting.DARK_AQUA)
								.next("] ").color(EnumChatFormatting.DARK_AQUA)
								.nextTranslation("chat.drone_linker.2").color(EnumChatFormatting.AQUA).flush());
					} else {
						player.addChatMessage(ChatBuilder.start("[").color(EnumChatFormatting.DARK_AQUA)
								.nextTranslation(this.getUnlocalizedName() + ".name").color(EnumChatFormatting.DARK_AQUA)
								.next("] ").color(EnumChatFormatting.DARK_AQUA)
								.nextTranslation("chat.drone_linker.previous_link_lost").color(EnumChatFormatting.RED).flush());
					}

					stack.stackTagCompound.setInteger("x", x);
					stack.stackTagCompound.setInteger("y", y);
					stack.stackTagCompound.setInteger("z", z);
				}
			}

			return true;
		}

		return false;
	}

	@Override
	public void onUpdate(ItemStack stack, World world, Entity entity, int slot, boolean inhand) {

		if(world.isRemote && inhand) {
			if(stack.hasTagCompound()) {
				int x = stack.stackTagCompound.getInteger("x");
				int y = stack.stackTagCompound.getInteger("y");
				int z = stack.stackTagCompound.getInteger("z");
				MainRegistry.proxy.displayTooltip(I18nUtil.resolveKey("gui.drone_linker.prev_pos") + x + " / " + y + " / " + z, MainRegistry.proxy.ID_DRONE);
			}
		}
	}

	@Override
	public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player) {

		if(!world.isRemote && stack.hasTagCompound()) {
			stack.stackTagCompound = null;

			player.addChatMessage(ChatBuilder.start("[").color(EnumChatFormatting.DARK_AQUA)
					.nextTranslation(this.getUnlocalizedName() + ".name").color(EnumChatFormatting.DARK_AQUA)
					.next("] ").color(EnumChatFormatting.DARK_AQUA)
					.nextTranslation("chat.drone_linker.position_cleared").color(EnumChatFormatting.GREEN).flush());
		}

		return stack;
	}
}
