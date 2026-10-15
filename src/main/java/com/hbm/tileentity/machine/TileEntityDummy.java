package com.hbm.tileentity.machine;

import com.hbm.interfaces.IMultiblock;
import com.hbm.util.FluidDebug;
import api.ntm1of90.compat.fluid.registry.ForgeFluidAdapterRegistry;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidHandler;

/**
 * Multiblock dummy port exposing the core tile's IFluidHandler to external mods.
 */
public class TileEntityDummy extends TileEntity implements IFluidHandler {

	public int targetX;
	public int targetY;
	public int targetZ;

	private IFluidHandler cachedHandler;
	private boolean handlerResolved = false;

    @Override
	public void updateEntity() {
    	if(!this.worldObj.isRemote) {
    		if(!(this.worldObj.getBlock(targetX, targetY, targetZ) instanceof IMultiblock)) {
    			worldObj.func_147480_a(xCoord, yCoord, zCoord, false);
    		}
    	}
    }

	private IFluidHandler getTargetHandler() {
		if(this.worldObj == null || this.worldObj.isRemote) return null;
		// re-resolve every second in case the core TE was replaced/repaired
		if(!this.handlerResolved || this.worldObj.getTotalWorldTime() % 20 == 0) {
			TileEntity target = this.worldObj.getTileEntity(targetX, targetY, targetZ);
			IFluidHandler prev = this.cachedHandler;
			if(target == null || target instanceof TileEntityDummy) {
				this.cachedHandler = null;
			} else {
				this.cachedHandler = ForgeFluidAdapterRegistry.getFluidHandler(target);
			}
			this.handlerResolved = this.cachedHandler != null;
			if(this.cachedHandler != prev) {
				FluidDebug.event("dummy.resolve|" + FluidDebug.tileKey(this),
					() -> "DUMMY handler " + (this.cachedHandler == null ? "lost" : "resolved to " + this.cachedHandler.getClass().getSimpleName())
					+ " for port " + FluidDebug.describeTile(this) + " -> target " + (target == null ? "null" : FluidDebug.describeTile(target)));
			}
		}
		return this.cachedHandler;
	}

	@Override
	public int fill(ForgeDirection from, FluidStack resource, boolean doFill) {
		IFluidHandler h = getTargetHandler();
		return h == null ? 0 : h.fill(from, resource, doFill);
	}

	@Override
	public FluidStack drain(ForgeDirection from, FluidStack resource, boolean doDrain) {
		IFluidHandler h = getTargetHandler();
		return h == null ? null : h.drain(from, resource, doDrain);
	}

	@Override
	public FluidStack drain(ForgeDirection from, int maxDrain, boolean doDrain) {
		IFluidHandler h = getTargetHandler();
		return h == null ? null : h.drain(from, maxDrain, doDrain);
	}

	@Override
	public boolean canFill(ForgeDirection from, Fluid fluid) {
		IFluidHandler h = getTargetHandler();
		return h != null && h.canFill(from, fluid);
	}

	@Override
	public boolean canDrain(ForgeDirection from, Fluid fluid) {
		IFluidHandler h = getTargetHandler();
		return h != null && h.canDrain(from, fluid);
	}

	@Override
	public FluidTankInfo[] getTankInfo(ForgeDirection from) {
		IFluidHandler h = getTargetHandler();
		return h == null ? new FluidTankInfo[0] : h.getTankInfo(from);
	}

    @Override
	public void readFromNBT(NBTTagCompound nbt)
    {
    	super.readFromNBT(nbt);
        this.targetX = nbt.getInteger("tx");
        this.targetY = nbt.getInteger("ty");
        this.targetZ = nbt.getInteger("tz");
    }

    @Override
	public void writeToNBT(NBTTagCompound nbt)
    {
    	super.writeToNBT(nbt);
    	nbt.setInteger("tx", this.targetX);
    	nbt.setInteger("ty", this.targetY);
    	nbt.setInteger("tz", this.targetZ);
    }
}
