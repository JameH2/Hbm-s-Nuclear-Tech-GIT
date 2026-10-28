package com.hbm.handler.overlay;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;

import com.hbm.inventory.fluid.FluidType;
import com.hbm.tileentity.network.TileEntityPipeBaseNT;
import com.hbm.tileentity.TileEntityProxyBase;
import com.hbm.util.fauxpointtwelve.BlockPos;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent.ClientTickEvent;
import cpw.mods.fml.common.gameevent.TickEvent.Phase;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.MovingObjectPosition.MovingObjectType;
import net.minecraft.world.World;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.common.util.ForgeDirection;

@SideOnly(Side.CLIENT)
public class FluidDuctHover {

	public static class Duct {

		public final BlockPos pos;
		public final int mask;
		public final int dist;

		public Duct(BlockPos pos, int mask, int dist) {
			this.pos = pos;
			this.mask = mask;
			this.dist = dist;
		}
	}

	public static FluidType hoveredType;
	public static final List<Duct> network = new ArrayList<>();
	private static int revealStart;

	@SubscribeEvent
	public void onClientTick(ClientTickEvent event) {

		if(event.phase != Phase.END) return;

		Minecraft mc = Minecraft.getMinecraft();
		EntityPlayer player = mc.thePlayer;

		if(player == null || mc.theWorld == null || mc.currentScreen != null || !Display.isActive() || mc.gameSettings.hideGUI) {
			clear();
			return;
		}

		MovingObjectPosition mop = mc.objectMouseOver;

		if(mop == null || mop.typeOfHit != MovingObjectType.BLOCK) {
			clear();
			return;
		}

		TileEntity te = mc.theWorld.getTileEntity(mop.blockX, mop.blockY, mop.blockZ);

		if(!(te instanceof TileEntityPipeBaseNT)) {
			clear();
			return;
		}

		FluidType type = ((TileEntityPipeBaseNT) te).getType();

		if(type == null) {
			clear();
			return;
		}

		boolean changedType = hoveredType != type;
		hoveredType = type;
		BlockPos pos = new BlockPos(mop.blockX, mop.blockY, mop.blockZ);

		if(changedType || !inNetwork(pos)) {
			revealStart = player.ticksExisted;
			scan(mc.theWorld, pos, type);
		} else if(player.ticksExisted % 10 == 0) {
			scan(mc.theWorld, pos, type);
		}
	}

	private static void clear() {
		hoveredType = null;
		network.clear();
	}

	private static boolean inNetwork(BlockPos pos) {
		for(Duct duct : network) {
			if(duct.pos.equals(pos)) return true;
		}
		return false;
	}

	private static void scan(World world, BlockPos start, FluidType type) {

		network.clear();
		Map<BlockPos, Integer> dist = new HashMap<>();
		ArrayDeque<BlockPos> open = new ArrayDeque<>();
		dist.put(start, 0);
		open.add(start);

		while(!open.isEmpty() && dist.size() < 4096) {
			BlockPos pos = open.poll();
			int d = dist.get(pos);

			for(ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS) {
				BlockPos next = new BlockPos(pos.getX() + dir.offsetX, pos.getY() + dir.offsetY, pos.getZ() + dir.offsetZ);
				if(dist.containsKey(next)) continue;

				if(!world.blockExists(next.getX(), next.getY(), next.getZ())) continue;
				TileEntity te = world.getTileEntity(next.getX(), next.getY(), next.getZ());
				if(te instanceof TileEntityProxyBase) te = ((TileEntityProxyBase) te).getTE();

				if(te instanceof TileEntityPipeBaseNT && ((TileEntityPipeBaseNT) te).getType() == type) {
					dist.put(next, d + 1);
					open.add(next);
				}
			}
		}

		for(Entry<BlockPos, Integer> entry : dist.entrySet()) {
			BlockPos pos = entry.getKey();
			Block block = world.getBlock(pos.getX(), pos.getY(), pos.getZ());
			block.setBlockBoundsBasedOnState(world, pos.getX(), pos.getY(), pos.getZ());
			int mask = 0;
			if(block.getBlockBoundsMinX() <= 0.001D) mask |= 1;
			if(block.getBlockBoundsMaxX() >= 0.999D) mask |= 2;
			if(block.getBlockBoundsMinY() <= 0.001D) mask |= 4;
			if(block.getBlockBoundsMaxY() >= 0.999D) mask |= 8;
			if(block.getBlockBoundsMinZ() <= 0.001D) mask |= 16;
			if(block.getBlockBoundsMaxZ() >= 0.999D) mask |= 32;

			network.add(new Duct(pos, mask, entry.getValue()));
		}
	}

	private static int bit(ForgeDirection dir) {
		switch(dir) {
		case WEST: return 1;
		case EAST: return 2;
		case DOWN: return 4;
		case UP: return 8;
		case NORTH: return 16;
		case SOUTH: return 32;
		default: return 0;
		}
	}

	private static boolean attached(int mask, ForgeDirection dir) {
		return (mask & bit(dir)) != 0;
	}

