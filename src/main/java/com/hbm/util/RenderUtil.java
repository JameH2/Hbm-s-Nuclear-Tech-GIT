package com.hbm.util;

import java.nio.DoubleBuffer;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.Tessellator;

public class RenderUtil {

	public static void renderBlock(Tessellator tessellator) {
		renderBlock(tessellator, 0, 1);
	}

	public static void renderBlock(Tessellator tessellator, double uvMin, double uvMax) {
		tessellator.startDrawingQuads();
		tessellator.addVertexWithUV(-0.5, +0.5, -0.5, uvMax, uvMax);
		tessellator.addVertexWithUV(+0.5, +0.5, -0.5, uvMin, uvMax);
		tessellator.addVertexWithUV(+0.5, -0.5, -0.5, uvMin, uvMin);
		tessellator.addVertexWithUV(-0.5, -0.5, -0.5, uvMax, uvMin);

		tessellator.addVertexWithUV(-0.5, +0.5, +0.5, uvMax, uvMax);
		tessellator.addVertexWithUV(-0.5, +0.5, -0.5, uvMin, uvMax);
		tessellator.addVertexWithUV(-0.5, -0.5, -0.5, uvMin, uvMin);;
		tessellator.addVertexWithUV(-0.5, -0.5, +0.5, uvMax, uvMin);

		tessellator.addVertexWithUV(+0.5, +0.5, +0.5, uvMax, uvMax);
		tessellator.addVertexWithUV(-0.5, +0.5, +0.5, uvMin, uvMax);
		tessellator.addVertexWithUV(-0.5, -0.5, +0.5, uvMin, uvMin);
		tessellator.addVertexWithUV(+0.5, -0.5, +0.5, uvMax, uvMin);

		tessellator.addVertexWithUV(+0.5, +0.5, -0.5, uvMax, uvMax);
		tessellator.addVertexWithUV(+0.5, +0.5, +0.5, uvMin, uvMax);
		tessellator.addVertexWithUV(+0.5, -0.5, +0.5, uvMin, uvMin);
		tessellator.addVertexWithUV(+0.5, -0.5, -0.5, uvMax, uvMin);

		tessellator.addVertexWithUV(-0.5, -0.5, -0.5, uvMax, uvMax);
		tessellator.addVertexWithUV(+0.5, -0.5, -0.5, uvMin, uvMax);
		tessellator.addVertexWithUV(+0.5, -0.5, +0.5, uvMin, uvMin);
		tessellator.addVertexWithUV(-0.5, -0.5, +0.5, uvMax, uvMin);

		tessellator.addVertexWithUV(+0.5, +0.5, -0.5, uvMax, uvMax);
		tessellator.addVertexWithUV(-0.5, +0.5, -0.5, uvMin, uvMax);
		tessellator.addVertexWithUV(-0.5, +0.5, +0.5, uvMin, uvMin);
		tessellator.addVertexWithUV(+0.5, +0.5, +0.5, uvMax, uvMin);
		tessellator.draw();
	}

	private static final DoubleBuffer buffer;

	static {
		buffer = GLAllocation.createDirectByteBuffer(8 * 4).asDoubleBuffer(); // four doubles
	}

	public static void pushClip(double x, double y, double z, double distance) {
		buffer.put(new double[] { x, y, z, distance });
		buffer.rewind();

		GL11.glEnable(GL11.GL_CLIP_PLANE0);
		GL11.glClipPlane(GL11.GL_CLIP_PLANE0, buffer);
	}

	public static void popClip() {
		GL11.glDisable(GL11.GL_CLIP_PLANE0);
	}

}
