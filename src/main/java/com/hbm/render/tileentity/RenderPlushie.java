package com.hbm.render.tileentity;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import com.hbm.blocks.ModBlocks;
import com.hbm.blocks.generic.BlockPlushie.PlushieType;
import com.hbm.blocks.generic.BlockPlushie.TileEntityPlushie;
import com.hbm.items.ModItems;
import com.hbm.lib.RefStrings;
import com.hbm.main.ResourceManager;
import com.hbm.render.item.ItemRenderBase;
import com.hbm.render.loader.HFRWavefrontObject;
import com.hbm.render.util.HorsePronter;
import com.hbm.util.BobMathUtil;
import com.hbm.util.EnumUtil;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.IItemRenderer;
import net.minecraftforge.client.model.IModelCustom;

public class RenderPlushie extends TileEntitySpecialRenderer implements IItemRendererProvider {

	public static final IModelCustom yomiModel = new HFRWavefrontObject(new ResourceLocation(RefStrings.MODID, "models/trinkets/yomi.obj"), false).asVBO();
	public static final IModelCustom hundunModel = new HFRWavefrontObject(new ResourceLocation(RefStrings.MODID, "models/trinkets/hundun.obj"), false).asVBO();
	public static final ResourceLocation yomiTex = new ResourceLocation(RefStrings.MODID, "textures/models/trinkets/yomi.png");
	public static final ResourceLocation numbernineTex = new ResourceLocation(RefStrings.MODID, "textures/models/horse/numbernine.png");
	public static final ResourceLocation hundunTex = new ResourceLocation(RefStrings.MODID, "textures/models/trinkets/hundun.png");

	//VOCALOIDS//
	public static final IModelCustom tetoModel = new HFRWavefrontObject(new ResourceLocation(RefStrings.MODID, "models/trinkets/teto.obj")).asVBO();
	public static final ResourceLocation tetoTex = new ResourceLocation(RefStrings.MODID, "textures/models/trinkets/teto.png");
	public static final ResourceLocation tetoBlush = new ResourceLocation(RefStrings.MODID, "textures/models/trinkets/teto_squished.png");

	public static final IModelCustom mikuModel = new HFRWavefrontObject(new ResourceLocation(RefStrings.MODID, "models/trinkets/miku.obj")).asVBO();
	public static final ResourceLocation mikuTex = new ResourceLocation(RefStrings.MODID, "textures/models/trinkets/miku.png");
	public static final ResourceLocation mikuBlush = new ResourceLocation(RefStrings.MODID, "textures/models/trinkets/miku_squished.png");

	public static final IModelCustom neruModel = new HFRWavefrontObject(new ResourceLocation(RefStrings.MODID, "models/trinkets/neru.obj")).asVBO();
	public static final ResourceLocation neruTex = new ResourceLocation(RefStrings.MODID, "textures/models/trinkets/neru.png");
	public static final ResourceLocation neruBlush = new ResourceLocation(RefStrings.MODID, "textures/models/trinkets/neru_squished.png");

	@Override
	public void renderTileEntityAt(TileEntity tile, double x, double y, double z, float interp) {
		GL11.glPushMatrix();
		GL11.glTranslated(x + 0.5, y, z + 0.5);
		GL11.glEnable(GL11.GL_CULL_FACE);

		GL11.glRotated(22.5D * tile.getBlockMetadata() + 90, 0, -1, 0);
		TileEntityPlushie te = (TileEntityPlushie) tile;

		if(te.miseryFactor > 0.01F) {
			GL11.glScaled(1, 1 - BobMathUtil.interp(te.lastMiseryFactor, te.miseryFactor, interp), 1);
		} else if(te.squishTimer > 0) {
			double squish = te.squishTimer - interp;
			GL11.glScaled(1, 1 + (-(Math.sin(squish)) * squish) * 0.025, 1);
		}

		switch(te.type) {
		case NONE: break;
		case YOMI: GL11.glScaled(0.5, 0.5, 0.5); break;
		case NUMBERNINE: GL11.glScaled(0.75, 0.75, 0.75); break;
		case TETO: GL11.glScaled(0.5, 0.5, 0.5); break;
		case MIKU: GL11.glScaled(0.5, 0.5, 0.5); break;
		case NERU: GL11.glScaled(0.5, 0.5, 0.5); break;
		case HUNDUN: GL11.glScaled(1, 1, 1); break;
		}
		renderPlushie(te.type, te.squishTimer);

		GL11.glPopMatrix();
	}

