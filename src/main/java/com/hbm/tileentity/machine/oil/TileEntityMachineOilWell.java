package com.hbm.tileentity.machine.oil;

import java.io.IOException;
import java.util.List;

import com.google.gson.JsonObject;
import com.google.gson.stream.JsonWriter;
import com.hbm.blocks.ModBlocks;
import com.hbm.blocks.generic.BlockOreFluid;
import com.hbm.inventory.container.ContainerMachineOilWell;
import com.hbm.inventory.gui.GUIMachineOilWell;
import com.hbm.items.machine.ItemMachineUpgrade.UpgradeType;
import com.hbm.tileentity.IConfigurableMachine;
import com.hbm.tileentity.IUpgradeInfoProvider;
import com.hbm.tileentity.TilePortShapes;
import com.hbm.tileentity.TilePort.PortDef;
import com.hbm.util.BobMathUtil;
import com.hbm.util.i18n.I18nUtil;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.world.World;
import net.minecraftforge.oredict.OreDictionary;

public class TileEntityMachineOilWell extends TileEntityOilDrillBase {

	protected static int maxPower = 100_000;
	protected static int consumption = 100;
	protected static int delay = 50;
	
	public PortDef[] getPorts() { if(cachedPorts == null) cachedPorts = TilePortShapes.horizontal(xCoord, yCoord, zCoord); return cachedPorts; }

	@Override
	public String getName() {
		return "container.oilWell";
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
	public void onDrill(int y) {
		Block b = worldObj.getBlock(xCoord, y, zCoord);
		ItemStack stack = new ItemStack(b);
		int[] ids = OreDictionary.getOreIDs(stack);
		for(Integer i : ids) {
			String name = OreDictionary.getOreName(i);

			if("oreUranium".equals(name)) {
				for(int j = -1; j <= 1; j++) {
					for(int k = -1; k <= 1; k++) {
						if(worldObj.getBlock(xCoord + j, yCoord + 10, zCoord + j).isReplaceable(worldObj, xCoord + j, yCoord + 7, zCoord + k)) {
							worldObj.setBlock(xCoord + k, yCoord + 10, zCoord + k, ModBlocks.gas_radon_dense);
						}
					}
				}
			}

			if("oreAsbestos".equals(name)) {
				for(int j = -1; j <= 1; j++) {
					for(int k = -1; k <= 1; k++) {
						if(worldObj.getBlock(xCoord + j, yCoord + 10, zCoord + j).isReplaceable(worldObj, xCoord + j, yCoord + 7, zCoord + k)) {
							worldObj.setBlock(xCoord + k, yCoord + 10, zCoord + k, ModBlocks.gas_asbestos);
						}
					}
				}
			}
		}
	}

	@Override
	public void onSuck(BlockOreFluid block, int x, int y, int z) {
		worldObj.playSoundEffect(this.xCoord, this.yCoord, this.zCoord, "game.neutral.swim.splash", 2.0F, 0.5F);

		super.onSuck(block, x, y, z);
	}

	AxisAlignedBB bb = null;

	@Override
	public AxisAlignedBB getRenderBoundingBox() {

		if(bb == null) {
			bb = AxisAlignedBB.getBoundingBox(
					xCoord - 1,
					yCoord,
					zCoord - 1,
					xCoord + 2,
					yCoord + 10,
					zCoord + 2
					);
		}

		return bb;
	}

	@Override
	public String getConfigName() {
		return "derrick";
	}

	@Override
	public void readIfPresent(JsonObject obj) {
		maxPower = IConfigurableMachine.grab(obj, "I:powerCap", maxPower);
		consumption = IConfigurableMachine.grab(obj, "I:consumption", consumption);
		delay = IConfigurableMachine.grab(obj, "I:delay", delay);
	}

	@Override
	public void writeConfig(JsonWriter writer) throws IOException {
		writer.name("I:powerCap").value(maxPower);
		writer.name("I:consumption").value(consumption);
		writer.name("I:delay").value(delay);
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
		info.add(IUpgradeInfoProvider.getStandardLabel(ModBlocks.machine_well));
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
