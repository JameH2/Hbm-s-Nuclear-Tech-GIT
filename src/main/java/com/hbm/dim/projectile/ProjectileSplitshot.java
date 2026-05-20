package com.hbm.dim.projectile;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import net.minecraft.world.World;


public class ProjectileSplitshot extends Projectile {

	private int splitCount = 4;

	private float splitProgress = 0.6F;

	private boolean hasSplit = false;
	private List<Projectile> children = null;

	public ProjectileSplitshot() {
	}

	public ProjectileSplitshot(String sourceName, String targetName, int travelTime, int damagePerChild, int splitCount, double worldX, double worldY, double worldZ) {
		super(sourceName, targetName, travelTime, damagePerChild, 2, worldX, worldY, worldZ);
		this.splitCount = splitCount;
	}

	@Override
	protected void onTravel(boolean isRemote) {
		if (!hasSplit && getTravelProgress() >= splitProgress) {
			hasSplit = true;

			if (!isRemote) {
				children = new ArrayList<>();
				Random rand = new Random();

				int remainingTicks = travelTime;

				for (int i = 0; i < splitCount; i++) {
					double offsetX = (rand.nextDouble() * 160 - 80);
					double offsetY = (rand.nextDouble() * 160 - 80);
					double offsetZ = (rand.nextDouble() * 160 - 80);

					int childTravel = remainingTicks + rand.nextInt(40) - 10;
					childTravel = Math.max(20, childTravel);

					ProjectileSmall child = new ProjectileSmall(sourceName, targetName, childTravel, damage, worldX + offsetX, worldY + offsetY, worldZ + offsetZ);

					child.angle = (rand.nextFloat() - 0.5F) * 300.0F;
					child.inclination = (rand.nextFloat() - 0.5F) * 120.0F;

					children.add(child);
				}
			}

			dead = true;
		}
	}

	@Override
	public List<Projectile> getSpawnedChildren() {
		List<Projectile> result = children;
		children = null;
		return result;
	}

	@Override
	protected int getImpactDuration() {
		return 0;
	}

	@Override
	public void onWorldImpact(World world) {
	}

	@Override
	protected void onFinish(boolean isRemote) {

	}

	@Override
	protected void writeExtraNBT(net.minecraft.nbt.NBTTagCompound nbt) {
		nbt.setInteger("splitCount", splitCount);
		nbt.setFloat("splitProgress", splitProgress);
		nbt.setBoolean("hasSplit", hasSplit);
	}

	@Override
	protected void readExtraNBT(net.minecraft.nbt.NBTTagCompound nbt) {
		splitCount = nbt.getInteger("splitCount");
		splitProgress = nbt.getFloat("splitProgress");
		hasSplit = nbt.getBoolean("hasSplit");
	}

	@Override
	protected void writeExtraBytes(io.netty.buffer.ByteBuf buf) {
		buf.writeInt(splitCount);
		buf.writeFloat(splitProgress);
		buf.writeBoolean(hasSplit);
	}

	@Override
	protected void readExtraBytes(io.netty.buffer.ByteBuf buf) {
		splitCount = buf.readInt();
		splitProgress = buf.readFloat();
		hasSplit = buf.readBoolean();
	}
}