package com.hbm.inventory.gui;

import java.awt.*;
import java.util.Arrays;
import java.util.Locale;

import com.hbm.util.i18n.I18nUtil;
import net.minecraft.client.renderer.OpenGlHelper;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

import com.hbm.inventory.SlotPattern;
import com.hbm.inventory.container.ContainerMachineCustom;
import com.hbm.inventory.gui.element.GUIElements;
import com.hbm.lib.RefStrings;
import com.hbm.main.MainRegistry;
import com.hbm.module.ModulePatternMatcher;
import com.hbm.tileentity.machine.TileEntityCustomMachine;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.ResourceLocation;
import java.util.ArrayList;
import java.util.List;
import com.hbm.inventory.material.Mats;
import com.hbm.inventory.material.NTMMaterial;
import com.hbm.packet.PacketDispatcher;
import com.hbm.packet.toserver.NBTControlPacket;
import net.minecraft.nbt.NBTTagCompound;

public class GUIMachineCustom extends GuiInfoContainer {

	private static ResourceLocation texture = new ResourceLocation(RefStrings.MODID + ":textures/gui/processing/gui_custom.png");
	private TileEntityCustomMachine custom;

	public GUIMachineCustom(InventoryPlayer invPlayer, TileEntityCustomMachine tedf) {
		super(new ContainerMachineCustom(invPlayer, tedf));
		custom = tedf;

		this.xSize = 176;
		this.ySize = 256;
	}

	@Override
	public void drawScreen(int x, int y, float interp) {
		super.drawScreen(x, y, interp);

		this.drawElectricityInfo(this, x, y, guiLeft + 150, guiTop + 18, 16, 52, custom.power, custom.config.maxPower);
		if(custom.config.maxHeat>0) this.drawCustomInfoStat(x, y, guiLeft + 61, guiTop + 53, 18, 18, x, y, new String[] { I18nUtil.resolveKey("gui.custom.heat") + String.format(Locale.US, "%,d", custom.heat) + " / " + String.format(Locale.US, "%,d", custom.config.maxHeat)});
		if(this.mc.thePlayer.inventory.getItemStack() == null) {
			for(int i = 0; i < this.inventorySlots.inventorySlots.size(); ++i) {
				Slot slot = (Slot) this.inventorySlots.inventorySlots.get(i);
				int tileIndex = slot.getSlotIndex();

				if(this.isMouseOverSlot(slot, x, y) && slot instanceof SlotPattern && custom.matcher.modes[tileIndex - 10] != null) {
					this.func_146283_a(Arrays.asList(new String[] { EnumChatFormatting.RED + I18nUtil.resolveKey("gui.common.change"), ModulePatternMatcher.getLabel(custom.matcher.modes[tileIndex - 10]) }), x, y - 30);
				}
			}
		}

		for(int i = 0; i < custom.inputTanks.length; i++) {
			custom.inputTanks[i].renderTankInfo(this, x, y, guiLeft + 8 + 18 * i, guiTop + 18, 16, 34);
		}

		for(int i = 0; i < custom.outputTanks.length; i++) {
			custom.outputTanks[i].renderTankInfo(this, x, y, guiLeft + 78 + 18 * i, guiTop + 18, 16, 34);
		}

		if(custom.config.materialOut && custom.hasMaterialSupport()) {
			int matX = guiLeft + 78 + 18 * 2;
			int matY = guiTop + 18;
			if(x >= matX && x < matX + 16 && y >= matY && y < matY + 34) {
				List<String> tip = new ArrayList();
				tip.add(EnumChatFormatting.GOLD + I18nUtil.resolveKey("gui.custom.material_pool"));
				tip.add(EnumChatFormatting.YELLOW + I18nUtil.resolveKey("gui.custom.total") + custom.getTotalMaterialAmount() + " / " + custom.config.materialInCap + I18nUtil.resolveKey("gui.custom.quanta"));
				for(Mats.MaterialStack ms : custom.materials) {
					if(ms.amount > 0 && ms.material != null) {
						tip.add("  " + I18nUtil.resolveKey(ms.material.getUnlocalizedName()) + ": " + Mats.formatAmount(ms.amount, Keyboard.isKeyDown(Keyboard.KEY_LSHIFT)));
					}
				}
				this.drawHoveringText(tip, x, y, fontRendererObj);
			}
		}
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int i, int j) {
		String name = this.custom.getInventoryName();
		String localizedName = this.custom.config.localization.get(MainRegistry.proxy.getLanguageCode());
		if(localizedName != null) name = localizedName;
		this.fontRendererObj.drawString(name, 68 - this.fontRendererObj.getStringWidth(name) / 2, 6, 4210752);
		this.fontRendererObj.drawString(I18n.format("container.inventory"), 8, this.ySize - 96 + 2, 4210752);
		if(custom.config.fluxMode) this.fontRendererObj.drawString(I18nUtil.resolveKey("gui.custom.flux", custom.flux),83, 57,0x08FF00);
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float interp, int x, int y) {
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		Minecraft.getMinecraft().getTextureManager().bindTexture(texture);
		drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);
		if(custom.config.fluxMode){
			drawTexturedModalRect(guiLeft + 78, guiTop + 54, 192, 122,51 , 15);
		}
		if(custom.maxHeat > 0) {
			drawTexturedModalRect(guiLeft + 61, guiTop + 53, 236, 0, 18, 18);
			GUIElements.drawSmoothGauge(guiLeft + 70, guiTop + 62, this.zLevel, (double) custom.heat / (double) custom.config.maxHeat, 5, 2, 1, 0x7F0000);
		}
		int p = custom.progress * 90 / custom.maxProgress;
		drawTexturedModalRect(guiLeft + 78, guiTop + 119, 192, 0, Math.min(p, 44), 16);
		if(p > 44) {
			p -= 44;
			drawTexturedModalRect(guiLeft + 78 + 44, guiTop + 119, 192, 16, p, 16);
		}

