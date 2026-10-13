package com.hbm.saveddata.satellites;

import com.hbm.items.ModItems;

import net.minecraft.item.ItemStack;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.IChatComponent;
import net.minecraft.world.World;

public class SatelliteDysonRelay extends SatelliteBase {

	@Override
	public String getType() {
		return "DYSON_RELAY";
	}

	@Override
	public IChatComponent[] getInfo(World world) {
		return new IChatComponent[] {
			new ChatComponentTranslation(ModItems.sat_dyson_relay.getUnlocalizedName(new ItemStack(ModItems.sat_dyson_relay)) + ".name")
		};
	}

}
