package com.hbm.saveddata.satellites;

import com.hbm.items.ModItems;

import net.minecraft.item.ItemStack;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.IChatComponent;
import net.minecraft.world.World;

public class SatelliteScanner extends SatelliteBase {
	
	public SatelliteScanner() { }

	@Override public String getType() { return "DEPTH_SCANNER"; }
	
	@Override
	public IChatComponent[] getInfo(World world) {
		return new IChatComponent[] {
				new ChatComponentTranslation(ModItems.sat_scanner.getUnlocalizedName(new ItemStack(ModItems.sat_scanner)) + ".name")
		};
	}
}
