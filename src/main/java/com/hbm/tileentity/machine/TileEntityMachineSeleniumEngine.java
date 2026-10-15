package com.hbm.tileentity.machine;

import java.io.IOException;
import java.util.HashMap;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.stream.JsonWriter;
import com.hbm.inventory.FluidContainerRegistry;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.tank.FluidTank;
import com.hbm.inventory.fluid.trait.FT_Combustible;
import com.hbm.inventory.fluid.trait.FT_Combustible.FuelGrade;
import com.hbm.items.ModItems;
import com.hbm.lib.Library;
import com.hbm.tileentity.IConfigurableMachine;
import com.hbm.tileentity.IFluidCopiable;
import com.hbm.tileentity.IGUIProvider;
import com.hbm.tileentity.TileEntityLoadedBase;
import com.hbm.tileentity.TilePort.PortDef;
import com.hbm.tileentity.TilePortShapes;

import api.hbm.energymk2.IEnergyProviderMK2;
import api.hbm.energymk2.IBatteryItem;
import api.hbm.fluidmk2.IFluidStandardTransceiverMK2;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import com.hbm.inventory.container.ContainerMachineSelenium;
import com.hbm.inventory.gui.GUIMachineSelenium;

public class TileEntityMachineSeleniumEngine extends TileEntityLoadedBase implements ISidedInventory, IEnergyProviderMK2, IFluidStandardTransceiverMK2, IConfigurableMachine, IGUIProvider, IFluidCopiable {

	private ItemStack slots[];

	public long power;
	public int soundCycle = 0;
	public long powerCap = 250000;
	public FluidTank tank;
	public int pistonCount = 0;

	public static long maxPower = 250000;
	public static int fluidCap = 16000;
	public static double pistonExp = 1.0D;
	public static boolean shutUp = false;
	public static HashMap<FuelGrade, Double> fuelEfficiency = new HashMap();

	static {
		fuelEfficiency.put(FuelGrade.LOW, 0.75D);
		fuelEfficiency.put(FuelGrade.MEDIUM, 0.5D);
		fuelEfficiency.put(FuelGrade.HIGH, 0.25D);
		fuelEfficiency.put(FuelGrade.AERO, 0.00D);
	}

	private static final int[] slots_top = new int[]{0};
	private static final int[] slots_bottom = new int[]{1, 2};
	private static final int[] slots_side = new int[]{2};

	private String customName;

	public TileEntityMachineSeleniumEngine() {
		slots = new ItemStack[14];
		tank = new FluidTank(Fluids.DIESEL, fluidCap);
	}

	@Override
	public int getSizeInventory() {
		return slots.length;
	}

	@Override
	public ItemStack getStackInSlot(int i) {
		return slots[i];
	}

	@Override
	public ItemStack getStackInSlotOnClosing(int i) {
		if(slots[i] != null) {
			ItemStack itemStack = slots[i];
			slots[i] = null;
			return itemStack;
		} else {
			return null;
		}
	}

	@Override
	public void setInventorySlotContents(int i, ItemStack itemStack) {
		slots[i] = itemStack;
		if(itemStack != null && itemStack.stackSize > getInventoryStackLimit()) {
			itemStack.stackSize = getInventoryStackLimit();
		}
	}

	@Override
	public String getInventoryName() {
		return this.hasCustomInventoryName() ? this.customName : "container.machineSelenium";
	}

	@Override
	public boolean hasCustomInventoryName() {
		return this.customName != null && this.customName.length() > 0;
	}

	public void setCustomName(String name) {
		this.customName = name;
	}

	@Override
	public int getInventoryStackLimit() {
		return 64;
	}

	@Override
	public boolean isUseableByPlayer(EntityPlayer player) {
		if(worldObj.getTileEntity(xCoord, yCoord, zCoord) != this) {
			return false;
		} else {
			return player.getDistanceSq(xCoord + 0.5D, yCoord + 0.5D, zCoord + 0.5D) <= 64;
		}
	}

	@Override
	public void openInventory() {}

	@Override
	public void closeInventory() {}

	@Override
	public boolean isItemValidForSlot(int i, ItemStack stack) {
		if(i == 9 && FluidContainerRegistry.getFluidContent(stack, tank.getTankType()) > 0)
			return true;
		if(i == 13 && stack.getItem() instanceof IBatteryItem)
			return true;
		return false;
	}

