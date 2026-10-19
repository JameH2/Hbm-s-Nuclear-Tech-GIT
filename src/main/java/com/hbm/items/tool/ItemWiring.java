package com.hbm.items.tool;

import java.util.List;

import com.hbm.blocks.BlockDummyable;
import com.hbm.main.MainRegistry;
import com.hbm.tileentity.network.TileEntityPylonBase;
import com.hbm.util.i18n.I18nUtil;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.MovingObjectPosition.MovingObjectType;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.minecraft.util.ChatComponentTranslation;

public class ItemWiring extends Item {

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

			if(te != null && te instanceof TileEntityPylonBase) {

				if(stack.stackTagCompound == null) {
					stack.stackTagCompound = new NBTTagCompound();

					stack.stackTagCompound.setInteger("x", x);
					stack.stackTagCompound.setInteger("y", y);
					stack.stackTagCompound.setInteger("z", z);

					if(!world.isRemote) {
						player.addChatMessage(new ChatComponentTranslation("chat.wiring.wire_start"));
					}
				} else if(!world.isRemote) {

					int x1 = stack.stackTagCompound.getInteger("x");
					int y1 = stack.stackTagCompound.getInteger("y");
					int z1 = stack.stackTagCompound.getInteger("z");

					if(world.getTileEntity(x1, y1, z1) instanceof TileEntityPylonBase) {

						TileEntityPylonBase first = (TileEntityPylonBase) world.getTileEntity(x1, y1, z1);
						TileEntityPylonBase second = ((TileEntityPylonBase) te);

						switch (TileEntityPylonBase.canConnect(first, second)) {
							case 0:
								first.addConnection(x, y, z);
								second.addConnection(x1, y1, z1);
								player.addChatMessage(new ChatComponentTranslation("chat.wiring.wire_end"));
								break;
							case 1:
								player.addChatMessage(new ChatComponentTranslation("chat.wiring.incompatible_pylons"));
								break;
							case 2:
								player.addChatMessage(new ChatComponentTranslation("chat.wiring.same_pylon"));
								break;
							case 3:
								player.addChatMessage(new ChatComponentTranslation("chat.wiring.out_of_range"));
								break;
						}

						stack.stackTagCompound = null;

					} else {

						if(!world.isRemote) {
							player.addChatMessage(new ChatComponentTranslation("chat.wiring.wire_error"));
						}
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
	public void addInformation(ItemStack itemstack, EntityPlayer player, List list, boolean bool) {
		if(itemstack.stackTagCompound != null) {
			list.add(I18nUtil.resolveKey("item.wiring.x", itemstack.stackTagCompound.getInteger("x")));
			list.add(I18nUtil.resolveKey("item.wiring.y", itemstack.stackTagCompound.getInteger("y")));
			list.add(I18nUtil.resolveKey("item.wiring.z", itemstack.stackTagCompound.getInteger("z")));
		} else {
			list.add(I18nUtil.resolveKey("item.wiring.connect"));
		}
	}

	@Override
	public void onUpdate(ItemStack stack, World world, Entity entity, int slot, boolean inhand) {

		if(world.isRemote && inhand && stack.stackTagCompound != null) {
			displayWireDistance(stack, world, entity);
		}
	}

	@SideOnly(Side.CLIENT)
	private void displayWireDistance(ItemStack stack, World world, Entity entity) {
		TileEntity start = world.getTileEntity(stack.stackTagCompound.getInteger("x"),
				stack.stackTagCompound.getInteger("y"), stack.stackTagCompound.getInteger("z"));
		if(!(start instanceof TileEntityPylonBase)) return;
		TileEntityPylonBase first = (TileEntityPylonBase) start;

		MovingObjectPosition target = Minecraft.getMinecraft().objectMouseOver;
		if(target != null && target.typeOfHit == MovingObjectType.BLOCK) {
			int x = target.blockX;
			int y = target.blockY;
			int z = target.blockZ;
			Block block = world.getBlock(x, y, z);
			if(block instanceof BlockDummyable) {
				int[] core = ((BlockDummyable) block).findCore(world, x, y, z);
				if(core != null) {
					x = core[0];
					y = core[1];
					z = core[2];
				}
			}

			TileEntity end = world.getTileEntity(x, y, z);
			if(end instanceof TileEntityPylonBase) {
				TileEntityPylonBase second = (TileEntityPylonBase) end;
				double distance = TileEntityPylonBase.getWireDistance(first, second);
				double limit = Math.min(first.getMaxWireLength(), second.getMaxWireLength());
				EnumChatFormatting color = TileEntityPylonBase.canConnect(first, second) == 0 ? EnumChatFormatting.GREEN : EnumChatFormatting.RED;
				// Round up so an over-range connection never appears within the displayed limit.
				MainRegistry.proxy.displayTooltip(stack.getDisplayName() + ": " + color + (int) Math.ceil(distance) + "m / " + (int) limit + "m", MainRegistry.proxy.ID_CABLE);
				return;
			}
		}

		// Without a target pylon, only an approximate distance to the player is available.
		Vec3 point = first.getConnectionPoint();
		double distance = point.distanceTo(Vec3.createVectorHelper(entity.posX, entity.posY, entity.posZ));
		MainRegistry.proxy.displayTooltip(stack.getDisplayName() + ": ~" + (int) Math.ceil(distance) + "m", MainRegistry.proxy.ID_CABLE);
	}
}
