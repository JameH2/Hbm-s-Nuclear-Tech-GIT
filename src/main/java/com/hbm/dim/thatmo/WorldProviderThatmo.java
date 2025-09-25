package com.hbm.dim.thatmo;

import java.util.ArrayList;
import java.util.Random;

import com.hbm.blocks.ModBlocks;
import com.hbm.config.SpaceConfig;
import com.hbm.dim.CelestialBody;
import com.hbm.dim.WorldProviderCelestial;
import com.hbm.dim.trait.CelestialBodyTrait.CBT_BATTLEFIELD;
import com.hbm.entity.logic.EntityBomber;
import com.hbm.main.MainRegistry;
import com.hbm.util.fauxpointtwelve.BlockPos;
import com.hbm.world.WorldUtil;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.Vec3;
import net.minecraft.world.biome.WorldChunkManagerHell;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraftforge.client.IRenderHandler;

public class WorldProviderThatmo extends WorldProviderCelestial {

	@Override
	public void registerWorldChunkManager() {
		this.worldChunkMgr = new WorldChunkManagerHell(new BiomeGenThatmo(SpaceConfig.thatmoBiome), dimensionId);
	}

	@Override
	public String getDimensionName() {
		return "Thatmo";
	}

	public static int chargetime;
	public static float flashd;
	public static float altitude;
	public static float randPos;
	public static float scale;
	public static float shield;
	public static float cooldown;
	public static float nmass;
	public static float shielde;
	public static float csyw;
	public static ArrayList<Meteor> meteors = new ArrayList<>();
	public static ArrayList<Meteor> fragments = new ArrayList<>();
	public static ArrayList<Meteor> smoke = new ArrayList<>();

	@Override
	public void updateWeather() {
		super.updateWeather();
		CBT_BATTLEFIELD war = CelestialBody.getTrait(worldObj, CBT_BATTLEFIELD.class);

		// i am violating the genercizing philosphy mellow told me about, this shit sucks
		// if other planets have the battlefield trait, what could be done....?

		if(war != null) {
			if(!worldObj.isRemote) {
				for(Object p : worldObj.playerEntities) {
					long currentTime = System.currentTimeMillis();
					if(currentTime % 8000 < 50) {

						BlockPos playerPos = new BlockPos(((EntityPlayer) p).posX, ((EntityPlayer) p).posY, ((EntityPlayer) p).posZ);
						int radius = 40;

						Random rand = worldObj.rand;
						int offsetX = rand.nextInt(radius * 2 + 1) - radius;
						int offsetZ = rand.nextInt(radius * 2 + 1) - radius;

						BlockPos targetPos = playerPos.add(offsetX, 0, offsetZ);
						EntityBomber bomber = EntityBomber.statFacCarpetJet(worldObj, targetPos.getX(), targetPos.getY(), targetPos.getZ());
						bomber.posY = targetPos.getY() + 20;
						WorldUtil.loadAndSpawnEntityInWorld(bomber);
					}
				}
			}

			Random rand = new Random();

			if(worldObj.isRemote) {
				EntityPlayer player = MainRegistry.proxy.me();

				if(chargetime <= 0 || chargetime <= 1000) {
					chargetime += 1;
					flashd = 0;
				} else if(chargetime >= 100) {
					flashd += 0.3f;
					flashd = Math.min(100.0f, flashd + 0.3f * (100.0f - flashd) * 0.15f);

					if(flashd <= 5) {
						player.playSound("hbm:misc.fireflash", 10F, 1F);
					}

					if(flashd >= 100) {
						chargetime = 0;
					}
				}

				if(chargetime == 285) {
					player.playSound("hbm:misc.impact", 10F, 1F);
				}

				if(chargetime >= 300 && chargetime <= 430) {
					if(scale <= 0 || scale <= 20) {
						scale += 1.5;
					}

					if(scale >= 20) {
						scale = 20;
						shield += 0.4f;
						shield = Math.min(25.0f, shield + 0.5f * (25.0f - shield) * 0.15f);
						nmass = Math.min(180.0f, nmass + 0.2f * (180.0f - nmass) * 0.15f);
						shielde = Math.min(15.0f, shielde + 0.6f * (15.0f - shielde) * 0.10f);
						csyw += 0.2f;
						csyw = Math.min(100.0f, csyw + 0.2f * (100.0f - csyw) * 0.15f);
					}

					if(shield > 0) {
						if(cooldown <= 0 || cooldown <= 60) {
							cooldown += 0.5;
						}

						if(cooldown >= 60) {
							reset();
						}
					}
				} else {
					reset();
				}

				if(altitude <= 400) {
					altitude += 5;
				}

				if(altitude >= 400) {
					altitude = 0;
					randPos = worldObj.rand.nextFloat();
				}

				for(Meteor meteor : meteors) {
					meteor.update();
				}

				for(Meteor fragment : fragments) {
					fragment.update();
				}

				for(Meteor smoke : smoke) {
					smoke.update();
				}

				if(rand.nextInt(1) == 0) {
					Meteor meteor = new Meteor((player.posX + rand.nextInt(16000)) - 8000, 2017, (player.posZ + rand.nextInt(16000)) - 8000);
					meteors.add(meteor);
				}

				meteors.removeIf(x -> x.isDead);
				fragments.removeIf(xx -> xx.isDead);
				smoke.removeIf(xxx -> xxx.isDead);
			}
		}
	}

