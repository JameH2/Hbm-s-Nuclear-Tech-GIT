package com.hbm.saveddata.satellites;

import org.lwjgl.opengl.GL11;

import com.hbm.lib.RefStrings;
import com.hbm.tileentity.network.RTTYSystem;
import com.hbm.util.AstronomyUtil;
import com.hbm.util.BufferUtil;

import api.hbm.redstoneoverradio.IRORInteractive;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

public abstract class SatelliteBase {

	public static final float DEFAULT_INCLINATION = 0F;
	public static final float MIN_INCLINATION = -180.0F;
	public static final float MAX_INCLINATION = 180.0F;
	public static final float DEFAULT_ALTITUDE_KM = AstronomyUtil.DEFAULT_ALTITUDE_KM;
	public static final float MIN_ALTITUDE_KM = 80.0F;
	public static final float MAX_ALTITUDE_KM = 125.0F;
	public static final float MIN_BLINK_PERIOD = 0.3F;
	public static final float MAX_BLINK_PERIOD = 1.0F;
	public static final boolean DEFAULT_IS_BLINKING = false;
	public static final float DEFAULT_BLINK_PERIOD = MIN_BLINK_PERIOD;
	public static final String DEFAULT_OWNER = "None";
	public static final float DEFAULT_PHASE_OFFSET = 0.0F;
	
	public static final String CHAN_SATLINK = "SAT_LINK";

	public static final String CMD_SETTARGET = "settarget";
	public static final String CMD_GETTARGET = "gettarget";
	public static final String CMD_GETTARGETX = "gettargetx";
	public static final String CMD_GETTARGETZ = "gettargetz";

	private static final ResourceLocation satelliteTexture = new ResourceLocation(RefStrings.MODID, "textures/misc/space/satellite.png");
	
	public int targetX;
	public int targetZ;
	
	public String tx = "";

	public float inclination = DEFAULT_INCLINATION;
	public float altitude = DEFAULT_ALTITUDE_KM;
	public float phaseOffset = DEFAULT_PHASE_OFFSET;

	public boolean isBlinking = DEFAULT_IS_BLINKING;
	public float blinkPeriod = DEFAULT_BLINK_PERIOD;

	public String owner = DEFAULT_OWNER;

	public float colorR;
	public float colorG;
	public float colorB;

	public int health;
	
	public int getID() {
		return XSatelliteRegistry.idToClass.inverse().get(this.getClass());
	}
	
	public abstract String getType();
	
	public void writeToNBT(NBTTagCompound nbt) {
		nbt.setInteger("targetX", targetX);
		nbt.setInteger("targetZ", targetZ);
		nbt.setString("tx", tx);

		nbt.setFloat("satInclination", inclination);
		nbt.setFloat("satAltitude", altitude);
		nbt.setFloat("satPhaseOffset", normalizePhaseOffset(phaseOffset));
		nbt.setBoolean("satIsBlinking", isBlinking);
		nbt.setFloat("satBlink", blinkPeriod);
		nbt.setString("satOwner", owner);
		nbt.setFloat("satColorR", colorR);
		nbt.setFloat("satColorG", colorG);
		nbt.setFloat("satColorB", colorB);
	}
	
	public void readFromNBT(NBTTagCompound nbt) {
		this.targetX = nbt.getInteger("targetX");
		this.targetZ = nbt.getInteger("targetZ");
		this.tx = nbt.getString("tx");
		
		inclination = nbt.getFloat("satInclination");
		altitude = nbt.hasKey("satAltitude") ? nbt.getFloat("satAltitude") : DEFAULT_ALTITUDE_KM;
		phaseOffset = nbt.hasKey("satPhaseOffset") ? normalizePhaseOffset(nbt.getFloat("satPhaseOffset")) : DEFAULT_PHASE_OFFSET;
		isBlinking = nbt.hasKey("satIsBlinking") ? nbt.getBoolean("satIsBlinking") : DEFAULT_IS_BLINKING;
		blinkPeriod = nbt.hasKey("satBlink") ? clampBlinkPeriod(nbt.getFloat("satBlink")) : DEFAULT_BLINK_PERIOD;
		owner = nbt.hasKey("satOwner") ? nbt.getString("satOwner") : DEFAULT_OWNER;
		float[] registeredColor = XSatelliteRegistry.getRegisteredColor(getClass());
		colorR = nbt.hasKey("satColorR") ? nbt.getFloat("satColorR") : registeredColor[0];
		colorG = nbt.hasKey("satColorG") ? nbt.getFloat("satColorG") : registeredColor[1];
		colorB = nbt.hasKey("satColorB") ? nbt.getFloat("satColorB") : registeredColor[2];
	}

