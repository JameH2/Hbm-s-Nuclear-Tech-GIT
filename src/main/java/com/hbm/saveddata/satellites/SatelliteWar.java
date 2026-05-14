package com.hbm.saveddata.satellites;

import com.hbm.dim.CelestialBody;

import io.netty.buffer.ByteBuf;
import net.minecraft.world.World;

public abstract class SatelliteWar extends Satellite {

	public SatelliteWar() {

	}

	public float effectTimer;

	public abstract void fire();
	public abstract void setTarget(CelestialBody body);

	@Override
	public void onUpdate(World world) {
		if(!world.isRemote) {
			// I assume this is for testing, attempting to fire every tick?
			fire();
		}
	}


	/**
	 * When a war satellite fires, this will brighten the atmosphere, and control timing of effects
	 * @return brightness
	 */
	public float getEffectTimer() {
		return effectTimer;
	}

	@Override
	public void serialize(ByteBuf buf) {
		buf.writeFloat(effectTimer);
	}

	@Override
	public void deserialize(ByteBuf buf) {
		this.effectTimer = buf.readFloat();
	}

}
