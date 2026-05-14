package com.hbm.tileentity.machine.storage;

import com.hbm.config.SpaceConfig;
import com.hbm.tileentity.IBufPacketReceiver;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;

public class TileEntityCombatDropPod extends TileEntity implements IBufPacketReceiver{

	public NBTTagCompound entityType;
	public int amount;
	public int color;
	public int delay = 40;
	
	public double hatchopen;
	public double hatchopen2;
	public double prevHatchopen;
	public double prevHatchopen2;

	public TileEntityCombatDropPod() {

	}

	public void setPayload(NBTTagCompound entityType, int amount, int color) {
		this.entityType = entityType;
		this.amount = amount;
		this.color = color;
	}

	public int getColor() {
		return this.color;
	}
	
	public void setColor(int color) {
		this.color = color;
	}

	@Override
	public void updateEntity() {	
		prevHatchopen = hatchopen;
		prevHatchopen2 = hatchopen2;
		
		if(delay > 0) {
			delay--;
			return;
		}

		if(delay == 0) {
			hatchopen += (90 - hatchopen) * 0.2;
			hatchopen2 += (-90 - hatchopen2) * 0.2;
		}
		
		if(entityType != null && amount > 0 && !worldObj.isRemote) {
			
			for(int i = 0; i < amount; i++) {

				Entity entity = EntityList.createEntityFromNBT(entityType, worldObj);

				if(entity != null) {
					entity.setPosition(xCoord + 0.5, yCoord + 1, zCoord + 0.5);

					worldObj.spawnEntityInWorld(entity);
				}
			}

			amount = 0;
			this.worldObj.playSoundEffect(this.xCoord, this.yCoord, this.zCoord, "hbm:block.hatchOpen", 10.0F, 0.9F);
			return;
		}
		
		if(SpaceConfig.combatPodDespawn) {
			if(this.worldObj.getTotalWorldTime() % 1000 == 0) {
				worldObj.setBlockToAir(xCoord, yCoord, zCoord);
			}
		}
	}
	
	//we serious?? like actually??
	//i hate working on this game so much sometimes
	@Override
	public Packet getDescriptionPacket() {
		NBTTagCompound nbt = new NBTTagCompound();
		this.writeToNBT(nbt);
		return new S35PacketUpdateTileEntity(xCoord, yCoord, zCoord, 1, nbt);
	}

	@Override
	public void onDataPacket(NetworkManager net, S35PacketUpdateTileEntity pkt) {
		this.readFromNBT(pkt.func_148857_g());
	}

	AxisAlignedBB bb = null;

	@Override
	public AxisAlignedBB getRenderBoundingBox() {

		if(bb == null) {
			bb = AxisAlignedBB.getBoundingBox(
				xCoord - 3,
				yCoord,
				zCoord - 3,
				xCoord + 4,
				yCoord + 8,
				zCoord + 4
			);
		}

		return bb;
	}


	@Override
	public void deserialize(ByteBuf buf) {
		this.color = buf.readInt();
		this.hatchopen = buf.readDouble();
		this.hatchopen2 = buf.readDouble();
	}

	@Override
	public void serialize(ByteBuf buf) {
		buf.writeInt(color);
		buf.writeDouble(hatchopen);
		buf.writeDouble(hatchopen2);
	}
	
	@Override
	public void writeToNBT(NBTTagCompound nbt) {
		super.writeToNBT(nbt);

		if(entityType != null)
			nbt.setTag("EntityType", entityType);

		nbt.setInteger("amount", amount);
		nbt.setInteger("color", color);
		nbt.setInteger("delay", delay);
		nbt.setDouble("hatchopen", hatchopen);
		nbt.setDouble("hatchopen2", hatchopen2);
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);

		entityType = nbt.getCompoundTag("EntityType");
		amount = nbt.getInteger("amount");
		color = nbt.getInteger("color");
		delay = nbt.getInteger("delay");
		hatchopen = nbt.getDouble("hatchopen");
		hatchopen2 = nbt.getDouble("hatchopen2");
	}

}
