package com.hbm.items.block;

import java.util.List;

import com.hbm.blocks.IBlockMulti;
import com.hbm.blocks.IPersistentInfoProvider;
import com.hbm.blocks.ITooltipProvider;
import com.hbm.blocks.generic.BlockMetalFence;
import com.hbm.tileentity.IPersistentNBT;
import com.hbm.util.NetworkPlacer;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.IIcon;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;

public class ItemBlockBase extends ItemBlock {
	
	public ItemBlockBase(Block block) {
		super(block);
		
		if(block instanceof IBlockMulti) {
			this.setMaxDamage(0);
			this.setHasSubtypes(true);
		}
	}
	
	@Override
	public boolean onItemUse(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side, float fx, float fy, float fz) {

		if(NetworkPlacer.handle(stack, player, world, x, y, z, side, this.field_150939_a, this.getMetadata(stack.getItemDamage()), 0)) return true;

		return super.onItemUse(stack, player, world, x, y, z, side, fx, fy, fz);
	}

	@Override
	public void onUpdate(ItemStack stack, World world, Entity entity, int slot, boolean inHand) {

		super.onUpdate(stack, world, entity, slot, inHand);

		if(entity instanceof EntityPlayer) NetworkPlacer.update(stack, (EntityPlayer) entity, inHand);
	}

	@Override
	public int getMetadata(int meta) {
		if(field_150939_a instanceof IBlockMulti)
			return meta;
		else
			return super.getMetadata(meta);
	}
	
	@Override
	public String getUnlocalizedName(ItemStack stack) {
		
		if(field_150939_a instanceof IBlockMulti) {
			return ((IBlockMulti) field_150939_a).getUnlocalizedName(stack);
		} else if(field_150939_a instanceof BlockMetalFence) {
			return ((BlockMetalFence) field_150939_a).getUnlocalizedName(stack); // I considered reworking IBlockMulti instead but there are like a bajillion implementers
		} else {
			return super.getUnlocalizedName(stack);
		}
	}

	@Override
	public String getItemStackDisplayName(ItemStack stack) {
		if(field_150939_a instanceof IBlockMulti) {
			String override = ((IBlockMulti) field_150939_a).getOverrideDisplayName(stack);
			if(override != null) {
				return override;
			}
		}
		return ("" + StatCollector.translateToLocal(this.getUnlocalizedNameInefficiently(stack) + ".name")).trim();
	}
	
	@Override
	public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean bool) {
		if(NetworkPlacer.isLine(this.field_150939_a)) NetworkPlacer.addInformation(list);
		
		if(field_150939_a instanceof ITooltipProvider) {
			((ITooltipProvider) field_150939_a).addInformation(stack, player, list, bool);
		}
		
		if(field_150939_a instanceof IPersistentInfoProvider && stack.hasTagCompound() && stack.getTagCompound().hasKey(IPersistentNBT.NBT_PERSISTENT_KEY)) {
			NBTTagCompound data = stack.getTagCompound().getCompoundTag(IPersistentNBT.NBT_PERSISTENT_KEY);
			((IPersistentInfoProvider) field_150939_a).addInformation(stack, data, player, list, bool);
		}
	}

	@SideOnly(Side.CLIENT)
	public IIcon getIconFromDamage(int meta) {
		return this.field_150939_a.getIcon(1, meta); //fuck you mojang
	}

	@Override
	public EnumRarity getRarity(ItemStack stack) {
		
		if(field_150939_a instanceof ITooltipProvider) {
			return ((ITooltipProvider) field_150939_a).getRarity(stack);
		}
		
		return EnumRarity.common;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public int getColorFromItemStack(ItemStack stack, int pass) {
		return this.field_150939_a.getRenderColor(stack.getItemDamage());
	}
}
