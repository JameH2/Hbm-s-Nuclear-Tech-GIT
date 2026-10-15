package com.hbm.util.i18n;

import java.util.List;

import com.hbm.main.MainRegistry;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

public class I18nUtil {
	public static final String ITEM_NAME_KEY = "hbmI18nName";
	public static final String ITEM_LORE_KEY = "hbmI18nLore";


	/**
	 * Simple wrapper for I18n, for consistency
	 * @param s
	 * @param args
	 * @return
	 */
	public static String resolveKey(String s, Object... args) {	return MainRegistry.proxy.getI18n().resolveKey(s, args); }
	public static String format(String s, Object... args) {		return MainRegistry.proxy.getI18n().resolveKey(s, args); } //alias

	public static ItemStack setItemName(ItemStack stack, String key) {
		if(!stack.hasTagCompound()) stack.setTagCompound(new NBTTagCompound());
		stack.getTagCompound().setString(ITEM_NAME_KEY, key);
		return stack;
	}

	/**
	 * Wrapper for I18n but cuts up the result using NTM's line break character ($)
	 * @param s
	 * @param args
	 * @return
	 */
	public static String[] resolveKeyArray(String s, Object... args) {
		return MainRegistry.proxy.getI18n().resolveKeyArray(s, args);
	}

	/**
	 * The same as autoBreak, but it also respects NTM's break character ($) for manual line breaking in addition to the automatic ones
	 * @param fontRenderer
	 * @param text
	 * @param width
	 * @return
	 */
	public static List<String> autoBreakWithParagraphs(Object fontRenderer, String text, int width) {
		return MainRegistry.proxy.getI18n().autoBreakWithParagraphs(fontRenderer, text, width);
	}

	/**
	 * Turns one string into a list of strings, cutting sentences up to fit within the defined width if they were rendered in a GUI
	 * @param fontRenderer
	 * @param text
	 * @param width
	 * @return
	 */
	public static List<String> autoBreak(Object fontRenderer, String text, int width) {
		return MainRegistry.proxy.getI18n().autoBreak(fontRenderer, text, width);
	}
}
