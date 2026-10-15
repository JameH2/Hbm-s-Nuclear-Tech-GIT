package com.hbm.items.machine;

import java.util.List;

import com.hbm.blocks.ModBlocks;
import com.hbm.blocks.machine.MachineDishControl;
import com.hbm.tileentity.TileEntityProxyCombo;
import com.hbm.tileentity.machine.TileEntityDishControl;
import com.hbm.util.ChatBuilder;
import com.hbm.util.i18n.I18nUtil;

import cpw.mods.fml.common.Mod;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.world.World;

public class ItemReactorSensor extends Item {

	@Override
	public boolean onItemUse(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int p_77648_7_,
			float p_77648_8_, float p_77648_9_, float p_77648_10_) {

		Block b = world.getBlock(x, y, z);

		if (b == ModBlocks.reactor_research || b == ModBlocks.machine_stardar) {

			if(stack.stackTagCompound == null)
				stack.stackTagCompound = new NBTTagCompound();

			if(!world.isRemote) {
				SendMessage(player, "chat.reactor_sensor.position_set");
			}

			stack.stackTagCompound.setInteger("x", x);
			stack.stackTagCompound.setInteger("y", y);
			stack.stackTagCompound.setInteger("z", z);

			world.playSoundAtEntity(player, "hbm:item.techBoop", 1.0F, 1.0F);
			player.swingItem();
			return true;
		}
		else if(b == ModBlocks.machine_dish_controller)
		{
			if(stack.stackTagCompound == null) return false;

			// Get Core
			MachineDishControl controllerBlock = (MachineDishControl)b;
			int[] corePos = controllerBlock.findCore(world, x, y, z);

			// Get TileEntity
			TileEntityDishControl entity = (TileEntityDishControl)world.getTileEntity(
				corePos[0],
				corePos[1],
				corePos[2]
			);

			if(entity == null) return false;

			if(!world.isRemote) {
				SendMessage(player, "chat.reactor_sensor.stardar_linked");
				entity.TryLink(stack);
			}

			world.playSoundAtEntity(player, "hbm:item.techBoop", 1.0F, 1.0F);
			player.swingItem();
			return true;
		}

		return false;
	}

	private void SendMessage(EntityPlayer player, String text)
	{
		player.addChatMessage(ChatBuilder.start("[").color(EnumChatFormatting.DARK_AQUA)
			.nextTranslation(this.getUnlocalizedName() + ".name").color(EnumChatFormatting.DARK_AQUA)
			.next("] ").color(EnumChatFormatting.DARK_AQUA)
			.nextTranslation(text).color(EnumChatFormatting.GREEN).flush());
	}

	@Override
	public void addInformation(ItemStack itemstack, EntityPlayer player, List list, boolean bool) {
		if (itemstack.stackTagCompound != null) {
			list.add(I18nUtil.resolveKey("item.reactor_sensor.desc1", itemstack.stackTagCompound.getInteger("x")));
			list.add(I18nUtil.resolveKey("item.reactor_sensor.desc2", itemstack.stackTagCompound.getInteger("y")));
			list.add(I18nUtil.resolveKey("item.reactor_sensor.desc3", itemstack.stackTagCompound.getInteger("z")));
		} else {
			list.add(I18nUtil.resolveKey("desc.item.reactor_sensor.no_reactor_selected"));
		}
	}
}
