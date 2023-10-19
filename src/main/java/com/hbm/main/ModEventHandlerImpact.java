package com.hbm.main;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import com.hbm.blocks.ModBlocks;
import com.hbm.config.GeneralConfig;
import com.hbm.config.WorldConfig;
import com.hbm.dim.BiomeGenBaseQuackosian;
import com.hbm.entity.mob.EntityDuck;
import com.hbm.entity.projectile.EntityTom;
import com.hbm.handler.BossSpawnHandler;
import com.hbm.handler.ImpactWorldHandler;
import com.hbm.handler.crater.ChunkCraterManager;
import com.hbm.lib.ModDamageSource;
import com.hbm.saveddata.TomSaveData;
import com.hbm.world.WorldProviderNTM;

import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.eventhandler.Event.Result;
import cpw.mods.fml.common.gameevent.TickEvent.Phase;
import cpw.mods.fml.common.gameevent.TickEvent.WorldTickEvent;
import net.minecraft.block.Block;
import net.minecraft.block.BlockBush;
import net.minecraft.block.BlockCrops;
import net.minecraft.block.BlockDoor;
import net.minecraft.block.BlockGrass;
import net.minecraft.block.BlockLeaves;
import net.minecraft.block.BlockLog;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.passive.EntityWaterMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MathHelper;
import net.minecraft.world.ChunkCoordIntPair;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.World;
import net.minecraft.world.WorldType;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingSpawnEvent;
import net.minecraftforge.event.terraingen.BiomeEvent;
import net.minecraftforge.event.terraingen.ChunkProviderEvent;
import net.minecraftforge.event.terraingen.DecorateBiomeEvent;
import net.minecraftforge.event.terraingen.PopulateChunkEvent;
import net.minecraftforge.event.terraingen.DecorateBiomeEvent.Decorate.EventType;
import net.minecraftforge.event.world.ChunkDataEvent;
import net.minecraftforge.event.world.ChunkEvent;
import net.minecraftforge.event.world.WorldEvent;

public class ModEventHandlerImpact {
	
	//////////////////////////////////////////
	private static Random rand = new Random();
	//////////////////////////////////////////
	
