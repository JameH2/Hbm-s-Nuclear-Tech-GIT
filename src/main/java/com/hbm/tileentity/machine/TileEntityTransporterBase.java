package com.hbm.tileentity.machine;

import java.util.Arrays;
import java.util.stream.IntStream;

import com.hbm.interfaces.IControlReceiver;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.tank.FluidTank;
import com.hbm.items.tool.ItemTransporterLinker.TransporterInfo;
import com.hbm.packet.toserver.NBTControlPacket;
import com.hbm.packet.PacketDispatcher;
import com.hbm.tileentity.IGUIProvider;
import com.hbm.tileentity.TileEntityMachineBase;
import com.hbm.uninos.GenNode;
import com.hbm.uninos.UniNodespace;
import com.hbm.util.BufferUtil;
import com.hbm.util.Compat;
import com.hbm.util.InventoryUtil;
import com.hbm.util.fauxpointtwelve.DirPos;

import api.hbm.fluid.IFluidStandardTransceiver;
import api.hbm.fluidmk2.IFluidConnectorMK2;
import api.hbm.fluidmk2.IFluidReceiverMK2;
import api.hbm.fluidmk2.IFluidStandardReceiverMK2;
import api.hbm.fluidmk2.IFluidUserMK2;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.util.ForgeDirection;

public abstract class TileEntityTransporterBase extends TileEntityMachineBase implements IGUIProvider, IControlReceiver, IFluidStandardTransceiver {

	private String name = "Transporter";

	public FluidTank[] tanks;

	public TileEntityTransporterBase(int slotCount, int tankCount, int tankSize) {
		this(slotCount, tankCount, tankSize, 0, 0, 0);
	}

	public TileEntityTransporterBase(int slotCount, int tankCount, int tankSize, int extraSlots, int extraTanks, int extraTankSize) {
		super(slotCount + tankCount / 2 + extraSlots + extraTanks);

		tanks = new FluidTank[tankCount + extraTanks];
		for(int i = 0; i < tankCount; i++) {
			tanks[i] = new FluidTank(Fluids.NONE, tankSize);
		}
		for(int i = tankCount; i < tankCount + extraTanks; i++) {
			tanks[i] = new FluidTank(Fluids.NONE, extraTankSize);
		}

		inputSlotMax = slotCount / 2;
		outputSlotMax = slotCount;

		inputTankMax = tankCount / 2;
		outputTankMax = tankCount;
	}

	// The transporter we're sending our contents to
	protected TileEntityTransporterBase linkedTransporter;
	private TransporterInfo linkedTransporterInfo;

	private int inputSlotMax;
	private int outputSlotMax;

	private int inputTankMax;
	private int outputTankMax;

	@Override
	public String getName() {
		return "container.transporter";
	}

	@Override
	public void updateEntity() {
		if(worldObj.isRemote) return;

		// Set tank types and split fills
		for(int i = 0; i < inputTankMax; i++) {
			tanks[i].setType(outputSlotMax + i, slots);

			// Evenly distribute fluids between all matching tanks
			for(int o = i + 1; o < inputTankMax; o++) {
				splitFill(tanks[i], tanks[o]);
			}
		}
		for(int i = inputTankMax; i < outputTankMax; i++) {
			for(int o = i + 1; o < outputTankMax; o++) {
				splitFill(tanks[i], tanks[o]);
			}
		}
		for(int i = outputTankMax; i < tanks.length; i++) {
			tanks[i].setType(outputSlotMax + inputTankMax + i - outputTankMax, slots);
		}

		updateConnections();

		fetchLinkedTransporter();

		if(linkedTransporter != null && canSend(linkedTransporter)) {
			boolean isDirty = false;

			int sentItems = 0;
			int sentFluid = 0;

			// Move all items into the target
			for(int i = 0; i < inputSlotMax; i++) {
				if(slots[i] != null) {
					int beforeSize = slots[i].stackSize;
					slots[i] = InventoryUtil.tryAddItemToInventory(linkedTransporter.slots, linkedTransporter.inputSlotMax, linkedTransporter.outputSlotMax - 1, slots[i]);
					int afterSize = slots[i] != null ? slots[i].stackSize : 0;
					sentItems += beforeSize - afterSize;
					isDirty = true;
				}
			}

			// Move all fluids into the target
			for(int i = 0; i < inputTankMax; i++) {
				int o = i+inputTankMax;

				linkedTransporter.tanks[o].setTankType(tanks[i].getTankType());

				int sourceFillLevel = tanks[i].getFill();
				int targetFillLevel = linkedTransporter.tanks[o].getFill();

				int spaceAvailable = linkedTransporter.tanks[o].getMaxFill() - targetFillLevel;
				int amountToSend = Math.min(sourceFillLevel, spaceAvailable);

				if(amountToSend > 0) {
					linkedTransporter.tanks[o].setFill(targetFillLevel + amountToSend);
					tanks[i].setFill(sourceFillLevel - amountToSend);
					sentFluid += amountToSend;
					isDirty = true;
				}
			}



			hasSent(linkedTransporter, sentItems + (sentFluid / 1000));

			if(isDirty) {
				markChanged();
				linkedTransporter.markChanged();
			}
		}

		this.networkPackNT(250);
	}

