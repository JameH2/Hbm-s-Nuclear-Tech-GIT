package com.hbm.handler.atmosphere;

import com.hbm.dim.trait.CBT_Atmosphere;

import net.minecraftforge.common.IPlantable;

public interface IPlantableBreathing extends IPlantable {

	public boolean canBreathe(CBT_Atmosphere atmosphere);

}
