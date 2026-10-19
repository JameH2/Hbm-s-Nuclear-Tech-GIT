package com.hbm.inventory.gui;

import com.hbm.inventory.container.ContainerTurretRailgun;
import com.hbm.inventory.gui.element.GUIElements;
import com.hbm.lib.RefStrings;
import com.hbm.main.NTMSounds;
import com.hbm.packet.PacketDispatcher;
import com.hbm.packet.toserver.NBTControlPacket;
import com.hbm.tileentity.turret.TileEntityTurretRailgun;
import com.hbm.util.BobMathUtil;
import com.hbm.util.i18n.I18nUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

import org.lwjgl.util.vector.Vector2f;
import java.util.ArrayList;
import java.util.List;

public class GUITurretRailgun extends GuiInfoContainer {

	protected static ResourceLocation texture = new ResourceLocation(RefStrings.MODID + ":textures/gui/weapon/gui_turret_railgun.png");

	private TileEntityTurretRailgun railgun;
	protected GuiTextField field;
	byte timer = 0; // only for buttons
	boolean yeSure = false; // charge dumping sanity check (for user)

	public GUITurretRailgun(InventoryPlayer invPlayer, TileEntityTurretRailgun tedf) {
		super(new ContainerTurretRailgun(invPlayer, tedf));

		railgun = tedf;

		this.xSize = 176;
		this.ySize = 222;
	}

	public void initGui() {

		super.initGui();

		Keyboard.enableRepeatEvents(true);
		this.field = new GuiTextField(this.fontRendererObj, guiLeft + 96, guiTop + 79, 50, 14);
		this.field.setTextColor(-1);
		this.field.setDisabledTextColour(-1);
		this.field.setEnableBackgroundDrawing(false);
		this.field.setMaxStringLength(25);
	}

	@Override
	public void drawScreen(int x, int y, float interp) {
		super.drawScreen(x, y, interp);

		this.drawElectricityInfo(this, x, y, guiLeft + 153, guiTop + 34, 16, 42, railgun.getPower(), railgun.getMaxPower());
		this.drawElectricityInfo(this, x, y, guiLeft + 7, guiTop + 34, 16, 60, railgun.charge, railgun.maxCharge);

		String[] text = new String[] {
			"Switch to " + (railgun.graphMode ? EnumChatFormatting.GREEN + "ballistics" : EnumChatFormatting.YELLOW + "orientation") + EnumChatFormatting.RESET + " graph"
		};
		this.drawCustomInfoStat(x, y, guiLeft + 7, guiTop + 100, 10, 24, x, y, text);
		text = new String[] {
			(yeSure ? "Are you " + (BobMathUtil.getBlink() ? EnumChatFormatting.RED : EnumChatFormatting.YELLOW) + "SURE" + EnumChatFormatting.RESET + "?" : EnumChatFormatting.RED + "Dump currently stored charge")
		};
		this.drawCustomInfoStat(x, y, guiLeft + 24, guiTop + 105, 27, 14, x, y, text);

		// draw acceptable ammo
		if(this.mc.thePlayer.inventory.getItemStack() == null && this.guiLeft + 61 <= x && guiLeft + 61 + 54 > x && guiTop + 103 < y && guiTop + 103 + 18 >= y) {
			boolean draw = true;
			for(int i = 1; i < 4; i++) {
				if(this.isMouseOverSlot(this.inventorySlots.getSlot(i), x, y) && this.inventorySlots.getSlot(i).getHasStack()) {
					draw = false;
					break;
				}
			}

			if(draw) {
				List<ItemStack> list = new ArrayList(railgun.getAmmoTypesForDisplay());
				List<Object[]> lines = new ArrayList();
				ItemStack selected = list.get(0);

				if(list.size() > 1) {
					int cycle = (int) ((System.currentTimeMillis() % (1000 * list.size())) / 1000);
					selected = ((ItemStack) list.get(cycle)).copy();
					selected.stackSize = 0;
					list.set(cycle, selected);
				}

				if(list.size() < 10) {
					lines.add(list.toArray());
				} else if(list.size() < 24) {
					lines.add(list.subList(0, list.size() / 2).toArray());
					lines.add(list.subList(list.size() / 2, list.size()).toArray());
				} else {
					int bound0 = (int) Math.ceil(list.size() / 3D);
					int bound1 = (int) Math.ceil(list.size() / 3D * 2D);
					lines.add(list.subList(0, bound0).toArray());
					lines.add(list.subList(bound0, bound1).toArray());
					lines.add(list.subList(bound1, list.size()).toArray());
				}

				lines.add(new Object[] {I18nUtil.resolveKey(selected.getDisplayName())});
				this.drawStackText(lines, x, y, this.fontRendererObj);
			}
		}
	}

