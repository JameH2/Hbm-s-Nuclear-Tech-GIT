package com.hbm.blocks.generic;

import com.hbm.blocks.IStepTickReceiver;
import com.hbm.blocks.ITooltipProvider;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;

import java.util.List;
import com.hbm.util.i18n.I18nUtil;

public class BlockSpeedyStairs extends BlockGenericStairs implements IStepTickReceiver, ITooltipProvider {

	double speed;

	public BlockSpeedyStairs(Block block, int meta, double speed) {
		super(block, meta);
		this.speed = speed;
	}

	@Override
	public void onPlayerStep(World world, int x, int y, int z, EntityPlayer player) {

		if(!world.isRemote)
			return;

		if(player.moveForward != 0 || player.moveStrafing != 0) {
			player.motionX *= speed;
			player.motionZ *= speed;
		}
	}

	@Override
	public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean ext) {
		list.add((EnumChatFormatting.BLUE) + I18nUtil.resolveKey("tile.speedy_stairs.increases_speed_by", (MathHelper.floor_double((speed - 1) * 100))));
	}
}
