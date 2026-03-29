package com.hbm.entity.mob.siege;

import com.hbm.entity.mob.EntityUFOBase;
import com.hbm.entity.projectile.EntityBulletBeamBase;
import com.hbm.items.weapon.sedna.factory.XFactoryEnergy;

import api.hbm.entity.IRadiationImmune;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EntityDamageSource;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

public class EntitySiegeUFO extends EntityUFOBase implements IRadiationImmune {

	private int attackCooldown;
	private double lastTargetX;
	private double lastTargetY;
	private double lastTargetZ;
	
	private int lifetime = 0;
	private boolean isRetreating = false;
	private static final int MAX_LIFETIME = 60 * 2 * 20;
	
	public EntitySiegeUFO(World world) {
		super(world);
		this.setSize(1.5F, 1F);
	}

	@Override
	protected void entityInit() {
		super.entityInit();
		this.getDataWatcher().addObject(12, (int) 0);
	}
	
	public void setTier(SiegeTier tier) {
		this.getDataWatcher().updateObject(12, tier.id);

		this.getEntityAttribute(SharedMonsterAttributes.movementSpeed).setBaseValue(tier.speedMod);
		this.getEntityAttribute(SharedMonsterAttributes.maxHealth).setBaseValue(tier.health * 0.25);
		this.setHealth(this.getMaxHealth());
	}
	
	public SiegeTier getTier() {
		SiegeTier tier = SiegeTier.tiers[this.getDataWatcher().getWatchableObjectInt(12)];
		return tier != null ? tier : SiegeTier.CLAY;
	}
	
	@Override
	public boolean attackEntityFrom(DamageSource source, float damage) {
		
		if(this.isEntityInvulnerable())
			return false;
		
		SiegeTier tier = this.getTier();
		
		if(tier.fireProof && source.isFireDamage()) {
			this.extinguish();
			return false;
		}
		
		if(tier.noFriendlyFire && source instanceof EntityDamageSource && !(((EntityDamageSource) source).getEntity() instanceof EntityPlayer))
			return false;
		
		damage -= tier.dt;
		
		if(damage < 0) {
			worldObj.playSoundAtEntity(this, "random.break", 5F, 1.0F + rand.nextFloat() * 0.5F);
			return false;
		}
		
		damage *= (1F - tier.dr);
		
		return super.attackEntityFrom(source, damage);
	}

	@Override
	protected void updateEntityActionState() {
		
		if(!worldObj.isRemote) {
			lifetime++;
			if(lifetime > MAX_LIFETIME) {
				isRetreating = true;
			}

			if(isRetreating) {
				this.target = null;
				this.setWaypoint((int)posX, 255, (int)posZ);
				approachPosition(20);
				if(this.posY > 250 || lifetime > MAX_LIFETIME + 300) {
					this.setDead();
					return;
				}
			}
		}

		super.updateEntityActionState();

		if(this.courseChangeCooldown > 0) {
			this.courseChangeCooldown--;
		}
		if(this.scanCooldown > 0) {
			this.scanCooldown--;
		}
		
		if(!worldObj.isRemote) {
			if(this.attackCooldown > 0) {
				this.attackCooldown--;
			}

			if(!isRetreating && this.target != null) {

				if(this.lastTargetX == 0 && this.lastTargetY == 0 && this.lastTargetZ == 0) {
					this.lastTargetX = this.target.posX;
					this.lastTargetY = this.target.posY + this.target.getEyeHeight();
					this.lastTargetZ = this.target.posZ;
				}

				if(rand.nextInt(10) == 0) {
					this.lastTargetX = this.target.posX;
					this.lastTargetY = this.target.posY + this.target.getEyeHeight();
					this.lastTargetZ = this.target.posZ;
				}
				
				if(this.attackCooldown == 0) {
					this.attackCooldown = 20 + rand.nextInt(25);

					double spawnX = this.posX;
					double spawnY = this.posY;
					double spawnZ = this.posZ;

					EntityBulletBeamBase bullet = new EntityBulletBeamBase(
							this,
							XFactoryEnergy.energy_emerald_overcharge.setKnockback(0),
							8F
					);

					bullet.setPosition(spawnX, spawnY, spawnZ);

					Vec3 delta = Vec3.createVectorHelper(
							lastTargetX - spawnX,
							lastTargetY - spawnY,
							lastTargetZ - spawnZ
					);

					bullet.setRotationsFromVector(delta);
					bullet.performHitscanExternal(250D);

					this.worldObj.spawnEntityInWorld(bullet);
					this.playSound("hbm:entity.bfashoot", 2.0F, 1.0F);
				}
			}
		}
		
		if(this.courseChangeCooldown > 0) {
			    double speed = this.target == null ? 0.5D : 1.0D + (this.getEntityAttribute(SharedMonsterAttributes.movementSpeed).getAttributeValue() * 1.0D);
			    
			    if(isRetreating) speed *= 2.0D; 
			    
			    approachPosition(speed);
			}
		
		
	}

	
	@Override
	public IEntityLivingData onSpawnWithEgg(IEntityLivingData data) {
		this.setTier(SiegeTier.tiers[rand.nextInt(SiegeTier.getLength())]);
		return super.onSpawnWithEgg(data);
	}
	
	@Override
	public void writeEntityToNBT(NBTTagCompound nbt) {
		super.writeEntityToNBT(nbt);
		nbt.setInteger("siegeTier", this.getTier().id);
		nbt.setInteger("lifetime", this.lifetime);
		nbt.setBoolean("isRetreating", this.isRetreating);
	}

	@Override
	public void readEntityFromNBT(NBTTagCompound nbt) {
		super.readEntityFromNBT(nbt);
		this.setTier(SiegeTier.tiers[nbt.getInteger("siegeTier")]);
		this.lifetime = nbt.getInteger("lifetime");
		this.isRetreating = nbt.getBoolean("isRetreating");
	}

	@Override
	protected void dropFewItems(boolean byPlayer, int fortune) {
		for (ItemStack drop : this.getTier().dropItem) {
			this.entityDropItem(drop.copy(), 0F);

		}
	}
}