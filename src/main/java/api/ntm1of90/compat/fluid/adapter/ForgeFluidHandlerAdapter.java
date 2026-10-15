package api.ntm1of90.compat.fluid.adapter;

import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.tank.FluidTank;

import api.ntm1of90.compat.fluid.registry.FluidMappingRegistry;
import api.ntm1of90.compat.fluid.util.NTMForgeFluidConverter;
import com.hbm.util.FluidDebug;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidHandler;

/**
 * A base adapter class that implements Forge's IFluidHandler interface for HBM tile entities.
 * This makes it easier to add Forge fluid compatibility to HBM tile entities.
 */
public abstract class ForgeFluidHandlerAdapter implements IFluidHandler {

    static {
        // Initialize the fluid mapping registry
        FluidMappingRegistry.initialize();
    }

    /**
     * Get the HBM fluid tanks for this tile entity
     * @return An array of HBM fluid tanks
     */
    protected abstract FluidTank[] getHbmTanks();

    /**
     * Get the tile entity that contains the tanks
     * @return The tile entity
     */
    protected abstract TileEntity getTileEntity();

    /**
     * Check if filling from the given direction is allowed.
     */
    protected abstract boolean isFillAllowed(ForgeDirection from);

    /**
     * Check if draining from the given direction is allowed.
     */
    protected abstract boolean isDrainAllowed(ForgeDirection from);

    /**
     * Convert NTM fluid amount to Forge fluid amount (mB)
     */
    protected int toForgeAmount(int ntmAmount) {
        return NTMForgeFluidConverter.toForgeAmount(ntmAmount);
    }

    /**
     * Convert Forge fluid amount to NTM fluid amount
     */
    protected int toNTMAmount(int forgeAmount) {
        return NTMForgeFluidConverter.toNTMAmount(forgeAmount);
    }

    @Override
    public int fill(ForgeDirection from, FluidStack resource, boolean doFill) {
        if (resource == null || resource.amount <= 0 || !isFillAllowed(from)) {
            return 0;
        }

        FluidType ntmFluid = FluidMappingRegistry.getHbmFluidType(resource.getFluid());
        if (ntmFluid == Fluids.NONE) {
            return 0; // Unknown fluid
        }

        FluidTank[] tanks = getHbmTanks();
        for (FluidTank tank : tanks) {
                    if (tank.getPressure() != 0) continue; // only unpressurized
            int currentFill = tank.getFill();

            if (!canAcceptInto(tanks, tank, ntmFluid)) {
                continue; // Tank contains a different fluid or conflicts with a tank that owns it
            }

            int ntmAmount = toNTMAmount(resource.amount);
            int maxFill = tank.getMaxFill();
            int fillAmount = Math.min(ntmAmount, maxFill - currentFill);

            if (fillAmount <= 0) {
                continue; // Tank is full
            }

            // Fill the tank
            if (doFill) {
                if (currentFill == 0) {
                    tank.setTankType(ntmFluid);
                    FluidDebug.eventStack("forge.retask|" + FluidDebug.tileKey(getTileEntity()) + "|" + ntmFluid.getName(),
                        () -> "FORGE fill retasked NONE tank to " + FluidDebug.describe(ntmFluid) + " at " + FluidDebug.describeTile(getTileEntity()) + " by " + FluidDebug.callerHint());
                }
                tank.setFill(currentFill + fillAmount);

                TileEntity tile = getTileEntity();
                if (tile != null) {
                    tile.markDirty();
                }
            }

            int filled = toForgeAmount(fillAmount);
            FluidDebug.event("forge.fill|" + FluidDebug.tileKey(getTileEntity()) + "|" + ntmFluid.getName(),
                () -> "FORGE fill " + FluidDebug.describeTile(getTileEntity()) + " side=" + from + " " + FluidDebug.describeForgeStack(resource) + " doFill=" + doFill + " => " + filled + " by " + FluidDebug.callerHint());
            return filled;
        }

        FluidDebug.event("forge.fill|" + FluidDebug.tileKey(getTileEntity()) + "|" + ntmFluid.getName(),
            () -> "FORGE fill " + FluidDebug.describeTile(getTileEntity()) + " side=" + from + " " + FluidDebug.describeForgeStack(resource) + " doFill=" + doFill + " => 0 (rejected) by " + FluidDebug.callerHint());
        return 0; // No tank could accept the fluid
    }

