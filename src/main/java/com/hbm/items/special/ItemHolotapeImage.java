package com.hbm.items.special;

import java.util.List;

import com.hbm.inventory.gui.GUIScreenHolotape;
import com.hbm.main.MainRegistry;
import com.hbm.tileentity.IGUIProvider;
import com.hbm.util.EnumUtil;
import com.hbm.util.i18n.I18nUtil;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.world.World;

public class ItemHolotapeImage extends ItemHoloTape implements IGUIProvider {

	public ItemHolotapeImage() {
		super(EnumHoloImage.class, false, false);
	}

	@Override
	public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player) {
		if(world.isRemote) player.openGui(MainRegistry.instance, 0, world, 0, 0, 0);
		return stack;
	}

	@Override
	public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean ext) {

		EnumHoloImage holo = EnumUtil.grabEnumSafely(EnumHoloImage.class, stack.getItemDamage());
		list.add(I18nUtil.resolveKey("desc.item.holotape_image.band_color", holo.colorCode, I18nUtil.resolveKey(holo.colorName)));
		list.add(I18nUtil.resolveKey("item.holotape_image.desc2", holo.name));
	}

	public static enum EnumHoloImage {
		HOLO_DIGAMMA(		EnumChatFormatting.RED,			"holotape.holo_digamma.color",	"D#",				"holotape.holo_digamma.text"),
		HOLO_RESTORED(		EnumChatFormatting.RED,			"holotape.holo_restored.color",	"D0",				"holotape.holo_restored.text"),
		HOLO_FE_HALL(		EnumChatFormatting.GREEN,		"holotape.holo_fe_hall.color",		"001-HALL"	,		"holotape.holo_fe_hall.text"),
		HOLO_FE_CORRIDOR(	EnumChatFormatting.GREEN,		"holotape.holo_fe_corridor.color",		"002-CORRIDOR",		"holotape.holo_fe_corridor.text"),
		HOLO_FE_SERVER(		EnumChatFormatting.GREEN,		"holotape.holo_fe_server.color",		"003-SERVER",		"holotape.holo_fe_server.text"),
		HOLO_FEH_DOME(		EnumChatFormatting.RED,			"holotape.holo_feh_dome.color",		"011-DOME",			"holotape.holo_feh_dome.text"),
		HOLO_FEH_BOAT(		EnumChatFormatting.RED,			"holotape.holo_feh_boat.color",		"012-BOAT",			"holotape.holo_feh_boat.text"),
		HOLO_FEH_LSC(		EnumChatFormatting.RED,			"holotape.holo_feh_lsc.color",		"013-LAUNCH",		"holotape.holo_feh_lsc.text"),
		HOLO_F3_RC(			EnumChatFormatting.DARK_GREEN,	"holotape.holo_f3_rc.color",	"021-RIVET",		"holotape.holo_f3_rc.text"),
		HOLO_F3_IV(			EnumChatFormatting.DARK_GREEN,	"holotape.holo_f3_iv.color",	"022-V87",			"holotape.holo_f3_iv.text"),
		HOLO_F3_WM(			EnumChatFormatting.DARK_GREEN,	"holotape.holo_f3_wm.color",	"023-MONUMENT",		"holotape.holo_f3_wm.text"),
		HOLO_NV_CRATER(		EnumChatFormatting.GOLD,		"holotape.holo_nv_crater.color",	"031-MOUNTAIN",		"holotape.holo_nv_crater.text"),
		HOLO_NV_DIVIDE(		EnumChatFormatting.GOLD,		"holotape.holo_nv_divide.color",	"032-ROAD",			"holotape.holo_nv_divide.text"),
		HOLO_NV_BM(			EnumChatFormatting.GOLD,		"holotape.holo_nv_bm.color",	"033-BROADCAST",	"holotape.holo_nv_bm.text"),
		HOLO_O_1(			EnumChatFormatting.WHITE,		"holotape.holo_o_1.color",	"X00-TRANSCRIPT",	"holotape.holo_o_1.text"),
		HOLO_O_2(			EnumChatFormatting.WHITE,		"holotape.holo_o_2.color",	"X01-NEWS",			"holotape.holo_o_2.text"),
		HOLO_O_3(			EnumChatFormatting.WHITE,		"holotape.holo_o_3.color",	"X02-FICTION",		"holotape.holo_o_3.text"),
		HOLO_CHALLENGE(		EnumChatFormatting.GRAY,		"holotape.holo_challenge.color",		"-",				"holotape.holo_challenge.text"),
		;

		private String name;
		private String text;
		private String colorName;
		private EnumChatFormatting colorCode;

		private EnumHoloImage(EnumChatFormatting colorCode, String colorName, String name, String text) {
			this.name = name;
			this.text = text;
			this.colorName = colorName;
			this.colorCode = colorCode;
		}

		public String getText() {
			return I18nUtil.resolveKey(this.text);
		}
	}

	@Override
	public Container provideContainer(int ID, EntityPlayer player, World world, int x, int y, int z) {
		return null;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public Object provideGUI(int ID, EntityPlayer player, World world, int x, int y, int z) {
		return new GUIScreenHolotape();
	}
}
