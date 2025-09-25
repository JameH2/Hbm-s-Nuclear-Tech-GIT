package com.hbm.entity.mob;

import com.hbm.entity.mob.ai.EntityAIBehemothGun;
import com.hbm.entity.mob.ai.EntityAIMaskmanMinigun;
import com.hbm.entity.mob.ai.EntityAIStepTowardsTarget;

import api.hbm.entity.ISuffocationImmune;
import net.minecraft.command.IEntitySelector;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IRangedAttackMob;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIHurtByTarget;
import net.minecraft.entity.ai.EntityAINearestAttackableTarget;
import net.minecraft.entity.ai.EntityAIPanic;
import net.minecraft.entity.ai.EntityAIWander;
import net.minecraft.entity.item.EntityTNTPrimed;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.passive.IAnimals;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityEgg;
import net.minecraft.potion.Potion;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EntityDamageSourceIndirect;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;

//no model yet, im gonna take this low n slow
public class EntityWarBehemoth extends EntityMob implements IMob, IAnimals, ISuffocationImmune {
    private int stepTimer = 0;
    public double headTargetYaw; 

    private static final IEntitySelector selector = new IEntitySelector() {
		public boolean isEntityApplicable(Entity p_82704_1_) {
			return !(p_82704_1_ instanceof EntityWarBehemoth);
		}
	};

    public EntityWarBehemoth(World p_i1733_1_)
    {
        super(p_i1733_1_);
        this.setSize(1.75F, 6.35F);
        this.getNavigator().setAvoidsWater(true);
        this.stepHeight = 5.0F;
        this.targetTasks.addTask(1, new EntityAINearestAttackableTarget(this, EntityPlayer.class, 0, true));
        this.targetTasks.addTask(2, new EntityAINearestAttackableTarget(this, EntityLiving.class, 0, true, true, selector));
        //this.tasks.addTask(3, new EntityAIStepTowardsTarget(this, 4, 0.18, 20, 60, 0.6));
		this.tasks.addTask(3, new EntityAIStepTowardsTarget(this, 50, 0.3D, 50, 10, 0.6));
        //this.tasks.addTask(4, new EntityAIAttackOnCollide(this, 0.2D, false));
		this.tasks.addTask(4, new EntityAIBehemothGun(this, true, true, 3, 35, 30));
		this.targetTasks.addTask(5, new EntityAIHurtByTarget(this, false));
		this.jumpMovementFactor = 0;

    }
    

	@Override
	public boolean attackEntityFrom(DamageSource source, float amount) {
		
		if(source instanceof EntityDamageSourceIndirect && ((EntityDamageSourceIndirect) source).getSourceOfDamage() instanceof EntityEgg && rand.nextInt(10) == 0) {
			this.experienceValue = 0;
			this.setHealth(0);
			return true;
		}

		if(source.isFireDamage())
			amount = 0;
		if(source.isMagicDamage())
			amount = 0;
		if(source.isProjectile())
			amount *= 0.25F;
		if(source.isExplosion())
			amount *= 0.5F;

		if(amount > 50) {
			amount = 50 + (amount - 50) * 0.25F;
		}

		return super.attackEntityFrom(source, amount);
	}
	
	@Override
	protected void applyEntityAttributes() {
		super.applyEntityAttributes();
		this.getEntityAttribute(SharedMonsterAttributes.followRange).setBaseValue(50.0D);
		this.getEntityAttribute(SharedMonsterAttributes.attackDamage).setBaseValue(15.0D);
		this.getEntityAttribute(SharedMonsterAttributes.knockbackResistance).setBaseValue(100.0D);
		this.getEntityAttribute(SharedMonsterAttributes.maxHealth).setBaseValue(200.0D);
	}

    @Override
	protected String getHurtSound()
    {
        return "hbm:entity.cybercrab";
    }
	@Override
	public boolean isAIEnabled() {
		return true;
	}
    @Override
    public void onUpdate() {
        super.onUpdate();
        if (!this.worldObj.isRemote) {
            this.motionY -= 0.10D;  // Increase the downward pull for heavier gravity (adjust value as needed)
        }

    }
   
    
	protected void entityInit() {
		super.entityInit();
		this.dataWatcher.addObject(19, (int) 0);
		
	}
	
	protected boolean canDespawn() {
		return false;
	}
	
    @Override
    public void setAttackTarget(EntityLivingBase entity) {
        super.setAttackTarget(entity);
        this.dataWatcher.updateObject(19, entity != null ? entity.getEntityId() : 0);
    }
}
