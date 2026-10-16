package com.hbm.tileentity.machine;

import java.util.HashMap;
import java.util.List;

import com.hbm.blocks.ModBlocks;
import com.hbm.dim.trait.CBT_Atmosphere;
import com.hbm.handler.atmosphere.ChunkAtmosphereManager;
import com.hbm.inventory.UpgradeManagerNT;
import com.hbm.inventory.RecipesCommon.AStack;
import com.hbm.inventory.container.ContainerVacuumCircuit;
import com.hbm.inventory.gui.GUIVacuumCircuit;
import com.hbm.inventory.recipes.VacuumCircuitRecipes;
import com.hbm.inventory.recipes.VacuumCircuitRecipes.VacuumCircuitRecipe;
import com.hbm.items.machine.ItemMachineUpgrade;
import com.hbm.items.machine.ItemMachineUpgrade.UpgradeType;
import com.hbm.lib.Library;
import com.hbm.module.portmanager.ModulePortManPower;
import com.hbm.packet.PacketDispatcher;
import com.hbm.packet.toclient.AuxParticlePacketNT;
import com.hbm.tileentity.IGUIProvider;
import com.hbm.tileentity.IUpgradeInfoProvider;
import com.hbm.tileentity.TileEntityMachineBase;
import com.hbm.tileentity.TilePort.PortDef;
import com.hbm.util.i18n.I18nUtil;

import api.hbm.energymk2.IEnergyReceiverMK2;
import cpw.mods.fml.common.network.NetworkRegistry.TargetPoint;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

public class TileEntityMachineVacuumCircuit extends TileEntityMachineBase implements IEnergyReceiverMK2, IGUIProvider, IUpgradeInfoProvider {

	public long power;
	public long maxPower = 2_000;
	public long consumption;

	public int progress;
	public int processTime = 1;

	private VacuumCircuitRecipe recipe;
	public ItemStack display;

	public boolean canOperate = true;

	public UpgradeManagerNT upgradeManager = new UpgradeManagerNT();

	protected ModulePortManPower modulePowerPorts;

	public TileEntityMachineVacuumCircuit() {
		super(8);

		modulePowerPorts = new ModulePortManPower(this);
	}


	@Override
	public String getName() {
		return "container.machineVacuumCircuit";
	}

	@Override
	public void setInventorySlotContents(int i, ItemStack stack) {
		super.setInventorySlotContents(i, stack);

		if(stack != null && stack.getItem() instanceof ItemMachineUpgrade && i >= 6 && i <=7) {
			worldObj.playSoundEffect(xCoord + 0.5, yCoord + 0.5, zCoord + 0.5, "hbm:item.upgradePlug", 1.0F, 1.0F);
		}
	}

