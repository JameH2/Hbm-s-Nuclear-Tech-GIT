package com.hbm.inventory.gui;

import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

import com.hbm.packet.PacketDispatcher;
import com.hbm.packet.toserver.NBTControlPacket;
import com.hbm.tileentity.machine.rbmk.TileEntityRBMKTerminal;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.nbt.NBTTagCompound;
import com.hbm.util.i18n.I18nUtil;

public class GUIScreenRBMKTerminal extends GuiScreen {

	protected GuiTextField line;
	public static TileEntityRBMKTerminal lastTerminal;
	
	public GUIScreenRBMKTerminal(TileEntityRBMKTerminal terminal) {
		this.lastTerminal = terminal;
	}

	@Override
	public void initGui() {
		super.initGui();

		Keyboard.enableRepeatEvents(true);

		line = new GuiTextField(this.fontRendererObj, 0, 0, 0, 0);
		line.setMaxStringLength(50);

		line.setFocused(true);
	}

	@Override
	public void drawScreen(int x, int y, float f) {
		
		if(lastTerminal.isInvalid()) { // for if the terminal blows up
			this.mc.displayGuiScreen((GuiScreen) null);
			this.mc.setIngameFocus();
			return;
		}

		GL11.glScaled(0.5, 0.5, 1);
		this.fontRendererObj.drawString(I18nUtil.resolveKey("gui.rbmk_terminal.esc_quit"), 2, 2, 0xffffff);
		this.fontRendererObj.drawString(I18nUtil.resolveKey("gui.rbmk_terminal.chan_set"), 2, 12, 0xffffff);
		this.fontRendererObj.drawString(I18nUtil.resolveKey("gui.rbmk_terminal.send_hint"), 2, 22, 0xffffff);
		this.fontRendererObj.drawString(I18nUtil.resolveKey("gui.rbmk_terminal.start_hint"), 2, 32, 0xffffff);
		this.fontRendererObj.drawString(I18nUtil.resolveKey("gui.rbmk_terminal.stop_hint"), 2, 42, 0xffffff);
		this.fontRendererObj.drawString(I18nUtil.resolveKey("gui.rbmk_terminal.clear_hint"), 2, 52, 0xffffff);
	}
	
	@Override
	protected void keyTyped(char c, int b) {
		if(b == 1) {
			this.mc.displayGuiScreen((GuiScreen) null);
			this.mc.setIngameFocus();
			return;
		}

		if(b == Keyboard.KEY_RETURN) {
			NBTTagCompound data = new NBTTagCompound();
			data.setString("cmd", this.line.getText());
			PacketDispatcher.wrapper.sendToServer(new NBTControlPacket(data, lastTerminal.xCoord, lastTerminal.yCoord, lastTerminal.zCoord));
			this.line.setText("");
			return;
		}

		if(b == 199) return;
		if(b == 203) return;
		if(b == 205) return;
		if(b == 207) return;

		this.line.setCursorPositionEnd();
		if(this.line.textboxKeyTyped(c, b)) return;
	}
	
	public static String getWorkingLine(TileEntityRBMKTerminal terminal) {
		
		if(terminal != lastTerminal) return "";
		
		if(Minecraft.getMinecraft().currentScreen instanceof GUIScreenRBMKTerminal) {
			GUIScreenRBMKTerminal gui = (GUIScreenRBMKTerminal) Minecraft.getMinecraft().currentScreen;
			return gui.line.getText();
		}

		return "";
	}

	@Override public void onGuiClosed() { Keyboard.enableRepeatEvents(false); }
	@Override public boolean doesGuiPauseGame() { return false; }
}
