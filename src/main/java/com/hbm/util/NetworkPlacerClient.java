package com.hbm.util;

import java.util.ArrayList;
import java.util.List;

import com.hbm.render.util.RenderOverhead;
import com.hbm.tileentity.network.TileEntityPipeBaseNT;
import com.hbm.util.fauxpointtwelve.BlockPos;
import com.hbm.wiaj.WorldInAJar;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.MovingObjectPosition.MovingObjectType;
import net.minecraft.world.World;

/**
 * Client-side companion to NetworkPlacer: builds a WorldInAJar preview of the
 * line that would be placed, anchored to the player's aim (mc.objectMouseOver).
 */
public class NetworkPlacerClient {

	public static void preview(ItemStack stack, EntityPlayer player) {

		World world = player.worldObj;

		if(!NetworkPlacer.hasPending(stack)) {
			clear();
			return;
		}

		Minecraft mc = Minecraft.getMinecraft();
		MovingObjectPosition mop = mc.objectMouseOver;

		if(mop == null || mop.typeOfHit != MovingObjectType.BLOCK) {
			clear();
			return;
		}

		int side = mop.sideHit;

		NBTTagCompound nbt = stack.stackTagCompound;
		int sx = nbt.getInteger("nw_x");
		int sy = nbt.getInteger("nw_y");
		int sz = nbt.getInteger("nw_z");
		int meta = nbt.getInteger("nw_meta");
		int fluid = nbt.getInteger("nw_itemFluid") > 0 ? nbt.getInteger("nw_itemFluid") : nbt.getInteger("nw_fluid");

		Block block = NetworkPlacer.blockOf(stack);
		BlockPos end = NetworkPlacer.placePos(world, block, mop.blockX, mop.blockY, mop.blockZ, side);
		List<BlockPos> path = NetworkPlacer.calcRoute(world, sx, sy, sz, end.getX(), end.getY(), end.getZ(), 0);

		if(path == null) {
			clear();
			return;
		}

		int minX = Integer.MAX_VALUE, minY = Integer.MAX_VALUE, minZ = Integer.MAX_VALUE;
		int maxX = Integer.MIN_VALUE, maxY = Integer.MIN_VALUE, maxZ = Integer.MIN_VALUE;

		for(BlockPos pos : path) {
			minX = Math.min(minX, pos.getX());
			minY = Math.min(minY, pos.getY());
			minZ = Math.min(minZ, pos.getZ());
			maxX = Math.max(maxX, pos.getX());
			maxY = Math.max(maxY, pos.getY());
			maxZ = Math.max(maxZ, pos.getZ());
		}

		minX--;
		minY--;
		minZ--;

		WorldInAJar wiaj = new WorldInAJar(maxX - minX + 2, maxY - minY + 2, maxZ - minZ + 2);
		List<TileEntity> nodes = new ArrayList<>();
		boolean success = true;

		for(BlockPos pos : path) {
			Block existing = world.getBlock(pos.getX(), pos.getY(), pos.getZ());
			TileEntity te = null;

			if(existing.isReplaceable(world, pos.getX(), pos.getY(), pos.getZ())) {
				int lx = pos.getX() - minX;
				int ly = pos.getY() - minY;
				int lz = pos.getZ() - minZ;
				int placed = NetworkPlacer.placedMeta(world, player, block, meta, pos.getX(), pos.getY(), pos.getZ(), nbt.getInteger("nw_side"));
				wiaj.setBlock(lx, ly, lz, block, placed);

				try {
					te = block.createTileEntity(null, placed);
					if(te != null) {
						if(te instanceof TileEntityPipeBaseNT && fluid > 0) {
							NBTTagCompound tileData = new NBTTagCompound();
							te.writeToNBT(tileData);
							tileData.setInteger("type", fluid);
							te.readFromNBT(tileData);
						}
						te.xCoord = pos.getX();
						te.yCoord = pos.getY();
						te.zCoord = pos.getZ();
						te.blockMetadata = placed;
						te.blockType = block;
						wiaj.setTileEntity(lx, ly, lz, te);
					}
				} catch(Exception ex) { }
			} else {
				success = false;
			}

			nodes.add(te);
		}

		for(TileEntity te : nodes) if(te != null) te.setWorldObj(world);

		RenderOverhead.setActionPreview(wiaj, minX, minY, minZ, success);
	}

	public static void clear() {
		RenderOverhead.clearActionPreview();
	}
}
