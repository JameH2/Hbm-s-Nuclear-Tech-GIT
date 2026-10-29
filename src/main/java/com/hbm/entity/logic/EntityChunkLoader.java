package com.hbm.entity.logic;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import net.minecraft.entity.Entity;
import net.minecraft.world.ChunkCoordIntPair;
import net.minecraftforge.common.ForgeChunkManager;
import net.minecraftforge.common.ForgeChunkManager.Ticket;

public class EntityChunkLoader {

	private final Entity entity;
	private Ticket ticket;
	private final Set<ChunkCoordIntPair> forcedChunks = new HashSet<ChunkCoordIntPair>();

	public EntityChunkLoader(Entity entity) {
		this.entity = entity;
	}

	public void setTicket(Ticket ticket) {
		if(entity.worldObj.isRemote || ticket == null) return;

		if(this.ticket != null && this.ticket != ticket) {
			ForgeChunkManager.releaseTicket(this.ticket);
		}

		this.ticket = ticket;
		this.ticket.bindEntity(entity);

		forcedChunks.clear();
		forcedChunks.addAll(ticket.getChunkList());
	}

	public void loadChunk(int chunkX, int chunkZ) {
		loadChunks(Collections.singleton(new ChunkCoordIntPair(chunkX, chunkZ)));
	}

	public void loadChunks(Set<ChunkCoordIntPair> desiredChunks) {
		if(entity.worldObj.isRemote || ticket == null) return;

		if(forcedChunks.equals(desiredChunks)) return;

		for(ChunkCoordIntPair chunk : forcedChunks) {
			if(!desiredChunks.contains(chunk)) {
				ForgeChunkManager.unforceChunk(ticket, chunk);
			}
		}

		for(ChunkCoordIntPair chunk : desiredChunks) {
			if(!forcedChunks.contains(chunk)) {	
				ForgeChunkManager.forceChunk(ticket, chunk);
			}
		}

		forcedChunks.clear();
		forcedChunks.addAll(ticket.getChunkList());
	}

	public void clear() {
		if(entity.worldObj.isRemote) return;

		if(ticket != null) {
			ForgeChunkManager.releaseTicket(ticket);
			ticket = null;
		}
		forcedChunks.clear();
	}
}
