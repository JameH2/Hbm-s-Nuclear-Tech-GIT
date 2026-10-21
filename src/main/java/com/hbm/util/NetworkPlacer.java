package com.hbm.util;

import java.util.ArrayList;
import java.util.List;

import com.hbm.blocks.BlockDummyable;
import com.hbm.blocks.ModBlocks;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.tileentity.network.TileEntityPipeBaseNT;
import com.hbm.util.fauxpointtwelve.BlockPos;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.MathHelper;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

/**
 * Conveyor-like placement for fluid ducts and cables.
 * Sneak-click to set a start position, then click again elsewhere to build a whole line
 * along the path between them (stepping over obstacles), consuming items from the inventory.
 * Adapted from the IQ edition; pipe anchors and other span blocks are intentionally not supported.
 */
public class NetworkPlacer {

	private static final String SET = "nw_set";

	public static boolean handle(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side, Block block, int meta, int fluid) {

		if(!isLine(block)) return false;
		if(!player.canPlayerEdit(x, y, z, side, stack)) return false;

		if(player.isSneaking()) {
			if(hasPending(stack)) {
				clearPending(stack);
				if(world.isRemote) player.addChatMessage(new ChatComponentText("Selection cleared"));
				return true;
			}
		} else if(!hasPending(stack)) {
			return false;
		}

		if(!hasPending(stack)) {
			if(!stack.hasTagCompound()) stack.stackTagCompound = new NBTTagCompound();
			NBTTagCompound nbt = stack.stackTagCompound;
			nbt.setBoolean(SET, true);
			nbt.setInteger("nw_side", side);
			nbt.setInteger("nw_count", count(player, stack));

			BlockPos pos = placePos(world, block, x, y, z, side);
			nbt.setInteger("nw_x", pos.getX());
			nbt.setInteger("nw_y", pos.getY());
			nbt.setInteger("nw_z", pos.getZ());

			TileEntity te = world.getTileEntity(x, y, z);
			if(te instanceof TileEntityPipeBaseNT) nbt.setInteger("nw_fluid", ((TileEntityPipeBaseNT) te).getType().getID());
			nbt.setInteger("nw_itemFluid", fluid);
			nbt.setInteger("nw_meta", meta);

			if(world.isRemote) player.addChatMessage(new ChatComponentText("Start set"));
			return true;
		}

		NBTTagCompound nbt = stack.stackTagCompound;
		int sx = nbt.getInteger("nw_x");
		int sy = nbt.getInteger("nw_y");
		int sz = nbt.getInteger("nw_z");
		int sSide = nbt.getInteger("nw_side");
		int max = count(player, stack);
		int startFluid = nbt.getInteger("nw_fluid");
		clearPending(stack);

		if(!world.isRemote) {
			BlockPos end = placePos(world, block, x, y, z, side);
			TileEntity endTe = world.getTileEntity(x, y, z);
			int type = fluid > 0 ? fluid : startFluid;
			if(type <= 0 && endTe instanceof TileEntityPipeBaseNT) type = ((TileEntityPipeBaseNT) endTe).getType().getID();

			List<BlockPos> path = calcRoute(world, sx, sy, sz, end.getX(), end.getY(), end.getZ(), 0);

			if(path == null) {
				player.addChatMessage(new ChatComponentText("Cannot build here"));
				return true;
			}

			int needed = 0;

			for(BlockPos pos : path) {
				if(!player.canPlayerEdit(pos.getX(), pos.getY(), pos.getZ(), sSide, stack)
						|| !world.blockExists(pos.getX(), pos.getY(), pos.getZ())
						|| !world.canPlaceEntityOnSide(block, pos.getX(), pos.getY(), pos.getZ(), false, sSide, player, stack)) {
					player.addChatMessage(new ChatComponentText("Cannot build here"));
					return true;
				}
				if(!world.getBlock(pos.getX(), pos.getY(), pos.getZ()).isReplaceable(world, pos.getX(), pos.getY(), pos.getZ())) {
					player.addChatMessage(new ChatComponentText("Obstructed, build cancelled"));
					return true;
				}
				needed++;
			}

			if(needed > max) {
				player.addChatMessage(new ChatComponentText("Not enough blocks, build cancelled"));
				return true;
			}

			for(BlockPos pos : path) {
				place(world, player, block, meta, pos.getX(), pos.getY(), pos.getZ(), sSide);

				if(type > 0) {
					TileEntity te = world.getTileEntity(pos.getX(), pos.getY(), pos.getZ());
					if(te instanceof TileEntityPipeBaseNT) ((TileEntityPipeBaseNT) te).setType(Fluids.fromID(type));
				}
			}

			if(needed > 0) {
				consume(player, stack, needed);
				player.inventory.markDirty();
				player.addChatMessage(new ChatComponentText("Placed " + needed + " block(s)"));
			}
		}

		return true;
	}

