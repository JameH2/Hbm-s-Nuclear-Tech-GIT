package com.hbm.tileentity.machine;

import java.util.ArrayList;
import java.util.List;

import api.hbm.tile.IHeatSource;
import com.hbm.blocks.ModBlocks;
import com.hbm.blocks.machine.ReactorResearch;
import com.hbm.config.CustomMachineConfigJSON;
import com.hbm.config.CustomMachineConfigJSON.MachineConfiguration;
import com.hbm.config.CustomMachineConfigJSON.MachineConfiguration.ComponentDefinition;
import com.hbm.handler.pollution.PollutionHandler;
import com.hbm.handler.radiation.ChunkRadiationManager;
import com.hbm.inventory.container.ContainerMachineCustom;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.tank.FluidTank;
import com.hbm.inventory.gui.GUIMachineCustom;
import com.hbm.inventory.recipes.CustomMachineRecipes;
import com.hbm.inventory.recipes.CustomMachineRecipes.CustomMachineRecipe;
import com.hbm.lib.Library;
import com.hbm.main.MainRegistry;
import com.hbm.module.ModulePatternMatcher;
import com.hbm.sound.AudioWrapper;
import com.hbm.tileentity.IGUIProvider;
import com.hbm.tileentity.TileEntityMachinePolluting;
import com.hbm.tileentity.TileEntityProxyBase;
import com.hbm.tileentity.TileEntityProxyCombo;
import com.hbm.tileentity.TilePort.PortDef;
import com.hbm.util.BufferUtil;
import com.hbm.util.Compat;
import com.hbm.util.fauxpointtwelve.BlockPos;
import com.hbm.util.fauxpointtwelve.DirPos;

import api.hbm.energymk2.IEnergyProviderMK2;
import api.hbm.energymk2.IEnergyReceiverMK2;
import api.hbm.fluidmk2.IFluidStandardTransceiverMK2;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import api.hbm.block.ICrucibleAcceptor;
import api.hbm.block.IToolable;
import api.hbm.block.IToolable.ToolType;
import api.hbm.item.IDepthRockTool;
import com.hbm.blocks.machine.FoundryOutlet;
import com.hbm.inventory.material.Mats;
import com.hbm.inventory.material.Mats.MaterialStack;
import com.hbm.inventory.material.NTMMaterial;
import com.hbm.interfaces.IControlReceiver;
import com.hbm.items.ModItems;
import com.hbm.items.machine.ItemScraps;
import com.hbm.items.tool.ItemToolAbility;
import com.hbm.items.tool.ItemTooling;
import net.minecraft.item.Item;
import net.minecraft.item.ItemTool;

public class TileEntityCustomMachine extends TileEntityMachinePolluting implements IFluidStandardTransceiverMK2, IEnergyProviderMK2, IEnergyReceiverMK2, ICrucibleAcceptor, IControlReceiver, IGUIProvider {

	public String machineType;
	public MachineConfiguration config;

	public long power;
	public int flux;
	public int heat;
	public int maxHeat;
	public int progress;
	public int maxProgress = 1;
	public boolean isProgressing;
	public int networkTicker = 0;
	public FluidTank[] inputTanks;
	public FluidTank[] outputTanks;
	public ModulePatternMatcher matcher;
	public int structureCheckDelay;
	public boolean structureOK = false;
	public int ghostAnimationIndex = 0;
	private AudioWrapper audio;
	public CustomMachineRecipe cachedRecipe;
	public List<MaterialStack> materials = new ArrayList();

	private static final int[] EMPTY_SLOTS = new int[0];
	private int[] accessibleSlots = null;
	private int cachedItemInCount = -1;
	public List<DirPos> connectionPos = new ArrayList();
	public PortDef[] ports = new PortDef[0];
	public List<DirPos> fluxPos = new ArrayList();
	public List<DirPos> heatPos = new ArrayList();
	

	public TileEntityCustomMachine() {
		/*
		 * 0: Battery
		 * 1-3: Fluid IDs
		 * 4-9: Inputs
		 * 10-15: Template
		 * 16-21: Output
		 */
		super(22, 100);
	}

