package com.hbm.blocks.generic;

import java.util.Random;

import com.hbm.blocks.BlockEnumMulti;
import com.hbm.blocks.ModBlocks;
import com.hbm.items.ModItems;

import cpw.mods.fml.client.registry.RenderingRegistry;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.World;

public class BlockRubberCacti extends BlockEnumMulti {

	public BlockRubberCacti(Material material) {
		super(Material.plants, EnumBushType.class, false, true);
	}

	public static enum EnumBushType {
		CACT,
		BUSH,
		FLOWER
	}

	public static int renderIDcact = RenderingRegistry.getNextAvailableRenderId();

	@Override
	public int getRenderType(){
		return renderIDcact;
	}

	@Override
	public boolean isOpaqueCube() {
		return false;
	}

	@Override
	public boolean renderAsNormalBlock() {
		return false;
	}

	@Override
	public boolean canPlaceBlockAt(World world, int x, int y, int z) {
		return super.canPlaceBlockAt(world, x, y, z) && this.canBlockStay(world, x, y, z);
	}

	protected boolean canPlaceBlockOn(Block block) {
		return block == ModBlocks.vinyl_sand || block == ModBlocks.rubber_grass || block == ModBlocks.rubber_silt || block == ModBlocks.rubber_farmland;
	}

	@Override
	public void onNeighborBlockChange(World world, int x, int y, int z, Block block) {
		super.onNeighborBlockChange(world, x, y, z, block);
		this.checkAndDropBlock(world, x, y, z);
	}

	protected void checkAndDropBlock(World world, int x, int y, int z) {
		if(!this.canBlockStay(world, x, y, z)) {
			this.dropBlockAsItem(world, x, y, z, world.getBlockMetadata(x, y, z), 0);
			world.setBlock(x, y, z, getBlockById(0), 0, 2);
		}
	}

	@Override
	public boolean canBlockStay(World world, int x, int y, int z) {
		return canPlaceBlockOn(world.getBlock(x, y - 1, z));
	}

	@Override
	public int getDamageValue(World world, int x, int y, int z) {
		return world.getBlockMetadata(x, y, z) % EnumBushType.values().length;
	}

	@Override
	public int damageDropped(int meta) {
		return meta % EnumBushType.values().length;
	}

	@Override
	public Item getItemDropped(int meta, Random rand, int fortune) {
		if(meta % EnumBushType.values().length == EnumBushType.FLOWER.ordinal()) {
			return ModItems.paraffin_seeds;
		}
		return Item.getItemFromBlock(this);
	}

	@Override
	public void onBlockPlacedBy(World world, int x, int y, int z, EntityLivingBase entity, ItemStack stack) {
		world.setBlockMetadataWithNotify(x, y, z, stack.getItemDamage(), 2);
	}

	@Override
	public AxisAlignedBB getCollisionBoundingBoxFromPool(World world, int x, int y, int z) {
		return null;
	}

}
