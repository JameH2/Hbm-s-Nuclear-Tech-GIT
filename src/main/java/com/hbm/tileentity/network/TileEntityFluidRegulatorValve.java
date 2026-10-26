package com.hbm.tileentity.network;

import java.util.ArrayList;
import java.util.List;

import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.tank.FluidTank;
import com.hbm.packet.PacketDispatcher;
import com.hbm.packet.toclient.AuxParticlePacketNT;
import com.hbm.tileentity.TileEntityProxyCombo;
import com.hbm.tileentity.TilePort;
import com.hbm.tileentity.TilePort.PortDef;
import com.hbm.tileentity.machine.storage.TileEntityBarrel;
import com.hbm.uninos.GenNode;
import com.hbm.uninos.UniNodespace;
import com.hbm.util.fauxpointtwelve.BlockPos;
import com.hbm.util.fauxpointtwelve.DirPos;

import api.hbm.energymk2.IEnergyReceiverMK2.ConnectionPriority;
import api.hbm.fluidmk2.IFluidConnectorMK2;
import api.hbm.fluidmk2.IFluidPipeMK2;
import api.hbm.fluidmk2.IFluidProviderMK2;
import api.hbm.fluidmk2.IFluidReceiverMK2;
import api.hbm.fluidmk2.IFluidStandardReceiverMK2;
import api.hbm.fluidmk2.IFluidStandardSenderMK2;
import api.hbm.fluidmk2.IFluidUserMK2;
import api.hbm.tile.ILoadedTile;
import cpw.mods.fml.common.network.NetworkRegistry.TargetPoint;
import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.MathHelper;
import net.minecraftforge.common.util.ForgeDirection;

