package com.hbm.handler.nei;

import java.util.ArrayList;
import java.util.HashMap;

import com.hbm.blocks.ModBlocks;
import com.hbm.dim.CelestialBody;
import com.hbm.dim.SolarSystem;
import com.hbm.items.ModItems;
import com.hbm.items.special.ItemBedrockOreNew.CelestialBedrockOre;

import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;

public class BedrockDrillHandler extends NEICelestialHandler {

	public BedrockDrillHandler() {
		super("Bedrock Drilling", ModBlocks.machine_excavator, getRecipes());
	}

	@Override
	public String getKey() {
		return "ntmBedrockCelestial";
	}

	public static HashMap<CelestialBody, ItemStack[]> getRecipes() {
		HashMap<CelestialBody, ItemStack[]> map = new HashMap<>();

		for(SolarSystem.Body bodyEnum : SolarSystem.Body.values()) {
			CelestialBody body = bodyEnum.getBody();
			if(body == null) continue;

			ArrayList<ItemStack> outputs = new ArrayList<>();

			CelestialBedrockOre ore = CelestialBedrockOre.get(bodyEnum);
			if(ore != null) {
				outputs.add(new ItemStack(ModItems.bedrock_ore_base, 1, bodyEnum.ordinal()));
			}

			if(body.hasIce) {
				outputs.add(new ItemStack(Blocks.packed_ice, 32));
			}

			map.put(body, outputs.toArray(new ItemStack[outputs.size()]));
		}

		return map;
	}

}
