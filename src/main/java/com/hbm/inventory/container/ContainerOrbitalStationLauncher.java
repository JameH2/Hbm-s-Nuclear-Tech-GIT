package com.hbm.inventory.container;

import com.hbm.handler.RocketStruct;
import com.hbm.inventory.SlotNonRetarded;
import com.hbm.inventory.SlotRocket.SlotCapsule;
import com.hbm.inventory.SlotRocket.SlotDrive;
import com.hbm.inventory.SlotRocket.SlotRocketPart;
import com.hbm.items.weapon.ItemCustomMissilePart.PartType;
import com.hbm.tileentity.machine.TileEntityOrbitalStationLauncher;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class ContainerOrbitalStationLauncher extends ContainerBase {

	public ContainerOrbitalStationLauncher(InventoryPlayer invPlayer, TileEntityOrbitalStationLauncher machine) {
		super(invPlayer, machine);

		int slotId = 0;

		// Target drive
		addSlotToContainer(new SlotNonRetarded(machine, slotId++, 42, 55));

		// Fuel in
		addSlotToContainer(new Slot(machine, slotId++, 42, 73));

		// Fuel out
		addSlotToContainer(new Slot(machine, slotId++, 42, 91));

		// Capsule slot
		addSlotToContainer(new SlotCapsule(machine, slotId++, 18, 13));

		// Stages
		for(int i = 0; i < RocketStruct.MAX_STAGES; i++) {
			addSlotToContainer(new SlotRocketPart(machine, slotId++, 18, 44, i, PartType.FUSELAGE));
			addSlotToContainer(new SlotRocketPart(machine, slotId++, 18, 62, i, PartType.FINS));
			addSlotToContainer(new SlotRocketPart(machine, slotId++, 18, 80, i, PartType.THRUSTER));
		}

		// Drives
		for(int i = 0; i < RocketStruct.MAX_STAGES; i++) {
			addSlotToContainer(new SlotDrive(machine, slotId++, 161, 54, i));
			addSlotToContainer(new SlotDrive(machine, slotId++, 170, 87, i));
		}

		addSlots(invPlayer, 9, 8, 142, 3, 9); // Player inventory
		addSlots(invPlayer, 0, 8, 200, 1, 9); // Player hotbar
	}

	@Override
	public ItemStack slotClick(int index, int button, int mode, EntityPlayer player) {

		//L/R: 0
		//M3: 3
		//SHIFT: 1
		//DRAG: 5

		if(index >= tile.getSizeInventory() && mode == 1) {
			return null;
		}

		if(index < tile.getSizeInventory() - RocketStruct.MAX_STAGES * 2 || index >= tile.getSizeInventory()) {
			return super.slotClick(index, button, mode, player);
		}

		Slot slot = this.getSlot(index);

		ItemStack ret = null;
		ItemStack held = player.inventory.getItemStack();

		if(slot.getHasStack())
			ret = slot.getStack().copy();

		slot.putStack(held != null ? held.copy() : null);

		if(slot.getHasStack()) {
			slot.getStack().stackSize = 1;
		}

		slot.onSlotChanged();

		return ret;
	}

}
