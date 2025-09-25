package com.hbm.tileentity.machine;

import java.util.List;

import com.hbm.blocks.BlockCrop;
import com.hbm.blocks.BlockDummyable;
import com.hbm.blocks.ModBlocks;
import com.hbm.handler.atmosphere.ChunkAtmosphereHandler;
import com.hbm.inventory.container.ContainerHydroponic;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.tank.FluidTank;
import com.hbm.inventory.gui.GUIHydroponic;
import com.hbm.items.ModItems;
import com.hbm.lib.Library;
import com.hbm.tileentity.IGUIProvider;
import com.hbm.tileentity.TileEntityMachineBase;
import com.hbm.util.InventoryUtil;
import com.hbm.util.fauxpointtwelve.DirPos;

import api.hbm.energymk2.IEnergyReceiverMK2;
import api.hbm.fluidmk2.IFluidStandardTransceiverMK2;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;
import net.minecraft.block.Block;
import net.minecraft.block.BlockStem;
import net.minecraft.block.IGrowable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.World;
import net.minecraftforge.common.EnumPlantType;
import net.minecraftforge.common.IPlantable;
import net.minecraftforge.common.util.ForgeDirection;

public class TileEntityHydroponic extends TileEntityMachineBase implements IGUIProvider, IFluidStandardTransceiverMK2, IEnergyReceiverMK2 {

	public FluidTank[] tanks;
	public long power;
	public static long maxPower = 2_000;

	public int fertilizer;
	public static int maxFertilizer = 200;

	private boolean lightsOn = false;
	private int[] prevMeta = new int[3];

	public TileEntityHydroponic() {
		super(6);
		tanks = new FluidTank[2];
		tanks[0] = new FluidTank(Fluids.CARBONDIOXIDE, 16_000);
		tanks[1] = new FluidTank(Fluids.OXYGEN, 16_000);
	}

