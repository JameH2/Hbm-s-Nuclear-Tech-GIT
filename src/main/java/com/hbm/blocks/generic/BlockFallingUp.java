package com.hbm.blocks.generic;

import java.util.Random;

import com.hbm.blocks.BlockFallingNT;
import com.hbm.entity.item.EntityFallingBlockNT;

import net.minecraft.block.Block;
import net.minecraft.block.BlockFalling;
import net.minecraft.world.World;

public class BlockFallingUp extends BlockFallingNT {

	public BlockFallingUp() {
		super();
		setTickRandomly(true);
	}

	// Don't schedule ticks, should only ever randomly fall up
	@Override public void onBlockAdded(World world, int x, int y, int z) {}
	@Override public void onNeighborBlockChange(World world, int x, int y, int z, Block block) {}

	// Reduce falling odds by a large margin
	@Override
	public void updateTick(World world, int x, int y, int z, Random rand) {
		if(rand.nextInt(100) != 0) return;
		super.updateTick(world, x, y, z, rand);
	}

	@Override
	protected void fall(World world, int x, int y, int z) {
		if(canFallThrough(world, x, y + 1, z)) {
			byte range = 32;

			if(!BlockFalling.fallInstantly && world.checkChunksExist(x - range, y - range, z - range, x + range, y + range, z + range)) {
				if(!world.isRemote) {
					EntityFallingBlockNT entityfallingblock = new EntityFallingBlockNT(world, x + 0.5D, y + 0.5D, z + 0.5D, this, world.getBlockMetadata(x, y, z));
					this.modifyFallingBlock(entityfallingblock);
					world.spawnEntityInWorld(entityfallingblock);
				}
			} else {
				world.setBlockToAir(x, y, z);

				while(canFallThrough(world, x, y + 1, z) && y < 256) {
					++y;
				}

				if(y < 256) {
					world.setBlock(x, y, z, this);
				}
			}
		}
	}

	@Override
	protected void modifyFallingBlock(EntityFallingBlockNT falling) {
		falling.fallingUp = true;
	}

}
