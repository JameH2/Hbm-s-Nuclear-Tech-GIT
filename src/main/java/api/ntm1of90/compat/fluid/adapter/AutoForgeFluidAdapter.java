package api.ntm1of90.compat.fluid.adapter;

import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.tank.FluidTank;

import api.hbm.fluidmk2.IFluidReceiverMK2;
import api.hbm.fluidmk2.IFluidStandardReceiverMK2;
import api.hbm.fluidmk2.IFluidStandardSenderMK2;
import api.hbm.fluidmk2.IFluidUserMK2;
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
 * An automatic adapter that implements Forge's IFluidHandler interface for any HBM fluid tile entity.
 * This class is used to make all HBM fluid tile entities compatible with Forge's fluid system.
 */
public class AutoForgeFluidAdapter implements IFluidHandler {

    static {
        // Initialize the fluid mapping registry
        FluidMappingRegistry.initialize();
    }

    private final IFluidUserMK2 fluidUser;
    private final TileEntity tileEntity;

    /**
     * Create a new adapter for an HBM fluid tile entity
     *
     * @param fluidUser The HBM fluid user to adapt
     * @param tileEntity The tile entity that contains the fluid user
     */
    public AutoForgeFluidAdapter(IFluidUserMK2 fluidUser, TileEntity tileEntity) {
        this.fluidUser = fluidUser;
        this.tileEntity = tileEntity;
    }

    /**
     * Convert NTM fluid amount to Forge fluid amount (mB)
     */
    private int toForgeAmount(int ntmAmount) {
        return NTMForgeFluidConverter.toForgeAmount(ntmAmount);
    }

    /**
     * Convert Forge fluid amount to NTM fluid amount
     */
    private int toNTMAmount(int forgeAmount) {
        return NTMForgeFluidConverter.toNTMAmount(forgeAmount);
    }

    @Override
    public int fill(ForgeDirection from, FluidStack resource, boolean doFill) {
        if (resource == null || resource.amount <= 0) {
            return 0;
        }

        FluidType ntmFluid = FluidMappingRegistry.getHbmFluidType(resource.getFluid());
        if (ntmFluid == Fluids.NONE) {
            return 0; // Unknown fluid
        }

        if (fluidUser instanceof IFluidStandardReceiverMK2) {
            IFluidStandardReceiverMK2 receiver = (IFluidStandardReceiverMK2) fluidUser;

            // Get the receiving tanks
            FluidTank[] tanks = receiver.getReceivingTanks();
            if (tanks.length == 0) {
                return 0; // No tanks to fill
            }

            for (FluidTank tank : tanks) {
                            if (tank.getPressure() != 0) continue; // only unpressurized via Forge
                int currentFill = tank.getFill();
                FluidType currentType = tank.getTankType();
                int maxFill = tank.getMaxFill();

                if (currentFill < maxFill && (canAcceptInto(tanks, tank, ntmFluid))) {
                    int ntmAmount = toNTMAmount(resource.amount);
                    int fillAmount = Math.min(ntmAmount, maxFill - currentFill);

                    if (fillAmount <= 0) {
                        continue; // Tank is full
                    }

                    // Fill the tank
                    if (doFill) {
                        if (currentType == Fluids.NONE) {
                            tank.setTankType(ntmFluid);
                            FluidDebug.eventStack("forge.retask|" + FluidDebug.tileKey(tileEntity) + "|" + ntmFluid.getName(),
                                () -> "FORGE fill retasked NONE tank to " + FluidDebug.describe(ntmFluid) + " at " + FluidDebug.describeTile(tileEntity) + " by " + FluidDebug.callerHint());
                        }
                        tank.setFill(currentFill + fillAmount);

                        if (tileEntity != null) {
                            tileEntity.markDirty();
                        }
                    }

                    int filled = toForgeAmount(fillAmount);
                    FluidDebug.event("forge.fill|" + FluidDebug.tileKey(tileEntity) + "|" + ntmFluid.getName(),
                        () -> "FORGE fill " + FluidDebug.describeTile(tileEntity) + " side=" + from + " " + FluidDebug.describeForgeStack(resource) + " doFill=" + doFill + " => " + filled + " by " + FluidDebug.callerHint());
                    return filled;
                }
            }
        }

        FluidDebug.event("forge.fill|" + FluidDebug.tileKey(tileEntity) + "|" + (ntmFluid == null ? "null" : ntmFluid.getName()),
            () -> "FORGE fill " + FluidDebug.describeTile(tileEntity) + " side=" + from + " " + FluidDebug.describeForgeStack(resource) + " doFill=" + doFill + " => 0 (rejected) by " + FluidDebug.callerHint());
        return 0; // No tank could accept the fluid
    }

