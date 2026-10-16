package com.hbm.inventory.recipes;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import static com.hbm.inventory.OreDictManager.*;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.stream.JsonWriter;
import com.hbm.blocks.ModBlocks;
import com.hbm.config.GeneralConfig;
import com.hbm.inventory.FluidStack;
import com.hbm.inventory.RecipesCommon.AStack;
import com.hbm.inventory.RecipesCommon.ComparableStack;
import com.hbm.inventory.RecipesCommon.OreDictStack;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.recipes.loader.SerializableRecipe;
import com.hbm.items.ModItems;
import com.hbm.items.machine.ItemFluidIcon;
import com.hbm.items.machine.ItemCircuit.EnumCircuitType;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.registry.GameRegistry;

import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraftforge.oredict.OreDictionary;

public class SolderingRecipes extends SerializableRecipe {

	public static List<SolderingRecipe> recipes = new ArrayList();

	@Override
	public void registerDefaults() {

		boolean lbsm = GeneralConfig.enableLBSM && GeneralConfig.enableLBSMSimpleCrafting;
		boolean no528 = !GeneralConfig.enable528;

		/*
		 * CIRCUITS
		 */

		recipes.add(new SolderingRecipe(new ItemStack(ModItems.circuit, 1, EnumCircuitType.ANALOG.ordinal()), 100, 100,
				new AStack[] {
						new ComparableStack(ModItems.circuit, 3, EnumCircuitType.VACUUM_TUBE),
						new ComparableStack(ModItems.circuit, 2, EnumCircuitType.CAPACITOR)},
				new AStack[] {
						new ComparableStack(ModItems.circuit, 4, EnumCircuitType.PCB)},
				new AStack[] {
						new OreDictStack(PB.wireFine(), 4)}
		));

		recipes.add(new SolderingRecipe(new ItemStack(ModItems.circuit, 1, EnumCircuitType.BASIC.ordinal()), 200, 250,
				new AStack[] {
						new ComparableStack(ModItems.circuit, 4, EnumCircuitType.CHIP)},
				new AStack[] {
						new ComparableStack(ModItems.circuit, 4, EnumCircuitType.PCB)},
				new AStack[] {
						new OreDictStack(PB.wireFine(), 4)}
		));

		recipes.add(new SolderingRecipe(new ItemStack(ModItems.circuit, 1, EnumCircuitType.ADVANCED.ordinal()), 300, 1_000,
				new FluidStack(Fluids.SULFURIC_ACID, 1_000),
				new AStack[] {
						new ComparableStack(ModItems.circuit, lbsm ? 4 : 16, EnumCircuitType.CHIP),
						new ComparableStack(ModItems.circuit, 4, EnumCircuitType.CAPACITOR)},
				new AStack[] {
						new ComparableStack(ModItems.circuit, 8, EnumCircuitType.PCB),
						new OreDictStack(RUBBER.ingot(), 2)},
				new AStack[] {
						new OreDictStack(PB.wireFine(), 8)}
		));

		recipes.add(new SolderingRecipe(new ItemStack(ModItems.circuit, 1, EnumCircuitType.CAPACITOR_BOARD.ordinal()), 200, 300,
				new FluidStack(Fluids.PEROXIDE, 250),
				new AStack[] {
						new ComparableStack(ModItems.circuit, 3, EnumCircuitType.CAPACITOR_TANTALIUM)},
				new AStack[] {
						new ComparableStack(ModItems.circuit, 1, EnumCircuitType.PCB)},
				new AStack[] {
						new OreDictStack(PB.wireFine(), 3)}
		));

		recipes.add(new SolderingRecipe(new ItemStack(ModItems.circuit, 1, EnumCircuitType.BISMOID.ordinal()), 400, 10_000,
				new FluidStack(Fluids.SOLVENT, 1_000),
				new AStack[] {
						new ComparableStack(ModItems.circuit, 4, EnumCircuitType.CHIP_BISMOID),
						new ComparableStack(ModItems.circuit, lbsm ? 4 : 16, EnumCircuitType.CHIP),
						new ComparableStack(ModItems.circuit, lbsm ? 8 : 24, EnumCircuitType.CAPACITOR)},
				new AStack[] {
						new ComparableStack(ModItems.circuit, 12, EnumCircuitType.PCB),
						new OreDictStack(ANY_HARDPLASTIC.ingot(), 2)},
				new AStack[] {
						new OreDictStack(PB.wireFine(), 12)}
		));

		recipes.add(new SolderingRecipe(new ItemStack(ModItems.circuit, 1, EnumCircuitType.QUANTUM.ordinal()), 400, 100_000,
				new FluidStack(Fluids.HELIUM4, 1_000),
				new AStack[] {
						new ComparableStack(ModItems.circuit, 4, EnumCircuitType.CHIP_QUANTUM),
						new ComparableStack(ModItems.circuit, lbsm ? 4 : 16, EnumCircuitType.CHIP_BISMOID),
						new ComparableStack(ModItems.circuit, lbsm ? 1 : 4, EnumCircuitType.ATOMIC_CLOCK)},
				new AStack[] {
						new ComparableStack(ModItems.circuit, 16, EnumCircuitType.PCB),
						new OreDictStack(ANY_HARDPLASTIC.ingot(), 4)},
				new AStack[] {
						new OreDictStack(PB.wireFine(), 16)}
		));

		/*
		 * COMPUTERS
		 */

		if(no528) {
			recipes.add(new SolderingRecipe(new ItemStack(ModItems.circuit, 1, EnumCircuitType.CONTROLLER.ordinal()), 400, 15_000,
					new FluidStack(Fluids.PERFLUOROMETHYL, 1_000),
					new AStack[] {
							new ComparableStack(ModItems.circuit, lbsm ? 8 : 32, EnumCircuitType.CHIP),
							new ComparableStack(ModItems.circuit, lbsm ? 8 : 32, EnumCircuitType.CAPACITOR),
							new ComparableStack(ModItems.circuit, lbsm ? 8 : 16, EnumCircuitType.CAPACITOR_TANTALIUM)},
					new AStack[] {
							new ComparableStack(ModItems.circuit, 1, EnumCircuitType.CONTROLLER_CHASSIS),
							new OreDictStack(RUBBER.ingot(), 4)},
					new AStack[] {
							new OreDictStack(PB.wireFine(), 16)}
			));
			recipes.add(new SolderingRecipe(new ItemStack(ModItems.circuit, 1, EnumCircuitType.CONTROLLER_ADVANCED.ordinal()), 600, 25_000,
					new FluidStack(Fluids.PERFLUOROMETHYL, 4_000),
					new AStack[] {
							new ComparableStack(ModItems.circuit, lbsm ? 8 : 16, EnumCircuitType.CHIP_BISMOID),
							new ComparableStack(ModItems.circuit, lbsm ? 16 : 48, EnumCircuitType.CAPACITOR_TANTALIUM),
							new ComparableStack(ModItems.circuit, lbsm ? 8 : 32, EnumCircuitType.CAPACITOR_LANTHANIUM)},
					new AStack[] {
							new ComparableStack(ModItems.circuit, 1, EnumCircuitType.CONTROLLER_CHASSIS),
							new OreDictStack(ANY_HARDPLASTIC.ingot(), 4)},
					new AStack[] {
							new OreDictStack(PB.wireFine(), 24)}
			));

		recipes.add(new SolderingRecipe(new ItemStack(ModItems.circuit, 1, EnumCircuitType.CONTROLLER_QUANTUM.ordinal()), 600, 250_000,
					new FluidStack(Fluids.PERFLUOROMETHYL_COLD, 6_000),
					new AStack[] {
							new ComparableStack(ModItems.circuit, lbsm ? 8 : 16, EnumCircuitType.CHIP_QUANTUM),
							new ComparableStack(ModItems.circuit, lbsm ? 16 : 48, EnumCircuitType.CHIP_BISMOID),
							new ComparableStack(ModItems.circuit, lbsm ? 1 : 8, EnumCircuitType.ATOMIC_CLOCK)},
					new AStack[] {
							new ComparableStack(ModItems.circuit, 2, EnumCircuitType.CONTROLLER_ADVANCED),
							new ComparableStack(ModItems.upgrade_speed_3)},
					new AStack[] {
							new OreDictStack(PB.wireFine(), 32)}
			));
		}
		
		recipes.add(new SolderingRecipe(new ItemStack(ModItems.sat_chip), 100, 100,
				new AStack[] {
						new ComparableStack(ModItems.circuit, 8, EnumCircuitType.CHIP),
						new ComparableStack(ModItems.circuit, 2, EnumCircuitType.CAPACITOR)},
				new AStack[] {
						new ComparableStack(ModItems.circuit, 2, EnumCircuitType.PCB)},
				new AStack[] {
						new OreDictStack(PB.wireFine(), 4)}
		));

		/*
		 * SPACE
		 * Includes cheaper recipes from TEKTO!
		 */

		recipes.add(new SolderingRecipe(new ItemStack(ModItems.circuit, 1, EnumCircuitType.AERO.ordinal()), 300, 1_000,
				new AStack[] {
						new ComparableStack(ModItems.circuit, 3, EnumCircuitType.CHIP)},
				new AStack[] {
						new ComparableStack(ModItems.circuit, 1, EnumCircuitType.ADVANCED),
						new OreDictStack(RUBBER.ingot(), 4)},
				new AStack[] {
						new OreDictStack(PB.wireFine(), 4)}
		));

		recipes.add(new SolderingRecipe(new ItemStack(ModItems.hard_drive, 1), 200, 250,
				new AStack[] {
						new ComparableStack(ModItems.circuit, 2, EnumCircuitType.CHIP)},
				new AStack[] {
						new ComparableStack(ModItems.circuit, 16, EnumCircuitType.PCB)},
				new AStack[] {
						new OreDictStack(MINGRADE.wireFine(), 4)}
		));
		// for Martian Mode
		recipes.add(new SolderingRecipe(new ItemStack(ModItems.circuit, 1, EnumCircuitType.CAPACITOR_BOARD.ordinal()), 200, 300,
				new FluidStack(Fluids.PEROXIDE, 500),
				new AStack[] {
						new ComparableStack(ModItems.circuit, 3, EnumCircuitType.CAPACITOR_LANTHANIUM)},
				new AStack[] {
						new ComparableStack(ModItems.circuit, 1, EnumCircuitType.PCB)},
				new AStack[] {
						new OreDictStack(PB.wireFine(), 3)}
		));

		recipes.add(new SolderingRecipe(new ItemStack(ModItems.circuit, 1, EnumCircuitType.BISMOID.ordinal()), 400, 10_000,
				new FluidStack(Fluids.POLYTHYLENE, 1_000),
				new AStack[] {
						new ComparableStack(ModItems.circuit, 4, EnumCircuitType.CHIP_BISMOID),
						new ComparableStack(ModItems.circuit, lbsm ? 1 : 4, EnumCircuitType.GASCHIP),
						new ComparableStack(ModItems.circuit, lbsm ? 2 : 8, EnumCircuitType.CAPACITOR)},
				new AStack[] {
						new ComparableStack(ModItems.circuit, 6, EnumCircuitType.PCB)},
				new AStack[] {
						new OreDictStack(PB.wireFine(), 12)}
		));

		recipes.add(new SolderingRecipe(new ItemStack(ModItems.circuit, 1, EnumCircuitType.ADVANCED.ordinal()), 300, 1_000,
				new FluidStack(Fluids.POLYTHYLENE, 250),
				new AStack[] {
						new ComparableStack(ModItems.circuit, lbsm ? 1 : 2, EnumCircuitType.GASCHIP),
						new ComparableStack(ModItems.circuit, 2, EnumCircuitType.CAPACITOR)},
				new AStack[] {
						new ComparableStack(ModItems.circuit, 4, EnumCircuitType.PCB)},
				new AStack[] {
						new OreDictStack(PB.wireFine(), 8)}
		));

		/*
		 * UPGRADES
		 */

		recipes.add(new SolderingRecipe(new ItemStack(ModItems.upgrade_speed_1), 200, 1_000,
				new AStack[] {new ComparableStack(ModItems.circuit, 4, EnumCircuitType.VACUUM_TUBE), new ComparableStack(ModItems.circuit, 1, EnumCircuitType.CAPACITOR)},
				new AStack[] {new ComparableStack(ModItems.upgrade_template), new OreDictStack(MINGRADE.dust(), 4)},
				new AStack[] {}
		));
		recipes.add(new SolderingRecipe(new ItemStack(ModItems.upgrade_effect_1), 200, 1_000,
				new AStack[] {new ComparableStack(ModItems.circuit, 4, EnumCircuitType.VACUUM_TUBE), new ComparableStack(ModItems.circuit, 1, EnumCircuitType.CAPACITOR)},
				new AStack[] {new ComparableStack(ModItems.upgrade_template), new OreDictStack(EMERALD.dust(), 4)},
				new AStack[] {}
		));
		recipes.add(new SolderingRecipe(new ItemStack(ModItems.upgrade_power_1), 200, 1_000,
				new AStack[] {new ComparableStack(ModItems.circuit, 4, EnumCircuitType.VACUUM_TUBE), new ComparableStack(ModItems.circuit, 1, EnumCircuitType.CAPACITOR)},
				new AStack[] {new ComparableStack(ModItems.upgrade_template), new OreDictStack(GOLD.dust(), 4)},
				new AStack[] {}
		));
		recipes.add(new SolderingRecipe(new ItemStack(ModItems.upgrade_fortune_1), 200, 1_000,
				new AStack[] {new ComparableStack(ModItems.circuit, 4, EnumCircuitType.VACUUM_TUBE), new ComparableStack(ModItems.circuit, 1, EnumCircuitType.CAPACITOR)},
				new AStack[] {new ComparableStack(ModItems.upgrade_template), new OreDictStack(NB.dust(), 4)},
				new AStack[] {}
		));
		recipes.add(new SolderingRecipe(new ItemStack(ModItems.upgrade_afterburn_1), 200, 1_000,
				new AStack[] {new ComparableStack(ModItems.circuit, 4, EnumCircuitType.VACUUM_TUBE), new ComparableStack(ModItems.circuit, 1, EnumCircuitType.CAPACITOR)},
				new AStack[] {new ComparableStack(ModItems.upgrade_template), new OreDictStack(W.dust(), 4)},
				new AStack[] {}
		));
		recipes.add(new SolderingRecipe(new ItemStack(ModItems.upgrade_radius), 200, 1_000,
				new AStack[] {new ComparableStack(ModItems.circuit, 4, EnumCircuitType.CHIP), new ComparableStack(ModItems.circuit, 4, EnumCircuitType.CAPACITOR)},
				new AStack[] {new ComparableStack(ModItems.upgrade_template), new OreDictStack("dustGlowstone", 4)},
				new AStack[] {}
		));
		recipes.add(new SolderingRecipe(new ItemStack(ModItems.upgrade_health), 200, 1_000,
				new AStack[] {new ComparableStack(ModItems.circuit, 4, EnumCircuitType.CHIP), new ComparableStack(ModItems.circuit, 4, EnumCircuitType.CAPACITOR)},
				new AStack[] {new ComparableStack(ModItems.upgrade_template), new OreDictStack(LI.dust(), 4)},
				new AStack[] {}
		));


		/*
		 * NTNH
		 */

		if(Loader.isModLoaded("ae2fc") && Loader.isModLoaded("appliedenergistics2")) {
		// Recipe 2
		recipes.add(new SolderingRecipe(
			new ItemStack(GameRegistry.findItem("ae2fc", "fluid_part"), 1, 0),
			100, 150,
			new AStack[] {
				new ComparableStack(GameRegistry.findItem("appliedenergistics2", "item.ItemMultiMaterial"), 8, 10),
				new ComparableStack(ModItems.plate_polymer, 16),
				new ComparableStack(ModItems.ingot_desh, 1)},
			new AStack[] {},
			new AStack[] {new OreDictStack("wireFineLead", 32)}
		));

		// Recipe 3
		recipes.add(new SolderingRecipe(
			new ItemStack(GameRegistry.findItem("ae2fc", "fluid_part"), 1, 1),
			100, 150,
			new AStack[] {
				new ComparableStack(GameRegistry.findItem("appliedenergistics2", "item.ItemMultiMaterial"), 8, 10),
				new ComparableStack(ModItems.plate_polymer, 16),
				new ComparableStack(GameRegistry.findItem("ae2fc", "fluid_part"), 3, 0)},
			new AStack[] {},
			new AStack[] {new OreDictStack("wireFineLead", 32)}
		));

		// Recipe 4
		recipes.add(new SolderingRecipe(
			new ItemStack(GameRegistry.findItem("ae2fc", "fluid_part"), 1, 2),
			100, 150,
			new AStack[] {
				new ComparableStack(GameRegistry.findItem("appliedenergistics2", "item.ItemMultiMaterial"), 8, 10),
				new ComparableStack(ModItems.plate_polymer, 16),
				new ComparableStack(GameRegistry.findItem("ae2fc", "fluid_part"), 3, 1)},
			new AStack[] {},
			new AStack[] {new OreDictStack("wireFineLead", 32)}
		));

		// Recipe 5
		recipes.add(new SolderingRecipe(
			new ItemStack(GameRegistry.findItem("ae2fc", "fluid_part"), 1, 3),
			100, 150,
			new AStack[] {
				new ComparableStack(GameRegistry.findItem("appliedenergistics2", "item.ItemMultiMaterial"), 8, 10),
				new ComparableStack(ModItems.plate_polymer, 16),
				new ComparableStack(GameRegistry.findItem("ae2fc", "fluid_part"), 3, 2)},
			new AStack[] {},
			new AStack[] {new OreDictStack("wireFineLead", 32)}
		));

		// Recipe 6
		recipes.add(new SolderingRecipe(
			new ItemStack(GameRegistry.findItem("ae2fc", "fluid_part"), 1, 4),
			100, 150,
			new AStack[] {
				new ComparableStack(GameRegistry.findItem("appliedenergistics2", "item.ItemMultiMaterial"), 8, 10),
				new ComparableStack(ModItems.plate_polymer, 16),
				new ComparableStack(GameRegistry.findItem("ae2fc", "fluid_part"), 3, 3)},
			new AStack[] {},
			new AStack[] {new OreDictStack("wireFineLead", 32)}
		));

		// Recipe 7
		recipes.add(new SolderingRecipe(
			new ItemStack(GameRegistry.findItem("ae2fc", "fluid_part"), 1, 5),
			100, 150,
			new AStack[] {
				new ComparableStack(GameRegistry.findItem("appliedenergistics2", "item.ItemMultiMaterial"), 8, 10),
				new ComparableStack(ModItems.plate_polymer, 16),
				new ComparableStack(GameRegistry.findItem("ae2fc", "fluid_part"), 3, 4)},
			new AStack[] {},
			new AStack[] {new OreDictStack("wireFineLead", 32)}
		));

		// Recipe 8
		recipes.add(new SolderingRecipe(
			new ItemStack(GameRegistry.findItem("ae2fc", "fluid_part"), 1, 6),
			100, 150,
			new AStack[] {
				new ComparableStack(GameRegistry.findItem("appliedenergistics2", "item.ItemMultiMaterial"), 8, 10),
				new ComparableStack(ModItems.plate_polymer, 16),
				new ComparableStack(GameRegistry.findItem("ae2fc", "fluid_part"), 3, 5)},
			new AStack[] {},
			new AStack[] {new OreDictStack("wireFineLead", 32)}
		));

		// Recipe 9
		recipes.add(new SolderingRecipe(
			new ItemStack(GameRegistry.findItem("ae2fc", "fluid_part"), 1, 7),
			100, 150,
			new AStack[] {
				new ComparableStack(GameRegistry.findItem("appliedenergistics2", "item.ItemMultiMaterial"), 8, 10),
				new ComparableStack(ModItems.plate_polymer, 16),
				new ComparableStack(GameRegistry.findItem("ae2fc", "fluid_part"), 3, 6)},
			new AStack[] {},
			new AStack[] {new OreDictStack("wireFineLead", 32)}
		));
		}

		// Recipe 10
		if(Loader.isModLoaded("OpenComputers")) {
		recipes.add(new SolderingRecipe(
			new ItemStack(GameRegistry.findItem("OpenComputers", "item"), 1, 23),
			90, 120,
			new AStack[] {
				new ComparableStack(Items.redstone, 1),
				new ComparableStack(ModItems.nugget_silicon, 1)},
			new AStack[] {new ComparableStack(ModItems.ingot_polymer, 1)},
			new AStack[] {}
		));

		// Recipe 11
		recipes.add(new SolderingRecipe(
			new ItemStack(GameRegistry.findItem("OpenComputers", "item"), 8, 24),
			120, 200,
			new AStack[] {
				new ComparableStack(Items.redstone, 4),
				new ComparableStack(ModItems.circuit, 1, 4),
				new ComparableStack(GameRegistry.findItem("OpenComputers", "item"), 4, 23)},
			new AStack[] {},
			new AStack[] {}
		));

		// Recipe 12
		recipes.add(new SolderingRecipe(
			new ItemStack(GameRegistry.findItem("OpenComputers", "item"), 4, 25),
			120, 200,
			new AStack[] {
				new ComparableStack(Items.redstone, 4),
				new ComparableStack(GameRegistry.findItem("OpenComputers", "item"), 8, 24),
				new ComparableStack(GameRegistry.findItem("OpenComputers", "item"), 4, 23)},
			new AStack[] {},
			new AStack[] {}
		));

		// Recipe 13
		recipes.add(new SolderingRecipe(
			new ItemStack(GameRegistry.findItem("OpenComputers", "item"), 2, 26),
			120, 200,
			new AStack[] {
				new ComparableStack(Items.redstone, 4),
				new ComparableStack(GameRegistry.findItem("OpenComputers", "item"), 4, 25),
				new ComparableStack(GameRegistry.findItem("OpenComputers", "item"), 4, 23)},
			new AStack[] {},
			new AStack[] {}
		));

		// Recipe 14
		recipes.add(new SolderingRecipe(
			new ItemStack(GameRegistry.findItem("OpenComputers", "item"), 1, 27),
			150, 300,
			new AStack[] {
				new ComparableStack(GameRegistry.findItem("appliedenergistics2", "item.ItemMultiMaterial"), 1, 23),
				new ComparableStack(GameRegistry.findItem("OpenComputers", "item"), 1, 24),
				new ComparableStack(GameRegistry.findItem("OpenComputers", "item"), 3, 23)},
			new AStack[] {},
			new AStack[] {}
		));

		// Recipe 15
		recipes.add(new SolderingRecipe(
			new ItemStack(GameRegistry.findItem("OpenComputers", "item"), 1, 28),
			150, 300,
			new AStack[] {
				new ComparableStack(GameRegistry.findItem("appliedenergistics2", "item.ItemMultiMaterial"), 1, 22),
				new ComparableStack(GameRegistry.findItem("OpenComputers", "item"), 1, 24),
				new ComparableStack(GameRegistry.findItem("OpenComputers", "item"), 3, 23)},
			new AStack[] {},
			new AStack[] {}
		));

		// Recipe 16
		recipes.add(new SolderingRecipe(
			new ItemStack(GameRegistry.findItem("OpenComputers", "item"), 1, 29),
			180, 400,
			new AStack[] {
				new ComparableStack(GameRegistry.findItem("OpenComputers", "item"), 1, 27),
				new ComparableStack(GameRegistry.findItem("OpenComputers", "item"), 1, 28),
				new ComparableStack(GameRegistry.findItem("OpenComputers", "item"), 1, 24)},
			new AStack[] {},
			new AStack[] {}
		));

		// Recipe 17
		recipes.add(new SolderingRecipe(
			new ItemStack(GameRegistry.findItem("OpenComputers", "item"), 1, 42),
			180, 400,
			new AStack[] {
				new ComparableStack(GameRegistry.findItem("OpenComputers", "item"), 1, 27),
				new ComparableStack(GameRegistry.findItem("OpenComputers", "item"), 1, 28),
				new ComparableStack(GameRegistry.findItem("OpenComputers", "item"), 1, 25)},
			new AStack[] {},
			new AStack[] {}
		));

		// Recipe 18
		recipes.add(new SolderingRecipe(
			new ItemStack(GameRegistry.findItem("OpenComputers", "item"), 1, 43),
			180, 400,
			new AStack[] {
				new ComparableStack(GameRegistry.findItem("OpenComputers", "item"), 1, 27),
				new ComparableStack(GameRegistry.findItem("OpenComputers", "item"), 1, 28),
				new ComparableStack(GameRegistry.findItem("OpenComputers", "item"), 1, 26)},
			new AStack[] {},
			new AStack[] {}
		));

		// Recipe 19
		recipes.add(new SolderingRecipe(
			new ItemStack(GameRegistry.findItem("OpenComputers", "item"), 1, 1),
			100, 150,
			new AStack[] {
				new ComparableStack(Items.redstone, 4),
				new ComparableStack(GameRegistry.findItem("OpenComputers", "item"), 2, 24)},
			new AStack[] {new ComparableStack(ModItems.circuit, 1, 3)},
			new AStack[] {}
		));

		// Recipe 20
		recipes.add(new SolderingRecipe(
			new ItemStack(GameRegistry.findItem("OpenComputers", "item"), 1, 2),
			100, 150,
			new AStack[] {
				new ComparableStack(Items.redstone, 4),
				new ComparableStack(GameRegistry.findItem("OpenComputers", "item"), 2, 25)},
			new AStack[] {new ComparableStack(ModItems.circuit, 1, 3)},
			new AStack[] {}
		));

		// Recipe 21
		recipes.add(new SolderingRecipe(
			new ItemStack(GameRegistry.findItem("OpenComputers", "item"), 1, 38),
			100, 150,
			new AStack[] {
				new ComparableStack(Items.redstone, 4),
				new ComparableStack(GameRegistry.findItem("OpenComputers", "item"), 2, 26)},
			new AStack[] {new ComparableStack(ModItems.circuit, 1, 3)},
			new AStack[] {}
		));

		// Navigation Upgrade - provide craftable version (map can be swapped via crafting table recraft)
		ItemStack navStack = getOCStack("oc:navigationUpgrade", 1);
		if(navStack != null) {
			recipes.add(new SolderingRecipe(
				navStack,
				180, 400,
				new AStack[] {
					new ComparableStack(Items.compass, 1),
					new ComparableStack(GameRegistry.findItem("OpenComputers", "item"), 1, 25),
					new ComparableStack(Items.redstone, 1)},
				new AStack[] {new ComparableStack(ModItems.circuit, 1, 3)},
				new AStack[] {}
			));
		}

		// RAM 1.5 (ram2) - missing OC tier
		ItemStack ram2Stack = getOCStack("oc:ram2", 1);
		if(ram2Stack != null) {
			recipes.add(new SolderingRecipe(
				ram2Stack,
				100, 150,
				new AStack[] {
					new ComparableStack(Items.redstone, 4),
					new ComparableStack(GameRegistry.findItem("OpenComputers", "item"), 1, 24),
					new ComparableStack(GameRegistry.findItem("OpenComputers", "item"), 1, 25)},
				new AStack[] {new ComparableStack(ModItems.circuit, 1, 3)},
				new AStack[] {}
			));
		}

		// RAM 2.5 (ram4)
		ItemStack ram4Stack = getOCStack("oc:ram4", 1);
		if(ram4Stack != null) {
			recipes.add(new SolderingRecipe(
				ram4Stack,
				100, 150,
				new AStack[] {
					new ComparableStack(Items.redstone, 4),
					new ComparableStack(GameRegistry.findItem("OpenComputers", "item"), 1, 25),
					new ComparableStack(GameRegistry.findItem("OpenComputers", "item"), 1, 26)},
				new AStack[] {new ComparableStack(ModItems.circuit, 1, 3)},
				new AStack[] {}
			));
		}

		// RAM 3.5 (ram6)
		ItemStack ram6Stack = getOCStack("oc:ram6", 1);
		if(ram6Stack != null) {
			recipes.add(new SolderingRecipe(
				ram6Stack,
				100, 150,
				new AStack[] {
					new ComparableStack(Items.redstone, 4),
					new ComparableStack(GameRegistry.findItem("OpenComputers", "item"), 2, 26),
					new ComparableStack(GameRegistry.findItem("OpenComputers", "item"), 1, 25)},
				new AStack[] {new ComparableStack(ModItems.circuit, 1, 3)},
				new AStack[] {}
			));
		}

		// Recipe 22
		recipes.add(new SolderingRecipe(
			new ItemStack(GameRegistry.findItem("OpenComputers", "item"), 1, 33),
			90, 120,
			new AStack[] {new ComparableStack(Items.redstone, 4)},
			new AStack[] {
				new ComparableStack(ModItems.circuit, 1, 3),
				new ComparableStack(ModItems.ingot_polymer, 1)},
			new AStack[] {}
		));

		// Recipe 23
		recipes.add(new SolderingRecipe(
			new ItemStack(GameRegistry.findItem("OpenComputers", "item"), 1, 8),
			180, 400,
			new AStack[] {
				new ComparableStack(GameRegistry.findItem("OpenComputers", "item"), 1, 27),
				new ComparableStack(GameRegistry.findItem("OpenComputers", "item"), 1, 33),
				new ComparableStack(GameRegistry.findItem("OpenComputers", "item"), 1, 1)},
			new AStack[] {},
			new AStack[] {}
		));

		// Recipe 24
		recipes.add(new SolderingRecipe(
			new ItemStack(GameRegistry.findItem("OpenComputers", "item"), 1, 9),
			180, 400,
			new AStack[] {
				new ComparableStack(GameRegistry.findItem("OpenComputers", "item"), 1, 27),
				new ComparableStack(GameRegistry.findItem("OpenComputers", "item"), 1, 33),
				new ComparableStack(GameRegistry.findItem("OpenComputers", "item"), 1, 2)},
			new AStack[] {},
			new AStack[] {}
		));

		// Recipe 25
		recipes.add(new SolderingRecipe(
			new ItemStack(GameRegistry.findItem("OpenComputers", "item"), 1, 10),
			180, 400,
			new AStack[] {
				new ComparableStack(GameRegistry.findItem("OpenComputers", "item"), 1, 27),
				new ComparableStack(GameRegistry.findItem("OpenComputers", "item"), 1, 33),
				new ComparableStack(GameRegistry.findItem("OpenComputers", "item"), 1, 38)},
			new AStack[] {},
			new AStack[] {}
		));

		// Recipe 26
		recipes.add(new SolderingRecipe(
			new ItemStack(GameRegistry.findItem("OpenComputers", "item"), 1, 70),
			120, 200,
			new AStack[] {
				new ComparableStack(GameRegistry.findItem("OpenComputers", "item"), 1, 28),
				new ComparableStack(GameRegistry.findItem("OpenComputers", "item"), 1, 24)},
			new AStack[] {new ComparableStack(ModItems.circuit, 1, 3)},
			new AStack[] {}
		));

		// Recipe 27
		recipes.add(new SolderingRecipe(
			new ItemStack(GameRegistry.findItem("OpenComputers", "item"), 1, 71),
			120, 200,
			new AStack[] {
				new ComparableStack(GameRegistry.findItem("OpenComputers", "item"), 1, 28),
				new ComparableStack(GameRegistry.findItem("OpenComputers", "item"), 1, 25)},
			new AStack[] {new ComparableStack(ModItems.circuit, 1, 3)},
			new AStack[] {}
		));

		// Recipe 28
		recipes.add(new SolderingRecipe(
			new ItemStack(GameRegistry.findItem("OpenComputers", "item"), 1, 72),
			120, 200,
			new AStack[] {
				new ComparableStack(GameRegistry.findItem("OpenComputers", "item"), 1, 28),
				new ComparableStack(GameRegistry.findItem("OpenComputers", "item"), 1, 26)},
			new AStack[] {new ComparableStack(ModItems.circuit, 1, 3)},
			new AStack[] {}
		));

		// Recipe 29
		recipes.add(new SolderingRecipe(
			new ItemStack(GameRegistry.findItem("OpenComputers", "item"), 1, 101),
			200, 500,
			new AStack[] {
				new ComparableStack(GameRegistry.findItem("OpenComputers", "item"), 1, 42),
				new ComparableStack(GameRegistry.findItem("OpenComputers", "item"), 1, 71),
				new ComparableStack(GameRegistry.findItem("OpenComputers", "item"), 1, 9)},
			new AStack[] {},
			new AStack[] {}
		));

		// Recipe 30
		recipes.add(new SolderingRecipe(
			new ItemStack(GameRegistry.findItem("OpenComputers", "item"), 1, 102),
			200, 500,
			new AStack[] {
				new ComparableStack(GameRegistry.findItem("OpenComputers", "item"), 1, 43),
				new ComparableStack(GameRegistry.findItem("OpenComputers", "item"), 1, 72),
				new ComparableStack(GameRegistry.findItem("OpenComputers", "item"), 1, 10)},
			new AStack[] {},
			new AStack[] {}
		));

		// Recipe 31
		recipes.add(new SolderingRecipe(
			new ItemStack(GameRegistry.findItem("OpenComputers", "item"), 1, 104),
			200, 500,
			new AStack[] {
				new ComparableStack(GameRegistry.findItem("OpenComputers", "item"), 1, 33),
				new ComparableStack(GameRegistry.findItem("OpenComputers", "item"), 1, 29),
				new ComparableStack(GameRegistry.findItem("OpenComputers", "item"), 1, 24)},
			new AStack[] {},
			new AStack[] {}
		));

		// Recipe 32
		recipes.add(new SolderingRecipe(
			new ItemStack(GameRegistry.findItem("OpenComputers", "item"), 1, 105),
			200, 500,
			new AStack[] {
				new ComparableStack(GameRegistry.findItem("OpenComputers", "item"), 1, 33),
				new ComparableStack(GameRegistry.findItem("OpenComputers", "item"), 1, 42),
				new ComparableStack(GameRegistry.findItem("OpenComputers", "item"), 1, 25)},
			new AStack[] {},
			new AStack[] {}
		));

		// Recipe 33
		recipes.add(new SolderingRecipe(
			new ItemStack(GameRegistry.findItem("OpenComputers", "item"), 1, 106),
			200, 500,
			new AStack[] {
				new ComparableStack(GameRegistry.findItem("OpenComputers", "item"), 1, 33),
				new ComparableStack(GameRegistry.findItem("OpenComputers", "item"), 1, 43),
				new ComparableStack(GameRegistry.findItem("OpenComputers", "item"), 1, 26)},
			new AStack[] {},
			new AStack[] {}
		));

		// Recipe 34
		recipes.add(new SolderingRecipe(
			new ItemStack(GameRegistry.findItem("OpenComputers", "item"), 1, 5),
			100, 150,
			new AStack[] {
				new ComparableStack(GameRegistry.findItem("OpenComputers", "item"), 2, 24),
				new ComparableStack(GameRegistry.findItem("OpenComputers", "item"), 3, 19)},
			new AStack[] {new ComparableStack(ModItems.circuit, 1, 3)},
			new AStack[] {}
		));

		// Recipe 35
		recipes.add(new SolderingRecipe(
			new ItemStack(GameRegistry.findItem("OpenComputers", "item"), 1, 6),
			100, 150,
			new AStack[] {
				new ComparableStack(GameRegistry.findItem("OpenComputers", "item"), 2, 25),
				new ComparableStack(GameRegistry.findItem("OpenComputers", "item"), 3, 19)},
			new AStack[] {new ComparableStack(ModItems.circuit, 1, 3)},
			new AStack[] {}
		));

		// Recipe 36
		recipes.add(new SolderingRecipe(
			new ItemStack(GameRegistry.findItem("OpenComputers", "item"), 1, 7),
			100, 150,
			new AStack[] {
				new ComparableStack(GameRegistry.findItem("OpenComputers", "item"), 2, 26),
				new ComparableStack(GameRegistry.findItem("OpenComputers", "item"), 3, 19)},
			new AStack[] {new ComparableStack(ModItems.circuit, 1, 3)},
			new AStack[] {}
		));

		// Recipe 37
		recipes.add(new SolderingRecipe(
			new ItemStack(GameRegistry.findItem("OpenComputers", "item"), 1, 11),
			120, 200,
			new AStack[] {
				new ComparableStack(GameRegistry.findItem("OpenComputers", "item"), 1, 33),
				new ComparableStack(GameRegistry.findItem("OpenComputers", "cable"), 1),
				new ComparableStack(GameRegistry.findItem("OpenComputers", "item"), 1, 24)},
			new AStack[] {},
			new AStack[] {}
		));

		// Recipe 38
		recipes.add(new SolderingRecipe(
			new ItemStack(GameRegistry.findItem("OpenComputers", "item"), 1, 113),
			150, 300,
			new AStack[] {
				new ComparableStack(GameRegistry.findItem("OpenComputers", "item"), 1, 33),
				new ComparableStack(GameRegistry.findItem("OpenComputers", "item"), 1, 11),
				new ComparableStack(GameRegistry.findItem("OpenComputers", "item"), 1, 24)},
			new AStack[] {},
			new AStack[] {}
		));

		// Recipe 39
		recipes.add(new SolderingRecipe(
			new ItemStack(GameRegistry.findItem("OpenComputers", "item"), 1, 13),
			150, 300,
			new AStack[] {
				new ComparableStack(GameRegistry.findItem("OpenComputers", "item"), 1, 33),
				new ComparableStack(GameRegistry.findItem("OpenComputers", "item"), 1, 113),
				new ComparableStack(GameRegistry.findItem("OpenComputers", "item"), 1, 25)},
			new AStack[] {},
			new AStack[] {}
		));

		// Recipe 40 - Linked Card (outputs 2 paired cards)
		recipes.add(new SolderingRecipe(
			new ItemStack(GameRegistry.findItem("OpenComputers", "item"), 2, 51),
			180, 400,
			new AStack[] {
				new ComparableStack(GameRegistry.findItem("OpenComputers", "item"), 2, 26),
				new ComparableStack(Items.ender_pearl, 2),
				new ComparableStack(GameRegistry.findItem("OpenComputers", "item"), 2, 11)},
			new AStack[] {},
			new AStack[] {}
		));

		// Recipe 41
		recipes.add(new SolderingRecipe(
			new ItemStack(GameRegistry.findItem("OpenComputers", "item"), 1, 44),
			150, 300,
			new AStack[] {
				new ComparableStack(GameRegistry.findItem("OpenComputers", "item"), 1, 33),
				new ComparableStack(Items.ender_pearl, 1),
				new ComparableStack(GameRegistry.findItem("OpenComputers", "item"), 1, 25)},
			new AStack[] {},
			new AStack[] {}
		));

		// Recipe 42
		recipes.add(new SolderingRecipe(
			new ItemStack(GameRegistry.findItem("OpenComputers", "item"), 1, 66),
			120, 200,
			new AStack[] {
				new ComparableStack(GameRegistry.findItem("OpenComputers", "item"), 1, 33),
				new ComparableStack(Blocks.redstone_block, 1),
				new ComparableStack(GameRegistry.findItem("OpenComputers", "item"), 1, 24)},
			new AStack[] {},
			new AStack[] {}
		));

		// Recipe 43
		recipes.add(new SolderingRecipe(
			new ItemStack(GameRegistry.findItem("OpenComputers", "item"), 1, 12),
			120, 200,
			new AStack[] {
				new ComparableStack(GameRegistry.findItem("OpenComputers", "item"), 1, 33),
				new ComparableStack(Blocks.redstone_block, 1),
				new ComparableStack(GameRegistry.findItem("OpenComputers", "item"), 1, 25)},
			new AStack[] {},
			new AStack[] {}
		));

		// Recipe 44
		recipes.add(new SolderingRecipe(
			new ItemStack(GameRegistry.findItem("OpenComputers", "item"), 1, 45),
			180, 400,
			new AStack[] {
				new ComparableStack(GameRegistry.findItem("OpenComputers", "item"), 1, 70),
				new ComparableStack(GameRegistry.findItem("OpenComputers", "item"), 1, 1),
				new ComparableStack(GameRegistry.findItem("OpenComputers", "item"), 1, 24)},
			new AStack[] {},
			new AStack[] {}
		));

		// Recipe 45
		recipes.add(new SolderingRecipe(
			new ItemStack(GameRegistry.findItem("OpenComputers", "item"), 1, 46),
			180, 400,
			new AStack[] {
				new ComparableStack(GameRegistry.findItem("OpenComputers", "item"), 1, 71),
				new ComparableStack(GameRegistry.findItem("OpenComputers", "item"), 1, 2),
				new ComparableStack(GameRegistry.findItem("OpenComputers", "item"), 1, 25)},
			new AStack[] {},
			new AStack[] {}
		));

		// Recipe 46
		recipes.add(new SolderingRecipe(
			new ItemStack(GameRegistry.findItem("OpenComputers", "item"), 1, 40),
			180, 400,
			new AStack[] {
				new ComparableStack(GameRegistry.findItem("OpenComputers", "item"), 1, 72),
				new ComparableStack(GameRegistry.findItem("OpenComputers", "item"), 1, 38),
				new ComparableStack(GameRegistry.findItem("OpenComputers", "item"), 1, 26)},
			new AStack[] {},
			new AStack[] {}
		));

		// Recipe 47
		recipes.add(new SolderingRecipe(
			new ItemStack(GameRegistry.findItem("OpenComputers", "item"), 1, 74),
			120, 200,
			new AStack[] {
				new ComparableStack(GameRegistry.findItem("OpenComputers", "item"), 1, 70),
				new ComparableStack(GameRegistry.findItem("OpenComputers", "item"), 1, 24)},
			new AStack[] {new ComparableStack(ModItems.circuit, 1, 3)},
			new AStack[] {}
		));

		// Recipe 48
		recipes.add(new SolderingRecipe(
			new ItemStack(GameRegistry.findItem("OpenComputers", "item"), 1, 92),
			120, 200,
			new AStack[] {
				new ComparableStack(GameRegistry.findItem("OpenComputers", "item"), 1, 71),
				new ComparableStack(GameRegistry.findItem("OpenComputers", "item"), 1, 25)},
			new AStack[] {new ComparableStack(ModItems.circuit, 1, 3)},
			new AStack[] {}
		));

		// Recipe 49
		recipes.add(new SolderingRecipe(
			new ItemStack(GameRegistry.findItem("OpenComputers", "item"), 1, 82),
			120, 200,
			new AStack[] {
				new ComparableStack(Blocks.redstone_block, 1),
				new ComparableStack(GameRegistry.findItem("OpenComputers", "item"), 1, 24)},
			new AStack[] {new ComparableStack(ModItems.circuit, 1, 3)},
			new AStack[] {}
		));

		// Recipe 50
		recipes.add(new SolderingRecipe(
			new ItemStack(GameRegistry.findItem("OpenComputers", "item"), 1, 86),
			120, 200,
			new AStack[] {
				new ComparableStack(Blocks.redstone_block, 1),
				new ComparableStack(GameRegistry.findItem("OpenComputers", "item"), 1, 25)},
			new AStack[] {new ComparableStack(ModItems.circuit, 1, 3)},
			new AStack[] {}
		));

		// Recipe 51
		recipes.add(new SolderingRecipe(
			new ItemStack(GameRegistry.findItem("OpenComputers", "item"), 1, 83),
			200, 500,
			new AStack[] {
				new ComparableStack(GameRegistry.findItem("OpenComputers", "item"), 1, 70),
				new ComparableStack(ModItems.drone, 1),
				new ComparableStack(GameRegistry.findItem("OpenComputers", "item"), 1, 82)},
			new AStack[] {},
			new AStack[] {}
		));

		// Recipe 52
		recipes.add(new SolderingRecipe(
			new ItemStack(GameRegistry.findItem("OpenComputers", "item"), 1, 87),
			200, 500,
			new AStack[] {
				new ComparableStack(GameRegistry.findItem("OpenComputers", "item"), 1, 71),
				new ComparableStack(ModItems.drone, 1),
				new ComparableStack(GameRegistry.findItem("OpenComputers", "item"), 1, 86)},
			new AStack[] {},
			new AStack[] {}
		));

		// Recipe 53
		recipes.add(new SolderingRecipe(
			new ItemStack(GameRegistry.findItem("OpenComputers", "item"), 1, 108),
			150, 300,
			new AStack[] {
				new ComparableStack(GameRegistry.findItem("OpenComputers", "item"), 1, 25),
				new ComparableStack(GameRegistry.findItem("OpenComputers", "item"), 3, 13)},
			new AStack[] {new ComparableStack(ModItems.circuit, 1, 3)},
			new AStack[] {}
		));

		// Recipe 54
		recipes.add(new SolderingRecipe(
			new ItemStack(GameRegistry.findItem("OpenComputers", "item"), 1, 41),
			150, 300,
			new AStack[] {
				new ComparableStack(GameRegistry.findItem("OpenComputers", "item"), 1, 25),
				new ComparableStack(GameRegistry.findItem("OpenComputers", "keyboard"), 1),
				new ComparableStack(GameRegistry.findItem("OpenComputers", "item"), 1, 13)},
			new AStack[] {},
			new AStack[] {}
		));
		}

		if(Loader.isModLoaded("appliedenergistics2")) {
		// Recipe 55
		recipes.add(new SolderingRecipe(
			new ItemStack(GameRegistry.findItem("appliedenergistics2", "item.ItemMultiMaterial"), 1, 35),
			100, 150,
			new AStack[] {
				new ComparableStack(GameRegistry.findItem("appliedenergistics2", "item.ItemMultiMaterial"), 8, 10),
				new ComparableStack(ModItems.plate_polymer, 16),
				new ComparableStack(ModItems.ingot_tcalloy, 1)},
			new AStack[] {},
			new AStack[] {new OreDictStack("wireFineLead", 32)}
		));

		// Recipe 56
		recipes.add(new SolderingRecipe(
			new ItemStack(GameRegistry.findItem("appliedenergistics2", "item.ItemMultiMaterial"), 1, 36),
			100, 150,
			new AStack[] {
				new ComparableStack(GameRegistry.findItem("appliedenergistics2", "item.ItemMultiMaterial"), 8, 10),
				new ComparableStack(ModItems.plate_polymer, 16),
				new ComparableStack(GameRegistry.findItem("appliedenergistics2", "item.ItemMultiMaterial"), 3, 35)},
			new AStack[] {},
			new AStack[] {new OreDictStack("wireFineLead", 32)}
		));

		// Recipe 57
		recipes.add(new SolderingRecipe(
			new ItemStack(GameRegistry.findItem("appliedenergistics2", "item.ItemMultiMaterial"), 1, 37),
			100, 150,
			new AStack[] {
				new ComparableStack(GameRegistry.findItem("appliedenergistics2", "item.ItemMultiMaterial"), 8, 10),
				new ComparableStack(ModItems.plate_polymer, 16),
				new ComparableStack(GameRegistry.findItem("appliedenergistics2", "item.ItemMultiMaterial"), 3, 36)},
			new AStack[] {},
			new AStack[] {new OreDictStack("wireFineLead", 32)}
		));

		// Recipe 58
		recipes.add(new SolderingRecipe(
			new ItemStack(GameRegistry.findItem("appliedenergistics2", "item.ItemMultiMaterial"), 1, 38),
			100, 150,
			new AStack[] {
				new ComparableStack(GameRegistry.findItem("appliedenergistics2", "item.ItemMultiMaterial"), 8, 10),
				new ComparableStack(ModItems.plate_polymer, 16),
				new ComparableStack(GameRegistry.findItem("appliedenergistics2", "item.ItemMultiMaterial"), 3, 37)},
			new AStack[] {},
			new AStack[] {new OreDictStack("wireFineLead", 32)}
		));

		// Recipe 59
		recipes.add(new SolderingRecipe(
			new ItemStack(GameRegistry.findItem("appliedenergistics2", "item.ItemMultiMaterial"), 1, 57),
			100, 150,
			new AStack[] {
				new ComparableStack(GameRegistry.findItem("appliedenergistics2", "item.ItemMultiMaterial"), 8, 10),
				new ComparableStack(ModItems.plate_polymer, 16),
				new ComparableStack(GameRegistry.findItem("appliedenergistics2", "item.ItemMultiMaterial"), 3, 38)},
			new AStack[] {},
			new AStack[] {new OreDictStack("wireFineLead", 32)}
		));

		// Recipe 60
		recipes.add(new SolderingRecipe(
			new ItemStack(GameRegistry.findItem("appliedenergistics2", "item.ItemMultiMaterial"), 1, 58),
			100, 150,
			new AStack[] {
				new ComparableStack(GameRegistry.findItem("appliedenergistics2", "item.ItemMultiMaterial"), 8, 10),
				new ComparableStack(ModItems.plate_polymer, 16),
				new ComparableStack(GameRegistry.findItem("appliedenergistics2", "item.ItemMultiMaterial"), 3, 57)},
			new AStack[] {},
			new AStack[] {new OreDictStack("wireFineLead", 32)}
		));

		// Recipe 61
		recipes.add(new SolderingRecipe(
			new ItemStack(GameRegistry.findItem("appliedenergistics2", "item.ItemMultiMaterial"), 1, 59),
			100, 150,
			new AStack[] {
				new ComparableStack(GameRegistry.findItem("appliedenergistics2", "item.ItemMultiMaterial"), 8, 10),
				new ComparableStack(ModItems.plate_polymer, 16),
				new ComparableStack(GameRegistry.findItem("appliedenergistics2", "item.ItemMultiMaterial"), 3, 58)},
			new AStack[] {},
			new AStack[] {new OreDictStack("wireFineLead", 32)}
		));

		// Recipe 62
		recipes.add(new SolderingRecipe(
			new ItemStack(GameRegistry.findItem("appliedenergistics2", "item.ItemMultiMaterial"), 1, 60),
			100, 150,
			new AStack[] {
				new ComparableStack(GameRegistry.findItem("appliedenergistics2", "item.ItemMultiMaterial"), 8, 10),
				new ComparableStack(ModItems.plate_polymer, 16),
				new ComparableStack(GameRegistry.findItem("appliedenergistics2", "item.ItemMultiMaterial"), 3, 59)},
			new AStack[] {},
			new AStack[] {new OreDictStack("wireFineLead", 32)}
		));

		// Recipe 63
		recipes.add(new SolderingRecipe(
			new ItemStack(GameRegistry.findItem("appliedenergistics2", "item.ItemExtremeStorageCell.Quantum"), 1),
			300, 1500,
			new AStack[] {
				new ComparableStack(GameRegistry.findItem("appliedenergistics2", "item.ItemExtremeStorageCell.Container"), 1),
				new ComparableStack(ModItems.circuit, 1, 16),
				new ComparableStack(GameRegistry.findItem("appliedenergistics2", "item.ItemMultiMaterial"), 1, 60)},
			new AStack[] {},
			new AStack[] {new OreDictStack("wireFineLead", 64)}
		));

		// Recipe 64
		recipes.add(new SolderingRecipe(
			new ItemStack(GameRegistry.findItem("appliedenergistics2", "item.ItemMultiMaterial"), 1, 24),
			90, 120,
			new AStack[] {
				new ComparableStack(GameRegistry.findItem("appliedenergistics2", "item.ItemMultiMaterial"), 1, 20),
				new ComparableStack(Items.redstone, 1),
				new ComparableStack(GameRegistry.findItem("appliedenergistics2", "item.ItemMultiMaterial"), 1, 17)},
			new AStack[] {},
			new AStack[] {new OreDictStack("wireFineLead", 8)}
		));

		// Recipe 65
		recipes.add(new SolderingRecipe(
			new ItemStack(GameRegistry.findItem("appliedenergistics2", "item.ItemMultiMaterial"), 1, 22),
			90, 120,
			new AStack[] {
				new ComparableStack(GameRegistry.findItem("appliedenergistics2", "item.ItemMultiMaterial"), 1, 20),
				new ComparableStack(Items.redstone, 1),
				new ComparableStack(GameRegistry.findItem("appliedenergistics2", "item.ItemMultiMaterial"), 1, 18)},
			new AStack[] {},
			new AStack[] {new OreDictStack("wireFineLead", 8)}
		));

		// Recipe 66
		recipes.add(new SolderingRecipe(
			new ItemStack(GameRegistry.findItem("appliedenergistics2", "item.ItemMultiMaterial"), 1, 23),
			90, 120,
			new AStack[] {
				new ComparableStack(GameRegistry.findItem("appliedenergistics2", "item.ItemMultiMaterial"), 1, 20),
				new ComparableStack(Items.redstone, 1),
				new ComparableStack(GameRegistry.findItem("appliedenergistics2", "item.ItemMultiMaterial"), 1, 16)},
			new AStack[] {},
			new AStack[] {new OreDictStack("wireFineLead", 8)}
		));

		// Recipe 67
		recipes.add(new SolderingRecipe(
			new ItemStack(GameRegistry.findItem("appliedenergistics2", "item.ItemMultiMaterial"), 1, 32),
			100, 150,
			new AStack[] {
				new ComparableStack(GameRegistry.findItem("appliedenergistics2", "item.ItemMultiMaterial"), 8, 9),
				new ComparableStack(ModItems.plate_polymer, 16),
				new ComparableStack(ModItems.nugget_technetium, 2)},
			new AStack[] {},
			new AStack[] {new OreDictStack("wireFineLead", 32)}
		));

		// Recipe 68
		recipes.add(new SolderingRecipe(
			new ItemStack(GameRegistry.findItem("appliedenergistics2", "item.ItemMultiMaterial"), 1, 33),
			100, 150,
			new AStack[] {
				new ComparableStack(GameRegistry.findItem("appliedenergistics2", "item.ItemMultiMaterial"), 8, 9),
				new ComparableStack(ModItems.plate_polymer, 16),
				new ComparableStack(GameRegistry.findItem("appliedenergistics2", "item.ItemMultiMaterial"), 3, 32)},
			new AStack[] {},
			new AStack[] {new OreDictStack("wireFineLead", 32)}
		));

		// Recipe 69
		recipes.add(new SolderingRecipe(
			new ItemStack(GameRegistry.findItem("appliedenergistics2", "item.ItemMultiMaterial"), 1, 34),
			100, 150,
			new AStack[] {
				new ComparableStack(GameRegistry.findItem("appliedenergistics2", "item.ItemMultiMaterial"), 8, 9),
				new ComparableStack(ModItems.plate_polymer, 16),
				new ComparableStack(GameRegistry.findItem("appliedenergistics2", "item.ItemMultiMaterial"), 3, 33)},
			new AStack[] {},
			new AStack[] {new OreDictStack("wireFineLead", 32)}
		));
		}

		if(Loader.isModLoaded("ae2fc") && Loader.isModLoaded("appliedenergistics2")) {
		// Recipe 70
		recipes.add(new SolderingRecipe(
			new ItemStack(GameRegistry.findItem("ae2fc", "fluid_storage.quantum"), 1),
			300, 1500,
			new AStack[] {
				new ComparableStack(GameRegistry.findItem("ae2fc", "fluid_part"), 1, 7),
				new ComparableStack(ModItems.circuit, 1, 16),
				new ComparableStack(GameRegistry.findItem("appliedenergistics2", "item.ItemMultiMaterial"), 1, 60)},
			new AStack[] {},
			new AStack[] {new OreDictStack("wireFineLead", 64)}
		));
		}



		if(no528) {
			addFirstUpgrade(ModItems.upgrade_speed_1, ModItems.upgrade_speed_2);
			addSecondUpgrade(ModItems.upgrade_speed_2, ModItems.upgrade_speed_3);
			addFirstUpgrade(ModItems.upgrade_effect_1, ModItems.upgrade_effect_2);
			addSecondUpgrade(ModItems.upgrade_effect_2, ModItems.upgrade_effect_3);
			addFirstUpgrade(ModItems.upgrade_power_1, ModItems.upgrade_power_2);
			addSecondUpgrade(ModItems.upgrade_power_2, ModItems.upgrade_power_3);
			addFirstUpgrade(ModItems.upgrade_fortune_1, ModItems.upgrade_fortune_2);
			addSecondUpgrade(ModItems.upgrade_fortune_2, ModItems.upgrade_fortune_3);
			addFirstUpgrade(ModItems.upgrade_afterburn_1, ModItems.upgrade_afterburn_2);
			addSecondUpgrade(ModItems.upgrade_afterburn_2, ModItems.upgrade_afterburn_3);
		}
		
		if (Loader.isModLoaded("OpenComputers")) {
			Item ocItem = GameRegistry.findItem("OpenComputers", "item");
			recipes.add(new SolderingRecipe(new ItemStack(ModItems.rtty_card), 100, 1_000,
					new AStack[] {
						    new ComparableStack(ModBlocks.radio_torch_receiver),
						    new ComparableStack(ModBlocks.radio_torch_sender),
						    new ComparableStack(ocItem, 1, 25 /* Microchip (Tier 2) */)},
					new AStack[] {new ComparableStack(ocItem, 1, 33 /* Card Base */)},
					new AStack[] {new OreDictStack(PB.wireFine(), 4)}
			));
		}
	}

