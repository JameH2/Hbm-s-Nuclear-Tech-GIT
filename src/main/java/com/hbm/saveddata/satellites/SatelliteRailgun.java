package com.hbm.saveddata.satellites;

import org.lwjgl.opengl.GL11;

import com.hbm.dim.CelestialBody;
import com.hbm.dim.trait.CBT_War;
import com.hbm.dim.trait.CBT_War.Projectile;
import com.hbm.dim.trait.CBT_War.ProjectileType;
import com.hbm.lib.RefStrings;
import com.hbm.main.ResourceManager;
import com.hbm.render.util.BeamPronter;
import com.hbm.render.util.BeamPronter.EnumBeamType;
import com.hbm.render.util.BeamPronter.EnumWaveType;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

public class SatelliteRailgun extends SatelliteWar {

	//time to clean up this shit and make it PROPER.
	private static final ResourceLocation texture = new ResourceLocation(RefStrings.MODID + ":textures/particle/shockwave.png");
	private static final ResourceLocation flash = new ResourceLocation("hbm:textures/misc/space/flare.png");

	public SatelliteRailgun() {

	}

	private boolean canFire = false;
	private boolean hasTarget = false;

	public long lastOp;
	public float interp;
	public int cooldown;
	private CelestialBody target;

	public void writeToNBT(NBTTagCompound nbt) {
		nbt.setLong("lastOp", lastOp);
	}

	public void readFromNBT(NBTTagCompound nbt) {
		lastOp = nbt.getLong("lastOp");
	}

	@Override
	public void onClick(World world, int x, int z) {
		fireAtTarget(target);

		if(!hasTarget) {
			canFire = false;
		}
		else {
			canFire = true;
		}
	}

	@Override
	public void fire() {
		if (canFire) {
			interp += 0.5f;
			interp = Math.min(100.0f, interp + 0.3f * (100.0f - interp) * 0.15f);

			if (interp >= 100) {
				interp = 0;
				canFire = false;
			}

		}

	}

	@Override
	public void setTarget(CelestialBody body) {
		target = CelestialBody.getBody(body.dimensionId);
		if(target != null) {
			hasTarget = true;
		}

	}

	@Override
	public void fireAtTarget(CelestialBody body) {
		if(hasTarget) {
			if(!target.hasTrait(CBT_War.class)) {
				target.modifyTraits(new CBT_War(100, 0));
			} else {
				CBT_War war = target.getTrait(CBT_War.class);
				if(war != null) {
					float rand = Minecraft.getMinecraft().theWorld.rand.nextFloat();
					//TODO: be able to choose projectile types
					Projectile projectile = new Projectile(100, 20, 50, 28 * rand * 5, 55, 20, ProjectileType.SMALL, body.dimensionId);
					projectile.GUIangle = (int) (rand * 360);
					war.launchProjectile(projectile);
					System.out.println(war.health);

				}
			}
		}
	}

	public void playsound() {
		Minecraft.getMinecraft().thePlayer.playSound("hbm:misc.fireflash", 10F, 1F);
	}

	@Override
	protected float[] getColor() {
		return new float[] { 0.0F, 0.0F, 0.0F, 0.0F };
	}

	public float getInterp() {
		return interp;
	}

	public int magSize() {
		return 0;
	}
	@Override
	public void serialize(ByteBuf buf) {
		buf.writeFloat(interp);

	}

	@Override
	public void deserialize(ByteBuf buf) {
		this.interp = buf.readFloat();
	}


	@Override
	public void render(float partialTicks, WorldClient world, Minecraft mc, float solarAngle, long id) {
		GL11.glPushMatrix();
		{

			GL11.glScaled(5, 5, 5);

			double rounded = Math.round(id / 1000.0);
			double x = ((id % 10) - 5) * 2;
			double y = (((id / 10) % 10) - 5) * 2;

			double xPos = Math.min(Math.max(-rounded + 30 + x, -50), 50);
			double yPos = Math.min(Math.max(-rounded - 20 + y, -50), 50);

			GL11.glTranslated(xPos, yPos, 20);
			float fuck = this.getInterp();
			float alped = 1.0F - Math.min(1.0F, fuck / 100);

			GL11.glPushMatrix();
			{

				GL11.glColor4d(1, 1, 1, alped);

				GL11.glTranslated(1, 5.5, 0);
				GL11.glScaled(fuck * 0.2, fuck * 0.2, fuck * 0.2);
				mc.renderEngine.bindTexture(flash);
				ResourceManager.plane.renderAll();

				mc.renderEngine.bindTexture(texture);
				ResourceManager.plane.renderAll();

			}
			GL11.glPopMatrix();

			GL11.glPushMatrix();
			{

				GL11.glTranslated(1, 5.5, 0);
				BeamPronter.prontBeam(Vec3.createVectorHelper(0, fuck * 2 , 0), EnumWaveType.SPIRAL, EnumBeamType.SOLID, 0x202060, 0x202060, 0, 1, 0F, 6, (float)1.6 * 1.2F * alped, alped * 0.2F );
				BeamPronter.prontBeam(Vec3.createVectorHelper(0, fuck * 2, 0), EnumWaveType.SPIRAL, EnumBeamType.SOLID, 0x202060, 0x202060, 0, 1, 0F, 6, (float)0.7 * 0.6F, alped * 0.6F );
				BeamPronter.prontBeam(Vec3.createVectorHelper(0, fuck * 2, 0), EnumWaveType.RANDOM, EnumBeamType.SOLID, 0x202060, 0x202060, (int)(world.getTotalWorldTime() / 5) % 1000, 35, 0.2F, 6, (float)0.2 * 0.1F, alped );
				GL11.glClear(GL11.GL_DEPTH_BUFFER_BIT);
				GL11.glDisable(GL11.GL_LIGHTING);
				GL11.glEnable(GL11.GL_CULL_FACE);

			}
			GL11.glPopMatrix();


			GL11.glEnable(GL11.GL_DEPTH_TEST);
			GL11.glDisable(GL11.GL_BLEND);
			GL11.glRotated(-90, 0, 0, 1);

			GL11.glDepthRange(0.0, 1.0);

			//GL11.glDepthMask(false);

			mc.renderEngine.bindTexture(ResourceManager.sat_rail_tex);
			ResourceManager.sat_rail.renderAll();

			GL11.glClear(GL11.GL_DEPTH_BUFFER_BIT);
			GL11.glEnable(GL11.GL_BLEND);

		}
		GL11.glPopMatrix();
	}

}
