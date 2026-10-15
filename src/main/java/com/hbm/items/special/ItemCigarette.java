package com.hbm.items.special;

import java.util.List;

import com.hbm.extprop.HbmLivingProps;
import com.hbm.handler.threading.PacketThreading;
import com.hbm.items.ModItems;
import com.hbm.main.MainRegistry;
import com.hbm.packet.toclient.AuxParticlePacketNT;
import com.hbm.potion.HbmPotion;
import com.hbm.util.i18n.I18nUtil;

import cpw.mods.fml.common.network.NetworkRegistry.TargetPoint;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumAction;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IIcon;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.world.World;

public class ItemCigarette extends Item {

	@SideOnly(Side.CLIENT)
	private IIcon[] jointIcons;

	@Override
	@SideOnly(Side.CLIENT)
	public void registerIcons(IIconRegister reg) {
		super.registerIcons(reg);

		if (this == ModItems.joint) {
			jointIcons = new IIcon[8];
			for (int i = 0; i < 8; i++) {
				jointIcons[i] = reg.registerIcon(this.getIconString() + "_lit" + (i + 1));
			}
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public IIcon getIcon(ItemStack stack, int renderPass, EntityPlayer player, ItemStack usingItem, int useRemaining) {
		if (this == ModItems.joint && jointIcons != null && usingItem != null && usingItem.getItem() == this
				&& useRemaining > 0) {
			int stage = 8 - (int) Math.ceil((double) useRemaining * 8.0 / (double) this.getMaxItemUseDuration(stack));
			if (stage < 0)
				stage = 0;
			if (stage > 7)
				stage = 7;
			return jointIcons[stage];
		}
		return super.getIcon(stack, renderPass, player, usingItem, useRemaining);
	}

	@Override
	public EnumAction getItemUseAction(ItemStack stack) {
		return EnumAction.bow;
	}

	@Override
	public int getMaxItemUseDuration(ItemStack stack) {
		return 30;
	}

	@Override
	public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player) {
		player.setItemInUse(stack, this.getMaxItemUseDuration(stack));
		return stack;
	}

	@Override
	public ItemStack onEaten(ItemStack stack, World world, EntityPlayer player) {
		if (!player.capabilities.isCreativeMode) {
			--stack.stackSize;
		}

		if (!world.isRemote) {

			if (this == ModItems.cigarette) {
				HbmLivingProps.incrementBlackLung(player, 2000);
				HbmLivingProps.incrementAsbestos(player, 2000);
				HbmLivingProps.incrementRadiation(player, 100F);

				ItemStack helmet = player.getEquipmentInSlot(4);
				if (helmet != null && helmet.getItem() == ModItems.no9) {
					player.triggerAchievement(MainRegistry.achNo9);
				}
			}

			if (this == ModItems.crackpipe) {
				HbmLivingProps.incrementBlackLung(player, 500);
				player.addPotionEffect(new PotionEffect(Potion.confusion.id, 200, 0));
				player.heal(10F);
			}

			if (this == ModItems.joint) {
				HbmLivingProps.incrementBlackLung(player, 500);
				player.addPotionEffect(new PotionEffect(HbmPotion.high.id, 15 * 20, 0));
				player.addPotionEffect(new PotionEffect(Potion.confusion.id, 15 * 20, 0));
				player.addPotionEffect(new PotionEffect(Potion.moveSlowdown.id, 15 * 20, 0));
			}

			world.playSoundEffect(player.posX, player.posY, player.posZ, "hbm:player.cough", 1.0F, 1.0F);

			NBTTagCompound nbt = new NBTTagCompound();
			nbt.setString("type", "vomit");
			nbt.setString("mode", "smoke");
			nbt.setInteger("count", 30);
			nbt.setInteger("entity", player.getEntityId());
			if (this == ModItems.joint) {
				nbt.setBoolean("rainbow", true);
			}
			PacketThreading.createAllAroundThreadedPacket(new AuxParticlePacketNT(nbt, 0, 0, 0),
					new TargetPoint(player.dimension, player.posX, player.posY, player.posZ, 25));
		}

		return stack;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean ext) {

		if (this == ModItems.cigarette) {
			list.add(EnumChatFormatting.RED + I18nUtil.resolveKey("desc.item.cigarette.asbestos_filter"));
			list.add(EnumChatFormatting.RED + I18nUtil.resolveKey("desc.item.cigarette.high_tar"));
			list.add(EnumChatFormatting.RED + I18nUtil.resolveKey("desc.item.cigarette.tobacco_contains_polonium"));
			list.add(EnumChatFormatting.RED + I18nUtil.resolveKey("item.cigarette.desc4"));
		} else if (this == ModItems.crackpipe) {
			String[] colors = new String[] {
					EnumChatFormatting.RED + "",
					EnumChatFormatting.GOLD + "",
					EnumChatFormatting.YELLOW + "",
					EnumChatFormatting.GREEN + "",
					EnumChatFormatting.AQUA + "",
					EnumChatFormatting.BLUE + "",
					EnumChatFormatting.DARK_PURPLE + "",
					EnumChatFormatting.LIGHT_PURPLE + "",
			};
			int len = 2000;
			list.add(I18nUtil.resolveKey("gui.cigarette.t_good_me_but_i")
					+ colors[(int) (System.currentTimeMillis() % len * colors.length / len)] + I18nUtil.resolveKey("gui.cigarette.great"));
		} else if (this == ModItems.joint) {
			list.add(EnumChatFormatting.GREEN + I18nUtil.resolveKey("item.cigarette.desc5"));
		}
	}
}
