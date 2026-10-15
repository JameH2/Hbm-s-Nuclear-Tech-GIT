package com.hbm.items.bomb;

import com.hbm.util.i18n.I18nUtil;

import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;

public class ItemPrototypeBlock extends ItemBlock {

	public ItemPrototypeBlock(Block p_i45328_1_) {
		super(p_i45328_1_);
	}

	@Override
	public void addInformation(ItemStack itemstack, EntityPlayer player, List list, boolean bool)
	{
		list.add(I18nUtil.resolveKey("item.prototype_block.desc1"));
		list.add("");
		list.add(I18nUtil.resolveKey("item.prototype_block.desc2"));

		/*list.add(I18nUtil.resolveKey("desc.item.memory_euphemia"));
		list.add("");
		list.add(I18nUtil.resolveKey("desc.item.rest_spaghetti_never_forgetti"));*/
	}

}
