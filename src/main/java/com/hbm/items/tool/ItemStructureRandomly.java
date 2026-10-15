package com.hbm.items.tool;

import java.util.List;

import com.hbm.util.fauxpointtwelve.BlockPos;
import com.hbm.util.i18n.I18nUtil;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.world.World;

public class ItemStructureRandomly extends ItemStructureTool {

	@Override
	public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean ext) {
		super.addInformation(stack, player, list, ext);
		list.add(EnumChatFormatting.YELLOW + I18nUtil.resolveKey("desc.item.structure_randomly.click_print"));
		list.add(EnumChatFormatting.YELLOW + I18nUtil.resolveKey("desc.item.structure_randomly.line_targeted_block"));
	}

	@Override
	protected boolean dualUse() {
		return true;
	}

	@Override
	protected void doTheThing(ItemStack stack, World world, int x, int y, int z) {

		BlockPos pos = this.getAnchor(stack);
		if(pos == null) return;

		int savedX = stack.stackTagCompound.getInteger("x");
		int savedY = stack.stackTagCompound.getInteger("y");
		int savedZ = stack.stackTagCompound.getInteger("z");

		int minX = Math.min(savedX, x) - pos.getX();
		int minY = Math.min(savedY, y) - pos.getY();
		int minZ = Math.min(savedZ, z) - pos.getZ();
		int maxX = Math.max(savedX, x) - pos.getX();
		int maxY = Math.max(savedY, y) - pos.getY();
		int maxZ = Math.max(savedZ, z) - pos.getZ();
		//Assumes the last selected block is the one that all of them are.
		Block b = world.getBlock(x, y, z);

		String line = "randomlyFillWithBlocks(world, box, rand, <limit>, " + minX + ", " + minY + ", " + minZ + ", " + maxX + ", " + maxY + ", " + maxZ + ", " + b.getUnlocalizedName() + ");\n";
		System.out.print(line);
		writeToFile(line);
	}

}
