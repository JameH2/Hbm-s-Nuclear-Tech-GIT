package com.hbm.dim.trait;

import java.util.Random;

import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.NBTTagCompound;

public class CBT_DEMETER extends CelestialBodyTrait {

	    public float health;
	    public boolean awakened;
	    public int awakenedTimer;
	    public float wingPhase;
	    public float spinSpeed;
	    public float attackTime;
	    public attackType currentAttack;

	    public CBT_DEMETER() {
	        this.health = 100.0f;
	        this.awakened = false;
	        this.awakenedTimer = 0;
	        this.currentAttack = null;
	    }

	    public CBT_DEMETER(float health, boolean awakened, int awakenedTime) {
	        this.health = health;
	        this.awakened = awakened;
	        this.awakenedTimer = awakenedTime;
	        this.currentAttack = null;
	    }

	    public void transitionPhase(float target, float spinTarget) {
		    wingPhase += (target - wingPhase) * 0.06f;

		}

	    public void selectAttackType() {
		    Random random = new Random();
		    attackType[] attacks = attackType.values(); 
		    currentAttack = attacks[random.nextInt(attacks.length)]; // store it
		}


	    public void Awake() {
	        if (awakenedTimer == 100) {
	            awakened = true;
	                selectAttackType();
	        }
	    }

	    @Override
	    public void update(boolean isRemote) {
	        if (!isRemote) {
	            if (awakened) {
	                attackTime++;
	                if (attackTime >= 100) {
	                    selectAttackType();
	                    attackTime = 0;
	                }
			    transitionPhase(currentAttack.phase, currentAttack.spinSpeed);

	            } else {
	                awakenedTimer++;
	                Awake();
	            }
	        } else {
	            if (currentAttack != null) {
	                //transitionPhase(1);
	            }

	        }
	    }


	    @Override
	    public void writeToNBT(NBTTagCompound nbt) {
	        nbt.setFloat("health", health);
	        nbt.setBoolean("awakened", awakened);
	        nbt.setInteger("awakenedTime", awakenedTimer);
	        nbt.setFloat("attackTime", attackTime);
	        if (currentAttack != null) {
	       	    nbt.setInteger("attackType", currentAttack.ordinal());
	       	} else {
	       	    nbt.setInteger("attackType", -1);
	       	}	    }

	    @Override
	    public void readFromNBT(NBTTagCompound nbt) {
	        health = nbt.getFloat("health");
	        awakened = nbt.getBoolean("awakened");
	        awakenedTimer = nbt.getInteger("awakenedTime");
	        attackTime = nbt.getFloat("attackTime");
	        int type = nbt.getInteger("attackType");
	        if (type >= 0) {
	       	    currentAttack = attackType.values()[type];
	       	}
	    }

	    @Override
	    public void writeToBytes(ByteBuf buf) {
	        buf.writeFloat(health);
	        buf.writeBoolean(awakened);
	        buf.writeInt(awakenedTimer);
	        buf.writeFloat(attackTime);
	        buf.writeInt(currentAttack == null ? -1 : currentAttack.ordinal());
	    }

	    @Override
	    public void readFromBytes(ByteBuf buf) {
	        health = buf.readFloat();
	        awakened = buf.readBoolean();
	        awakenedTimer = buf.readInt();
	        attackTime = buf.readFloat();
	        int type = buf.readInt();
	        currentAttack = type >= 0 ? attackType.values()[type] : null;
	    }

	    public enum attackType {
	        METEORS(5, 0.5f),
	        ANGELIC(4, 0.5f),
	        BEAM(9, 0.5f),
	        DEATH(11,0.5f);

		 float spinSpeed;
	        float phase;
	        attackType(float phase, float spinSpeed) {
	            this.phase = phase;
	            this.spinSpeed = spinSpeed;
	        }
	    }

	}