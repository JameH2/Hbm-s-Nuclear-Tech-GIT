package com.hbm.tileentity;

import java.util.HashMap;
import java.util.List;

import com.hbm.items.machine.ItemMachineUpgrade.UpgradeType;
import com.hbm.items.machine.ItemMachineUpgrade;
import com.hbm.util.i18n.I18nUtil;

import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;

public interface IUpgradeInfoProvider {

	/** If any of the automated display stuff should be applied for this upgrade. A level of 0 is used by the GUI's indicator, as opposed to the item tooltips */
	public boolean canProvideInfo(UpgradeType type, int level, boolean extendedInfo);
	public void provideInfo(UpgradeType type, int level, List<String> info, boolean extendedInfo);
	public HashMap<UpgradeType, Integer> getValidUpgrades();

	public default boolean isUpgradeValid(ItemStack stack) {
		if(stack == null || !(stack.getItem() instanceof ItemMachineUpgrade)) return false;
		HashMap<UpgradeType, Integer> upgrades = getValidUpgrades();
		return upgrades != null && upgrades.containsKey(((ItemMachineUpgrade) stack.getItem()).type);
	}

	public static String getStandardLabel(Block block) {
		return EnumChatFormatting.GREEN.YELLOW + ">>> " + I18nUtil.resolveKey(block.getUnlocalizedName() + ".name") + " <<<";
	}

	public static final String KEY_ACID = "upgrade.acid";
	public static final String KEY_BURN = "upgrade.burn";
	public static final String KEY_CONSUMPTION = "upgrade.consumption";
	public static final String KEY_COOLANT_CONSUMPTION = "upgrade.coolantConsumption";
	public static final String KEY_DELAY = "upgrade.delay";
	public static final String KEY_SPEED = "upgrade.speed";
	public static final String KEY_EFFICIENCY = "upgrade.efficiency";
	public static final String KEY_PRODUCTIVITY = "upgrade.productivity";
	public static final String KEY_FORTUNE = "upgrade.fortune";
	public static final String KEY_RANGE = "upgrade.range";

	public static void addOverdriveInfo(List<String> info, boolean extendedInfo, double speed, double consumption) {
		if(!extendedInfo) {
			info.add(I18nUtil.resolveKey("desc.misc.lshift", ""));
			return;
		}
		info.add(EnumChatFormatting.GRAY + I18nUtil.resolveKey("upgrade.overdrive.desc"));
		if(speed > 0) info.add(EnumChatFormatting.GREEN + I18nUtil.resolveKey(KEY_SPEED, formatMultiplier(speed)));
		info.add(EnumChatFormatting.RED + I18nUtil.resolveKey(KEY_CONSUMPTION, formatMultiplier(consumption)));
	}

	public static String formatMultiplier(double value) {
		return "x" + java.math.BigDecimal.valueOf(value).setScale(2, java.math.RoundingMode.HALF_UP).stripTrailingZeros().toPlainString();
	}
}
