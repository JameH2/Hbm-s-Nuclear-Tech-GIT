package com.hbm.items.tool;

import java.util.List;

import com.hbm.blocks.BlockDummyable;
import com.hbm.main.MainRegistry;
import com.hbm.tileentity.network.TileEntityPipelineBase;
import com.hbm.util.i18n.I18nUtil;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.minecraft.util.ChatComponentTranslation;

public class ItemWrench extends ItemSword {

	public ItemWrench(ToolMaterial mat) {
		super(mat);
	}

	@Override
	public boolean onItemUse(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int p_77648_7_, float p_77648_8_, float p_77648_9_, float p_77648_10_) {

		if(!player.isSneaking()) {

			Block b = world.getBlock(x, y, z);

			if(b instanceof BlockDummyable) {
				int[] core = ((BlockDummyable)b).findCore(world, x, y, z);

				if(core != null) {
					x = core[0];
					y = core[1];
					z = core[2];
				}
			}

			TileEntity te = world.getTileEntity(x, y, z);

			if(te != null && te instanceof TileEntityPipelineBase) {

				if(!stack.stackTagCompound.hasKey("x")) {
					stack.stackTagCompound.setInteger("x", x);
					stack.stackTagCompound.setInteger("y", y);
					stack.stackTagCompound.setInteger("z", z);

					if(!world.isRemote) {
						player.addChatMessage(new ChatComponentTranslation("chat.wrench.pipe_start"));
					}
				} else if(!world.isRemote) {

					int x1 = stack.stackTagCompound.getInteger("x");
					int y1 = stack.stackTagCompound.getInteger("y");
					int z1 = stack.stackTagCompound.getInteger("z");

					if(world.getTileEntity(x1, y1, z1) instanceof TileEntityPipelineBase) {

						TileEntityPipelineBase first = (TileEntityPipelineBase) world.getTileEntity(x1, y1, z1);
						TileEntityPipelineBase second = ((TileEntityPipelineBase) te);

						switch (TileEntityPipelineBase.canConnect(first, second)) {
							case 0:
								first.addConnection(x, y, z);
								second.addConnection(x1, y1, z1);
								player.addChatMessage(new ChatComponentTranslation("chat.wrench.pipe_end"));
								break;
							case 1: player.addChatMessage(new ChatComponentTranslation("chat.wrench.incompatible_pipes")); break;
							case 2: player.addChatMessage(new ChatComponentTranslation("chat.wrench.4")); break;
							case 3: player.addChatMessage(new ChatComponentTranslation("chat.wrench.out_of_range")); break;
							case 4: player.addChatMessage(new ChatComponentTranslation("chat.wrench.6")); break;
						}

						stack.stackTagCompound = null;

					} else {

						player.addChatMessage(new ChatComponentTranslation("chat.wrench.pipe_error"));
						stack.stackTagCompound = null;
					}
				}

				player.swingItem();
				return true;
			}
		}

		return false;
	}

	@Override
	public boolean hitEntity(ItemStack stack, EntityLivingBase entity, EntityLivingBase entityPlayer) {
		World world = entity.worldObj;

		Vec3 vec = entityPlayer.getLookVec();

		double dX = vec.xCoord * 0.5;
		double dY = vec.yCoord * 0.5;
		double dZ = vec.zCoord * 0.5;

		entity.motionX += dX;
		entity.motionY += dY;
		entity.motionZ += dZ;
		world.playSoundAtEntity(entity, "random.anvil_land", 3.0F, 0.75F);

		return false;
	}

	@Override
	public void addInformation(ItemStack itemstack, EntityPlayer player, List list, boolean bool) {
		if(itemstack.stackTagCompound != null) {
			list.add(I18nUtil.resolveKey("item.wrench.desc1", itemstack.stackTagCompound.getInteger("x")));
			list.add(I18nUtil.resolveKey("item.wrench.desc2", itemstack.stackTagCompound.getInteger("y")));
			list.add(I18nUtil.resolveKey("item.wrench.desc3", itemstack.stackTagCompound.getInteger("z")));
		} else {
			list.add(I18nUtil.resolveKey("item.wrench.desc4"));
		}
	}

	@Override
	public void onUpdate(ItemStack stack, World world, Entity entity, int slot, boolean inhand) {

		if(world.isRemote) {
			if(stack.hasTagCompound() && stack.stackTagCompound.hasKey("x")) {
				Vec3 vec = Vec3.createVectorHelper(
						entity.posX - stack.stackTagCompound.getInteger("x"),
						entity.posY - stack.stackTagCompound.getInteger("y"),
						entity.posZ - stack.stackTagCompound.getInteger("z"));

				MainRegistry.proxy.displayTooltip(stack.getDisplayName() + ": " + ((int) vec.lengthVector()) + "m", MainRegistry.proxy.ID_WRENCH);
			}
		}
	}
}
