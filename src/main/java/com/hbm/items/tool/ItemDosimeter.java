package com.hbm.items.tool;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import baubles.api.BaubleType;
import baubles.api.IBauble;

import com.hbm.extprop.HbmLivingProps;
import com.hbm.main.NTMSounds;
import com.hbm.util.ContaminationUtil;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class ItemDosimeter extends Item implements IBauble {

	Random rand = new Random();

	@Override
	public void onUpdate(ItemStack stack, World world, Entity entity, int i, boolean bool) {

		if(entity instanceof EntityLivingBase) tickDosimeter(world, (EntityLivingBase) entity);
	}

	private void tickDosimeter(World world, EntityLivingBase entity) {
		if(world.isRemote) return;

		float x = HbmLivingProps.getRadBuf(entity);

		if(world.getTotalWorldTime() % 5 == 0) {

			if(x > 1E-5) {
				if(world.rand.nextFloat() > x) return;

				List<Integer> list = new ArrayList<Integer>();

				if(x < 0.5) list.add(0);
				if(x < 1) list.add(1);
				if(x >= 0.5 && x < 2) list.add(2);
				if(x >= 1 && x >= 2) list.add(3);
			
				int r = list.get(rand.nextInt(list.size()));

				if(r > 0)
					world.playSoundAtEntity(entity, NTMSounds.GEIGER_PREFIX + r, 1.0F, 1.0F);
				
			} else if(rand.nextInt(100) == 0) {
				world.playSoundAtEntity(entity, NTMSounds.GEIGER_PREFIX + 1, 1.0F, 1.0F);
			}
		}
	}

	@Override
	public BaubleType getBaubleType(ItemStack stack) {
		return BaubleType.BELT;
	}

	@Override
	public void onWornTick(ItemStack stack, EntityLivingBase entity) {
		tickDosimeter(entity.worldObj, entity);
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
	public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player) {

		if(!world.isRemote) {
			world.playSoundAtEntity(player, NTMSounds.TECH_BOOP, 1.0F, 1.0F);
			ContaminationUtil.printDosimeterData(player);
		}

		return stack;
	}
}