	public static void renderPlushie(PlushieType type, int squishTimer) {

		GL11.glEnable(GL12.GL_RESCALE_NORMAL);

		switch(type) {
		case NONE: break;
		case YOMI:
			Minecraft.getMinecraft().getTextureManager().bindTexture(yomiTex);
			yomiModel.renderAll();
			break;
		case NUMBERNINE:
			GL11.glRotated(90, 0, 1, 0);
			GL11.glRotated(15, -1, 0, 0);
			GL11.glTranslated(0, -0.25, 0.75);
			Minecraft.getMinecraft().getTextureManager().bindTexture(numbernineTex);
			HorsePronter.reset();
			double r = 45;
			HorsePronter.pose(HorsePronter.id_body, 0, -r, 0);
			HorsePronter.pose(HorsePronter.id_tail, 0, 60, 90);
			HorsePronter.pose(HorsePronter.id_lbl, 0, -75 + r, 35);
			HorsePronter.pose(HorsePronter.id_rbl, 0, -75 + r, -35);
			HorsePronter.pose(HorsePronter.id_lfl, 0, r - 25, 5);
			HorsePronter.pose(HorsePronter.id_rfl, 0, r - 25, -5);
			HorsePronter.pose(HorsePronter.id_head, 0, r + 15, 0);
			HorsePronter.pront();
			GL11.glRotated(15, 1, 0, 0);
			GL11.glPushMatrix();
			GL11.glDisable(GL11.GL_CULL_FACE);
			GL11.glShadeModel(GL11.GL_SMOOTH);
			GL11.glTranslated(0, 1, -0.6875);
			double s = 1.125D;
			GL11.glScaled(0.0625 * s,  0.0625 * s,  0.0625 * s);
			GL11.glRotated(180, 1, 0, 0);
			Minecraft.getMinecraft().renderEngine.bindTexture(ResourceManager.no9);
			ResourceManager.armor_no9.renderPart("Helmet");
			Minecraft.getMinecraft().renderEngine.bindTexture(ResourceManager.no9_insignia);
			ResourceManager.armor_no9.renderPart("Insignia");
			GL11.glShadeModel(GL11.GL_FLAT);
			GL11.glEnable(GL11.GL_CULL_FACE);
			GL11.glPopMatrix();
			ItemStack stack = new ItemStack(ModItems.cigarette);
			double scale = 0.25;
			GL11.glTranslated(-0.06, 1.13, -0.42);
			GL11.glScaled(scale, scale, scale);
			GL11.glRotated(90, 0, -1, 0);
			GL11.glRotated(60, 0, 0, -1);
			Minecraft.getMinecraft().getTextureManager().bindTexture(TextureMap.locationItemsTexture);
			IIcon icon = stack.getIconIndex();
			ItemRenderer.renderItemIn2D(Tessellator.instance, icon.getMaxU(), icon.getMinV(), icon.getMinU(), icon.getMaxV(), icon.getIconWidth(), icon.getIconHeight(), 0.0625F);
			break;
		case HUNDUN:
			Minecraft.getMinecraft().getTextureManager().bindTexture(hundunTex);
			hundunModel.renderPart("goober_posed");
			break;
		case TETO:
			Minecraft.getMinecraft().getTextureManager().bindTexture(squishTimer > 6 ? tetoBlush : tetoTex);
			tetoModel.renderAll();
			break;
		case MIKU:
			Minecraft.getMinecraft().getTextureManager().bindTexture(squishTimer > 6 ? mikuBlush : mikuTex);
			mikuModel.renderAll();
			break;
		case NERU:
			Minecraft.getMinecraft().getTextureManager().bindTexture(squishTimer > 6 ? neruBlush : neruTex);
			neruModel.renderAll();
			break;
		}
	}

	@Override
	public Item getItemForRenderer() {
		return Item.getItemFromBlock(ModBlocks.plushie);
	}

	@Override
	public IItemRenderer getRenderer() {
		return new ItemRenderBase() {
			public void renderInventory() {
				GL11.glTranslated(0, -6, 0);
				GL11.glScaled(6, 6, 6);
			}
			public void renderCommonWithStack(ItemStack item) {
				GL11.glTranslated(0, 0.25, 0);
				GL11.glEnable(GL11.GL_CULL_FACE);
				PlushieType type = EnumUtil.grabEnumSafely(PlushieType.class, item.getItemDamage());

				switch(type) {
				case NONE: break;
				case YOMI: GL11.glScaled(1.25, 1.25, 1.25); break;
				case NUMBERNINE: GL11.glTranslated(0, 0.25, 0.25); GL11.glScaled(1.25, 1.25, 1.25); break;
				case HUNDUN: GL11.glTranslated(0.5, 0.5, 0); GL11.glScaled(1.25, 1.25, 1.25); break;
				case TETO: GL11.glTranslated(0, 0.25, 0); GL11.glScaled(1.5, 1.5, 1.5); break;
				case MIKU: GL11.glTranslated(0, 0.25, 0); GL11.glScaled(1.5, 1.5, 1.5); break;
				case NERU: GL11.glTranslated(0, 0.25, 0); GL11.glScaled(1.5, 1.5, 1.5); break;
				}
				renderPlushie(type, 0);
			}};
	}
}
