package com.hbm.items.tool;

import java.util.List;

import com.hbm.blocks.BlockDummyable;
import com.hbm.lib.Library;
import com.hbm.main.NTMSounds;
import com.hbm.tileentity.turret.TileEntityTurretBaseArtillery;
import com.hbm.util.i18n.I18nUtil;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;

public class ItemDesignatorArtyRange extends Item {

	public ItemDesignatorArtyRange() {
		this.setFull3D();
		this.setMaxStackSize(1);
	}

	@Override
	public void addInformation(ItemStack itemstack, EntityPlayer player, List list, boolean bool) {
		if(itemstack.getTagCompound() == null) {
			list.add(EnumChatFormatting.RED + I18nUtil.resolveKey("desc.item.designator_arty_range.no_turret_linked"));
		} else {
			list.add(EnumChatFormatting.YELLOW + I18nUtil.resolveKey("item.designator_arty_range.desc2", itemstack.stackTagCompound.getInteger("x"), itemstack.stackTagCompound.getInteger("y"), itemstack.stackTagCompound.getInteger("z")));
		}
	}

	@Override
	public boolean onItemUse(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side, float hitX, float hitY, float hitZ) {

		Block b = world.getBlock(x, y, z);

		if(b instanceof BlockDummyable) {
			int pos[] = ((BlockDummyable) b).findCore(world, x, y, z);

			if(pos == null)
				return false;

			TileEntity te = world.getTileEntity(pos[0], pos[1], pos[2]);

			if(te instanceof TileEntityTurretBaseArtillery) {

				if(world.isRemote)
					return true;

				if(!stack.hasTagCompound())
					stack.stackTagCompound = new NBTTagCompound();

				stack.stackTagCompound.setInteger("x", pos[0]);
				stack.stackTagCompound.setInteger("y", pos[1]);
				stack.stackTagCompound.setInteger("z", pos[2]);
				world.playSoundAtEntity(player, NTMSounds.TECH_BLEEP, 1.0F, 1.0F);
				return true;
			}
		}

		return false;
	}

	@Override
	public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player) {

		if(!stack.hasTagCompound())
			return stack;

		MovingObjectPosition pos = Library.rayTrace(player, 500, 1);
		int x = pos.blockX;
		int y = pos.blockY;
		int z = pos.blockZ;

		if(!world.isRemote) {
			TileEntity te = world.getTileEntity(stack.stackTagCompound.getInteger("x"), stack.stackTagCompound.getInteger("y"), stack.stackTagCompound.getInteger("z"));

			if(te instanceof TileEntityTurretBaseArtillery) {
				TileEntityTurretBaseArtillery arty = (TileEntityTurretBaseArtillery) te;
				arty.enqueueTarget(x + 0.5, y + 0.5, z + 0.5);
				world.playSoundAtEntity(player, NTMSounds.TECH_BOOP, 1.0F, 1.0F);
			}
		}

		return stack;
	}
}
