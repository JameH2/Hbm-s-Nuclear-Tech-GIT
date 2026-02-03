package com.hbm.entity.mob;

import com.hbm.entity.mob.ai.EntityMoonWalkHelper;
import com.hbm.config.MobConfig;
import api.hbm.entity.ISuffocationImmune;
import api.hbm.entity.IRadiationImmune;
import net.minecraft.entity.monster.EntitySlime;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.util.DamageSource;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.World;

public class EntityBrineSlime extends EntitySlime implements IRadiationImmune, ISuffocationImmune {

    public EntityBrineSlime(World world) {
        this(world, 1 + world.rand.nextInt(3)); // Default constructor with random size 1-3
    }

    public EntityBrineSlime(World world, int size) {
        super(world);
        this.isImmuneToFire = true;
        this.setSlimeSize(size);
        this.moveHelper = new EntityMoonWalkHelper(this);
    }

    @Override
    public EntityBrineSlime createInstance() {
        return new EntityBrineSlime(this.worldObj, this.getSlimeSize());
    }

    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        this.getEntityAttribute(SharedMonsterAttributes.movementSpeed).setBaseValue(0.2D);
        this.getEntityAttribute(SharedMonsterAttributes.maxHealth).setBaseValue(this.getSlimeSize() * this.getSlimeSize() * 2.0D);
    }

    @Override
    protected void setSlimeSize(int size) {
        super.setSlimeSize(size);
        // Update health when size changes
        this.getEntityAttribute(SharedMonsterAttributes.maxHealth).setBaseValue(size * size * 2.0D);
        this.setHealth(this.getMaxHealth());
        this.experienceValue = size;
    }

    @Override
    public boolean getCanSpawnHere() {
    // Skip the parent's getCanSpawnHere() check since it's too restrictive
        boolean canSpawn = this.worldObj.difficultySetting != EnumDifficulty.PEACEFUL && 
                          MobConfig.enableBrineSlime &&
                          this.worldObj.checkNoEntityCollision(this.boundingBox) &&
                          this.worldObj.getCollidingBoundingBoxes(this, this.boundingBox).isEmpty() &&
                          !this.worldObj.isAnyLiquid(this.boundingBox);
        return canSpawn;
    }

    @Override
    protected int getJumpDelay() {
        return this.rand.nextInt(20) + 10; // Slower jumping than normal slimes
    }

    @Override
    protected String getJumpSound() {
        return "mob.slime." + (this.getSlimeSize() > 1 ? "big" : "small");
    }

    @Override
    protected String getHurtSound() {
        return "mob.slime." + (this.getSlimeSize() > 1 ? "big" : "small");
    }

    @Override
    protected String getDeathSound() {
        return "mob.slime." + (this.getSlimeSize() > 1 ? "big" : "small");
    }
}
