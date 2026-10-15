package com.hbm.items.tool;

import baubles.api.BaubleType;
import baubles.api.IBauble;

import com.hbm.dim.trait.CBT_Atmosphere;
import com.hbm.dim.trait.CBT_Atmosphere.FluidEntry;
import com.hbm.handler.atmosphere.ChunkAtmosphereManager;
import com.hbm.packet.PacketDispatcher;
import com.hbm.packet.toclient.PlayerInformPacket;
import com.hbm.util.BobMathUtil;
import com.hbm.util.ChatBuilder;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.world.World;

public class ItemAtmosphereScanner extends Item implements IBauble {

	@Override
	public void onUpdate(ItemStack stack, World world, Entity entity, int slot, boolean inHand) {
		tickScanner(world, entity);
	}

	private void tickScanner(World world, Entity entity) {
		if(!(entity instanceof EntityPlayerMP) || world.getTotalWorldTime() % 5 != 0) return;

		EntityPlayerMP player = (EntityPlayerMP) entity;

		CBT_Atmosphere atmosphere = ChunkAtmosphereManager.proxy.getAtmosphere(entity);

		boolean hasAtmosphere = false;
		if(atmosphere != null) {
			for(int i = 0; i < atmosphere.fluids.size(); i++) {
				FluidEntry entry = atmosphere.fluids.get(i);
				if(entry.pressure > 0.0001) {
					String pressure = String.format("%.4f", BobMathUtil.roundDecimal(entry.pressure, 4));
					PacketDispatcher.wrapper.sendTo(new PlayerInformPacket(ChatBuilder.startTranslation(entry.fluid.getUnlocalizedName()).color(EnumChatFormatting.AQUA).next(": ").nextTranslation("item.fluid_icon.desc2", pressure).color(EnumChatFormatting.RESET).flush(), 969 + i, 4000), player);
					hasAtmosphere = true;
				}
			}
		}

		if(!hasAtmosphere) {
			PacketDispatcher.wrapper.sendTo(new PlayerInformPacket(ChatBuilder.startTranslation("chat.atmosphere_scanner.near_vacuum").color(EnumChatFormatting.YELLOW).flush(), 969, 4000), player);
		}
	}

	@Override
	public BaubleType getBaubleType(ItemStack stack) { return BaubleType.BELT; }

	@Override
	public void onWornTick(ItemStack stack, EntityLivingBase entity) { tickScanner(entity.worldObj, entity); }

	@Override
	public void onEquipped(ItemStack stack, EntityLivingBase entity) { }

	@Override
	public void onUnequipped(ItemStack stack, EntityLivingBase entity) { }

	@Override
	public boolean canEquip(ItemStack stack, EntityLivingBase entity) { return true; }

	@Override
	public boolean canUnequip(ItemStack stack, EntityLivingBase entity) { return true; }

}
