package com.hbm.inventory.gui;

import java.util.ArrayList;
import java.util.List;

import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

import com.hbm.inventory.container.ContainerMachineWarController;
import com.hbm.items.ISatChip;
import com.hbm.lib.RefStrings;
import com.hbm.packet.PacketDispatcher;
import com.hbm.packet.toserver.NBTControlPacket;
import com.hbm.packet.toserver.SatActivatePacket;
import com.hbm.saveddata.SatelliteSavedData;
import com.hbm.saveddata.satellites.Satellite;
import com.hbm.saveddata.satellites.SatelliteWar;
import com.hbm.tileentity.machine.TileEntityMachineWarController;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.ResourceLocation;

public class GUIWarController extends GuiInfoContainer {

	private List<String> commandHistory = new ArrayList<>();
	private static final int MAX_HISTORY = 6; // Number of lines to keep visible
	private GuiTextField textField;

	private static ResourceLocation texture = new ResourceLocation(RefStrings.MODID + ":textures/gui/machine/gui_controlpanel.png");
	private TileEntityMachineWarController sucker;

	public GUIWarController(InventoryPlayer playerInv, TileEntityMachineWarController tile) {
		super(new ContainerMachineWarController(playerInv, tile));

		this.sucker = tile;
		this.xSize = 176;
		this.ySize = 204;
	}

	@Override
	public void drawScreen(int x, int y, float interp) {
		super.drawScreen(x, y, interp);

		this.drawElectricityInfo(this, x, y, guiLeft + 132, guiTop + 18, 16, 52, sucker.getPower(), sucker.getMaxPower());
		this.drawCustomInfoStat(x, y, guiLeft + 52, guiTop + 19, 8, 8, guiLeft + 52, guiTop + 19, this.getUpgradeInfo(sucker));
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int i, int j) {
		// String name = this.sucker.hasCustomInventoryName() ? this.sucker.getInventoryName() : I18n.format(this.sucker.getInventoryName());
		// this.fontRendererObj.drawString(name, this.xSize / 2 -
		// this.fontRendererObj.getStringWidth(name) / 2 - 1, 60, 4210752);
		// this.fontRendererObj.drawString(I18n.format("container.inventory"), 8,
		// this.ySize - 115 + 2, 4210752);

		int yOffset = 15;
		for(int x = Math.max(0, commandHistory.size() - MAX_HISTORY); x < commandHistory.size(); x++) {
			String text = commandHistory.get(x);

			float scale = 0.5F;
			GL11.glPushMatrix();
			GL11.glScalef(scale, scale, scale);
			fontRendererObj.drawString(text, (int) (78 / scale), (int) (yOffset / scale), 0x00FF00);
			GL11.glPopMatrix();

			yOffset += 12 * scale;
		}

		String typedText = textField.getText();
		float textScale = 0.5F;

		GL11.glPushMatrix();
		GL11.glScalef(textScale, textScale, textScale);
		fontRendererObj.drawString("> " + typedText + (textField.isFocused() && (mc.theWorld.getTotalWorldTime() % 20 < 10) ? "_" : ""), (int) (78 / textScale), (int) (57 / textScale), 0x00FF00);
		GL11.glPopMatrix();
	}

	@Override
	protected void keyTyped(char typedChar, int keyCode) {
		if(textField.textboxKeyTyped(typedChar, keyCode)) return;

		if(keyCode == Keyboard.KEY_RETURN) {
			processCommand(textField.getText().trim());
			textField.setText(""); // Clear after processing
		}

		super.keyTyped(typedChar, keyCode);
	}

