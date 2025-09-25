package com.hbm.dim;

import net.minecraft.world.biome.BiomeGenBase;

public class BiomeCollisionException extends RuntimeException {

	private static String EXCEPTION_MESSAGE
		= "Biome ID conflict!"
		+ "\n\n!!!!!  ALERT ALERT - READ ME - I AM THE REASON YOUR GAME IS CRASHING  !!!!!"
		+ "\n\n!!!!!             FOLLOW THE INSTRUCTIONS BELOW TO RESOLVE            !!!!!"
		+ "\n\nAttempted to register NTM Space biome to an ID which is already in use by:"
		+ "\nBiome ID: %d"
		+ "\nBiome name: %s"
		+ "\nBiome class: %s"
		+ "\nPlease modify hbm.cfg to fix this error. Note that the maximum biome ID is 255, if you run out you MUST install EndlessIDs!";

	public BiomeCollisionException(BiomeGenBase conflictsWith) {
		super(String.format(EXCEPTION_MESSAGE, conflictsWith.biomeID, conflictsWith.biomeName, conflictsWith.getBiomeClass().getName()));
	}

	public BiomeCollisionException(String message) {
		super(message);
	}

}