	@Override
	public void updateEntity() {

		if(!worldObj.isRemote) {
			modulePowerPorts.update(getPorts());

			CBT_Atmosphere atmosphere = ChunkAtmosphereManager.proxy.getAtmosphere(worldObj, xCoord, yCoord, zCoord);
			canOperate = atmosphere == null || atmosphere.getPressure() <= 0.001;

			this.power = Library.chargeTEFromItems(slots, 5, this.getPower(), this.getMaxPower());
			recipe = VacuumCircuitRecipes.getRecipe(new ItemStack[] {slots[0], slots[1], slots[2], slots[3]});
			long intendedMaxPower;



			upgradeManager.checkSlots(this, slots, 4, 4);
			int redLevel = upgradeManager.getLevel(UpgradeType.SPEED);
			int blueLevel = upgradeManager.getLevel(UpgradeType.POWER);

			if(recipe != null) {
				this.processTime = recipe.duration - (recipe.duration * redLevel / 6) + (recipe.duration * blueLevel / 3);
				this.consumption = recipe.consumption + (recipe.consumption * redLevel) - (recipe.consumption * blueLevel / 6);
				intendedMaxPower = recipe.consumption * 20;

				if(canProcess(recipe)) {
					this.progress++;
					this.power -= this.consumption;

					if(progress >= processTime) {
						this.progress = 0;
						this.consumeItems(recipe);

						if(slots[4] == null) {
							slots[4] = recipe.output.copy();
						} else {
							slots[4].stackSize += recipe.output.stackSize;
						}

						this.markDirty();
					}

					if(worldObj.getTotalWorldTime() % 20 == 0) {
						ForgeDirection dir = ForgeDirection.getOrientation(this.getBlockMetadata() - 10);
						ForgeDirection rot = dir.getRotation(ForgeDirection.UP);
						NBTTagCompound dPart = new NBTTagCompound();
						dPart.setString("type", "tau");
						dPart.setByte("count", (byte) 3);
						PacketDispatcher.wrapper.sendToAllAround(new AuxParticlePacketNT(dPart, xCoord + 0.5 + dir.offsetX * 0.625 + rot.offsetX * 0.5, yCoord + 1.25, zCoord + 0.5 + dir.offsetZ * 0.625 + rot.offsetZ * 0.5), new TargetPoint(worldObj.provider.dimensionId, xCoord, yCoord, zCoord, 25));
					}
				} else {
					this.progress = 0;
				}

			} else {
				this.progress = 0;
				this.consumption = 100;
				intendedMaxPower = 2000;
			}

			this.maxPower = Math.max(intendedMaxPower, power);

			this.networkPackNT(25);
		}
	}

	public boolean canProcess(VacuumCircuitRecipe recipe) {
		if(!canOperate) return false;

		if(this.power < this.consumption) return false;

		if(slots[4] != null) {
			if(slots[4].getItem() != recipe.output.getItem()) return false;
			if(slots[4].getItemDamage() != recipe.output.getItemDamage()) return false;
			if(slots[4].stackSize + recipe.output.stackSize > slots[4].getMaxStackSize()) return false;
		}

		return true;
	}

	public void consumeItems(VacuumCircuitRecipe recipe) {

		for(AStack aStack : recipe.wafer) {
			for(int i = 0; i < 2; i++) {
				ItemStack stack = slots[i];
				if(aStack.matchesRecipe(stack, true) && stack.stackSize >= aStack.stacksize) { this.decrStackSize(i, aStack.stacksize); break; }
			}
		}

		for(AStack aStack : recipe.pcb) {
			for(int i = 2; i < 4; i++) {
				ItemStack stack = slots[i];
				if(aStack.matchesRecipe(stack, true) && stack.stackSize >= aStack.stacksize) { this.decrStackSize(i, aStack.stacksize); break; }
			}
		}

	}

	@Override
	public boolean isItemValidForSlot(int slot, ItemStack stack) {
		if(slot < 2) {
			for(int i = 0; i < 2; i++) if(i != slot && slots[i] != null && slots[i].isItemEqual(stack)) return false;
			for(AStack t : VacuumCircuitRecipes.wafer) if(t.matchesRecipe(stack, true)) return true;
		} else if(slot < 4) {
			for(int i = 2; i < 4; i++) if(i != slot && slots[i] != null && slots[i].isItemEqual(stack)) return false;
			for(AStack t : VacuumCircuitRecipes.pcb) if(t.matchesRecipe(stack, true)) return true;
		}
		return false;
	}

	@Override
	public boolean canExtractItem(int i, ItemStack itemStack, int j) {
		return i == 4;
	}

	@Override
	public int[] getAccessibleSlotsFromSide(int side) {
		return new int[] { 0, 1, 2, 3, 4 };
	}

	protected PortDef[] cachedPorts;

	public PortDef[] getPorts() {
		if(cachedPorts == null) {
			cachedPorts = new PortDef[] {
				PortDef.make(xCoord + 1, yCoord, zCoord + 1, Library.POS_X),
				PortDef.make(xCoord + 1, yCoord, zCoord - 1, Library.POS_X),
				PortDef.make(xCoord - 1, yCoord, zCoord + 1, Library.NEG_X),
				PortDef.make(xCoord - 1, yCoord, zCoord - 1, Library.NEG_X),
				PortDef.make(xCoord + 1, yCoord, zCoord + 1, Library.POS_Z),
				PortDef.make(xCoord - 1, yCoord, zCoord + 1, Library.POS_Z),
				PortDef.make(xCoord + 1, yCoord, zCoord - 1, Library.NEG_Z),
				PortDef.make(xCoord - 1, yCoord, zCoord - 1, Library.NEG_Z)
			};
		}

		return cachedPorts;
	}

