package com.hbm.blocks.machine;

import com.hbm.blocks.BlockDummyable;
import com.hbm.blocks.ITooltipProvider;
import com.hbm.tileentity.TileEntityProxyCombo;
import com.hbm.tileentity.machine.TileEntityDishControl;
import com.hbm.util.ChatBuilder;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.world.World;
import java.util.List;

public class MachineDishControl extends BlockDummyable implements ITooltipProvider {

	public MachineDishControl(Material p_i45386_1_) {
		super(p_i45386_1_);
	}

	@Override
	public TileEntity createNewTileEntity(World p_149915_1_, int meta) {
		if(meta >= 12) return new TileEntityDishControl();
		return new TileEntityProxyCombo(false, false, false); // no need for extra atm, it's just two blocks
	}

	@Override
	public int[] getDimensions() {
		return new int[] { 0, 0, 0, 0, 0, 1 };
	}

	@Override
	public int getOffset() {
		return 0;
	}

	@Override
	public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float hitX, float hitY, float hitZ) {
		if(world.isRemote) {
			return true;
		} else if(!player.isSneaking()) {
			// Find the controller TileEntity
			int[] pos = findCore(world, x, y, z);

			TileEntity e = world.getTileEntity(pos[0], pos[1], pos[2]);
			if(e instanceof TileEntityDishControl) {
				TileEntityDishControl entityDishControl = (TileEntityDishControl) e;

				// Check if a dish was not assigned
				if(!entityDishControl.isLinked) {
					// No StarDar linked
					player.addChatMessage(ChatBuilder.start("[").color(EnumChatFormatting.DARK_AQUA)
						.nextTranslation(this.getUnlocalizedName() + ".name").color(EnumChatFormatting.DARK_AQUA)
						.next("] ").color(EnumChatFormatting.DARK_AQUA)
						.next("Dish not linked!").color(EnumChatFormatting.RED).flush());

					return false;
				}

				// also check that you're still pointing at a stardar...
				Block block = world.getBlock(entityDishControl.linkPosition[0], entityDishControl.linkPosition[1], entityDishControl.linkPosition[2]);

				if(!(block instanceof MachineStardar)) {
					// Invalid link
					player.addChatMessage(ChatBuilder.start("[").color(EnumChatFormatting.DARK_AQUA)
						.nextTranslation(this.getUnlocalizedName() + ".name").color(EnumChatFormatting.DARK_AQUA)
						.next("] ").color(EnumChatFormatting.DARK_AQUA)
						.next("Missing control target!").color(EnumChatFormatting.RED).flush());

					return false;
				}

				// Get StarDar
				MachineStardar stardarBlock = (MachineStardar) world.getBlock(
					entityDishControl.linkPosition[0],
					entityDishControl.linkPosition[1],
					entityDishControl.linkPosition[2]);

				// Trigger the StarDar UI to open
				return stardarBlock.onBlockActivated(
					world,
					entityDishControl.linkPosition[0],
					entityDishControl.linkPosition[1],
					entityDishControl.linkPosition[2],
					player, side, hitX, hitY, hitZ);
			}

			return false;

		} else {
			return false;
		}
	}

	@SuppressWarnings("rawtypes")
	@Override
	public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean ext) {
		addStandardInfo(stack, player, list, ext);
	}

}
