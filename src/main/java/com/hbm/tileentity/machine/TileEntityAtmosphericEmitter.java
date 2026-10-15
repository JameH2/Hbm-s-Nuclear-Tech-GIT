package com.hbm.tileentity.machine;

import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.tank.FluidTank;
import com.hbm.inventory.fluid.trait.FluidTrait;
import com.hbm.inventory.fluid.trait.FluidTrait.FluidReleaseType;
import com.hbm.inventory.fluid.trait.FluidTraitSimple.FT_Amat;
import com.hbm.main.MainRegistry;
import com.hbm.module.portmanager.ModulePortManFluidAdaptive;
import com.hbm.module.portmanager.ModulePortManPower;
import com.hbm.sound.AudioWrapper;
import com.hbm.tileentity.TileEntityLoadedBase;
import com.hbm.tileentity.TilePort.PortDef;

import api.hbm.energymk2.IEnergyReceiverMK2;
import api.hbm.fluidmk2.IFluidStandardReceiverMK2;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.AxisAlignedBB;
import net.minecraftforge.common.util.ForgeDirection;

public class TileEntityAtmosphericEmitter extends TileEntityLoadedBase implements IEnergyReceiverMK2, IFluidStandardReceiverMK2 {

	public FluidTank tank;
	public long power;
	
	public float fan = 0;
	public float prevFan = 0;
	private AudioWrapper audio;

	protected ModulePortManFluidAdaptive moduleFluidPorts;
	protected ModulePortManPower modulePowerPorts;

	public TileEntityAtmosphericEmitter() {
		this.tank = new FluidTank(Fluids.NONE, 50_000);

		moduleFluidPorts = new ModulePortManFluidAdaptive(this).setInputTanks(getReceivingTanks());
		modulePowerPorts = new ModulePortManPower(this);
	}
	
	@Override
	public void updateEntity() {
		if(!worldObj.isRemote) {
			moduleFluidPorts.update(getPorts());
			modulePowerPorts.update(getPorts());

			networkPackNT(50);

			if(tank.getFill() > 0) {
				int toSpill = Math.max(tank.getFill() / 2, 1);

				if(this.power >= toSpill * 10) {
					if(tank.getTankType().hasTrait(FT_Amat.class)) {
						worldObj.newExplosion(null, xCoord + 0.5, yCoord + 0.5, zCoord + 0.5, 10F, true, true);
						return;
					}
	
					tank.setFill(tank.getFill() - toSpill);

					FluidTrait.onRelease(worldObj, xCoord, yCoord, zCoord, tank.getTankType(), tank, FluidReleaseType.SPILL, toSpill);
	
					this.power -= toSpill * 10;
				}
			}
		} else {
			this.prevFan = this.fan;

			if(this.power >= Math.max(tank.getFill() / 2, 1) * 10) {
				this.fan += 45;

				if(this.fan >= 360) {
					this.fan -= 360;
					this.prevFan -= 360;
				}

				if(audio == null) {
					audio = createAudioLoop();
					audio.startSound();
				} else if(!audio.isPlaying()) {
					audio = rebootAudio(audio);
				}

				audio.keepAlive();
				audio.updateVolume(this.getVolume(0.25F));

				if(tank.getFill() > 0 && MainRegistry.proxy.me().getDistance(xCoord, yCoord, zCoord) < 100) {
					ForgeDirection dir = ForgeDirection.getOrientation(this.getBlockMetadata() - 10);
					ForgeDirection rot = dir.getRotation(ForgeDirection.UP);

					NBTTagCompound data = new NBTTagCompound();
					data.setFloat("lift", 0.5F);
					data.setFloat("base", 0.375F);
					data.setFloat("max", 3F);
					data.setInteger("life", 100 + worldObj.rand.nextInt(50));
					
					data.setString("type", "tower");

					data.setInteger("color", tank.getTankType().getColor());
					data.setDouble("posX", xCoord + 0.5 - dir.offsetX * 0.5 + rot.offsetX * 0.5);
					data.setDouble("posZ", zCoord + 0.5 - dir.offsetZ * 0.5 + rot.offsetZ * 0.5);
					data.setDouble("posY", yCoord + 2.5);

					data.setDouble("mY", 0.2);

					MainRegistry.proxy.effectNT(data);
				}

			} else {

				if(audio != null) {
					audio.stopSound();
					audio = null;
				}
			}
		}
	}

	protected PortDef[] cachedPorts;

	public PortDef[] getPorts() {
		if(cachedPorts == null) {
			ForgeDirection dir = ForgeDirection.getOrientation(this.getBlockMetadata() - 10);
			ForgeDirection rot = dir.getRotation(ForgeDirection.UP);

			cachedPorts = new PortDef[] {
				PortDef.make(xCoord, yCoord, zCoord, dir),
				PortDef.make(xCoord + rot.offsetX, yCoord, zCoord + rot.offsetZ, dir),
	
				PortDef.make(xCoord - dir.offsetX, yCoord, zCoord - dir.offsetZ, dir.getOpposite()),
				PortDef.make(xCoord - dir.offsetX + rot.offsetX, yCoord, zCoord - dir.offsetZ + rot.offsetZ, dir.getOpposite()),
	
				PortDef.make(xCoord + rot.offsetX, yCoord, zCoord + rot.offsetZ, rot),
				PortDef.make(xCoord + rot.offsetX - dir.offsetX, yCoord, zCoord + rot.offsetZ - dir.offsetZ, rot),
	
				PortDef.make(xCoord, yCoord, zCoord, rot.getOpposite()),
				PortDef.make(xCoord - dir.offsetX, yCoord, zCoord - dir.offsetZ, rot.getOpposite())
			};
		}

		return cachedPorts;
	}

	@Override public AudioWrapper createAudioLoop() {
		return MainRegistry.proxy.getLoopedSound("hbm:block.motor", xCoord, yCoord, zCoord, 0.25F, 10F, 1.0F, 20);
	}

	@Override public void onChunkUnload() {
		if(audio != null) { audio.stopSound(); audio = null; }
	}

	@Override public void invalidate() {
		super.invalidate();
		if(audio != null) { audio.stopSound(); audio = null; }
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);
		this.tank.readFromNBT(nbt, "t");
		this.power = nbt.getLong("power");
	}

	@Override
	public void writeToNBT(NBTTagCompound nbt) {
		super.writeToNBT(nbt);
		nbt.setLong("power", power);
		this.tank.writeToNBT(nbt, "t");
	}

	@Override
	public void serialize(ByteBuf buf) {
		buf.writeLong(power);
		tank.serialize(buf);
	}

	@Override
	public void deserialize(ByteBuf buf) {
		this.power = buf.readLong();
		tank.deserialize(buf);
	}

	@Override public FluidTank[] getAllTanks() { return new FluidTank[] {tank}; }
	@Override public FluidTank[] getReceivingTanks() { return new FluidTank[] {tank}; }

	@Override public void setPower(long i) { power = i; }
	@Override public long getPower() { return power; }
	@Override public long getMaxPower() { return 250_000; }

	AxisAlignedBB bb = null;

	@Override
	public AxisAlignedBB getRenderBoundingBox() {
		if(bb == null) {
			bb = AxisAlignedBB.getBoundingBox(
				xCoord - 1,
				yCoord,
				zCoord - 1,
				xCoord + 2,
				yCoord + 3,
				zCoord + 2
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
