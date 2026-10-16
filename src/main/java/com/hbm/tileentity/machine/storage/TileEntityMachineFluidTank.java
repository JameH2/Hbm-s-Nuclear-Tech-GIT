package com.hbm.tileentity.machine.storage;

import api.hbm.fluidmk2.IFluidStandardTransceiverMK2;
import api.hbm.redstoneoverradio.IRORInteractive;
import api.hbm.redstoneoverradio.IRORValueProvider;

import com.hbm.explosion.vanillant.ExplosionVNT;
import com.hbm.extprop.HbmPlayerProps;
import com.hbm.inventory.OreDictManager;
import com.hbm.inventory.RecipesCommon.AStack;
import com.hbm.inventory.RecipesCommon.OreDictStack;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.fluid.trait.*;
import com.hbm.inventory.fluid.trait.FluidTrait.FluidReleaseType;
import com.hbm.inventory.fluid.trait.FluidTraitSimple.*;
import com.hbm.packet.PacketDispatcher;
import com.hbm.tileentity.*;
import com.hbm.tileentity.TilePort.PortDef;
import com.hbm.packet.toclient.AuxParticlePacketNT;
import com.hbm.util.ParticleUtil;
import com.hbm.util.FluidDebug;
import cpw.mods.fml.common.Optional;
import cpw.mods.fml.common.network.NetworkRegistry.TargetPoint;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MathHelper;
import net.minecraft.world.Explosion;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidHandler;
import api.ntm1of90.compat.fluid.registry.FluidMappingRegistry;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Optional.InterfaceList({@Optional.Interface(iface = "li.cil.oc.api.network.SimpleComponent", modid = "opencomputers")})
public class TileEntityMachineFluidTank extends TileEntityBarrel implements IFluidStandardTransceiverMK2, IPersistentNBT, IOverpressurable, IRepairable, IFluidCopiable, IRORValueProvider, IRORInteractive, IFluidHandler {

	public boolean hasExploded = false;
	public boolean onFire = false;
	public Explosion lastExplosion = null;

	public TileEntityMachineFluidTank() {
		super(256_000);
	}

	@Override public long getReceiverSpeed(FluidType type, int pressure) { return this.hasExploded ? 0 : (mode == 0 || mode == 1) ? Math.max(500, (tank.getMaxFill() - tank.getFill()) / 100) : 0; }
	@Override public long getProviderSpeed(FluidType type, int pressure) { return this.hasExploded ? 0 : (mode == 1 || mode == 2) ? Math.max(500, tank.getFill() / 100) : 0; }

	@Override
	public String getName() {
		return "container.fluidtank";
	}

	public byte getComparatorPower() {
		if(tank.getFill() == 0) return 0;
		double frac = (double) tank.getFill() / (double) tank.getMaxFill() * 15D;
		return (byte) (MathHelper.clamp_int((int) frac + 1, 0, 15));
	}

	@Override
	public void updateEntity() {
		super.updateEntity();

		ForgeDirection dir = ForgeDirection.getOrientation(this.getBlockMetadata() - 10);
		ForgeDirection rot = dir.getRotation(ForgeDirection.UP);
		List<EntityPlayer> players = worldObj.getEntitiesWithinAABB(EntityPlayer.class, AxisAlignedBB.getBoundingBox(xCoord, yCoord, zCoord, xCoord + 1, yCoord + 2.875, zCoord + 1).offset(dir.offsetX * 0.5 - rot.offsetX * 2.25, 0, dir.offsetZ * 0.5 - rot.offsetZ * 2.25));

		for(EntityPlayer player : players) {
			HbmPlayerProps props = HbmPlayerProps.getData(player);
			props.isOnLadder = true;
		}
	}
	
	@Override
	public int trackingRange() {
		return 150;
	}

