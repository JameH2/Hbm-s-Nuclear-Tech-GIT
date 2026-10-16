package com.hbm.inventory.gui;

import org.lwjgl.opengl.GL11;

import com.hbm.blocks.bomb.NukeCustom;
import com.hbm.inventory.container.ContainerNukeCustom;
import com.hbm.lib.RefStrings;
import com.hbm.tileentity.bomb.TileEntityNukeCustom;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.ResourceLocation;
import com.hbm.util.i18n.I18nUtil;

public class GUINukeCustom extends GuiInfoContainer {

	private static ResourceLocation texture = new ResourceLocation(RefStrings.MODID + ":textures/gui/weapon/gunBombSchematic.png");
	private TileEntityNukeCustom testNuke;

	public GUINukeCustom(InventoryPlayer invPlayer, TileEntityNukeCustom tedf) {
		super(new ContainerNukeCustom(invPlayer, tedf));
		testNuke = tedf;

		this.xSize = 176;
		this.ySize = 222;
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float f) {
		super.drawScreen(mouseX, mouseY, f);

		String[] text;

		text = new String[] { EnumChatFormatting.YELLOW + I18nUtil.resolveKey("gui.nuke_custom.conventional") + testNuke.tnt + "/" + Math.min(testNuke.tnt, NukeCustom.maxTnt) + ")",
				I18nUtil.resolveKey("gui.nuke_custom.caps_at") + NukeCustom.maxTnt,
				I18nUtil.resolveKey("gui.nuke_custom.conventional_lore"),
				EnumChatFormatting.ITALIC + I18nUtil.resolveKey("gui.nuke_custom.goes_boom") };
		this.drawCustomInfoStat(mouseX, mouseY, guiLeft + 16, guiTop + 89, 18, 18, mouseX, mouseY, text);

		text = new String[] { EnumChatFormatting.YELLOW + I18nUtil.resolveKey("gui.nuke_custom.nuclear_level") + testNuke.nuke + "/" + testNuke.getNukeAdj() + ")",
				I18nUtil.resolveKey("gui.nuke_custom.tnt_requirement"),
				I18nUtil.resolveKey("gui.nuke_custom.caps_at") + NukeCustom.maxNuke,
				I18nUtil.resolveKey("gui.nuke_custom.has_fallout"),
				EnumChatFormatting.ITALIC + I18nUtil.resolveKey("gui.nuke_custom.nuclear") };
		this.drawCustomInfoStat(mouseX, mouseY, guiLeft + 34, guiTop + 89, 18, 18, mouseX, mouseY, text);

		text = new String[] { EnumChatFormatting.YELLOW + I18nUtil.resolveKey("gui.nuke_custom.thermonuclear_level") + testNuke.hydro + "/" + testNuke.getHydroAdj() + ")",
				I18nUtil.resolveKey("gui.nuke_custom.nuclear_requirement"),
				I18nUtil.resolveKey("gui.nuke_custom.caps_at") + NukeCustom.maxHydro,
				I18nUtil.resolveKey("gui.nuke_custom.fallout_reduction"),
				EnumChatFormatting.ITALIC + I18nUtil.resolveKey("gui.nuke_custom.thermonuclear_lore1"),
				EnumChatFormatting.ITALIC + I18nUtil.resolveKey("gui.nuke_custom.thermonuclear_lore2") };
		this.drawCustomInfoStat(mouseX, mouseY, guiLeft + 52, guiTop + 89, 18, 18, mouseX, mouseY, text);

		text = new String[] { EnumChatFormatting.YELLOW + I18nUtil.resolveKey("gui.nuke_custom.antimatter_level") + testNuke.amat + "/" + testNuke.getAmatAdj() + ")",
				I18nUtil.resolveKey("gui.nuke_custom.caps_at") + NukeCustom.maxAmat,
				EnumChatFormatting.ITALIC + I18nUtil.resolveKey("gui.nuke_custom.exotic") };
		this.drawCustomInfoStat(mouseX, mouseY, guiLeft + 70, guiTop + 89, 18, 18, mouseX, mouseY, text);

		text = new String[] { EnumChatFormatting.YELLOW + I18nUtil.resolveKey("gui.nuke_custom.salted_level") + testNuke.dirty + "/" + Math.min(testNuke.dirty, 100) + ")",
				I18nUtil.resolveKey("gui.nuke_custom.fallout_extension"),
				I18nUtil.resolveKey("gui.nuke_custom.thermonuclear_stages"),
				I18nUtil.resolveKey("gui.nuke_custom.fallout_limit"),
				EnumChatFormatting.ITALIC + I18nUtil.resolveKey("gui.nuke_custom.confused_tablesalt") };
		this.drawCustomInfoStat(mouseX, mouseY, guiLeft + 88, guiTop + 89, 18, 18, mouseX, mouseY, text);

		text = new String[] { EnumChatFormatting.YELLOW + I18nUtil.resolveKey("gui.nuke_custom.schrabidium_level") + testNuke.schrab + "/" + testNuke.getSchrabAdj() + ")",
				I18nUtil.resolveKey("gui.nuke_custom.nuclear_requirement50"),
				I18nUtil.resolveKey("gui.nuke_custom.caps_at") + NukeCustom.maxSchrab,
				EnumChatFormatting.ITALIC + I18nUtil.resolveKey("gui.nuke_custom.cap_warning1"),
				EnumChatFormatting.ITALIC + I18nUtil.resolveKey("gui.nuke_custom.cap_warning2") };
		this.drawCustomInfoStat(mouseX, mouseY, guiLeft + 106, guiTop + 89, 18, 18, mouseX, mouseY, text);

		text = new String[] { EnumChatFormatting.YELLOW + I18nUtil.resolveKey("gui.nuke_custom.ice_cream"),
				EnumChatFormatting.ITALIC + I18nUtil.resolveKey("gui.nuke_custom.ice_cream_lore") };
		this.drawCustomInfoStat(mouseX, mouseY, guiLeft + 142, guiTop + 89, 18, 18, mouseX, mouseY, text);
	}

