package com.hbm.dim.trait;

import com.hbm.entity.mob.siege.EntitySiegeCraft;

import io.netty.buffer.ByteBuf;
import com.hbm.blocks.ModBlocks;
import com.hbm.config.GeneralConfig;
import com.hbm.config.MobConfig;
import com.hbm.dim.CelestialBody;
import com.hbm.entity.mob.EntityMaskMan;
import com.hbm.entity.mob.siege.EntitySiegeCraft;
import com.hbm.main.MainRegistry;
import com.hbm.util.ContaminationUtil;

import io.netty.buffer.ByteBuf;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
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
	
	public boolean warningPlayed;
	
	public CBT_Invasion() {
		
	}
	


	public CBT_Invasion(int wave, double waveTime, boolean isInvading) {
		this.wave = wave;
		this.waveTime = waveTime;
		this.isInvading = isInvading;
	}
	
	public void Prepare() {
		System.out.println(waveTime);

		if (!isInvading && waveTime >= 0) {
			waveTime--;
			warningPlayed = true;
			if (waveTime <= 5) {
				warningPlayed = false;

			}
			if (waveTime <= 0) {

				isInvading = true;

			}

		}

	}
	
	public void Invade(int killReq, double wavetimerbase) {
		if(!isInvading) return;
		waveTime--;
		if(waveTime <= 0 || kills % 10 == 0) {
			wave++;
			waveTime = wavetimerbase;
		}
		
	}
	
	@Override
	public void update(boolean isRemote) {
		if(!isRemote) {
			Prepare();
			Invade(30, 60*60);
		}else {
			if (!isInvading && !warningPlayed) {
				warningPlayed = true;
				System.out.println(isInvading);
				MainRegistry.proxy.me().playSound("hbm:alarm.ping", 10F, 1F);
				MainRegistry.proxy.me().addChatComponentMessage(new ChatComponentText("Incoming Invasion!").setChatStyle(new ChatStyle().setColor(EnumChatFormatting.RED)));
			}
	
		}

	}
	
	
	
	public void Increment() {
		kills++;
		lastSpawns--;
		System.out.println(kills);
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
	
	public void SpawnAttempt(World world) {
		if(lastSpawns > 10 || !isInvading) return;
			if(world.getTotalWorldTime() % 260 == 0) {

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
						lastSpawns++;
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
	        nbt.setBoolean("warningPlayed", warningPlayed);
	    }

	    @Override
	    public void readFromNBT(NBTTagCompound nbt) {
	        wave = nbt.getInteger("wave");
	        kills = nbt.getInteger("kills");
	        isInvading = nbt.getBoolean("isInvading");
	        waveTime = nbt.getDouble("waveTime");
	        lastSpawns = nbt.getInteger("lastSpawns");
	        spawndelay = nbt.getInteger("spawnDelay");
	        warningPlayed = nbt.getBoolean("warningPlayed");
	    }

	    @Override
	    public void writeToBytes(ByteBuf buf) {
	        buf.writeInt(kills);
	        buf.writeInt(wave);
	        buf.writeDouble(waveTime);
	        buf.writeBoolean(isInvading);
	        buf.writeInt(lastSpawns);
	        buf.writeInt(spawndelay);
	        buf.writeBoolean(warningPlayed);
	    }

	    @Override
	    public void readFromBytes(ByteBuf buf) {
	        kills = buf.readInt();
	        wave = buf.readInt();
	        waveTime = buf.readDouble();
	        isInvading = buf.readBoolean();
	        lastSpawns = buf.readInt();
	        spawndelay = buf.readInt();
	        warningPlayed = buf.readBoolean();
	    }
}