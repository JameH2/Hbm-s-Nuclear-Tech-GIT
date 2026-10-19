package com.hbm.tileentity.machine;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.hbm.entity.item.EntityMovingItem;
import com.hbm.inventory.RecipesCommon.ComparableStack;
import com.hbm.tileentity.TileEntityMachineBase;
import com.hbm.tileentity.machine.TileEntityMachineAutocrafter.InventoryCraftingAuto;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.AxisAlignedBB;


public class TileEntityConveyorCombinator extends TileEntityMachineBase {

	public static final int MODE_3x3 = 0;
	public static final int MODE_2x2 = 1;

	public int mode = MODE_3x3;

	public double hammer;
	public double renderHammer;
	public double lastHammer;
	private double syncHammer;
	private int turnProgress;

	private double phase;
	private double amplitude;

	private int activeTicks;

	public TileEntityConveyorCombinator() {
		super(0);
	}

	@Override
	public String getName() {
		return "container.conveyor_combinator";
	}

	@Override
	public void updateEntity() {

		if(!worldObj.isRemote) {

			boolean did = this.process();

			if(did) {
				this.activeTicks = 10;
				this.worldObj.playSoundEffect(this.xCoord, this.yCoord, this.zCoord, "hbm:block.pressOperate", this.getVolume(0.5F), 1.25F);
			}

			this.animateHammer();

			this.networkPackNT(50);

		} else {

			this.lastHammer = this.renderHammer;

			if(this.turnProgress > 0) {
				this.renderHammer = this.renderHammer + ((this.syncHammer - this.renderHammer) / (double) this.turnProgress);
				--this.turnProgress;
			} else {
				this.renderHammer = this.syncHammer;
			}
		}
	}

	protected void animateHammer() {

		if(this.activeTicks > 0) this.activeTicks--;

		double target = this.activeTicks > 0 ? 1D : 0D;
		this.amplitude += (target - this.amplitude) * 0.2D;

		this.phase += 0.08D;
		if(this.phase >= 1D) this.phase -= 1D;

		this.hammer = (0.5D - 0.5D * Math.cos(this.phase * Math.PI * 2D)) * this.amplitude;
	}

	public int getRequired() {
		return this.mode == MODE_2x2 ? 4 : 9;
	}

	public boolean process() {

		List<EntityMovingItem> items = worldObj.getEntitiesWithinAABB(EntityMovingItem.class, AxisAlignedBB.getBoundingBox(xCoord, yCoord, zCoord, xCoord + 1, yCoord + 0.5, zCoord + 1));

		if(items.isEmpty()) return false;

		HashMap<ComparableStack, List<EntityMovingItem>> groups = new HashMap();

		for(EntityMovingItem item : items) {
			ItemStack stack = item.getItemStack();
			if(stack == null || stack.stackSize <= 0) continue;

			if(this.isOwnOutput(item)) continue;

			ComparableStack key = new ComparableStack(stack).makeSingular();
			List<EntityMovingItem> list = groups.get(key);
			if(list == null) {
				list = new ArrayList();
				groups.put(key, list);
			}
			list.add(item);
		}

		int needed = this.getRequired();
		boolean did = false;

		for(Map.Entry<ComparableStack, List<EntityMovingItem>> entry : groups.entrySet()) {

			List<EntityMovingItem> list = entry.getValue();

			int total = 0;
			for(EntityMovingItem item : list) total += item.getItemStack().stackSize;

			if(total < needed) continue;

			ItemStack output = this.getCombined(list.get(0).getItemStack(), needed);
			if(output == null) continue;

			int sets = total / needed;
			int toConsume = sets * needed;

			for(EntityMovingItem item : list) {
				ItemStack stack = item.getItemStack();
				int take = Math.min(stack.stackSize, toConsume);
				toConsume -= take;

				if(take >= stack.stackSize) {
					item.setDead();
				} else {
					stack.stackSize -= take;
					item.setItemStack(stack);
				}

				if(toConsume <= 0) break;
			}

			this.spawnOutput(output, sets * output.stackSize);
			did = true;
		}

		return did;
	}