	@Override
	protected void drawGuiContainerForegroundLayer( int i, int j) {
		String name = this.testNuke.hasCustomInventoryName() ? this.testNuke.getInventoryName() : I18n.format(this.testNuke.getInventoryName());

		this.fontRendererObj.drawString(name, this.xSize / 2 - this.fontRendererObj.getStringWidth(name) / 2, 6, 4210752);
		this.fontRendererObj.drawString(I18n.format("container.inventory"), 8, this.ySize - 96 + 2, 4210752);
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float p_146976_1_, int p_146976_2_, int p_146976_3_) {
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		Minecraft.getMinecraft().getTextureManager().bindTexture(texture);
		drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);

		if(this.testNuke.euph > 0)
			drawTexturedModalRect(guiLeft + 142, guiTop + 89, 176, 108, 18, 18);
		else if(this.testNuke.schrab > 0)
			drawTexturedModalRect(guiLeft + 106, guiTop + 89, 176, 90, 18, 18);
		else if(this.testNuke.amat > 0)
			drawTexturedModalRect(guiLeft + 70, guiTop + 89, 176, 54, 18, 18);
		else if(this.testNuke.hydro > 0)
			drawTexturedModalRect(guiLeft + 52, guiTop + 89, 176, 36, 18, 18);
		else if(this.testNuke.nuke > 0)
			drawTexturedModalRect(guiLeft + 34, guiTop + 89, 176, 18, 18, 18);
		else if(this.testNuke.tnt > 0)
			drawTexturedModalRect(guiLeft + 16, guiTop + 89, 176, 0, 18, 18);

		if(this.testNuke.dirty > 0 &&
				this.testNuke.nuke > 0 &&
				this.testNuke.amat == 0 &&
				this.testNuke.schrab == 0 &&
				this.testNuke.euph == 0)
			drawTexturedModalRect(guiLeft + 88, guiTop + 89, 176, 72, 18, 18);
	}
}
