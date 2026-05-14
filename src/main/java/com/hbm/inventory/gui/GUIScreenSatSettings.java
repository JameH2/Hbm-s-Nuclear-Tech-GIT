package com.hbm.inventory.gui;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;

import com.hbm.dim.CelestialBody;
import com.hbm.dim.trait.CBT_Impact;
import com.hbm.dim.trait.CBT_Lights;
import com.hbm.handler.CelestialNukeShockHandler;
import com.hbm.lib.RefStrings;
import com.hbm.main.NTMSounds;
import com.hbm.packet.PacketDispatcher;
import com.hbm.packet.toserver.NBTItemControlPacket;
import com.hbm.render.shader.Shader;
import com.hbm.render.util.AtmosphereRenderUtil;
import com.hbm.saveddata.SatelliteSavedData;
import com.hbm.saveddata.satellites.Satellite;
import com.hbm.saveddata.satellites.SatelliteFoeq;
import com.hbm.saveddata.satellites.SatelliteLaser;
import com.hbm.saveddata.satellites.SatelliteMapper;
import com.hbm.saveddata.satellites.SatelliteMiner;
import com.hbm.saveddata.satellites.SatelliteRadar;
import com.hbm.saveddata.satellites.SatelliteResonator;
import com.hbm.saveddata.satellites.SatelliteScanner;
import com.hbm.util.AstronomyUtil;
import com.hbm.util.i18n.I18nUtil;

import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.resources.IResource;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;

import javax.imageio.ImageIO;

public class GUIScreenSatSettings extends GuiScreen {

	private static final ResourceLocation texture = new ResourceLocation(RefStrings.MODID + ":textures/gui/satellites/gui_sat_settings.png");
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
		new ResourceLocation(RefStrings.MODID, "textures/misc/space/citylights_3.png")
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

	private final EntityPlayer player;
	private final Map<ResourceLocation, Boolean> textureAlphaCache = new HashMap<ResourceLocation, Boolean>();
	private int guiLeft;
	private int guiTop;
	private int draggedSlider = -1;
	private boolean hasPendingChanges;
	private String editOwner;
	private float editAltitude;
	private float editInclination;
	private float editPhaseOffset;
	private boolean editBlinking;
	private float editBlinkPeriod;
	private int editColorR;
	private int editColorG;
	private int editColorB;
	private boolean showSatelliteDetails = true;
	private int detailsMode = 0;

	public GUIScreenSatSettings(EntityPlayer player) {
		this.player = player;
	}

	@Override
	public void initGui() {
		super.initGui();
		mc.getSoundHandler().playSound(PositionedSoundRecord.func_147674_a(new ResourceLocation(NTMSounds.BLOCK_FALLOUT_3_POPUP), 1.0F));
		guiLeft = (width - 134) / 2;
		guiTop = (height - 221) / 2;
		loadEditableValues();
	}

	@Override
	public void updateScreen() {
		if(getHeldSatellite() == null) {
			player.closeScreen();
		}
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		drawDefaultBackground();
		GL11.glColor4f(1F, 1F, 1F, 1F);
		Minecraft.getMinecraft().getTextureManager().bindTexture(texture);
		func_146110_a(guiLeft, guiTop, 0, 0, 134, 221, 152, 221);

		ItemStack held = getHeldSatellite();
		if(held == null) return;

		if(showSatelliteDetails) {
			drawBatterySlice();
			drawOrbitPreview(held, partialTicks);
			if(detailsMode == 0) {
				drawLeftAligned(10, 130, 140, I18nUtil.resolveKey("item.sat.desc.speed") + ": " + formatOrbitSpeed(editAltitude) + I18nUtil.resolveKey("gui.sat.settings.unit.km_per_second"), 0x00FF00);
				drawLeftAligned(10, 145, 155, I18nUtil.resolveKey("item.sat.desc.altitude") + ": " + formatValue(editAltitude) + I18nUtil.resolveKey("gui.sat.settings.unit.km"), 0x00FF00);
				drawLeftAligned(10, 160, 170, I18nUtil.resolveKey("item.sat.desc.inclination") + ": " + formatValue(editInclination) + "\u00B0", 0x00FF00);
			} else {
				drawLeftAligned(10, 130, 140, I18nUtil.resolveKey("gui.sat.settings.label.orbital_phase") + ": " + formatPhaseOffset(editPhaseOffset) + "\u00B0", 0x00FF00);
				drawLeftAligned(10, 145, 155, I18nUtil.resolveKey("item.sat.desc.owner") + ": " + (editOwner != null ? editOwner : Satellite.DEFAULT_OWNER), 0x00FF00);
				drawLeftAligned(10, 160, 170, "", 0x00FF00);
			}
			drawRect(guiLeft + 81, guiTop + 177, guiLeft + 110, guiTop + 199, 0xFF000000 | (editColorR << 16) | (editColorG << 8) | editColorB);
			drawRightAligned(108, 204, 213, formatValue(editBlinkPeriod) + I18nUtil.resolveKey("gui.sat.settings.unit.seconds"), 0xFFFFFF, 2F / 3F);
		}

		Minecraft.getMinecraft().getTextureManager().bindTexture(texture);
		GL11.glColor4f(1F, 1F, 1F, 1F);
		drawSlider(editColorR, 180, 17);
		drawSlider(editColorG, 187, 24);
		drawSlider(editColorB, 194, 31);
		if(showSatelliteDetails && editBlinking) {
			func_146110_a(guiLeft + 114, guiTop + 202, 136, 3, 12, 12, 152, 221);
		}

		if(showSatelliteDetails && isOwnerButtonAt(mouseX, mouseY)) {
			drawCreativeTabHoveringText(I18nUtil.resolveKey("gui.sat.settings.tooltip.owner_assign"), mouseX, mouseY);
		} else if(showSatelliteDetails && isModeButtonAt(mouseX, mouseY)) {
			drawCreativeTabHoveringText(I18nUtil.resolveKey("gui.sat.settings.tooltip.switch_settings_list"), mouseX, mouseY);
		} else if(showSatelliteDetails) {
			int scrollField = getScrollFieldAt(mouseX, mouseY);
			if(scrollField == 0) {
				if(detailsMode == 0) {
					drawCreativeTabHoveringText(I18nUtil.resolveKey("gui.sat.settings.tooltip.scroll_speed"), mouseX, mouseY);
				} else {
					drawCreativeTabHoveringText(I18nUtil.resolveKey("gui.sat.settings.tooltip.scroll_phase"), mouseX, mouseY);
				}
			} else if(scrollField == 1) {
				drawCreativeTabHoveringText(I18nUtil.resolveKey("gui.sat.settings.tooltip.scroll_altitude"), mouseX, mouseY);
			} else if(scrollField == 2) {
				drawCreativeTabHoveringText(I18nUtil.resolveKey("gui.sat.settings.tooltip.scroll_inclination"), mouseX, mouseY);
			} else if(scrollField == 3) {
				drawCreativeTabHoveringText(I18nUtil.resolveKey("gui.sat.settings.tooltip.scroll_blink"), mouseX, mouseY);
			}
		}
	}