	public static void update(ItemStack stack, EntityPlayer player, boolean inHand) {
		if(!isLine(blockOf(stack))) return;

		if(!inHand && hasPending(stack)) {
			ItemStack held = player.getHeldItem();

			if(held == null || held.getItem() != stack.getItem() || held.getItemDamage() != stack.getItemDamage()) {
				clearPending(stack);

				if(player.worldObj.isRemote) NetworkPlacerClient.clear();
			}
		}

		if(player.worldObj.isRemote && inHand) NetworkPlacerClient.preview(stack, player);
	}

	public static boolean hasPending(ItemStack stack) {
		return stack.stackTagCompound != null && stack.stackTagCompound.getBoolean(SET);
	}

	public static void addInformation(List list) {
		list.add(StatCollector.translateToLocal("desc.network_placer.start"));
		list.add(StatCollector.translateToLocal("desc.network_placer.finish"));
		list.add(StatCollector.translateToLocal("desc.network_placer.cancel"));
	}

	private static void clearPending(ItemStack stack) {
		if(!stack.hasTagCompound()) return;
		for(String key : new String[] {SET, "nw_side", "nw_count", "nw_x", "nw_y", "nw_z", "nw_fluid", "nw_itemFluid", "nw_meta"}) {
			stack.stackTagCompound.removeTag(key);
		}
		if(stack.stackTagCompound.hasNoTags()) stack.stackTagCompound = null;
	}

	public static Block blockOf(ItemStack stack) {
		if(stack.getItem() instanceof com.hbm.items.machine.ItemFluidDuct) return ModBlocks.fluid_duct_neo;
		Block block = Block.getBlockFromItem(stack.getItem());
		return block != null ? block : ModBlocks.fluid_duct_neo;
	}

	public static BlockPos placePos(World world, Block block, int x, int y, int z, int side) {

		return new BlockPos(x + off(side, 0), y + off(side, 1), z + off(side, 2));
	}

	public static List<BlockPos> calcPath(int x1, int y1, int z1, int x2, int y2, int z2) {

		List<BlockPos> path = new ArrayList<>();

		int x = x1, y = y1, z = z1;
		path.add(new BlockPos(x, y, z));

		while(x != x2) { x += x2 > x ? 1 : -1; path.add(new BlockPos(x, y, z)); }
		while(z != z2) { z += z2 > z ? 1 : -1; path.add(new BlockPos(x, y, z)); }
		while(y != y2) { y += y2 > y ? 1 : -1; path.add(new BlockPos(x, y, z)); }

		return path;
	}

	public static List<BlockPos> calcRoute(World world, int x1, int y1, int z1, int x2, int y2, int z2, double range) {
		if(Math.abs((long) x2 - x1) + Math.abs((long) y2 - y1) + Math.abs((long) z2 - z1) > 256) return null;

		List<BlockPos> path = calcPath(x1, y1, z1, x2, y2, z2);

		List<BlockPos> route = new ArrayList<>();
		route.add(path.get(0));

		BlockPos cur = path.get(0);
		int elev = 0;

		for(int i = 1; i < path.size(); i++) {
			BlockPos ideal = path.get(i);
			int next = elev;

			while(!isFree(world, ideal.add(0, next, 0))) {
				if(++next > 16) return null;
			}

			while(elev < next) {
				cur = cur.add(0, 1, 0);
				if(!isFree(world, cur)) return null;
				route.add(cur);
				elev++;
			}

			cur = ideal.add(0, elev, 0);
			route.add(cur);

			while(elev > 0 && isFree(world, ideal.add(0, elev - 1, 0))) {
				elev--;
				cur = ideal.add(0, elev, 0);
				route.add(cur);
			}
		}

		return new ArrayList<>(new java.util.LinkedHashSet<>(route));
	}

