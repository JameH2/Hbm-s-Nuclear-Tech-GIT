package com.hbm.handler.nei;

import com.hbm.blocks.ModBlocks;
import com.hbm.inventory.recipes.CryoRecipes;

public class CryoHandler extends NEIUniversalHandler {

	public CryoHandler() {
		super("nei.cryo.name", ModBlocks.machine_cryo_distill, CryoRecipes.getCryoRecipes());
	}

	@Override
	public String getKey() {
		return "ntmCryodistill";
	}

}

