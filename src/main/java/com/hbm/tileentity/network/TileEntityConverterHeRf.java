package com.hbm.tileentity.network;

import com.google.gson.JsonObject;
import com.google.gson.stream.JsonWriter;
import com.hbm.tileentity.IConfigurableMachine;
import com.hbm.tileentity.TileEntityLoadedBase;
import com.hbm.tileentity.TilePortShapes;
import com.hbm.tileentity.TilePort.PortDef;
import com.hbm.util.Compat;
import com.hbm.util.fauxpointtwelve.BlockPos;

import api.hbm.energymk2.IEnergyReceiverMK2;
import cofh.api.energy.EnergyStorage;
import cofh.api.energy.IEnergyHandler;
import cofh.api.energy.IEnergyReceiver;
import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;

import java.io.IOException;

public class TileEntityConverterHeRf extends TileEntityLoadedBase implements IEnergyReceiverMK2, IEnergyHandler, IConfigurableMachine {

	//Thanks to the great people of Fusion Warfare for helping me with the original implementation of the RF energy API

	public long power;
	private static final long[] HE_LIMITS = {5_000_000, 4_000_000, 3_000_000, 2_000_000, 1_000_000, 100_000};
	private static final int[] RF_LIMITS = {1_000_000, 800_000, 600_000, 400_000, 200_000, 20_000};
	private int maxPowerIndex = 0;
	public static long heInput = 5;
	public static long rfOutput = 1;
	public static double inputDecay = 0.0;
	public EnergyStorage storage = new ConverterEnergyStorage(1_000_000);

	protected PortDef[] cachedPorts;
	public PortDef[] getPorts() { if(cachedPorts == null) cachedPorts = TilePortShapes.around(xCoord, yCoord, zCoord); return cachedPorts; }

	public TileEntityConverterHeRf() {
		updateRfLimits();
	}

	@Override
	public void updateEntity() {

		if(!worldObj.isRemote) {

			this.setupPowerPorts(getPorts());
			this.updateAllPorts();
			this.receivePower();

			long rfCreated = Math.min(Math.max(0, storage.getMaxEnergyStored() - storage.getEnergyStored()), power / heInput * rfOutput);
			this.power -= rfCreated * heInput / rfOutput;
			this.storage.setEnergyStored((int) (storage.getEnergyStored() + rfCreated));
			if(power > 0) this.power *= (1D - inputDecay);
			if(rfCreated > 0) this.worldObj.markTileEntityChunkModified(this.xCoord, this.yCoord, this.zCoord, this);

			for(ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS) {

				BlockPos loc = new BlockPos(xCoord, yCoord, zCoord).offset(dir);
				TileEntity entity = Compat.getTileStandard(worldObj, loc.getX(), loc.getY(), loc.getZ());

				if(entity != null && entity instanceof IEnergyReceiver) {
					IEnergyReceiver receiver = (IEnergyReceiver) entity;

					int maxExtract = storage.getMaxExtract();
					int maxAvailable = storage.extractEnergy(maxExtract, true);
					int energyTransferred = receiver.receiveEnergy(dir.getOpposite(), maxAvailable, false);

					storage.extractEnergy(energyTransferred, false);
				}
			}

			networkPackNT(15);
		}
	}

	@Override public boolean canConnectEnergy(ForgeDirection from) { return true; }
	@Override public int receiveEnergy(ForgeDirection from, int maxReceive, boolean simulate) { return 0; }
	@Override public int extractEnergy(ForgeDirection from, int maxExtract, boolean simulate) { return storage.extractEnergy(maxExtract, simulate); }
	@Override public int getEnergyStored(ForgeDirection from) { return storage.getEnergyStored(); }
	@Override public int getMaxEnergyStored(ForgeDirection from) { return storage.getMaxEnergyStored(); }

	@Override public void setPower(long i) { power = i; }
	@Override public long getPower() { return power; }
	@Override public long getMaxPower() { return HE_LIMITS[maxPowerIndex]; }
	@Override public ConnectionPriority getPriority() { return ConnectionPriority.LOW; }

	@Override
	public long transferPower(long incoming) {
		if(power >= getMaxPower()) return incoming;
		return IEnergyReceiverMK2.super.transferPower(incoming);
	}

	public void adjustMaxPower(boolean decrease) {
		maxPowerIndex = Math.max(0, Math.min(HE_LIMITS.length - 1, maxPowerIndex + (decrease ? 1 : -1)));
		updateRfLimits();
		markDirty();
		networkPackNT(15);
	}

	private void updateRfLimits() {
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
		return "HEToRFConverter";
	}

	@Override
	public void readIfPresent(JsonObject obj) {
		heInput = IConfigurableMachine.grab(obj, "L:HE_Used", heInput);
		rfOutput = IConfigurableMachine.grab(obj, "L:RF_Created", rfOutput);
		inputDecay = IConfigurableMachine.grab(obj, "D:inputDecay2", inputDecay);
		updateRfLimits();
	}

	@Override
	public void writeConfig(JsonWriter writer) throws IOException {
		writer.name("L:HE_Used").value(heInput);
		writer.name("L:RF_Created").value(rfOutput);
		writer.name("D:inputDecay2").value(inputDecay);
	}
}
