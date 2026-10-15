package api.ntm1of90.compat.fluid.registry;

import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.fluid.Fluids;

import api.ntm1of90.compat.fluid.render.ColoredForgeFluid;
import api.ntm1of90.compat.fluid.registry.FluidRegistry;
import api.ntm1of90.compat.fluid.util.NTMFluidLocalization;
import com.hbm.util.FluidDebug;
import net.minecraftforge.fluids.Fluid;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Registry for mapping between HBM's Nuclear Tech Mod fluids and Forge fluids.
 * This allows for compatibility between HBM's custom fluid system and mods that use Forge's fluid system.
 */
public class FluidMappingRegistry {

    // Mapping between Forge Fluid names and HBM FluidTypes
    private static final Map<String, FluidType> forgeToHbmMap = new HashMap<>();
    private static final Map<FluidType, String> hbmToForgeMap = new HashMap<>();
    private static boolean initialized = false;

    /**
     * Initialize the fluid mapping registry.
     * This should be called during mod initialization.
     */
    public static void initialize() {
        if (initialized) {
            return;
        }

        // Initialize the fluid mapping registry

        System.out.println("[NTM] Starting automatic fluid mapping between NTM and Forge fluid systems...");
        int mappedCount = 0;

        // Automatically map all HBM fluids to Forge fluids
        // This ensures that all fluids are correctly mapped without manual listing
        for (FluidType fluidType : Fluids.getAll()) {
            if (fluidType == Fluids.NONE) {
                continue;
            }

            if (fluidType.hasNoContainer() || fluidType.hasNoID()) {
                System.out.println("[NTM] Skipping special fluid: " + fluidType.getName());
                continue;
            }

            String forgeName = fluidType.getName().toLowerCase(Locale.US);

            registerFluidMapping(forgeName, fluidType);
            mappedCount++;

            addFluidAliases(fluidType);
        }

        mappedCount = forgeToHbmMap.size();

        for (String forgeName : forgeToHbmMap.keySet()) {
            if (net.minecraftforge.fluids.FluidRegistry.getFluid(forgeName) == null) {
                FluidType hbmFluid = forgeToHbmMap.get(forgeName);
                Fluid forgeFluid = new ColoredForgeFluid(forgeName, hbmFluid);
                net.minecraftforge.fluids.FluidRegistry.registerFluid(forgeFluid);
                System.out.println("[NTM] Registered new Forge fluid: " + forgeName + " with color 0x" + Integer.toHexString(((ColoredForgeFluid)forgeFluid).getColor()));
            }
        }

        // Explicitly register Forge fluids for special NOCON/NOID fluid types (SPENTSTEAM, SMOKE,
        // PLASMA_*, GAS_WATZ, ...) which the mapping loop above intentionally skips. Doing this in
        // common init makes both logical sides agree on the Forge registry contents; previously
        // clients depended on the texture-stitch loop incidentally creating these via getForgeFluid().
        for (FluidType fluidType : Fluids.getAll()) {
            if (fluidType == Fluids.NONE) {
                continue;
            }
            if (getForgeFluidName(fluidType) != null) {
                continue;
            }

            String hbmName = fluidType.getName().toLowerCase(java.util.Locale.US);
            if (net.minecraftforge.fluids.FluidRegistry.getFluid(hbmName) != null) {
                continue;
            }

            Fluid newFluid = new ColoredForgeFluid(hbmName, fluidType);
            net.minecraftforge.fluids.FluidRegistry.registerFluid(newFluid);
            registerFluidMapping(hbmName, fluidType);
            FluidDebug.event("mapping.special|" + hbmName,
                () -> "MAPPING explicitly registered special/NOCON/NOID Forge fluid '" + hbmName
                    + "' for NTM " + FluidDebug.describe(fluidType));
        }

        System.out.println("[NTM] Fluid mapping complete! Mapped " + mappedCount + " NTM fluids to Forge fluid system.");
        System.out.println("[NTM] Total number of fluid mappings created: " + forgeToHbmMap.size());

        api.ntm1of90.compat.fluid.registry.FluidRegistry.initialize();

        NTMFluidLocalization.initialize();

        printFluidMappingSummary();

        initialized = true;
    }

