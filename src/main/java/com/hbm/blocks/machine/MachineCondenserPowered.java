package com.hbm.blocks.machine;

import com.hbm.blocks.BlockDummyable;
import com.hbm.blocks.ILookOverlay;
import com.hbm.tileentity.IPersistentNBT;
import com.hbm.tileentity.IRepairable;
import com.hbm.tileentity.TileEntityProxyCombo;
import com.hbm.tileentity.machine.TileEntityCondenserPowered;
import com.hbm.util.BobMathUtil;
import com.hbm.util.I18nUtil;

import api.hbm.block.IToolable;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.world.Explosion;
import net.minecraft.world.World;
import net.minecraftforge.client.event.RenderGameOverlayEvent.Pre;
import net.minecraftforge.common.util.ForgeDirection;

import java.util.ArrayList;
import java.util.List;

public class MachineCondenserPowered extends BlockDummyable implements ILookOverlay, IToolable {

	public MachineCondenserPowered(Material mat) {
		super(mat);
	}

	@Override
	public TileEntity createNewTileEntity(World p_149915_1_, int meta) {
		if(meta >= 12) return new TileEntityCondenserPowered();
		if(meta >= 6) return new TileEntityProxyCombo().power().fluid();
		return null;
	}

	@Override
	public int[] getDimensions() {
		return new int[] {2, 0, 1, 1, 3, 3};
	}

	@Override
	public int getOffset() {
		return 1;
	}

	@Override
	public void fillSpace(World world, int x, int y, int z, ForgeDirection dir, int o) {
		super.fillSpace(world, x, y, z, dir, o);

		x = x + dir.offsetX * o;
		z = z + dir.offsetZ * o;

		ForgeDirection rot = dir.getRotation(ForgeDirection.UP);

		this.makeExtra(world, x + rot.offsetX * 3, y + 1, z + rot.offsetZ * 3);
		this.makeExtra(world, x - rot.offsetX * 3, y + 1, z - rot.offsetZ * 3);
		this.makeExtra(world, x + dir.offsetX + rot.offsetX, y + 1, z + dir.offsetZ + rot.offsetZ);
		this.makeExtra(world, x + dir.offsetX - rot.offsetX, y + 1, z + dir.offsetZ - rot.offsetZ);
		this.makeExtra(world, x - dir.offsetX + rot.offsetX, y + 1, z - dir.offsetZ + rot.offsetZ);
		this.makeExtra(world, x - dir.offsetX - rot.offsetX, y + 1, z - dir.offsetZ - rot.offsetZ);
	}

	@Override
	public void printHook(Pre event, World world, int x, int y, int z) {
		int[] pos = this.findCore(world, x, y, z);

		if(pos == null)
			return;

		TileEntity te = world.getTileEntity(pos[0], pos[1], pos[2]);

		if(!(te instanceof TileEntityCondenserPowered)) return;

		TileEntityCondenserPowered tower = (TileEntityCondenserPowered) te;
		if(tower.damaged) {
			IRepairable.addGenericOverlay(event, world, x, y, z, this);
			return;
		}

		List<String> text = new ArrayList<>();

		text.add(BobMathUtil.getShortNumber(tower.power) + "HE / " + BobMathUtil.getShortNumber(TileEntityCondenserPowered.maxPower) + "HE");

		for(int i = 0; i < tower.tanks.length; i++)
			text.add((i < 1 ? (EnumChatFormatting.GREEN + "-> ") : (EnumChatFormatting.RED + "<- ")) + EnumChatFormatting.RESET + tower.tanks[i].getTankType().getLocalizedName() + ": " + tower.tanks[i].getFill() + "/" + tower.tanks[i].getMaxFill() + "mB");

		ILookOverlay.printGeneric(event, I18nUtil.resolveKey(getUnlocalizedName() + ".name"), 0xffff00, 0x404000, text);
	}

	@Override
	public boolean onScrew(World world, EntityPlayer player, int x, int y, int z, int side, float fX, float fY, float fZ, ToolType tool) {
		if(tool != ToolType.TORCH) return false;
		return IRepairable.tryRepairMultiblock(world, x, y, z, this, player);
	}

	@Override
	public ArrayList<ItemStack> getDrops(World world, int x, int y, int z, int metadata, int fortune) {
		return IPersistentNBT.getDrops(world, x, y, z, this);
	}

	@Override
	public boolean canDropFromExplosion(Explosion explosion) {
		return false;
	}

	@Override
	public void onBlockExploded(World world, int x, int y, int z, Explosion explosion) {

		int[] pos = this.findCore(world, x, y, z);
		if(pos == null) return;
		TileEntity core = world.getTileEntity(pos[0], pos[1], pos[2]);
		if(!(core instanceof TileEntityCondenserPowered)) return;

		TileEntityCondenserPowered tank = (TileEntityCondenserPowered) core;
		if(tank.lastExplosion == explosion) return;
		tank.lastExplosion = explosion;

		if(!tank.damaged) {
			tank.damaged = true;
			tank.markDirty();
		} else {
			world.setBlock(pos[0], pos[1], pos[2], Blocks.air);
		}
	}

}
