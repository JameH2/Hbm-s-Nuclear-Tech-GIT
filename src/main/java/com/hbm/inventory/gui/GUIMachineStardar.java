package com.hbm.inventory.gui;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;

import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;

import com.hbm.config.SpaceConfig;
import com.hbm.dim.CelestialBody;
import com.hbm.dim.SolarSystem;
import com.hbm.dim.trait.CBT_Impact;
import com.hbm.dim.trait.CBT_Lights;
import com.hbm.handler.CelestialNukeShockHandler;
import com.hbm.items.ItemVOTVdrive;
import com.hbm.inventory.container.ContainerStardar;
import com.hbm.items.ModItems;
import com.hbm.lib.Library;
import com.hbm.lib.RefStrings;
import com.hbm.packet.PacketDispatcher;
import com.hbm.packet.toserver.NBTControlPacket;
import com.hbm.render.shader.Shader;
import com.hbm.render.util.AtmosphereRenderUtil;
import com.hbm.saveddata.SatelliteSavedData;
import com.hbm.saveddata.satellites.Satellite;
import com.hbm.saveddata.satellites.SatelliteLaser;
import com.hbm.saveddata.satellites.SatelliteMapper;
import com.hbm.saveddata.satellites.SatelliteMiner;
import com.hbm.saveddata.satellites.SatelliteRadar;
import com.hbm.saveddata.satellites.SatelliteFoeq;
import com.hbm.saveddata.satellites.SatelliteResonator;
import com.hbm.saveddata.satellites.SatelliteScanner;
import com.hbm.tileentity.machine.TileEntityMachineStardar;
import com.hbm.util.AstronomyUtil;
import com.hbm.util.i18n.I18nUtil;

import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.resources.IResource;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;

public class GUIMachineStardar extends GuiInfoContainer {

	public static final ResourceLocation texture = new ResourceLocation(RefStrings.MODID + ":textures/gui/machine/gui_stardar.png");
	private static final ResourceLocation starmapTexture = new ResourceLocation(RefStrings.MODID + ":textures/gui/starmap3.png");
	private static final ResourceLocation ringTexture = new ResourceLocation(RefStrings.MODID + ":textures/misc/space/rings.png");
	private static final ResourceLocation impactTexture = new ResourceLocation(RefStrings.MODID + ":textures/misc/space/impact.png");
	private static final ResourceLocation defaultMask = new ResourceLocation(RefStrings.MODID, "textures/misc/space/default_mask.png");
	private static final ResourceLocation satelliteTextureDefault = new ResourceLocation(RefStrings.MODID, "textures/items/sat_base.png");
	private static final ResourceLocation satelliteTextureFoeq = new ResourceLocation(RefStrings.MODID, "textures/items/sat_foeq.png");
	private static final ResourceLocation satelliteTextureLaser = new ResourceLocation(RefStrings.MODID, "textures/items/sat_laser.png");
	private static final ResourceLocation satelliteTextureMapper = new ResourceLocation(RefStrings.MODID, "textures/items/sat_mapper.png");
	private static final ResourceLocation satelliteTextureMiner = new ResourceLocation(RefStrings.MODID, "textures/items/sat_miner.png");
	private static final ResourceLocation satelliteTextureRadar = new ResourceLocation(RefStrings.MODID, "textures/items/sat_radar.png");
	private static final ResourceLocation satelliteTextureResonator = new ResourceLocation(RefStrings.MODID, "textures/items/sat_resonator.png");
	private static final ResourceLocation satelliteTextureScanner = new ResourceLocation(RefStrings.MODID, "textures/items/sat_scanner.png");
	private static final Map<Class<?>, ResourceLocation> satelliteTextureByClass = new HashMap<Class<?>, ResourceLocation>();
	private static final ResourceLocation[] citylights = new ResourceLocation[]{
		new ResourceLocation(RefStrings.MODID, "textures/misc/space/citylights_0.png"),
		new ResourceLocation(RefStrings.MODID, "textures/misc/space/citylights_1.png"),
		new ResourceLocation(RefStrings.MODID, "textures/misc/space/citylights_2.png"),
		new ResourceLocation(RefStrings.MODID, "textures/misc/space/citylights_3.png"),
	};
	private static final Shader crescentShader = new Shader(new ResourceLocation(RefStrings.MODID, "shaders/crescent.frag"));
	private static final Shader atmosphereShader = new Shader(new ResourceLocation(RefStrings.MODID, "shaders/atmosphere.frag"));
	private static final Shader atmosphereEmissiveShader = new Shader(new ResourceLocation(RefStrings.MODID, "shaders/atmosphere_emissive.frag"));
	private static final Shader lightningShader = new Shader(new ResourceLocation(RefStrings.MODID, "shaders/lightning.frag"));
	private static final Shader nukeShader = new Shader(new ResourceLocation(RefStrings.MODID, "shaders/nuke.frag"));
	private static final Shader nightLightsShader = new Shader(new ResourceLocation(RefStrings.MODID, "shaders/nightlights.frag"));

	static {
		satelliteTextureByClass.put(SatelliteMapper.class, satelliteTextureMapper);
		satelliteTextureByClass.put(SatelliteScanner.class, satelliteTextureScanner);
		satelliteTextureByClass.put(SatelliteRadar.class, satelliteTextureRadar);
		satelliteTextureByClass.put(SatelliteLaser.class, satelliteTextureLaser);
		satelliteTextureByClass.put(SatelliteResonator.class, satelliteTextureResonator);
		satelliteTextureByClass.put(SatelliteFoeq.class, satelliteTextureFoeq);
		satelliteTextureByClass.put(SatelliteMiner.class, satelliteTextureMiner);
	}

	private static final int MAP_X = 9;
	private static final int MAP_Y = 9;
	private static final int MAP_W = 158;
	private static final int MAP_H = 108;

	private static final float MAP_TEX_W = 1024F;
	private static final float MAP_TEX_H = 1024F;
	private static final float STARMAP_BG_TEX_W = 1024F;
	private static final float STARMAP_BG_TEX_H = 1024F;
	private static final float SYSTEM_LAYOUT_REF_TEX = 256F;

	private static final float MIN_ZOOM = 0.5F;
	private static final float MAX_ZOOM = 180.0F;
	private static final float ZOOM_STEP = 1.1F;

	private static final float ISO_X = 0.70F;
	private static final float ISO_Y = 0.35F;
	private static final float STARDAR_BODY_SCALE = 0.45F;
	private static final float STARDAR_MOON_VISUAL_SCALE = 0.82F;
	private static final float STARDAR_SYSTEM_RADIUS_FRACTION = 0.50F;
	private static final float SATELLITE_ICON_BODY_SCALE = 0.75F;
	private static final float SATELLITE_ORBIT_RADIUS_SCALE = 1.5F;
	private static final long SATELLITE_CYCLE_MS = 600L * 50L;

	private final TileEntityMachineStardar star;
	private final DynamicTexture groundTexture;
	private final ResourceLocation groundMap;
	private final int[] groundColors;
	private final Map<ResourceLocation, Boolean> textureAlphaCache = new HashMap<ResourceLocation, Boolean>();

	private final List<BodyRenderInfo> renderedBodies = new ArrayList<BodyRenderInfo>();
	private final List<BodyLabelRenderInfo> renderedBodyLabels = new ArrayList<BodyLabelRenderInfo>();
	private final List<SatelliteRenderInfo> renderedSatellites = new ArrayList<SatelliteRenderInfo>();
	private final BodyPosition trackedBodyPosition = new BodyPosition();
	private final BodyPosition trackedSatellitePosition = new BodyPosition();

	private final CelestialBody currentBody;
	private CelestialBody focusedBody;
	private CelestialBody focusAnimationBody;
	private BodyRenderInfo hoveredBody;
	private SatelliteRenderInfo hoveredSatellite;
	private SatelliteRenderInfo focusedSatellite;
	private Integer focusedSatelliteFrequency;
	private CelestialBody landingBody;

	private boolean landingMode;
	private boolean hadFullDrive;
	private int lastFullDriveMeta = -1;

	private boolean draggingMap;
	private int dragLastX;
	private int dragLastY;

	private int mX;
	private int mY;
	private int lX;
	private int lY;
	private int sX;
	private int sY;
	private boolean draggingSurface;

	private float surfaceX;
	private float surfaceY;
	private float velocityX;
	private float velocityY;

	private float mapU = 0F;
	private float mapV = 0F;
	private float mapZoom = 1.0F;

	private boolean focusAnimationActive = false;
	private boolean focusAnimationToBody = false;
	private float focusAnimStartCenterU = 0F;
	private float focusAnimStartCenterV = 0F;
	private float focusAnimStartZoom = 1.0F;
	private float focusAnimTargetCenterU = 0F;
	private float focusAnimTargetCenterV = 0F;
	private float focusAnimTargetZoom = 1.0F;
	private boolean focusAnimTrackingInitialized = false;
	private float focusAnimTrackingOffsetU = 0F;
	private float focusAnimTrackingOffsetV = 0F;
	private long focusAnimStartTimeMs = 0L;
	private int focusAnimDurationMs = 500;

	private int lastMouseX;
	private int lastMouseY;

	public GUIMachineStardar(InventoryPlayer playerInventory, TileEntityMachineStardar stardar) {
		super(new ContainerStardar(playerInventory, stardar));
		this.star = stardar;

		this.xSize = 176;
		this.ySize = 256;

		this.currentBody = CelestialBody.getBody(star.getWorldObj());
		this.focusedBody = null;

		groundTexture = new DynamicTexture(256, 256);
		groundMap = Minecraft.getMinecraft().getTextureManager().getDynamicTextureLocation("stardar_ground_map", groundTexture);
		groundColors = groundTexture.getTextureData();

		setViewFromCenter(MAP_TEX_W * 0.5F, MAP_TEX_H * 0.5F, mapZoom);
	}

	@Override
	public void updateScreen() {
		super.updateScreen();
		updateLandingModeFromDrive();
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		lastMouseX = mouseX;
		lastMouseY = mouseY;

		super.drawScreen(mouseX, mouseY, partialTicks);

		this.drawCustomInfoStat(mouseX, mouseY, guiLeft + 129, guiTop + 124, 18, 18, mouseX, mouseY, I18nUtil.resolveKey("gui.stardar.tooltip.focus_current_body"));
		this.drawCustomInfoStat(mouseX, mouseY, guiLeft + 129, guiTop + 143, 18, 18, mouseX, mouseY, I18nUtil.resolveKey("gui.stardar.tooltip.program_new_station"));
		this.drawCustomInfoStat(mouseX, mouseY, guiLeft + 149, guiTop + 143, 18, 18, mouseX, mouseY, I18nUtil.resolveKey("gui.stardar.tooltip.program_current_body"));
		drawSatelliteTooltip(mouseX, mouseY);

	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
		GL11.glColor4f(1F, 1F, 1F, 1F);
		mc.getTextureManager().bindTexture(texture);
		drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);

