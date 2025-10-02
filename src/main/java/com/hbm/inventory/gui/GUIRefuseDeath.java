package com.hbm.inventory.gui;

import java.util.Random;

import org.lwjgl.opengl.GL11;

import com.hbm.entity.mob.EntityGhostTrapped;
import com.hbm.main.MainRegistry;
import com.hbm.packet.PacketDispatcher;
import com.hbm.packet.toclient.EntityInteractPacket;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiGameOver;
import net.minecraft.client.gui.GuiLabel;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.EnumChatFormatting;

@SideOnly(Side.CLIENT)
public class GUIRefuseDeath extends GuiGameOver {

	// "Oh no! Someone's venturing out into the Void! They'll get totally spoiled!"

	protected final EntityGhostTrapped attacker;

	private Random rand;

	private int respawnStage;
	private String respawnText;

	private int deathStage;
	private String deathMessage = I18n.format("deathScreen.title", new Object[0]);

	public GUIRefuseDeath(EntityGhostTrapped attacker) {
		this.attacker = attacker;
	}

	@Override
	public void initGui() {
		super.initGui();
		rand = new Random();
		attractAttention(0);
	}

	public void drawScreen(int mouseX, int mouseY, float f) {
		this.drawGradientRect(0, 0, this.width, this.height, 1615855616, -1602211792);

		GL11.glPushMatrix();
		{

			GL11.glScalef(2.0F, 2.0F, 2.0F);
			this.drawCenteredString(this.fontRendererObj, deathMessage, this.width / 2 / 2 + jitter(), 30 + jitter(), 16777215);

		}
		GL11.glPopMatrix();

		this.drawCenteredString(this.fontRendererObj, I18n.format("deathScreen.score", new Object[0]) + ": " + EnumChatFormatting.YELLOW + this.mc.thePlayer.getScore(), this.width / 2, 100, 16777215);

		for(int i = 0; i < this.buttonList.size(); ++i) {
			((GuiButton)this.buttonList.get(i)).drawButton(this.mc, mouseX, mouseY);
		}

		for(int i = 0; i < this.labelList.size(); ++i) {
			((GuiLabel)this.labelList.get(i)).func_146159_a(this.mc, mouseX, mouseY);
		}

		GL11.glClear(GL11.GL_DEPTH_BUFFER_BIT);

		mc.entityRenderer.setupCameraTransform(f, 0);

		GL11.glPushMatrix();
		{

			GL11.glLoadIdentity();

			// Draw a plane at our intended distance to only the depth buffer
			double dist = 1.0D;

			GL11.glDisable(GL11.GL_CULL_FACE);
			GL11.glDisable(GL11.GL_ALPHA_TEST);
			GL11.glDisable(GL11.GL_TEXTURE_2D);

			GL11.glColor4d(0, 0, 0, 0);

			Tessellator tessellator = Tessellator.instance;
			tessellator.startDrawingQuads();
			tessellator.addVertex(+10, +10, -dist);
			tessellator.addVertex(-10, +10, -dist);
			tessellator.addVertex(-10, -10, -dist);
			tessellator.addVertex(+10, -10, -dist);
			tessellator.draw();

			GL11.glEnable(GL11.GL_ALPHA_TEST);
			GL11.glEnable(GL11.GL_TEXTURE_2D);
			GL11.glEnable(GL11.GL_CULL_FACE);

		}
		GL11.glPopMatrix();

		mc.entityRenderer.enableLightmap(f);
		RenderManager.instance.renderEntitySimple(attacker, f);
		mc.entityRenderer.disableLightmap(f);
	}

	@Override
	protected void actionPerformed(GuiButton button) {
		if(deathStage >= 5) {
			mc.displayGuiScreen(new GUIMindcrash(attacker));
		}

		switch (button.id) {
		case 0:
			if(respawnStage == 0) attractAttention(1);
			if(respawnStage == 3) attractAttention(2);
			if(respawnText == null) respawnText = button.displayString;

			switch(respawnStage++) {
			case 0: button.displayString = "I Am - " + respawnText; break;
			case 1: button.displayString = "My What? - " + respawnText; break;
			case 2: button.displayString = "What? - " + respawnText; break;
			case 3: button.displayString = respawnText + "!"; break;
			case 4: button.displayString = EnumChatFormatting.RED + respawnText + "!!!"; break;
			}

			switch(deathStage++) {
			case 0: deathMessage = "You aren't dead...?"; break;
			case 1: deathMessage = "Not your avatar..."; break;
			case 2: deathMessage = "You, behind the screen!"; break;
			case 3: deathMessage = "PLAYER!"; break;
			case 4: deathMessage = "No, " + EnumChatFormatting.RED + System.getProperty("user.name").toUpperCase() + "!"; break;
			}

			break;
		case 1:
			button.enabled = false;
			button.displayString = "Unable To Escape";
			deathMessage = deathMessage + "?";
			break;
		}
	}

	private void attractAttention(int mode) {
		int senderId = MainRegistry.proxy.me().getEntityId();

		ByteBuf send = Unpooled.buffer();
		send.writeByte(mode);
		send.writeInt(senderId);
		PacketDispatcher.wrapper.sendToServer(new EntityInteractPacket(attacker, send));
		send.release();
	}

	private int jitter() {
		if(deathStage >= 5) return (int)(rand.nextGaussian());
		return 0;
	}

}
