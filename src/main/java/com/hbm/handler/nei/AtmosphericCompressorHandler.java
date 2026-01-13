package com.hbm.handler.nei;

import java.util.HashMap;

import com.hbm.blocks.ModBlocks;
import com.hbm.dim.CelestialBody;
import com.hbm.dim.SolarSystem;
import com.hbm.dim.trait.CBT_Atmosphere;
import com.hbm.dim.trait.CBT_Atmosphere.FluidEntry;
import com.hbm.items.machine.ItemFluidIcon;

import net.minecraft.item.ItemStack;

public class AtmosphericCompressorHandler extends NEICelestialHandler {

	public AtmosphericCompressorHandler() {
		super("Atmosphere Extraction", ModBlocks.machine_atmo_vent, getRecipes());
	}

	@Override
	public String getKey() {
		return "ntmAtmoCompressor";
	}

	public static HashMap<CelestialBody, ItemStack[]> getRecipes() {
		HashMap<CelestialBody, ItemStack[]> map = new HashMap<>();

		for(SolarSystem.Body bodyEnum : SolarSystem.Body.values()) {
			CelestialBody body = bodyEnum.getBody();
			if(body == null) continue;

			CBT_Atmosphere atmosphere = body.getDefaultTrait(CBT_Atmosphere.class);
			if(atmosphere == null) continue;

			ItemStack[] outputs = new ItemStack[atmosphere.fluids.size()];
			for(int i = 0; i < outputs.length; i++) {
				FluidEntry entry = atmosphere.fluids.get(i);
				outputs[i] = ItemFluidIcon.make(entry.fluid, entry.pressure);
			}

			map.put(body, outputs);
		}

		return map;
	}

}
