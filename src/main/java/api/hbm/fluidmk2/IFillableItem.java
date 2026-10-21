package api.hbm.fluidmk2;

import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.fluid.Fluids;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

public interface IFillableItem {

	/** Whether this stack can be filled with this type. Not particularly useful for normal operations */
	public boolean acceptsFluid(FluidType type, ItemStack stack);
	/** Tries to fill the stack, returns the remainder that couldn't be added */
	public int tryFill(FluidType type, int amount, ItemStack stack);
	/** Whether this stack can fill tiles with this type. Not particularly useful for normal operations */
	public boolean providesFluid(FluidType type, ItemStack stack);
	/** Provides fluid with the maximum being the requested amount */
	public int tryEmpty(FluidType type, int amount, ItemStack stack);
	/** Returns the first (or only) corrently held type, may return null. Currently only used for setting bedrock ores */
	public FluidType getFirstFluidType(ItemStack stack);
	/** Returns the fillstate for the specified fluid. Currently only used for setting bedrock ores */
	public int getFill(ItemStack stack);

	/** Type/Fill logic imported from the old pipette class, used for Syringes and stuff now.. **/
	public static FluidType getFluidType(ItemStack stack) {
		if(!stack.hasTagCompound()) return Fluids.NONE;
		return Fluids.fromID(stack.stackTagCompound.getShort("type"));
	}
	public static short getFluidFill(ItemStack stack) {
		if(!stack.hasTagCompound()) return 0;
		return stack.stackTagCompound.getShort("fill");
	}
	public static void setFluidFill(ItemStack stack, FluidType type, short fill) {
		if(!stack.hasTagCompound()) stack.stackTagCompound = new NBTTagCompound();
		stack.stackTagCompound.setShort("type", (short) type.getID());
		stack.stackTagCompound.setShort("fill", fill);
	}
}
