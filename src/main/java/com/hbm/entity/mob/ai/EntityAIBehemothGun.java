package com.hbm.entity.mob.ai;

import com.hbm.entity.projectile.EntityArtilleryShell;
import com.hbm.entity.projectile.EntityBulletBaseMK4;
import com.hbm.entity.projectile.EntityBulletBaseNT;
import com.hbm.handler.BulletConfigSyncingUtil;
import com.hbm.items.weapon.sedna.factory.XFactory12ga;
import com.hbm.items.weapon.sedna.factory.XFactory762mm;

import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.util.Vec3;

public class EntityAIBehemothGun extends EntityAIBase {
	
	private EntityCreature owner;
    private EntityLivingBase target;
    private int delay;
    private int timer;
    private int switchAttackDistance;
    private boolean artilleryMode;
    private int reloadTimer;
    private int reloadDelay;

	public EntityAIBehemothGun(EntityCreature owner, boolean checkSight, boolean nearbyOnly, int delay, int switchAttackDistance, int reloadDelay) {
		this.owner = owner;
		this.delay = delay;
		this.timer = delay;
		this.switchAttackDistance = switchAttackDistance;
		this.artilleryMode = false;
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
            if(dist > switchAttackDistance) {
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
			} else {
				fireGatlingBarrage();
			}
			timer = delay;
		}
		this.owner.rotationYaw = this.owner.rotationYawHead;
    }

	private void fireGatlingBarrage() {
	    double xOffset = Math.cos(Math.toRadians(owner.rotationYaw)) * 1.5;
	    double zOffset = Math.sin(Math.toRadians(owner.rotationYaw)) * 1.5;
	    double yOffset = 10; 

	    double targetX = target.posX - (owner.posX + xOffset);
	    double targetY = target.posY + target.getEyeHeight() - (owner.posY + yOffset);
	    double targetZ = target.posZ - (owner.posZ + zOffset);

	    double distance = Math.sqrt(targetX * targetX + targetY * targetY + targetZ * targetZ);
	    targetX /= distance;
	    targetY /= distance;
	    targetZ /= distance;

	    EntityBulletBaseMK4 bullet = new EntityBulletBaseMK4(owner, XFactory762mm.r762_ap.setKnockback(0), 0.5F, 0.01F, owner.posX + xOffset, owner.posY + yOffset, owner.posZ + zOffset);
	    bullet.setPosition(owner.posX + xOffset, owner.posY + yOffset, owner.posZ + zOffset);
	    
	    bullet.motionX = targetX * 0.5D;  
	    bullet.motionY = targetY * 0.5D;
	    bullet.motionZ = targetZ * 0.5D;

	    owner.worldObj.spawnEntityInWorld(bullet);
	    owner.playSound("hbm:weapon.calShoot", 15.0F, 1.0F);
	}

	private void fireArtilleryShell() {
		if(reloadTimer <= 0) {
			EntityArtilleryShell grenade = new EntityArtilleryShell(owner.worldObj);
			grenade.setType(9);
			grenade.setPosition(owner.posX, owner.posY + 14, owner.posZ);
			Vec3 vec = Vec3.createVectorHelper(target.posX - owner.posX, 0, target.posZ - owner.posZ);
			grenade.motionX = vec.xCoord * 0.05D;
			grenade.motionY = 0.5D + owner.getRNG().nextDouble() * 0.5D;
			grenade.motionZ = vec.zCoord * 0.05D;
			grenade.setThrowableHeading(grenade.motionX , grenade.motionY * 6, grenade.motionZ , 5F, 0);
			grenade.setTarget(target.posX, target.posZ, target.posY);
			owner.worldObj.playSoundEffect(owner.posX, owner.posY, owner.posZ, "hbm:turret.jeremy_fire", 25.0F, 1.0F);
			owner.worldObj.spawnEntityInWorld(grenade);
			reloadTimer = reloadDelay;
		} else {
			reloadTimer--;
		}
	}

}