	public static final String TAG_OWNER = "hbmConveyorCombinator";

	public boolean isOwnOutput(EntityMovingItem item) {

		NBTTagCompound data = item.getEntityData();

		if(!data.hasKey(TAG_OWNER + "X")) return false;

		return data.getInteger(TAG_OWNER + "X") == this.xCoord
			&& data.getInteger(TAG_OWNER + "Y") == this.yCoord
			&& data.getInteger(TAG_OWNER + "Z") == this.zCoord;
	}

	protected void spawnOutput(ItemStack output, int amount) {

		int max = Math.max(1, output.getMaxStackSize());

		while(amount > 0) {
			int size = Math.min(amount, max);
			ItemStack stack = output.copy();
			stack.stackSize = size;

			EntityMovingItem item = new EntityMovingItem(worldObj);
			item.setPosition(this.xCoord + 0.5, this.yCoord + 0.25, this.zCoord + 0.5);
			item.setItemStack(stack);

			NBTTagCompound data = item.getEntityData();
			data.setInteger(TAG_OWNER + "X", this.xCoord);
			data.setInteger(TAG_OWNER + "Y", this.yCoord);
			data.setInteger(TAG_OWNER + "Z", this.zCoord);

			worldObj.spawnEntityInWorld(item);

			amount -= size;
		}
	}

	public static final HashMap<ComparableStack, ItemStack> from4Cache = new HashMap();
	public static final HashMap<ComparableStack, ItemStack> from9Cache = new HashMap();

	protected static final InventoryCraftingAuto craftingInventory = new InventoryCraftingAuto(3, 3);

	public ItemStack getCombined(ItemStack ingredient, int size) {

		ComparableStack singular = new ComparableStack(ingredient).makeSingular();
		HashMap<ComparableStack, ItemStack> cache = size >= 9 ? from9Cache : from4Cache;

		if(cache.containsKey(singular)) return cache.get(singular);

		craftingInventory.clear();

		if(size >= 9) {
			for(int i = 0; i < 9; i++) craftingInventory.setInventorySlotContents(i, ingredient.copy());
		} else {
			craftingInventory.setInventorySlotContents(0, ingredient.copy());
			craftingInventory.setInventorySlotContents(1, ingredient.copy());
			craftingInventory.setInventorySlotContents(3, ingredient.copy());
			craftingInventory.setInventorySlotContents(4, ingredient.copy());
		}

		ItemStack match = this.getMatch(craftingInventory);
		cache.put(singular, match != null ? match.copy() : null);
		return match;
	}

	public ItemStack getMatch(InventoryCrafting grid) {

		for(Object o : CraftingManager.getInstance().getRecipeList()) {
			IRecipe recipe = (IRecipe) o;

			if(recipe.matches(grid, worldObj)) {
				return recipe.getCraftingResult(grid);
			}
		}

		return null;
	}

	@Override
	public void serialize(ByteBuf buf) {
		super.serialize(buf);
		buf.writeInt(this.mode);
		buf.writeDouble(this.hammer);
	}

	@Override
	public void deserialize(ByteBuf buf) {
		super.deserialize(buf);
		this.mode = buf.readInt();
		this.syncHammer = buf.readDouble();
		this.turnProgress = 2;
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);
		this.mode = nbt.getInteger("mode");
	}

	@Override
	public void writeToNBT(NBTTagCompound nbt) {
		super.writeToNBT(nbt);
		nbt.setInteger("mode", mode);
	}

	AxisAlignedBB bb = null;

	@Override
	public AxisAlignedBB getRenderBoundingBox() {

		if(bb == null) {
			bb = AxisAlignedBB.getBoundingBox(
					xCoord - 1,
					yCoord,
					zCoord - 1,
					xCoord + 2,
					yCoord + 2,
					zCoord + 2
					);
		}

		return bb;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public double getMaxRenderDistanceSquared() {
		return 65536.0D;
	}
}
