package com.hbm.blocks.generic;

import com.hbm.blocks.machine.BlockPillar;

import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.world.World;
import net.minecraft.util.ChatComponentTranslation;

public class BlockWriting extends BlockPillar {

	public BlockWriting(Material mat, String top) {
		super(mat, top);
	}

	@Override
	public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float hitX, float hitY, float hitZ) {
		if(world.isRemote) {
			return true;

		} else if(!player.isSneaking()) {

			ChatStyle red = new ChatStyle().setColor(EnumChatFormatting.RED);
			player.addChatMessage(new ChatComponentTranslation("chat.block_writing.line.01").setChatStyle(red));
			player.addChatMessage(new ChatComponentTranslation("chat.block_writing.line.02").setChatStyle(red));
			player.addChatMessage(new ChatComponentTranslation("chat.block_writing.line.03").setChatStyle(red));
			player.addChatMessage(new ChatComponentTranslation("chat.block_writing.line.04").setChatStyle(red));
			player.addChatMessage(new ChatComponentTranslation("chat.block_writing.line.05").setChatStyle(red));
			player.addChatMessage(new ChatComponentTranslation("chat.block_writing.line.06").setChatStyle(red));
			player.addChatMessage(new ChatComponentTranslation("chat.block_writing.line.07").setChatStyle(red));
			player.addChatMessage(new ChatComponentTranslation("chat.block_writing.line.08").setChatStyle(red));
			player.addChatMessage(new ChatComponentTranslation("chat.block_writing.line.09").setChatStyle(red));
			player.addChatMessage(new ChatComponentTranslation("chat.block_writing.line.10").setChatStyle(red));
			return true;

		} else {
			return false;
		}
	}
}
