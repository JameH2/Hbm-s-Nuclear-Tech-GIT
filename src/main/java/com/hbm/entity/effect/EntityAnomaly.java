package com.hbm.entity.effect;

import com.hbm.explosion.ExplosionChaos;
import com.hbm.packet.PacketDispatcher;
import com.hbm.packet.toclient.EntityBufPacket;
import com.hbm.tileentity.IBufPacketReceiver;
import com.hbm.wiaj.WorldInAJar;

import cpw.mods.fml.common.network.NetworkRegistry.TargetPoint;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;

public class EntityAnomaly extends Entity implements IBufPacketReceiver {

	/**
	 * gravitational anomaly
	 * powered by wiaj
	 */

	// all shall hear
	public WorldInAJar[] theMany;


	// the violins sing
	public EntityAnomaly(World world) {
		super(world);
		ignoreFrustumCheck = true;
	}

	@Override
	protected void entityInit() {
		// the chorus begins
	}

	@Override
	public void onUpdate() {
		super.onUpdate();

		if(!worldObj.isRemote) {
			if(theMany == null && !isDead) releaseFromGravity();

			// sync every five seconds, while avoiding updating all entities simultaneously
			if(theMany != null && (worldObj.getTotalWorldTime() + getEntityId()) % 100 == 0) {
				PacketDispatcher.wrapper.sendToAllAround(new EntityBufPacket(getEntityId(), this), new TargetPoint(this.worldObj.provider.dimensionId, posX, posY, posZ, 250));
			}
		}
	}

	protected void releaseFromGravity() {
		if(!canRelease()) return;

		if(rand.nextInt(3) == 0) {
			ExplosionChaos.floater(worldObj, (int)posX, (int)posY, (int)posZ, rand.nextInt(24) + 8, rand.nextInt(16) + 16);
			setDead();
			return;
		}

		int count = rand.nextInt(3) + 1;
		theMany = new WorldInAJar[count];

		for(int i = 0; i < count; i++/*--unchained--*/) {
			int x1 = MathHelper.floor_double(posX + rand.nextGaussian() * 8);
			int y1 = MathHelper.floor_double(posY + rand.nextGaussian() * 8);
			int z1 = MathHelper.floor_double(posZ + rand.nextGaussian() * 8);
			int x2 = MathHelper.floor_double(posX + rand.nextGaussian() * 8);
			int y2 = MathHelper.floor_double(posY + rand.nextGaussian() * 8);
			int z2 = MathHelper.floor_double(posZ + rand.nextGaussian() * 8);

			WorldInAJar vessel = WorldInAJar.munchBlob(worldObj, x1, y1, z1, x2, y2, z2);

			theMany[i] = vessel;
		}
	}

	private boolean canRelease() {
		// make sure all surrounding chunks are generated before ripping them out of the ground
		return worldObj.doChunksNearChunkExist(MathHelper.floor_double(this.posX), 128, MathHelper.floor_double(this.posZ), 32);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public boolean isInRangeToRenderDist(double distance) {
		return true;
	}

	@Override
	protected void readEntityFromNBT(NBTTagCompound nbt) {
		int count = nbt.getInteger("count");

		if(count > 0) {
			theMany = new WorldInAJar[count];

			for(int i = 0; i < count; i++) {
				NBTTagCompound data = nbt.getCompoundTag("w" + i);
				theMany[i] = new WorldInAJar(data);
			}
		}

	}

	@Override
	protected void writeEntityToNBT(NBTTagCompound nbt) {
		if(theMany != null) {
			nbt.setInteger("count", theMany.length);

			for(int i = 0; i < theMany.length; i++) {
				NBTTagCompound data = new NBTTagCompound();
				theMany[i].writeToNBT(data);
				nbt.setTag("w" + i, data);
			}
		}
	}

	@Override
	public void serialize(ByteBuf buf) {
		buf.writeByte(theMany.length);

		for(int i = 0; i < theMany.length; i++) {
			theMany[i].serialize(buf);
		}
	}

	@Override
	public void deserialize(ByteBuf buf) {
		int count = buf.readByte();

		theMany = new WorldInAJar[count];

		for(int i = 0; i < count; i++) {
			theMany[i] = new WorldInAJar(buf);
		}
	}

}
