package com.hbm.tileentity.machine;

import api.hbm.redstoneoverradio.IRORInteractive;
import api.hbm.redstoneoverradio.IRORValueProvider;
import api.hbm.redstoneoverradio.RORFunctionException;

import com.hbm.blocks.BlockDummyable;
import com.hbm.dim.CelestialBody;
import com.hbm.dim.orbit.WorldProviderOrbit;
import com.hbm.dim.trait.CBT_Atmosphere;
import com.hbm.handler.atmosphere.ChunkAtmosphereManager;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.tank.FluidTank;
import com.hbm.inventory.fluid.trait.FT_Gaseous;
import com.hbm.module.portmanager.ModulePortManFluidAdaptive;
import com.hbm.module.portmanager.ModulePortManPower;
import com.hbm.tileentity.TileEntityMachineBase;
import com.hbm.tileentity.TilePort.PortDef;
import com.hbm.util.RORFluidUtil;

import api.hbm.energymk2.IEnergyReceiverMK2;
import api.hbm.fluidmk2.IFluidStandardSenderMK2;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.AxisAlignedBB;
import net.minecraftforge.common.util.ForgeDirection;

public class TileEntityAtmosphericCompressor extends TileEntityMachineBase implements IEnergyReceiverMK2, IFluidStandardSenderMK2, IRORValueProvider, IRORInteractive {

	int consumption = 200;
	public float rot;
	public float prevRot;
	private float rotSpeed;
	public long power = 0;
	public FluidTank tank;

	protected ModulePortManFluidAdaptive moduleFluidPorts;
	protected ModulePortManPower modulePowerPorts;

	public TileEntityAtmosphericCompressor() {
		super(0);
		tank = new FluidTank(Fluids.EARTHAIR, 50000);

		moduleFluidPorts = new ModulePortManFluidAdaptive(this).setOutputTanks(getSendingTanks());
		modulePowerPorts = new ModulePortManPower(this);
	}

	@Override
	public String getName() {
		return "container.air";
	}

