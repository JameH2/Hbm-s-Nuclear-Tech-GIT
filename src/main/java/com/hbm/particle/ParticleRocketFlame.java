package com.hbm.particle;

import java.util.Random;

import com.hbm.main.ModEventHandlerClient;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.particle.EntityFX;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.world.World;

@SideOnly(Side.CLIENT)
public class ParticleRocketFlame extends EntityFX {

	protected int age;
	protected int maxAge;

	private float customRed;
	private float customGreen;
	private float customBlue;



	protected double pressure = 1;

	public ParticleRocketFlame(TextureManager p_i1213_1_, World p_i1218_1_, double p_i1218_2_, double p_i1218_4_, double p_i1218_6_) {
		super(p_i1218_1_, p_i1218_2_, p_i1218_4_, p_i1218_6_);
		particleIcon = ModEventHandlerClient.particleBase;
		maxAge = 300 + rand.nextInt(50);
		this.particleScale = 1F;
	}

	public ParticleRocketFlame setScale(float scale) {
		this.particleScale = scale;
		return this;
	}

	public ParticleRocketFlame setMaxAge(int maxAge) {
		this.maxAge = maxAge;
		return this;
	}

	public ParticleRocketFlame setCustomColor(float red, float green, float blue) {
		this.customRed = red;
		this.customGreen = green;
		this.customBlue = blue;
		return this;
	}

	public ParticleRocketFlame setAtmosphericPressure(double pressure) {
		this.pressure = pressure;

		if(pressure < 0.08) {
			double factor = (0.08 - pressure) * 100;
			this.motionX += (rand.nextDouble() - 0.5) * factor;
			this.motionY += (rand.nextDouble() - 0.5) * factor;
			this.motionZ += (rand.nextDouble() - 0.5) * factor;
		}

		if(pressure < 0.05) {
			this.maxAge /= 4;
		} else if (pressure < 0.2) {
			this.maxAge /= 2;
		}

		return this;
	}

	@Override
	public void onUpdate() {
		this.prevPosX = this.posX;
		this.prevPosY = this.posY;
		this.prevPosZ = this.posZ;

		this.age++;

		if(this.age == this.maxAge) {
			this.setDead();
		}

		this.motionX *= 0.91D;
		this.motionY *= 0.91D;
		this.motionZ *= 0.91D;

		this.moveEntity(this.motionX, this.motionY, this.motionZ);
	}

	@Override
	public int getFXLayer() {
		return 1;
	}

	@Override
	public void renderParticle(Tessellator p_70539_1_, float interp, float sX, float sY, float sZ, float dX, float dZ) {
		if(age == 0) return;

		Random urandom = new Random(this.getEntityId());

		for(int i = 0; i < 10; i++) {

			float add = urandom.nextFloat() * 0.3F;
			float dark = 1 - Math.min(((float) (age) / (float) (maxAge * 0.25F)), 1);

			this.particleRed = (customRed != 0) ? (customRed * dark + add) : (1 * dark + add);
			this.particleGreen = (customGreen != 0) ? (customGreen * dark + add) : (0.6F * dark + add);
			this.particleBlue = (customBlue != 0) ? (customBlue * dark + add) : (0 + add);

			this.particleAlpha = (float) Math.pow(1 - Math.min(((float) (age) / (float) (maxAge)), 1), 0.5);

			p_70539_1_.setColorRGBA_F(this.particleRed, this.particleGreen, this.particleBlue, this.particleAlpha * 0.75F);
			p_70539_1_.setNormal(0.0F, 1.0F, 0.0F);
			p_70539_1_.setBrightness(240);

			float spread = (float) Math.pow(((float) (age) / (float) maxAge) * 4F, 1.5) + 1F;
			spread *= this.particleScale;

			float scale = (urandom.nextFloat() * 0.5F + 0.1F + ((float) (age) / (float) maxAge) * 2F) * particleScale;
			float pX = (float) ((this.prevPosX + (this.posX - this.prevPosX) * (double) interp - interpPosX) + (urandom.nextGaussian() - 1D) * 0.2F * spread);
			float pY = (float) ((this.prevPosY + (this.posY - this.prevPosY) * (double) interp - interpPosY) + (urandom.nextGaussian() - 1D) * 0.5F * spread);
			float pZ = (float) ((this.prevPosZ + (this.posZ - this.prevPosZ) * (double) interp - interpPosZ) + (urandom.nextGaussian() - 1D) * 0.2F * spread);

			p_70539_1_.addVertexWithUV((double) (pX - sX * scale - dX * scale), (double) (pY - sY * scale), (double) (pZ - sZ * scale - dZ * scale), particleIcon.getMaxU(), particleIcon.getMaxV());
			p_70539_1_.addVertexWithUV((double) (pX - sX * scale + dX * scale), (double) (pY + sY * scale), (double) (pZ - sZ * scale + dZ * scale), particleIcon.getMaxU(), particleIcon.getMinV());
			p_70539_1_.addVertexWithUV((double) (pX + sX * scale + dX * scale), (double) (pY + sY * scale), (double) (pZ + sZ * scale + dZ * scale), particleIcon.getMinU(), particleIcon.getMinV());
			p_70539_1_.addVertexWithUV((double) (pX + sX * scale - dX * scale), (double) (pY - sY * scale), (double) (pZ + sZ * scale - dZ * scale), particleIcon.getMinU(), particleIcon.getMaxV());
		}
	}
}
