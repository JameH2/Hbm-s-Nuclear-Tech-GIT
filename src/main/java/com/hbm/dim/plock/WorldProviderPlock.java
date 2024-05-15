package com.hbm.dim.plock;

import com.hbm.config.WorldConfig;
import com.hbm.dim.WorldProviderCelestial;
import com.hbm.main.MainRegistry;
import com.hbm.util.AstronomyUtil;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.entity.Entity;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.biome.*;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraftforge.client.IRenderHandler;

public class WorldProviderPlock extends WorldProviderCelestial {
	@Override
	public void registerWorldChunkManager() {
		this.worldChunkMgr = new WorldChunkManagerPlock(worldObj);
	}

	@Override
	public String getDimensionName() {
		return "Plock";
	}
	
	@Override
	public IChunkProvider createChunkGenerator() {
		return new ChunkProviderPlock(this.worldObj, this.getSeed(), false);
	}
}