	public void serialize(ByteBuf buf) {
		buf.writeFloat(inclination);
		buf.writeFloat(altitude);
		buf.writeFloat(normalizePhaseOffset(phaseOffset));
		BufferUtil.writeString(buf, owner);
		buf.writeFloat(colorR);
		buf.writeFloat(colorG);
		buf.writeFloat(colorB);
		buf.writeBoolean(isBlinking);
		buf.writeFloat(blinkPeriod);
	}

	public void deserialize(ByteBuf buf) {
		inclination = buf.readFloat();
		altitude = buf.readFloat();
		phaseOffset = normalizePhaseOffset(buf.readFloat());
		owner = BufferUtil.readString(buf);
		colorR = buf.readFloat();
		colorG = buf.readFloat();
		colorB = buf.readFloat();
		isBlinking = buf.readBoolean();
		blinkPeriod = clampBlinkPeriod(buf.readFloat());
	}
	
	/** When a satellite is created, i.e. this frequency is occupied for the first time */
	public void onOrbit(World world, double x, double y, double z) {
		setTarget((int) Math.floor(x), (int) Math.floor(z));
		
		RTTYSystem.broadcast(world, CHAN_SATLINK, "Established connection to " + getType() + " at " + targetX + " / " + targetZ);
	}
	
	/** For subsequent items sent under the same frequency as an existing satellite */
	public void onPartDelivered(World world, ItemStack part) { }
	
	public void onCommand(World world, String... cmd) {
		onCommandTarget(world, cmd);
		onCommandImpl(world, cmd);
	}
	
	public void onCommandTarget(World world, String... cmd) {
		if(cmd.length <= 0) return;
		
		if(cmd[0].equals(CMD_SETTARGET)) {
			if(cmd.length == 3) {
				targetX = IRORInteractive.parseInt(cmd[1]);
				targetZ = IRORInteractive.parseInt(cmd[2]);
			}
			if(cmd.length == 4) {
				targetX = IRORInteractive.parseInt(cmd[1]);
				targetZ = IRORInteractive.parseInt(cmd[3]);
			}
			return;
		}
		
		if(cmd[0].equals(CMD_GETTARGET)) {
			this.tx = targetX + ";" + targetZ;
			return;
		}
		
		if(cmd[0].equals(CMD_GETTARGETX)) {
			this.tx = "" + targetX;
			return;
		}
		
		if(cmd[0].equals(CMD_GETTARGETZ)) {
			this.tx = "" + targetZ;
			return;
		}
	}
	
	public void setTarget(int x, int z) {
		this.targetX = x;
		this.targetZ = z;
	}
	
	public void onCommandImpl(World world, String... cmd) { }
	
	public void onCoordAction(World world, EntityPlayer player, int x, int y, int z) { }

	
	public void render(float partialTicks, WorldClient world, Minecraft mc, float solarAngle, long id) {
		renderDefault(partialTicks, world, mc, solarAngle, id, colorR, colorG, colorB, inclination, altitude, phaseOffset, isBlinking, blinkPeriod);
	}

	public static void renderDefault(float partialTicks, WorldClient world, Minecraft mc, float solarAngle, long seed, float r, float g, float b, float inclination, float altitude, boolean isBlinking, float blinkPeriod) {
		renderDefault(partialTicks, world, mc, solarAngle, seed, r, g, b, inclination, altitude, DEFAULT_PHASE_OFFSET, isBlinking, blinkPeriod);
	}

