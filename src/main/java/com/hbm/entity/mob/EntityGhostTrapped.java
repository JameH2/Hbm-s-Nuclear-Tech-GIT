package com.hbm.entity.mob;

import java.util.HashSet;
import java.util.Set;

import com.hbm.inventory.gui.GUIRefuseDeath;
import com.hbm.packet.PacketDispatcher;
import com.hbm.packet.toclient.EntityBufPacket;
import com.hbm.tileentity.IBufPacketReceiver;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.ai.EntityAIAttackOnCollide;
import net.minecraft.entity.ai.EntityAILookIdle;
import net.minecraft.entity.ai.EntityAINearestAttackableTarget;
import net.minecraft.entity.ai.EntityAIWatchClosest;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.world.World;

public class EntityGhostTrapped extends EntityCreature implements IBufPacketReceiver {

	Set<EntityPlayer> killedPlayers = new HashSet<>();

	public EntityGhostTrapped(World world) {
		super(world);
		this.tasks.addTask(1, new EntityAIAttackOnCollide(this, EntityPlayer.class, 1.0D, true));
		this.tasks.addTask(2, new EntityAILookIdle(this));
		this.tasks.addTask(3, new EntityAIWatchClosest(this, EntityPlayer.class, 15.0F));
        this.targetTasks.addTask(2, new EntityAINearestAttackableTarget(this, EntityPlayer.class, 0, false));
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

}
