package com.hbm.blocks.machine;

import java.util.ArrayList;
import java.util.List;

import com.hbm.blocks.BlockDummyable;
import com.hbm.blocks.ILookOverlay;
import com.hbm.blocks.ITooltipProvider;
import com.hbm.dim.CelestialBody;
import com.hbm.inventory.material.Mats;
import com.hbm.inventory.material.Mats.MaterialStack;
import com.hbm.tileentity.TileEntityProxyCombo;
import com.hbm.tileentity.machine.TileEntityMachineMagma;
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

public class MachineMagma extends BlockDummyable implements ILookOverlay, ITooltipProvider {

	public MachineMagma() {
		super(Material.iron);
	}

	@Override
	public TileEntity createNewTileEntity(World world, int meta) {
		if(meta >= 12) return new TileEntityMachineMagma();
		if(meta >= 6) return new TileEntityProxyCombo().power().fluid();
		return null;
	}

	@Override
	public int[] getDimensions() {
		return new int[] {3, 3, 3, 3, 3, 3};
	}

	@Override
	public int getOffset() {
		return 3;
	}

	@Override
	public int getHeightOffset() {
		return 3;
	}

	@Override
	protected void fillSpace(World world, int x, int y, int z, ForgeDirection dir, int o) {
		super.fillSpace(world, x, y, z, dir, o);

		x += dir.offsetX * o;
		y += dir.offsetY * o;
		z += dir.offsetZ * o;

		ForgeDirection rot = dir.getRotation(ForgeDirection.UP);
		this.makeExtra(world, x - dir.offsetX * 3, y - 1, z - dir.offsetZ * 3);
		this.makeExtra(world, x - dir.offsetX * 3, y - 2, z - dir.offsetZ * 3);
		this.makeExtra(world, x - dir.offsetX * 3 + rot.offsetX, y - 1, z - dir.offsetZ * 3 + rot.offsetZ);
		this.makeExtra(world, x - dir.offsetX * 3 - rot.offsetX, y - 1, z - dir.offsetZ * 3 - rot.offsetZ);
		this.makeExtra(world, x - dir.offsetX * 3 + rot.offsetX, y - 2, z - dir.offsetZ * 3 + rot.offsetZ);
		this.makeExtra(world, x - dir.offsetX * 3 - rot.offsetX, y - 2, z - dir.offsetZ * 3 - rot.offsetZ);
	}

	@Override
	public void printHook(Pre event, World world, int x, int y, int z) {
		int[] pos = this.findCore(world, x, y, z);

		if(pos == null)
			return;

		TileEntity te = world.getTileEntity(pos[0], pos[1], pos[2]);

		if(!(te instanceof TileEntityMachineMagma))
			return;

		TileEntityMachineMagma drill = (TileEntityMachineMagma) te;

		List<String> text = new ArrayList<String>();

		CelestialBody body = CelestialBody.getBody(world);

		if(body.name != "moho") {
			text.add(I18nUtil.resolveKey("overlay.magma.must_be_on_moho", (BobMathUtil.getBlink() ? 0xff0000 : 0xffff00)));
		} else if(!drill.validPosition) {
			text.add(I18nUtil.resolveKey("overlay.magma.insufficient_lava_found", (BobMathUtil.getBlink() ? 0xff0000 : 0xffff00)));
		} else {
			text.add(((drill.power < drill.consumption ? EnumChatFormatting.RED : EnumChatFormatting.GREEN)) + I18nUtil.resolveKey("overlay.common.power", BobMathUtil.getShortNumber(drill.power)));

			for(int i = 0; i < drill.tanks.length; i++)
				text.add(I18nUtil.resolveKey("overlay.common.tank", (i == 0 ? (EnumChatFormatting.GREEN + "-> ") : (EnumChatFormatting.RED + "<- ")) + EnumChatFormatting.RESET, drill.tanks[i].getTankType().getLocalizedName(), drill.tanks[i].getFill(), drill.tanks[i].getMaxFill()));

			for(MaterialStack sta : drill.liquids) {
				text.add(EnumChatFormatting.YELLOW + I18nUtil.resolveKey(sta.material.getUnlocalizedName()) + ": " + Mats.formatAmount(sta.amount, false));
			}
		}

		ILookOverlay.printGeneric(event, I18nUtil.resolveKey(getUnlocalizedName() + ".name"), 0xffff00, 0x404000, text);
	}

	@SuppressWarnings("rawtypes")
	@Override
	public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean ext) {
		addStandardInfo(stack, player, list, ext);
	}

}
