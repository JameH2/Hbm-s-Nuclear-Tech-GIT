package com.hbm.blocks.generic;

import java.util.HashMap;
import java.util.Random;

import com.hbm.config.WorldConfig;
import com.hbm.dim.SolarSystem;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.fluid.Fluids;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.world.World;

public class BlockOreFluid extends BlockOre {

	private final Block empty;
	private final ReserveType type;

	Random rand = new Random();

	private static HashMap<Block, Block> emptyToFull = new HashMap<>();

	public enum ReserveType {
		OIL,
		GAS,
		BRINE,
	}

	public BlockOreFluid(Material mat, Block empty, ReserveType type) {
		super(mat);
		this.empty = empty;
		this.type = type;

		emptyToFull.put(empty, this);
	}

	public String getUnlocalizedReserveType() {
		switch(type) {
		case GAS: return "_gas";
		case BRINE: return "_brine";
		default: return "";
		}
	}

	public FluidType getPrimaryFluid(int meta) {
		switch(type) {
		case OIL:
			if(meta == SolarSystem.Body.LAYTHE.ordinal()) return Fluids.OIL_DS;
			if(meta == SolarSystem.Body.TEKTO.ordinal()) return Fluids.TCRUDE;
			return Fluids.OIL;
		case GAS: return Fluids.GAS;
		case BRINE: return Fluids.BRINE;
		default: return Fluids.NONE;
		}
	}

	public FluidType getSecondaryFluid(int meta) {
		switch(type) {
		case OIL:
			if(meta == SolarSystem.Body.TEKTO.ordinal()) return Fluids.HGAS;
			return Fluids.GAS;
		case GAS: return Fluids.PETROLEUM;
		default: return Fluids.NONE;
		}
	}

	// how much you can get by putting the block straight into a barrel like a container
	public int getBlockFluidAmount(int meta) {
		switch(type) {
		case OIL: return 250;
		case GAS: return 100;
		default: return 0;
		}
	}

	public int getPrimaryFluidAmount(int meta) {
		if(empty == null) {
			if(meta == SolarSystem.Body.TEKTO.ordinal()) return WorldConfig.tektoBedrockOilPerDeposit;
			return WorldConfig.bedrockOilPerDeposit;
		}

		if(meta == SolarSystem.Body.DUNA.ordinal()) return WorldConfig.dunaOilPerDeposit;
		if(meta == SolarSystem.Body.LAYTHE.ordinal()) return WorldConfig.laytheOilPerDeposit;
		if(meta == SolarSystem.Body.EVE.ordinal()) return WorldConfig.eveGasPerDeposit;
		if(meta == SolarSystem.Body.MUN.ordinal()) return WorldConfig.munBrinePerDeposit;
		if(meta == SolarSystem.Body.MINMUS.ordinal()) return WorldConfig.minmusBrinePerDeposit;
		if(meta == SolarSystem.Body.IKE.ordinal()) return WorldConfig.ikeBrinePerDeposit;
		if(meta == SolarSystem.Body.TEKTO.ordinal()) return WorldConfig.tektoOilPerDeposit;
		return WorldConfig.earthOilPerDeposit;
	}

	public int getSecondaryFluidAmount(int meta) {
		if(empty == null) {
			if(meta == SolarSystem.Body.TEKTO.ordinal()) return WorldConfig.tektoBedrockGasPerDepositMin + rand.nextInt(WorldConfig.tektoBedrockGasPerDepositMax - WorldConfig.tektoBedrockGasPerDepositMin);
			return WorldConfig.bedrockGasPerDepositMin + rand.nextInt(WorldConfig.bedrockGasPerDepositMax - WorldConfig.bedrockGasPerDepositMin);
		}

		if(meta == SolarSystem.Body.DUNA.ordinal()) return WorldConfig.dunaGasPerDepositMin + rand.nextInt(WorldConfig.dunaGasPerDepositMax - WorldConfig.dunaGasPerDepositMin);
		if(meta == SolarSystem.Body.LAYTHE.ordinal()) return WorldConfig.laytheGasPerDepositMin + rand.nextInt(WorldConfig.laytheGasPerDepositMax - WorldConfig.laytheGasPerDepositMin);
		if(meta == SolarSystem.Body.EVE.ordinal()) return WorldConfig.evePetPerDepositMin + rand.nextInt(WorldConfig.evePetPerDepositMax - WorldConfig.evePetPerDepositMin);
		if(meta == SolarSystem.Body.TEKTO.ordinal()) return WorldConfig.tektoGasPerDepositMin + rand.nextInt(WorldConfig.tektoGasPerDepositMax - WorldConfig.tektoGasPerDepositMin);
		return WorldConfig.earthGasPerDepositMin + rand.nextInt(WorldConfig.earthGasPerDepositMax - WorldConfig.earthGasPerDepositMin);
	}

	private double getDrainChance(int meta) {
		if(empty == null) return 0;
		if(meta == SolarSystem.Body.DUNA.ordinal()) return WorldConfig.dunaOilDrainChance;
		if(meta == SolarSystem.Body.LAYTHE.ordinal()) return WorldConfig.laytheOilDrainChance;
		if(meta == SolarSystem.Body.EVE.ordinal()) return WorldConfig.eveGasDrainChance;
		if(meta == SolarSystem.Body.MUN.ordinal()) return WorldConfig.munBrineDrainChance;
		if(meta == SolarSystem.Body.MINMUS.ordinal()) return WorldConfig.minmusBrineDrainChance;
		if(meta == SolarSystem.Body.IKE.ordinal()) return WorldConfig.ikeBrineDrainChance;
		if(meta == SolarSystem.Body.TEKTO.ordinal()) return WorldConfig.tektoOilDrainChance;
		return WorldConfig.earthOilDrainChance;
	}

	public void drain(World world, int x, int y, int z, int meta, double chanceMultiplier) {
		if(empty == null) return;

		if(world.rand.nextDouble() < getDrainChance(meta) * chanceMultiplier) {
			world.setBlock(x, y, z, empty, meta, 3);
		}
	}

	public boolean requiresFracking() {
		return empty == null;
	}

	@Override
	public void onNeighborBlockChange(World world, int x, int y, int z, Block block) {
		if(empty == null) return;

		int meta = world.getBlockMetadata(x, y, z);

		if(world.getBlock(x, y - 1, z) == empty) {
			world.setBlock(x, y, z, empty, meta, 3);
			world.setBlock(x, y - 1, z, this, meta, 3);
		}
	}

	public static Block getFullBlock(Block block) {
		return emptyToFull.get(block);
	}

}
