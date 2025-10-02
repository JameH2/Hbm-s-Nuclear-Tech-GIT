package com.hbm.tileentity;

import io.netty.buffer.ByteBuf;

public interface IEntityInteractionReceiver {

	public void interaction(ByteBuf buf);

	public int getDimensionId();
	public int getEntityId();

}
