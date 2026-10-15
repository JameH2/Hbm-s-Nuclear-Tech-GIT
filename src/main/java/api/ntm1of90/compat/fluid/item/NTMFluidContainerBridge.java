package api.ntm1of90.compat.fluid.item;

import com.hbm.inventory.FluidContainerRegistry;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.fluid.Fluids;

import api.ntm1of90.compat.fluid.registry.FluidMappingRegistry;
import com.hbm.util.FluidDebug;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

/**
 * Bridge between NTM fluid container items and Forge's IFluidContainerItem,
 * consumed by ae2fc filter slots and the AE2 terminal container insert/extract.
 */
public class NTMFluidContainerBridge {

	/** Fallback when the container has no entry in NTM's FluidContainerRegistry. */
	public static final int DEFAULT_AMOUNT = 1000;

	public static FluidType getTypeFromDamage(ItemStack stack) {
		if(stack == null || stack.getItemDamage() < 0) return Fluids.NONE;
		return Fluids.fromID(stack.getItemDamage());
	}

	/** Real content of a filled container, from NTM's registry (barrel 16000, canister 1000, ...). */
	public static int getContentAmount(ItemStack stack, FluidType type) {
		if(stack == null) return DEFAULT_AMOUNT;
		int amount = FluidContainerRegistry.getFluidContent(stack, type);
		return amount > 0 ? amount : DEFAULT_AMOUNT;
	}

	public static FluidStack getFluidStack(ItemStack stack, FluidType type) {
		if(type == null || type == Fluids.NONE || type.hasNoID()) return null;
		net.minecraftforge.fluids.Fluid fluid = FluidMappingRegistry.getForgeFluid(type);
		if(fluid == null) {
			FluidDebug.event("container.unmapped|" + (type == null ? "null" : type.getName()),
				() -> "CONTAINER no Forge fluid for " + FluidDebug.describe(type) + " requested by " + FluidDebug.callerHint());
			return null;
		}
		FluidStack result = new FluidStack(fluid, getContentAmount(stack, type));
		FluidDebug.event("container.getfluid|" + type.getName(),
			() -> "CONTAINER " + (stack == null ? "null" : stack.getUnlocalizedName()) + " -> " + FluidDebug.describeForgeStack(result) + " requested by " + FluidDebug.callerHint());
		return result;
	}

	public static int getCapacity(ItemStack stack, FluidType type) {
		return getContentAmount(stack, type);
	}

	/** All-or-nothing drain, matching vanilla bucket semantics. Swaps the stack to its empty variant on doDrain. */
	public static FluidStack drain(ItemStack stack, FluidType type, int maxDrain, boolean doDrain) {
		FluidStack content = getFluidStack(stack, type);
		if(content == null || maxDrain < content.amount) return null;

		if(doDrain && stack != null && stack.getItem().hasContainerItem(stack)) {
			net.minecraft.item.ItemStack empty = stack.getItem().getContainerItem(stack);
			if(empty != null) {
				stack.func_150996_a(empty.getItem());
				stack.setItemDamage(empty.getItemDamage());
				stack.stackTagCompound = empty.stackTagCompound;
			}
		}

		return content;
	}
}