	public static void renderDefault(float partialTicks, WorldClient world, Minecraft mc, float solarAngle, long seed, float r, float g, float b, float inclination, float altitude, float phaseOffset, boolean isBlinking, float blinkPeriod) {
		Tessellator tessellator = Tessellator.instance;

		double ticks = (double) System.currentTimeMillis() / 50.0D;
		float orbitAngle = applyPhaseOffsetToOrbitAngle(phaseOffset, altitude, (ticks / 600.0D) * -360.0D, 360.0F);
		float renderAltitude = Math.max(1.0F, altitude);

		GL11.glPushMatrix();
		{

			GL11.glRotatef(solarAngle * -360.0F, 1.0F, 0.0F, 0.0F);
			GL11.glRotatef(inclination, 0.0F, 0.0F, 1.0F);
			GL11.glRotated(orbitAngle, 1.0F, 0.0F, 0.0F);

			GL11.glColor4f(r, g, b, getBlinkAlpha(isBlinking, blinkPeriod));

			mc.renderEngine.bindTexture(satelliteTexture);

			float size = 0.5F;

			tessellator.startDrawingQuads();
			tessellator.addVertexWithUV(-size, renderAltitude, -size, 0.0D, 0.0D);
			tessellator.addVertexWithUV(size, renderAltitude, -size, 0.0D, 1.0D);
			tessellator.addVertexWithUV(size, renderAltitude, size, 1.0D, 1.0D);
			tessellator.addVertexWithUV(-size, renderAltitude, size, 1.0D, 0.0D);
			tessellator.draw();

		}
		GL11.glPopMatrix();
	}

	public static void renderOrbitLine(float solarAngle, float r, float g, float b, float inclination, float altitude, boolean isBlinking, float blinkPeriod) {
		Tessellator tessellator = Tessellator.instance;
		float renderAltitude = Math.max(1.0F, altitude);
		float alpha = 0.35F * getBlinkAlpha(isBlinking, blinkPeriod);

		GL11.glPushAttrib(GL11.GL_ENABLE_BIT | GL11.GL_COLOR_BUFFER_BIT | GL11.GL_LINE_BIT);
		GL11.glDisable(GL11.GL_TEXTURE_2D);
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glLineWidth(1.0F);
		GL11.glColor4f(r, g, b, alpha);

		GL11.glPushMatrix();
		{
			GL11.glRotatef(solarAngle * -360.0F, 1.0F, 0.0F, 0.0F);
			GL11.glRotatef(inclination, 0.0F, 0.0F, 1.0F);

			tessellator.startDrawing(GL11.GL_LINE_LOOP);
			for (int i = 0; i < 72; i++) {
				double angle = Math.PI * 2.0D * i / 72.0D;
				tessellator.addVertex(0.0D, renderAltitude * Math.cos(angle), renderAltitude * Math.sin(angle));
			}
			tessellator.draw();
		}
		GL11.glPopMatrix();

		GL11.glPopAttrib();
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
	}

	private static float getBlinkAlpha(boolean isBlinking, float blinkPeriod) {
		if(!isBlinking) {
			return 1.0F;
		}

		long cycleMillis = (long) (clampBlinkPeriod(blinkPeriod) * 1000.0F);
		if(cycleMillis <= 0L) {
			return 1.0F;
		}

		return 1.0F - (float) (System.currentTimeMillis() % cycleMillis) / cycleMillis;
	}




	

	public static void copyItemData(ItemStack from, ItemStack to) {
		if(to == null) return;
		setInclination(to, getInclination(from));
		setAltitude(to, getAltitude(from));
		setPhaseOffset(to, getPhaseOffset(from));
		setOwner(to, getOwner(from));
		setColor(to, getColorR(from), getColorG(from), getColorB(from));
		setBlinking(to, isBlinking(from));
		setBlinkPeriod(to, getBlinkPeriod(from));
	}

	public static void ensureItemData(ItemStack stack) {
		getItemData(stack);
	}

	public static NBTTagCompound getItemData(ItemStack stack) {
		NBTTagCompound nbt = stack.stackTagCompound;
		if(nbt == null) {
			nbt = new NBTTagCompound();
			float[] color = XSatelliteRegistry.getRegisteredColor(stack.getItem());
			nbt.setFloat("satInclination", DEFAULT_INCLINATION);
			nbt.setFloat("satAltitude", DEFAULT_ALTITUDE_KM);
			nbt.setFloat("satPhaseOffset", DEFAULT_PHASE_OFFSET);
			nbt.setBoolean("satIsBlinking", DEFAULT_IS_BLINKING);
			nbt.setFloat("satBlink", DEFAULT_BLINK_PERIOD);
			nbt.setString("satOwner", DEFAULT_OWNER);
			nbt.setFloat("satColorR", color[0]);
			nbt.setFloat("satColorG", color[1]);
			nbt.setFloat("satColorB", color[2]);
			stack.stackTagCompound = nbt;
		} else {
			nbt.setFloat("satInclination", nbt.hasKey("satInclination") ? nbt.getFloat("satInclination") : DEFAULT_INCLINATION);
			nbt.setFloat("satPhaseOffset", nbt.hasKey("satPhaseOffset") ? normalizePhaseOffset(nbt.getFloat("satPhaseOffset")) : DEFAULT_PHASE_OFFSET);
			if(!nbt.hasKey("satIsBlinking")) {
				nbt.setBoolean("satIsBlinking", DEFAULT_IS_BLINKING);
			}
			nbt.setFloat("satBlink", nbt.hasKey("satBlink") ? clampBlinkPeriod(nbt.getFloat("satBlink")) : DEFAULT_BLINK_PERIOD);
		}

		return nbt;
	}

