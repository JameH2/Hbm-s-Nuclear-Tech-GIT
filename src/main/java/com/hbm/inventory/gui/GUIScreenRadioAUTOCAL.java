package com.hbm.inventory.gui;

import java.awt.Desktop;
import java.io.File;
import java.io.PrintWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.net.URI;
import java.util.Arrays;

import org.lwjgl.Sys;
import org.lwjgl.opengl.GL11;

import com.hbm.lib.RefStrings;
import com.hbm.main.MainRegistry;
import com.hbm.packet.PacketDispatcher;
import com.hbm.packet.toserver.NBTControlPacket;
import com.hbm.tileentity.network.TileEntityRadioAUTOCAL;

import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.ResourceLocation;
import com.hbm.util.i18n.I18nUtil;

public class GUIScreenRadioAUTOCAL extends GuiScreen {

	protected static final ResourceLocation texture = new ResourceLocation(RefStrings.MODID + ":textures/gui/machine/gui_rtty_autocal.png");
	protected TileEntityRadioAUTOCAL autocal;

	protected int xSize = 170;
	protected int ySize = 138;
	protected int guiLeft;
	protected int guiTop;

	public GUIScreenRadioAUTOCAL(TileEntityRadioAUTOCAL autocal) {
		this.autocal = autocal;
	}

	private void browse(URI uri) throws IOException {
		// workaround for Java not supporting all platforms, mostly for Linux
		if (Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
			Desktop.getDesktop().browse(uri);
		} else {
			if (Sys.getVersion().charAt(0) == '3') {
				// probably a LWJGL3ify user, open the folder instead since that somehow seems to work
				File uploadFolder = new File(MainRegistry.configDir.getParentFile(), "hbmComputerUpload");
				if (uploadFolder.exists()) Sys.openURL(uploadFolder.toString());
			} else {
				Sys.openURL(uri.toString());
			}
		}
	}

	@Override
	public void initGui() {
		super.initGui();
		this.guiLeft = (this.width - this.xSize) / 2;
		this.guiTop = (this.height - this.ySize) / 2;
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float f) {

		if(this.autocal == null || this.autocal.isInvalid()) {
			Minecraft.getMinecraft().thePlayer.closeScreen();
			return;
		}

		this.drawDefaultBackground();
		this.drawGuiContainerBackgroundLayer(f, mouseX, mouseY);
		GL11.glDisable(GL11.GL_LIGHTING);
		this.drawGuiContainerForegroundLayer(mouseX, mouseY);
		GL11.glEnable(GL11.GL_LIGHTING);
	}

