package com.hbm.saveddata.satellites;

import com.hbm.dim.CelestialBody;
import com.hbm.items.ModItems;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.IChatComponent;
import net.minecraft.world.World;
import net.minecraftforge.client.model.IModelCustom;

public class SatelliteWar extends SatelliteBase {

	//time to clean up this shit and make it PROPER.

	public SatelliteWar() {

	}

	public long lastOp;
	public float interp;
	public int cooldown;

	public void writeToNBT(NBTTagCompound nbt) {
		super.writeToNBT(nbt);
		nbt.setLong("lastOp", lastOp);
	}

	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);
		lastOp = nbt.getLong("lastOp");
	}

	public void onClick(World world, int x, int z) {

	}

	public void fire() {


	}

	public void setTarget(CelestialBody body) {

	}

	public void fireAtTarget(CelestialBody body) {

	}

	public void playsound() {
		Minecraft.getMinecraft().thePlayer.playSound("hbm:misc.fireflash", 10F, 1F);
	}

	public int magSize() {
		return 0;
	}

	@Override
	public void serialize(ByteBuf buf) {
		super.serialize(buf);
		buf.writeFloat(interp);

	}

	@Override
	public void deserialize(ByteBuf buf) {
		super.deserialize(buf);
		this.interp = buf.readFloat();
	}

	public IModelCustom getModel() {
		return null;
	}

	@Override
	public String getType() {
		return "WAR";
	}

	@Override
	public IChatComponent[] getInfo(World world) {
		return new IChatComponent[] {
			new ChatComponentTranslation(ModItems.sat_war.getUnlocalizedName(new ItemStack(ModItems.sat_war)) + ".name")
		};
	}

}