	public void init() {
		MachineConfiguration config = CustomMachineConfigJSON.customMachines.get(this.machineType);

		if (config != null) {
			this.config = config;
			this.bb = null; // invalidate cached bounding box since config changed

			inputTanks = new FluidTank[config.fluidInCount];
			for (int i = 0; i < inputTanks.length; i++) inputTanks[i] = new FluidTank(Fluids.NONE, config.fluidInCap);
			outputTanks = new FluidTank[config.fluidOutCount];
			for (int i = 0; i < outputTanks.length; i++)
				outputTanks[i] = new FluidTank(Fluids.NONE, config.fluidOutCap);
			maxHeat = config.maxHeat;
			matcher = new ModulePatternMatcher(config.itemInCount);
			smoke.changeTankSize(config.maxPollutionCap);
			smoke_leaded.changeTankSize(config.maxPollutionCap);
			smoke_poison.changeTankSize(config.maxPollutionCap);

		} else {
			worldObj.func_147480_a(xCoord, yCoord, zCoord, false);
		}
	}

	public boolean hasMaterialSupport() {
		return config != null && config.materialInCount > 0;
	}

	public int getTotalMaterialAmount() {
		int total = 0;
		for(MaterialStack ms : materials) total += ms.amount;
		return total;
	}

	private int containsMaterial(NTMMaterial mat) {
		for(int i = 0; i < materials.size(); i++) {
			if(materials.get(i).material == mat) return i;
		}
		return -1;
	}

	private void addToMaterials(Mats.MaterialStack stack) {
		int idx = containsMaterial(stack.material);
		if(idx >= 0) {
			materials.get(idx).amount += stack.amount;
		} else {
			materials.add(stack.copy());
		}
	}

	@Override
	public boolean canAcceptPartialPour(World world, int x, int y, int z, double dX, double dY, double dZ, ForgeDirection side, Mats.MaterialStack stack) {
		if(!hasMaterialSupport()) return false;
		if(side != ForgeDirection.UP) return false;
		int existingType = containsMaterial(stack.material);
		if(existingType == -1 && materials.size() >= config.materialInCount) return false;
		if(getTotalMaterialAmount() + stack.amount > config.materialInCap) return false;
		return true;
	}

	@Override
	public Mats.MaterialStack pour(World world, int x, int y, int z, double dX, double dY, double dZ, ForgeDirection side, Mats.MaterialStack stack) {
		if(!canAcceptPartialPour(world, x, y, z, dX, dY, dZ, side, stack)) return stack;
		addToMaterials(stack);
		return null;
	}

	@Override
	public boolean canAcceptPartialFlow(World world, int x, int y, int z, ForgeDirection side, Mats.MaterialStack stack) { return false; }

	@Override
	public Mats.MaterialStack flow(World world, int x, int y, int z, ForgeDirection side, Mats.MaterialStack stack) { return stack; }

	@Override
	public String getName() {
		return config != null ? config.localizedName : "INVALID";
	}

