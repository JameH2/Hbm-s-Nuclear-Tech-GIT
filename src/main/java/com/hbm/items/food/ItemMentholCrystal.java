package com.hbm.items.food;

import java.util.List;

import com.hbm.potion.HbmPotion;
import com.hbm.util.i18n.I18nUtil;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumAction;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.world.World;

public class ItemMentholCrystal extends ItemFood {

	public ItemMentholCrystal() {
		super(0, 1F, false);
		this.setAlwaysEdible();
	}

	@Override
	public EnumAction getItemUseAction(ItemStack stack) {
		return EnumAction.eat;
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

		player.addPotionEffect(new PotionEffect(Potion.moveSpeed.id, 10, 10));
		player.addPotionEffect(new PotionEffect(Potion.blindness.id, 20, 10));
		player.addPotionEffect(new PotionEffect(Potion.nightVision.id, 3 * 20, 10));

		player.addPotionEffect(new PotionEffect(HbmPotion.high.id, 15 * 20, 0));
		player.addPotionEffect(new PotionEffect(Potion.confusion.id, 15 * 20, 0));

		if (!world.isRemote) {
			world.playSoundEffect(player.posX, player.posY, player.posZ, "hbm:player.sniff", 1.0F,
					1.0F + (world.rand.nextFloat() - 0.5F) * 2F);
		}

		return stack;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean ext) {
		list.add(EnumChatFormatting.GRAY + I18nUtil.resolveKey("item.menthol_crystal.desc1"));
	}
}
