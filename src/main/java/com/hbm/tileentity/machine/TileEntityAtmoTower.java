package com.hbm.tileentity.machine;

import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.tank.FluidTank;

public class TileEntityAtmoTower extends TileEntityDeuteriumTower {

	public TileEntityAtmoTower() {
		super();
		tanks[0] = new FluidTank(Fluids.AIR, 50000);
		tanks[1] = new FluidTank(Fluids.NITROGEN, 5000);
	}

	@Override
	public long getMaxPower() {
		return 1000000;
	}

}