	@Override
	public void serialize(ByteBuf buf) {
		super.serialize(buf);

		buf.writeLong(power);
		buf.writeLong(maxPower);
		buf.writeLong(consumption);
		buf.writeInt(progress);
		buf.writeInt(processTime);
		buf.writeBoolean(canOperate);

		if(recipe != null) {
			buf.writeBoolean(true);
			buf.writeInt(Item.getIdFromItem(recipe.output.getItem()));
			buf.writeInt(recipe.output.getItemDamage());
		} else {
			buf.writeBoolean(false);
		}
	}

	@Override
	public void deserialize(ByteBuf buf) {
		super.deserialize(buf);

		power = buf.readLong();
		maxPower = buf.readLong();
		consumption = buf.readLong();
		progress = buf.readInt();
		processTime = buf.readInt();
		canOperate = buf.readBoolean();

		if(buf.readBoolean()) {
			int id = buf.readInt();
			int meta = buf.readInt();
			display = new ItemStack(Item.getItemById(id), 1, meta);
		} else {
			display = null;
		}
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);

		this.power = nbt.getLong("power");
		this.maxPower = nbt.getLong("maxPower");
		this.progress = nbt.getInteger("progress");
		this.processTime = nbt.getInteger("processTime");
	}

	@Override
	public void writeToNBT(NBTTagCompound nbt) {
		super.writeToNBT(nbt);

		nbt.setLong("power", power);
		nbt.setLong("maxPower", maxPower);
		nbt.setInteger("progress", progress);
		nbt.setInteger("processTime", processTime);
	}

	@Override
	public long getPower() {
		return Math.max(Math.min(power, maxPower), 0);
	}

	@Override
	public void setPower(long power) {
		this.power = power;
	}

	@Override
	public long getMaxPower() {
		return maxPower;
	}


	@Override
	public Container provideContainer(int ID, EntityPlayer player, World world, int x, int y, int z) {
		return new ContainerVacuumCircuit(player.inventory, this);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public Object provideGUI(int ID, EntityPlayer player, World world, int x, int y, int z) {
		return new GUIVacuumCircuit(player.inventory, this);
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
					yCoord + 3,
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

	@Override
	public boolean canProvideInfo(UpgradeType type, int level, boolean extendedInfo) {
		return type == UpgradeType.SPEED || type == UpgradeType.POWER;
	}

	@Override
	public void provideInfo(UpgradeType type, int level, List<String> info, boolean extendedInfo) {
		info.add(IUpgradeInfoProvider.getStandardLabel(ModBlocks.machine_vacuum_circuit));
		if(type == UpgradeType.SPEED) {
			info.add(EnumChatFormatting.GREEN + I18nUtil.resolveKey(KEY_DELAY, "-" + (level * 100 / 6) + "%"));
			info.add(EnumChatFormatting.RED + I18nUtil.resolveKey(KEY_CONSUMPTION, "+" + (level * 100) + "%"));
		}
		if(type == UpgradeType.POWER) {
			info.add(EnumChatFormatting.GREEN + I18nUtil.resolveKey(KEY_CONSUMPTION, "-" + (level * 100 / 6) + "%"));
			info.add(EnumChatFormatting.RED + I18nUtil.resolveKey(KEY_DELAY, "+" + (level * 100 / 3) + "%"));
		}
	}

	@Override
	public HashMap<UpgradeType, Integer> getValidUpgrades() {
		HashMap<UpgradeType, Integer> upgrades = new HashMap<>();
		upgrades.put(UpgradeType.SPEED, 3);
		upgrades.put(UpgradeType.POWER, 3);
		return upgrades;
	}

}