	@Override
	public ItemStack decrStackSize(int i, int j) {
		if(slots[i] != null) {
			if(slots[i].stackSize <= j) {
				ItemStack itemStack = slots[i];
				slots[i] = null;
				return itemStack;
			}
			ItemStack itemStack1 = slots[i].splitStack(j);
			if(slots[i].stackSize == 0) {
				slots[i] = null;
			}
			return itemStack1;
		} else {
			return null;
		}
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);
		NBTTagList list = nbt.getTagList("items", 10);
		this.power = nbt.getLong("powerTime");
		this.powerCap = nbt.getLong("powerCap");
		tank.readFromNBT(nbt, "fuel");
		slots = new ItemStack[getSizeInventory()];
		for(int i = 0; i < list.tagCount(); i++) {
			NBTTagCompound nbt1 = list.getCompoundTagAt(i);
			byte b0 = nbt1.getByte("slot");
			if(b0 >= 0 && b0 < slots.length) {
				slots[b0] = ItemStack.loadItemStackFromNBT(nbt1);
			}
		}
	}

	@Override
	public void writeToNBT(NBTTagCompound nbt) {
		super.writeToNBT(nbt);
		nbt.setLong("powerTime", power);
		nbt.setLong("powerCap", powerCap);
		tank.writeToNBT(nbt, "fuel");
		NBTTagList list = new NBTTagList();
		for(int i = 0; i < slots.length; i++) {
			if(slots[i] != null) {
				NBTTagCompound nbt1 = new NBTTagCompound();
				nbt1.setByte("slot", (byte) i);
				slots[i].writeToNBT(nbt1);
				list.appendTag(nbt1);
			}
		}
		nbt.setTag("items", list);
	}

	@Override
	public int[] getAccessibleSlotsFromSide(int p_94128_1_) {
		return p_94128_1_ == 0 ? slots_bottom : (p_94128_1_ == 1 ? slots_top : slots_side);
	}

	@Override
	public boolean canInsertItem(int i, ItemStack itemStack, int j) {
		return this.isItemValidForSlot(i, itemStack);
	}

	@Override
	public boolean canExtractItem(int i, ItemStack itemStack, int j) {
		if(i == 1 && (itemStack.getItem() == ModItems.canister_empty || itemStack.getItem() == ModItems.tank_steel))
			return true;
		if(i == 2 && itemStack.getItem() instanceof IBatteryItem && ((IBatteryItem) itemStack.getItem()).getCharge(itemStack) == ((IBatteryItem) itemStack.getItem()).getMaxCharge(itemStack))
			return true;
		return false;
	}

	public long getPowerScaled(long i) {
		return (power * i) / powerCap;
	}

	@Override
	public void updateEntity() {
		if(!worldObj.isRemote) {

			if(cachedFluidPorts == null) cachedFluidPorts = TilePortShapes.around(xCoord, yCoord, zCoord);
			if(cachedPowerPorts == null) cachedPowerPorts = new PortDef[] { PortDef.make(xCoord, yCoord, zCoord, ForgeDirection.DOWN) };
			this.setupFluidPorts(cachedFluidPorts);
			this.setupPowerPorts(cachedPowerPorts);
			this.updatePortPOFIFO();

			pistonCount = countPistons();

			tank.setType(11, 12, slots);
			tank.loadTank(9, 10, slots);
			tank.setFill(tank.getFill());

			FluidType type = tank.getTankType();
			powerCap = type == Fluids.NITAN ? maxPower * 10 : maxPower;

			power = Library.chargeItemsFromTE(slots, 13, power, powerCap);

			if(this.pistonCount > 2)
				generate();

			this.networkPackNT(150);
		}
	}

	protected PortDef[] cachedFluidPorts;
	protected PortDef[] cachedPowerPorts;

	@Override
	public void serialize(ByteBuf buf) {
		super.serialize(buf);
		buf.writeLong(power);
		buf.writeInt(pistonCount);
		buf.writeLong(powerCap);
		tank.serialize(buf);
	}

	@Override
	public void deserialize(ByteBuf buf) {
		super.deserialize(buf);
		this.power = buf.readLong();
		this.pistonCount = buf.readInt();
		this.powerCap = buf.readLong();
		tank.deserialize(buf);
	}

	public int countPistons() {
		int count = 0;
		for(int i = 0; i < 9; i++) {
			if(slots[i] != null && slots[i].getItem() == ModItems.piston_selenium)
				count++;
		}
		return count;
	}

	public boolean hasAcceptableFuel() {
		return getHEFromFuel() > 0;
	}

	public long getHEFromFuel() {
		return getHEFromFuel(tank.getTankType());
	}

	public static long getHEFromFuel(FluidType type) {
		if(type.hasTrait(FT_Combustible.class)) {
			FT_Combustible fuel = type.getTrait(FT_Combustible.class);
			FuelGrade grade = fuel.getGrade();
			double efficiency = fuelEfficiency.containsKey(grade) ? fuelEfficiency.get(grade) : 0;
			return (long) (fuel.getCombustionEnergy() / 1000L * efficiency);
		}
		return 0;
	}

	public void generate() {
		if(hasAcceptableFuel() && tank.getFill() > 0) {
			if(!shutUp) {
				if(soundCycle == 0) {
					this.worldObj.playSoundEffect(this.xCoord, this.yCoord, this.zCoord, "fireworks.blast", this.getVolume(1.0F), 0.5F);
				}
				soundCycle++;
				if(soundCycle >= 3)
					soundCycle = 0;
			}
			tank.setFill(tank.getFill() - this.pistonCount);
			if(tank.getFill() < 0)
				tank.setFill(0);
			power += getHEFromFuel() * Math.pow(this.pistonCount, pistonExp);
			if(power > powerCap)
				power = powerCap;
		}
	}

	@Override
	public long getPower() {
		return power;
	}

	@Override
	public void setPower(long power) {
		this.power = power;
	}

	@Override
	public long getMaxPower() {
		return powerCap;
	}

	@Override
	public boolean canConnect(ForgeDirection dir) {
		return dir == ForgeDirection.DOWN;
	}

	@Override
	public String getConfigName() {
		return "radialengine";
	}

	@Override
	public void readIfPresent(JsonObject obj) {
		maxPower = IConfigurableMachine.grab(obj, "L:powerCap", maxPower);
		fluidCap = IConfigurableMachine.grab(obj, "I:fuelCap", fluidCap);
		pistonExp = IConfigurableMachine.grab(obj, "D:pistonGenExponent", pistonExp);
		if(obj.has("D[:efficiency")) {
			JsonArray array = obj.get("D[:efficiency").getAsJsonArray();
			for(FuelGrade grade : FuelGrade.values()) {
				fuelEfficiency.put(grade, array.get(grade.ordinal()).getAsDouble());
			}
		}
		shutUp = IConfigurableMachine.grab(obj, "B:shutUp", shutUp);
	}

	@Override
	public void writeConfig(JsonWriter writer) throws IOException {
		writer.name("L:powerCap").value(maxPower);
		writer.name("I:fuelCap").value(fluidCap);
		writer.name("D:pistonGenExponent").value(pistonExp);
		String info = "Fuel grades in order: ";
		for(FuelGrade grade : FuelGrade.values()) info += grade.name() + " ";
		info = info.trim();
		writer.name("INFO").value(info);
		writer.name("D[:efficiency").beginArray().setIndent("");
		for(FuelGrade grade : FuelGrade.values()) {
			double d = fuelEfficiency.containsKey(grade) ? fuelEfficiency.get(grade) : 0.0D;
			writer.value(d);
		}
		writer.endArray().setIndent("  ");
		writer.name("B:shutUp").value(shutUp);
	}

	@Override
	public FluidTank[] getAllTanks() {
		return new FluidTank[]{tank};
	}

	@Override
	public FluidTank[] getReceivingTanks() {
		return new FluidTank[]{tank};
	}

	@Override
	public FluidTank[] getSendingTanks() {
		return new FluidTank[0];
	}

	@Override
	public Container provideContainer(int ID, EntityPlayer player, World world, int x, int y, int z) {
		return new ContainerMachineSelenium(player.inventory, this);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public Object provideGUI(int ID, EntityPlayer player, World world, int x, int y, int z) {
		return new GUIMachineSelenium(player.inventory, this);
	}
}