	@Override
	public void checkFluidInteraction() {
		
		if(tank.getTankType().isAntimatter()) {
			new ExplosionVNT(worldObj, xCoord + 0.5, yCoord + 1.5, zCoord + 0.5, 5F).makeAmat().setBlockAllocator(null).setBlockProcessor(null).explode();
			this.explode();
			this.tank.setFill(0);
		}

		if(tank.getTankType().hasTrait(FT_Corrosive.class) && tank.getTankType().getTrait(FT_Corrosive.class).isHighlyCorrosive()) {
			this.explode();
		}

		if(this.hasExploded) {

			int leaking = 0;
			if(tank.getTankType().isAntimatter()) {
				leaking = tank.getFill();
			} else if(tank.getTankType().hasTrait(FT_Gaseous.class) || tank.getTankType().hasTrait(FT_Gaseous_ART.class)) {
				leaking = Math.min(tank.getFill(), tank.getMaxFill() / 100);
			} else {
				leaking = Math.min(tank.getFill(), tank.getMaxFill() / 10000);
			}

			updateLeak(leaking);
		}
	}

	@Override
	public void serialize(ByteBuf buf) {
		super.serialize(buf);
		buf.writeBoolean(hasExploded);
	}

	@Override
	public void deserialize(ByteBuf buf) {
		super.deserialize(buf);
		hasExploded = buf.readBoolean();
	}

	/** called when the tank breaks due to hazardous materials or external force, can be used to quickly void part of the tank or spawn a mushroom cloud */
	public void explode() {
		this.hasExploded = true;
		this.onFire = tank.getTankType().hasTrait(FT_Flammable.class);
		this.markChanged();
	}

	/** called every tick post explosion, used for leaking fluid and spawning particles */
	public void updateLeak(int amount) {
		if(!hasExploded) return;
		if(amount <= 0) return;

		this.tank.getTankType().onFluidRelease(this, tank, amount);
		this.tank.setFill(Math.max(0, this.tank.getFill() - amount));

		FluidType type = tank.getTankType();

		if(type.hasTrait(FT_Amat.class)) {
			new ExplosionVNT(worldObj, xCoord + 0.5, yCoord + 1.5, zCoord + 0.5, 5F).makeAmat().setBlockAllocator(null).setBlockProcessor(null).explode();

		} else if(type.hasTrait(FT_Flammable.class) && onFire) {
			List<Entity> affected = worldObj.getEntitiesWithinAABB(Entity.class, AxisAlignedBB.getBoundingBox(xCoord - 1.5, yCoord, zCoord - 1.5, xCoord + 2.5, yCoord + 5, zCoord + 2.5));
			for(Entity e : affected) e.setFire(5);
			Random rand = worldObj.rand;
			ParticleUtil.spawnGasFlame(worldObj, xCoord + rand.nextDouble(), yCoord + 0.5 + rand.nextDouble(), zCoord + rand.nextDouble(), rand.nextGaussian() * 0.2, 0.1, rand.nextGaussian() * 0.2);

			if(worldObj.getTotalWorldTime() % 5 == 0) {
				FluidTrait.onRelease(worldObj, xCoord, yCoord, zCoord, tank.getTankType(), tank, FluidReleaseType.BURN, amount * 5);
			}

		} else if(type.hasTrait(FT_Gaseous.class) || type.hasTrait(FT_Gaseous_ART.class)) {

			if(worldObj.getTotalWorldTime() % 5 == 0) {
				NBTTagCompound data = new NBTTagCompound();
				data.setString("type", "tower");
				data.setFloat("lift", 1F);
				data.setFloat("base", 1F);
				data.setFloat("max", 5F);
				data.setInteger("life", 100 + worldObj.rand.nextInt(20));
				data.setInteger("color", tank.getTankType().getColor());
				PacketDispatcher.wrapper.sendToAllAround(new AuxParticlePacketNT(data, xCoord + 0.5, yCoord + 1, zCoord + 0.5), new TargetPoint(worldObj.provider.dimensionId, xCoord, yCoord, zCoord, 150));
			}

			if(worldObj.getTotalWorldTime() % 5 == 0 ) {
				FluidTrait.onRelease(worldObj, xCoord, yCoord, zCoord, tank.getTankType(), tank, FluidReleaseType.SPILL, amount * 5);
			}
		}
	}

	@Override
	public void explode(World world, int x, int y, int z) {

		if(this.hasExploded) return;
		this.onFire = tank.getTankType().hasTrait(FT_Flammable.class);
		this.hasExploded = true;
		this.markChanged();
	}