	public static void addFirstUpgrade(Item lower, Item higher) {
		boolean lbsm = GeneralConfig.enableLBSM && GeneralConfig.enableLBSMSimpleCrafting;
		recipes.add(new SolderingRecipe(new ItemStack(higher), 300, 10_000,
				new AStack[] {new ComparableStack(ModItems.circuit, lbsm ? 4 : 8, EnumCircuitType.CHIP), new ComparableStack(ModItems.circuit, lbsm ? 2 : 4, EnumCircuitType.CAPACITOR)},
				new AStack[] {new ComparableStack(lower), new OreDictStack(ANY_PLASTIC.ingot(), 4)},
				new AStack[] {}
		));
	}

	public static void addSecondUpgrade(Item lower, Item higher) {
		boolean lbsm = GeneralConfig.enableLBSM && GeneralConfig.enableLBSMSimpleCrafting;
		recipes.add(new SolderingRecipe(new ItemStack(higher), 400, 25_000,
				new FluidStack(Fluids.SOLVENT, 500),
				new AStack[] {new ComparableStack(ModItems.circuit, lbsm ? 6 : 16, EnumCircuitType.CHIP), new ComparableStack(ModItems.circuit, lbsm ? 4 : 16, EnumCircuitType.CAPACITOR)},
				new AStack[] {new ComparableStack(lower), new OreDictStack(RUBBER.ingot(), 4)},
				new AStack[] {}
		));
	}

