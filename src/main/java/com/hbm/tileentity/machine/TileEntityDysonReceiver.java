package com.hbm.tileentity.machine;

import java.util.List;

import com.hbm.blocks.BlockDummyable;
import com.hbm.dim.trait.CBT_Dyson;
import com.hbm.explosion.vanillant.ExplosionVNT;
import com.hbm.explosion.vanillant.standard.BlockAllocatorStandard;
import com.hbm.explosion.vanillant.standard.BlockMutatorFire;
import com.hbm.explosion.vanillant.standard.BlockProcessorStandard;
import com.hbm.explosion.vanillant.standard.CustomDamageHandlerDyson;
import com.hbm.explosion.vanillant.standard.EntityProcessorStandard;
import com.hbm.explosion.vanillant.standard.ExplosionEffectStandard;
import com.hbm.explosion.vanillant.standard.PlayerProcessorStandard;
import com.hbm.items.ISatChip;
import com.hbm.main.MainRegistry;
import com.hbm.saveddata.SatelliteSavedData;
import com.hbm.saveddata.satellites.Satellite;
import com.hbm.saveddata.satellites.SatelliteDysonRelay;
import com.hbm.sound.AudioWrapper;
import com.hbm.tileentity.IDysonConverter;
import com.hbm.tileentity.TileEntityMachineBase;
import com.hbm.util.BobMathUtil;
import com.hbm.util.ParticleUtil;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;
import net.minecraft.block.Block;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.EnumSkyBlock;
import net.minecraftforge.common.util.ForgeDirection;

public class TileEntityDysonReceiver extends TileEntityMachineBase {

	// Connects to a dyson swarm via ID, receiving energy during the day
	// also receives energy at night if a satellite relay is in orbit around the planet

	// The energy received is fired as a violently powerful beam,
	// converters can collect this beam and turn it into HE/TU or used for analysis, crafting, etc.

	public boolean isReceiving;
	public int swarmId;
	public int swarmCount;
	public int swarmConsumers;
	public int beamLength;

	private AudioWrapper audio;

	public TileEntityDysonReceiver() {
		super(1);
	}

	// Sun luminosity is 4*10^26, which we can't represent in any Java integer primitive
	// therefore the upper bound for power generation is higher than a FEnSU, effectively
	// reality doesn't provide any interesting solutions that make the system engaging to use
	// so we're going to build our own power curve.
	// We need to encourage players to build large swarms, so single satellites must suck but together they produce enormous power
	// Gompertz is a funne name
	public static long getEnergyOutput(int swarmCount) {
		double adjustedDensity = (double)swarmCount / 1024.0D;
		long maxOutput = Long.MAX_VALUE / 10;
		double b = 32.0D;
		double c = 1.3D;
		double gompertz = Math.exp(-b * Math.exp(-c * adjustedDensity));
		return (long)(maxOutput * gompertz) / 20;
	}

	@Override
	public String getName() {
		return "container.machineDysonReceiver";
	}

