package com.hbm.items.machine;

import java.util.List;

import com.hbm.dim.CelestialBody;
import com.hbm.dim.orbit.WorldProviderOrbit;
import com.hbm.dim.trait.CBT_Water;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.items.ModItems;
import com.hbm.util.i18n.I18nUtil;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import api.ntm1of90.compat.fluid.item.NTMFluidContainerBridge;
import net.minecraftforge.fluids.FluidStack;

public class ItemInfiniteFluid extends Item implements net.minecraftforge.fluids.IFluidContainerItem {

	private FluidType type;
	private int amount;
	private int chance;
	private boolean requiresTable; // Whether or not a CBT_Water with matching FluidType must be present to function

	public ItemInfiniteFluid(FluidType type, int amount) {
		this(type, amount, 1, false);
	}

	public ItemInfiniteFluid(FluidType type, int amount, int chance) {
		this(type, amount, chance, false);
	}

	public ItemInfiniteFluid(FluidType type, int amount, boolean requiresTable) {
		this(type, amount, 1, requiresTable);
	}

	public ItemInfiniteFluid(FluidType type, int amount, int chance, boolean requiresTable) {
		this.type = type;
		this.amount = amount;
		this.chance = chance;
		this.requiresTable = requiresTable;
	}

	public void onUpdate(ItemStack stack, World world, Entity player, int slot, boolean inHand) {
		if(!requiresTable) return;

		if(stack.stackTagCompound == null)
			stack.stackTagCompound = new NBTTagCompound();

		if(world.provider instanceof WorldProviderOrbit) {
			stack.stackTagCompound.setBoolean("noAtmo", true);
			stack.stackTagCompound.setInteger("fluid", 0);
			return;
		}

		// Check that the current body has a water table
		CBT_Water table = CelestialBody.getTrait(world, CBT_Water.class);
		boolean canOperate = table != null && table.fluid == type;
		stack.stackTagCompound.setBoolean("noAtmo", !canOperate);
		stack.stackTagCompound.setInteger("fluid", table != null ? table.fluid.getID() : 0);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean bool) {
		if(canOperate(stack))
			return;

		FluidType currentFluid = Fluids.fromID(stack.stackTagCompound.getInteger("fluid"));

		if(currentFluid == Fluids.NONE) {
			list.add(I18nUtil.resolveKey("item.infinite_fluid.desc1"));
		} else {
			list.add(I18nUtil.resolveKey("item.infinite_fluid.desc2"));
		}

		list.add(I18nUtil.resolveKey("desc.item.infinite_fluid.requires_table", type.getLocalizedName()));

		if(currentFluid == Fluids.NONE) {
			list.add(I18nUtil.resolveKey("item.infinite_fluid.desc4"));
		} else {
			list.add(I18nUtil.resolveKey("desc.item.infinite_fluid.current_environment_table", currentFluid.getLocalizedName()));
		}
	}

	public FluidType getType() { return this.type; }
	public int getAmount() { return this.amount; }
	public int getChance() { return this.chance; }
	public boolean allowPressure(int pressure) { return this == ModItems.fluid_barrel_infinite || pressure == 0; }
	public boolean canOperate(ItemStack stack) { return stack.stackTagCompound == null || !stack.stackTagCompound.getBoolean("noAtmo"); }

	// ===== Forge IFluidContainerItem bridge (AE2 filter slots etc.) =====

	@Override
	public FluidStack getFluid(ItemStack stack) {
		if(this.type == null || this.type == Fluids.NONE) return null;
		net.minecraftforge.fluids.Fluid fluid = api.ntm1of90.compat.fluid.registry.FluidMappingRegistry.getForgeFluid(this.type);
		return fluid == null ? null : new FluidStack(fluid, Math.max(this.amount, 1));
	}

	@Override
	public int getCapacity(ItemStack stack) {
		return this.amount;
	}

	@Override
	public int fill(ItemStack stack, FluidStack resource, boolean doFill) {
		return 0;
	}

	@Override
	public FluidStack drain(ItemStack stack, int maxDrain, boolean doDrain) {
		FluidStack content = getFluid(stack);
		if(content == null) return null;
		return maxDrain < content.amount ? null : content;
	}
}
