package com.hbm.dim.projectile;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import com.hbm.dim.CelestialBody;
import com.hbm.dim.SolarSystemWorldSavedData;
import com.hbm.dim.trait.CBT_War;

import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;


public class ProjectileManager {

	private final List<Projectile> projectiles = new ArrayList<>();
	private static final Random rand = new Random();


	public static void launch(String sourceName, String targetName, Projectile projectile) {
		if(projectile.angle == 0 && projectile.inclination == 0) {
			projectile.angle = (rand.nextFloat() - 0.5F) * 300.0F;
			projectile.inclination = (rand.nextFloat() - 0.5F) * 120.0F;
		}

		SolarSystemWorldSavedData data = SolarSystemWorldSavedData.get();
		data.getProjectileManager().projectiles.add(projectile);
		data.markDirty();

		CelestialBody target = CelestialBody.getBody(targetName);
		if(target != null && !target.hasTrait(CBT_War.class)) {
			target.modifyTraits(new CBT_War());
		}
	}

	/**
	 * Convenience: launch a small projectile.
	 */
	public static void launchSmall(String sourceName, String targetName, int travelTime, int damage, double worldX, double worldY, double worldZ) {
		launch(sourceName, targetName, new ProjectileSmall(sourceName, targetName, travelTime, damage, worldX, worldY, worldZ));
	}

	/**
	 * Convenience: launch a splitshot projectile.
	 */
	public static void launchSplitshot(String sourceName, String targetName, int travelTime, int damagePerChild, int splitCount, double worldX, double worldY ,double worldZ) {
		launch(sourceName, targetName, new ProjectileSplitshot(sourceName, targetName, travelTime, damagePerChild, splitCount, worldX, worldY, worldZ));
	}

	// === Tick ===


	public void update(boolean isRemote) {
		List<Projectile> toAdd = new ArrayList<>();

		Iterator<Projectile> it = projectiles.iterator();
		while(it.hasNext()) {
			Projectile proj = it.next();
			proj.tick(isRemote);

			List<Projectile> children = proj.getSpawnedChildren();
			if(children != null && !children.isEmpty()) {
				toAdd.addAll(children);
			}

			if(!isRemote && proj.getImpactTimer() == 1) {
				applyWorldImpact(proj);
			}

			if(proj.isDead()) {
				it.remove();
			}
		}

		if(!toAdd.isEmpty()) {
			projectiles.addAll(toAdd);
		}
	}

	private void applyWorldImpact(Projectile proj) {
		CelestialBody target = CelestialBody.getBody(proj.getTargetName());
		if(target == null || !target.canLand) return;

		MinecraftServer server = MinecraftServer.getServer();
		if(server == null) return;

		World targetWorld = server.worldServerForDimension(target.dimensionId);
		if(targetWorld != null) {
			proj.onWorldImpact(targetWorld);
		}
	}


	public List<Projectile> getProjectiles() {
		return projectiles;
	}

	public List<Projectile> getProjectilesTargeting(String bodyName) {
		List<Projectile> result = new ArrayList<>();
		for(Projectile proj : projectiles) {
			if(bodyName.equals(proj.getTargetName())) {
				result.add(proj);
			}
		}
		return result;
	}

	public List<Projectile> getProjectilesFrom(String bodyName) {
		List<Projectile> result = new ArrayList<>();
		for(Projectile proj : projectiles) {
			if(bodyName.equals(proj.getSourceName())) {
				result.add(proj);
			}
		}
		return result;
	}

	
	public void writeToNBT(NBTTagCompound nbt) {
		NBTTagList list = new NBTTagList();
		for(Projectile proj : projectiles) {
			NBTTagCompound tag = new NBTTagCompound();
			proj.writeToNBT(tag);
			list.appendTag(tag);
		}
		nbt.setTag("projectiles", list);
	}

	public void readFromNBT(NBTTagCompound nbt) {
		projectiles.clear();
		
		NBTTagList list = nbt.getTagList("projectiles", Constants.NBT.TAG_COMPOUND);
		
		for(int i = 0; i < list.tagCount(); i++) {
			NBTTagCompound tag = list.getCompoundTagAt(i);
			int id = tag.getInteger("id");
			Projectile proj = Projectile.create(id);
			if(proj != null) {
				proj.readFromNBT(tag);
				projectiles.add(proj);
			}
		}
	}

	public void writeToBytes(ByteBuf buf) {
		buf.writeInt(projectiles.size());
		for(Projectile proj : projectiles) {
			proj.writeToBytes(buf);
		}
	}

	public void readFromBytes(ByteBuf buf) {
		projectiles.clear();
		int count = buf.readInt();
		for(int i = 0; i < count; i++) {
			int id = buf.readInt();
			Projectile proj = Projectile.create(id);
			if(proj != null) {
				proj.readFromBytes(buf);
				projectiles.add(proj);
			}
		}
	}
}