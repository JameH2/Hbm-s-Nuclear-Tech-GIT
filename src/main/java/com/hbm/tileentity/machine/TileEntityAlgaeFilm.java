package com.hbm.tileentity.machine;

import com.hbm.dim.orbit.WorldProviderOrbit;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.tank.FluidTank;
import com.hbm.module.portmanager.ModulePortManFluidAdaptive;
import com.hbm.tileentity.TileEntityMachineBase;
import com.hbm.tileentity.TilePort.PortDef;
import com.hbm.tileentity.TilePortShapes;

import api.hbm.fluidmk2.IFluidStandardTransceiverMK2;
import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.NBTTagCompound;

public class TileEntityAlgaeFilm extends TileEntityMachineBase implements IFluidStandardTransceiverMK2 {

	public FluidTank[] tanks;
	public boolean canOperate;

	protected ModulePortManFluidAdaptive moduleFluidPorts;

	public TileEntityAlgaeFilm() {
		super(0);
		tanks = new FluidTank[2];
		tanks[0] = new FluidTank(Fluids.CARBONDIOXIDE, 8_000);
		tanks[1] = new FluidTank(Fluids.OXYGEN, 8_000);

		moduleFluidPorts = new ModulePortManFluidAdaptive(this).setInputTanks(getReceivingTanks()).setOutputTanks(getSendingTanks());
	}
	
	protected PortDef[] cachedPorts;
	public PortDef[] getPorts() { if(cachedPorts == null) cachedPorts = TilePortShapes.horizontal(xCoord, yCoord, zCoord); return cachedPorts; }

	@Override
	public String getName() {
		return "container.algaeFilm";
	}

	@Override
	public void updateEntity() {
		if(!worldObj.isRemote) {
			moduleFluidPorts.update(getPorts());

			canOperate = worldObj.provider instanceof WorldProviderOrbit;

			if(canOperate && worldObj.rand.nextBoolean()) {
				if(tanks[0].getFill() > 0 && tanks[1].getFill() < tanks[1].getMaxFill()) {
					tanks[0].setFill(tanks[0].getFill() - 1);
					tanks[1].setFill(tanks[1].getFill() + 1);
				}
			}

			networkPackNT(20);
		}
	}

	@Override
	public void serialize(ByteBuf buf) {
		super.serialize(buf);
		buf.writeBoolean(canOperate);
		for(FluidTank tank : tanks) tank.serialize(buf);
	}

	@Override
	public void deserialize(ByteBuf buf) {
		super.deserialize(buf);
		canOperate = buf.readBoolean();
		for(FluidTank tank : tanks) tank.deserialize(buf);
	}

	@Override
	public void writeToNBT(NBTTagCompound nbt) {
		super.writeToNBT(nbt);
		for(int i = 0; i < tanks.length; i++) tanks[i].writeToNBT(nbt, "t" + i);
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);
		for(int i = 0; i < tanks.length; i++) tanks[i].readFromNBT(nbt, "t" + i);
	}

	@Override
	public FluidTank[] getAllTanks() {
		return tanks;
	}

	@Override
	public FluidTank[] getSendingTanks() {
		return new FluidTank[] { tanks[1] };
	}

	@Override
	public FluidTank[] getReceivingTanks() {
		return new FluidTank[] { tanks[0] };
	}
	
}
