package com.hbm.dim.dima;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;

import org.lwjgl.opengl.GL11;

import com.hbm.config.SpaceConfig;
import com.hbm.extprop.HbmLivingProps;
import com.hbm.extprop.HbmPlayerProps;
import com.hbm.util.fauxpointtwelve.BlockPos;
import com.hbm.wiaj.WorldInAJar;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent.ClientTickEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.world.ChunkCoordIntPair;
import net.minecraft.world.World;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.event.entity.EntityEvent.EnteringChunk;
import net.minecraftforge.event.entity.player.PlayerEvent.BreakSpeed;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.Action;
import net.minecraftforge.event.world.ChunkDataEvent;
import net.minecraftforge.event.world.ChunkEvent;
import net.minecraftforge.event.world.WorldEvent;

public class EventHandlerDima {

	private Map<BlockPos, BlockData> jitterBlocks = new HashMap<>();

	private static class BlockData {

		WorldInAJar jar;
		int volatility;

		public BlockData(Block block, int meta) {
			this.jar = new WorldInAJar(1, 1, 1);
			this.jar.setBlock(0, 0, 0, block, meta);

			this.volatility = 10;
		}

	}


	// Prevent breaking of blocks
	@SubscribeEvent
	public void onBreakSpeed(BreakSpeed event) {
		if(!isValidDimension(event.entity.worldObj)) return;
		if(event.entityPlayer.capabilities.isCreativeMode) return;

		if(hasPsychosis(event.entity.worldObj, event.x, event.z, event.entityPlayer)) {
			event.setCanceled(true);

			if(event.entity.worldObj.isRemote) {
				jitterBlocks.computeIfAbsent(new BlockPos(event.x, event.y, event.z), j -> new BlockData(event.block, event.metadata));
			}
		}
	}

	// Prevent placing of blocks
	@SubscribeEvent
	public void onPlayerInteract(PlayerInteractEvent event) {
		if(!isValidDimension(event.world)) return;
		if(event.entityPlayer.capabilities.isCreativeMode) return;

		ForgeDirection dir = ForgeDirection.getOrientation(event.face);
		int x = event.x + dir.offsetX;
		int y = event.y + dir.offsetY;
		int z = event.z + dir.offsetZ;

		if(hasPsychosis(event.world, x, z, event.entityPlayer)) {
			ItemStack held = event.entityPlayer.getHeldItem();

			if(event.action == Action.RIGHT_CLICK_BLOCK && held != null && held.getItem() instanceof ItemBlock) {
				event.setCanceled(true);

				if(event.entityPlayer.worldObj.isRemote) {
					ItemBlock heldItem = (ItemBlock) held.getItem();
					int meta = heldItem.getMetadata(held.getItemDamage());

					jitterBlocks.computeIfAbsent(new BlockPos(x, y, z), j -> new BlockData(heldItem.field_150939_a, meta));
				}
			}
		}
	}

	@SideOnly(Side.CLIENT)
	@SubscribeEvent
	public void onRenderWorldLastEvent(RenderWorldLastEvent event) {
		EntityPlayer player = Minecraft.getMinecraft().thePlayer;
		if(!isValidDimension(player.worldObj)) return;

		for(Entry<BlockPos, BlockData> jitter : jitterBlocks.entrySet()) {
			BlockPos pos = jitter.getKey();
			double volatility = jitter.getValue().volatility * 0.1D;
			WorldInAJar jar = jitter.getValue().jar;
			Random rand = player.worldObj.rand;

			RenderBlocks renderer = new RenderBlocks(jar);

			renderer.enableAO = true;

			RenderHelper.disableStandardItemLighting();
			GL11.glDisable(GL11.GL_BLEND);

			GL11.glPushMatrix();
			{

				double x = player.prevPosX + (player.posX - player.prevPosX) * event.partialTicks;
				double y = player.prevPosY + (player.posY - player.prevPosY) * event.partialTicks;
				double z = player.prevPosZ + (player.posZ - player.prevPosZ) * event.partialTicks;

				GL11.glTranslated(pos.getX() - x, pos.getY() - y, pos.getZ() - z);
				GL11.glTranslated((rand.nextDouble() - 0.5D) * volatility, (rand.nextDouble() - 0.5D) * volatility, (rand.nextDouble() - 0.5D) * volatility);

				Minecraft.getMinecraft().getTextureManager().bindTexture(TextureMap.locationBlocksTexture);
				GL11.glShadeModel(GL11.GL_SMOOTH);
				Tessellator.instance.startDrawingQuads();

				Tessellator.instance.disableColor();
				GL11.glColor3f(0.8F, 0, 0.1F);

				for(int ix = 0; ix < jar.sizeX; ix++) {
					for(int iy = 0; iy < jar.sizeY; iy++) {
						for(int iz = 0; iz < jar.sizeZ; iz++) {
							try { renderer.renderBlockByRenderType(jar.getBlock(ix, iy, iz), ix, iy, iz); } catch(Exception ex) { }
						}
					}
				}

				Tessellator.instance.draw();
				GL11.glShadeModel(GL11.GL_FLAT);

			}
			GL11.glPopMatrix();
		}
	}

