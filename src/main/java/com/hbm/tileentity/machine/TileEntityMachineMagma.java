package com.hbm.tileentity.machine;

import java.util.ArrayList;
import java.util.List;

import com.hbm.blocks.BlockDummyable;
import com.hbm.dim.CelestialBody;
import com.hbm.inventory.fluid.tank.FluidTank;
import com.hbm.inventory.material.MaterialShapes;
import com.hbm.inventory.material.Mats;
import com.hbm.inventory.material.Mats.MaterialStack;
import com.hbm.inventory.material.NTMMaterial;
import com.hbm.module.portmanager.ModulePortManFluidAdaptive;
import com.hbm.module.portmanager.ModulePortManPower;
import com.hbm.packet.PacketDispatcher;
import com.hbm.packet.toclient.AuxParticlePacketNT;
import com.hbm.tileentity.TileEntityMachineBase;
import com.hbm.tileentity.TilePort.PortDef;
import com.hbm.util.BobMathUtil;
import com.hbm.util.CrucibleUtil;

import api.hbm.energymk2.IEnergyReceiverMK2;
import api.hbm.fluidmk2.IFluidStandardTransceiverMK2;
import cpw.mods.fml.common.network.NetworkRegistry.TargetPoint;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.Vec3;
import net.minecraftforge.common.util.ForgeDirection;

public class TileEntityMachineMagma extends TileEntityMachineBase implements IEnergyReceiverMK2, IFluidStandardTransceiverMK2 {

	public boolean operating;

	public long power;
	public long consumption = 10_000;

	// TODO: probably to handle cooling fluids, remove me if we don't do that
	public FluidTank[] tanks;

	public static final int maxLiquid = MaterialShapes.BLOCK.q(16);
	public List<MaterialStack> liquids = new ArrayList<>();

	public float drillSpeed;
	public float drillRotation;
	public float prevDrillRotation;

	public float lavaHeight;
	public float prevLavaHeight;

	public boolean validPosition = true;

	protected MaterialStack[] defaultOutputs = new MaterialStack[] {
		new MaterialStack(Mats.MAT_SLAG, MaterialShapes.INGOT.q(1)),
		new MaterialStack(Mats.MAT_RICH_MAGMA, MaterialShapes.QUANTUM.q(4)),
	};

	protected ModulePortManFluidAdaptive moduleFluidPorts;
	protected ModulePortManPower modulePowerPorts;

	public TileEntityMachineMagma() {
		super(0);
		tanks = new FluidTank[0];

		moduleFluidPorts = new ModulePortManFluidAdaptive(this).setInputTanks(getReceivingTanks()).setOutputTanks(getSendingTanks());
		modulePowerPorts = new ModulePortManPower(this);
	}

	@Override
	public void updateEntity() {
		if(!worldObj.isRemote) {
			moduleFluidPorts.update(getPorts());
			modulePowerPorts.update(getPorts());

			// baby drill, baby
			operating = canOperate();
			if(operating) {
				power -= consumption;

				int timeBetweenOutputs = 10;

				if(worldObj.getTotalWorldTime() % timeBetweenOutputs == 0) {
					for(MaterialStack mat : getOutputs()) {
						int totalLiquid = 0;
						for(MaterialStack m : liquids) totalLiquid += m.amount;

						int toAdd = mat.amount;

						if(totalLiquid + toAdd <= maxLiquid) {
							addToStack(mat);
						} else {
							break;
						}
					}
				}
			}

			// pour me a drink, barkeep
			if(!liquids.isEmpty()) {
				ForgeDirection dir = ForgeDirection.getOrientation(this.getBlockMetadata() - 10);

				Vec3 impact = Vec3.createVectorHelper(0, 0, 0);
				MaterialStack didPour = CrucibleUtil.pourFullStack(worldObj, xCoord + 0.5D + dir.offsetX * 3.875D, yCoord + 1.25D, zCoord + 0.5D + dir.offsetZ * 3.875D, 6, true, liquids, MaterialShapes.INGOT.q(1), impact);

				if(didPour != null) {
					NBTTagCompound data = new NBTTagCompound();
					data.setString("type", "foundry");
					data.setInteger("color", didPour.material.moltenColor);
					data.setByte("dir", (byte) dir.ordinal());
					data.setFloat("off", 0.625F);
					data.setFloat("base", 0.625F);
					data.setFloat("len", Math.max(1F, yCoord + 1 - (float) (Math.ceil(impact.yCoord) - 0.875)));
					PacketDispatcher.wrapper.sendToAllAround(new AuxParticlePacketNT(data, xCoord + 0.5D + dir.offsetX * 3.875D, yCoord + 1, zCoord + 0.5D + dir.offsetZ * 3.875D), new TargetPoint(worldObj.provider.dimensionId, xCoord + 0.5, yCoord + 1, zCoord + 0.5, 50));
				}
			}

			liquids.removeIf(o -> o.amount <= 0);

			networkPackNT(250);
		} else {
			prevLavaHeight = lavaHeight;
			prevDrillRotation = drillRotation;

			if(operating) {
				drillSpeed += 0.15F;
				if(drillSpeed > 15F) drillSpeed = 15F;

				lavaHeight += (worldObj.rand.nextFloat() - 0.5) * 0.01;
				lavaHeight = (float)BobMathUtil.lerp(0.02D, lavaHeight, 0.9D);
			} else {
				drillSpeed -= 0.3F;
				if(drillSpeed < 0F) drillSpeed = 0F;

				lavaHeight = (float)BobMathUtil.lerp(0.02D, lavaHeight, 0D);
			}

			drillRotation += drillSpeed;

			if(drillRotation > 360F) {
				drillRotation -= 360F;
				prevDrillRotation -= 360F;
			}
		}
	}

