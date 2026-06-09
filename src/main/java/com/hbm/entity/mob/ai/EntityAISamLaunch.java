package com.hbm.entity.mob.ai;

import com.hbm.entity.mob.EntitySamBot;
import com.hbm.entity.projectile.EntityBulletBaseMK4;
import com.hbm.items.weapon.sedna.factory.XFactoryRocket;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.util.Vec3;

public class EntityAISamLaunch extends EntityAIBase {

	private EntityCreature owner;
	private Entity target;

	private final int minRange         = 8;
	private final int maxRange         = 80;
	private final int salvoSize        = 4;
	private final int salvoShotDelay   = 8;
	private final int salvoCooldown    = 80;
	private final double leadFactor    = 1.0;
	private final double missileSpeed  = 0.6;
	private final double moveSpeed     = 0.28;

	private final int pathInterval = 5;

	private int shotTimer;
	private int cooldownTimer;
	private int salvoRemaining;
	private int pathCooldown;

	public EntityAISamLaunch(EntityCreature owner) {
		this.owner = owner;
		this.setMutexBits(3);
	}

	@Override
	public boolean shouldExecute() {
		Entity t = resolveTarget();
		if(t == null || !t.isEntityAlive()) return false;
		if(!EntitySamBot.isAerial(t)) return false;

		double dist = owner.getDistanceToEntity(t);
		if(dist > maxRange) return false;

		this.target = t;
		return true;
	}

	@Override
	public boolean continueExecuting() {
		return shouldExecute();
	}

	@Override
	public void resetTask() {
		this.target = null;
		this.salvoRemaining = 0;
		this.shotTimer = 0;
		this.pathCooldown = 0;
		this.owner.getNavigator().clearPathEntity();
	}

	@Override
	public void updateTask() {
		double dist = owner.getDistanceToEntity(target);

		if(target instanceof EntityLivingBase) {
			owner.getLookHelper().setLookPositionWithEntity(target, 30F, 30F);
		}

		updateMovement(dist);

		if(cooldownTimer > 0) {
			cooldownTimer--;
			return;
		}

		if(salvoRemaining <= 0) {
			salvoRemaining = salvoSize;
			shotTimer = 0;
		}

		if(shotTimer > 0) {
			shotTimer--;
			return;
		}

		fireMissile();
		salvoRemaining--;
		if(salvoRemaining <= 0) {
			cooldownTimer = salvoCooldown;
		} else {
			shotTimer = salvoShotDelay;
		}
	}

	private Entity resolveTarget() {
		EntityLivingBase living = owner.getAttackTarget();
		if(living != null && living.isEntityAlive() && EntitySamBot.isAerial(living)) return living;

		int id = owner.getDataWatcher().getWatchableObjectInt(20);
		if(id != 0) {
			Entity e = owner.worldObj.getEntityByID(id);
			if(e != null && e.isEntityAlive() && EntitySamBot.isAerial(e)) return e;
		}
		return null;
	}

	private void updateMovement(double dist) {
		if(pathCooldown > 0) {
			pathCooldown--;
			return;
		}
		pathCooldown = pathInterval;

		if(dist < minRange) {
			Vec3 away = Vec3.createVectorHelper(owner.posX - target.posX, 0, owner.posZ - target.posZ).normalize();
			double bx = owner.posX + away.xCoord * 6;
			double bz = owner.posZ + away.zCoord * 6;
			owner.getNavigator().tryMoveToXYZ(bx, owner.posY, bz, moveSpeed);
			return;
		}

		if(target instanceof EntityLivingBase) {
			owner.getNavigator().tryMoveToEntityLiving((EntityLivingBase) target, moveSpeed);
		} else {
			owner.getNavigator().tryMoveToXYZ(target.posX, owner.posY, target.posZ, moveSpeed);
		}
	}



	private void fireMissile() {
		double dx = target.posX - owner.posX;
		double dy = (target.posY + target.height * 0.5) - (owner.posY + 3);
		double dz = target.posZ - owner.posZ;

		double horiz = Math.sqrt(dx * dx + dz * dz);
		if(horiz < 0.01 && Math.abs(dy) < 0.01) return;

		float yaw   = (float) Math.atan2(dz, dx);
		float pitch = (float) Math.atan2(dy, horiz);

		EntityBulletBaseMK4 missile = new EntityBulletBaseMK4(owner.worldObj, XFactoryRocket.rocket_ncrpa[1], 6F, 0.05F, yaw, pitch);
		missile.lockonTarget = target;

		Vec3 vec = Vec3.createVectorHelper(1.5, 0, 0);
		vec.rotateAroundZ(-pitch);
		vec.rotateAroundY(-(yaw + (float)(Math.PI * 0.5)));

		double spawnX = owner.posX + vec.xCoord;
		double spawnY = owner.posY + 3 + vec.yCoord;
		double spawnZ = owner.posZ + vec.zCoord;

		missile.setPositionAndRotation(spawnX, spawnY, spawnZ, missile.rotationYaw, missile.rotationPitch);
		owner.worldObj.spawnEntityInWorld(missile);

		owner.worldObj.playSoundEffect(owner.posX, owner.posY, owner.posZ, "hbm:turret.richard_fire", 15.0F, 1.0F);
	}
}