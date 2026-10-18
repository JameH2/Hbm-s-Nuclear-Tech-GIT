package com.hbm.items.special;

import java.util.List;

import baubles.api.BaubleType;
import baubles.api.IBauble;
import baubles.api.expanded.BaubleExpandedSlots;
import baubles.api.expanded.IBaubleExpanded;

import com.hbm.main.MainRegistry;
import com.hbm.util.i18n.I18nUtil;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.world.World;

public class ItemPolaroid extends Item implements IBauble, IBaubleExpanded {

    @Override
	public void onUpdate(ItemStack stack, World world, Entity entity, int i, boolean b) {
		tickPolaroid(entity);
    }

	private void tickPolaroid(Entity entity) {
		if(!(entity instanceof EntityPlayer)) return;
		EntityPlayer player = (EntityPlayer) entity;
		if(player.getHealth() < 10F) player.addPotionEffect(new PotionEffect(Potion.resistance.id, 10, 2));
	}

	@Override
	public BaubleType getBaubleType(ItemStack stack) { return BaubleType.AMULET; }

	@Override
	public String[] getBaubleTypes(ItemStack stack) {
		return new String[] { BaubleExpandedSlots.charmType };
	}

	@Override
	public void onWornTick(ItemStack stack, EntityLivingBase entity) {
		tickPolaroid(entity);
	}

	@Override
	public void onEquipped(ItemStack stack, EntityLivingBase entity) { }

	@Override
	public void onUnequipped(ItemStack stack, EntityLivingBase entity) { }

	@Override
	public boolean canEquip(ItemStack stack, EntityLivingBase entity) { return true; }

	@Override
	public boolean canUnequip(ItemStack stack, EntityLivingBase entity) { return true; }

	@Override
	public void addInformation(ItemStack itemstack, EntityPlayer player, List list, boolean bool)
	{
		list.add(I18nUtil.resolveKey("item.polaroid.fate"));
		list.add("");
		switch(MainRegistry.polaroidID) {
		case 1:
			list.add("...");
			break;
		case 2:
			list.add(I18nUtil.resolveKey("item.polaroid.clarity"));
			break;
		case 3:
			list.add(I18nUtil.resolveKey("item.polaroid.glitch"));
			break;
		case 4:
			list.add(I18nUtil.resolveKey("item.polaroid.time"));
			break;
		case 5:
			list.add(I18nUtil.resolveKey("item.polaroid.abyss"));
			break;
		case 6:
			list.add(I18nUtil.resolveKey("item.polaroid.celebration"));
			break;
		case 7:
			list.add(I18nUtil.resolveKey("item.polaroid.cipher"));
			break;
		case 8:
			list.add("11011100");
			break;
		case 9:
			list.add(I18nUtil.resolveKey("item.polaroid.encoded_time"));
			break;
		case 10:
			list.add(I18nUtil.resolveKey("item.polaroid.schrabidium"));
			break;
		case 11:
			list.add(I18nUtil.resolveKey("item.polaroid.corrupted"));
			break;
		case 12:
			list.add(I18nUtil.resolveKey("item.polaroid.streaks"));
			break;
		case 13:
			list.add(I18nUtil.resolveKey("item.polaroid.quadrant", 1));
			break;
		case 14:
			list.add(I18nUtil.resolveKey("item.polaroid.quadrant", 4));
			break;
		case 15:
			list.add(I18nUtil.resolveKey("item.polaroid.quadrant", 3));
			break;
		case 16:
			list.add(I18nUtil.resolveKey("item.polaroid.quadrant", 2));
			break;
		case 17:
			list.add(I18nUtil.resolveKey("item.polaroid.friends"));
			break;
		case 18:
			list.add(I18nUtil.resolveKey("item.polaroid.boxcars"));
			list.add("");
			for(String line : I18nUtil.resolveKeyArray("item.polaroid.postscript.desc")) {
				list.add(line);
			}
			break;
		}
	}

}
