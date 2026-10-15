package com.hbm.items.armor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import com.hbm.handler.ArmorModHandler;
import com.hbm.handler.threading.PacketThreading;
import com.hbm.packet.toclient.AuxParticlePacketNT;
import com.hbm.potion.HbmPotion;
import com.hbm.util.i18n.I18nUtil;

import cpw.mods.fml.common.network.NetworkRegistry.TargetPoint;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumAction;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.world.World;
import net.minecraft.init.Items;
import net.minecraft.util.ChatComponentTranslation;

public class ItemModMilk extends ItemArmorMod {

	public ItemModMilk() {
		super(ArmorModHandler.extra, true, true, true, true);
	}

	@Override
	public void addInformation(ItemStack itemstack, EntityPlayer player, List list, boolean bool) {

		list.add(EnumChatFormatting.WHITE + I18nUtil.resolveKey("desc.item.mod_milk.removes_bad_potion_effects"));
		list.add("");
		super.addInformation(itemstack, player, list, bool);
	}

	@Override
	public void addDesc(List list, ItemStack stack, ItemStack armor) {
		list.add(EnumChatFormatting.WHITE + I18nUtil.resolveKey("item.mod_milk.desc2", stack.getDisplayName()));
	}

	@Override
	public void modUpdate(EntityLivingBase entity, ItemStack armor) {

		List<Integer> ints = new ArrayList();
		Iterator iterator = ((Collection) entity.getActivePotionEffects()).iterator();

		while (iterator.hasNext()) {
			PotionEffect eff = (PotionEffect) iterator.next();

			if (HbmPotion.getIsBadEffect(Potion.potionTypes[eff.getPotionID()])) {
				ints.add(eff.getPotionID());
			}
		}

		for (Integer i : ints)
			entity.removePotionEffect(i);
	}

	@Override
	public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player) {
		player.setItemInUse(stack, this.getMaxItemUseDuration(stack));
		return stack;
	}

	@Override
	public int getMaxItemUseDuration(ItemStack stack) {
		return 32;
	}

	@Override
	public EnumAction getItemUseAction(ItemStack stack) {
		return EnumAction.drink;
	}

	@Override
	public ItemStack onEaten(ItemStack stack, World world, EntityPlayer player) {
		if (!player.capabilities.isCreativeMode) {
			return new ItemStack(Items.glass_bottle);
		}

		if (!world.isRemote) {
			this.modUpdate(player, stack);

			player.addPotionEffect(new PotionEffect(Potion.confusion.id, 7 * 20, 0));
			player.addChatMessage(
					new ChatComponentTranslation("chat.mod_milk.1").setChatStyle(new net.minecraft.util.ChatStyle().setColor(EnumChatFormatting.GRAY).setItalic(true)));

			NBTTagCompound nbt = new NBTTagCompound();
			nbt.setString("type", "vomit");
			nbt.setString("mode", "normal");
			nbt.setInteger("count", 10);
			nbt.setInteger("entity", player.getEntityId());
			PacketThreading.createAllAroundThreadedPacket(new AuxParticlePacketNT(nbt, 0, 0, 0),
					new TargetPoint(player.dimension, player.posX, player.posY, player.posZ, 25));
			world.playSoundEffect(player.posX, player.posY, player.posZ, "hbm:player.vomit", 1.0F, 1.0F);
		}

		return stack;
	}
}
