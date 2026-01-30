package com.hbm.dim.trait;

import com.hbm.blocks.ModBlocks;
import com.hbm.config.GeneralConfig;
import com.hbm.config.MobConfig;
import com.hbm.entity.mob.EntityMaskMan;
import com.hbm.entity.mob.siege.EntitySiegeCraft;
import com.hbm.util.ContaminationUtil;

import io.netty.buffer.ByteBuf;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.stats.StatBase;
import net.minecraft.stats.StatList;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.world.World;

public class CBT_Invasion extends CelestialBodyTrait{

	//while i could polymorphize to the heavens, this event is more-or-less "scripted" in the sense that you would be fighting the ufo types we HAVE sequentially to the boss
	//oh dont worry you still get to have fun killing! :)
	
	public int wave;
	public int kills;
	public double waveTime;
	public boolean isInvading;
	public int lastSpawns; //prevent over-lagging the server
	public int spawndelay;
	
	public CBT_Invasion() {
		
	}
	


	public CBT_Invasion(int wave, double waveTime, boolean isInvading) {
		this.wave = wave;
		this.waveTime = waveTime;
		this.isInvading = isInvading;
	}
	
	public void Prepare() {

		if (!isInvading && waveTime > 100) {
			waveTime--;
			if (waveTime <= 0) {
				isInvading = true;
			}
		}

	}
	
	public void Invade(int killReq, double wavetimerbase) {
		if(!isInvading) return;
		
		waveTime--;
		
		if(waveTime <= 0) {
			wave++;
			waveTime = wavetimerbase;
		}
		
	}
	
	public void Spawn() {
		
		if(lastSpawns > 10) return; //10 total mobs
		
		switch (wave) {
		case 1:
			
			break;
		case 2:
			
			break;
		case 3:
			
			break; 
		default:
			break;
		}
		
		
	}
	
	public static void SpawnAttempt(World world) {




			if(world.getTotalWorldTime() % 60 == 0) {

				if(!world.playerEntities.isEmpty()) {	

					EntityPlayer player = (EntityPlayer) world.playerEntities.get(world.rand.nextInt(world.playerEntities.size()));	//choose a random player

		
					if(!(player instanceof EntityPlayerMP)) return;
					EntityPlayerMP playerMP = (EntityPlayerMP) player;


						player.addChatComponentMessage(new ChatComponentText("Incoming!").setChatStyle(new ChatStyle().setColor(EnumChatFormatting.RED)));

						double spawnX = player.posX + world.rand.nextGaussian() * 20;
						double spawnZ = player.posZ + world.rand.nextGaussian() * 20;
						double spawnY = world.getHeightValue((int)spawnX, (int)spawnZ);

					
						EntitySiegeCraft invaderCraft = new EntitySiegeCraft(world);
						invaderCraft.setLocationAndAngles(spawnX, spawnY, spawnZ, world.rand.nextFloat() * 360.0F, 0.0F);
						world.spawnEntityInWorld(invaderCraft);
					}
				}
			
		}
	
	
	@Override
	public void writeToNBT(NBTTagCompound nbt) {
		nbt.setInteger("wave", wave);
		nbt.setInteger("kills", kills);
		nbt.setDouble("waveTime", waveTime);
		nbt.setBoolean("isInvading", isInvading);
		nbt.setInteger("lastSpawns", lastSpawns);
		nbt.setInteger("spawnDelay", spawndelay);

	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		wave = nbt.getInteger("wave");
		kills = nbt.getInteger("kills");
		isInvading = nbt.getBoolean("isInvading");
		waveTime = nbt.getDouble("waveTime");
		lastSpawns = nbt.getInteger("lastSpawns");
		spawndelay = nbt.getInteger("spawnDelay");
	}

	@Override
	public void writeToBytes(ByteBuf buf) {
		buf.writeInt(kills);
		buf.writeInt(wave);
		buf.writeDouble(waveTime);
		buf.writeBoolean(isInvading);
		buf.writeInt(lastSpawns);
		buf.writeInt(spawndelay);

	}

	@Override
	public void readFromBytes(ByteBuf buf) {
		kills = buf.readInt();
		wave = buf.readInt();
		waveTime = buf.readDouble();
		isInvading = buf.readBoolean();
		lastSpawns = buf.readInt();
		spawndelay = buf.readInt();
	}

}
