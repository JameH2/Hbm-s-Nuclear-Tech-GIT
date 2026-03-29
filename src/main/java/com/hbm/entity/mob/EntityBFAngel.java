package com.hbm.entity.mob;

import java.util.ArrayList;
import java.util.List;

import com.hbm.dim.CelestialBody;
import com.hbm.dim.trait.CBT_Impact;
import com.hbm.dim.trait.CBT_Invasion;
import com.hbm.dim.trait.CelestialBodyTrait;
import com.hbm.entity.projectile.EntityBulletBaseMK4;
import com.hbm.entity.projectile.EntityBulletBaseNT;
import com.hbm.entity.projectile.EntityBulletBeamBase;
import com.hbm.explosion.ExplosionNukeSmall;
import com.hbm.handler.BulletConfigSyncingUtil;
import com.hbm.handler.threading.PacketThreading;
import com.hbm.items.ModItems;
import com.hbm.items.weapon.sedna.factory.XFactory762mm;
import com.hbm.items.weapon.sedna.factory.XFactoryEnergy;
import com.hbm.items.weapon.sedna.factory.XFactoryFlamer;
import com.hbm.lib.ModDamageSource;
import com.hbm.main.MainRegistry;
import com.hbm.packet.toclient.AuxParticlePacketNT;
import com.hbm.util.ContaminationUtil;
import com.hbm.util.ContaminationUtil.ContaminationType;
import com.hbm.util.ContaminationUtil.HazardType;

import api.hbm.entity.ISuffocationImmune;
import api.hbm.entity.IRadiationImmune;
import cpw.mods.fml.common.network.NetworkRegistry.TargetPoint;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityFlying;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.boss.IBossDisplayData;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.Potion;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.Vec3;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.World;

public class EntityBFAngel extends EntityFlying implements IMob, IBossDisplayData, IRadiationImmune, ISuffocationImmune {

	public int courseChangeCooldown;
	public int scanCooldown;
	/*public double waypointX;
	public double waypointY;
	public double waypointZ;*/
	public int hurtCooldown;
	public int beamTimer;
	private Entity target;
	private List<Entity> secondaries = new ArrayList();
	
	private double lockedX;
	private double lockedY;
	private double lockedZ;
	
	private int aoeCooldown = 0;
	private int aoeStage = 0;
	private int aoeTimer = 0;
	private Vec3[] strikepositions = new Vec3[3];
	private int[] strikeTimers = new int[3];
	private boolean[] strikeArmed = new boolean[3];
	
	public int gatlingCharge;
	public boolean isChargingGatling;
	
	
	
	public EntityBFAngel(World p_i1587_1_) {
		super(p_i1587_1_);
		this.setSize(5F, 4F);
		this.isImmuneToFire = true;
		this.experienceValue = 500;
		this.ignoreFrustumCheck = true;
		this.deathTime = -30;
	}

	@Override
	protected boolean canDespawn() {
		return false;
	}

	@Override
	public boolean attackEntityFrom(DamageSource source, float amount) {

		if(hurtCooldown > 0)
			return false;

		boolean hit = super.attackEntityFrom(source, amount);

		if(hit)
			hurtCooldown = 5;

		return hit;
	}

	@Override
	protected void applyEntityAttributes() {
		super.applyEntityAttributes();
		this.getEntityAttribute(SharedMonsterAttributes.maxHealth).setBaseValue(10000.0D);
	}