	private void updateConnections() {
		// Sending/Receiving tanks
		for(DirPos pos : getConPos()) {
			for(int i = 0; i < outputTankMax; i++) {
				if(tanks[i].getTankType() != Fluids.NONE) {
					trySubscribe(tanks[i].getTankType(), worldObj, pos.getX(), pos.getY(), pos.getZ(), pos.getDir());
					this.sendFluid(tanks[i], worldObj, pos.getX(), pos.getY(), pos.getZ(), pos.getDir());
				}
			}
		}

		// Fuel tanks
		for(DirPos pos : getTankPos()) {
			for(int i = outputTankMax; i < tanks.length; i++) {
				if(tanks[i].getTankType() != Fluids.NONE) {
					trySubscribeFuel(tanks[i].getTankType(), worldObj, pos.getX(), pos.getY(), pos.getZ(), pos.getDir());
				}
			}
		}

		// Inserter
		for(DirPos pos : getInsertPos()) {
			tryLoad(pos.getX(), pos.getY(), pos.getZ(), pos.getDir());
		}

		// Extractor
		for(DirPos pos : getExtractPos()) {
			tryUnload(pos.getX(), pos.getY(), pos.getZ(), pos.getDir());
		}
	}

	private void tryLoad(int x, int y, int z, ForgeDirection dir) {
		TileEntity te = worldObj.getTileEntity(x, y, z);
		if(!(te instanceof IInventory)) return;

		IInventory inv = (IInventory) te;
		ISidedInventory sided = inv instanceof ISidedInventory ? (ISidedInventory) inv : null;
		int[] access = sided != null ? sided.getAccessibleSlotsFromSide(dir.ordinal()) : null;

		for(int i = 0; i < (access != null ? access.length : inv.getSizeInventory()); i++) {
			int slot = access != null ? access[i] : i;
			ItemStack stack = inv.getStackInSlot(slot);
			if(stack != null && (sided == null || sided.canExtractItem(slot, stack, dir.ordinal()))) {
				for(int j = 0; j < inputSlotMax; j++) {
					if(slots[j] != null && slots[j].stackSize < slots[j].getMaxStackSize() & InventoryUtil.doesStackDataMatch(slots[j], stack)) {
						inv.decrStackSize(slot, 1);
						slots[j].stackSize++;
						return;
					}
				}

				for(int j = 0; j < inputSlotMax; j++) {
					if(slots[j] == null) {
						slots[j] = stack.copy();
						slots[j].stackSize = 1;
						inv.decrStackSize(slot, 1);
						return;
					}
				}
			}
		}
	}

	private void tryUnload(int x, int y, int z, ForgeDirection dir) {
		TileEntity te = worldObj.getTileEntity(x, y, z);
		if(!(te instanceof IInventory)) return;

		IInventory inv = (IInventory) te;
		ISidedInventory sided = inv instanceof ISidedInventory ? (ISidedInventory) inv : null;
		int[] access = sided != null ? sided.getAccessibleSlotsFromSide(dir.ordinal()) : null;

		for(int i = inputSlotMax; i < outputSlotMax; i++) {
			ItemStack out = slots[i];

			if(out != null) {
				for(int j = 0; j < (access != null ? access.length : inv.getSizeInventory()); j++) {
					int slot = access != null ? access[j] : j;

					if(!inv.isItemValidForSlot(slot, out))
						continue;

					ItemStack target = inv.getStackInSlot(slot);

					if(InventoryUtil.doesStackDataMatch(out, target) && target.stackSize < Math.min(target.getMaxStackSize(), inv.getInventoryStackLimit())) {
						this.decrStackSize(i, 1);
						target.stackSize++;
						return;
					}
				}

				for(int j = 0; j < (access != null ? access.length : inv.getSizeInventory()); j++) {
					int slot = access != null ? access[j] : j;

					if(!inv.isItemValidForSlot(slot, out))
						continue;

					if(inv.getStackInSlot(slot) == null && (sided != null ? sided.canInsertItem(slot, out, dir.ordinal()) : inv.isItemValidForSlot(slot, out))) {
						ItemStack copy = out.copy();
						copy.stackSize = 1;
						inv.setInventorySlotContents(slot, copy);
						this.decrStackSize(i, 1);
						return;
					}
				}
			}
		}
	}

