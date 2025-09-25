package com.hbm.blocks;

import java.util.List;

import com.hbm.lib.ModDamageSource;
import com.hbm.main.MainRegistry;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;

public class BlockGeysierDCM extends BlockContainer {

	public BlockGeysierDCM(Material rock) {
		super(rock);
	}

	@Override
	public TileEntity createNewTileEntity(World world, int meta) {
		return new TileEntityDCM();
	}

	@Override
	public boolean isOpaqueCube() {
		return false;
	}

	public static class TileEntityDCM extends TileEntity {

		public TileEntityDCM() {

		}

		@Override
		public void updateEntity() {
			if(!worldObj.isRemote) {
				NBTTagCompound data = new NBTTagCompound();
				data.setDouble("posX", xCoord + 0.5);
				data.setDouble("posY", yCoord + 0);
				data.setDouble("posZ", zCoord + 0.5);
				data.setString("type", "missileContrail");
				data.setFloat("scale", 1.5f);
				data.setDouble("moX", 0);
				data.setDouble("moY", 4);
				data.setDouble("moZ", 0);
				data.setInteger("maxAge", 100 + worldObj.rand.nextInt(20));
				data.setInteger("color", 0xA4D7DD);
				MainRegistry.proxy.effectNT(data);

				vapor();
			}
		}

		@Override
		public AxisAlignedBB getRenderBoundingBox() {
			return TileEntity.INFINITE_EXTENT_AABB;
		}

		@Override
		@SideOnly(Side.CLIENT)
		public double getMaxRenderDistanceSquared() {
			return 65536.0D;
		}

		// copy pasting without reading makes you create a fluid geyser that... zaps you to death with electricty?
		private void vapor() {
			List<Entity> entities = this.worldObj.getEntitiesWithinAABB(Entity.class, AxisAlignedBB.getBoundingBox(this.xCoord - 0.5, this.yCoord + 0.5, this.zCoord - 0.5, this.xCoord + 1.5, this.yCoord + 60, this.zCoord + 1.5));

			if(!entities.isEmpty()) {
				for(Entity e : entities) {
					if(e instanceof EntityLivingBase) {
						if(e.attackEntityFrom(ModDamageSource.acid, MathHelper.clamp_float(((EntityLivingBase) e).getMaxHealth() * 0.1F, 3, 20))) {
							worldObj.playSoundAtEntity(e, "random.fizz", 1.0F, 1.0F);
						}
					}
				}
			}
		}

	}


}