	private static ItemStack getOCStack(String oreName, int count) {
		for(ItemStack stack : OreDictionary.getOres(oreName)) {
			ItemStack copy = stack.copy();
			copy.stackSize = count;
			return copy;
		}
		// fallback: scan all possible damages for OC item to find matching ore dict name
		Item ocItem = GameRegistry.findItem("OpenComputers", "item");
		if(ocItem != null) {
			for(int dmg = 0; dmg < 128; dmg++) {
				ItemStack test = new ItemStack(ocItem, 1, dmg);
				int[] ids = OreDictionary.getOreIDs(test);
				for(int id : ids) {
					if(OreDictionary.getOreName(id).equals(oreName)) {
						ItemStack copy = test.copy();
						copy.stackSize = count;
						return copy;
					}
				}
			}
			// hard fallback for known GTNH OC damages if ore dict scan fails (e.g. early registration)
			int damage = -1;
			if("oc:ram2".equals(oreName)) damage = 50;
			else if("oc:ram4".equals(oreName)) damage = 3;
			else if("oc:ram6".equals(oreName)) damage = 39;
			else if("oc:navigationUpgrade".equals(oreName)) damage = 35;
			if(damage != -1) return new ItemStack(ocItem, count, damage);
		}
		return null;
	}

	public static SolderingRecipe getRecipe(ItemStack[] inputs) {

		for(SolderingRecipe recipe : recipes) {
			if(matchesIngredients(new ItemStack[] {inputs[0], inputs[1], inputs[2]}, recipe.toppings) &&
					matchesIngredients(new ItemStack[] {inputs[3], inputs[4]}, recipe.pcb) &&
					matchesIngredients(new ItemStack[] {inputs[5]}, recipe.solder)) return recipe;
		}

		return null;
	}