	@Override
	public Container provideContainer(int ID, EntityPlayer player, World world, int x, int y, int z) {
		return new ContainerHydroponic(player.inventory, this);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public Object provideGUI(int ID, EntityPlayer player, World world, int x, int y, int z) {
		return new GUIHydroponic(player.inventory, this);
	}

	@Override
	public String getName() {
		return "container.hydrobay";
	}

	@Override
	public void updateEntity() {
		if(!worldObj.isRemote) {
			ForgeDirection dir = ForgeDirection.getOrientation(this.getBlockMetadata() - BlockDummyable.offset);
			ForgeDirection rot = dir.getRotation(ForgeDirection.UP);

			for(DirPos pos : getFluidPos()) {
				trySubscribe(tanks[0].getTankType(), worldObj, pos);
				tryProvide(tanks[1].getTankType(), worldObj, pos);
			}

			for(DirPos pos : getPowerPos()) {
				trySubscribe(worldObj, pos);
			}

			if(power > 0) {
				power = Math.max(power - 25, 0);
			}

			if(slots[1] != null) {
				int strength = getFertilizerStrength(slots[1]);
				if(strength > 0 && fertilizer <= maxFertilizer - strength) {
					slots[1].stackSize--;
					fertilizer += strength;
					if(slots[1].stackSize <= 0) slots[1] = null;
					markDirty();
				}
			}

			power = Library.chargeTEFromItems(slots, 2, power, maxPower);

			BlockDummyable.safeRem = true;
			{

				int lMeta = worldObj.getBlockMetadata(xCoord - rot.offsetX, yCoord + 2, zCoord - rot.offsetZ);
				int rMeta = worldObj.getBlockMetadata(xCoord + rot.offsetX, yCoord + 2, zCoord + rot.offsetZ);

				if(power >= 200) {
					if(!lightsOn) {
						worldObj.setBlock(xCoord - rot.offsetX, yCoord + 2, zCoord - rot.offsetZ, ModBlocks.dummy_beam, lMeta, 3);
						worldObj.setBlock(xCoord + rot.offsetX, yCoord + 2, zCoord + rot.offsetZ, ModBlocks.dummy_beam, rMeta, 3);

						lightsOn = true;
					}
				} else {
					if(lightsOn) {
						worldObj.setBlock(xCoord - rot.offsetX, yCoord + 2, zCoord - rot.offsetZ, ModBlocks.hydrobay, lMeta, 3);
						worldObj.setBlock(xCoord + rot.offsetX, yCoord + 2, zCoord + rot.offsetZ, ModBlocks.hydrobay, rMeta, 3);

						lightsOn = false;
					}
				}

			}
			BlockDummyable.safeRem = false;

			for(int i = 0; i < 3; i++) {
				int x = xCoord + rot.offsetX * (i - 1);
				int y = yCoord + 1;
				int z = zCoord + rot.offsetZ * (i - 1);

				Block currentPlant = worldObj.getBlock(x, y, z);

				// Minimum CO2 pressure required to start growing
				if(power >= 200 && tanks[0].getFill() >= 100) {

					// Attempt planting a new plant
					// Only allows single block crops
					if(!(currentPlant instanceof IGrowable)) {
						if(slots[0] == null || !(slots[0].getItem() instanceof IPlantable)) continue;

						IPlantable plantable = (IPlantable) slots[0].getItem();
						if(plantable.getPlantType(worldObj, x, y, z) != EnumPlantType.Crop) continue;

						currentPlant = plantable.getPlant(worldObj, x, y, z);
						if(!(currentPlant instanceof IGrowable) || currentPlant instanceof BlockStem) continue;

						if(currentPlant instanceof BlockCrop && !((BlockCrop) currentPlant).canHydro) continue;

						worldObj.setBlock(x, y, z, currentPlant);
						prevMeta[i] = 0;

						slots[0].stackSize--;
						if(slots[0].stackSize <= 0) slots[0] = null;

						markDirty();
					}

					IGrowable currentGrowable = (IGrowable) currentPlant;

					// Increase growth speed by about x10
					if(worldObj.rand.nextInt(120) == 0) currentPlant.updateTick(worldObj, x, y, z, worldObj.rand);

					boolean fullyGrown = false;
					if(currentGrowable.func_149851_a(worldObj, x, y, z, worldObj.isRemote)) { // should consume bonemeal, if not, assume fully grown
						if(fertilizer > 0 && worldObj.rand.nextInt(60) == 0) {
							if(currentGrowable.func_149852_a(worldObj, worldObj.rand, x, y, z)) { // does bonemeal apply
								currentGrowable.func_149853_b(worldObj, worldObj.rand, x, y, z); // apply bonemeal
								worldObj.playAuxSFX(2005, x, y, z, 0);
							}

							// now consume the bonemeal
							fertilizer--;
						}
					} else {
						fullyGrown = true;
					}

					int newMeta = worldObj.getBlockMetadata(x, y, z);

					if(newMeta != prevMeta[i]) {
						// each growth stage sequesters 5mb of carbon
						int toProduce = Math.max(newMeta - prevMeta[i], 0) * ChunkAtmosphereHandler.CROP_GROWTH_CONVERSION;
						tanks[0].setFill(Math.max(tanks[0].getFill() - toProduce, 0));
						tanks[1].setFill(Math.min(tanks[1].getFill() + toProduce, tanks[1].getMaxFill()));

						prevMeta[i] = newMeta;
					}

					// after collecting produced O2, break any fully grown plants
					// unless there is no space to collect the drops
					if(fullyGrown && attemptHarvest(currentPlant.getDrops(worldObj, x, y, z, newMeta, 0))) {
						worldObj.setBlockToAir(x, y, z);
						markDirty();
					}
				} else if(currentPlant instanceof IGrowable) {
					// pause growth until sufficient CO2 added
					worldObj.setBlockMetadataWithNotify(x, y, z, prevMeta[i], 2);
				}
			}

			networkPackNT(15);
		} else {

		}
	}

	public ItemStack[] getValidFertilizers() {
		return new ItemStack[] {
			new ItemStack(Items.dye, 1, 15),
			new ItemStack(ModItems.powder_fertilizer),
		};
	}

	private int getFertilizerStrength(ItemStack stack) {
		if(stack == null) return 0;
		if(stack.getItem() == Items.dye && stack.getItemDamage() == 15) return 1;
		if(stack.getItem() == ModItems.powder_fertilizer) return 9;
		return 0;
	}

	private boolean attemptHarvest(List<ItemStack> drops) {
		ItemStack[] originals = new ItemStack[3];
		originals[0] = slots[3];
		originals[1] = slots[4];
		originals[2] = slots[5];

		for(ItemStack drop : drops) {
			if(InventoryUtil.tryAddItemToInventory(slots, 3, 5, drop) != null) {
				slots[3] = originals[0];
				slots[4] = originals[1];
				slots[5] = originals[2];
				return false;
			}
		}

		return true;
	}

	private DirPos[] getFluidPos() {
		ForgeDirection dir = ForgeDirection.getOrientation(this.getBlockMetadata() - BlockDummyable.offset);
		ForgeDirection rot = dir.getRotation(ForgeDirection.UP);

		return new DirPos[] {
			new DirPos(xCoord + rot.offsetX * 2 + dir.offsetX, yCoord, zCoord + rot.offsetZ * 2 + dir.offsetZ, dir),
			new DirPos(xCoord - rot.offsetX * 2 + dir.offsetX, yCoord, zCoord - rot.offsetZ * 2 + dir.offsetZ, dir),
			new DirPos(xCoord + rot.offsetX * 2 - dir.offsetX, yCoord, zCoord + rot.offsetZ * 2 - dir.offsetZ, dir.getOpposite()),
			new DirPos(xCoord - rot.offsetX * 2 - dir.offsetX, yCoord, zCoord - rot.offsetZ * 2 - dir.offsetZ, dir.getOpposite()),
		};
	}

	private DirPos[] getPowerPos() {
		return new DirPos[] {
			new DirPos(xCoord, yCoord + 3, zCoord, ForgeDirection.UP),
		};
	}

	@Override
	public void serialize(ByteBuf buf) {
		for(int i = 0; i < tanks.length; i++) tanks[i].serialize(buf);
		buf.writeLong(power);
		buf.writeInt(fertilizer);
	}

	@Override
	public void deserialize(ByteBuf buf) {
		for(int i = 0; i < tanks.length; i++) tanks[i].deserialize(buf);
		power = buf.readLong();
		fertilizer = buf.readInt();
	}

	@Override
	public void writeToNBT(NBTTagCompound nbt) {
		super.writeToNBT(nbt);
		for(int i = 0; i < tanks.length; i++) tanks[i].writeToNBT(nbt, "t" + i);
		for(int i = 0; i < 3; i++) nbt.setInteger("p" + i, prevMeta[i]);

		nbt.setLong("power", power);
		nbt.setBoolean("lights", lightsOn);
		nbt.setInteger("fertilizer", fertilizer);
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);
		for(int i = 0; i < tanks.length; i++) tanks[i].readFromNBT(nbt, "t" + i);
		for(int i = 0; i < 3; i++) prevMeta[i] = nbt.getInteger("p" + i);

		power = nbt.getLong("power");
		lightsOn = nbt.getBoolean("lights");
		fertilizer = nbt.getInteger("fertilizer");
	}

