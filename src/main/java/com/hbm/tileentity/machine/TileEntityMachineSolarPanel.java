package com.hbm.tileentity.machine;

import com.hbm.dim.WorldProviderCelestial;
import com.hbm.module.portmanager.ModulePortManPower;
import com.hbm.tileentity.TileEntityMachineBase;
import com.hbm.tileentity.TilePort.PortDef;
import com.hbm.tileentity.TilePortShapes;

import api.hbm.energymk2.IEnergyProviderMK2;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MathHelper;
import net.minecraft.world.EnumSkyBlock;

public class TileEntityMachineSolarPanel extends TileEntityMachineBase implements IEnergyProviderMK2 {

	private long power;
	private long maxpwr = 1_000;

	protected ModulePortManPower modulePowerPorts;

	public TileEntityMachineSolarPanel() {
		super(0);

		modulePowerPorts = new ModulePortManPower(this);
	}
	
	protected PortDef[] cachedPorts;
	public PortDef[] getPorts() { if(cachedPorts == null) cachedPorts = TilePortShapes.around(xCoord, yCoord, zCoord); return cachedPorts; }

	@Override
	public void updateEntity() {
		
		if(!worldObj.isRemote) {
			modulePowerPorts.update(getPorts());
			
			// Sun power ranges from 1-4
			int sun = worldObj.getSavedLightValue(EnumSkyBlock.Sky, xCoord, yCoord, zCoord) - worldObj.skylightSubtracted - 11;
			
			if(sun <= 0 || !worldObj.canBlockSeeTheSky(xCoord, yCoord + 1, zCoord)) {
				return;
			}
			
			power += getOutput(sun);
			
			if(power > getMaxPower())
				power = getMaxPower();
		}
	}

	// Balanced around 100he/t on Earth
	public long getOutput(int sun) {
		return MathHelper.ceiling_float_int(sun * 25 * WorldProviderCelestial.getSunPower(worldObj.provider, xCoord, zCoord));
	}

	@Override
	public long getPower() {
		return power;
	}
	
	@Override
	public AxisAlignedBB getRenderBoundingBox() {
		return TileEntity.INFINITE_EXTENT_AABB;
	}

	@Override
	public void setPower(long power) {
		this.power = power;		
	}

	@Override
	public long getMaxPower() {
		return maxpwr; //temp
	}
	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);

		this.power = nbt.getLong("power");
		this.maxpwr = nbt.getLong("maxpwr");
	}

	@Override
	public void writeToNBT(NBTTagCompound nbt) {
		super.writeToNBT(nbt);
		
		nbt.setLong("power", power);
		nbt.setLong("maxpwr", maxpwr);
	}

	@Override
	public String getName() {
		return "container.solarPanel";
	}

}
