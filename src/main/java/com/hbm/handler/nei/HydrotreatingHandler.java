package com.hbm.handler.nei;

import com.hbm.blocks.ModBlocks;
import com.hbm.inventory.recipes.HydrotreatingRecipes;

public class HydrotreatingHandler extends NEIUniversalHandler {

	public HydrotreatingHandler() {
		super("nei.hydrotreating.name", ModBlocks.machine_hydrotreater, HydrotreatingRecipes.getRecipes());
	}

	@Override
	public String getKey() {
		return "ntmHydrotreating";
	}
}