	public static HashMap getRecipes() {

		HashMap<Object, Object> recipes = new HashMap<Object, Object>();

		for(SolderingRecipe recipe : SolderingRecipes.recipes) {

			List ingredients = new ArrayList();
			for(AStack stack : recipe.toppings) ingredients.add(stack);
			for(AStack stack : recipe.pcb) ingredients.add(stack);
			for(AStack stack : recipe.solder) ingredients.add(stack);
			if(recipe.fluid != null) ingredients.add(ItemFluidIcon.make(recipe.fluid));

			recipes.put(ingredients.toArray(), recipe.output);
		}

		return recipes;
	}

	@Override
	public String getFileName() {
		return "hbmSoldering.json";
	}

	@Override
	public Object getRecipeObject() {
		return recipes;
	}

	@Override
	public void deleteRecipes() {
		recipes.clear();
		toppings.clear();
		pcb.clear();
		solder.clear();
	}

	@Override
	public void readRecipe(JsonElement recipe) {
		JsonObject obj = (JsonObject) recipe;

		AStack[] toppings = this.readAStackArray(obj.get("toppings").getAsJsonArray());
		AStack[] pcb = this.readAStackArray(obj.get("pcb").getAsJsonArray());
		AStack[] solder = this.readAStackArray(obj.get("solder").getAsJsonArray());
		FluidStack fluid = obj.has("fluid") ? this.readFluidStack(obj.get("fluid").getAsJsonArray()) : null;
		ItemStack output = this.readItemStack(obj.get("output").getAsJsonArray());
		int duration = obj.get("duration").getAsInt();
		long consumption = obj.get("consumption").getAsLong();

		recipes.add(new SolderingRecipe(output, duration, consumption, fluid, toppings, pcb, solder));
	}