	@Override
	protected void mouseClicked(int x, int y, int i) {
		super.mouseClicked(x, y, i);

		NBTTagCompound data = null;

		if(checkClick(x, y, 8, 36, 18, 18)) { data = new NBTTagCompound(); data.setBoolean("on", true); }
		if(checkClick(x, y, 28, 36, 18, 18)) { data = new NBTTagCompound(); data.setBoolean("ignore", true); }
		if(checkClick(x, y, 48, 36, 18, 18)) { data = new NBTTagCompound(); data.setBoolean("auto", true); }

		// open folder and generate new script file
		if(checkClick(x, y, 104, 36, 18, 18)) {
			try {
				File uploadFolder = new File(MainRegistry.configDir.getParentFile(), "hbmComputerUpload");
				File script = new File(uploadFolder, "script.txt");
				if(!uploadFolder.exists()) uploadFolder.mkdir();
				if(!script.exists()) script.createNewFile();
				script.setExecutable(false);
				browse(script.toURI());
			} catch(Throwable ex) { MainRegistry.logger.error("Couldn't open link", ex); }
		}

		// open folder and generate new doc file
		if(checkClick(x, y, 144, 36, 18, 18)) {
			try {
				File uploadFolder = new File(MainRegistry.configDir.getParentFile(), "hbmComputerUpload");
				File doc = new File(uploadFolder, "documentation_v1.1.md");
				if(!uploadFolder.exists()) uploadFolder.mkdir();
				if(!doc.exists()) {
					doc.createNewFile();
					try {
						PrintWriter printer = new PrintWriter(doc, StandardCharsets.US_ASCII.name());
						for(String line : DOCS) printer.println(line);
						printer.close();
					} catch(Throwable e) { }
				}
				browse(doc.toURI());
			} catch(Throwable ex) { MainRegistry.logger.error("Couldn't open link", ex); }
		}

		if(checkClick(x, y, 84, 36, 18, 18)) {
			try {
				File uploadFolder = new File(MainRegistry.configDir.getParentFile(), "hbmComputerUpload");
				File script = new File(uploadFolder, "script.txt");
				if(!uploadFolder.exists()) uploadFolder.mkdir();
				if(!script.exists()) {
					script.createNewFile();
					script.setExecutable(false);
					return;
				}

				byte[] bytes = Files.readAllBytes(Paths.get(script.toURI()));
				data = new NBTTagCompound();
				data.setString("payload", new String(bytes, StandardCharsets.UTF_8));

			} catch(Throwable ex) { }
		}

		// this thing can both upload and download files so let's be careful about this
		// the upload is simple, it's just text that is handled by the AUTOCAL, so doing anything malicious isn't more likely than with any other package
		// download is iffy, because we take text from the server, fully user-definable, and save it to disk. it's stored as a txt so accudentally running it
		// or getting it to run itself, should it be a malicious script, is unlikely. still, we want to minimized the chances as much as we can
		// option 1: set file attribute to disallow running (i.e. disable executable perm)
		// option 2: add fluff that would break scripts, however they might work. we can't change the actual lines because we want the script to be edited,
		//           but we can add some extra crap that would either halt common scripting langs entirely or at least disrupt them into not functioning
		// option 3: enforce validation so only MS-ES1 script can be received by the client. this means that info such as comments or incorrectly written commands
		//           are lost, however this is the safest way because it becomes impossible to send malicious code, but it also interferes with regular user operation more

		if(data != null) {
			mc.getSoundHandler().playSound(PositionedSoundRecord.func_147674_a(new ResourceLocation("gui.button.press"), 1.0F));
			PacketDispatcher.wrapper.sendToServer(new NBTControlPacket(data, autocal.xCoord, autocal.yCoord, autocal.zCoord));
		}
	}

	protected boolean checkClick(int x, int y, int left, int top, int sizeX, int sizeY) {
		return guiLeft + left <= x && guiLeft + left + sizeX > x && guiTop + top < y && guiTop + top + sizeY >= y;
	}

	private void drawGuiContainerForegroundLayer(int x, int y) {

		for(int i = 0; i < autocal.history.length; i++) {
			String line = autocal.history[i];
			if(line == null || line.isEmpty()) continue;
			String localized = I18nUtil.resolveKey(line);
			if(!localized.equals(line)) line = localized;
			this.fontRendererObj.drawString(line, guiLeft + 7, guiTop + 73 + i * 10, 0x00ff00);
		}

		if(checkClick(x, y, 8, 36, 18, 18)) this.func_146283_a(Arrays.asList(new String[] {EnumChatFormatting.RED + I18nUtil.resolveKey("gui.autocal.power")}), x, y);
		if(checkClick(x, y, 28, 36, 18, 18)) this.func_146283_a(Arrays.asList(new String[] {EnumChatFormatting.RED + I18nUtil.resolveKey("gui.autocal.ignore_errors"), I18nUtil.resolveKey("gui.autocal.doc.line.004"), I18nUtil.resolveKey("gui.autocal.doc.line.002"), I18nUtil.resolveKey("gui.autocal.doc.line.003"), I18nUtil.resolveKey("gui.autocal.doc.line.001")}), x, y);
		if(checkClick(x, y, 48, 36, 18, 18)) this.func_146283_a(Arrays.asList(new String[] {EnumChatFormatting.RED + I18nUtil.resolveKey("gui.autocal.auto_reboot"), I18nUtil.resolveKey("gui.autocal.doc.line.006"), I18nUtil.resolveKey("gui.autocal.doc.line.007"), I18nUtil.resolveKey("gui.autocal.doc.line.005")}), x, y);

		if(checkClick(x, y, 84, 36, 18, 18)) this.func_146283_a(Arrays.asList(new String[] {EnumChatFormatting.BLUE + I18nUtil.resolveKey("gui.autocal.upload")}), x, y);
		if(checkClick(x, y, 104, 36, 18, 18)) this.func_146283_a(Arrays.asList(new String[] {EnumChatFormatting.BLUE + I18nUtil.resolveKey("gui.autocal.open_file")}), x, y);
		if(checkClick(x, y, 124, 36, 18, 18)) this.func_146283_a(Arrays.asList(new String[] {EnumChatFormatting.BLUE + I18nUtil.resolveKey("gui.autocal.download"), EnumChatFormatting.RED + I18nUtil.resolveKey("gui.autocal.unsupported")}), x, y);
		if(checkClick(x, y, 144, 36, 18, 18)) this.func_146283_a(Arrays.asList(new String[] {EnumChatFormatting.BLUE + I18nUtil.resolveKey("gui.autocal.documentation")}), x, y);
	}

