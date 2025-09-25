package com.hbm.blocks;

import java.util.List;
import java.util.Locale;
import java.util.Random;

import com.hbm.dim.tekto.TTree;
import com.hbm.dim.trait.CBT_Atmosphere;
import com.hbm.handler.atmosphere.IPlantableBreathing;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.lib.RefStrings;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.block.BlockSapling;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.WorldGenAbstractTree;
import net.minecraftforge.common.util.ForgeDirection;

public class BlockNTSapling extends BlockSapling implements IBlockMulti, IPlantableBreathing {

	public static enum EnumSapling {
		VINYL,
		PVC,
	}

	private IIcon[] textures;

	public BlockNTSapling() {
		super();
	}

	@Override
	public boolean canBreathe(CBT_Atmosphere atmosphere) {
		return atmosphere != null && (atmosphere.hasFluid(Fluids.TEKTOAIR, 0.1) || atmosphere.hasFluid(Fluids.CHLORINE, 0.1));
	}

	@Override
	protected boolean canPlaceBlockOn(Block block) {
		return block == ModBlocks.rubber_silt || block == ModBlocks.rubber_grass || block == ModBlocks.rubber_farmland;
	}

	@Override
	public void registerBlockIcons(IIconRegister iconRegister) {
		textures = new IIcon[EnumSapling.values().length];
		for(int i = 0; i < EnumSapling.values().length; i++) {
			EnumSapling sapling = EnumSapling.values()[i];
			textures[i] = iconRegister.registerIcon(RefStrings.MODID + ":sapling_" + sapling.name().toLowerCase(Locale.US));
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public IIcon getIcon(int side, int meta) {
		return textures[meta];
	}

	@Override
	public void getSubBlocks(Item item, CreativeTabs tab, List list) {
		for(int i = 0; i < EnumSapling.values().length; i++) {
			list.add(new ItemStack(item, 1, i));
		}
	}

	@Override
	public boolean canPlaceBlockOnSide(World world, int x, int y, int z, int side) {
		Block soil = world.getBlock(x, y - 1, z);
		return soil == ModBlocks.rubber_grass || soil == ModBlocks.rubber_silt || soil.canSustainPlant(world, x, y - 1, z, ForgeDirection.UP, this);
	}

	@Override
	public boolean canBlockStay(World world, int x, int y, int z) {
		Block soil = world.getBlock(x, y - 1, z);
		return (world.getFullBlockLightValue(x, y, z) >= 8 || world.canBlockSeeTheSky(x, y, z)) && soil != null && soil.canSustainPlant(world, x, y - 1, z, ForgeDirection.UP, this);
	}

	@Override
	public void updateTick(World world, int x, int y, int z, Random random) {
		if(!world.isRemote) {
			if(world.getBlockLightValue(x, y + 1, z) >= 9 && random.nextInt(7) == 0) {
				this.func_149878_d(world, x, y, z, random);
			}
		}
	}

	@Override
	public void func_149878_d(World world, int x, int y, int z, Random rand) {
		int meta = world.getBlockMetadata(x, y, z);
		WorldGenAbstractTree treeGen = new TTree(true, 3, 4, 6, 3, 2, false, ModBlocks.vinyl_log, ModBlocks.pet_leaves);

		switch(meta) {
		case 0:
			treeGen = new TTree(false, 2, 4, 5, 3, 2, false, ModBlocks.vinyl_log, ModBlocks.pet_leaves);
			break;
		case 1:
			treeGen = new TTree(true, 2, 5, 7, 4, 3, false, ModBlocks.pvc_log, ModBlocks.rubber_leaves);
			break;
		}

		if(treeGen != null) {
			world.setBlockToAir(x, y, z);
			if (!treeGen.generate(world, rand, x, y, z)) {
				world.setBlock(x, y, z, this, meta, 2);
			}
		}
	}

	@Override
	public int damageDropped(int meta) {
		return meta;
	}

	@Override
	public int getDamageValue(World world, int x, int y, int z) {
		return world.getBlockMetadata(x, y, z);
	}

	@Override
	public void onBlockPlacedBy(World world, int x, int y, int z, EntityLivingBase entity, ItemStack stack) {
		world.setBlockMetadataWithNotify(x, y, z, stack.getItemDamage(), 2);
	}

	@Override
	public void func_149853_b(World world, Random random, int x, int y, int z) {
		this.func_149878_d(world, x, y, z, random);
	}

	@Override
	public int getSubCount() {
		return EnumSapling.values().length;
	}

	@Override
	public String getUnlocalizedName(ItemStack stack) {
		EnumSapling sapling = EnumSapling.values()[stack.getItemDamage()];
		return super.getUnlocalizedName() + "_" + sapling.name().toLowerCase(Locale.US);
	}

}