	@Override
	protected void mouseClicked(int x, int y, int button) {
		super.mouseClicked(x, y, button);

		boolean flag = x >= this.field.xPosition && x < this.field.xPosition + this.field.width && y >= this.field.yPosition && y < this.field.yPosition + this.field.height;
		this.field.setFocused(flag);

		// on-off
		if (guiLeft + 132 <= x && guiLeft + 132 + 29 > x && guiTop + 99 < y && guiTop + 99 + 17 >= y) {
			NBTTagCompound data = new NBTTagCompound();
			data.setBoolean("activeChange", !railgun.active);
			PacketDispatcher.wrapper.sendToServer(new NBTControlPacket(data, railgun.xCoord, railgun.yCoord, railgun.zCoord));
			mc.getSoundHandler().playSound(PositionedSoundRecord.func_147674_a(new ResourceLocation("gui.button.press"), 1.0F));
			return;
		}
		// graph mode
		if (guiLeft + 7 <= x && guiLeft + 7 + 10 > x && guiTop + 100 < y && guiTop + 100 + 24 >= y) {
			NBTTagCompound data = new NBTTagCompound();
			data.setBoolean("graphModeChange", !railgun.graphMode);
			PacketDispatcher.wrapper.sendToServer(new NBTControlPacket(data, railgun.xCoord, railgun.yCoord, railgun.zCoord));
			mc.getSoundHandler().playSound(PositionedSoundRecord.func_147674_a(new ResourceLocation("gui.button.press"), 1.0F));
			return;
		}
		// dump charge
		if (guiLeft + 24 <= x && guiLeft + 24 + 27 > x && guiTop + 105 < y && guiTop + 105 + 14 >= y) {
			if (!yeSure) {
				yeSure = true;
				timer = 5;
				mc.getSoundHandler().playSound(PositionedSoundRecord.func_147674_a(new ResourceLocation("hbm:block.buttonIncorrect"), 1.0F));
			} else {
				yeSure = false;
				NBTTagCompound data = new NBTTagCompound();
				data.setBoolean("dumpCharge", true);
				PacketDispatcher.wrapper.sendToServer(new NBTControlPacket(data, railgun.xCoord, railgun.yCoord, railgun.zCoord));
				mc.getSoundHandler().playSound(PositionedSoundRecord.func_147674_a(new ResourceLocation("gui.button.press"), 1.0F));
			}
			return;
		}
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int i, int j) {
		String name = this.railgun.hasCustomInventoryName() ? this.railgun.getInventoryName() : I18n.format(this.railgun.getInventoryName());
		this.fontRendererObj.drawString(name, this.xSize / 2 - this.fontRendererObj.getStringWidth(name) / 2, 9, 4210752);

		if (railgun.active && railgun.getPower() > 0) {
			if (!this.field.getText().isEmpty() && Keyboard.isKeyDown(Keyboard.KEY_RETURN)) {
				mc.getSoundHandler().playSound(PositionedSoundRecord.func_147674_a(new ResourceLocation(NTMSounds.TECH_BOOP), 1.0F));

				NBTTagCompound data = new NBTTagCompound();
				data.setString("dataInput", this.field.getText());
				PacketDispatcher.wrapper.sendToServer(new NBTControlPacket(data, railgun.xCoord, railgun.yCoord, railgun.zCoord));

				this.field.setText("");
			}

			String t = this.field.getText();

			String cursor = System.currentTimeMillis() % 1000 < 500 ? " " : "||";

			if (this.field.isFocused())
				t = t.substring(0, this.field.getCursorPosition()) + cursor + t.substring(this.field.getCursorPosition(), t.length());

			double scale = 2;

			GL11.glScaled(1D / scale, 1D / scale, 1);
			this.fontRendererObj.drawString("Target:", (int) (97 * scale), (int) (36 * scale), 0x08ff00);
			if (railgun.targetVector != null) {
				this.fontRendererObj.drawString("X: " + (Math.abs(railgun.targetVector.x) > 1000000000 ? BobMathUtil.getShortNumber((long) railgun.targetVector.x) : (int) railgun.targetVector.x), (int) (97 * scale), (int) (41 * scale), 0x08ff00);
				this.fontRendererObj.drawString("Z: " + (Math.abs(railgun.targetVector.y) > 1000000000 ? BobMathUtil.getShortNumber((long) railgun.targetVector.y) : (int) railgun.targetVector.y), (int) (97 * scale), (int) (46 * scale), 0x08ff00);
			}
			this.fontRendererObj.drawString("Loaded: " + railgun.SabotsNames(railgun.sabotType), (int) (97 * scale), (int) (53 * scale), 0x08ff00);
			this.fontRendererObj.drawString("V0: " + BobMathUtil.getShortNumber((long) (railgun.v0 * Math.sqrt((double) railgun.charge / (double) railgun.maxCharge))) + "m/s", (int) (97 * scale), (int) (58 * scale), 0x08ff00);
			this.fontRendererObj.drawString("Charging: " + (int) (railgun.charge * 100 / railgun.maxCharge) + "%", (int) (97 * scale), (int) (65 * scale), 0x00ff00);
			this.fontRendererObj.drawString("Status: " + railgun.status, (int) (97 * scale), (int) (70 * scale), 0x08ff00);

			// arrows' names for the graph
			if (railgun.graphMode) {
				this.fontRendererObj.drawString("X", (int) (55 * scale), (int) (36 * scale), 0x08ff00);
				this.fontRendererObj.drawString("Z", (int) (84 * scale), (int) (66 * scale), 0x08ff00);
			} else {
				this.fontRendererObj.drawString("> The graph may not", (int) (31 * scale), (int) (48 * scale), 0x08ff00);
				this.fontRendererObj.drawString("accurately represent", (int) (31 * scale), (int) (52 * scale), 0x08ff00);
				this.fontRendererObj.drawString("the actual path.", (int) (31 * scale), (int) (58 * scale), 0x08ff00);
				this.fontRendererObj.drawString(String.format("e: %.2f s", railgun.iterations / 20F), (int) (61 * scale), (int) (89 * scale), 0x08ff00);
			}

			// current pitch & yaw display
			this.fontRendererObj.drawString("P: " + (int) (Math.toDegrees(railgun.rotationPitch)) + "°", (int) (31 * scale), (int) (36 * scale), 0x08ff00);
			this.fontRendererObj.drawString("Y: " + (int) (Math.toDegrees(railgun.rotationYaw)) + "°", (int) (31 * scale), (int) (42 * scale), 0x08ff00);

			this.fontRendererObj.drawString(t, (int) (97 * scale), (int) (85 * scale), 0x08ff00); // text field
			GL11.glScaled(scale, scale, 1);

			if (railgun.graphMode) {
				GUIElements.drawArrowVector(59, 92, this.zLevel, new Vector2f(59, 36), 1F, 0x08ff00); // ordered axis arrow
				GUIElements.drawArrowVector(31, 64, this.zLevel, new Vector2f(87, 64), 1F, 0x08ff00); // abscissa axis arrow
				GUIElements.drawHollowCircle(59, 64, this.zLevel, 20F, 48, 0x08ff00);
				GUIElements.drawArrowVector(59, 64, this.zLevel, new Vector2f((float) (59 + 15 * -Math.cos(railgun.rotationYaw)), (float) (64 + 15 * Math.sin(railgun.rotationYaw))), 1F, 0xfafa00); // barrel direction
				if (railgun.targetDirectionVector != null)
					GUIElements.drawArrowVector(59, 64, this.zLevel, new Vector2f((float) (59 + 10 * -railgun.targetDirectionVector.x), (float) (64 + 10 * -railgun.targetDirectionVector.y)), 1F, 0xff0000);
			} else {
				// ballistics graph
				List<Vector2f> points = new ArrayList();
				float maxY = 0F;
				float y;
				for (double T = 0D; T < railgun.t_max; T += 0.01D) {
					y = (float) railgun.y_i(T, railgun.m, railgun.v0*Math.sin(railgun.theta), railgun.bc, railgun.g);
					if (y > maxY) maxY = y;
					points.add(new Vector2f(
						(float) (35 + railgun.x_i(T, railgun.m, railgun.v0*Math.cos(railgun.theta), railgun.bc) / railgun.x_target * 50),
						y
					));
				}

				if (!points.isEmpty() && maxY != 0F) {
					GL11.glDisable(GL11.GL_TEXTURE_2D);
					Tessellator tess = Tessellator.instance;
					tess.startDrawing(GL11.GL_LINE_STRIP);
					tess.setColorOpaque_I(0xff0000);
					for (Vector2f vec : points) {
						vec.y = 88 - vec.y / maxY * 24;
						tess.addVertex(
							MathHelper.clamp_float(vec.x, 35, 85),
							MathHelper.clamp_float(vec.y, 64, 88),
							this.zLevel
						);
					}
					tess.draw();
					GL11.glColor4f(1F, 1F, 1F, 1F);
					GL11.glEnable(GL11.GL_TEXTURE_2D);
				}

				GUIElements.drawArrowVector(35, 92, this.zLevel, new Vector2f(35, 64), 1F, 0x08ff00); // ordered axis arrow
				GUIElements.drawArrowVector(31, 88, this.zLevel, new Vector2f(87, 88), 1F, 0x08ff00); // abscissa axis arrow
			}
		}
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float v, int i, int i1) {
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		Minecraft.getMinecraft().getTextureManager().bindTexture(texture);
		drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);

