package com.hbm.tileentity.machine;

import com.hbm.main.MainRegistry;
import com.hbm.sound.AudioWrapper;
import com.hbm.tileentity.TileEntityLoadedBase;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.util.AxisAlignedBB;

public class TileEntityMachineIntake extends TileEntityLoadedBase {

	public float fan = 0;
	public float prevFan = 0;
	private AudioWrapper audio;

	public TileEntityMachineIntake() {

	}

	@Override
	public void updateEntity() {

		if(!worldObj.isRemote) {

		} else {

			this.prevFan = this.fan;

			this.fan += 45;

			if(this.fan >= 360) {
				this.fan -= 360;
				this.prevFan -= 360;
			}

			if(audio == null) {
				audio = createAudioLoop();
				audio.startSound();
			} else if(!audio.isPlaying()) {
				audio = rebootAudio(audio);
			}

			audio.keepAlive();
			audio.updateVolume(this.getVolume(0.25F));
		}
	}

	@Override public AudioWrapper createAudioLoop() {
		return MainRegistry.proxy.getLoopedSound("hbm:block.motor", xCoord, yCoord, zCoord, 0.25F, 10F, 1.0F, 20);
	}

	@Override public void onChunkUnload() {
		if(audio != null) { audio.stopSound(); audio = null; }
	}

	@Override public void invalidate() {
		super.invalidate();
		if(audio != null) { audio.stopSound(); audio = null; }
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
					yCoord + 1,
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
