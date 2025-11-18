package com.hbm.tileentity.machine;

import java.util.ArrayList;
import java.util.List;

import com.hbm.dim.CelestialBody;
import com.hbm.dim.SolarSystem;
import com.hbm.dim.orbit.OrbitalStation;
import com.hbm.entity.missile.EntityRideableRocket;
import com.hbm.entity.missile.EntityRideableRocket.RocketState;
import com.hbm.handler.RocketStruct;
import com.hbm.interfaces.IControlReceiver;
import com.hbm.inventory.SlotRocket.IStage;
import com.hbm.inventory.container.ContainerOrbitalStationLauncher;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.tank.FluidTank;
import com.hbm.inventory.gui.GUIOrbitalStationLauncher;
import com.hbm.items.ISatChip;
import com.hbm.items.ItemVOTVdrive;
import com.hbm.items.ItemVOTVdrive.Target;
import com.hbm.items.weapon.ItemCustomRocket;
import com.hbm.items.ModItems;
import com.hbm.tileentity.IGUIProvider;
import com.hbm.tileentity.TileEntityMachineBase;
import com.hbm.tileentity.bomb.TileEntityLaunchPadRocket;
import com.hbm.tileentity.bomb.TileEntityLaunchPadRocket.SolidFuelTank;
import com.hbm.util.fauxpointtwelve.DirPos;

import api.hbm.fluidmk2.IFluidStandardReceiverMK2;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

public class TileEntityOrbitalStationLauncher extends TileEntityMachineBase implements IGUIProvider, IControlReceiver, IStage, IFluidStandardReceiverMK2 {

	public RocketStruct rocket;

	private OrbitalStation station;
	private EntityRideableRocket docked;

	public FluidTank[] tanks;
	public SolidFuelTank solidFuel = new SolidFuelTank();

	public float rot;
	public float prevRot;

	public int currentStage;

	public boolean isBreaking;

	// Client synced state information
	public boolean hasDocked = false;
	public boolean hasRider = false;

	public TileEntityOrbitalStationLauncher() {
		// launch:			drive + fuel in + fuel out
		// construction:	capsule + stages + program drives
		super(
			1 + 1 + 1 +
			1 + RocketStruct.MAX_STAGES * 3 + RocketStruct.MAX_STAGES * 2
		);

		tanks = new FluidTank[RocketStruct.MAX_STAGES * 2]; // enough tanks for any combination of rocket stages
		for(int i = 0; i < tanks.length; i++) tanks[i] = new FluidTank(Fluids.NONE, 64_000);
	}

	@Override
	public String getName() {
		return "container.orbitalStationLauncher";
	}

	@Override
	public void updateEntity() {
		if(!CelestialBody.inOrbit(worldObj)) return;

		if(!worldObj.isRemote) {
			// This TE acts almost entirely like a port, except doesn't register itself so nothing actually tries to dock here
			station = OrbitalStation.getStationFromPosition(xCoord, zCoord);
			if(station != null) {

			}


			// ROCKET CONSTRUCTION //
			// Setup the constructed rocket
			ItemStack fromStack = slots[slots.length - (RocketStruct.MAX_STAGES - currentStage) * 2];
			ItemStack toStack = slots[slots.length - (RocketStruct.MAX_STAGES - currentStage) * 2 + 1];

			// updates the orbital station information and syncs it to the client, if necessary
			ItemVOTVdrive.getTarget(slots[0], worldObj);
			ItemVOTVdrive.getTarget(fromStack, worldObj);
			ItemVOTVdrive.getTarget(toStack, worldObj);

			rocket = new RocketStruct(slots[3]);
			if(slots[3] != null && slots[3].getItem() instanceof ISatChip) {
				rocket.satFreq = ISatChip.getFreqS(slots[3]);
			}
			for(int i = 4; i < RocketStruct.MAX_STAGES * 3 + 3; i += 3) {
				if(slots[i] == null && slots[i+1] == null && slots[i+2] == null) {
					// Check for later stages and shift them up into empty stages
					if(i + 3 < RocketStruct.MAX_STAGES * 3 && (slots[i+3] != null || slots[i+4] != null || slots[i+5] != null)) {
						slots[i] = slots[i+3];
						slots[i+1] = slots[i+4];
						slots[i+2] = slots[i+5];
						slots[i+3] = null;
						slots[i+4] = null;
						slots[i+5] = null;
					} else {
						break;
					}
				}
				rocket.addStage(slots[i], slots[i+1], slots[i+2]);
			}


			// ROCKET LAUNCHING
			updateTanks();

			// Connections
			if(worldObj.getTotalWorldTime() % 20 == 0) {
				for(DirPos pos : getConPos()) {
					// trySubscribe(worldObj, pos.getX(), pos.getY(), pos.getZ(), pos.getDir());

					if(rocket.validate()) {
						for(FluidTank tank : tanks) {
							if(tank.getTankType() == Fluids.NONE) continue;
							trySubscribe(tank.getTankType(), worldObj, pos);
						}
					}
				}
			}

			for(FluidTank tank : tanks) tank.loadTank(1, 2, slots);
			if(slots[1] != null && slots[1].getItem() == ModItems.rocket_fuel && solidFuel.level < solidFuel.max) {
				decrStackSize(1, 1);
				solidFuel.level += 250;
				if(solidFuel.level > solidFuel.max) solidFuel.level = solidFuel.max;
			}

			if(docked != null && (docked.isDead || docked.getState() == RocketState.UNDOCKING)) {
				undockRocket();
			}

			hasDocked = docked != null;
			hasRider = hasDocked && docked.riddenByEntity != null;

			networkPackNT(250);
		} else {
			prevRot = rot;
			if(hasDocked) {
				rot += 2.25F;
				if(rot > 90) rot = 90;
			} else {
				rot -= 2.25F;
				if(rot < 0) rot = 0;
			}
		}
	}

