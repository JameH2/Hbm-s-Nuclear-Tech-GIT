package com.hbm.dim.trait;

import com.hbm.dim.CelestialBody;

import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.NBTTagCompound;

public class CBT_Destroyed extends CelestialBodyTrait {

	public float effectTimer;

	public CBT_Destroyed() {}

	public CBT_Destroyed(float interp) {
		this.effectTimer = interp;
	}

	@Override
	public void writeToNBT(NBTTagCompound nbt) {
		nbt.setFloat("interp", effectTimer);
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		effectTimer = nbt.getFloat("interp");
	}

	@Override
	public void writeToBytes(ByteBuf buf) {

	}

	@Override
	public void readFromBytes(ByteBuf buf) {

	}

	@Override
	public void update(boolean isRemote, CelestialBody body) {
		if(isRemote) {
			effectTimer = Math.min(201.0f, effectTimer + 0.0025f * (201.0f - effectTimer) * 0.15f);
			if(effectTimer >= 200) {
				effectTimer = 0;
			}
		}
	}

}
