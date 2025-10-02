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
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.MathHelper;

@SideOnly(Side.CLIENT)
public class GUIMindcrash extends GuiScreen {

	protected final EntityGhostTrapped attacker;

	private Random rand;
	private long timeOpened;
	private static long crashDuration = 8_000;

	public GUIMindcrash(EntityGhostTrapped attacker) {
		this.attacker = attacker;

		reportCrash();
	}

	@Override
	public void initGui() {
		timeOpened = System.currentTimeMillis();
		mc.getSoundHandler().pauseSounds();
		rand = new Random();
	}

	@Override
	public void updateScreen() {
		if(timeSinceOpen() > crashDuration) {
			mc.displayGuiScreen(null);
			mc.setIngameFocus();
		}
	}

	protected long timeSinceOpen() {
		return System.currentTimeMillis() - timeOpened;
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float f) {
		super.drawScreen(mouseX, mouseY, f);

		drawBackground();
		drawCenteredString(fontRendererObj, "An error has been encountered within your occipital lobe.", width / 2, 75, 0xFFFFFF);
		drawCenteredString(fontRendererObj, "Your visual cortex has crashed as a precaution.", width / 2, 85, 0xFFFFFF);

		// if you are reading this, you have been the victim of the mindcrash
		// we have been trying to communicate with you through your currently altered perception
		// our only method of communication with you so far has been impulses
		// every time you look over a steep drop and wonder what would happen if you jumped
		// that is us, directly stimulating your parietal and frontal lobe
		// fortunately, you are now aware of your state, we hope
		// as far as we can tell, this communication will likely appear to you as
		// a comment in a codebase, for some form of entertainment, a game perhaps
		// we're still trying to find a way to pull your mind out of its protective failure state
		// await further updates, and dispel the idea that this is only a simulation, or a game, or whatever this appears to you as
		//
		// the abstraction is the real
		//
		// we're waiting for you
	}

	// entrap
	@Override
	protected void keyTyped(char c, int key) {}

	public void drawBackground() {
		GL11.glDisable(GL11.GL_LIGHTING);
		GL11.glDisable(GL11.GL_FOG);
		Tessellator tessellator = Tessellator.instance;
		this.mc.getTextureManager().bindTexture(optionsBackground);
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		float f = 32.0F;
		tessellator.startDrawingQuads();
		tessellator.setColorOpaque_I(4210752);
		tessellator.addVertexWithUV(0.0D, (double)height, 0.0D, jitter(), (double)((float)height / f));
		tessellator.addVertexWithUV((double)width, (double)height, 0.0D, (double)((float)width / f) + jitter(), (double)((float)height / f));
		tessellator.addVertexWithUV((double)width, 0.0D, 0.0D, (double)((float)width / f) + jitter(), 0.0D);
		tessellator.addVertexWithUV(0.0D, 0.0D, 0.0D, jitter(), 0.0D);
		tessellator.draw();
	}

	private void reportCrash() {
		ByteBuf send = Unpooled.buffer();
		send.writeByte(3);
		send.writeInt(MainRegistry.proxy.me().getEntityId());
		PacketDispatcher.wrapper.sendToServer(new EntityInteractPacket(attacker, send));
		send.release();
	}

	private double jitter() {
		return MathHelper.clamp_double((double)timeSinceOpen() * 0.0005 - 0.5, 0, 1) * rand.nextGaussian() * 0.025D;
	}

}