		int scale = (int) (railgun.getPower() * 42 / railgun.getMaxPower());
		drawTexturedModalRect(guiLeft + 153, guiTop + 76 - scale, 176, 59 - scale, 16, scale);

		if (railgun.active)
			drawTexturedModalRect(guiLeft + 132, guiTop + 99, 176, 0, 29, 17);

		if (!railgun.graphMode)
			drawTexturedModalRect(guiLeft + 7, guiTop + 100, 205, 0, 10, 24);

		if (timer > 0 && guiLeft + 24 <= i && guiLeft + 24 + 27 > i && guiTop + 105 < i1 && guiTop + 105 + 14 >= i1)
			drawTexturedModalRect(guiLeft + 24, guiTop + 105, 215, 0, 27, 14);

		if (yeSure)
			drawTexturedModalRect(guiLeft + 24, guiTop + 105, 215, 14, 27, 14);

		if (timer > 0) timer--;

		// yes im reusing the power gauge scale variable and also yes i dont care
		scale = (int) (railgun.charge * 3 * 60 / railgun.maxCharge); // the value is for all 3 of them, not just one, like they're one single gauge
		int intermediateScale = Math.min(scale, 60);
		drawTexturedModalRect(guiLeft +  7, guiTop + 94 - intermediateScale, 176, 119 - intermediateScale, 4, intermediateScale);
		intermediateScale = MathHelper.clamp_int(scale - 60, 0, 60);
		drawTexturedModalRect(guiLeft + 13, guiTop + 94 - intermediateScale, 176, 119 - intermediateScale, 4, intermediateScale);
		intermediateScale = MathHelper.clamp_int(scale - 120, 0, 60);
		drawTexturedModalRect(guiLeft + 19, guiTop + 94 - intermediateScale, 176, 119 - intermediateScale, 4, intermediateScale);
	}

	protected void keyTyped(char c, int i) {
		if(this.field.textboxKeyTyped(c, i)) return;
		super.keyTyped(c, i);
	}
}