	@SubscribeEvent
	public void worldTick(WorldTickEvent event) {

		if(event.world != null && !event.world.isRemote && event.phase == Phase.START) {
			float settle = 1F / 14400000F; 	// 600 days to completely clear all dust.
			float cool = 1F / 24000F;		// One MC day between initial impact and total darkness.
			
			ImpactWorldHandler.impactEffects(event.world);
			TomSaveData data = TomSaveData.forWorld(event.world);
			
			if(data.dust > 0 && data.fire == 0) {
				data.dust = Math.max(0, data.dust - settle);
				data.markDirty();
			}
			
			if(data.fire > 0) {
				data.fire = Math.max(0, (data.fire - cool));
				data.dust = Math.min(1, (data.dust + cool));
				data.markDirty();
			}
			
			long r = (event.world.getWorldTime())+data.time;
			long t = (data.dtime);
			
			if(data.time > 0) {
				data.time--;
				if(r != t)
				{
					
					//System.out.println("R: "+(r)/20+" T: "+(t)/20+" Difference: "+(r-t)/20+" time: "+(data.time)/20+" dtime: "+(data.dtime)/20);
					data.time-=(r-t);
					//r = (event.world.getWorldTime()-w)+delay;
				}
				if(data.time<=2)
				{
					data.impact=true;
					data.fire=1;
					if(!event.world.loadedEntityList.isEmpty()) {
						
						List<Object> oList = new ArrayList<Object>();
						oList.addAll(event.world.loadedEntityList);
						
						for(Object e : oList) {
							if(e instanceof EntityLivingBase) {
								EntityLivingBase entity = (EntityLivingBase) e;
								double dX = Math.pow((data.x - entity.posX), 2);
								double dZ = Math.pow((data.z - entity.posZ), 2);
								double distance = MathHelper.sqrt_double(dX + dZ);
								
								if(entity.worldObj.provider.dimensionId == 0 && distance <=2300) {
			
									entity.setFire(5);
									entity.attackEntityFrom(ModDamageSource.asteroid, 66043000);
									entity.onDeath(ModDamageSource.asteroid);
									//entity.setHealth(0);
								}
							}
						}
					}
				}
				if(data.time<=2400)
				{
					List<EntityPlayer> entities = event.world.playerEntities;
					for(Iterator<EntityPlayer> en = new ArrayList<>(entities).iterator() ; en.hasNext();) {
						EntityPlayer e = en.next();
						Random rand = new Random();
						if(rand.nextInt(100)==0)
						{
							BossSpawnHandler.spawnMeteorAtPlayer(e, false, true);
						}	
					}
				}
				/*if(data.time==data.dtime)
				{
					EntityTom tom = new EntityTom(event.world);
					tom.setPosition(data.x + 0.5, 600, data.z + 0.5);
					event.world.spawnEntityInWorld(tom);
					IChunkProvider provider = event.world.getChunkProvider();
					provider.loadChunk(data.x >> 4, data.z >> 4);
				}*/
				data.markDirty();
			}
			
			if(!event.world.loadedEntityList.isEmpty()) {
				
				List<Object> oList = new ArrayList<Object>();
				oList.addAll(event.world.loadedEntityList);
				
				for(Object e : oList) {
					if(e instanceof EntityLivingBase) {
						EntityLivingBase entity = (EntityLivingBase) e;
						
						if(entity.worldObj.provider.dimensionId == 0 && data.fire > 0 && data.dust < 0.75f &&
								event.world.getSavedLightValue(EnumSkyBlock.Sky, (int) entity.posX, (int) entity.posY, (int) entity.posZ) > 7) {
							
							entity.setFire(5);
							entity.attackEntityFrom(DamageSource.onFire, 2);
						}
					}
				}
			}
		}
	}

	//data is always pooled out of the perWorld save data so resetting values isn't needed
	/*@SubscribeEvent(priority = EventPriority.LOWEST)
	public void onUnload(WorldEvent.Unload event) {
		// We don't want Tom's impact data transferring between worlds.
		TomSaveData data = TomSaveData.forWorld(event.world);
		this.fire = 0;
		this.dust = 0;
		this.impact = false;
		data.fire = 0;
		data.dust = 0;
		data.impact = false;
	}*/

	@SubscribeEvent
	public void extinction(EntityJoinWorldEvent event) {
		
		TomSaveData data = TomSaveData.forWorld(event.world);
		
		if(data.impact) {
			if(!(event.entity instanceof EntityPlayer) && event.entity instanceof EntityLivingBase) {
				EntityLivingBase living = (EntityLivingBase) event.entity;
				if(event.world.provider.dimensionId == 0) {
					if(event.entity.height >= 0.85f || event.entity.width >= 0.85f && event.entity.ticksExisted < 20 && !(event.entity instanceof EntityWaterMob) && !living.isChild()) {
						event.setCanceled(true);
					}
				}
				if(event.entity instanceof EntityWaterMob && event.entity.ticksExisted < 20) {
					Random rand = new Random();
					if(rand.nextInt(9) != 0) {
						event.setCanceled(true);
					}
				}
			}		
		}		
	}

