package com.hbm.dim.dima;

import com.hbm.dim.WeatherProviderCelestial;
import com.hbm.dim.WorldProviderCelestial;

import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.util.Vec3;

public class WeatherProviderDima extends WeatherProviderCelestial {

	@Override
	public Vec3 getSnowColor(WorldClient world) {
		return Vec3.createVectorHelper(1.0D, 0D, 0D);
	}
	

}