    @Override
    public FluidStack drain(ForgeDirection from, FluidStack resource, boolean doDrain) {
        if (resource == null || resource.amount <= 0 || !isDrainAllowed(from)) {
            return null;
        }

        FluidType ntmFluid = FluidMappingRegistry.getHbmFluidType(resource.getFluid());
        if (ntmFluid == Fluids.NONE) {
            return null; // Unknown fluid
        }

        FluidTank[] tanks = getHbmTanks();
        for (FluidTank tank : tanks) {
                    if (tank.getPressure() != 0) continue; // only unpressurized
            int currentFill = tank.getFill();
            FluidType currentType = tank.getTankType();

            if (currentFill <= 0 || currentType != ntmFluid) {
                continue; // Tank is empty or contains a different fluid
            }

            int ntmAmount = toNTMAmount(resource.amount);
            int drainAmount = Math.min(ntmAmount, currentFill);

            if (drainAmount <= 0) {
                continue; // Nothing to drain
            }

            // Drain the tank
            if (doDrain) {
                int newFill = currentFill - drainAmount;
                tank.setFill(newFill);

                TileEntity tile = getTileEntity();
                if (tile != null) {
                    tile.markDirty();
                }
            }

            Fluid forgeFluid = FluidMappingRegistry.getForgeFluid(currentType);
            if (forgeFluid != null) {
                FluidStack drained = new FluidStack(forgeFluid, toForgeAmount(drainAmount));
                FluidDebug.event("forge.drain|" + FluidDebug.tileKey(getTileEntity()) + "|" + ntmFluid.getName(),
                    () -> "FORGE drain " + FluidDebug.describeTile(getTileEntity()) + " side=" + from + " " + FluidDebug.describeForgeStack(resource) + " doDrain=" + doDrain + " => " + FluidDebug.describeForgeStack(drained) + " by " + FluidDebug.callerHint());
                return drained;
            }
        }

        FluidDebug.event("forge.drain|" + FluidDebug.tileKey(getTileEntity()) + "|" + ntmFluid.getName(),
            () -> "FORGE drain " + FluidDebug.describeTile(getTileEntity()) + " side=" + from + " " + FluidDebug.describeForgeStack(resource) + " doDrain=" + doDrain + " => null by " + FluidDebug.callerHint());
        return null; // No tank could provide the fluid
    }

    @Override
    public FluidStack drain(ForgeDirection from, int maxDrain, boolean doDrain) {
        if (maxDrain <= 0 || !isDrainAllowed(from)) {
            return null;
        }

        FluidTank[] tanks = getHbmTanks();
        for (FluidTank tank : tanks) {
                    if (tank.getPressure() != 0) continue; // only unpressurized
            int currentFill = tank.getFill();
            FluidType currentType = tank.getTankType();

            if (currentFill <= 0 || currentType == Fluids.NONE) {
                continue; // Tank is empty
            }

            // Get the corresponding Forge fluid
            Fluid forgeFluid = FluidMappingRegistry.getForgeFluid(currentType);
            if (forgeFluid == null) {
                continue; // No Forge fluid mapping
            }

            int ntmDrainAmount = toNTMAmount(maxDrain);
            int drainAmount = Math.min(ntmDrainAmount, currentFill);

            if (drainAmount <= 0) {
                continue; // Nothing to drain
            }

            // Drain the tank
            if (doDrain) {
                int newFill = currentFill - drainAmount;
                tank.setFill(newFill);

                TileEntity tile = getTileEntity();
                if (tile != null) {
                    tile.markDirty();
                }
            }

            FluidStack drainedAll = new FluidStack(forgeFluid, toForgeAmount(drainAmount));
            FluidDebug.event("forge.drainall|" + FluidDebug.tileKey(getTileEntity()) + "|" + currentType.getName(),
                () -> "FORGE drain-all " + FluidDebug.describeTile(getTileEntity()) + " side=" + from + " maxDrain=" + maxDrain + " doDrain=" + doDrain + " => " + FluidDebug.describeForgeStack(drainedAll) + " by " + FluidDebug.callerHint());
            return drainedAll;
        }

        return null; // No tank could provide fluid
    }

    @Override
    public boolean canFill(ForgeDirection from, Fluid fluid) {
        if (!isFillAllowed(from)) {
            return false;
        }

        FluidTank[] tanks = getHbmTanks();

        if (fluid == null) {
            for (FluidTank tank : tanks) {
                        if (tank.getPressure() != 0) continue; // only unpressurized
                if (tank.getFill() < tank.getMaxFill()) {
                    FluidDebug.event("forge.canfill|" + FluidDebug.tileKey(getTileEntity()),
                        () -> "FORGE canFill " + FluidDebug.describeTile(getTileEntity()) + " side=" + from + " null => true by " + FluidDebug.callerHint());
                    return true;
                }
            }
            return false;
        }

        FluidType ntmFluid = FluidMappingRegistry.getHbmFluidType(fluid);
        if (ntmFluid == Fluids.NONE) {
            return false; // Unknown fluid
        }

        for (FluidTank tank : tanks) {
                    if (tank.getPressure() != 0) continue; // only unpressurized
            int currentFill = tank.getFill();
            int maxFill = tank.getMaxFill();

            if (currentFill < maxFill && canAcceptInto(tanks, tank, ntmFluid)) {
                FluidDebug.event("forge.canfill|" + FluidDebug.tileKey(getTileEntity()) + "|" + ntmFluid.getName(),
                    () -> "FORGE canFill " + FluidDebug.describeTile(getTileEntity()) + " side=" + from + " " + FluidDebug.describeForgeFluid(fluid) + " => true (" + FluidDebug.describeTank(tank) + ") by " + FluidDebug.callerHint());
                return true; // Tank can accept the fluid
            }
        }

        FluidDebug.event("forge.canfill|" + FluidDebug.tileKey(getTileEntity()) + "|" + FluidDebug.describeForgeFluid(fluid),
            () -> "FORGE canFill " + FluidDebug.describeTile(getTileEntity()) + " side=" + from + " " + FluidDebug.describeForgeFluid(fluid) + " => false by " + FluidDebug.callerHint());
        return false; // No tank can accept the fluid
    }

