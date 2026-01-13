package com.hbm.handler.nei;

import java.util.HashMap;

import com.hbm.blocks.ModBlocks;
import com.hbm.dim.CelestialBody;
import com.hbm.dim.SolarSystem;
import com.hbm.dim.trait.CBT_Water;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.items.machine.ItemFluidIcon;

import net.minecraft.item.ItemStack;

public class WaterTableHandler extends NEICelestialHandler {

	public WaterTableHandler() {
		super("Fluid Table", new ItemStack[] { new ItemStack(ModBlocks.pump_electric), new ItemStack(ModBlocks.pump_steam) }, getRecipes());
	}

	@Override
	public String getKey() {
		return "ntmFluidTable";
	}

	public static HashMap<CelestialBody, ItemStack> getRecipes() {
		HashMap<CelestialBody, ItemStack> map = new HashMap<>();

		for(SolarSystem.Body bodyEnum : SolarSystem.Body.values()) {
			CelestialBody body = bodyEnum.getBody();
			if(body == null) continue;

			CBT_Water table = body.getDefaultTrait(CBT_Water.class);
			if(table == null) continue;

			map.put(body, ItemFluidIcon.make(table.fluid, table.fluid == Fluids.WATER ? 10_000 : 1_000));
		}

		return map;
	}

}
