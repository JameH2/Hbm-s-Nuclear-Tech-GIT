package com.hbm.blocks.machine;

import java.util.ArrayList;
import java.util.List;

import com.hbm.blocks.BlockDummyable;
import com.hbm.blocks.ILookOverlay;
import com.hbm.blocks.ITooltipProvider;
import com.hbm.handler.MultiblockHandlerXR;
import com.hbm.tileentity.TileEntityProxyCombo;
import com.hbm.tileentity.machine.TileEntityHydroponic;
import com.hbm.util.BobMathUtil;
import com.hbm.util.i18n.I18nUtil;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.client.event.RenderGameOverlayEvent.Pre;
import net.minecraftforge.common.IPlantable;
import net.minecraftforge.common.util.ForgeDirection;

public class MachineHydroponic extends BlockDummyable implements ILookOverlay, ITooltipProvider {

	public MachineHydroponic(Material mat) {
		super(mat);

		// Middle
		this.bounding.add(AxisAlignedBB.getBoundingBox(-0.5, 0, -1.5, 0.5, 3, 1.5));

		// Sides
		this.bounding.add(AxisAlignedBB.getBoundingBox(-1.125, 1, -2.5, 1.125, 3, -1.5));
		this.bounding.add(AxisAlignedBB.getBoundingBox(-1.125, 1, 1.5, 1.125, 3, 2.5));

		// Side butts
		this.bounding.add(AxisAlignedBB.getBoundingBox(-0.75, 0, -2.5, 0.75, 1, -1.5));
		this.bounding.add(AxisAlignedBB.getBoundingBox(-0.75, 0, 1.5, 0.75, 1, 2.5));

	}

	@Override
	public TileEntity createNewTileEntity(World world, int meta) {
		if(meta >= 12) return new TileEntityHydroponic();
		if(meta >= 6) return new TileEntityProxyCombo(true, true, true);
		return null;
	}

	@Override
	public int[] getDimensions() {
		return new int[] {2, 0, 0, 0, 2, 2};
	}

	@Override
	public int getOffset() {
		return 0;
	}

	@Override
	protected boolean checkRequirement(World world, int x, int y, int z, ForgeDirection dir, int o) {
		if(!super.checkRequirement(world, x, y, z, dir, o)) return false;

		if(!MultiblockHandlerXR.checkSpace(world, x + dir.offsetX * o, y + dir.offsetY * o, z + dir.offsetZ * o, new int[] {2, -1, 1, 1, 2, 2}, x, y, z, dir)) return false;

		return true;
	}

	@Override
	protected void fillSpace(World world, int x, int y, int z, ForgeDirection dir, int o) {
		x += dir.offsetX * o;
		z += dir.offsetZ * o;

		ForgeDirection rot = dir.getRotation(ForgeDirection.UP);

		// Hollow machines hate this weird trick
		// ABRA CHUPACADABRA

		// Front/Back
		MultiblockHandlerXR.fillSpace(world, x + rot.offsetX * 2 + dir.offsetX, y + 2, z + rot.offsetZ * 2 + dir.offsetZ, new int[] {0, 1, 0, 0, 0, 4}, this, dir);
		MultiblockHandlerXR.fillSpace(world, x + rot.offsetX * 2 - dir.offsetX, y + 2, z + rot.offsetZ * 2 - dir.offsetZ, new int[] {0, 1, 0, 0, 0, 4}, this, dir);

		// Top
		MultiblockHandlerXR.fillSpace(world, x + rot.offsetX * 2, y + 2, z + rot.offsetZ * 2, new int[] {0, 0, 0, 0, 0, 4}, this, dir);

		// Side caps
		MultiblockHandlerXR.fillSpace(world, x + rot.offsetX * 2, y + 1, z + rot.offsetZ * 2, new int[] {1, 0, 1, 1, 0, 0}, this, dir);
		MultiblockHandlerXR.fillSpace(world, x - rot.offsetX * 2, y + 1, z - rot.offsetZ * 2, new int[] {1, 0, 1, 1, 0, 0}, this, dir);
		MultiblockHandlerXR.fillSpace(world, x, y, z, new int[] {2, 0, 0, 0, -2, 2}, this, dir);
		MultiblockHandlerXR.fillSpace(world, x, y, z, new int[] {2, 0, 0, 0, 2, -2}, this, dir);

		// Top connector needs to point laterally to avoid disconnecting when light blocks are added
		MultiblockHandlerXR.fillSpace(world, x + dir.offsetX, y + 2, z + dir.offsetZ, new int[] {0, 0, 1, 0, 0, 0}, this, dir);

		// Base
		MultiblockHandlerXR.fillSpace(world, x, y, z, new int[] {0, 0, 0, 0, 2, 2}, this, dir);

		makeExtra(world, x + rot.offsetX * 2, y, z + rot.offsetZ * 2);
		makeExtra(world, x - rot.offsetX * 2, y, z - rot.offsetZ * 2);
		makeExtra(world, x, y + 2, z);
	}

	// Only the blocks immediately horizontally adjacent to the core can actually sustain plants, so we do a 4 block search in all cardinals
	@Override
	public boolean canSustainPlant(IBlockAccess world, int x, int y, int z, ForgeDirection direction, IPlantable plantable) {
		if(world.getBlockMetadata(x, y, z) >= 12
		|| world.getBlockMetadata(x + 1, y, z) >= 12
		|| world.getBlockMetadata(x - 1, y, z) >= 12
		|| world.getBlockMetadata(x, y, z + 1) >= 12
		|| world.getBlockMetadata(x, y, z - 1) >= 12)
			return true;
		return false;
	}

	@SideOnly(Side.CLIENT)
	@Override
	public void printHook(Pre event, World world, int x, int y, int z) {
		int[] pos = this.findCore(world, x, y, z);

		if(pos == null)
			return;

		TileEntity te = world.getTileEntity(pos[0], pos[1], pos[2]);

		if(!(te instanceof TileEntityHydroponic))
			return;

		TileEntityHydroponic hydro = (TileEntityHydroponic) te;

		List<String> text = new ArrayList<String>();

		text.add(((hydro.getPower() <= 200 ? EnumChatFormatting.RED : EnumChatFormatting.GREEN)) + I18nUtil.resolveKey("overlay.common.power", BobMathUtil.getShortNumber(hydro.getPower())));

		text.add((EnumChatFormatting.GREEN) + "-> " + EnumChatFormatting.RESET + I18nUtil.resolveKey("overlay.common.tank_input", hydro.tanks[0].getTankType().getLocalizedName(), hydro.tanks[0].getFill(), hydro.tanks[0].getMaxFill()));
		text.add((EnumChatFormatting.RED) + "<- " + EnumChatFormatting.RESET + I18nUtil.resolveKey("overlay.common.tank_output", hydro.tanks[1].getTankType().getLocalizedName(), hydro.tanks[1].getFill(), hydro.tanks[1].getMaxFill()));

		ILookOverlay.printGeneric(event, I18nUtil.resolveKey(getUnlocalizedName() + ".name"), 0xffff00, 0x404000, text);
	}

	@Override
	public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float hitX, float hitY, float hitZ) {
		return this.standardOpenBehavior(world, x, y, z, player, 0);
	}

	@SuppressWarnings("rawtypes")
	@Override
	public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean ext) {
		addStandardInfo(stack, player, list, ext);
	}

}
