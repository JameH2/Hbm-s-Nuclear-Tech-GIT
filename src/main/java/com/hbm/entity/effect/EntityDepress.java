package com.hbm.entity.effect;

import java.util.List;

import com.hbm.extprop.HbmLivingProps;
import com.hbm.main.MainRegistry;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

public class EntityDepress extends Entity {

	public int timeToLive;

	public static double range = 8; // push/pull range

	public EntityDepress(World world) {
		this(world, ForgeDirection.DOWN, 100);
	}

	public EntityDepress(World world, ForgeDirection dir, int timeToLive) {
		super(world);
		this.ignoreFrustumCheck = true;
		this.isImmuneToFire = true;
		this.noClip = true;

		this.timeToLive = timeToLive;
		setDir(dir);
	}

	protected void setDir(ForgeDirection dir) {
		this.dataWatcher.updateObject(10, (byte)dir.ordinal());
	}

	protected ForgeDirection getDir() {
		return ForgeDirection.getOrientation(this.dataWatcher.getWatchableObjectByte(10));
	}

	@Override
	protected void entityInit() {
		this.dataWatcher.addObject(10, new Byte((byte)0));
	}

	@SuppressWarnings("unchecked")
	@Override
	public void onUpdate() {
		super.onUpdate();

		ForgeDirection dir = getDir();

		NBTTagCompound data = new NBTTagCompound();
		data.setDouble("posX", posX + worldObj.rand.nextGaussian() * 0.25);
		data.setDouble("posY", posY + worldObj.rand.nextGaussian() * 0.25);
		data.setDouble("posZ", posZ + worldObj.rand.nextGaussian() * 0.25);
		data.setString("type", "depress");
		data.setFloat("scale", 0.5f);
		data.setDouble("moX", dir.offsetX * 0.5 + worldObj.rand.nextGaussian() * 0.25);
		data.setDouble("moY", dir.offsetY * 0.5 + worldObj.rand.nextGaussian() * 0.25);
		data.setDouble("moZ", dir.offsetZ * 0.5 + worldObj.rand.nextGaussian() * 0.25);
		data.setInteger("maxAge", 100 + worldObj.rand.nextInt(20));
		data.setInteger("color", 0xFFFFFF);
		MainRegistry.proxy.effectNT(data);

		List<Entity> entities = worldObj.getEntitiesWithinAABBExcludingEntity(this, AxisAlignedBB.getBoundingBox(
			posX - range, posY - range, posZ - range, posX + range, posY + range, posZ + range));

		for(Entity entity : entities) {
			Vec3 vec = Vec3.createVectorHelper(posX - entity.posX, posY - entity.posY, posZ - entity.posZ);

			// check if the player is in a different pressurized room
			// other living entities update gravity too infrequently to use this check, so it is for players only
			if(entity instanceof EntityPlayer) {
				if(HbmLivingProps.hasGravity((EntityLivingBase) entity)) continue;
			}

			double dist = vec.lengthVector();

			if(dist > range)
				continue;

			boolean succ = true;

			switch(dir) {
			case UP: succ = entity.posY < posY; break;
			case DOWN: succ = entity.posY > posY; break;
			case EAST: succ = entity.posX < posX; break;
			case WEST: succ = entity.posX > posX; break;
			case SOUTH: succ = entity.posZ < posZ; break;
			case NORTH: succ = entity.posZ > posZ; break;
			default: break;
			}

			vec = vec.normalize();

			double speed = succ ? 0.1D : -0.1D;
			entity.motionX += vec.xCoord * speed;
			entity.motionY += vec.yCoord * speed;
			entity.motionZ += vec.zCoord * speed;
		}

		timeToLive--;

		if(timeToLive <= 0) {
			setDead();
		}
	}

	@Override
	protected void readEntityFromNBT(NBTTagCompound nbt) {
		timeToLive = nbt.getInteger("ttl");
	}

	@Override
	protected void writeEntityToNBT(NBTTagCompound nbt) {
		nbt.setInteger("ttl", timeToLive);
	}

}
