package com.hbm.dim.eve;

import com.hbm.config.WorldConfig;
import com.hbm.handler.ImpactWorldHandler;
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

public class WorldProviderEve extends WorldProvider {
	
	public void registerWorldChunkManager() {
		
		this.worldChunkMgr = new WorldChunkManagerEve(worldObj);
		//this.dimensionId = WorldConfig.dunaDimension;
		//this.hasNoSky = false;
	}

	@Override
	public String getDimensionName() {
		return "Eve";
	}
	
    public IChunkProvider createChunkGenerator()
    {
        return new ChunkProviderEve(this.worldObj, this.getSeed(), false);
    }
    
	public void renderClouds() {
	}
    
    @SideOnly(Side.CLIENT)
    public Vec3 getFogColor(float x, float y) {
		float f2 = MathHelper.cos(x * (float)Math.PI * 2.0F) * 2.0F + 0.5F;

		if (f2 < 0.0F)
		{
			f2 = 0.0F;
		}

		if (f2 > 1.0F)
		{
			f2 = 1.0F;
		}

		//float f3 = 0.7529412F;
		float f3 = 75F / 255F;
		float f4 = 44F / 255F;
		//float f5 = 1.0F;
		float f5 = 107F / 255F;

		f3 *= f2 * 0.94F + 0.06F;
		f4 *= f2 * 0.94F + 0.06F;
		f5 *= f2 * 0.91F + 0.09F;
		return Vec3.createVectorHelper((double)f3, (double)f4, (double)f5);

        //float f = 1.0F - this.getStarBrightness(1.0F);
      //return Vec3.createVectorHelper((double)f3, (double)f4, (double)f5);
    }
    
    public Vec3 getSkyColor(Entity camera, float partialTicks) {
        float f1 = worldObj.getCelestialAngle(partialTicks);
        float f2 = MathHelper.cos(f1 * (float)Math.PI * 2.0F) * 2.0F + 0.5F;

        if (f2 < 0.0F)
        {
            f2 = 0.0F;
        }

        if (f2 > 1.0F)
        {
            f2 = 1.0F;
        }
        float f4 = (float)(75) / 255.0F;
        float f5 = (float)(44) / 255.0F;
        float f6 = (float)(107) / 255.0F;
        f4 *= f2;
        f5 *= f2;
        f6 *= f2;
        float f9;

       
        if (worldObj.lastLightningBolt > 0)
        {
            f9 = (float)worldObj.lastLightningBolt - partialTicks;

            if (f9 > 1.0F)
            {
                f9 = 1.0F;
            }

            f9 *= 0.45F;
            f4 = f4 * (1.0F - f9) + 0.8F * f9;
            f5 = f5 * (1.0F - f9) + 0.8F * f9;
            f6 = f6 * (1.0F - f9) + 1.0F * f9;
        }        
        return Vec3.createVectorHelper((double)f4, (double)f5, (double)f6);
    }
    
    @SideOnly(Side.CLIENT)
    public float[] calcSunriseSunsetColors(float p_76560_1_, float p_76560_2_) {
    	return null;
    }

    public boolean canDoLightning(Chunk chunk)
    {
        return true;
    }

    public boolean canDoRainSnowIce(Chunk chunk)
    {
        return true;
    }
	@Override
	@SideOnly(Side.CLIENT)
	public float getStarBrightness(float par1) {
		return 0;
	}
    public boolean canRespawnHere()
    {
        return false;
    }
    
    @SideOnly(Side.CLIENT)
    public float getCloudHeight()
    {
		return -100;
	}

	@SideOnly(Side.CLIENT)
	public IRenderHandler getSkyRenderer() {
		return new SkyProviderEve();
	}
	
    public long getDayLength()
    {
    	return (long) (3.045*24000);
    }
    
    @Override
    public float calculateCelestialAngle(long par1, float par3)
    {
        par1 = this.getWorldTime();
        int j = (int) (par1 % this.getDayLength());
        float f1 = (j + par3) / this.getDayLength() - 0.25F;

        if (f1 < 0.0F)
        {
            ++f1;
        }

        if (f1 > 1.0F)
        {
            --f1;
        }

        float f2 = f1;
        f1 = 0.5F - MathHelper.cos(f1 * 3.1415927F) / 2.0F;
        return f2 + (f1 - f2) / 3.0F;
    }

	@Override
	public void updateWeather()
	{
		//this.worldObj.getWorldInfo().setRainTime(0);
		this.worldObj.getWorldInfo().setRaining(true);
		//this.worldObj.getWorldInfo().setThunderTime(0);
		this.worldObj.getWorldInfo().setThundering(true);
		this.worldObj.rainingStrength = 1F;
		this.worldObj.thunderingStrength = 1.0F;
	}
    
	@Override
	@SideOnly(Side.CLIENT)
	public float getSunBrightness(float par1) {
		float sunBr = worldObj.getSunBrightnessFactor(par1);
		return (sunBr * 0.3F);
	}
}