public class TileEntityFluidRegulatorValve extends TileEntityPipeBaseNT
		implements IFluidReceiverMK2, IFluidStandardSenderMK2 {

	private static final int[] LEVELS = { 1, 10, 20, 30, 40, 50, 60, 70, 80, 90, 99 };

	public int levelIndex = 5;
	public int lastLevelIndex = 5;

	// Client-side synced data for HUD overlay
	public int clientTankMax = 0;
	private final TilePort regulatorPort = new TilePort().setupOwner(this);
	private ForgeDirection portTankDirection;
	private TankRef tankRef;

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
		this.markDirty();
		this.networkPackNT(25);
	}

	private static class TankRef {
		final TileEntity tile;
		final ForgeDirection dir;
		final FluidTank tank;

		private TankRef(TileEntity tile, ForgeDirection dir, FluidTank tank) {
			this.tile = tile;
			this.dir = dir;
			this.tank = tank;
		}
	}

	private List<TankRef> getConnectedTanks() {
		List<TankRef> list = new ArrayList<>();
		if (worldObj == null || this.type == Fluids.NONE)
			return list;

		for (ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS) {
			TileEntity tile = getAdjacentTile(dir);
			if (tile == null || tile == this || tile instanceof IFluidPipeMK2) continue;
			if (!(tile instanceof IFluidStandardReceiverMK2)
					&& !(tile instanceof IFluidStandardSenderMK2)) continue;

			FluidTank[] tanks = ((IFluidUserMK2) tile).getAllTanks();
			if (tanks == null) continue;
			for (FluidTank tank : tanks) {
				if (!isRegulatableTank(tile, tank)) continue;
				TankRef ref = new TankRef(tile, dir, tank);
				if (canAccess(ref, this.type) && !containsTankRef(list, ref)) list.add(ref);
				break;
			}
		}

		return list;
	}

	private TileEntity getAdjacentTile(ForgeDirection dir) {
		TileEntity tile = worldObj.getTileEntity(xCoord + dir.offsetX, yCoord + dir.offsetY, zCoord + dir.offsetZ);
		if (tile != null && tile.isInvalid()) return null;
		if (tile instanceof ILoadedTile && !((ILoadedTile) tile).isLoaded()) return null;
		if (!(tile instanceof TileEntityProxyCombo)) return tile;

		TileEntityProxyCombo proxy = (TileEntityProxyCombo) tile;
		if (!proxy.canConnect(this.type, dir.getOpposite())) return null;

		TileEntity core = proxy.getTile();
		if (!(core instanceof TileEntityBarrel)) return null;
		if (!isStoragePortConnected((TileEntityBarrel) core, proxy, dir)) return null;
		return core;
	}

	private boolean isRegulatableTank(TileEntity tile, FluidTank tank) {
		return tile instanceof TileEntityBarrel && tank != null && tank.getTankType() == this.type && hasIsolatedTransfers(tile, tank);
	}

	private boolean isStoragePortConnected(TileEntityBarrel core, TileEntityProxyCombo proxy, ForgeDirection dir) {
		PortDef[] ports = core.getPorts();
		if (ports == null) return false;
		for (PortDef port : ports) {
			if (port == null || port.portPositions == null || port.portConnections == null) continue;
			boolean containsProxy = false;
			for (BlockPos pos : port.portPositions) {
				if (pos.compare(proxy.xCoord, proxy.yCoord, proxy.zCoord)) {
					containsProxy = true;
					break;
				}
			}
			if (!containsProxy) continue;
			for (DirPos connection : port.portConnections) {
				if (connection.compare(xCoord, yCoord, zCoord) && connection.getDir() == dir.getOpposite()) return true;
			}
		}
		return false;
	}

	private boolean containsTankRef(List<TankRef> refs, TankRef target) {
		for (TankRef ref : refs) {
			if (ref.tile == target.tile && ref.tank == target.tank) return true;
		}
		return false;
	}

	private TankRef getTankRef() {
		if (worldObj != null && worldObj.isRemote) {
			List<TankRef> tanks = getConnectedTanks();
			return tanks.isEmpty() ? null : tanks.get(0);
		}
		return tankRef;
	}

	private boolean isTankConnected(TankRef ref) {
		if (worldObj == null || isInvalid() || ref.tile.isInvalid()) return false;
		if (ref.tile instanceof ILoadedTile && !((ILoadedTile) ref.tile).isLoaded()) return false;
		if (!worldObj.blockExists(ref.tile.xCoord, ref.tile.yCoord, ref.tile.zCoord)
				|| !worldObj.blockExists(xCoord + ref.dir.offsetX, yCoord + ref.dir.offsetY, zCoord + ref.dir.offsetZ)) return false;
		return worldObj.getTileEntity(ref.tile.xCoord, ref.tile.yCoord, ref.tile.zCoord) == ref.tile
				&& getAdjacentTile(ref.dir) == ref.tile;
	}

	private FluidType getRegulatedType(TankRef ref) {
		if (ref == null || ref.tank.getTankType() != this.type) return Fluids.NONE;
		return this.type;
	}

	private boolean canAccess(TankRef ref, FluidType type) {
		return ((IFluidConnectorMK2) ref.tile).canConnect(type, ref.dir.getOpposite());
	}

	private boolean containsTank(FluidTank[] tanks, FluidTank target) {
		if (tanks != null) for (FluidTank tank : tanks) if (tank == target) return true;
		return false;
	}

	private boolean hasOtherMatchingTank(FluidTank[] tanks, FluidTank target) {
		if (tanks != null) for (FluidTank tank : tanks) {
			if (tank != null && tank != target && tank.getTankType() == target.getTankType()
					&& tank.getPressure() == target.getPressure()) return true;
		}
		return false;
	}

	private boolean hasIsolatedTransfers(TileEntity tile, FluidTank tank) {
		return (!(tile instanceof IFluidStandardReceiverMK2)
				|| !hasOtherMatchingTank(((IFluidStandardReceiverMK2) tile).getReceivingTanks(), tank))
				&& (!(tile instanceof IFluidStandardSenderMK2)
				|| !hasOtherMatchingTank(((IFluidStandardSenderMK2) tile).getSendingTanks(), tank));
	}

	private boolean hasOtherTankConnection(TankRef ref) {
		if (ref == null || !(ref.tile instanceof TileEntityBarrel)) return false;
		PortDef[] ports = ((TileEntityBarrel) ref.tile).getPorts();
		if (ports == null) return false;
		for (PortDef port : ports) {
			if (port == null || port.portConnections == null) continue;
			for (DirPos connection : port.portConnections) {
				if (connection == null || connection.compare(xCoord, yCoord, zCoord)) continue;
				TileEntity tile = worldObj.getTileEntity(connection.getX(), connection.getY(), connection.getZ());
				if (tile instanceof TileEntityFluidRegulatorValve) {
					TileEntityFluidRegulatorValve other = (TileEntityFluidRegulatorValve) tile;
					if (!other.isInvalid() && other.isLoaded() && other.type == this.type
							&& other.getAdjacentTile(connection.getDir().getOpposite()) == ref.tile) return true;
				} else if (tile instanceof IFluidConnectorMK2 && !tile.isInvalid()
						&& (!(tile instanceof ILoadedTile) || ((ILoadedTile) tile).isLoaded())
						&& ((IFluidConnectorMK2) tile).canConnect(this.type, connection.getDir().getOpposite())) {
					return true;
				}
				GenNode<?> node = UniNodespace.getNode(worldObj, connection.getX(), connection.getY(), connection.getZ(),
						this.type.getNetworkProvider());
				if (node != null && !node.expired && UniNodespace.checkConnection(node, connection, false)) return true;
			}
		}
		return false;
	}

	private boolean permitsTransfer(TankRef ref, FluidType type, int pressure, boolean receiving) {
		if (ref == null || ref.tank.getMaxFill() <= 0 || type == Fluids.NONE || type != getRegulatedType(ref)
				|| pressure != ref.tank.getPressure() || !isTankConnected(ref) || !canAccess(ref, type)
				|| !hasIsolatedTransfers(ref.tile, ref.tank)) return false;
		return receiving
				? ref.tile instanceof IFluidStandardReceiverMK2 && containsTank(((IFluidStandardReceiverMK2) ref.tile).getReceivingTanks(), ref.tank)
				: ref.tile instanceof IFluidStandardSenderMK2 && containsTank(((IFluidStandardSenderMK2) ref.tile).getSendingTanks(), ref.tank);
	}

	@Override
	public void updateEntity() {
		super.updateEntity();
		if (worldObj.isRemote && lastLevelIndex != levelIndex) {
			worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
			lastLevelIndex = levelIndex;
		}
		if (!worldObj.isRemote) {
			List<TankRef> tanks = getConnectedTanks();

			if (tanks.size() >= 2) {
				tankRef = null;
				this.explode();
				return;
			}

			tankRef = tanks.isEmpty() ? null : tanks.get(0);

			if (hasOtherTankConnection(tankRef)) {
				tankRef = null;
				this.explode();
				return;
			}

			updateRegulatorPort(tankRef);

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
		if (portTankDirection != tankDirection) {
			List<ForgeDirection> directions = new ArrayList<>();
			for (ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS) {
				if (dir != tankDirection) directions.add(dir);
			}
			regulatorPort.setupPort(PortDef.make(xCoord, yCoord, zCoord,
					directions.toArray(new ForgeDirection[0])));
			portTankDirection = tankDirection;
		}

		regulatorPort.setupType(getRegulatedType(ref).getNetworkProvider());
		regulatorPort.update(worldObj);
		regulatorPort.checkSubscribe(worldObj);
		regulatorPort.checkProvide(worldObj);
	}

	@Override
	public void invalidate() {
		tankRef = null;
		super.invalidate();
		if (destroyPortsOnInvalidate) regulatorPort.disableIfPresent(worldObj);
	}

	@Override
	public void onChunkUnload() {
		tankRef = null;
		super.onChunkUnload();
		regulatorPort.disableIfPresent(worldObj);
	}

	@Override
	public boolean canConnect(FluidType type, ForgeDirection dir) {
		if (dir == ForgeDirection.UNKNOWN) return false;
		FluidType regType = getRegulatedType(getTankRef());
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
		return getTankPressureRange();
	}

	private int[] getTankPressureRange() {
		TankRef ref = getTankRef();
		if (ref == null) return DEFAULT_PRESSURE_RANGE;

		int pressure = ref.tank.getPressure();
		return new int[] { pressure, pressure };
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
		return getTankPressureRange();
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