	@Override
	public void updateEntity() {

		if (!worldObj.isRemote) {

			if(config == null) {
				worldObj.func_147480_a(xCoord, yCoord, zCoord, false);
				return;
			}
			this.isProgressing = false;
			
			if(this.structureOK){
				this.setupAllPorts(ports);
				this.moduleSmokePorts.update(ports);
				if(config.generatorMode) {
					this.updatePortPOFIFO();
				} else {
					this.updatePortPIFIFO();
				}
			}

			this.power = Library.chargeTEFromItems(slots, 0, power, this.config.maxPower);

			if (this.inputTanks.length > 0) this.inputTanks[0].setType(1, slots);
			if (this.inputTanks.length > 1) this.inputTanks[1].setType(2, slots);
			if (this.inputTanks.length > 2) this.inputTanks[2].setType(3, slots);

			this.structureCheckDelay--;
			if (this.structureCheckDelay <= 0) this.checkStructure();

			if (this.worldObj.getTotalWorldTime() % 20 == 0) {
				for (byte d = 2; d < 6; d++) {
					ForgeDirection dir = ForgeDirection.getOrientation(d);
					for (DirPos pos : this.fluxPos) {
						Block b = worldObj.getBlock(pos.getX() + dir.offsetX, pos.getY(), pos.getZ() + dir.offsetZ);
						if (b == ModBlocks.reactor_research) {
							int[] source = ((ReactorResearch) ModBlocks.reactor_research).findCore(worldObj, pos.getX() + dir.offsetX, pos.getY(), pos.getZ() + dir.offsetZ);
							if (source != null) {

								TileEntity tile = worldObj.getTileEntity(source[0], source[1], source[2]);

								if (tile instanceof TileEntityReactorResearch) {

									TileEntityReactorResearch reactor = (TileEntityReactorResearch) tile;
									this.flux = reactor.totalFlux;
								}
							}
						}
					}
					if(config.maxHeat>0){
						for (DirPos pos : this.heatPos){
							this.tryPullHeat(pos.getX() + dir.offsetX, pos.getY()-1, pos.getZ() + dir.offsetZ);
						}
					}
				}
			}

			if(hasMaterialSupport() && !materials.isEmpty() && worldObj.getTotalWorldTime() % 20 == 0) {
				CustomMachineRecipe recipe = this.getMatchingRecipe();
				Mats.MaterialStack[] inputMats = recipe != null ? recipe.inputMaterials : null;
				for(DirPos pos : this.connectionPos) {
					if(pos.getDir() == ForgeDirection.UP || pos.getDir() == ForgeDirection.DOWN) continue;
					TileEntity te = worldObj.getTileEntity(pos.getX(), pos.getY(), pos.getZ());
					if(te instanceof TileEntityFoundryChannel || te instanceof TileEntityFoundryOutlet) {
						ICrucibleAcceptor acc = (ICrucibleAcceptor) te;
						ForgeDirection side = pos.getDir().getOpposite();
						matLoop:
						for(MaterialStack ms : materials) {
							if(ms.amount <= 0) continue;
							if(inputMats != null) {
								for(MaterialStack req : inputMats) {
									if(ms.material == req.material) continue matLoop;
								}
							}
							if(acc.canAcceptPartialFlow(worldObj, pos.getX(), pos.getY(), pos.getZ(), side, ms)) {
								MaterialStack remaining = acc.flow(worldObj, pos.getX(), pos.getY(), pos.getZ(), side, ms);
								ms.amount = remaining != null ? remaining.amount : 0;
							}
						}
					}
				}
				materials.removeIf(m -> m.amount <= 0);
			}

			if (this.structureOK) {

				if (config.generatorMode) {
					if (this.cachedRecipe == null) {
						CustomMachineRecipe recipe = this.getMatchingRecipe();
						if (recipe != null && this.hasRequiredQuantities(recipe) && this.hasSpace(recipe)) {
							this.cachedRecipe = recipe;
							this.useUpInput(recipe);
						}
					}

					if (this.cachedRecipe != null) {
						isProgressing = true;
						this.maxProgress = (int) Math.max(cachedRecipe.duration / this.config.recipeSpeedMult, 1);
						int powerReq = (int) Math.max(cachedRecipe.consumptionPerTick * this.config.recipeConsumptionMult, 1);

						this.progress++;
						this.power += powerReq;
						this.heat -= cachedRecipe.heat;
						if (power > config.maxPower) power = config.maxPower;
						if (worldObj.getTotalWorldTime() % 20 == 0) {
							pollution(cachedRecipe);
							radiation(cachedRecipe);
						}
						if (progress >= this.maxProgress) {
							this.progress = 0;
							this.processRecipe(cachedRecipe);
							this.cachedRecipe = null;
						}
					}

				} else {
					CustomMachineRecipe recipe = this.getMatchingRecipe();

					if (recipe != null) {
						this.maxProgress = (int) Math.max(recipe.duration / this.config.recipeSpeedMult, 1);
						int powerReq = (int) Math.max(recipe.consumptionPerTick * this.config.recipeConsumptionMult, 1);

						if (this.power >= powerReq && this.hasRequiredQuantities(recipe) && this.hasSpace(recipe)) {
							this.progress++;
							this.power -= powerReq;
							this.heat -= recipe.heat;
							if (worldObj.getTotalWorldTime() % 20 == 0) {
								pollution(recipe);
								radiation(recipe);
							}
							if (progress >= this.maxProgress) {
								this.progress = 0;
								this.useUpInput(recipe);
								this.processRecipe(recipe);
							}
						}
					} else {
						this.progress = 0;
					}
				}
			} else {
				this.progress = 0;
			}
			// Adaptive sync: 50t when active, 200t when idle
			int sendInterval = this.isProgressing ? 50 : 200;
			this.networkTicker++;
			if(this.networkTicker >= sendInterval) {
				this.networkTicker = 0;
				this.networkPackNT(50);
			}
		} else {
			// Pre-compute ghost animation index so renderer doesn't call System.currentTimeMillis()
			if(!this.structureOK && this.config != null && this.config.components != null
				&& !this.config.components.isEmpty()) {
				this.ghostAnimationIndex = (int)((this.worldObj.getTotalWorldTime() / 20)
					% this.config.components.size());
			}

			float volume = this.getVolume(1F);
			if (this.isProgressing && config.progressSound!=null && MainRegistry.proxy.me().getDistance(xCoord, yCoord, zCoord) < 50) {
				if (audio == null) {
					audio = this.createAudioLoop();
					audio.startSound();
				} else if (!audio.isPlaying()) {
					audio = rebootAudio(audio);
				}
				audio.keepAlive();
				audio.updateVolume(volume);
			} else {
				if (audio != null) {
					audio.stopSound();
					audio = null;
				}
			}

		}

	}

