package com.hbm.saveddata.satellites;

import com.hbm.inventory.RecipesCommon.ComparableStack;
import com.hbm.itempool.ItemPool;
import com.hbm.itempool.ItemPoolsSatellite;
import com.hbm.items.ModItems;
import com.hbm.util.WeightedRandomObject;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.IChatComponent;
import net.minecraft.util.WeightedRandomChestContent;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;

import java.util.HashMap;

public class SatelliteMiner extends SatelliteBase {
	
	// maps satellite types to item pool names
	private static final HashMap<Class<? extends SatelliteMiner>, String> CARGO = new HashMap<>();
	
	public double progress;
	public static final double SPEED = 1D / (15 * 60 * 20); // 15 minutes
	private long lastUpdateTick = -1L;

	public SatelliteMiner() { }

	@Override public String getType() { return "ASTEROID_MINER"; }
	
	@Override
	public IChatComponent[] getInfo(World world) {
		
		int progress = (int) Math.round(this.progress * 100);
		
		return new IChatComponent[] {
				new ChatComponentTranslation(ModItems.sat_miner.getUnlocalizedName(new ItemStack(ModItems.sat_miner)) + ".name"),
				new ChatComponentTranslation("satellite.minerprogress", progress + "%")
		};
	}

	@Override
	public void onUpdateTick(World world) {
		updateProgress(world);
	}

	@Override
	public void onOrbit(World world, double x, double y, double z) {
		super.onOrbit(world, x, y, z);
		this.lastUpdateTick = getMiningTime(world);
	}

	@Override
	public boolean tryRequestItems(World world, int x, int y, int z) {
		updateProgress(world);
		// Persist catch-up progress even if the resource dimension unloads before its next tick.
		this.markDirty();
		return super.tryRequestItems(world, x, y, z);
	}

	private static long getMiningTime(World world) {
		World overworld = DimensionManager.getWorld(0);
		return overworld != null ? overworld.getTotalWorldTime() : world.getTotalWorldTime();
	}

	private void updateProgress(World world) {
		// Use a shared clock so unloaded mining dimensions catch up without counting a tick twice.
		long time = getMiningTime(world);
		long elapsed = this.lastUpdateTick < 0L ? 1L : Math.max(0L, time - this.lastUpdateTick);
		this.lastUpdateTick = time;
		
		if(this.requestableSlots.length <= 0) {
			this.progress += elapsed * SPEED;
			
			if(this.progress >= 1D) {
				this.progress = 0D;
				
				WeightedRandomChestContent[] pool = ItemPool.getPool(getCargo());
				
				int itemAmount = 10 + world.rand.nextInt(6); // 10-15
				this.requestableSlots = new ItemStack[itemAmount];
				
				for(int i = 0; i < itemAmount; i++) {
					this.requestableSlots[i] = ItemPool.getStack(pool, world.rand);
				}
				
				this.markDirty();
			}
			
			if(time % 1200 == 0) this.markDirty(); // force a save every minute
		}
	}

	@Override
	public void writeToNBT(NBTTagCompound nbt) {
		super.writeToNBT(nbt);
		nbt.setDouble("progress", progress);
		nbt.setLong("lastUpdateTick", lastUpdateTick);
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);
		this.progress = nbt.getDouble("progress");
		this.lastUpdateTick = nbt.hasKey("lastUpdateTick") ? nbt.getLong("lastUpdateTick") : -1L;
	}

	/**
	 * Replaces cargo of the satellite.
	 * @param cargo - Array of {@link WeightedRandomObject} representing the loot that will be delivered.
	 */
	public static void registerCargo(Class<? extends SatelliteMiner> minerSatelliteClass, String cargo) {
		CARGO.put(minerSatelliteClass, cargo);
	}

	/**
	 * Gets items the satellite can deliver.
	 * @return - Array of {@link WeightedRandomObject} of satellite loot.
	 */
	public String getCargo() {
		return CARGO.get(getClass());
	}

	/**
	 * Gets the cargo key for the satellite item. If the item is not a miner satellite null is returned.
	 * @param satelliteItem - Satellite item
	 * @return - Returns {@link com.hbm.itempool.ItemPool} key or null if the item is not a mining satellite.
	 */
	public static String getCargoForItem(ComparableStack satelliteItem) {
		Class<? extends SatelliteBase> satelliteClass = XSatelliteRegistry.itemToClass.getOrDefault(satelliteItem, null);
		return satelliteClass != null ? CARGO.getOrDefault(satelliteClass, null) : null;
	}

	static {
		registerCargo(SatelliteMiner.class, ItemPoolsSatellite.POOL_SAT_MINER);
	}
}