	/*@SubscribeEvent
	public void ReplaceBiomeBlocks(ChunkProviderEvent.ReplaceBiomeBlocks event) {
		TomSaveData data = TomSaveData.forWorld(event.world);
		int X = data.x;
		int Z = data.z;
		if(data.impact)
		{
			Block chunkArray[] = event.blockArray;
			for(int x = 15; x >= 0; x--) {
				for(int z = 15; z >= 0; z--) {
					for(int y = 254; y >= 0; y--) {
						int index = (x * 16 + z) * 256 + y;
						int blockX = event.chunkX*16+x;
						int blockZ = event.chunkZ*16+z;
						ChunkCraterManager.proxy.setCraterGen(event.world, blockX, blockZ, true);
						double dX = Math.pow((X - blockX), 2);
						double dZ = Math.pow((Z - blockZ), 2);
						double distance = MathHelper.sqrt_double(dX + dZ);
						int breccia = (int) (-10+Math.pow(Math.E, (distance/518d)));
						int lens = (int) (32+Math.pow(Math.E, (distance/670d)));
						int outerRim = (int) (lens+(Math.pow(Math.E, -Math.pow(distance - 2000, 2)/24000)*50));
						int chicxulub = (int) (outerRim+(Math.pow(Math.E, -Math.pow(distance - 1000, 2)/24000)*50))+ event.world.rand.nextInt(2);

						//BiomeGenBase b = event.world.getBiomeGenForCoords(blockX, blockZ);
						if (distance<=3500)
						{
							ChunkCraterManager.proxy.setCraterGen(event.world, (event.chunkX*16)+8, (event.chunkZ*16)+8, true);
						}
						if(distance<=2250)
						{						
							event.biomeArray[(blockZ & 15) << 4 | (blockX & 15)] = BiomeGenBaseQuackosian.crater;
						}
						if (distance>3500)
						{
							return;
						}
						if((y)>chicxulub)
						{
							//int Y2 = Math.max(0,(int)Math.floor(y/16));
							int Y3 = (int)Math.floor(y%16);
								//Block block = storage.getBlockByExtId(x, (Y3), z);
								//if (block==Blocks.air)
								//{
							chunkArray[index] = null;
							if(distance<=2000 && y<64)
							{
								chunkArray[index] =  Blocks.lava;	
							}
								//}						
						}
						if(y<=chicxulub && y>breccia)
						{
							if (chunkArray[index] !=Blocks.bedrock)
							{
								if(event.world.rand.nextInt(499) < 1) {
									chunkArray[index] =  ModBlocks.ore_tektite_osmiridium;
								} else {
									chunkArray[index] =  ModBlocks.tektite;
								}
							}
							if (chunkArray[index] ==Blocks.bedrock)
							{
								chunkArray[index] =  ModBlocks.ore_volcano;
							}
						}

					}
				}
			}
		}
	}*/

	
	  @SubscribeEvent
	  public void preQuackosianDuckSpawn(LivingSpawnEvent.CheckSpawn event)
	  {
		  TomSaveData data = TomSaveData.forWorld(event.world);
		  if(event.entity instanceof EntityDuck && !data.impact)
		  {
			  event.setResult(Result.DENY);
		  }
	  }
	
	@SubscribeEvent(priority = EventPriority.LOWEST)
	public void onLoad(WorldEvent.Load event) {
		
		TomSaveData.resetLastCached();
		
		if(GeneralConfig.enableImpactWorldProvider) {
			DimensionManager.unregisterProviderType(0);
			DimensionManager.registerProviderType(0, WorldProviderNTM.class, true);
		}
	}

	@SubscribeEvent(priority = EventPriority.LOWEST)
	public void onUnload(WorldEvent.Unload event) {
		
		TomSaveData.resetLastCached();
	}
	
