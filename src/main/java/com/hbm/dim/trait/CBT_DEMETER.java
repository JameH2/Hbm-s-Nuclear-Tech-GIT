package com.hbm.dim.trait;

import java.util.Random;

import com.hbm.dim.CelestialBody;

import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.NBTTagCompound;

public class CBT_DEMETER extends CelestialBodyTrait {

	public float health;
	public boolean awakened;
	public int awakenedTimer;
	public float wingPhase;
	public float spinSpeed;
	public float attackTime;
	public AttackType currentAttack;

	public CBT_DEMETER() {
		this(100.0F, false, 0);
	}

	public CBT_DEMETER(float health, boolean awakened, int awakenedTime) {
		this.health = health;
		this.awakened = awakened;
		this.awakenedTimer = awakenedTime;
		this.currentAttack = null;
	}

	public void transitionPhase(float target, float spinTarget) {
		wingPhase += (target - wingPhase) * 0.06f;
		spinSpeed += (spinTarget - spinSpeed) * 0.06f;
	}

	public void selectAttackType() {
		Random random = new Random();
		AttackType[] attacks = AttackType.values();
		currentAttack = attacks[random.nextInt(attacks.length)]; // store it
	}

	public void Awake() {
		if(!awakened) {
			transitionPhase(0,0);
		}
		if(awakenedTimer == 100) {
			awakened = true;
			selectAttackType();
		}
	}
	
	public void Transition() { //no not like that
		
	}

	@Override
	public void update(boolean isRemote, CelestialBody body) {
		if(!isRemote) {
			if(awakened) {
				attackTime++;
				if(attackTime >= 100) {
					selectAttackType();
					attackTime = 0;
				}

				transitionPhase(currentAttack.phase, currentAttack.spinSpeed);
			} else {
				awakenedTimer++;
				Awake();
			}
		} else {
			if(currentAttack != null) {
				// transitionPhase(1);
			}
		}
	}

	@Override
	public void writeToNBT(NBTTagCompound nbt) {
		nbt.setFloat("health", health);
		nbt.setBoolean("awakened", awakened);
		nbt.setInteger("awakenedTime", awakenedTimer);
		nbt.setFloat("attackTime", attackTime);
		if(currentAttack != null) {
			nbt.setInteger("attackType", currentAttack.ordinal());
		} else {
			nbt.setInteger("attackType", -1);
		}
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		health = nbt.getFloat("health");
		awakened = nbt.getBoolean("awakened");
		awakenedTimer = nbt.getInteger("awakenedTime");
		attackTime = nbt.getFloat("attackTime");
		int type = nbt.getInteger("attackType");
		if(type >= 0) {
			currentAttack = AttackType.values()[type];
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
		currentAttack = type >= 0 ? AttackType.values()[type] : null;
	}

	public enum AttackType {
		METEORS(5, 0.1f),
		ANGELIC(4, 0.5f),
		BEAM(9, 0.2f),
		DEATH(11, 0.5f);

		float spinSpeed;
		float phase;

		AttackType(float phase, float spinSpeed) {
			this.phase = phase;
			this.spinSpeed = spinSpeed;
		}
	}

}