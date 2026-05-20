package com.hbm.saveddata.satellites;

import com.hbm.dim.CelestialBody;

import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

public abstract class SatelliteWar extends Satellite {

	protected boolean firing;
	 
	public SatelliteWar() {
		this.effectTimer = 0;
		this.firing = false;
	}

	public float effectTimer;

	public abstract void fire();
	public abstract void setTarget(CelestialBody body);

	
	protected void triggerFireEffect() {
		this.firing = true;
		this.effectTimer = 0;
	}

	@Override
	public void onUpdate(World world) {
		if(firing) {
			effectTimer += 0.5F;
			effectTimer = Math.min(100.0F, effectTimer + 0.3F * (100.0F - effectTimer) * 0.15F);
 
			if(effectTimer >= 100) {
				effectTimer = 0;
				firing = false;
			}
		}
	}


	public float getEffectTimer() {
		return effectTimer;
	}

	@Override
	public void writeToNBT(NBTTagCompound nbt) {
		nbt.setFloat("effectTimer", effectTimer);
		nbt.setBoolean("firing", firing);
	}
 
	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		effectTimer = nbt.getFloat("effectTimer");
		firing = nbt.getBoolean("firing");
	}
 
	@Override
	public void serialize(ByteBuf buf) {
		buf.writeFloat(effectTimer);
		buf.writeBoolean(firing);
	}
 
	@Override
	public void deserialize(ByteBuf buf) {
		effectTimer = buf.readFloat();
		firing = buf.readBoolean();
	}

}
