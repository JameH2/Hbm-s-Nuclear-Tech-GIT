package com.hbm.blocks.network;

import api.hbm.conveyor.IConveyorBelt;
import api.hbm.conveyor.IConveyorItem;
import api.hbm.conveyor.IConveyorPackage;
import api.hbm.conveyor.IEnterableBlock;
import com.hbm.blocks.IBlockMultiPass;
import com.hbm.blocks.ITooltipProvider;
import com.hbm.entity.item.EntityMovingConveyorObject;
import com.hbm.entity.item.EntityMovingItem;
import com.hbm.entity.item.EntityMovingPackage;
import com.hbm.items.tool.ItemConveyorWand;
import com.hbm.lib.RefStrings;
import com.hbm.main.MainRegistry;
import com.hbm.module.ModulePatternMatcher;
import com.hbm.render.block.RenderBlockMultipass;
import com.hbm.tileentity.network.TileEntityCraneRouter;
import cpw.mods.fml.common.network.internal.FMLNetworkHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import java.util.ArrayList;
import java.util.List;

public class CraneRouter extends BlockContainer implements IBlockMultiPass, IEnterableBlock, ITooltipProvider {

	@SideOnly(Side.CLIENT) protected IIcon iconOverlay;

	public CraneRouter() {
		super(Material.iron);
		this.setBlockTextureName(RefStrings.MODID + ":crane_in");
	}