		pushScissor(MAP_X, MAP_Y, MAP_W, MAP_H);
		if(landingMode) {
			drawSurfaceMap();
		} else {
			drawSystemMap(partialTicks);
		}
		popScissor();
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
		if(landingMode) {
			drawLandingForeground();
		} else {
			drawMapForeground();
		}
	}

	private void drawMapForeground() {
		if(focusedSatelliteFrequency != null) {
			fontRendererObj.drawString(I18nUtil.resolveKey("gui.stardar.label.focus_satellite"), 10, 128, 0x00FF00);
			fontRendererObj.drawString(I18nUtil.resolveKey("gui.stardar.label.unfocus_hint"), 10, 148, 0x00FF00);
			return;
		}

		CelestialBody activeFocus = getRenderFocusBody();
		CelestialBody displayBody = hoveredBody != null ? hoveredBody.body : activeFocus;

		if(displayBody == null) {
			fontRendererObj.drawString(I18nUtil.resolveKey("gui.stardar.label.drag_map"), 10, 128, 0x00FF00);
			fontRendererObj.drawString(I18nUtil.resolveKey("gui.stardar.label.click_body_focus"), 10, 148, 0x00FF00);
			return;
		}

		fontRendererObj.drawString(I18nUtil.resolveKey("gui.stardar.label.focus_body", getBodyDisplayName(displayBody)), 10, 128, 0x00FF00);
		if(activeFocus != null && displayBody == activeFocus) {
			if(!displayBody.canLand) {
				fontRendererObj.drawString(I18nUtil.resolveKey("gui.stardar.label.cant_land"), 10, 148, 0xFF4040);
			} else {
				ItemStack slotStack = inventorySlots.getSlot(0).getStack();

				if(slotStack == null) {
					fontRendererObj.drawString(I18nUtil.resolveKey("gui.stardar.label.insert_drive"), 10, 148, 0x00FF00);
				} else if(slotStack.getItem() == ModItems.hard_drive) {
					fontRendererObj.drawString(I18nUtil.resolveKey("gui.stardar.label.click_again_land"), 10, 148, 0x00FF00);
				} else {
					fontRendererObj.drawString(I18nUtil.resolveKey("gui.stardar.label.insert_drive"), 10, 148, 0x00FF00);
				}
			}
		} else {
			fontRendererObj.drawString(I18nUtil.resolveKey("gui.stardar.label.click_body_focus"), 10, 148, 0x00FF00);
		}
	}

	private void drawLandingForeground() {
		if(star.heightmap == null) {
			fontRendererObj.drawString(I18nUtil.resolveKey("gui.stardar.label.loading_surface_map"), 10, 148, 0x00FF00);
			return;
		}

		int sx = lastMouseX - ((lastMouseX + (int) surfaceX) % 2);
		int sy = lastMouseY - ((lastMouseY + (int) surfaceY) % 2);

		if(lastMouseX < guiLeft + MAP_X || lastMouseX >= guiLeft + MAP_X + MAP_W || lastMouseY < guiTop + MAP_Y || lastMouseY >= guiTop + MAP_Y + MAP_H) {
			fontRendererObj.drawString(I18nUtil.resolveKey("gui.stardar.label.select_landing_zone"), 10, 128, 0x00FF00);
			return;
		}

		int hx = (lastMouseX - (int) surfaceX - guiLeft - MAP_X + 256) / 2;
		int hz = (lastMouseY - (int) surfaceY - guiTop - MAP_Y + 256) / 2;
		String info = landingInfo(hx, hz);
		int altitude = altitude(hx, hz);
		boolean canLand = info == null;

		pushScissor(MAP_X, MAP_Y, MAP_W, MAP_H);
		mc.getTextureManager().bindTexture(texture);
		drawTexturedModalRect(sx - guiLeft - 6, sy - guiTop - 6, xSize + (canLand ? 14 : 0), 28, 14, 14);
		popScissor();

		fontRendererObj.drawString(canLand ? I18nUtil.resolveKey("gui.stardar.label.valid_location") : info, 10, 128, canLand ? 0x00FF00 : 0xFF0000);
		if(altitude > 0) fontRendererObj.drawString(I18nUtil.resolveKey("gui.stardar.label.target_altitude", altitude), 10, 148, 0x00FF00);
	}

	private void drawSystemMap(float partialTicks) {
		updateDragPanning(lastMouseX, lastMouseY);

		float bgSrcW = MAP_W;
		float bgSrcH = MAP_H;
		float bgU = MathHelper.clamp_float(mapU + (MAP_W / mapZoom) * 0.5F - bgSrcW * 0.5F, 0F, MAP_TEX_W - bgSrcW);
		float bgV = MathHelper.clamp_float(mapV + (MAP_H / mapZoom) * 0.5F - bgSrcH * 0.5F, 0F, MAP_TEX_H - bgSrcH);
		float bgScaleU = STARMAP_BG_TEX_W / MAP_TEX_W;
		float bgScaleV = STARMAP_BG_TEX_H / MAP_TEX_H;
		float bgUTex = bgU * bgScaleU;
		float bgVTex = bgV * bgScaleV;
		float bgSrcWTex = bgSrcW * bgScaleU;
		float bgSrcHTex = bgSrcH * bgScaleV;

		mc.getTextureManager().bindTexture(starmapTexture);
		drawPartialTex(
			guiLeft + MAP_X,
			guiTop + MAP_Y,
			MAP_W,
			MAP_H,
			bgUTex / STARMAP_BG_TEX_W,
			bgVTex / STARMAP_BG_TEX_H,
			(bgUTex + bgSrcWTex) / STARMAP_BG_TEX_W,
			(bgVTex + bgSrcHTex) / STARMAP_BG_TEX_H
		);

		renderedBodies.clear();
		renderedBodyLabels.clear();
		renderedSatellites.clear();
		hoveredSatellite = null;
		focusedSatellite = null;

		CelestialBody root = SolarSystem.kerbol;
		if(root == null) return;

		double dayTicks = mc != null && mc.theWorld != null ? (double) mc.theWorld.getTotalWorldTime() + (double) partialTicks : 0D;
		double worldTicks = dayTicks * (double) AstronomyUtil.TIME_MULTIPLIER;
		float centerU = MAP_TEX_W * 0.5F;
		float centerV = MAP_TEX_H * 0.5F;
		float systemOrbitScale = getOrbitScalePxPerKm(root);

		if(focusedBody != null && focusedBody.getStar() != root) {
			cancelFocusState();
		}
		if(focusAnimationBody != null && focusAnimationBody.getStar() != root) {
			cancelFocusState();
		}

		GL11.glPushAttrib(GL11.GL_ENABLE_BIT | GL11.GL_COLOR_BUFFER_BIT | GL11.GL_LINE_BIT);
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		GL11.glDisable(GL11.GL_ALPHA_TEST);

		updateFocusView(root, centerU, centerV, systemOrbitScale, worldTicks);
		drawBodyTree(root, centerU, centerV, systemOrbitScale, worldTicks, dayTicks, Float.NaN, Float.NaN);
		drawQueuedBodyLabels();

		GL11.glPopAttrib();

		hoveredBody = findBodyUnderCursor(lastMouseX, lastMouseY);
		hoveredSatellite = findSatelliteUnderCursor(lastMouseX, lastMouseY);
	}

	private void drawSurfaceMap() {
		if(!Mouse.isButtonDown(0)) {
			velocityX *= 0.85F;
			velocityY *= 0.85F;
			surfaceX += velocityX;
			surfaceY += velocityY;
			surfaceX = MathHelper.clamp_float(surfaceX, -256 + MAP_W, 256);
			surfaceY = MathHelper.clamp_float(surfaceY, -256 + MAP_H, 256);
		}

		mc.getTextureManager().bindTexture(starmapTexture);
		drawPartialTex(
			guiLeft + MAP_X,
			guiTop + MAP_Y,
			MAP_W,
			MAP_H,
			0F,
			0F,
			MAP_W / STARMAP_BG_TEX_W,
			MAP_H / STARMAP_BG_TEX_H
		);

		if(star.heightmap == null) {
			return;
		}

		if(star.updateHeightmap) {
			for(int i = 0; i < star.heightmap.length; i++) {
				int h = star.heightmap[i] % 16 * 16;
				groundColors[i] = 255 << 24 | h << 8;
			}
			groundTexture.updateDynamicTexture();
			star.updateHeightmap = false;
		}

		mc.getTextureManager().bindTexture(groundMap);
		drawPartialTex(
			guiLeft + MAP_X + (int) surfaceX - 256,
			guiTop + MAP_Y + (int) surfaceY - 256,
			512,
			512,
			0F,
			0F,
			1F,
			1F
		);
	}

	private void drawBodyTree(CelestialBody body, float bodyMapU, float bodyMapV, float systemOrbitScalePxPerKm, double worldTicks, double dayTicks, float parentMapU, float parentMapV) {
		if(body == null) return;

		List<ChildRenderState> childStates = new ArrayList<ChildRenderState>();
		float parentScreenY = mapToScreenY(bodyMapU, bodyMapV);

		if(body.satellites != null) {
			for(CelestialBody child : body.satellites) {
				if(child == null) continue;

				float childScale = getChildOrbitScalePxPerKm(body, systemOrbitScalePxPerKm);
				double meanAnomaly = calculateMeanAnomaly(child, worldTicks);
				float[] offset = calculateOrbitOffsetPx(body, child, meanAnomaly, childScale);

				boolean moonChild = body.parent != null;
				float childMapU = bodyMapU + offset[0];
				float childMapV = bodyMapV + offset[1];
				boolean drawInFrontOfParent = !moonChild || mapToScreenY(childMapU, childMapV) > parentScreenY;

				childStates.add(new ChildRenderState(child, childScale, offset[0], offset[1], moonChild, drawInFrontOfParent));
			}
		}

		for(ChildRenderState childState : childStates) {
			if(!shouldDrawChildOrbit(body, childState.childBody)) {
				continue;
			}

			if(childState.moonChild) {
				drawOrbitHalf(bodyMapU, bodyMapV, body, childState.childBody, childState.orbitScalePxPerKm, false);
			} else {
				drawOrbit(bodyMapU, bodyMapV, body, childState.childBody, childState.orbitScalePxPerKm);
			}
		}

		for(ChildRenderState childState : childStates) {
			if(!childState.moonChild || childState.drawInFrontOfParent) {
				continue;
			}

			drawBodyTree(
				childState.childBody,
				bodyMapU + childState.orbitOffsetU,
				bodyMapV + childState.orbitOffsetV,
				systemOrbitScalePxPerKm,
				worldTicks,
				dayTicks,
				bodyMapU,
				bodyMapV
			);
		}

		drawArtificialSatellites(body, bodyMapU, bodyMapV, false);
		drawBody(body, bodyMapU, bodyMapV, dayTicks, parentMapU, parentMapV);
		drawArtificialSatellites(body, bodyMapU, bodyMapV, true);

		for(ChildRenderState childState : childStates) {
			if(!childState.moonChild || !shouldDrawChildOrbit(body, childState.childBody)) {
				continue;
			}

			drawOrbitHalf(bodyMapU, bodyMapV, body, childState.childBody, childState.orbitScalePxPerKm, true);
		}

		for(ChildRenderState childState : childStates) {
			if(childState.moonChild && !childState.drawInFrontOfParent) {
				continue;
			}

			drawBodyTree(
				childState.childBody,
				bodyMapU + childState.orbitOffsetU,
				bodyMapV + childState.orbitOffsetV,
				systemOrbitScalePxPerKm,
				worldTicks,
				dayTicks,
				bodyMapU,
				bodyMapV
			);
		}
	}

	private boolean shouldDrawChildOrbit(CelestialBody parent, CelestialBody child) {
		if(parent == null || child == null) return false;

		if(parent.parent == null) {
			return true;
		}

		if(!isMoon(child)) {
			return true;
		}

		// Moon orbit lines are visible only when the parent body (or one of its moons) is focused.
		CelestialBody activeFocus = getOrbitRuleFocusBody();
		return activeFocus != null && (activeFocus == parent || activeFocus.parent == parent);
	}

	private void drawOrbit(float parentMapU, float parentMapV, CelestialBody parent, CelestialBody body, float orbitScalePxPerKm) {
		if(body == null || body.semiMajorAxisKm <= 0F) return;

		GL11.glDisable(GL11.GL_TEXTURE_2D);
		GL11.glLineWidth(2F);

		float[] color = body.color != null && body.color.length >= 3 ? body.color : null;
		float r = color != null ? color[0] : 0.8F;
		float g = color != null ? color[1] : 0.8F;
		float b = color != null ? color[2] : 0.8F;

		Tessellator tess = Tessellator.instance;
		tess.startDrawing(GL11.GL_LINE_STRIP);
		tess.setColorRGBA_F(r, g, b, 0.4F);

		for(int i = 0; i <= 96; i++) {
			double meanAnomaly = 2D * Math.PI * ((double) i / 96D);
			float[] offset = calculateOrbitOffsetPx(parent, body, meanAnomaly, orbitScalePxPerKm);
			float orbitMapU = parentMapU + offset[0];
			float orbitMapV = parentMapV + offset[1];
			tess.addVertex(mapToScreenX(orbitMapU, orbitMapV), mapToScreenY(orbitMapU, orbitMapV), this.zLevel);
		}
		tess.draw();

		GL11.glEnable(GL11.GL_TEXTURE_2D);
		GL11.glColor4f(1F, 1F, 1F, 1F);
	}

	private void drawOrbitHalf(float parentMapU, float parentMapV, CelestialBody parent, CelestialBody body, float orbitScalePxPerKm, boolean frontHalf) {
		if(body == null || body.semiMajorAxisKm <= 0F) {
			return;
		}

		GL11.glDisable(GL11.GL_TEXTURE_2D);
		GL11.glLineWidth(2F);

		float[] color = body.color != null && body.color.length >= 3 ? body.color : null;
		float r = color != null ? color[0] : 0.8F;
		float g = color != null ? color[1] : 0.8F;
		float b = color != null ? color[2] : 0.8F;

		float crossY = mapToScreenY(parentMapU, parentMapV);
		Tessellator t = Tessellator.instance;
		boolean hasPrevPoint = false;
		float prevScreenX = 0F;
		float prevScreenY = 0F;
		boolean prevInFrontHalf = false;
		boolean drawing = false;

		for(int i = 0; i <= 96; i++) {
			double meanAnomaly = 2D * Math.PI * ((double) i / 96D);
			float[] offset = calculateOrbitOffsetPx(parent, body, meanAnomaly, orbitScalePxPerKm);
			float orbitMapU = parentMapU + offset[0];
			float orbitMapV = parentMapV + offset[1];
			float currScreenX = mapToScreenX(orbitMapU, orbitMapV);
			float currScreenY = mapToScreenY(orbitMapU, orbitMapV);
			boolean currInFrontHalf = currScreenY >= crossY;

			if(!hasPrevPoint) {
				prevScreenX = currScreenX;
				prevScreenY = currScreenY;
				prevInFrontHalf = currInFrontHalf;
				hasPrevPoint = true;
				continue;
			}

			boolean prevInSelectedHalf = prevInFrontHalf == frontHalf;
			boolean currInSelectedHalf = currInFrontHalf == frontHalf;

			if(prevInSelectedHalf && currInSelectedHalf) {
				if(!drawing) {
					t.startDrawing(GL11.GL_LINE_STRIP);
					t.setColorRGBA_F(r, g, b, 0.4F);
					t.addVertex(prevScreenX, prevScreenY, this.zLevel);
					drawing = true;
				}
				t.addVertex(currScreenX, currScreenY, this.zLevel);
			} else if(prevInSelectedHalf != currInSelectedHalf) {
				float dy = currScreenY - prevScreenY;
				float tCross = dy == 0F ? 0.5F : (crossY - prevScreenY) / dy;
				tCross = MathHelper.clamp_float(tCross, 0F, 1F);
				float crossX = prevScreenX + (currScreenX - prevScreenX) * tCross;

				if(prevInSelectedHalf) {
					if(!drawing) {
						t.startDrawing(GL11.GL_LINE_STRIP);
						t.setColorRGBA_F(r, g, b, 0.4F);
						t.addVertex(prevScreenX, prevScreenY, this.zLevel);
						drawing = true;
					}
					t.addVertex(crossX, crossY, this.zLevel);
					t.draw();
					drawing = false;
				} else {
					t.startDrawing(GL11.GL_LINE_STRIP);
					t.setColorRGBA_F(r, g, b, 0.4F);
					t.addVertex(crossX, crossY, this.zLevel);
					t.addVertex(currScreenX, currScreenY, this.zLevel);
					drawing = true;
				}
			}

			prevScreenX = currScreenX;
			prevScreenY = currScreenY;
			prevInFrontHalf = currInFrontHalf;
		}

		if(drawing) {
			t.draw();
		}

		GL11.glEnable(GL11.GL_TEXTURE_2D);
		GL11.glColor4f(1F, 1F, 1F, 1F);
	}

	private void drawBody(CelestialBody body, float bodyMapU, float bodyMapV, double dayTicks, float parentMapU, float parentMapV) {
		if(body == null) return;

		float drawSize = getBodySizePxAtZoom(body);
		float bodyScreenX = mapToScreenX(bodyMapU, bodyMapV);
		float bodyScreenY = mapToScreenY(bodyMapU, bodyMapV);
		float half = drawSize * 0.5F;
		float ringHalfWidth = 0F;
		float ringHalfHeight = 0F;

		float minX = bodyScreenX - half;
		float maxX = bodyScreenX + half;
		float minY = bodyScreenY - half;
		float maxY = bodyScreenY + half;

		if(body.hasRings) {
			ringHalfWidth = drawSize * 0.5F * Math.max(1F, body.ringSize);
			float ringTiltSin = Math.abs(MathHelper.sin((float) Math.toRadians(body.ringTilt)));
			ringTiltSin = Math.max(0.08F, ringTiltSin);
			ringHalfHeight = Math.max(0.5F, ringHalfWidth * ringTiltSin);

			minX = Math.min(minX, bodyScreenX - ringHalfWidth);
			maxX = Math.max(maxX, bodyScreenX + ringHalfWidth);
			minY = Math.min(minY, bodyScreenY - ringHalfHeight);
			maxY = Math.max(maxY, bodyScreenY + ringHalfHeight);
		}

		if(maxX < guiLeft + MAP_X || minX > guiLeft + MAP_X + MAP_W ||
			maxY < guiTop + MAP_Y || minY > guiTop + MAP_Y + MAP_H) {
			return;
		}

		if(body.hasRings) {
			drawBodyRingHalf(body, bodyScreenX, bodyScreenY, ringHalfWidth, ringHalfHeight, false);
		}

		if(body.texture != null) {
			mc.getTextureManager().bindTexture(body.texture);
			if(body.parent == null) {
				drawTexturedQuad(bodyScreenX, bodyScreenY, drawSize, 0F);
			} else {
				AtmosphereRenderUtil.renderAtmosphereGlow2D(Tessellator.instance, body, bodyScreenX, bodyScreenY, drawSize, 1.0F);

				float phase = getBodyRotationPhase(body, dayTicks);
				float bodyRotationAngle = phase * 360F;
				boolean rotateBody = hasTransparentPixels(body.texture);
				float textureUOffset = 0F;
				if(rotateBody) {
					drawTexturedQuadRotating(bodyScreenX, bodyScreenY, drawSize, bodyRotationAngle);
				} else {
					drawTexturedQuad(bodyScreenX, bodyScreenY, drawSize, phase);
					textureUOffset = phase;
				}
				drawBodyOverlays(body, bodyScreenX, bodyScreenY, drawSize, bodyMapU, bodyMapV, parentMapU, parentMapV, rotateBody, bodyRotationAngle, dayTicks, textureUOffset);
			}
		} else {
			int color = colorArrayToRgb(body.color);
			drawRect((int) (bodyScreenX - half), (int) (bodyScreenY - half), (int) (bodyScreenX + half), (int) (bodyScreenY + half), color);
		}

		if(body.hasRings) {
			drawBodyRingHalf(body, bodyScreenX, bodyScreenY, ringHalfWidth, ringHalfHeight, true);
		}

		float labelAnchorExtentX = half;
		float labelAnchorExtentY = half;
		if(body.parent == null) {
			float starLabelHalfExtent = (8F * mapZoom) * 0.5F;
			labelAnchorExtentX = Math.min(labelAnchorExtentX, starLabelHalfExtent);
			labelAnchorExtentY = Math.min(labelAnchorExtentY, starLabelHalfExtent);
		}
		renderedBodyLabels.add(new BodyLabelRenderInfo(body, bodyScreenX + labelAnchorExtentX, bodyScreenY - labelAnchorExtentY));

		renderedBodies.add(new BodyRenderInfo(body, bodyMapU, bodyMapV, drawSize));
	}

	private void drawArtificialSatellites(CelestialBody body, float bodyMapU, float bodyMapV, boolean frontHalf) {
		if(!shouldDrawArtificialSatellites(body)) {
			return;
		}

		Map<Integer, Satellite> satellites = SatelliteSavedData.getClientSats();
		if(satellites == null || satellites.isEmpty()) {
			return;
		}

		float baseOrbitRadiusMapPx = getBodySizePxAt1x(body) * SATELLITE_ORBIT_RADIUS_SCALE;
		if(baseOrbitRadiusMapPx <= 0F) {
			return;
		}

		if(shouldDrawArtificialSatelliteOrbitLines(body)) {
			for(Satellite satellite : satellites.values()) {
				if(satellite == null) {
					continue;
				}
				drawArtificialSatelliteOrbitHalf(bodyMapU, bodyMapV, baseOrbitRadiusMapPx, satellite, frontHalf);
			}
		}

		float iconSize = MathHelper.clamp_float(getBodySizePxAtZoom(body) * SATELLITE_ICON_BODY_SCALE * 0.25F, 0.2F, 4.0F);
		float iconHalf = iconSize * 0.5F;
		float mapLeft = guiLeft + MAP_X;
		float mapTop = guiTop + MAP_Y;
		float mapRight = mapLeft + MAP_W;
		float mapBottom = mapTop + MAP_H;
		double angle = getArtificialSatelliteAngle();

		GL11.glColor4f(1F, 1F, 1F, 1F);
		for(Map.Entry<Integer, Satellite> entry : satellites.entrySet()) {
			Integer frequency = entry.getKey();
			Satellite satellite = entry.getValue();
			if(frequency == null || satellite == null) {
				continue;
			}

			float satelliteAngle = Satellite.applyPhaseOffsetToOrbitAngle(satellite.phaseOffset, satellite.altitude, angle, (float) (2D * Math.PI));
			SatelliteOrbitPoint orbitPoint = getArtificialSatelliteOrbitPoint(satellite, satelliteAngle, baseOrbitRadiusMapPx);
			float screenX = mapToScreenX(bodyMapU + orbitPoint.offsetU, bodyMapV + orbitPoint.offsetV);
			float screenY = mapToScreenY(bodyMapU + orbitPoint.offsetU, bodyMapV + orbitPoint.offsetV);
			boolean inFrontHalf = orbitPoint.depth >= 0F;
			if(inFrontHalf != frontHalf) {
				continue;
			}
			if(screenX + iconHalf < mapLeft || screenX - iconHalf > mapRight || screenY + iconHalf < mapTop || screenY - iconHalf > mapBottom) {
				continue;
			}

			mc.getTextureManager().bindTexture(getArtificialSatelliteTexture(satellite));
			drawPartialTex(screenX - iconHalf, screenY - iconHalf, iconSize, iconSize, 0F, 0F, 1F, 1F);

			SatelliteRenderInfo renderInfo = new SatelliteRenderInfo(frequency.intValue(), satellite, bodyMapU + orbitPoint.offsetU, bodyMapV + orbitPoint.offsetV, iconSize);
			renderedSatellites.add(renderInfo);
			if(focusedSatelliteFrequency != null && focusedSatelliteFrequency.intValue() == renderInfo.frequency) {
				focusedSatellite = renderInfo;
			}
		}
	}

	private boolean shouldDrawArtificialSatellites(CelestialBody body) {
		return body != null && currentBody != null && body == currentBody;
	}

	private void drawArtificialSatelliteOrbitHalf(float bodyMapU, float bodyMapV, float baseRadiusMapPx, Satellite satellite, boolean frontHalf) {
		float r = MathHelper.clamp_float(satellite.colorR, 0F, 1F);
		float g = MathHelper.clamp_float(satellite.colorG, 0F, 1F);
		float b = MathHelper.clamp_float(satellite.colorB, 0F, 1F);

		GL11.glDisable(GL11.GL_TEXTURE_2D);
		GL11.glLineWidth(1F);

		Tessellator tessellator = Tessellator.instance;
		boolean hasPrev = false;
		float prevX = 0F;
		float prevY = 0F;
		float prevDepth = 0F;
		boolean prevInFrontHalf = false;
		boolean drawing = false;

		for(int i = 0; i <= 64; i++) {
			float angle = (float) (2D * Math.PI * ((double) i / 64D));
			SatelliteOrbitPoint orbitPoint = getArtificialSatelliteOrbitPoint(satellite, angle, baseRadiusMapPx);
			float currX = mapToScreenX(bodyMapU + orbitPoint.offsetU, bodyMapV + orbitPoint.offsetV);
			float currY = mapToScreenY(bodyMapU + orbitPoint.offsetU, bodyMapV + orbitPoint.offsetV);
			float currDepth = orbitPoint.depth;
			boolean currInFrontHalf = currDepth >= 0F;

			if(!hasPrev) {
				prevX = currX;
				prevY = currY;
				prevDepth = currDepth;
				prevInFrontHalf = currInFrontHalf;
				hasPrev = true;
				continue;
			}

			boolean prevSelected = prevInFrontHalf == frontHalf;
			boolean currSelected = currInFrontHalf == frontHalf;

			if(prevSelected && currSelected) {
				if(!drawing) {
					tessellator.startDrawing(GL11.GL_LINE_STRIP);
					tessellator.setColorRGBA_F(r, g, b, 0.25F);
					tessellator.addVertex(prevX, prevY, this.zLevel);
					drawing = true;
				}
				tessellator.addVertex(currX, currY, this.zLevel);
			} else if(prevSelected != currSelected) {
				float depthDelta = currDepth - prevDepth;
				float t = depthDelta == 0F ? 0.5F : -prevDepth / depthDelta;
				t = MathHelper.clamp_float(t, 0F, 1F);
				float crossX = prevX + (currX - prevX) * t;
				float crossPointY = prevY + (currY - prevY) * t;

				if(prevSelected) {
					if(!drawing) {
						tessellator.startDrawing(GL11.GL_LINE_STRIP);
						tessellator.setColorRGBA_F(r, g, b, 0.25F);
						tessellator.addVertex(prevX, prevY, this.zLevel);
						drawing = true;
					}
					tessellator.addVertex(crossX, crossPointY, this.zLevel);
					tessellator.draw();
					drawing = false;
				} else {
					tessellator.startDrawing(GL11.GL_LINE_STRIP);
					tessellator.setColorRGBA_F(r, g, b, 0.25F);
					tessellator.addVertex(crossX, crossPointY, this.zLevel);
					tessellator.addVertex(currX, currY, this.zLevel);
					drawing = true;
				}
			}

			prevX = currX;
			prevY = currY;
			prevDepth = currDepth;
			prevInFrontHalf = currInFrontHalf;
		}

		if(drawing) {
			tessellator.draw();
		}

		GL11.glEnable(GL11.GL_TEXTURE_2D);
		GL11.glColor4f(1F, 1F, 1F, 1F);
	}

	private SatelliteOrbitPoint getArtificialSatelliteOrbitPoint(Satellite satellite, float angle, float baseRadiusMapPx) {
		float altitude = satellite != null ? satellite.altitude : Satellite.DEFAULT_ALTITUDE_KM;
		double inclination = Math.toRadians(satellite != null ? satellite.inclination : Satellite.DEFAULT_INCLINATION);
		double radiusMapPx = baseRadiusMapPx * (altitude / Satellite.DEFAULT_ALTITUDE_KM);

		double x = radiusMapPx * MathHelper.cos(angle);
		double y = radiusMapPx * MathHelper.sin(angle);
		double z = Math.sin(inclination) * y;
		y = Math.cos(inclination) * y;

		y -= z * 0.35D;
		y *= 0.8D;

		return new SatelliteOrbitPoint((float) x, (float) y, (float) z);
	}

	private double getArtificialSatelliteAngle() {
		long cycle = SATELLITE_CYCLE_MS;
		double progress = (double) System.currentTimeMillis() / (double) cycle;
		return -progress * 2D * Math.PI;
	}

	private ResourceLocation getArtificialSatelliteTexture(Satellite satellite) {
		for(Class<?> type = satellite != null ? satellite.getClass() : null; type != null; type = type.getSuperclass()) {
			ResourceLocation texture = satelliteTextureByClass.get(type);
			if(texture != null) {
				return texture;
			}
			if(type == Satellite.class) {
				break;
			}
		}
		return satelliteTextureDefault;
	}

	private void drawQueuedBodyLabels() {
		if(renderedBodyLabels.isEmpty() || this.fontRendererObj == null) {
			return;
		}

		GL11.glDisable(GL11.GL_DEPTH_TEST);
		GL11.glDepthMask(false);

		List<BodyLabelRenderInfo> sortedLabels = new ArrayList<BodyLabelRenderInfo>(renderedBodyLabels);
		sortedLabels.sort(new Comparator<BodyLabelRenderInfo>() {
			@Override
			public int compare(BodyLabelRenderInfo a, BodyLabelRenderInfo b) {
				return Integer.compare(getBodyHierarchyDepth(b.body), getBodyHierarchyDepth(a.body));
			}
		});
		for(BodyLabelRenderInfo labelInfo : sortedLabels) {
			if(labelInfo == null || labelInfo.body == null) {
				continue;
			}
			drawBodyLabel(labelInfo.body, labelInfo.topRightAnchorScreenX, labelInfo.topRightAnchorScreenY);
		}

		GL11.glDepthMask(true);
	}

	private void drawBodyLabel(CelestialBody body, float anchorScreenX, float anchorScreenY) {
		if(body == null || this.fontRendererObj == null) {
			return;
		}
		if(!shouldShowBodyLabel(body)) {
			return;
		}

		String label = getBodyDisplayName(body);
		if(label.isEmpty()) {
			return;
		}

		float labelX = anchorScreenX + 1.5F;
		float labelY = anchorScreenY - 1.5F - this.fontRendererObj.FONT_HEIGHT * 0.6F;
		int labelColor = colorArrayToRgb(body.color);
		int labelDrawX = Math.round(labelX / 0.6F);
		int labelDrawY = Math.round(labelY / 0.6F);

		GL11.glPushMatrix();
		GL11.glScalef(0.6F, 0.6F, 1F);
		this.fontRendererObj.drawStringWithShadow(label, labelDrawX, labelDrawY, labelColor);
		GL11.glPopMatrix();
		GL11.glColor4f(1F, 1F, 1F, 1F);
	}

	private boolean shouldShowBodyLabel(CelestialBody body) {
		if(body == null) {
			return false;
		}

		if(body.parent == null) {
			return false;
		}

		if(!isMoon(body)) {
			return true;
		}

		CelestialBody focusedTopLevelPlanet = getTopLevelPlanetForBody(getRenderFocusBody());
		if(focusedTopLevelPlanet == null) {
			return false;
		}
		return getTopLevelPlanetForBody(body) == focusedTopLevelPlanet;
	}

	private void drawBodyRingHalf(CelestialBody body, float bodyScreenX, float bodyScreenY, float ringHalfWidth, float drawH, boolean frontHalf) {
		if(body == null || ringHalfWidth <= 0F || drawH <= 0F) {
			return;
		}

		float[] ringColor = body.ringColor != null && body.ringColor.length >= 3 ? body.ringColor : null;
		float r = ringColor != null ? ringColor[0] : 0.5F;
		float g = ringColor != null ? ringColor[1] : 0.5F;
		float b = ringColor != null ? ringColor[2] : 0.5F;
		float a = ringColor != null && ringColor.length >= 4 ? ringColor[3] : 1F;

		float drawX = bodyScreenX - ringHalfWidth;
		float drawY = frontHalf ? bodyScreenY : bodyScreenY - drawH;
		float drawW = ringHalfWidth * 2F;
		float v1 = frontHalf ? 0.5F : 0F;
		float v2 = frontHalf ? 1F : 0.5F;

		GL11.glColor4f(r, g, b, a);
		mc.getTextureManager().bindTexture(ringTexture);
		drawPartialTex(drawX, drawY, drawW, drawH, 0F, v1, 1F, v2);
		GL11.glColor4f(1F, 1F, 1F, 1F);
	}

	private void drawPartialTex(float x, float y, float w, float h, float u1, float v1, float u2, float v2) {
		Tessellator tess = Tessellator.instance;
		tess.startDrawingQuads();
		tess.addVertexWithUV(x, y + h, this.zLevel, u1, v2);
		tess.addVertexWithUV(x + w, y + h, this.zLevel, u2, v2);
		tess.addVertexWithUV(x + w, y, this.zLevel, u2, v1);
		tess.addVertexWithUV(x, y, this.zLevel, u1, v1);
		tess.draw();
	}

	private void drawTexturedQuad(float x, float y, float size, float uOffset) {
		float half = size * 0.5F;
		Tessellator tess = Tessellator.instance;
		float minU = uOffset;
		float maxU = 1F + uOffset;
		tess.startDrawingQuads();
		tess.addVertexWithUV(x - half, y + half, 0, minU, 1);
		tess.addVertexWithUV(x + half, y + half, 0, maxU, 1);
		tess.addVertexWithUV(x + half, y - half, 0, maxU, 0);
		tess.addVertexWithUV(x - half, y - half, 0, minU, 0);
		tess.draw();
	}

	private void drawTexturedQuadRotating(float x, float y, float size, float angle) {
		GL11.glPushMatrix();
		GL11.glTranslatef(x, y, 0F);
		GL11.glRotatef(angle, 0F, 0F, 1F);
		drawTexturedQuad(0F, 0F, size, 0F);
		GL11.glPopMatrix();
	}

	private void drawBodyOverlays(CelestialBody body, float bodyScreenX, float bodyScreenY, float drawSize, float bodyMapU, float bodyMapV, float parentMapU, float parentMapV, boolean rotateBody, float bodyRotationAngle, double dayTicks, float textureUOffset) {
		if(body == null || body.parent == null || body.texture == null) {
			return;
		}

		float phase = calculateHorizontalCrescentPhase(body, bodyMapU, bodyMapV, parentMapU, parentMapV);
		CBT_Impact impact = body.getTrait(CBT_Impact.class);
		CBT_Lights light = body.getTrait(CBT_Lights.class);
		List<CelestialNukeShockHandler.ShockStatus> nukeShocks = CelestialNukeShockHandler.getClientShocks(body);
		double impactTime = impact != null ? dayTicks - impact.time : 0.0D;
		float impactAnimationTime = impact != null ? (float) impactTime : -1.0F;
		int lightIntensity = light != null && impactTime < 40.0D ? MathHelper.clamp_int(light.getIntensity(), 0, citylights.length - 1) : 0;
		int activeBlackouts = Math.max(0, Math.min((int) (impactTime / 8.0D), 5));
		float atmosphereAlpha = AtmosphereRenderUtil.getAtmosphereSurfaceAlpha(body);
		float atmosphereDensity = AtmosphereRenderUtil.getAtmosphereDensity(body);
		net.minecraft.util.Vec3 atmosphereColor = AtmosphereRenderUtil.getBodyAtmosphereColor(body);
		net.minecraft.util.Vec3 cloudColor = AtmosphereRenderUtil.getBodyCloudColor(body);
		float cloudTintStrength = AtmosphereRenderUtil.getBodyCloudTintStrength(body);
		float weatherPartialTicks = (float) (dayTicks - Math.floor(dayTicks));
		float cloudStormDarkness = AtmosphereRenderUtil.getBodyCloudStormDarkness(body, weatherPartialTicks);
		float cloudLightningStrength = AtmosphereRenderUtil.getBodyCloudLightningStrength(body, weatherPartialTicks);
		int atmosphereStyle = AtmosphereRenderUtil.getAtmosphereStyle(body);
		float atmosphereTime = (float) (dayTicks / 20.0D);
		float atmospherePatternOffset = textureUOffset;
		if(!rotateBody && body != null) {
			double period = body.getRotationalPeriod();
			if(period > 0D && !Double.isNaN(period) && !Double.isInfinite(period)) {
				atmospherePatternOffset = (float) (dayTicks / period);
			}
		}

		if(atmosphereAlpha > 0.001F) {
			GL11.glEnable(GL11.GL_BLEND);
			GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
			GL11.glColor4f(1F, 1F, 1F, 1F);

			atmosphereShader.use();
			atmosphereShader.setUniform1f("offset", textureUOffset);
			atmosphereShader.setUniform1f("patternOffset", atmospherePatternOffset);
			atmosphereShader.setUniform1i("bodyTex", 0);
			atmosphereShader.setUniform1i("useBodyAlphaMask", 1);
			atmosphereShader.setUniform1f("atmosphereColorR", (float) atmosphereColor.xCoord);
			atmosphereShader.setUniform1f("atmosphereColorG", (float) atmosphereColor.yCoord);
			atmosphereShader.setUniform1f("atmosphereColorB", (float) atmosphereColor.zCoord);
			atmosphereShader.setUniform1f("cloudColorR", (float) cloudColor.xCoord);
			atmosphereShader.setUniform1f("cloudColorG", (float) cloudColor.yCoord);
			atmosphereShader.setUniform1f("cloudColorB", (float) cloudColor.zCoord);
			atmosphereShader.setUniform1f("cloudTintStrength", cloudTintStrength);
			atmosphereShader.setUniform1f("cloudStormDarkness", cloudStormDarkness);
			atmosphereShader.setUniform1f("atmosphereAlpha", atmosphereAlpha);
			atmosphereShader.setUniform1f("atmosphereTime", atmosphereTime);
			atmosphereShader.setUniform1i("atmosphereStyle", atmosphereStyle);
			atmosphereShader.setUniform1f("impactTime", impactAnimationTime);
			AtmosphereRenderUtil.applyNukeShockUniforms(atmosphereShader, nukeShocks, dayTicks);

			GL13.glActiveTexture(GL13.GL_TEXTURE0);
			mc.getTextureManager().bindTexture(body.texture);
			if(rotateBody) {
				drawTexturedQuadRotating(bodyScreenX, bodyScreenY, drawSize, bodyRotationAngle);
			} else {
				drawTexturedQuad(bodyScreenX, bodyScreenY, drawSize, 0F);
			}

			atmosphereShader.stop();
		}

		GL11.glEnable(GL11.GL_BLEND);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		GL11.glColor4f(1F, 1F, 1F, 1F);

		crescentShader.use();
		crescentShader.setUniform1f("phase", phase);
		crescentShader.setUniform1f("offset", textureUOffset);
		crescentShader.setUniform1i("bodyTex", 0);
		crescentShader.setUniform1i("useBodyAlphaMask", 1);

		GL13.glActiveTexture(GL13.GL_TEXTURE0);
		mc.getTextureManager().bindTexture(body.texture);
		if(rotateBody) {
			drawTexturedQuadRotating(bodyScreenX, bodyScreenY, drawSize, bodyRotationAngle);
		} else {
			drawTexturedQuad(bodyScreenX, bodyScreenY, drawSize, 0F);
		}

		crescentShader.stop();

			if(lightIntensity > 0
				&& atmosphereDensity > 0.001F
				&& (atmosphereStyle == AtmosphereRenderUtil.ATMOSPHERE_STYLE_CLOUDS || atmosphereStyle == AtmosphereRenderUtil.ATMOSPHERE_STYLE_HAZE)) {
			GL11.glEnable(GL11.GL_BLEND);
			GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
			GL11.glColor4f(1F, 1F, 1F, 1F);

			atmosphereEmissiveShader.use();
			atmosphereEmissiveShader.setUniform1f("phase", phase);
			atmosphereEmissiveShader.setUniform1f("offset", textureUOffset);
			atmosphereEmissiveShader.setUniform1f("atmosphereDensity", atmosphereDensity);
			atmosphereEmissiveShader.setUniform1f("patternOffset", atmospherePatternOffset);
			atmosphereEmissiveShader.setUniform1f("atmosphereTime", atmosphereTime);
			atmosphereEmissiveShader.setUniform1i("atmosphereStyle", atmosphereStyle);
			atmosphereEmissiveShader.setUniform1f("impactTime", impactAnimationTime);
			AtmosphereRenderUtil.applyNukeShockUniforms(atmosphereEmissiveShader, nukeShocks, dayTicks);
			atmosphereEmissiveShader.setUniform1i("bodyTex", 0);
			atmosphereEmissiveShader.setUniform1i("lights", 1);
			atmosphereEmissiveShader.setUniform1i("cityMask", 2);
			atmosphereEmissiveShader.setUniform1i("blackouts", activeBlackouts);
			atmosphereEmissiveShader.setUniform1i("useBodyAlphaMask", 1);

			GL13.glActiveTexture(GL13.GL_TEXTURE0);
			mc.getTextureManager().bindTexture(body.texture);
			GL13.glActiveTexture(GL13.GL_TEXTURE1);
			mc.getTextureManager().bindTexture(citylights[lightIntensity]);
			GL13.glActiveTexture(GL13.GL_TEXTURE2);
			mc.getTextureManager().bindTexture(body.cityMask != null ? body.cityMask : defaultMask);
			GL13.glActiveTexture(GL13.GL_TEXTURE0);
			if(rotateBody) {
				drawTexturedQuadRotating(bodyScreenX, bodyScreenY, drawSize, bodyRotationAngle);
			} else {
				drawTexturedQuad(bodyScreenX, bodyScreenY, drawSize, 0F);
			}

			atmosphereEmissiveShader.stop();
		}

			if(lightIntensity > 0) {
				GL11.glEnable(GL11.GL_BLEND);
				GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
				GL11.glColor4f(1F, 1F, 1F, 1F);

				nightLightsShader.use();
				nightLightsShader.setUniform1f("phase", phase);
				nightLightsShader.setUniform1f("offset", textureUOffset);
				nightLightsShader.setUniform1f("atmosphereDensity", atmosphereDensity);
				nightLightsShader.setUniform1f("patternOffset", atmospherePatternOffset);
				nightLightsShader.setUniform1f("atmosphereTime", atmosphereTime);
				nightLightsShader.setUniform1i("atmosphereStyle", atmosphereStyle);
				nightLightsShader.setUniform1f("impactTime", impactAnimationTime);
				AtmosphereRenderUtil.applyNukeShockUniforms(nightLightsShader, nukeShocks, dayTicks);
				nightLightsShader.setUniform1i("bodyTex", 0);
				nightLightsShader.setUniform1i("lights", 1);
				nightLightsShader.setUniform1i("cityMask", 2);
				nightLightsShader.setUniform1i("blackouts", activeBlackouts);
				nightLightsShader.setUniform1i("useBodyAlphaMask", 1);

				GL13.glActiveTexture(GL13.GL_TEXTURE0);
				mc.getTextureManager().bindTexture(body.texture);
				GL13.glActiveTexture(GL13.GL_TEXTURE1);
				mc.getTextureManager().bindTexture(citylights[lightIntensity]);
				GL13.glActiveTexture(GL13.GL_TEXTURE2);
				mc.getTextureManager().bindTexture(body.cityMask != null ? body.cityMask : defaultMask);
				GL13.glActiveTexture(GL13.GL_TEXTURE0);
				if(rotateBody) {
					drawTexturedQuadRotating(bodyScreenX, bodyScreenY, drawSize, bodyRotationAngle);
				} else {
					drawTexturedQuad(bodyScreenX, bodyScreenY, drawSize, 0F);
				}

				nightLightsShader.stop();
			}

		if(atmosphereAlpha > 0.001F && cloudLightningStrength > 0.001F
			&& (atmosphereStyle == AtmosphereRenderUtil.ATMOSPHERE_STYLE_CLOUDS || atmosphereStyle == AtmosphereRenderUtil.ATMOSPHERE_STYLE_HAZE)) {
			GL11.glEnable(GL11.GL_BLEND);
			GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
			GL11.glColor4f(1F, 1F, 1F, 1F);

			lightningShader.use();
			lightningShader.setUniform1f("phase", phase);
			lightningShader.setUniform1f("offset", textureUOffset);
			lightningShader.setUniform1f("patternOffset", atmospherePatternOffset);
			lightningShader.setUniform1i("bodyTex", 0);
			lightningShader.setUniform1i("cityMask", 1);
			lightningShader.setUniform1i("useBodyAlphaMask", 1);
			lightningShader.setUniform1f("cloudTintStrength", cloudTintStrength);
			lightningShader.setUniform1f("cloudLightningStrength", cloudLightningStrength);
			lightningShader.setUniform1f("atmosphereAlpha", atmosphereAlpha);
			lightningShader.setUniform1f("atmosphereTime", atmosphereTime);
			lightningShader.setUniform1f("eveFlashStrength", AtmosphereRenderUtil.getBodyEveFlashStrength(body, atmosphereTime));
			lightningShader.setUniform1i("atmosphereStyle", atmosphereStyle);
			lightningShader.setUniform1i("lightningMode", AtmosphereRenderUtil.getBodyLightningMode(body));
			lightningShader.setUniform1f("impactTime", impactAnimationTime);
			AtmosphereRenderUtil.applyNukeShockUniforms(lightningShader, nukeShocks, dayTicks);

			GL13.glActiveTexture(GL13.GL_TEXTURE0);
			mc.getTextureManager().bindTexture(body.texture);
			GL13.glActiveTexture(GL13.GL_TEXTURE1);
			mc.getTextureManager().bindTexture(body.cityMask != null ? body.cityMask : defaultMask);
			GL13.glActiveTexture(GL13.GL_TEXTURE0);
			if(rotateBody) {
				drawTexturedQuadRotating(bodyScreenX, bodyScreenY, drawSize, bodyRotationAngle);
			} else {
				drawTexturedQuad(bodyScreenX, bodyScreenY, drawSize, 0F);
			}

			lightningShader.stop();
		}

		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

		if(impact != null) {
			float lavaAlpha = (float) Math.min(impactTime * 0.1D, 1.0D);
			if(lavaAlpha > 0F) {
				GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
				GL11.glColor4f(1F, 1F, 1F, lavaAlpha);
				mc.getTextureManager().bindTexture(impactTexture);
				if(rotateBody) {
					drawTexturedQuadRotating(bodyScreenX, bodyScreenY, drawSize, bodyRotationAngle);
				} else {
					drawTexturedQuad(bodyScreenX, bodyScreenY, drawSize, textureUOffset);
				}
				GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
			}
		}

		if(!nukeShocks.isEmpty()) {
			renderNukeImpactOverlay(bodyScreenX, bodyScreenY, drawSize, rotateBody, bodyRotationAngle, phase, nukeShocks, dayTicks);
			GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		}

		GL11.glColor4f(1F, 1F, 1F, 1F);
	}

	private void renderNukeImpactOverlay(float bodyScreenX, float bodyScreenY, float drawSize, boolean rotateBody, float bodyRotationAngle, float phase, List<CelestialNukeShockHandler.ShockStatus> nukeShocks, double currentShockTime) {
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
		GL11.glColor4f(1F, 1F, 1F, 1F);

		nukeShader.use();
		nukeShader.setUniform1f("phase", phase);
		AtmosphereRenderUtil.applyNukeShockUniforms(nukeShader, nukeShocks, currentShockTime);
		if(rotateBody) {
			drawTexturedQuadRotating(bodyScreenX, bodyScreenY, drawSize, bodyRotationAngle);
		} else {
			drawTexturedQuad(bodyScreenX, bodyScreenY, drawSize, 0F);
		}

		nukeShader.stop();
	}

	// yeah no, radius checks and new trait just don't work. drop them transparency checks, planets won't have it anyway, how bad it can be!
	private boolean hasTransparentPixels(ResourceLocation texture) {
		if(texture == null || mc == null || mc.getResourceManager() == null) {
			return false;
		}

		Boolean cached = textureAlphaCache.get(texture);
		if(cached != null) {
			return cached;
		}

		boolean hasAlpha = false;
		InputStream stream = null;
		try {
			IResource resource = mc.getResourceManager().getResource(texture);
			stream = resource.getInputStream();
			BufferedImage image = ImageIO.read(stream);
			if(image != null && image.getColorModel().hasAlpha()) {
				for(int y = 0; y < image.getHeight() && !hasAlpha; y++) {
					for(int x = 0; x < image.getWidth(); x++) {
						if(((image.getRGB(x, y) >>> 24) & 255) < 255) {
							hasAlpha = true;
							break;
						}
					}
				}
			}
		} catch (IOException ignored) {
		} finally {
			if(stream != null) {
				try {
					stream.close();
				} catch (IOException ignored) {
				}
			}
		}

		textureAlphaCache.put(texture, hasAlpha);
		return hasAlpha;
	}

	private float calculateHorizontalCrescentPhase(CelestialBody body, float bodyMapU, float bodyMapV, float parentMapU, float parentMapV) {
		float sunMapU = MAP_TEX_W * 0.5F;
		float sunMapV = MAP_TEX_H * 0.5F;
		float bodyProjX = projectMapToIsoX(bodyMapU, bodyMapV);
		float bodyProjY = projectMapToIsoY(bodyMapU, bodyMapV);
		float sunProjX = projectMapToIsoX(sunMapU, sunMapV);
		float sunProjY = projectMapToIsoY(sunMapU, sunMapV);

		float dx = bodyProjX - sunProjX;
		float dy = bodyProjY - sunProjY;
		float lengthSq = dx * dx + dy * dy;
		if(lengthSq <= 0.0001F) {
			return 0F;
		}

		float length = MathHelper.sqrt_float(lengthSq);
		float verticalFactor = dy / length;
		float phaseMagnitude = MathHelper.clamp_float((verticalFactor + 1F) * 0.5F, 0F, 1F);

		phaseMagnitude = applyMoonEclipseDarkening(body, bodyMapU, bodyMapV, parentMapU, parentMapV, sunMapU, sunMapV, phaseMagnitude);

		float phaseSign = dx <= 0F ? 1F : -1F;
		return MathHelper.clamp_float(phaseMagnitude * phaseSign, -1F, 1F);
	}

	private float applyMoonEclipseDarkening(CelestialBody body, float moonMapU, float moonMapV,
											float parentMapU, float parentMapV, float sunMapU, float sunMapV, float phaseMagnitude) {
		if(!isMoon(body) || body.parent == null) {
			return phaseMagnitude;
		}
		if(Float.isNaN(parentMapU) || Float.isNaN(parentMapV)) {
			return phaseMagnitude;
		}

		float moonProjX = projectMapToIsoX(moonMapU, moonMapV);
		float moonProjY = projectMapToIsoY(moonMapU, moonMapV);
		float parentProjX = projectMapToIsoX(parentMapU, parentMapV);
		float parentProjY = projectMapToIsoY(parentMapU, parentMapV);
		float sunProjX = projectMapToIsoX(sunMapU, sunMapV);
		float sunProjY = projectMapToIsoY(sunMapU, sunMapV);

		float sunToParentX = parentProjX - sunProjX;
		float sunToParentY = parentProjY - sunProjY;
		float parentToMoonX = moonProjX - parentProjX;
		float parentToMoonY = moonProjY - parentProjY;

		float sunParentLenSq = sunToParentX * sunToParentX + sunToParentY * sunToParentY;
		if(sunParentLenSq <= 0.0001F) {
			return phaseMagnitude;
		}

		float sunParentLen = MathHelper.sqrt_float(sunParentLenSq);
		float alongShadowAxis = (sunToParentX * parentToMoonX + sunToParentY * parentToMoonY) / sunParentLen;
		float lineDistance = Math.abs(sunToParentX * parentToMoonY - sunToParentY * parentToMoonX) / sunParentLen;
		float eclipseRadius = Math.max(1.5F, getBodySizePxAt1x(body.parent) * 0.45F);
		float penumbraRadius = eclipseRadius * 2F;
		float behindFade = Math.max(0.15F, eclipseRadius * 0.12F);

		if(lineDistance >= penumbraRadius || alongShadowAxis <= -behindFade) {
			return phaseMagnitude;
		}

		float behindFactor = alongShadowAxis >= 0F ? 1F : Library.smoothstep(alongShadowAxis, -behindFade, 0F);
		if(lineDistance <= eclipseRadius) {
			return MathHelper.clamp_float(phaseMagnitude + (1F - phaseMagnitude) * behindFactor, 0F, 1F);
		}

		float penumbraFactor = 1F - Library.smoothstep(lineDistance, eclipseRadius, penumbraRadius);
		float eclipseFactor = behindFactor * penumbraFactor;
		return MathHelper.clamp_float(phaseMagnitude + (1F - phaseMagnitude) * eclipseFactor, 0F, 1F);
	}

	private float projectMapToIsoX(float mapU, float mapV) {
		return (mapU - mapV) * ISO_X;
	}

	private float projectMapToIsoY(float mapU, float mapV) {
		return (mapU + mapV) * ISO_Y;
	}

	private float mapToScreenX(float mapCoordU, float mapCoordV) {
		float centerU = mapU + (MAP_W / mapZoom) * 0.5F;
		float centerV = mapV + (MAP_H / mapZoom) * 0.5F;
		float relU = mapCoordU - centerU;
		float relV = mapCoordV - centerV;
		return guiLeft + MAP_X + MAP_W * 0.5F + (relU - relV) * mapZoom * ISO_X;
	}

	private float mapToScreenY(float mapCoordU, float mapCoordV) {
		float centerU = mapU + (MAP_W / mapZoom) * 0.5F;
		float centerV = mapV + (MAP_H / mapZoom) * 0.5F;
		float relU = mapCoordU - centerU;
		float relV = mapCoordV - centerV;
		return guiTop + MAP_Y + MAP_H * 0.5F + (relU + relV) * mapZoom * ISO_Y;
	}

	private BodyRenderInfo findBodyUnderCursor(int mouseX, int mouseY) {
		BodyRenderInfo nearest = null;
		float nearestDistSq = Float.MAX_VALUE;

		for(int i = renderedBodies.size() - 1; i >= 0; i--) {
			BodyRenderInfo bodyInfo = renderedBodies.get(i);
			float bodyX = mapToScreenX(bodyInfo.mapU, bodyInfo.mapV);
			float bodyY = mapToScreenY(bodyInfo.mapU, bodyInfo.mapV);
			float clickableRadius = Math.max(5F, bodyInfo.drawSize * 0.5F);

			float dx = mouseX - bodyX;
			float dy = mouseY - bodyY;
			float distanceSq = dx * dx + dy * dy;

			if(distanceSq <= clickableRadius * clickableRadius && distanceSq < nearestDistSq) {
				nearestDistSq = distanceSq;
				nearest = bodyInfo;
			}
		}

		return nearest;
	}

	private SatelliteRenderInfo findSatelliteUnderCursor(int mouseX, int mouseY) {
		SatelliteRenderInfo nearest = null;
		float nearestDistSq = Float.MAX_VALUE;

		for(int i = renderedSatellites.size() - 1; i >= 0; i--) {
			SatelliteRenderInfo satelliteInfo = renderedSatellites.get(i);
			float satelliteX = mapToScreenX(satelliteInfo.mapU, satelliteInfo.mapV);
			float satelliteY = mapToScreenY(satelliteInfo.mapU, satelliteInfo.mapV);
			float clickableRadius = Math.max(4F, satelliteInfo.drawSize * 0.75F);
			float dx = mouseX - satelliteX;
			float dy = mouseY - satelliteY;
			float distanceSq = dx * dx + dy * dy;

			if(distanceSq <= clickableRadius * clickableRadius && distanceSq < nearestDistSq) {
				nearestDistSq = distanceSq;
				nearest = satelliteInfo;
			}
		}

		return nearest;
	}

	@Override
	public void handleMouseInput() {
		super.handleMouseInput();

		if(landingMode) {
			handleLandingMouseInput();
			return;
		}

		if(this.mc == null) return;

		if(Mouse.getEventButton() == -1) {
			int wheel = Mouse.getEventDWheel();
			if(wheel == 0) return;

			int mouseX = Mouse.getEventX() * this.width / this.mc.displayWidth;
			int mouseY = this.height - Mouse.getEventY() * this.height / this.mc.displayHeight - 1;

			if(mouseX < guiLeft + MAP_X || mouseX >= guiLeft + MAP_X + MAP_W || mouseY < guiTop + MAP_Y || mouseY >= guiTop + MAP_Y + MAP_H || focusAnimationActive) return;
			if(isMoon(focusedBody)) return;

			float oldZoom = mapZoom;
			float minZoom = MIN_ZOOM;
			float maxZoom = MAX_ZOOM;

			if(focusedBody != null) {
				float[] bounds = new float[2];
				if(calculateFocusedChildZoomBounds(focusedBody, bounds)) {
					minZoom = bounds[0];
					maxZoom = Math.max(bounds[1], getBodyDetailZoomCap(focusedBody));
				}
			}

			float newZoom = oldZoom;
			if(wheel > 0) newZoom = Math.min(maxZoom, oldZoom * ZOOM_STEP);
			if(wheel < 0) newZoom = Math.max(minZoom, oldZoom / ZOOM_STEP);

			if(newZoom != oldZoom) {
				if(isFocusContextActive()) {
					mapZoom = newZoom;
				} else {
					zoomAtMouse(mouseX, mouseY, oldZoom, newZoom);
				}
			}
		}
	}

	private void handleLandingMouseInput() {
		if(surfaceX > 256) {
			velocityX = 0;
			surfaceX = 256;
		}
		if(surfaceX < -256 + MAP_W) {
			velocityX = 0;
			surfaceX = -256 + MAP_W;
		}
		if(surfaceY > 256) {
			velocityY = 0;
			surfaceY = 256;
		}
		if(surfaceY < -256 + MAP_H) {
			velocityY = 0;
			surfaceY = -256 + MAP_H;
		}

		int button = Mouse.getEventButton();
		if(draggingSurface && button == 0 && !Mouse.getEventButtonState()) {
			velocityX = (mX - lX) * 0.8F;
			velocityY = (mY - lY) * 0.8F;
			draggingSurface = false;
		}
	}

	@Override
	protected void mouseClickMove(int mouseX, int mouseY, int button, long heldTime) {
		super.mouseClickMove(mouseX, mouseY, button, heldTime);
		if(!landingMode || !draggingSurface) return;

		int deltaX = mouseX - mX;
		int deltaY = mouseY - mY;
		surfaceX += deltaX;
		surfaceY += deltaY;

		lX = mX;
		lY = mY;
		mX = mouseX;
		mY = mouseY;
	}

	@Override
	protected void mouseClicked(int mouseX, int mouseY, int button) {
		super.mouseClicked(mouseX, mouseY, button);

		if(checkClick(mouseX, mouseY, 129, 143, 18, 18)) {
			mc.getSoundHandler().playSound(PositionedSoundRecord.func_147674_a(new ResourceLocation("gui.button.press"), 1.0F));
			NBTTagCompound data = new NBTTagCompound();
			data.setInteger("pid", SpaceConfig.orbitDimension);
			PacketDispatcher.wrapper.sendToServer(new NBTControlPacket(data, star.xCoord, star.yCoord, star.zCoord));
			return;
		}

		if(checkClick(mouseX, mouseY, 129, 123, 18, 18)) {
			mc.getSoundHandler().playSound(PositionedSoundRecord.func_147674_a(new ResourceLocation("gui.button.press"), 1.0F));
			CelestialBody body = CelestialBody.getBody(star.getWorldObj());
			if(body != null && body.getStar() != null) {
				clearSatelliteFocus();
				if(landingMode) {
					exitLandingMode();
				}
				configureFocusAnimation(body, body, true, 420, 0F, 0F, getFocusZoomForBody(body));
			}
			return;
		}

		if(checkClick(mouseX, mouseY, 149, 143, 18, 18)) {
			mc.getSoundHandler().playSound(PositionedSoundRecord.func_147674_a(new ResourceLocation("gui.button.press"), 1.0F));
			NBTTagCompound data = new NBTTagCompound();
			data.setInteger("pid", star.getWorldObj().provider.dimensionId);
			data.setInteger("ix", star.xCoord);
			data.setInteger("iz", star.zCoord);
			PacketDispatcher.wrapper.sendToServer(new NBTControlPacket(data, star.xCoord, star.yCoord, star.zCoord));
			return;
		}

		if(mouseX < guiLeft + MAP_X || mouseX >= guiLeft + MAP_X + MAP_W || mouseY < guiTop + MAP_Y || mouseY >= guiTop + MAP_Y + MAP_H) {
			draggingMap = false;
			draggingSurface = false;
			return;
		}

		if(landingMode) {
			if(button == 1) {
				exitLandingMode();
				return;
			}
			if(button == 0) {
				draggingSurface = true;
				sX = mX = lX = mouseX;
				sY = mY = lY = mouseY;
			}
			return;
		}

		if(button == 1) {
			if(focusedSatelliteFrequency != null) {
				clearSatelliteFocus();
				draggingMap = false;
				return;
			}
			beginUnfocusAnimation();
			draggingMap = false;
			return;
		}

		if(button != 0) {
			return;
		}

		SatelliteRenderInfo clickedSatellite = findSatelliteUnderCursor(mouseX, mouseY);
		if(clickedSatellite != null) {
			focusSatellite(clickedSatellite);
			draggingMap = false;
			return;
		}

		BodyRenderInfo clickedBody = findBodyUnderCursor(mouseX, mouseY);
		if(clickedBody != null) {
			CelestialBody activeFocusBody = getRenderFocusBody();
			if(activeFocusBody != null && clickedBody.body == activeFocusBody) {
				handleFocusedBodySecondClick(clickedBody.body);
				draggingMap = false;
				return;
			}

			if(clickedBody.body.parent != null) {
				startFocusAnimation(clickedBody);
				draggingMap = false;
				return;
			}
		}

		if(isFocusContextActive()) {
			draggingMap = false;
			return;
		}

		draggingMap = true;
		dragLastX = mouseX;
		dragLastY = mouseY;
	}

	private void handleFocusedBodySecondClick(CelestialBody body) {
		if(body == null) return;
		if(!body.canLand) {
			return;
		}

		ItemStack slotStack = inventorySlots.getSlot(0).getStack();
		if(slotStack != null && slotStack.getItem() == ModItems.hard_drive) {
			enterLandingMode(body);
		}
	}

	@Override
	protected void mouseMovedOrUp(int mouseX, int mouseY, int which) {
		super.mouseMovedOrUp(mouseX, mouseY, which);

		if(which == 0) {
			draggingMap = false;
			draggingSurface = false;
		}

		if(!landingMode || which == -1 || which == 1) return;

		if(Math.abs(sX - mouseX) > 2 || Math.abs(sY - mouseY) > 2) return;
		if(mouseX < guiLeft + MAP_X || mouseX >= guiLeft + MAP_X + MAP_W || mouseY < guiTop + MAP_Y || mouseY >= guiTop + MAP_Y + MAP_H) return;
		if(star.heightmap == null) return;

		int hx = (mouseX - (int) surfaceX - guiLeft - MAP_X + 256) / 2;
		int hz = (mouseY - (int) surfaceY - guiTop - MAP_Y + 256) / 2;
		if(landingInfo(hx, hz) != null) return;

		mc.getSoundHandler().playSound(PositionedSoundRecord.func_147674_a(new ResourceLocation("gui.button.press"), 1.0F));
		NBTTagCompound data = new NBTTagCompound();
		data.setInteger("px", hx);
		data.setInteger("pz", hz);
		PacketDispatcher.wrapper.sendToServer(new NBTControlPacket(data, star.xCoord, star.yCoord, star.zCoord));
	}

	private void updateDragPanning(int mouseX, int mouseY) {
		if(!draggingMap) {
			return;
		}

		if(isFocusContextActive() || !Mouse.isButtonDown(0)) {
			draggingMap = false;
			return;
		}

		int dx = mouseX - dragLastX;
		int dy = mouseY - dragLastY;
		if(dx != 0 || dy != 0) {
			panByScreenDelta(dx, dy);
			clampMapUV();
		}

		dragLastX = mouseX;
		dragLastY = mouseY;
	}

	private void panByScreenDelta(int dx, int dy) {
		float zoom = Math.max(0.0001F, mapZoom);
		float normalizedZoom = (MathHelper.clamp_float(mapZoom, MIN_ZOOM, 5.0F) - MIN_ZOOM) / (5.0F - MIN_ZOOM);
		float speed = (0.35F + (1.20F - 0.35F) * normalizedZoom) * 0.75F;

		float a = -dx / (zoom * ISO_X);
		float b = -dy / (zoom * ISO_Y);
		float deltaU = 0.5F * (a + b);
		float deltaV = 0.5F * (b - a);

		mapU += deltaU * speed;
		mapV += deltaV * speed;
	}

	private void startFocusAnimation(BodyRenderInfo bodyInfo) {
		if(bodyInfo == null || bodyInfo.body == null || bodyInfo.body.parent == null) {
			return;
		}
		if(bodyInfo.body == focusedBody) {
			return;
		}
		if(focusAnimationActive && focusAnimationToBody && bodyInfo.body == focusAnimationBody) {
			return;
		}

		clearSatelliteFocus();
		mc.getSoundHandler().playSound(PositionedSoundRecord.func_147674_a(new ResourceLocation("gui.button.press"), 1.0F));
		configureFocusAnimation(bodyInfo.body, bodyInfo.body, true, 500, bodyInfo.mapU, bodyInfo.mapV, getFocusZoomForBody(bodyInfo.body));
	}

	private void beginUnfocusAnimation() {
		if(!hasBodyFocusContext()) {
			return;
		}

		CelestialBody activeFocus = focusedBody != null ? focusedBody : focusAnimationBody;
		CelestialBody parentFocus = activeFocus != null ? activeFocus.parent : null;
		if(parentFocus != null && parentFocus.parent != null) {
			configureFocusAnimation(parentFocus, parentFocus, true, 420, 0F, 0F, getFocusZoomForBody(parentFocus));
			return;
		}

		configureFocusAnimation(null, null, false, 420, MAP_TEX_W * 0.5F, MAP_TEX_H * 0.5F, 1.0F);
	}

	private void configureFocusAnimation(CelestialBody newFocusedBody, CelestialBody animationBody, boolean animationToBody,
										 int durationMs, float targetCenterU, float targetCenterV, float targetZoom) {
		focusedBody = newFocusedBody;
		focusAnimationBody = animationBody;
		focusAnimationToBody = animationToBody;
		focusAnimationActive = true;
		focusAnimDurationMs = durationMs;
		focusAnimStartTimeMs = System.currentTimeMillis();
		focusAnimStartCenterU = mapU + (MAP_W / mapZoom) * 0.5F;
		focusAnimStartCenterV = mapV + (MAP_H / mapZoom) * 0.5F;
		focusAnimStartZoom = mapZoom;
		focusAnimTargetCenterU = targetCenterU;
		focusAnimTargetCenterV = targetCenterV;
		focusAnimTargetZoom = targetZoom;
		focusAnimTrackingInitialized = false;
		focusAnimTrackingOffsetU = 0F;
		focusAnimTrackingOffsetV = 0F;
	}

	private void updateFocusView(CelestialBody starBody, float starCenterU, float starCenterV, float systemOrbitScalePxPerKm, double worldTicks) {
		if(updateFocusedSatelliteView(starBody, starCenterU, starCenterV, systemOrbitScalePxPerKm, worldTicks)) {
			return;
		}

		if(!hasBodyFocusContext()) {
			return;
		}

		CelestialBody trackingBody = focusedBody != null ? focusedBody : focusAnimationBody;
		boolean hasTrackingBodyPosition = false;
		if(trackingBody != null) {
			hasTrackingBodyPosition = findBodyMapPosition(starBody, starCenterU, starCenterV, systemOrbitScalePxPerKm, worldTicks, trackingBody, trackedBodyPosition);
		}

		if(focusAnimationActive) {
			long now = System.currentTimeMillis();
			double duration = Math.max(1D, focusAnimDurationMs);
			float t = MathHelper.clamp_float((float) ((now - focusAnimStartTimeMs) / duration), 0F, 1F);
			float eased = t * t * (3F - 2F * t);

			float centerU;
			float centerV;
			if(focusAnimationToBody && hasTrackingBodyPosition) {
				if(!focusAnimTrackingInitialized) {
					focusAnimTrackingOffsetU = focusAnimStartCenterU - trackedBodyPosition.mapU;
					focusAnimTrackingOffsetV = focusAnimStartCenterV - trackedBodyPosition.mapV;
					focusAnimTrackingInitialized = true;
				}
				float remaining = 1F - eased;
				centerU = trackedBodyPosition.mapU + focusAnimTrackingOffsetU * remaining;
				centerV = trackedBodyPosition.mapV + focusAnimTrackingOffsetV * remaining;
			} else {
				centerU = focusAnimStartCenterU + (focusAnimTargetCenterU - focusAnimStartCenterU) * eased;
				centerV = focusAnimStartCenterV + (focusAnimTargetCenterV - focusAnimStartCenterV) * eased;
			}

			float zoom = focusAnimStartZoom + (focusAnimTargetZoom - focusAnimStartZoom) * eased;
			setViewFromCenter(centerU, centerV, zoom);

			if(t >= 1F) {
				focusAnimationActive = false;
				focusAnimationBody = null;
				focusAnimTrackingInitialized = false;
			}
			return;
		}

		if(focusedBody != null && hasTrackingBodyPosition) {
			setViewFromCenter(trackedBodyPosition.mapU, trackedBodyPosition.mapV, mapZoom);
			return;
		}

		if(focusedBody != null) {
			cancelFocusState();
		}
	}

	private boolean updateFocusedSatelliteView(CelestialBody starBody, float starCenterU, float starCenterV, float systemOrbitScalePxPerKm, double worldTicks) {
		if(focusedSatelliteFrequency == null) {
			return false;
		}

		if(!findSatelliteMapPosition(starBody, starCenterU, starCenterV, systemOrbitScalePxPerKm, worldTicks, focusedSatelliteFrequency.intValue(), trackedSatellitePosition)) {
			clearSatelliteFocus();
			return false;
		}

		setViewFromCenter(trackedSatellitePosition.mapU, trackedSatellitePosition.mapV, mapZoom);
		return true;
	}

	private boolean findBodyMapPosition(CelestialBody body, float bodyMapU, float bodyMapV, float systemOrbitScalePxPerKm,
										double worldTicks, CelestialBody targetBody, BodyPosition outPosition) {
		if(body == targetBody) {
			outPosition.mapU = bodyMapU;
			outPosition.mapV = bodyMapV;
			return true;
		}

		if(body.satellites == null) {
			return false;
		}

		float childOrbitScale = getChildOrbitScalePxPerKm(body, systemOrbitScalePxPerKm);
		for(CelestialBody childBody : body.satellites) {
			if(childBody == null) continue;
			double meanAnomaly = calculateMeanAnomaly(childBody, worldTicks);
			float[] orbitOffset = calculateOrbitOffsetPx(body, childBody, meanAnomaly, childOrbitScale);
			if(findBodyMapPosition(childBody, bodyMapU + orbitOffset[0], bodyMapV + orbitOffset[1], systemOrbitScalePxPerKm, worldTicks, targetBody, outPosition)) {
				return true;
			}
		}

		return false;
	}

	private boolean findSatelliteMapPosition(CelestialBody starBody, float starCenterU, float starCenterV, float systemOrbitScalePxPerKm, double worldTicks, int frequency, BodyPosition outPosition) {
		if(currentBody == null || outPosition == null) {
			return false;
		}

		Map<Integer, Satellite> satellites = SatelliteSavedData.getClientSats();
		if(satellites == null || satellites.isEmpty()) {
			return false;
		}

		Satellite satellite = satellites.get(frequency);
		if(satellite == null) {
			return false;
		}

		if(!findBodyMapPosition(starBody, starCenterU, starCenterV, systemOrbitScalePxPerKm, worldTicks, currentBody, trackedBodyPosition)) {
			return false;
		}

		float baseOrbitRadiusMapPx = getBodySizePxAt1x(currentBody) * SATELLITE_ORBIT_RADIUS_SCALE;
		if(baseOrbitRadiusMapPx <= 0F) {
			return false;
		}

		float satelliteAngle = Satellite.applyPhaseOffsetToOrbitAngle(satellite.phaseOffset, satellite.altitude, getArtificialSatelliteAngle(), (float) (2D * Math.PI));
		SatelliteOrbitPoint orbitPoint = getArtificialSatelliteOrbitPoint(satellite, satelliteAngle, baseOrbitRadiusMapPx);
		outPosition.mapU = trackedBodyPosition.mapU + orbitPoint.offsetU;
		outPosition.mapV = trackedBodyPosition.mapV + orbitPoint.offsetV;
		return true;
	}

	private void zoomAtMouse(int mouseX, int mouseY, float oldZoom, float newZoom) {
		float minZoom = MIN_ZOOM;
		if(oldZoom <= 0F || newZoom <= 0F) {
			mapZoom = MathHelper.clamp_float(newZoom, minZoom, MAX_ZOOM);
			clampMapUV();
			return;
		}

		float mapCenterScreenX = guiLeft + MAP_X + MAP_W * 0.5F;
		float mapCenterScreenY = guiTop + MAP_Y + MAP_H * 0.5F;

		float oldCenterU = mapU + (MAP_W / oldZoom) * 0.5F;
		float oldCenterV = mapV + (MAP_H / oldZoom) * 0.5F;

		float oldA = (mouseX - mapCenterScreenX) / (oldZoom * ISO_X);
		float oldB = (mouseY - mapCenterScreenY) / (oldZoom * ISO_Y);
		float targetMapU = oldCenterU + 0.5F * (oldA + oldB);
		float targetMapV = oldCenterV + 0.5F * (oldB - oldA);

		float newA = (mouseX - mapCenterScreenX) / (newZoom * ISO_X);
		float newB = (mouseY - mapCenterScreenY) / (newZoom * ISO_Y);
		float newCenterU = targetMapU - 0.5F * (newA + newB);
		float newCenterV = targetMapV - 0.5F * (newB - newA);

		mapZoom = MathHelper.clamp_float(newZoom, minZoom, MAX_ZOOM);
		float viewW = MAP_W / mapZoom;
		float viewH = MAP_H / mapZoom;
		mapU = newCenterU - viewW * 0.5F;
		mapV = newCenterV - viewH * 0.5F;
		clampMapUV();
	}

	private CelestialBody getRenderFocusBody() {
		if(focusedBody != null) {
			return focusedBody;
		}
		if(focusAnimationActive && focusAnimationToBody) {
			return focusAnimationBody;
		}
		return null;
	}

	private CelestialBody getLocalOrbitParent() {
		CelestialBody activeFocus = getRenderFocusBody();
		if(activeFocus == null) {
			return null;
		}
		if(isTopLevelPlanet(activeFocus)) {
			return activeFocus;
		}
		if(activeFocus.parent != null) {
			return activeFocus.parent;
		}
		return null;
	}

	private CelestialBody getTopLevelPlanetForBody(CelestialBody body) {
		if(body == null) {
			return null;
		}

		CelestialBody current = body;
		int steps = 0;
		while (current.parent != null && current.parent.parent != null && steps <= 64) {
			current = current.parent;
			steps++;
		}
		return isTopLevelPlanet(current) ? current : null;
	}

	private boolean isTopLevelPlanet(CelestialBody body) {
		return body != null && body.parent != null && body.parent.parent == null;
	}

	private boolean isMoon(CelestialBody body) {
		return body != null && body.parent != null && body.parent.parent != null;
	}

	private void cancelFocusState() {
		focusedBody = null;
		focusAnimationBody = null;
		focusAnimationActive = false;
		focusAnimationToBody = false;
		focusAnimTrackingInitialized = false;
	}

	private void clearSatelliteFocus() {
		focusedSatelliteFrequency = null;
		focusedSatellite = null;
	}

	private void focusSatellite(SatelliteRenderInfo satelliteInfo) {
		if(satelliteInfo == null || satelliteInfo.satellite == null) {
			return;
		}
		if(focusedSatelliteFrequency != null && focusedSatelliteFrequency.intValue() == satelliteInfo.frequency) {
			return;
		}

		cancelFocusState();
		focusedSatelliteFrequency = satelliteInfo.frequency;
		focusedSatellite = satelliteInfo;
		setViewFromCenter(satelliteInfo.mapU, satelliteInfo.mapV, mapZoom);
		mc.getSoundHandler().playSound(PositionedSoundRecord.func_147674_a(new ResourceLocation("gui.button.press"), 1.0F));
	}

	private void drawSatelliteTooltip(int mouseX, int mouseY) {
		if(landingMode) {
			return;
		}

		boolean focusedTooltip = focusedSatelliteFrequency != null && focusedSatellite != null;
		if(!focusedTooltip) {
			if(!isMouseInsideMap(mouseX, mouseY)) {
				return;
			}
			if(!shouldDrawArtificialSatelliteOrbitLines(currentBody)) {
				return;
			}
		}

		SatelliteRenderInfo satelliteInfo = focusedTooltip ? focusedSatellite : hoveredSatellite;
		if(satelliteInfo == null || satelliteInfo.satellite == null) {
			return;
		}

		Satellite satellite = satelliteInfo.satellite;
		String owner = satellite.owner != null && !satellite.owner.isEmpty() ? satellite.owner : Satellite.DEFAULT_OWNER;
		List<String> tooltip = new ArrayList<String>(5);
		tooltip.add(EnumChatFormatting.YELLOW + I18nUtil.resolveKey("item.sat.desc.owner") + ": " + EnumChatFormatting.GOLD + owner);
		tooltip.add(EnumChatFormatting.YELLOW + I18nUtil.resolveKey("item.sat.desc.speed") + ": " + EnumChatFormatting.GOLD + formatOrbitSpeed(satellite.altitude) + I18nUtil.resolveKey("gui.sat.settings.unit.km_per_second"));
		tooltip.add(EnumChatFormatting.YELLOW + I18nUtil.resolveKey("item.sat.desc.altitude") + ": " + EnumChatFormatting.GOLD + formatValue(satellite.altitude) + I18nUtil.resolveKey("gui.sat.settings.unit.km"));
		tooltip.add(EnumChatFormatting.YELLOW + I18nUtil.resolveKey("item.sat.desc.inclination") + ": " + EnumChatFormatting.GOLD + formatValue(satellite.inclination) + "\u00B0");
		tooltip.add(EnumChatFormatting.YELLOW + I18nUtil.resolveKey("item.sat.desc.phase") + ": " + EnumChatFormatting.GOLD + formatPhaseOffset(satellite.phaseOffset) + "\u00B0");

		int tooltipX = mouseX;
		int tooltipY = mouseY;
		if(focusedTooltip) {
			float screenX = mapToScreenX(satelliteInfo.mapU, satelliteInfo.mapV);
			float screenY = mapToScreenY(satelliteInfo.mapU, satelliteInfo.mapV);
			float half = satelliteInfo.drawSize * 0.5F;
			tooltipX = Math.round(screenX + half);
			tooltipY = Math.round(screenY - half);
		}

		func_146283_a(tooltip, tooltipX, tooltipY);
	}

	private boolean isMouseInsideMap(int mouseX, int mouseY) {
		return mouseX >= guiLeft + MAP_X && mouseX < guiLeft + MAP_X + MAP_W && mouseY >= guiTop + MAP_Y && mouseY < guiTop + MAP_Y + MAP_H;
	}

	private boolean shouldDrawArtificialSatelliteOrbitLines(CelestialBody body) {
		CelestialBody activeFocus = getOrbitRuleFocusBody();
		return body != null && activeFocus != null && (activeFocus == body || activeFocus.parent == body);
	}

	private CelestialBody getOrbitRuleFocusBody() {
		CelestialBody activeFocus = getRenderFocusBody();
		if(activeFocus != null) {
			return activeFocus;
		}
		if(focusedSatelliteFrequency != null) {
			return currentBody;
		}
		return null;
	}

	private boolean hasBodyFocusContext() {
		return focusedBody != null || focusAnimationActive;
	}

	private boolean isFocusContextActive() {
		return hasBodyFocusContext() || focusedSatelliteFrequency != null;
	}

	private void setViewFromCenter(float centerU, float centerV, float zoom) {
		mapZoom = MathHelper.clamp_float(zoom, MIN_ZOOM, MAX_ZOOM);
		float viewW = MAP_W / mapZoom;
		float viewH = MAP_H / mapZoom;
		mapU = centerU - viewW * 0.5F;
		mapV = centerV - viewH * 0.5F;
		clampMapUV();
	}

	private float getFocusZoomForBody(CelestialBody body) {
		// If the focused body has satellites, start zoom so all of them are visible.
		if(body != null) {
			float[] focusZoomBounds = new float[2];
			if(calculateFocusedChildZoomBounds(body, focusZoomBounds)) {
				return focusZoomBounds[0];
			}
		}

		float bodySizeAt1x = isMoon(body) ? getMoonSizePxAt1x(body) : getBodySizePxAt1x(body);
		if(bodySizeAt1x <= 0F) {
			return MathHelper.clamp_float(mapZoom, MIN_ZOOM, MAX_ZOOM);
		}

		float targetBodySizePx = Math.min(MAP_W, MAP_H) * 0.88F;
		float targetZoom = targetBodySizePx / bodySizeAt1x;
		targetZoom /= 3.0F;
		targetZoom = Math.max(targetZoom, 4.0F);
		return MathHelper.clamp_float(targetZoom, MIN_ZOOM, MAX_ZOOM);
	}

	private boolean calculateFocusedChildZoomBounds(CelestialBody focusBody, float[] outBounds) {
		if(outBounds == null || outBounds.length < 2 || focusBody == null || focusBody.satellites == null || focusBody.satellites.isEmpty()) {
			return false;
		}

		CelestialBody starBody = focusBody.getStar();
		if(starBody == null) {
			return false;
		}

		float systemOrbitScale = getOrbitScalePxPerKm(starBody);
		if(systemOrbitScale <= 0F) {
			return false;
		}

		float childOrbitScale = getChildOrbitScalePxPerKm(focusBody, systemOrbitScale);
		if(childOrbitScale <= 0F) {
			return false;
		}

		float nearestProjectedDist = Float.MAX_VALUE;
		float furthestProjectedDist = 0F;
		for(CelestialBody childBody : focusBody.satellites) {
			if(childBody == null || childBody.semiMajorAxisKm <= 0F) {
				continue;
			}
			for(int i = 0; i <= 96; i++) {
				double meanAnomaly = 2D * Math.PI * ((double) i / 96D);
				float[] orbitOffset = calculateOrbitOffsetPx(focusBody, childBody, meanAnomaly, childOrbitScale);
				float projected = getProjectedOffsetDistanceAt1x(orbitOffset[0], orbitOffset[1]);
				if(projected <= 0F || Float.isNaN(projected) || Float.isInfinite(projected)) {
					continue;
				}
				nearestProjectedDist = Math.min(nearestProjectedDist, projected);
				furthestProjectedDist = Math.max(furthestProjectedDist, projected);
			}
		}

		if(nearestProjectedDist == Float.MAX_VALUE || furthestProjectedDist <= 0F) {
			return false;
		}

		float availableRadiusPx = Math.max(1F, Math.min(MAP_W, MAP_H) * 0.5F - 10F);
		float minZoom = availableRadiusPx / furthestProjectedDist;
		float maxZoom = availableRadiusPx / nearestProjectedDist;

		minZoom = MathHelper.clamp_float(minZoom, MIN_ZOOM, MAX_ZOOM);
		maxZoom = MathHelper.clamp_float(maxZoom, MIN_ZOOM, MAX_ZOOM);
		if(maxZoom < minZoom) {
			float swap = minZoom;
			minZoom = maxZoom;
			maxZoom = swap;
		}

		outBounds[0] = minZoom;
		outBounds[1] = maxZoom;
		return true;
	}

	private float getProjectedOffsetDistanceAt1x(float offsetU, float offsetV) {
		float screenOffsetX = (offsetU - offsetV) * ISO_X;
		float screenOffsetY = (offsetU + offsetV) * ISO_Y;
		return MathHelper.sqrt_float(screenOffsetX * screenOffsetX + screenOffsetY * screenOffsetY);
	}

	private float getBodyDetailZoomCap(CelestialBody body) {
		if(body == null) {
			return MIN_ZOOM;
		}

		float bodySizeAt1x = isMoon(body) ? getMoonSizePxAt1x(body) : getBodySizePxAt1x(body);
		if(bodySizeAt1x <= 0F || Float.isNaN(bodySizeAt1x) || Float.isInfinite(bodySizeAt1x)) {
			return MIN_ZOOM;
		}

		float targetBodySizePx = Math.min(MAP_W, MAP_H) * 0.88F;
		float detailZoom = targetBodySizePx / bodySizeAt1x;
		return MathHelper.clamp_float(detailZoom, MIN_ZOOM, MAX_ZOOM);
	}

	private void clampMapUV() {
		float viewW = MAP_W / mapZoom;
		float viewH = MAP_H / mapZoom;
		float maxU = Math.max(0F, MAP_TEX_W - viewW);
		float maxV = Math.max(0F, MAP_TEX_H - viewH);
		mapU = MathHelper.clamp_float(mapU, 0F, maxU);
		mapV = MathHelper.clamp_float(mapV, 0F, maxV);
	}

	private float getOrbitScalePxPerKm(CelestialBody starBody) {
		float maxDistanceKm = getSystemMaxDistanceKm(starBody);
		if(maxDistanceKm <= 0F) {
			return 0F;
		}

		float starRadiusPxAt1x = getBodySizePxAt1x(starBody) * 0.5F;
		float availableRadiusPx = Math.max(0F, (SYSTEM_LAYOUT_REF_TEX * STARDAR_SYSTEM_RADIUS_FRACTION) - starRadiusPxAt1x);
		if(availableRadiusPx <= 0F) {
			return 0F;
		}

		return availableRadiusPx / maxDistanceKm;
	}

	private float getSystemMaxDistanceKm(CelestialBody starBody) {
		if(starBody == null || starBody.satellites == null) {
			return 0F;
		}

		float maxDistance = 0F;
		for(CelestialBody child : starBody.satellites) {
			if(child == null) {
				continue;
			}

			float apoapsisKm = child.semiMajorAxisKm * (1F + child.eccentricity);
			maxDistance = Math.max(maxDistance, Math.max(0F, apoapsisKm));
		}

		return maxDistance;
	}

	private float getChildOrbitScalePxPerKm(CelestialBody parent, float systemOrbitScalePxPerKm) {
		if(parent != null && parent.parent != null) {
			float moonOrbitScale = getMoonOrbitScalePxPerKm(parent, systemOrbitScalePxPerKm);
			if(moonOrbitScale > 0F) {
				return moonOrbitScale;
			}
		}
		return systemOrbitScalePxPerKm;
	}

	private float getMoonOrbitScalePxPerKm(CelestialBody parent, float systemOrbitScalePxPerKm) {
		if(parent == null || parent.satellites == null || parent.satellites.isEmpty()) {
			return 0F;
		}

		float maxMoonDistanceKm = 0F;
		for(CelestialBody moon : parent.satellites) {
			if(moon == null) continue;
			float apoapsisKm = moon.semiMajorAxisKm * (1F + moon.eccentricity);
			maxMoonDistanceKm = Math.max(maxMoonDistanceKm, Math.max(0F, apoapsisKm));
		}

		if(maxMoonDistanceKm <= 0F) {
			return 0F;
		}

		float parentRadiusPxAt1x = getBodySizePxAt1x(parent) * 0.5F;
		float moonOrbitRadiusPxAt1x = Math.max(10.0F, parentRadiusPxAt1x * 14F);

		float nearestSiblingGapKm = getNearestSiblingOrbitGapKm(parent);
		if(systemOrbitScalePxPerKm > 0F && nearestSiblingGapKm > 0F) {
			float siblingGapPxAt1x = nearestSiblingGapKm * systemOrbitScalePxPerKm;
			float maxAllowedMoonOrbitRadiusPxAt1x = siblingGapPxAt1x * 0.45F;
			if(maxAllowedMoonOrbitRadiusPxAt1x > 0F) {
				moonOrbitRadiusPxAt1x = Math.min(moonOrbitRadiusPxAt1x, maxAllowedMoonOrbitRadiusPxAt1x);
			}
		}

		return moonOrbitRadiusPxAt1x / maxMoonDistanceKm;
	}

	private float getNearestSiblingOrbitGapKm(CelestialBody body) {
		if(body == null || body.parent == null || body.semiMajorAxisKm <= 0F || body.parent.satellites == null) {
			return 0F;
		}

		float nearestGapKm = Float.MAX_VALUE;
		for(CelestialBody sibling : body.parent.satellites) {
			if(sibling == null || sibling == body || sibling.semiMajorAxisKm <= 0F) {
				continue;
			}
			float gapKm = Math.abs(sibling.semiMajorAxisKm - body.semiMajorAxisKm);
			if(gapKm > 0F) {
				nearestGapKm = Math.min(nearestGapKm, gapKm);
			}
		}

		return nearestGapKm == Float.MAX_VALUE ? 0F : nearestGapKm;
	}

	private float[] calculateOrbitOffsetPx(CelestialBody parent, CelestialBody body, double meanAnomaly, float orbitScalePxPerKm) {
		double eccentricAnomaly = calculateEccentricAnomaly(meanAnomaly, body.eccentricity);
		double semiMinorAxisFactor = body.semiMinorAxisFactor > 0 ? body.semiMinorAxisFactor : Math.sqrt(1D - (body.eccentricity * body.eccentricity));

		double x = body.semiMajorAxisKm * (Math.cos(eccentricAnomaly) - body.eccentricity);
		double y = body.semiMajorAxisKm * semiMinorAxisFactor * Math.sin(eccentricAnomaly);
		double z;

		double px = x;
		x = Math.cos(body.argumentPeriapsis) * px - Math.sin(body.argumentPeriapsis) * y;
		y = Math.sin(body.argumentPeriapsis) * px + Math.cos(body.argumentPeriapsis) * y;

		z = Math.sin(body.inclination) * y;
		y = Math.cos(body.inclination) * y;

		px = x;
		x = Math.cos(body.ascendingNode) * px - Math.sin(body.ascendingNode) * y;
		y = Math.sin(body.ascendingNode) * px + Math.cos(body.ascendingNode) * y;

		y -= z * 0.35D;
		y *= 0.8F;

		float mapX = (float) (x * orbitScalePxPerKm);
		float mapY = (float) (y * orbitScalePxPerKm);

		if(parent != null) {
			float parentVisualRadiusPx = getBodySizePxAt1x(parent) * 0.5F;
			float distanceFromParentPx = MathHelper.sqrt_float(mapX * mapX + mapY * mapY);
			if(distanceFromParentPx > 0F) {
				float radialScale = (distanceFromParentPx + parentVisualRadiusPx) / distanceFromParentPx;
				mapX *= radialScale;
				mapY *= radialScale;
			} else {
				mapX = parentVisualRadiusPx;
				mapY = 0F;
			}
		}

		return new float[]{mapX, mapY};
	}

	private double calculateMeanAnomaly(CelestialBody body, double worldTicks) {
		if(body == null) {
			return 0D;
		}

		double orbitPeriodTicks = getOrbitalPeriodTicks(body);
		if(orbitPeriodTicks <= 0D || Double.isNaN(orbitPeriodTicks) || Double.isInfinite(orbitPeriodTicks)) {
			return 0D;
		}

		return 2D * Math.PI * (worldTicks / orbitPeriodTicks);
	}

	private double getOrbitalPeriodTicks(CelestialBody body) {
		if(body == null || body.parent == null || body.semiMajorAxisKm <= 0F) {
			return 0D;
		}

		double orbitPeriodDays = body.getOrbitalPeriod();
		if(orbitPeriodDays <= 0D || Double.isNaN(orbitPeriodDays) || Double.isInfinite(orbitPeriodDays)) {
			return 0D;
		}

		return orbitPeriodDays * (double) AstronomyUtil.TICKS_IN_DAY;
	}

	private double calculateEccentricAnomaly(double meanAnomaly, float eccentricity) {
		double eccentricAnomaly = meanAnomaly;
		for(int i = 0; i < 4; i++) {
			eccentricAnomaly = meanAnomaly + eccentricity * Math.sin(eccentricAnomaly);
		}
		return eccentricAnomaly;
	}

	private float getBodyRotationPhase(CelestialBody body, double dayTicks) {
		if(body == null) {
			return 0F;
		}

		double period = body.getRotationalPeriod();
		if(period <= 0D || Double.isNaN(period) || Double.isInfinite(period)) {
			return 0F;
		}

		return (float) ((dayTicks % period) / period);
	}

	private float getBodySizePxAtZoom(CelestialBody body) {
		float bodySize = getBodySizePxAt1x(body) * mapZoom;
		CelestialBody renderFocusBody = getRenderFocusBody();
		if(renderFocusBody != null) {
			CelestialBody localOrbitParent = getLocalOrbitParent();
			boolean isLocalMoon = localOrbitParent != null && body.parent == localOrbitParent;
			if(isLocalMoon) {
				float moonSizeAt1x = getMoonSizePxAt1x(body);
				bodySize = moonSizeAt1x * mapZoom;
			}
		}
		return bodySize;
	}

	private float getBodySizePxAt1x(CelestialBody body) {
		if(body == null) {
			return 1F;
		}
		if(body.parent == null) {
			return 36F * STARDAR_BODY_SCALE;
		}
		if(isMoon(body)) {
			return getMoonSizePxAt1x(body);
		}
		float bodySizeAt1x = body.radiusKm * (36F / 261_600F) * 2.6F;
		bodySizeAt1x = MathHelper.clamp_float(bodySizeAt1x, 0.8F, 2.0F);
		return bodySizeAt1x * STARDAR_BODY_SCALE;
	}

	private float getMoonSizePxAt1x(CelestialBody moon) {
		if(moon == null) {
			return 0.2F * STARDAR_BODY_SCALE * STARDAR_MOON_VISUAL_SCALE;
		}
		float moonRadiusKm = Math.max(0F, moon.radiusKm);
		float radiusRangeKm = 500F - 65F;
		float t = (moonRadiusKm - 65F) / radiusRangeKm;
		t = MathHelper.clamp_float(t, 0F, 1F);
		return (0.2F + (0.5F - 0.2F) * t) * STARDAR_BODY_SCALE * STARDAR_MOON_VISUAL_SCALE;
	}

	private void enterLandingMode(CelestialBody body) {
		landingMode = true;
		landingBody = body;
		surfaceX = 0;
		surfaceY = 0;
		velocityX = 0;
		velocityY = 0;

		mc.getSoundHandler().playSound(PositionedSoundRecord.func_147674_a(new ResourceLocation("gui.button.press"), 1.0F));

		NBTTagCompound data = new NBTTagCompound();
		data.setInteger("pid", body.dimensionId);

		if(body == currentBody && star.getWorldObj().provider.dimensionId != SpaceConfig.orbitDimension) {
			data.setInteger("ix", star.xCoord);
			data.setInteger("iz", star.zCoord);
		}

		PacketDispatcher.wrapper.sendToServer(new NBTControlPacket(data, star.xCoord, star.yCoord, star.zCoord));
	}

	private void exitLandingMode() {
		landingMode = false;
		landingBody = null;
		draggingSurface = false;
	}

	private void updateLandingModeFromDrive() {
		ItemStack slotStack = inventorySlots.getSlot(0).getStack();

		if(slotStack == null) {
			hadFullDrive = false;
			lastFullDriveMeta = -1;
			if(landingMode) {
				exitLandingMode();
			}
			return;
		}

		if(slotStack.getItem() != ModItems.full_drive) {
			hadFullDrive = false;
			lastFullDriveMeta = -1;
			if(landingMode && slotStack.getItem() != ModItems.hard_drive) {
				exitLandingMode();
			}
			return;
		}

		int meta = slotStack.getItemDamage();
		if(hadFullDrive && meta == lastFullDriveMeta) return;

		hadFullDrive = true;
		lastFullDriveMeta = meta;

		CelestialBody body = ItemVOTVdrive.getBody(slotStack).getBody();
		if(landingMode && landingBody == body) return;

		if(body != null && body.canLand) {
			enterLandingMode(body);
		} else if(landingMode) {
			exitLandingMode();
		}
	}

	private String landingInfo(int x, int z) {
		if(star.heightmap == null) return I18nUtil.resolveKey("gui.stardar.landing.no_heightmap");
		if(x < 3 || x > 252 || z < 3 || z > 252) return I18nUtil.resolveKey("gui.stardar.landing.outside_bounds");

		for(int ox = x - 2; ox <= x + 2; ox++) {
			for(int oz = z - 2; oz <= z + 2; oz++) {
				if(star.heightmap[256 * oz + ox] != star.heightmap[256 * z + x]) {
					return I18nUtil.resolveKey("gui.stardar.landing.area_not_flat");
				}
			}
		}

		return null;
	}

	private int altitude(int x, int z) {
		if(star.heightmap == null) return -1;
		if(x < 0 || x > 255 || z < 0 || z > 255) return -1;
		return star.heightmap[256 * z + x];
	}

	private String getBodyDisplayName(CelestialBody body) {
		if(body == null || body.name == null) return I18nUtil.resolveKey("gui.stardar.body.unknown");
		String key = "body." + body.name;
		String translated = I18nUtil.resolveKey(key);
		if(translated != null && !translated.equals(key)) return translated;
		if(body.name.length() <= 1) return body.name.toUpperCase();
		return body.name.substring(0, 1).toUpperCase() + body.name.substring(1);
	}

	private static String formatValue(float value) {
		return value == (int) value ? Integer.toString((int) value) : Float.toString(value);
	}

	private static String formatOrbitSpeed(float altitude) {
		return formatValue(Math.round(Satellite.getOrbitSpeedKmPerSecond(altitude) * 10.0F) / 10.0F);
	}

	private static String formatPhaseOffset(float phaseOffset) {
		float rounded = Math.round(Satellite.normalizePhaseOffset(phaseOffset) * 10.0F) / 10.0F;
		return formatValue(rounded);
	}

	private int colorArrayToRgb(float[] color) {
		if(color == null || color.length < 3) {
			return 0xFFFFFFFF;
		}
		int r = MathHelper.clamp_int((int) (color[0] * 255F), 0, 255);
		int g = MathHelper.clamp_int((int) (color[1] * 255F), 0, 255);
		int b = MathHelper.clamp_int((int) (color[2] * 255F), 0, 255);
		return 0xFF000000 | r << 16 | g << 8 | b;
	}

	private int getBodyHierarchyDepth(CelestialBody body) {
		if(body == null) {
			return 0;
		}

		int depth = 0;
		CelestialBody current = body;
		while (current.parent != null && depth < 64) {
			depth++;
			current = current.parent;
		}
		return depth;
	}

	private static class BodyPosition {
		float mapU;
		float mapV;
	}

	private static class ChildRenderState {
		final CelestialBody childBody;
		final float orbitScalePxPerKm;
		final float orbitOffsetU;
		final float orbitOffsetV;
		final boolean moonChild;
		final boolean drawInFrontOfParent;

		ChildRenderState(CelestialBody childBody, float orbitScalePxPerKm, float orbitOffsetU, float orbitOffsetV, boolean moonChild, boolean drawInFrontOfParent) {
			this.childBody = childBody;
			this.orbitScalePxPerKm = orbitScalePxPerKm;
			this.orbitOffsetU = orbitOffsetU;
			this.orbitOffsetV = orbitOffsetV;
			this.moonChild = moonChild;
			this.drawInFrontOfParent = drawInFrontOfParent;
		}
	}

	private static class SatelliteOrbitPoint {
		final float offsetU;
		final float offsetV;
		final float depth;

		SatelliteOrbitPoint(float offsetU, float offsetV, float depth) {
			this.offsetU = offsetU;
			this.offsetV = offsetV;
			this.depth = depth;
		}
	}

	private static class BodyRenderInfo {
		final CelestialBody body;
		final float mapU;
		final float mapV;
		final float drawSize;

		BodyRenderInfo(CelestialBody body, float mapU, float mapV, float drawSize) {
			this.body = body;
			this.mapU = mapU;
			this.mapV = mapV;
			this.drawSize = drawSize;
		}
	}

	private static class SatelliteRenderInfo {
		final int frequency;
		final Satellite satellite;
		final float mapU;
		final float mapV;
		final float drawSize;

		SatelliteRenderInfo(int frequency, Satellite satellite, float mapU, float mapV, float drawSize) {
			this.frequency = frequency;
			this.satellite = satellite;
			this.mapU = mapU;
			this.mapV = mapV;
			this.drawSize = drawSize;
		}
	}

	private static class BodyLabelRenderInfo {
		final CelestialBody body;
		final float topRightAnchorScreenX;
		final float topRightAnchorScreenY;

		BodyLabelRenderInfo(CelestialBody body, float topRightAnchorScreenX, float topRightAnchorScreenY) {
			this.body = body;
			this.topRightAnchorScreenX = topRightAnchorScreenX;
			this.topRightAnchorScreenY = topRightAnchorScreenY;
		}
	}
}
