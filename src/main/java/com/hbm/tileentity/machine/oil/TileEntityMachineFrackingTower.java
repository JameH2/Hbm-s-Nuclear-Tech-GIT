package com.hbm.tileentity.machine.oil;

import java.io.IOException;
import java.util.List;

import com.google.gson.JsonObject;
import com.google.gson.stream.JsonWriter;
import com.hbm.blocks.ModBlocks;
import com.hbm.blocks.generic.BlockOreFluid;
import com.hbm.dim.SolarSystem;
import com.hbm.inventory.container.ContainerMachineOilWell;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.tank.FluidTank;
import com.hbm.inventory.gui.GUIMachineOilWell;
import com.hbm.items.machine.ItemMachineUpgrade.UpgradeType;
import com.hbm.lib.Library;
import com.hbm.tileentity.IConfigurableMachine;
import com.hbm.tileentity.IUpgradeInfoProvider;
import com.hbm.util.BobMathUtil;
import com.hbm.util.fauxpointtwelve.DirPos;
import com.hbm.util.i18n.I18nUtil;
import com.hbm.world.feature.OilSpot;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.world.World;

public class TileEntityMachineFrackingTower extends TileEntityOilDrillBase {

	protected static int maxPower = 5_000_000;
	protected static int consumption = 5000;
	protected static int solutionRequired = 10;
	protected static int delay = 20;
	protected static int destructionRange = 75;

	public TileEntityMachineFrackingTower() {
		super();
		tanks = new FluidTank[3];
		tanks[0] = new FluidTank(Fluids.OIL, 64_000);
		tanks[1] = new FluidTank(Fluids.GAS, 64_000);
		tanks[2] = new FluidTank(Fluids.FRACKSOL, 64_000);
	}

	@Override
	public String getName() {
		return "container.frackingTower";
	}

	@Override
	public long getMaxPower() {
		return maxPower;
	}

	@Override
	public int getPowerReq() {
		return consumption;
	}

	@Override
	public int getDelay() {
		return delay;
	}

	@Override
	public int getDrillDepth() {
		return 0;
	}

	@Override
	public boolean canPump() {
		boolean b = this.tanks[2].getFill() >= solutionRequired;

		if(!b) {
			this.indicator = 3;
		}

		return b;
	}

	@Override
	public boolean canSuckBlock(Block b) {
		return super.canSuckBlock(b) || b == ModBlocks.ore_bedrock_oil;
	}

	@Override
	public void onSuck(BlockOreFluid block, int x, int y, int z) {
		super.onSuck(block, x, y, z);

		int meta = worldObj.getBlockMetadata(x, y, z);

		tanks[2].setFill(tanks[2].getFill() - solutionRequired);

		if(meta == SolarSystem.Body.TEKTO.ordinal()) {
			OilSpot.generateCrack(worldObj, xCoord, zCoord, destructionRange, 10);
		} else {
			OilSpot.generateOilSpot(worldObj, xCoord, zCoord, destructionRange, 10, false);
		}
	}

	@Override
	public FluidTank[] getSendingTanks() {
		return new FluidTank[] { tanks[0], tanks[1] };
	}

	@Override
	public FluidTank[] getReceivingTanks() {
		return new FluidTank[] { tanks[2] };
	}

	@Override
	public FluidTank[] getAllTanks() {
		return tanks;
	}

	@Override
	public DirPos[] getConPos() {
		return new DirPos[] {
				new DirPos(xCoord + 1, yCoord, zCoord, Library.POS_X),
				new DirPos(xCoord - 1, yCoord, zCoord, Library.NEG_X),
				new DirPos(xCoord, yCoord, zCoord + 1, Library.POS_Z),
				new DirPos(xCoord, yCoord, zCoord - 1, Library.NEG_Z)
		};
	}

	@Override
	protected void updateConnections() {
		for(DirPos pos : getConPos()) {
			this.trySubscribe(worldObj, pos.getX(), pos.getY(), pos.getZ(), pos.getDir());
			this.trySubscribe(tanks[2].getTankType(), worldObj, pos.getX(), pos.getY(), pos.getZ(), pos.getDir());
		}
	}

	@Override
	public String getConfigName() {
		return "frackingtower";
	}

	@Override
	public void readIfPresent(JsonObject obj) {
		maxPower = IConfigurableMachine.grab(obj, "I:powerCap", maxPower);
		consumption = IConfigurableMachine.grab(obj, "I:consumption", consumption);
		solutionRequired = IConfigurableMachine.grab(obj, "I:solutionRequired", solutionRequired);
		delay = IConfigurableMachine.grab(obj, "I:delay", delay);
		destructionRange = IConfigurableMachine.grab(obj, "I:destructionRange", destructionRange);
	}

	@Override
	public void writeConfig(JsonWriter writer) throws IOException {
		writer.name("I:powerCap").value(maxPower);
		writer.name("I:consumption").value(consumption);
		writer.name("I:solutionRequired").value(solutionRequired);
		writer.name("I:delay").value(delay);
		writer.name("I:destructionRange").value(destructionRange);
	}

	@Override
	public Container provideContainer(int ID, EntityPlayer player, World world, int x, int y, int z) {
		return new ContainerMachineOilWell(player.inventory, this);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public Object provideGUI(int ID, EntityPlayer player, World world, int x, int y, int z) {
		return new GUIMachineOilWell(player.inventory, this);
	}

	@Override
	public void provideInfo(UpgradeType type, int level, List<String> info, boolean extendedInfo) {
		info.add(IUpgradeInfoProvider.getStandardLabel(ModBlocks.machine_fracking_tower));
		if(type == UpgradeType.SPEED) {
			info.add(EnumChatFormatting.GREEN + I18nUtil.resolveKey(KEY_DELAY, "-" + (level * 25) + "%"));
			info.add(EnumChatFormatting.RED + I18nUtil.resolveKey(KEY_CONSUMPTION, "+" + (level * 25) + "%"));
		}
		if(type == UpgradeType.POWER) {
			info.add(EnumChatFormatting.GREEN + I18nUtil.resolveKey(KEY_CONSUMPTION, "-" + (level * 25) + "%"));
			info.add(EnumChatFormatting.RED + I18nUtil.resolveKey(KEY_DELAY, "+" + (level * 10) + "%"));
		}
		if(type == UpgradeType.AFTERBURN) {
			info.add(EnumChatFormatting.GREEN + I18nUtil.resolveKey(KEY_BURN, level * 10, level * 50));
		}
		if(type == UpgradeType.OVERDRIVE) {
			info.add((BobMathUtil.getBlink() ? EnumChatFormatting.RED : EnumChatFormatting.DARK_GRAY) + "YES");
		}
	}
}