	public static float getInclination(ItemStack stack) {
		return getItemData(stack).getFloat("satInclination");
	}

	public static float getAltitude(ItemStack stack) {
		return getItemData(stack).getFloat("satAltitude");
	}

	public static float getPhaseOffset(ItemStack stack) {
		return getItemData(stack).getFloat("satPhaseOffset");
	}

	public static String getOwner(ItemStack stack) {
		return getItemData(stack).getString("satOwner");
	}

	public static float getColorR(ItemStack stack) {
		return getItemData(stack).getFloat("satColorR");
	}

	public static float getColorG(ItemStack stack) {
		return getItemData(stack).getFloat("satColorG");
	}

	public static float getColorB(ItemStack stack) {
		return getItemData(stack).getFloat("satColorB");
	}

	public static float getBlinkPeriod(ItemStack stack) {
		return getItemData(stack).getFloat("satBlink");
	}

	public static boolean isBlinking(ItemStack stack) {
		return getItemData(stack).getBoolean("satIsBlinking");
	}

	public static void setInclination(ItemStack stack, float inclination) {
		getItemData(stack).setFloat("satInclination", inclination);
	}

	public static void setAltitude(ItemStack stack, float altitude) {
		getItemData(stack).setFloat("satAltitude", altitude);
	}

	public static void setPhaseOffset(ItemStack stack, float phaseOffset) {
		getItemData(stack).setFloat("satPhaseOffset", normalizePhaseOffset(phaseOffset));
	}

	public static void setOwner(ItemStack stack, String owner) {
		getItemData(stack).setString("satOwner", owner);
	}

	public static void setColor(ItemStack stack, float r, float g, float b) {
		NBTTagCompound nbt = getItemData(stack);
		nbt.setFloat("satColorR", r);
		nbt.setFloat("satColorG", g);
		nbt.setFloat("satColorB", b);
	}

	public static void setBlinking(ItemStack stack, boolean isBlinking) {
		getItemData(stack).setBoolean("satIsBlinking", isBlinking);
	}

	public static void setBlinkPeriod(ItemStack stack, float blinkPeriod) {
		getItemData(stack).setFloat("satBlink", clampBlinkPeriod(blinkPeriod));
	}

	public static float clampBlinkPeriod(float blinkPeriod) {
		return Math.max(MIN_BLINK_PERIOD, Math.min(MAX_BLINK_PERIOD, blinkPeriod));
	}

	public static float applyPhaseOffsetToOrbitAngle(float phaseOffset, float altitude, double baseAngle, float fullRotation) {
		double orbitSpeed = getAltitudeOrbitSpeed(altitude);
		double phase = normalizePhaseOffset(phaseOffset) / 360.0D * fullRotation;
		double angle = baseAngle * orbitSpeed + phase;
		double wrapped = angle % fullRotation;
		if(wrapped < 0.0D) wrapped += fullRotation;
		return (float) wrapped;
	}

	public static float getOrbitSpeedKmPerSecond(float altitude) {
		double radiusKm = Math.max(1.0D, altitude);
		double turnsPerSecond = getAltitudeOrbitSpeed(altitude) / 30.0D;
		return (float) (2.0D * Math.PI * radiusKm * turnsPerSecond);
	}

	private static double getAltitudeOrbitSpeed(float altitude) {
		return Math.pow((double) DEFAULT_ALTITUDE_KM / Math.max(1.0D, altitude), 1.5D);
	}

	public static float normalizePhaseOffset(float phaseOffset) {
		float wrapped = phaseOffset % 360.0F;
		if(wrapped < 0.0F) wrapped += 360.0F;
		return wrapped;
	}



}