	@Override
	public void serialize(ByteBuf buf) {
		super.serialize(buf);

		BufferUtil.writeString(buf, this.machineType);

		buf.writeLong(power);
		buf.writeInt(progress);
		buf.writeBoolean(isProgressing);
		buf.writeInt(flux);
		buf.writeInt(heat);
		buf.writeBoolean(structureOK);
		buf.writeInt(maxProgress);
		for (FluidTank inputTank : inputTanks) inputTank.serialize(buf);
		for (FluidTank outputTank : outputTanks) outputTank.serialize(buf);
		this.matcher.serialize(buf);
		if(hasMaterialSupport()) {
			buf.writeShort(materials.size());
			for(Mats.MaterialStack ms : materials) {
				buf.writeInt(ms.material != null ? ms.material.id : -1);
				buf.writeInt(ms.amount);
			}
		}
	}

	@Override
	public void deserialize(ByteBuf buf) {
		super.deserialize(buf);

		this.machineType = BufferUtil.readString(buf);
		if(this.config == null) this.init();

		this.power = buf.readLong();
		this.progress = buf.readInt();
		this.isProgressing = buf.readBoolean();
		this.flux = buf.readInt();
		this.heat = buf.readInt();
		this.structureOK = buf.readBoolean();
		this.maxProgress = buf.readInt();
		for (FluidTank inputTank : inputTanks) inputTank.deserialize(buf);
		for (FluidTank outputTank : outputTanks) outputTank.deserialize(buf);
		this.matcher.deserialize(buf);
		if(hasMaterialSupport()) {
			materials.clear();
			int matSize = buf.readShort();
			for(int i = 0; i < matSize; i++) {
				int id = buf.readInt();
				int amount = buf.readInt();
				if(id == -1) continue;
				NTMMaterial mat = Mats.matById.get(id);
				if(mat != null) materials.add(new MaterialStack(mat, amount));
			}
		}
	}
	@Override
	public AudioWrapper createAudioLoop() {
		return MainRegistry.proxy.getLoopedSound(config.progressSound, xCoord, yCoord, zCoord, 1.0F, 10F, 1.0F);
	}
	@Override
	public void onChunkUnload() {

		if(audio != null) {
			audio.stopSound();
			audio = null;
		}
	}

