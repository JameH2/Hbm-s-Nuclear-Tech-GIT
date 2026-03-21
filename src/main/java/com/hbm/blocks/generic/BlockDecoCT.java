package com.hbm.blocks.generic;

import java.util.Random;

import com.hbm.blocks.ModBlocks;
import com.hbm.render.block.ct.CT;
import com.hbm.render.block.ct.CTStitchReceiver;
import com.hbm.render.block.ct.IBlockCT;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.item.Item;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;

public class BlockDecoCT extends Block implements IBlockCT {
	public boolean allowFortune = true;

	public BlockDecoCT(Material mat) {
		super(mat);
	}

	@Override
	public int getRenderType() {
		return CT.renderID;
	}

	@SideOnly(Side.CLIENT)
	public CTStitchReceiver rec;

	@SideOnly(Side.CLIENT)
	public void registerBlockIcons(IIconRegister reg) {
		this.blockIcon = reg.registerIcon(this.getTextureName());
		this.rec = IBlockCT.primeReceiver(reg, this.getTextureName(), this.blockIcon);
	}

	@Override
	public IIcon[] getFragments(IBlockAccess world, int x, int y, int z) {
		return rec.fragCache;
	}

	@Override
	public boolean canConnect(IBlockAccess world, int x, int y, int z, Block block) {
		// i don't care
		if(this == ModBlocks.deco_steel && block == ModBlocks.deco_rusty_steel) return true;
		if(this == ModBlocks.deco_rusty_steel && block == ModBlocks.deco_steel) return true;
		return this == block;
	}
	
	public BlockDecoCT noFortune() {
		this.allowFortune = false;
		return this;
	}
	
	@Override
	public int quantityDroppedWithBonus(int fortune, Random rand) {

		if(fortune > 0 && Item.getItemFromBlock(this) != this.getItemDropped(0, rand, fortune) && allowFortune) {
			int mult = rand.nextInt(fortune + 2) - 1;

			if(mult < 0) {
				mult = 0;
			}

			return this.quantityDropped(rand) * (mult + 1);
		} else {
			return this.quantityDropped(rand);
		}
	}
}
