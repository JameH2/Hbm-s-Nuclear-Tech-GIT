package com.hbm.tileentity.machine;

import java.util.List;

import com.hbm.blocks.BlockDummyable;
import com.hbm.dim.SolarSystem;
import com.hbm.handler.atmosphere.IBlockSealable;
import com.hbm.main.MainRegistry;
import com.hbm.sound.AudioWrapper;
import com.hbm.tileentity.machine.albion.TileEntityCooledBase;
import com.hbm.tileentity.machine.fusion.IFusionPowerReceiver;
import com.hbm.uninos.GenNode;
import com.hbm.uninos.UniNodespace;
import com.hbm.uninos.networkproviders.PlasmaNetworkProvider;
import com.hbm.util.BobMathUtil;
import com.hbm.util.fauxpointtwelve.BlockPos;
import com.hbm.util.fauxpointtwelve.DirPos;

import api.hbm.tile.IPropulsion;
import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

public class TileEntityMachineHTRNeo extends TileEntityCooledBase implements IPropulsion, IFusionPowerReceiver, IBlockSealable {

	// i smushed these together because i need you so bad
	protected GenNode plasmaNode;

	public long plasmaEnergy;
	public long plasmaEnergySync;

	public static long maxPower = 200_000_000L;

	public static final int COOLANT_USE = 50;
	
	public float rotor;
	public float prevRotor;
	public float rotorSpeed;
	private float soundtime;
	private AudioWrapper audio;

	private boolean hasRegistered;
	public boolean isOn;
	public int fuelCost;
	public float thrustAmount;

	public float plasmaR;
	public float plasmaG;
	public float plasmaB;

	public TileEntityMachineHTRNeo() {
		super(0);
	}

	@Override
	public void updateEntity() {

		if(!worldObj.isRemote) {
			if(!hasRegistered) {
				if(isFacingPrograde()) registerPropulsion();
				hasRegistered = true;
				isOn = false;
			}

			for(DirPos pos : this.getConPos()) {
				this.trySubscribe(worldObj, pos);
				this.trySubscribe(coolantTanks[0].getTankType(), worldObj, pos);
				this.tryProvide(coolantTanks[1], worldObj, pos);
			}

			plasmaEnergySync = plasmaEnergy;

			this.temperature += this.temp_passive_heating;
			if(this.temperature > KELVIN + 20) this.temperature = KELVIN + 20;

			if(this.temperature > this.temperature_target) {
				int cyclesTemp = (int) Math.ceil((Math.min(this.temperature - temperature_target, temp_change_max)) / temp_change_per_mb);
				int cyclesCool = coolantTanks[0].getFill();
				int cyclesHot = coolantTanks[1].getMaxFill() - coolantTanks[1].getFill();
				int cycles = BobMathUtil.min(cyclesTemp, cyclesCool, cyclesHot);

				coolantTanks[0].setFill(coolantTanks[0].getFill() - cycles);
				coolantTanks[1].setFill(coolantTanks[1].getFill() + cycles);
				this.temperature -= this.temp_change_per_mb * cycles;
			}

			if(isOn) {
				soundtime++;

				if(soundtime == 1) {
					this.worldObj.playSoundEffect(this.xCoord, this.yCoord, this.zCoord, "hbm:misc.htrfstart", 1.5F, 1F);
				} else if(soundtime > 30) {
					soundtime = 30;
				}
			} else {
				soundtime--;

				if(soundtime == 29) {
					this.worldObj.playSoundEffect(this.xCoord, this.yCoord, this.zCoord, "hbm:misc.htrstop", 2.0F, 1F);
				} else if(soundtime <= 0) {
					soundtime = 0;
				}
			}


			// nodespace setup
			if(plasmaNode == null || plasmaNode.expired) {
				ForgeDirection dir = ForgeDirection.getOrientation(this.getBlockMetadata() - BlockDummyable.offset).getRotation(ForgeDirection.UP);
				plasmaNode = UniNodespace.getNode(worldObj, xCoord + dir.offsetX * -10, yCoord, zCoord + dir.offsetZ * -10, PlasmaNetworkProvider.THE_PROVIDER);

				if(plasmaNode == null) {
					plasmaNode = new GenNode(PlasmaNetworkProvider.THE_PROVIDER,
							new BlockPos(xCoord + dir.offsetX * -10, yCoord, zCoord + dir.offsetZ * -10))
							.setConnections(new DirPos(xCoord + dir.offsetX * -11, yCoord, zCoord + dir.offsetZ * -11, dir));

					UniNodespace.createNode(worldObj, plasmaNode);
				}
			}

			if(plasmaNode != null && plasmaNode.hasValidNet()) plasmaNode.net.addReceiver(this);


			this.networkPackNT(200);
			this.plasmaEnergy = 0;
		} else {
			
			if(power >= maxPower || isOn) this.rotorSpeed += 0.125F;
			else this.rotorSpeed -= 0.125F;

			this.rotorSpeed = MathHelper.clamp_float(this.rotorSpeed, 0F, 15F);

			this.prevRotor = this.rotor;
			this.rotor += this.rotorSpeed;

			if(this.rotor >= 360F) {
				this.rotor -= 360F;
				this.prevRotor -= 360F;
			}

			if(isOn) {
				if(soundtime > 28) {
					if(audio == null) {
						audio = createAudioLoop();
						audio.startSound();
					} else if(!audio.isPlaying()) {
						audio = rebootAudio(audio);
					}

					audio.updateVolume(getVolume(1F));
					audio.keepAlive();
				}
				
				thrustAmount += 0.01D;
				if(thrustAmount > 1) thrustAmount = 1;
			} else {
				if(audio != null) {
					audio.stopSound();
					audio = null;
				}
				
				thrustAmount -= 0.01D;
				if(thrustAmount < 0) thrustAmount = 0;
			}
		}
	}