	public void enterCapsule(EntityPlayer player) {
		if(docked == null || docked.riddenByEntity != null) return;
		docked.interactFirst(player);
	}

	public void dockRocket(EntityRideableRocket rocket) {
		docked = rocket;
	}

	public void undockRocket() {
		docked = null;
	}

	private boolean hasDrive() {
		return slots[0] != null && slots[0].getItem() instanceof ItemVOTVdrive;
	}

	private boolean areTanksFull() {
		for(FluidTank tank : tanks) if(tank.getTankType() != Fluids.NONE && tank.getFill() < tank.getMaxFill()) return false;
		if(solidFuel.level < solidFuel.max) return false;
		return true;
	}

	private boolean canReachDestination() {
		// Check that the drive is processed
		if(!ItemVOTVdrive.getProcessed(slots[0])) {
			return false;
		}

		SolarSystem.Body target = ItemVOTVdrive.getDestination(slots[0]).body;
		if(target == SolarSystem.Body.ORBIT && rocket.capsule.part != ModItems.rp_capsule_20 && rocket.capsule.part != ModItems.rp_station_core_20)
		return false;

		Target from = CelestialBody.getTarget(worldObj, xCoord, zCoord);
		Target to = ItemVOTVdrive.getTarget(slots[0], worldObj);

		if(!to.isValid && rocket.capsule.part != ModItems.rp_station_core_20) return false;
		if(to.isValid && rocket.capsule.part == ModItems.rp_station_core_20) return false;

		// Check if the stage can make the journey
		return rocket.hasSufficientFuel(from.body, to.body, from.inOrbit, to.inOrbit);
	}

	public boolean canLaunch() {
		return rocket.validate() && hasDrive() && areTanksFull() && canReachDestination();
	}

	public void launch(EntityPlayer player) {
		// if(docked != null) {
		// 	enterCapsule(player);
		// 	return;
		// }

		if(!canLaunch()) return;

		ItemStack stack = ItemCustomRocket.build(rocket);

		EntityRideableRocket rocket = new EntityRideableRocket(worldObj, xCoord + 0.5F, yCoord + 1.5F, zCoord + 0.5F, stack).withProgram(slots[0]).launchedBy(player);
		rocket.posY -= rocket.height;
		worldObj.spawnEntityInWorld(rocket);

		// Deplete all fills
		for(int i = 0; i < tanks.length; i++) tanks[i] = new FluidTank(Fluids.NONE, 64_000);
		solidFuel.level = solidFuel.max = 0;

		// power -= maxPower * 0.75;

		slots[0] = null;
		for(int i = 3; i < slots.length; i++) slots[i] = null;

		dockRocket(rocket);

		// if(rocket.canRide()) enterCapsule(player);
	}

	@Override
	public boolean isItemValidForSlot(int index, ItemStack stack) {
		if(stack == null) return true;
		if(index == 0 && !(stack.getItem() instanceof ItemVOTVdrive)) return false;
		return true;
	}

	private void updateTanks() {
		if(!rocket.validate()) return;
		TileEntityLaunchPadRocket.updateStorageTanks(rocket, tanks, solidFuel, false);
	}

