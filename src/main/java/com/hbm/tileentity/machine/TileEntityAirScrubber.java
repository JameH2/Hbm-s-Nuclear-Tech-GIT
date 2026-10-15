package com.hbm.tileentity.machine;

import java.util.List;

import com.hbm.handler.ThreeInts;
import com.hbm.handler.atmosphere.AtmosphereBlob;
import com.hbm.handler.atmosphere.ChunkAtmosphereManager;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.tank.FluidTank;
import com.hbm.module.portmanager.ModulePortManFluidAdaptive;
import com.hbm.module.portmanager.ModulePortManPower;
import com.hbm.tileentity.TileEntityMachineBase;
import com.hbm.tileentity.TilePort.PortDef;
import com.hbm.tileentity.TilePortShapes;

import api.hbm.energymk2.IEnergyReceiverMK2;
import api.hbm.fluidmk2.IFluidStandardSenderMK2;
import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;

public class TileEntityAirScrubber extends TileEntityMachineBase implements IFluidStandardSenderMK2, IEnergyReceiverMK2 {

	private TileEntityAirPump pump;
	public FluidTank tank;

	private long power;

	public float rot;
	public float prevRot;
	private float rotSpeed;

	protected ModulePortManFluidAdaptive moduleFluidPorts;
	protected ModulePortManPower modulePowerPorts;

	public TileEntityAirScrubber() {
		super(0);
		tank = new FluidTank(Fluids.CARBONDIOXIDE, 16_000);

		moduleFluidPorts = new ModulePortManFluidAdaptive(this).setOutputTanks(getSendingTanks());
		modulePowerPorts = new ModulePortManPower(this);
	}
	
	protected PortDef[] cachedPorts;
	public PortDef[] getPorts() { if(cachedPorts == null) cachedPorts = TilePortShapes.around(xCoord, yCoord, zCoord); return cachedPorts; }

	@Override
	public String getName() {
		return "container.airScrubber";
	}

	@Override
	public void updateEntity() {
		if(!worldObj.isRemote) {
			moduleFluidPorts.update(getPorts());
			modulePowerPorts.update(getPorts());

			if(canOperate()) {
				// Fetch a new pump to scrub CO2 from
				if(worldObj.getTotalWorldTime() % 5 == 0 && (pump == null || pump.getFluidPressure() == 0 || !pump.registerScrubber(this))) {
					pump = null;

					List<AtmosphereBlob> blobs = ChunkAtmosphereManager.proxy.getBlobs(worldObj, xCoord, yCoord, zCoord);

					for(AtmosphereBlob blob : blobs) {
						if(blob != null) {
							ThreeInts pos = blob.getRootPosition();
							TileEntity te = worldObj.getTileEntity(pos.x, pos.y, pos.z);
							if(te != null && te instanceof TileEntityAirPump) {
								pump = (TileEntityAirPump) te;
								if(!pump.registerScrubber(this)) {
									pump = null;
								} else {
									break;
								}
							}
						}
					}
				}
			}

			networkPackNT(20);
		} else {
			float maxSpeed = 30F;

			if(canOperate()) {
				rotSpeed += 0.2;
				if(rotSpeed > maxSpeed) rotSpeed = maxSpeed;
			} else {
				rotSpeed -= 0.1;
				if(rotSpeed < 0) rotSpeed = 0;
			}

			prevRot = rot;

			rot += rotSpeed;

			if(rot >= 360) {
				rot -= 360;
				prevRot -= 360;
			}
		}
	}

	public boolean canOperate() {
		return power > 200;
	}

	public int scrub(int amount) {
		if(!canOperate()) return 0;
		int add = Math.min(tank.getMaxFill() - tank.getFill(), amount);
		tank.setFill(tank.getFill() + add);
		power -= add * 10;
		return add;
	}

	@Override
	public void serialize(ByteBuf buf) {
		super.serialize(buf);
		buf.writeLong(power);
		tank.serialize(buf);
	}

	@Override
	public void deserialize(ByteBuf buf) {
		super.deserialize(buf);
		power = buf.readLong();
		tank.deserialize(buf);
	}

	@Override
	public void writeToNBT(NBTTagCompound nbt) {
		super.writeToNBT(nbt);
		nbt.setLong("power", power);
		tank.writeToNBT(nbt, "t");
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);
		power = nbt.getLong("power");
		tank.readFromNBT(nbt, "t");
	}

	@Override
	public FluidTank[] getAllTanks() {
		return new FluidTank[] { tank };
	}

	@Override
	public FluidTank[] getSendingTanks() {
		return new FluidTank[] { tank };
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
		return 10000;
	}

	AxisAlignedBB bb = null;

	@Override
	public AxisAlignedBB getRenderBoundingBox() {
		if(bb == null) {
			bb = AxisAlignedBB.getBoundingBox(
				xCoord,
				yCoord,
				zCoord,
				xCoord + 1,
				yCoord + 2,
				zCoord + 1
			);
		}

		return bb;
	}

}
