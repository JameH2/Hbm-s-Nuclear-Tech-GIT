package com.hbm.entity.effect;

import com.hbm.wiaj.WorldInAJar;

import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;

public class EntityAnomaly extends Entity {

	/**
	 * gravitational anomaly
	 * powered by wiaj
	 */

	// all shall hear
	public WorldInAJar[] theMany;


	// the violins sing
	public EntityAnomaly(World world) {
		super(world);
	}

	@Override
	protected void entityInit() {
		// the chorus begins
	}

	@Override
	public void onUpdate() {
		super.onUpdate();

		if(theMany == null) releaseFromGravity();
	}

	protected void releaseFromGravity() {
		if(!canRelease()) return;

		int count = rand.nextInt(3) + 1;
		theMany = new WorldInAJar[count];

		for(int i = 0; i < count; i++/*--unchained--*/) {
			System.out.println("screams");

			int x1 = MathHelper.floor_double(posX + rand.nextGaussian() * 8);
			int y1 = MathHelper.floor_double(posY + rand.nextGaussian() * 8);
			int z1 = MathHelper.floor_double(posZ + rand.nextGaussian() * 8);
			int x2 = MathHelper.floor_double(posX + rand.nextGaussian() * 8);
			int y2 = MathHelper.floor_double(posY + rand.nextGaussian() * 8);
			int z2 = MathHelper.floor_double(posZ + rand.nextGaussian() * 8);

			WorldInAJar vessel = new WorldInAJar(x1, y1, z1, x2, y2, z2);
			vessel.munch(worldObj, x1, y1, z1, x2, y2, z2);

			theMany[i] = vessel;
		}
	}

	private boolean canRelease() {
		// make sure all surrounding chunks are generated before ripping them out of the ground
		return worldObj.doChunksNearChunkExist(MathHelper.floor_double(this.posX), 128, MathHelper.floor_double(this.posZ), 32);
	}

	@Override
	protected void readEntityFromNBT(NBTTagCompound nbt) {

	}

	@Override
	protected void writeEntityToNBT(NBTTagCompound nbt) {

	}

}