	@Override
	public void updateEntity() {

		if(!worldObj.isRemote) {
			moduleFluidPorts.update(getPorts());
			modulePowerPorts.update(getPorts());

			// Extractors will not work indoors (or in space oops)
			CBT_Atmosphere atmosphere = getAvailableAtmosphere();

			boolean canCollect = updateTankForAtmosphere(atmosphere);

			if(canCollect && hasPower() && tank.getFill() + 200 <= tank.getMaxFill()) {
				tank.setFill(tank.getFill() + 200);
				power -= this.getMaxPower() / 50;

				FT_Gaseous.capture(worldObj, tank.getTankType(), 200);
			}

			markDirty();

			this.networkPackNT(50);
		} else {
			float maxSpeed = 30F;

			if(hasPower()) {
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

	public void cycleGas() {
		if(tank.getFill() != 0) return;
		CBT_Atmosphere atmosphere = getAvailableAtmosphere();

		if(atmosphere == null || atmosphere.getPressure() <= 0.0D) return;

		atmosphere.sortDescending();

		FluidType currentFluid = tank.getTankType();

		for(int i = 0; i < atmosphere.fluids.size(); i++) {
			if(atmosphere.fluids.get(i).fluid == currentFluid) {
				int targetIndex = i + 1;
				if(targetIndex >= atmosphere.fluids.size()) targetIndex = 0;
				if(atmosphere.fluids.get(targetIndex).pressure <= 0.0001) targetIndex = 0;

				switchGas(atmosphere.fluids.get(targetIndex).fluid);
				break;
			}
		}
	}

	public boolean switchGas(FluidType targetFluid) {
		if(targetFluid == tank.getTankType()) return false;
		if(tank.getFill() != 0) return false;

		CBT_Atmosphere atmosphere = !ChunkAtmosphereManager.proxy.hasAtmosphere(worldObj, xCoord, yCoord, zCoord)
			? CelestialBody.getTrait(worldObj, CBT_Atmosphere.class)
			: null;
		if(atmosphere == null) return false;

		for(CBT_Atmosphere.FluidEntry entry : atmosphere.fluids) {
			if(entry.fluid == targetFluid) {
				tank.setTankType(targetFluid);
				return true;
			}
		}
		return false;
	}

	/** Preserves stored gas and returns whether the current atmosphere can supply more of it. */
	protected boolean updateTankForAtmosphere(CBT_Atmosphere atmosphere) {
		boolean available = atmosphere != null && atmosphere.getPressure() > 0.0D;
		if(tank.getFill() == 0) {
			if(!available) {
				tank.setTankType(Fluids.NONE);
			} else if(!atmosphere.hasFluid(tank.getTankType())) {
				tank.setTankType(atmosphere.getMainFluid());
			}
		}
		return available && tank.getTankType() != Fluids.NONE && atmosphere.hasFluid(tank.getTankType());
	}

	protected CBT_Atmosphere getAvailableAtmosphere() {
		return worldObj != null && !(worldObj.provider instanceof WorldProviderOrbit) && !ChunkAtmosphereManager.proxy.hasAtmosphere(worldObj, xCoord, yCoord, zCoord)
			? CelestialBody.getTrait(worldObj, CBT_Atmosphere.class)
			: null;
	}

	private boolean canSelectFluid(FluidType type) {
		CBT_Atmosphere atmosphere = getAvailableAtmosphere();
		return atmosphere != null && atmosphere.getPressure() > 0.0D && atmosphere.hasFluid(type);
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

	public boolean hasPower() {
		return power >= this.getMaxPower() / 50;
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);
		this.power = nbt.getLong("power");
		tank.readFromNBT(nbt, "water");
	}

	@Override
	public void writeToNBT(NBTTagCompound nbt) {
		super.writeToNBT(nbt);
		nbt.setLong("power", power);
		tank.writeToNBT(nbt, "water");
	}



	@Override
	public void setPower(long i) {
		power = i;
	}

	@Override
	public long getPower() {
		return power;
	}

	@Override
	public long getMaxPower() {
		return 1000000;
	}

	@Override
	public FluidTank[] getSendingTanks() {
		return new FluidTank[] { tank };
	}

	@Override
	public FluidTank[] getAllTanks() {
		return new FluidTank[] { tank };
	}

	protected PortDef[] cachedPorts;

	public PortDef[] getPorts() {
		if(cachedPorts == null) {
			ForgeDirection dir = ForgeDirection.getOrientation(this.getBlockMetadata() - BlockDummyable.offset);
			ForgeDirection rot = dir.getRotation(ForgeDirection.DOWN);
	
			cachedPorts = new PortDef[] {
				PortDef.make(xCoord - dir.offsetX, yCoord, zCoord - dir.offsetZ, dir.getOpposite(), rot.getOpposite()),
				PortDef.make(xCoord - dir.offsetX + rot.offsetX, yCoord, zCoord - dir.offsetZ + rot.offsetZ, dir.getOpposite(), rot),

				PortDef.make(xCoord, yCoord, zCoord, dir, rot.getOpposite()),
				PortDef.make(xCoord + rot.offsetX, yCoord, zCoord + rot.offsetZ, dir, rot),

			};
		}

		return cachedPorts;
	}

	AxisAlignedBB bb = null;

	@Override
	public AxisAlignedBB getRenderBoundingBox() {
		if(bb == null) {
			bb = AxisAlignedBB.getBoundingBox(
				xCoord - 1,
				yCoord,
				zCoord - 1,
				xCoord + 2,
				yCoord + 10,
				zCoord + 2
			);
		}

		return bb;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public double getMaxRenderDistanceSquared() {
		return 65536.0D;
	}


	@Override
	public String[] getFunctionInfo() {
		String[] info = new String[] {
				RORFluidUtil.SET_FLUID_INFO,
		};
		info = RORFluidUtil.addSingleTankInfo(info);
		return info;
	}

	@Override
	public String provideRORValue(String name) {
		String fluidValue = RORFluidUtil.getSingleTankValue(name, tank);
		if(fluidValue != null) return fluidValue;

		return null;
	}

	@Override
	public String runRORFunction(String name, String[] params) {
		if(RORFluidUtil.SET_FLUID.equals(name)) {
			if(RORFluidUtil.setFluid(tank, params, type -> {
				if(!canSelectFluid(type)) throw new RORFunctionException("Exception: Fluid Not Present In Atmosphere");
			})) {
				this.markDirty();
				if(worldObj != null) worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
			}
			return null;
		}
		return null;
	}
}
