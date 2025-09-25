package com.hbm.packet.toserver;

import com.hbm.saveddata.SatelliteSavedData;
import com.hbm.saveddata.satellites.Satellite;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;

public class SatActivatePacket implements IMessage {

	//0: Add
	//1: Subtract
	//2: Set

	int freq;

	public SatActivatePacket()
	{

	}

	public SatActivatePacket(int freq)
	{

		this.freq = freq;
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		freq = buf.readInt();
	}

	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeInt(freq);
	}

	public static class Handler implements IMessageHandler<SatActivatePacket, IMessage> {

		@Override
		public IMessage onMessage(SatActivatePacket m, MessageContext ctx) {
			EntityPlayer p = ctx.getServerHandler().playerEntity;
			//how gross is this...
			Satellite sat = SatelliteSavedData.getData(p.worldObj, (int)p.posX, (int)p.posZ).getSatFromFreq(m.freq);
			if(sat != null) {
				sat.onClick(p.worldObj, 0, 0);

			}

			System.out.println("facket");
			return null;
		}
	}
}
