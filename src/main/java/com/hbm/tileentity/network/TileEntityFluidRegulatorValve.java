package com.hbm.tileentity.network;

import java.util.ArrayList;
import java.util.List;

import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.tank.FluidTank;
import com.hbm.packet.PacketDispatcher;
import com.hbm.packet.toclient.AuxParticlePacketNT;
import com.hbm.tileentity.TilePort;
import com.hbm.tileentity.TilePort.PortDef;

import api.hbm.energymk2.IEnergyReceiverMK2.ConnectionPriority;
import api.hbm.fluidmk2.IFluidConnectorMK2;
import api.hbm.fluidmk2.IFluidPipeMK2;
import api.hbm.fluidmk2.IFluidProviderMK2;
import api.hbm.fluidmk2.IFluidReceiverMK2;
import api.hbm.fluidmk2.IFluidStandardReceiverMK2;
import api.hbm.fluidmk2.IFluidStandardSenderMK2;
import api.hbm.fluidmk2.IFluidUserMK2;
import cpw.mods.fml.common.network.NetworkRegistry.TargetPoint;
import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.MathHelper;
import net.minecraftforge.common.util.ForgeDirection;

public class TileEntityFluidRegulatorValve extends TileEntityPipeBaseNT
		implements IFluidReceiverMK2, IFluidStandardSenderMK2 {

	public static final int[] LEVELS = new int[] { 1, 10, 20, 30, 40, 50, 60, 70, 80, 90, 99 };
	public int levelIndex = 5; // default 50%

	// Client-side synced data for HUD overlay
	public int clientTankMax = 0;
	private TilePort regulatorPort;
	private ForgeDirection portTankDirection = ForgeDirection.UNKNOWN;

	public int getTargetPercentage() {
		return LEVELS[MathHelper.clamp_int(levelIndex, 0, LEVELS.length - 1)];
	}

	public long getTargetAmount(int capacity) {
		return (long) capacity * getTargetPercentage() / 100L;
	}

	@Override
	public boolean shouldCreateNode() {
		return false;
	}

	public void cycleThreshold(boolean backwards) {
		int nextIndex = MathHelper.clamp_int(levelIndex + (backwards ? -1 : 1), 0, LEVELS.length - 1);
		if (nextIndex == levelIndex) return;
		levelIndex = nextIndex;
		worldObj.playSoundEffect(xCoord + 0.5, yCoord + 0.5, zCoord + 0.5, "hbm:item.screwdriver", 1.0F, 1.0F);
		this.markDirty();
		this.networkPackNT(25);
	}

	public static class TankRef {
		public TileEntity tile;
		public ForgeDirection dir;
		public FluidTank tank;
	}

	public List<TankRef> getConnectedTanks() {
		List<TankRef> list = new ArrayList<>();
		if (worldObj == null || this.type == Fluids.NONE)
			return list;

		for (ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS) {
			TileEntity te = worldObj.getTileEntity(xCoord + dir.offsetX, yCoord + dir.offsetY, zCoord + dir.offsetZ);
			if (te == null || te == this)
				continue;
			if (te instanceof IFluidPipeMK2)
				continue;

			if (te instanceof IFluidStandardReceiverMK2 || te instanceof IFluidStandardSenderMK2) {
				FluidTank[] tanks = ((IFluidUserMK2) te).getAllTanks();
				if (tanks == null) continue;
				for (FluidTank tank : tanks) {
					if (tank == null || tank.getTankType() != this.type) continue;
					TankRef ref = new TankRef();
					ref.tile = te;
					ref.dir = dir;
					ref.tank = tank;
					if (canAccess(ref, this.type)) list.add(ref);
					break;
				}
			}

		}

		return list;
	}

	public TankRef getTankRef() {
		List<TankRef> list = getConnectedTanks();
		return list.isEmpty() ? null : list.get(0);
	}

	public FluidType getRegulatedType(TankRef ref) {
		if (ref == null || ref.tank.getTankType() != this.type) return Fluids.NONE;
		return this.type;
	}

	private boolean canAccess(TankRef ref, FluidType type) {
		return !(ref.tile instanceof IFluidConnectorMK2)
				|| ((IFluidConnectorMK2) ref.tile).canConnect(type, ref.dir.getOpposite());
	}

	private boolean containsTank(FluidTank[] tanks, FluidTank target) {
		if (tanks != null) for (FluidTank tank : tanks) if (tank == target) return true;
		return false;
	}

	private boolean permitsTransfer(TankRef ref, FluidType type, int pressure, boolean receiving) {
		if (ref == null || ref.tank.getMaxFill() <= 0 || type == Fluids.NONE || type != getRegulatedType(ref)
				|| pressure != ref.tank.getPressure() || !canAccess(ref, type)) return false;
		return receiving
				? ref.tile instanceof IFluidStandardReceiverMK2 && containsTank(((IFluidStandardReceiverMK2) ref.tile).getReceivingTanks(), ref.tank)
				: ref.tile instanceof IFluidStandardSenderMK2 && ref.tank.getTankType() == type && containsTank(((IFluidStandardSenderMK2) ref.tile).getSendingTanks(), ref.tank);
	}

	@Override
	public void updateEntity() {
		super.updateEntity();
		if (!worldObj.isRemote) {
			List<TankRef> tanks = getConnectedTanks();

			// If placed between 2 or more tanks, break itself and drop as an item
			if (tanks.size() >= 2) {
				this.explode();
				return;
			}

			updateRegulatorPort(tanks.isEmpty() ? null : tanks.get(0));

			if (worldObj.getTotalWorldTime() % 10 == 0) {
				this.networkPackNT(25);
			}
		}
	}

	private void explode() {
		worldObj.playSoundEffect(xCoord + 0.5, yCoord + 0.5, zCoord + 0.5, "hbm:block.fart_reverb", 1.0F, 1.0F);

		NBTTagCompound data = new NBTTagCompound();
		data.setString("type", "vanillaExt");
		data.setString("mode", "largeexplode");
		data.setFloat("size", 4.0F);
		data.setByte("count", (byte) 4);
		PacketDispatcher.wrapper.sendToAllAround(
			new AuxParticlePacketNT(data, xCoord + 0.5, yCoord + 0.5, zCoord + 0.5),
			new TargetPoint(worldObj.provider.dimensionId, xCoord + 0.5, yCoord + 0.5, zCoord + 0.5, 100)
		);

		worldObj.func_147480_a(xCoord, yCoord, zCoord, true);
	}

	private void updateRegulatorPort(TankRef ref) {
		ForgeDirection tankDirection = ref == null ? ForgeDirection.UNKNOWN : ref.dir;
		if (regulatorPort == null || portTankDirection != tankDirection) {
			destroyRegulatorPort();
			List<ForgeDirection> directions = new ArrayList<>();
			for (ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS) {
				if (dir != tankDirection) directions.add(dir);
			}
			regulatorPort = new TilePort().setupOwner(this).setupPort(PortDef.make(xCoord, yCoord, zCoord,
					directions.toArray(new ForgeDirection[0])));
			portTankDirection = tankDirection;
		}

		FluidType regType = getRegulatedType(ref);
		TilePort port = regulatorPort;
		port.setupType(regType.getNetworkProvider());
		port.update(worldObj);
		// Register both roles persistently; demand and availability enforce the threshold.
		port.checkSubscribe(worldObj);
		port.checkProvide(worldObj);
	}

	private void destroyRegulatorPort() {
		if (regulatorPort != null) {
			regulatorPort.disableIfPresent(worldObj);
			regulatorPort = null;
		}
	}

	@Override
	public void invalidate() {
		super.invalidate();
		if (destroyPortsOnInvalidate) destroyRegulatorPort();
	}

	@Override
	public void onChunkUnload() {
		super.onChunkUnload();
		destroyRegulatorPort();
	}

	@Override
	public boolean canConnect(FluidType type, ForgeDirection dir) {
		if (dir == ForgeDirection.UNKNOWN) return false;
		TankRef ref = getTankRef();
		if (ref != null && dir == ref.dir) {
			return false; // Isolate the tank from direct pipe network bridging
		}
		FluidType regType = getRegulatedType(ref);
		return regType != Fluids.NONE && type == regType;
	}

	// ===== IFluidReceiverMK2 (Inflow when tank < target) =====

	private long getDemand(TankRef ref, FluidType type, int pressure) {
		if (!permitsTransfer(ref, type, pressure, true)) return 0;
		IFluidReceiverMK2 receiver = (IFluidReceiverMK2) ref.tile;
		long needed = getTargetAmount(ref.tank.getMaxFill()) - ref.tank.getFill();
		return Math.max(0, Math.min(needed, Math.min(receiver.getDemand(type, pressure), receiver.getReceiverSpeed(type, pressure))));
	}

	@Override
	public long getDemand(FluidType type, int pressure) {
		return getDemand(getTankRef(), type, pressure);
	}

	@Override
	public long getReceiverSpeed(FluidType type, int pressure) {
		TankRef ref = getTankRef();
		return permitsTransfer(ref, type, pressure, true)
				? Math.max(0, ((IFluidReceiverMK2) ref.tile).getReceiverSpeed(type, pressure)) : 0;
	}

	@Override
	public long transferFluid(FluidType type, int pressure, long amount) {
		if (amount <= 0) return amount;
		TankRef ref = getTankRef();
		long toAccept = Math.min(amount, getDemand(ref, type, pressure));
		if (toAccept <= 0) return amount;

		long remainder = ((IFluidReceiverMK2) ref.tile).transferFluid(type, pressure, toAccept);
		long accepted = toAccept - remainder;
		if (accepted > 0) ref.tile.markDirty();
		return amount - accepted;
	}

	@Override
	public int[] getReceivingPressureRange(FluidType type) {
		TankRef ref = getTankRef();
		if (ref != null) {
			int pressure = ref.tank.getPressure();
			return new int[] { pressure, pressure };
		}
		return DEFAULT_PRESSURE_RANGE;
	}

	@Override
	public ConnectionPriority getFluidPriority() {
		TankRef ref = getTankRef();
		if (ref != null && ref.tile instanceof IFluidReceiverMK2) {
			return ((IFluidReceiverMK2) ref.tile).getFluidPriority();
		}
		return ConnectionPriority.NORMAL;
	}

	// ===== IFluidProviderMK2 (Outflow when tank > target) =====

	@Override
	public FluidTank[] getAllTanks() {
		TankRef ref = getTankRef();
		if (ref != null) {
			return new FluidTank[] { ref.tank };
		}
		return new FluidTank[0];
	}

	@Override
	public FluidTank[] getSendingTanks() {
		return new FluidTank[0];
	}

	private long getFluidAvailable(TankRef ref, FluidType type, int pressure) {
		if (!permitsTransfer(ref, type, pressure, false)) return 0;
		IFluidProviderMK2 provider = (IFluidProviderMK2) ref.tile;
		long excess = ref.tank.getFill() - getTargetAmount(ref.tank.getMaxFill());
		return Math.max(0, Math.min(excess, Math.min(provider.getFluidAvailable(type, pressure), provider.getProviderSpeed(type, pressure))));
	}

	@Override
	public long getFluidAvailable(FluidType type, int pressure) {
		return getFluidAvailable(getTankRef(), type, pressure);
	}

	@Override
	public long getProviderSpeed(FluidType type, int pressure) {
		TankRef ref = getTankRef();
		return permitsTransfer(ref, type, pressure, false)
				? Math.max(0, ((IFluidProviderMK2) ref.tile).getProviderSpeed(type, pressure)) : 0;
	}

	@Override
	public void useUpFluid(FluidType type, int pressure, long amount) {
		if (amount <= 0) return;
		TankRef ref = getTankRef();
		long toRemove = Math.min(amount, getFluidAvailable(ref, type, pressure));
		if (toRemove <= 0) return;

		((IFluidProviderMK2) ref.tile).useUpFluid(type, pressure, toRemove);
		ref.tile.markDirty();
	}

	@Override
	public int[] getProvidingPressureRange(FluidType type) {
		TankRef ref = getTankRef();
		if (ref != null) {
			int pressure = ref.tank.getPressure();
			return new int[] { pressure, pressure };
		}
		return DEFAULT_PRESSURE_RANGE;
	}

	// ===== Network Sync & NBT =====

	@Override
	public void serialize(ByteBuf buf) {
		super.serialize(buf);
		buf.writeByte(this.levelIndex);
		buf.writeInt(this.type.getID());

		TankRef ref = getTankRef();
		buf.writeInt(ref != null ? ref.tank.getMaxFill() : 0);
	}

	@Override
	public void deserialize(ByteBuf buf) {
		super.deserialize(buf);
		this.levelIndex = buf.readByte();
		this.type = Fluids.fromID(buf.readInt());

		this.clientTankMax = buf.readInt();
	}

	@Override
	public void writeToNBT(NBTTagCompound nbt) {
		super.writeToNBT(nbt);
		nbt.setInteger("level", levelIndex);
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);
		if (nbt.hasKey("level")) levelIndex = MathHelper.clamp_int(nbt.getInteger("level"), 0, LEVELS.length - 1);
	}
}
