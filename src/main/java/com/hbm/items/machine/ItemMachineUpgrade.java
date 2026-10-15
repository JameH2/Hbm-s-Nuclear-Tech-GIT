package com.hbm.items.machine;

import java.util.List;

import com.hbm.items.ModItems;
import com.hbm.tileentity.IUpgradeInfoProvider;
import com.hbm.util.i18n.I18nUtil;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;

public class ItemMachineUpgrade extends Item {

	public UpgradeType type;
	public int tier = 0;

	public ItemMachineUpgrade() {
		this.setMaxStackSize(1);
		this.type = UpgradeType.SPECIAL;
	}

	public ItemMachineUpgrade(UpgradeType type) {
		this.setMaxStackSize(1);
		this.type = type;
	}

	public ItemMachineUpgrade(UpgradeType type, int tier) {
		this(type);
		this.tier = tier;
	}

	@Override
	public void addInformation(ItemStack itemstack, EntityPlayer player, List list, boolean bool) {

		GuiScreen open = Minecraft.getMinecraft().currentScreen;

		if(open != null && open instanceof GuiContainer) {
			GuiContainer guiContainer = (GuiContainer) open;
			Container container = guiContainer.inventorySlots;
			if(container.inventorySlots.size() > 0) {
				Slot first = container.getSlot(0);
				IInventory inv = (IInventory) first.inventory;
				if(inv instanceof IUpgradeInfoProvider) {
					IUpgradeInfoProvider provider = (IUpgradeInfoProvider) inv;
					if(provider.canProvideInfo(this.type, this.tier, bool)) {
						provider.provideInfo(this.type, this.tier, list, bool);
						return;
					}
				}
			}
		}

		if(this == ModItems.upgrade_radius) {
			list.add(EnumChatFormatting.RED + I18nUtil.resolveKey("item.machine_upgrade.desc1"));
			list.add(I18nUtil.resolveKey("item.machine_upgrade.desc2"));
			list.add(I18nUtil.resolveKey("item.machine_upgrade.desc3"));
		}

		if(this == ModItems.upgrade_health) {
			list.add(EnumChatFormatting.RED + I18nUtil.resolveKey("item.machine_upgrade.desc4"));
			list.add(I18nUtil.resolveKey("item.machine_upgrade.desc5"));
			list.add(I18nUtil.resolveKey("item.machine_upgrade.desc3"));
		}

		if(this == ModItems.upgrade_smelter) {
			list.add(EnumChatFormatting.RED + I18nUtil.resolveKey("item.machine_upgrade.desc6"));
			list.add(I18nUtil.resolveKey("item.machine_upgrade.desc7"));
		}

		if(this == ModItems.upgrade_shredder) {
			list.add(EnumChatFormatting.RED + I18nUtil.resolveKey("item.machine_upgrade.desc6"));
			list.add(I18nUtil.resolveKey("item.machine_upgrade.desc8"));
		}

		if(this == ModItems.upgrade_centrifuge) {
			list.add(EnumChatFormatting.RED + I18nUtil.resolveKey("item.machine_upgrade.desc6"));
			list.add(I18nUtil.resolveKey("item.machine_upgrade.desc9"));
		}

		if(this == ModItems.upgrade_crystallizer) {
			list.add(EnumChatFormatting.RED + I18nUtil.resolveKey("item.machine_upgrade.desc6"));
			list.add(I18nUtil.resolveKey("item.machine_upgrade.desc10"));
		}

		if(this == ModItems.upgrade_screm) {
			list.add(EnumChatFormatting.RED + I18nUtil.resolveKey("item.machine_upgrade.desc6"));
			list.add(I18nUtil.resolveKey("item.machine_upgrade.desc11"));
			list.add(I18nUtil.resolveKey("item.machine_upgrade.desc12"));
			list.add(I18nUtil.resolveKey("item.machine_upgrade.desc13"));
		}

		if(this == ModItems.upgrade_nullifier) {
			list.add(EnumChatFormatting.RED + I18nUtil.resolveKey("item.machine_upgrade.desc6"));
			list.add(I18nUtil.resolveKey("item.machine_upgrade.desc14"));
			list.add(I18nUtil.resolveKey("item.machine_upgrade.desc15"));
		}

		if(this == ModItems.upgrade_gc_speed) {
			list.add(EnumChatFormatting.RED + I18nUtil.resolveKey("item.machine_upgrade.desc16"));
			list.add(I18nUtil.resolveKey("item.machine_upgrade.desc17"));
			list.add(EnumChatFormatting.YELLOW + I18nUtil.resolveKey("item.machine_upgrade.desc18"));
		}
	}

	public static enum UpgradeType {
		SPEED,
		EFFECT,
		POWER,
		FORTUNE,
		AFTERBURN,
		OVERDRIVE,
		SPECIAL,
		LM_DESROYER,
		LM_SCREM,
		LM_SMELTER(true),
		LM_SHREDDER(true),
		LM_CENTRIFUGE(true),
		LM_CRYSTALLIZER(true),
		GS_SPEED,
		CLAUDE;

		public boolean mutex = false;

		private UpgradeType() { }

		private UpgradeType(boolean mutex) {
			this.mutex = mutex;
		}
	}

}
