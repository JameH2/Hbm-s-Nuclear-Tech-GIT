package com.hbm.blocks.machine;

import java.util.Random;

import com.hbm.main.MainRegistry;
import com.hbm.tileentity.machine.storage.TileEntityCombatDropPod;
import com.hbm.tileentity.machine.storage.TileEntitySoyuzCapsule;

import cpw.mods.fml.common.network.internal.FMLNetworkHandler;
import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class CombatDropPod extends BlockContainer {

	public CombatDropPod(Material p_i45386_1_) {
		super(p_i45386_1_);
	}

	@Override
	public TileEntity createNewTileEntity(World p_149915_1_, int p_149915_2_) {
		return new TileEntityCombatDropPod();
	}
	
	@Override
	public int getRenderType(){
		return -1;
	}
	
	@Override
	public boolean isOpaqueCube() {
		return false;
	}
	
	@Override
	public boolean renderAsNormalBlock() {
		return false;
	}
	

	
	@Override
	public void breakBlock(World world, int x, int y, int z, Block block, int meta)
    {
		super.breakBlock(world, x, y, z, block, meta);
    }

}