	@Override
	public boolean doesGuiPauseGame() {
		return false;
	}

	@Override
	protected void keyTyped(char c, int key) {
		if(key == 1 || key == mc.gameSettings.keyBindInventory.getKeyCode()) {
			mc.thePlayer.closeScreen();
			return;
		}

		super.keyTyped(c, key);
	}

	@Override
	protected void mouseClicked(int mouseX, int mouseY, int button) {
		super.mouseClicked(mouseX, mouseY, button);

		if(button != 0) return;
		if(getHeldSatellite() == null) return;

		if(isBatteryToggleAt(mouseX, mouseY)) {
			boolean removeBattery = showSatelliteDetails;
			mc.getSoundHandler().playSound(PositionedSoundRecord.func_147674_a(new ResourceLocation(RefStrings.MODID, removeBattery ? "item.unpackRemove" : "item.unpackInsert"), 1.0F));
			showSatelliteDetails = !showSatelliteDetails;
			return;
		}

		if(isBlinkButtonAt(mouseX, mouseY)) {
			mc.getSoundHandler().playSound(PositionedSoundRecord.func_147674_a(new ResourceLocation("gui.button.press"), 1.0F));
			editBlinking = !editBlinking;
			markDirty();
			return;
		}

		if(isOwnerButtonAt(mouseX, mouseY)) {
			mc.getSoundHandler().playSound(PositionedSoundRecord.func_147674_a(new ResourceLocation("gui.button.press"), 1.0F));
			String owner = player.getCommandSenderName();
			if(owner.equals(editOwner)) return;
			editOwner = owner;
			markDirty();
			return;
		}

		if(isModeButtonAt(mouseX, mouseY)) {
			mc.getSoundHandler().playSound(PositionedSoundRecord.func_147674_a(new ResourceLocation("gui.button.press"), 1.0F));
			detailsMode = detailsMode == 0 ? 1 : 0;
			return;
		}

		draggedSlider = getSliderAt(mouseX, mouseY);
		if(draggedSlider >= 0) {
			updateSlider(draggedSlider, mouseX);
		}
	}

