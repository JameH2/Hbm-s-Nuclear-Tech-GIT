package com.hbm.entity.projectile;

import api.hbm.entity.IRadarDetectableNT;
import com.google.common.collect.ImmutableSet;
import com.hbm.entity.logic.IChunkLoader;
import com.hbm.items.weapon.ItemAmmoRailgun;
import com.hbm.items.weapon.ItemAmmoRailgun.RailgunSabot;
import com.hbm.main.MainRegistry;
import com.hbm.main.NTMSounds;
import com.hbm.particle.helper.ExplosionCreator;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.ChunkCoordIntPair;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeChunkManager;

import java.util.ArrayList;
import java.util.List;

public class EntityRailgunProjectile extends EntityThrowableNT implements IChunkLoader, IRadarDetectableNT {
	public boolean sabotSeparation = false;
	// runtime-only: the fluid this railgun shot is carrying (filled fluid sabot)
	public com.hbm.inventory.fluid.FluidType fluidType = com.hbm.inventory.fluid.Fluids.NONE;
	public short fluidFill = 0;

	private ForgeChunkManager.Ticket loaderTicket;

	public EntityRailgunProjectile(World world) {
		super(world);
		this.setSize(0.5F, 0.5F);
	}

	@Override
	protected void entityInit() {
		super.entityInit();
		init(ForgeChunkManager.requestTicket(MainRegistry.instance, worldObj, ForgeChunkManager.Type.ENTITY));
		this.dataWatcher.addObject(10, new Integer(0));
	}

	@Override
	public void init(ForgeChunkManager.Ticket ticket) {
		if(!worldObj.isRemote && ticket != null) {
			if(loaderTicket == null) {
				loaderTicket = ticket;
				loaderTicket.bindEntity(this);
				loaderTicket.getModData();
			}
			ForgeChunkManager.forceChunk(loaderTicket, new ChunkCoordIntPair(chunkCoordX, chunkCoordZ));
		}
	}

	@Override
	public void onUpdate() {

		if (!worldObj.isRemote) {
			super.onUpdate();

			loadNeighboringChunks((int)Math.floor(posX / 16D), (int)Math.floor(posZ / 16D));
			this.getType().onUpdate(this);
		} else {
			if (!this.sabotSeparation && this.ticksExisted > 3) {
				this.sabotSeparation = true;
				ExplosionCreator.composeEffect(worldObj, this.posX, this.posY, this.posZ, 3, 0.5F, 0.4F, 5F, 3, 4, 15, 1F, 1F, -2F, 100);
				worldObj.playSoundEffect(this.posX, this.posY, this.posZ, NTMSounds.GUN_FATMAN_FIRE, 75F, 1F);
			}
		}
	}

	@Override
	protected void onImpact(MovingObjectPosition mop) {
		if(!worldObj.isRemote) {

			if (mop.typeOfHit == mop.typeOfHit.ENTITY && mop.entityHit instanceof EntityRailgunProjectile) return;
			this.getType().onImpact(this, mop);
		}
	}

	@Override
	public double getGravityVelocity() {
		return 0.05D;
	}

	@Override
	protected float getAirDrag() {
		return (float) (1D - this.getType().bc/20D);
	}

	public void clearChunkLoader() {
		if(!worldObj.isRemote && loaderTicket != null) {
			ForgeChunkManager.releaseTicket(loaderTicket);
			this.loaderTicket = null;
		}
	}

	public EntityRailgunProjectile setType(int type) {
		this.dataWatcher.updateObject(10, type);
		return this;
	}

	public RailgunSabot getType() {
		try {
			return ItemAmmoRailgun.itemTypes[this.dataWatcher.getWatchableObjectInt(10)];
		} catch(Exception ex) {
			return ItemAmmoRailgun.itemTypes[0];
		}
	}

	public void killAndClear() {
		this.setDead();
		this.clearChunkLoader();
	}

	List<ChunkCoordIntPair> loadedChunks = new ArrayList<ChunkCoordIntPair>();

	public void loadNeighboringChunks(int newChunkX, int newChunkZ) {
		if(!worldObj.isRemote && loaderTicket != null) {

			for(ChunkCoordIntPair chunk : ImmutableSet.copyOf(loaderTicket.getChunkList())) {
				ForgeChunkManager.unforceChunk(loaderTicket, chunk);
			}

			loadedChunks.clear();
			loadedChunks.add(new ChunkCoordIntPair(newChunkX, newChunkZ));
			//loadedChunks.add(new ChunkCoordIntPair(newChunkX + (int) Math.floor((this.posX + this.motionX) / 16D), newChunkZ + (int) Math.floor((this.posZ + this.motionZ) / 16D)));

			for(ChunkCoordIntPair chunk : loadedChunks) {
				ForgeChunkManager.forceChunk(loaderTicket, chunk);
			}
		}
	}

	@Override
	public void writeEntityToNBT(NBTTagCompound nbt) {
		super.writeEntityToNBT(nbt);

		nbt.setInteger("type", this.dataWatcher.getWatchableObjectInt(10));
	}

	@Override
	public void readEntityFromNBT(NBTTagCompound nbt) {
		super.readEntityFromNBT(nbt);

		this.dataWatcher.updateObject(10, nbt.getInteger("type"));
	}

	@Override
	public String getUnlocalizedName() {
		int type = 0;
		try {
			type = this.dataWatcher.getWatchableObjectInt(10);
		} catch(Exception ex) {
		}

		switch (type) {
			case (ItemAmmoRailgun.TUNGSTEN):  return "radar.target.railgun_tungsten";
			case (ItemAmmoRailgun.DU):        return "radar.target.railgun_du";
			case (ItemAmmoRailgun.NUKE):      return "radar.target.railgun_nuke";
			case (ItemAmmoRailgun.DESH):      return "radar.target.railgun_desh";
			case (ItemAmmoRailgun.STARMETAL): return "radar.target.railgun_starmetal";
			case (ItemAmmoRailgun.FLUID): 	  return "radar.target.railgun_fluid";
			default: return "Unknown";
		}
	}

	@Override
	public int getBlipLevel() {
		return IRadarDetectableNT.SPECIAL;
	}

	@Override
	public boolean canBeSeenBy(Object radar) {
		return true;
	}

	@Override
	public boolean paramsApplicable(RadarScanParams params) {
		if(!params.scanShells) return false;
		return true;
	}

	@Override
	public boolean suppliesRedstone(RadarScanParams params) {
		if(params.smartMode && this.motionY >= 0) return false;
		return true;
	}
}
