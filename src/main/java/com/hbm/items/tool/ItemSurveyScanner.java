package com.hbm.items.tool;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import com.hbm.blocks.ModBlocks;
import com.hbm.blocks.generic.BlockBedrockOreTE.TileEntityBedrockOre;
import com.hbm.handler.threading.PacketThreading;
import com.hbm.blocks.generic.BlockRichOre;
import com.hbm.inventory.material.Mats;
import com.hbm.inventory.material.NTMMaterial;
import com.hbm.items.ModItems;
import com.hbm.packet.toclient.AuxParticlePacketNT;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;
import net.minecraft.util.ChatComponentTranslation;

public class ItemSurveyScanner extends Item {

	@Override
	public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player) {

		if(!world.isRemote) {

			int x = (int) Math.floor(player.posX);
			int y = (int) Math.floor(player.posY);
			int z = (int) Math.floor(player.posZ);

			boolean hasOil = false;
			boolean hasGas = false;
			boolean hasBrine = false;
			boolean hasColtan = false;
			boolean hasBedrockOil = false;
			boolean hasDepth = false;
			boolean hasSchist = false;
			boolean hasAussie = false;
			TileEntityBedrockOre tile = null;
			Set<String> richOres = new HashSet<String>();

			for(int a = -5; a <= 5; a++) {
				for(int b = -5; b <= 5; b++) {
					for(int i =  y + 15; i > 1; i -= 2) {

						Block block = world.getBlock(x + a * 5, i, z + b * 5);

						//wow, this sucks!
						if(block == ModBlocks.ore_oil) hasOil = true;
						else if(block == ModBlocks.ore_gas) hasGas = true;
						else if(block == ModBlocks.ore_brine) hasBrine = true;
						else if(block == ModBlocks.ore_coltan) hasColtan = true;
						else if(block == ModBlocks.stone_depth) hasDepth = true;
						else if(block == ModBlocks.stone_depth_nether) hasDepth = true;
						else if(block == ModBlocks.stone_gneiss) hasSchist = true;
						else if(block == ModBlocks.ore_australium) hasAussie = true;
						else if(block == ModBlocks.ore_bedrock_oil) {
							hasBedrockOil = true;
							
							NBTTagCompound data = new NBTTagCompound();
							data.setString("type", "marker");
							data.setInteger("color", 0x000000);
							data.setInteger("expires", 15_000);
							data.setDouble("dist", 300D);
							data.setString("label", "Bedrock Oil");
							PacketThreading.createSendToThreadedPacket(new AuxParticlePacketNT(data, x + a * 5, i, z + b * 5), (EntityPlayerMP) player);
		
							break;
						}
						else if(block instanceof BlockRichOre) {
							NTMMaterial mat = Mats.matById.get(((BlockRichOre) block).type.matId());
							richOres.add(mat != null ? StatCollector.translateToLocal(mat.getUnlocalizedName()) : "?");
						}
					}
				}
			}
			
			int cX = ((x >> 4) << 4) + 8;
			int cZ = ((z >> 4) << 4) + 8;
			
			for(int i = -2; i <= 2; i++) for(int j = -2; j <= 2; j++) {
				Block center = world.getBlock(cX + i, 0, cZ + j);
				
				System.out.println(cX + " " + cZ);
				
				if(center == ModBlocks.ore_bedrock) {
					
					NBTTagCompound data = new NBTTagCompound();
					data.setString("type", "marker");
					data.setInteger("color", 0xff0000);
					data.setInteger("expires", 15_000);
					data.setDouble("dist", 300D);
					data.setString("label", "Bedrock Ore");
					PacketThreading.createSendToThreadedPacket(new AuxParticlePacketNT(data, cX + i, 0, cZ + j), (EntityPlayerMP) player);
					
					tile = (TileEntityBedrockOre) world.getTileEntity(cX + i, 0, cZ + j);
				}
			}

			if(hasOil) player.addChatComponentMessage(new ChatComponentTranslation("chat.survey_scanner.1").setChatStyle(new ChatStyle().setColor(EnumChatFormatting.BLACK)));
			if(hasGas) player.addChatComponentMessage(new ChatComponentTranslation("chat.survey_scanner.2").setChatStyle(new ChatStyle().setColor(EnumChatFormatting.WHITE)));
			if(hasBrine) player.addChatComponentMessage(new ChatComponentTranslation("chat.survey_scanner.3").setChatStyle(new ChatStyle().setColor(EnumChatFormatting.WHITE)));
			if(hasBedrockOil) player.addChatComponentMessage(new ChatComponentTranslation("chat.survey_scanner.4").setChatStyle(new ChatStyle().setColor(EnumChatFormatting.BLACK)));
			if(hasColtan) player.addChatComponentMessage(new ChatComponentTranslation("chat.survey_scanner.5").setChatStyle(new ChatStyle().setColor(EnumChatFormatting.GOLD)));
			if(hasDepth) player.addChatComponentMessage(new ChatComponentTranslation("chat.survey_scanner.6").setChatStyle(new ChatStyle().setColor(EnumChatFormatting.GRAY)));
			if(hasSchist) player.addChatComponentMessage(new ChatComponentTranslation("chat.survey_scanner.7").setChatStyle(new ChatStyle().setColor(EnumChatFormatting.DARK_AQUA)));
			if(hasAussie) player.addChatComponentMessage(new ChatComponentTranslation("chat.survey_scanner.8").setChatStyle(new ChatStyle().setColor(EnumChatFormatting.YELLOW)));
			for(String rich : richOres) player.addChatComponentMessage(new ChatComponentTranslation("chat.survey_scanner.9", rich.toUpperCase(Locale.US)).setChatStyle(new ChatStyle().setColor(EnumChatFormatting.GREEN)));
			if(tile != null && tile.resource != null) player.addChatComponentMessage(new ChatComponentTranslation("chat.survey_scanner.10", tile.resource.getDisplayName()).setChatStyle(new ChatStyle().setColor(EnumChatFormatting.RED)));
		}

		player.swingItem();

		return stack;

	}

	@Override
	public boolean onItemUse(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int i, float f0, float f1, float f2) {
		if(world.getBlock(x, y, z) == ModBlocks.block_beryllium && player.inventory.hasItem(ModItems.entanglement_kit)) {
			player.travelToDimension(1);
			return true;
		}

		return false;
	}
}