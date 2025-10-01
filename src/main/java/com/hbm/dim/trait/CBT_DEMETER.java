package com.hbm.dim.trait;

import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.NBTTagCompound;

public class CBT_DEMETER extends CelestialBodyTrait {

    private float health;
    private boolean awakened;
    private int awakenedTime;

    public CBT_DEMETER() {
        this.health = 100.0f;
        this.awakened = false;
        this.awakenedTime = 0;
    }

    public CBT_DEMETER(float health, boolean awakened, int awakenedTime) {
        this.health = health;
        this.awakened = awakened;
        this.awakenedTime = awakenedTime;
    }

    @Override
    public void update(boolean isRemote) {
        if (!isRemote) {
            if (awakened) {
                awakenedTime++;
            }
        }
    }

    @Override
    public void writeToNBT(NBTTagCompound nbt) {
        nbt.setFloat("health", health);
        nbt.setBoolean("awakened", awakened);
        nbt.setInteger("awakenedTime", awakenedTime);
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        health = nbt.getFloat("health");
        awakened = nbt.getBoolean("awakened");
        awakenedTime = nbt.getInteger("awakenedTime");
    }

    @Override
    public void writeToBytes(ByteBuf buf) {
        buf.writeFloat(health);
        buf.writeBoolean(awakened);
        buf.writeInt(awakenedTime);
    }

    @Override
    public void readFromBytes(ByteBuf buf) {
        health = buf.readFloat();
        awakened = buf.readBoolean();
        awakenedTime = buf.readInt();
    }
    
    public enum attackType{
	    METEORS,
	    ANGELIC,
	    BEAM,
	    DEATH
    }

}