	@Override
	public void tryExtinguish(World world, int x, int y, int z, EnumExtinguishType type) {
		if(!this.hasExploded || !this.onFire) return;

		if(type == EnumExtinguishType.WATER) {
			if(tank.getTankType().hasTrait(FT_Liquid.class)) { // extinguishing oil with water is a terrible idea!
				worldObj.newExplosion(null, xCoord + 0.5, yCoord + 1.5, zCoord + 0.5, 5F, true, true);
			} else {
				this.onFire = false;
				this.markChanged();
				return;
			}
		}

		if(type == EnumExtinguishType.FOAM || type == EnumExtinguishType.CO2) {
			this.onFire = false;
			this.markChanged();
		}
	}
	
	@Override
	public PortDef[] getPorts() { if(cachedPorts == null) cachedPorts = TilePortShapes.refinery(xCoord, yCoord, zCoord); return cachedPorts; }

	AxisAlignedBB bb = null;

	@Override
	public AxisAlignedBB getRenderBoundingBox() {

		if(bb == null) {
			bb = AxisAlignedBB.getBoundingBox(
					xCoord - 2,
					yCoord,
					zCoord - 2,
					xCoord + 3,
					yCoord + 3,
					zCoord + 3
					);
		}

		return bb;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public double getMaxRenderDistanceSquared() {
		return 65536.0D;
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);

		hasExploded = nbt.getBoolean("exploded");
		onFire = nbt.getBoolean("onFire");
	}

	@Override
	public void writeToNBT(NBTTagCompound nbt) {
		super.writeToNBT(nbt);

		nbt.setBoolean("exploded", hasExploded);
		nbt.setBoolean("onFire", onFire);
	}

	@Override
	public void writeNBT(NBTTagCompound nbt) {
		if(tank.getFill() == 0 && !this.hasExploded) return;
		NBTTagCompound data = new NBTTagCompound();
		this.tank.writeToNBT(data, "tank");
		data.setShort("mode", mode);
		data.setBoolean("hasExploded", hasExploded);
		data.setBoolean("onFire", onFire);
		nbt.setTag(NBT_PERSISTENT_KEY, data);
	}

	@Override
	public void readNBT(NBTTagCompound nbt) {
		NBTTagCompound data = nbt.getCompoundTag(NBT_PERSISTENT_KEY);
		this.tank.readFromNBT(data, "tank");
		this.mode = data.getShort("mode");
		this.hasExploded = data.getBoolean("hasExploded");
		this.onFire = data.getBoolean("onFire");
	}

	@Override public boolean canConnect(FluidType fluid, ForgeDirection dir) { return true; }

	@Override
	public boolean isDamaged() {
		return this.hasExploded;
	}

	List<AStack> repair = new ArrayList<>();
	@Override
	public List<AStack> getRepairMaterials() {

		if(!repair.isEmpty())
			return repair;

		repair.add(new OreDictStack(OreDictManager.STEEL.plate(), 6));
		return repair;
	}

	@Override
	public void repair(EntityPlayer player) {
		this.hasExploded = false;
		this.markChanged();
	}

	@Override
	public String[] getFunctionInfo() {
		return new String[] {
				PREFIX_VALUE + "type",
				PREFIX_VALUE + "fill",
				PREFIX_VALUE + "fillpercent",
				PREFIX_FUNCTION + "setmode" + NAME_SEPARATOR + "mode (0-3)",
				PREFIX_FUNCTION + "setmode" + NAME_SEPARATOR + "mode" + PARAM_SEPARATOR + "fallback (0-3)",
		};
	}

	@Override
	public String provideRORValue(String name) {
		if((PREFIX_VALUE + "type").equals(name))		return tank.getTankType().getName();
		if((PREFIX_VALUE + "fill").equals(name))		return "" + tank.getFill();
		if((PREFIX_VALUE + "fillpercent").equals(name))	return "" + (tank.getFill() * 100 / tank.getMaxFill());
		return null;
	}

