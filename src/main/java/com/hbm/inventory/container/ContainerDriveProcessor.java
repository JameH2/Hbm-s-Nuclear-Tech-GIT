package com.hbm.inventory.container;

import com.hbm.inventory.SlotNonRetarded;

import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.IInventory;

public class ContainerDriveProcessor extends ContainerBase {

	public ContainerDriveProcessor(InventoryPlayer invPlayer, IInventory machine) {
		super(invPlayer, machine);

		// 0 - active drive slot
		// 1 - cloning drive slot

		// 2 - upgrade slot

		// 3 - battery slot

		addSlotToContainer(new SlotNonRetarded(machine, 0, 30, 18));
		addSlotToContainer(new SlotNonRetarded(machine, 1, 50, 38));

		addSlotToContainer(new SlotNonRetarded(machine, 2, 81, 24));

		addSlotToContainer(new SlotNonRetarded(machine, 3, 134, 72));

		playerInv(invPlayer, 8, 125, 183);
	}

}
