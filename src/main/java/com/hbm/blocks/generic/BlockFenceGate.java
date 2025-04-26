package com.hbm.blocks.generic;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.material.Material;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

public class BlockFenceGate extends BlockModDoor {

    public BlockFenceGate(Material mat) {
        super(mat);
    }

	private static boolean interacting = false;


	@SideOnly(Side.CLIENT)
	public AxisAlignedBB getSelectedBoundingBoxFromPool(World world, int x, int y, int z) {
		interacting = true;
		AxisAlignedBB bb = super.getSelectedBoundingBoxFromPool(world, x, y, z);
		interacting = false;
		return bb;
	}

	public MovingObjectPosition collisionRayTrace(World p_149731_1_, int p_149731_2_, int p_149731_3_, int p_149731_4_, Vec3 p_149731_5_, Vec3 p_149731_6_) {
		interacting = true;
		MovingObjectPosition mop = super.collisionRayTrace(p_149731_1_, p_149731_2_, p_149731_3_, p_149731_4_, p_149731_5_, p_149731_6_);
		interacting = false;
		return mop;
	}

	@Override
	protected void func_150011_b(int meta) {
        float f = 0.0625F;
		float f1 = 0.46875F;
		float f2 = 0.53125F;
		this.setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 2.0F, 1.0F);
		int j = meta & 3;
		boolean isOpen = !interacting && (meta & 4) != 0;
		boolean isOtherSide = (meta & 16) != 0;

		if (j == 0) {
			if (isOpen) {
				if (!isOtherSide) {
					this.setBlockBounds(0.5F, 0.0F, 0.0F, 1.5F, 1.0F, f);
				} else {
					this.setBlockBounds(0.5F, 0.0F, 1.0F - f, 1.5F, 1.0F, 1.0F);
				}
			} else {
				this.setBlockBounds(f1, 0.0F, 0.0F, f2, 1.0F, 1.0F);
			}
		} else if (j == 1) {
			if (isOpen) {
				if (!isOtherSide) {
					this.setBlockBounds(1.0F - f, 0.0F, 0.5F, 1.0F, 1.0F, 1.5F);
				} else {
					this.setBlockBounds(0.0F, 0.0F, 0.5F, f, 1.0F, 1.5F);
				}
			} else {
				this.setBlockBounds(0.0F, 0.0F, f1, 1.0F, 1.0F, f2);
			}
		} else if (j == 2) {
			if (isOpen) {
				if (!isOtherSide) {
					this.setBlockBounds(-0.5F, 0.0F, 1.0F - f, 0.5F, 1.0F, 1.0F);
				} else {
					this.setBlockBounds(-0.5F, 0.0F, 0.0F, 0.5F, 1.0F, f);
				}
			} else {
				this.setBlockBounds(f1, 0.0F, 0.0F, f2, 1.0F, 1.0F);
			}
		} else if (j == 3) {
			if (isOpen) {
				if (!isOtherSide) {
					this.setBlockBounds(0.0F, 0.0F, -0.5F, f, 1.0F, 0.5F);
				} else {
					this.setBlockBounds(1.0F - f, 0.0F, -0.5F, 1.0F, 1.0F, 0.5F);
				}
			} else {
				this.setBlockBounds(0.0F, 0.0F, f1, 1.0F, 1.0F, f2);
			}
		}
	}

    @Override
    public AxisAlignedBB getCollisionBoundingBoxFromPool(World world, int x, int y, int z) {
        int meta = this.func_150012_g(world, x, y, z);
		boolean isOpen = (meta & 4) != 0;
        if(isOpen) return null;
        return super.getCollisionBoundingBoxFromPool(world, x, y, z);
    }

	@Override
	protected String getSFX() {
		return "hbm:block.fenceGate";
	}

}
