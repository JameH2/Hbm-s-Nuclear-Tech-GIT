package api.ntm1of90.compat.fluid.render;

import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.fluid.trait.FT_Gaseous;
import com.hbm.inventory.fluid.trait.FluidTraitSimple.FT_Viscous;

import api.ntm1of90.compat.fluid.util.NTMFluidLocalization;
import com.hbm.util.FluidDebug;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.util.IIcon;
import net.minecraftforge.fluids.Fluid;

/*
 * A Forge Fluid that carries color information from HBM's fluid system.
 * The stitched texture is fully colored already (tint included), so getColor(FluidStack)
 * reports white to stop external renderers from tinting the icon twice. The approximate
 * NTM color stays available through getColor().
 */
public class ColoredForgeFluid extends Fluid {

    private int color;
    private FluidType hbmFluidType;

    public ColoredForgeFluid(String fluidName, FluidType hbmFluid) {
        super(fluidName);
        this.hbmFluidType = hbmFluid;
        this.color = hbmFluid.getColor();

        // basic properties based on the HBM fluid
        this.setDensity(hbmFluid.hasTrait(FT_Gaseous.class) ? -1000 : 1000);
        this.setViscosity(hbmFluid.hasTrait(FT_Viscous.class) ? 3000 : 1000);
        this.setTemperature(hbmFluid.temperature);
        this.setLuminosity(0);

        // icons are applied by FluidRegistry during texture stitching
    }

    public ColoredForgeFluid(String fluidName, int color) {
        super(fluidName);
        this.color = color;
    }

    // approximate NTM color
    public int getColor() {
        return color;
    }

    public FluidType getHbmFluidType() {
        return hbmFluidType;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public IIcon getStillIcon() {
        IIcon icon = NTMFluidTextureMapper.getStillIcon(getName());
        if(icon != null) return icon;
        icon = super.getStillIcon();
        if(icon != null) return icon;
        // Never hand null to external renderers (AE2 NPEs); also covers post-stitch registrations.
        FluidDebug.event("fluidicon.fallback|" + getName(),
            () -> "ICON fallback to water for icon-less Forge fluid '" + getName() + "' rendered by " + FluidDebug.callerHint());
        net.minecraftforge.fluids.Fluid water = net.minecraftforge.fluids.FluidRegistry.WATER;
        if(water != null) {
            IIcon fallback = water.getStillIcon();
            if(fallback != null) return fallback;
        }
        // Water's icon can itself be null during early init. TextureMap.getAtlasSprite(String)
        // is guaranteed non-null: it returns the missing-image sprite when the name is absent,
        // so this is the ultimate fallback that guarantees a usable icon.
        try {
            net.minecraft.client.Minecraft mc = net.minecraft.client.Minecraft.getMinecraft();
            if(mc != null) {
                net.minecraft.client.renderer.texture.TextureMap map = mc.getTextureMapBlocks();
                if(map != null) {
                    net.minecraft.util.IIcon fallback = map.getAtlasSprite(getName());
                    if(fallback != null) return fallback;
                }
            }
        } catch(Throwable t) { }
        return null;   // unreachable in practice, kept as a last resort
    }

    @Override
    @SideOnly(Side.CLIENT)
    public IIcon getFlowingIcon() {
        IIcon icon = NTMFluidTextureMapper.getFlowingIcon(getName());
        if(icon != null) return icon;
        icon = super.getFlowingIcon();
        if(icon != null) return icon;
        net.minecraftforge.fluids.Fluid water = net.minecraftforge.fluids.FluidRegistry.WATER;
        if(water != null) {
            IIcon fallback = water.getFlowingIcon();
            if(fallback != null) return fallback;
        }
        // Water's icon can itself be null during early init. TextureMap.getAtlasSprite(String)
        // is guaranteed non-null: it returns the missing-image sprite when the name is absent,
        // so this is the ultimate fallback that guarantees a usable icon.
        try {
            net.minecraft.client.Minecraft mc = net.minecraft.client.Minecraft.getMinecraft();
            if(mc != null) {
                net.minecraft.client.renderer.texture.TextureMap map = mc.getTextureMapBlocks();
                if(map != null) {
                    net.minecraft.util.IIcon fallback = map.getAtlasSprite(getName());
                    if(fallback != null) return fallback;
                }
            }
        } catch(Throwable t) { }
        return null;   // unreachable in practice, kept as a last resort
    }

    @SideOnly(Side.CLIENT)
    public IIcon getInventoryIcon() {
        IIcon icon = api.ntm1of90.compat.fluid.registry.FluidRegistry.getInventoryIcon(getName());
        return icon != null ? icon : getStillIcon();
    }

    @Override
    public String getLocalizedName() {
        return NTMFluidLocalization.getForgeFluidDisplayName(this);
    }

    // texture is fully colored, no extra tint should be applied
    @Override
    public int getColor(net.minecraftforge.fluids.FluidStack stack) {
        return 0xFFFFFF;
    }

    // color with alpha channel, used by AE2 integration
    public int getColorARGB() {
        return 0xFF000000 | color;
    }

    public void setColor(int color) {
        this.color = color;
    }

    @SideOnly(Side.CLIENT)
    public void renderUsingForgeFluidTexture(int x, int y, int width, int height) {
        IIcon icon = getInventoryIcon();

        if (icon == null) return;

        net.minecraft.client.Minecraft.getMinecraft().getTextureManager().bindTexture(net.minecraft.client.renderer.texture.TextureMap.locationBlocksTexture);

        net.minecraft.client.renderer.Tessellator tessellator = net.minecraft.client.renderer.Tessellator.instance;
        tessellator.startDrawingQuads();
        tessellator.addVertexWithUV(x, y + height, 0, icon.getMinU(), icon.getMaxV());
        tessellator.addVertexWithUV(x + width, y + height, 0, icon.getMaxU(), icon.getMaxV());
        tessellator.addVertexWithUV(x + width, y, 0, icon.getMaxU(), icon.getMinV());
        tessellator.addVertexWithUV(x, y, 0, icon.getMinU(), icon.getMinV());
        tessellator.draw();
    }
}
