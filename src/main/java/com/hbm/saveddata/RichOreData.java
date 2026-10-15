package com.hbm.saveddata;

import java.util.HashMap;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.World;
import net.minecraft.world.WorldSavedData;

// exact remaining units for touched rich ore blocks; untouched blocks read as the configured full value
public class RichOreData extends WorldSavedData {

	public static final String KEY = "richOreData";

	private final HashMap<RichPos, Integer> units = new HashMap<>();

	public static class RichPos {
		public final int x, y, z;

		public RichPos(int x, int y, int z) {
			this.x = x;
			this.y = y;
			this.z = z;
		}

		@Override
		public boolean equals(Object o) {
			if(!(o instanceof RichPos)) return false;
			RichPos p = (RichPos) o;
			return p.x == x && p.y == y && p.z == z;
		}

		@Override
		public int hashCode() {
			return (x * 31 + y) * 31 + z;
		}
	}

	public static RichOreData forWorld(World world) {
		RichOreData result = (RichOreData) world.perWorldStorage.loadData(RichOreData.class, KEY);

		if(result == null) {
			world.perWorldStorage.setData(KEY, new RichOreData(KEY));
			result = (RichOreData) world.perWorldStorage.loadData(RichOreData.class, KEY);
		}

		return result;
	}

	public RichOreData(String tagName) {
		super(tagName);
	}

	public Integer get(int x, int y, int z) {
		return units.get(new RichPos(x, y, z));
	}

	public void set(int x, int y, int z, int count) {
		units.put(new RichPos(x, y, z), count);
		this.markDirty();
	}

	public void remove(int x, int y, int z) {
		if(units.remove(new RichPos(x, y, z)) != null) this.markDirty();
	}

	@Override
	public void readFromNBT(NBTTagCompound compound) {
		units.clear();
		NBTTagList list = compound.getTagList("units", 10);

		for(int i = 0; i < list.tagCount(); i++) {
			NBTTagCompound entry = list.getCompoundTagAt(i);
			if(entry.getInteger("u") <= 0) continue;
			int y = entry.getInteger("y");
			if(y < 0 || y > 255) continue;
			units.put(new RichPos(entry.getInteger("x"), y, entry.getInteger("z")), entry.getInteger("u"));
		}
	}

	@Override
	public void writeToNBT(NBTTagCompound nbt) {
		NBTTagList list = new NBTTagList();

		for(HashMap.Entry<RichPos, Integer> entry : units.entrySet()) {
			NBTTagCompound tag = new NBTTagCompound();
			tag.setInteger("x", entry.getKey().x);
			tag.setInteger("y", entry.getKey().y);
			tag.setInteger("z", entry.getKey().z);
			tag.setInteger("u", entry.getValue());
			list.appendTag(tag);
		}

		nbt.setTag("units", list);
	}
}
