package com.hbm.blocks.generic;

import java.util.Random;
import java.util.Set;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import com.hbm.blocks.IBlockMulti;
import com.hbm.blocks.IBlockMultiPass;
import com.hbm.blocks.ITooltipProvider;
import com.hbm.blocks.ModBlocks;
import com.hbm.dim.SolarSystem;
import com.hbm.items.ItemEnums.EnumChunkType;
import com.hbm.items.ModItems;
import com.hbm.render.block.RenderBlockMultipass;
import com.hbm.util.I18nUtil;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class BlockOre extends Block implements IBlockMultiPass, IBlockMulti, ITooltipProvider {

	// Any ore can be placed on any planet, with a planet enum to set the stone type
	protected IIcon[] stoneIcons;

	// I'd rather a system that both spawns _and_ marks spawnable, to ensure we don't get any desync
	// but that aint trivial for the moment
	public Set<SolarSystem.Body> spawnsOn = new HashSet<>();

	public BlockOre(Material mat) {
		super(mat);
	}

	public static void addValidBody(Block ore, SolarSystem.Body body) {
		if(!(ore instanceof BlockOre)) return;
		((BlockOre) ore).spawnsOn.add(body);
	}

	public static void addAllBodies(Block ore) {
		for(SolarSystem.Body celestial : SolarSystem.Body.values()) {
			addValidBody(ore, celestial);
		}
	}

	public static void addAllExcept(Block ore, SolarSystem.Body body) {
		for(SolarSystem.Body celestial : SolarSystem.Body.values()) {
			if(celestial == body) continue;
			addValidBody(ore, celestial);
		}
	}

	// mappings from vanilla ores (for tooltips)
	public static Map<Block, BlockOre> vanillaMap = new HashMap<>();

	public BlockOre(Material mat, Block vanillaBlock) {
		this(mat);
		vanillaMap.put(vanillaBlock, this);
	}

	@Override
	public Item getItemDropped(int i, Random rand, int j) {
		if(this == ModBlocks.ore_fluorite) {
			return ModItems.fluorite;
		}
		if(this == ModBlocks.ore_niter) {
			return ModItems.niter;
		}
		if(this == ModBlocks.ore_sulfur) {
			return ModItems.sulfur;
		}
		if(this == ModBlocks.ore_glowstone) {
			return Items.glowstone_dust;
		}
		if(this == ModBlocks.ore_fire) {
			return rand != null && rand.nextInt(10) == 0 ? ModItems.ingot_phosphorus : ModItems.powder_fire;
		}
		if(this == ModBlocks.ore_rare) {
			return ModItems.chunk_ore;
		}
		if(this == ModBlocks.ore_asbestos) {
			return ModItems.ingot_asbestos;
		}
		if(this == ModBlocks.ore_lignite) {
			return ModItems.lignite;
		}
		if(this == ModBlocks.ore_cinnebar) {
			return ModItems.cinnebar;
		}
		if(this == ModBlocks.ore_coltan) {
			return ModItems.fragment_coltan;
		}
		if(this == ModBlocks.ore_cobalt) {
			return ModItems.fragment_cobalt;
		}
		// Vanilla reproduction
		if(this == ModBlocks.ore_redstone) {
			return Items.redstone;
		}
		if(this == ModBlocks.ore_lapis) {
			return Items.dye;
		}
		if(this == ModBlocks.ore_emerald) {
			return Items.emerald;
		}
		if(this == ModBlocks.ore_quartz) {
			return Items.quartz;
		}
		if(this == ModBlocks.ore_diamond) {
			return Items.diamond;
		}

		return Item.getItemFromBlock(this);
	}

	@Override
	public int quantityDropped(Random rand) {
		if(this == ModBlocks.ore_fluorite) {
			return 2 + rand.nextInt(3);
		}
		if(this == ModBlocks.ore_niter) {
			return 2 + rand.nextInt(3);
		}
		if(this == ModBlocks.ore_sulfur) {
			return 2 + rand.nextInt(3);
		}
		if(this == ModBlocks.ore_glowstone) {
			return 1 + rand.nextInt(3);
		}
		if(this == ModBlocks.ore_cobalt) {
			return 4 + rand.nextInt(6);
		}
		if(this == ModBlocks.ore_redstone) {
			return 4 + rand.nextInt(2);
		}
		if(this == ModBlocks.ore_lapis) {
			return 4 + rand.nextInt(5);
		}
		return 1;
	}

	public boolean allowFortune = true;

	public BlockOre noFortune() {
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

	@Override
	@SideOnly(Side.CLIENT)
	public void registerBlockIcons(IIconRegister reg) {
		// blockIcon is the ore texture
		super.registerBlockIcons(reg);

		stoneIcons = new IIcon[SolarSystem.Body.values().length];
		stoneIcons[0] = reg.registerIcon("stone");

		for(int i = 1; i < SolarSystem.Body.values().length; i++) {
			SolarSystem.Body body = SolarSystem.Body.values()[i];
			stoneIcons[i] = reg.registerIcon(body.getStoneTexture());
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public IIcon getIcon(IBlockAccess world, int x, int y, int z, int side) {
		if(RenderBlockMultipass.currentPass == 0) {
			int meta = world.getBlockMetadata(x, y, z);
			return stoneIcons[meta % stoneIcons.length];
		}

		return blockIcon;
	}

	@Override
    @SideOnly(Side.CLIENT)
    public IIcon getIcon(int side, int meta) {
		if(RenderBlockMultipass.currentPass == 0) {
			return stoneIcons[meta % stoneIcons.length];
		}

		return blockIcon;
    }

	@Override
	public int damageDropped(int meta) {
		if(this == ModBlocks.ore_rare) return EnumChunkType.RARE.ordinal();
		if(this == ModBlocks.ore_lapis) return 4;
		if(getItemDropped(0, null, 0) != Item.getItemFromBlock(this)) return 0;
		return rectify(meta);
	}

	@Override
	public void onNeighborBlockChange(World world, int x, int y, int z, Block block) {
		int meta = world.getBlockMetadata(x, y, z);

		if(world.getBlock(x, y - 1, z) == ModBlocks.ore_oil_empty) {
			world.setBlock(x, y, z, ModBlocks.ore_oil_empty, meta, 3);
			world.setBlock(x, y - 1, z, ModBlocks.ore_oil, meta, 3);
		}
		if(world.getBlock(x, y - 1, z) == ModBlocks.ore_gas_empty) {
			world.setBlock(x, y, z, ModBlocks.ore_gas_empty, meta, 3);
			world.setBlock(x, y - 1, z, ModBlocks.ore_gas, meta, 3);
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void getSubBlocks(Item item, CreativeTabs tab, List list) {
		for(int i = 0; i < getSubCount(); i++)
			list.add(new ItemStack(item, 1, i));
	}

	@Override
	public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean ext) {
		if(spawnsOn.isEmpty()) return;

		if(spawnsOn.size() == SolarSystem.Body.values().length) {
			list.add(EnumChatFormatting.GOLD + "Can be found anywhere");
			return;
		} else if(spawnsOn.size() == SolarSystem.Body.values().length - 1) {
			list.add(EnumChatFormatting.GOLD + "Can be found anywhere except:");
			for(SolarSystem.Body body : SolarSystem.Body.values()) {
				if(spawnsOn.contains(body)) continue;
				list.add(EnumChatFormatting.RED + " - " + I18nUtil.resolveKey("body." + body.name));
			}
			return;
		}

		list.add(EnumChatFormatting.GOLD + "Can be found on:");
		for(SolarSystem.Body body : spawnsOn) {
			list.add(EnumChatFormatting.AQUA + " - " + I18nUtil.resolveKey("body." + body.name));
		}
	}

	public EnumRarity getRarity(ItemStack stack) {
		if(this == ModBlocks.ore_australium) return EnumRarity.uncommon;
		if(this == ModBlocks.ore_rare) return EnumRarity.uncommon;
		return EnumRarity.common;
	}

	@Override
    public void onBlockPlacedBy(World world, int x, int y, int z, EntityLivingBase player, ItemStack stack) {
		int meta = stack.getItemDamage();
		world.setBlockMetadataWithNotify(x, y, z, meta, 2);
	}

	@Override
	public int getPasses() {
		return 2;
	}

	@Override
	public boolean shouldRenderItemMulti() {
		return true;
	}

	@Override
	public int getRenderType() {
		return IBlockMultiPass.getRenderType();
	}

	@Override
	public int getSubCount() {
		return SolarSystem.Body.values().length;
	}

}