	public DirPos[] getConPos() {
		return new DirPos[] {
			new DirPos(xCoord - 1, yCoord + 1, zCoord + 3, ForgeDirection.NORTH),
			new DirPos(xCoord + 0, yCoord + 1, zCoord + 3, ForgeDirection.NORTH),
			new DirPos(xCoord + 1, yCoord + 1, zCoord + 3, ForgeDirection.NORTH),

			new DirPos(xCoord - 1, yCoord + 1, zCoord - 3, ForgeDirection.SOUTH),
			new DirPos(xCoord + 0, yCoord + 1, zCoord - 3, ForgeDirection.SOUTH),
			new DirPos(xCoord + 1, yCoord + 1, zCoord - 3, ForgeDirection.SOUTH),

			new DirPos(xCoord + 3, yCoord + 1, zCoord - 1, ForgeDirection.EAST),
			new DirPos(xCoord + 3, yCoord + 1, zCoord + 0, ForgeDirection.EAST),
			new DirPos(xCoord + 3, yCoord + 1, zCoord + 1, ForgeDirection.EAST),

			new DirPos(xCoord - 3, yCoord + 1, zCoord - 1, ForgeDirection.WEST),
			new DirPos(xCoord - 3, yCoord + 1, zCoord + 0, ForgeDirection.WEST),
			new DirPos(xCoord - 3, yCoord + 1, zCoord + 1, ForgeDirection.WEST),
		};
	}

	public List<String> findIssues() {
		List<String> issues = new ArrayList<String>();

		if(!rocket.validate()) return issues;

		// if(power < maxPower * 0.75) {
		// 	issues.add(EnumChatFormatting.RED + "Insufficient power");
		// }

		TileEntityLaunchPadRocket.findTankIssues(issues, tanks, solidFuel);
		if(TileEntityLaunchPadRocket.findDriveIssues(issues, rocket, slots[0])) return issues;

		// Check that the rocket is actually capable of reaching our destination
		Target from = CelestialBody.getTarget(worldObj, xCoord, zCoord);
		Target to = ItemVOTVdrive.getTarget(slots[0], worldObj);

		TileEntityLaunchPadRocket.findTravelIssues(issues, rocket, from, to);

		return issues;
	}

	@Override
	public void serialize(ByteBuf buf) {
		rocket.writeToByteBuffer(buf);

		buf.writeBoolean(hasDocked);
		buf.writeBoolean(hasRider);

		// buf.writeLong(power);
		buf.writeInt(solidFuel.level);
		buf.writeInt(solidFuel.max);

		for(int i = 0; i < tanks.length; i++) tanks[i].serialize(buf);
	}

	@Override
	public void deserialize(ByteBuf buf) {
		rocket = RocketStruct.readFromByteBuffer(buf);

		hasDocked = buf.readBoolean();
		hasRider = buf.readBoolean();

		// power = buf.readLong();
		solidFuel.level = buf.readInt();
		solidFuel.max = buf.readInt();

		for(int i = 0; i < tanks.length; i++) tanks[i].deserialize(buf);
	}

	@Override
	public void writeToNBT(NBTTagCompound nbt) {
		super.writeToNBT(nbt);
		// nbt.setLong("power", power);
		nbt.setInteger("solid", solidFuel.level);
		nbt.setInteger("maxSolid", solidFuel.max);
		for(int i = 0; i < tanks.length; i++) tanks[i].writeToNBT(nbt, "t" + i);
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);
		// power = nbt.getLong("power");
		solidFuel.level = nbt.getInteger("solid");
		solidFuel.max = nbt.getInteger("maxSolid");
		for(int i = 0; i < tanks.length; i++) tanks[i].readFromNBT(nbt, "t" + i);
	}

	@Override
	public AxisAlignedBB getRenderBoundingBox() {
		return TileEntity.INFINITE_EXTENT_AABB;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public double getMaxRenderDistanceSquared() {
		return 65536.0D;
	}

	@Override
	public boolean hasPermission(EntityPlayer player) {
		return isUseableByPlayer(player);
	}

	@Override
	public void receiveControl(NBTTagCompound data) { }

	@Override
	public void receiveControl(EntityPlayer player, NBTTagCompound data) {
		if(data.getBoolean("launch")) {
			launch(player);
		}
	}

	@Override
	public Container provideContainer(int ID, EntityPlayer player, World world, int x, int y, int z) {
		return new ContainerOrbitalStationLauncher(player.inventory, this);
	}

	@Override
	public Object provideGUI(int ID, EntityPlayer player, World world, int x, int y, int z) {
		return new GUIOrbitalStationLauncher(player.inventory, this);
	}

	@Override
	public void setCurrentStage(int stage) {
		currentStage = stage;
	}

	@Override
	public FluidTank[] getAllTanks() {
		return tanks;
	}

	@Override
	public FluidTank[] getReceivingTanks() {
		return tanks;
	}

}