	@SubscribeEvent
	public void modifyVillageGen(BiomeEvent.GetVillageBlockID event) {
		Block b = event.original;
		Material mat = event.original.getMaterial();
		
		TomSaveData data = TomSaveData.getLastCachedOrNull();
		
		if(data == null || event.biome == null) {
			return;
		}
		
		if(data.impact) {
			if(mat == Material.wood || mat == Material.glass || b == Blocks.ladder || b instanceof BlockCrops ||
					b == Blocks.chest || b instanceof BlockDoor || mat == Material.cloth || mat == Material.water || b == Blocks.stone_slab) {
				event.replacement = Blocks.air;
				
			} else if(b == Blocks.cobblestone || b == Blocks.stonebrick) {
				if(rand.nextInt(3) == 1) {
					event.replacement = Blocks.gravel;
				}
			} else if(b == Blocks.sandstone) {
				if(rand.nextInt(3) == 1) {
					event.replacement = Blocks.sand;
				}
			} else if(b == Blocks.farmland) {
				event.replacement = Blocks.dirt;
			}
		}
		
		if(event.replacement != null) {
			event.setResult(Result.DENY);
		}
	}

	
	@SubscribeEvent
	public void postImpactGeneration(BiomeEvent event) {
		/// Disables post-impact surface replacement for superflat worlds
		/// because they are retarded and crash with a NullPointerException if
		/// you try to look for biome-specific blocks.
		TomSaveData data = TomSaveData.getLastCachedOrNull(); //despite forcing the data, we cannot rule out canceling events or custom firing shenanigans 
		if(data != null && event.biome != null) {
			if(event.biome.topBlock != null) {
				if(event.biome.topBlock == Blocks.grass) {
					if(data.impact && (data.dust > 0 || data.fire > 0)) {
						event.biome.topBlock = ModBlocks.impact_dirt;
					} else {
						event.biome.topBlock = Blocks.grass;
					}
				}
			}
		}
	}

	@SubscribeEvent
	public void postImpactDecoration(DecorateBiomeEvent.Decorate event) {
		
		TomSaveData data = TomSaveData.forWorld(event.world);
		//Chunk chunk = event.world.getChunkFromChunkCoords(X >> 4, Z >> 4);
		
		if(data.impact) {
			EventType type = event.type;
			
			if(data.dust > 0 || data.fire > 0) {
				if(type == event.type.TREE || type == event.type.BIG_SHROOM || type == event.type.GRASS || type == event.type.REED || type == event.type.FLOWERS || type == event.type.DEAD_BUSH
						|| type == event.type.CACTUS || type == event.type.PUMPKIN || type == event.type.LILYPAD) {
					event.setResult(Result.DENY);
				}
				
			} else if(data.dust == 0 && data.fire == 0) {
				if(type == event.type.TREE || type == event.type.BIG_SHROOM || type == event.type.CACTUS) {
					Random rand = new Random();
					if(rand.nextInt(4) == 0) {
						event.setResult(Result.DEFAULT);
					} else {
						event.setResult(Result.DENY);
					}
				}
				
				if(type == event.type.GRASS || type == event.type.REED) {
					event.setResult(Result.DEFAULT);
				}
			}
			
		} else {
			event.setResult(Result.DEFAULT);
		}
	}

	@SubscribeEvent
	public void populateChunkPost(PopulateChunkEvent.Populate event) {
		TomSaveData data = TomSaveData.forWorld(event.world);
		
		int X = data.x;
		int Z = data.z;
		if(data.impact) {
			for(int x = 15; x >= 0; x--) {
				for(int z = 15; z >= 0; z--) {
					int blockX = event.chunkX*16+x;
					int blockZ = event.chunkZ*16+z;
					double dX = Math.pow((X - blockX), 2);
					double dZ = Math.pow((Z - blockZ), 2);
					double distance = MathHelper.sqrt_double(dX + dZ);
					if(distance<=2300)
					{
						event.setResult(Result.DENY);
					}
				}
			}
		}
	}
	