	@Override
	public void invalidate() {

		super.invalidate();

		if(audio != null) {
			audio.stopSound();
			audio = null;
		}
	}
	/** Only accepts inputs in a fixed order, saves a ton of performance because there's no permutations to check for.
	 *  Caches the last matched recipe: validates it against current inputs first (O(slots)),
	 *  only falls back to full scan (O(recipes × slots)) when inputs changed. */
	public CustomMachineRecipe getMatchingRecipe() {
		List<CustomMachineRecipe> recipes = CustomMachineRecipes.recipes.get(this.config.recipeKey);
		if(recipes == null || recipes.isEmpty()) return null;

		// Fast path: validate cached recipe against current inputs
		if(cachedRecipe != null) {
			boolean stillMatches = true;
			for(int i = 0; i < cachedRecipe.inputFluids.length && stillMatches; i++) {
				if(this.inputTanks[i].getTankType() != cachedRecipe.inputFluids[i].type
					|| this.inputTanks[i].getPressure() != cachedRecipe.inputFluids[i].pressure)
					stillMatches = false;
			}
			for(int i = 0; i < cachedRecipe.inputItems.length && stillMatches; i++) {
				if(cachedRecipe.inputItems[i] != null && slots[i + 4] == null) stillMatches = false;
				if(!cachedRecipe.inputItems[i].matchesRecipe(slots[i + 4], true)) stillMatches = false;
			}
			if(stillMatches) return cachedRecipe;
		}

		// Slow path: full scan
		outer:
		for(CustomMachineRecipe recipe : recipes) {
			for(int i = 0; i < recipe.inputFluids.length; i++) {
				if(this.inputTanks[i].getTankType() != recipe.inputFluids[i].type || this.inputTanks[i].getPressure() != recipe.inputFluids[i].pressure) continue outer;
			}

			for(int i = 0; i < recipe.inputItems.length; i++) {
				if(recipe.inputItems[i] != null && slots[i + 4] == null) continue outer;
				if(!recipe.inputItems[i].matchesRecipe(slots[i + 4], true)) continue outer;
			}

			cachedRecipe = recipe;
			return recipe;
		}

		cachedRecipe = null;
		return null;
	}
	public void pollution(CustomMachineRecipe recipe) {
		if(recipe.pollutionAmount > 0) {
			this.pollute(PollutionHandler.PollutionType.valueOf(recipe.pollutionType), recipe.pollutionAmount);
		} else if(recipe.pollutionAmount < 0 && PollutionHandler.getPollution(worldObj, xCoord, yCoord, zCoord, PollutionHandler.PollutionType.valueOf(recipe.pollutionType)) >= -recipe.pollutionAmount) {
			PollutionHandler.decrementPollution(worldObj, xCoord, yCoord, zCoord, PollutionHandler.PollutionType.valueOf(recipe.pollutionType), -recipe.pollutionAmount);
		}
	}
	public void radiation(CustomMachineRecipe recipe){
		if(recipe.radiationAmount > 0) {
			ChunkRadiationManager.proxy.incrementRad(worldObj, xCoord, yCoord, zCoord, recipe.radiationAmount);
		} else if (recipe.radiationAmount < 0) {
			ChunkRadiationManager.proxy.decrementRad(worldObj, xCoord, yCoord, zCoord, -recipe.radiationAmount);
		}
	}
	protected void tryPullHeat(int x, int y, int z) {
		TileEntity con = worldObj.getTileEntity(x, y, z);

		if(con instanceof IHeatSource) {
			IHeatSource source = (IHeatSource) con;
			int diff = source.getHeatStored() - this.heat;

			if(diff == 0) {
				return;
			}

			if(diff > 0) {
				source.useUpHeat(diff);
				this.heat += diff;
				if(this.heat > this.maxHeat)
					this.heat = this.maxHeat;
			}
		}
	}
	public boolean hasRequiredQuantities(CustomMachineRecipe recipe) {

		for(int i = 0; i < recipe.inputFluids.length; i++) {
			if(this.inputTanks[i].getFill() < recipe.inputFluids[i].fill) return false;
		}

		for(int i = 0; i < recipe.inputItems.length; i++) {
			if(slots[i + 4] != null && slots[i + 4].stackSize < recipe.inputItems[i].stacksize) return false;
		}
		if(recipe.inputMaterials != null) {
			for(Mats.MaterialStack req : recipe.inputMaterials) {
				int idx = containsMaterial(req.material);
				if(idx == -1 || materials.get(idx).amount < req.amount) return false;
			}
		}
		if(config.fluxMode ? this.flux < recipe.flux : false) return false;
		if(config.maxHeat>0 && recipe.heat>0 ? this.heat < recipe.heat : false) return false;
		return true;
	}

	public boolean hasSpace(CustomMachineRecipe recipe) {

		for(int i = 0; i < recipe.outputFluids.length; i++) {
			if(this.outputTanks[i].getTankType() == recipe.outputFluids[i].type && this.outputTanks[i].getFill() + recipe.outputFluids[i].fill > this.outputTanks[i].getMaxFill()) return false;
		}

		for(int i = 0; i < recipe.outputItems.length; i++) {
			if(slots[i + 16] != null && (slots[i + 16].getItem() != recipe.outputItems[i].key.getItem() || slots[i + 16].getItemDamage() != recipe.outputItems[i].key.getItemDamage())) return false;
			if(slots[i + 16] != null && slots[16 + i].stackSize + recipe.outputItems[i].key.stackSize > slots[i + 16].getMaxStackSize()) return false;
		}
		if(recipe.outputMaterials != null && hasMaterialSupport()) {
			int total = getTotalMaterialAmount();
			int newTypes = 0;
			for(Mats.MaterialStack out : recipe.outputMaterials) {
				total += out.amount;
				if(containsMaterial(out.material) == -1) newTypes++;
			}
			if(total > config.materialInCap) return false;
			if(materials.size() + newTypes > config.materialInCount) return false;
		}

		return true;
	}

