package com.hbm.config;

import java.util.HashMap;

import com.hbm.dim.BiomeCollisionException;
import com.hbm.util.Compat;

import cpw.mods.fml.common.Loader;
import net.minecraftforge.common.config.Configuration;

public class SpaceConfig {

	// Dimension ID limit is over 4 million, so go crazy
	public static int moonDimension = 413_015;
	public static int dunaDimension = 413_016;
	public static int ikeDimension = 413_017;
	public static int eveDimension = 413_018;
	public static int dresDimension = 413_019;
	public static int mohoDimension = 413_020;
	public static int minmusDimension = 413_021;
	public static int laytheDimension = 413_022;
	public static int orbitDimension = 413_023;
	public static int tektoDimension = 413_024;
	public static int thatmoDimension = 413_025;

	// Biome ID limit is 255

	// NOTE: some popular mod biome IDs, try to avoid colliding with these

	// Vanilla biomes: 0-39
	// Vanilla mutated variants: 128-167
	// Biomes O' Plenty: 41-124 (oof)
	// Aether Legacy: 127
	// Galacticraft: 102+ (variable, ugh, just assume up to 110 I guess)
	// Advanced Rocketry: "maxBiomes = 512" - ...that isn't even valid but okay
	// Witchery: needs investigating
	// Thaumcraft: needs investigating
	// The Twilight Forest: 40-58
	// ExtraBiomesXL: needs investigating
	// NTM upstream: 80-82 for craters

	// Most mods start at 40 and go up, so easiest way to avoid conflicts is to count backwards!

	public static int orbitBiome = 126;

	public static int moonBiome = 125;
	public static int minmusBiome = 124;
	public static int minmusBasinBiome = 123;

	public static int dunaBiome = 122;
	public static int dunaLowlandsBiome = 121;
	public static int dunaPolarBiome = 120;
	public static int dunaHillsBiome = 119;
	public static int dunaPolarHillsBiome = 118;

	public static int eveBiome = 117;
	public static int eveMountainsBiome = 116;
	public static int eveOceanBiome = 115;
	public static int eveSeismicBiome = 114;
	public static int eveRiverBiome = 113;

	public static int dresBiome = 112;
	public static int dresBasinBiome = 111;

	public static int mohoBiome = 101;
	public static int mohoBasaltBiome = 100;
	public static int mohoLavaBiome = 99;
	public static int mohoPlateauBiome = 98;

	public static int laytheBiome = 97;
	public static int laytheOceanBiome = 96;
	public static int laythePolarBiome = 95;
	public static int laytheCoastBiome = 94;

	public static int ikeBiome = 93;

	public static int tektoPolyvinylBiome = 92;
	public static int tektoHalogenHillBiome = 91;
	public static int tektoRiverBiome = 90;
	public static int tektoForestBiome = 89;
	public static int tektoVinylIslandBiome = 88;

	public static int thatmoBiome = 87;



	public static boolean allowNetherPortals = false;

	public static boolean enableVolcanoGen = true;

	public static boolean crashOnBiomeConflict = true;

	public static boolean showOreLocations = true;

	public static int maxProbeDistance = 32_000;
	public static int maxStationDistance = 32_000;

