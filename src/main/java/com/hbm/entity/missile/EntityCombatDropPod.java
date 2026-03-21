package com.hbm.entity.missile;

import com.hbm.blocks.ModBlocks;
import com.hbm.explosion.ExplosionLarge;
import com.hbm.items.ModItems;
import com.hbm.main.MainRegistry;
import com.hbm.packet.PacketDispatcher;
import com.hbm.packet.toclient.BufPacket;
import com.hbm.packet.toserver.NBTControlPacket;
import com.hbm.tileentity.machine.storage.TileEntityCombatDropPod;
import com.hbm.tileentity.machine.storage.TileEntitySoyuzCapsule;

import cpw.mods.fml.common.network.NetworkRegistry.TargetPoint;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.projectile.EntityThrowable;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;
import scala.reflect.internal.Trees.This;

public class EntityCombatDropPod extends EntityThrowable {

	private NBTTagCompound entityType;
	private int amount;
	private int color;

	public EntityCombatDropPod(World p_i1582_1_) {
		super(p_i1582_1_);
		this.ignoreFrustumCheck = true;
		this.isImmuneToFire = true;
	}

	public void setEntity(Entity entity, int amount) {
		    this.entityType = new NBTTagCompound();
		    entity.writeToNBT(this.entityType);
		    this.amount = amount;
	}
	
	public void setColor(int color) {
		this.color = color;
		this.dataWatcher.updateObject(20, color);
	}

	public int getColor() {
		return this.dataWatcher.getWatchableObjectInt(20);
	}
	
	@Override
	protected void entityInit() {
	    this.dataWatcher.addObject(20, (int) 0);
	}
	
	public void setPayload(NBTTagCompound entityData, int amount, int color) {
		    this.entityType = entityData;
		    this.amount = amount;
		    this.setColor(color);
		}
	
	@Override
	public void onUpdate() {
		motionY = -1;
		motionX = 0;
		motionZ = 0;
		this.lastTickPosX = this.prevPosX = this.posX;
		this.lastTickPosY = this.prevPosY = this.posY;
		this.lastTickPosZ = this.prevPosZ = this.posZ;
		
		for(int i = 0; i < 4; i++) {
			
			if(worldObj.getBlock((int)(posX - 0.5), (int)(posY + 1), (int)(posZ - 0.5)).getMaterial() != Material.air && !worldObj.isRemote) {
				ExplosionLarge.spawnParticles(worldObj, posX, posY + 1, posZ, 50);

	            this.worldObj.playSoundEffect(this.posX, this.posY, this.posZ, "hbm:block.hatchImpact", 10.0F, 0.5F + this.rand.nextFloat() * 0.5F);

	            int x = (int)this.posX;
	            int y = (int)this.posY + 1;
	            int z = (int)this.posZ;

	            worldObj.setBlock(x, y, z, ModBlocks.combat_drop);

	            TileEntity te = worldObj.getTileEntity(x, y, z);

	            if(te instanceof TileEntityCombatDropPod) {

	                TileEntityCombatDropPod capsule = (TileEntityCombatDropPod) te;

	                if(this.entityType != null) {
	                    capsule.setPayload(this.entityType, this.amount, this.color);
	                    worldObj.markBlockForUpdate(x, y, z);

	                } else {
	                    return;
	                }
	            }
			this.setDead();
			break;
			}
			

			this.posX += this.motionX;
			this.posY += this.motionY;
			this.posZ += this.motionZ;
		}


	
	}


	@Override
	protected void onImpact(MovingObjectPosition p_70184_1_) {

	}


	@Override
	@SideOnly(Side.CLIENT)
	public boolean isInRangeToRenderDist(double distance) {
		return distance < 500000;
	}

	@Override
	public void readEntityFromNBT(NBTTagCompound nbt) {
		this.entityType = nbt.getCompoundTag("entityType");
		this.color = nbt.getInteger("color");
		this.amount = nbt.getInteger("amount");
		this.setColor(nbt.getInteger("color"));

	}

	@Override
	public void writeEntityToNBT(NBTTagCompound nbt) {
		nbt.setInteger("color", color);
		nbt.setInteger("amount", amount);
		nbt.setTag("entityType", entityType);

	}
}
