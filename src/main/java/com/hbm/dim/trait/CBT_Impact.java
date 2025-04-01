package com.hbm.dim.trait;

import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.NBTTagCompound;

public class CBT_Impact extends CelestialBodyTrait {

	public long time;

	public CBT_Impact() {}

	public CBT_Impact(long time) {
		this.time = time;
	}

	@Override
	public void writeToNBT(NBTTagCompound nbt) {
		nbt.setLong("time", time);
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		time = nbt.getLong("time");
	}

	@Override
	public void writeToBytes(ByteBuf buf) {
		buf.writeLong(time);
	}

	@Override
	public void readFromBytes(ByteBuf buf) {
		time = buf.readLong();
	}

}
