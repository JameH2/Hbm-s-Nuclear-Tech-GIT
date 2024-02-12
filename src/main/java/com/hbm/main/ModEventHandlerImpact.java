package com.hbm.main;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import com.hbm.blocks.ModBlocks;
import com.hbm.config.GeneralConfig;
import com.hbm.config.WorldConfig;
import com.hbm.entity.mob.EntityDuck;
import com.hbm.entity.projectile.EntityTom;
import com.hbm.handler.BossSpawnHandler;
import com.hbm.handler.ImpactWorldHandler;
import com.hbm.handler.crater.ChunkCraterManager;
import com.hbm.lib.ModDamageSource;
import com.hbm.saveddata.SatelliteSavedData;
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
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MathHelper;
import net.minecraft.world.ChunkCoordIntPair;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.WorldType;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingSpawnEvent;
import net.minecraftforge.event.entity.living.LivingSpawnEvent.CheckSpawn;
import net.minecraftforge.event.terraingen.BiomeEvent;
import net.minecraftforge.event.terraingen.ChunkProviderEvent;
import net.minecraftforge.event.terraingen.DecorateBiomeEvent;
import net.minecraftforge.event.terraingen.PopulateChunkEvent;
import net.minecraftforge.event.terraingen.PopulateChunkEvent.Populate;
import net.minecraftforge.event.terraingen.DecorateBiomeEvent.Decorate.EventType;
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
			
			if(data.time > 0) {
				data.time--;
				if(data.time==0 && data.shouldImpact)
				{
					data.impact=true;
					data.fire=1;
					SatelliteSavedData sats = SatelliteSavedData.getData(event.world);
					sats.sats.clear();
					if(!event.world.loadedEntityList.isEmpty()) {

						List<Object> oList = new ArrayList<Object>();
						oList.addAll(event.world.loadedEntityList);

						for(Object e : oList) {
							if(e instanceof EntityLivingBase) {
								EntityLivingBase entity = (EntityLivingBase) e;
								double dX = Math.pow((data.x - entity.posX), 2);
								double dZ = Math.pow((data.z - entity.posZ), 2);
								double distance = MathHelper.sqrt_double(dX + dZ);

								if(entity.worldObj.provider.dimensionId == 0 && distance <=1730) {

									if(entity instanceof EntityPlayer && !((EntityPlayer)entity).capabilities.isCreativeMode && ((EntityPlayer)entity).getHealth()>0)
									{
										entity.onDeath(ModDamageSource.asteroid);
										entity.setDead();
									}
									else if(!(entity instanceof EntityPlayer))
									{
										entity.onDeath(ModDamageSource.asteroid);
										entity.setDead();
									}
									//entity.setFire(5);
									//entity.attackEntityFrom(ModDamageSource.asteroid, 66043000);
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
			
			if(data.impact)
			{
				int X = data.x;
				int Z = data.z;
				WorldServer serv = (WorldServer) event.world;

				List<Chunk> list = serv.theChunkProviderServer.loadedChunks;
				//List<Chunk> list2 = new ArrayList<Chunk>();
				int listSize = list.size();

				if(listSize > 0) {
					for(int i = 0; i < 4; i++) {						
						Chunk chunk = list.get(serv.rand.nextInt(listSize));
						int blockX = chunk.xPosition*16+8;
						int blockZ = chunk.zPosition*16+8;
						double dX = Math.pow((X - blockX), 2);
						double dZ = Math.pow((Z - blockZ), 2);
						double distance = MathHelper.sqrt_double(dX + dZ);
						boolean crater = ChunkCraterManager.proxy.getCraterGen(event.world, chunk.xPosition*16, chunk.zPosition*16);
						if(!crater && distance<2600)
						{
							craterize(chunk, serv, true);
							//list2.add(chunk);
						}
					}
				}
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
	public void extinction(CheckSpawn event) {
		
		TomSaveData data = TomSaveData.forWorld(event.world);
		
		if(data.impact) {
			if(!(event.entityLiving instanceof EntityPlayer) && event.entityLiving instanceof EntityLivingBase) {
				if(event.world.provider.dimensionId == 0) {
					if(event.entityLiving.height >= 0.85F || event.entityLiving.width >= 0.85F && !(event.entity instanceof EntityWaterMob) && !event.entityLiving.isChild()) {
						event.setResult(Result.DENY);
						event.entityLiving.setDead();
					}
				}
				if(event.entityLiving instanceof EntityWaterMob) {
					Random rand = new Random();
					if(rand.nextInt(5) != 0) {
						event.setResult(Result.DENY);
						event.entityLiving.setDead();
					}
				}
			}		
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
					if(distance<=2000)
					{
						event.setResult(Result.DENY);
					}
				}
			}
		}
	}
	
	@SubscribeEvent
	public void onPopulate(Populate event) {
		
		if(event.type == Populate.EventType.ANIMALS) {
			
			TomSaveData data = TomSaveData.forWorld(event.world);
			
			if(data.impact) { // OHHH THIS IS WHAT I WAS FUCKING MISSING. WHY FORGE WHY???? WHY THE FUCK DID YOU ADVERTISE THE CANCELSPAWN EVENTHANDLER WHEN THIS EXISTS???
				event.setResult(Result.DENY);
			}
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
	public void populateChunkPost(PopulateChunkEvent.Post event) {
		
		TomSaveData data = TomSaveData.forWorld(event.world);
		
		if(data.impact) {
			Chunk chunk = event.world.getChunkFromChunkCoords(event.chunkX, event.chunkZ);
			
			for(ExtendedBlockStorage storage : chunk.getBlockStorageArray()) {
				
				if(storage != null) {
					
					for(int x = 0; x < 16; ++x) {
						for(int y = 0; y < 16; ++y) {
							for(int z = 0; z < 16; ++z) {
								/*if(storage.getBlockByExtId(x, y, z)== Blocks.lava)
								{
									storage.setExtBlocklightValue(x, y, z, 15);
								}*/
								if(data.dust > 0.25 || data.fire > 0) {
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
	
	@SubscribeEvent(priority = EventPriority.LOWEST)
	public void onEntityDeathImpact(LivingDeathEvent event) {//
		if(event.source==ModDamageSource.asteroid && event.isCanceled())
		{
			event.setCanceled(false);//We want to *really* make sure that direct Tom hits are not survivable.
		}
	}
	
	@SubscribeEvent(priority = EventPriority.LOWEST)
	public void onUnload(WorldEvent.Unload event) {

		TomSaveData.resetLastCached();
	}

	@SubscribeEvent
	public void chunkLoad(ChunkEvent.Load event) {

		TomSaveData data = TomSaveData.forWorld(event.world);
		if(data.impact)
		{
			craterize(event.getChunk(), event.world, false);
		}
	}

	/**
	*	Generates the actual crater. You should ONLY set clientUpdate to true if updating loaded chunks.
	*/
	public static void craterize(Chunk chunk, World world, boolean clientUpdate) {
		TomSaveData data = TomSaveData.forWorld(world);

		int X = data.x;
		int Z = data.z;

		if(world.provider == null || world.provider.dimensionId != 0 || world.provider.terrainType == WorldType.FLAT)
			return;

		if(!ChunkCraterManager.proxy.getCraterGen(world, chunk.xPosition*16, chunk.zPosition*16))
		{
			//Chunk chunk = event.getChunk();
			ExtendedBlockStorage[] storageArray = chunk.getBlockStorageArray();
			for(int x = 0; x < 16; x++) {
				for(int z = 0; z < 16; z++) {
					int blockX = chunk.xPosition*16+x;
					int blockZ = chunk.zPosition*16+z;
					double dX = Math.pow((X - blockX), 2);
					double dZ = Math.pow((Z - blockZ), 2);
					double distance = MathHelper.sqrt_double(dX + dZ);
					int fill = (int) (Math.min(63, 420-(distance/5)))+ world.rand.nextInt(2);
					int breccia = (int) (-10+Math.pow(Math.E, (distance/414d)));
					int lens = (int) (32+Math.pow(Math.E, (distance/523d)));
					int outerRim = (int) (lens+(Math.pow(Math.E, -Math.pow(distance - 1500, 2)/18000)*50));
					int chicxulub = (int) (outerRim+(Math.pow(Math.E, -Math.pow(distance - 750, 2)/18000)*50))+ world.rand.nextInt(2);
					/*if(distance<=1730)
					{						
						WorldUtil.setBiome(world, blockX, blockZ, BiomeGenBaseQuackosian.crater);
						//ChunkCraterManager.proxy.setCraterGen(event.world, (event.chunkX*16)+8, (event.chunkZ*16)+8, true);
					}*/
					for (int y = 0; y < Math.max(chunk.getHeightValue(x, z), 127); ++y) 
					{
							if(distance<=3000)
							{
									ExtendedBlockStorage storage = storageArray[y>>4];	
									if((y)>chicxulub-3)
									{
										//int Y2 = Math.max(0,(int)Math.floor(y/16));
										if(storage == null && y<=64) {
											storage = storageArray[y>>4] = new ExtendedBlockStorage(y >> 4 << 4, !world.provider.hasNoSky);
										}
										if (storage != null) {
											int Y3 = (int)Math.floor(y%16);
											int Y4 = (int)Math.floor((y+1)%16);
											//Block block = storage.getBlockByExtId(x, (Y3), z);
											//if (block==Blocks.air)
											//{
											Block block = storage.getBlockByExtId(x, (Y3), z);
											Material m = block.getMaterial();
											if((m == Material.rock || m == Material.sand || m == Material.ground || m == Material.grass) && block!=ModBlocks.tektite && block!=ModBlocks.ore_tektite_osmiridium) {
											//	storage.func_150818_a(x, Y3, z, ModBlocks.ore_tektite_osmiridium);
											//} else {
												storage.func_150818_a(x, Y3, z, ModBlocks.sellafield_slaked);
												if(distance<=2000)
												{
													storage.setExtBlockMetadata(x, Y3, z, 10);
												}
												if(distance>2000 && distance<=3000)
												{
													int meta = (int) Math.floor(10-((distance-2000)/100));
													storage.setExtBlockMetadata(x, Y3, z, meta);
												}
											}
											if(y>chicxulub)
											{
												storage.func_150818_a(x, (Y3), z, Blocks.air);
												storage.setExtSkylightValue(x, (Y3), z, 15);
												if(y<56 && distance<=1500)
												{
													storage.func_150818_a(x, (Y3), z, Blocks.lava);	
													storage.setExtBlocklightValue(x, (Y3), z, 15);
												}
											}

												/*if(y>=chicxulub-3)
												{
													Block block = storage.getBlockByExtId(x, (Y3), z);
													Material m = block.getMaterial();
													if((m == Material.rock || m == Material.sand || m == Material.ground || m == Material.grass) && block!=ModBlocks.tektite && block!=ModBlocks.ore_tektite_osmiridium) {
													//	storage.func_150818_a(x, Y3, z, ModBlocks.ore_tektite_osmiridium);
													//} else {
														storage.func_150818_a(x, Y3, z, ModBlocks.sellafield_slaked);
													}
												}*/
												if(clientUpdate)
													world.markBlockForUpdate(blockX, y, blockZ);
											//}
										}
									}
									if(y<=chicxulub && y>breccia)
									{
										if(storage == null) {
											storage = storageArray[y>>4] = new ExtendedBlockStorage(y >> 4 << 4, !world.provider.hasNoSky);
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
											if(clientUpdate)
												world.markBlockForUpdate(blockX, y, blockZ);
										}
									}
									int surface = world.getTopSolidOrLiquidBlock(blockX, blockZ);
									if((y>= surface && y<=fill) && distance>=1500)
									{
										if(storage == null) {
											storage = storageArray[y>>4] = new ExtendedBlockStorage(y >> 4 << 4, !world.provider.hasNoSky);
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
											if(clientUpdate)
												world.markBlockForUpdate(blockX, y, blockZ);
										}
									}
									if((y<= world.getHeightValue(blockX, blockZ) && y>=surface-16) && distance>=1600 && distance<=3000)
									{
										if(storage == null) {
											storage = storageArray[y>>4] = new ExtendedBlockStorage(y >> 4 << 4, !world.provider.hasNoSky);
										}
										if (storage != null) {
											int Y3 = (int)Math.floor(y%16);
											Block block = storage.getBlockByExtId(x, (Y3), z);
											Material m = block.getMaterial();
											if(block instanceof BlockLeaves || block instanceof BlockBush || block instanceof BlockLog) {
												storage.func_150818_a(x, Y3, z, Blocks.air);
												storage.setExtSkylightValue(x, (Y3), z, 15);
											}
											if(clientUpdate)
												world.markBlockForUpdate(blockX, y, blockZ);
										}
									}
									if((y<= world.getHeightValue(blockX, blockZ) && y>=surface-3) && distance>=1600 && distance<=3000)
									{
										if(storage == null) {
											storage = storageArray[y>>4] = new ExtendedBlockStorage(y >> 4 << 4, !world.provider.hasNoSky);
										}
										if (storage != null) {
											int Y3 = (int)Math.floor(y%16);
											Block block = storage.getBlockByExtId(x, (Y3), z);
											Material m = block.getMaterial();
											if((m == Material.rock || m == Material.sand || m == Material.ground || m == Material.grass) && block!=ModBlocks.tektite && block!=ModBlocks.ore_tektite_osmiridium) {
											//	storage.func_150818_a(x, Y3, z, ModBlocks.ore_tektite_osmiridium);
											//} else {
												storage.func_150818_a(x, Y3, z, ModBlocks.sellafield_slaked);
												if(distance<=2000)
												{
													storage.setExtBlockMetadata(x, Y3, z, 10);
												}
												if(distance>2000 && distance<=3000)
												{
													int meta = (int) Math.floor(10-((distance-2000)/100));
													storage.setExtBlockMetadata(x, Y3, z, meta);
												}
											}
											if(clientUpdate)
												world.markBlockForUpdate(blockX, y, blockZ);
										}
									}
									ChunkCraterManager.proxy.setCraterGen(world, (chunk.xPosition*16)+8, (chunk.zPosition*16)+8, true);
								}						
							}
						}
					//}
				//}	
			}
		}
	}
}
