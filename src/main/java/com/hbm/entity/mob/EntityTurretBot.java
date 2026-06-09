package com.hbm.entity.mob;

import com.hbm.entity.mob.ai.EntityAIStepTowardsTarget;
import com.hbm.entity.mob.ai.EntityAITankshell;
import com.hbm.entity.mob.ai.EntityAITurretBurst;
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
import net.minecraft.util.DamageSource;
import net.minecraft.util.EntityDamageSourceIndirect;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;

public class EntityTurretBot extends EntityMob implements IRadiationImmune, ISuffocationImmune {

	public double headTargetYaw;
	public double headTargetPitch;

	private static final IEntitySelector selector = new IEntitySelector() {
		public boolean isEntityApplicable(Entity entity) {
			return !(entity instanceof EntityTurretBot);
		}
	};

	public EntityTurretBot(World world) {
		super(world);

		this.setSize(2.45F, 3.25F);
		this.getNavigator().setAvoidsWater(true);

		this.deathTime = -50;
		this.isImmuneToFire = true;
		this.renderDistanceWeight = 24.0D;

		this.tasks.addTask(5, new EntityAIWander(this, 0.4D));
		this.tasks.addTask(4, new EntityAITurretBurst(this));
		this.targetTasks.addTask(1, new EntityAINearestAttackableTarget(this, EntityPlayer.class, 0, true));
		this.targetTasks.addTask(2, new EntityAINearestAttackableTarget(this, EntityLiving.class, 0, true, true, selector));
		this.targetTasks.addTask(3, new EntityAIHurtByTarget(this, false));
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
			worldObj.newExplosion(this, posX, posY, posZ, 5F, true, false);
			ExplosionCreator.composeEffectSmall(worldObj, this.posX, this.posY, this.posZ);
			this.setDead();
		}

		super.onDeathUpdate();
	}

	@Override
	protected Entity findPlayerToAttack() {
		if(this.isPotionActive(Potion.blindness))
			return null;

		return this.worldObj.getClosestVulnerablePlayerToEntity(this, 64D);
	}

	@Override
	protected void entityInit() {
		super.entityInit();
		this.dataWatcher.addObject(20, (int) 0);
		this.dataWatcher.addObject(21, (float) 0F);  

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
		this.getEntityAttribute(SharedMonsterAttributes.followRange).setBaseValue(60.0D);
		this.getEntityAttribute(SharedMonsterAttributes.attackDamage).setBaseValue(15.0D);
		this.getEntityAttribute(SharedMonsterAttributes.knockbackResistance).setBaseValue(1.0D);
		this.getEntityAttribute(SharedMonsterAttributes.maxHealth).setBaseValue(180.0D);
	}

	@Override
	protected String getHurtSound() {
		return "hbm:entity.cybercrab";
	}

	@Override
	public boolean isAIEnabled() {
		return true;
	}

	@Override
	public void onUpdate() {
		super.onUpdate();

	}

}
