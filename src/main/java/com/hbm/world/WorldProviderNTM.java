package com.hbm.world;

import com.hbm.handler.ImpactWorldHandler;
import com.hbm.main.MainRegistry;
import com.hbm.saveddata.TomSaveData;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.WorldProviderSurface;
import net.minecraft.world.WorldType;
import net.minecraft.world.biome.BiomeGenBase;

public class WorldProviderNTM extends WorldProviderSurface {
	
	private float[] colorsSunriseSunset = new float[4];
	//public WorldChunkManagerNTM worldChunkMgr;

	public WorldProviderNTM() {
	}


	
	@Override
    public void registerWorldChunkManager()
    {
		this.worldChunkMgr = new WorldChunkManagerNTM(this.worldObj);
    }
	
	@Override
	public float calculateCelestialAngle(long worldTime, float partialTicks) {
		return super.calculateCelestialAngle(worldTime, partialTicks);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public float[] calcSunriseSunsetColors(float par1, float par2) {
		float f2 = 0.4F;
		float f3 = MathHelper.cos(par1 * (float) Math.PI * 2.0F) - 0.0F;
		float f4 = -0.0F;
		float dust = MainRegistry.proxy.getImpactDust(worldObj);

		if(f3 >= f4 - f2 && f3 <= f4 + f2) {
			float f5 = (f3 - f4) / f2 * 0.5F + 0.5F;
			float f6 = 1.0F - (1.0F - MathHelper.sin(f5 * (float) Math.PI)) * 0.99F;
			f6 *= f6;
			this.colorsSunriseSunset[0] = (f5 * 0.3F + 0.7F) * (1 - dust);
			this.colorsSunriseSunset[1] = (f5 * f5 * 0.7F + 0.2F) * (1 - dust);
			this.colorsSunriseSunset[2] = (f5 * f5 * 0.0F + 0.2F) * (1 - dust);
			this.colorsSunriseSunset[3] = f6 * (1 - dust);
			return this.colorsSunriseSunset;
		} else {
			return null;
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public float getStarBrightness(float par1) {
		float starBr = worldObj.getStarBrightnessBody(par1);
		float dust = MainRegistry.proxy.getImpactDust(worldObj);
		float f1 = worldObj.getCelestialAngle(par1);
		float f2 = 1.0F - (MathHelper.cos(f1 * (float) Math.PI * 2.0F) * 2.0F + 0.25F);

		if(f2 < 0.0F) {
			f2 = 0.0F;
		}

		if(f2 > 1.0F) {
			f2 = 1.0F;
		}
		return starBr * (1 - dust);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public float getSunBrightness(float par1) {
		float dust = ImpactWorldHandler.getDustForClient(MainRegistry.proxy.me().worldObj);
		float sunBr = worldObj.getSunBrightnessFactor(par1);
		return (sunBr * 0.8F + 0.2F) * (1 - dust);
	}

	@Override
	public boolean isDaytime() {
		float dust = MainRegistry.proxy.getImpactDust(worldObj);

		if(dust >= 0.75F) {
			return false;
		}
		return super.isDaytime();
	}

	@Override
	public float getSunBrightnessFactor(float par1) {
		float dust = MainRegistry.proxy.getImpactDust(worldObj);
		float sunBr = worldObj.getSunBrightnessFactor(par1);
		float dimSun = sunBr * (1 - dust);
		return dimSun;
	}

	/**
	 * Return Vec3D with biome specific fog color
	 */
	@SideOnly(Side.CLIENT)
	@Override
	public Vec3 getFogColor(float p_76562_1_, float p_76562_2_) {
        float f2 = MathHelper.cos(p_76562_1_ * (float)Math.PI * 2.0F) * 2.0F + 0.5F;

        if (f2 < 0.0F)
        {
            f2 = 0.0F;
        }

        if (f2 > 1.0F)
        {
            f2 = 1.0F;
        }
		Vec3 fog = super.getFogColor(p_76562_1_, p_76562_2_);
		Vec3 quack = Vec3.createVectorHelper(1.0F*(f2 * 0.94F + 0.06F),0.80392157F*(f2 * 0.94F + 0.06F),0.80392157F*(f2 * 0.91F + 0.09F));
		float dust = MainRegistry.proxy.getImpactDust(worldObj);
		float fire = MainRegistry.proxy.getImpactFire(worldObj);
		boolean impact = MainRegistry.proxy.getImpact(worldObj);
		float f3;
		float f4;
		float f5;
		if(impact && fire == 0)
		{
			f3=(float) quack.xCoord;
			f4=(float) quack.yCoord;
			f5=(float) quack.zCoord;
			if(dust>=0.75)
			{
				f3 = (float) ((105f/255f)*(1f-((dust-0.75f)*4))+((0)*((dust-0.75f)*4)));//(float) ((sky.xCoord + (0 - sky.xCoord) * 1) * dust);
				f4 = (float) ((27f/255f)*(1f-((dust-0.75f)*4))+((0)*((dust-0.75f)*4)));
				f5 = (float) ((4f/255f)*(1f-((dust-0.75f)*4))+((0)*((dust-0.75f)*4)));
			}
			if(dust<0.75 && dust>=0.25)
			{
				f3 = (float) ((202f/255f)*(1f-((dust-0.25f)*2))+((105f/255f)*((dust-0.25f)*2)));//(float) ((sky.xCoord + (0 - sky.xCoord) * 1) * dust);
				f4 = (float) ((167f/255f)*(1f-((dust-0.25f)*2))+((27f/255f)*((dust-0.25f)*2)));
				f5 = (float) ((123f/255f)*(1f-((dust-0.25f)*2))+((4f/255f)*((dust-0.25f)*2)));
			}
			if(dust<0.25)
			{
				f3 = (float) (quack.xCoord*(1f-(dust*4))+((202f/255f)*(dust*4)));//(float) ((sky.xCoord + (0 - sky.xCoord) * 1) * dust);
				f4 = (float) (quack.yCoord*(1f-(dust*4))+((167f/255f)*(dust*4)));
				f5 = (float) (quack.zCoord*(1f-(dust*4))+((123f/255f)*(dust*4)));
			}
			return Vec3.createVectorHelper((double) f3, (double) f4, (double) f5);
		}
		f3 = (float) fog.xCoord;
		f4 = (float) fog.yCoord;
		f5 = (float) fog.zCoord;
		if(dust>=0.75)
		{
			f3 = (float) ((105f/255f)*(1f-((dust-0.75f)*4))+((0)*((dust-0.75f)*4)));//(float) ((sky.xCoord + (0 - sky.xCoord) * 1) * dust);
			f4 = (float) ((27f/255f)*(1f-((dust-0.75f)*4))+((0)*((dust-0.75f)*4)));
			f5 = (float) ((4f/255f)*(1f-((dust-0.75f)*4))+((0)*((dust-0.75f)*4)));
		}
		if(dust<0.75 && dust>=0.25)
		{
			f3 = (float) ((202f/255f)*(1f-((dust-0.25f)*2))+((105f/255f)*((dust-0.25f)*2)));//(float) ((sky.xCoord + (0 - sky.xCoord) * 1) * dust);
			f4 = (float) ((167f/255f)*(1f-((dust-0.25f)*2))+((27f/255f)*((dust-0.25f)*2)));
			f5 = (float) ((123f/255f)*(1f-((dust-0.25f)*2))+((4f/255f)*((dust-0.25f)*2)));
		}
		if(dust<0.25)
		{
			f3 = (float) (fog.xCoord*(1f-(dust*4))+((202f/255f)*(dust*4)));//(float) ((sky.xCoord + (0 - sky.xCoord) * 1) * dust);
			f4 = (float) (fog.yCoord*(1f-(dust*4))+((167f/255f)*(dust*4)));
			f5 = (float) (fog.zCoord*(1f-(dust*4))+((123f/255f)*(dust*4)));
		}
		//if(fire > 0) {
		//	return Vec3.createVectorHelper((double) f3 * (Math.max((1 - (dust * 2)), 0)), (double) f4 * (Math.max((1 - (dust * 2)), 0)), (double) f5 * (Math.max((1 - (dust * 2)), 0)));
		//}
		return Vec3.createVectorHelper((double) f3, (double) f4, (double) f5);
		//return Vec3.createVectorHelper((double) f3 * (1 - dust), (double) f4 * (1 - dust), (double) f5 * (1 - dust));
	}

	@Override
	@SideOnly(Side.CLIENT)
	public Vec3 getSkyColor(Entity cameraEntity, float partialTicks) {
		Vec3 sky = super.getSkyColor(cameraEntity, partialTicks);
		float dust = MainRegistry.proxy.getImpactDust(worldObj);
		float fire = MainRegistry.proxy.getImpactFire(worldObj);

		float f4;
		float f5;
		float f6;
		Vec3 color;
		Vec3 prevColor;
		//System.out.println("sky.zCoord"+sky.zCoord);
		f4 = (float) (sky.xCoord);//(float) ((sky.xCoord + (0 - sky.xCoord) * 1) * dust);
		f5 = (float) (sky.yCoord);
		f6 = (float) (sky.zCoord);
		if(dust>=0.75)
		{
			f4 = (float) ((105f/255f)*(1f-((dust-0.75f)*4))+((0)*((dust-0.75f)*4)));//(float) ((sky.xCoord + (0 - sky.xCoord) * 1) * dust);
			f5 = (float) ((27f/255f)*(1f-((dust-0.75f)*4))+((0)*((dust-0.75f)*4)));
			f6 = (float) ((4f/255f)*(1f-((dust-0.75f)*4))+((0)*((dust-0.75f)*4)));
		}
		if(dust<0.75 && dust>=0.25)
		{
			f4 = (float) ((202f/255f)*(1f-((dust-0.25f)*2))+((105f/255f)*((dust-0.25f)*2)));//(float) ((sky.xCoord + (0 - sky.xCoord) * 1) * dust);
			f5 = (float) ((167f/255f)*(1f-((dust-0.25f)*2))+((27f/255f)*((dust-0.25f)*2)));
			f6 = (float) ((123f/255f)*(1f-((dust-0.25f)*2))+((4f/255f)*((dust-0.25f)*2)));
		}
		if(dust<0.25)
		{
			if(fire==0)
			{
				f4 = (float) (sky.xCoord*(1f-(dust*4))+((202f/255f)*(dust*4)));//(float) ((sky.xCoord + (0 - sky.xCoord) * 1) * dust);
				f5 = (float) (sky.yCoord*(1f-(dust*4))+((167f/255f)*(dust*4)));
				f6 = (float) (sky.zCoord*(1f-(dust*4))+((123f/255f)*(dust*4)));
			}
			if(fire>0)
			{
				f4 = (float) (1*(1f-(dust*4))+((202f/255f)*(dust*4)));//(float) ((sky.xCoord + (0 - sky.xCoord) * 1) * dust);
				f5 = (float) (1*(1f-(dust*4))+((167f/255f)*(dust*4)));
				f6 = (float) (1*(1f-(dust*4))+((123f/255f)*(dust*4)));
			}
		}
		return Vec3.createVectorHelper((double) f4, (double) f5, (double) f6);
		//return Vec3.createVectorHelper((double) f4 * (fire + (1 - dust)), (double) f5 * (fire + (1 - dust)), (double) f6 * (fire + (1 - dust)));
	}


	@SideOnly(Side.CLIENT)
	public Vec3 drawClouds(float partialTicks) {
		Vec3 clouds = super.drawClouds(partialTicks);
		float dust = MainRegistry.proxy.getImpactDust(worldObj);;
		float f3 = (float) clouds.xCoord;
		float f4 = (float) clouds.yCoord;
		float f5 = (float) clouds.zCoord;
		return Vec3.createVectorHelper((double) f3 * (1 - dust), (double) f4 * (1 - dust), (double) f5 * (1 - dust));
	}
	
	@Override
    public boolean canBlockFreeze(int x, int y, int z, boolean byWater)
    {
		TomSaveData data = TomSaveData.getLastCachedOrNull();
        BiomeGenBase biomegenbase = super.getBiomeGenForCoords(x, z);
        float f = biomegenbase.getFloatTemperature(x, y, z);
       // WorldChunkManagerNTM.temp = f;
        if (f > 0.15F+(data.winter*0.75f))
        {
            return false;
        }
        else
        {
            if (y >= 0 && y < 256 && worldObj.getSavedLightValue(EnumSkyBlock.Block, x, y, z) < 10)
            {
                Block block = worldObj.getBlock(x, y, z);

                if ((block == Blocks.water || block == Blocks.flowing_water) && worldObj.getBlockMetadata(x, y, z) == 0)
                {
                    if (!byWater)
                    {
                        return true;
                    }

                    boolean flag1 = true;

                    if (flag1 && worldObj.getBlock(x - 1, y, z).getMaterial() != Material.water)
                    {
                        flag1 = false;
                    }

                    if (flag1 && worldObj.getBlock(x + 1, y, z).getMaterial() != Material.water)
                    {
                        flag1 = false;
                    }

                    if (flag1 && worldObj.getBlock(x, y, z - 1).getMaterial() != Material.water)
                    {
                        flag1 = false;
                    }

                    if (flag1 && worldObj.getBlock(x, y, z + 1).getMaterial() != Material.water)
                    {
                        flag1 = false;
                    }

                    if (!flag1)
                    {
                        return true;
                    }
                }
            }

            return false;
        }
    }
	@Override
    public boolean canSnowAt(int x, int y, int z, boolean checkLight)
    {
		TomSaveData data = TomSaveData.getLastCachedOrNull();
    	BiomeGenBase biomegenbase = super.getBiomeGenForCoords(x, z);
        float f = biomegenbase.getFloatTemperature(x, y, z);

        if (f > 0.15F+(data.winter*0.75f))
        {
            return false;
        }
        else if (!checkLight)
        {
            return true;
        }
        else
        {
            if (y >= 0 && y < 256 && worldObj.getSavedLightValue(EnumSkyBlock.Block, x, y, z) < 10)
            {
                Block block = worldObj.getBlock(x, y, z);

                if (block.getMaterial() == Material.air && Blocks.snow_layer.canPlaceBlockAt(worldObj, x, y, z))
                {
                    return true;
                }
            }

            return false;
        }
    }
}