		int e = (int) (custom.power * 52 / custom.config.maxPower);
		drawTexturedModalRect(guiLeft + 150, guiTop + 70 - e, 176, 52 - e, 16, e);

		for(int i = 0; i < 2; i++) {
			for(int j = 0; j < 3; j++) {
				int index = i * 3 + j;
				if(custom.config.itemInCount <= index) {
					drawTexturedModalRect(guiLeft + 7 + j * 18, guiTop + 71 + i * 18, 192 + j * 18, 86 + i * 18, 18, 18);
					drawTexturedModalRect(guiLeft + 7 + j * 18, guiTop + 107 + i * 18, 192 + j * 18, 86 + i * 18, 18, 18);
				}
				if(custom.config.itemOutCount <= index) {
					drawTexturedModalRect(guiLeft + 77 + j * 18, guiTop + 71 + i * 18, 192 + j * 18, 86 + i * 18, 18, 18);
				}
			}
		}

		for(int i = 0; i < 3; i++) {
			if(custom.config.fluidInCount <= i) {
				drawTexturedModalRect(guiLeft + 7 + i * 18, guiTop + 17, 192 + i * 18, 32, 18, 54);
			}
			int displayOutCount = (custom.config.materialOut && i == 2) ? custom.config.fluidOutCount + 3 : custom.config.fluidOutCount;
			if(displayOutCount <= i) {
				drawTexturedModalRect(guiLeft + 77 + i * 18, guiTop + 17, 192 + i * 18, 32, 18, 36);
			}
		}

		for(int i = 0; i < custom.inputTanks.length; i++) {
			custom.inputTanks[i].renderTank(guiLeft + 8 + 18 * i, guiTop + 52, this.zLevel, 16, 34);
		}

		for(int i = 0; i < custom.outputTanks.length; i++) {
			custom.outputTanks[i].renderTank(guiLeft + 78 + 18 * i, guiTop + 52, this.zLevel, 16, 34);
		}

		// Material display in 3rd output slot
		if(custom.config.materialOut && custom.hasMaterialSupport()) {
			// Re-bind GUI texture after FluidTank.renderTank() switched it to fluid textures
			Minecraft.getMinecraft().getTextureManager().bindTexture(texture);
			if(!custom.materials.isEmpty()) {
				int cap = custom.config.materialInCap;
				int slotH = 34;
				int lastQuant = 0;
				int lastHeight = 0;

				GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);

				for(int mi = custom.materials.size() - 1; mi >= 0; mi--) {
					Mats.MaterialStack ms = custom.materials.get(mi);
					if(ms.amount <= 0) continue;
					int layerH = (lastQuant + ms.amount) * slotH / cap;
					if(lastHeight == layerH) continue; //skip draw calls that would be 0 pixels high

					int offset = ms.material.smeltable == NTMMaterial.SmeltingBehavior.ADDITIVE ? 16 : 0;
					int hex = ms.material.moltenColor;
					Color color = new Color(hex);
					GL11.glColor3f(color.getRed() / 255F, color.getGreen() / 255F, color.getBlue() / 255F);
					drawTexturedModalRect(guiLeft + 78 + 18 * 2, guiTop + 52 - layerH, 192 + offset, 172 - layerH, 16, layerH - lastHeight);
					GL11.glEnable(GL11.GL_BLEND);
					GL11.glColor4f(1F, 1F, 1F, 0.3F);
					drawTexturedModalRect(guiLeft + 78 + 18 * 2, guiTop + 52 - layerH, 192 + offset, 172 - layerH, 16, layerH - lastHeight);
					GL11.glDisable(GL11.GL_BLEND);

					lastQuant += ms.amount;
					lastHeight = layerH;
				}
				OpenGlHelper.glBlendFunc(770, 771, 1, 0);
				GL11.glColor3f(255, 255, 255);			}
		}
	}

	@Override
	protected void mouseClicked(int x, int y, int button) {
		super.mouseClicked(x, y, button);

		// Right-click on material display area -> send clear packet
		if(button == 1 && custom.config.materialOut && custom.hasMaterialSupport()) {
			int matX = guiLeft + 78 + 18 * 2;
			int matY = guiTop + 18;
			if(x >= matX && x < matX + 16 && y >= matY && y < matY + 34) {
				NBTTagCompound data = new NBTTagCompound();
				data.setBoolean("clearMaterials", true);
				PacketDispatcher.wrapper.sendToServer(new NBTControlPacket(data, custom.xCoord, custom.yCoord, custom.zCoord));
			}
		}
	}

}
