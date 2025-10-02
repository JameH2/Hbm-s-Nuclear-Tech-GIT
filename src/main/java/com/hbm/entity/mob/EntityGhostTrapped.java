package com.hbm.entity.mob;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.hbm.inventory.gui.GUIRefuseDeath;
import com.hbm.packet.PacketDispatcher;
import com.hbm.packet.toclient.EntityBufPacket;
import com.hbm.tileentity.IBufPacketReceiver;
import com.hbm.tileentity.IEntityInteractionReceiver;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIAttackOnCollide;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.ai.EntityAILookIdle;
import net.minecraft.entity.ai.EntityAINearestAttackableTarget;
import net.minecraft.entity.ai.EntityAIWatchClosest;
import net.minecraft.entity.ai.EntityAITasks.EntityAITaskEntry;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.pathfinding.PathEntity;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

public class EntityGhostTrapped extends EntityCreature implements IBufPacketReceiver, IEntityInteractionReceiver {

	Set<EntityPlayer> killedPlayers = new HashSet<>();

	public EntityGhostTrapped(World world) {
		super(world);
		this.tasks.addTask(1, new EntityAIAttackOnCollide(this, EntityPlayer.class, 1.0D, true));
		this.tasks.addTask(2, new EntityAILookIdle(this));
		this.tasks.addTask(3, new EntityAIWatchClosest(this, EntityPlayer.class, 15.0F));
		this.targetTasks.addTask(2, new EntityAINearestAttackableTarget(this, EntityPlayer.class, 0, false));
	}

	private boolean awaiting;
	private EntityPlayer stareTarget;
	private PathEntity starePath;
	private boolean shouldApproach;

	// can you hear that?
	// the signs of life
	// not yet awake
	// the stirring, the screwing up of eyes
	// a blanket that forms mountains
	// the valley between knee and cheek
	@SuppressWarnings("unchecked")
	public void noticePlayer(EntityPlayer player) {
		System.out.println("YOU...!");
		stareTarget = player;
		starePath = null;
		detachHome();

		// kill all tasks, only one thing matters now
		// (clearing all tasks is a pain in the comodification ass)
		List<EntityAIBase> tasksToKill = new ArrayList<>();
		for(EntityAITaskEntry task : (List<EntityAITaskEntry>) tasks.taskEntries) tasksToKill.add(task.action);
		for(EntityAITaskEntry task : (List<EntityAITaskEntry>) targetTasks.taskEntries) tasksToKill.add(task.action);
		for(EntityAIBase ai : tasksToKill) tasks.removeTask(ai);
		for(EntityAIBase ai : tasksToKill) targetTasks.removeTask(ai);

		tasks.addTask(1, new EntityAIWatchClosest(this, EntityPlayer.class, 15.0F));
	}

	public void positionRelativeTo(EntityPlayer player) {
		awaiting = true;

		// find a position a few blocks in front of the player
		double x = player.posX + Math.cos(player.rotationYawHead) * 5;
		double y = player.posY;
		double z = player.posZ + Math.sin(player.rotationYawHead) * 5;

		starePath = getNavigator().getPathToXYZ(x, y, z);
	}

	@Override
	protected void updateEntityActionState() {
		// when they look at you with that fluoride stare
		if(stareTarget != null) {
			isJumping = false;

			if(shouldApproach && starePath == null) {
				double x = stareTarget.posX + Math.cos(stareTarget.rotationYawHead) * 0.75;
				double y = stareTarget.posY;
				double z = stareTarget.posZ + Math.sin(stareTarget.rotationYawHead) * 0.75;
				// starePath = getNavigator().getPathToEntityLiving(stareTarget);
				starePath = getNavigator().getPathToXYZ(x, y, z);
			}

			if(shouldApproach) {
				if(starePath != null && !followPath(starePath)) starePath = null;
				if(starePath != null) moveForward = 0.6F;
				// float distance = this.entityToAttack.getDistanceToEntity(this);
				// moveForward = distance > 1.0F ? 0.1F : 0.0F;
			}

			faceEntity(stareTarget, 10.0F, 10.0F);

			return;
		}

		if(awaiting) {
			if(starePath != null && !followPath(starePath)) starePath = null;
			return;
		}

		super.updateEntityActionState();
	}

