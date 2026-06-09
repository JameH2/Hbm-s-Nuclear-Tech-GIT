package com.hbm.entity.mob;

import java.util.List;

import com.hbm.entity.mob.ai.EntityAIStepTowardsTarget;
import com.hbm.entity.projectile.EntityBombletZeta;
import com.hbm.entity.projectile.EntityBulletBaseMK4;
import com.hbm.entity.mob.ai.EntityAISamLaunch;
import com.hbm.particle.helper.ExplosionCreator;
import com.hbm.util.ParticleUtil;

import api.hbm.entity.IRadiationImmune;
import api.hbm.entity.ISuffocationImmune;
import net.minecraft.command.IEntitySelector;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIHurtByTarget;
import net.minecraft.entity.ai.EntityAINearestAttackableTarget;
import net.minecraft.entity.ai.EntityAIWander;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityEgg;
import net.minecraft.potion.Potion;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EntityDamageSourceIndirect;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;

public class EntitySamBot extends EntityMob implements IRadiationImmune, ISuffocationImmune {

	public double headTargetYaw;
	public double headTargetPitch;

	private int targetScanCooldown = 0;

	private static final IEntitySelector aerialSelector = new IEntitySelector() {
		public boolean isEntityApplicable(Entity entity) {
			if(entity instanceof EntitySamBot) return false;
			return isAerial(entity);
		}
	};

	public EntitySamBot(World world) {
		super(world);

		this.setSize(2.45F, 3.25F);
		this.getNavigator().setAvoidsWater(true);

		this.deathTime = -50;
		this.isImmuneToFire = true;
		this.renderDistanceWeight = 24.0D;

		this.tasks.addTask(5, new EntityAIWander(this, 0.2D));
		this.tasks.addTask(3, new EntityAIStepTowardsTarget(this, 50, 0.2D, 200, 20, 0.6));
		this.tasks.addTask(4, new EntityAISamLaunch(this));

		this.targetTasks.addTask(1, new EntityAINearestAttackableTarget(this, EntityPlayer.class, 10, true) {
			@Override
			public boolean shouldExecute() {
				return super.shouldExecute() && taskOwner.getAttackTarget() != null && isAerial(taskOwner.getAttackTarget());
			}
		});
		this.targetTasks.addTask(2, new EntityAINearestAttackableTarget(this, EntityLiving.class, 10, true, true, aerialSelector));
		this.targetTasks.addTask(3, new EntityAIHurtByTarget(this, false));
	}

	public static boolean isAerial(Entity e) {
		if(e == null) return false;
		if(e.onGround) return false;
		if(e instanceof EntityBulletBaseMK4) return false;
		if(e instanceof EntityBombletZeta) return false; 

		int feetY = MathHelper.floor_double(e.boundingBox.minY);

		for(int dy = 1; dy <= 6; dy++) {
			if(e.worldObj.getBlock(
				MathHelper.floor_double(e.posX),
				feetY - dy,
				MathHelper.floor_double(e.posZ)
			).getMaterial().isSolid()) {
				return false;
			}
		}
		return true;
	}

	@Override
	public void onUpdate() {
		super.onUpdate();

		if(!worldObj.isRemote) {
			if(targetScanCooldown > 0) {
				targetScanCooldown--;
			} else {
				targetScanCooldown = 10;
				scanForAerialTargets();
			}
		}
	}

	@SuppressWarnings("unchecked")
	private void scanForAerialTargets() {
		EntityLivingBase current = this.getAttackTarget();
		if(current != null && current.isEntityAlive() && isAerial(current) && this.getDistanceToEntity(current) < 80) return;

		List<Entity> candidates = worldObj.getEntitiesWithinAABBExcludingEntity(this, this.boundingBox.expand(80, 80, 80));

		Entity bestLiving = null;
		Entity bestNonLiving = null;

		for(Entity e : candidates) {
			if(e == this || e instanceof EntitySamBot || !isAerial(e)) continue;

			double d = this.getDistanceSqToEntity(e);
			if(e instanceof EntityLivingBase) {
				if(bestLiving == null || d < this.getDistanceSqToEntity(bestLiving)) bestLiving = e;
			} else {
				if(bestNonLiving == null || d < this.getDistanceSqToEntity(bestNonLiving)) bestNonLiving = e;
			}
		}

		if(bestLiving != null) setAttackTarget((EntityLivingBase) bestLiving);
		else if(bestNonLiving != null) dataWatcher.updateObject(20, bestNonLiving.getEntityId());
		else setAttackTarget(null);
	}

	@Override
	protected void onDeathUpdate() {
		if(this.deathTime == -30) {
			worldObj.playSoundAtEntity(this, "hbm:entity.chopperDamage", 10.0F, 1.0F);
		}
		if(this.deathTime >= -30 && !worldObj.isRemote) {
			ParticleUtil.spawnGasFlame(worldObj, this.posX, this.posY + 1, this.posZ, rand.nextGaussian(), 0.4, rand.nextGaussian());
		}
		if(this.deathTime == -5 && !worldObj.isRemote) {
			worldObj.newExplosion(this, posX, posY, posZ, 6F, true, false);
			ExplosionCreator.composeEffectSmall(worldObj, this.posX, this.posY, this.posZ);
			this.setDead();
		}
		super.onDeathUpdate();
	}

	@Override
	protected Entity findPlayerToAttack() {
		if(this.isPotionActive(Potion.blindness)) return null;
		EntityPlayer p = this.worldObj.getClosestVulnerablePlayerToEntity(this, 64D);
		return (p != null && isAerial(p)) ? p : null;
	}

	@Override
	protected void entityInit() {
		super.entityInit();
		this.dataWatcher.addObject(20, (int) 0);
	}

	@Override
	protected boolean canDespawn() {
		return false;
	}

	@Override
	public void setAttackTarget(EntityLivingBase entity) {
		super.setAttackTarget(entity);
		this.dataWatcher.updateObject(20, entity != null ? entity.getEntityId() : 0);
	}

	@Override
	public boolean attackEntityFrom(DamageSource source, float amount) {
		if(source instanceof EntityDamageSourceIndirect && ((EntityDamageSourceIndirect) source).getSourceOfDamage() instanceof EntityEgg && rand.nextInt(10) == 0) {
			this.experienceValue = 0;
			this.setHealth(0);
			return true;
		}

		if(source.isFireDamage()) amount = 0;
		if(source.isMagicDamage()) amount = 0;
		if(source.isProjectile()) amount *= 0.25F;
		if(source.isExplosion()) amount *= 0.5F;

		if(amount > 50) {
			amount = 50 + (amount - 50) * 0.25F;
		}

		return super.attackEntityFrom(source, amount);
	}

	@Override
	protected void applyEntityAttributes() {
		super.applyEntityAttributes();
		this.getEntityAttribute(SharedMonsterAttributes.followRange).setBaseValue(80.0D);
		this.getEntityAttribute(SharedMonsterAttributes.attackDamage).setBaseValue(25.0D);
		this.getEntityAttribute(SharedMonsterAttributes.knockbackResistance).setBaseValue(1.0D);
		this.getEntityAttribute(SharedMonsterAttributes.maxHealth).setBaseValue(200.0D);
		this.getEntityAttribute(SharedMonsterAttributes.movementSpeed).setBaseValue(1.28D);
	}

	@Override
	protected String getHurtSound() {
		return "hbm:entity.cybercrab";
	}

	@Override
	public boolean isAIEnabled() {
		return true;
	}
}