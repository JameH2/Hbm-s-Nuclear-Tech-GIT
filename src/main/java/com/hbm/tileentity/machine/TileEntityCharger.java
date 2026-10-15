package com.hbm.tileentity.machine;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import api.hbm.energymk2.IBatteryItem;
import api.hbm.energymk2.IEnergyReceiverMK2;

import com.hbm.main.NTMSounds;
import com.hbm.tileentity.IBufPacketReceiver;
import com.hbm.tileentity.TileEntityLoadedBase;
	import com.hbm.util.AE2Compat;
import baubles.api.BaublesApi;
import com.hbm.tileentity.TilePort.PortDef;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.AxisAlignedBB;
import net.minecraftforge.common.util.ForgeDirection;
import xonin.backhand.api.core.BackhandUtils;

public class TileEntityCharger extends TileEntityLoadedBase implements IEnergyReceiverMK2, IBufPacketReceiver {

	private List<EntityPlayer> players = new ArrayList();
	private long charge = 0;
	private int lastOp = 0;

	boolean particles = false;

	public int usingTicks;
	public int lastUsingTicks;
	public static final int delay = 20;

	protected PortDef[] cachedPorts;

	public PortDef[] getPorts() {
		if(cachedPorts == null) {
			ForgeDirection dir = ForgeDirection.getOrientation(this.getBlockMetadata()).getOpposite();
			cachedPorts = new PortDef[] {
					PortDef.make(xCoord, yCoord, zCoord, dir)
			};
		}
		return cachedPorts;
	}

	@Override
	public void updateEntity() {

		ForgeDirection dir = ForgeDirection.getOrientation(this.getBlockMetadata()).getOpposite();

		if(!worldObj.isRemote) {
			
			this.setupPowerPorts(getPorts());
			this.updateAllPorts();
			this.receivePower();

			players = worldObj.getEntitiesWithinAABB(EntityPlayer.class, AxisAlignedBB.getBoundingBox(xCoord + 0.5, yCoord, zCoord + 0.5, xCoord + 0.5, yCoord + 0.5, zCoord + 0.5).expand(0.5, 0.0, 0.5));

			charge = 0;

			for(EntityPlayer player : players) {

				for(int i = 0; i < 5; i++) {
					charge += getChargeDemand(player.getEquipmentInSlot(i));
				}

				charge += getChargeDemand(BackhandUtils.getOffhandItem(player));

				IInventory baubles = BaublesApi.getBaubles(player);
				if(baubles != null) {
					for(int i = 0; i < baubles.getSizeInventory(); i++) {
						charge += getChargeDemand(baubles.getStackInSlot(i));
					}
				}
			}

			particles = lastOp > 0;

			if(particles) {

				lastOp--;

				if(worldObj.getTotalWorldTime() % 20 == 0)
					worldObj.playSoundEffect(xCoord + 0.5, yCoord + 0.5, zCoord + 0.5, NTMSounds.VANILLA_HISS, 0.2F, 0.5F);
			}

			networkPackNT(50);
		}

		lastUsingTicks = usingTicks;

		if((charge > 0 || particles) && usingTicks < delay) {
			usingTicks++;
			if(usingTicks == 2)
				worldObj.playSoundEffect(xCoord + 0.5, yCoord + 0.5, zCoord + 0.5, NTMSounds.VANILLA_PISTON_OUT, 0.5F, 0.5F);
		}
		if((charge <= 0 && !particles) && usingTicks > 0) {
			usingTicks--;
			if(usingTicks == 4)
				worldObj.playSoundEffect(xCoord + 0.5, yCoord + 0.5, zCoord + 0.5, NTMSounds.VANILLA_PISTON_IN, 0.5F, 0.5F);
		}

		if(particles) {
			Random rand = worldObj.rand;
			worldObj.spawnParticle("magicCrit",
					xCoord + 0.5 + rand.nextDouble() * 0.0625 + dir.offsetX * 0.75,
					yCoord + 0.1,
					zCoord + 0.5 + rand.nextDouble() * 0.0625 + dir.offsetZ * 0.75,
					-dir.offsetX + rand.nextGaussian() * 0.1,
					0,
					-dir.offsetZ + rand.nextGaussian() * 0.1);
		}
	}

	@Override
	public void serialize(ByteBuf buf) {
		buf.writeLong(this.charge);
		buf.writeBoolean(this.particles);
	}

	@Override
	public void deserialize(ByteBuf buf) {
		this.charge = buf.readLong();
		this.particles = buf.readBoolean();
	}

	@Override
	public long getPower() {
		return 0;
	}

	@Override
	public long getMaxPower() {
		return charge;
	}

	@Override
	public void setPower(long power) { }

	@Override
	public long transferPower(long power) {

		if(this.usingTicks < delay || power == 0)
			return power;

		for(EntityPlayer player : players) {

			for(int i = 0; i < 5; i++) {
				power = chargeStack(player.getEquipmentInSlot(i), power);
			}

			power = chargeStack(BackhandUtils.getOffhandItem(player), power);

			IInventory baubles = BaublesApi.getBaubles(player);
			if(baubles != null) {
				for(int i = 0; i < baubles.getSizeInventory(); i++) {
					power = chargeStack(baubles.getStackInSlot(i), power);
				}
			}
		}

		return power;
	}

	private long getChargeDemand(ItemStack stack) {
		if(stack == null) return 0;

		if(stack.getItem() instanceof IBatteryItem) {
			IBatteryItem battery = (IBatteryItem) stack.getItem();
			return Math.min(battery.getMaxCharge(stack) - battery.getCharge(stack), battery.getChargeRate(stack));
		}

		if(AE2Compat.isEnergyStorage(stack)) {
			return AE2Compat.getChargeDemand(stack);
		}

		return 0;
	}

	private long chargeStack(ItemStack stack, long power) {
		if(stack == null || power <= 0) return power;

		if(stack.getItem() instanceof IBatteryItem) {
			IBatteryItem battery = (IBatteryItem) stack.getItem();
			long toCharge = Math.min(battery.getMaxCharge(stack) - battery.getCharge(stack), battery.getChargeRate(stack));
			toCharge = Math.min(toCharge, Math.max(power / 5, 1));
			battery.chargeBattery(stack, toCharge);
			if(toCharge > 0) lastOp = 4;
			return power - toCharge;
		}

		if(AE2Compat.isEnergyStorage(stack)) {
			long budget = Math.min(AE2Compat.getChargeRate(), Math.max(power / 5, 1));
			long remaining = AE2Compat.charge(stack, budget);
			long consumed = Math.max(0, budget - remaining);
			if(consumed > 0) lastOp = 4;
			return power - consumed;
		}

		return power;
	}
}
