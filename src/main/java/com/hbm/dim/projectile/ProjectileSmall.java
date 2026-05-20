package com.hbm.dim.projectile;

import net.minecraft.world.World;

public class ProjectileSmall extends Projectile {
	 
	public ProjectileSmall() {
		
	}
 
	public ProjectileSmall(String sourceName, String targetName, int travelTime, int damage, double worldX, double WorldY, double worldZ) {
		super(sourceName, targetName, travelTime, damage, 1, worldX, WorldY, worldZ);
	}
 
	@Override
	protected int getImpactDuration() {
		return 200;
	}
 
	@Override
	public void onWorldImpact(World world) {
		//TODO: basic nuclear explosion or custom one of some sort :)
	}
}