	@Override
	public String runRORFunction(String name, String[] params) {
		
		if((PREFIX_FUNCTION + "setmode").equals(name) && params.length > 0) {
			int mode = IRORInteractive.parseInt(params[0], 0, 3);
			
			if(mode != this.mode) {
				this.mode = (short) mode;
				this.markChanged();
				return null;
			} else if(params.length > 1) {
				int altmode = IRORInteractive.parseInt(params[1], 0, 3);
				this.mode = (short) altmode;
				this.markChanged();
				return null;
			}
			return null;
		}
		return null;
	}

	// ===== Forge Fluid Handler Implementation =====
	
	@Override
	public int fill(ForgeDirection from, FluidStack resource, boolean doFill) {
		if (resource == null || resource.amount <= 0 || !canFillFrom(from) || tank.getPressure() != 0) {
			return 0;
		}
		com.hbm.inventory.fluid.FluidType ntmFluid = FluidMappingRegistry.getHbmFluidType(resource.getFluid());
		if (ntmFluid == com.hbm.inventory.fluid.Fluids.NONE) {
			return 0;
		}
		int currentFill = tank.getFill();
		com.hbm.inventory.fluid.FluidType currentType = tank.getTankType();
		if (currentType != com.hbm.inventory.fluid.Fluids.NONE && currentType != ntmFluid) {
			return 0;
		}
		int maxFill = tank.getMaxFill();
		int fillAmount = Math.min(resource.amount, maxFill - currentFill);
		if (fillAmount <= 0) {
			return 0;
		}
		if (doFill) {
			if (currentType == com.hbm.inventory.fluid.Fluids.NONE) {
				tank.setTankType(ntmFluid);
			}
			tank.setFill(currentFill + fillAmount);
			this.markDirty();
		}
		FluidDebug.event("fluidtank.fill|" + FluidDebug.tileKey(this) + "|" + ntmFluid.getName(),
			() -> "FLUIDTANK fill " + FluidDebug.describeTile(this) + " side=" + from + " " + FluidDebug.describeForgeStack(resource) + " doFill=" + doFill + " => " + fillAmount + " by " + FluidDebug.callerHint());
		return fillAmount;
	}

	@Override
	public FluidStack drain(ForgeDirection from, FluidStack resource, boolean doDrain) {
		if (resource == null || resource.amount <= 0 || !canDrainFrom(from) || tank.getPressure() != 0) {
			return null;
		}
		com.hbm.inventory.fluid.FluidType ntmFluid = FluidMappingRegistry.getHbmFluidType(resource.getFluid());
		if (ntmFluid == com.hbm.inventory.fluid.Fluids.NONE) {
			return null;
		}
		int currentFill = tank.getFill();
		com.hbm.inventory.fluid.FluidType currentType = tank.getTankType();
		if (currentFill <= 0 || currentType != ntmFluid) {
			return null;
		}
		int drainAmount = Math.min(resource.amount, currentFill);
		if (drainAmount <= 0) {
			return null;
		}
		if (doDrain) {
			tank.setFill(currentFill - drainAmount);
			this.markDirty();
		}
		Fluid forgeFluid = FluidMappingRegistry.getForgeFluid(currentType);
		if (forgeFluid != null) {
			FluidStack drained = new FluidStack(forgeFluid, drainAmount);
			FluidDebug.event("fluidtank.drain|" + FluidDebug.tileKey(this) + "|" + ntmFluid.getName(),
				() -> "FLUIDTANK drain " + FluidDebug.describeTile(this) + " side=" + from + " " + FluidDebug.describeForgeStack(resource) + " doDrain=" + doDrain + " => " + FluidDebug.describeForgeStack(drained) + " by " + FluidDebug.callerHint());
			return drained;
		}
		return null;
	}

