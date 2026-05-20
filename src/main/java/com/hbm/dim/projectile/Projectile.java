package com.hbm.dim.projectile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.hbm.dim.CelestialBody;

import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

public class Projectile {
	
	// V.2
	//i get it now, after all this time; i might be rusting with the documentation but i can still think!
	//IM STILL ALIVE AND KICKING!
	
	public static final List<Class<?extends Projectile>> registry = new ArrayList<>();
	
	public static final Map<String, Class<? extends Projectile>> nameMap = new HashMap<>();
	
	public static void register() {
		registerProjectile("small", ProjectileSmall.class);
		registerProjectile("splitshot", ProjectileSplitshot.class);
	}
	
	public static void registerProjectile(String name, Class<? extends Projectile> clazz) {
		nameMap.put(name, clazz);
		registry.add(clazz);
	}
	
	public static Projectile create(String name) {
		Class<? extends Projectile> clazz = nameMap.get(name);
		if (clazz != null) {
			try {
				return clazz.newInstance();
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
		} else {
			return null;
		}
	}

	public static Projectile create(int id) {
		try {
			return registry.get(id).newInstance();
		} catch (Exception e) {
			e.printStackTrace();
			return null;

		}
	}


	public int getRegistryId() {
		return registry.indexOf(this.getClass());
	}
	
	
	
	protected double worldX;
	protected double worldY;
	protected double worldZ;
	
	protected int travelTime;

	protected int damage;
	protected int size;

	public String sourceName;
	public String targetName;
	
	protected boolean dead = false;
	
	//rendering 
	protected int initialTravelTime;
	
	protected int impactTimer = -1;
	
	protected float angle = 0;
	
	protected float inclination = 0;
	
	protected Projectile() {

	}
	
	protected Projectile(String sourceName, String targetName, int travelTime, int damage, int size, double worldX, double worldY, double worldZ) {
		this.sourceName = sourceName;
		this.targetName = targetName;
		this.travelTime = travelTime;
		this.initialTravelTime = travelTime;
		this.damage = damage;
		this.size = size;
		this.worldX = worldX;
		this.worldY = worldY;
		this.worldZ = worldZ;
	}
	

	public final void tick(boolean isRemote) {
		if(dead) return;
 
		if(travelTime > 0) {
			travelTime--;
			onTravel(isRemote);
		} else if(impactTimer < 0) {
			impactTimer = 0;
		}
 
		if(impactTimer >= 0) {
			impactTimer++;
			onImpact(isRemote);
 
			if(impactTimer >= getImpactDuration()) {
				onFinish(isRemote);
				dead = true;
			}
		}
 
		onUpdate(isRemote);
	}
 

	protected void onTravel(boolean isRemote) { }
 

	protected void onImpact(boolean isRemote) { }
 

	protected void onFinish(boolean isRemote) {
		if(!isRemote) {
			CelestialBody target = CelestialBody.getBody(targetName);
			if(target != null) {
				target.applyDamage(damage);
			}
		}
	}
 

	protected void onUpdate(boolean isRemote) { }

	public void onWorldImpact(World world) { }

	protected int getImpactDuration() {
		return 200;
	}
	
	public List<Projectile> getSpawnedChildren() {
		return null;
	}
	
	
	//getters//
	public boolean isDead() { return dead; }
	public void kill() { dead = true; }
 
	public int getTravelTime() { return travelTime; }
	public int getDamage() { return damage; }
	public int getSize() { return size; }
	public String getSourceName() { return sourceName; }
	public String getTargetName() { return targetName; }
	public double getWorldX() { return worldX; }
	public double getWorldZ() { return worldZ; }
	public int getImpactTimer() { return impactTimer; }
	
	
	public float getTravelProgress() {
		if(initialTravelTime <= 0) return 1.0F;
		return 1.0F - ((float) travelTime / (float) initialTravelTime);
	}
 
	public float getTravelProgress(float partialTicks) {
		if(impactTimer < 0) return 1.0F;
		float current = travelTime - (travelTime > 0 ? partialTicks : 0);
		return 1.0F - (current / (float) initialTravelTime);
	}
	
	public float getImpactProgress() {
		if(impactTimer < 0) return 0F;
		return Math.min(1.0F, (float) impactTimer / (float) getImpactDuration());
	}
	
	public float getImpactProgress(float partialTicks) {
		if(impactTimer < 0) return 0.0F;
		float impactInterp = (float) impactTimer + partialTicks; //laughs super loud
		return Math.min(1.0F, impactInterp / (float) getImpactDuration());
	}
 
	public float getImpactTimerSmooth(float partialTicks) {
		if(impactTimer < 0) return 0.0F;
		return (float) impactTimer + partialTicks;
	}
 
 
 
	public int getInitialTravelTime() { return initialTravelTime; }
	public float getAngleOffset() { return angle; }
	public float getInclinationOffset() { return inclination; }
	
	public void writeToNBT(NBTTagCompound nbt) {
		nbt.setInteger("id", getRegistryId());
		nbt.setInteger("travelTime", travelTime);
		nbt.setInteger("initialTravelTime", initialTravelTime);
		nbt.setInteger("damage", damage);
		nbt.setInteger("size", size);
		nbt.setString("source", sourceName != null ? sourceName : "");
		nbt.setString("target", targetName != null ? targetName : "");
		nbt.setDouble("worldX", worldX);
		nbt.setDouble("worldY", worldY);
		nbt.setDouble("worldZ", worldZ);
		nbt.setInteger("impactTimer", impactTimer);
		nbt.setFloat("angleOffset", angle);
		nbt.setFloat("inclinationOffset", inclination);
		writeExtraNBT(nbt);
	}
 
	public void readFromNBT(NBTTagCompound nbt) {
		travelTime = nbt.getInteger("travelTime");
		initialTravelTime = nbt.getInteger("initialTravelTime");
		damage = nbt.getInteger("damage");
		size = nbt.getInteger("size");
		sourceName = nbt.getString("source");
		targetName = nbt.getString("target");
		worldX = nbt.getDouble("worldX");
		worldY = nbt.getDouble("worldY");
		worldZ = nbt.getDouble("worldZ");
		impactTimer = nbt.getInteger("impactTimer");
		angle = nbt.getFloat("angleOffset");
		inclination = nbt.getFloat("inclinationOffset");
		readExtraNBT(nbt);
	}
 
	public void writeToBytes(ByteBuf buf) {
		buf.writeInt(getRegistryId());
		buf.writeInt(travelTime);
		buf.writeInt(initialTravelTime);
		buf.writeInt(damage);
		buf.writeInt(size);
		writeString(buf, sourceName);
		writeString(buf, targetName);
		buf.writeDouble(worldX);
		buf.writeDouble(worldY);
		buf.writeDouble(worldZ);
		buf.writeInt(impactTimer);
		buf.writeFloat(angle);
		buf.writeFloat(inclination);
		writeExtraBytes(buf);
	}
 


	public void readFromBytes(ByteBuf buf) {
		travelTime = buf.readInt();
		initialTravelTime = buf.readInt();
		damage = buf.readInt();
		size = buf.readInt();
		sourceName = readString(buf);
		targetName = readString(buf);
		worldX = buf.readDouble();
		worldY = buf.readDouble();
		worldZ = buf.readDouble();
		impactTimer = buf.readInt();
		angle = buf.readFloat();
		inclination = buf.readFloat();
		readExtraBytes(buf);
	}
 
	protected void writeExtraNBT(NBTTagCompound nbt) { }
	protected void readExtraNBT(NBTTagCompound nbt) { }
	protected void writeExtraBytes(ByteBuf buf) { }
	protected void readExtraBytes(ByteBuf buf) { }

	
	
	protected static void writeString(ByteBuf buf, String str) {
		if(str == null) str = "";
		byte[] bytes = str.getBytes();
		buf.writeShort(bytes.length);
		buf.writeBytes(bytes);
	}
 
	protected static String readString(ByteBuf buf) {
		int len = buf.readShort();
		byte[] bytes = new byte[len];
		buf.readBytes(bytes);
		return new String(bytes);
	}
	
}