	private void reset() {
		cooldown = shield = scale = nmass = shielde = csyw = 0;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public Vec3 getSkyColor(Entity camera, float partialTicks) {
		Vec3 ohshit = super.getSkyColor(camera, partialTicks);
		float xalpha = (csyw <= 0) ? 0.0F : 0.5F - Math.min(0.5F, csyw / 100);
		float alpha = (flashd <= 0) ? 0.0F : 1.0F - Math.min(1.0F, flashd / 100);

		return Vec3.createVectorHelper(ohshit.xCoord + alpha + xalpha, ohshit.yCoord + alpha + xalpha, ohshit.zCoord + alpha + xalpha);

	}

	@Override
	@SideOnly(Side.CLIENT)
	public Vec3 getFogColor(float x, float y) {
		Vec3 ohshit = super.getFogColor(x, y);
		float xalpha = (csyw <= 0) ? 0.0F : 0.5F - Math.min(0.5F, csyw / 100);
		float alpha = (flashd <= 0) ? 0.0F : 1.0F - Math.min(1.0F, flashd / 100);

		return Vec3.createVectorHelper(ohshit.xCoord + alpha + xalpha, ohshit.yCoord + alpha + xalpha, ohshit.zCoord + alpha + xalpha);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public IRenderHandler getSkyRenderer() {
		return new SkyProviderThatmo();
	}

	@Override
	public IChunkProvider createChunkGenerator() {
		return new ChunkProviderThatmo(this.worldObj, this.getSeed(), false);
	}

	@Override
	public Block getStone() {
		return ModBlocks.sellafield_slaked;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public float getSunBrightness(float par1) {
		float imsuper = super.getSunBrightness(par1);
		float xalpha = (csyw <= 0) ? 0.0F : 0.5F - Math.min(0.5F, csyw / 100);
		float alpha = (flashd <= 0) ? 0.0F : 1.0F - Math.min(1.0F, flashd / 100);
		return imsuper * 0.1F + alpha + xalpha;
	}

	public class Meteor {

		public double posX;
		public double posY;
		public double posZ;
		public double prevPosX;
		public double prevPosY;
		public double prevPosZ;
		public double motionX;
		public double motionY;
		public double motionZ;
		public boolean isDead = false;
		public long age;
		public MeteorType type;

		public Meteor(double posX, double posY, double posZ) {
			this(posX, posY, posZ, MeteorType.STANDARD, -31.2, -20.8, 20);
		}

		public Meteor(double posX, double posY, double posZ, MeteorType type, double motionX, double motionY, double motionZ) {
			this.posX = posX;
			this.posY = posY;
			this.posZ = posZ;
			this.type = type;
			this.motionX = motionX;
			this.motionY = motionY;
			this.motionZ = motionZ;
		}

		private void update() {
			Random rand = new Random();
			if(this.type != MeteorType.SMOKE && this.type != MeteorType.FRAGMENT) {
				Meteor meteor = new Meteor((this.posX + rand.nextInt(16)) - 8, (this.posY + rand.nextInt(16)), (this.posZ + rand.nextInt(16)) - 8, MeteorType.SMOKE, 0, 0, 0);
				smoke.add(meteor);
			}

			if(this.posY <= 500 && this.type != MeteorType.SMOKE) {
				this.isDead = true;
			}

			if(this.type == MeteorType.SMOKE) {
				this.age++;
				if(this.age >= 60)
					this.isDead = true;
			}

			this.prevPosX = this.posX;
			this.prevPosY = this.posY;
			this.prevPosZ = this.posZ;
			this.posX += this.motionX;
			this.posY += this.motionY;
			this.posZ += this.motionZ;
		}
	}

	public static enum MeteorType {
		STANDARD,
		FRAGMENT,
		SMOKE
	}
}