	private Random rand = new Random();
	public static boolean tunedToADeadChannel;
	private int tuneTimer;

	@SideOnly(Side.CLIENT)
	@SubscribeEvent
	public void clientTick(ClientTickEvent event) {
		Iterator<BlockData> iterator = jitterBlocks.values().iterator();
		while(iterator.hasNext()) {
			BlockData jitter = iterator.next();
			jitter.volatility--;

			if(jitter.volatility <= 0) iterator.remove();
		}

		EntityPlayer player = Minecraft.getMinecraft().thePlayer;

		if(player != null && isValidDimension(player.worldObj)) {
			if(HbmLivingProps.getDigamma(player) > 0.01F) {
				tuneTimer--;
				if(tuneTimer <= 0) {
					tunedToADeadChannel = !tunedToADeadChannel;
					tuneTimer = tunedToADeadChannel ? rand.nextInt(20) : rand.nextInt(1200);
				}
			} else {
				tunedToADeadChannel = false;
			}
		}
	}


	private static Set<ChunkCoordIntPair> psychoChunks;
	private static final String NBT_KEY_PSYCHOSIS = "chunk_psychosis";

	public static void addPsychoChunk(World world, int x, int z) {
		if(world.isRemote || !isValidDimension(world)) return;
		psychoChunks.add(new ChunkCoordIntPair(x >> 4, z >> 4));
	}

	public static void removePsychoChunk(World world, int x, int z) {
		if(world.isRemote || !isValidDimension(world)) return;
		psychoChunks.remove(new ChunkCoordIntPair(x >> 4, z >> 4));
	}

	@SubscribeEvent
	public void onWorldLoad(WorldEvent.Load event) {
		if(event.world.isRemote || !isValidDimension(event.world)) return;
		psychoChunks = new HashSet<>();
	}

	@SubscribeEvent
	public void onWorldUnload(WorldEvent.Unload event) {
		if(event.world.isRemote || !isValidDimension(event.world)) return;
		psychoChunks = null;
	}

	@SubscribeEvent
	public void onChunkLoad(ChunkDataEvent.Load event) {
		if(event.world.isRemote || !isValidDimension(event.world) || psychoChunks == null) return;

		if(event.getData().getBoolean(NBT_KEY_PSYCHOSIS)) {
			psychoChunks.add(event.getChunk().getChunkCoordIntPair());
		}
	}

	@SubscribeEvent
	public void onChunkSave(ChunkDataEvent.Save event) {
		if(event.world.isRemote || !isValidDimension(event.world) || psychoChunks == null) return;

		if(psychoChunks.contains(event.getChunk().getChunkCoordIntPair())) {
			event.getData().setBoolean(NBT_KEY_PSYCHOSIS, true);
		} else {
			event.getData().removeTag(NBT_KEY_PSYCHOSIS);
		}
	}

	@SubscribeEvent
	public void onChunkUnload(ChunkEvent.Unload event) {
		if(event.world.isRemote || !isValidDimension(event.world) || psychoChunks == null) return;

		psychoChunks.remove(event.getChunk().getChunkCoordIntPair());
	}

	@SubscribeEvent
	public void enteringChunk(EnteringChunk event) {
		if(event.entity.worldObj.isRemote || !isValidDimension(event.entity.worldObj) || !(event.entity instanceof EntityPlayer)) return;

		HbmPlayerProps props = HbmPlayerProps.getData((EntityPlayer) event.entity);
		props.setMiningBlocked(psychoChunks.contains(new ChunkCoordIntPair(event.newChunkX, event.newChunkZ)));
	}


	// just in case something goes wrong, make sure it never affects normal gameplay
	// weird shit in the weird dimension is a-okay
	private static boolean isValidDimension(World world) {
		return world.provider.dimensionId == SpaceConfig.dimaDimension;
	}

	// Check both player props AND chunk data, so both clients and servers can find the correct value
	private static boolean hasPsychosis(World world, int x, int z, EntityPlayer player) {
		HbmPlayerProps props = HbmPlayerProps.getData(player);
		if(props.getMiningBlocked()) return true;

		if(psychoChunks == null) return false;

		return psychoChunks.contains(new ChunkCoordIntPair(x >> 4, z >> 4));
	}

}
