package com.hbm.items.tool;

import java.util.List;

import com.hbm.blocks.ModBlocks;
import com.hbm.config.SpaceConfig;
import com.hbm.dim.CelestialBody;
import com.hbm.dim.CelestialTeleporter;
import com.hbm.dim.SolarSystem;
import com.hbm.dim.orbit.WorldProviderOrbit;
import com.hbm.dim.trait.CBT_Atmosphere;
import com.hbm.dim.trait.CBT_Atmosphere.FluidEntry;
import com.hbm.dim.trait.CBT_Destroyed;
import com.hbm.lib.Library;
import com.hbm.util.i18n.I18nUtil;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;
import net.minecraft.util.ChatComponentTranslation;

public class ItemWandD extends Item {


	@Override
	public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player) {

		if(world.isRemote)
			return stack;

		MovingObjectPosition pos = Library.rayTrace(player, 500, 1, false, true, false);

		if(pos != null) {

			if(stack.stackTagCompound == null)
				stack.stackTagCompound = new NBTTagCompound();

			if(!player.isSneaking()) {
				int targetId = stack.stackTagCompound.getInteger("dim");

				if(targetId == 0) {
					CelestialTeleporter.teleport(player, SpaceConfig.orbitDimension, player.posX, 128, player.posZ, false);
					player.addChatMessage(new ChatComponentTranslation("chat.wand_d.1"));
				} else {
					SolarSystem.Body target = SolarSystem.Body.values()[targetId];

					CelestialTeleporter.teleport(player, target.getBody().dimensionId, player.posX, 300, player.posZ, true);
					player.addChatMessage(new ChatComponentTranslation("chat.wand_d.2", target.getBody().getUnlocalizedName()));
				}

			} else {


				int targetId = stack.stackTagCompound.getInteger("dim");
				targetId++;

				if(targetId >= SolarSystem.Body.values().length) {
					targetId = 0;
				}

				stack.stackTagCompound.setInteger("dim", targetId);

				if(targetId == 0) {
					player.addChatMessage(new ChatComponentTranslation("chat.wand_d.3"));
				} else {
					SolarSystem.Body target = SolarSystem.Body.values()[targetId];
					player.addChatMessage(new ChatComponentTranslation("chat.wand_d.4", target.getBody().getUnlocalizedName()));
				}
			}
		} else if(!(world.provider instanceof WorldProviderOrbit)) {
			if(!player.isSneaking()) {
				// TESTING: View atmospheric data
				CBT_Atmosphere atmosphere = CelestialBody.getTrait(world, CBT_Atmosphere.class);

				boolean isVacuum = true;
				if(atmosphere != null) {
					for(FluidEntry entry : atmosphere.fluids) {
						// if(entry.pressure > 0.001) {
							player.addChatMessage(new ChatComponentTranslation("chat.wand_d.5", entry.fluid.getUnlocalizedName(), entry.pressure));
							isVacuum = false;
						// }
					}
				}

				if(isVacuum)
					player.addChatMessage(new ChatComponentTranslation("chat.wand_d.6"));
			} else {
				CelestialBody star = CelestialBody.getStar(world);

				if(!star.hasTrait(CBT_Destroyed.class)) {

					// TESTING: END OF TIME
					star.modifyTraits(new CBT_Destroyed());

					// TESTING: END OF LIFE
					CelestialBody.degas(world);

					// GOD
					// DAMN
					player.addChatMessage(new ChatComponentTranslation("chat.wand_d.7").setChatStyle(new net.minecraft.util.ChatStyle().setColor(EnumChatFormatting.RED)));
					player.addChatMessage(new ChatComponentTranslation("chat.wand_d.8").setChatStyle(new net.minecraft.util.ChatStyle().setColor(EnumChatFormatting.RED)));
					player.addChatMessage(new ChatComponentTranslation("chat.wand_d.9").setChatStyle(new net.minecraft.util.ChatStyle().setColor(EnumChatFormatting.RED)));
					player.addChatMessage(new ChatComponentTranslation("chat.wand_d.10").setChatStyle(new net.minecraft.util.ChatStyle().setColor(EnumChatFormatting.RED).setObfuscated(true)));
				} else {

					star.clearTraits();
					CelestialBody.clearTraits(world);

					player.addChatMessage(new ChatComponentTranslation("chat.wand_d.11"));
				}
			}
		} else {
			world.setBlock(MathHelper.floor_double(player.posX), MathHelper.floor_double(player.posY) - 1, MathHelper.floor_double(player.posZ), ModBlocks.concrete);
		}

		return stack;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean bool) {
		list.add(I18nUtil.resolveKey("desc.item.wand_d.dimension_teleporter_atmosphere"));

		if(stack.stackTagCompound != null) {
			int targetId = stack.stackTagCompound.getInteger("dim");
			if(targetId == 0) {
				list.add(I18nUtil.resolveKey("desc.item.wand_d.teleportation_target_orbit"));
			} else {
				SolarSystem.Body target = SolarSystem.Body.values()[targetId];
				list.add(I18nUtil.resolveKey("desc.item.wand_d.teleportation_target", target.getBody().getUnlocalizedName()));
			}
		}
	}
}