package com.hbm.saveddata.satellites;

import com.hbm.items.ModItems;
import com.hbm.lib.RefStrings;
import com.hbm.saveddata.SatelliteSavedData;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.lwjgl.opengl.GL11;

public abstract class Satellite {

	public static final List<Class<? extends Satellite>> satellites = new ArrayList<>();
	public static final HashMap<Item, Class<? extends Satellite>> itemToClass = new HashMap<>();

	private static final ResourceLocation satelliteTexture = new ResourceLocation(RefStrings.MODID, "textures/misc/space/satellite.png");

	public enum InterfaceActions {
		HAS_MAP,		//lets the interface display loaded chunks
		CAN_CLICK,		//enables onClick events
		SHOW_COORDS,	//enables coordinates as a mouse tooltip
		HAS_RADAR,		//lets the interface display loaded entities
		HAS_ORES		//like HAS_MAP but only shows ores
	}

	public enum CoordActions {
		HAS_Y		//enables the Y-coord field which is disabled by default
	}

	public enum Interfaces {
		NONE,		//does not interact with any sat interface (i.e. asteroid miners)
		SAT_PANEL,	//allows to interact with the sat interface panel (for graphical applications)
		SAT_COORD	//allows to interact with the sat coord remote (for teleportation or other coord related actions)
	}

	public List<InterfaceActions> ifaceAcs = new ArrayList<>();
	public List<CoordActions> coordAcs = new ArrayList<>();
	public Interfaces satIface = Interfaces.NONE;

	public static void register() {
		registerSatellite(SatelliteMapper.class, ModItems.sat_mapper);
		registerSatellite(SatelliteScanner.class, ModItems.sat_scanner);
		registerSatellite(SatelliteRadar.class, ModItems.sat_radar);
		registerSatellite(SatelliteLaser.class, ModItems.sat_laser);
		registerSatellite(SatelliteResonator.class, ModItems.sat_resonator);
		registerSatellite(SatelliteRelay.class, ModItems.sat_foeq);
		registerSatellite(SatelliteMiner.class, ModItems.sat_miner);
		registerSatellite(SatelliteLunarMiner.class, ModItems.sat_lunar_miner);
		registerSatellite(SatelliteDysonRelay.class, ModItems.sat_dyson_relay);
		registerSatellite(SatelliteHorizons.class, ModItems.sat_gerald);
		registerSatellite(SatelliteRailgun.class, ModItems.sat_war);
	}

	/**
	 * Register satellite.
	 * @param sat - Satellite class
	 * @param item - Satellite item (which will be placed in a rocket)
	 */
	public static void registerSatellite(Class<? extends Satellite> sat, Item item) {
		if(!itemToClass.containsKey(item) && !itemToClass.containsValue(sat)) {
			satellites.add(sat);
			itemToClass.put(item, sat);
		}
	}

	public static void orbit(World world, int id, int freq, double x, double y, double z) {
		if(world.isRemote) {
			return;
		}

		Satellite sat = create(id);

		if(sat != null) {
			SatelliteSavedData data = SatelliteSavedData.getData(world, (int)x, (int)z);
			data.sats.put(freq, sat);
			sat.onOrbit(world, x, y, z);
			data.markDirty();
		}
	}

	public static Satellite create(int id) {
		Satellite sat = null;

		try {
			Class<? extends Satellite> c = satellites.get(id);
			sat = c.newInstance();
		} catch(Exception e) {
			e.printStackTrace();
		}

		return sat;
	}

	public static int getIDFromItem(Item item) {
		Class<? extends Satellite> sat = itemToClass.get(item);

		return satellites.indexOf(sat);
	}

	public int getID() {
		return satellites.indexOf(this.getClass());
	}

	public void writeToNBT(NBTTagCompound nbt) { }
	public void readFromNBT(NBTTagCompound nbt) { }

	public void serialize(ByteBuf buf) { }
	public void deserialize(ByteBuf buf) { }

	/**
	 * Called when the satellite reaches space, used to trigger achievements and other funny stuff.
	 * @param x posX of the rocket
	 * @param y ditto
	 * @param z ditto
	 */
	public void onOrbit(World world, double x, double y, double z) { }

	/**
	 * Called by the sat interface when clicking on the screen
	 * @param x the x-coordinate translated from the on-screen coords to actual world coordinates
	 * @param z ditto
	 */
	public void onClick(World world, int x, int z) { }

	/**
	 * Called by the coord sat interface
	 * @param x the specified x-coordinate
	 * @param y ditto
	 * @param z ditto
	 */
	public void onCoordAction(World world, EntityPlayer player, int x, int y, int z) { }


	protected abstract float[] getColor();

	public void render(float partialTicks, WorldClient world, Minecraft mc, float solarAngle, long id) {
		renderDefault(partialTicks, world, mc, solarAngle, id, getColor());
	}

	public static void renderDefault(float partialTicks, WorldClient world, Minecraft mc, float solarAngle, long seed, float[] color) {
		if(color[3] <= 0.0F) return;

		Tessellator tessellator = Tessellator.instance;

		double ticks = (double)(System.currentTimeMillis() % (600 * 50)) / 50;

		GL11.glPushMatrix();
		{

			GL11.glRotatef(solarAngle * -360.0F, 1.0F, 0.0F, 0.0F);
			GL11.glRotatef(-40.0F + (float)(seed % 800) * 0.1F - 5.0F, 1.0F, 0.0F, 0.0F);
			GL11.glRotatef((float)(seed % 50) * 0.1F - 20.0F, 0.0F, 1.0F, 0.0F);
			GL11.glRotatef((float)(seed % 80) * 0.1F - 2.5F, 0.0F, 0.0F, 1.0F);
			GL11.glRotated((ticks / 600.0D) * -360.0D, 1.0F, 0.0F, 0.0F);

			GL11.glColor4f(color[0], color[1], color[2], color[3]);

			mc.renderEngine.bindTexture(satelliteTexture);

			float size = 0.5F;

			tessellator.startDrawingQuads();
			tessellator.addVertexWithUV(-size, 100.0, -size, 0.0D, 0.0D);
			tessellator.addVertexWithUV(size, 100.0, -size, 0.0D, 1.0D);
			tessellator.addVertexWithUV(size, 100.0, size, 1.0D, 1.0D);
			tessellator.addVertexWithUV(-size, 100.0, size, 1.0D, 0.0D);
			tessellator.draw();

		}
		GL11.glPopMatrix();
	}

	// killing myself
	public float getInterp() {
		return 0;
	}

}