	public void useUpInput(CustomMachineRecipe recipe) {

		for(int i = 0; i < recipe.inputFluids.length; i++) {
			this.inputTanks[i].setFill(this.inputTanks[i].getFill() - recipe.inputFluids[i].fill);
		}

		for(int i = 0; i < recipe.inputItems.length; i++) {
			this.decrStackSize(i + 4, recipe.inputItems[i].stacksize);
		}
		if(recipe.inputMaterials != null) {
			for(Mats.MaterialStack req : recipe.inputMaterials) {
				int idx = containsMaterial(req.material);
				if(idx >= 0) materials.get(idx).amount -= req.amount;
			}
			materials.removeIf(ms -> ms.amount <= 0);
		}
	}

	public void processRecipe(CustomMachineRecipe recipe) {

		for(int i = 0; i < recipe.outputFluids.length; i++) {
			if(this.outputTanks[i].getTankType() != recipe.outputFluids[i].type) this.outputTanks[i].setTankType(recipe.outputFluids[i].type);
			this.outputTanks[i].setFill(this.outputTanks[i].getFill() + recipe.outputFluids[i].fill);
		}

		for(int i = 0; i < recipe.outputItems.length; i++) {

			if(worldObj.rand.nextFloat() < recipe.outputItems[i].value) {
				if(slots[i + 16] == null) {
					slots[i + 16] = recipe.outputItems[i].key.copy();
				} else {
					slots[i + 16].stackSize += recipe.outputItems[i].key.stackSize;
				}
			}
		}
		if(recipe.outputMaterials != null) {
			for(Mats.MaterialStack out : recipe.outputMaterials) addToMaterials(out);
		}
	}

	/**
	 * Transforms a component's local coordinates to world coordinates, handling rotation.
	 */
	protected BlockPos getComponentWorldPos(ComponentDefinition comp) {
		ForgeDirection dir = ForgeDirection.getOrientation(this.getBlockMetadata());
		ForgeDirection rot = dir.getRotation(ForgeDirection.UP);

		int x = xCoord - dir.offsetX * comp.x + rot.offsetX * comp.x;
		int y = yCoord + comp.y;
		int z = zCoord - dir.offsetZ * comp.z + rot.offsetZ * comp.z;

		if(dir == ForgeDirection.EAST || dir == ForgeDirection.WEST) {
			x = xCoord + dir.offsetZ * comp.z - rot.offsetZ * comp.z;
			z = zCoord + dir.offsetX * comp.x - rot.offsetX * comp.x;
		}

		return new BlockPos(x, y, z);
	}

	public boolean checkStructure() {

		this.structureCheckDelay = 300;
		this.structureOK = false;
		if(this.config == null) return false;
		this.connectionPos.clear();
		
		List<PortDef> portList = new ArrayList();

		for(ComponentDefinition comp : config.components) {

			BlockPos pos = getComponentWorldPos(comp);
			int x = pos.getX();
			int y = pos.getY();
			int z = pos.getZ();

			Block b = worldObj.getBlock(x, y, z);
			if(b != comp.block) return false;

			int meta = worldObj.getBlockMetadata(x, y, z);
			if(!comp.allowedMetas.contains(meta)) return false;

			TileEntity tile = Compat.getTileStandard(worldObj, x, y, z);
			if(tile instanceof TileEntityProxyBase) {
				TileEntityProxyBase proxy = (TileEntityProxyBase) tile;
				proxy.cachedPosition = new BlockPos(xCoord, yCoord, zCoord);
				proxy.markDirty();

				if(proxy instanceof TileEntityProxyCombo && hasMaterialSupport()) {
					((TileEntityProxyCombo) proxy).moltenMetal = true;
					proxy.markDirty();
				}

				for(ForgeDirection facing : ForgeDirection.VALID_DIRECTIONS) {
					this.connectionPos.add(new DirPos(x + facing.offsetX, y + facing.offsetY, z + facing.offsetZ, facing));
				}
				portList.add(PortDef.make(x, y, z, ForgeDirection.VALID_DIRECTIONS));
			}
			if(worldObj.getBlock(x,y,z) == ModBlocks.cm_flux){
				for(ForgeDirection facing : ForgeDirection.VALID_DIRECTIONS) {
					this.fluxPos.add(new DirPos(x + facing.offsetX, y + facing.offsetY, z + facing.offsetZ, facing));
				}
			}
			else if(worldObj.getBlock(x,y,z) == ModBlocks.cm_heat){
				for(ForgeDirection facing : ForgeDirection.VALID_DIRECTIONS) {
					this.heatPos.add(new DirPos(x + facing.offsetX, y + facing.offsetY, z + facing.offsetZ, facing));
				}
			}

		}
		
		portList.add(PortDef.make(xCoord, yCoord, zCoord, ForgeDirection.VALID_DIRECTIONS));
		this.ports = portList.toArray(new PortDef[0]);

		this.structureOK = true;
		return true;
	}

