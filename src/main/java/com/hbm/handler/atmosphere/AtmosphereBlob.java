package com.hbm.handler.atmosphere;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Set;
import java.util.Stack;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.hbm.blocks.BlockDummyable;
import com.hbm.config.GeneralConfig;
import com.hbm.dim.trait.CBT_Atmosphere;
import com.hbm.entity.effect.EntityDepress;
import com.hbm.handler.ThreeInts;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.main.MainRegistry;
import com.hbm.util.AdjacencyGraph;

import net.minecraft.block.Block;
import net.minecraft.block.BlockFarmland;
import net.minecraft.block.BlockFence;
import net.minecraft.block.IGrowable;
import net.minecraft.block.material.Material;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.util.ForgeDirection;

public class AtmosphereBlob implements Runnable {

	/**
	 * Somewhat based on the Advanced-Rocketry implementation, but extended to
	 * define the gases and gas pressure inside the enclosed volume
	 */

	// Graph containing the enclosed area
	protected final AdjacencyGraph<ThreeInts> graph;

	// Handler, provides atmosphere information and receives callbacks
	protected IAtmosphereProvider handler;


	private static ThreadPoolExecutor pool = new ThreadPoolExecutor(2, 16, 60, TimeUnit.SECONDS, new LinkedBlockingQueue<>(32));

	private static HashMap<Block, Boolean> fullBounds = new HashMap<Block, Boolean>();


	private boolean executing;
	private ThreeInts blockPos;

	// If true, run depressurization effects on blobbing failure
	public boolean runDepress;
	public ForgeDirection depressDir = ForgeDirection.UP;

	private LinkedHashMap<ThreeInts, Integer> plants = new LinkedHashMap<>();

	public AtmosphereBlob(IAtmosphereProvider handler) {
		this.handler = handler;
		graph = new AdjacencyGraph<ThreeInts>();
	}

	public boolean isPositionAllowed(World world, ThreeInts pos) {
		return !isBlockSealed(world, pos);
	}

	public static boolean isBlockSealed(World world, ThreeInts pos) {
		return isBlockSealed(world, pos.x, pos.y, pos.z);
	}

	public static boolean isBlockSealed(World world, int x, int y, int z) {
		if(y < 0 || y > 256) return false;

		// Prevent loading new chunks, or we violate thread safety!
		if(world instanceof WorldServer && !((WorldServer) world).getChunkProvider().chunkExists(x >> 4, z >> 4))
			return true;

		Block block = world.getBlock(x, y, z);

		if(block.isAir(world, x, y, z)) return false; // Air obviously doesn't seal
		if(block instanceof BlockFarmland) return false;
		if(block instanceof BlockFence) return false;
		if(block instanceof IBlockSealable) { // Custom semi-sealables, like doors
			return ((IBlockSealable)block).isSealed(world, x, y, z);
		}
		if(block instanceof BlockDummyable) return false; // Machines can't seal, almost all have gaps

		Material material = block.getMaterial();
		if(material.isLiquid() || !material.isSolid()) return false; // Liquids need to know what pressurized atmosphere they're in to determine evaporation
		if(material == Material.leaves) return false; // Leaves never block air

		Boolean isFull = fullBounds.get(block);
		if(isFull != null) return isFull;

		AxisAlignedBB bb = null;
		try {
			// In the most common case, blocks don't use the passed World, only when they need to check neighbours to modify their bounding box
			// we just want to check that it attempts to change its bounding box at all, resulting in null if it does so by checking neighbours
			// anything that modifies its own bounding box safely (like half slabs) will still work as normal
			bb = block.getCollisionBoundingBoxFromPool(null, x, y, z);
		} catch(Exception ex) {}

		if(bb == null) {
			fullBounds.put(block, false);
			return false; // No collision, can't seal (like lamps)
		}

		// size * 100 to correct rounding errors
		int minX = (int) ((bb.minX - x) * 100);
		int minY = (int) ((bb.minY - y) * 100);
		int minZ = (int) ((bb.minZ - z) * 100);
		int maxX = (int) ((bb.maxX - x) * 100);
		int maxY = (int) ((bb.maxY - y) * 100);
		int maxZ = (int) ((bb.maxZ - z) * 100);

		isFull = minX == 0 && minY == 0 && minZ == 0 && maxX == 100 && maxY == 100 && maxZ == 100;

		fullBounds.put(block, isFull);

		return isFull;
	}