	public void trySubscribeFuel(FluidType type, World world, int x, int y, int z, ForgeDirection dir) {
		fuelReceiver.trySubscribe(type, world, x, y, z, dir);
	}

	FuelReceiver fuelReceiver = new FuelReceiver();

	private class FuelReceiver implements IFluidStandardReceiverMK2 {

		boolean valid = true;

		@Override
		public boolean isLoaded() {
			return valid && TileEntityTransporterBase.this.isLoaded();
		}

		@Override
		public FluidTank[] getAllTanks() {
			return TileEntityTransporterBase.this.getAllTanks();
		}

		@Override
		public FluidTank[] getReceivingTanks() {
			return (FluidTank[]) Arrays.copyOfRange(tanks, outputTankMax, tanks.length);
		}

	}

	@Override
	public void invalidate() {
		super.invalidate();
		fuelReceiver.valid = false;
	}

	// splitting is commutative, order don't matter
	private void splitFill(FluidTank in, FluidTank out) {
		if(in.getTankType() == out.getTankType()) {
			int fill = in.getFill() + out.getFill();

			float iFill = in.getMaxFill();
			float oFill = out.getMaxFill();
			float total = iFill + oFill;
			float iFrac = iFill / total;
			float oFrac = oFill / total;

			in.setFill(MathHelper.ceiling_float_int(iFrac * (float)fill));
			out.setFill(MathHelper.floor_double(oFrac * (float)fill));

			// cap filling (this will generate 1mB of fluid in rare cases)
			if(out.getFill() == out.getMaxFill() - 1) out.setFill(out.getMaxFill());
		}
	}

	@Override
	public FluidTank[] getSendingTanks() {
		return (FluidTank[]) Arrays.copyOfRange(tanks, inputTankMax, outputTankMax);
	}

	@Override
	public FluidTank[] getReceivingTanks() {
		return (FluidTank[]) Arrays.copyOfRange(tanks, 0, inputTankMax);
	}

	@Override
	public FluidTank[] getAllTanks() {
		return tanks;
	}

	@Override
	public void serialize(ByteBuf buf) {
		super.serialize(buf);

		if(linkedTransporterInfo != null) {
			buf.writeBoolean(true);
			buf.writeInt(linkedTransporterInfo.dimensionId);
			buf.writeInt(linkedTransporterInfo.x);
			buf.writeInt(linkedTransporterInfo.y);
			buf.writeInt(linkedTransporterInfo.z);
		} else {
			buf.writeBoolean(false);
		}

		for(int i = 0; i < tanks.length; i++) tanks[i].serialize(buf);

		BufferUtil.writeString(buf, name);
	}

	@Override
	public void deserialize(ByteBuf buf) {
		super.deserialize(buf);

		linkedTransporter = null;
		if(buf.readBoolean()) {
			int id = buf.readInt();
			int x = buf.readInt();
			int y = buf.readInt();
			int z = buf.readInt();
			linkedTransporterInfo = new TransporterInfo("Linked Transporter", id, x, y, z);
		} else {
			linkedTransporterInfo = null;
		}

		for(int i = 0; i < tanks.length; i++) tanks[i].deserialize(buf);

		name = BufferUtil.readString(buf);
	}

	protected abstract DirPos[] getConPos();
	protected abstract DirPos[] getTankPos();
	protected abstract DirPos[] getInsertPos();
	protected abstract DirPos[] getExtractPos();

	// Designated overrides for delaying sending or requiring fuel
	protected abstract boolean canSend(TileEntityTransporterBase linkedTransporter);
	protected abstract void hasSent(TileEntityTransporterBase linkedTransporter, int quantitySent);
	protected abstract void hasConnected(TileEntityTransporterBase linkedTransporter);

