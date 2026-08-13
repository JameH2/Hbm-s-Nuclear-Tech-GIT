package com.hbm.tileentity.machine;

import com.hbm.blocks.BlockDummyable;
import com.hbm.dim.CelestialBody;
import com.hbm.dim.trait.CBT_Atmosphere;
import com.hbm.dim.trait.CBT_Dyson;
import com.hbm.items.ISatChip;
import com.hbm.items.ModItems;
import com.hbm.packet.PacketDispatcher;
import com.hbm.packet.toclient.AuxParticlePacketNT;
import com.hbm.tileentity.TileEntityMachineBase;
import com.hbm.util.fauxpointtwelve.DirPos;

import api.hbm.energymk2.IEnergyReceiverMK2;
import cpw.mods.fml.common.network.NetworkRegistry.TargetPoint;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraftforge.common.util.ForgeDirection;

public class TileEntityDysonLauncher extends TileEntityMachineBase implements IEnergyReceiverMK2 {

	public int swarmId;
	public int swarmCount;

	public long power;
	public static final long MAX_POWER = 20_000_000;

	private static final int MEMBERS_PER_LAUNCH = 4;

	// SHAKE IT LIKE IT'S HEAT, OVERDRIVE
	public boolean sunsetOverdrive = false;

	public boolean isOperating;
	public boolean isSpinningDown;
	public int operatingTime;

	public float rotation;
	public float lastRotation;
	public float speed;

	public int payloadTicks;

	public int satCount;

	public TileEntityDysonLauncher() {
		super(2);
	}

	@Override
	public String getName() {
		return "container.machineDysonLauncher";
	}

	@Override
	public void updateEntity() {
		if(!worldObj.isRemote) {
			for(DirPos pos : getConPos()) trySubscribe(worldObj, pos.getX(), pos.getY(), pos.getZ(), pos.getDir());
			for(DirPos pos : getInvPos()) tryLoad(pos.getX(), pos.getY(), pos.getZ(), pos.getDir());

			swarmId = ISatChip.getFreqS(slots[1]);
			swarmCount = CBT_Dyson.count(worldObj, swarmId);

			isOperating = !isSpinningDown && power >= getPowerPerTick() && slots[0] != null && slots[0].getItem() == ModItems.swarm_member && swarmId > 0;

			if(isSpinningDown) {
				operatingTime++;

				if(operatingTime > getSpinDownTime()) {
					isSpinningDown = false;
					operatingTime = 0;
				}
			} else if(isOperating) {
				if(operatingTime == 0) {
					float pitch = sunsetOverdrive ? 1.0F : 0.25F;
					worldObj.playSoundEffect(xCoord, yCoord + 8, zCoord, "hbm:misc.spincharge", 1.5F, pitch);
				}

				operatingTime++;
				power -= getPowerPerTick();

				if(operatingTime > getSpinUpTime()) {
					int toLaunch = Math.min(slots[0].stackSize, MEMBERS_PER_LAUNCH);
					CBT_Dyson.launch(worldObj, swarmId, toLaunch);

					CBT_Atmosphere atmosphere = CelestialBody.getTrait(worldObj, CBT_Atmosphere.class);
					double pressure = atmosphere != null ? atmosphere.getPressure() : 0;
					double scaledPressure = 1.0 - Math.pow(1.0 - pressure, 3);

					float volume = Math.min((float)scaledPressure * 16.0F, 4.0F);

					ForgeDirection dir = ForgeDirection.getOrientation(this.getBlockMetadata() - BlockDummyable.offset);
					ForgeDirection rot = dir.getRotation(ForgeDirection.UP);

					worldObj.playSoundEffect(xCoord + rot.offsetX * 6, yCoord + 8, zCoord + rot.offsetZ * 6, "hbm:misc.spinshot", volume, 0.9F + worldObj.rand.nextFloat() * 0.3F);
					worldObj.playSoundEffect(xCoord + rot.offsetX * 6, yCoord + 8, zCoord + rot.offsetZ * 6, "hbm:misc.spinshot", volume, 1F + worldObj.rand.nextFloat() * 0.3F);

					int count = Math.min(20, (int)(pressure * 80));

					double posX = xCoord + rot.offsetX * 9;
					double posY = yCoord + 12;
					double posZ = zCoord + rot.offsetZ * 9;

					NBTTagCompound data = new NBTTagCompound();
					data.setInteger("count", count);
					data.setString("type", "spinlaunch");
					data.setFloat("scale", 3);
					data.setDouble("moX", dir.offsetX * 10);
					data.setDouble("moY", 10);
					data.setDouble("moZ", dir.offsetZ * 10);
					data.setInteger("maxAge", 10 + count / 2 + worldObj.rand.nextInt(5));
					PacketDispatcher.wrapper.sendToAllAround(new AuxParticlePacketNT(data, posX, posY, posZ), new TargetPoint(this.worldObj.provider.dimensionId, xCoord, yCoord, zCoord, 150));

					slots[0].stackSize -= toLaunch;
					if(slots[0].stackSize <= 0) slots[0] = null;

					operatingTime = 0;
					isSpinningDown = true;
				}
			} else {
				operatingTime = 0;
			}

			networkPackNT(250);
		} else {
			float acceleration = sunsetOverdrive ? 2.5F : 0.75F;
			float deceleration = sunsetOverdrive ? 15.0F : 3.0F;
			float resetSpeed = sunsetOverdrive ? 30.0F : 8.0F;

			if(isOperating) {
				speed += acceleration;
				if(speed > 90) speed = 90;
			} else if(speed > 0.1F) {
				speed -= deceleration;
				if(speed < resetSpeed) speed = resetSpeed;
			}

			lastRotation = rotation;
			if(!isOperating && speed <= resetSpeed && rotation > 360 - resetSpeed * 1.5) {
				lastRotation -= 360;
				rotation = 0;
				speed = 0;
			} else {
				rotation += speed;
			}

			if(rotation >= 360) {
				rotation -= 360;
				lastRotation -= 360;
			}

			if(isSpinningDown) {
				payloadTicks++;
			} else {
				payloadTicks = 0;
			}
		}
	}

