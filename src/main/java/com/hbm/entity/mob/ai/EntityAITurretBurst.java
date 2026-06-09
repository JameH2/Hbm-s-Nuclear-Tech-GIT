package com.hbm.entity.mob.ai;

import com.hbm.entity.projectile.EntityBulletBaseMK4;
import com.hbm.handler.CasingEjector;
import com.hbm.items.weapon.sedna.factory.XFactory12ga;
import com.hbm.items.weapon.sedna.factory.XFactory762mm;
import com.hbm.packet.PacketDispatcher;
import com.hbm.packet.toclient.AuxParticlePacketNT;
import com.hbm.particle.SpentCasing;
import com.hbm.particle.SpentCasing.CasingType;

import cpw.mods.fml.common.network.NetworkRegistry.TargetPoint;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;

public class EntityAITurretBurst extends EntityAIBase {

	private EntityCreature owner;
	private EntityLivingBase target;

	private final int closeRange      = 12;
	private final int maxRange        = 50;
	private final int burstSize       = 5;
	private final int burstShotDelay  = 2;
	private final int burstCooldown   = 25;
	private final int shotgunPellets  = 8;
	private final int shotgunCooldown = 40;
	private final double bulletSpeed  = 0.5;
	private final double strafeSpeed  = 0.35;
	private final int strafeMinTime   = 20;
	private final int strafeMaxTime   = 50;
	private final int pathInterval    = 5;
	private final int losInterval     = 3;

	private int shotTimer;
	private int cooldownTimer;
	private int burstRemaining;
	private int strafeTimer;
	private int strafeDir;
	private boolean hadLOS;

	private int pathCooldown;
	private int losCooldown;
	private boolean cachedLOS;

	public EntityAITurretBurst(EntityCreature owner) {
		this.owner = owner;
		this.setMutexBits(3);
	}

	@Override
	public boolean shouldExecute() {
		EntityLivingBase t = this.owner.getAttackTarget();
		if(t == null || !t.isEntityAlive()) return false;
		if(owner.getDistanceToEntity(t) > maxRange) return false;
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
		this.burstRemaining = 0;
		this.shotTimer = 0;
		this.strafeTimer = 0;
		this.pathCooldown = 0;
		this.losCooldown = 0;
		this.owner.getNavigator().clearPathEntity();
	}

	@Override
	public void updateTask() {
		double dist = owner.getDistanceToEntity(target);
		boolean closeMode = dist <= closeRange;
		boolean los = hasLineOfSight();

		owner.getLookHelper().setLookPositionWithEntity(target, 30F, 30F);
		updateMovement(dist, los);

		if(!los) {
			burstRemaining = 0;
			cooldownTimer = Math.max(cooldownTimer, 5);
			hadLOS = false;
			return;
		}

		if(!hadLOS) {
			cooldownTimer = Math.max(cooldownTimer, 5);
			hadLOS = true;
		}

		if(cooldownTimer > 0) {
			cooldownTimer--;
			return;
		}

		if(burstRemaining <= 0) {
			burstRemaining = closeMode ? 1 : burstSize;
			shotTimer = 0;
		}

		if(shotTimer > 0) {
			shotTimer--;
			return;
		}

		if(closeMode) {
			fireShotgun();
			cooldownTimer = shotgunCooldown;
			burstRemaining = 0;
		} else {
			fireAssaultShot();
			burstRemaining--;
			if(burstRemaining <= 0) cooldownTimer = burstCooldown;
			else shotTimer = burstShotDelay;
		}
	}

	private void updateMovement(double dist, boolean los) {
		if(pathCooldown > 0) {
			pathCooldown--;
			return;
		}
		pathCooldown = pathInterval;

		if(!los || dist > maxRange * 0.8) {
			owner.getNavigator().tryMoveToEntityLiving(target, strafeSpeed);
			strafeTimer = 0;
			return;
		}

		if(dist < closeRange * 0.6) {
			Vec3 away = Vec3.createVectorHelper(owner.posX - target.posX, 0, owner.posZ - target.posZ).normalize();
			double bx = owner.posX + away.xCoord * 4;
			double bz = owner.posZ + away.zCoord * 4;
			owner.getNavigator().tryMoveToXYZ(bx, owner.posY, bz, strafeSpeed);
			strafeTimer = 0;
			return;
		}

		if(strafeTimer <= 0) {
			strafeDir = owner.getRNG().nextBoolean() ? 1 : -1;
			strafeTimer = strafeMinTime + owner.getRNG().nextInt(strafeMaxTime - strafeMinTime);
		} else {
			strafeTimer -= pathInterval;
		}

		double dx = target.posX - owner.posX;
		double dz = target.posZ - owner.posZ;
		double len = Math.sqrt(dx * dx + dz * dz);
		if(len < 0.01) return;
		double perpX = -dz / len * strafeDir;
		double perpZ =  dx / len * strafeDir;

		double sx = owner.posX + perpX * 4;
		double sz = owner.posZ + perpZ * 4;
		if(!owner.getNavigator().tryMoveToXYZ(sx, owner.posY, sz, strafeSpeed)) {
			strafeTimer = 0;
			strafeDir = -strafeDir;
		}
	}