	public static boolean isFree(World world, BlockPos pos) {
		return world.blockExists(pos.getX(), pos.getY(), pos.getZ()) && world.getBlock(pos.getX(), pos.getY(), pos.getZ()).isReplaceable(world, pos.getX(), pos.getY(), pos.getZ());
	}

	public static int placedMeta(World world, EntityPlayer player, Block block, int meta, int x, int y, int z, int side) {

		if(!(block instanceof BlockDummyable)) return block.onBlockPlaced(world, x, y, z, side, 0.5F, 0.5F, 0.5F, meta);

		ForgeDirection placed = ForgeDirection.getOrientation(side);
		if(placed != ForgeDirection.UP && placed != ForgeDirection.DOWN) return BlockDummyable.offset + placed.ordinal();

		int i = MathHelper.floor_double(player.rotationYaw * 4.0F / 360.0F + 0.5D) & 3;
		return BlockDummyable.offset + (i == 0 ? 2 : i == 1 ? 5 : i == 2 ? 3 : 4);
	}

	public static boolean isLine(Block block) {
		return block == ModBlocks.fluid_duct_neo || block == ModBlocks.fluid_duct_box || block == ModBlocks.fluid_duct_exhaust || block == ModBlocks.fluid_duct_paintable || block == ModBlocks.fluid_duct_gauge || block == ModBlocks.fluid_duct_paintable_block_exhaust
				|| block == ModBlocks.red_cable || block == ModBlocks.red_cable_classic || block == ModBlocks.red_cable_paintable || block == ModBlocks.red_cable_gauge || block == ModBlocks.red_cable_box || block == ModBlocks.red_wire_coated;
	}

	private static void place(World world, EntityPlayer player, Block block, int meta, int x, int y, int z, int side) {

		world.setBlock(x, y, z, block);
		int placedMeta = block.onBlockPlaced(world, x, y, z, side, 0.5F, 0.5F, 0.5F, meta);
		world.setBlockMetadataWithNotify(x, y, z, placedMeta, 3);
		block.onBlockPlacedBy(world, x, y, z, player, new ItemStack(block, 1, meta));
	}

	private static int off(int side, int axis) {
		switch(side) {
		case 0: return axis == 1 ? -1 : 0;
		case 1: return axis == 1 ? 1 : 0;
		case 2: return axis == 2 ? -1 : 0;
		case 3: return axis == 2 ? 1 : 0;
		case 4: return axis == 0 ? -1 : 0;
		case 5: return axis == 0 ? 1 : 0;
		default: return 0;
		}
	}

	private static int count(EntityPlayer player, ItemStack stack) {

		if(player.capabilities.isCreativeMode) return 256;

		int count = 0;

		for(ItemStack inventoryStack : player.inventory.mainInventory) {
			if(inventoryStack != null && inventoryStack.getItem() == stack.getItem() && inventoryStack.getItemDamage() == stack.getItemDamage()) count += inventoryStack.stackSize;
		}

		return count;
	}

	private static void consume(EntityPlayer player, ItemStack stack, int amount) {

		if(player.capabilities.isCreativeMode) return;

		for(ItemStack inventoryStack : player.inventory.mainInventory) {
			if(inventoryStack == null || inventoryStack.getItem() != stack.getItem() || inventoryStack.getItemDamage() != stack.getItemDamage()) continue;

			int remove = Math.min(amount, inventoryStack.stackSize);
			inventoryStack.stackSize -= remove;
			amount -= remove;

			if(amount <= 0) break;
		}

		player.inventoryContainer.detectAndSendChanges();
	}
}