	public static void loadFromConfig(Configuration config) {

		final String CATEGORY_DIM = CommonConfig.CATEGORY_DIMS;
		allowNetherPortals = CommonConfig.createConfigBool(config, CATEGORY_DIM, "17.00_allowNetherPortals", "Should Nether portals function on other celestial bodies?", false);

		moonDimension = CommonConfig.createConfigInt(config, CATEGORY_DIM, "17.01_moonDimension", "Mun dimension ID", moonDimension);
		dunaDimension = CommonConfig.createConfigInt(config, CATEGORY_DIM, "17.02_dunaDimension", "Duna dimension ID", dunaDimension);
		ikeDimension = CommonConfig.createConfigInt(config, CATEGORY_DIM, "17.03_ikeDimension", "Ike dimension ID", ikeDimension);
		eveDimension = CommonConfig.createConfigInt(config, CATEGORY_DIM, "17.04_eveDimension", "Eve dimension ID", eveDimension);
		dresDimension = CommonConfig.createConfigInt(config, CATEGORY_DIM, "17.05_dresDimension", "Dres dimension ID", dresDimension);
		mohoDimension = CommonConfig.createConfigInt(config, CATEGORY_DIM, "17.06_mohoDimension", "Moho dimension ID", mohoDimension);
		minmusDimension = CommonConfig.createConfigInt(config, CATEGORY_DIM, "17.07_minmusDimension", "Minmus dimension ID", minmusDimension);
		laytheDimension = CommonConfig.createConfigInt(config, CATEGORY_DIM, "17.08_laytheDimension", "Laythe dimension ID", laytheDimension);
		orbitDimension = CommonConfig.createConfigInt(config, CATEGORY_DIM, "17.09_orbitDimension", "Orbital dimension ID", orbitDimension);
		tektoDimension = CommonConfig.createConfigInt(config, CATEGORY_DIM, "17.10_tektoDimension", "Tekto dimension ID", tektoDimension);
		thatmoDimension = CommonConfig.createConfigInt(config, CATEGORY_DIM, "17.10_thatmoDimension", "Thatmo dimension ID", thatmoDimension);

		final String CATEGORY_GENERAL = CommonConfig.CATEGORY_GENERAL;
		maxProbeDistance = CommonConfig.createConfigInt(config, CATEGORY_GENERAL, "1.90_maxProbeDistance", "How far from the center of the dimension can probes generate landing coordinates", maxProbeDistance);
		maxStationDistance = CommonConfig.createConfigInt(config, CATEGORY_GENERAL, "1.93_maxStationDistance", "How far from the center of the dimension can orbital stations be generated", maxStationDistance);
		enableVolcanoGen = CommonConfig.createConfigBool(config, CATEGORY_GENERAL, "1.91_enableVolcanoGen", "Should volcanoes be active when spawning, disabling will prevent natural volcanoes from spewing lava and growing", enableVolcanoGen);
		crashOnBiomeConflict = CommonConfig.createConfigBool(config, CATEGORY_GENERAL, "1.92_crashOnBiomeConflict", "To avoid biome ID collisions, the game will crash if one occurs, and give instructions on how to fix. Only disable this if you know what you're doing!", crashOnBiomeConflict);
		showOreLocations = CommonConfig.createConfigBool(config, CATEGORY_GENERAL, "1.93_showOreLocations", "Should ores indicate which planets they can be found on.", showOreLocations);

		// Move defaults into unused ranges if EndlessIDs is installed
		int defaultBiomeOffset = Loader.isModLoaded(Compat.MOD_EIDS) ? 12_000 : 0;

		final String CATEGORY_BIOME = CommonConfig.CATEGORY_BIOMES;
		moonBiome = createConfigBiome(config, CATEGORY_BIOME, "16.02_moonBiome", "Mun Biome ID", moonBiome + defaultBiomeOffset);
		dunaBiome = createConfigBiome(config, CATEGORY_BIOME, "16.03_dunaBiome", "Duna Biome ID", dunaBiome + defaultBiomeOffset);
		dunaLowlandsBiome = createConfigBiome(config, CATEGORY_BIOME, "16.04_dunaLowlandsBiome", "Duna Lowlands Biome ID", dunaLowlandsBiome + defaultBiomeOffset);
		dunaPolarBiome = createConfigBiome(config, CATEGORY_BIOME, "16.05_dunaPolarBiome", "Duna Polar Biome ID", dunaPolarBiome + defaultBiomeOffset);
		dunaHillsBiome = createConfigBiome(config, CATEGORY_BIOME, "16.06_dunaHillsBiome", "Duna Hills Biome ID", dunaHillsBiome + defaultBiomeOffset);
		dunaPolarHillsBiome = createConfigBiome(config, CATEGORY_BIOME, "16.07_dunaPolarHillsBiome", "Duna Polar Hills Biome ID", dunaPolarHillsBiome + defaultBiomeOffset);
		eveBiome = createConfigBiome(config, CATEGORY_BIOME, "16.08_eveBiome", "Eve Biome ID", eveBiome + defaultBiomeOffset);
		eveMountainsBiome = createConfigBiome(config, CATEGORY_BIOME, "16.09_eveMountainsBiome", "Eve Mountains Biome ID", eveMountainsBiome + defaultBiomeOffset);
		eveOceanBiome = createConfigBiome(config, CATEGORY_BIOME, "16.10_eveOceanBiome", "Eve Ocean Biome ID", eveOceanBiome + defaultBiomeOffset);
		eveSeismicBiome = createConfigBiome(config, CATEGORY_BIOME, "16.12_eveSeismicBiome", "Eve Seismic Biome ID", eveSeismicBiome + defaultBiomeOffset);
		eveRiverBiome = createConfigBiome(config, CATEGORY_BIOME, "16.24_eveRiverBiome", "Eve River Biome ID", eveRiverBiome + defaultBiomeOffset);
		ikeBiome = createConfigBiome(config, CATEGORY_BIOME, "16.13_ikeBiome", "Ike Biome ID", ikeBiome + defaultBiomeOffset);
		laytheBiome = createConfigBiome(config, CATEGORY_BIOME, "16.14_laytheBiome", "Laythe Biome ID", laytheBiome + defaultBiomeOffset);
		laytheOceanBiome = createConfigBiome(config, CATEGORY_BIOME, "16.15_laytheOceanBiome", "Laythe Ocean Biome ID", laytheOceanBiome + defaultBiomeOffset);
		laythePolarBiome = createConfigBiome(config, CATEGORY_BIOME, "16.16_laythePolarBiome", "Laythe Polar Biome ID", laythePolarBiome + defaultBiomeOffset);
		minmusBasinBiome = createConfigBiome(config, CATEGORY_BIOME, "16.17_minmusBasinsBiome", "Minmus Basins Biome ID", minmusBasinBiome + defaultBiomeOffset);
		minmusBiome = createConfigBiome(config, CATEGORY_BIOME, "16.18_minmusBiome", "Minmus Biome ID", minmusBiome + defaultBiomeOffset);
		mohoBiome = createConfigBiome(config, CATEGORY_BIOME, "16.19_mohoBiome", "Moho Biome ID", mohoBiome + defaultBiomeOffset);
		dresBiome = createConfigBiome(config, CATEGORY_BIOME, "16.20_dresBiome", "Dres Biome ID", dresBiome + defaultBiomeOffset);
		dresBasinBiome = createConfigBiome(config, CATEGORY_BIOME, "16.21_dresBasinsBiome", "Dres Basins Biome ID", dresBasinBiome + defaultBiomeOffset);
		mohoBasaltBiome = createConfigBiome(config, CATEGORY_BIOME, "16.22_mohoBasaltBiome", "Moho Basalt Biome ID", mohoBasaltBiome + defaultBiomeOffset);
		orbitBiome = createConfigBiome(config, CATEGORY_BIOME, "16.23_orbitBiome", "Space Biome ID", orbitBiome + defaultBiomeOffset);
		laytheCoastBiome = createConfigBiome(config, CATEGORY_BIOME, "16.24_laytheCoastBiome", "Laythe Coast Biome ID", laytheCoastBiome + defaultBiomeOffset);
		mohoLavaBiome = createConfigBiome(config, CATEGORY_BIOME, "16.25_mohoLavaBiome", "Moho Lava Biome ID", mohoLavaBiome + defaultBiomeOffset);
		mohoPlateauBiome = createConfigBiome(config, CATEGORY_BIOME, "16.26_mohoPlateauBiome", "Moho Plateau Biome ID", mohoPlateauBiome + defaultBiomeOffset);
		tektoPolyvinylBiome = createConfigBiome(config, CATEGORY_BIOME, "16.27_tektoPolyvinylBiome", "Tekto Polyvinyl Biome ID", tektoPolyvinylBiome + defaultBiomeOffset);
		tektoHalogenHillBiome = createConfigBiome(config, CATEGORY_BIOME, "16.28_tektoHalogenHillBiome", "Tekto Halogen Hills Biome ID", tektoHalogenHillBiome + defaultBiomeOffset);
		tektoRiverBiome = createConfigBiome(config, CATEGORY_BIOME, "16.29_tektoRiverBiome", "Tekto River Biome ID", tektoRiverBiome + defaultBiomeOffset);
		tektoForestBiome = createConfigBiome(config, CATEGORY_BIOME, "16.30_tektoForestBiome", "Tekto Forest Biome ID", tektoForestBiome + defaultBiomeOffset);
		tektoVinylIslandBiome = createConfigBiome(config, CATEGORY_BIOME, "16.31_tektoVinylSandsBiome", "Tekto Vinyl Sands Biome ID", tektoVinylIslandBiome + defaultBiomeOffset);
		thatmoBiome = createConfigBiome(config, CATEGORY_BIOME, "16.32_thatmoBiome", "Thatmo Biome ID", thatmoBiome + defaultBiomeOffset);
	}

	private static HashMap<Integer, String> registeredIds = new HashMap<>();

	private static int createConfigBiome(Configuration config, String category, String name, String comment, int def) {
		int value = CommonConfig.createConfigInt(config, category, name, comment, def);

		if(registeredIds.containsKey(value)) {
			throw new BiomeCollisionException(String.format("Biome ID conflict, attempted to register NTM Space biome on top of another:"
				+ "\nBiome config 1: %s"
				+ "\nBiome config 2: %s"
				+ "\nPlease modify hbm.cfg to fix this error. Note that the maximum biome ID is 255, if you run out you MUST install EndlessIDs!", name, registeredIds.get(value)));
		}

		registeredIds.put(value, name);

		return value;
	}

}