    @Override
    public FluidStack drain(ForgeDirection from, FluidStack resource, boolean doDrain) {
        if (resource == null || resource.amount <= 0) {
            return null;
        }

        FluidType ntmFluid = FluidMappingRegistry.getHbmFluidType(resource.getFluid());
        if (ntmFluid == Fluids.NONE) {
            return null; // Unknown fluid
        }

        if (fluidUser instanceof IFluidStandardSenderMK2) {
            IFluidStandardSenderMK2 sender = (IFluidStandardSenderMK2) fluidUser;

            // Get the sending tanks
            FluidTank[] tanks = sender.getSendingTanks();
            if (tanks.length == 0) {
                return null; // No tanks to drain
            }

            for (FluidTank tank : tanks) {
                            if (tank.getPressure() != 0) continue; // only unpressurized via Forge
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

                    // Never reset the tank type on drain: type is configuration, and wiping
                    // fixed-type tanks (e.g. Rotary STEAM/SPENTSTEAM) bricks them into NONE-ghosts.

                    if (tileEntity != null) {
                        tileEntity.markDirty();
                    }
                }

                Fluid forgeFluid = FluidMappingRegistry.getForgeFluid(currentType);
                if (forgeFluid != null) {
                    FluidStack drained = new FluidStack(forgeFluid, toForgeAmount(drainAmount));
                    FluidDebug.event("forge.drain|" + FluidDebug.tileKey(tileEntity) + "|" + ntmFluid.getName(),
                        () -> "FORGE drain " + FluidDebug.describeTile(tileEntity) + " side=" + from + " " + FluidDebug.describeForgeStack(resource) + " doDrain=" + doDrain + " => " + FluidDebug.describeForgeStack(drained) + " by " + FluidDebug.callerHint());
                    return drained;
                }
            }
        }

