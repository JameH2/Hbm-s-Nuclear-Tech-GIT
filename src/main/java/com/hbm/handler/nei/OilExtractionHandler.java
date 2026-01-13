package com.hbm.handler.nei;

import static codechicken.lib.gui.GuiDraw.drawString;

import java.util.ArrayList;
import java.util.HashMap;

import com.hbm.blocks.ModBlocks;
import com.hbm.dim.CelestialBody;
import com.hbm.dim.SolarSystem;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.items.machine.ItemFluidIcon;

import net.minecraft.item.ItemStack;

public class OilExtractionHandler extends NEICelestialHandler {

	public OilExtractionHandler() {
		super("Oil Extraction", new ItemStack[] { new ItemStack(ModBlocks.machine_well), new ItemStack(ModBlocks.machine_pumpjack), new ItemStack(ModBlocks.machine_fracking_tower) }, getRecipes());
	}

	@Override
	public String getKey() {
		return "ntmOilExtraction";
	}

	// private HashSet<Integer> hasBedrock = new HashSet<>();

	public static HashMap<CelestialBody, ItemStack[]> getRecipes() {
		HashMap<CelestialBody, ItemStack[]> map = new HashMap<>();

		for(SolarSystem.Body bodyEnum : SolarSystem.Body.values()) {
			CelestialBody body = bodyEnum.getBody();
			if(body == null) continue;

			ArrayList<ItemStack> outputs = new ArrayList<>();

			// TODO: not be hardcoded
			switch(bodyEnum) {
				case KERBIN:
				case DUNA:
					outputs.add(ItemFluidIcon.make(Fluids.OIL, 1000));
					outputs.add(ItemFluidIcon.make(Fluids.GAS, 200));
					break;
				case EVE:
					outputs.add(ItemFluidIcon.make(Fluids.GAS, 1000));
					outputs.add(ItemFluidIcon.make(Fluids.PETROLEUM, 200));
					break;
				case LAYTHE:
					outputs.add(ItemFluidIcon.make(Fluids.OIL_DS, 1000));
					outputs.add(ItemFluidIcon.make(Fluids.GAS, 200));
					break;
				case TEKTO:
					outputs.add(ItemFluidIcon.make(Fluids.TCRUDE, 1000));
					outputs.add(ItemFluidIcon.make(Fluids.HGAS, 200));
					break;
				case MUN:
				case MINMUS:
				case IKE:
					outputs.add(ItemFluidIcon.make(Fluids.BRINE, 1000));
					break;
				default: break;
			}

			if(outputs.size() == 0) continue;

			map.put(body, outputs.toArray(new ItemStack[outputs.size()]));
		}

		return map;
	}

	@Override
	public void drawBackground(int recipe) {
		super.drawBackground(recipe);

		CelestialBody body = recipeInputCache.get(recipe);

		// TODO: also not be hardcoded
		if(body.name == "kerbin" || body.name == "tekto") {
			drawString("Frackable", 102, 48, 0x000000, false);
		}
	}

}
