package com.hbm.tileentity.network;

import com.google.gson.JsonObject;
import com.google.gson.stream.JsonWriter;
import com.hbm.tileentity.IConfigurableMachine;
import com.hbm.tileentity.TileEntityLoadedBase;
import com.hbm.tileentity.TilePortShapes;
import com.hbm.tileentity.TilePort.PortDef;

import api.hbm.energymk2.IEnergyProviderMK2;
import cofh.api.energy.EnergyStorage;
import cofh.api.energy.IEnergyHandler;
import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.ForgeDirection;

import java.io.IOException;

public class TileEntityConverterRfHe extends TileEntityLoadedBase implements IEnergyProviderMK2, IEnergyHandler, IConfigurableMachine {

	public long power;
	private static final long[] HE_LIMITS = {5_000_000, 4_000_000, 3_000_000, 2_000_000, 1_000_000, 100_000};
	private static final int[] RF_LIMITS = {1_000_000, 800_000, 600_000, 400_000, 200_000, 20_000};
	private int maxPowerIndex = 0;
	public static long rfInput = 2;
	public static long heOutput = 5;
	public static double inputDecay = 0.0;

	public EnergyStorage storage = new EnergyStorage(1_000_000);

	protected PortDef[] cachedPorts;
	public PortDef[] getPorts() { if(cachedPorts == null) cachedPorts = TilePortShapes.around(xCoord, yCoord, zCoord); return cachedPorts; }

	public TileEntityConverterRfHe() {
		updateRfLimits();
	}

	@Override
	public void updateEntity() {
		
		if(!worldObj.isRemote) {
			
			this.setupPowerPorts(getPorts());
			this.updateAllPorts();
			this.providePower();

			long rfCreated = Math.min(storage.getEnergyStored(), (getMaxPower() - power) * rfInput / heOutput);
			storage.setEnergyStored((int) (storage.getEnergyStored() - rfCreated));
			power += rfCreated * heOutput / rfInput;
			if(storage.getEnergyStored() > 0) storage.extractEnergy((int) Math.ceil(storage.getEnergyStored() * inputDecay), false);
			if(rfCreated > 0) this.worldObj.markTileEntityChunkModified(this.xCoord, this.yCoord, this.zCoord, this);

			networkPackNT(15);
		}
	}
	
	@Override public boolean canConnectEnergy(ForgeDirection from) { return true; }
	@Override public int receiveEnergy(ForgeDirection from, int maxReceive, boolean simulate) { return storage.receiveEnergy(maxReceive, simulate); }
	@Override public int getEnergyStored(ForgeDirection from) { return storage.getEnergyStored(); }
	@Override public int getMaxEnergyStored(ForgeDirection from) { return storage.getMaxEnergyStored(); }
	@Override public int extractEnergy(ForgeDirection from, int maxExtract, boolean simulate) { return 0; }

	@Override public long getPower() { return power; }
	@Override public void setPower(long power) { this.power = power; }
	@Override public long getMaxPower() { return HE_LIMITS[maxPowerIndex]; }
	
	public boolean adjustMaxPower(boolean decrease) {
		int nextIndex = Math.max(0, Math.min(HE_LIMITS.length - 1, maxPowerIndex + (decrease ? 1 : -1)));
		if(nextIndex == maxPowerIndex) return false;
		maxPowerIndex = nextIndex;
		updateRfLimits();
		markDirty();
		networkPackNT(15);
		return true;
	}

	private void updateRfLimits() {
		power = Math.min(power, getMaxPower());
		int maxRf = RF_LIMITS[maxPowerIndex];
		storage.setCapacity(maxRf);
		storage.setMaxTransfer(maxRf);
	}

	private void setMaxPower(long limit) {
		maxPowerIndex = HE_LIMITS.length - 1;
		for(int i = 0; i < HE_LIMITS.length; i++) {
			if(HE_LIMITS[i] <= limit) {
				maxPowerIndex = i;
				break;
			}
		}
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);
		
		setMaxPower(nbt.hasKey("maxPower") ? nbt.getLong("maxPower") : HE_LIMITS[0]);
		this.power = nbt.getLong("power");
		updateRfLimits();
		storage.readFromNBT(nbt);
	}
	
	@Override
	public void writeToNBT(NBTTagCompound nbt) {
		super.writeToNBT(nbt);
		
		nbt.setLong("power", power);
		nbt.setLong("maxPower", getMaxPower());
		storage.writeToNBT(nbt);
	}

	@Override
	public void serialize(ByteBuf buf) {
		super.serialize(buf);

		buf.writeLong(power);
		buf.writeLong(getMaxPower());
		buf.writeInt(storage.getEnergyStored());
	}

	@Override
	public void deserialize(ByteBuf buf) {
		super.deserialize(buf);

		power = buf.readLong();
		setMaxPower(buf.readLong());
		updateRfLimits();
		storage.setEnergyStored(buf.readInt());
	}

	@Override
	public String getConfigName() {
		return "RFToHEConverter";
	}

	@Override
	public void readIfPresent(JsonObject obj) {
		rfInput = IConfigurableMachine.grab(obj, "L:RF_Used2", rfInput);
		heOutput = IConfigurableMachine.grab(obj, "L:HE_Created2", heOutput);
		inputDecay = IConfigurableMachine.grab(obj, "D:inputDecay2", inputDecay);
		updateRfLimits();
	}

	@Override
	public void writeConfig(JsonWriter writer) throws IOException {
		writer.name("L:RF_Used2").value(rfInput);
		writer.name("L:HE_Created2").value(heOutput);
		writer.name("D:inputDecay2").value(inputDecay);
	}
}
