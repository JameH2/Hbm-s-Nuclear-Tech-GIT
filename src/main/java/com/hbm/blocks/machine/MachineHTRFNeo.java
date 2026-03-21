package com.hbm.blocks.machine;

import java.util.ArrayList;
import java.util.List;

import com.hbm.blocks.BlockDummyable;
import com.hbm.blocks.ILookOverlay;
import com.hbm.blocks.ITooltipProvider;
import com.hbm.dim.CelestialBody;
import com.hbm.tileentity.TileEntityProxyCombo;
import com.hbm.tileentity.machine.TileEntityMachineHTRNeo;
import com.hbm.util.BobMathUtil;
import com.hbm.util.i18n.I18nUtil;

import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.world.World;
import net.minecraftforge.client.event.RenderGameOverlayEvent.Pre;
import net.minecraftforge.common.util.ForgeDirection;

public class MachineHTRFNeo extends BlockDummyable implements ILookOverlay, ITooltipProvider {

	public MachineHTRFNeo() {
		super(Material.iron);
	}

	@Override
	public TileEntity createNewTileEntity(World world, int meta) {
		if(meta >= 12) return new TileEntityMachineHTRNeo();
		if(meta >= 6) return new TileEntityProxyCombo(false, true, true);
		return null;
	}

	@Override
	public int[] getDimensions() {
		return new int[] {2, 2, 2, 2, 11, 9};
	}

	@Override
	public int getOffset() {
		return 11;
	}

	@Override
	public int getHeightOffset() {
		return 2;
	}

	@Override
	public ForgeDirection getDirModified(ForgeDirection dir) {
		return dir.getRotation(ForgeDirection.DOWN);
	}

	@Override
	public void fillSpace(World world, int x, int y, int z, ForgeDirection dir, int o) {
		super.fillSpace(world, x, y, z, dir, o);

		ForgeDirection rot = dir.getRotation(ForgeDirection.UP);

		x += dir.offsetX * o;
		z += dir.offsetZ * o;

		this.makeExtra(world, x - rot.offsetX * 9, y, z - rot.offsetZ * 9);
		this.makeExtra(world, x - rot.offsetX * 5 - dir.offsetX * 3, y - 2, z - rot.offsetZ * 5 - dir.offsetZ * 2);
		this.makeExtra(world, x + rot.offsetX * 1 - dir.offsetX * 3, y - 2, z + rot.offsetZ * 1 - dir.offsetZ * 2);
		this.makeExtra(world, x - rot.offsetX * 5 - dir.offsetX * 3, y - 2, z - rot.offsetZ * 5 + dir.offsetZ * 2);
		this.makeExtra(world, x + rot.offsetX * 1 - dir.offsetX * 3, y - 2, z + rot.offsetZ * 1 + dir.offsetZ * 2);
	}

	@Override
	public void printHook(Pre event, World world, int x, int y, int z) {
		if(!CelestialBody.inOrbit(world)) return;

		int[] pos = this.findCore(world, x, y, z);

		if(pos == null) return;

		TileEntity te = world.getTileEntity(pos[0], pos[1], pos[2]);

		if(!(te instanceof TileEntityMachineHTRNeo))
			return;

		TileEntityMachineHTRNeo thruster = (TileEntityMachineHTRNeo) te;

		List<String> text = new ArrayList<String>();

		if(!thruster.isFacingPrograde()) {
			text.add("&[" + (BobMathUtil.getBlink() ? 0xff0000 : 0xffff00) + "&]! ! ! " + I18nUtil.resolveKey("atmosphere.engineFacing") + " ! ! !");
		} else {
			text.add("Plasma Energy: " + (thruster.plasmaEnergy == 0 ? EnumChatFormatting.RED : EnumChatFormatting.GREEN) + BobMathUtil.getShortNumber(thruster.plasmaEnergy) + "TU");

			text.add("Power: " + (thruster.power < thruster.getMaxPower() ? EnumChatFormatting.RED : EnumChatFormatting.GREEN) + BobMathUtil.getShortNumber(thruster.power) + "HE");

			int heat = (int) Math.ceil(thruster.temperature);
			String label = (heat > 123 ? EnumChatFormatting.RED : EnumChatFormatting.AQUA) + "" + heat + "K";
			text.add("Temperature: " + label);

			text.add(EnumChatFormatting.GREEN + "-> " + EnumChatFormatting.RESET + thruster.coolantTanks[0].getTankType().getLocalizedName() + ": " + thruster.coolantTanks[0].getFill() + "/" + thruster.coolantTanks[0].getMaxFill() + "mB");
			text.add(EnumChatFormatting.RED + "<- " + EnumChatFormatting.RESET + thruster.coolantTanks[1].getTankType().getLocalizedName() + ": " + thruster.coolantTanks[1].getFill() + "/" + thruster.coolantTanks[1].getMaxFill() + "mB");

			if(!thruster.isCool()) text.add("&[" + (BobMathUtil.getBlink() ? 0xff0000 : 0xffff00) + "&]! ! ! INSUFFICIENT COOLING ! ! !");

			if(world.getTileEntity(x, y, z) instanceof TileEntityProxyCombo) {
				if(pos[0] == x || pos[2] == z) {
					text.add("Connect to Fusion Reactor from here");
				}
			}
		}

		ILookOverlay.printGeneric(event, I18nUtil.resolveKey(getUnlocalizedName() + ".name"), 0xffff00, 0x404000, text);
	}

	@Override
	public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean ext) {
		this.addStandardInfo(stack, player, list, ext);
	}

}