	@Override
	protected void mouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick) {
		super.mouseClickMove(mouseX, mouseY, clickedMouseButton, timeSinceLastClick);

		if(clickedMouseButton == 0 && draggedSlider >= 0) {
			updateSlider(draggedSlider, mouseX);
		}
	}

	@Override
	protected void mouseMovedOrUp(int mouseX, int mouseY, int button) {
		super.mouseMovedOrUp(mouseX, mouseY, button);

		if(button == 0 && draggedSlider >= 0) {
			draggedSlider = -1;
		}
	}

	@Override
	public void handleMouseInput() {
		super.handleMouseInput();

		if(mc == null || Mouse.getEventButton() != -1) return;

		int scroll = Mouse.getEventDWheel();
		if(scroll == 0) return;

		int mouseX = Mouse.getEventX() * width / mc.displayWidth;
		int mouseY = height - Mouse.getEventY() * height / mc.displayHeight - 1;
		int scrollField = getScrollFieldAt(mouseX, mouseY);
		if(scrollField < 0) return;

		adjustScrollField(scrollField, scroll > 0 ? 1 : -1);
	}

	private void drawLeftAligned(int x, int y1, int y2, String text, int color) {
		drawScaledString(text, guiLeft + x, getCenteredY(y1, y2, 2F / 3F), color, 2F / 3F);
	}

	private void drawRightAligned(int x, int y1, int y2, String text, int color, float scale) {
		drawScaledString(text, guiLeft + x - Math.round(fontRendererObj.getStringWidth(text) * scale), getCenteredY(y1, y2, scale), color, scale);
	}

	private int getCenteredY(int y1, int y2, float scale) {
		return guiTop + y1 + ((y2 - y1 + 1) - Math.round(fontRendererObj.FONT_HEIGHT * scale)) / 2;
	}

	private void drawScaledString(String text, int x, int y, int color, float scale) {
		GL11.glPushMatrix();
		GL11.glScalef(scale, scale, 1F);
		fontRendererObj.drawString(text, Math.round(x / scale), Math.round(y / scale), color);
		GL11.glPopMatrix();
	}

	private void drawSlider(int value, int y, int v) {
		func_146110_a(guiLeft + 12 + Math.round(value * 62 / 255F), guiTop + y - 2, 136, v, 2, 6, 152, 221);
	}

	private int getSliderAt(int mouseX, int mouseY) {
		int x = mouseX - guiLeft;
		int y = mouseY - guiTop;

		if(x < 12 || x >= 12 + 64) return -1;
		if(y >= 180 - 2 && y < 180 - 2 + 6) return 0;
		if(y >= 187 - 2 && y < 187 - 2 + 6) return 1;
		if(y >= 194 - 2 && y < 194 - 2 + 6) return 2;
		return -1;
	}

	private void drawBatterySlice() {
		float u1 = 136F / 152F;
		float v1 = 38F / 221F;
		float u2 = 149F / 152F;
		float v2 = 67F / 221F;
		float x = guiLeft + 15;
		float y = guiTop + 200;
		drawPartialTexRotated90(x, y, 29F, 13F, u1, v1, u2, v2);
	}

	private void drawPartialTexRotated90(float x, float y, float w, float h, float u1, float v1, float u2, float v2) {
		Tessellator tess = Tessellator.instance;
		tess.startDrawingQuads();
		tess.addVertexWithUV(x, y + h, this.zLevel, u2, v2);
		tess.addVertexWithUV(x + w, y + h, this.zLevel, u2, v1);
		tess.addVertexWithUV(x + w, y, this.zLevel, u1, v1);
		tess.addVertexWithUV(x, y, this.zLevel, u1, v2);
		tess.draw();
	}

	private void updateSlider(int slider, int mouseX) {
		ItemStack held = getHeldSatellite();
		if(held == null) return;

		int value = Math.round(MathHelper.clamp_int(mouseX - guiLeft - 12, 0, 62) * 255F / 62);

		if(slider == 0) {
			if(editColorR == value) return;
			editColorR = value;
			markDirty();
			return;
		}
		if(slider == 1) {
			if(editColorG == value) return;
			editColorG = value;
			markDirty();
			return;
		}
		if(slider == 2) {
			if(editColorB == value) return;
			editColorB = value;
			markDirty();
		}
	}

	private void adjustScrollField(int field, int delta) {
		if(getHeldSatellite() == null) return;

		if(detailsMode != 0) {
			if(field == 0) adjustPhaseOffset(delta);
			return;
		}

		switch(field) {
		case 0: adjustSpeed(delta); break;
		case 1: adjustAltitude(delta); break;
		case 2: adjustInclination(delta); break;
		case 3: adjustBlinkPeriod(delta); break;
		}
	}

	private void adjustAltitude(int delta) {
		float oldValue = editAltitude;
		float step = isShiftKeyDown() ? 5.0F : 1.0F;
		float newValue = MathHelper.clamp_float(oldValue + (delta > 0 ? step : -step),Satellite.MIN_ALTITUDE_KM, Satellite.MAX_ALTITUDE_KM);

		if(newValue == oldValue) return;

		editAltitude = newValue;
		markDirty();
	}

	private void adjustInclination(int delta) {
		float oldValue = editInclination;
		float step = isShiftKeyDown() ? 15.0F : 1.0F;
		float newValue = MathHelper.clamp_float(oldValue + (delta > 0 ? step : -step), Satellite.MIN_INCLINATION, Satellite.MAX_INCLINATION);

		if(newValue == oldValue) return;

		editInclination = newValue;
		markDirty();
	}

	private void adjustSpeed(int delta) {
		// speed is just the inverse of the altitude
		adjustAltitude(-delta);
	}

	private void adjustPhaseOffset(int delta) {
		float oldValue = editPhaseOffset;
		float step = isShiftKeyDown() ? 15.0F : 1.0F;
		float newValue = Satellite.normalizePhaseOffset(oldValue + (delta > 0 ? step : -step));
		if(Math.abs(newValue - oldValue) < 0.0005F) return;

		editPhaseOffset = newValue;
		markDirty();
	}

	private void adjustBlinkPeriod(int delta) {
		float oldValue = editBlinkPeriod;
		float newValue = Math.round((oldValue + (delta > 0 ? 0.1F : -0.1F)) * 10F) / 10F;

		newValue = Satellite.clampBlinkPeriod(newValue);
		if(newValue == oldValue) return;

		editBlinkPeriod = newValue;
		markDirty();
	}

	private int getScrollFieldAt(int mouseX, int mouseY) {
		int x = mouseX - guiLeft;
		int y = mouseY - guiTop;

		if(x >= 9 && x < 111) {
			if(y >= 130 && y <= 140) return 0;
			if(detailsMode == 0) {
				if(y >= 145 && y <= 155) return 1;
				if(y >= 160 && y <= 170) return 2;
			}
		}
		if(x >= 81 && x < 111 && y >= 204 && y <= 213) return 3;
		return -1;
	}

	private boolean isBlinkButtonAt(int mouseX, int mouseY) {
		int x = mouseX - guiLeft;
		int y = mouseY - guiTop;
		return x >= 114 && x < 127 && y >= 202 && y < 215;
	}

	private boolean isBatteryToggleAt(int mouseX, int mouseY) {
		int x = mouseX - guiLeft;
		int y = mouseY - guiTop;
		return x >= 15 && x < 44 && y >= 200 && y < 213;
	}

	private boolean isOwnerButtonAt(int mouseX, int mouseY) {
		int x = mouseX - guiLeft;
		int y = mouseY - guiTop;
		return x >= 113 && x < 126 && y >= 128 && y < 141;
	}

	private boolean isModeButtonAt(int mouseX, int mouseY) {
		int x = mouseX - guiLeft;
		int y = mouseY - guiTop;
		return x >= 113 && x < 126 && y >= 143 && y < 156;
	}

	private ItemStack getHeldSatellite() {
		ItemStack held = player.getHeldItem();
		return held != null && Satellite.isSatelliteItem(held.getItem()) ? held : null;
	}

	private void loadEditableValues() {
		ItemStack held = getHeldSatellite();
		if(held == null) return;

		hasPendingChanges = false;
		editOwner = Satellite.getOwner(held);
		editAltitude = Satellite.getAltitude(held);
		editInclination = Satellite.getInclination(held);
		editPhaseOffset = Satellite.getPhaseOffset(held);
		editBlinking = Satellite.isBlinking(held);
		editBlinkPeriod = Satellite.getBlinkPeriod(held);
		editColorR = toColorChannel(Satellite.getColorR(held));
		editColorG = toColorChannel(Satellite.getColorG(held));
		editColorB = toColorChannel(Satellite.getColorB(held));
	}

	private void markDirty() {
		hasPendingChanges = true;
	}

	private void applyEditableValues(ItemStack held) {
		Satellite.setOwner(held, editOwner);
		Satellite.setAltitude(held, editAltitude);
		Satellite.setInclination(held, editInclination);
		Satellite.setPhaseOffset(held, editPhaseOffset);
		Satellite.setBlinking(held, editBlinking);
		Satellite.setBlinkPeriod(held, editBlinkPeriod);
		Satellite.setColor(held, editColorR / 255F, editColorG / 255F, editColorB / 255F);
	}

	private NBTTagCompound buildControlData() {
		NBTTagCompound data = new NBTTagCompound();
		data.setString("satOwner", editOwner);
		data.setFloat("satAltitude", editAltitude);
		data.setFloat("satInclination", editInclination);
		data.setFloat("satPhaseOffset", editPhaseOffset);
		data.setBoolean("satIsBlinking", editBlinking);
		data.setFloat("satBlink", editBlinkPeriod);
		data.setInteger("satColorR", editColorR);
		data.setInteger("satColorG", editColorG);
		data.setInteger("satColorB", editColorB);
		return data;
	}

	@Override
	public void onGuiClosed() {
		if(hasPendingChanges) {
			ItemStack held = getHeldSatellite();
			if(held != null) {
				applyEditableValues(held);
				PacketDispatcher.wrapper.sendToServer(new NBTItemControlPacket(buildControlData()));
			}
		}
		super.onGuiClosed();
	}

	private void drawOrbitPreview(ItemStack held, float partialTicks) {
		CelestialBody body = getPreviewBody(held);
		float bodySizeAt1x = getBodySizePxAt1x(body);
		float baseOrbitRadiusMapPx = bodySizeAt1x * 1.5F;
		Map<Integer, Satellite> satellites = SatelliteSavedData.getClientSats(body.dimensionId);
		String owner = editOwner;
		float maxAltitude = editAltitude;

		for(Satellite satellite : satellites.values()) {
			if(owner.equals(satellite.owner)) {
				maxAltitude = Math.max(maxAltitude, satellite.altitude);
			}
		}

		float renderZoom = getPreviewZoom(bodySizeAt1x, baseOrbitRadiusMapPx, maxAltitude) * 2.0F;
		float centerX = guiLeft + 9 + 116 * 0.5F;
		float centerY = guiTop + 8 + 116 * 0.5F;
		float bodySize = MathHelper.clamp_float(bodySizeAt1x * renderZoom, 8F, 96F);
		float iconSize = MathHelper.clamp_float(bodySize * 0.75F * 0.25F, 0.4F, 9.0F);
		double angle = getArtificialSatelliteAngle();

		float heldAltitude = editAltitude;
		float heldInclination = editInclination;
		float heldPhaseOffset = editPhaseOffset;
		float heldR = editColorR / 255F;
		float heldG = editColorG / 255F;
		float heldB = editColorB / 255F;
		ResourceLocation heldTexture = getSatelliteTextureByType(Satellite.itemToClass.get(held.getItem()));

		double dayTicks = mc.theWorld.getTotalWorldTime() + partialTicks;

		pushScissor(9, 8, 116, 116);
		drawStarmapBackground();

		GL11.glPushAttrib(GL11.GL_ENABLE_BIT | GL11.GL_COLOR_BUFFER_BIT | GL11.GL_LINE_BIT);
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		GL11.glDisable(GL11.GL_ALPHA_TEST);

		drawOwnedSatellites(satellites, owner, centerX, centerY, baseOrbitRadiusMapPx, renderZoom, angle, iconSize, false);
		float heldBlinkAlpha = getBlinkAlpha(editBlinking, editBlinkPeriod);
		drawSatelliteOrbitHalf(centerX, centerY, baseOrbitRadiusMapPx, renderZoom, heldAltitude, heldInclination, heldR, heldG, heldB, false, 0.45F * heldBlinkAlpha);
		drawSatelliteIcon(heldTexture, centerX, centerY, baseOrbitRadiusMapPx, renderZoom, heldPhaseOffset, heldAltitude, heldInclination, angle, false, iconSize * 1.2F);
		drawBodyPreview(body, centerX, centerY, bodySize, dayTicks);

		drawOwnedSatellites(satellites, owner, centerX, centerY, baseOrbitRadiusMapPx, renderZoom, angle, iconSize, true);
		drawSatelliteOrbitHalf(centerX, centerY, baseOrbitRadiusMapPx, renderZoom, heldAltitude, heldInclination, heldR, heldG, heldB, true, 0.45F * heldBlinkAlpha);
		drawSatelliteIcon(heldTexture, centerX, centerY, baseOrbitRadiusMapPx, renderZoom, heldPhaseOffset, heldAltitude, heldInclination, angle, true, iconSize * 1.2F);
		GL11.glPopAttrib();

		GL11.glDisable(GL11.GL_SCISSOR_TEST);
		GL11.glColor4f(1F, 1F, 1F, 1F);
	}

	private void drawStarmapBackground() {
		float bgSrcW = 116;
		float bgSrcH = 116;
		float bgU = MathHelper.clamp_float(1024F * 0.5F - bgSrcW * 0.5F, 0F, 1024F - bgSrcW);
		float bgV = MathHelper.clamp_float(1024F * 0.5F - bgSrcH * 0.5F, 0F, 1024F - bgSrcH);
		float bgScaleU = 1F;
		float bgScaleV = 1F;
		float bgUTex = bgU * bgScaleU;
		float bgVTex = bgV * bgScaleV;
		float bgSrcWTex = bgSrcW * bgScaleU;
		float bgSrcHTex = bgSrcH * bgScaleV;

		mc.getTextureManager().bindTexture(starmapTexture);
		drawPartialTex(
			guiLeft + 9,
			guiTop + 8,
			116,
			116,
			bgUTex / 1024F,
			bgVTex / 1024F,
			(bgUTex + bgSrcWTex) / 1024F,
			(bgVTex + bgSrcHTex) / 1024F
		);
	}

	private void drawOwnedSatellites(Map<Integer, Satellite> satellites, String owner, float centerX, float centerY, float baseOrbitRadiusMapPx, float zoom, double angle, float iconSize, boolean frontHalf) {
		for(Satellite satellite : satellites.values()) {
			if(!owner.equals(satellite.owner)) continue;
			float blinkAlpha = getBlinkAlpha(satellite.isBlinking, satellite.blinkPeriod);

			drawSatelliteOrbitHalf(centerX, centerY, baseOrbitRadiusMapPx, zoom, satellite.altitude, satellite.inclination, satellite.colorR, satellite.colorG, satellite.colorB, frontHalf, 0.25F * blinkAlpha);
		}

		for(Map.Entry<Integer, Satellite> entry : satellites.entrySet()) {
			Satellite satellite = entry.getValue();
			if(!owner.equals(satellite.owner)) continue;

			drawSatelliteIcon(getSatelliteTextureByType(satellite.getClass()), centerX, centerY, baseOrbitRadiusMapPx, zoom, satellite.phaseOffset, satellite.altitude, satellite.inclination, angle, frontHalf, iconSize);
		}
	}

	private void drawSatelliteOrbitHalf(float centerX, float centerY, float baseRadiusMapPx, float zoom, float altitude, float inclination, float r, float g, float b, boolean frontHalf, float alpha) {
		GL11.glDisable(GL11.GL_TEXTURE_2D);
		GL11.glLineWidth(1F);

		Tessellator tess = Tessellator.instance;
		float lineR = MathHelper.clamp_float(r, 0F, 1F);
		float lineG = MathHelper.clamp_float(g, 0F, 1F);
		float lineB = MathHelper.clamp_float(b, 0F, 1F);

		boolean hasPrev = false;
		float prevX = 0F;
		float prevY = 0F;
		float prevDepth = 0F;
		boolean prevInFrontHalf = false;
		boolean drawing = false;

		for(int i = 0; i <= 64; i++) {
			float orbitAngle = (float) (2D * Math.PI * ((double) i / 64D));
			SatelliteOrbitPoint orbitPoint = getArtificialSatelliteOrbitPoint(altitude, inclination, orbitAngle, baseRadiusMapPx);
			float currX = mapToScreenX(centerX, orbitPoint.offsetU, orbitPoint.offsetV, zoom);
			float currY = mapToScreenY(centerY, orbitPoint.offsetU, orbitPoint.offsetV, zoom);
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
					tess.startDrawing(GL11.GL_LINE_STRIP);
					tess.setColorRGBA_F(lineR, lineG, lineB, alpha);
					tess.addVertex(prevX, prevY, this.zLevel);
					drawing = true;
				}
				tess.addVertex(currX, currY, this.zLevel);
			} else if(prevSelected != currSelected) {
				float depthDelta = currDepth - prevDepth;
				float t = depthDelta == 0F ? 0.5F : -prevDepth / depthDelta;
				t = MathHelper.clamp_float(t, 0F, 1F);
				float crossX = prevX + (currX - prevX) * t;
				float crossPointY = prevY + (currY - prevY) * t;

				if(prevSelected) {
					if(!drawing) {
						tess.startDrawing(GL11.GL_LINE_STRIP);
						tess.setColorRGBA_F(lineR, lineG, lineB, alpha);
						tess.addVertex(prevX, prevY, this.zLevel);
						drawing = true;
					}
					tess.addVertex(crossX, crossPointY, this.zLevel);
					tess.draw();
					drawing = false;
				} else {
					tess.startDrawing(GL11.GL_LINE_STRIP);
					tess.setColorRGBA_F(lineR, lineG, lineB, alpha);
					tess.addVertex(crossX, crossPointY, this.zLevel);
					tess.addVertex(currX, currY, this.zLevel);
					drawing = true;
				}
			}

			prevX = currX;
			prevY = currY;
			prevDepth = currDepth;
			prevInFrontHalf = currInFrontHalf;
		}

		if(drawing) tess.draw();
		GL11.glEnable(GL11.GL_TEXTURE_2D);
		GL11.glColor4f(1F, 1F, 1F, 1F);
	}

	private void drawSatelliteIcon(ResourceLocation texture, float centerX, float centerY, float baseRadiusMapPx, float zoom, float phaseOffset, float altitude, float inclination, double angle, boolean frontHalf, float size) {
		float satelliteAngle = Satellite.applyPhaseOffsetToOrbitAngle(phaseOffset, altitude, angle, (float) (2D * Math.PI));
		SatelliteOrbitPoint orbitPoint = getArtificialSatelliteOrbitPoint(altitude, inclination, satelliteAngle, baseRadiusMapPx);
		float screenX = mapToScreenX(centerX, orbitPoint.offsetU, orbitPoint.offsetV, zoom);
		float screenY = mapToScreenY(centerY, orbitPoint.offsetU, orbitPoint.offsetV, zoom);
		if((orbitPoint.depth >= 0F) != frontHalf) return;

		float half = size * 0.5F;
		float minX = guiLeft + 9;
		float minY = guiTop + 8;
		float maxX = minX + 116;
		float maxY = minY + 116;

		if(screenX + half < minX || screenX - half > maxX || screenY + half < minY || screenY - half > maxY) return;

		GL11.glColor4f(1F, 1F, 1F, 1F);
		mc.getTextureManager().bindTexture(texture != null ? texture : satelliteTextureDefault);
		drawPartialTex(screenX - half, screenY - half, size, size, 0F, 0F, 1F, 1F);
		GL11.glColor4f(1F, 1F, 1F, 1F);
	}

	private float getBlinkAlpha(boolean isBlinking, float blinkPeriod) {
		if(!isBlinking) return 1.0F;
		long cycleMillis = (long) (Satellite.clampBlinkPeriod(blinkPeriod) * 1000.0F);
		if(cycleMillis <= 0L) return 1.0F;
		return 1.0F - (float) (System.currentTimeMillis() % cycleMillis) / cycleMillis;
	}

	private void drawBodyPreview(CelestialBody body, float centerX, float centerY, float size, double dayTicks) {
		float half = size * 0.5F;
		float ringHalfWidth = 0F;
		float ringHalfHeight = 0F;
		float minX = centerX - half;
		float maxX = centerX + half;
		float minY = centerY - half;
		float maxY = centerY + half;

		if(body.hasRings) {
			ringHalfWidth = size * 0.5F * Math.max(1F, body.ringSize);
			float ringTiltSin = Math.abs(MathHelper.sin((float) Math.toRadians(body.ringTilt)));
			ringTiltSin = Math.max(0.08F, ringTiltSin);
			ringHalfHeight = Math.max(0.5F, ringHalfWidth * ringTiltSin);

			minX = Math.min(minX, centerX - ringHalfWidth);
			maxX = Math.max(maxX, centerX + ringHalfWidth);
			minY = Math.min(minY, centerY - ringHalfHeight);
			maxY = Math.max(maxY, centerY + ringHalfHeight);
		}

		if(maxX < guiLeft + 9 || minX > guiLeft + 9 + 116 || maxY < guiTop + 8 || minY > guiTop + 8 + 116) {
			return;
		}

		if(body.hasRings) {
			drawBodyRingHalf(body, centerX, centerY, ringHalfWidth, ringHalfHeight, false);
		}

		if(body.texture != null) {
			mc.getTextureManager().bindTexture(body.texture);
			if(body.parent == null) {
				drawTexturedQuad(centerX, centerY, size, 0F);
			} else {
				AtmosphereRenderUtil.renderAtmosphereGlow2D(Tessellator.instance, body, centerX, centerY, size, 1.0F);

				float phase = getBodyRotationPhase(body, dayTicks);
				float bodyRotationAngle = phase * 360F;
				boolean rotateBody = hasTransparentPixels(body.texture);
				float textureUOffset = 0F;

				if(rotateBody) {
					drawTexturedQuadRotating(centerX, centerY, size, bodyRotationAngle);
				} else {
					drawTexturedQuad(centerX, centerY, size, phase);
					textureUOffset = phase;
				}

				drawBodyOverlays(body, centerX, centerY, size, rotateBody, bodyRotationAngle, dayTicks, textureUOffset);
			}
		} else {
			int color = 0xFF666666;
			if(body.color != null && body.color.length >= 3) {
				color = 0xFF000000 | (toColorChannel(body.color[0]) << 16) | (toColorChannel(body.color[1]) << 8) | toColorChannel(body.color[2]);
			}
			drawRect((int) (centerX - half), (int) (centerY - half), (int) (centerX + half), (int) (centerY + half), color);
		}

		if(body.hasRings) {
			drawBodyRingHalf(body, centerX, centerY, ringHalfWidth, ringHalfHeight, true);
		}
	}

	private void drawBodyRingHalf(CelestialBody body, float bodyScreenX, float bodyScreenY, float ringHalfWidth, float drawH, boolean frontHalf) {
		if(body == null || ringHalfWidth <= 0F || drawH <= 0F) return;

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

	private void drawBodyOverlays(CelestialBody body, float bodyScreenX, float bodyScreenY, float drawSize, boolean rotateBody, float bodyRotationAngle, double dayTicks, float textureUOffset) {
		float phase = calculateBodyCrescentPhase(body, dayTicks);
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

	private boolean hasTransparentPixels(ResourceLocation texture) {
		Boolean cached = textureAlphaCache.get(texture);
		if(cached != null) return cached;

		boolean hasAlpha = false;
		InputStream stream = null;

		try {
			IResource resource = mc.getResourceManager().getResource(texture);
			stream = resource.getInputStream();
			BufferedImage image = ImageIO.read(stream);

			if(image.getColorModel().hasAlpha()) {
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

	private float calculateBodyCrescentPhase(CelestialBody body, double dayTicks) {
		if(body == null || body.parent == null) return 0F;

		double orbitalPeriodTicks = body.getOrbitalPeriod() * (double) AstronomyUtil.TICKS_IN_DAY;
		if(orbitalPeriodTicks <= 0D) return 0F;

		double worldTicks = dayTicks * AstronomyUtil.TIME_MULTIPLIER;
		float orbitalAngle = (float) (2D * Math.PI * (worldTicks / orbitalPeriodTicks));
		float dx = MathHelper.cos(orbitalAngle);
		float dy = MathHelper.sin(orbitalAngle);

		float phaseMagnitude = MathHelper.clamp_float((dy + 1F) * 0.5F, 0F, 1F);
		float phaseSign = dx <= 0F ? 1F : -1F;
		return MathHelper.clamp_float(phaseMagnitude * phaseSign, -1F, 1F);
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
		float minU = uOffset;
		float maxU = 1F + uOffset;
		Tessellator tess = Tessellator.instance;
		tess.startDrawingQuads();
		tess.addVertexWithUV(x - half, y + half, this.zLevel, minU, 1F);
		tess.addVertexWithUV(x + half, y + half, this.zLevel, maxU, 1F);
		tess.addVertexWithUV(x + half, y - half, this.zLevel, maxU, 0F);
		tess.addVertexWithUV(x - half, y - half, this.zLevel, minU, 0F);
		tess.draw();
	}

	private void drawTexturedQuadRotating(float x, float y, float size, float angle) {
		GL11.glPushMatrix();
		GL11.glTranslatef(x, y, 0F);
		GL11.glRotatef(angle, 0F, 0F, 1F);
		drawTexturedQuad(0F, 0F, size, 0F);
		GL11.glPopMatrix();
	}

	private float getPreviewZoom(float bodySizeAt1x, float baseOrbitRadiusMapPx, float maxAltitude) {
		float altitude = Math.max(Satellite.DEFAULT_ALTITUDE_KM, maxAltitude);
		float altitudeFactor = altitude / Satellite.DEFAULT_ALTITUDE_KM;
		float maxOrbitRadius = 116F * 0.46F;
		float zoomForOrbit = maxOrbitRadius / Math.max(0.0001F, baseOrbitRadiusMapPx * altitudeFactor);
		float zoomForBody = 16F / Math.max(0.0001F, bodySizeAt1x);
		return MathHelper.clamp_float(Math.min(zoomForOrbit, zoomForBody), 16F, 240F);
	}

	private float mapToScreenX(float centerX, float mapU, float mapV, float zoom) {
		return centerX + mapU * zoom;
	}

	private float mapToScreenY(float centerY, float mapU, float mapV, float zoom) {
		return centerY + mapV * zoom;
	}

	private SatelliteOrbitPoint getArtificialSatelliteOrbitPoint(float altitude, float inclination, float angle, float baseRadiusMapPx) {
		float satAltitude = altitude;
		double satInclination = Math.toRadians(inclination);
		double radiusMapPx = baseRadiusMapPx * (satAltitude / Satellite.DEFAULT_ALTITUDE_KM);

		double x = radiusMapPx * MathHelper.cos(angle);
		double orbitY = radiusMapPx * MathHelper.sin(angle);
		double y = Math.sin(satInclination) * orbitY;
		double z = Math.cos(satInclination) * orbitY;

		return new SatelliteOrbitPoint((float) x, (float) y, (float) z);
	}

	private double getArtificialSatelliteAngle() {
		long cycle = 30000L;
		double progress = (double) System.currentTimeMillis() / (double) cycle;
		return -progress * 2D * Math.PI;
	}

	private CelestialBody getCurrentBody() {
		CelestialBody body = CelestialBody.getTarget(player.worldObj, (int) player.posX, (int) player.posZ).body;
		return body != null ? body : CelestialBody.getBody(player.worldObj);
	}

	private CelestialBody getPreviewBody(ItemStack held) {
		int previewDimensionId = Satellite.getTargetDimensionId(held, getCurrentBody().dimensionId);
		return CelestialBody.getBody(previewDimensionId);
	}

	private float getBodyRotationPhase(CelestialBody body, double dayTicks) {
		double period = body.getRotationalPeriod();
		return (float) ((dayTicks % period) / period);
	}

	private float getBodySizePxAt1x(CelestialBody body) {
		if(body.parent == null) return 36F * 0.45F;
		if(isMoon(body)) return getMoonSizePxAt1x(body);

		float size = body.radiusKm * (36F / 261_600F) * 2.6F;
		return MathHelper.clamp_float(size, 0.8F, 2.0F) * 0.45F;
	}

	private float getMoonSizePxAt1x(CelestialBody moon) {
		float t = (Math.max(0F, moon.radiusKm) - 65F) / (500F - 65F);
		t = MathHelper.clamp_float(t, 0F, 1F);
		return (0.2F + (0.5F - 0.2F) * t) * 0.45F * 0.82F;
	}

	private ResourceLocation getSatelliteTextureByType(Class<?> type) {
		for(Class<?> current = type; current != null; current = current.getSuperclass()) {
			ResourceLocation texture = satelliteTextureByClass.get(current);
			if(texture != null) return texture;
			if(current == Satellite.class) break;
		}
		return satelliteTextureDefault;
	}

	private boolean isMoon(CelestialBody body) {
		return body.parent != null && body.parent.parent != null;
	}

	private void pushScissor(int x, int y, int w, int h) {
		ScaledResolution res = new ScaledResolution(this.mc, this.mc.displayWidth, this.mc.displayHeight);
		int scale = res.getScaleFactor();
		int scissorX = (guiLeft + x) * scale;
		int scissorY = mc.displayHeight - (guiTop + y + h) * scale;
		GL11.glEnable(GL11.GL_SCISSOR_TEST);
		GL11.glScissor(scissorX, scissorY, w * scale, h * scale);
	}

	private static class SatelliteOrbitPoint {
		private final float offsetU;
		private final float offsetV;
		private final float depth;

		private SatelliteOrbitPoint(float offsetU, float offsetV, float depth) {
			this.offsetU = offsetU;
			this.offsetV = offsetV;
			this.depth = depth;
		}
	}

	private static int toColorChannel(float value) {
		int color = Math.round(value * 255.0F);
		if(color < 0) return 0;
		if(color > 255) return 255;
		return color;
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
}