	@SubscribeEvent
	public void onRenderWorldLast(RenderWorldLastEvent event) {

		Minecraft mc = Minecraft.getMinecraft();
		if(hoveredType == null || network.isEmpty() || mc.theWorld == null || mc.thePlayer == null || mc.currentScreen != null || mc.gameSettings.hideGUI) return;

		EntityPlayer player = mc.thePlayer;
		double cx = player.prevPosX + (player.posX - player.prevPosX) * event.partialTicks;
		double cy = player.prevPosY + (player.posY - player.prevPosY) * event.partialTicks;
		double cz = player.prevPosZ + (player.posZ - player.prevPosZ) * event.partialTicks;
		int color = hoveredType.getColor();
		float r = (color >> 16 & 0xFF) / 255F;
		float g = (color >> 8 & 0xFF) / 255F;
		float b = (color & 0xFF) / 255F;

		GL11.glPushAttrib(GL11.GL_ENABLE_BIT | GL11.GL_DEPTH_BUFFER_BIT | GL11.GL_COLOR_BUFFER_BIT | GL11.GL_CURRENT_BIT);
		GL11.glPushMatrix();
		GL11.glTranslated(-cx, -cy, -cz);
		GL11.glDisable(GL11.GL_TEXTURE_2D);
		GL11.glDisable(GL11.GL_LIGHTING);
		GL11.glDisable(GL11.GL_CULL_FACE);
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		GL11.glDisable(GL11.GL_DEPTH_TEST);
		GL11.glDepthMask(false);

		float time = player.ticksExisted + event.partialTicks;
		float progress = MathHelper.clamp_float((time - revealStart) / 40F, 0F, 1F);
		float w = 5F * (float) Math.tan(Math.toRadians(mc.gameSettings.fovSetting) / 2D) / (float) mc.displayHeight;
		int maxDist = 0;
		for(Duct duct : network) if(duct.dist > maxDist) maxDist = duct.dist;

		Tessellator tess = Tessellator.instance;
		tess.startDrawingQuads();

		for(Duct duct : network) {
			float reveal = MathHelper.clamp_float(progress * (maxDist + 1F) - duct.dist, 0F, 1F);
			if(reveal <= 0F) continue;

			tess.setColorRGBA_F(r, g, b, (duct.dist == 0 ? 1F : 0.8F) * (0.25F + 0.75F * reveal));
			centerLine(tess, duct.pos, duct.mask, cx, cy, cz, w);
		}

		tess.draw();
		GL11.glDepthMask(true);
		GL11.glDisable(GL11.GL_BLEND);
		GL11.glEnable(GL11.GL_DEPTH_TEST);
		GL11.glEnable(GL11.GL_CULL_FACE);
		GL11.glEnable(GL11.GL_LIGHTING);
		GL11.glEnable(GL11.GL_TEXTURE_2D);
		GL11.glPopMatrix();
		GL11.glPopAttrib();
	}

	private static void centerLine(Tessellator tess, BlockPos pos, int mask, double cx, double cy, double cz, float w) {

		double x = pos.getX() + 0.5D, y = pos.getY() + 0.5D, z = pos.getZ() + 0.5D;
		boolean any = false;

		for(ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS) {
			if(!attached(mask, dir)) continue;
			any = true;
			line(tess, cx, cy, cz, w, x, y, z, x + dir.offsetX * 0.5D, y + dir.offsetY * 0.5D, z + dir.offsetZ * 0.5D);
		}

		if(!any) line(tess, cx, cy, cz, w, x, y - 0.35D, z, x, y + 0.35D, z);
	}

	private static void line(Tessellator tess, double cx, double cy, double cz, float w, double x1, double y1, double z1, double x2, double y2, double z2) {

		double dx = x2 - x1, dy = y2 - y1, dz = z2 - z1;
		double len = Math.sqrt(dx * dx + dy * dy + dz * dz);
		if(len < 1.0E-6D) return;
		dx /= len;
		dy /= len;
		dz /= len;

		double vx = x1 - cx, vy = y1 - cy, vz = z1 - cz;
		double half = Math.sqrt(vx * vx + vy * vy + vz * vz) * w;
		double sx = dy * vz - dz * vy, sy = dz * vx - dx * vz, sz = dx * vy - dy * vx;
		double sl = Math.sqrt(sx * sx + sy * sy + sz * sz);
		if(sl < 1.0E-6D) return;
		double ax = sx * half / sl, ay = sy * half / sl, az = sz * half / sl;

		vx = x2 - cx;
		vy = y2 - cy;
		vz = z2 - cz;
		half = Math.sqrt(vx * vx + vy * vy + vz * vz) * w;
		sx = dy * vz - dz * vy;
		sy = dz * vx - dx * vz;
		sz = dx * vy - dy * vx;
		sl = Math.sqrt(sx * sx + sy * sy + sz * sz);
		if(sl < 1.0E-6D) return;
		double bx = sx * half / sl, by = sy * half / sl, bz = sz * half / sl;

		tess.addVertex(x1 + ax, y1 + ay, z1 + az);
		tess.addVertex(x2 + bx, y2 + by, z2 + bz);
		tess.addVertex(x2 - bx, y2 - by, z2 - bz);
		tess.addVertex(x1 - ax, y1 - ay, z1 - az);
	}

}
