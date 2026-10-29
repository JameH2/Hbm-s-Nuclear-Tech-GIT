package com.hbm.entity.logic;

import com.hbm.main.MainRegistry;

import net.minecraft.entity.Entity;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeChunkManager;
import net.minecraftforge.common.ForgeChunkManager.Ticket;
import net.minecraftforge.common.ForgeChunkManager.Type;

public abstract class EntityExplosionChunkloading extends Entity implements IChunkLoader {

	private EntityChunkLoader chunkLoader;

	public EntityExplosionChunkloading(World world) {
		super(world);
	}

	@Override
	protected void entityInit() {
		init(ForgeChunkManager.requestTicket(MainRegistry.instance, worldObj, Type.ENTITY));
	}

	@Override
	public void init(Ticket ticket) {
		if(this.chunkLoader == null) this.chunkLoader = new EntityChunkLoader(this);
		this.chunkLoader.setTicket(ticket);
	}

	public void loadChunk(int x, int z) {
		if(this.chunkLoader != null) this.chunkLoader.loadChunk(x, z);
	}

	@Override
	public void setDead() {
		super.setDead();
		this.clearChunkLoader();
	}
	
	public void clearChunkLoader() {
		if(this.chunkLoader != null) this.chunkLoader.clear();
	}
}