	@Override
	protected void updateEntityActionState() {

		if(!this.worldObj.isRemote) {

			if(this.worldObj.difficultySetting == EnumDifficulty.PEACEFUL) {
				this.setDead();
				return;
			}

			if(this.hurtCooldown > 0) {
				this.hurtCooldown--;
			}
		}

		if(this.courseChangeCooldown > 0) {
			this.courseChangeCooldown--;
		}
		if(this.scanCooldown > 0) {
			this.scanCooldown--;
		}

		if(this.target != null && !this.target.isEntityAlive()) {
			this.target = null;
		}

		if(this.scanCooldown <= 0) {
			List<Entity> entities = worldObj.getEntitiesWithinAABB(Entity.class, this.boundingBox.expand(100, 50, 100));
			this.secondaries.clear();
			this.target = null;

			for(Entity entity : entities) {

				if(!entity.isEntityAlive() || !canAttackClass(entity.getClass()))
					continue;

				if(entity instanceof EntityPlayer) {

					if(((EntityPlayer)entity).capabilities.isCreativeMode)
						continue;

					if(((EntityPlayer)entity).isPotionActive(Potion.invisibility.id))
						continue;

					if(this.target == null) {
						this.target = entity;
					} else {
						if(this.getDistanceSqToEntity(entity) < this.getDistanceSqToEntity(this.target)) {
							this.target = entity;
						}
					}
				}

				if(entity instanceof EntityLivingBase && this.getDistanceSqToEntity(entity) < 100 * 100 && this.canEntityBeSeen(entity) && entity != this.target) {
					this.secondaries.add(entity);
				}
			}

			if(this.target == null && !this.secondaries.isEmpty())
				this.target = this.secondaries.get(rand.nextInt(this.secondaries.size()));

			this.scanCooldown = 50;
		}

		if(this.target != null && this.courseChangeCooldown <= 0) {

			Vec3 vec = Vec3.createVectorHelper(this.posX - this.target.posX, 0, this.posZ - this.target.posZ);

			if(rand.nextInt(3) > 0)
				vec.rotateAroundY((float)Math.PI * 2 * rand.nextFloat());

			double length = vec.lengthVector();
			double overshoot = 35;

			int wX = (int)Math.floor(this.target.posX - vec.xCoord / length * overshoot);
			int wZ = (int)Math.floor(this.target.posZ - vec.zCoord / length * overshoot);

			this.setWaypoint(wX, Math.max(this.worldObj.getHeightValue(wX, wZ) + rand.nextInt(15), (int) this.target.posY - 5),  wZ);

			this.courseChangeCooldown = 40 + rand.nextInt(20);
		}

		if(!worldObj.isRemote) {

			if(beamTimer <= 0 && this.getBeam()) {
				this.setBeam(false);
			}

			if(this.target != null) {
				double dist = Math.abs(this.target.posX - this.posX) + Math.abs(this.target.posZ - this.posZ);
				//if(dist < 25)
					//this.beamTimer = 30;
			}

			if(beamTimer > 0) {
				this.beamTimer--;

				if(!this.getBeam()) {
					//worldObj.playSoundAtEntity(this, "hbm:entity.ufoBeam", 10.0F, 1.0F);
					//this.setBeam(true);
				}

				int ix = (int)Math.floor(this.posX);
				int iz = (int)Math.floor(this.posZ);
				int iy = 0;

				for(int i = (int)Math.ceil(this.posY); i >= 0; i--) {

					//if(this.worldObj.getBlock(ix, i, iz) != Blocks.air) {
						//iy = i;
						//break;
					//}
				}

				if(iy < this.posY) {
					List<Entity> entities = worldObj.getEntitiesWithinAABBExcludingEntity(this, AxisAlignedBB.getBoundingBox(this.posX, iy, this.posZ, this.posX, this.posY, this.posZ).expand(5, 0, 5));

					for(Entity e : entities) {
						if(this.canAttackClass(e.getClass())) {
							e.attackEntityFrom(ModDamageSource.causeCombineDamage(this, e), 1000F);
							e.setFire(5);

							if(e instanceof EntityLivingBase)
								ContaminationUtil.contaminate((EntityLivingBase)e, HazardType.RADIATION, ContaminationType.CREATIVE, 5F);
						}
					}

					NBTTagCompound data = new NBTTagCompound();
					data.setString("type", "ufo");
					PacketThreading.createAllAroundThreadedPacket(new AuxParticlePacketNT(data, posX, iy + 0.5, posZ),  new TargetPoint(dimension, posX, iy + 0.5, posZ, 150));
					PacketThreading.createAllAroundThreadedPacket(new AuxParticlePacketNT(data, posX + this.motionX * 0.5, iy + 0.5, posZ + this.motionZ * 0.5),  new TargetPoint(dimension, posX + this.motionX * 0.5, iy + 0.5, posZ + this.motionZ * 0.5, 150));
				}
			}


			if (this.ticksExisted % 300 < 200) {

				if (!this.secondaries.isEmpty()) {
					Entity e = this.secondaries.get(rand.nextInt(this.secondaries.size()));

					if (!e.isEntityAlive()) {
						this.secondaries.remove(e);
						e = null;
					}
				}

				if (this.target != null) {

					if (!isChargingGatling) {
						isChargingGatling = true;
						gatlingCharge = 70;
					} else {

						if (gatlingCharge > 10) {
							lockedX = this.target.posX;
							lockedY = this.target.posY;
							lockedZ = this.target.posZ;
						}

						gatlingCharge--;

						if (gatlingCharge == 30) {
							worldObj.playSoundAtEntity(this, "hbm:entity.bfatalk", 10.0F, 1.0F);
						}

						if (gatlingCharge <= 0) {
							fireBeamAt(lockedX, lockedY, lockedZ);

							isChargingGatling = false;
							gatlingCharge = 0;
						}
					}
				}

			} else {

				if (this.ticksExisted % 20 == 0) {

					if (!this.secondaries.isEmpty()) {
						Entity e = this.secondaries.get(rand.nextInt(this.secondaries.size()));

						if (e.isEntityAlive()) {
							rocketAttack(e);
						} else {
							this.secondaries.remove(e);
						}

					} else if (this.target != null) {
						rocketAttack(this.target);
					}

				} else if (this.ticksExisted % 4 == 2) {

					if (this.target != null) {
						rocketAttack(this.target);
					}
				}
			}
		}
		if(this.target != null) {

			    if(aoeCooldown <= 0 && aoeStage == 0) {
			        aoeStage = 1;

			        for(int i = 0; i < 3; i++) {

			            double offsetX = (rand.nextDouble() - 0.5) * 12;
			            double offsetZ = (rand.nextDouble() - 0.5) * 12;

			            strikepositions[i] = Vec3.createVectorHelper(
			                target.posX + offsetX,
			                target.posY,
			                target.posZ + offsetZ
			            );

			            strikeTimers[i] = -i * 4; 
			            strikeArmed[i] = true;
			        }

			        aoeCooldown = 200;
			    }

			    for(int i = 0; i < 3; i++) {

			        if(strikeArmed[i]) {

			            strikeTimers[i]++;

			            if(strikeTimers[i] < 0) continue;

			            Vec3 pos = strikepositions[i];

			            if(strikeTimers[i] < 60 && strikeTimers[i] % 5 == 0) {
			                NBTTagCompound fx = new NBTTagCompound();
			                fx.setString("type", "vanillaburst");
			                fx.setString("mode", "reddust");
			                fx.setDouble("motion", 0.2D);
			                fx.setInteger("count", 35);
					  worldObj.playSoundEffect(pos.xCoord, pos.yCoord, pos.zCoord, "hbm:weapon.stingerLockOn", 1.0F, 1F);

			                PacketThreading.createAllAroundThreadedPacket(
			                    new AuxParticlePacketNT(fx, pos.xCoord, pos.yCoord, pos.zCoord),
			                    new TargetPoint(this.dimension, pos.xCoord, pos.yCoord, pos.zCoord, 100)
			                );
			            }

			            if(strikeTimers[i] >= 60) {
			       	  fireDownwardBeamAt(pos.xCoord, pos.yCoord, pos.zCoord);
			                strikeArmed[i] = false;
			            }
			        }
			    }

			    boolean allDone = true;
			    for(int i = 0; i < 3; i++) {
			        if(strikeArmed[i]) {
			            allDone = false;
			            break;
			        }
			    }

			    if(allDone) {
			        aoeStage = 0;
			    }

			    if(aoeCooldown > 0) {
			        aoeCooldown--;
			    }
			}
		this.motionX = 0;
		this.motionY = 0;
		this.motionZ = 0;

		if (this.target != null) {

		    if (this.courseChangeCooldown <= 0) {

		        double angle = rand.nextDouble() * Math.PI * 2;
		        double radius = 15 + rand.nextInt(10);

		        
		        int targetX = (int)(this.target.posX + Math.cos(angle) * radius);
		        int targetZ = (int)(this.target.posZ + Math.sin(angle) * radius);
		        int targetY = (int)(this.target.posY + 5 + rand.nextInt(5));

		        this.setWaypoint(targetX, targetY, targetZ);

		        this.courseChangeCooldown = 85;
		    }

		    if (this.courseChangeCooldown <= 10) {

		        double dx = this.getX() - this.posX;
		        double dy = this.getY() - this.posY;
		        double dz = this.getZ() - this.posZ;

		        Vec3 dir = Vec3.createVectorHelper(dx, dy, dz).normalize();

		        double t = this.courseChangeCooldown / 15.0; 
		        double dashSpeed = 2.0D * t;

		        this.motionX = dir.xCoord * dashSpeed;
		        this.motionY = dir.yCoord * dashSpeed;
		        this.motionZ = dir.zCoord * dashSpeed;
		    }

		    this.courseChangeCooldown--;
		}
	
		if (this.target != null) {
			    double dx = this.target.posX - this.posX;
			    double dy = this.target.posY + this.target.height * 0.5 - (this.posY + 1);
			    double dz = this.target.posZ - this.posZ;

			    float targetYaw = (float)(Math.atan2(dz, dx) * 180D / Math.PI) + 90F;

			    float delta = net.minecraft.util.MathHelper.wrapAngleTo180_float(targetYaw - this.rotationYaw);
			    this.rotationYaw += delta * 0.15F;
			    
			    double dist = Math.sqrt(dx * dx + dz * dz);
			    float targetPitch = (float)(-(Math.atan2(dy, dist) * -180D / Math.PI));
			    this.rotationPitch += (targetPitch - this.rotationPitch) * 0.1F;
			}
	}

