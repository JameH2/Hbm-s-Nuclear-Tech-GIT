package com.hbm.blocks;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.block.BlockVine;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class BlockVinylVine extends BlockVine {

	public BlockVinylVine() {
		super();
	}

	public void onNeighborBlockChange(World world, int x, int y, int z, Block neighborBlockId) {
		super.onNeighborBlockChange(world, x, y, z, neighborBlockId);
	}

	@Override
	public boolean canPlaceBlockAt(World world, int x, int y, int z) {
		return super.canPlaceBlockAt(world, x, y, z) && this.canBlockStay(world, x, y, z);
	}

	protected boolean canPlaceBlockOn(Block block) {
		return !block.isOpaqueCube() || block instanceof BlockRubberLeaves;
	}

	@Override
	public boolean canBlockStay(World world, int x, int y, int z) {
		Block blockBelow = world.getBlock(x, y - 1, z);
		return !blockBelow.isOpaqueCube() || blockBelow instanceof BlockRubberLeaves || super.canBlockStay(world, x, y, z);
	}

	@SideOnly(Side.CLIENT)
	@Override
	public int getBlockColor() {
		return 0xFFFFFF;
	}

	/**
	 * Returns the color this block should be rendered. Used by leaves.
	 */
	@SideOnly(Side.CLIENT)
	@Override
	public int getRenderColor(int p_149741_1_) {
		return 0xFFFFFF;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public int colorMultiplier(IBlockAccess p_149720_1_, int p_149720_2_, int p_149720_3_, int p_149720_4_) {
		return 0xFFFFFF;
	}

}
