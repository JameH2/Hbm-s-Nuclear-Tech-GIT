package com.hbm.util;

import appeng.api.config.AccessRestriction;
import appeng.api.config.PowerUnits;
import appeng.api.implementations.items.IAEItemPowerStorage;
import com.hbm.tileentity.network.TileEntityConverterHeRf;
import com.hbm.tileentity.network.TileEntityConverterRfHe;

import net.minecraft.item.ItemStack;

/**
 * Integration for AE2's item power storage API.
 */
public final class AE2Compat {

	private static final double ITEM_CHARGE_RATE_AE = 12_000D;

	private AE2Compat() { }

	/**
	 * Returns whether the stack uses AE2's item power storage API.
	 */
	public static boolean isEnergyStorage(ItemStack stack) {
		return stack != null && stack.getItem() instanceof IAEItemPowerStorage;
	}

	/**
	 * Returns the amount of HE needed to fill an AE2 item, or zero when the item cannot accept power.
	 */
	public static long getChargeDemand(ItemStack stack) {
		if(!isEnergyStorage(stack)) return 0;

		IAEItemPowerStorage storage = (IAEItemPowerStorage) stack.getItem();
		if(!storage.getPowerFlow(stack).hasPermission(AccessRestriction.WRITE)) return 0;

		double missing = Math.max(0, storage.getAEMaxPower(stack) - storage.getAECurrentPower(stack));
		return Math.min(ceilToLong(aeToHeLossless(missing)), getChargeRate());
	}

	/**
	 * Returns the fixed AE2 item charge rate of 12,000 AE/t in HE/t.
	 */
	public static long getChargeRate() {
		return ceilToLong(aeToHeLossless(ITEM_CHARGE_RATE_AE));
	}

	/**
	 * Offers HE to an AE2 item and returns the amount that was not consumed.
	 */
	public static long charge(ItemStack stack, long power) {
		if(power <= 0 || !isEnergyStorage(stack)) return power;

		IAEItemPowerStorage storage = (IAEItemPowerStorage) stack.getItem();
		if(!storage.getPowerFlow(stack).hasPermission(AccessRestriction.WRITE)) return power;

		double offered = heToAe(power);
		if(offered <= 0) return power;

		double remainder = storage.injectAEPower(stack, offered);
		double accepted = Math.max(0, offered - remainder);
		long consumed = Math.min(power, ceilToLong(aeToHeLossless(accepted)));
		return power - consumed;
	}

	/**
	 * Uses the rate from {@link TileEntityConverterHeRf}.
	 */
	private static double heToAe(long power) {
		if(TileEntityConverterHeRf.heInput <= 0 || TileEntityConverterHeRf.rfOutput <= 0) return 0;

		double rf = (double) power / TileEntityConverterHeRf.heInput * TileEntityConverterHeRf.rfOutput;
		return PowerUnits.RF.convertTo(PowerUnits.AE, rf);
	}

	/**
	 * Uses the rate from {@link TileEntityConverterHeRf} instead of {@link TileEntityConverterRfHe} so it's lossless and in turn the opposite of {@link #heToAe(long)}.
	 * Uses ceil to prevent energy creation
	 */
	private static double aeToHeLossless(double power) {
		if(TileEntityConverterHeRf.heInput <= 0 || TileEntityConverterHeRf.rfOutput <= 0) return 0;

		double rf = PowerUnits.AE.convertTo(PowerUnits.RF, power);
		return rf / TileEntityConverterHeRf.rfOutput * TileEntityConverterHeRf.heInput;
	}

	private static long ceilToLong(double value) {
		if(value <= 0 || Double.isNaN(value)) return 0;
		if(value >= Long.MAX_VALUE) return Long.MAX_VALUE;
		return (long) Math.ceil(value);
	}
}
