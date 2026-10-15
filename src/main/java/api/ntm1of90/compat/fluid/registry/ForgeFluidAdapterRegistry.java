package api.ntm1of90.compat.fluid.registry;

import java.util.Map;
import java.util.WeakHashMap;

import com.hbm.main.MainRegistry;

import api.hbm.fluidmk2.IFluidUserMK2;
import api.ntm1of90.compat.fluid.adapter.AutoForgeFluidAdapter;
import com.hbm.util.FluidDebug;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.fluids.IFluidHandler;

/**
 * A registry for managing fluid adapters for tile entities.
 * This allows for Forge fluid compatibility without replacing tile entities.
 */
public class ForgeFluidAdapterRegistry {

    // Use a WeakHashMap to avoid memory leaks when tile entities are unloaded
    private static final Map<TileEntity, IFluidHandler> adapterMap = new WeakHashMap<>();

    private static boolean initialized = false;

    /**
     * Initialize the registry
     */
    public static void initialize() {
        if (initialized) {
            return;
        }

        initialized = true;
        MainRegistry.logger.info("ForgeFluidAdapterRegistry initialized");
    }

    /**
     * Get a fluid handler for a tile entity
     *
     * @param tileEntity The tile entity to get a fluid handler for
     * @return The fluid handler, or null if the tile entity is not a fluid handler
     */
    public static IFluidHandler getFluidHandler(TileEntity tileEntity) {
        if (tileEntity == null) {
            return null;
        }

        if (tileEntity instanceof IFluidHandler) {
            return (IFluidHandler) tileEntity;
        }

        if (adapterMap.containsKey(tileEntity)) {
            return adapterMap.get(tileEntity);
        }

        if (isBlacklistedTileEntity(tileEntity)) {
            return null;
        }

        if (tileEntity instanceof IFluidUserMK2) {
            IFluidUserMK2 fluidUser = (IFluidUserMK2) tileEntity;

            // Check if the tile entity has any fluid tanks
            com.hbm.inventory.fluid.tank.FluidTank[] tanks = fluidUser.getAllTanks();
            if (tanks == null || tanks.length == 0) {
                return null;
            }

            IFluidHandler adapter = new AutoForgeFluidAdapter(fluidUser, tileEntity);
            adapterMap.put(tileEntity, adapter);
            FluidDebug.event("adapter.created|" + FluidDebug.tileKey(tileEntity),
                () -> "ADAPTER created AutoForgeFluidAdapter for " + FluidDebug.describeTile(tileEntity) + " with " + tanks.length + " tank(s) requested by " + FluidDebug.callerHint());
            return adapter;
        }

        if (tileEntity instanceof com.hbm.tileentity.machine.TileEntityDummy) {
            com.hbm.tileentity.machine.TileEntityDummy dummy = (com.hbm.tileentity.machine.TileEntityDummy) tileEntity;
            TileEntity target = tileEntity.getWorldObj().getTileEntity(dummy.targetX, dummy.targetY, dummy.targetZ);
            return getFluidHandler(target);
        }

        return null;
    }

    /**
     * Register a fluid handler for a tile entity
     *
     * @param tileEntity The tile entity to register a fluid handler for
     * @param fluidHandler The fluid handler to register
     */
    public static void registerFluidHandler(TileEntity tileEntity, IFluidHandler fluidHandler) {
        if (tileEntity != null && fluidHandler != null) {
            adapterMap.put(tileEntity, fluidHandler);
        }
    }

    /**
     * Unregister a fluid handler for a tile entity
     *
     * @param tileEntity The tile entity to unregister a fluid handler for
     */
    public static void unregisterFluidHandler(TileEntity tileEntity) {
        if (tileEntity != null) {
            adapterMap.remove(tileEntity);
        }
    }

    /**
     * Check if a tile entity is blacklisted from fluid compatibility
     *
     * @param tileEntity The tile entity to check
     * @return True if the tile entity is blacklisted, false otherwise
     */
    private static boolean isBlacklistedTileEntity(TileEntity tileEntity) {
        // All machines now expose Forge fluids via adapter/direct IFluidHandler.
        // Pressure filtering is handled in the adapters/handlers themselves.
        return false;
    }

    /**
     * Clear the adapter map
     * This should be called when a world is unloaded
     */
    public static void clearAdapters() {
        adapterMap.clear();
    }
}
