package com.hbm.dim;

import java.util.ArrayDeque;
import java.util.Queue;

import com.hbm.entity.missile.EntityRideableRocket;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.relauncher.Side;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.ServerConfigurationManager;
import net.minecraft.util.MathHelper;
import net.minecraft.world.Teleporter;
import net.minecraft.world.WorldServer;

public class CelestialTeleporter extends Teleporter {

	private final WorldServer sourceServer;
	private final WorldServer targetServer;

	private double x;
	private double y;
	private double z;

	private boolean grounded; // Should we be placed directly on the first ground block below?

	private Entity entity;

	public CelestialTeleporter(WorldServer sourceServer, WorldServer targetServer, Entity entity, double x, double y, double z, boolean grounded) {
		super(targetServer);
		this.sourceServer = sourceServer;
		this.targetServer = targetServer;
		this.entity = entity;
		this.x = x;
		this.y = y;
		this.z = z;
		this.grounded = grounded;
	}

	@Override
	public void placeInPortal(Entity entity, double ox, double oy, double oz, float yaw) {
		int ix = (int)x;
		int iy = (int)y;
		int iz = (int)z;

		if(grounded) {
			for(int i = targetServer.getHeight(); i > 0; i--) {
				if(targetServer.getBlock(ix, i, iz) != Blocks.air) {
					y = i + 5;
					break;
				}
			}
		} else {
			targetServer.getBlock(ix, MathHelper.clamp_int(iy, 1, 255), iz); // dummy load to maybe gen chunk
		}

		entity.setPosition(x, y, z);
	}

	private void runTeleport() {
		MinecraftServer mcServer = MinecraftServer.getServer();
		ServerConfigurationManager manager = mcServer.getConfigurationManager();

		// If this entity got teleported with a player rider, switch to the rider!
		if(entity.riddenByEntity instanceof EntityPlayerMP) {
			entity = entity.riddenByEntity;
		}

		// Store these since they change after transfer
		int fromDimension = entity.dimension;

		entity.posX = x;
		entity.posZ = z;

		if(entity instanceof EntityPlayerMP) {
			EntityPlayerMP playerMP = (EntityPlayerMP) entity;
			Entity ridingEntity = entity.ridingEntity;

			manager.transferPlayerToDimension(playerMP, targetServer.provider.dimensionId, this);

			if(ridingEntity != null && !ridingEntity.isDead) {
				ridingEntity.dimension = fromDimension;
				ridingEntity.worldObj.removeEntity(ridingEntity);
				ridingEntity.isDead = false;

				manager.transferEntityToWorld(ridingEntity, fromDimension, sourceServer, targetServer, this);

				Entity newEntity = EntityList.createEntityByName(EntityList.getEntityString(ridingEntity), targetServer);
				if(newEntity != null) {
					newEntity.copyDataFrom(ridingEntity, true);
					newEntity.posX = x;
					newEntity.posZ = z;
					targetServer.spawnEntityInWorld(newEntity);
					newEntity.dimension = targetServer.provider.dimensionId;
				}

				ridingEntity.isDead = true;
				sourceServer.resetUpdateEntityTick();
				targetServer.resetUpdateEntityTick();

				playerMP.mountEntity(newEntity);

				// Ensure rocket stickiness
				if(newEntity instanceof EntityRideableRocket) {
					((EntityRideableRocket) newEntity).setThrower(playerMP);
				}

				// Send another packet to the client to make sure they load in correctly!
				playerMP.setPositionAndUpdate(x, 900, z);
			}
		} else {
			entity.worldObj.removeEntity(entity);
			entity.isDead = false;

			manager.transferEntityToWorld(entity, fromDimension, sourceServer, targetServer, this);

			Entity newEntity = EntityList.createEntityByName(EntityList.getEntityString(entity), targetServer);
			if(newEntity != null) {
				newEntity.copyDataFrom(entity, true);
				newEntity.posX = x;
				newEntity.posZ = z;
				targetServer.spawnEntityInWorld(newEntity);
				newEntity.dimension = targetServer.provider.dimensionId;
			}

			entity.isDead = true;
			sourceServer.resetUpdateEntityTick();
			targetServer.resetUpdateEntityTick();
		}
	}

	public static void runQueuedTeleport() {
		CelestialTeleporter teleporter = queue.poll();
		if(teleporter != null) teleporter.runTeleport();
	}

	private static Queue<CelestialTeleporter> queue = new ArrayDeque<>();

	public static void teleport(Entity entity, int dim, double x, double y, double z, boolean grounded) {
		if(entity.dimension == dim) return; // ignore if we're teleporting to the same place

		MinecraftServer mcServer = MinecraftServer.getServer();
		Side sidex = FMLCommonHandler.instance().getEffectiveSide();
		if(sidex == Side.SERVER) {
			WorldServer sourceServer = mcServer.worldServerForDimension(entity.dimension);
			WorldServer targetServer = mcServer.worldServerForDimension(dim);

			queue.add(new CelestialTeleporter(sourceServer, targetServer, entity, x, y, z, grounded));
		}
	}

}