    /**
     * Print a summary of all mapped fluids to the console
     */
    public static void printFluidMappingSummary() {
        System.out.println("\n[NTM] ===== FLUID MAPPING SUMMARY =====");
        System.out.println("[NTM] Total NTM fluids mapped: " + hbmToForgeMap.size());
        System.out.println("[NTM] Total Forge fluid mappings: " + forgeToHbmMap.size());

        Set<FluidType> mappedHbmFluids = new HashSet<>(hbmToForgeMap.keySet());
        System.out.println("[NTM] NTM Fluids with Forge Mappings: " + mappedHbmFluids.size());

        List<FluidType> sortedHbmFluids = new ArrayList<>(mappedHbmFluids);
        sortedHbmFluids.sort((f1, f2) -> f1.getName().compareTo(f2.getName()));

        for (FluidType hbmFluid : sortedHbmFluids) {
            List<String> forgeNames = new ArrayList<>();
            for (Map.Entry<String, FluidType> entry : forgeToHbmMap.entrySet()) {
                if (entry.getValue() == hbmFluid) {
                    forgeNames.add(entry.getKey());
                }
            }

            Collections.sort(forgeNames);

            // Print the HBM fluid and all its Forge mappings
            System.out.println("[NTM]   - " + hbmFluid.getName() + " -> " + String.join(", ", forgeNames));
        }

        List<FluidType> unmappedFluids = new ArrayList<>();
        for (FluidType fluidType : Fluids.getAll()) {
            if (fluidType != Fluids.NONE && !mappedHbmFluids.contains(fluidType)) {
                unmappedFluids.add(fluidType);
            }
        }

        if (!unmappedFluids.isEmpty()) {
            System.out.println("\n[NTM] WARNING: The following NTM fluids were not mapped to Forge fluids:");
            for (FluidType fluid : unmappedFluids) {
                System.out.println("[NTM]   - " + fluid.getName());
            }
        }

        System.out.println("[NTM] ================================\n");
    }

    /**
     * Add common aliases for a fluid type to improve compatibility with other mods
     */
    private static void addFluidAliases(FluidType fluidType) {
        String name = fluidType.getName();

        // BuildCraft compatibility
        if (fluidType == Fluids.OIL) {
            registerFluidMapping("crude_oil", fluidType);
        } else if (fluidType == Fluids.HEAVYOIL) {
            registerFluidMapping("heavy_oil", fluidType);
        } else if (fluidType == Fluids.LIGHTOIL) {
            registerFluidMapping("light_oil", fluidType);
        } else if (fluidType == Fluids.GAS) {
            registerFluidMapping("natural_gas", fluidType);
        } else if (fluidType == Fluids.PETROIL) {
            registerFluidMapping("fuel", fluidType);
        }

        // IC2 compatibility
        else if (fluidType == Fluids.SULFURIC_ACID) {
            registerFluidMapping("sulfuricacid", fluidType);
        } else if (fluidType == Fluids.UF6) {
            registerFluidMapping("uranium_hexafluoride", fluidType);
        }

        // Forestry compatibility
        else if (fluidType == Fluids.BIOFUEL) {
            registerFluidMapping("ethanol", fluidType);
        }

        // General compatibility
        else if (fluidType == Fluids.HELIUM3) {
            registerFluidMapping("helium", fluidType);
        } else if (fluidType == Fluids.CARBONDIOXIDE) {
            registerFluidMapping("carbon_dioxide", fluidType);
        }

        String lowercaseName = name.toLowerCase(Locale.US);
        if (lowercaseName.contains("_")) {
            registerFluidMapping(lowercaseName.replace("_", ""), fluidType);
        } else if (!lowercaseName.contains("_") && lowercaseName.length() > 3) {
            for (String forgeName : net.minecraftforge.fluids.FluidRegistry.getRegisteredFluids().keySet()) {
                if (forgeName.replace("_", "").equals(lowercaseName)) {
                    registerFluidMapping(forgeName, fluidType);
                    break;
                }
            }
        }
    }

    /**
     * Register a mapping between a Forge fluid name and an HBM FluidType
     */
    public static void registerFluidMapping(String forgeName, FluidType hbmType) {
        // Check if this mapping already exists
        FluidType existingType = forgeToHbmMap.get(forgeName);
        if (existingType != null && existingType != hbmType) {
            System.out.println("[NTM] Refusing to remap '" + forgeName + "': keeping " +
                existingType.getName() + ", rejecting " + hbmType.getName());
            return;
        }

        forgeToHbmMap.put(forgeName, hbmType);
        hbmToForgeMap.put(hbmType, forgeName);

        // Only log if we're not in the initial mapping phase (to avoid console spam)
        if (forgeToHbmMap.size() > 1 && existingType == null) {
            System.out.println("[NTM] Mapped Forge fluid '" + forgeName + "' to NTM fluid '" + hbmType.getName() + "'");
        }
    }

