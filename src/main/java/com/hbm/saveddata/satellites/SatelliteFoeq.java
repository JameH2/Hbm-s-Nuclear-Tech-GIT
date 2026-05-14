package com.hbm.saveddata.satellites;

import com.hbm.main.MainRegistry;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;

public class SatelliteFoeq extends Satellite {

	public SatelliteFoeq() {
		this.satIface = Interfaces.NONE;
	}

	public void onOrbit(World world, double x, double y, double z) {

		for(Object p : world.playerEntities)
			((EntityPlayer) p).triggerAchievement(MainRegistry.achFOEQ);
	}

}