	protected void onDeathUpdate() {

		if(this.getBeam())
			this.setBeam(false);

		this.motionY -= 0.05D;

		if(this.deathTime == -10) {
			worldObj.playSoundAtEntity(this, "hbm:entity.chopperDamage", 10.0F, 1.0F);
		}

		if(this.deathTime == 19 && !worldObj.isRemote) {
			NBTTagCompound data = new NBTTagCompound();
			data.setString("type", "tinytot");
			PacketThreading.createAllAroundThreadedPacket(new AuxParticlePacketNT(data, posX, posY + 0.5, posZ), new TargetPoint(this.dimension, posX, posY, posZ, 250));
			worldObj.playSoundEffect(posX, posY, posZ, "hbm:weapon.mukeExplosion", 15.0F, 1.0F);
			
			this.entityDropItem(new ItemStack(ModItems.core_angel, 1, 0), 1);

			List<EntityPlayer> players = worldObj.getEntitiesWithinAABB(EntityPlayer.class, this.boundingBox.expand(200, 200, 200));

			
			for(EntityPlayer player : players) {
				player.addChatComponentMessage(new ChatComponentText("Stars are starting to flicker...").setChatStyle(new ChatStyle().setColor(EnumChatFormatting.RED)));
			}
			
			CelestialBody body = CelestialBody.getBody(worldObj);

			CBT_Invasion invasion = body.getTrait(CBT_Invasion.class);

			if (invasion == null) {
				body.modifyTraits(new CBT_Invasion(0, 122, false));

			}

			
		}

		super.onDeathUpdate();
	}

