package com.hbm.blocks.network;

import api.hbm.conveyor.IConveyorItem;
import api.hbm.conveyor.IConveyorPackage;
import api.hbm.conveyor.IEnterableBlock;
import com.hbm.lib.RefStrings;
import com.hbm.tileentity.network.TileEntityCraneBase;
import com.hbm.tileentity.network.TileEntityCraneBoxer;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

public class CraneBoxer extends BlockCraneBase implements IEnterableBlock {

	public CraneBoxer() {
		super(Material.iron);
	}

	@Override
	public TileEntityCraneBase createNewTileEntity(World world, int meta) {
		return new TileEntityCraneBoxer();
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public void registerBlockIcons(IIconRegister iconRegister) {
		super.registerBlockIcons(iconRegister);
		this.iconOut = iconRegister.registerIcon(RefStrings.MODID + ":crane_box");
		this.iconSideOut = iconRegister.registerIcon(RefStrings.MODID + ":crane_side_box");
		this.iconDirectional = iconRegister.registerIcon(RefStrings.MODID + ":crane_boxer_top");
		this.iconDirectionalUp = iconRegister.registerIcon(RefStrings.MODID + ":crane_boxer_side_up");
		this.iconDirectionalDown = iconRegister.registerIcon(RefStrings.MODID + ":crane_boxer_side_down");
		this.iconDirectionalTurnLeft = iconRegister.registerIcon(RefStrings.MODID + ":crane_boxer_top_left");
		this.iconDirectionalTurnRight = iconRegister.registerIcon(RefStrings.MODID + ":crane_boxer_top_right");
		this.iconDirectionalSideLeftTurnUp = iconRegister.registerIcon(RefStrings.MODID + ":crane_boxer_side_left_turn_up");
		this.iconDirectionalSideRightTurnUp = iconRegister.registerIcon(RefStrings.MODID + ":crane_boxer_side_right_turn_up");
		this.iconDirectionalSideLeftTurnDown = iconRegister.registerIcon(RefStrings.MODID + ":crane_boxer_side_left_turn_down");
		this.iconDirectionalSideRightTurnDown = iconRegister.registerIcon(RefStrings.MODID + ":crane_boxer_side_right_turn_down");
		this.iconDirectionalSideUpTurnLeft = iconRegister.registerIcon(RefStrings.MODID + ":crane_boxer_side_up_turn_left");
		this.iconDirectionalSideUpTurnRight = iconRegister.registerIcon(RefStrings.MODID + ":crane_boxer_side_up_turn_right");
		this.iconDirectionalSideDownTurnLeft = iconRegister.registerIcon(RefStrings.MODID + ":crane_boxer_side_down_turn_left");
		this.iconDirectionalSideDownTurnRight = iconRegister.registerIcon(RefStrings.MODID + ":crane_boxer_side_down_turn_right");
	}

    @Override
	public boolean canItemEnter(World world, int x, int y, int z, ForgeDirection dir, IConveyorItem entity) {
		if(getInputSide(world, x, y, z) != dir || entity == null) return false;

		TileEntity te = world.getTileEntity(x, y, z);
		if(!(te instanceof TileEntityCraneBoxer)) return false;

		TileEntityCraneBoxer boxer = (TileEntityCraneBoxer) te;
		return CraneInserter.canAddAllToInventory(boxer, boxer.getAccessibleSlotsFromSide(dir.ordinal()), dir.ordinal(), entity.getItemStack());
	}

	@Override
	public void onItemEnter(World world, int x, int y, int z, ForgeDirection dir, IConveyorItem entity) {
		TileEntityCraneBoxer boxer = (TileEntityCraneBoxer) world.getTileEntity(x, y, z);
		
		CraneInserter.addToInventory(boxer, boxer.getAccessibleSlotsFromSide(dir.ordinal()), entity.getItemStack(), dir.ordinal());
	}
	
	@Override
	public boolean hasComparatorInputOverride() {
		return true;
	}
	
	@Override
	public int getComparatorInputOverride(World world, int x, int y, int z, int side) {
		return Container.calcRedstoneFromInventory((TileEntityCraneBoxer)world.getTileEntity(x, y, z));
	}

	@Override
	public void breakBlock(World world, int x, int y, int z, Block block, int meta) {
		this.dropContents(world, x, y, z, block, meta, 0, 21);
		super.breakBlock(world, x, y, z, block, meta);
	}

	@Override
	public boolean canPackageEnter(World world, int x, int y, int z, ForgeDirection dir, IConveyorPackage entity) {
		TileEntity te = world.getTileEntity(x, y, z);
		if(!(te instanceof TileEntityCraneBoxer) || entity == null) return false;

		TileEntityCraneBoxer boxer = (TileEntityCraneBoxer) te;
		ForgeDirection accessedSide = getOutputSide(world, x, y, z).getOpposite();
		return CraneInserter.canAddAllToInventory(boxer, boxer.getAccessibleSlotsFromSide(accessedSide.ordinal()), accessedSide.ordinal(), entity.getItemStacks());
	}

	@Override
	public void onPackageEnter(World world, int x, int y, int z, ForgeDirection dir, IConveyorPackage entity) {
		TileEntityCraneBoxer unboxer = (TileEntityCraneBoxer) world.getTileEntity(x, y, z);
		ForgeDirection accessedSide = getOutputSide(world, x, y, z).getOpposite();

		for(ItemStack stack : entity.getItemStacks()) {
			if(stack != null) CraneInserter.addToInventory(unboxer, unboxer.getAccessibleSlotsFromSide(accessedSide.ordinal()), stack, accessedSide.ordinal());
		}
	}
}
