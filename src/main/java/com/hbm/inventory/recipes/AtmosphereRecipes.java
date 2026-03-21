package com.hbm.inventory.recipes;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;

import com.google.gson.JsonElement;
import com.google.gson.stream.JsonWriter;
import com.hbm.inventory.FluidStack;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.recipes.loader.SerializableRecipe;
import com.hbm.items.machine.ItemFluidIcon;

public class AtmosphereRecipes extends SerializableRecipe {

	private static HashMap<FluidStack, AtmosphereRecipe> recipes = new LinkedHashMap<>();

	// processing actually does about 64x this, to speed up the reaction
	@Override
	public void registerDefaults() {
		recipes.put(new FluidStack(Fluids.LITHCARBONATE, 32_000), new AtmosphereRecipe(
			new FluidStack(Fluids.LITHYDRO, 16_000),
			new FluidStack(Fluids.DUNAAIR, 256_000)
		));
	}



	public static AtmosphereRecipe getOutput(FluidStack type) {
		return recipes.get(type);
	}

	public static HashMap<FluidStack, AtmosphereRecipe> getRecipesMap() {
		return recipes;
	}

	public static HashMap<Object[], Object> getRecipes() {

		HashMap<Object[], Object> map = new HashMap<Object[], Object>();

		for(Entry<FluidStack, AtmosphereRecipe> recipe : recipes.entrySet()) {
			
			FluidStack output = recipe.getKey();

			List<Object> objects = new ArrayList<>();
			for(FluidStack fluidStack : recipe.getValue().inputFluids) {
				objects.add(ItemFluidIcon.make(fluidStack));
			}

			map.put(objects.toArray(), ItemFluidIcon.make(output));
		}

		return map;
	}

	@Override
	public String getFileName() {
		return "hbmAtmosphere.json";
	}

	@Override
	public Object getRecipeObject() {
		return recipes;
	}

	@Override
	public void readRecipe(JsonElement recipe) { }

	@Override
	public void writeRecipe(Object recipe, JsonWriter writer) throws IOException { }

	@Override
	public void deleteRecipes() {
		recipes.clear();
	}

	public static class AtmosphereRecipe {
		
		public FluidStack[] inputFluids;
		
		public AtmosphereRecipe(FluidStack inputA, FluidStack inputB) {
			inputFluids = new FluidStack[] {
				inputA,
				inputB,
			};
		}
	}
}