	@Override
	public AudioWrapper createAudioLoop() {
		return MainRegistry.proxy.getLoopedSound("hbm:misc.htrloop", xCoord, yCoord, zCoord, 0.25F, 27.5F, 1.0F, 20);
	}

	@Override
	public void onChunkUnload() {
		super.onChunkUnload();

		if(hasRegistered) {
			unregisterPropulsion();
			hasRegistered = false;
		}
	}

	@Override
	public void invalidate() {
		super.invalidate();

		if(hasRegistered) {
			unregisterPropulsion();
			hasRegistered = false;
		}

		if(!worldObj.isRemote) {
			if(this.plasmaNode != null) UniNodespace.destroyNode(worldObj, plasmaNode);
		}
	}

	@Override
	public boolean receivesFusionPower() {
		return true;
	}

	@Override
	public void receiveFusionPower(long fusionPower, double neutronPower, float r, float g, float b) {
		plasmaEnergy = fusionPower;
		// genuinely useful method refactor
		plasmaR = r;
		plasmaG = g;
		plasmaB = b;
	}

	@Override
	public DirPos[] getConPos() {
		ForgeDirection dir = ForgeDirection.getOrientation(this.getBlockMetadata() - BlockDummyable.offset);
		ForgeDirection rot = dir.getRotation(ForgeDirection.UP);

		return new DirPos[] {
			new DirPos(xCoord - rot.offsetX * 5 - dir.offsetX * 3, yCoord - 2, zCoord - rot.offsetZ * 5 - dir.offsetZ * 3, dir.getOpposite()),
			new DirPos(xCoord - rot.offsetX * -1 - dir.offsetX * 3, yCoord - 2, zCoord - rot.offsetZ * -1 - dir.offsetZ * 3, dir.getOpposite()),
			new DirPos(xCoord - rot.offsetX * 5 - dir.offsetX * -3, yCoord - 2, zCoord - rot.offsetZ * 5 - dir.offsetZ * -3, dir),
			new DirPos(xCoord - rot.offsetX * -1 - dir.offsetX * -3, yCoord - 2, zCoord - rot.offsetZ * -1 - dir.offsetZ * -3, dir),
		};
	}

	@Override
	public boolean canPerformBurn(int shipMass, double deltaV) {
		fuelCost = SolarSystem.getFuelCost(deltaV, shipMass, 250); // i think this engine *itself* would have a base ISP..?

		if(plasmaEnergySync < fuelCost) return false;
		if(power < maxPower) return false;
		if(!isCool()) return false;

		return true;
	}

	@Override
	public void addErrors(List<String> errors) {
		if(plasmaEnergySync < fuelCost) {
			errors.add(EnumChatFormatting.RED + "Insufficient plasma energy: needs " + BobMathUtil.getShortNumber(fuelCost) + " TU");
		}

		if(power < maxPower) {
			errors.add(EnumChatFormatting.RED + "Insufficient power");
		}

		if(!isCool()) {
			errors.add(EnumChatFormatting.RED + "Coolant loop not operational!");
		}
	}

	@Override
	public float getThrust() {
		return 1_600_000_000.0F;
	}

	@Override
	public int startBurn() {
		isOn = true;
		power = 0;

		return 180;
	}

	@Override
	public int endBurn() {
		isOn = false;
		return 180;
	}

	@Override
	public long getMaxPower() {
		return maxPower;
	}

	/*
	 * -------------------------
	 * NBT / Sync
	 * -------------------------
	 */

	@Override
	public void serialize(ByteBuf buf) {
		super.serialize(buf);

		buf.writeLong(plasmaEnergySync);
		buf.writeBoolean(isOn);
		buf.writeInt(fuelCost);
		buf.writeFloat(soundtime);

		buf.writeFloat(plasmaR);
		buf.writeFloat(plasmaG);
		buf.writeFloat(plasmaB);
	}

	@Override
	public void deserialize(ByteBuf buf) {
		super.deserialize(buf);

		plasmaEnergy = buf.readLong();
		isOn = buf.readBoolean();
		fuelCost = buf.readInt();
		soundtime = buf.readFloat();

		plasmaR = buf.readFloat();
		plasmaG = buf.readFloat();
		plasmaB = buf.readFloat();
	}

	@Override
	public void writeToNBT(NBTTagCompound nbt) {
		super.writeToNBT(nbt);

		nbt.setLong("plasma", plasmaEnergy);
		nbt.setBoolean("on", isOn);
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);

		plasmaEnergy = nbt.getLong("plasma");
		isOn = nbt.getBoolean("on");
	}

	@Override
	public TileEntity getTileEntity() {
		return this;
	}

	AxisAlignedBB bb = null;

	@Override
	public AxisAlignedBB getRenderBoundingBox() {
		if(bb == null)
			bb = AxisAlignedBB.getBoundingBox(
				xCoord - 11,
				yCoord - 2,
				zCoord - 11,
				xCoord + 12,
				yCoord + 3,
				zCoord + 12
			);

		return bb;
	}

	@Override
	public String getName() {
		return "container.htrfneo";
	}

	public boolean isFacingPrograde() {
		return ForgeDirection.getOrientation(this.getBlockMetadata() - BlockDummyable.offset) == ForgeDirection.SOUTH;
	}

	@Override
	public boolean isFtlCapable() {
		return false; //true actually
	}
	public boolean isSealed(World world, int x, int y, int z) {
		return true;
	}

}
