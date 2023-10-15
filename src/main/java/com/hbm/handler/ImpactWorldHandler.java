package com.hbm.handler;

import java.util.HashMap;
import java.util.List;

import com.hbm.blocks.ModBlocks;
import com.hbm.handler.crater.ChunkCraterManager;
import com.hbm.saveddata.TomSaveData;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;

import net.minecraft.block.BlockBush;
import net.minecraft.block.BlockLeaves;
import net.minecraft.block.BlockVine;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.MathHelper;
import net.minecraft.world.ChunkCoordIntPair;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.ChunkProviderServer;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.event.world.ChunkDataEvent;
import net.minecraftforge.event.world.ChunkEvent;
import net.minecraftforge.event.world.WorldEvent;

public class ImpactWorldHandler {
	
	public static void impactEffects(World world) {

		if(!(world instanceof WorldServer))
			return;

		if(world.provider.dimensionId != 0) {
			return;
		}

		WorldServer serv = (WorldServer) world;

		List<Chunk> list = serv.theChunkProviderServer.loadedChunks;
		int listSize = list.size();
		
		if(listSize > 0) {
			for(int i = 0; i < 12; i++) {
				
				Chunk chunk = list.get(serv.rand.nextInt(listSize));
				ChunkCoordIntPair coord = chunk.getChunkCoordIntPair();
				
				for(int x = 0; x < 16; x++) {
					for(int z = 0; z < 16; z++) {
						
						int X = coord.getCenterXPos() - 8 + x;
						int Z = coord.getCenterZPosition() - 8 + z;
						int Y = world.getHeightValue(X, Z) - world.rand.nextInt(Math.max(1, world.getHeightValue(X, Z)));

						TomSaveData data = TomSaveData.forWorld(world);
						
						if(data.dust > 0) {
							die(world, X, Y, Z);
						}
						if(data.fire > 0) {
							burn(world, X, Y, Z);
						}
					}
				}
			}
		}
	}

	/// Plants die without sufficient light.
	public static void die(World world, int x, int y, int z) {

		TomSaveData data = TomSaveData.forWorld(world);
		int light = Math.max(world.getSavedLightValue(EnumSkyBlock.Block, x, y + 1, z), (int) (world.getBlockLightValue(x, y + 1, z) * (1 - data.dust)));
		
		if(light < 4) {
			if(world.getBlock(x, y, z) == Blocks.grass) {
				world.setBlock(x, y, z, ModBlocks.waste_earth);
			} else if(world.getBlock(x, y, z) instanceof BlockBush) {
				world.setBlock(x, y, z, Blocks.air);
			} else if(world.getBlock(x, y, z) instanceof BlockLeaves) {
				world.setBlock(x, y, z, ModBlocks.waste_leaves);
			} else if(world.getBlock(x, y, z) instanceof BlockVine) {
				world.setBlock(x, y, z, Blocks.air);
			}
		}
	}

	/// Burn the world.
	public static void burn(World world, int x, int y, int z) {
		
		Block b = world.getBlock(x, y, z);
		if(b.isFlammable(world, x, y, z, ForgeDirection.UP) && world.getBlock(x, y + 1, z) == Blocks.air && world.getSavedLightValue(EnumSkyBlock.Sky, x, y + 1, z) >= 7) {
			if(b instanceof BlockLeaves || b instanceof BlockBush) {
				world.setBlockToAir(x, y, z);
			}
			world.setBlock(x, y + 1, z, Blocks.fire);
			
		} else if((b == Blocks.grass || b == Blocks.mycelium || b == ModBlocks.waste_earth || b == ModBlocks.frozen_grass || b == ModBlocks.waste_mycelium) &&
				!world.canLightningStrikeAt(x, y, z) && world.getSavedLightValue(EnumSkyBlock.Sky, x, y + 1, z) >= 7) {
			world.setBlock(x, y, z, ModBlocks.burning_earth);
			
		} else if(b == ModBlocks.frozen_dirt && world.getSavedLightValue(EnumSkyBlock.Sky, x, y + 1, z) >= 7) {
			world.setBlock(x, y, z, Blocks.dirt);
		}
	}

	public static World lastSyncWorld = null;
	public static float fire = 0F;
	public static float dust = 0F;
	public static long time = 0;
	public static int x = 0;
	public static int z = 0;
	public static boolean impact = false;

	@SideOnly(Side.CLIENT)
	public static float getFireForClient(World world) {
		if(world != lastSyncWorld) return 0F;
		return fire;
	}

	@SideOnly(Side.CLIENT)
	public static float getDustForClient(World world) {
		if(world != lastSyncWorld) return 0F;
		return dust;
	}

	@SideOnly(Side.CLIENT)
	public static boolean getImpactForClient(World world) {
		if(world != lastSyncWorld) return false;
		return impact;
	}
	
	@SideOnly(Side.CLIENT)
	public static long getTimeForClient(World world) {
		if(world != lastSyncWorld) return 0;
		return time;
	}
	
	@SideOnly(Side.CLIENT)
	public static int getXForClient(World world) {
		if(world != lastSyncWorld) return 0;
		return x;
	}
	
	@SideOnly(Side.CLIENT)
	public static int getZForClient(World world) {
		if(world != lastSyncWorld) return 0;
		return z;
	}
	
	public static void regenerateChunk(World world, Chunk ochunk) {
		//Chunk ochunk = world.getChunkFromChunkCoords(x, z);
        if (!world.isRemote)
        {
            try
            {			            	
				
				 if (world instanceof WorldServer)
	             {
					 	WorldServer ws = (WorldServer)world;
	                    ChunkProviderServer cps = ws.theChunkProviderServer;
	                    IChunkProvider chunkProviderGenerate = cps.currentChunkProvider;
	                    Chunk nc = chunkProviderGenerate.provideChunk(ochunk.xPosition, ochunk.zPosition);
	                    
	                    for (int X = 0; X < 16; X++)
	                    {
	                        for (int Z = 0; Z < 16; Z++)
	                        {
	                            for (int y = 0; y < world.getHeight(); y++)
	                            {
	                                Block block = nc.getBlock(X, y, Z);
	                                int metadata = nc.getBlockMetadata(X, y, Z);

	                                ws.setBlock(X + ochunk.xPosition * 16, y, Z + ochunk.zPosition * 16, block, metadata, 2);

	                                TileEntity tileEntity = nc.getTileEntityUnsafe(X, y, Z);

	                                if (tileEntity != null)
	                                {
	                                    ws.setTileEntity(X + ochunk.xPosition * 16, y, Z + ochunk.zPosition * 16, tileEntity);
	                                }
	                            }
	                        }
	                    }
	                    ochunk.isTerrainPopulated = false;
	                    chunkProviderGenerate.populate(chunkProviderGenerate, ochunk.xPosition, ochunk.zPosition);
	                    //nc.setChunkModified();
	             }
            }
            catch (Exception e)
            {
                System.out.println("ICBM Rejuvenation Failed!");
                e.printStackTrace();
            }
        }
        ochunk.setChunkModified();
	}
}