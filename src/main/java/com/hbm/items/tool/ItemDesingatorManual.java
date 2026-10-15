package com.hbm.items.tool;

import java.util.List;

import com.hbm.inventory.gui.GUIScreenDesignator;
import com.hbm.main.MainRegistry;
import com.hbm.tileentity.IGUIProvider;
import com.hbm.util.i18n.I18nUtil;

import api.hbm.item.IDesignatorItem;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

public class ItemDesingatorManual extends Item implements IDesignatorItem, IGUIProvider {

	@Override
	public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player) {

		if(world.isRemote)
			player.openGui(MainRegistry.instance, 0, world, 0, 0, 0);

		return stack;
	}

	@Override
	public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean bool) {
		if(stack.stackTagCompound != null) {
			list.add(I18nUtil.resolveKey("desc.item.desingator.target_coordinates"));
			list.add(I18nUtil.resolveKey("item.tele_link.desc1", stack.stackTagCompound.getInteger("xCoord")));
			list.add(I18nUtil.resolveKey("item.tele_link.desc3", stack.stackTagCompound.getInteger("zCoord")));
		} else {
			list.add(I18nUtil.resolveKey("desc.item.desingator.please_select_target"));
		}
	}

	@Override
	public boolean isReady(World world, ItemStack stack, int x, int y, int z) {
		return stack.hasTagCompound();
	}

	@Override
	public Vec3 getCoords(World world, ItemStack stack, int x, int y, int z) {
		return Vec3.createVectorHelper(stack.stackTagCompound.getInteger("xCoord"), 0, stack.stackTagCompound.getInteger("zCoord"));
	}

	@Override
	public Container provideContainer(int ID, EntityPlayer player, World world, int x, int y, int z) {
		return null;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public Object provideGUI(int ID, EntityPlayer player, World world, int x, int y, int z) {
		return new GUIScreenDesignator(player);
	}
}