	@Override
	public boolean isItemValidForSlot(int slot, ItemStack itemStack) {
		if(slot == 0) return itemStack.getItem() instanceof IPlantable;
		if(slot == 1) return getFertilizerStrength(itemStack) > 0;
		return false;
	}

	@Override
	public int[] getAccessibleSlotsFromSide(int side) {
		return new int[] {0, 1, 2, 3, 4, 5};
	}

	@Override
	public boolean canExtractItem(int slot, ItemStack itemStack, int side) {
		return slot >= 3;
	}

	AxisAlignedBB bb = null;

	@Override
	public AxisAlignedBB getRenderBoundingBox() {
		if(bb == null) {
			bb = AxisAlignedBB.getBoundingBox(
				xCoord - 2,
				yCoord,
				zCoord - 2,
				xCoord + 3,
				yCoord + 2,
				zCoord + 3
			);
		}

		return bb;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public double getMaxRenderDistanceSquared() {
		return 65536.0D;
	}

	@Override public FluidTank[] getReceivingTanks() { return new FluidTank[] { tanks[0] }; }
	@Override public FluidTank[] getSendingTanks() { return new FluidTank[] { tanks[1] }; }
	@Override public FluidTank[] getAllTanks() { return tanks; }

	@Override public long getPower() { return power; }
	@Override public void setPower(long power) { this.power = power; }
	@Override public long getMaxPower() { return maxPower; }

}