	@Override
	public FluidStack drain(ForgeDirection from, int maxDrain, boolean doDrain) {
		if (maxDrain <= 0 || !canDrainFrom(from) || tank.getPressure() != 0) {
			return null;
		}
		int currentFill = tank.getFill();
		com.hbm.inventory.fluid.FluidType currentType = tank.getTankType();
		if (currentFill <= 0 || currentType == com.hbm.inventory.fluid.Fluids.NONE) {
			return null;
		}
		Fluid forgeFluid = FluidMappingRegistry.getForgeFluid(currentType);
		if (forgeFluid == null) {
			return null;
		}
		int drainAmount = Math.min(maxDrain, currentFill);
		if (drainAmount <= 0) {
			return null;
		}
		if (doDrain) {
			tank.setFill(currentFill - drainAmount);
			this.markDirty();
		}
		FluidStack drainedAll = new FluidStack(forgeFluid, drainAmount);
		FluidDebug.event("fluidtank.drainall|" + FluidDebug.tileKey(this) + "|" + currentType.getName(),
			() -> "FLUIDTANK drain-all " + FluidDebug.describeTile(this) + " side=" + from + " maxDrain=" + maxDrain + " doDrain=" + doDrain + " => " + FluidDebug.describeForgeStack(drainedAll) + " by " + FluidDebug.callerHint());
		return drainedAll;
	}

	@Override
	public boolean canFill(ForgeDirection from, Fluid fluid) {
		if (this.hasExploded || tank.getPressure() != 0) {
			return false;
		}
		if (fluid == null) {
			return tank.getFill() < tank.getMaxFill();
		}
		if (!canFillFrom(from)) {
			return false;
		}
		com.hbm.inventory.fluid.FluidType ntmFluid = FluidMappingRegistry.getHbmFluidType(fluid);
		if (ntmFluid == com.hbm.inventory.fluid.Fluids.NONE) {
			return false;
		}
		int currentFill = tank.getFill();
		com.hbm.inventory.fluid.FluidType currentType = tank.getTankType();
		int maxFill = tank.getMaxFill();
		return currentFill < maxFill && (currentType == com.hbm.inventory.fluid.Fluids.NONE || currentType == ntmFluid);
	}

	@Override
	public boolean canDrain(ForgeDirection from, Fluid fluid) {
		if (this.hasExploded || tank.getPressure() != 0) {
			return false;
		}
		if (fluid == null) {
			return tank.getFill() > 0 && tank.getTankType() != com.hbm.inventory.fluid.Fluids.NONE;
		}
		if (!canDrainFrom(from)) {
			return false;
		}
		com.hbm.inventory.fluid.FluidType ntmFluid = FluidMappingRegistry.getHbmFluidType(fluid);
		if (ntmFluid == com.hbm.inventory.fluid.Fluids.NONE) {
			return false;
		}
		int currentFill = tank.getFill();
		com.hbm.inventory.fluid.FluidType currentType = tank.getTankType();
		return currentFill > 0 && currentType == ntmFluid;
	}

	@Override
	public FluidTankInfo[] getTankInfo(ForgeDirection from) {
		if (tank.getPressure() != 0) return new FluidTankInfo[0];
		int currentFill = tank.getFill();
		int maxFill = tank.getMaxFill();
		com.hbm.inventory.fluid.FluidType currentType = tank.getTankType();
		FluidStack stack = null;
		if (currentFill > 0 && currentType != com.hbm.inventory.fluid.Fluids.NONE) {
			Fluid forgeFluid = FluidMappingRegistry.getForgeFluid(currentType);
			if (forgeFluid != null) {
				stack = new FluidStack(forgeFluid, currentFill);
			}
		}
		final FluidStack stackFinal = stack;
		final int maxFillFinal = maxFill;
		FluidDebug.event("fluidtank.tankinfo|" + FluidDebug.tileKey(this),
			() -> "FLUIDTANK getTankInfo " + FluidDebug.describeTile(this) + " side=" + from + " => [" + (stackFinal == null ? "empty" : FluidDebug.describeForgeStack(stackFinal)) + "/" + maxFillFinal + "] by " + FluidDebug.callerHint());
		return new FluidTankInfo[] { new FluidTankInfo(stack, maxFill) };
	}

	private boolean canFillFrom(ForgeDirection from) {
		return !this.hasExploded && (mode == 0 || mode == 1);
	}

	private boolean canDrainFrom(ForgeDirection from) {
		return !this.hasExploded && (mode == 1 || mode == 2);
	}

	@Override
	public boolean canInputFluid(ForgeDirection from) { return canFillFrom(from) && tank.getPressure() == 0; }

	@Override
	public boolean canOutputFluid(ForgeDirection from) { return canDrainFrom(from) && tank.getPressure() == 0; }
}
