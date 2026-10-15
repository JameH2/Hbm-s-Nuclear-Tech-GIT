package com.hbm.tileentity.network;

import com.hbm.tileentity.TileEntityLoadedBase;

import io.netty.buffer.ByteBuf;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

public class TileEntityCraneSplitter extends TileEntityLoadedBase {

	/* false: left belt is preferred, true: right belt is preferred */
	private final boolean[] position = new boolean[3];
	private final byte[] remaining = new byte[3]; // count until position swaps, per conveyor lane

	public byte leftRatio = 1;
	public byte rightRatio = 1;

	// Splits the input stack into two, based on current ratio and internal state
	public ItemStack[] splitStack(ItemStack stack, int lane) {
		lane = Math.max(0, Math.min(lane, position.length - 1));
		int left = 0;
		int right = 0;
		int count = stack.stackSize;

		if(remaining[lane] <= 0) remaining[lane] = position[lane] ? rightRatio : leftRatio;

		while(count > 0) {
			int toExtract = Math.min(remaining[lane], count);

			remaining[lane] -= toExtract;
			count -= toExtract;
			if(position[lane]) right += toExtract; else left += toExtract;

			if(remaining[lane] <= 0) {
				position[lane] = !position[lane];
				remaining[lane] = position[lane] ? rightRatio : leftRatio;
			}
		}

		ItemStack leftStack = stack.copy();
		ItemStack rightStack = stack.copy();
		leftStack.stackSize = left;
		rightStack.stackSize = right;

		worldObj.markTileEntityChunkModified(xCoord, yCoord, zCoord, this);
		return new ItemStack[] { leftStack, rightStack };
	}

	public void updateEntity() {
		if(worldObj.isRemote) return;
		networkPackNT(15);
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);

		for(int lane = 0; lane < position.length; lane++) {
			position[lane] = nbt.getBoolean("pos" + lane);
			remaining[lane] = nbt.getByte("count" + lane);
		}

		// Make sure existing conveyors are initialised with ratios
		leftRatio = (byte)Math.max(nbt.getByte("left"), 1);
		rightRatio = (byte)Math.max(nbt.getByte("right"), 1);
	}

	@Override
	public void writeToNBT(NBTTagCompound nbt) {
		super.writeToNBT(nbt);

		for(int lane = 0; lane < position.length; lane++) {
			nbt.setBoolean("pos" + lane, position[lane]);
			nbt.setByte("count" + lane, remaining[lane]);
		}

		nbt.setByte("left", leftRatio);
		nbt.setByte("right", rightRatio);
	}

	@Override
	public void serialize(ByteBuf buf) {
		buf.writeByte(leftRatio);
		buf.writeByte(rightRatio);
	}

	@Override
	public void deserialize(ByteBuf buf) {
		leftRatio = buf.readByte();
		rightRatio = buf.readByte();
	}

}
