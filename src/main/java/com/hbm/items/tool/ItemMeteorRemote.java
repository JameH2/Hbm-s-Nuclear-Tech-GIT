package com.hbm.items.tool;

import java.util.List;
import java.util.Random;

import com.hbm.handler.BossSpawnHandler;
import com.hbm.main.NTMSounds;
import com.hbm.util.i18n.I18nUtil;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraft.util.ChatComponentTranslation;

public class ItemMeteorRemote extends Item {

	Random rand = new Random();

	public ItemMeteorRemote() {
		this.canRepair = false;
		this.setMaxDamage(2);
	}

	@Override
	public void addInformation(ItemStack itemstack, EntityPlayer player, List list, boolean bool) {
		list.add(I18nUtil.resolveKey("desc.item.meteor_remote.right_click_summon_meteorite"));
	}

	@Override
	public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player) {

		stack.damageItem(1, player);

		if(!world.isRemote) {
			BossSpawnHandler.spawnMeteorAtPlayer(player, false);
			player.addChatMessage(new ChatComponentTranslation("chat.glitch.15"));
		}

		world.playSoundAtEntity(player, NTMSounds.TECH_BLEEP, 1.0F, 1.0F);
		player.swingItem();

		return stack;
	}
}
