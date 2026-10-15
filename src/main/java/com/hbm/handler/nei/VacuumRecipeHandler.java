package com.hbm.handler.nei;

import com.hbm.blocks.ModBlocks;
import com.hbm.inventory.recipes.VacuumRefineryRecipes;

public class VacuumRecipeHandler extends NEIUniversalHandler {

	public VacuumRecipeHandler() {
		super("nei.vacuum.name", ModBlocks.machine_vacuum_distill, VacuumRefineryRecipes.getVacuumRecipe());
	}

	@Override
	public String getKey() {
		return "ntmVacuum";
	}
}