	@Override
	public TileEntity createNewTileEntity(World world, int meta) {
		return new TileEntityCraneRouter();
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerBlockIcons(IIconRegister iconRegister) {
		super.registerBlockIcons(iconRegister);
		this.iconOverlay = iconRegister.registerIcon(RefStrings.MODID + ":crane_router_overlay");
	}

	@Override
	@SideOnly(Side.CLIENT)
	public IIcon getIcon(int side, int metadata) {
		return RenderBlockMultipass.currentPass == 0 ? this.blockIcon : this.iconOverlay;
	}

	@Override
	public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float hitX, float hitY, float hitZ) {
		if(player.getHeldItem() != null && player.getHeldItem().getItem() instanceof ItemConveyorWand) {
			return false;
		} else if(world.isRemote) {
			return true;
		} else if(!player.isSneaking()) {
			FMLNetworkHandler.openGui(player, MainRegistry.instance, 0, world, x, y, z);
			return true;
		} else {
			return false;
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public int colorMultiplier(IBlockAccess world, int x, int y, int z) {

		if(RenderBlockMultipass.currentPass == 0)
			return 0xffffff;

		switch(RenderBlockMultipass.currentPass - 1) {
		case 0: return 0xff0000;
		case 1: return 0xff8000;
		case 2: return 0xffff00;
		case 3: return 0x00ff00;
		case 4: return 0x0080ff;
		case 5: return 0x8000ff;
		default: return 0xffffff;
		}
	}

	@Override
	public int getRenderType(){
		return IBlockMultiPass.getRenderType();
	}

	/*
	 * arg arg arg spongeboy me bob i have fooled the system that only allows one tint per pass by disabling all rendered sides except one and rendering multiple passes arg arg arg
	 */
	@Override
	@SideOnly(Side.CLIENT)
	public boolean shouldSideBeRendered(IBlockAccess world, int x, int y, int z, int side) {

		if(RenderBlockMultipass.currentPass == 0)
			return true;

		return side == RenderBlockMultipass.currentPass - 1;
	}

	@Override
	public int getPasses() {
		return 7;
	}

	@Override
	public boolean canItemEnter(World world, int x, int y, int z, ForgeDirection dir, IConveyorItem entity) {
		return entity != null && canRouteAll(world, x, y, z, false, entity.getItemStack());
	}

	@Override
	public boolean canPackageEnter(World world, int x, int y, int z, ForgeDirection dir, IConveyorPackage entity) {
		return entity != null && canRouteAll(world, x, y, z, true, entity.getItemStacks());
	}

	private boolean canRouteAll(World world, int x, int y, int z, boolean packaged, ItemStack... stacks) {
		TileEntity te = world.getTileEntity(x, y, z);
		if(!(te instanceof TileEntityCraneRouter) || stacks == null) return false;

		TileEntityCraneRouter router = (TileEntityCraneRouter) te;
		boolean hasItems = false;

		for(ItemStack stack : stacks) {
			if(stack == null || stack.stackSize <= 0) continue;
			hasItems = true;
			if(getOutputDir(router, stack.copy(), packaged) == ForgeDirection.UNKNOWN) return false;
		}

		return hasItems;
	}

	@Override
	public void onItemEnter(World world, int x, int y, int z, ForgeDirection dir, IConveyorItem entity) {
		List<ItemStack>[] sort = this.sort(world, x, y, z, false, entity.getItemStack());
		double laneOffset = getLaneOffset(x, z, dir, entity);

		for(int i = 0; i < 7; i++) {
			ForgeDirection d = ForgeDirection.getOrientation(i);
			List<ItemStack> list = sort[i];
			
			if(d != ForgeDirection.UNKNOWN) {
				for(ItemStack stack : list) sendOnRoute(world, x, y, z, stack, d, laneOffset);
			}
		}
		
	}

	private double getLaneOffset(int x, int z, ForgeDirection dir, Object entity) {
		double laneOffset = 0;
		if(entity instanceof Entity) {
			Entity moving = (Entity) entity;
			if(dir.offsetX != 0) laneOffset = moving.posZ - (z + 0.5);
			if(dir.offsetZ != 0) laneOffset = moving.posX - (x + 0.5);
		}
		return MathHelper.clamp_double(laneOffset, -0.5, 0.5);
	}

	protected void sendOnRoute(World world, int x, int y, int z, ItemStack item, ForgeDirection dir) {
		sendOnRoute(world, x, y, z, item, dir, 0);
	}

	protected void sendOnRoute(World world, int x, int y, int z, ItemStack item, ForgeDirection dir, double laneOffset) {
		IConveyorBelt belt = null;
		Block block = world.getBlock(x + dir.offsetX, y + dir.offsetY, z + dir.offsetZ);

		if(block instanceof IConveyorBelt) {
			belt = (IConveyorBelt) block;
		}

		if(belt != null) {
			EntityMovingItem moving = new EntityMovingItem(world);
			Vec3 pos = Vec3.createVectorHelper(x + 0.5 + dir.offsetX * 0.55, y + 0.5 + dir.offsetY * 0.55, z + 0.5 + dir.offsetZ * 0.55);
			if(dir.offsetX != 0) pos.zCoord += laneOffset;
			if(dir.offsetZ != 0) pos.xCoord += laneOffset;
			Vec3 snap = belt.getClosestSnappingPosition(world, x + dir.offsetX, y + dir.offsetY, z + dir.offsetZ, pos);
			moving.setPosition(snap.xCoord, snap.yCoord, snap.zCoord);
			moving.setItemStack(item);
			EntityMovingConveyorObject.trySendToConveyor(world,
					x + dir.offsetX, y + dir.offsetY, z + dir.offsetZ, dir.getOpposite(), moving);
		}
	}

	@Override
	public void onPackageEnter(World world, int x, int y, int z, ForgeDirection dir, IConveyorPackage entity) {
		List<ItemStack>[] sort = this.sort(world, x, y, z, true, entity.getItemStacks());
		double laneOffset = getLaneOffset(x, z, dir, entity);

		for(int i = 0; i < 7; i++) {
			ForgeDirection d = ForgeDirection.getOrientation(i);
			List<ItemStack> list = sort[i];
			if(list.isEmpty()) continue;

			if(d != ForgeDirection.UNKNOWN) {
				
				IConveyorBelt belt = null;
				Block block = world.getBlock(x + d.offsetX, y + d.offsetY, z + d.offsetZ);
				if(block instanceof IConveyorBelt) belt = (IConveyorBelt) block;

				if(belt != null) {
					EntityMovingPackage moving = new EntityMovingPackage(world);
					Vec3 pos = Vec3.createVectorHelper(x + 0.5 + d.offsetX * 0.55, y + 0.5 + d.offsetY * 0.55, z + 0.5 + d.offsetZ * 0.55);
					if(d.offsetX != 0) pos.zCoord += laneOffset;
					if(d.offsetZ != 0) pos.xCoord += laneOffset;
					Vec3 snap = belt.getClosestSnappingPosition(world, x + d.offsetX, y + d.offsetY, z + d.offsetZ, pos);
					moving.setPosition(snap.xCoord, snap.yCoord, snap.zCoord);
					moving.setItemStacks(list.toArray(new ItemStack[0]));
					EntityMovingConveyorObject.trySendToConveyor(world,
							x + d.offsetX, y + d.offsetY, z + d.offsetZ, d.getOpposite(), moving);
				}
			}
		}
	}

	@Override
	public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean ext) {
		this.addStandardInfo(stack, player, list, ext);
	}
	
	/** Accepts an arbitrary amount of ItemStacks (either solo or from boxes) and returns an array corresponding with ForgeDirections,
	 * with each direction having a list of items being output in that direction. Index 6 is used for UNKNOWN, i.e. unsortable items.
	 * Returned lists are populated with COPIES of the original stacks. */
	public static List<ItemStack>[] sort(World world, int x, int y, int z, ItemStack... stacks) {
		return sort(world, x, y, z, false, stacks);
	}

	private static List<ItemStack>[] sort(World world, int x, int y, int z, boolean packaged, ItemStack... stacks) {
		TileEntityCraneRouter router = (TileEntityCraneRouter) world.getTileEntity(x, y, z);
		List<ItemStack>[] output = new List[7];
		for(int i = 0; i < 7; i++) output[i] = new ArrayList();
		
		for(ItemStack stack : stacks) {
			if(stack == null) continue;
			ForgeDirection dir = getOutputDir(router, stack.copy(), packaged);
			output[dir.ordinal()].add(stack);
		}
		
		return output;
	}
	
	public static ForgeDirection getOutputDir(TileEntityCraneRouter router, ItemStack stack) {
		return getOutputDir(router, stack, false);
	}

	private static ForgeDirection getOutputDir(TileEntityCraneRouter router, ItemStack stack, boolean packaged) {
		List<ForgeDirection> validDirs = new ArrayList();

		//check filters for all sides
		for(int side = 0; side < 6; side++) {

			ModulePatternMatcher matcher = router.patterns[side];
			int mode = router.modes[side];

			//if the side is disabled or wildcard, skip
			if(mode == router.MODE_NONE || mode == router.MODE_WILDCARD)
				continue;

			boolean matchesFilter = false;

			for(int slot = 0; slot < 5; slot++) {
				ItemStack filter = router.slots[side * 5 + slot];

				if(filter == null)
					continue;

				//the filter kicks in so long as one entry matches
				if(matcher.isValidForFilter(filter, slot, stack)) {
					matchesFilter = true;
					break;
				}
			}

			//add dir if matches with whitelist on or doesn't match with blacklist on
			if((mode == router.MODE_WHITELIST && matchesFilter) || (mode == router.MODE_BLACKLIST && !matchesFilter)) {
				validDirs.add(ForgeDirection.getOrientation(side));
			}
		}

		//if no valid dirs have yet been found, use wildcard
		if(validDirs.isEmpty()) {
			for(int side = 0; side < 6; side++) {
				if(router.modes[side] == router.MODE_WILDCARD) {
					validDirs.add(ForgeDirection.getOrientation(side));
				}
			}
		}

		if(validDirs.isEmpty()) {
			return ForgeDirection.UNKNOWN;
		}

		for(int i = validDirs.size() - 1; i >= 0; i--) {
			if(!isRouteAvailable(router, validDirs.get(i), stack, packaged)) validDirs.remove(i);
		}

		if(validDirs.isEmpty()) {
			return ForgeDirection.UNKNOWN;
		}

		int i = router.getWorldObj().rand.nextInt(validDirs.size());
		return validDirs.get(i);
	}

	private static boolean isRouteAvailable(TileEntityCraneRouter router, ForgeDirection dir, ItemStack stack, boolean packaged) {
		int x = router.xCoord + dir.offsetX;
		int y = router.yCoord + dir.offsetY;
		int z = router.zCoord + dir.offsetZ;

		if(packaged) {
			EntityMovingPackage moving = new EntityMovingPackage(router.getWorldObj());
			moving.setItemStacks(new ItemStack[] { stack });
			return EntityMovingConveyorObject.canSendToConveyor(router.getWorldObj(), x, y, z, dir.getOpposite(), moving);
		}

		EntityMovingItem moving = new EntityMovingItem(router.getWorldObj());
		moving.setItemStack(stack);
		return EntityMovingConveyorObject.canSendToConveyor(router.getWorldObj(), x, y, z, dir.getOpposite(), moving);
	}
}
