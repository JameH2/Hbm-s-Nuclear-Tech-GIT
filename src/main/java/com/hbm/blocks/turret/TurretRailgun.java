package com.hbm.blocks.turret;

import com.hbm.blocks.BlockDummyable;
import com.hbm.blocks.ITooltipProvider;
import com.hbm.tileentity.TileEntityProxyCombo;
import com.hbm.tileentity.turret.TileEntityTurretRailgun;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import java.util.List;

public class TurretRailgun extends BlockDummyable implements ITooltipProvider {
	public TurretRailgun(Material mat) {
		super(mat);
	}

	@Override
	public int[] getDimensions() {
		return new int[] { 2, 0, 2, 2, 2, 2 };
	}

	@Override
	public int getOffset() {
		return 0;
	}

	@Override
	public TileEntity createNewTileEntity(World world, int i) {
		if (i >= 12) return new TileEntityTurretRailgun();
		if (i >= 6) return new TileEntityProxyCombo().inventory().power();
		return null;
	}

	@Override
	public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float hitX, float hitY, float hitZ) {
		return this.standardOpenBehavior(world, x, y, z, player, 0);
	}

	@Override
	public void fillSpace(World world, int x, int y, int z, ForgeDirection dir, int o) {
		super.fillSpace(world, x, y, z, dir, o);
		this.makeExtra(world, x + dir.offsetX * o + 2, y, z + dir.offsetZ * o);
		this.makeExtra(world, x + dir.offsetX * o - 2, y, z + dir.offsetZ * o);
		this.makeExtra(world, x + dir.offsetX * o, y, z + dir.offsetZ * o + 2);
		this.makeExtra(world, x + dir.offsetX * o, y, z + dir.offsetZ * o - 2);
	}

	@Override
	public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean ext) {
		this.addStandardInfo(stack, player, list, ext);
	}
}