    /**
     * Convert a Forge Fluid to an HBM FluidType
     * Uses a dynamic approach to find the best matching fluid type
     */
    public static FluidType getHbmFluidType(Fluid fluid) {
        if (fluid == null) {
            return Fluids.NONE;
        }

        String fluidName = fluid.getName().toLowerCase(Locale.US);

        FluidType type = forgeToHbmMap.get(fluidName);
        if (type != null) {
            return type;
        }

        for (FluidType hbmFluid : Fluids.getAll()) {
            if (hbmFluid == Fluids.NONE || hbmFluid.hasNoContainer() || hbmFluid.hasNoID()) continue;

            String hbmName = hbmFluid.getName().toLowerCase(Locale.US);

            if (hbmName.equals(fluidName)) {
                registerFluidMapping(fluidName, hbmFluid);
                return hbmFluid;
            }

            String cleanHbm = hbmName.replace("_", "");
            String cleanForge = fluidName.replace("_", "");
            if (cleanHbm.equals(cleanForge)) {
                registerFluidMapping(fluidName, hbmFluid);
                return hbmFluid;
            }
        }

        for (FluidType hbmFluid : Fluids.getAll()) {
            if (hbmFluid == Fluids.NONE || hbmFluid.hasNoContainer() || hbmFluid.hasNoID()) continue;

            String hbmName = hbmFluid.getName().toLowerCase(Locale.US);

            if (hbmName.startsWith(fluidName) || fluidName.startsWith(hbmName)) {
                registerFluidMapping(fluidName, hbmFluid);
                return hbmFluid;
            }

            if (fluidName.startsWith("fluid") && fluidName.substring(5).equals(hbmName)) {
                registerFluidMapping(fluidName, hbmFluid);
                return hbmFluid;
            }

            if ((fluidName.equals("fuel") && hbmName.equals("petroleum")) ||
                (fluidName.equals("ethanol") && hbmName.equals("biofuel")) ||
                (fluidName.equals("crude_oil") && hbmName.equals("oil"))) {
                registerFluidMapping(fluidName, hbmFluid);
                return hbmFluid;
            }
        }

        System.out.println("[NTM] Unknown Forge fluid: '" + fluid.getName() + "'. No matching NTM fluid found. This fluid will not be usable in NTM machines.");
        FluidDebug.eventStack("mapping.unknown|" + fluid.getName(),
            () -> "MAPPING unknown Forge fluid '" + fluid.getName() + "' requested by " + FluidDebug.callerHint());
        return Fluids.NONE;
    }

    /**
     * Get the Forge fluid name for an HBM FluidType
     * This is used by the texture mapper to find the corresponding Forge fluid
     */
    public static String getForgeFluidName(FluidType type) {
        if (type == null || type == Fluids.NONE) {
            return null;
        }

        return hbmToForgeMap.get(type);
    }

    /**
     * Convert an HBM FluidType to a Forge Fluid
     * Uses a dynamic approach to find the best matching Forge fluid
     */
    public static Fluid getForgeFluid(FluidType type) {
        if (type == null || type == Fluids.NONE) {
            return null;
        }

        String fluidName = hbmToForgeMap.get(type);
        if (fluidName != null) {
            Fluid fluid = net.minecraftforge.fluids.FluidRegistry.getFluid(fluidName);
            if (fluid != null) {
                return fluid;
            }
        }

        String hbmName = type.getName().toLowerCase(Locale.US);

        Fluid fluid = net.minecraftforge.fluids.FluidRegistry.getFluid(hbmName);
        if (fluid != null) {
            registerFluidMapping(hbmName, type);
            return fluid;
        }

        for (String forgeName : net.minecraftforge.fluids.FluidRegistry.getRegisteredFluids().keySet()) {
            String cleanHbm = hbmName.replace("_", "");
            String cleanForge = forgeName.replace("_", "");
            if (cleanHbm.equals(cleanForge)) {
                registerFluidMapping(forgeName, type);
                return net.minecraftforge.fluids.FluidRegistry.getFluid(forgeName);
            }
        }

        for (String forgeName : net.minecraftforge.fluids.FluidRegistry.getRegisteredFluids().keySet()) {
            // Try substring matching
            if (forgeName.startsWith(hbmName) || hbmName.startsWith(forgeName)) {
                registerFluidMapping(forgeName, type);
                return net.minecraftforge.fluids.FluidRegistry.getFluid(forgeName);
            }

            if (forgeName.startsWith("fluid") && forgeName.substring(5).equals(hbmName)) {
                registerFluidMapping(forgeName, type);
                return net.minecraftforge.fluids.FluidRegistry.getFluid(forgeName);
            }
        }

        if (type == Fluids.PETROIL) {
            Fluid fuelFluid = net.minecraftforge.fluids.FluidRegistry.getFluid("fuel");
            if (fuelFluid != null) {
                registerFluidMapping("fuel", type);
                return fuelFluid;
            }
        } else if (type == Fluids.BIOFUEL) {
            Fluid ethanolFluid = net.minecraftforge.fluids.FluidRegistry.getFluid("ethanol");
            if (ethanolFluid != null) {
                registerFluidMapping("ethanol", type);
                return ethanolFluid;
            }
        } else if (type == Fluids.OIL) {
            Fluid crudeOilFluid = net.minecraftforge.fluids.FluidRegistry.getFluid("crude_oil");
            if (crudeOilFluid != null) {
                registerFluidMapping("crude_oil", type);
                return crudeOilFluid;
            }
        }

        if (net.minecraftforge.fluids.FluidRegistry.getFluid(hbmName) == null) {
            int color = type.getColor();
            Fluid newFluid = new ColoredForgeFluid(hbmName, color);
            net.minecraftforge.fluids.FluidRegistry.registerFluid(newFluid);
            registerFluidMapping(hbmName, type);
            System.out.println("[NTM] Created new Forge fluid '" + hbmName + "' for NTM fluid '" + type.getName() + "' with color 0x" + Integer.toHexString(color));
            FluidDebug.eventStack("mapping.created|" + hbmName,
                () -> "MAPPING lazily created Forge fluid '" + hbmName + "' for NTM " + FluidDebug.describe(type) + " requested by " + FluidDebug.callerHint());
            return newFluid;
        }

        return null;
    }
}
