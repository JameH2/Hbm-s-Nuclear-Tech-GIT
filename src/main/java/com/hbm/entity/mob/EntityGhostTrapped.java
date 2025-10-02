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
import net.minecraft.entity.ai.EntityAIAttackOnCollide;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.ai.EntityAILookIdle;
import net.minecraft.entity.ai.EntityAINearestAttackableTarget;
import net.minecraft.entity.ai.EntityAIWatchClosest;
import net.minecraft.entity.ai.EntityAITasks.EntityAITaskEntry;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
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

	private EntityPlayer stareTarget;
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
		setPathToEntity(null);
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

	@Override
	protected void updateEntityActionState() {
		// when they look at you with that fluoride stare
		if(stareTarget != null) {
			isJumping = false;
			faceEntity(stareTarget, 10.0F, 10.0F);

			// if(shouldApproach && !hasPath()) {
			// 	setPathToEntity(worldObj.getPathEntityToEntity(this, stareTarget, 16.0F, true, false, false, true));
			// }

			if(shouldApproach) {
				moveForward = 0.1F;
			}

			return;
		}
		super.updateEntityActionState();
	}

	@Override
	protected Entity findPlayerToAttack() {
		EntityPlayer player = worldObj.getClosestVulnerablePlayerToEntity(this, 16.0D);
		if(killedPlayers.contains(player)) return null;
		return player;
	}

	@Override
	protected void attackEntity(Entity target, float distance) {
		if(this.attackTime <= 0 && distance < 2.0F && target.boundingBox.maxY > this.boundingBox.minY && target.boundingBox.minY < this.boundingBox.maxY) {
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
		case 0: if(entity instanceof EntityPlayer) noticePlayer((EntityPlayer) entity); break;
		case 1: shouldApproach = true; break;
		case 2: setDead(); break; // just kill the entity for now, for testing
		}
	}

	@Override
	public int getDimensionId() {
		return worldObj.provider.dimensionId;
	}

}
