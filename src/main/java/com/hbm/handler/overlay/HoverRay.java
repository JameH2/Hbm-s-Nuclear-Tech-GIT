package com.hbm.handler.overlay;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.Minecraft;
import net.minecraft.util.MovingObjectPosition;

/** Use the normal game target; no free-cursor camera or input handling is needed. */
@SideOnly(Side.CLIENT)
public class HoverRay {

	public static MovingObjectPosition raycast(Minecraft mc, double range) {
		return mc.objectMouseOver;
	}
}
