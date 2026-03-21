package com.hbm.items.armor;

import com.hbm.main.MainRegistry;

import net.minecraft.entity.player.EntityPlayer;

public interface IPAWeaponsProvider {
	
	public IPAMelee getMeleeComponent(EntityPlayer entity);
	
	public static IPAMelee getMeleeComponentClient() {
		return getMeleeComponentCommon(MainRegistry.proxy.me());
	}
	
	public static IPAMelee getMeleeComponentCommon(EntityPlayer player) {
		if(player.inventory.armorInventory[2] != null && player.inventory.armorInventory[2].getItem() instanceof IPAWeaponsProvider) {
			IPAWeaponsProvider prov = (IPAWeaponsProvider) player.inventory.armorInventory[2].getItem();
			return prov.getMeleeComponent(player);
		}
		return null;
	}
	
	public IPARanged getRangedComponent(EntityPlayer entity);
	
	public static IPARanged getRangedComponentClient() {
		return getRangedComponentCommon(MainRegistry.proxy.me());
	}
	
	public static IPARanged getRangedComponentCommon(EntityPlayer player) {
		if(player.inventory.armorInventory[2] != null && player.inventory.armorInventory[2].getItem() instanceof IPAWeaponsProvider) {
			IPAWeaponsProvider prov = (IPAWeaponsProvider) player.inventory.armorInventory[2].getItem();
			return prov.getRangedComponent(player);
		}
		return null;
	}
}