	public void buildStructure() {

		if(this.config == null) return;

		for(ComponentDefinition comp : config.components) {
			BlockPos pos = getComponentWorldPos(comp);
			worldObj.setBlock(pos.getX(), pos.getY(), pos.getZ(), comp.block, (int) comp.allowedMetas.toArray()[0], 3);
		}
	}

	@Override
	public int[] getAccessibleSlotsFromSide(int side) {
		if(this.config == null) return EMPTY_SLOTS;
		if(this.config.itemInCount != cachedItemInCount || accessibleSlots == null) {
			accessibleSlots = computeAccessibleSlots(this.config.itemInCount);
			cachedItemInCount = this.config.itemInCount;
		}
		return accessibleSlots;
	}

	private int[] computeAccessibleSlots(int itemInCount) {
		if(itemInCount > 5) return new int[] { 4, 5, 6, 7, 8, 9, 16, 17, 18, 19, 20, 21 };
		if(itemInCount > 4) return new int[] { 4, 5, 6, 7, 8, 16, 17, 18, 19, 20, 21 };
		if(itemInCount > 3) return new int[] { 4, 5, 6, 7, 16, 17, 18, 19, 20, 21 };
		if(itemInCount > 2) return new int[] { 4, 5, 6, 16, 17, 18, 19, 20, 21 };
		if(itemInCount > 1) return new int[] { 4, 5, 16, 17, 18, 19, 20, 21 };
		if(itemInCount > 0) return new int[] { 4, 16, 17, 18, 19, 20, 21 };
		return new int[] { 16, 17, 18, 19, 20, 21 };
	}

	@Override
	public boolean canExtractItem(int i, ItemStack stack, int j) {
		return i >= 16 && i <= 21;
	}

