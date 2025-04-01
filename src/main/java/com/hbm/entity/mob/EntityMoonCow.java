package com.hbm.entity.mob;

import com.hbm.entity.mob.ai.EntityMoonWalkHelper;
import com.hbm.items.ModItems;

import api.hbm.entity.ISuffocationImmune;
import api.hbm.entity.IRadiationImmune;
import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.passive.EntityCow;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.world.World;

public class EntityMoonCow extends EntityCow implements IRadiationImmune, ISuffocationImmune {

	public EntityMoonCow(World world) {
		super(world);

		moveHelper = new EntityMoonWalkHelper(this);
	}

	@Override
	public EntityMoonCow createChild(EntityAgeable entity) {
		return new EntityMoonCow(this.worldObj);
	}

	@Override
	protected float getSoundVolume() {
		return 0.1F; // muffled by helmet
	}

	@Override
	protected Item getDropItem() {
		return ModItems.cheese;
	}

	@Override
	protected void dropFewItems(boolean hitByPlayer, int looting) {
		int j = this.rand.nextInt(3) + this.rand.nextInt(1 + looting);
		int k;

		for(k = 0; k < j; ++k) {
			this.dropItem(Items.leather, 1);
		}

		j = this.rand.nextInt(3) + 1 + this.rand.nextInt(1 + looting);

		for(k = 0; k < j; ++k) {
			this.dropItem(ModItems.cheese, 1);
		}
	}

}