	@SubscribeEvent
	public void populateChunkPost(PopulateChunkEvent.Post event) {
		
		TomSaveData data = TomSaveData.forWorld(event.world);
		
		if(data.impact) {
			Chunk chunk = event.world.getChunkFromChunkCoords(event.chunkX, event.chunkZ);
			
			for(ExtendedBlockStorage storage : chunk.getBlockStorageArray()) {
				
				if(storage != null) {
					
					for(int x = 0; x < 16; ++x) {
						for(int y = 0; y < 16; ++y) {
							for(int z = 0; z < 16; ++z) {
								if(storage.getBlockByExtId(x, y, z)== Blocks.lava)
								{
									storage.setExtBlocklightValue(x, y, z, 15);
								}
								if((data.dust > 0.25 || data.fire > 0)&& data.impact) {
									if(storage.getBlockByExtId(x, y, z) == Blocks.grass) {
										storage.func_150818_a(x, y, z, ModBlocks.impact_dirt);
									} else if(storage.getBlockByExtId(x, y, z) instanceof BlockLog) {
										storage.func_150818_a(x, y, z, Blocks.air);
									} else if(storage.getBlockByExtId(x, y, z) instanceof BlockLeaves) {
										storage.func_150818_a(x, y, z, Blocks.air);
									} else if(storage.getBlockByExtId(x, y, z).getMaterial() == Material.leaves) {
										storage.func_150818_a(x, y, z, Blocks.air);
									} else if(storage.getBlockByExtId(x, y, z).getMaterial() == Material.plants) {
										storage.func_150818_a(x, y, z, Blocks.air);
									} else if(storage.getBlockByExtId(x, y, z) instanceof BlockBush) {
										storage.func_150818_a(x, y, z, Blocks.air);
									}
								}
							}
						}
					}
				}
			}
		}
	}
	@SubscribeEvent
	public void chunkLoad(ChunkEvent.Load event) {

		TomSaveData data = TomSaveData.forWorld(event.world);
		
		int X = data.x;
		int Z = data.z;
		
		World world = event.world;
		
		if(world.provider == null || world.provider.dimensionId != 0 || world.provider.terrainType == WorldType.FLAT)
			return;
		
		//int cX = event.getChunk().xPosition;
		//int cZ = event.getChunk().zPosition;
		
		//double scale = 0.01D;
		//int threshold = 5;
		//Random rand = new Random();
		//int terrain = 63;
		//int height = terrain - 14;
		//int offset = 20;
		if(data.impact && !ChunkCraterManager.proxy.getCraterGen(event.world, event.getChunk().xPosition*16, event.getChunk().zPosition*16))
		{
			Chunk chunk = event.getChunk();
			ExtendedBlockStorage[] storageArray = chunk.getBlockStorageArray();
			for (int y = 0; y < 256; ++y) 
			{
				
				for(int x = 0; x < 16; x++) {
					for(int z = 0; z < 16; z++) {					
							int blockX = event.getChunk().xPosition*16+x;
							int blockZ = event.getChunk().zPosition*16+z;
							double dX = Math.pow((X - blockX), 2);
							double dZ = Math.pow((Z - blockZ), 2);
							double distance = MathHelper.sqrt_double(dX + dZ);
							int breccia = (int) (-10+Math.pow(Math.E, (distance/518d)));
							int lens = (int) (32+Math.pow(Math.E, (distance/670d)));
							int outerRim = (int) (lens+(Math.pow(Math.E, -Math.pow(distance - 2000, 2)/24000)*50));
							int chicxulub = (int) (outerRim+(Math.pow(Math.E, -Math.pow(distance - 1000, 2)/24000)*50))+ event.world.rand.nextInt(2);
							//for(int y2 = 0; y2 < 16; y2++) {
							if(distance<=3500)
							{
									ExtendedBlockStorage storage = storageArray[y>>4];	
									if(distance<=2250)
									{						
										chunk.getBiomeArray()[(blockZ & 15) << 4 | (blockX & 15)] = (byte)BiomeGenBaseQuackosian.crater.biomeID;
										//ChunkCraterManager.proxy.setCraterGen(event.world, (event.chunkX*16)+8, (event.chunkZ*16)+8, true);
									}
									if((y)>chicxulub)
									{
										//int Y2 = Math.max(0,(int)Math.floor(y/16));
										if(storage == null) {
											storage = storageArray[y>>4] = new ExtendedBlockStorage(y >> 4 << 4, !event.world.provider.hasNoSky);
										}
										if (storage != null) {
											int Y3 = (int)Math.floor(y%16);
											int Y4 = (int)Math.floor((y+1)%16);
											//Block block = storage.getBlockByExtId(x, (Y3), z);
											//if (block==Blocks.air)
											//{
												storage.func_150818_a(x, (Y3), z, Blocks.air);
												storage.setExtSkylightValue(x, (Y3), z, 15);
												if(y<64 && distance<=2000)
												{
													storage.func_150818_a(x, (Y3), z, Blocks.lava);	
													storage.setExtBlocklightValue(x, (Y3), z, 15);
												}
											//}
										}
									}
									if(y<=chicxulub && y>breccia)
									{
										if(storage == null) {
											storage = storageArray[y>>4] = new ExtendedBlockStorage(y >> 4 << 4, !event.world.provider.hasNoSky);
										}
										if (storage != null) {
											int Y3 = (int)Math.floor(y%16);
											Block block = storage.getBlockByExtId(x, (Y3), z);
											if(world.rand.nextInt(499) < 1) {
												storage.func_150818_a(x, Y3, z, ModBlocks.ore_tektite_osmiridium);
											} else {
												storage.func_150818_a(x, Y3, z, ModBlocks.tektite);
											}
											if (block==Blocks.bedrock)
											{
												storage.func_150818_a(x, Y3, z, ModBlocks.ore_volcano);
											}
										}
									}
									ChunkCraterManager.proxy.setCraterGen(event.world, (event.getChunk().xPosition*16)+8, (event.getChunk().zPosition*16)+8, true);
								}						
							}
						}
					//}
				//}	
			}
		}
		

		
		/*if(data.impact && !event.world.isRemote) {
			for(int x = 15; x >= 0; x--) {
				for(int z = 15; z >= 0; z--) {
					int blockX = event.getChunk().xPosition*16+x;
					int blockZ = event.getChunk().zPosition*16+z;
					double dX = Math.pow((X - blockX), 2);
					double dZ = Math.pow((Z - blockZ), 2);
					double distance = MathHelper.sqrt_double(dX + dZ);
					if(distance<=2500)
					{
						if(!ChunkCraterManager.proxy.getCraterGen(event.world, blockX, blockZ))
						{
							ImpactWorldHandler.regenerateChunk(event.world, event.getChunk());
					        ChunkCraterManager.proxy.setCraterGen(event.world, (event.getChunk().xPosition*16)+8, (event.getChunk().zPosition*16)+8, true);
					        return;
						}
					}
				}
			}
		}*/
		/*if(!event.world.isRemote) {
			World world = event.world;
			Chunk chunk = event.getChunk();
			NBTTagCompound nbt = event.getData();
			if(!nbt.hasKey("impact")) {
				nbt.setTag("impact", new NBTTagCompound());
			}
			nbt = event.getData().getCompoundTag("impact");
			if(!nbt.getBoolean("crater"))
			{
				TomSaveData data = TomSaveData.forWorld(event.world);
				
				int X = data.x;
				int Z = data.z;
				if(data.impact) {
					for(int x = 15; x >= 0; x--) {
						for(int z = 15; z >= 0; z--) {
							int blockX = event.getChunk().xPosition*16+x;
							int blockZ = event.getChunk().zPosition*16+z;
							double dX = Math.pow((X - blockX), 2);
							double dZ = Math.pow((Z - blockZ), 2);
							double distance = MathHelper.sqrt_double(dX + dZ);
							if(distance<=2300)
							{
								ImpactWorldHandler.regenerateChunk(world, event.getChunk().xPosition, event.getChunk().zPosition);
								return;
							}
						}
					}
				}
			}
					
		}*/
	}
}
