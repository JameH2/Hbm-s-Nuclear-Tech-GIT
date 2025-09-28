package com.hbm.dim.dima;

import com.hbm.dim.WeatherProviderCelestial;

import net.minecraft.util.Vec3;

public class WeatherProviderDima extends WeatherProviderCelestial {

	@Override
	protected Vec3 getRainColor() {
		return Vec3.createVectorHelper(1, 0, 0);
	}

	@Override
	protected boolean canSnow() {
		return false;
	}

}
