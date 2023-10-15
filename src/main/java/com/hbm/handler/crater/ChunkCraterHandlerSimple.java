package com.hbm.handler.crater;

import java.util.HashMap;
import java.util.Map.Entry;

import com.hbm.blocks.ModBlocks;
import com.hbm.config.RadiationConfig;
import com.hbm.packet.AuxParticlePacket;
import com.hbm.packet.PacketDispatcher;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.network.NetworkRegistry.TargetPoint;
import net.minecraft.block.material.Material;
import net.minecraft.init.Blocks;
import net.minecraft.util.MathHelper;
import net.minecraft.world.ChunkCoordIntPair;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.gen.ChunkProviderServer;
import net.minecraftforge.event.world.ChunkDataEvent;
import net.minecraftforge.event.world.ChunkEvent;
import net.minecraftforge.event.world.WorldEvent;

/**
 * Most basic implementation of a chunk radiation system: Each chunk has a radiation value which spreads out to its neighbors.
 * @author hbm
 */
public class ChunkCraterHandlerSimple extends ChunkCraterHandler {
	
	private HashMap<World, CraterGenPerWorld> perWorld = new HashMap();
	
	public boolean getCraterGen(World world, int x, int z) {
		CraterGenPerWorld radWorld = perWorld.get(world);
		
		if(radWorld != null) {
			ChunkCoordIntPair coords = new ChunkCoordIntPair(x >> 4, z >> 4);
			Boolean rad = radWorld.crater.get(coords);
			return rad == null ? false : rad;
		}
		
		return false;
	}

	public void setCraterGen(World world, int x, int z, boolean retro) {
		CraterGenPerWorld radWorld = perWorld.get(world);
		
		if(radWorld != null) {
			
			if(world.blockExists(x, 0, z)) {
				
				ChunkCoordIntPair coords = new ChunkCoordIntPair(x >> 4, z >> 4);
				radWorld.crater.put(coords, retro);
				world.getChunkFromBlockCoords(x, z).isModified = true;
			}
		}
	}
	
	@SubscribeEvent
	public void receiveWorldLoad(WorldEvent.Load event) {
		if(!event.world.isRemote)
			perWorld.put(event.world, new CraterGenPerWorld());
	}

	@SubscribeEvent
	public void receiveWorldUnload(WorldEvent.Unload event) {
		if(!event.world.isRemote)
			perWorld.remove(event.world);
	}
	
	private static final String NBT_KEY_CRATER_RETROGEN = "hfr_crater_retrogen";

	@SubscribeEvent
	public void receiveChunkLoad(ChunkDataEvent.Load event) {
		
		if(!event.world.isRemote) {
			CraterGenPerWorld radWorld = perWorld.get(event.world);
			
			if(radWorld != null) {
				radWorld.crater.put(event.getChunk().getChunkCoordIntPair(), event.getData().getBoolean(NBT_KEY_CRATER_RETROGEN));
			}
		}
	}

	@SubscribeEvent
	public void receiveChunkSave(ChunkDataEvent.Save event) {
		
		if(!event.world.isRemote) {
			CraterGenPerWorld radWorld = perWorld.get(event.world);
			
			if(radWorld != null) {
				Boolean val = radWorld.crater.get(event.getChunk().getChunkCoordIntPair());
				boolean rad = val == null ? false : val;
				event.getData().setBoolean(NBT_KEY_CRATER_RETROGEN, rad);
			}
		}
	}

	@SuppressWarnings("unlikely-arg-type")
	@SubscribeEvent
	public void receiveChunkUnload(ChunkEvent.Unload event) {
		
		if(!event.world.isRemote) {
			CraterGenPerWorld radWorld = perWorld.get(event.world);
			
			if(radWorld != null) {
				radWorld.crater.remove(event.getChunk());
			}
		} 
	}
	
	public static class CraterGenPerWorld {
		
		public HashMap<ChunkCoordIntPair, Boolean> crater = new HashMap();
	}
}
