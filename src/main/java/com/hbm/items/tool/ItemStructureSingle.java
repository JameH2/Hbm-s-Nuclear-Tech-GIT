package com.hbm.items.tool;

import java.util.List;

import com.hbm.util.fauxpointtwelve.BlockPos;
import com.hbm.util.i18n.I18nUtil;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.world.World;

public class ItemStructureSingle extends ItemStructureTool {

	@Override
	public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean ext) {
		super.addInformation(stack, player, list, ext);
		list.add(EnumChatFormatting.YELLOW + I18nUtil.resolveKey("item.structure_single.desc1"));
		list.add(EnumChatFormatting.YELLOW + I18nUtil.resolveKey("desc.item.structure_single.line_targted_block_metadata"));
	}

	@Override
	protected void doTheThing(ItemStack stack, World world, int x, int y, int z) {

		BlockPos pos = this.getAnchor(stack);
		if(pos == null) return;

		int ix = x - pos.getX();
		int iy = y - pos.getY();
		int iz = z - pos.getZ();

		Block b = world.getBlock(x, y, z);
		int meta = world.getBlockMetadata(x, y, z);

		String message = "placeBlockAtCurrentPosition(world, " + b.getUnlocalizedName() + ", " + meta + ", " + ix + ", " + iy + ", " + iz + ", box);\n";
		System.out.print(message);
		writeToFile(message);
	}
}
