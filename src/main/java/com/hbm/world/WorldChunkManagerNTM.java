package com.hbm.world;

import java.util.List;

import com.hbm.handler.ImpactWorldHandler;
import com.hbm.saveddata.TomSaveData;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.World;
import net.minecraft.world.WorldProviderSurface;
import net.minecraft.world.WorldType;
import net.minecraft.world.biome.BiomeCache;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.biome.WorldChunkManager;

public class WorldChunkManagerNTM extends WorldChunkManager {
	//public static float temp;
	public WorldChunkManagerNTM(long seed, WorldType type)
	{
		super(seed, type);
	}
	public WorldChunkManagerNTM(World world)
	{
		this(world.getSeed(), world.getWorldInfo().getTerrainType());
	}
	@Override
    @SideOnly(Side.CLIENT)
    public float getTemperatureAtHeight(float temp, int height)
    {
		TomSaveData data = TomSaveData.getLastCachedOrNull();
		//System.out.println("temp: "+temp);
        return temp-(ImpactWorldHandler.getWinterForClient(Minecraft.getMinecraft().theWorld)*0.75f);
    }
}