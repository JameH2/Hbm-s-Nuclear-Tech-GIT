package com.hbm.items.block;

import com.hbm.util.i18n.I18nUtil;

import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;

public class ItemGlyphBlock extends ItemBlock {

	public ItemGlyphBlock(Block block) {
		super(block);
        this.setMaxDamage(0);
        this.setHasSubtypes(true);
	}

	@Override
	public void addInformation(ItemStack itemstack, EntityPlayer player, List list, boolean bool) {

		switch(itemstack.getItemDamage()) {
		case 0: list.add(I18nUtil.resolveKey("item.glyph_block.hourglass")); break;
		case 1: list.add(I18nUtil.resolveKey("item.glyph_block.eye")); break;
		case 2: list.add(I18nUtil.resolveKey("item.glyph_block.pillar")); break;
		case 3: list.add(I18nUtil.resolveKey("item.glyph_block.ioi")); break;
		case 4: list.add(I18nUtil.resolveKey("item.glyph_block.delta")); break;
		case 5: list.add(I18nUtil.resolveKey("item.glyph_block.vtpc")); break;
		case 6: list.add(I18nUtil.resolveKey("item.glyph_block.cool_s")); break;
		case 7: list.add(I18nUtil.resolveKey("item.glyph_block.trefoil")); break;
		case 8: list.add(I18nUtil.resolveKey("item.glyph_block.pony")); break;
		case 9: list.add(I18nUtil.resolveKey("item.glyph_block.sparkle")); break;
		case 10: list.add(I18nUtil.resolveKey("item.glyph_block.pip")); break;
		case 11: list.add(I18nUtil.resolveKey("item.glyph_block.triangles")); break;
		case 12: list.add(I18nUtil.resolveKey("item.glyph_block.linux_mint")); break;
		case 13: list.add("13"); break;
		case 14: list.add(I18nUtil.resolveKey("item.glyph_block.digamma")); break;
		case 15: list.add(I18nUtil.resolveKey("item.glyph_block.celestial_altar")); break;
		}
	}

    public int getMetadata(int meta)
    {
        return meta;
    }
}
