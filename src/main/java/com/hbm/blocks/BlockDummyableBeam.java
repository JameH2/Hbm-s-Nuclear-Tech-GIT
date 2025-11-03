package com.hbm.blocks;

import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.client.event.RenderGameOverlayEvent.Pre;
import net.minecraftforge.common.util.ForgeDirection;

public class BlockDummyableBeam extends BlockDummyable implements ILookOverlay {

	// Passes on any interactions to the true dummyable

	public BlockDummyableBeam(Material mat) {
		super(mat);
		setLightLevel(1.0F);
		setLightOpacity(0);
	}

	@Override public int getRenderType() { return -1; }

	@Override
	public TileEntity createNewTileEntity(World world, int meta) {
		return null;
	}

	@Override
	public int[] getDimensions() {
		return new int[] {0, 0, 0, 0, 0, 0};
	}

	@Override
	public int getOffset() {
		return 0;
	}

	@Override
	public int[] findCore(World world, int x, int y, int z) {
		int metadata = world.getBlockMetadata(x, y, z);

		// if it's an extra, remove the extra-ness
		if(metadata >= extra) metadata -= extra;

		ForgeDirection dir = ForgeDirection.getOrientation(metadata).getOpposite();

		x += dir.offsetX;
		y += dir.offsetY;
		z += dir.offsetZ;

		Block b = world.getBlock(x, y, z);

		if(b instanceof BlockDummyable && !(b instanceof BlockDummyableBeam)) {
			return ((BlockDummyable) b).findCore(world, x, y, z);
		}

		return null;
	}

	@Override
	public void breakBlock(World world, int x, int y, int z, Block block, int i) {
		int metadata = world.getBlockMetadata(x, y, z);

		// if it's an extra, remove the extra-ness
		if(metadata >= extra) metadata -= extra;

		ForgeDirection dir = ForgeDirection.getOrientation(metadata).getOpposite();

		x += dir.offsetX;
		y += dir.offsetY;
		z += dir.offsetZ;

		Block b = world.getBlock(x, y, z);

		if(b instanceof BlockDummyable) {
			((BlockDummyable) b).breakBlock(world, x, y, z, b, i);
		}
	}

	@Override
	public void onNeighborBlockChange(World world, int x, int y, int z, Block block) {
		if(world.isRemote || safeRem) return;

		int metadata = world.getBlockMetadata(x, y, z);

		// if it's an extra, remove the extra-ness
		if(metadata >= extra) metadata -= extra;

		ForgeDirection dir = ForgeDirection.getOrientation(metadata).getOpposite();
		Block b = world.getBlock(x + dir.offsetX, y + dir.offsetY, z + dir.offsetZ);

		if(!(b instanceof BlockDummyable)) {
			world.setBlockToAir(x, y, z);
		}
	}

	@Override
	public void updateTick(World world, int x, int y, int z, Random rand) {
		if(world.isRemote) return;

		int metadata = world.getBlockMetadata(x, y, z);

		// if it's an extra, remove the extra-ness
		if(metadata >= extra) metadata -= extra;

		ForgeDirection dir = ForgeDirection.getOrientation(metadata).getOpposite();
		Block b = world.getBlock(x + dir.offsetX, y + dir.offsetY, z + dir.offsetZ);

		if(!(b instanceof BlockDummyable)) {
			world.setBlockToAir(x, y, z);
		}
	}

	@Override
	public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float hitX, float hitY, float hitZ) {
		int metadata = world.getBlockMetadata(x, y, z);

		// if it's an extra, remove the extra-ness
		if(metadata >= extra) metadata -= extra;

		ForgeDirection dir = ForgeDirection.getOrientation(metadata).getOpposite();

		x += dir.offsetX;
		y += dir.offsetY;
		z += dir.offsetZ;

		Block b = world.getBlock(x, y, z);

		if(b instanceof BlockDummyable && !(b instanceof BlockDummyableBeam)) {
			return ((BlockDummyable) b).onBlockActivated(world, x, y, z, player, side, hitX, hitY, hitZ);
		}

		return false;
	}

	@Override
	public void printHook(Pre event, World world, int x, int y, int z) {
		int metadata = world.getBlockMetadata(x, y, z);

		// if it's an extra, remove the extra-ness
		if(metadata >= extra) metadata -= extra;

		ForgeDirection dir = ForgeDirection.getOrientation(metadata).getOpposite();

		x += dir.offsetX;
		y += dir.offsetY;
		z += dir.offsetZ;

		Block b = world.getBlock(x, y, z);

		if(b instanceof BlockDummyable && !(b instanceof BlockDummyableBeam) && b instanceof ILookOverlay) {
			((ILookOverlay) b).printHook(event, world, x, y, z);
		}
	}

}
