package com.hbm.render.entity.mob;

import com.hbm.entity.mob.EntityBrineSlime;
import com.hbm.lib.RefStrings;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelSlime;
import net.minecraft.client.renderer.entity.RenderSlime;
import net.minecraft.entity.Entity;
import net.minecraft.entity.monster.EntitySlime;
import net.minecraft.util.ResourceLocation;

public class RenderBrineSlime extends RenderSlime {
    
    private static final ResourceLocation texture = new ResourceLocation(RefStrings.MODID, "textures/entity/brine_slime.png");
    private final ModelBase slimeModel = new ModelSlime(0);

    public RenderBrineSlime() {
        super(new ModelSlime(0), new ModelSlime(1), 0.25F);
    }

    @Override
    protected ResourceLocation getEntityTexture(Entity entity) {
        return this.getEntityTexture((EntityBrineSlime) entity);
    }

    protected ResourceLocation getEntityTexture(EntityBrineSlime entity) {
        return texture;
    }
}
