package com.hbm.packet.toclient;

import com.hbm.main.MainRegistry;
import com.hbm.tileentity.IEntityInteractionReceiver;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.Entity;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.DimensionManager;

public class EntityInteractPacket implements IMessage {

	int dimensionId;
	int entityId;
	ByteBuf buf;

	public EntityInteractPacket() {}

	public EntityInteractPacket(IEntityInteractionReceiver rec, ByteBuf buf) {
		this.dimensionId = rec.getDimensionId();
		this.entityId = rec.getEntityId();
		this.buf = buf;
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		this.dimensionId = buf.readInt();
		this.entityId = buf.readInt();
		this.buf = buf;
	}

	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeInt(dimensionId);
		buf.writeInt(entityId);
		buf.writeBytes(this.buf);
	}

	public static class Handler implements IMessageHandler<EntityInteractPacket, IMessage> {

		@Override
		public IMessage onMessage(EntityInteractPacket m, MessageContext ctx) {
			WorldServer world = DimensionManager.getWorld(m.dimensionId);

			if(world == null) return null;

			Entity entity = world.getEntityByID(m.entityId);

			if(entity instanceof IEntityInteractionReceiver) {
				try {
					((IEntityInteractionReceiver) entity).interaction(m.buf);
				} catch(Exception e) { // I  don't think I can blame gamma anymore at this point ;)
					MainRegistry.logger.warn("An EntityInteractPacket packet failed to be read and has thrown an error. This normally means that there was a buffer underflow and more data was read than was actually in the packet.");
					MainRegistry.logger.warn("Entity: {}", entity.getCommandSenderName());
					MainRegistry.logger.warn(e.getMessage());
				} finally {
					m.buf.release();
				}
			}

			return null;
		}
	}
}
