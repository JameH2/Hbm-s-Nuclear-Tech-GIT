package com.hbm.saveddata.satellites;

import java.util.Random;

import org.lwjgl.opengl.GL11;

import com.hbm.dim.CelestialBody;
import com.hbm.dim.projectile.ProjectileManager;
import com.hbm.dim.trait.CBT_War;
import com.hbm.lib.RefStrings;
import com.hbm.main.MainRegistry;
import com.hbm.main.ResourceManager;
import com.hbm.render.util.BeamPronter;
import com.hbm.render.util.BeamPronter.EnumBeamType;
import com.hbm.render.util.BeamPronter.EnumWaveType;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

public class SatelliteRailgun extends SatelliteWar {
	 
	private static final ResourceLocation texture = new ResourceLocation(RefStrings.MODID + ":textures/particle/shockwave.png");
	private static final ResourceLocation flash = new ResourceLocation("hbm:textures/misc/space/flare.png");
 
	private CelestialBody target;
	private boolean hasTarget = false;
 
	private String orbitingBodyName;
 
	private Random rand = new Random();
 
	public SatelliteRailgun() {
		super();
	}


	@Override
	public void setTarget(CelestialBody body) {
		this.target = body;
		this.hasTarget = body != null && body.canLand;
	}

	
	public void setOrbitingBody(CelestialBody body) {
		this.orbitingBodyName = body != null ? body.name : null;
	}
 
 
	@Override
	public void onCoordAction(World world, EntityPlayer player, int x, int y, int z) {
		if(!world.isRemote && hasTarget && target != null) {
			fireAtTarget(world);
		}
 
		if(world.isRemote) {
			triggerFireEffect();
			MainRegistry.proxy.me().playSound("hbm:misc.fireflash", 10F, 1F);
		}
	}
 

	private void fireAtTarget(World world) {
		if(target == null || !hasTarget) return;
 
		String sourceName = orbitingBodyName != null ? orbitingBodyName : CelestialBody.getBody(world).name;
 
		double impactX = (rand.nextDouble() - 0.5) * 1000;
		double impactY = (rand.nextDouble() - 0.5) * 1000;
		double impactZ = (rand.nextDouble() - 0.5) * 1000;
 
		ProjectileManager.launchSmall(
			sourceName,
			target.name,
			600,    
			50,    
			impactX,
			impactY,
			impactZ
		);
 
		triggerFireEffect();
	}
 
 
	@Override
	public void onOrbit(World world, double x, double y, double z) {
		// Set the orbiting body when the satellite reaches orbit
		this.orbitingBodyName = CelestialBody.getBody(world).name;
	}
 
	@Override
	public void onUpdate(World world) {
		super.onUpdate(world);
	}

	
 
	@Override
	public void writeToNBT(NBTTagCompound nbt) {
		super.writeToNBT(nbt);
		if(target != null) nbt.setString("target", target.name);
		if(orbitingBodyName != null) nbt.setString("orbiting", orbitingBodyName);
		nbt.setBoolean("hasTarget", hasTarget);
	}
 
	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);
		if(nbt.hasKey("target")) {
			target = CelestialBody.getBody(nbt.getString("target"));
			hasTarget = nbt.getBoolean("hasTarget");
		}
		if(nbt.hasKey("orbiting")) {
			orbitingBodyName = nbt.getString("orbiting");
		}
	}
 
	@Override
	public void serialize(ByteBuf buf) {
		super.serialize(buf);
		boolean hasTargetName = target != null;
		buf.writeBoolean(hasTargetName);
		if(hasTargetName) {
			byte[] nameBytes = target.name.getBytes();
			buf.writeShort(nameBytes.length);
			buf.writeBytes(nameBytes);
		}
	}
 
	@Override
	public void deserialize(ByteBuf buf) {
		super.deserialize(buf);
		if(buf.readBoolean()) {
			int len = buf.readShort();
			byte[] nameBytes = new byte[len];
			buf.readBytes(nameBytes);
			target = CelestialBody.getBody(new String(nameBytes));
			hasTarget = target != null;
		} else {
			target = null;
			hasTarget = false;
		}
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
			float fuck = this.getEffectTimer();
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
				BeamPronter.prontBeam(Vec3.createVectorHelper(0, fuck * 2, 0), EnumWaveType.SPIRAL, EnumBeamType.SOLID, 0x202060, 0x202060, 0, 1, 0F, 6, (float) 1.6 * 1.2F * alped, alped * 0.2F);
				BeamPronter.prontBeam(Vec3.createVectorHelper(0, fuck * 2, 0), EnumWaveType.SPIRAL, EnumBeamType.SOLID, 0x202060, 0x202060, 0, 1, 0F, 6, (float) 0.7 * 0.6F, alped * 0.6F);
				BeamPronter.prontBeam(Vec3.createVectorHelper(0, fuck * 2, 0), EnumWaveType.RANDOM, EnumBeamType.SOLID, 0x202060, 0x202060, (int) (world.getTotalWorldTime() / 5) % 1000, 35, 0.2F, 6, (float) 0.2 * 0.1F, alped);
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


	@Override
	public void fire() {
		// TODO Auto-generated method stub
		
	}

}