        FluidDebug.event("forge.drain|" + FluidDebug.tileKey(tileEntity) + "|" + ntmFluid.getName(),
            () -> "FORGE drain " + FluidDebug.describeTile(tileEntity) + " side=" + from + " " + FluidDebug.describeForgeStack(resource) + " doDrain=" + doDrain + " => null by " + FluidDebug.callerHint());
        return null; // No tank could provide the fluid
    }

    @Override
    public FluidStack drain(ForgeDirection from, int maxDrain, boolean doDrain) {
        if (maxDrain <= 0) {
            return null;
        }

        if (fluidUser instanceof IFluidStandardSenderMK2) {
            IFluidStandardSenderMK2 sender = (IFluidStandardSenderMK2) fluidUser;

            // Get the sending tanks
            FluidTank[] tanks = sender.getSendingTanks();
            if (tanks.length == 0) {
                return null; // No tanks to drain
            }

            for (FluidTank tank : tanks) {
                            if (tank.getPressure() != 0) continue; // only unpressurized via Forge
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

                    // Same as above: never reset the tank type on drain.

                    if (tileEntity != null) {
                        tileEntity.markDirty();
                    }
                }

                FluidStack drainedAll = new FluidStack(forgeFluid, toForgeAmount(drainAmount));
                FluidDebug.event("forge.drainall|" + FluidDebug.tileKey(tileEntity) + "|" + currentType.getName(),
                    () -> "FORGE drain-all " + FluidDebug.describeTile(tileEntity) + " side=" + from + " maxDrain=" + maxDrain + " doDrain=" + doDrain + " => " + FluidDebug.describeForgeStack(drainedAll) + " by " + FluidDebug.callerHint());
                return drainedAll;
            }
        }

        return null; // No tank could provide fluid
    }

    @Override
    public boolean canFill(ForgeDirection from, Fluid fluid) {
        if (fluid == null) {
            return false;
        }

        FluidType ntmFluid = FluidMappingRegistry.getHbmFluidType(fluid);
        if (ntmFluid == Fluids.NONE) {
            return false; // Unknown fluid
        }

        if (fluidUser instanceof IFluidStandardReceiverMK2) {
            IFluidStandardReceiverMK2 receiver = (IFluidStandardReceiverMK2) fluidUser;

            // Get the receiving tanks
            FluidTank[] tanks = receiver.getReceivingTanks();
            if (tanks.length == 0) {
                return false; // No tanks to fill
            }

            for (FluidTank tank : tanks) {
                            if (tank.getPressure() != 0) continue; // only unpressurized via Forge
                int currentFill = tank.getFill();
                FluidType currentType = tank.getTankType();
                int maxFill = tank.getMaxFill();

                if (currentFill < maxFill && (canAcceptInto(tanks, tank, ntmFluid))) {
                    FluidDebug.event("forge.canfill|" + FluidDebug.tileKey(tileEntity) + "|" + ntmFluid.getName(),
                        () -> "FORGE canFill " + FluidDebug.describeTile(tileEntity) + " side=" + from + " " + FluidDebug.describeForgeFluid(fluid) + " => true (" + FluidDebug.describeTank(tank) + ") by " + FluidDebug.callerHint());
                    return true; // Tank can accept the fluid
                }
            }
        }

        FluidDebug.event("forge.canfill|" + FluidDebug.tileKey(tileEntity) + "|" + FluidDebug.describeForgeFluid(fluid),
            () -> "FORGE canFill " + FluidDebug.describeTile(tileEntity) + " side=" + from + " " + FluidDebug.describeForgeFluid(fluid) + " => false by " + FluidDebug.callerHint());
        return false; // No tank can accept the fluid
    }

    @Override
    public boolean canDrain(ForgeDirection from, Fluid fluid) {
        if (fluid == null) {
            return false;
        }

        FluidType ntmFluid = FluidMappingRegistry.getHbmFluidType(fluid);
        if (ntmFluid == Fluids.NONE) {
            return false; // Unknown fluid
        }

        if (fluidUser instanceof IFluidStandardSenderMK2) {
            IFluidStandardSenderMK2 sender = (IFluidStandardSenderMK2) fluidUser;

            // Get the sending tanks
            FluidTank[] tanks = sender.getSendingTanks();
            if (tanks.length == 0) {
                return false; // No tanks to drain
            }

            for (FluidTank tank : tanks) {
                            if (tank.getPressure() != 0) continue; // only unpressurized via Forge
                int currentFill = tank.getFill();
                FluidType currentType = tank.getTankType();

                if (currentFill > 0 && currentType == ntmFluid) {
                    FluidDebug.event("forge.candrain|" + FluidDebug.tileKey(tileEntity) + "|" + ntmFluid.getName(),
                        () -> "FORGE canDrain " + FluidDebug.describeTile(tileEntity) + " side=" + from + " " + FluidDebug.describeForgeFluid(fluid) + " => true (" + FluidDebug.describeTank(tank) + ") by " + FluidDebug.callerHint());
                    return true; // Tank contains the fluid
                }
            }
        }

        FluidDebug.event("forge.candrain|" + FluidDebug.tileKey(tileEntity) + "|" + FluidDebug.describeForgeFluid(fluid),
            () -> "FORGE canDrain " + FluidDebug.describeTile(tileEntity) + " side=" + from + " " + FluidDebug.describeForgeFluid(fluid) + " => false by " + FluidDebug.callerHint());
        return false; // No tank contains the fluid
    }

    @Override
    public FluidTankInfo[] getTankInfo(ForgeDirection from) {
        FluidTank[] all = fluidUser.getAllTanks();
        java.util.List<FluidTankInfo> list = new java.util.ArrayList<>();
        StringBuilder summary = FluidDebug.isEnabled() ? new StringBuilder() : null;
        for (FluidTank tank : all) {
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
            FluidDebug.event("forge.tankinfo|" + FluidDebug.tileKey(tileEntity),
                () -> "FORGE getTankInfo " + FluidDebug.describeTile(tileEntity) + " side=" + from + " => [" + summary + "] by " + FluidDebug.callerHint());
        }
        return list.toArray(new FluidTankInfo[0]);
    }

    /** Untyped (NONE) tanks need machine demand, and must not steal a type another tank already owns. */
    private boolean canAcceptInto(FluidTank[] tanks, FluidTank tank, FluidType ntmFluid) {
        FluidType currentType = tank.getTankType();
        if (currentType != Fluids.NONE && currentType != ntmFluid) return false;
        if (currentType == Fluids.NONE) {
            for (FluidTank other : tanks) {
                if (other != tank && other.getTankType() == ntmFluid) {
                    FluidDebug.event("forge.conflict|" + FluidDebug.tileKey(tileEntity) + "|" + ntmFluid.getName(),
                        () -> "FORGE routing refused: NONE tank " + FluidDebug.describeTank(tank) + " keeps type, " + FluidDebug.describe(ntmFluid) + " already owned by " + FluidDebug.describeTank(other) + " at " + FluidDebug.describeTile(tileEntity));
                    return false;
                }
            }
            if (fluidUser instanceof IFluidReceiverMK2) {
                return ((IFluidReceiverMK2) fluidUser).getDemand(ntmFluid, tank.getPressure()) > 0;
            }
        }
        return true;
    }
}