package api.ntm1of90.compat.fluid.render;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;

import javax.imageio.ImageIO;

import com.hbm.inventory.fluid.FluidType;
import com.hbm.lib.RefStrings;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;

/*
 * Sprite that sources its pixels straight from the fluid's GUI texture (FluidType.getTexture),
 * so no dedicated atlas texture files are needed. Tint gets baked in, missing textures
 * fall back to a solid color instead of the checkerboard.
 */
@SideOnly(Side.CLIENT)
public class FluidAtlasSprite extends TextureAtlasSprite {

	private final FluidType fluid;
	private final int mipmap;
	private final boolean anisotropic;

	public FluidAtlasSprite(String spriteName, FluidType fluid) {
		super(spriteName);
		this.fluid = fluid;
		this.mipmap = Minecraft.getMinecraft().gameSettings.mipmapLevels;
		this.anisotropic = Minecraft.getMinecraft().gameSettings.anisotropicFiltering > 1;
	}

	@Override
	public boolean hasCustomLoader(IResourceManager manager, ResourceLocation location) {
		return true;
	}

	@Override
	public boolean load(IResourceManager manager, ResourceLocation location) {

		BufferedImage image = null;
		try {
			image = this.readImage(manager, this.fluid.getTexture());
			this.applyTint(image);
		} catch(Exception ex) {
			com.hbm.main.MainRegistry.logger.warn("Failed to load fluid texture " + this.fluid.getTexture() + ", using solid color fallback.");
		}

		try {
			if(image == null) image = this.generateSolidFallback();
			// remaining slots would be hand-made mip textures, the atlas generates those itself
			BufferedImage[] frames = new BufferedImage[1 + this.mipmap];
			frames[0] = image;
			this.loadSprite(frames, null, this.anisotropic);
		} catch(RuntimeException ex) {
			com.hbm.main.MainRegistry.logger.error("Error preparing fluid sprite for " + this.fluid.getName() + ", using solid color fallback: " + ex);
			this.loadSprite(new BufferedImage[] { this.generateSolidFallback() }, null, false);
		}

		return false; // we just did the loading ourselves
	}

	private BufferedImage readImage(IResourceManager manager, ResourceLocation location) throws IOException {
		try (InputStream stream = manager.getResource(location).getInputStream()) {
			BufferedImage image = ImageIO.read(stream);
			if(image == null) throw new IOException("ImageIO returned null for " + location);
			return image;
		}
	}

	// same tint application as FluidTank.renderTank
	private void applyTint(BufferedImage image) {
		int tint = this.fluid.getTint();
		if(tint == 0xffffff) return;

		int tr = (tint >> 16) & 0xFF;
		int tg = (tint >> 8) & 0xFF;
		int tb = tint & 0xFF;

		for(int x = 0; x < image.getWidth(); x++) {
			for(int y = 0; y < image.getHeight(); y++) {
				image.setRGB(x, y, multiplyRGB(image.getRGB(x, y), tr, tg, tb));
			}
		}
	}

	private BufferedImage generateSolidFallback() {
		BufferedImage image = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
		int color = 0xFF000000 | this.fluid.getColor();
		for(int x = 0; x < 16; x++) {
			for(int y = 0; y < 16; y++) {
				image.setRGB(x, y, color);
			}
		}
		return image;
	}

	private static int multiplyRGB(int rgb, int tr, int tg, int tb) {
		int r = ((rgb >> 16) & 0xFF) * tr / 255;
		int g = ((rgb >> 8) & 0xFF) * tg / 255;
		int b = (rgb & 0xFF) * tb / 255;
		return (rgb & 0xFF000000) | (r << 16) | (g << 8) | b;
	}

	public static String getSpriteName(FluidType fluid) {
		return RefStrings.MODID + ":fluidicon_" + fluid.getName().toLowerCase(Locale.US);
	}
}
