package com.hbm.tileentity.machine;

import com.hbm.blocks.BlockDummyable;
import com.hbm.module.portmanager.ModulePortManPower;
import com.hbm.packet.PacketDispatcher;
import com.hbm.packet.toclient.AuxParticlePacketNT;
import com.hbm.tileentity.IDysonConverter;
import com.hbm.tileentity.TileEntityMachineBase;
import com.hbm.tileentity.TilePort.PortDef;

import api.hbm.energymk2.IEnergyProviderMK2;
import cpw.mods.fml.common.network.NetworkRegistry.TargetPoint;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.AxisAlignedBB;
import net.minecraftforge.common.util.ForgeDirection;

public class TileEntityDysonConverterHE extends TileEntityMachineBase implements IDysonConverter, IEnergyProviderMK2 {

	public long power;

	public boolean isConverting;
	private int cooldown;

	protected ModulePortManPower modulePowerPorts;

	public TileEntityDysonConverterHE() {
		super(0);
		
		modulePowerPorts = new ModulePortManPower(this);
	}

	@Override
	public String getName() {
		return "container.machineDysonConverterHE";
	}

	@Override
	public void updateEntity() {
		if(!worldObj.isRemote) {
			modulePowerPorts.update(getPorts());

			ForgeDirection dir = ForgeDirection.getOrientation(this.getBlockMetadata() - BlockDummyable.offset).getOpposite();
			ForgeDirection rot = dir.getRotation(ForgeDirection.UP);

			isConverting = power > 0;

			if(isConverting && worldObj.getTotalWorldTime() % 2 == 0) {
				NBTTagCompound dPart = new NBTTagCompound();
				dPart.setString("type", worldObj.getTotalWorldTime() % 10 == 0 ? "tau" : "hadron");
				dPart.setByte("count", (byte) 1);
				PacketDispatcher.wrapper.sendToAllAround(new AuxParticlePacketNT(dPart, xCoord + 0.5 + dir.offsetX * 4 + rot.offsetX, yCoord + 2.25, zCoord + 0.5 + dir.offsetZ * 4 + rot.offsetZ), new TargetPoint(worldObj.provider.dimensionId, xCoord, yCoord, zCoord, 25));
				PacketDispatcher.wrapper.sendToAllAround(new AuxParticlePacketNT(dPart, xCoord + 0.5 + dir.offsetX * 4 - rot.offsetX, yCoord + 2.25, zCoord + 0.5 + dir.offsetZ * 4 - rot.offsetZ), new TargetPoint(worldObj.provider.dimensionId, xCoord, yCoord, zCoord, 25));
				PacketDispatcher.wrapper.sendToAllAround(new AuxParticlePacketNT(dPart, xCoord + 0.5 + dir.offsetX * 3 + rot.offsetX, yCoord + 2.75, zCoord + 0.5 + dir.offsetZ * 3 + rot.offsetZ), new TargetPoint(worldObj.provider.dimensionId, xCoord, yCoord, zCoord, 25));
				PacketDispatcher.wrapper.sendToAllAround(new AuxParticlePacketNT(dPart, xCoord + 0.5 + dir.offsetX * 3 - rot.offsetX, yCoord + 2.75, zCoord + 0.5 + dir.offsetZ * 3 - rot.offsetZ), new TargetPoint(worldObj.provider.dimensionId, xCoord, yCoord, zCoord, 25));

				cooldown++;
				if(cooldown > 10) {
					// To prevent this machine acting like an endgame battery, but still be able to transmit every drop of power
					// this machine will clear its buffers (almost) immediately after transmitting power
					power = 0;
					cooldown = 0;
				}
			}

			networkPackNT(250);
		}
	}
	
	protected PortDef[] cachedPorts;

	public PortDef[] getPorts() {
		if(cachedPorts == null) {
			ForgeDirection dir = ForgeDirection.getOrientation(this.getBlockMetadata() - BlockDummyable.offset).getOpposite();

			cachedPorts = new PortDef[] {
				PortDef.make(xCoord + dir.offsetX * 4, yCoord, zCoord + dir.offsetZ * 4, dir),
			};
		}
		
		return cachedPorts;
	}

	@Override
	public boolean provideEnergy(int x, int y, int z, long energy) {
		ForgeDirection dir = ForgeDirection.getOrientation(this.getBlockMetadata() - BlockDummyable.offset);
		int rx = xCoord + dir.offsetX * 4;
		int ry = yCoord + 1;
		int rz = zCoord + dir.offsetZ * 4;

		if(x != rx || y != ry || z != rz) return false;

		power = energy;
		cooldown = 0;

		return true;
	}

	@Override
	public long maximumEnergy() {
		return Long.MAX_VALUE;
	}

	@Override
	public void serialize(ByteBuf buf) {
		super.serialize(buf);
		buf.writeBoolean(isConverting);
	}

	@Override
	public void deserialize(ByteBuf buf) {
		super.deserialize(buf);
		isConverting = buf.readBoolean();
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);
	}

	@Override
	public void writeToNBT(NBTTagCompound nbt) {
		super.writeToNBT(nbt);
	}

	@Override
	public long getPower() {
		return power;
	}

	@Override
	public void setPower(long power) {
		this.power = power;
	}

	@Override
	public long getMaxPower() {
		return power;
	}

	AxisAlignedBB bb = null;

	@Override
	public AxisAlignedBB getRenderBoundingBox() {

		if(bb == null) {
			bb = AxisAlignedBB.getBoundingBox(
				xCoord - 6,
				yCoord,
				zCoord - 6,
				xCoord + 7,
				yCoord + 6,
				zCoord + 7
			);
		}

		return bb;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public double getMaxRenderDistanceSquared() {
		return 65536.0D;
	}

}