	public int getBlobMaxRadius() {
		return handler.getMaxBlobRadius();
	}

	public boolean hasFluid(FluidType fluid) {
		return hasFluid(fluid, 0.001);
	}

	public boolean hasFluid(FluidType fluid, double abovePressure) {
		if(handler.getFluidType() != fluid) return false;
		return handler.getFluidPressure() >= abovePressure;
	}

	public void consume(int amount) {
		handler.consume(amount);
	}

	public void produce(int amount) {
		handler.produce(amount);
	}

	/**
	 * Adds a block position to the blob
	 */
	public void addBlock(int x, int y , int z) {
		addBlock(new ThreeInts(x, y, z));
	}

	/**
	 * Recursively checks for contiguous blocks and adds them to the graph
	 */
	public void addBlock(ThreeInts blockPos) {
		if(!this.contains(blockPos) &&
				(this.graph.size() == 0 || this.contains(blockPos.getPositionAtOffset(ForgeDirection.UP)) || this.contains(blockPos.getPositionAtOffset(ForgeDirection.DOWN)) ||
						this.contains(blockPos.getPositionAtOffset(ForgeDirection.EAST)) || this.contains(blockPos.getPositionAtOffset(ForgeDirection.WEST)) ||
						this.contains(blockPos.getPositionAtOffset(ForgeDirection.NORTH)) || this.contains(blockPos.getPositionAtOffset(ForgeDirection.SOUTH)))) {
			if(!executing) {
				this.blockPos = blockPos;
				executing = true;

				if(GeneralConfig.enableThreadedAtmospheres) {
					try {
						pool.execute(this);
					} catch (RejectedExecutionException e) {
						MainRegistry.logger.warn("Atmosphere calculation at " + this.getRootPosition() + " aborted due to oversize queue!");
					}
				} else {
					this.run();
				}
			}
		}
	}

	private void addSingleBlock(ThreeInts blockPos) {
		if(!graph.contains(blockPos)) {
			graph.add(blockPos, getPositionsToAdd(blockPos));
			if(handler.getWorld().getBlock(blockPos.x, blockPos.y, blockPos.z) instanceof IGrowable) {
				addPlant(handler.getWorld(), blockPos.x, blockPos.y, blockPos.z);
			}
		}
	}

	/**
	 * @return the BlockPosition of the root of the blob
	 */
	public ThreeInts getRootPosition() {
		return handler.getRootPosition();
	}

	/**
	 * Gets adjacent blocks if they exist in the blob
	 * @param blockPos block to find things adjacent to
	 * @return list containing valid adjacent blocks
	 */
	protected HashSet<ThreeInts> getPositionsToAdd(ThreeInts blockPos) {
		HashSet<ThreeInts> set = new HashSet<>();

		for(ForgeDirection direction : ForgeDirection.VALID_DIRECTIONS) {

			ThreeInts offset = blockPos.getPositionAtOffset(direction);
			if(graph.contains(offset))
				set.add(offset);
		}

		return set;
	}

	/**
	 * Given a block position returns whether or not it exists in the graph
	 * @return true if the block exists in the blob
	 */
	public boolean contains(ThreeInts position) {
		boolean contains;

		synchronized (graph) {
			contains = graph.contains(position);
		}

		return contains;
	}

	/**
	 * Given a block position returns whether or not it exists in the graph
	 * @param x
	 * @param y
	 * @param z
	 * @return true if the block exists in the blob
	 */
	public boolean contains(int x, int y, int z) {
		return contains(new ThreeInts(x, y, z));
	}

	/**
	 * Removes the block at the given coords for this blob
	 * @param blockPos
	 */
	public void removeBlock(ThreeInts blockPos) {
		synchronized (graph) {
			graph.remove(blockPos);

			for(ForgeDirection direction : ForgeDirection.VALID_DIRECTIONS) {

				ThreeInts newBlock = blockPos.getPositionAtOffset(direction);
				if(graph.contains(newBlock) && !graph.doesPathExist(newBlock, handler.getRootPosition()))
					runEffectOnWorldBlocks(handler.getWorld(), graph.removeAllNodesConnectedTo(newBlock));
			}
		}
	}

	/**
	 * Removes all nodes from the blob
	 */
	public void clearBlob() {
		World world = handler.getWorld();

		runEffectOnWorldBlocks(world, getLocations());

		graph.clear();
	}

