package api.hbm.item;

import net.minecraft.item.ItemStack;

public interface IItemMass {
	
	/**
	 * Calculates the mass of this item, you must multiply by stackSize within this method!
	 * @return the mass of this item in kg
	 */
	public float getMass(ItemStack stack);

}
