package com.hbm.items.tool;

import com.hbm.dim.trait.CBT_Atmosphere;
import com.hbm.dim.trait.CBT_Atmosphere.FluidEntry;
import com.hbm.handler.atmosphere.ChunkAtmosphereManager;
import com.hbm.packet.PacketDispatcher;
import com.hbm.packet.toclient.PlayerInformPacket;
import com.hbm.util.BobMathUtil;
import com.hbm.util.ChatBuilder;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.world.World;

public class ItemAtmosphereScanner extends Item {

	@Override
	public void onUpdate(ItemStack stack, World world, Entity entity, int slot, boolean inHand) {

		if(!(entity instanceof EntityPlayerMP) || world.getTotalWorldTime() % 5 != 0) return;

		EntityPlayerMP player = (EntityPlayerMP) entity;

		CBT_Atmosphere atmosphere = ChunkAtmosphereManager.proxy.getAtmosphere(entity);

		boolean hasAtmosphere = false;
		if(atmosphere != null) {
			for(int i = 0; i < atmosphere.fluids.size(); i++) {
				FluidEntry entry = atmosphere.fluids.get(i);
				if(entry.pressure > 0.0001) {
					String pressure = String.format("%.4f", BobMathUtil.roundDecimal(entry.pressure, 4));
					PacketDispatcher.wrapper.sendTo(new PlayerInformPacket(ChatBuilder.startTranslation(entry.fluid.getUnlocalizedName()).color(EnumChatFormatting.AQUA).next(": ").next(pressure + "atm").color(EnumChatFormatting.RESET).flush(), 969 + i, 4000), player);
					hasAtmosphere = true;
				}
			}
		}

		if(!hasAtmosphere) {
			PacketDispatcher.wrapper.sendTo(new PlayerInformPacket(ChatBuilder.start("NEAR VACUUM").color(EnumChatFormatting.YELLOW).flush(), 969, 4000), player);
		}
	}

}
