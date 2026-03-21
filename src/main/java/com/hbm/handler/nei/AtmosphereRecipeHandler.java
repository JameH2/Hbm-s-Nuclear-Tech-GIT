package com.hbm.handler.nei;

import com.hbm.blocks.ModBlocks;
import com.hbm.inventory.recipes.AtmosphereRecipes;

public class AtmosphereRecipeHandler extends NEIUniversalHandler {

	public AtmosphereRecipeHandler() {
		super("Atmospheric Chemistry", ModBlocks.machine_atmo_emitter, AtmosphereRecipes.getRecipes());
	}

	@Override
	public String getKey() {
		return "ntmAtmoChem";
	}
	
}