	private boolean hasLineOfSight() {
		if(losCooldown > 0) {
			losCooldown--;
			return cachedLOS;
		}
		losCooldown = losInterval;

		Vec3 from = Vec3.createVectorHelper(owner.posX, owner.posY + owner.getEyeHeight(), owner.posZ);
		Vec3 to   = Vec3.createVectorHelper(target.posX, target.posY + target.getEyeHeight(), target.posZ);
		MovingObjectPosition mop = owner.worldObj.rayTraceBlocks(from, to);
		cachedLOS = (mop == null);
		return cachedLOS;
	}



	private void fireAssaultShot() {

		double tx = target.posX - owner.posX;
		double ty = target.posY - (owner.posY + 3);
		double tz = target.posZ - owner.posZ;

		double d = Math.sqrt(tx * tx + ty * ty + tz * tz);
		if(d < 0.01) return;
		tx /= d; ty /= d; tz /= d;

		double spread = 0.02;
		tx += owner.getRNG().nextGaussian() * spread;
		ty += owner.getRNG().nextGaussian() * spread;
		tz += owner.getRNG().nextGaussian() * spread;

		EntityBulletBaseMK4 bullet = new EntityBulletBaseMK4(owner, XFactory762mm.r762_ap.setKnockback(0), 2F, 0.01F, owner.posX, owner.posY + 3, owner.posZ);
		bullet.setPosition(owner.posX, owner.posY + 3, owner.posZ);
		bullet.motionX = tx * bulletSpeed;
		bullet.motionY = ty * bulletSpeed;
		bullet.motionZ = tz * bulletSpeed;
		owner.worldObj.spawnEntityInWorld(bullet);

		owner.playSound("hbm:weapon.fire.rifle", 6.0F, 1.0F);
		spawnCasing(false);
	}

	private void fireShotgun() {
		
		double tx = target.posX - owner.posX;
		double ty = target.posY - (owner.posY + 3);
		double tz = target.posZ - owner.posZ;

		double d = Math.sqrt(tx * tx + ty * ty + tz * tz);
		if(d < 0.01) return;
		tx /= d; ty /= d; tz /= d;

		for(int i = 0; i < shotgunPellets; i++) {
			double spread = 0.12;
			double sx = tx + owner.getRNG().nextGaussian() * spread;
			double sy = ty + owner.getRNG().nextGaussian() * spread;
			double sz = tz + owner.getRNG().nextGaussian() * spread;

			EntityBulletBaseMK4 pellet = new EntityBulletBaseMK4(owner, XFactory12ga.g12_flechette, 4, 0.01F, owner.posX, owner.posY + 3, owner.posZ);
			pellet.setPosition(owner.posX, owner.posY + 3, owner.posZ);
			pellet.motionX = sx * (bulletSpeed + owner.getRNG().nextDouble() * 0.1);
			pellet.motionY = sy * (bulletSpeed + owner.getRNG().nextDouble() * 0.1);
			pellet.motionZ = sz * (bulletSpeed + owner.getRNG().nextDouble() * 0.1);
			owner.worldObj.spawnEntityInWorld(pellet);
		}

		owner.worldObj.playSoundEffect(owner.posX, owner.posY, owner.posZ, "hbm:weapon.shotgunShoot", 12.0F, 1.0F);
		spawnCasing(true);
	}

	protected static CasingEjector ejector = new CasingEjector().setMotion(0, 0.6, -1).setAngleRange(0.1F, 0.1F);
	protected SpentCasing cachedCasingConfig = null;

	protected void spawnCasing(boolean shotgun) {
		if(owner.worldObj.getClosestPlayerToEntity(owner, 64.0D) == null) return;

		if(shotgun) {
			cachedCasingConfig = new SpentCasing(CasingType.SHOTGUN).setColor(0xE5DD00, SpentCasing.COLOR_CASE_12GA).setScale(5F).register("TurretGa").setupSmoke(0.02F, 0.5D, 60, 20).setMaxAge(60);
		} else {
			cachedCasingConfig = new SpentCasing(CasingType.STRAIGHT).setColor(0xC9B870, SpentCasing.COLOR_CASE_BRASS).setScale(3F).register("Turret762").setupSmoke(0.01F, 0.3D, 40, 15).setMaxAge(40);
		}
		if(cachedCasingConfig == null) return;

		NBTTagCompound data = new NBTTagCompound();
		data.setString("type", "casing");
		data.setFloat("pitch", 0);
		data.setFloat("yaw", (float) owner.rotationYawHead);
		data.setBoolean("crouched", false);
		data.setString("name", cachedCasingConfig.getName());
		if(ejector != null) data.setInteger("ej", ejector.getId());
		PacketDispatcher.wrapper.sendToAllAround(
			new AuxParticlePacketNT(data, owner.posX, owner.posY, owner.posZ),
			new TargetPoint(owner.worldObj.provider.dimensionId, owner.posX, owner.posY, owner.posZ, 50));

		cachedCasingConfig = null;
	}
}