	protected PortDef[] cachedPorts;

	public PortDef[] getPorts() {
		if(cachedPorts == null) {
			ForgeDirection dir = ForgeDirection.getOrientation(this.getBlockMetadata() - BlockDummyable.offset);
			ForgeDirection rot = dir.getRotation(ForgeDirection.UP);

			cachedPorts = new PortDef[] {
				PortDef.make(xCoord - dir.offsetX * 3, yCoord - 1, zCoord - dir.offsetZ * 3, dir),
				PortDef.make(xCoord - dir.offsetX * 3, yCoord - 2, zCoord - dir.offsetZ * 3, dir),
				PortDef.make(xCoord - dir.offsetX * 3 + rot.offsetX, yCoord - 1, zCoord - dir.offsetZ * 3 + rot.offsetZ, dir),
				PortDef.make(xCoord - dir.offsetX * 3 + rot.offsetX, yCoord - 2, zCoord - dir.offsetZ * 3 + rot.offsetZ, dir),
				PortDef.make(xCoord - dir.offsetX * 3 - rot.offsetX, yCoord - 1, zCoord - dir.offsetZ * 3 - rot.offsetZ, dir),
				PortDef.make(xCoord - dir.offsetX * 3 - rot.offsetX, yCoord - 2, zCoord - dir.offsetZ * 3 - rot.offsetZ, dir),
			};
		}

		return cachedPorts;
	}

	private boolean canOperate() {
		// Currently only functions on Moho, so the simplest solution is acceptable
		CelestialBody body = CelestialBody.getBody(worldObj);
		if(body.name != "moho") return false;

		validPosition = isValidPosition();
		if(!validPosition) return false;

		if(power < consumption) return false;

		return true;
	}

	private boolean isValidPosition() {
		for(int x = -1; x <= 1; x++) {
			for(int z = -1; z <= 1; z++) {
				if(worldObj.getBlock(xCoord + x, yCoord - 4, zCoord + z) != Blocks.lava) return false;
			}
		}

		return true;
	}

	// Returns materials produced at this location, varied by perlin noise
	private MaterialStack[] getOutputs() {
		return defaultOutputs;
	}

	private void addToStack(MaterialStack matStack) {
		for(MaterialStack mat : liquids) {
			if(mat.material == matStack.material) {
				mat.amount += matStack.amount;
				return;
			}
		}

		liquids.add(matStack.copy());
	}

	@Override
	public void serialize(ByteBuf buf) {
		super.serialize(buf);

		buf.writeBoolean(operating);
		buf.writeLong(power);
		buf.writeBoolean(validPosition);

		for(int i = 0; i < tanks.length; i++) tanks[i].serialize(buf);

		buf.writeShort(liquids.size());
		for(MaterialStack sta : liquids) {
			buf.writeInt(sta.material.id);
			buf.writeInt(sta.amount);
		}
	}

	@Override
	public void deserialize(ByteBuf buf) {
		super.deserialize(buf);

		operating = buf.readBoolean();
		power = buf.readLong();
		validPosition = buf.readBoolean();

		for(int i = 0; i < tanks.length; i++) tanks[i].deserialize(buf);

		liquids.clear();
		int mats = buf.readShort();
		for(int i = 0; i < mats; i++) {
			liquids.add(new MaterialStack(Mats.matById.get(buf.readInt()), buf.readInt()));
		}
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);

		power = nbt.getLong("power");

		for(int i = 0; i < tanks.length; i++) tanks[i].readFromNBT(nbt, "t" + i);

		int[] liquidData = nbt.getIntArray("liquids");
		for(int i = 0; i < liquidData.length / 2; i++) {
			NTMMaterial mat = Mats.matById.get(liquidData[i * 2]);
			if(mat == null) continue;
			liquids.add(new MaterialStack(mat, liquidData[i * 2 + 1]));
		}
	}

	@Override
	public void writeToNBT(NBTTagCompound nbt) {
		super.writeToNBT(nbt);

		nbt.setLong("power", power);

		for(int i = 0; i < tanks.length; i++) tanks[i].writeToNBT(nbt, "t" + i);

		int[] liquidData = new int[liquids.size() * 2];
		for(int i = 0; i < liquids.size(); i++) { MaterialStack sta = liquids.get(i); liquidData[i * 2] = sta.material.id; liquidData[i * 2 + 1] = sta.amount; }
		nbt.setIntArray("liquids", liquidData);
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
		return 1_000_000;
	}

	@Override
	public FluidTank[] getAllTanks() {
		return tanks;
	}

	@Override
	public FluidTank[] getSendingTanks() {
		return tanks;
	}

	@Override
	public FluidTank[] getReceivingTanks() {
		return tanks;
	}

	@Override
	public String getName() {
		return "container.machineMagma";
	}

	AxisAlignedBB bb = null;

	@Override
	public AxisAlignedBB getRenderBoundingBox() {

		if(bb == null) {
			bb = AxisAlignedBB.getBoundingBox(
				xCoord - 4,
				yCoord - 3,
				zCoord - 4,
				xCoord + 5,
				yCoord + 3,
				zCoord + 5
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