    @Override
    public boolean canDrain(ForgeDirection from, Fluid fluid) {
        if (!isDrainAllowed(from)) {
            return false;
        }

        FluidTank[] tanks = getHbmTanks();

        if (fluid == null) {
            for (FluidTank tank : tanks) {
                        if (tank.getPressure() != 0) continue; // only unpressurized
                if (tank.getFill() > 0 && tank.getTankType() != Fluids.NONE) {
                    FluidDebug.event("forge.candrain|" + FluidDebug.tileKey(getTileEntity()),
                        () -> "FORGE canDrain " + FluidDebug.describeTile(getTileEntity()) + " side=" + from + " null => true by " + FluidDebug.callerHint());
                    return true;
                }
            }
            return false;
        }

        FluidType ntmFluid = FluidMappingRegistry.getHbmFluidType(fluid);
        if (ntmFluid == Fluids.NONE) {
            return false; // Unknown fluid
        }

        // Find a tank that contains this specific fluid
        for (FluidTank tank : tanks) {
                    if (tank.getPressure() != 0) continue; // only unpressurized
            int currentFill = tank.getFill();
            FluidType currentType = tank.getTankType();

            if (currentFill > 0 && currentType == ntmFluid) {
                FluidDebug.event("forge.candrain|" + FluidDebug.tileKey(getTileEntity()) + "|" + ntmFluid.getName(),
                    () -> "FORGE canDrain " + FluidDebug.describeTile(getTileEntity()) + " side=" + from + " " + FluidDebug.describeForgeFluid(fluid) + " => true (" + FluidDebug.describeTank(tank) + ") by " + FluidDebug.callerHint());
                return true; // Tank contains the fluid
            }
        }

        FluidDebug.event("forge.candrain|" + FluidDebug.tileKey(getTileEntity()) + "|" + FluidDebug.describeForgeFluid(fluid),
            () -> "FORGE canDrain " + FluidDebug.describeTile(getTileEntity()) + " side=" + from + " " + FluidDebug.describeForgeFluid(fluid) + " => false by " + FluidDebug.callerHint());
        return false; // No tank contains the fluid
    }

    @Override
    public FluidTankInfo[] getTankInfo(ForgeDirection from) {
        FluidTank[] hbmTanks = getHbmTanks();
        java.util.List<FluidTankInfo> list = new java.util.ArrayList<>();
        StringBuilder summary = FluidDebug.isEnabled() ? new StringBuilder() : null;
        for (FluidTank tank : hbmTanks) {
            if (tank.getPressure() != 0) continue;
            int currentFill = tank.getFill();
            int maxFill = tank.getMaxFill();
            FluidType currentType = tank.getTankType();
            FluidStack stack = null;
            if (currentFill > 0 && currentType != Fluids.NONE) {
                Fluid forgeFluid = FluidMappingRegistry.getForgeFluid(currentType);
                if (forgeFluid != null) {
                    stack = new FluidStack(forgeFluid, toForgeAmount(currentFill));
                }
            }
            list.add(new FluidTankInfo(stack, toForgeAmount(maxFill)));
            if (summary != null) {
                if (summary.length() > 0) summary.append(", ");
                summary.append(stack == null ? "empty" : FluidDebug.describeForgeStack(stack)).append("/").append(toForgeAmount(maxFill));
            }
        }
        if (summary != null) {
            FluidDebug.event("forge.tankinfo|" + FluidDebug.tileKey(getTileEntity()),
                () -> "FORGE getTankInfo " + FluidDebug.describeTile(getTileEntity()) + " side=" + from + " => [" + summary + "] by " + FluidDebug.callerHint());
        }
        return list.toArray(new FluidTankInfo[0]);
    }

    /** Untyped (NONE) tanks must not steal a type another tank already owns. */
    protected boolean canAcceptInto(FluidTank[] tanks, FluidTank tank, FluidType ntmFluid) {
        FluidType currentType = tank.getTankType();
        if (currentType != Fluids.NONE && currentType != ntmFluid) return false;
        if (currentType == Fluids.NONE) {
            for (FluidTank other : tanks) {
                if (other != tank && other.getTankType() == ntmFluid) {
                    FluidDebug.event("forge.conflict|" + FluidDebug.tileKey(getTileEntity()) + "|" + ntmFluid.getName(),
                        () -> "FORGE routing refused: NONE tank " + FluidDebug.describeTank(tank) + " keeps type, " + FluidDebug.describe(ntmFluid) + " already owned by another tank at " + FluidDebug.describeTile(getTileEntity()));
                    return false;
                }
            }
        }
        return true;
    }
}