	/**
	 * @return a set containing all locations
	 */
	public Set<ThreeInts> getLocations() {
		return graph.getKeys();
	}

	/**
	 * @return the number of elements in the blob
	 */
	public int getBlobSize() {
		return graph.size();
	}

	@Override
	public void run() {
		Stack<ThreeInts> stack = new Stack<>();
		stack.push(blockPos);

		final int maxSize = this.getBlobMaxRadius();
		final HashSet<ThreeInts> addableBlocks = new HashSet<>();

		// Breadth first search; non recursive
		while(!stack.isEmpty()) {
			ThreeInts stackElement = stack.pop();
			addableBlocks.add(stackElement);

			for(ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS) {
				ThreeInts searchNextPosition = stackElement.getPositionAtOffset(dir);

				// Don't path areas we have already scanned
				if(!graph.contains(searchNextPosition) && !addableBlocks.contains(searchNextPosition)) {

					try {
						if(isPositionAllowed(handler.getWorld(), searchNextPosition)) {
							if(searchNextPosition.getDistanceSquared(this.getRootPosition()) <= maxSize * maxSize) {
								stack.push(searchNextPosition);
								addableBlocks.add(searchNextPosition);
							} else {
								// BANG HISSS
								if(runDepress) decompress(blockPos, depressDir);

								// Failed to seal, void
								clearBlob();
								executing = false;
								return;
							}
						}
					} catch (Exception e){
						// Catches errors with additional information
						MainRegistry.logger.info("Error: AtmosphereBlob has failed to form correctly due to an error. \nCurrentBlock: " + stackElement + "\tNextPos: " + searchNextPosition + "\tDir: " + dir + "\tStackSize: " + stack.size());
						e.printStackTrace();

						// Failed to seal, void
						clearBlob();
						executing = false;
						return;
					}
				}
			}
		}

		//only one instance can editing this at a time because this will not run again b/c "worker" is not null
		synchronized (graph) {
			for(ThreeInts addableBlock : addableBlocks) {
				addSingleBlock(addableBlock);
			}

			handler.onBlobCreated(this);
		}

		executing = false;
	}


	/**
	 * @param world
	 * @param blocks Collection containing affected locations
	 */
	protected void runEffectOnWorldBlocks(World world, Collection<ThreeInts> blocks) {
		ThreeInts root = handler.getRootPosition();
		CBT_Atmosphere newAtmosphere = ChunkAtmosphereManager.proxy.getAtmosphere(world, root.x, root.y, root.z, this);

		for(ThreeInts pos : blocks) {
			final Block block = world.getBlock(pos.x, pos.y, pos.z);
			ChunkAtmosphereManager.proxy.runEffectsOnBlock(newAtmosphere, world, block, pos.x, pos.y, pos.z);
		}
	}

	public void decompress(ThreeInts pos, ForgeDirection dir) {
		World world = handler.getWorld();

		EntityDepress depress = new EntityDepress(world, dir.getOpposite(), 20);
		depress.posX = pos.x + 0.5;
		depress.posY = pos.y + 0.5;
		depress.posZ = pos.z + 0.5;
		world.spawnEntityInWorld(depress);

		world.playSoundEffect(depress.posX, depress.posY, depress.posZ, "random.explode", 1.0F, 1.6F);
		world.playSoundEffect(depress.posX, depress.posY, depress.posZ, "random.fizz", 1.0F, 0.25F);
	}

	public void checkGrowth() {
		World world = handler.getWorld();

		Iterator<HashMap.Entry<ThreeInts, Integer>> iterator = plants.entrySet().iterator();
		while(iterator.hasNext()) {
			HashMap.Entry<ThreeInts, Integer> entry = iterator.next();
			ThreeInts pos = entry.getKey();
			int oldMeta = entry.getValue();
			Block block = world.getBlock(pos.x, pos.y, pos.z);

			if(!(block instanceof IGrowable)) {
				iterator.remove();
				continue;
			}

			int newMeta = world.getBlockMetadata(pos.x, pos.y, pos.z);

			if(newMeta != oldMeta) {
				entry.setValue(newMeta);
				produce(Math.max(newMeta - oldMeta, 0) * ChunkAtmosphereHandler.CROP_GROWTH_CONVERSION);
			}
		}
	}

	public void addPlant(World world, int x, int y, int z) {
		plants.put(new ThreeInts(x, y, z), world.getBlockMetadata(x, y, z));
	}

}
