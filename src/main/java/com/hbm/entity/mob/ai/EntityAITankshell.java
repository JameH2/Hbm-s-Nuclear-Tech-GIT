package com.hbm.entity.mob.ai;

import com.hbm.entity.projectile.EntityBulletBaseMK4;
import com.hbm.handler.CasingEjector;
import com.hbm.items.weapon.sedna.factory.XFactory12ga;
import com.hbm.items.weapon.sedna.factory.XFactoryRocket;
import com.hbm.packet.PacketDispatcher;
import com.hbm.packet.toclient.AuxParticlePacketNT;
import com.hbm.particle.SpentCasing;
import com.hbm.particle.SpentCasing.CasingType;

import cpw.mods.fml.common.network.NetworkRegistry.TargetPoint;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.Vec3;

public class EntityAITankshell extends EntityAIBase {

	private EntityCreature owner;
	private EntityLivingBase target;
	private int delay;
	private int timer;
	private int AttackDistance;
	private boolean artilleryMode;
	private int reloadTimer;
	private int reloadDelay;

	public EntityAITankshell(EntityCreature owner, boolean checkSight, boolean nearbyOnly, int delay, int switchAttackDistance, int reloadDelay) {
		this.owner = owner;
		this.delay = delay;
		this.timer = delay;
		this.AttackDistance = switchAttackDistance;
		this.artilleryMode = true;
		this.reloadTimer = reloadDelay;
		this.reloadDelay = reloadDelay;
	}

	@Override
	public boolean shouldExecute() {
		EntityLivingBase entity = this.owner.getAttackTarget();

		if(entity == null) {
			return false;
		} else {
			this.target = entity;
			double dist = Vec3.createVectorHelper(target.posX - owner.posX, target.posY - owner.posY, target.posZ - owner.posZ).lengthVector();
			if(dist > AttackDistance) {
				artilleryMode = true;
			} else {
				artilleryMode = false;
			}

			return dist > 2 && dist < 50;
		}

	}

	@Override
	public boolean continueExecuting() {
		return this.shouldExecute() || !this.owner.getNavigator().noPath();
	}

	@Override
	public void updateTask() {
		timer--;
		if(timer <= 0) {
			if(artilleryMode) {
				fireArtilleryShell();
			}
			timer = delay;
		}
		this.owner.rotationYaw = this.owner.rotationYawHead;
	}

	protected static CasingEjector ejector = new CasingEjector().setMotion(0, 0.6, -1).setAngleRange(0.1F, 0.1F);

	protected CasingEjector getEjector() {
		return ejector;
	}

	protected SpentCasing cachedCasingConfig = null;

	protected Vec3 getCasingSpawnPos() {
		return Vec3.createVectorHelper(owner.posX, owner.posY, owner.posZ);
	}

	protected void spawnCasing() {
		cachedCasingConfig = new SpentCasing(CasingType.SHOTGUN).setColor(0xE5DD00, SpentCasing.COLOR_CASE_12GA).setScale(10F).register("TankGa").setupSmoke(0.02F, 0.5D, 60, 20).setMaxAge(60);

		if(cachedCasingConfig == null) return;
		CasingEjector ej = getEjector();

		Vec3 spawn = this.getCasingSpawnPos();
		NBTTagCompound data = new NBTTagCompound();
		data.setString("type", "casing");
		data.setFloat("pitch", (float) 0);
		data.setFloat("yaw", (float) owner.rotationYawHead);
		data.setBoolean("crouched", false);
		data.setString("name", cachedCasingConfig.getName());
		if(ej != null) data.setInteger("ej", ej.getId());
		PacketDispatcher.wrapper.sendToAllAround(new AuxParticlePacketNT(data, spawn.xCoord, spawn.yCoord, spawn.zCoord), new TargetPoint(owner.worldObj.provider.dimensionId, owner.posX, owner.posY, owner.posZ, 50));

		cachedCasingConfig = null;
	}

	private void fireArtilleryShell() {
		if(reloadTimer <= 0) {
			double xOffset = Math.cos(Math.toRadians(owner.rotationYawHead)) * 0;
			double zOffset = Math.sin(Math.toRadians(owner.rotationYawHead)) * 1.5;
			double yOffset = 10;

			double targetX = target.posX - (owner.posX + xOffset);
			double targetY = target.posY + target.getEyeHeight() - (owner.posY + yOffset);
			double targetZ = target.posZ - (owner.posZ + zOffset);

			double distance = Math.sqrt(targetX * targetX + targetY * targetY + targetZ * targetZ);
			targetX /= distance;
			targetY /= distance;
			targetZ /= distance;
			spawnCasing();

			if (distance > 30) {
				if(owner.getHealth() < 30) {
					EntityBulletBaseMK4 bullet = new EntityBulletBaseMK4(owner, XFactoryRocket.rocket_qd[3], 4, 0.01F, owner.posX, owner.posY + 3, owner.posZ);
					bullet.setPosition(owner.posX, owner.posY + 3, owner.posZ);
					bullet.motionX = targetX * 0.5D;
					bullet.motionZ = targetZ * 0.5D;
					bullet.motionY = targetY * 0.1D;
					owner.worldObj.spawnEntityInWorld(bullet);
				}else {
					EntityBulletBaseMK4 bullet = new EntityBulletBaseMK4(owner, XFactoryRocket.rocket_qd[1], 4, 0.01F, owner.posX, owner.posY + 3, owner.posZ);
					bullet.setPosition(owner.posX, owner.posY + 3, owner.posZ);
					bullet.motionX = targetX * 0.5D;
					bullet.motionZ = targetZ * 0.5D;
					bullet.motionY = targetY * 0.1D;
					owner.worldObj.spawnEntityInWorld(bullet);
				}
			} else {
				for (int i = 0; i < 8; i++) {
					EntityBulletBaseMK4 bullet = new EntityBulletBaseMK4(owner, XFactory12ga.g12_explosive, 4, 0.01F, owner.posX, owner.posY + 3, owner.posZ);
					bullet.setPosition(owner.posX , owner.posY + 3, owner.posZ);

					bullet.motionX = targetX * (0.2 + (0.5 * Math.random()));
					bullet.motionZ = targetZ * (0.2 + (0.5 * Math.random()));
					bullet.motionY = targetY * 0.1;

					owner.worldObj.spawnEntityInWorld(bullet);
				}
			}

			owner.worldObj.playSoundEffect(owner.posX, owner.posY, owner.posZ, "hbm:turret.jeremy_fire", 25.0F, 1.0F);
			reloadTimer = reloadDelay;
		} else {
			reloadTimer--;
		}

		if(reloadTimer == 20) {
			owner.worldObj.playSoundEffect(owner.posX, owner.posY, owner.posZ, "hbm:turret.jeremy_reload", 3.0F, 1.0F);
		}
	}

}