	private void drawGuiContainerBackgroundLayer(float f, int mouseX, int mouseY) {
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		Minecraft.getMinecraft().getTextureManager().bindTexture(texture);
		drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);

		if(autocal.isOn) drawTexturedModalRect(guiLeft + 8, guiTop + 36, xSize, 0, 18, 18);
		if(!autocal.ignoreError) drawTexturedModalRect(guiLeft + 28, guiTop + 36, xSize, 18, 18, 18);
		if(!autocal.autoReboot) drawTexturedModalRect(guiLeft + 48, guiTop + 36, xSize, 36, 18, 18);
	}

	@Override
	protected void keyTyped(char c, int b) {
		if(b == 1 || b == Minecraft.getMinecraft().gameSettings.keyBindInventory.getKeyCode()) {
			Minecraft.getMinecraft().thePlayer.closeScreen();
		}
	}

	@Override public boolean doesGuiPauseGame() { return false; }


	public static final String[] DOCS = new String[] {
			I18nUtil.resolveKey("gui.autocal.doc.autocal_automatic_calculator"),
			"",
			I18nUtil.resolveKey("gui.autocal.doc.about_document"),
			I18nUtil.resolveKey("gui.autocal.doc.line.001"),
			"",
			I18nUtil.resolveKey("gui.autocal.doc.line.009"),
			"",
			I18nUtil.resolveKey("gui.autocal.doc.about_autocal"),
			I18nUtil.resolveKey("gui.autocal.doc.line.002"),
			"",
			I18nUtil.resolveKey("gui.autocal.doc.line.003"),
			"",
			I18nUtil.resolveKey("gui.autocal.doc.line.004"),
			"",
			I18nUtil.resolveKey("gui.autocal.doc.line.005"),
			"",
			I18nUtil.resolveKey("gui.autocal.doc.line.006"),
			"",
			I18nUtil.resolveKey("gui.autocal.doc.line.007"),
			"",
			I18nUtil.resolveKey("gui.autocal.doc.line.008"),
			"",
			I18nUtil.resolveKey("gui.autocal.doc.line.009"),
			"",
			I18nUtil.resolveKey("gui.autocal.doc.about_ms_es1"),
			I18nUtil.resolveKey("gui.autocal.doc.line.010"),
			"",
			I18nUtil.resolveKey("gui.autocal.doc.line.019"),
			"",
			I18nUtil.resolveKey("gui.autocal.doc.about_ms_es_v1_1"),
			I18nUtil.resolveKey("gui.autocal.doc.line.011"),
			"",
			I18nUtil.resolveKey("gui.autocal.doc.line.012"),
			"",
			I18nUtil.resolveKey("gui.autocal.doc.about_buffer"),
			I18nUtil.resolveKey("gui.autocal.doc.line.013"),
			"",
			I18nUtil.resolveKey("gui.autocal.doc.about_stack_v1_1"),
			I18nUtil.resolveKey("gui.autocal.doc.line.014"),
			"",
			I18nUtil.resolveKey("gui.autocal.doc.about_variables"),
			I18nUtil.resolveKey("gui.autocal.doc.line.015"),
			"",
			I18nUtil.resolveKey("gui.autocal.doc.about_variable_substitution"),
			I18nUtil.resolveKey("gui.autocal.doc.line.016"),
			"",
			I18nUtil.resolveKey("gui.autocal.doc.line.026"),
			"",
			I18nUtil.resolveKey("gui.autocal.doc.line.017"),
			"",
			I18nUtil.resolveKey("gui.autocal.doc.about_redstone_over_radio"),
			I18nUtil.resolveKey("gui.autocal.doc.line.018"),
			"",
			I18nUtil.resolveKey("gui.autocal.doc.script_commands"),
			"",
			I18nUtil.resolveKey("gui.autocal.doc.comments"),
			I18nUtil.resolveKey("gui.autocal.doc.line.019"),
			"",
			I18nUtil.resolveKey("gui.autocal.doc.line.020"),
			"",
			I18nUtil.resolveKey("gui.autocal.doc.nop"),
			I18nUtil.resolveKey("gui.autocal.doc.line.021"),
			"",
			I18nUtil.resolveKey("gui.autocal.doc.clockspeed"),
			I18nUtil.resolveKey("gui.autocal.doc.line.022"),
			"",
			I18nUtil.resolveKey("gui.autocal.doc.line.023"),
			"",
			I18nUtil.resolveKey("gui.autocal.doc.dest"),
			I18nUtil.resolveKey("gui.autocal.doc.line.024"),
			"",
			I18nUtil.resolveKey("gui.autocal.doc.line.025"),
			"",
			I18nUtil.resolveKey("gui.autocal.doc.jmp"),
			I18nUtil.resolveKey("gui.autocal.doc.line.026"),
			"",
			I18nUtil.resolveKey("gui.autocal.doc.line.037"),
			I18nUtil.resolveKey("gui.autocal.doc.line.027"),
			"",
			I18nUtil.resolveKey("gui.autocal.doc.jmpif"),
			I18nUtil.resolveKey("gui.autocal.doc.line.028"),
			"",
			I18nUtil.resolveKey("gui.autocal.doc.line.040"),
			I18nUtil.resolveKey("gui.autocal.doc.line.041"),
			I18nUtil.resolveKey("gui.autocal.doc.line.042"),
			I18nUtil.resolveKey("gui.autocal.doc.line.043"),
			I18nUtil.resolveKey("gui.autocal.doc.line.042"),
			"",
			I18nUtil.resolveKey("gui.autocal.doc.line.029"),
			"",
			I18nUtil.resolveKey("gui.autocal.doc.jmpnot"),
			I18nUtil.resolveKey("gui.autocal.doc.line.030"),
			"",
			I18nUtil.resolveKey("gui.autocal.doc.line.040"),
			I18nUtil.resolveKey("gui.autocal.doc.line.046"),
			I18nUtil.resolveKey("gui.autocal.doc.line.047"),
			I18nUtil.resolveKey("gui.autocal.doc.line.048"),
			I18nUtil.resolveKey("gui.autocal.doc.line.043"),
			I18nUtil.resolveKey("gui.autocal.doc.line.049"),
			I18nUtil.resolveKey("gui.autocal.doc.line.050"),
			"",
			I18nUtil.resolveKey("gui.autocal.doc.line.031"),
			"",
			I18nUtil.resolveKey("gui.autocal.doc.endtick"),
			I18nUtil.resolveKey("gui.autocal.doc.line.032"),
			"",
			I18nUtil.resolveKey("gui.autocal.doc.shutdown"),
			I18nUtil.resolveKey("gui.autocal.doc.line.033"),
			"",
			I18nUtil.resolveKey("gui.autocal.doc.load"),
			I18nUtil.resolveKey("gui.autocal.doc.line.034"),
			"",
			I18nUtil.resolveKey("gui.autocal.doc.line.035"),
			"",
			I18nUtil.resolveKey("gui.autocal.doc.save"),
			I18nUtil.resolveKey("gui.autocal.doc.line.036"),
			"",
			I18nUtil.resolveKey("gui.autocal.doc.line.037"),
			"",
			I18nUtil.resolveKey("gui.autocal.doc.buffer"),
			I18nUtil.resolveKey("gui.autocal.doc.line.038"),
			"",
			I18nUtil.resolveKey("gui.autocal.doc.line.040"),
			I18nUtil.resolveKey("gui.autocal.doc.line.059"),
			I18nUtil.resolveKey("gui.autocal.doc.line.060"),
			"",
			I18nUtil.resolveKey("gui.autocal.doc.line.061"),
			"",
			I18nUtil.resolveKey("gui.autocal.doc.push_v1_1"),
			I18nUtil.resolveKey("gui.autocal.doc.line.039"),
			"",
			I18nUtil.resolveKey("gui.autocal.doc.pop_v1_1"),
			I18nUtil.resolveKey("gui.autocal.doc.line.063"),
			"",
			I18nUtil.resolveKey("gui.autocal.doc.peek_v1_1"),
			I18nUtil.resolveKey("gui.autocal.doc.line.040"),
			"",
			I18nUtil.resolveKey("gui.autocal.doc.eval"),
			I18nUtil.resolveKey("gui.autocal.doc.line.041"),
			"",
			I18nUtil.resolveKey("gui.autocal.doc.line.066"),
			I18nUtil.resolveKey("gui.autocal.doc.line.042"),
			"",
			I18nUtil.resolveKey("gui.autocal.doc.line.040"),
			I18nUtil.resolveKey("gui.autocal.doc.line.068"),
			I18nUtil.resolveKey("gui.autocal.doc.line.069"),
			"",
			I18nUtil.resolveKey("gui.autocal.doc.line.043"),
			"",
			I18nUtil.resolveKey("gui.autocal.doc.evalr"),
			I18nUtil.resolveKey("gui.autocal.doc.line.044"),
			"",
			I18nUtil.resolveKey("gui.autocal.doc.rounddown_floor"),
			I18nUtil.resolveKey("gui.autocal.doc.line.045"),
			"",
			I18nUtil.resolveKey("gui.autocal.doc.line.073"),
			I18nUtil.resolveKey("gui.autocal.doc.line.074"),
			"",
			I18nUtil.resolveKey("gui.autocal.doc.roundup_ceil"),
			I18nUtil.resolveKey("gui.autocal.doc.line.046"),
			"",
			I18nUtil.resolveKey("gui.autocal.doc.line.076"),
			I18nUtil.resolveKey("gui.autocal.doc.line.077"),
			"",
			I18nUtil.resolveKey("gui.autocal.doc.round_nearest"),
			I18nUtil.resolveKey("gui.autocal.doc.line.047"),
			"",
			I18nUtil.resolveKey("gui.autocal.doc.line.079"),
			I18nUtil.resolveKey("gui.autocal.doc.line.080"),
			"",
			I18nUtil.resolveKey("gui.autocal.doc.concat"),
			I18nUtil.resolveKey("gui.autocal.doc.line.048"),
			"",
			I18nUtil.resolveKey("gui.autocal.doc.line.049"),
			"",
			I18nUtil.resolveKey("gui.autocal.doc.eq"),
			I18nUtil.resolveKey("gui.autocal.doc.line.050"),
			"",
			I18nUtil.resolveKey("gui.autocal.doc.line.051"),
			I18nUtil.resolveKey("gui.autocal.doc.line.052"),
			"",
			I18nUtil.resolveKey("gui.autocal.doc.gtb"),
			I18nUtil.resolveKey("gui.autocal.doc.line.053"),
			"",
			I18nUtil.resolveKey("gui.autocal.doc.line.054"),
			I18nUtil.resolveKey("gui.autocal.doc.line.055"),
			"",
			I18nUtil.resolveKey("gui.autocal.doc.ltb"),
			I18nUtil.resolveKey("gui.autocal.doc.line.056"),
			"",
			I18nUtil.resolveKey("gui.autocal.doc.line.057"),
			I18nUtil.resolveKey("gui.autocal.doc.line.058"),
			"",
			I18nUtil.resolveKey("gui.autocal.doc.geb"),
			I18nUtil.resolveKey("gui.autocal.doc.line.092"),
			"",
			I18nUtil.resolveKey("gui.autocal.doc.leb"),
			I18nUtil.resolveKey("gui.autocal.doc.line.093"),
			"",
			I18nUtil.resolveKey("gui.autocal.doc.splitter_v1_1"),
			I18nUtil.resolveKey("gui.autocal.doc.line.059"),
			"",
			I18nUtil.resolveKey("gui.autocal.doc.split_v1_1"),
			I18nUtil.resolveKey("gui.autocal.doc.line.060"),
			"",
			I18nUtil.resolveKey("gui.autocal.doc.line.040"),
			I18nUtil.resolveKey("gui.autocal.doc.line.096"),
			I18nUtil.resolveKey("gui.autocal.doc.line.097"),
			"",
			I18nUtil.resolveKey("gui.autocal.doc.line.098"),
			"",
			I18nUtil.resolveKey("gui.autocal.doc.splitcount_v1_1"),
			I18nUtil.resolveKey("gui.autocal.doc.line.061"),
			"",
			I18nUtil.resolveKey("gui.autocal.doc.line.040"),
			I18nUtil.resolveKey("gui.autocal.doc.line.100"),
			I18nUtil.resolveKey("gui.autocal.doc.line.101"),
			"",
			I18nUtil.resolveKey("gui.autocal.doc.line.102"),
			"",
			I18nUtil.resolveKey("gui.autocal.doc.length_v1_1"),
			I18nUtil.resolveKey("gui.autocal.doc.line.062"),
			"",
			I18nUtil.resolveKey("gui.autocal.doc.line.040"),
			I18nUtil.resolveKey("gui.autocal.doc.line.104"),
			I18nUtil.resolveKey("gui.autocal.doc.line.105"),
			"",
			I18nUtil.resolveKey("gui.autocal.doc.line.106"),
			"",
			I18nUtil.resolveKey("gui.autocal.doc.first_v1_1"),
			I18nUtil.resolveKey("gui.autocal.doc.line.063"),
			"",
			I18nUtil.resolveKey("gui.autocal.doc.line.040"),
			I18nUtil.resolveKey("gui.autocal.doc.line.104"),
			I18nUtil.resolveKey("gui.autocal.doc.line.108"),
			"",
			I18nUtil.resolveKey("gui.autocal.doc.line.109"),
			"",
			I18nUtil.resolveKey("gui.autocal.doc.last_v1_1"),
			I18nUtil.resolveKey("gui.autocal.doc.line.064"),
			"",
			I18nUtil.resolveKey("gui.autocal.doc.line.040"),
			I18nUtil.resolveKey("gui.autocal.doc.line.104"),
			I18nUtil.resolveKey("gui.autocal.doc.line.111"),
			"",
			I18nUtil.resolveKey("gui.autocal.doc.line.112"),
			"",
			I18nUtil.resolveKey("gui.autocal.doc.send"),
			I18nUtil.resolveKey("gui.autocal.doc.line.065"),
			"",
			I18nUtil.resolveKey("gui.autocal.doc.line.040"),
			I18nUtil.resolveKey("gui.autocal.doc.line.114"),
			I18nUtil.resolveKey("gui.autocal.doc.line.115"),
			"",
			I18nUtil.resolveKey("gui.autocal.doc.line.116"),
			"",
			I18nUtil.resolveKey("gui.autocal.doc.line.040"),
			I18nUtil.resolveKey("gui.autocal.doc.line.117"),
			I18nUtil.resolveKey("gui.autocal.doc.line.118"),
			"",
			I18nUtil.resolveKey("gui.autocal.doc.line.119"),
			"",
			I18nUtil.resolveKey("gui.autocal.doc.listen"),
			I18nUtil.resolveKey("gui.autocal.doc.line.066"),
			"",
			I18nUtil.resolveKey("gui.autocal.doc.line.040"),
			I18nUtil.resolveKey("gui.autocal.doc.line.121"),
			I18nUtil.resolveKey("gui.autocal.doc.line.122"),
			I18nUtil.resolveKey("gui.autocal.doc.line.123"),
			"",
			I18nUtil.resolveKey("gui.autocal.doc.line.067"),
			"",
			I18nUtil.resolveKey("gui.autocal.doc.poll_v1_1"),
			I18nUtil.resolveKey("gui.autocal.doc.line.068"),
			"",
			I18nUtil.resolveKey("gui.autocal.doc.worldtime_v1_1"),
			I18nUtil.resolveKey("gui.autocal.doc.line.069"),
			"",
			I18nUtil.resolveKey("gui.autocal.doc.advanced"),
			"",
			I18nUtil.resolveKey("gui.autocal.doc.conditional_branches"),
			I18nUtil.resolveKey("gui.autocal.doc.line.070"),
			"",
			I18nUtil.resolveKey("gui.autocal.doc.line.071"),
			"",
			I18nUtil.resolveKey("gui.autocal.doc.line.129"),
			I18nUtil.resolveKey("gui.autocal.doc.line.130"),
			I18nUtil.resolveKey("gui.autocal.doc.line.131"),
			I18nUtil.resolveKey("gui.autocal.doc.line.132"),
			I18nUtil.resolveKey("gui.autocal.doc.line.133"),
			I18nUtil.resolveKey("gui.autocal.doc.line.134"),
			I18nUtil.resolveKey("gui.autocal.doc.line.135"),
			I18nUtil.resolveKey("gui.autocal.doc.line.136"),
			I18nUtil.resolveKey("gui.autocal.doc.line.137"),
			I18nUtil.resolveKey("gui.autocal.doc.line.138"),
			"",
			I18nUtil.resolveKey("gui.autocal.doc.methods"),
			I18nUtil.resolveKey("gui.autocal.doc.line.072"),
			"",
			I18nUtil.resolveKey("gui.autocal.doc.line.140"),
			"",
			I18nUtil.resolveKey("gui.autocal.doc.line.141"),
			I18nUtil.resolveKey("gui.autocal.doc.line.142"),
			I18nUtil.resolveKey("gui.autocal.doc.line.143"),
			I18nUtil.resolveKey("gui.autocal.doc.line.144"),
			"",
			I18nUtil.resolveKey("gui.autocal.doc.line.073"),
			"",
			I18nUtil.resolveKey("gui.autocal.doc.line.146"),
			I18nUtil.resolveKey("gui.autocal.doc.line.147"),
			I18nUtil.resolveKey("gui.autocal.doc.line.148"),
			I18nUtil.resolveKey("gui.autocal.doc.line.149"),
			I18nUtil.resolveKey("gui.autocal.doc.line.150"),
			I18nUtil.resolveKey("gui.autocal.doc.line.151"),
			I18nUtil.resolveKey("gui.autocal.doc.line.152"),
			I18nUtil.resolveKey("gui.autocal.doc.line.153"),
			I18nUtil.resolveKey("gui.autocal.doc.line.154"),
			I18nUtil.resolveKey("gui.autocal.doc.line.155"),
			"",
			I18nUtil.resolveKey("gui.autocal.doc.line.156"),
	};
}
