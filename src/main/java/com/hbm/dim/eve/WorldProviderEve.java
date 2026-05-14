package com.hbm.dim.eve;

import com.hbm.blocks.ModBlocks;
import com.hbm.dim.CelestialBody;
import com.hbm.dim.WorldChunkManagerCelestial;
import com.hbm.dim.WorldProviderCelestial;
import com.hbm.dim.WorldChunkManagerCelestial.BiomeGenLayers;
import com.hbm.dim.eve.genlayer.GenLayerEveBiomes;
import com.hbm.dim.eve.genlayer.GenLayerEveRiverMix;
import com.hbm.inventory.FluidStack;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.main.MainRegistry;
import com.hbm.render.util.AtmosphereRenderUtil;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.util.Vec3;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.layer.GenLayer;
import net.minecraft.world.gen.layer.GenLayerRiver;
import net.minecraft.world.gen.layer.GenLayerSmooth;
import net.minecraft.world.gen.layer.GenLayerVoronoiZoom;
import net.minecraft.world.gen.layer.GenLayerZoom;

public class WorldProviderEve extends WorldProviderCelestial {

	@Override
	public void registerWorldChunkManager() {
		this.worldChunkMgr = new WorldChunkManagerCelestial(createBiomeGenerators(worldObj.getSeed()));
	}

	@Override
	public String getDimensionName() {
		return "Eve";
	}

	@Override
	public IChunkProvider createChunkGenerator() {
		return new ChunkProviderEve(this.worldObj, this.getSeed());
	}

	@SideOnly(Side.CLIENT)
	private float lastFlashStrength;

	@Override
	public void updateWeather() {
		super.updateWeather();
		if(worldObj.isRemote) {
			float flashStrength = getFlashStrength(0.0F);
			if(lastFlashStrength <= 0.01F && flashStrength > 0.01F) {
				MainRegistry.proxy.me().playSound("hbm:misc.rumble", 10F, 1F);
			}
			lastFlashStrength = flashStrength;
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public Vec3 getSkyColor(Entity camera, float partialTicks) {
		Vec3 ohshit = super.getSkyColor(camera, partialTicks);
		float alpha = getFlashStrength(partialTicks);

		return Vec3.createVectorHelper(ohshit.xCoord + alpha, ohshit.yCoord + alpha, ohshit.zCoord + alpha);

	}

	@Override
	@SideOnly(Side.CLIENT)
	public float getSunBrightness(float par1) {
		float imsuper = super.getSunBrightness(par1);
		float alpha = getFlashStrength(par1);

		return imsuper + alpha * 0.7F;
	}

	@SideOnly(Side.CLIENT)
	private float getFlashStrength(float partialTicks) {
		return AtmosphereRenderUtil.getBodyEveFlashStrength(CelestialBody.getBody(worldObj), getAtmosphereTime(partialTicks));
	}

	private float getAtmosphereTime(float partialTicks) {
		return ((float) worldObj.getTotalWorldTime() + partialTicks) / 20.0F;
	}

	@Override
	public Block getStone() {
		return ModBlocks.eve_rock;
	}

	@Override
	public FluidStack getBedrockAcid() {
		return new FluidStack(Fluids.KMnO4, 500);
	}

	private static BiomeGenLayers createBiomeGenerators(long seed) {
		GenLayer genlayerBiomes = new GenLayerEveBiomes(seed); // Your custom biome layer

		genlayerBiomes = new GenLayerZoom(1000L, genlayerBiomes);
		genlayerBiomes = new GenLayerZoom(1001L, genlayerBiomes);
		genlayerBiomes = new GenLayerZoom(1002L, genlayerBiomes);
		genlayerBiomes = new GenLayerZoom(1003L, genlayerBiomes);
		genlayerBiomes = new GenLayerZoom(1004L, genlayerBiomes);
		genlayerBiomes = new GenLayerZoom(1005L, genlayerBiomes);

		GenLayer genlayerRiverZoom = new GenLayerZoom(1000L, genlayerBiomes);
		GenLayer genlayerRiver = new GenLayerRiver(1001L, genlayerRiverZoom); // Your custom river layer
		GenLayerSmooth genlayersmooth = new GenLayerSmooth(1000L, genlayerRiver);

		GenLayerSmooth genlayersmooth1 = new GenLayerSmooth(1000L, genlayerBiomes);
		GenLayerEveRiverMix genlayerrivermix = new GenLayerEveRiverMix(100L, genlayersmooth1, genlayersmooth);
		GenLayerVoronoiZoom genlayervoronoizoom = new GenLayerVoronoiZoom(10L, genlayerrivermix);

		return new BiomeGenLayers(genlayerrivermix, genlayervoronoizoom, seed);
	}

}
