package com.hbm.blocks.generic;

import com.hbm.dim.dima.EventHandlerDima;
import com.hbm.entity.mob.EntityGhostTrapped;

import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class BlockDungeonController extends BlockContainer {

	public BlockDungeonController(Material mat) {
		super(mat);
	}

	@Override
	public TileEntity createNewTileEntity(World world, int meta) {
		return new TileEntityDungeonController();
	}

	@Override
	public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float hitX, float hitY, float hitZ) {
		if(!world.isRemote) {
			EntityGhostTrapped ghost = new EntityGhostTrapped(world);
			ghost.setLocationAndAngles(x + 32, y + 8, z, world.rand.nextFloat() * 360.0F, 0.0F);
			world.spawnEntityInWorld(ghost);
		}

		return true;
	}

	public void breakBlock(World world, int x, int y, int z, Block block, int meta) {
		super.breakBlock(world, x, y, z, block, meta);
		EventHandlerDima.removePsychoChunk(world, x, z);
	}

	public static class TileEntityDungeonController extends TileEntity {

		@Override
		public void updateEntity() {
			if(!worldObj.isRemote && !isInvalid()) {
				EventHandlerDima.addPsychoChunk(worldObj, xCoord, zCoord);
			}
		}

	}

}