	@Override
	public boolean isItemValidForSlot(int slot, ItemStack stack) {
		if(slot < 4 || slot > 9) return false;

		int index = slot - 4;
		int filterSlot = slot + 6;

		if(slots[filterSlot] == null) return true;

		return matcher.isValidForFilter(slots[filterSlot], index, stack);
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {

		this.machineType = nbt.getString("machineType");
		this.init();

		super.readFromNBT(nbt);

		if(this.config != null) {

			for(int i = 0; i < inputTanks.length; i++) inputTanks[i].readFromNBT(nbt, "i" + i);
			for(int i = 0; i < outputTanks.length; i++) outputTanks[i].readFromNBT(nbt, "o" + i);

			this.matcher.readFromNBT(nbt);

			int index = nbt.getInteger("cachedIndex");
			if(index != -1) {
				this.cachedRecipe = CustomMachineRecipes.recipes.get(this.config.recipeKey).get(index);
			}
			if(hasMaterialSupport()) {
				materials.clear();
				int[] matArray = nbt.getIntArray("materials");
				if(matArray != null) {
					for(int i = 0; i < matArray.length / 2; i++) {
						NTMMaterial mat = Mats.matById.get(matArray[i * 2]);
						if(mat != null) materials.add(new MaterialStack(mat, matArray[i * 2 + 1]));
					}
				}
			}
		}
	}

	@Override
	public void writeToNBT(NBTTagCompound nbt) {

		if(machineType == null || this.config == null) {
			super.writeToNBT(nbt);
			return;
		}

		nbt.setString("machineType", machineType);

		super.writeToNBT(nbt);

		for(int i = 0; i < inputTanks.length; i++) inputTanks[i].writeToNBT(nbt, "i" + i);
		for(int i = 0; i < outputTanks.length; i++) outputTanks[i].writeToNBT(nbt, "o" + i);

		this.matcher.writeToNBT(nbt);

		if(this.cachedRecipe != null) {
			int index = CustomMachineRecipes.recipes.get(this.config.recipeKey).indexOf(this.cachedRecipe);
			nbt.setInteger("cachedIndex", index);
		} else {
			nbt.setInteger("cachedIndex", -1);
		}
		if(hasMaterialSupport()) {
			int[] matArray = new int[materials.size() * 2];
			for(int i = 0; i < materials.size(); i++) {
				matArray[i * 2] = materials.get(i).material.id;
				matArray[i * 2 + 1] = materials.get(i).amount;
			}
			nbt.setIntArray("materials", matArray);
		}
	}
	AxisAlignedBB bb = null;

	@Override
	public AxisAlignedBB getRenderBoundingBox() {

		if(bb == null ) {

			if(config!=null && config.customModel!=null){
				bb = AxisAlignedBB.getBoundingBox(
					xCoord + config.customModel.model_Bounding_x1,
					yCoord + config.customModel.model_Bounding_y1,
					zCoord + config.customModel.model_Bounding_z1,
					xCoord + config.customModel.model_Bounding_x2,
					yCoord + config.customModel.model_Bounding_y2,
					zCoord + config.customModel.model_Bounding_z2
				);
			}
			else {
				bb = AxisAlignedBB.getBoundingBox(
					xCoord,
					yCoord,
					zCoord,
					xCoord,
					yCoord,
					zCoord
				);
			}
		}

		return bb;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public double getMaxRenderDistanceSquared() {
		return 65536.0D;
	}

	@Override
	public FluidTank[] getAllTanks() {

		FluidTank[] all = new FluidTank[inputTanks.length + outputTanks.length];

		for(int i = 0; i < inputTanks.length; i++) all[i] = inputTanks[i];
		for(int i = 0; i < outputTanks.length; i++) all[inputTanks.length + i] = outputTanks[i];

		return all;
	}

	@Override
	public FluidTank[] getSendingTanks() {
		FluidTank[] all = new FluidTank[outputTanks.length + this.getSmokeTanks().length];
		for(int i = 0; i < outputTanks.length; i++) all[i] = outputTanks[i];
		for(int i = 0; i < this.getSmokeTanks().length; i++) all[outputTanks.length + i] = this.getSmokeTanks()[i];
		//return outputTanks != null ? outputTanks : new FluidTank[0];
		return all;
	}

	@Override
	public FluidTank[] getReceivingTanks() {
		return inputTanks != null ? inputTanks : new FluidTank[0];
	}

	@Override
	public Container provideContainer(int ID, EntityPlayer player, World world, int x, int y, int z) {
		if(this.config == null) return null;
		return new ContainerMachineCustom(player.inventory, this);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public Object provideGUI(int ID, EntityPlayer player, World world, int x, int y, int z) {
		if(this.config == null) return null;
		return new GUIMachineCustom(player.inventory, this);
	}

	@Override
	public boolean hasPermission(EntityPlayer player) { return player.getDistanceSq(xCoord + 0.5, yCoord + 0.5, zCoord + 0.5) <= 256; }

	@Override
	public void receiveControl(EntityPlayer player, NBTTagCompound nbt) {
		if(nbt.hasKey("clearMaterials")) {
			clearMaterialsWithTool(player);
		}
	}

	private void clearMaterialsWithTool(EntityPlayer player) {
		if(!hasMaterialSupport() || materials.isEmpty()) return;
		ItemStack held = player.inventory.getItemStack();
		if(!isMaterialClearTool(held)) return;

		for(MaterialStack ms : materials) {
			if(ms.amount <= 0) continue;
			ItemStack scrap = ItemScraps.create(ms);
			if(!player.inventory.addItemStackToInventory(scrap)) {
				player.dropPlayerItemWithRandomChoice(scrap, true);
			}
		}
		materials.clear();
		markDirty();
	}

	private boolean isMaterialClearTool(ItemStack stack) {
		if(stack == null) return false;
		Item item = stack.getItem();
		if(item instanceof ItemTool && ((ItemTool) item).getToolClasses(stack).contains("shovel")) return true;
		if(item == ModItems.smashing_hammer) return true;
		if(item == ModItems.centri_stick) return true;
		if(ToolType.getType(stack) == ToolType.HAND_DRILL) return true;
		if(item instanceof IDepthRockTool) return true;
		return false;
	}

	@Override
	public long getPower() {
		return this.power;
	}

	@Override
	public long getMaxPower() {
		return this.config != null ? this.config.maxPower : 1;
	}

	@Override
	public void setPower(long power) {
		this.power = power;
	}

	@Override
	public long transferPower(long power) {
		if(this.config != null && this.config.generatorMode) return power;

		this.setPower(this.getPower() + power);

		if(this.getPower() > this.getMaxPower()) {

			long overshoot = this.getPower() - this.getMaxPower();
			this.setPower(this.getMaxPower());
			return overshoot;
		}

		return 0;
	}

	@Override
	public long getReceiverSpeed() {
		if(this.config != null && !this.config.generatorMode) return this.getMaxPower();
		return 0;
	}

	@Override
	public long getProviderSpeed() {
		if(this.config != null && this.config.generatorMode) return this.getMaxPower();
		return 0;
	}
}