	private void processCommand(String command) {
		if(command.isEmpty()) return;

		int satId = ISatChip.getFreqS(sucker.slots[2]);
		Satellite sat = SatelliteSavedData.getClientSats().get(satId);
		String[] parts = command.split(" ");

		String cmd = parts[0].toLowerCase();
		NBTTagCompound data = new NBTTagCompound();

		switch(cmd) {

		case "setpos":
			if(parts.length < 3) {
				addCommandHistory("> " + command);
				addCommandHistory("Error, invalid args", EnumChatFormatting.RED);
				return;
			}

			String xValueStr = parts[1];
			String zValueStr = parts[2];
			if(sucker.slots[1] == null) {
				addCommandHistory("> " + command);
				addCommandHistory("No drive.", EnumChatFormatting.RED);
				return;
			}
			if(!xValueStr.matches("-?\\d+") || !zValueStr.matches("-?\\d+")) {
				addCommandHistory("> " + command);
				addCommandHistory("Invalid number format.", EnumChatFormatting.RED);
				return;
			}

			int xValue = Integer.parseInt(xValueStr);
			int zValue = Integer.parseInt(zValueStr);

			addCommandHistory("> " + command);
			addCommandHistory("Set to: X=" + xValue + ", Z=" + zValue);

			data.setInteger("xcoord", xValue);
			data.setInteger("zcoord", zValue);

			PacketDispatcher.wrapper.sendToServer(new NBTControlPacket(data, sucker.xCoord, sucker.yCoord, sucker.zCoord));
			break;

		case "health":
			addCommandHistory("> " + command);
			addCommandHistory("Requesting " + cmd + "...");

			if(sat == null) {
				addCommandHistory("Satellite not in orbit!", EnumChatFormatting.RED);
			} else {
				if(sat instanceof SatelliteWar) {
					addCommandHistory("health: " + sat.getInterp());
				}
			}

			break;

		case "fire":
			if(sat == null) {
				addCommandHistory("Satellite not in orbit!", EnumChatFormatting.RED);
			} else {
				if(sat instanceof SatelliteWar) {
					addCommandHistory("Firing!");

					PacketDispatcher.wrapper.sendToServer(new SatActivatePacket(satId));
				} else {
					addCommandHistory("Wrong satellite" + EnumChatFormatting.RED);
				}
			}
			break;

		case "getsat":
			addCommandHistory("> " + command);
			if(sucker.slots[2] == null) {
				addCommandHistory("No satellite chip in slot 2.", EnumChatFormatting.RED);
			} else {
				addCommandHistory("Requesting Satellite ID: " + satId);

				if(sat == null) {
					addCommandHistory("Satellite not in orbit!", EnumChatFormatting.RED);
				} else {
					addCommandHistory("Satellite: " + sat.getClass().getSimpleName());
				}
			}
			break;

		default:
			addCommandHistory("> " + command);
			addCommandHistory("Unknown command.", EnumChatFormatting.RED);
			break;
		}

	}

	private void addCommandHistory(String text) {
		addToHistory(text, null);
	}

	private void addCommandHistory(String text, EnumChatFormatting color) {
		addToHistory(text, color);
	}

	private void addToHistory(String text, EnumChatFormatting color) {
		if(color != null) {
			ChatComponentText chatComponent = new ChatComponentText(text);
			chatComponent.setChatStyle(new ChatStyle().setColor(color));
			commandHistory.add(chatComponent.getFormattedText());
		} else {
			commandHistory.add(text);
		}

		if(commandHistory.size() > MAX_HISTORY * 2) {
			commandHistory = commandHistory.subList(commandHistory.size() - MAX_HISTORY * 2, commandHistory.size());
		}

	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float interp, int x, int y) {
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		Minecraft.getMinecraft().getTextureManager().bindTexture(texture);
		drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);
	}

	@Override
	public void initGui() {
		super.initGui();
		Keyboard.enableRepeatEvents(true);
		textField = new GuiTextField(fontRendererObj, guiLeft - 74, guiTop + 28, 100, 20);
		textField.setMaxStringLength(100);
		textField.setFocused(true);
		textField.setEnableBackgroundDrawing(false);
	}

	@Override
	public void onGuiClosed() {
		Keyboard.enableRepeatEvents(false);
	}

}