	private void fireBeamAt(double x, double y, double z) {

		    double spawnX = this.posX - 1;
		    double spawnY = this.posY + 4;
		    double spawnZ = this.posZ;

		    EntityBulletBeamBase bullet = new EntityBulletBeamBase(this,XFactoryEnergy.energy_tesla_overcharge.setKnockback(0),8F);

		    bullet.setPosition(spawnX, spawnY, spawnZ);

		    Vec3 delta = Vec3.createVectorHelper(
		        x - spawnX,
		        y - spawnY,
		        z - spawnZ
		    );

		    bullet.setRotationsFromVector(delta);

		    bullet.performHitscanExternal(250D);

		    this.worldObj.spawnEntityInWorld(bullet);
		    this.playSound("hbm:entity.bfashoot", 5.0F, 1.0F);
		}
	
	private void fireDownwardBeamAt(double x, double y, double z) {

		    double spawnX = x;
		    double spawnY = y + 40;
		    double spawnZ = z;

		    EntityBulletBeamBase bullet = new EntityBulletBeamBase(
		        this,
		        XFactoryEnergy.energy_tesla_overcharge.setKnockback(0),
		        6F
		    );

		    bullet.setPosition(spawnX, spawnY, spawnZ);

		    Vec3 delta = Vec3.createVectorHelper( 0,-1,0);

		    bullet.setRotationsFromVector(delta);

		    bullet.performHitscanExternal(250D);

		    this.worldObj.spawnEntityInWorld(bullet);

		    this.playSound("hbm:entity.bfashoot", 5.0F, 1.0F);
		}