	private boolean followPath(PathEntity pathToEntity) {
		Vec3 vec3 = pathToEntity.getPosition(this);
		double diameter = (double)(this.width * 2.0F);

		while(vec3 != null && vec3.squareDistanceTo(this.posX, vec3.yCoord, this.posZ) < diameter * diameter) {
			pathToEntity.incrementPathIndex();

			if(pathToEntity.isFinished()) {
				vec3 = null;
			} else {
				vec3 = pathToEntity.getPosition(this);
			}
		}

		this.isJumping = false;

		if(vec3 != null) {
			double tx = vec3.xCoord - this.posX;
			double tz = vec3.zCoord - this.posZ;
			double ty = vec3.yCoord - (double)MathHelper.floor_double(this.boundingBox.minY + 0.5D);
			float dirTo = (float)(Math.atan2(tz, tx) * 180.0D / Math.PI) - 90.0F;
			float relativeDirTo = MathHelper.wrapAngleTo180_float(dirTo - this.rotationYaw);
			this.moveForward = (float)this.getEntityAttribute(SharedMonsterAttributes.movementSpeed).getAttributeValue();

			if(relativeDirTo > 30.0F) {
				relativeDirTo = 30.0F;
			}

			if(relativeDirTo < -30.0F) {
				relativeDirTo = -30.0F;
			}

			this.rotationYaw += relativeDirTo;

			if(this.hasAttacked && this.entityToAttack != null) {
				double d4 = this.entityToAttack.posX - this.posX;
				double d5 = this.entityToAttack.posZ - this.posZ;
				float f3 = this.rotationYaw;
				this.rotationYaw = (float)(Math.atan2(d5, d4) * 180.0D / Math.PI) - 90.0F;
				relativeDirTo = (f3 - this.rotationYaw + 90.0F) * (float)Math.PI / 180.0F;
				this.moveStrafing = -MathHelper.sin(relativeDirTo) * this.moveForward * 1.0F;
				this.moveForward = MathHelper.cos(relativeDirTo) * this.moveForward * 1.0F;
			}

			if(ty > 0.0D) {
				this.isJumping = true;
			}

			return true;
		}

		return false;
	}

	@Override
	protected Entity findPlayerToAttack() {
		EntityPlayer player = worldObj.getClosestVulnerablePlayerToEntity(this, 16.0D);
		if(killedPlayers.contains(player)) return null;
		return player;
	}

	@Override
	protected void attackEntity(Entity target, float distance) {
		if(this.attackTime <= 0 && distance < 2.5F && target.boundingBox.maxY > this.boundingBox.minY && target.boundingBox.minY < this.boundingBox.maxY) {
			this.attackTime = 20;
			this.attackEntityAsMob(target);
		}
	}

	@Override
	public boolean attackEntityAsMob(Entity target) {

		// Good evening, you are dead
		if(target instanceof EntityPlayerMP) {
			killedPlayers.add((EntityPlayer) target);
			entityToAttack = null;
			PacketDispatcher.wrapper.sendTo(new EntityBufPacket(getEntityId(), this), (EntityPlayerMP) target);
		}

		return super.attackEntityAsMob(target);
	}

	@Override
	public void serialize(ByteBuf buf) {

	}

	@Override
	public void deserialize(ByteBuf buf) {
		quoteKillUnquote();
	}

	@SideOnly(Side.CLIENT)
	private void quoteKillUnquote() {
		Minecraft.getMinecraft().displayGuiScreen(new GUIRefuseDeath(this));
	}

	@Override
	public void setHealth(float health) {
		super.setHealth(this.getMaxHealth());
	}

	@Override
	public boolean isEntityInvulnerable() {
		return true;
	}

	@Override
	public void interaction(ByteBuf buf) {
		byte state = buf.readByte();
		int playerEntityId = buf.readInt();
		Entity entity = worldObj.getEntityByID(playerEntityId);

		switch(state) {
		case 0: if(entity instanceof EntityPlayer) positionRelativeTo((EntityPlayer) entity); break;
		case 1: if(entity instanceof EntityPlayer) noticePlayer((EntityPlayer) entity); break;
		case 2: shouldApproach = true; break;
		case 3: setDead(); break; // just kill the entity for now, for testing
		}
	}

	@Override
	public int getDimensionId() {
		return worldObj.provider.dimensionId;
	}

}
