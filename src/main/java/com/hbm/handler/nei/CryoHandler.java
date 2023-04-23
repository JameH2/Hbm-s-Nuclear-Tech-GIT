package com.hbm.handler.nei;

import com.hbm.blocks.ModBlocks;
import com.hbm.inventory.recipes.CryoRecipes;
import com.hbm.inventory.recipes.ReformingRecipes;

public class CryoHandler extends NEIUniversalHandler {

	public CryoHandler() {
		super("Cryogenic Distillation", ModBlocks.machine_cryo_distill, CryoRecipes.getRecipes());
	}

	@Override
	public String getKey() {
		return "ntmCryodistill";
	}
}
