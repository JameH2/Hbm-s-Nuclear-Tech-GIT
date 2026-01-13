package com.hbm.dim;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map.Entry;

import com.google.gson.JsonElement;
import com.google.gson.stream.JsonWriter;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.recipes.loader.SerializableRecipe;
import com.hbm.items.machine.ItemFluidIcon;
import com.hbm.util.Tuple.Triplet;

import net.minecraft.item.ItemStack;

public class AtmosphereRecipes extends SerializableRecipe {

	private static HashMap<FluidType, Triplet<AtmoStack, AtmoStack, AtmoStack>> recipes = new HashMap<>();

	@Override
	public void registerDefaults() {
		recipes.put(Fluids.LITHCARBONATE, new Triplet<>(
			new AtmoStack(Fluids.NONE, 0),
			new AtmoStack(Fluids.LITHYDRO, 128000),
			new AtmoStack(Fluids.DUNAAIR, 320000)
		));
	}



	public static Triplet<AtmoStack, AtmoStack, AtmoStack> getOutput(FluidType type) {
		return recipes.get(type);
	}

	public static HashMap<FluidType, Triplet<AtmoStack, AtmoStack, AtmoStack>> getRecipesMap() {
		return recipes;
	}

	public static HashMap<Object, Object[]> getRecipes() {

		HashMap<Object, Object[]> map = new HashMap<Object, Object[]>();

		for(Entry<FluidType, Triplet<AtmoStack, AtmoStack, AtmoStack>> recipe : recipes.entrySet()) {
			ItemStack[] inputs = recipe.getValue().getX().type == Fluids.NONE
				? new ItemStack[] { ItemFluidIcon.make(recipe.getKey(), 1000) }
				: new ItemStack[] {
					ItemFluidIcon.make(recipe.getKey(), 1000),
					ItemFluidIcon.make(recipe.getValue().getX().type,	recipe.getValue().getX().pressure * 10) }; // this nesting level is bird-behaviour

			map.put(inputs,
				new ItemStack[] {
					ItemFluidIcon.make(recipe.getValue().getY().type,	recipe.getValue().getY().pressure * 10),
					ItemFluidIcon.make(recipe.getValue().getZ().type,	recipe.getValue().getZ().pressure * 10) });
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
	public void readRecipe(JsonElement recipe) {

	}

	@SuppressWarnings("unchecked")
	@Override
	public void writeRecipe(Object recipe, JsonWriter writer) throws IOException {

	}

	@Override
	public void deleteRecipes() {
		recipes.clear();
	}
}