	@Override
	public void writeRecipe(Object obj, JsonWriter writer) throws IOException {
		SolderingRecipe recipe = (SolderingRecipe) obj;

		writer.name("toppings").beginArray();
		for(AStack aStack : recipe.toppings) this.writeAStack(aStack, writer);
		writer.endArray();

		writer.name("pcb").beginArray();
		for(AStack aStack : recipe.pcb) this.writeAStack(aStack, writer);
		writer.endArray();

		writer.name("solder").beginArray();
		for(AStack aStack : recipe.solder) this.writeAStack(aStack, writer);
		writer.endArray();

		if(recipe.fluid != null) {
			writer.name("fluid");
			this.writeFluidStack(recipe.fluid, writer);
		}

		writer.name("output");
		this.writeItemStack(recipe.output, writer);

		writer.name("duration").value(recipe.duration);
		writer.name("consumption").value(recipe.consumption);
	}

	public static HashSet<AStack> toppings = new HashSet();
	public static HashSet<AStack> pcb = new HashSet();
	public static HashSet<AStack> solder = new HashSet();

	public static class SolderingRecipe {

		public AStack[] toppings;
		public AStack[] pcb;
		public AStack[] solder;
		public FluidStack fluid;
		public ItemStack output;
		public int duration;
		public long consumption;

		public SolderingRecipe(ItemStack output, int duration, long consumption, FluidStack fluid, AStack[] toppings, AStack[] pcb, AStack[] solder) {
			this.toppings = toppings;
			this.pcb = pcb;
			this.solder = solder;
			this.fluid = fluid;
			this.output = output;
			this.duration = duration;
			this.consumption = consumption;
			for(AStack t : toppings) SolderingRecipes.toppings.add(t);
			for(AStack t : pcb) SolderingRecipes.pcb.add(t);
			for(AStack t : solder) SolderingRecipes.solder.add(t);
		}

		public SolderingRecipe(ItemStack output, int duration, long consumption, AStack[] toppings, AStack[] pcb, AStack[] solder) {
			this(output, duration, consumption, null, toppings, pcb, solder);
		}
	}
}