	private int getSpinUpTime() { return sunsetOverdrive ? 38 : 132; }
	private int getSpinDownTime() { return sunsetOverdrive ? 12 : 68; }
	private long getPowerPerTick() { return MAX_POWER / getSpinUpTime(); }

	public DirPos[] getConPos() {
		ForgeDirection dir = ForgeDirection.getOrientation(this.getBlockMetadata() - BlockDummyable.offset);
		ForgeDirection rot = dir.getRotation(ForgeDirection.UP);

		return new DirPos[] {
			new DirPos(xCoord - dir.offsetX * 0 - rot.offsetX * 3, yCoord, zCoord - dir.offsetZ * 0 - rot.offsetZ * 3, rot.getOpposite()),
			new DirPos(xCoord - dir.offsetX * 1 - rot.offsetX * 3, yCoord, zCoord - dir.offsetZ * 1 - rot.offsetZ * 3, rot.getOpposite()),
			new DirPos(xCoord - dir.offsetX * 2 - rot.offsetX * 3, yCoord, zCoord - dir.offsetZ * 2 - rot.offsetZ * 3, rot.getOpposite()),
			new DirPos(xCoord - dir.offsetX * 3 - rot.offsetX * 3, yCoord, zCoord - dir.offsetZ * 3 - rot.offsetZ * 3, rot.getOpposite()),
			new DirPos(xCoord - dir.offsetX * 4 - rot.offsetX * 3, yCoord, zCoord - dir.offsetZ * 4 - rot.offsetZ * 3, rot.getOpposite()),
		};
	}

	public DirPos[] getInvPos() {
		ForgeDirection dir = ForgeDirection.getOrientation(this.getBlockMetadata() - BlockDummyable.offset);

		return new DirPos[] {
			new DirPos(xCoord - dir.offsetX * 9, yCoord, zCoord - dir.offsetZ * 9, dir.getOpposite()),
		};
	}

	private void tryLoad(int x, int y, int z, ForgeDirection dir) {
		if(slots[0] != null && slots[0].stackSize >= MEMBERS_PER_LAUNCH) return;

		TileEntity te = worldObj.getTileEntity(x, y, z);
		if(!(te instanceof IInventory)) return;

		IInventory inv = (IInventory) te;
		ISidedInventory sided = inv instanceof ISidedInventory ? (ISidedInventory) inv : null;
		int[] access = sided != null ? sided.getAccessibleSlotsFromSide(dir.ordinal()) : null;

		for(int i = 0; i < (access != null ? access.length : inv.getSizeInventory()); i++) {
			int slot = access != null ? access[i] : i;
			ItemStack stack = inv.getStackInSlot(slot);
			if(stack != null && stack.getItem() == ModItems.swarm_member && (sided == null || sided.canExtractItem(slot, stack, dir.ordinal()))) {
				ItemStack removed = inv.decrStackSize(slot, 1);
				if(slots[0] == null) {
					slots[0] = removed;
				} else {
					slots[0].stackSize++;
				}
				break;
			}
		}
	}

	@Override
	public void serialize(ByteBuf buf) {
		super.serialize(buf);
		buf.writeInt(swarmId);
		buf.writeLong(power);
		buf.writeBoolean(isOperating);
		buf.writeBoolean(isSpinningDown);
		buf.writeInt(swarmCount);
		buf.writeBoolean(sunsetOverdrive);
		buf.writeInt(slots[0] != null ? slots[0].stackSize : 0);
	}

	@Override
	public void deserialize(ByteBuf buf) {
		super.deserialize(buf);
		swarmId = buf.readInt();
		power = buf.readLong();
		isOperating = buf.readBoolean();
		isSpinningDown = buf.readBoolean();
		swarmCount = buf.readInt();
		sunsetOverdrive = buf.readBoolean();
		satCount = buf.readInt();
	}

	@Override
	public void writeToNBT(NBTTagCompound nbt) {
		super.writeToNBT(nbt);
		nbt.setLong("power", power);
		nbt.setBoolean("spinDown", isSpinningDown);
		nbt.setInteger("time", operatingTime);
		nbt.setBoolean("overdrive", sunsetOverdrive);
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);
		power = nbt.getLong("power");
		isSpinningDown = nbt.getBoolean("spinDown");
		operatingTime = nbt.getInteger("time");
		sunsetOverdrive = nbt.getBoolean("overdrive");
	}

	@Override
	public int getInventoryStackLimit() {
		return 4;
	}

	@Override
	public boolean isItemValidForSlot(int slot, ItemStack itemStack) {
		if(slot == 0) return itemStack.getItem() == ModItems.swarm_member;
		return false;
	}

	@Override
	public int[] getAccessibleSlotsFromSide(int side) {
		return new int[] {0};
	}

	@Override public long getPower() { return power; }
	@Override public void setPower(long power) { this.power = power; }
	@Override public long getMaxPower() { return MAX_POWER; }

	AxisAlignedBB bb = null;

	@Override
	public AxisAlignedBB getRenderBoundingBox() {

			if(bb == null) {
				bb = AxisAlignedBB.getBoundingBox(
					xCoord - 100,
					yCoord,
					zCoord - 100,
					xCoord + 100,
					yCoord + 100,
					zCoord + 100
				);
			}

			return bb;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public double getMaxRenderDistanceSquared() {
		return 65536.0D;
	}

}