	// Turns items and fluids into a "mass" of sorts
	protected int itemCount() {
		int count = 0;
		for(int i = 0; i < inputSlotMax; i++) {
			if(slots[i] != null) count += slots[i].stackSize;
		}
		for(int i = 0; i < inputTankMax; i++) {
			count += tanks[i].getFill() / 1000;
		}
		return count;
	}

	public String getTransporterName() {
		return name;
	}

	public void setTransporterName(String name) {
		this.name = name;
		NBTTagCompound data = new NBTTagCompound();
		data.setString("name", name);
		PacketDispatcher.wrapper.sendToServer(new NBTControlPacket(data, xCoord, yCoord, zCoord));
	}

	private void fetchLinkedTransporter() {
		if(linkedTransporter == null && linkedTransporterInfo != null) {
			World transporterWorld = DimensionManager.getWorld(linkedTransporterInfo.dimensionId);
			TileEntity te = transporterWorld.getTileEntity(linkedTransporterInfo.x, linkedTransporterInfo.y, linkedTransporterInfo.z);
			if(te != null && te instanceof TileEntityTransporterBase) {
				linkedTransporter = (TileEntityTransporterBase) te;
			}
		}
	}

	public TransporterInfo getLinkedTransporter() {
		return linkedTransporterInfo;
	}

	@Override
	public int[] getAccessibleSlotsFromSide(int side) {
		return IntStream.range(0, outputSlotMax).toArray();
	}

	@Override
	public boolean isItemValidForSlot(int i, ItemStack itemStack) {
		return i < inputSlotMax;
	}

	@Override
	public boolean canExtractItem(int i, ItemStack itemStack, int side) {
		return i >= inputSlotMax && i < outputSlotMax;
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);
		name = nbt.getString("name");
		linkedTransporter = null;
		int dimensionId = nbt.getInteger("dimensionId");
		int[] coords = nbt.getIntArray("linkedTo");
		if(coords.length > 0) {
			linkedTransporterInfo = new TransporterInfo("Linked Transporter", dimensionId, coords[0], coords[1], coords[2]);
		} else {
			linkedTransporterInfo = null;
		}
		for(int i = 0; i < tanks.length; i++) tanks[i].readFromNBT(nbt, "t" + i);
	}

	@Override
	public void writeToNBT(NBTTagCompound nbt) {
		super.writeToNBT(nbt);
		nbt.setString("name", name);
		if(linkedTransporterInfo != null) {
			nbt.setInteger("dimensionId", linkedTransporterInfo.dimensionId);
			nbt.setIntArray("linkedTo", new int[] { linkedTransporterInfo.x, linkedTransporterInfo.y, linkedTransporterInfo.z });
		}
		for(int i = 0; i < tanks.length; i++) tanks[i].writeToNBT(nbt, "t" + i);
	}

	public void unlinkTransporter() {
		if(linkedTransporter != null) {
			linkedTransporter.linkedTransporter = null;
			linkedTransporter.linkedTransporterInfo = null;
		}

		linkedTransporter = null;
		linkedTransporterInfo = null;
	}

	// Is commutative, will automatically link and unlink its pair
	@Override
	public void receiveControl(NBTTagCompound nbt) {
		if(nbt.hasKey("name")) name = nbt.getString("name");
		if(nbt.hasKey("unlink")) {
			unlinkTransporter();
		}
		if(nbt.hasKey("linkedTo")) {
			// If already linked, unlink the target
			if(linkedTransporter != null) {
				linkedTransporter.linkedTransporter = null;
				linkedTransporter.linkedTransporterInfo = null;
			}

			linkedTransporter = null;

			int[] coords = nbt.getIntArray("linkedTo");
			int dimensionId = nbt.getInteger("dimensionId");
			linkedTransporterInfo = new TransporterInfo("Linked Transporter", dimensionId, coords[0], coords[1], coords[2]);

			fetchLinkedTransporter();

			if(linkedTransporter != null) {
				linkedTransporter.linkedTransporterInfo = TransporterInfo.from(worldObj.provider.dimensionId, this);
				linkedTransporter.fetchLinkedTransporter();

				hasConnected(linkedTransporter);
			}
		}

		this.markDirty();
	}

	@Override
	public boolean hasPermission(EntityPlayer player) {
		return this.isUseableByPlayer(player);
	}

}
