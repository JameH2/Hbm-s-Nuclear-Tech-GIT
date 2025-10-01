package com.hbm.inventory.gui;

import org.lwjgl.opengl.GL11;

import com.hbm.entity.mob.EntityGhostTrapped;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiGameOver;
import net.minecraft.client.gui.GuiLabel;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.EnumChatFormatting;

@SideOnly(Side.CLIENT)
public class GUIRefuseDeath extends GuiGameOver {

	// "Oh no! Someone's venturing out into the Void! They'll get totally spoiled!"

	protected final EntityGhostTrapped attacker;

	public GUIRefuseDeath(EntityGhostTrapped attacker) {
		this.attacker = attacker;
	}

	private int lastClicked = -1;
	private String deathMessage = I18n.format("deathScreen.title", new Object[0]);

	public void drawScreen(int mouseX, int mouseY, float f) {
		GL11.glPushMatrix();
		{

			// GL11.glTranslated(0, 0, 100);

			this.drawGradientRect(0, 0, this.width, this.height, 1615855616, -1602211792);

			GL11.glPushMatrix();
			{

				GL11.glScalef(2.0F, 2.0F, 2.0F);
				this.drawCenteredString(this.fontRendererObj, deathMessage, this.width / 2 / 2, 30, 16777215);

			}
			GL11.glPopMatrix();

			this.drawCenteredString(this.fontRendererObj, I18n.format("deathScreen.score", new Object[0]) + ": " + EnumChatFormatting.YELLOW + this.mc.thePlayer.getScore(), this.width / 2, 100, 16777215);

			for(int i = 0; i < this.buttonList.size(); ++i) {
				((GuiButton)this.buttonList.get(i)).drawButton(this.mc, mouseX, mouseY);
			}

			for(int i = 0; i < this.labelList.size(); ++i) {
				((GuiLabel)this.labelList.get(i)).func_146159_a(this.mc, mouseX, mouseY);
			}

		}
		GL11.glPopMatrix();


		mc.entityRenderer.setupCameraTransform(f, 0);

		GL11.glClear(GL11.GL_DEPTH_BUFFER_BIT);

		mc.entityRenderer.enableLightmap(f);
		GL11.glDisable(GL11.GL_ALPHA_TEST);
		RenderManager.instance.renderEntitySimple(attacker, f);
		GL11.glEnable(GL11.GL_ALPHA_TEST);
		mc.entityRenderer.disableLightmap(f);

	}

	@Override
	protected void actionPerformed(GuiButton button) {
		if(lastClicked == button.id) {
			mc.displayGuiScreen(new GUIMindcrash());
		}

		deathMessage = "You aren't dead...?";

		switch (button.id) {
		case 0:
			button.displayString = "I Am - " + button.displayString;
			break;
		case 1:
			button.displayString = "Let Me Go - " + button.displayString;
			break;
		}

		lastClicked = button.id;
	}

}