	@Override
	public void updateEntity() {
		ForgeDirection dir = ForgeDirection.getOrientation(this.getBlockMetadata() - BlockDummyable.offset).getOpposite();

		if(!worldObj.isRemote) {
			swarmId = ISatChip.getFreqS(slots[0]);

			SatelliteSavedData data = SatelliteSavedData.getData(worldObj, xCoord, zCoord);
			Satellite sat = data.getSatFromFreq(swarmId);
			int sun = worldObj.getSavedLightValue(EnumSkyBlock.Sky, xCoord, yCoord, zCoord) - worldObj.skylightSubtracted - 11;

			boolean occluded = false;
			for(int x = -3; x <= 3; x++) {
				for(int z = -3; z <= 3; z++) {
					if(worldObj.getHeightValue(xCoord + x, zCoord + z) > yCoord + 10) {
						occluded = true;
						break;
					}
				}
			}

			swarmCount = CBT_Dyson.count(worldObj, swarmId);
			swarmConsumers = CBT_Dyson.consumers(worldObj, swarmId);

			isReceiving = (sat instanceof SatelliteDysonRelay || sun > 0) && swarmId > 0 && !occluded && swarmCount > 0 && swarmConsumers > 0;

			if(isReceiving) {
				long energyOutput = getEnergyOutput(swarmCount) / swarmConsumers;
				int maxLength = 24;

				beamLength = maxLength;
				for(int i = 9; i < maxLength; i++) {
					int x = xCoord + dir.offsetX * i;
					int y = yCoord + 1;
					int z = zCoord + dir.offsetZ * i;

					Block block = worldObj.getBlock(x, y, z);

					// two block gap minimum
					boolean detonate = true;
					TileEntity te = null;
					if(i > 10) {
						if(block instanceof BlockDummyable) {
							int[] pos = ((BlockDummyable) block).findCore(worldObj, x, y, z);
							if(pos != null) {
								te = worldObj.getTileEntity(pos[0], pos[1], pos[2]);
							}
						} else {
							te = worldObj.getTileEntity(x, y, z);
						}

						if(te instanceof IDysonConverter) {
							detonate = !((IDysonConverter) te).provideEnergy(x, y, z, energyOutput);
						}
					}

					if(block.isOpaqueCube() || te != null) {
						if(detonate) {
							worldObj.setBlockToAir(x, y, z);

							ExplosionVNT vnt = new ExplosionVNT(worldObj, x, y, z, 3, null);
							vnt.setBlockAllocator(new BlockAllocatorStandard());
							vnt.setBlockProcessor(new BlockProcessorStandard().withBlockEffect(new BlockMutatorFire()));
							vnt.setEntityProcessor(new EntityProcessorStandard().allowSelfDamage());
							vnt.setPlayerProcessor(new PlayerProcessorStandard());
							vnt.setSFX(new ExplosionEffectStandard());
							vnt.explode();
						}

						beamLength = i;
						break;
					}
				}


				double blx = Math.min(xCoord, xCoord + dir.offsetX * beamLength) + 0.2;
				double bux = Math.max(xCoord, xCoord + dir.offsetX * beamLength) + 0.8;
				double bly = Math.min(yCoord, 1 + yCoord + dir.offsetY * beamLength) + 0.2;
				double buy = Math.max(yCoord, 1 + yCoord + dir.offsetY * beamLength) + 0.8;
				double blz = Math.min(zCoord, zCoord + dir.offsetZ * beamLength) + 0.2;
				double buz = Math.max(zCoord, zCoord + dir.offsetZ * beamLength) + 0.8;

				@SuppressWarnings("unchecked")
				List<EntityLivingBase> list = worldObj.getEntitiesWithinAABB(EntityLivingBase.class, AxisAlignedBB.getBoundingBox(blx, bly, blz, bux, buy, buz));

				for(EntityLivingBase entity : list) {
					ExplosionVNT vnt = new ExplosionVNT(worldObj, entity.posX - dir.offsetX, entity.posY + 1.5, entity.posZ - dir.offsetZ, 3, null);
					vnt.setBlockAllocator(new BlockAllocatorStandard());
					vnt.setBlockProcessor(new BlockProcessorStandard().withBlockEffect(new BlockMutatorFire()));
					vnt.setEntityProcessor(new EntityProcessorStandard().withDamageMod(new CustomDamageHandlerDyson(energyOutput)));
					vnt.setPlayerProcessor(new PlayerProcessorStandard());
					vnt.setSFX(new ExplosionEffectStandard());
					vnt.explode();
				}
			}

			networkPackNT(250);
		} else {
			if(isReceiving) {
				if(audio == null) {
					audio = MainRegistry.proxy.getLoopedSound("hbm:block.dysonBeam", xCoord + dir.offsetX * 8, yCoord, zCoord + dir.offsetZ * 8, 0.75F, 20F, 1.0F, 20);
					audio.startSound();
				}

				audio.keepAlive();
				audio.updatePitch(0.85F);

				if(worldObj.rand.nextInt(10) == 0) {
					ParticleUtil.spawnFlare(worldObj, xCoord - 5 + worldObj.rand.nextDouble() * 10, yCoord + 11, zCoord - 5 + worldObj.rand.nextDouble() * 10, 0, 0.1 + worldObj.rand.nextFloat() * 0.1, 0, 4F + worldObj.rand.nextFloat() * 2);
				}
			} else {
				if(audio != null) {
					audio.stopSound();
					audio = null;
				}
			}
		}
	}

	@Override
	public void serialize(ByteBuf buf) {
		super.serialize(buf);
		buf.writeBoolean(isReceiving);
		buf.writeInt(swarmId);
		buf.writeInt(swarmCount);
		buf.writeInt(swarmConsumers);
		buf.writeInt(beamLength);
	}

	@Override
	public void deserialize(ByteBuf buf) {
		super.deserialize(buf);
		isReceiving = buf.readBoolean();
		swarmId = buf.readInt();
		swarmCount = buf.readInt();
		swarmConsumers = buf.readInt();
		beamLength = buf.readInt();
	}

	@Override
	public void onChunkUnload() {
		super.onChunkUnload();

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

	AxisAlignedBB bb = null;

	@Override
	public AxisAlignedBB getRenderBoundingBox() {

		if(bb == null) {
			bb = AxisAlignedBB.getBoundingBox(
				xCoord - 25,
				yCoord,
				zCoord - 25,
				xCoord + 25,
				yCoord + 19,
				zCoord + 25
			);
		}

		return bb;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public double getMaxRenderDistanceSquared() {
		return 65536.0D;
	}

	public static void runTests() {
		for(int i = 1; i < 5000; i *= 2) {
			MainRegistry.logger.info(i + " dyson swarm members produces: " + BobMathUtil.getShortNumber(getEnergyOutput(i) * 20) + "HE/s");
		}
	}

}
