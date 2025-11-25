package com.hbm.blocks.machine;

import java.util.ArrayList;
import java.util.List;

import com.hbm.blocks.ILookOverlay;
import com.hbm.blocks.ITooltipProvider;
import com.hbm.dim.CelestialBody;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.tank.FluidTank;
import com.hbm.items.ModItems;
import com.hbm.tileentity.TileEntityProxyCombo;
import com.hbm.tileentity.machine.TileEntityOrbitalStationLauncher;
import com.hbm.util.BobMathUtil;
import com.hbm.util.i18n.I18nUtil;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.world.World;
import net.minecraftforge.client.event.RenderGameOverlayEvent.Pre;

public class BlockOrbitalStationLauncher extends BlockOrbitalStation implements ITooltipProvider {

	public BlockOrbitalStationLauncher(Material mat) {
		super(mat);
	}

	@Override
	public TileEntity createNewTileEntity(World world, int meta) {
		if(meta >= 12) return new TileEntityOrbitalStationLauncher();
		if(meta >= 6) return new TileEntityProxyCombo(true, true, true);
		return null;
	}

	@Override
	public void breakBlock(World world, int x, int y, int z, Block block, int meta) {
		TileEntity te = world.getTileEntity(x, y, z);
		if(te instanceof TileEntityOrbitalStationLauncher) ((TileEntityOrbitalStationLauncher) te).isBreaking = true;
		super.breakBlock(world, x, y, z, block, meta);
	}

	@Override
	public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float hitX, float hitY, float hitZ) {
		if(!CelestialBody.inOrbit(world)) return false;

		int[] pos = this.findCore(world, x, y, z);

		if(pos == null)
			return false;

		// If activating the side blocks, ignore, to allow placing
		if(Math.abs(pos[0] - x) >= 2 || Math.abs(pos[2] - z) >= 2)
			return false;

		TileEntity te = world.getTileEntity(pos[0], pos[1], pos[2]);

		if(!(te instanceof TileEntityOrbitalStationLauncher))
			return false;

		TileEntityOrbitalStationLauncher station = (TileEntityOrbitalStationLauncher) te;

		if(world.isRemote) return true;

		if(station.hasDocked) {
			if(!station.hasRider) {
				station.enterCapsule(player);
			}
			return true;
		}

		return standardOpenBehavior(world, x, y, z, player, 0);
	}

	@SuppressWarnings("rawtypes")
	@Override
	public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean ext) {
		addStandardInfo(stack, player, list, ext);
	}

	@Override
	public void printHook(Pre event, World world, int x, int y, int z) {
		if(!CelestialBody.inOrbit(world)) {
			List<String> text = new ArrayList<String>();
			text.add("&[" + (BobMathUtil.getBlink() ? 0xff0000 : 0xffff00) + "&]! ! ! " + I18nUtil.resolveKey("atmosphere.noOrbit") + " ! ! !");
			ILookOverlay.printGeneric(event, I18nUtil.resolveKey(getUnlocalizedName() + ".name"), 0xffff00, 0x404000, text);
			return;
		}

		int[] pos = this.findCore(world, x, y, z);

		if(pos == null)
			return;

		TileEntity te = world.getTileEntity(pos[0], pos[1], pos[2]);

		if(!(te instanceof TileEntityOrbitalStationLauncher))
			return;

		TileEntityOrbitalStationLauncher pad = (TileEntityOrbitalStationLauncher) te;

		List<String> text = new ArrayList<String>();
		if(pad.hasDocked) {
			if(!pad.hasRider) {
				text.add(I18nUtil.resolveKey("station.enterRocket"));
			} else {
				text.add(EnumChatFormatting.YELLOW + I18nUtil.resolveKey("station.occupiedRocket"));
			}

			ILookOverlay.printGeneric(event, I18nUtil.resolveKey(getUnlocalizedName() + ".name"), 0xffff00, 0x404000, text);
			return;
		}

		if(pad.rocket == null || !pad.rocket.validate()) return;

		text.add("Required fuels:");

		for(int i = 0; i < pad.tanks.length; i++) {
			FluidTank tank = pad.tanks[i];
			if(tank.getTankType() == Fluids.NONE) continue;
			text.add(EnumChatFormatting.GREEN + "-> " + EnumChatFormatting.RESET + tank.getTankType().getLocalizedName() + ": " + tank.getFill() + "/" + tank.getMaxFill() + "mB");
		}

		if(pad.solidFuel.max > 0) {
			text.add(EnumChatFormatting.GREEN + "-> " + EnumChatFormatting.RESET + I18nUtil.resolveKey(ModItems.rocket_fuel.getUnlocalizedName() + ".name") + ": " + pad.solidFuel.level + "/" + pad.solidFuel.max + "kg");
		}

		if(text.size() <= 1) return;

		ILookOverlay.printGeneric(event, I18nUtil.resolveKey(getUnlocalizedName() + ".name"), 0xffff00, 0x404000, text);
	}

}
