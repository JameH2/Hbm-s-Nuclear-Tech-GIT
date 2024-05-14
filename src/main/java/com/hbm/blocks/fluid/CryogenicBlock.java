package com.hbm.blocks.fluid;

import java.util.List;
import java.util.Random;

import com.hbm.blocks.ModBlocks;
import com.hbm.extprop.HbmLivingProps;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.lib.ModDamageSource;
import com.hbm.lib.RefStrings;
import com.hbm.main.MainRegistry;
import com.hbm.util.ContaminationUtil;
import com.hbm.util.EntityDamageUtil;
import com.hbm.util.ContaminationUtil.ContaminationType;
import com.hbm.util.ContaminationUtil.HazardType;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.block.BlockBush;
import net.minecraft.block.BlockLeaves;
import net.minecraft.block.BlockLog;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.BlockFluidClassic;
import net.minecraftforge.fluids.Fluid;

public class CryogenicBlock extends BlockFluidClassic {

	@SideOnly(Side.CLIENT)
	public static IIcon stillIcon;
	@SideOnly(Side.CLIENT)
	public static IIcon flowingIcon;
	public Random rand = new Random();
	
	private String stillName;
	private String flowingName;

	public float damage;
	public DamageSource damageSource;

	public CryogenicBlock(Fluid fluid, Material material, String still, String flowing) {
		super(fluid, material);
		setCreativeTab(null);
		stillName = still;
		flowingName = flowing;
		displacements.put(this, false);
	}

	public CryogenicBlock setDamage(String source, float amount) {
		damageSource = new DamageSource(source);
		damage = amount;
		return this;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public IIcon getIcon(int side, int meta) {
		return (side == 0 || side == 1) ? stillIcon : flowingIcon;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerBlockIcons(IIconRegister register) {
		stillIcon = register.registerIcon(RefStrings.MODID + ":" + stillName);
		flowingIcon = register.registerIcon(RefStrings.MODID + ":" + flowingName);
	}

	@Override
	public void onEntityCollidedWithBlock(World world, int x, int y, int z, Entity entity) {
		
		if(damageSource != null && entity instanceof EntityLivingBase) {
			EntityLivingBase living = (EntityLivingBase)entity;
			if(!world.isRemote) {
				HbmLivingProps.setTemperature(living, HbmLivingProps.getTemperature(living) + Fluids.NITROGEN.temperature / 20);
				
				if(HbmLivingProps.isFrozen(living)) {
					if(!EntityDamageUtil.attackEntityFromIgnoreIFrame(entity, new DamageSource(ModDamageSource.s_cryolator), living.getMaxHealth() * -Fluids.NITROGEN.temperature / 273 * 0.01F))
						living.attackEntityFrom(new DamageSource(ModDamageSource.s_cryolator), living.getMaxHealth() * -Fluids.NITROGEN.temperature / 273);
					living.addPotionEffect(new PotionEffect(Potion.moveSlowdown.id, 100, 2));
					living.addPotionEffect(new PotionEffect(Potion.digSlowdown.id, 100, 4));
				}			
			}
			
			if(entity.ticksExisted % 5 == 0) {
				world.playSoundAtEntity(entity, "random.fizz", 0.2F, 1F);
				world.spawnParticle("cloud", entity.posX, entity.posY, entity.posZ, 0.0, 0.1, 0.0);
			}
		}
	}

	@Override
	public void onBlockAdded(World world, int x, int y, int z) {
		super.onBlockAdded(world, x, y, z);
		
		for(ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS) {

			freeze(world, x + dir.offsetX, y + dir.offsetY, z + dir.offsetZ);
		}
	}
	
	@Override
	public void onNeighborBlockChange(World world, int x, int y, int z, Block block) {
		super.onNeighborBlockChange(world, x, y, z, block);
		
		for(ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS) {

			freeze(world, x + dir.offsetX, y + dir.offsetY, z + dir.offsetZ);
		}
	}

	@Override
	public int tickRate(World p_149738_1_) {
		return 15;
	}

	@SideOnly(Side.CLIENT)
	public void randomDisplayTick(World world, int x, int y, int z, Random rand) {
		super.randomDisplayTick(world, x, y, z, rand);

		double ix = x + 0.5F + rand.nextDouble() * 2 - 1D;
		double iy = y + 1;
		double iz = z + 0.5F + rand.nextDouble() * 2 - 1D;

		if(world.getBlock(x, y+1, z)==Blocks.air && rand.nextInt(8)==0)
		{
			NBTTagCompound fx = new NBTTagCompound();
			fx.setString("type", "tower");
			fx.setFloat("lift", 0.25F);
			fx.setFloat("base", 0.75F);
			fx.setFloat("max", 1F);
			fx.setInteger("life", 65 + world.rand.nextInt(10));
			fx.setInteger("color",0xffffff);
			fx.setDouble("posX", ix);
			fx.setDouble("posY", iy);
			fx.setDouble("posZ", iz);
			MainRegistry.proxy.effectNT(fx);
		}
	}
	
	public static void freeze(World world, int x, int y, int z) {
		Block b = world.getBlock(x, y, z);
			if(b == Blocks.grass) {
				world.setBlock(x, y, z, ModBlocks.frozen_grass);
			} else if(b instanceof BlockBush) {
				world.setBlock(x, y, z, Blocks.air);
			} else if(b instanceof BlockLog) {
				int meta = world.getBlockMetadata(x, y, z);
				world.setBlock(x, y, z, ModBlocks.frozen_log, meta, 2);
			} else if(b instanceof BlockLeaves) {
				world.setBlock(x, y, z, ModBlocks.frozen_leaves);
			} else if(b == Blocks.water) {
				world.setBlock(x, y, z, Blocks.ice);
			} else if(b == Blocks.dirt) {
				world.setBlock(x, y, z, ModBlocks.frozen_dirt);
			} else if(b == Blocks.gravel) {
				world.setBlock(x, y, z, ModBlocks.frozen_gravel);
			} else if(b == Blocks.farmland) {
				world.setBlock(x, y, z, ModBlocks.frozen_farmland);
			} else if(b == Blocks.planks) {
				world.setBlock(x, y, z, ModBlocks.frozen_planks);
			}	
	}	
}