	private void rocketAttack(Entity e) {

		    Vec3 heading = Vec3.createVectorHelper(
		        e.posX - this.posX,
		        e.posY + e.height / 2.0 - this.posY - 0.5D,
		        e.posZ - this.posZ
		    );

		    heading = heading.normalize();

		    EntityBulletBaseMK4 bullet = new EntityBulletBaseMK4(
		        this,
		        XFactoryFlamer.flame_balefire.setKnockback(0),
		        0.5F, 1, 0, 0, 0
		    );

		    bullet.setThrower(this);

		    double speed = 1.0; 
		    bullet.motionX = heading.xCoord * speed;
		    bullet.motionY = heading.yCoord * speed;
		    bullet.motionZ = heading.zCoord * speed;

		    this.worldObj.spawnEntityInWorld(bullet);
		    this.playSound("hbm:turret.richard_fire", 5.0F, 1.0F);
		}

	@Override
	public boolean canAttackClass(Class clazz) {
		return clazz != this.getClass() && clazz != EntityBulletBaseNT.class;
	}

	@Override
	protected void entityInit() {
		super.entityInit();
		this.dataWatcher.addObject(16, Byte.valueOf((byte) 0));
		this.dataWatcher.addObject(17, 0);
		this.dataWatcher.addObject(18, 0);
		this.dataWatcher.addObject(19, 0);
	}

	private boolean isCourseTraversable(double p_70790_1_, double p_70790_3_, double p_70790_5_, double p_70790_7_) {

		double d4 = (this.getX() - this.posX) / p_70790_7_;
		double d5 = (this.getY() - this.posY) / p_70790_7_;
		double d6 = (this.getZ() - this.posZ) / p_70790_7_;
		AxisAlignedBB axisalignedbb = this.boundingBox.copy();

		for(int i = 1; i < p_70790_7_; ++i) {
			axisalignedbb.offset(d4, d5, d6);

			if(!this.worldObj.getCollidingBoundingBoxes(this, axisalignedbb).isEmpty()) {
				return false;
			}
		}

		return true;
	}

	@Override
	protected float getSoundVolume() {
		return 10.0F;
	}

	@Override
	protected String getHurtSound() {
		return "mob.blaze.hit";
	}

	@Override
	protected String getDeathSound() {
		return null;
	}

	@Override
	public void writeEntityToNBT(NBTTagCompound p_70014_1_) {
		super.writeEntityToNBT(p_70014_1_);
	}

	@Override
	public void readEntityFromNBT(NBTTagCompound p_70037_1_) {
		super.readEntityFromNBT(p_70037_1_);
	}

	public void setBeam(boolean b) {
		this.dataWatcher.updateObject(16, Byte.valueOf((byte) (b ? 1 : 0)));
	}

	public boolean getBeam() {
		return this.dataWatcher.getWatchableObjectByte(16) == 1;
	}

	public void setWaypoint(int x, int y, int z) {
		this.dataWatcher.updateObject(17, x);
		this.dataWatcher.updateObject(18, y);
		this.dataWatcher.updateObject(19, z);
	}

	public int getX() {
		return this.dataWatcher.getWatchableObjectInt(17);
	}

	public int getY() {
		return this.dataWatcher.getWatchableObjectInt(18);
	}

	public int getZ() {
		return this.dataWatcher.getWatchableObjectInt(19);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public boolean isInRangeToRenderDist(double distance) {
		return distance < 500000;
	}
}
