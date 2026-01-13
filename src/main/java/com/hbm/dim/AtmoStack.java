package com.hbm.dim;

import com.hbm.inventory.FluidStack;
import com.hbm.inventory.fluid.FluidType;


public class AtmoStack {

	public FluidType type;
	public int pressure;
	
	public AtmoStack(FluidType type, int pressure) {
		this.pressure = pressure;
		this.type = type;
	}
}