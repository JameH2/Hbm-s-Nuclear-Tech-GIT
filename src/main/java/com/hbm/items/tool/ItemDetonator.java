package com.hbm.items.tool;

import java.util.List;

import org.apache.logging.log4j.Level;

import com.hbm.config.GeneralConfig;
import com.hbm.interfaces.IBomb;
import com.hbm.interfaces.IBomb.BombReturnCode;
import com.hbm.main.MainRegistry;
import com.hbm.main.NTMSounds;
import com.hbm.util.ChatBuilder;
import com.hbm.util.i18n.I18nUtil;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.world.World;

public class ItemDetonator extends Item {

	@Override
	public void addInformation(ItemStack itemstack, EntityPlayer player, List list, boolean bool) {
		list.add(I18nUtil.resolveKey("item.drop.desc26"));
		list.add(I18nUtil.resolveKey("desc.item.detonator.right_click_detonate"));
		if(itemstack.getTagCompound() == null) {
			list.add(EnumChatFormatting.RED + I18nUtil.resolveKey("desc.misc.noPos"));
		} else {
			list.add(EnumChatFormatting.YELLOW + I18nUtil.resolveKey("item.designator_arty_range.desc2", itemstack.stackTagCompound.getInteger("x"), itemstack.stackTagCompound.getInteger("y"), itemstack.stackTagCompound.getInteger("z")));
		}
	}

	@Override
	public boolean onItemUse(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int p_77648_7_, float p_77648_8_, float p_77648_9_, float p_77648_10_) {
		if(stack.stackTagCompound == null) {
			stack.stackTagCompound = new NBTTagCompound();
		}

		if(player.isSneaking()) {
			stack.stackTagCompound.setInteger("x", x);
			stack.stackTagCompound.setInteger("y", y);
			stack.stackTagCompound.setInteger("z", z);

			if(!world.isRemote) {
				player.addChatMessage(ChatBuilder.start("[").color(EnumChatFormatting.DARK_AQUA)
						.nextTranslation(this.getUnlocalizedName() + ".name").color(EnumChatFormatting.DARK_AQUA)
						.next("] ").color(EnumChatFormatting.DARK_AQUA)
						.nextTranslation("desc.misc.posSet").color(EnumChatFormatting.GREEN).flush());
			}

			world.playSoundAtEntity(player, NTMSounds.TECH_BOOP, 2.0F, 1.0F);

			return true;
		}

		return false;
	}

	@Override
	public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player) {

		if(stack.stackTagCompound == null) {
			if(!world.isRemote) {
				player.addChatMessage(ChatBuilder.start("[").color(EnumChatFormatting.DARK_AQUA)
						.nextTranslation(this.getUnlocalizedName() + ".name").color(EnumChatFormatting.DARK_AQUA)
						.next("] ").color(EnumChatFormatting.DARK_AQUA)
						.nextTranslation("desc.misc.noPos").color(EnumChatFormatting.RED).flush());
			}
		} else {
			int x = stack.stackTagCompound.getInteger("x");
			int y = stack.stackTagCompound.getInteger("y");
			int z = stack.stackTagCompound.getInteger("z");

			if(world.getBlock(x, y, z) instanceof IBomb) {
				world.playSoundAtEntity(player, NTMSounds.TECH_BLEEP, 1.0F, 1.0F);
				if(!world.isRemote) {
					BombReturnCode ret = ((IBomb) world.getBlock(x, y, z)).explode(world, x, y, z);

					if(GeneralConfig.enableExtendedLogging)
						MainRegistry.logger.log(Level.INFO, "[DET] Tried to detonate block at " + x + " / " + y + " / " + z + " by " + player.getDisplayName() + "!");

					player.addChatMessage(ChatBuilder.start("[").color(EnumChatFormatting.DARK_AQUA)
							.nextTranslation(this.getUnlocalizedName() + ".name").color(EnumChatFormatting.DARK_AQUA)
							.next("] ").color(EnumChatFormatting.DARK_AQUA)
							.nextTranslation(ret.getUnlocalizedMessage()).color(ret.wasSuccessful() ? EnumChatFormatting.YELLOW : EnumChatFormatting.RED).flush());
				}

			} else {
				if(!world.isRemote) {
					player.addChatMessage(ChatBuilder.start("[").color(EnumChatFormatting.DARK_AQUA)
							.nextTranslation(this.getUnlocalizedName() + ".name").color(EnumChatFormatting.DARK_AQUA)
							.next("] ").color(EnumChatFormatting.DARK_AQUA)
							.nextTranslation(BombReturnCode.ERROR_NO_BOMB.getUnlocalizedMessage()).color(EnumChatFormatting.RED).flush());
				}
			}
		}

		return stack;

	}

}
