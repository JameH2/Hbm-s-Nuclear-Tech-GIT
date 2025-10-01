package com.hbm.entity.mob;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.ai.EntityAIAttackOnCollide;
import net.minecraft.entity.ai.EntityAILookIdle;
import net.minecraft.entity.ai.EntityAIWatchClosest;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;

public class EntityGhostTrapped extends EntityCreature {

	public EntityGhostTrapped(World world) {
		super(world);
		this.tasks.addTask(1, new EntityAIAttackOnCollide(this, EntityPlayer.class, 1.0D, true));
		this.tasks.addTask(2, new EntityAILookIdle(this));
		this.tasks.addTask(3, new EntityAIWatchClosest(this, EntityPlayer.class, 15.0F));
	}

	// should open a GUI for the attacked player, eventually
	@Override
	public boolean attackEntityAsMob(Entity target) {
		return super.attackEntityAsMob(target);
	}

}
