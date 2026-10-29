package com.hbm.util;

import java.lang.reflect.Method;

import com.hbm.tileentity.machine.storage.TileEntityCrateBase;

import api.hbm.redstoneoverradio.IRORValueProvider;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;

public class RORValueProviders {

	private static final Class<?> DRAWERS;
	private static final Method GET_DRAWER;
	private static final Method GET_STORED_ITEM_COPY;

	static {
		Class<?> drawers = null;
		Method getDrawer = null;
		Method getStoredItemCopy = null;
		try {
			drawers = Class.forName("com.jaquadro.minecraft.storagedrawers.block.tile.TileEntityDrawers");
			getDrawer = drawers.getMethod("getDrawer", int.class);
			getStoredItemCopy = Class.forName("com.jaquadro.minecraft.storagedrawers.api.storage.IDrawer").getMethod("getStoredItemCopy");
		} catch(ReflectiveOperationException e) {
			// Storage Drawers is optional; incompatible versions are not reader targets.
			drawers = null;
		}
		DRAWERS = drawers;
		GET_DRAWER = getDrawer;
		GET_STORED_ITEM_COPY = getStoredItemCopy;
	}

	public static IRORValueProvider getProvider(final TileEntity tile) {
		// Native providers (especially the MSU) retain all their existing values.
		if(tile instanceof IRORValueProvider) return (IRORValueProvider) tile;
		if(!(tile instanceof TileEntityCrateBase) && (DRAWERS == null || !DRAWERS.isInstance(tile))) return null;

		return new IRORValueProvider() {
			@Override
			public String[] getFunctionInfo() {
				return new String[] { PREFIX_VALUE + "type" };
			}

			@Override
			public String provideRORValue(String name) {
				if(!(PREFIX_VALUE + "type").equals(name)) return null;
				ItemStack stack;
				if(tile instanceof TileEntityCrateBase) {
					TileEntityCrateBase crate = (TileEntityCrateBase) tile;
					stack = crate.getSizeInventory() > 0 ? crate.getStackInSlot(0) : null;
				} else {
					try {
						Object drawer = GET_DRAWER.invoke(tile, 0);
						stack = drawer == null ? null : (ItemStack) GET_STORED_ITEM_COPY.invoke(drawer);
					} catch(ReflectiveOperationException e) {
						return null;
					}
				}
				return stack == null ? "None" : stack.getDisplayName();
			}
		};
	}
}
