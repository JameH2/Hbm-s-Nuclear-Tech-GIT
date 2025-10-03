package com.hbm.dim.trait;

import java.util.Random;

import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.NBTTagCompound;

public class CBT_DEMETER extends CelestialBodyTrait {

    private float health;
    private boolean awakened;
    private int awakenedTimer;
    private float wingPhase;
    private float attackTime;

    public CBT_DEMETER() {
        this.health = 100.0f;
        this.awakened = false;
        this.awakenedTimer = 0;
    }

    public CBT_DEMETER(float health, boolean awakened, int awakenedTime) {
        this.health = health;
        this.awakened = awakened;
        this.awakenedTimer = awakenedTime;
    }
    
    public void transitionPhase(float target) {
	    wingPhase += (Math.round(target) - wingPhase) * 0.1f;
	    
	    if(Math.abs(wingPhase - Math.round(target)) < 0.01f) {
		    wingPhase = Math.round(target);
		    
	    }
	   
    }
    
    public void selectAttackType() {
	    Random random = new Random();
	    attackType[] attacks = attackType.values(); 
           attackType type = attacks[random.nextInt(attacks.length)];
           transitionPhase(type.phase);
    }
    
    public void Awake() {	
	    if(awakenedTimer == 100) {
		    awakened = true;
		    transitionPhase(3);        
	    }

    }

    @Override
    public void update(boolean isRemote) {
        if (!isRemote) {
            if (awakened) {
                   if (attackTime >= 100) {
                          // Pick a new attack
                          selectAttackType();
                          attackTime = 0; // reset timer
                      }
            } else {
       	     Awake();
            }
        }
    }

    @Override
    public void writeToNBT(NBTTagCompound nbt) {
        nbt.setFloat("health", health);
        nbt.setBoolean("awakened", awakened);
        nbt.setInteger("awakenedTime", awakenedTimer);
        nbt.setFloat("attackTime", attackTime);
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        health = nbt.getFloat("health");
        awakened = nbt.getBoolean("awakened");
        awakenedTimer = nbt.getInteger("awakenedTime");
        attackTime = nbt.getInteger("attackTime");
    }

    @Override
    public void writeToBytes(ByteBuf buf) {
        buf.writeFloat(health);
        buf.writeBoolean(awakened);
        buf.writeInt(awakenedTimer);
        buf.writeFloat(attackTime);
    }

    @Override
    public void readFromBytes(ByteBuf buf) {
        health = buf.readFloat();
        awakened = buf.readBoolean();
        awakenedTimer = buf.readInt();
        attackTime = buf.readFloat();
    }
    
    public enum attackType{
	    METEORS(5),
	    ANGELIC(4),
	    BEAM(9),
	    DEATH(11);
	    
	    float phase;
	    
	    attackType(float phase) {
		    this.phase = phase;
	    }
	   
		
    }

}
