package com.hbm.dim.dima;

import com.hbm.dim.SkyProviderCelestial;
import com.hbm.lib.RefStrings;

import net.minecraft.util.ResourceLocation;

public class SkyProviderDima extends SkyProviderCelestial {

	public SkyProviderDima() {
		super();
		nightTexture = new ResourceLocation(RefStrings.MODID, "textures/misc/space/night_2.png");
	}

}
